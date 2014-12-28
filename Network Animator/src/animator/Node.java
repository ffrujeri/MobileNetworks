package animator;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;


public class Node {
	private int id;
	private static int dx, dy;
	private Color color;
	private double x, y, t;
	private double maxDistance;
	private double r;
	private boolean draw, checked;
	
	// construtor
	public Node (int id, double x, double y, double t){
		this.id = id;
		this.x = x;
		this.y = y;
		this.t = t;
		r = 8;
		draw = true;
		color = Color.GREEN;

		maxDistance = GraphInformation.getMaxDistance();
		dx = 30;
		dy = 15;
	}
	
	public void draw(Graphics2D g2d){
		Ellipse2D.Double nodeToDraw = new Ellipse2D.Double(x-r+dx, y-r+dy, 2*r, 2*r);
		g2d.setPaint(color);
		g2d.fill(nodeToDraw);
		g2d.setPaint(Color.BLACK);
		g2d.draw(nodeToDraw);
		if (id > 9)
			g2d.drawString(Integer.toString(id), (float) x-7+dx, (float) y+5+dy);
		else
			g2d.drawString(Integer.toString(id), (float) x-3+dx, (float) y+5+dy);
	}
	
	public void drawRadius(Graphics2D g2d){
		Ellipse2D.Double circleAroundNode = new Ellipse2D.Double(x-maxDistance+dx, y-maxDistance+dy, 2*maxDistance, 2*maxDistance);
		g2d.draw(circleAroundNode);
	}
	
	public String info(){
		String info = "Nó [" + id + "], x = " + x + ", y = " + y + ", t = " + t;
		return info;
	}
	
	public static int getDx(){
		return dx;
	}

	public static int getDy(){
		return dy;
	}
	
	public double getT(){
		return t;
	}
	
	public int getId(){
		return id;
	}

	public boolean getDraw(){
		return draw;
	}
	
	public void setDraw(boolean b){
		draw = b;
	}
	
	public boolean getChecked(){
		return checked;
	}
	
	public void setChecked(boolean b){
		checked = b;
	}
	
	public double getX(){
		return x;
	}

	public double getY(){
		return y;
	}
	
	public Color getColor(){
		return color;
	}

	public void setColor(Color color){
		this.color = color;
	}
}
