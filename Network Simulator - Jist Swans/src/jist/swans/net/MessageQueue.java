//////////////////////////////////////////////////
// JIST (Java In Simulation Time) Project
// Timestamp: <MessageQueue.java Tue 2004/04/06 11:32:34 barr pompom.cs.cornell.edu>
//

// Copyright (C) 2004 by Cornell University
// All rights reserved.
// Refer to LICENSE for terms and conditions of use.

package jist.swans.net;

import guiTrace.DequeueTraceEvent;
import guiTrace.EnqueueTraceEvent;
import guiTrace.Trace;

import java.io.IOException;

/**
 * Implements a prioritized queue of items. Items are dequeued in order of
 * their priority, where a lower value priority comes first in the queue.
 * Priority values should be in the range [0, priorities), where
 * <code>priorities</code> is the queue initialization parameter.
 *
 * @author Rimon Barr &lt;barr+jist@cs.cornell.edu&gt;
 * @version $Id: MessageQueue.java,v 1.8 2004/04/06 16:07:49 barr Exp $
 * @since SWANS1.0
 */

public interface MessageQueue
{

  /**
   * Return whether queue is empty.
   *
   * @return whether queue is empty
   */
  public boolean isEmpty();

  /**
   * Return whether the queue is filled to capacity.
   *
   * @return whether queue is filled to capacity
   */
  public boolean isFull();

  /**
   * Return number of items in the queue.
   *
   * @return number of items in the queue
   */
  public int size();

  /**
   * Insert message into queue at end (with lowest priority).
   *
   * @param msg message to insert
   */
  public void insert(QueuedMessage msg);

  /**
   * Return first message, but do not dequeue.
   *
   * @return first message (not dequeued)
   */
  public QueuedMessage get();

  /**
   * Return first message and dequeue.
   *
   * @return first message in queue
   */
  public QueuedMessage remove();

  //////////////////////////////////////////////////
  // priority
  //

  /**
   * Insert message into queue with given priority.
   *
   * @param msg message to insert
   * @param pri message priority
   */
  public void insert(QueuedMessage msg, int pri);

  /**
   * Return priority of first queued message.
   *
   * @return priority of first queued message
   */
  public int getPri();

  /**
   * Return first message of given priority, but do not dequeue.
   *
   * @param pri priority of message requested
   * @return message of given priority (not dequeued), or null if
   *   no such message exists
   */
  public QueuedMessage get(int pri);

  /**
   * Return first message with given priority and dequeue.
   *
   * @param pri priority of message requested
   * @return message of given priority
   */
  public QueuedMessage remove(int pri);

  public Trace getTrace();
  
  public void setTrace(Trace trace);
  
  public NetAddress getLocalAddr() ;
  public void setLocalAddr(NetAddress localAddr);
  
  //////////////////////////////////////////////////
  // IMPLEMENTATIONS
  //

  //////////////////////////////////////////////////
  // NoDropMessage
  //

  public class NoDropMessageQueue implements MessageQueue
  {

	  /** local network address. */
	protected NetAddress localAddr;
	  
    /**
     * Heads of message queues for different priorities.
     */
    private QueuedMessage[] heads; 

    /**
     * Tails of message queues for different priorities.
     */
    private QueuedMessage[] tails;

    /**
     * Index of highest priority.
     */
    private int topPri;

    /**
     * Length of list.
     */
    private int size;

    /**
     * List size limit.
     */
    private int capacity;
    
    /** trace object for this Network Interface. */
   
    private Trace trace;

    /**
     * Initialize prioritized message queue.
     *
     * @param priorities number of priority levels
     * @param capacity maximum number of items allowed in list
     */
    public NoDropMessageQueue(int priorities, int capacity)
    {
      heads = new QueuedMessage[priorities];
      tails = new QueuedMessage[priorities];
      topPri = heads.length;
      size = 0;
      this.capacity = capacity;

    }

    /**
     * Return whether list is empty.
     *
     * @return whether list is empty
     */
    public boolean isEmpty()
    {
      return size==0;
    }

    /**
     * Return whether the list is filled to capacity.
     *
     * @return whether list is filled to capacity
     */
    public boolean isFull()
    {
      return size==capacity;
    }

    /**
     * Return number of items in the list.
     *
     * @return number of items in the list
     */
    public int size()
    {
      return size;
    }

    /**
     * Insert message into queue with given priority.
     *
     * @param msg message to insert
     * @param pri message priority
     */
    public void insert(QueuedMessage msg, int pri)
    {
      if(size==capacity)
      {
        throw new IndexOutOfBoundsException("list maximum exceeded");
      }
      
      //ENTER QUEUE EVENT OCCURS HERE!
      
      //String s = this.getLocalAddr().getIP().getHostName().substring(6);
  	   
    	String destid = msg.getNextHop().toString();
    	
    	Short packettype ;
	    Integer packetSize ;
		Short packetid ;
		 String s ;
		 String srcid;
    	if (destid.equalsIgnoreCase("ANY"))
    	{
    		destid ="-1";
    	}
    	else
    	{
    		//destid = destid.substring(6);
    	}
    	
    	if(msg.getPayload() instanceof NetMessage.Ip)
    	{
    		NetMessage.Ip ipmsg = (NetMessage.Ip) msg.getPayload(); 
    		//s = ipmsg.getSrc().getIP().getHostName().substring(6);
    	    s = s = this.getLocalAddr().getIP().getHostName().substring(6);
    		srcid = s;
    		packettype =ipmsg.getProtocol();
    	    packetSize = ipmsg.getSize();
    		packetid = ipmsg.getId();
    	}
    	else
    	{
    		    packettype = null;
        	    packetSize = null;
        		packetid = null;
        		srcid = null;
    	}
    	//GUI-SUPPORT for SWANS
      	try {
      		trace.handleEvent(new EnqueueTraceEvent(srcid, destid,packettype,packetSize,packetid));
      		//System.out.println("Enqueue packet in the source queue : "+srcid+" for dest "+destid);
        	
      	}catch (IOException e) {
      		// TODO Auto-generated catch block
      		e.printStackTrace();
      	}
      	//END- GUI SUPPRT for SWANS
      
      
      
      size++;
      topPri = (byte)Math.min(pri, topPri);
      QueuedMessage tail = tails[pri];
      if(tail==null)
      {
        heads[pri] = msg;
        tails[pri] = msg;
      }
      else
      {
        tail.next = msg;
        tails[pri] = msg;
      }
    }

    /**
     * Insert message into queue at end (with lowest priority).
     *
     * @param msg message to insert
     */
    public void insert(QueuedMessage msg)
    {
      insert(msg, heads.length-1);
    }

    /**
     * Return priority of first queued message.
     *
     * @return priority of first queued message
     */
    public int getPri()
    {
      while(heads[topPri]==null) topPri++;
      return topPri;
    }

    /**
     * Return first message of given priority, but do not dequeue.
     *
     * @param pri priority of message requested
     * @return message of given priority (not dequeued), or null if
     *   no such message exists
     */
    public QueuedMessage get(int pri)
    {
      return heads[pri];
    }

    /**
     * Return first message, but do not dequeue.
     *
     * @return first message (not dequeued)
     */
    public QueuedMessage get()
    {
      return heads[getPri()];
    }

    /**
     * Return first message with given priority and dequeue.
     *
     * @param pri priority of message requested
     * @return message of given priority
     */
    public QueuedMessage remove(int pri)
    {
     
    	
      QueuedMessage msg = heads[pri];
      
      

      //REMOVE QUEUE EVENT OCCURS HERE!
      
      //String s = this.getLocalAddr().getIP().getHostName().substring(6);
  	  String s ;
  	  String srcid ;
    	String destid = msg.getNextHop().toString();
    	
    	Short packettype ;
	    Integer packetSize ;
		Short packetid ;
    	if (destid.equalsIgnoreCase("ANY"))
    	{
    		destid ="-1";
    	}
    	else
    	{
    		//destid = destid.substring(6);
    	}
    	
    	if(msg.getPayload() instanceof NetMessage.Ip)
    	{
    		NetMessage.Ip ipmsg = (NetMessage.Ip) msg.getPayload(); 
    		//s = ipmsg.getSrc().getIP().getHostName().substring(6);
    		s = this.getLocalAddr().getIP().getHostName().substring(6);
    		srcid = s;
    		
    		packettype =ipmsg.getProtocol();
    	    packetSize = ipmsg.getSize();
    		packetid = ipmsg.getId();
    	}
    	else
    	{
    		    packettype = null;
        	    packetSize = null;
        		packetid = null;
        		srcid = null;
    	}
    	//GUI-SUPPORT for SWANS
      	try {
      		trace.handleEvent(new DequeueTraceEvent(srcid, destid,packettype,packetSize,packetid));
      		//System.out.println("Dequeue packet in the source queue : "+srcid+" for dest "+destid);
        	
      	}catch (IOException e) {
      		// TODO Auto-generated catch block
      		e.printStackTrace();
      	}
      	//END- GUI SUPPRT for SWANS
      
      
      
      
      
      heads[pri] = msg.next;
      if(msg.next==null)
      {
        tails[pri] = null;
      }
      else
      {
        msg.next = null;
      }
      size--;
      
      
      /*
      // GUI-SUPPORT for SWANS
    	try {
    		trace.handleEvent(new HopTraceEvent(srcid, destid,packettype,packetSize,packetid));
    		//System.out.println("Hop packet in the source queue : "+srcid+" for dest "+destid);
      	
    	}catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	//END- GUI SUPPRT for SWANS
    
      */
      
      return msg;
    }

    /**
     * Return first message and dequeue.
     *
     * @return first message in queue
     */
    public QueuedMessage remove()
    {
      return remove(getPri());
    }

	public Trace getTrace() {
		return trace;
	}

	public void setTrace(Trace trace) {
		this.trace = trace;
	}

	public NetAddress getLocalAddr() {
		return localAddr;
	}

	public void setLocalAddr(NetAddress localAddr) {
		this.localAddr = localAddr;
	}

  }

} // class: MessageQueue
