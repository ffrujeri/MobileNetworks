package communityAlgorithms;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class CommunityFinder2 {
	private Matrix A, B, S;
	private int k[], n, numberOfEdges;
	private double Q;
	
	public CommunityFinder2(Matrix A){
		this.A = A;
		Q = 0;
		n = A.getRowDimension();
		if (A.getColumnDimension() != n)
			System.err.println("Error: square matrix input expected");
		else{
			zerosToDiagonal();
			calculateK();
			calculateB();
			double aux[][] = new double[n][1]; // matriz S inicial nx1
			for (int i=0; i<n; i++)
				aux[i][0] = 1;
			S = new Matrix(aux);
			S = findCommunities(S, 0);
		}
	}
	
	private void calculateB(){
		int kLength = k.length;
		
		numberOfEdges = 0;
		for (int i=0; i<kLength; i++)
			numberOfEdges += k[i];
		numberOfEdges = numberOfEdges/2;
		
		Matrix Kproduct = new Matrix(kLength, kLength);
		for (int i=0; i<kLength; i++)
			for (int j=0; j<kLength; j++)
				Kproduct.set(i, j, 1.0*k[i]*k[j]/(2*numberOfEdges));
		B = A.minus(Kproduct);
		
	}
	
	private void calculateK(){
		k = columnsSum(A);
	}
	
	
	private int[] columnsSum(Matrix M){
		int size = M.getRowDimension();
		int sum[] = new int[size];
		for (int i=0; i<size; i++){
			sum[i] = 0;
			for (int j=0; j<M.getColumnDimension(); j++)
				sum[i] += (int) M.get(i, j);
		}return sum;
	}

	private double columnSum (Matrix M, int columnNumber){
		double sum=0;
		for (int i=0; i<M.getRowDimension(); i++)
			sum += M.get(i, columnNumber);
		return sum;
	}
	

	private Matrix findCommunities(Matrix S, int g){
		int ng = (int) columnSum(S, g);
		
		int G[] = new int[ng];
		int counter = 0;
		for (int i=0; i<n; i++){
			if (S.get(i, g)==1){
				G[counter] = i;
				counter++;
			}
		}
		
		Matrix Bg = new Matrix(ng, ng, 0);
		for (int i=0; i<ng; i++){
			for (int j=0; j<ng; j++){
				if (i==j){
					double aux = 0;
					for (int k=0; k<ng; k++)
						aux += B.get(i,G[k]);
					Bg.set(i, j, B.get(i,j)-aux);
				}else Bg.set(i, j, B.get(i, j));
			}
		}
		
		EigenvalueDecomposition e = Bg.eig();
		Matrix eigenvectors = e.getV();
		double eigenvalues[] = e.getRealEigenvalues();

		// acho que posso apagar
		eigenvectors = orderEigenvectors(eigenvectors, eigenvalues);

		// 1o tratamento
		for (int i=0; i<eigenvectors.getRowDimension(); i++){
			for (int j=0; j<eigenvectors.getColumnDimension(); j++){
				if (Math.abs(eigenvectors.get(i, j)) <= 0.000001)
					eigenvectors.set(i, j, 0);
			}
		}
		
		Matrix Snew = new Matrix(n, S.getColumnDimension()+1);
		for (int i=0; i<n; i++)
			for (int j=0; j<S.getColumnDimension(); j++)
				Snew.set(i, j, S.get(i, j));
		
		Matrix Sg = new Matrix(ng, S.getColumnDimension()+1);
		boolean b1 = false, b2 = false;
		for (int i=0; i<ng; i++){
			if (eigenvectors.get(i, ng-1)>0){
				Snew.set(G[i], g, 1);
				Sg.set(i, g, 1);
				b1 = true;
			}else{
				Snew.set(G[i], g, 0);
				Sg.set(i, g, 0);
				Snew.set(G[i], S.getColumnDimension(), 1);
				Sg.set(i, S.getColumnDimension(), 1);
				b2 = true;
			}
		}
		boolean control = b1 && b2;
		Matrix M = Bg.times(Sg);
		M=Sg.transpose().times(M);
		double dQ = M.trace();

		if ((dQ>0) && control){
			Q = Q+dQ;
			Snew = findCommunities(Snew, Snew.getColumnDimension()-1);
			Snew = findCommunities(Snew, g);
			return Snew;
		}else return S;
	}

	public Matrix getS(){
		return S;
	}
	
	private Matrix orderEigenvectors(Matrix eigenvectors, double eigenvalues[]){
		boolean switched = true;
		while (switched){
			switched = false;
			for (int i=1; i<eigenvalues.length; i++){
				if (eigenvalues[i]  < eigenvalues[i-1]){
					double aux = eigenvalues[i];
					eigenvalues[i] = eigenvalues[i-1];
					eigenvalues[i-1] = aux;
					eigenvectors = switchColumns(i, i-1, eigenvectors);
					switched = true;
				}
			}
		}return eigenvectors;
		
	}
	
	private Matrix switchColumns(int m, int n, Matrix M){
		int columnsSize = M.getColumnDimension();
		for (int i=0; i<columnsSize; i++){
			double aux = M.get(i,m);
			M.set(i, m, M.get(i,n));
			M.set(i, n, aux);
		}return M;
	}
	

	private void zerosToDiagonal(){
		for (int i=0; i<n; i++)
			A.set(i, i, 0);
	}

	
	

	public static void main(String args[]){
/*		int n = 4;
		double a[][] = new double[n][n];
		for (int i=0; i<n; i++)
			for (int j=0; j<n; j++)
				a[i][j] = 5*Math.random();

		Matrix A = new Matrix(a);
		System.out.print("A = ");
		A.print(0, 5);

		EigenvalueDecomposition e = A.eig();
		System.out.print("D = ");
		e.getD().print(4, 4);
		
		System.out.print("V = ");
		e.getV().print(4, 4);

		double imag[];
		System.out.print("Imag = ");
		imag = e.getImagEigenvalues();
		for (int i=0; i<imag.length; i++)
			System.out.printf("%4f  ", imag[i]);

		double real[];
		System.out.print("\nReal = ");
		real = e.getRealEigenvalues();
		for (int i=0; i<real.length; i++)
			System.out.printf("%4f  ", real[i]);
*/
		int n = 6;
		double a[][] = new double[n][n];
		for (int i=0; i<n; i++)
			for (int j=0; j<i; j++){
				a[i][j] =  (Math.random() > 0.6? 1 : 0);
				a[j][i] =  a[i][j];
		}
		
		Matrix A = new Matrix(a);
		System.out.print("A = ");
		A.print(1, 0);

		CommunityFinder2 f = new CommunityFinder2(A);
		f.getS();
	}

}
