

package driver;

import guiTrace.JavisTrace;

import java.util.Vector;

import jist.runtime.JistAPI;
import jist.swans.Constants;
import jist.swans.field.Fading;
import jist.swans.field.Field;
import jist.swans.field.Mobility;
import jist.swans.field.PathLoss;
import jist.swans.field.Placement;
import jist.swans.field.Spatial;
import jist.swans.mac.Mac802_11;
import jist.swans.mac.MacAddress;
import jist.swans.misc.Location;
import jist.swans.misc.Mapper;
import jist.swans.misc.Message;
import jist.swans.misc.MessageBytes;
import jist.swans.misc.Util;
import jist.swans.net.NetAddress;
import jist.swans.net.NetIp;
import jist.swans.net.NetMessage;
import jist.swans.net.PacketLoss;
import jist.swans.radio.RadioInfo;
import jist.swans.radio.RadioNoise;
import jist.swans.radio.RadioNoiseImprovedIndep;
import jist.swans.route.RouteAodv;
import jist.swans.route.RouteInterface;
import jist.swans.trans.TransUdp;


public class aodv
{

	/** Default port number to send and receive packets. */
	private static final int PORT = 3001;
	/** Routing protocol to use. */
	private static int protocol = Constants.NET_PROTOCOL_AODV;
	/** Number of nodes. */
	private static int nodes = 20;
	/** Field dimensions (in meters). */
	private static Location.Location2D field = new Location.Location2D(1500, 300);
	/** Node mobility model. */
	private static String mobilityString="waypoint";
	private static int mobilityModel = Constants.MOBILITY_WAYPOINT;
	/** Node mobility options. */
	private static String mobilityOpts = "0:1:1:20";
	/** Packet loss options. */
	private static String lossOpts = "0.2";
	/** Number of messages sent per minute per node. */
	private static double sendRate = 4*60.0;
	/** Start of sending (seconds). */
	private static int startTime = 10;
	/** Number of seconds to send messages. */
	private static int duration = 900;
	/** Number of seconds after messages stop sending to end simulation. */
	private static int resolutionTime = 10; 
	/** SNR limite ambiente noise */
	private static double limiteSNR = 10;


	public static void addNode( int i, Vector routers, 
			Field field, Placement place, RadioInfo.RadioInfoShared radioInfo, Mapper protMap,
			PacketLoss inLoss, PacketLoss outLoss)
	{
		// radio
		RadioNoiseImprovedIndep r = new RadioNoiseImprovedIndep(i, radioInfo);
		r.setThresholdSNR(limiteSNR);
		RadioNoise radio =r; 

		// mac
		Mac802_11 mac = new Mac802_11(new MacAddress(i), radio.getRadioInfo());

		// network
		final NetAddress address = new NetAddress(i);
		NetIp net = new NetIp(address, protMap, inLoss, outLoss, field.getTrace());

		// routing
		RouteInterface route = null;
		switch(protocol)
		{
		case Constants.NET_PROTOCOL_AODV:
			RouteAodv aodv = new RouteAodv(address);
			aodv.setNetEntity(net.getProxy());
			aodv.getProxy().start();      
			route = aodv.getProxy();
			routers.add(aodv);
			// statistics
			//aodv.setStats(stats);
			break;
		default:
			throw new RuntimeException("invalid routing protocol");
		}

		// transport
		TransUdp udp = new TransUdp();

		// placement
		Location location = place.getNextLocation();
		field.addRadio(radio.getRadioInfo(), radio.getProxy(), location);
		field.startMobility(radio.getRadioInfo().getUnique().getID());

		// node entity hookup
		radio.setFieldEntity(field.getProxy());
		radio.setMacEntity(mac.getProxy());
		byte intId = net.addInterface(mac.getProxy());
		net.setRouting(route);
		mac.setRadioEntity(radio.getProxy());
		mac.setNetEntity(net.getProxy(), intId);
		udp.setNetEntity(net.getProxy());
		net.setProtocolHandler(Constants.NET_PROTOCOL_UDP, udp.getProxy());
		net.setProtocolHandler(protocol, route);
	}  //method: addNode


	private static Field buildField(final Vector routers)
	{
		// initialize node mobility model
		Mobility mobility = null;
		switch(mobilityModel)
		{
		case Constants.MOBILITY_WAYPOINT:
			mobility = new Mobility.RandomWaypoint(field, mobilityOpts);
			break;
		case Constants.MOBILITY_UNIFORM_CIRCLE:
			mobility = new Mobility.UniformCircular(field, mobilityOpts,nodes);
			break;
		case Constants.MOBILITY_UNIFORM_RECT:
			mobility = null;//new Mobility.Uniforme(field, mobilityOpts,nodes);
			break;
		case Constants.MOBILITY_UNIFORM_RECT_NOMANDE:
			mobility = new Mobility.GrupoUniforme(field, mobilityOpts);
			break;
		default:
			throw new RuntimeException("unknown node mobility model");
		}
		// initialize spatial binning
		Spatial spatial = null;
		spatial = new Spatial.LinearList(field);


		Field field = new Field(spatial, new Fading.Rayleigh(), new PathLoss.TwoRay(), 
				mobility, Constants.PROPAGATION_LIMIT_DEFAULT);
		// initialize shared radio information
		RadioInfo.RadioInfoShared radioInfo = RadioInfo.createShared(
				Constants.FREQUENCY_DEFAULT,
				Constants.BANDWIDTH_DEFAULT,
				Constants.TRANSMIT_DEFAULT,
				Constants.GAIN_DEFAULT,
				Util.fromDB(Constants.SENSITIVITY_DEFAULT),
				Util.fromDB(Constants.THRESHOLD_DEFAULT),
				Constants.TEMPERATURE_DEFAULT,
				Constants.TEMPERATURE_FACTOR_DEFAULT,
				Constants.AMBIENT_NOISE_DEFAULT);

		// initialize shared protocol mapper
		Mapper protMap = new Mapper(new int[] { Constants.NET_PROTOCOL_UDP, protocol, });
		// initialize packet loss models
		PacketLoss outLoss = new PacketLoss.Uniform(Double.parseDouble(lossOpts));
		PacketLoss inLoss = new PacketLoss.Uniform(Double.parseDouble(lossOpts));
		// initialize node placement model
		Placement place =  new Placement.Random(aodv.field);

		JavisTrace.createTraceSetTrace(field,"aodvsim_"+nodes+"_"+mobilityString+"Snr_"+limiteSNR+"_NodeSim");



		// create each node
		for (int i=1; i<=nodes; i++)
		{
			addNode( i, routers,  field, place, radioInfo, protMap, inLoss, outLoss);
		}


		JavisTrace.drawGuiTrace(field);



		// set up message sending events
		JistAPI.sleep(startTime*Constants.SECOND);
		//System.out.println("clear stats at t="+JistAPI.getTimeString());
	//	stats.clear();//1300/6
		int numTotalMessages = (int)Math.floor(((double)sendRate/60) * nodes * duration);
		long delayInterval = (long)Math.ceil((double)duration * (double)Constants.SECOND / (double)numTotalMessages);
		for(int i=0; i<numTotalMessages; i++)
		{
			//pick random send node
			int srcIdx = Constants.random.nextInt(routers.size());
			int destIdx;
			do
			{
				//pick random dest node
				destIdx = Constants.random.nextInt(routers.size());
			} while (destIdx == srcIdx);
			RouteAodv srcAodv = (RouteAodv)routers.elementAt(srcIdx);
			RouteAodv destAodv = (RouteAodv)routers.elementAt(destIdx);

			Message m = new MessageBytes(new byte[64]);

			TransUdp.UdpMessage udpMsg = new TransUdp.UdpMessage(PORT, PORT, m);

			NetMessage msg = new NetMessage.Ip(udpMsg, srcAodv.getLocalAddr(), destAodv.getLocalAddr(), 
					Constants.NET_PROTOCOL_UDP, Constants.NET_PRIORITY_NORMAL, Constants.TTL_DEFAULT);
			srcAodv.getProxy().send(msg);
			//stats
			
			JistAPI.sleep(delayInterval);
		}

		return field;
	} // buildField

	public static void main(String[] args)
	{

		 long endTime = startTime+duration+resolutionTime;
	      if(endTime>0)
	      {
	        JistAPI.endAt(endTime*Constants.SECOND);
	      }	
		final Vector routers = new Vector();
		buildField(routers);

		JistAPI.runAt(new Runnable()
		{
			public void run()
			{
				//WAIT A LITTLE FILE WRITE IS COMPLETE
				long filewait = 15;
				try {
					Thread.sleep(filewait);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}, JistAPI.END);
	}

}
