package communityAlgorithms;
import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class CommunityFinder {
	Matrix A, B, S;
	int k[], n, numberOfEdges;
	double Q;
	
	public CommunityFinder(Matrix A){
		this.A = A;
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
		
		// TODO Verificação
		System.out.println("n = " + numberOfEdges);
		System.out.print("\nK = ");
		for (int i=0; i<kLength; i++)
			System.out.print(k[i] + "  ");
		System.out.println("\n");
		System.out.print("K'.K/(2*numberOfEdges) = ");
		Kproduct.print(1, 3);
		System.out.print("B = ");
		B.print(1, 3);
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
		System.out.println("-------------------------------------");
		System.out.print("S' = ");
		S.transpose().print(2, 0);

		int ng = 0;
		for (int i=0; i<n; i++)
			if ( S.get(i,g) == 1 )
				ng++;
		
		int G[] = new int[ng];
		int counter = 0;
		for (int i=0; i<n; i++){
			if (S.get(i, g)==1){
				G[counter] = i;
				counter++;
			}
		}
		
		Matrix Bg = new Matrix(ng, ng, 0);
		for (int i=0; i<ng; i++)
			for (int j=0; j<ng; j++){
				if (i==j){
					double aux = 0;
					for (int k=0; k<ng; k++)
						aux += B.get(i,G[k]);
					Bg.set(i, j, B.get(i,j)-aux);
				}else Bg.set(i, j, B.get(i, j));
			}
		
		EigenvalueDecomposition e = Bg.eig();
		Matrix eigenvectors = e.getV();
		double eigenvalues[] = e.getRealEigenvalues();

		// TODO: apagar
		System.out.println("eigvalues = ");
		for (int i=0; i<eigenvalues.length; i++)
			System.out.print(eigenvalues[i] + "  ");
		System.out.println("\n");
		eigenvectors = orderEigenvectors(eigenvectors, eigenvalues);

		Matrix s = new Matrix(ng,1,0);
		for (int i=0; i<ng; i++)
			if (eigenvectors.get(i, ng-1) >= 0)
				s.set(i, 0, 1);
			else s.set(i, 0, -1);
		
		System.out.print("eigvec = ");
		for (int i=0; i<ng; i++)
			System.out.print(eigenvectors.get(i, ng-1) + "  *  ");
		System.out.print("\ns' = ");
		s.transpose().print(0, 0);

		Matrix product = s.transpose().times(Bg);
		product = product.times(s);
		double dQ = (1/(4.0*numberOfEdges))*product.get(0, 0);

		double sums = columnSum(s,0);
		double sumS = columnSum(S,g);
		
		System.out.println("dQ = " + dQ + ", sums = " + sums + ", sumS = " + sumS);
		if ((dQ != 0) && (sums != sumS) && (sums != -1*sumS)){
			System.out.println("ok");
			Matrix columnToAdd = new Matrix(n,1,0);
			int j=0;
			for (int i=0; i<n; i++) {
				if (S.get(i, g) == 1){
					if (s.get(j,0)==-1){
						S.set(i, g, 0);
						columnToAdd.set(i,0,1);
					}j++;
				}
			}

//			System.out.println("Smod = ");
//			S.print(2, 3);
//			System.out.println("columnToAdd = ");
//			columnToAdd.print(2, 3);

			int SColumnDimension = S.getColumnDimension();
			Matrix newS = new Matrix (n, SColumnDimension+1);
			for (int i=0; i<n; i++){
				for (int k=0; k<SColumnDimension+1; k++){
					if (k<SColumnDimension)
						newS.set(i, k, S.get(i,k));
					else newS.set(i, k, columnToAdd.get(i, 0));
				}
			}
						
			Q += dQ;
			newS = findCommunities(newS, SColumnDimension);
			newS = findCommunities(newS, g);
			return newS;
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

		CommunityFinder f = new CommunityFinder(A);
		f.getS();
	}

}
