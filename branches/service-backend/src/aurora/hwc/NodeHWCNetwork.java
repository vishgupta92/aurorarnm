/**
 * @(#)NodeHWCNetwork.java
 */

package aurora.hwc;

import java.util.*;
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
	 * Updates Sensor data.<br>
	 * @param ts time step.
	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	public synchronized boolean sensorDataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		if (resetAllSums)
			resetSums();
		int initTS = Math.max(myNetwork.getContainer().getMySettings().getTSInitial(), (int)(myNetwork.getContainer().getMySettings().getTimeInitial()/myNetwork.getTop().getTP()));
		if ((ts - initTS == 1) || (((ts - tsV) * getTop().getTP()) >= container.getMySettings().getDisplayTP()))
			resetAllSums = true;
		if (ts - initTS == 1)
			resetSums();
		return super.sensorDataUpdate(ts);
	}
	
	/**
	 * Updates Link data.<br>
	 * @param ts time step.
	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	public synchronized boolean linkDataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		totalDelay.setCenter(0, 0);
		boolean res = super.linkDataUpdate(ts);
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
	
	/**
	 * Returns density over multiple links specified in the given vector.
	 * @param v vector of links
	 * @return density vector
	 */
	public AuroraIntervalVector computeMultiLinkDensity(Vector<AbstractLinkHWC> v) {
		if ((v == null) || (v.size() == 0))
			return null;
		AuroraIntervalVector den = new AuroraIntervalVector();
		den.copy(v.firstElement().getDensity());
		double len = v.firstElement().getLength();
		den.affineTransform(len, 0);
		for (int i = 1; i < v.size(); i++) {
			double ll = v.get(i).getLength();
			len += ll;
			AuroraIntervalVector dd = v.get(i).getDensity();
			dd.affineTransform(ll, 0);
			den.add(dd);
		}
		den.affineTransform(1/len, 0);
		return den;
	}
	
	/**
	 * Returns critical density over multiple links specified in the given vector.
	 * @param v vector of links
	 * @return critical density
	 */
	public double computeMultiLinkCriticalDensity(Vector<AbstractLinkHWC> v) {
		if ((v == null) || (v.size() == 0))
			return 0;
		double len = v.firstElement().getLength();
		double den_crit = v.firstElement().getCriticalDensity() * len;
		for (int i = 1; i < v.size(); i++) {
			double ll = v.get(i).getLength();
			len += ll;
			double cd = v.get(i).getCriticalDensity() * ll;
			den_crit += cd;
		}
		den_crit = den_crit / len;
		return den_crit;
	}
	
}
