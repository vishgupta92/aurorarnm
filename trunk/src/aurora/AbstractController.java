/**
 * @(#)AbstractController.java
 */

package aurora;

import java.io.*;
import org.w3c.dom.Node;


/**
 * Base class for controllers.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractController.java,v 1.9.2.3 2008/10/16 04:27:07 akurzhan Exp $
 */
public abstract class AbstractController implements AuroraConfigurable, Serializable, Cloneable {
	protected double tp; // time period
	protected int ts; // time step
	
	
	/**
	 * Initializes controller from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public abstract boolean initFromDOM(Node p) throws ExceptionConfiguration;

	/**
	 * Generates XML description of a controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public abstract void xmlDump(PrintStream out) throws IOException;
	
	/**
	 * Bogus function that always returns <code>true</code>.
	 */
	public boolean validate() throws ExceptionConfiguration {
		return true;
	}
	
	/**
	 * Checks if the controller is simple or complex.
	 * @return <code>true</code> if simple, <code>false</code> - otherwise.
	 */
	public abstract boolean isSimple();
	
	/**
	 * Returns simulation sampling period.
	 */
	public final double getTP() {
		return tp;
	}
	
	/**
	 * Returns controller description.
	 */
	public abstract String getDescription();
	
	/**
	 * Sets simulation sampling period.<br>
	 * Checks that new sampling period is consistent with upper and lower level nodes.
	 * @param x sampling time.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setTP(double x) {
		tp = x;
		return true;
	}
	
	/**
	 * Resets the simulation time step.
	 */
	public void resetTimeStep() {
		ts = 0;
		return;
	}
	
	/**
	 * Implementation of a "deep copy" of the object.
	 */
	public AbstractController deepCopy() {
		AbstractController ctrlCopy = null;
		try {
			ctrlCopy = (AbstractController)clone();
		}
		catch(Exception e) { System.err.println("Clone failed: " + e.getMessage()); }
		return ctrlCopy;
	}
	
}
