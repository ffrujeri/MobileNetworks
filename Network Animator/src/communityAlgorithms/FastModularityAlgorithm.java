package communityAlgorithms;
import java.util.Hashtable;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;
import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabTypeConverter;

public class FastModularityAlgorithm {
	private Hashtable<Double, Double> modularityHashtable, averageNumberOfNodesPerCommunityHashtable, stdDevNumberOfNodesPerCommunityHashtable;
	private Hashtable<Double, boolean[][]> communitiesHashtable;
	private MatlabProxyFactory factory;
	private MatlabProxy proxy;
	
	public FastModularityAlgorithm(Hashtable<Double, boolean[][]> adjacencyMatrix, double time[]){
		communitiesHashtable = new Hashtable<Double, boolean[][]>();
		modularityHashtable = new Hashtable<Double, Double>();
		averageNumberOfNodesPerCommunityHashtable = new Hashtable<Double, Double>();
		stdDevNumberOfNodesPerCommunityHashtable = new Hashtable<Double, Double>();
		
		try{
			MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
					.setUsePreviouslyControlledSession(true)
					.build();
		    factory = new MatlabProxyFactory(options);
		    proxy = factory.getProxy();
		    System.out.println("MATLAB connection succesful, determining communities and modularity...");
	
		    proxy.eval("nodesMean = 0;");
		    proxy.eval("nodesStd = 0;");
		    proxy.eval("Q = 0;");
		    proxy.eval("t = 0;");
		    for (int i=0; i<time.length; i++){
				boolean com[][] = getCommunities(adjacencyMatrix.get(time[i]), time[i]);
				communitiesHashtable.put(time[i], com);
			}
			
		    proxy.eval("disp('Communities matrix generated.')");
		    proxy.eval("plot(t, Q)");
		    proxy.eval("disp('Q vs t graph plotted.')");

		    proxy.disconnect();
		}catch(MatlabConnectionException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}catch(MatlabInvocationException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
			
	private boolean[][] getCommunities(boolean adjacencyMatrix[][], double t){
		String matrixString = getMatrixString(adjacencyMatrix);
		boolean communities[][] = null;

		try{
		    
		    proxy.eval("A=" + matrixString + ";");
		    proxy.eval("[com mod s] = fast_mo(A);");

		    // modularity
		    double Q = ((double[]) proxy.getVariable("mod"))[0];
		    modularityHashtable.put(t, Q);
		    proxy.eval("Q = [Q mod];");
		    proxy.eval("t = [t " + t + "];");

		    proxy.eval("num = nnz(com(:)==1); for i = 2:max(com) num = [num nnz(com(:)==i)]; end");
		    proxy.eval("thisMean = mean(num);");
		    proxy.eval("thisStd = std(num);");
		    double thisMean = ((double[]) proxy.getVariable("thisMean"))[0];
		    double thisStd = ((double[]) proxy.getVariable("thisStd"))[0];
		    averageNumberOfNodesPerCommunityHashtable.put(t, thisMean);
		    stdDevNumberOfNodesPerCommunityHashtable.put(t, thisStd);
		    proxy.eval("nodesMean = [nodesMean thisMean];");
		    proxy.eval("nodesStd = [nodesStd thisStd];");
		    
		    // communities matrix
		    MatlabTypeConverter processor = new MatlabTypeConverter(proxy);
		    MatlabNumericArray array = processor.getNumericArray("s");

		    proxy.eval("columns = size(s,2);");
		    double columns = ((double[]) proxy.getVariable("columns"))[0];

		    int size = adjacencyMatrix.length;
		    communities = new boolean[size][(int) columns];
		    for (int i=0; i<size; i++){
			    for (int j=0; j<columns; j++){
			    	if ( array.getRealValue(i, j) > 0.5)
			    		communities[i][j] = true;
			    	else communities[i][j] = false;
			    }
		    }
		    
		}catch(MatlabInvocationException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}return communities;
	}

	public Hashtable<Double, Double> getAverageNumberOfNodesPerCommunity(){
		return averageNumberOfNodesPerCommunityHashtable;
	}
	
	public Hashtable<Double, Double> getStdDevNumberOfNodesPerCommunity(){
		return stdDevNumberOfNodesPerCommunityHashtable;
	}
	
	public Hashtable<Double, boolean[][]> getCommunitiesHashtable(){
		return communitiesHashtable;
	}
	
	private String getMatrixString(boolean adjacencyMatrix[][]){
		String s = "[";
		
		for (int i=0; i<adjacencyMatrix.length; i++){
			for (int j=0; j<adjacencyMatrix.length; j++){
				s += ((adjacencyMatrix[i][j] && i != j)? "1" : "0") + " ";
			}s += "; ";
		}s += "]";
			
		return s;
	}
	
	public Hashtable<Double, Double> getModularityHashtable(){
		return modularityHashtable;
	}
}
