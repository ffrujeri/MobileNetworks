package animator;

public class BasicNode {
	private double x, y, t;
	private int id;
	
	public BasicNode (double x, double y, double t){
		this.x = x;
		this.y = y;
		this.t = t;
	}

	public BasicNode (double x, double y, double t, int id){
		this.x = x;
		this.y = y;
		this.t = t;
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public double getT() {
		return t;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void setId(int id) {
		this.id = id;
	}
}
