/**
 * @(#)AbstractController.java
 */

package aurora;

import java.io.*;
import java.util.Vector;

import org.w3c.dom.Node;


/**
 * Base class for controllers.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public abstract class AbstractController implements AuroraConfigurable, Serializable, Cloneable {
	private static final long serialVersionUID = -3651447094697919017L;
	
	protected double tp = 0.016666666666666666666666666666667; // time period
	protected Vector<Object> limits = new Vector<Object>(); // input limits
	protected int ts; // time step
	protected boolean dependent = false;
	protected boolean initialized = false;
	
	
	/**
	 * Initializes controller from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if (p == null)
			return !res;
		tp = Double.parseDouble(p.getAttributes().getNamedItem("tp").getNodeValue());
		if (tp > 10) // invocation period in seconds
			tp = tp/3600;
		initialized = true;
		return res;
	}
	
	/**
	 * Additional optional initialization steps.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initialize() throws ExceptionConfiguration {
		ts = 0;
		return true;
	}

	/**
	 * Generates XML description of a controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		out.print("<controller class=\"" + this.getClass().getName() + "\" tp=\"" + Double.toString(3600*tp) + "\">");
		return;
	}
	
	/**
	 * Bogus function that always returns <code>true</code>.
	 */
	public boolean validate() throws ExceptionConfiguration {
		return true;
	}
	
	/**
	 * Returns letter code of the controller type.
	 */
	public abstract String getTypeLetterCode();
	
	/**
	 * Checks if the controller is simple.
	 * @return <code>true</code> if simple, <code>false</code> - otherwise.
	 */
	public abstract boolean isSimple();
	
	/**
	 * Checks if it is node controller.
	 * @return <code>true</code> if node, <code>false</code> - otherwise.
	 */
	public abstract boolean isNode();
	
	/**
	 * Checks if the controller is complex.
	 * @return <code>true</code> if complex, <code>false</code> - otherwise.
	 */
	public abstract boolean isComplex();
	
	/**
	 * Checks if this simple controller depends on a complex one
	 */
	public final boolean isDependent() {
		return dependent;
	}
	
	/**
	 * Returns simulation sampling period.
	 */
	public final double getTP() {
		return tp;
	}
	
	/**
	 * Returns input limits.
	 */
	public final Vector<Object> getLimits() {
		return limits;
	}
	
	/**
	 * Returns controller description.
	 */
	public abstract String getDescription();
	
	/**
	 * Sets dependent status.
	 * @param x dependence flag.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDependent(boolean x) {
		dependent = x;
		return true;
	}
	
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
	 * Sets input limits.
	 * @param x limits.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setLimits(Vector<Object> x) {
		if (x == null)
			return false;
		limits = x;
		return true;
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
