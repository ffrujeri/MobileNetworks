/*
 * Created on 20/10/2005
 * @author Gabi Kliot
 */

package guiTrace;

import jist.swans.Constants;

/**
 * A trace event representing a link between 2 nodes.
 * Nodes are represented by their radioId.
 */
public class LinkTraceEvent extends TraceEvent
{
    private Integer srcId;
    private Integer dstId;
    
    /**
     * @param name
     */
    public LinkTraceEvent() {
        super(TraceEvent.LinkTraceEvent);
    }

    public LinkTraceEvent(Integer srcId, Integer dstId) {
        super(TraceEvent.LinkTraceEvent);
        this.srcId = srcId;
        this.dstId = dstId;
    }
    
    /* (non-Javadoc)
     * @see guiTrace.TraceEvent#showYourself()
     */
    // l -t * -s 1 -d 11 -S UP -r 500000 -D 0.0080 
    public String showYourself() {
        String s = "";
        

        int z = (int)(1000000000.0*(double)(getTime()/(float)Constants.SECOND));
        //System.out.println("Sayi: "+ z);
        double v =(double) z / 1000000000.0;
        //System.out.println("Sayi2: "+ v);
        
        
        // Current implementation of Javis does not support -t
        s += "l -t " + v  + " ";
        //s += "l -t " + "*" + " ";
        s += "-s " + srcId + " ";
        s += "-d " + dstId + " ";
        return s;
    }
}
