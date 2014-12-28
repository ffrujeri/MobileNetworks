package animator;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import javax.swing.JPanel;

public class GraphPainter extends JPanel {
	private Color colors[] = {new Color(255, 0, 0), new Color(0, 255, 255), new Color(255, 255, 0),
							  new Color(196, 0, 38), new Color(143, 146, 192), new Color(51, 153, 255), 
							  new Color(204, 204, 153), new Color(255, 153, 0), new Color(212, 190, 222), 
							  new Color(199, 159, 109), new Color(154, 100, 0), new Color(0, 0, 136), 
							  new Color(83, 0, 93), new Color(255, 0, 255), new Color(222, 222, 222), 
							  new Color(36, 77, 12), new Color(255, 204, 51), new Color(154, 100, 0), 
							  new Color(86, 53, 13), new Color(255, 255, 214), new Color(255, 204, 153), 
							  new Color(255, 255, 255)};
	private static final long serialVersionUID = 1L;
	private int timeCounter, width, height, countColors, dx, dy;
	private boolean adjacencyMatrix[][], communities[][], showRadius, showAxis, showEdges;
	private double time[], t;
	private Main main;
	private Node nodesToDraw[];
	private GraphInformation graphInfo;

	public GraphPainter(double time[], GraphInformation graphInfo, Main main){
		this.main = main;
		this.width = GraphInformation.getWidth();
		this.height = GraphInformation.getHeight();
		this.time = time;
		this.graphInfo = graphInfo;
		showRadius = false;
		showEdges = true;
		showAxis = false;
		dx = Node.getDx();
		dy = Node.getDy();
	}
	
	private void drawEdge(Graphics2D g2d, Node n1, Node n2){
        Line2D line = new Line2D.Float((float) n1.getX()+dx, (float) n1.getY()+dy, (float) n2.getX()+dx, (float) n2.getY()+dy);
        g2d.draw(line);
	}
	
	private void communitiesColors(Graphics2D g2d){
		countColors = 0;
		for (int i=0; i<nodesToDraw.length; i++){
		    nodesToDraw[i].setChecked(false);
		    nodesToDraw[i].setColor(Color.GREEN);
		}

		communities = graphInfo.getCommunities(t);
		int rows = nodesToDraw.length;
		int columns = communities[0].length;
		int isolatedNodes = 0;
		
		int counter, index = 0;
		for (int j=0; j<columns; j++){
			counter = 0;
			for (int i=0; i<rows; i++){
				if ( communities[i][j] ){
					nodesToDraw[i].setColor(colors[countColors]);
					counter++;
					index = i;
				}
			}

			if(counter == 1){
				nodesToDraw[index].setColor(Color.GREEN);
				isolatedNodes++;
			}else{
				countColors++;
				if (countColors == colors.length){
			    	countColors = 0;
			    	System.out.println("Repeating colors.");
			    }
			}
		}
		
		main.setInformationDisplayed(columns-isolatedNodes, graphInfo.getModularity(t), graphInfo.getNodesMean(t));
	}

	private void paintAxis(Graphics g){
		int space = 25;
		for (int i=space; i<width; i+=space)
			g.drawLine(i+dx, dy-3, i+dx, dy+3);
		for (int i=space; i<height; i+=space)
			g.drawLine(dx-3, i+dy, dx+3, i+dy);
		g.drawLine(width+dx, dy, width+dx-10, dy-5);
		g.drawLine(width+dx, dy, width+dx-10, dy+5);
		g.drawLine(dx, height+dy, dx-5, height+dy-10);
		g.drawLine(dx, height+dy, dx+5, height+dy-10);
		
		g.drawString("x", width+dx+8, dy+4);
		g.drawString("y", dx-2, height+dy+12);
	}	
	
	public void paintComponent(Graphics g) {		
		super.paintComponent(g);
		g.drawRect(dx, dy, width, height);
		
		if (showAxis){
			paintAxis(g);
		}
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		nodesToDraw = graphInfo.getNodesToDraw(t);
		paintEdges(g2d);
		communitiesColors(g2d);
		for (int i = 0; i<nodesToDraw.length; i++){
			if ( graphInfo.getShouldDraw(nodesToDraw[i].getId()) ){
				nodesToDraw[i].draw (g2d);
				if (showRadius){
					nodesToDraw[i].drawRadius (g2d);
				}
			}
		}
	}

	private void paintEdges(Graphics2D g2d){
		adjacencyMatrix = graphInfo.getAdjacencyMatrix(t);
		for (int i=0; i<nodesToDraw.length; i++)
			for (int j=i; j<nodesToDraw.length; j++)
				if (adjacencyMatrix[i][j])
					drawEdge(g2d, nodesToDraw[i], nodesToDraw[j]);
	}
	
	public void setShowAxis (boolean b){
		showAxis = b;
		repaint();
	}

	public void setShowEdges (){
		showEdges = true;
		for (int i=0; i<nodesToDraw.length && showEdges; i++)
			showEdges = graphInfo.getShouldDraw(nodesToDraw[i].getId());
		repaint();
	}

	public void setShowRadius (boolean b){
		showRadius = b;
		repaint();
	}
	
	public void update (double t){
		this.t = t;
		if (t < time[timeCounter]){
			timeCounter--;
			repaint();
		}else if (t > time[timeCounter] && timeCounter<time.length-1){
			timeCounter++;
			repaint();
		}
	}
	
}