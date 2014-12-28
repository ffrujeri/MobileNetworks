package guiTrace;

import jist.swans.Constants;
/**
* A trace event representing a receive event: receive of a packet at the final destination.
*/
public class ReceiveTraceEvent extends TraceEvent {

	//private Integer radioId;
	private String sourceID;
	private String destinationID;
	private Short packetType;
	private Integer sizeOfPacket;
	private Integer packetAttribute=0; //used as color id
	private Short packetID;
	
    public ReceiveTraceEvent() {
        super(TraceEvent.ReceiveTraceEvent);
    }
	
    public ReceiveTraceEvent(String SrcID, String destID,Short pcktType, Integer sizeOfPacket, Short pcktID)
    {
    	super (TraceEvent.ReceiveTraceEvent);
    	this.sourceID =SrcID;
    	this.destinationID =destID;
    	this.packetType = pcktType;
    	this.sizeOfPacket = sizeOfPacket;
    	this.packetID = pcktID;
    }
	@Override
	public String showYourself() {
		// TODO Auto-generated method stub
		
		String s="";
	        s += "r -t " + getTime()/(float)Constants.SECOND + " ";
	        //s += "n -t " + "*" + " ";
	        s += "-s " + this.sourceID + " ";
	        s += "-d " + this.destinationID + " ";
	        s += "-p " + getProtocolName(new Integer(this.packetType)) +" ";
	        s += "-e " + this.sizeOfPacket+ " ";
	        s += "-i " + this.packetID + " ";
	        //s += "-c " + "2 ";
	        s += "-a " + this.packetAttribute + " ";
	 
		return s;
	}

}
