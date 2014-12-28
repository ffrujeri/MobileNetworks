package animator;
// HIPÓTESE PARA FUNCIONAR: os eventos estão ordenados por tempo e os ids dos nós começam do 1.

import java.util.Hashtable;

import communityAlgorithms.FastModularityAlgorithm;

import Jama.Matrix;

public class GraphInformation {
	private Hashtable<Double, Double> modularityHashtable, averageNumberOfNodesPerCommunityHashtable, stdDevNumberOfNodesPerCommunityHashtable;
	private Hashtable<Double, Node[]> nodesToDraw = new Hashtable<Double, Node[]>(); // matriz de n�s a serem desenhados para cada tempo
	private Hashtable<Double, boolean[][]> adjacencyMatrix = new Hashtable<Double, boolean[][]>(); // matriz de adjac�ncias
	private Hashtable<Double, boolean[][]> communitiesHashtable = new Hashtable<Double, boolean[][]>();
	private Hashtable<Double, Matrix> indexMatrix = new Hashtable<Double, Matrix>();
	private Node[] events;
	private double allTimes[], consideredTimes[];
	private boolean nodeIdsBool[];
	private static int maxDistance = 70, width = 500, height = 500;
	private int maxId, timeInterval;

	public GraphInformation (Node[] event, int timeInterval){
		this.events = event;
		System.out.println("Creating graph information from events array.");
		createTimeArray();
		createNodesToDrawHashtable();

		this.timeInterval = timeInterval;
		System.out.println("Time interval to be considered in adjacency and community matrices = " + timeInterval + "s");
		setConsideredTimesArray();

		createAdjacencyMatrixHashtable();
		createCommunitiesHashtable();
		createNodeIdsBool();
		setMaxId();
		orderCommunitiesHashtable();
	}

	private void createAdjacencyMatrixHashtable(){		
		for (int k=0; k<consideredTimes.length; k++){
			Node[] nodesToDraw = getNodesToDraw(consideredTimes[k]);
			int size = nodesToDraw.length;

			// Atenção: ID dos nós deve ser entre 0 e size-1
			boolean[][] adjacencies = new boolean[size][size];

			for (int i=0; i<size-1; i++)
				for (int j=i+1; j<size; j++)
					adjacencies[nodesToDraw[i].getId()-1][nodesToDraw[i].getId()-1] = false;

			// Os elementos na diagonal principal indicam se o nó de ID = i tem vizinhos
			for (int i=0; i<size; i++){
				boolean hasNeighbours = false;
				for (int j=0; j<size; j++){
					double x1 = nodesToDraw[i].getX();
					double y1 = nodesToDraw[i].getY();
					double x2 = nodesToDraw[j].getX();
					double y2 = nodesToDraw[j].getY();
					if ( distance(x1,y1,x2,y2)<maxDistance && i != j){
						adjacencies[nodesToDraw[i].getId()-1][nodesToDraw[j].getId()-1] = true;
						hasNeighbours = true;
					}	
				}adjacencies[nodesToDraw[i].getId()-1][nodesToDraw[i].getId()-1] = hasNeighbours;
			}
			
			adjacencyMatrix.put(consideredTimes[k], adjacencies);
			
		}
		
		System.out.println("Adjacency matrices for each instant of time created.");

	}

	private void createCommunitiesHashtable(){
		System.out.println("Number of communities matrices to be calculated = " + consideredTimes.length);
		FastModularityAlgorithm alg = new FastModularityAlgorithm(adjacencyMatrix, consideredTimes);
		communitiesHashtable = alg.getCommunitiesHashtable();
		modularityHashtable = alg.getModularityHashtable();
		averageNumberOfNodesPerCommunityHashtable = alg.getAverageNumberOfNodesPerCommunity();
		stdDevNumberOfNodesPerCommunityHashtable = alg.getAverageNumberOfNodesPerCommunity();
		System.out.println("Communities and modularities calculated.");
	}
	
	private void createNodeIdsBool(){
		nodeIdsBool = new boolean[nodesToDraw.get(allTimes[allTimes.length-1]).length];
		for (int i=0; i<nodeIdsBool.length; i++)
			nodeIdsBool[i] = true;
	}
	
	
	private void createNodesToDrawHashtable(){
		// cria array de nós a ser desenhado para cada tempo
		for (int k=0; k<allTimes.length; k++){

			int nodesCount=0;
			// inicialmente, deve-se checar todos os nós
			for (int i=0; i<events.length; i++){
				events[i].setChecked(false);
				events[i].setDraw(true);
				nodesCount++;
			}
			
			boolean ok = false;
			while (!ok){
				// encontrar 1o nó que deve ser checado
				int i = 0;
				while ( i<events.length && (events[i].getChecked()==true))
					i++;
				
				// se já estourou o tempo ou todos os nós já foram verificados
				if (i>=events.length || events[i].getT() > allTimes[k]){
					ok = true;
					while (i<events.length){
						events[i].setDraw(false);
						nodesCount--;
						i++;
					}
				}else{
					Node currentNode = events[i];
					for (int j=i+1; j<events.length && events[j].getT()<=allTimes[k]; j++){
						if (events[j].getId() == currentNode.getId()){
							currentNode.setDraw(false);
							nodesCount--;
							currentNode.setChecked(true);
							currentNode = events[j];
						}
					}currentNode.setChecked(true);
				}
			}
			
			Node[] nodesToHashtable = new Node[nodesCount];
			int counter = 0;
			for (int j=0; j<events.length; j++){
				if (events[j].getDraw()){
					nodesToHashtable[counter++] = events[j];
				}
			}nodesToHashtable = orderNodes (nodesToHashtable);
			nodesToDraw.put(allTimes[k], nodesToHashtable);
		}
		
		System.out.println("List with nodes to be drawn at each instant of time created.");
	}	
	

	// elementos do vetor time são chaves da hashtable
	private void createTimeArray(){
		int counter=0, length=events.length;
		
		for (int i=0; i<length-1; i++){
			if (events[i].getT() != events[i+1].getT())
				counter++;
		}counter++;
		
		allTimes = new double[counter];
		counter = 0;
		for (int i=0; i<length-1; i++){
			if (events[i].getT() != events[i+1].getT()){
				allTimes[counter] = events[i].getT();
				counter++;
			}
		}allTimes[counter] = events[length-1].getT();
		
	}


	private double distance (double x1, double y1, double x2, double y2){
		return Math.sqrt( Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2) );
	}

	
	public boolean[][] getAdjacencyMatrix(double t){
		int i;
		for (i=0; i<consideredTimes.length && consideredTimes[i]<=t; i++);
		return adjacencyMatrix.get(consideredTimes[i-1]);
	}

	public boolean[][] getCommunities(double t){
		int i;
		for (i=0; i<consideredTimes.length && consideredTimes[i]<=t; i++);
		return communitiesHashtable.get(consideredTimes[i-1]);
	}

	public Matrix getIndexMatrix(double t){
		int i;
		for (i=0; i<allTimes.length && allTimes[i]<=t; i++);
		return indexMatrix.get(allTimes[i-1]);
	}

	public int getMaxId(){
		return maxId;
	}

	public static int getHeight(){
		return height;
	}
	
	public static int getMaxDistance(){
		return maxDistance;
	}
	
	public double getModularity(double t){
		int i;
		for (i=0; i<consideredTimes.length && consideredTimes[i]<=t; i++);
		return modularityHashtable.get(consideredTimes[i-1]);
	}

	public double getNodesMean(double t){
		int i;
		for (i=0; i<consideredTimes.length && consideredTimes[i]<=t; i++);
		return averageNumberOfNodesPerCommunityHashtable.get(consideredTimes[i-1]);
	}

	public double getNodesStd(double t){
		int i;
		for (i=0; i<consideredTimes.length && consideredTimes[i]<=t; i++);
		return stdDevNumberOfNodesPerCommunityHashtable.get(consideredTimes[i-1]);
	}
	
	public Node[] getNodesToDraw(double t){
		int i;
		for (i=0; i<consideredTimes.length && consideredTimes[i]<=t; i++);
		double newT = consideredTimes[i-1];

		for (i=0; i<allTimes.length && allTimes[i]<=newT; i++);
		return nodesToDraw.get(allTimes[i-1]);
	}

	public boolean getShouldDraw (int id){
		return nodeIdsBool[id-1];
	}
	
	public double[] getTimeArray(){
		return allTimes;
	}
	
	public static int getWidth(){
		return width;
	}
	
	private boolean[][] orderCommunities(int timeIndex){
		boolean communitiesMatrix[][] = communitiesHashtable.get(consideredTimes[timeIndex]);

		int id=0, columnCounter=0;
		while(id<maxId && columnCounter<communitiesMatrix[0].length-1){
			// find column with id
			int columnFound;
			for (columnFound=0; !communitiesMatrix[id][columnFound]; columnFound++);

			// determine next id to search on next loop
			while (communitiesMatrix[id][columnFound]){
				id++;
			}

			if (columnCounter < columnFound){
				// switch columns (columnFound and columnCounter)
				for(int i=0; i<communitiesMatrix.length; i++){
					boolean aux = communitiesMatrix[i][columnCounter];
					communitiesMatrix[i][columnCounter] = communitiesMatrix[i][columnFound];
					communitiesMatrix[i][columnFound] = aux;
				}
			}columnCounter++;
		}

		return communitiesMatrix;
	}
	
	// ordenação feita para reduzir a variação de cores com o tempo entre as comunidades
	private void orderCommunitiesHashtable(){
		for (int i=0; i<consideredTimes.length; i++){
			boolean communitiesMatrix[][] = orderCommunities(i);
			communitiesHashtable.put(consideredTimes[i], communitiesMatrix);
		}

		System.out.println("Communities matrices ordered by node index.");

	}
	
	private Node[] orderNodes(Node[] nodes){
		boolean switched = true;
		while (switched){
			switched = false;
			for (int i=1; i<nodes.length; i++){
				Node n0 = nodes[i-1];
				Node n1 = nodes[i];
				if (n1.getId()  < n0.getId()){
					Node aux = n1;
					nodes[i] = n0;
					nodes[i-1] = aux;
					switched = true;
				}
			}
		}

		return nodes;
	}

	private void setConsideredTimesArray(){
		double maxTime = allTimes[allTimes.length-1];
		int size = (int) (maxTime/timeInterval);
		consideredTimes = new double[size];
		
		for (int i=0; i<size; i++){
			consideredTimes[i] = i*timeInterval;
		}
	}
	
	private void setMaxId(){
		maxId = 0;
		Node[] nodes = nodesToDraw.get(allTimes[allTimes.length-1]);
		for (int i=0; i<nodes.length; i++)
			if (maxId<nodes[i].getId())
				maxId = nodes[i].getId();
	}
	
	public void setSholdDraw (int id, boolean b){
		nodeIdsBool[id-1] = b;
	}
	
}
