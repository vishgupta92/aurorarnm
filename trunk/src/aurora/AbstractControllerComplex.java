/**
 * @(#)AbstractControllerComplex.java
 */

package aurora;

import java.util.*;

/**
 * This class is a base for Sytem Wide Controllers.
 * @author Alex Kurzhanskiy, Gabriel Gomes
 * @version $Id: AbstractControllerComplex.java,v 1.1.2.2.4.1.2.9 2009/11/22 22:19:35 akurzhan Exp $
 */
public abstract class AbstractControllerComplex extends AbstractController {
	private static final long serialVersionUID = 6181398228416347756L;
	
	protected AbstractMonitorController myMonitor = null;
	protected HashMap<AbstractController, Object> ctrl2input = new HashMap<AbstractController, Object>(); 
	protected Vector<AbstractController> dependentControllers = new Vector<AbstractController>();
	
	
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
		int period = (int)Math.round((double)(tp/myMonitor.getTop().getTP()));
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
	
	/**
	 * Returns dependent controller input.
	 * @param ctrl dependent controller.
	 * @return input object.
	 */
	public Object getControlInput(AbstractController ctrl) {
		return ctrl2input.get(ctrl);
	}

	/**
	 * Sets input for a dependent controller.
	 * @param ctrl dependent controller.
	 * @param obj object the complex controller is supposed to set on the dependent controller.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean setControlInput(AbstractController ctrl, Object obj) {
		if (ctrl == null)
			return false;
		ctrl2input.put(ctrl, obj);
		return true;
	}
	
	/**
	 * Sets input for a dependent controller.
	 * @param idx index of the dependent controller in the list.
	 * @param obj object the complex controller is supposed to set on the dependent controller.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean setControlInput(int idx, Object obj) {
		if ((idx < 0) || (idx >= dependentControllers.size()))
			return false;
		ctrl2input.put(dependentControllers.get(idx), obj);
		return true;
	}
	
	/**
	 * Adds dependent controller to the list.
	 * @param ctrl dependent controller.
	 * @param obj object the complex controller is supposed to set on the dependent controller.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean addDependentController(AbstractController ctrl, Object obj) {
		if (ctrl == null)
			return false;
		if (dependentControllers.indexOf(ctrl) < 0)
			dependentControllers.add(ctrl);
		ctrl2input.put(ctrl, obj);
		return true;
	}

	/**
	 * Get index of a particular controller in dependentControllers.
	 * @param ctrl dependent controller.
	 * @return integer index to vector of dependent controllers.
	 */
	public synchronized int getDependentControllerIndexOf(AbstractController ctrl) {
		if (ctrl == null)
			return -1;
		return dependentControllers.indexOf(ctrl);
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
