package interpolation;



/* TODO:
 * - objetivo: interpolar at� mudar dire��o. Identificar colis�o com a parede
 * - procurar pr�ximo evento n�o alinhado
 * - interpolar at� o anterior a este
 * - interpolar novamente a partir da colis�o
 */


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import animator.BasicNode;
import animator.DataReader;
import animator.GraphInformation;
import animator.Node;

public class InterpolationWithBorderReflection {
	private int maxId;
	private double dt; // intervalo de tempo para interpolação
	private File file;
	private Node[] events;
	private double epsilon;
	
	// hashtable inicial de posições para cada nó
	private Hashtable< Integer, ArrayList<BasicNode> > nodeEvents;

	// hashtable de posições para cada nó após interpolação
	private Hashtable< Integer, ArrayList<BasicNode> > nodeEventsInterpolated;

	// ArrayList com todos os nós e posições
	private ArrayList<BasicNode> traceInfo;
	
	public InterpolationWithBorderReflection(){
		file = new File("data.txt");
		dt = .02;
		epsilon = 0.1;
	}

	private boolean aligned (BasicNode n1, BasicNode n2, BasicNode n3){
		double t1 = n1.getT(), t2 = n2.getT(), t3 = n3.getT(),
				x1 = n1.getX(), x2 = n2.getX(), x3 = n3.getX(),
				y1 = n1.getY(), y2 = n2.getY(), y3 = n3.getY(),
				ax12 = (x2-x1)/(t2-t1), ax13 = (x3-x1)/(t3-t1),
				ay12 = (y2-y1)/(t2-t1), ay13 = (y3-y1)/(t2-t1),
				t12 = t2-t1, t13 = t3 - t1,
				vx12 = ax12/t12, vy12 = ay12/t12, vx13 = ax13/t13, vy13 = ay13/t13;
		return ( equals(ax13, ax12) &&  equals(ay13, ay12) && equals(vx13, vx12) && equals(vy13, vy12));
	}

	private void createInterpolationFile(){
		file = new File("interpolated data.txt");
		BufferedWriter out;
		try {
			out = new BufferedWriter(new FileWriter("interpolated data.txt"));
			for(int i=1; i<=5; i++){
				out.write("linha [" + i + "]");
				out.newLine();
			}for (BasicNode n : traceInfo){
				out.write("n -t " + n.getT() + " -s " + n.getId() + " -S UP -v circle -c red -x " + n.getX() + " -y " + n.getY() + " -z 4");
				out.newLine();
			}
			try {
				if (out != null)
					out.close();
			} catch (Exception ioe2) {
			}
		}catch (Exception ioe) {
			System.out.println("IO problem: " + ioe);
			ioe.printStackTrace();
		}
		
		System.out.println("Interpolated data file created.");
	}
	
	private void createNodeEventsHashtable(){
		nodeEvents = new Hashtable<Integer, ArrayList<BasicNode>>();
		for (int id=1; id<=maxId; id++){
			ArrayList<BasicNode> array = new ArrayList<BasicNode>();
			for (int j=0; j<events.length; j++){
				if (events[j].getId() == id){
					array.add( new BasicNode(events[j].getX(), events[j].getY(), events[j].getT()) );
				}
			}nodeEvents.put(id, array);
		}
		
		System.out.println("Events hashtable created.");
	}
	
	private void createNodeInterpolationHashtable(){
		nodeEventsInterpolated = new Hashtable<Integer, ArrayList<BasicNode>>();
		for (int id=1; id<=maxId; id++){
			ArrayList<BasicNode> eventsArray = nodeEvents.get(id);
			ArrayList<BasicNode> interpolationArray = new ArrayList<BasicNode>();

			interpolationArray.add(eventsArray.get(0));
			for (int i=1; i<eventsArray.size(); i++){
				BasicNode n1 = interpolationArray.get(interpolationArray.size()-1);
				BasicNode n2 = eventsArray.get(i);
				if (i < eventsArray.size()-1){
					BasicNode n3 = eventsArray.get(i+1);
					// procurar último nó alinhado
					while ( aligned(n1, n2, n3) ){
						i++;
						n3 = eventsArray.get(i+1);
					}
				}ArrayList<BasicNode> nodesToAdd = interpolate(n1, n2);
				interpolationArray.addAll(nodesToAdd);
			}nodeEventsInterpolated.put(id, interpolationArray);
		}
		
		System.out.println("Interpolation hashtable created.");
		
		// Teste
//		ArrayList<BasicNode> a = nodeEventsInterpolated.get(1);
//		System.out.println("tam a = " + a.size());
//		for (BasicNode n : a)
//			System.out.println("t = " + n.getT() + ", x = " + n.getX() + ", y = " + n.getY());
	}
	
	private void createTraceInfo(){
		
		traceInfo = new ArrayList<BasicNode>();
		for (int id=1; id<=maxId; id++){
			ArrayList<BasicNode> array = nodeEventsInterpolated.get(id);
			for (BasicNode n : array){
				n.setId(id);
				traceInfo.add(n);
			}
		}
		System.out.println("Trace information created. Number of events = " + traceInfo.size());
		orderTraceInfo();
		
		System.out.println("Trace information ordered by time.");
	}

	private boolean equals (double v1, double v2){
		return ( v1<(1+epsilon)*v2 && v1>(1-epsilon)*v2 );
	}

	private void getEvents(){
		DataReader reader = new DataReader(file);
		events = reader.getEvents();		
	}

	
	private void getMaxId(){
		maxId = 0;
		for (int i=0; i<events.length; i++)
			if (maxId<events[i].getId())
				maxId = events[i].getId();
	}
	
	private ArrayList<BasicNode> interpolate(BasicNode n1, BasicNode n2){
		ArrayList<BasicNode> interpolationArray = new ArrayList<BasicNode>();
		
		double t1 = n1.getT(), t2 = n2.getT();
		// verificação: t1 = t2 = 0
		if (t1 + t2 > 0){
			double x1 = n1.getX(), x2 = n2.getX(), 
					y1 = n1.getY(), y2 = n2.getY(),
					ax = (x2-x1)/(t2-t1), ay = (y2-y1)/(t2-t1);
			
			BasicNode n = n2;
			// enquanto a posição do nó resultante da interpolação está dentro das dimensões estabelecidas
			for (double t=t1+dt; !outOfBounds(n) ; t+=dt){
				interpolationArray.add(n);
				double x = x1 + ax*(t-t1);
				double y = y1 + ay*(t-t1);
				n = new BasicNode(x, y, t);
			}
		}else interpolationArray.add(n2);
		return interpolationArray;
	}

	private void orderTraceInfo(){
		boolean switched = true;
		while (switched){
			switched = false;
//			int counter = 0;
			for (int i=1; i<traceInfo.size(); i++){
				BasicNode n0 = traceInfo.get(i-1);
				BasicNode n1 = traceInfo.get(i);
				if (n1.getT()  < n0.getT()){
					BasicNode aux = n1;
					traceInfo.set(i, n0);
					traceInfo.set(i-1, aux);
					switched = true;
//					counter++;
				}
			}
//			System.out.print(" * " + counter);
		}
		
		// Verificação
//		for (BasicNode n : traceInfo)
//			System.out.println("id = " + n.getId() + ", t = " + n.getT() + ", x = " + n.getX() + ", y = " + n.getY());
	}
	
	private boolean outOfBounds(BasicNode n){
		return (n.getX() <= 0 || n.getX() >= GraphInformation.getWidth() || n.getY() <= 0 || n.getY() >= GraphInformation.getHeight() );
	}

	public static void main(String args[]){
		InterpolationWithBorderReflection i = new InterpolationWithBorderReflection();
		System.out.println("Interpolation interval = " + i.dt + " s");
		i.getEvents();
		i.getMaxId();
		i.createNodeEventsHashtable();
		i.createNodeInterpolationHashtable();
		i.createTraceInfo();
		i.createInterpolationFile();
	}
	
}
