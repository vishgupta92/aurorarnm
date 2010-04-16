/**
 * @(#)NodeHWCNetwork.java
 */

package aurora.hwc;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aurora.*;


/**
 * Road Network Node.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class NodeHWCNetwork extends AbstractNodeComplex {
	private static final long serialVersionUID = -124608463365357280L;
	
	private AuroraInterval totalDelay = new AuroraInterval();
	private AuroraInterval totalDelaySum = new AuroraInterval();
	private boolean resetAllSums = true;
	private int tsV = 0;
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
	 * Initialize OD list from the DOM structure.
	 */
	protected boolean initODListFromDOM(Node p) throws Exception {
		boolean res = true;
		if (p == null)
			return false;
		if (p.hasChildNodes()) {
			NodeList pp2 = p.getChildNodes();
			for (int j = 0; j < pp2.getLength(); j++) {
				if (pp2.item(j).getNodeName().equals("od")) {
					OD od = new ODHWC();
					od.setMyNetwork(this);
					res &= od.initFromDOM(pp2.item(j));
					addOD(od);
				}
				if (pp2.item(j).getNodeName().equals("include")) {
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp2.item(j).getAttributes().getNamedItem("uri").getNodeValue());
					if (doc.hasChildNodes())
						res &= initODListFromDOM(doc.getChildNodes().item(0));
				}
			}
		}
		return res;
	}
	
	/**
	 * Updates Network data.<br>
	 * Initiates data update on all Monitors, Nodes and Links that belong to this Network.
	 * @param ts time step.
	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		totalDelay.setCenter(0, 0);
		boolean res = super.dataUpdate(ts);
		if (resetAllSums)
			resetSums();
		if ((ts == 1) || (((ts - tsV) * getTop().getTP()) >= getTop().getContainer().getMySettings().getDisplayTP())) {
			tsV = ts;
			resetAllSums = true;
		}
		totalDelaySum.add(totalDelay);
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
	 * Returns letter code of the Node type.
	 */
	public String getTypeLetterCode() {
		return "N";
	}
	
	/**
	 * Returns type description.
	 */
	public final String getTypeString() {
		return "Network";
	}
	
	/**
	 * Returns total network delay.
	 */
	public final AuroraInterval getDelay() {
		AuroraInterval v = new AuroraInterval();
		v.copy(totalDelay);
		return v;
	}
	
	/**
	 * Returns sum of total network delay.
	 */
	public final AuroraInterval getSumDelay() {
		AuroraInterval v = new AuroraInterval();
		v.copy(totalDelaySum);
		return v;
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
	public synchronized void addToTotalDelay(AuroraInterval x) {
		totalDelay.add(x);
		return;
	}
	
	/**
	 * Resets quantities derived by integration: VHT, VMT, Delay, Productivity Loss.
	 */
	public synchronized void resetSums() {
		totalDelaySum.setCenter(0, 0);
		resetAllSums = false;
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