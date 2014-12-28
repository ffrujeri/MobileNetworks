//////////////////////////////////////////////////
// JIST (Java In Simulation Time) Project
// Timestamp: <aodvsim.java Tue 2004/04/06 11:57:32 barr pompom.cs.cornell.edu>
//

// Copyright (C) 2004 by Cornell University
// All rights reserved.
// Refer to LICENSE for terms and conditions of use.

package driver;

import guiTrace.JavisTrace;
import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.Option;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import jist.runtime.JistAPI;
import jist.swans.Constants;
import jist.swans.field.Fading;
import jist.swans.field.Field;
import jist.swans.field.Mobility;
import jist.swans.field.PathLoss;
import jist.swans.field.Placement;
import jist.swans.field.Spatial;
import jist.swans.mac.MacAddress;
import jist.swans.mac.MacDumb;
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


/**
 * AODV simulation.  Derived from bordercast
 * 
 * @author Clifton
 *
 */
public class aodvsim
{

  /** Default port number to send and receive packets. */
  private static final int PORT = 3001;
  private static final Option opt_snr = null;
  private static String mobilityString=null;
  private static int flag_x =0;
  private static int vezes = 1;
  private static int total_de_mensagens = 0;
  
  
  //////////////////////////////////////////////////
  // command-line options
  //
private static PrintStream statsfile;

  /** Simulation parameters with default values. */
  private static class CommandLineOptions
  {
    /** Whether to print a usage statement. */
    private boolean help = false;
    ///** Time to end simulation. */
    //private int endTime = -1;
    /** Routing protocol to use. */
    private int protocol = Constants.NET_PROTOCOL_AODV;
    /** Number of nodes. */
    private int nodes = 100;
    /** Field dimensions (in meters). */
    private Location.Location2D field = new Location.Location2D(1500, 300);
    /** Field wrap-around. */
    private boolean wrapField = false;
    /** Node placement model. */
    private int placement = Constants.PLACEMENT_RANDOM;
    /** Node placement options. */
    private String placementOpts = null;
    /** Node mobility model. */
    private int mobility = Constants.MOBILITY_STATIC;
    /** Node mobility options. */
    private String mobilityOpts = null;
    /** Packet loss model. */
    private int loss = Constants.NET_LOSS_NONE;
    /** Packet loss options. */
    private String lossOpts = "0.2";
    /** Number of messages sent per minute per node. */
    private double sendRate = 5; //antes era 30 - taxa maxima de envio de mensagens por minuto -
    /** Start of sending (seconds). */
    private int startTime = 10;
    /** Number of seconds to send messages. */
    private int duration = 1000;
    /** Number of seconds after messages stop sending to end simulation. */
    private int resolutionTime = 10; 
    /** Random seed. */
    private long seed = System.currentTimeMillis();
    /** Gui Support */
    private boolean guisupport = false;
    /** binning mode. */
    public int spatial_mode = Constants.SPATIAL_LINEAR;
    /** binning degree. */
    public int spatial_div = 5;
    
    private double limiteSNR = 10;
    
    
    
  } // class: CommandLineOptions

  /** Prints a usage statement. */
  private static void showUsage() 
  {
    System.out.println("Usage: java driver.aodvsim [options]");
    System.out.println();
    System.out.println("  -h, --help           print this message");
    System.out.println("  -n, --nodes          number of nodes: n [100] ");
    System.out.println("  -f, --field          field dimensions: x,y [100,100]");
    System.out.println("  -w, --wraparound     wrap the field around the edges:boolean [false],true");
    System.out.println("  -a, --arrange        placement model: [random],grid:ixj");
    System.out.println("  -m, --mobility       mobility: [static],waypoint:opts,teleport:p,walk:opts,direction:opts, boundless:opts");
    System.out.println("  -l, --loss           packet loss model: [none],uniform:p");
    System.out.println("  -s, --send rate      send rate per-minute: [1.0]");
    System.out.println("  -t, --timing         node activity timing: start,duration,resolution [60,3600,30]");
    System.out.println("  -r, --randomseed     random seed: [0]");
    System.out.println("  -x, --guisupport     gui support: [true, false]");
    System.out.println();
    System.out.println("New Mobility Models Usages: ");
    System.out.println(" --> Boundless Simulation Area: -m boundless:velocityMax:accelerationMax:deltaT:maximumAngularChange");
    System.out.println(" --> Random Direction :         -m direction:Velocity:pauseTime");
    System.out.println();
    System.out.println("e.g.");
    System.out.println("  swans driver.aodvsim -n 25 -f 2000x2000 -a grid:5x5 -t 10,600,60 -x false");
    System.out.println();
  }

  /**
   * Parses command-line arguments.
   *
   * @param args command-line arguments
   * @return parsed command-line options
   * @throws CmdLineParser.OptionException if the command-line arguments are not well-formed.
   */
  private static CommandLineOptions parseCommandLineOptions(String[] args)
    throws CmdLineParser.OptionException
  {
    if(args.length==0)
    {
      args = new String[] { "-h" };
    }
    CmdLineParser parser = new CmdLineParser();
    CmdLineParser.Option opt_help = parser.addBooleanOption('h', "help");
    CmdLineParser.Option opt_nodes = parser.addIntegerOption('n', "nodes");
    CmdLineParser.Option opt_field = parser.addStringOption('f', "field");
    CmdLineParser.Option opt_wrapField = parser.addStringOption('w', "wraparound");
    CmdLineParser.Option opt_placement = parser.addStringOption('a', "arrange");
    CmdLineParser.Option opt_mobility = parser.addStringOption('m', "mobility");
    CmdLineParser.Option opt_loss = parser.addStringOption('l', "loss");
    CmdLineParser.Option opt_rate = parser.addDoubleOption('s', "send rate");
    CmdLineParser.Option opt_timing = parser.addStringOption('t', "timing");
    CmdLineParser.Option opt_randseed = parser.addIntegerOption('r', "randomseed");
    CmdLineParser.Option opt_guisupport = parser.addStringOption('x', "guisupport");
    CmdLineParser.Option opt_ = parser.addStringOption('z', "");
    
    parser.parse(args);

    CommandLineOptions cmdOpts = new CommandLineOptions();
    // help
    if(parser.getOptionValue(opt_help) != null)
    {
      cmdOpts.help = true;
    }
    
    
    if(parser.getOptionValue(opt_)!=null)
    	
    	//System.out.print(parser.getOptionValue(opt_));
    	cmdOpts.limiteSNR = new Double(parser.getOptionValue(opt_snr).toString());
    //Wrap -around Field
    
    if(parser.getOptionValue(opt_wrapField) != null)
    {
      String wrap=(String)parser.getOptionValue(opt_wrapField);
      //System.out.println(wrap);
      if(wrap.equalsIgnoreCase("true"))
      {
    	  cmdOpts.wrapField=true;
      }
      else
      	{
    	  cmdOpts.wrapField=false;
    	}
    }
    
    //// endat
    //if (parser.getOptionValue(opt_endat) != null)
    //{
    //  cmdOpts.endTime = ((Integer)parser.getOptionValue(opt_endat)).intValue();
    //}
    // nodes
    if(parser.getOptionValue(opt_nodes) != null)
    {
      cmdOpts.nodes = ((Integer)parser.getOptionValue(opt_nodes)).intValue();
    }
    // field
    if(parser.getOptionValue(opt_field) != null)
    {
      cmdOpts.field = (Location.Location2D)Location.parse((String)parser.getOptionValue(opt_field));
    }
    // placement
    if(parser.getOptionValue(opt_placement) != null)
    {
      String placementString = ((String)parser.getOptionValue(opt_placement)).split(":")[0];
      if(placementString!=null)
      {
    	  if(placementString.equalsIgnoreCase("random"))
    	  {
    		  cmdOpts.placement = Constants.PLACEMENT_RANDOM;
    	  }else if(placementString.equalsIgnoreCase("random_circle"))
    	  {
    		  cmdOpts.placement = Constants.PLACEMENT_RANDOM_CIRCLE;
    	  }

    	  else if(placementString.equalsIgnoreCase("grid"))
    	  {
    		  cmdOpts.placement = Constants.PLACEMENT_GRID;
    	  }
        else
        {
          throw new CmdLineParser.IllegalOptionValueException(opt_placement, "unrecognized placement model");
        }
      }
      cmdOpts.placementOpts = Util.stringJoin((String[])Util.rest(((String)parser.getOptionValue(opt_placement)).split(":")), ":");
    }
    // mobility
    if(parser.getOptionValue(opt_mobility)!=null)
    {
      mobilityString = ((String)parser.getOptionValue(opt_mobility)).split(":")[0];
      if(mobilityString!=null)
      {
        if(mobilityString.equalsIgnoreCase("static"))
        {
          cmdOpts.mobility = Constants.MOBILITY_STATIC;
        }
        else if(mobilityString.equalsIgnoreCase("waypoint"))
        {
          cmdOpts.mobility = Constants.MOBILITY_WAYPOINT;
        }
        else if(mobilityString.equalsIgnoreCase("teleport"))
        {
          cmdOpts.mobility = Constants.MOBILITY_TELEPORT;
        }
        else if(mobilityString.equalsIgnoreCase("walk"))
        {
          cmdOpts.mobility = Constants.MOBILITY_WALK;
        }
        else if(mobilityString.equalsIgnoreCase("uniform_circle"))
        {
          cmdOpts.mobility = Constants.MOBILITY_UNIFORM_CIRCLE;
        }
        else if(mobilityString.equalsIgnoreCase("uniform_rect"))
        {
          cmdOpts.mobility = Constants.MOBILITY_UNIFORM_RECT;
        }
        else if(mobilityString.equalsIgnoreCase("nomande_rect_uniform"))
        {
          cmdOpts.mobility = Constants.MOBILITY_UNIFORM_RECT_NOMANDE;
        }
        else if(mobilityString.equalsIgnoreCase("nomande_circle_uniform"))
        {
          cmdOpts.mobility = Constants.MOBILITY_UNIFORM_RECT_NOMANDE;
        }
        else if(mobilityString.equalsIgnoreCase("direction"))
        {
          cmdOpts.mobility = Constants.MOBILITY_DIRECTION;
        }
        else if(mobilityString.equalsIgnoreCase("gauss_markov"))
        {
          cmdOpts.mobility = Constants.MOBILITY_GAUSS_MARKOV;
        }
        else if(mobilityString.equalsIgnoreCase("boundless"))
        {
        	if(cmdOpts.wrapField==false)
        	{
        		throw new RuntimeException("Boundless Simulation Area Mobility Model needs a wraped-around field! Try to use (-w true) option!");
        	}
          cmdOpts.mobility = Constants.MOBILITY_BOUNDLESS_SIM_AREA;
        }
        else
        {
          throw new CmdLineParser.IllegalOptionValueException(opt_mobility, "unrecognized mobility model");
        }
      }
      cmdOpts.mobilityOpts = Util.stringJoin((String[])Util.rest(((String)parser.getOptionValue(opt_mobility)).split(":")), ":");
    }
    // loss
    if(parser.getOptionValue(opt_loss)!=null)
    {
      String lossString = ((String)parser.getOptionValue(opt_loss)).split(":")[0];
      if(lossString!=null)
      {
        if(lossString.equalsIgnoreCase("none"))
        {
          cmdOpts.loss = Constants.NET_LOSS_NONE;
        }
        
        else if(lossString.equalsIgnoreCase("uniform"))
        {
          cmdOpts.loss = Constants.NET_LOSS_UNIFORM;
        }
        else
        {
          throw new CmdLineParser.IllegalOptionValueException(opt_loss, "unrecognized mobility model");
        }
      }
      cmdOpts.lossOpts = Util.stringJoin((String[])Util.rest(((String)parser.getOptionValue(opt_loss)).split(":")), ":");
    }
    
    //// bordercast
    //if(parser.getOptionValue(opt_bordercast) != null)
    //{
    //  String[] data = ((String)parser.getOptionValue(opt_bordercast)).split(",");
    //  if(data.length!=3) throw new CmdLineParser.IllegalOptionValueException(opt_bordercast, "bad format: num,start,delay");
    //  cmdOpts.bordercasts = Integer.parseInt(data[0]);
    //  cmdOpts.bordercastStart = Integer.parseInt(data[1]);
    //  cmdOpts.bordercastDelay = Integer.parseInt(data[2]);
    //}
    
    //send rate
    if (parser.getOptionValue(opt_rate) != null)
    {
      cmdOpts.sendRate = ((Double)parser.getOptionValue(opt_rate)).doubleValue();      
    }
    
    //timing parameters
    if (parser.getOptionValue(opt_timing) != null)
    {
      String[] data = ((String)parser.getOptionValue(opt_timing)).split(",");
      if(data.length!=3) throw new CmdLineParser.IllegalOptionValueException(opt_timing, "bad format: start,duration,resolution");
      cmdOpts.startTime = Integer.parseInt(data[0]);      
      cmdOpts.duration = Integer.parseInt(data[1]);
      cmdOpts.resolutionTime = Integer.parseInt(data[2]);
    }
    
    // random seed
    if (parser.getOptionValue(opt_randseed) != null)
    {
      cmdOpts.seed = ((Integer)parser.getOptionValue(opt_randseed)).intValue();
    }
    
    //----Javis GUI SUPPORT
    if (parser.getOptionValue(opt_guisupport) != null)
    {
    
    	String support=(String)parser.getOptionValue(opt_guisupport);
        //System.out.println(wrap);
        if(support.equalsIgnoreCase("true"))
        {
      	  cmdOpts.guisupport=true;
        }
        else
        {
      	  cmdOpts.guisupport=false;
      	}
    	
    }
    
    
    return cmdOpts;

  } // parseCommandLineOptions


  //////////////////////////////////////////////////
  // simulation setup
  //

  /**
   * Add node to the field and start it.
   *
   * @param opts command-line options
   * @param i node number, which also serves as its address
   * @param routers list of zrp entities to be appended to
   * @param stats statistics collector
   * @param field simulation field
   * @param place node placement model
   * @param radioInfo shared radio information
   * @param protMap registered protocol map
   * @param inLoss packet incoming loss model
   * @param outLoss packet outgoing loss model
   */
  public static void addNode(CommandLineOptions opts, int i, Vector routers, RouteAodv.AodvStats stats,
      Field field, Placement place, RadioInfo.RadioInfoShared radioInfo, Mapper protMap,
      PacketLoss inLoss, PacketLoss outLoss)
  {
    // radio
	  RadioNoiseImprovedIndep r = new RadioNoiseImprovedIndep(i, radioInfo);
	 r.setThresholdSNR(opts.limiteSNR);
	 // r.setThreshold_Default=-72; // For example, THRESHOLD_DEFAULT=-72 gave me a transmit range of 220m, 
	 								// SENSITIVITY_DEFAULT=-75 gave sensing range of 310m 
	 								// (keeping  TRANSMIT_DEFAULT at 15.0)
    RadioNoise radio =r; 
    
    // mac
    MacDumb mac = new MacDumb(new MacAddress(i), radio.getRadioInfo());

    // network
    final NetAddress address = new NetAddress(i);
    NetIp net = new NetIp(address, protMap, inLoss, outLoss, field.getTrace());

    // routing
    RouteInterface route = null;
    switch(opts.protocol)
    {
      case Constants.NET_PROTOCOL_AODV:
        RouteAodv aodv = new RouteAodv(address);
        aodv.setNetEntity(net.getProxy());
        aodv.getProxy().start();      
        route = aodv.getProxy();
        routers.add(aodv);
        // statistics
        aodv.setStats(stats);
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
    net.setProtocolHandler(opts.protocol, route);
  }  //method: addNode




  /**
   * Constructs field and nodes with given command-line options, establishes
   * client/server pairs and starts them.
   *
   * @param opts command-line parameters
   * @param routers vectors to place zrp objects into
   * @param stats zrp statistics collection object
   */
  private static Field buildField(CommandLineOptions opts, final Vector routers, final RouteAodv.AodvStats stats)
  {
    // initialize node mobility model
    Mobility mobility = null;
    
    // setup para estabelecer raio de até 70m => Constants.Sensitivity = -62
    // alterar no arquivo Constants.java que esta em jist.swans
    
    switch(opts.mobility)
    {
      case Constants.MOBILITY_STATIC:
        mobility = new Mobility.Static();
        break;
      case Constants.MOBILITY_WAYPOINT:
        mobility = new Mobility.RandomWaypoint(opts.field, opts.mobilityOpts);
        break;
      case Constants.MOBILITY_TELEPORT:
        mobility = new Mobility.Teleport(opts.field, Long.parseLong(opts.mobilityOpts));
        break;
      case Constants.MOBILITY_WALK:
        mobility = new Mobility.RandomWalk(opts.field, opts.mobilityOpts);
        break;
      case Constants.MOBILITY_DIRECTION:
         mobility = new Mobility.RandomDirection(opts.field, opts.mobilityOpts);
         break;
      case Constants.MOBILITY_UNIFORM_CIRCLE:
          mobility = new Mobility.UniformCircular(opts.field, opts.mobilityOpts,opts.nodes);
          break;
      case Constants.MOBILITY_UNIFORM_RECT:
          mobility = new Mobility.Uniforme(opts.field, opts.mobilityOpts);
          break;
      case Constants.MOBILITY_UNIFORM_RECT_NOMANDE:
          mobility = new Mobility.GrupoUniforme(opts.field, opts.mobilityOpts);
          break;
      case Constants.MOBILITY_UNIFORM_CIRCLE_NOMANDE:
          mobility = new Mobility.NomadicCircular(opts.field, opts.mobilityOpts,opts.nodes);
          break;
      case Constants.MOBILITY_BOUNDLESS_SIM_AREA:
          mobility = new Mobility.BoundlessSimulationArea(opts.field, opts.mobilityOpts);
          break;
      case Constants.MOBILITY_GAUSS_MARKOV:
          mobility = new Mobility.GaussMarkov(opts.field, opts.mobilityOpts);
          break;
      
      default:
        throw new RuntimeException("unknown node mobility model");
    }
    // initialize spatial binning
    Spatial spatial = null;
    switch(opts.spatial_mode)
    {
      case Constants.SPATIAL_LINEAR:
        spatial = new Spatial.LinearList(opts.field);
        break;
      case Constants.SPATIAL_GRID:
        spatial = new Spatial.Grid(opts.field, opts.spatial_div);
        break;
      case Constants.SPATIAL_HIER:
        spatial = new Spatial.HierGrid(opts.field, opts.spatial_div);
        break;
      default:
        throw new RuntimeException("unknown spatial binning model");
    }
    if(opts.wrapField) spatial = new Spatial.TiledWraparound(spatial);
    // initialize field
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
    Mapper protMap = new Mapper(new int[] { Constants.NET_PROTOCOL_UDP, opts.protocol, });
    // initialize packet loss models
    PacketLoss outLoss = new PacketLoss.Zero();
    PacketLoss inLoss = null;
    switch(opts.loss)
    {
      case Constants.NET_LOSS_NONE:
        inLoss = new PacketLoss.Zero();
        break;
      case Constants.NET_LOSS_UNIFORM:
        inLoss = new PacketLoss.Uniform(Double.parseDouble(opts.lossOpts));
        break;
      default:
        throw new RuntimeException("unknown packet loss model");
    }
    // initialize node placement model
    Placement place = null;
    switch(opts.placement)
    {
      case Constants.PLACEMENT_RANDOM:
        place = new Placement.Random(opts.field);
        break;
      case Constants.PLACEMENT_GRID:
        place = new Placement.Grid(opts.field, opts.placementOpts);
        break;
      case Constants.PLACEMENT_RANDOM_CIRCLE:
          place = new Placement.RandomCircle(opts.field);
          break;
      default:
        throw new RuntimeException("unknown node placement model");
    }
    
    
    //If gui Support enabled, then draw trace. EMRE ATSAN
    if(opts.guisupport)
    {
    	// String numero = new Integer(vezes).toString();
    	
    	//JAVIS -GUI SUPPORT
    	//JavisTrace.createTraceSetTrace(field,"aodvsim_"+opts.nodes+"_"+mobilityString+"Snr_"+opts.limiteSNR+"_NodeSim");
    	JavisTrace.createTraceSetTrace(field,"n_"+opts.nodes+"_t_"+opts.duration+"_ex_");
    	
    	
    	FileOutputStream out; // declare a file output object
        // declare a print stream object

     
                // Create a new file output stream
                // connected to "myfile.txt"
                try {
					out = new FileOutputStream("n_"+opts.nodes+"_t_"+opts.duration+"_ex_stats_.txt");
					statsfile = new PrintStream( out );

                } catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

                // Connect print stream to the output stream
               
                
     
    	
    	//END OF CODE -for JAVIS GUI SUPPORT
   
    }
    
    
    // create each node
    for (int i=1; i<=opts.nodes; i++)
    {
      addNode(opts, i, routers, stats, field, place, radioInfo, protMap, inLoss, outLoss);
    }
    
    if(opts.guisupport)
    {
    	  //JAVIS -GUI SUPPORT
    	JavisTrace.drawGuiTrace(field);
    	//END OF CODE -for JAVIS GUI SUPPORT

   
    }
  
    
    
    // set up message sending events
    JistAPI.sleep(opts.startTime*Constants.SECOND);
    //System.out.println("clear stats at t="+JistAPI.getTimeString());
    stats.clear();//1300/6
    int numTotalMessages = (int)Math.floor(((double)opts.sendRate/60) * opts.nodes * opts.duration);
    long delayInterval = (long)Math.ceil((double)opts.duration * (double)Constants.SECOND / (double)numTotalMessages);
    total_de_mensagens  = numTotalMessages;
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
     // double end=JistAPI.getTime(),begin = JistAPI.getTime();
      srcAodv.getProxy().send(msg);
      //stats
      if (stats != null)
      {
        stats.netMsgs++;
       // end = JistAPI.getTime();
      }
     // System.err.println(end-begin);
      JistAPI.sleep(delayInterval);
    }

    return field;
  } // buildField


  /**
   * Display statistics at end of simulation.
   *
   * @param routers vectors to place zrp objects into
   * @param stats zrp statistics collection object
   */
  public static void showStats(Field field,Vector routers, RouteAodv.AodvStats stats, CommandLineOptions opt, Date startTime)
  {
    Date endTime = new Date();
    long elapsedTime = endTime.getTime() - startTime.getTime();
    FileOutputStream out; // declare a file output object
    // declare a print stream object

    

    
    
    System.err.println("-------------");   
    statsfile.println("-------------");
    System.err.println("Packet stats:");
    statsfile.println("Packet stats:");
    System.err.println("-------------");
    statsfile.println("-------------");
    
    System.err.println("Rreq packets sent = "+stats.send.rreqPackets);
    statsfile.println("Rreq packets sent = "+stats.send.rreqPackets);
    System.err.println("Rreq packets recv = "+stats.recv.rreqPackets);
    statsfile.println("Rreq packets recv = "+stats.recv.rreqPackets);
    
    System.err.println("Rrep packets sent = "+stats.send.rrepPackets);
    statsfile.println("Rrep packets sent = "+stats.send.rrepPackets);
    System.err.println("Rrep packets recv = "+stats.recv.rrepPackets);
    statsfile.println("Rrep packets recv = "+stats.recv.rrepPackets);
   
    System.err.println("Rerr packets sent = "+stats.send.rerrPackets);
    statsfile.println("Rerr packets sent = "+stats.send.rerrPackets);
    System.err.println("Rerr packets recv = "+stats.recv.rerrPackets);
    statsfile.println("Rerr packets recv = "+stats.recv.rerrPackets);
    
    System.err.println("Hello packets sent = "+stats.send.helloPackets);
    statsfile.println("Hello packets sent = "+stats.send.helloPackets);
    System.err.println("Hello packets recv = "+stats.recv.helloPackets);
    statsfile.println("Hello packets recv = "+stats.recv.helloPackets);
    
    System.err.println("Total aodv packets sent = "+stats.send.aodvPackets);
    statsfile.println("Total aodv packets sent = "+stats.send.aodvPackets);
    System.err.println("Total aodv packets recv = "+stats.recv.aodvPackets);
    statsfile.println("Total aodv packets recv = "+stats.recv.aodvPackets);
    
    
    System.err.println("Non-hello packets sent = "+(stats.send.aodvPackets - stats.send.helloPackets));
    statsfile.println("Non-hello packets sent = "+(stats.send.aodvPackets - stats.send.helloPackets));
    System.err.println("Non-hello packets recv = "+(stats.recv.aodvPackets - stats.recv.helloPackets));
    statsfile.println("Non-hello packets recv = "+(stats.recv.aodvPackets - stats.recv.helloPackets));
    
    System.err.println("--------------");
    statsfile.println("--------------");
    System.err.println("Overall stats:");
    statsfile.println("Overall stats:");
    System.err.println("--------------");
    statsfile.println("--------------");
    System.err.println("Messages to deliver = "+stats.netMsgs);
    statsfile.println("Messages to deliver = "+stats.netMsgs);
    System.err.println("Route requests      = "+stats.rreqOrig);
    statsfile.println("Route requests      = "+stats.rreqOrig);
    System.err.println("Route replies       = "+stats.rrepOrig);
    statsfile.println("Route replies       = "+stats.rrepOrig);
    System.err.println("Routes added        = "+stats.rreqSucc);
    statsfile.println("Routes added        = "+stats.rreqSucc);
    
    System.err.println();
    statsfile.println();
    System.gc();
    System.err.println("freemem:  "+Runtime.getRuntime().freeMemory());
    statsfile.println("freemem:  "+Runtime.getRuntime().freeMemory());
    System.err.println("maxmem:   "+Runtime.getRuntime().maxMemory());
    statsfile.println("maxmem:   "+Runtime.getRuntime().maxMemory());
    System.err.println("totalmem: "+Runtime.getRuntime().totalMemory());
    statsfile.println("totalmem: "+Runtime.getRuntime().totalMemory());
    long usedMem = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
    System.err.println("used:     "+usedMem);
    statsfile.println("used:     "+usedMem);

    System.err.println("start time  : "+startTime);
    statsfile.println("start time  : "+startTime);
    System.err.println("end time    : "+endTime);
    statsfile.println("end time    : "+endTime);
    System.err.println("elapsed time: "+elapsedTime);
    statsfile.println("elapsed time: "+elapsedTime);
    System.err.flush();
    statsfile.flush();

    System.out.println(opt.nodes+"\t"
      +stats.send.rreqPackets+"\t"
      +stats.recv.rreqPackets+"\t"
      +stats.send.rrepPackets+"\t"
      +stats.recv.rrepPackets+"\t"
      +stats.send.rerrPackets+"\t"
      +stats.recv.rerrPackets+"\t"
      +stats.send.helloPackets+"\t"
      +stats.recv.helloPackets+"\t"
      +stats.send.aodvPackets+"\t"
      +stats.recv.aodvPackets+"\t"
      +(stats.send.aodvPackets - stats.send.helloPackets)+"\t"
      +(stats.recv.aodvPackets - stats.recv.helloPackets)+"\t"
      +usedMem+"\t"
      +elapsedTime);
    
    statsfile.println(opt.nodes+"\t"
    	      +stats.send.rreqPackets+"\t"
    	      +stats.recv.rreqPackets+"\t"
    	      +stats.send.rrepPackets+"\t"
    	      +stats.recv.rrepPackets+"\t"
    	      +stats.send.rerrPackets+"\t"
    	      +stats.recv.rerrPackets+"\t"
    	      +stats.send.helloPackets+"\t"
    	      +stats.recv.helloPackets+"\t"
    	      +stats.send.aodvPackets+"\t"
    	      +stats.recv.aodvPackets+"\t"
    	      +(stats.send.aodvPackets - stats.send.helloPackets)+"\t"
    	      +(stats.recv.aodvPackets - stats.recv.helloPackets)+"\t"
    	      +usedMem+"\t"
    	      +elapsedTime);
        
    //clear memory
    routers = null;
    stats = null;
    
    System.out.println("Average density = " + field.computeDensity()  + "/m^2");
    statsfile.println("Average density = " + field.computeDensity()  + "/m^2");
    System.out.println("Average sensing = " + field.computeAvgConnectivity(true));
    statsfile.println("Average sensing = " + field.computeAvgConnectivity(true));
    System.out.println("Average receive = " + field.computeAvgConnectivity(false));
    statsfile.println("Average receive = " + field.computeAvgConnectivity(false));
    statsfile.close();
    if (flag_x==0){
    		System.out.println("PROPAGATION_LIMIT_DEFAULT: "+Constants.PROPAGATION_LIMIT_DEFAULT);
    		//System.out.println("RADIUS_DEFAULT: "+Constants.RADIUS_DEFAULT);
    		System.out.println("FREQUENCY_DEFAULT: "+ Constants.FREQUENCY_DEFAULT);
    		System.out.println("BANDWIDTH_DEFAULT: "+  Constants.BANDWIDTH_DEFAULT);
    		System.out.println("TRANSMIT_DEFAULT: "+  Constants.TRANSMIT_DEFAULT);
    		System.out.println("GAIN_DEFAULT: "+Constants.GAIN_DEFAULT);
    		System.out.println("SENSITIVITY_DEFAULT: "+Constants.SENSITIVITY_DEFAULT);
    		System.out.println("THRESHOLD_DEFAULT: "+Constants.THRESHOLD_DEFAULT);
    	    Util.fromDB(Constants.SENSITIVITY_DEFAULT);
    	    Util.fromDB(Constants.THRESHOLD_DEFAULT);
    	    System.out.println("TEMPERATURE_DEFAULT: "+Constants.TEMPERATURE_DEFAULT);
    	    System.out.println("TEMPERATURE_FACTOR_DEFAULT: "+Constants.TEMPERATURE_FACTOR_DEFAULT);
    	    System.out.println("AMBIENT_NOISE_DEFAULT: "+Constants.AMBIENT_NOISE_DEFAULT);
    	    flag_x = 1;
    }
    // calcular metricas e gerar mapa inicial (antes das movimentações)
    // gerar matriz de adjacencia
    // calcular metricas a partir dela
    // gerar arquivo para o pajek (mapa inicial)
    
    // abrir arquivo .NAM
    // flag = 0
    // cont_no = 0
    // inicializar o vetor node[cont_no] com 0
    // enquanto nao eh fim de arquivo
    //		ler linhas (a partir da 5a linha)
    //		se o primeiro caracter do string da linha for diferente de n
    //			flag = 1
    //		senao 
    //	        node[cont_no] = 1
    //			pegar posicao x, y do ponto
    //			node[cont_no].x = x
    //			node[cont_no].y = y
    //			cont_no = cont_no + 1
    			
  }

  /**
   * Main entry point.
   *
   * @param args command-line arguments 
   */  
  public static void main(String[] args)
  {
	//for (vezes=1; vezes<11; vezes++){
		System.gc();
		System.err.flush();
		try
		{
			final CommandLineOptions options = parseCommandLineOptions(args);
			if(options.help) 
			{
				showUsage();
				return;
			}
			long endTime = options.startTime+options.duration+options.resolutionTime;
			if(endTime>0)
			{
				JistAPI.endAt(endTime*Constants.SECOND);
			}
			Constants.random = new Random(options.seed);
			// Constants.random = new Random();
			final Vector routers = new Vector();
      
			final RouteAodv.AodvStats stats = new RouteAodv.AodvStats();
      
			final Date startTime = new Date();
      
			final Field field = buildField(options, routers, stats);
      
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
                
                
					showStats(field,routers, stats, options, startTime);
				}
			}, JistAPI.END);

			//added
			/*
      			JistAPI.runAt(new Runnable()
          		{
            		public void run()
            		{
              			Iterator itr = routers.iterator();
              			while (itr.hasNext())
              			{
                			RouteAodv aodv = (RouteAodv)itr.next();
                			aodv.printPrecursors();
                			aodv.printOutgoing();
              			}
            		}
          		}, JistAPI.END);
			 */
		}
		catch(CmdLineParser.OptionException e)
		{
			System.out.println(e.getMessage());
		}
	//}// fim do for
  } // fim do main

}
