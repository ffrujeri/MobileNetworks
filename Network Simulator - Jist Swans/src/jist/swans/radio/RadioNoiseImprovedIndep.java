//////////////////////////////////////////////////
// JIST (Java In Simulation Time) Project
// Timestamp: <RadioNoiseCapture.java Tue 2004/04/20 09:00:20 gabik at cs.technion.ac.il>
//

// Copyright (C) 2004 by Cornell University
// All rights reserved.
// Refer to LICENSE for terms and conditions of use.

package jist.swans.radio;

import jist.runtime.JistAPI;
import jist.swans.Constants;
import jist.swans.Main;
import jist.swans.misc.Message;
import jist.swans.misc.Util;

/** 
 * <code>RadioNoiseImprovedIndep</code> is a fixed version of RadioNoiseIndep.
 * It does not use Additive Noise and is very similar to RadioNoiseIndep.
 * However, it fixes a number of bugs in RadioNoiseIndep.
 * More specificaly, it causes a collision of two packets, if they are both above sensitivity_mW
 * but not SNR bigger than each other.
 * The RadioNoiseIndep caused only the second packet not to be accepted correctly, while not colliding the first packet.
 *
 * @author Gabi Kliot &lt;gabik@cs.technion.ac.il&rt;
 * @version $Id: RadioNoiseImprovedIndep.java,v 1.1 2008/07/18 18:27:16 gabik Exp $
 * @since SWANS1.6
 */

public final class RadioNoiseImprovedIndep extends RadioNoise
{
  //////////////////////////////////////////////////
  // locals
  //

  /**
   * threshold signal-to-noise ratio.
   */
  protected double thresholdSNR;

  //////////////////////////////////////////////////
  // initialize
  //

  /**
   * Create new radio with independent noise model.
   *
   * @param id radio identifier
   * @param sharedInfo shared radio properties
   */
  public RadioNoiseImprovedIndep(int id, RadioInfo.RadioInfoShared sharedInfo)
  {
    this(id, sharedInfo, Constants.SNR_THRESHOLD_DEFAULT);
  }

  /**
   * Create new radio with independent noise model.
   *
   * @param id radio identifier
   * @param sharedInfo shared radio properties
   * @param thresholdSNR threshold signal-to-noise ratio
   */
  public RadioNoiseImprovedIndep(int id, RadioInfo.RadioInfoShared sharedInfo, double thresholdSNR)
  {
    super(id, sharedInfo);
    this.thresholdSNR = thresholdSNR;
  }

  //////////////////////////////////////////////////
  // reception
  //

  // RadioInterface interface
  /** {@inheritDoc} */
  public void receive(Message msg, Double powerObj_mW, Long durationObj)
  {
	final double power_mW = powerObj_mW.doubleValue();
	final long duration = durationObj.longValue();
		
    // This is a week noise - ignore if below sensitivity
    if(power_mW < radioInfo.shared.sensitivity_mW) return;
    
    // This is a strong noise - discard message if below threshold.
    // Old version
	//if(power_mW < radioInfo.shared.threshold_mW) msg = null;

	// New fix by Rimon - 80 meters
	if(power_mW < radioInfo.shared.threshold_mW 
	        || power_mW < radioInfo.shared.background_mW * thresholdSNR) msg = null;
        
    switch(mode)
    {
      case Constants.RADIO_MODE_IDLE:
		// FIX ONE:
		// goto receiving mode in any case (even if strong noise)!
		// This is the first difference between RadioNoiseImprovedIndep and RadioNoiseIndep
		// Strong noise should not leave us in the IDLE state, but put in a receive mode, 
		// since we need to take this noise into account for next packet (later, this msg will be ignored).
        
    	//if(msg!=null) // was in RadioNoiseIndep
    	{
        	setMode(Constants.RADIO_MODE_RECEIVING);
        }
        lockSignal(msg, power_mW, duration);
        break;
      case Constants.RADIO_MODE_RECEIVING:
    	if(Main.ASSERT) Util.assertion(signals>0);
		// if new signal is stronger then old by SNR - we lock on the new signal
		// if prev signal is stronger then new by SNR - we stay locked on prev signal
		// if they are both at the same power level - need to collide them both.
		// collision is done by setting the signalBuffer to null. This is THE major fix here.
		
		if(power_mW >= radioInfo.shared.threshold_mW
		    &&  power_mW > signalPower_mW*thresholdSNR) // signalPower_mW is previous signal strength
		{
		  // lock on the new signal
		  lockSignal(msg, power_mW, duration);
		}
        else if(signalPower_mW > power_mW*thresholdSNR) // signalPower_mW is previous signal strength
        {
        	// stay locked on prev signal
        }else{
        	// FIX TWO:
        	// collide them both - an old and a new one.
        	signalBuffer = null;
    		
        	// FIX THREE:
        	// The below code is a replica of NS2 code:
    		// Since a collision has occurred, figure out 
        	// which packet that caused the collision will
    		// "last" the longest.  Make this packet,
    		//  pktRx_ and reset the Recv Timer if necessary.
        	if((JistAPI.getTime() + duration) > signalFinish)
        	{
        		signalPower_mW = power_mW;
        		signalFinish = JistAPI.getTime() + duration;
        		// actualy, a more correct fix will pick the strongest amon the two signals 
        		// as long as this signal propogates. Butn this is quiet a complex fix and modelled correctly by RadioNoiseAdditive. 
        	}
        }
        break;
      case Constants.RADIO_MODE_TRANSMITTING:
        break;
      case Constants.RADIO_MODE_SLEEP:
        break;
      default:
        throw new RuntimeException("invalid radio mode: "+mode);
    }
    // increment number of incoming signals
    signals++;
    // schedule an endReceive
    JistAPI.sleep(duration); 
    self.endReceive(powerObj_mW);
  }
  
  // RadioInterface interface
  /** {@inheritDoc} */
  // This method is exactly like RadioNoiseIndep.endReceive()
  public void endReceive(final Double powerObj_mW)
  {
//    if(logger.isInfoEnabled())
//	{
//		logger.info("[" + JistAPI.getTime() + "] " + "(" + getRadioInfo().unique.getID() + ")" + " END receive. Radio at " + mode + " mode." + 
//		" signals=" + signals + " signalBuffer=" + signalBuffer + " " + JistAPI.getTime() + " " + signalFinish);
//	}
//	
    if(Main.ASSERT) Util.assertion(signals>0);
    signals--;

	if(mode==Constants.RADIO_MODE_SLEEP) return;
    if(mode==Constants.RADIO_MODE_RECEIVING)
    {
      if(signalBuffer!=null && JistAPI.getTime()==signalFinish)
      {
        this.macEntity.receive(signalBuffer);
        unlockSignal();
      }
      // WAS BEFORE: setMode(Constants.RADIO_MODE_IDLE);
	  // FIX: GK if signals>0 leave mode at receiving or transmitting  
	  if(signals==0) setMode(Constants.RADIO_MODE_IDLE);
    }
  }

public void setThresholdSNR(double limiteSNR) {
	// TODO Auto-generated method stub
	this.thresholdSNR = limiteSNR;
	
}

} // class: RadioNoiseImprovedInde