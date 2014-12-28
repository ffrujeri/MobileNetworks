/*
 * Created on 20/10/2005
 * @author Gabi Kliot
 */
package guiTrace;

import java.io.IOException;

/**
 * Trace defines an abstract class that can be extended to implement a class
 * that can receive messages from the network simulator and convert it to 
 * human readable (or machine readable) format.
 * An example of this would be a guiTrace file output class.
 * @see JavisTrace
 */
public abstract class Trace {


    /**
     * Write the leading section of the output that should appear before the
     * actual guiTrace data. Very useful if a given file-format has to be used.
     */
    public abstract void writePreamble(int x, int y) throws IOException;

    /**
     * Handle an event. The way an event from the simulator is handled depends
     * entirely on the subclass. It could be added to a statistic our just
     * be displayed on the screen.
     */
    public abstract void handleEvent(TraceEvent e) throws IOException;

    /**
     * Write any trailing output that has to be written. Useful if a given
     * file-format has to be used.
     */
    public abstract void writePostamble() throws IOException;
}
