package guiTrace;
import jist.swans.Constants;

public class HopTraceEvent extends TraceEvent {
	
	private String sourceID;
	private String destinationID;
	private Short packetType;
	private Integer sizeOfPacket;
	private Integer packetAttribute=0; //used as color id
	private Short packetID;
	
    public HopTraceEvent() {
        super(TraceEvent.HopTraceEvent);
    }
	
    public HopTraceEvent(String SrcID, String destID,Short pcktType, Integer sizeOfPacket, Short pcktID)
    {
    	super (TraceEvent.HopTraceEvent);
    	this.sourceID =SrcID;
    	this.destinationID =destID;
    	this.packetType = pcktType;
    	this.sizeOfPacket = sizeOfPacket;
    	this.packetID = pcktID;
    }
	@Override
	public String showYourself() {

		String s="";
	        s += "h -t " + getTime()/(float)Constants.SECOND + " ";
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
