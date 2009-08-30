/**
 * @(#)AbstractControllerComplex.java
 */

package aurora;

import java.util.Vector;

/**
 * This class is a base for Sytem Wide Controllers.
 * @author Alex Kurzhanskiy, Gabriel Gomes
 * @version $Id: AbstractControllerComplex.java,v 1.1.2.2.4.1.2.4 2009/08/16 21:14:22 akurzhan Exp $
 */
public abstract class AbstractControllerComplex extends AbstractController {
	protected AbstractMonitorController myMonitor = null;
	protected Vector<AbstractControllerSimple> controllers = new Vector<AbstractControllerSimple>();
	protected Vector<Double> controloutput = new Vector<Double>();
	
	
	/**
	 * Updates the state of controller.
	 * @param ts new time step.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		if (ts < 1)
			throw new ExceptionSimulation(null, "Nonpositive time step (" + Integer.toString(ts) + ").");
		if (this.ts == 0) {
			this.ts = ts;
			return true;
		}
		int period = (int)Math.round((double)(myMonitor.getTop().getTP()/tp));
		if (period == 0)
			period = 1;
		if ((ts - this.ts) < period)
			return false;
		this.ts = ts;
		return true;
	}
	
	/**
	 * Returns <code>false</code> indicating that it is not a simple controller.
	 */
	public final boolean isSimple() {
		return false;
	}
	
	/**
	 * Returns <code>false</code> indicating that it is not a node controller.
	 */
	public final boolean isNode() {
		return false;
	}
	
	/**
	 * Returns <code>true</code> indicating that it is a complex controller.
	 */
	public final boolean isComplex() {
		return true;
	}
	
	/**
	 * Returns monitor to which this controller belongs.
	 */
	public final AbstractMonitor getMyMonitor() {
		return myMonitor;
	}
	
	public double getControlInput(int i) {
		return controloutput.get(i);
	}

	public synchronized void setControlInput(int i, double x) {
		controloutput.set(i,x);
	}
	
	public int attachController(AbstractControllerSimple x){
		controllers.add(x);
		controloutput.add(0.0);
		return controllers.size()-1;
	}
	
	/**
	 * Assigns monitor to which this controller should belong.
	 * @param x monitor.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setMyMonitor(AbstractMonitorController x) {
		if (x == null)
			return false;
		myMonitor = x;
		return true;
	}

}
