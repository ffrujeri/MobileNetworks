package animator;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;


public class DataReader {
	File file;
	Node events[];
	double tmax;
	int numberOfEvents;
	
	public DataReader(File file) {
		this.file = file;
		numberOfEvents = findNumberOfLines() - 5;
		System.out.println("File opened, starting reading...");
		System.out.println("Number of events in file = " + numberOfEvents);
		events = new Node[numberOfEvents];
		readFile();
	}
	
	private int findNumberOfLines(){
	    try {
		    InputStream is = new BufferedInputStream(new FileInputStream(file));
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean empty = true;
	        while ((readChars = is.read(c)) != -1) {
	            empty = false;
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	        }
	        is.close();
	        return (count == 0 && !empty) ? 1 : count;
	    }catch(IOException ioe){
	    	System.out.println("IOException");
	    	ioe.printStackTrace();
	    	return 0;
	    }
	}
	
	public int getNumberOfEvents(){
		return numberOfEvents;
	}
	
	private void readFile(){
		BufferedReader in;
		try{
			in = new BufferedReader (new FileReader(file));
			String s;
			int counter = 0;
			for (int i=0; i<5; i++){
				s = in.readLine();
			}
			do{
				s = in.readLine();

				if (s != null){
					String[] split = s.split(" ");
					int id = Integer.parseInt(split[4]);
					double t = Double.parseDouble(split[2]);
					double x = Double.parseDouble(split[12]);
					double y = Double.parseDouble(split[14]);
					events[counter] = new Node(id, x, y, t);
					counter++;
				}
			}while (s != null);
			in.close();
		}catch(IOException ioe){
			System.out.println("Problems with file.");
			ioe.printStackTrace();
		}
		
		System.out.println("Finished reading file.");
		
	}

	public Node[] getEvents(){
		return events;
	}

}
