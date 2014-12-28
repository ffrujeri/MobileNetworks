//////////////////////////////////////////////////
// JIST (Java In Simulation Time) Project
// Timestamp: <Placement.java Tue 2004/04/06 11:31:11 barr pompom.cs.cornell.edu>
//

// Copyright (C) 2004 by Cornell University
// All rights reserved.
// Refer to LICENSE for terms and conditions of use.

package jist.swans.field;

import java.util.ArrayList;
import java.util.List;

import jist.swans.Constants;
import jist.swans.misc.Location;
import jist.swans.misc.Location.Location2D;

/** 
 * Interface of all initial placement models.
 *
 * @author Rimon Barr &lt;barr+jist@cs.cornell.edu&gt;
 * @version $Id: Placement.java,v 1.13 2004/04/06 16:07:47 barr Exp $
 * @since SWANS1.0
 */

public interface Placement
{

  /**
   * Return location of next node.
   *
   * @return location of next node
   */
  Location getNextLocation();

//////////////////////////////////////////////////
  // uniform circular placement model
  //

  public static class RandomCircle implements Placement{

	private double Rmax;
	private static int count= 0;
	public RandomCircle(Location loc){
		this.Rmax = loc.getX();
	}
	public Location getNextLocation(){
		 float x, y,r,a;
		 r = (float) (Constants.betaUniform.nextDouble()*Rmax);
		 a = (float) (Constants.random.nextDouble()*Math.PI*2);
		 
		 x = (float) (r*Math.cos(a) + Rmax);
		 y = (float) (r*Math.sin(a) + Rmax);
		 
		/* if(count<5);
		 switch (count++) {
		 case 0:
			 return new Location.Location2D(0,0);	
		 case 1:
			 return new Location.Location2D((float)Rmax,(float)Rmax);		 
		 case 2:
			 return new Location.Location2D(-(float)Rmax,(float)Rmax);
		 case 3:
			 return new Location.Location2D(-(float)Rmax,-(float)Rmax);
		 case 4:
			 return new Location.Location2D((float)Rmax,-(float)Rmax);
	     default:*/
	    	 return new Location.Location2D(x,y);	 
//		 }
		 
	     
	}
	  
  }
  
  public static class GridCircle1 implements Placement{

		private double raio,angulo,anguloCurrent, raioCurrent;
		private int qtdCoroa,gtdRaio;
		private double raios[];
		private int i=0;
		public GridCircle1(String field){
			
			String[] data = field.split("x|,");
	//		System.out.println("ff - "+field);
		      if(data.length!=3) throw new IllegalArgumentException("invalid format, expected i,j,z");
		      init(Integer.parseInt(data[0]), Integer.parseInt(data[1]),Double.parseDouble(data[2]));
			
			
		}
		private void init(int qtdCoroa, int gtdRaio,double raio ){

			this.qtdCoroa = qtdCoroa;
			this.raio = raio;
			this.gtdRaio=gtdRaio;
			angulo=(2*Math.PI)/qtdCoroa;
			raios=new double[qtdCoroa*gtdRaio];
			this.anguloCurrent=0;
			this.raioCurrent=0;
		}
		
		public Location getNextLocation(){
			 float x, y;
			 
			 if (i==qtdCoroa*gtdRaio)throw new IllegalStateException("grid points exhausted");
			 else{
				 if(i%qtdCoroa==0)
					 this.raioCurrent += raio;
				 else 
					 this.anguloCurrent+=this.angulo;
				 x = (float) (raioCurrent*Math.cos(anguloCurrent));
				 y = (float) (raioCurrent*Math.sin(anguloCurrent));
				 i++;
			 }
		     return new Location.Location2D(x,y);
		}
		  
	  }
  
  public static class GridCircle2 implements Placement{

		private double raio,arco,anguloCurrent, raioCurrent;
	
		private int i=0,n,j=0;
		public GridCircle2(String field){
			
			String[] data = field.split("x|,");
	//		System.out.println("ff - "+field);
		      if(data.length!=3) throw new IllegalArgumentException("invalid format, expected i,j,z");
		      init(Double.parseDouble(data[0]), Double.parseDouble(data[1]),Integer.parseInt(data[2]));
			
			
		}
		private void init(double arco,double raio ,int n){

			this.arco = arco;
			this.n =n;
			this.raio = raio;
			this.anguloCurrent=0;
			this.raioCurrent=raio;
		}
		
		public Location getNextLocation(){
			 float x, y;
			 
			 if (i==n)throw new IllegalStateException("grid points exhausted");
			 else{
				 if(anguloCurrent > 2*Math.PI){
					 this.raioCurrent += raio;
				 	 anguloCurrent=0;
				 }

				 this.anguloCurrent+=(this.arco/this.raioCurrent);
				 x = (float) (raioCurrent*Math.cos(anguloCurrent));
				 y = (float) (raioCurrent*Math.sin(anguloCurrent));
				 i++;
			 }
		     return new Location.Location2D(x,y);
		}
		  
	  }

  public static class ClusterDispersion implements Placement{

		private float x,y,raio;
		private int i;
		private Location2D areaTotal;
		private List setores = new ArrayList();;
		private int qtdClusters;
		private int []qtdJaClustered;
		private Location[] clusters;
		public ClusterDispersion(String field){
			
			String[] data = field.split("x|,");
			if(data.length!=4) throw new IllegalArgumentException("invalid format, expected i,j,z");
				init(Integer.parseInt(data[0]), Float.parseFloat(data[1]),Float.parseFloat(data[2]), Float.parseFloat(data[3]));

			
		}
		private void init(int qtdClusters,float raio,float x ,float y){

			this.x = x;
			this.y=y;
			
			this.areaTotal = new Location2D(x,y);
			this.raio = raio;
			this.qtdClusters = qtdClusters;
			this.qtdJaClustered = new int[qtdClusters];
			this.clusters = new Location[qtdClusters];
			
			initClusterHead();
			
			
		}
	    private void initClusterHead(){

			if ((this.x*this.y)/(4*this.raio*this.raio)<this.qtdClusters)throw new IllegalStateException("cluster area points exhausted");
			else{	
				for (float xCurrent=0;xCurrent<x+x;xCurrent=xCurrent+(2*raio))
					for (float yCurrent=0;yCurrent<y+y;yCurrent=yCurrent+(2*raio))
						setores.add( new Location.Location2D(xCurrent,yCurrent));	
				
				for (int i=0;i<qtdClusters;i++)
					clusters[i] = (Location) setores.get(Constants.random.nextInt(setores.size()));
			}
	    }
		public Location getNextLocation(){

			Location loc=null;
			if(i<qtdClusters)	
				return clusters[i++];
			else{
			do{
				loc = next(clusters[i%qtdClusters]);
			}while(!isInternAnyCluster(clusters[i%qtdClusters],loc));
			i++;
			return loc;
			}
		}
  
	  private boolean isInternAnyCluster(Location cluster, Location loc){
		    Location low = cluster;
		    Location upper = new Location.Location2D(cluster.getX()+(2*raio),cluster.getY()+(2*raio));
		  
			if(loc.inside(low, upper))
				return true;
			else 			  
				return false;
	  }
	  
	  private Location next(){
		  return new Location.Location2D(
					Constants.random.nextFloat()*x,
					Constants.random.nextFloat()*y);
	  }
	  private Location next(Location cluster){
		  Location2D loc = new Location.Location2D(
				  				Constants.random.nextFloat()*2*raio,
				  				Constants.random.nextFloat()*2*raio);
		  
		  loc.add(cluster);
		  return loc;
	  }
	  
	}



  
  //////////////////////////////////////////////////
  // random placement model
  //

  /**
   * Random (uniform) placement.
   */
  public static class Random implements Placement
  {
    /** placement boundaries. */
    private float x, y;

    /**
     * Initialize random placement model.
     *
     * @param x x-axis upper limit
     * @param y y-axis upper limit
     */
    public Random(float x, float y)
    {
      init(x, y);
    }

    /**
     * Initialize random placement.
     *
     * @param loc upper limit coordinate
     */
    public Random(Location loc)
    {
      init(loc.getX(), loc.getY());
    }

    /**
     * Initialize random placement.
     *
     * @param field field dimensions string
     */
    public Random(String field)
    {
      String[] data = field.split("x|,");
      if(data.length!=2) throw new IllegalArgumentException("invalid format, expected i,j");
      init(Float.parseFloat(data[0]), Float.parseFloat(data[1]));
    }

    /**
     * Initialize random placement.
     *
     * @param x field x-dimension (in meters)
     * @param y field y-dimension (in meters)
     */
    private void init(float x, float y)
    {
      this.x = x;
      this.y = y;
    }

    //////////////////////////////////////////////////
    // Placement interface
    //

    /** {@inheritDoc} */
    public Location getNextLocation()
    {
      return new Location.Location2D(
          Constants.random.nextFloat()*x,
          Constants.random.nextFloat()*y);
    }

  } // class: Random


  //////////////////////////////////////////////////
  // grid placement model
  //

  /**
   * Placement along a regular grid.
   */
  public static class Grid implements Placement
  {
    /** field dimensions. */
    private float fieldx, fieldy;
    /** node placement array dimensions. */
    private int nodex, nodey;
    /** number of nodes already placed. */
    private long i;

    /**
     * Initialize grid placement model.
     *
     * @param loc field dimensions (in meters)
     * @param nodex number of nodes in x-dimension
     * @param nodey number of nodes in y-dimension
     */
    public Grid(Location loc, int nodex, int nodey)
    {
      init(loc.getX(), loc.getY(), nodex, nodey);
    }

    /**
     * Initialize grid placement model.
     *
     * @param loc field dimensions (in meters)
     * @param s node configuration string
     */
    public Grid(Location loc, String s)
    {
      init(loc, s);
    }

    /**
     * Initialize grid placement model.
     *
     * @param field field dimensions string
     * @param nodes node configuration string
     */
    public Grid(String field, String nodes)
    {
      init(field, nodes);
    }

    /**
     * Initialize grid placement model.
     *
     * @param field field dimensions string
     * @param nodes node configuration string
     */
    private void init(String field, String nodes)
    {
      String[] data = field.split("x|,");
      if(data.length!=2) throw new IllegalArgumentException("invalid format, expected i,j");
      init(new Location.Location2D(Float.parseFloat(data[0]), Float.parseFloat(data[1])), nodes);
    }

    /**
     * Initialize grid placement model.
     *
     * @param loc field dimensions (in meters)
     * @param s node configuration string
     */
    private void init(Location loc, String s)
    {
      String[] data = s.split("x|,");
      if(data.length!=2) throw new IllegalArgumentException("invalid format, expected i,j");
      init(loc.getX(), loc.getY(), Integer.parseInt(data[0]), Integer.parseInt(data[1]));
    }

    /**
     * Initialize grid placement model.
     *
     * @param fieldx field x-dimension (in meters)
     * @param fieldy field y-dimension (in meters)
     * @param nodex number of nodes in x-dimension
     * @param nodey number of nodes in y-dimension
     */
    private void init(float fieldx, float fieldy, int nodex, int nodey)
    {
      this.fieldx = fieldx;
      this.fieldy = fieldy;
      this.nodex = nodex;
      this.nodey = nodey;
      i = 0;
    }

    //////////////////////////////////////////////////
    // Placement interface
    //

    /** {@inheritDoc} */
    public Location getNextLocation()
    {
      if(i/nodex==nodey) throw new IllegalStateException("grid points exhausted");
      Location l = new Location.Location2D(
          (i%nodex)*fieldx/nodex, (i/nodex)*fieldy/nodey);
      i++;
      return l;
    }

  } // class: Grid

} // interface: Placement

