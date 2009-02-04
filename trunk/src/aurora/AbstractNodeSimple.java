/**
 * @(#)AbstractNodeSimple.java
 */

package aurora;

import java.util.*;


/**
 * Base class for all simple Nodes.<br>
 * These nodes must have input and output links.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractNodeSimple.java,v 1.13.2.4.2.2 2009/01/14 01:45:04 akurzhan Exp $
 */
public abstract class AbstractNodeSimple extends AbstractNode {
	protected Vector<AbstractControllerSimple> controllers = new Vector<AbstractControllerSimple>();
	
	
	/**
	 * Returns <code>true</code> indicating that it is a simple node.
	 */
	public final boolean isSimple() {
		return true;
	}
	
	/**
	 * Updates Node data.<br>
	 * @param ts time step.
	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		boolean res = true;
		DataStorage db = myNetwork.getDatabase();
		if (db != null)
			res &= db.saveNodeData(this);
		if (res)
			res &= super.dataUpdate(ts);
		return res;
	}
	
	/**
	 * Validates Node configuration.
	 * <p>Checks if the node has both, inputs and outputs;
	 * and that the number of controllers matches the number of inputs.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration.
	 */
	public boolean validate() throws ExceptionConfiguration {
		boolean res = super.validate();
		if (outputs.size() == 0) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Node has no outputs."));
			res = false;
			//throw new ExceptionConfiguration(this, "Node has no outputs.");
		}
		int m = inputs.size();
		if (m == 0) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Node has no inputs."));
			res = false;
			//throw new ExceptionConfiguration(this, "Node has no inputs.");
		}
		if (m != controllers.size()) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Number of controllers (" + Integer.toString(controllers.size()) + ") does not match number of inputs (" + Integer.toString(m) + ")."));
			res = false;
			//throw new ExceptionConfiguration(this, "Number of controllers (" + Integer.toString(controllers.size()) + ") does not match number of inputs (" + Integer.toString(m) + ").");
		}
		return res;
	}
	
	/**
	 * Updates configuration summary.
	 * @param cs configuration summary.
	 */
	public void updateConfigurationSummary(AbstractConfigurationSummary cs) {
		if (cs == null)
			return;
		cs.incrementNodes();
		return;
	}
	
	/**
	 * Returns vector of controllers.
	 */
	public final Vector<AbstractControllerSimple> getControllers() {
		return controllers;
	}
	
	/**
	 * Returns controller assigned to given input.
	 * @param lk input Link.
	 */
	public synchronized AbstractControllerSimple getController(AbstractLink lk) {
		if (lk == null)
			return null;
		int idx = predecessors.indexOf(lk);
		if (idx < 0)
			return null;
		return controllers.get(idx);
	}
	
	/**
	 * Returns the smallest period of controller invocation.
	 */
	public final double getTP() {
		int i = 0;
		double tp = -1;
		AbstractControllerSimple mc = null;
		while ((mc == null) && (i < controllers.size()))
			mc = controllers.get(i++);
		if (mc != null)
			tp = mc.getTP();
		while (i < controllers.size()) {
			mc = controllers.get(i++);
			if ((mc != null) && (tp > mc.getTP()))
				tp = mc.getTP();
		}
		return tp;		
	}
	
	/**
	 * Sets controller for given input Link.
	 * @param x simple controller.
	 * @param lk given input Link.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setController(AbstractControllerSimple x, AbstractLink lk) {
		if (lk == null)
			return false;
		int idx = predecessors.indexOf(lk);
		if ((idx < 0) ||
			((controllers.get(idx) != null) && (controllers.get(idx).isDependent())) ||
			((x != null) && ((x.getCompatibleNodeTypes() & getType()) == 0)))
			return false;
		controllers.set(idx, x);
		return true;
	}
	
	/**
	 * Sets controller for given input Link.
	 * @param x simple controller.
	 * @param idx index of the input Link.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setController(AbstractControllerSimple x, int idx) {
		if ((idx < 0) ||
			(idx >= controllers.size()) ||
			((controllers.get(idx) != null) && (controllers.get(idx).isDependent())) ||
			((x != null) && ((x.getCompatibleNodeTypes() & getType()) == 0)))
			return false;
		controllers.set(idx, x);
		return true;
	}
	
	/**
	 * Sets time period for controller invocation.
	 * @param x time period.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setTP(double x) {
		AbstractNodeComplex mn = getMyNetwork();
		if (x < mn.getTP())
			return false;
		for(int i = 0; i < controllers.size(); i++) {
			AbstractControllerSimple mc = controllers.get(i);
			if (mc != null)
				mc.setTP(x);
		}
		return true;
	}
	
	/**
	 * Resets the simulation time step.
	 */
	public void resetTimeStep() {
		super.resetTimeStep();
		for (int i = 0; i < controllers.size(); i++) {
			AbstractControllerSimple ctrl = controllers.get(i);
			if (ctrl != null)
				ctrl.resetTimeStep();
		}
		return;
	}
	
	/**
	 * Adds input Link to the list.
	 * @param x input Link.
	 * @param c corresponding simple controller.
	 * @return index of the added Link, <code>-1</code> - if the Link could not be added.
	 */
	public synchronized int addInLink(AbstractLink x, AbstractControllerSimple c) {
		int idx = super.addInLink(x);
		if (idx >= 0)
			if ((c != null) && ((c.getCompatibleNodeTypes() & getType()) == 0))
				controllers.add(null);
			else
				controllers.add(c);
		return idx;
	}

	/**
	 * Adds input Link to the list.
	 * @param x input Link.
	 * @return index of the added Link, <code>-1</code> - if the Link could not be added.
	 */
	public synchronized int addInLink(AbstractLink x) {
		return addInLink(x, null);
	}
	
	/**
	 * Deletes specified NE from the list of predecessors. 
	 * @param x predecessor NE to be deleted.
	 * @return idx index of deleted predecessor, <code>-1</code> - if such NE was not found.
	 */
	public synchronized int deletePredecessor(AbstractNetworkElement x) {
		int idx = super.deletePredecessor(x);
		if (idx >= 0)
			controllers.remove(idx);
		return idx;
	}
	
	/**
	 * Copies data from given Node to a current one.
	 * @param x given Network Element.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean copyData(AbstractNetworkElement x) {
		boolean res = super.copyData(x);
		if ((!res) || ((x.getType() & AbstractTypes.MASK_NODE) == 0))
			return false;
		AbstractNodeSimple nd = (AbstractNodeSimple)x;
		controllers = nd.getControllers();
		return res;
	}

}