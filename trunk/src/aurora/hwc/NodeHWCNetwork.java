/**
 * @(#)NodeHWCNetwork.java
 */

package aurora.hwc;

import aurora.*;


/**
 * Road Network Node.
 * @author Alex Kurzhanskiy
 * @version $Id: NodeHWCNetwork.java,v 1.8.2.8.2.1.2.1 2009/06/21 05:16:02 akurzhan Exp $
 */
public final class NodeHWCNetwork extends AbstractNodeComplex {
	private static final long serialVersionUID = -124608463365357280L;
	
	private double totalDelay = 0.0;
	private double totalDelaySum = 0.0;
	private boolean qControl = true;
	

	public NodeHWCNetwork() { }
	public NodeHWCNetwork(int id) { this.id = id; }
	public NodeHWCNetwork(int id, boolean top) {
		this.id = id;
		this.top = top;
		if (top)
			myNetwork = this;
	}
	
	
	/**
	 * Updates Network data.<br>
	 * Initiates data update on all Monitors, Nodes and Links that belong to this Network.
	 * @param ts time step.
	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		totalDelay = 0.0;
		boolean res = super.dataUpdate(ts);
		totalDelaySum += totalDelay;
		if (!isTop())
			((NodeHWCNetwork)myNetwork).addToTotalDelay(totalDelay);
		return res;
	}
	
	/**
	 * Validates Network configuration.<br>
	 * Initiates validation of all Monitors, Nodes and Links that belong to this node.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean validate() throws ExceptionConfiguration {
		boolean res = super.validate();
		return res;
	}
	
	/**
	 * Returns <code>true</code> if queue control is on, <code>false</code> otherwise.
	 */
	public boolean hasQControl() {
		return qControl;
	}
	
	/**
	 * Returns type.
	 */
	public final int getType() {
		return TypesHWC.NETWORK_HWC;
	}
	
	/**
	 * Returns total network delay.
	 */
	public final double getDelay() {
		return totalDelay;
	}
	
	/**
	 * Returns sum of total network delay.
	 */
	public final double getSumDelay() {
		return totalDelaySum;
	}
	
	/**
	 * Sets mainline control mode On/Off and queue control On/Off.<br>
	 * @param cv true/false value for mainline control.
	 * @param qcv true/false value for queue control.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setControlled(boolean cv, boolean qcv) {
		boolean res = true;
		controlled = cv;
		qControl = qcv;
		for (int i = 0; i < nodes.size(); i++) {
			if (!nodes.get(i).isSimple())
				res &= ((NodeHWCNetwork)nodes.get(i)).setControlled(cv, qcv);
		}
		return res;
	}
	
	/**
	 * Increments total delay by the given value.
	 */
	public synchronized void addToTotalDelay(double x) {
		totalDelay += x;
		return;
	}
	
	/**
	 * Resets quantities derived by integration: VHT, VMT, Delay, Productivity Loss.
	 */
	public synchronized void resetSums() {
		int i;
		for (i = 0; i < links.size(); i++) {
			((AbstractLinkHWC)links.get(i)).resetSumVMT();
			((AbstractLinkHWC)links.get(i)).resetSumVHT();
			((AbstractLinkHWC)links.get(i)).resetSumDelay();
			((AbstractLinkHWC)links.get(i)).resetSumPLoss();
		}
		for (i = 0; i < nodes.size(); i++)
			if (!nodes.get(i).isSimple())
				((NodeHWCNetwork)nodes.get(i)).resetSums();
		totalDelaySum = 0.0;
		return;
	}
	
	/**
	 * Adjust vector data according to new vehicle weights.
	 * @param w array of weights.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean adjustWeightedData(double[] w) {
		int i;
		boolean res = true;
		for (i = 0; i < nodes.size(); i++)
			if (nodes.get(i).isSimple())
				res &= ((AbstractNodeHWC)nodes.get(i)).adjustWeightedData(w);
			else
				res &= ((NodeHWCNetwork)nodes.get(i)).adjustWeightedData(w);
		for (i = 0; i < links.size(); i++)
			res &= ((AbstractLinkHWC)links.get(i)).adjustWeightedData(w);
		return res;
	}
	
}
