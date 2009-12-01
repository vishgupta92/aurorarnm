/**
 * @(#)AbstractNode.java
 */

package aurora;

import java.util.*;


/**
 * Base class for all Nodes - complex and simple.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractNode.java,v 1.13.2.4.2.3.2.1 2009/09/30 23:51:17 akurzhan Exp $
 */
public abstract class AbstractNode extends AbstractNetworkElement {
	private static final long serialVersionUID = 4318602879545730827L;
	
	protected String name;
	protected String description;
	protected PositionNode position = new PositionNode();
	
	protected Vector<Object> inputs = new Vector<Object>();
	protected Vector<Object> outputs = new Vector<Object>();
	
	protected AbstractMonitorController myMonitor;
	
	
	/**
	 * Checks if the node is simple or complex.
	 * @return <code>true</code> if simple, <code>false</code> - otherwise.
	 */
	public abstract boolean isSimple();
	
	/**
	 * Validates node configuration.<br>
	 * Checks if the number of inputs and number of outputs
	 * correspond to the numbers of predecessors and successors,
	 * and that the node name is initialized.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration. 
	 */
	public boolean validate() throws ExceptionConfiguration {
		boolean res = super.validate();
		if (name == null) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Node name empty."));
			res = false;
			//throw new ExceptionConfiguration(this, "Node name empty.");
		}
		if (predecessors.size() != inputs.size()) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Number of In-Links (" + Integer.toString(predecessors.size()) + ") does not match number of inputs (" + Integer.toString(inputs.size()) + ")."));
			res = false;
			//throw new ExceptionConfiguration(this, "Number of in-links (" + Integer.toString(predecessors.size()) + ") does not match number of inputs (" + Integer.toString(inputs.size()) + ").");
		}
		for (int i = 0; i < predecessors.size(); i++) {
			AbstractLink lk = (AbstractLink)predecessors.get(i);
			AbstractNode nd = lk.getEndNode();
			if ((nd == null) || (nd.getId() != id)) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "Wrong In-Link (" + lk + ")."));
				res = false;
			}
		}
		if (successors.size() != outputs.size()) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Number of Out-Links (" + Integer.toString(successors.size()) + ") does not match number of outputs (" + Integer.toString(outputs.size()) + ")."));
			res = false;
			//throw new ExceptionConfiguration(this, "Number of out-links (" + Integer.toString(successors.size()) + ") does not match number of outputs (" + Integer.toString(outputs.size()) + ").");
		}
		for (int i = 0; i < successors.size(); i++) {
			AbstractLink lk = (AbstractLink)successors.get(i);
			AbstractNode nd = lk.getBeginNode();
			if ((nd == null) || (nd.getId() != id)) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "Wrong Out-Link (" + lk + ")."));
				res = false;
			}
		}
		return res;
	}

	/**
	 * Returns Node name.
	 */
	public final String getName() {
		return name;
	}
	
	/**
	 * Returns Node description.
	 */
	public final String getDescription() {
		return description;
	}
	
	/**
	 * Returns position of the Node.
	 */
	public final PositionNode getPosition() {
		return position;
	}
	
	/**
	 * Returns simulation sampling period set for this node.
	 */
	public abstract double getTP();
	
	/**
	 * Returns Monitor that controls this node if any.
	 */
	public final AbstractMonitorController getMyMonitor() {
		return myMonitor;
	}
	
	/**
	 * Returns vector of inputs.
	 */
	public final Vector<Object> getInputs() {
		return inputs;
	}
	
	/**
	 * Returns vector of outputs.
	 */
	public final Vector<Object> getOutputs() {
		return outputs;
	}

	/**
	 * Sets Node name.
	 * @param x name.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setName(String x) {
		if (x == null)
			return false;
		else
			name = x;
		return true;
	}
	
	/**
	 * Sets Node description.
	 * @param x description.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDescription(String x) {
		if (x == null)
			return false;
		else
			description = x;
		return true;
	}
	
	/**
	 * Sets position of the Node.
	 * @param x node position.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setPosition(PositionNode x) {
		if (x == null)
			return false;
		else
			position = x;
		return true;
	}
	
	
	/**
	 * Sets simulation sampling time for this node.
	 * @param x sampling time.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public abstract boolean setTP(double x);
	
	/**
	 * Assigns Complex Node of which current NE is part.
	 * @param x Complex Node.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean setMyNetwork(AbstractNodeComplex x) {
		if (x != null)
			myNetwork = x;
		else
			return false;
		setTP(myNetwork.getTP());
		return true;
	}
	
	/**
	 * Sets Monitor that controls this node.
	 * @param x Monitor object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setMyMonitor(AbstractMonitorController x) {
		if (x == null)
			return false;
		myMonitor = x;
		return true;
	}
	
	/**
	 * Sets vector of inputs.
	 * @param x vector of inputs.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setInputs(Vector<Object> x) {
		if ((x == null) || (x.size() != predecessors.size())) 
			return false;
		inputs = x;
		return true;
	}
	
	/**
	 * Sets vector of outputs.
	 * @param x vector of outputs.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public boolean setOutputs(Vector<Object> x) {
		if ((x == null) || (x.size() != successors.size())) 
			return false;
		outputs = x;
		return true;
	}
	
	/**
	 * Adds input Link to the list.
	 * @param x input Link.
	 * @return index of the added Link, <code>-1</code> - if the Link could not be added.
	 */
	public synchronized int addInLink(AbstractLink x) {
		int idx = addPredecessor(x);
		if (idx >= 0)
			if (!inputs.add(null)) {
				deletePredecessor(x);
				idx = -1;
			}
		return idx;
	}
	
	/**
	 * Adds output Link to the list.
	 * @param x output Link.
	 * @return index of the added Link, <code>-1</code> - if the Link could not be added.
	 */
	public synchronized int addOutLink(AbstractLink x) {
		int idx = addSuccessor(x);
		if (idx >= 0)
			if (!outputs.add(null)) {
				deleteSuccessor(x);
				idx = -1;
			}
		return idx;
	}
	
	/**
	 * Deletes specified NE from the list of predecessors. 
	 * @param x predecessor NE to be deleted.
	 * @return idx index of deleted predecessor, <code>-1</code> - if such NE was not found.
	 */
	public synchronized int deletePredecessor(AbstractNetworkElement x) {
		int idx = super.deletePredecessor(x);
		if (idx >= 0)
			inputs.remove(idx);
		return idx;
	}
	
	/**
	 * Deletes specified NE from the list of successors. 
	 * @param x successor NE to be deleted.
	 * @return idx index of deleted successor, <code>-1</code> - if such NE was not found.
	 */
	public synchronized int deleteSuccessor(AbstractNetworkElement x) {
		int idx = super.deleteSuccessor(x);
		if (idx >= 0)
			outputs.remove(idx);
		return idx;
	}
	
	/**
	 * Copies data from given Node to a current one.
	 * @param x given Network Element.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean copyData(AbstractNetworkElement x) {
		boolean res = super.copyData(x);
		if (res) {
			AbstractNode nd = (AbstractNode)x;
			name = nd.getName();
			description = nd.getDescription();
			position = nd.getPosition();
			inputs = nd.getInputs();
			outputs = nd.getOutputs();
			myMonitor = nd.getMyMonitor();
		}
		return res;
	}
	
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Node.
	 */
	public String toString() {
		String buf = name + " (" + Integer.toString(id) + ")";
		return buf;
	}
	
}
