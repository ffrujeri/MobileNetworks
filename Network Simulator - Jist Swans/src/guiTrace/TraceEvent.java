/*
 * Created on 20/10/2005
 * @author Gabi Kliot
 */

package guiTrace;

import jist.runtime.JistAPI;
import jist.swans.Constants;
/**
 * Abstract class representing a single event of the gui trace.
 */
public abstract class TraceEvent {

    public static final String NodeTraceEvent = "NodeEvent";
    public static final String LinkTraceEvent = "LinkEvent";
    public static final String HopTraceEvent = "HopEvent";
    public static final String ReceiveTraceEvent = "ReceiveEvent";
    public static final String EnqueueTraceEvent = "EnqueueEvent";
    public static final String DequeueTraceEvent = "DequeueEvent";
    public static final String LinkDropTraceEvent = "LinkDropEvent";
    public static final String QueueDropTraceEvent = "QueueDropEvent";

    
    // The event name, e.g. 'NodeEvent'
    protected String m_name;

    // The time at which the event occured
    protected long m_time;


    public TraceEvent(String name) {
      m_name=name;
      m_time = JistAPI.getTime();
    }

    public TraceEvent(String name, long time) {
      m_name=name;
      m_time=time;
    }

    public void setTime(long time) {
      m_time=time;
    }

    public long getTime() {
      return m_time;
    }

    public String getName() {
        return m_name;
    }
    
    public String getProtocolName(Integer pid)
    {
    	if(pid == Constants.NET_PROTOCOL_AODV)
    	{	
    		return "aodv";
    		//return "message";
    	}
    	else if(pid == Constants.NET_PROTOCOL_DSR)
    	{	
    		return "dsr";
    	}
    	else if(pid == Constants.NET_PROTOCOL_INVALID)
    	{	
    		return "INVALID TYPE";
    	}
    	else if(pid == Constants.NET_PROTOCOL_TCP)
    	{	
    		return "tcp";
    	}
    	else if(pid == Constants.NET_PROTOCOL_UDP)
    	{	
    		return "udp";
    		//return "message";
    	}
    	else if(pid == Constants.NET_PROTOCOL_ZRP)
    	{	
    		return "zrp";
    	}
    	else if(pid == Constants.NET_PROTOCOL_HEARTBEAT)
    	{	
    		return "heartbeat";
    	}
    	else if(pid == Constants.NET_PROTOCOL_BELLMANFORD)
    	{	
    		return "BF";
    	}
    	else
    	{
    		return null;
    	}
    	
    }

    /**
     * To be implemented by an actual event, according to
     * the syntax of the trace event 
     * (in case of Javis trace - accoirding to NAM syntax) 
     */
    public abstract String showYourself();
    
}
