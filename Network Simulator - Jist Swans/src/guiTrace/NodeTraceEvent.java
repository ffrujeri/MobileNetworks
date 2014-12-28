/*
 * Created on 20/10/2005
 * @author Gabi Kliot
 */

package guiTrace;

import jist.swans.Constants;
import jist.swans.misc.Location;

/**
 * A trace event representing a single node: radioId and its location in the Field
 */
public class NodeTraceEvent extends TraceEvent
{
    private Location 	loc;
    private Integer 	radioId;
    

    public NodeTraceEvent() {
        super(TraceEvent.NodeTraceEvent);
    }

    public NodeTraceEvent(Location loc, Integer radioId) {
        super(TraceEvent.NodeTraceEvent);
        this.loc = loc;
        this.radioId = radioId;
    }
    
    /* (non-Javadoc)
     * @see guiTrace.TraceEvent#showYourself()
     */
    //n -t * -a 20 -s 20 -S UP -v circle -c black -x 496.80377 -y 207.64095 
    public String showYourself() {
        String s = "";
        // Current implementation of Javis does not support -t
        //System.out.printf("%.3f\n",(double)(getTime()/(float)Constants.SECOND));
        
        
        //int z = (int)(100000.0*(double)(getTime()/(float)Constants.SECOND));
        //System.out.println("Sayi: "+ z);
        //double v =(double) z / 100000.0;
        //System.out.println("Sayi2: "+ v);
        
        s += "n -t " + getTime()/(float)Constants.SECOND + " ";
        //s += "n -t " + "*" + " ";
        //s += "-a " + radioId + " ";
        s += "-s " + radioId + " ";
        s += "-S UP" + " ";
        if(radioId>0 && radioId<=8)
        	s += "-v circle -c red" + " ";
        else
        	s += "-v circle -c blue" + " ";	
        s += "-x " + loc.getX() + " ";
        s += "-y " + loc.getY() + " ";
        s += "-z "+4;
        return s;
    }
}
