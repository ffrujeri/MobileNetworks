package animator;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Main extends JFrame {
	private static final long serialVersionUID = 1L;

	private DataReader reader;
	private GraphInformation graphInfo;
	private Node events[];
	private GraphPainter painter;

	private boolean isPlaying;
	private double sliderTime, tmax, sliderPace, time[];
	private int numberOfEvents, width, height, t, sleepTime, playPace;

	private GridLayout grid;
	private Icon playIcon, pauseIcon;
	private JButton playButton, changePaceButton, changeSleepTimeButton, setTimeButton;
	private JCheckBox showRadius, showAxis;
	private JPanel panel1, panel2, mainPanel;
	private JSlider timeSlider;
	private JTextField printTime, informationDisplay;
	private Timer myTimer;
	
	public Main(){
		super ("Network Animator");
		
		int interval = getTimeInterval();		
		
		width = GraphInformation.getWidth();
		height = GraphInformation.getHeight();
		sliderTime = 0;
		sliderPace = 0.01;
		sleepTime = 500;
		playPace = interval*100;
		
		File file = new File("GUn50Exp01.txt");
//		File file = new File("GrupoUniformet1000n100exp01.txt");
//		File file = new File("GrupoUniformet1000n50exp01.txt");
		reader = new DataReader(file);
		events = reader.getEvents();
		graphInfo = new GraphInformation(events, interval);
		
		numberOfEvents = reader.getNumberOfEvents();
		tmax = events[numberOfEvents-1].getT();
		time = graphInfo.getTimeArray();
		for (t=0; time[t]<sliderTime*sliderPace; t++);
		
		painter = new GraphPainter(time, graphInfo, this);

		// slider
		timeSlider = new JSlider(SwingConstants.HORIZONTAL, 0, (int) (tmax/sliderPace) + 1, (int) sliderTime);
		timeSlider.setMajorTickSpacing(1);
		timeSlider.addChangeListener(
			new ChangeListener(){
				public void stateChanged(ChangeEvent e){
					sliderTime = (timeSlider.getValue());
					printTime.setText("t = " + sliderTime/100);
					painter.update(sliderTime*sliderPace);
				}
			}
		);
		
		// botão play
		isPlaying = false;
		playIcon = new ImageIcon("playButton.gif");
		pauseIcon = new ImageIcon("pauseButton.gif");
		playButton = new JButton(playIcon);
		playButton.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e){
					play();
				}
			}
		);
		
		// impressão do tempo
		printTime = new JTextField("t = " + sliderTime/100, 6);
		printTime.setEditable(false);
		
		// checkBoxes showRadius e showInstantSituation
		CheckBoxHandler cbhandler = new CheckBoxHandler();
		showRadius = new JCheckBox("radius", false);
		showRadius.addItemListener(cbhandler);
		showAxis = new JCheckBox("axis", false);
		showAxis.addItemListener(cbhandler);

		panel1 = new JPanel();
		panel1.add(playButton);
		panel1.add(timeSlider);
		panel1.add(printTime);
		panel1.add(showRadius);
		panel1.add(showAxis);
		
		// outros botões
		ButtonHandler bHandler = new ButtonHandler();
		changePaceButton = new JButton("pace");
		changePaceButton.addActionListener(bHandler);
		changeSleepTimeButton = new JButton("sleep time");
		changeSleepTimeButton.addActionListener(bHandler);
		setTimeButton = new JButton("time");
		setTimeButton.addActionListener(bHandler);
		informationDisplay = new JTextField("#communities: ", 27);
		informationDisplay.setEditable(false);
		
		panel2 = new JPanel();
		panel2.add(changePaceButton);
		panel2.add(changeSleepTimeButton);
		panel2.add(setTimeButton);
		panel2.add(informationDisplay);
		
		grid = new GridLayout(2,1,0,0);
		mainPanel = new JPanel(grid);
		mainPanel.add(panel1);
		mainPanel.add(panel2);
		
		Container container = getContentPane();
		container.add(mainPanel, BorderLayout.SOUTH);
		container.add(painter, BorderLayout.CENTER);
		
		container.setBackground(Color.WHITE);
		setSize(width+80, height+150);
		setVisible(true);
	}
	
	private class CheckBoxHandler implements ItemListener {
		public void itemStateChanged(ItemEvent event) {
			if (event.getSource() == showRadius){
				if (showRadius.isSelected())
					painter.setShowRadius(true);
				else painter.setShowRadius(false);
			}else if (event.getSource() == showAxis){
				if (showAxis.isSelected())
					painter.setShowAxis(true);
				else painter.setShowAxis(false);
			}repaint();
		}
	}

	private class ButtonHandler implements ActionListener {
		public void actionPerformed (ActionEvent event) {
			if (event.getSource() == changePaceButton){
				String s;
				s = JOptionPane.showInputDialog(null, "New pace:", "", JOptionPane.QUESTION_MESSAGE);
				if (s != null){
					playPace = (int) (Double.parseDouble(s)/sliderPace);
				}
			}else if (event.getSource() == changeSleepTimeButton){
				String s;
				s = JOptionPane.showInputDialog(null, "New sleep time (ms):", "", JOptionPane.QUESTION_MESSAGE);
				if (s != null){
					sleepTime = Integer.parseInt(s);
					myTimer.setDelay(sleepTime);
				}
			}else if (event.getSource() == setTimeButton){
				String s;
				s = JOptionPane.showInputDialog(null, "Time:", "", JOptionPane.QUESTION_MESSAGE);
				if (s != null){
					double newTime = Double.parseDouble(s);
					if (newTime>=0 && newTime <= tmax){
						sliderTime = newTime/sliderPace;
			        	timeSlider.setValue((int) sliderTime);
					}
				}
			}repaint();
		}
	}
	
	private int getTimeInterval(){
		String s = JOptionPane.showInputDialog(null, "Digite o intervalo de tempo (inteiro): ", "", JOptionPane.QUESTION_MESSAGE);
		if (s != null){
			return Integer.parseInt(s);
		}return 20;
	}

	private void play(){
		if (isPlaying){
			myTimer.stop();
			isPlaying = false;
			playButton.setIcon(playIcon);
		}else{
			isPlaying = true;
			playButton.setIcon(pauseIcon);
			myTimer = new Timer(sleepTime, new ActionListener() {
				    public void actionPerformed(ActionEvent timerActionEvent) {
				    	if ( sliderTime*sliderPace >= tmax ){
				        	timeSlider.setValue((int) tmax);
				    		((Timer)timerActionEvent.getSource()).stop();
				    		isPlaying = false;
							playButton.setIcon(playIcon);
				    	}else{
				        	timeSlider.setValue((int) sliderTime);
				        }
			    		painter.update(sliderTime*sliderPace);
			        	sliderTime += playPace;
				    }
			 	}
			);
			myTimer.start();
		}
	}
	
	public void setInformationDisplayed(int n, double Q, double nodesMean){
		if (informationDisplay != null){
			int aux = (int) (Q*1000);
			double aux2 = aux/1000.0;
			aux = (int) (nodesMean*1000);
			double aux3 = aux/1000.0;
			informationDisplay.setText("#communities: " + n + ", Q = " + aux2 + " and nodes mean = " + aux3);
		}
	}
	
	public static void main (String args[]){
		Main application = new Main();
		application.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
}
