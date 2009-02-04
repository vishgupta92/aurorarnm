/**
 * @(#)AbstractNodeComplex.java
 */

package aurora;

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;


/**
 * Base class for Network Nodes.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractNodeComplex.java,v 1.18.2.12.2.10 2009/01/15 04:54:50 akurzhan Exp $
 */
public abstract class AbstractNodeComplex extends AbstractNode {
	protected double tp;  // sampling time
	//protected Vector<Double> timeH = new Vector<Double>();
	protected int simNo = 0; // simulation number
	protected int maxTimeStep = 100000; // maximum simulation step
	protected boolean top = false;
	protected boolean controlled = true;
	protected DataStorage database = null;
	protected boolean verbose = false;
	
	protected Vector<AbstractMonitor> monitors = new Vector<AbstractMonitor>();
	protected Vector<AbstractNode> nodes = new Vector<AbstractNode>();
	protected Vector<AbstractLink> links = new Vector<AbstractLink>();
	protected Vector<OD> odList = new Vector<OD>();
	protected Vector<ErrorConfiguration> cfgErrors = new Vector<ErrorConfiguration>();
	
	protected AbstractContainer container = null; 
	
	private Vector<Node> domnodes = new Vector<Node>();
	
	
	/**
	 * Initialize Node list from the DOM structure.
	 */
	public boolean initNodeListFromDOM(Node p) throws Exception {
		boolean res = true;
		if (p == null)
			return false;
		if (p.hasChildNodes()) {
			NodeList pp2 = p.getChildNodes();
			for (int j = 0; j < pp2.getLength(); j++) {
				if ((pp2.item(j).getNodeName().equals("node")) || (pp2.item(j).getNodeName().equals("network"))) {
					Class c = Class.forName(pp2.item(j).getAttributes().getNamedItem("class").getNodeValue());
					AbstractNode nd = (AbstractNode)c.newInstance();
					nd.setMyNetwork(this);
					res &= nd.initFromDOM(pp2.item(j));
					addNode(nd);
					domnodes.add(pp2.item(j));
				}
				if (pp2.item(j).getNodeName().equals("include")) {
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp2.item(j).getAttributes().getNamedItem("uri").getNodeValue());
					if (doc.hasChildNodes())
						res &= initNodeListFromDOM(doc.getChildNodes().item(0));
				}
			}
		}
		return res;
	}
	
	/**
	 * Initialize Link list from the DOM structure.
	 */
	private boolean initLinkListFromDOM(Node p) throws Exception {
		boolean res = true;
		if (p == null)
			return false;
		if (p.hasChildNodes()) {
			NodeList pp2 = p.getChildNodes();
			for (int j = 0; j < pp2.getLength(); j++) {
				if (pp2.item(j).getNodeName().equals("link")) {
					Class c = Class.forName(pp2.item(j).getAttributes().getNamedItem("class").getNodeValue());
					AbstractLink lk = (AbstractLink)c.newInstance();
					lk.setMyNetwork(this);
					res &= lk.initFromDOM(pp2.item(j));
					addLink(lk);
				}
				if (pp2.item(j).getNodeName().equals("include")) {
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp2.item(j).getAttributes().getNamedItem("uri").getNodeValue());
					if (doc.hasChildNodes())
						res &= initLinkListFromDOM(doc.getChildNodes().item(0));
				}
			}
		}
		return res;
	}
	
	/**
	 * Initialize Monitor list from the DOM structure.
	 */
	private boolean initMonitorListFromDOM(Node p) throws Exception {
		boolean res = true;
		if (p == null)
			return false;
		if (p.hasChildNodes()) {
			NodeList pp2 = p.getChildNodes();
			for (int j = 0; j < pp2.getLength(); j++) {
				if (pp2.item(j).getNodeName().equals("monitor")) {
					Class c = Class.forName(pp2.item(j).getAttributes().getNamedItem("class").getNodeValue());
					AbstractMonitor mon = (AbstractMonitor)c.newInstance();
					mon.setMyNetwork(this);
					res &= mon.initFromDOM(pp2.item(j));
					addMonitor(mon);
				}
				if (pp2.item(j).getNodeName().equals("include")) {
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp2.item(j).getAttributes().getNamedItem("uri").getNodeValue());
					if (doc.hasChildNodes())
						res &= initMonitorListFromDOM(doc.getChildNodes().item(0));
				}
			}
		}
		return res;
	}
	
	/**
	 * Initialize OD list from the DOM structure.
	 */
	private boolean initODListFromDOM(Node p) throws Exception {
		boolean res = true;
		if (p == null)
			return false;
		if (p.hasChildNodes()) {
			NodeList pp2 = p.getChildNodes();
			for (int j = 0; j < pp2.getLength(); j++) {
				if (pp2.item(j).getNodeName().equals("od")) {
					Class c = Class.forName(pp2.item(j).getAttributes().getNamedItem("class").getNodeValue());
					OD od = (OD)c.newInstance();
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
	 * Initializes the complex Node from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if (p == null)
			return !res;
		if (initialized) {
			if (top)
				return res;
			try  {
				if (p.hasChildNodes()) {
					NodeList pp = p.getChildNodes();
					for (int i = 0; i < pp.getLength(); i++) {
						if (pp.item(i).getNodeName().equals("inputs"))
							if (pp.item(i).hasAttributes()) {
								NodeList pp2 = pp.item(i).getChildNodes();
								for (int j = 0; j < pp2.getLength(); j++)
									if (pp2.item(j).getNodeName().equals("input"))
										addPredecessor(myNetwork.getLinkById(Integer.parseInt(pp2.item(j).getAttributes().getNamedItem("id").getNodeValue())));
							}
						if (pp.item(i).getNodeName().equals("outputs"))
							if (pp.item(i).hasAttributes()) {
								NodeList pp2 = pp.item(i).getChildNodes();
								for (int j = 0; j < pp2.getLength(); j++)
									if (pp2.item(j).getNodeName().equals("output"))
										addSuccessor(myNetwork.getLinkById(Integer.parseInt(pp2.item(j).getAttributes().getNamedItem("id").getNodeValue())));
							}
					}
				}
			}
			catch(Exception e) {
				res = false;
				throw new ExceptionConfiguration(e.getMessage());
			}
			return res;
		}
		if (myNetwork == null) {
			myNetwork = this;
			top = true;
		}
		else {
			top = false;
			container = myNetwork.getContainer();
		}
		try  {
			id = Integer.parseInt(p.getAttributes().getNamedItem("id").getNodeValue());
			controlled = Boolean.parseBoolean(p.getAttributes().getNamedItem("controlled").getNodeValue());
			tp = Double.parseDouble(p.getAttributes().getNamedItem("tp").getNodeValue());
			name = p.getAttributes().getNamedItem("name").getNodeValue();
			if (p.hasChildNodes()) {
				domnodes.clear();
				int i;
				NodeList pp = p.getChildNodes();
				for (i = 0; i < pp.getLength(); i++) {
					if (pp.item(i).getNodeName().equals("description")) {
						description = pp.item(i).getTextContent();
						if (description.equals("null"))
							description = null;
					}
					if (pp.item(i).getNodeName().equals("position")) {
						position = new PositionNode();
						res &= position.initFromDOM(pp.item(i));
					}
					if (pp.item(i).getNodeName().equals("NodeList")) { // 1: Nodes
						res &= initNodeListFromDOM(pp.item(i));
					}
				}
				for (i = 0; i < pp.getLength(); i++) // 2: Links
					if (pp.item(i).getNodeName().equals("LinkList"))
						res &= initLinkListFromDOM(pp.item(i));
				for (i = 0; i < pp.getLength(); i++) // 3: Monitors
					if (pp.item(i).getNodeName().equals("MonitorList"))
						res &= initMonitorListFromDOM(pp.item(i));
				for (i = 0; i < nodes.size(); i++) // 4: Nodes again
					res &= nodes.get(i).initFromDOM(domnodes.get(i));
				domnodes.clear();
				for (i = 0; i < pp.getLength(); i++) // 5: ODs
					if (pp.item(i).getNodeName().equals("ODList"))
						res &= initODListFromDOM(pp.item(i));
			}
			else
				if (top)
					res = false;
			
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		initialized = true;
		return res;
	}
	
	/**
	 * Generates XML description of the complex Node.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		int i;
		if (out == null)
			out = System.out;
		out.print("<network class=\"" + this.getClass().getName() + "\" id=\"" + id + "\" name=\"" + name + "\" top=\"" + top + "\" controlled=\"" + controlled + "\" tp=\"" + tp + "\">\n");
		out.print("<description>" + description + "</description>\n");
		position.xmlDump(out);
		out.print("<MonitorList>\n");
		for (i = 0; i < monitors.size(); i++)
			monitors.get(i).xmlDump(out);
		out.print("</MonitorList>\n");
		out.print("<NodeList>\n");
		for (i = 0; i < nodes.size(); i++)
			nodes.get(i).xmlDump(out);
		out.print("</NodeList>\n");
		out.print("<LinkList>\n");
		for (i = 0; i < links.size(); i++)
			links.get(i).xmlDump(out);
		out.print("</LinkList>\n");
		out.print("<ODList>\n");
		for (i = 0; i < odList.size(); i++)
			odList.get(i).xmlDump(out);
		out.print("</ODList>\n");
		out.print("</network>\n");
		return;
	}
	
	/**
	 * Updates Node data.<br>
	 * Initiates data update on all Monitors, Nodes and Links that belong to this Node.
	 * @param ts time step.
	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		if (simNo != myNetwork.getSimNo())
			throw new ExceptionSimulation(this, "Simulation number inconsistency (" + Integer.toString(simNo) + "," + Integer.toString(myNetwork.getSimNo())+ ").");
		boolean res = true;
		if (database != null)
			res &= database.saveNodeData(this);
		if (!res)
			return false;
		//int oldts = ts;
		res = super.dataUpdate(ts);
		if (!res)
			return res;
		//timeH.add(oldts * getTop().getTP());
		int i;
		for (i = 0; i < monitors.size(); i++)
			res &= monitors.get(i).dataUpdate(ts);
		for (i = 0; i < nodes.size(); i++)
			res &= nodes.get(i).dataUpdate(ts);
		for (i = 0; i < monitors.size(); i++)
			res &= monitors.get(i).dataUpdate(ts);
		for (i = 0; i < links.size(); i++)
			res &= links.get(i).dataUpdate(ts);
		for (i = 0; i < monitors.size(); i++)
			res &= monitors.get(i).dataUpdate(ts);
		if ((res) && (database != null)) {
			res &= database.execute();
			if (res)
				res &= database.commit();
		}
		return res;
	}
	
	/**
	 * Validates Node configuration.<br>
	 * Initiates validation of all Monitors, Nodes and Links that belong to this node.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean validate() throws ExceptionConfiguration {
		int i;
		cfgErrors.clear();
		boolean res = super.validate();
		for (i = 0; i < monitors.size(); i++)
			res &= monitors.get(i).validate();
		for (i = 0; i < nodes.size(); i++) {
			res &= nodes.get(i).validate();
			if ((nodes.get(i).getType() & AbstractTypes.MASK_NETWORK) > 0) {
				Vector<ErrorConfiguration> errs = ((AbstractNodeComplex)nodes.get(i)).getConfigurationErrors();
				for (int j = 0; j < errs.size(); j++)
					cfgErrors.add(errs.get(j));
			}
		}
		for (i = 0; i < links.size(); i++)
			res &= links.get(i).validate();
		for (i = 0; i < odList.size(); i++)
			res &= odList.get(i).validate();
		return res;
	}
	
	/**
	 * Updates configuration summary.
	 * @param cs configuration summary.
	 */
	public void updateConfigurationSummary(AbstractConfigurationSummary cs) {
		if (cs == null)
			return;
		cs.incrementNetworks();
		int i;
		for (i = 0; i < monitors.size(); i++)
			monitors.get(i).updateConfigurationSummary(cs);
		for (i = 0; i < nodes.size(); i++)
			nodes.get(i).updateConfigurationSummary(cs);
		for (i = 0; i < links.size(); i++)
			links.get(i).updateConfigurationSummary(cs);
		return;
	}

	/**
	 * Returns <code>false</code> indicating that it is a complex node.
	 */
	public final boolean isSimple() {
		return false;
	}
	
	/**
	 * Returns <code>true</code> if it is a top node, <code>false</code> - otherwise.
	 */
	public final boolean isTop() {
		return top;
	}
	
	/**
	 * Returns <code>true</code> if control is on, <code>false</code> - otherwise.
	 */
	public final boolean isControlled() {
		return controlled;
	}
	
	/**
	 * Returns container to which it belongs.
	 */
	public AbstractContainer getContainer() {
		return container;
	}
	
	/**
	 * Returns sampling time of the simulation.
	 */
	public final double getTP() {
		return tp;
	}
	
	/**
	 * Returns simulation time.
	 */
	public final double getSimTime() {
		return ts * getTop().getTP();
	}
	
	/**
	 * Returns simulation number.
	 */
	public final int getSimNo() {
		return simNo;
	}
	
	/**
	 * Returns maximum simulation step.
	 */
	public final int getMaxTimeStep() {
		return maxTimeStep;
	}
	
	/**
	 * Returns database interface.
	 */
	public final DataStorage getDatabase() {
		return database;
	}
	
	/**
	 * Returns verbosity mode.
	 */
	public final boolean getVerbose() {
		return verbose;
	}
	
	/**
	 * Returns vector of Monitors.
	 */
	public final Vector<AbstractMonitor> getMonitors() {
		return monitors;
	}
	
	/**
	 * Returns vector of Networks.
	 */
	public final Vector<AbstractNodeComplex> getNetworks() {
		Vector<AbstractNodeComplex> networks = new Vector<AbstractNodeComplex>();
		networks.add(this);
		for (int i = 0; i < nodes.size(); i++)
			if (!nodes.get(i).isSimple())
				networks.addAll(((AbstractNodeComplex)nodes.get(i)).getNetworks());
		return networks;
	}
	
	/**
	 * Returns vector of Nodes.
	 */
	public final Vector<AbstractNode> getNodes() {
		return nodes;
	}
	
	/**
	 * Returns vector of Links.
	 */
	public final Vector<AbstractLink> getLinks() {
		return links;
	}
	
	/**
	 * Returns vector of source Links.
	 */
	public final Vector<AbstractLink> getSourceLinks() {
		Vector<AbstractLink> sources = new Vector<AbstractLink>();
		for (int i = 0; i < links.size(); i++)
			if (links.get(i).getBeginNode() == null)
				sources.add(links.get(i));
		return sources;
	}
	
	/**
	 * Returns vector of destination Links.
	 */
	public final Vector<AbstractLink> getDestinationLinks() {
		Vector<AbstractLink> destinations = new Vector<AbstractLink>();
		for (int i = 0; i < links.size(); i++)
			if (links.get(i).getEndNode() == null)
				destinations.add(links.get(i));
		return destinations;
	}
	
	/**
	 * Returns list of ODs.
	 */
	public final Vector<OD> getODList() {
		return odList;
	}
	
	/**
	 * Returns list of configuration errors.
	 */
	public final Vector<ErrorConfiguration> getConfigurationErrors() {
		return cfgErrors;
	}
	
	/**
	 * Finds and returns Monitor specified by its identifier.
	 * @param id Monitor identifier.
	 * @return Monitor, <code>null</code> if Monitor was not found.
	 */
	public final AbstractMonitor getMonitorById(int id) {
		int i;
		for (i = 0; i < monitors.size(); i++)
			if (id == monitors.get(i).getId())
				return monitors.get(i);
		AbstractMonitor mon = null;
		for (i = 0; i < nodes.size(); i++) {
			if (!nodes.get(i).isSimple())
				mon = ((AbstractNodeComplex)nodes.get(i)).getMonitorById(id);
			if (mon != null)
				return mon;
		}
		return mon;
	}
	
	/**
	 * Finds and returns Node specified by its identifier.
	 * @param id Node identifier.
	 * @return Node, <code>null</code> if Node was not found.
	 */
	public final AbstractNode getNodeById(int id) {
		int i;
		if (this.id == id)
			return this;
		for (i = 0; i < nodes.size(); i++)
			if (id == nodes.get(i).getId())
				return nodes.get(i);
		AbstractNode nd = null;
		for (i = 0; i < nodes.size(); i++) {
			if (!nodes.get(i).isSimple())
				nd = ((AbstractNodeComplex)nodes.get(i)).getNodeById(id);
			if (nd != null)
				return nd;
		}
		return nd;
	}
	
	/**
	 * Finds and returns Link specified by its identifier.
	 * @param id Link identifier.
	 * @return Link, <code>null</code> if Link was not found.
	 */
	public final AbstractLink getLinkById(int id) {
		int i;
		for (i = 0; i < links.size(); i++)
			if (id == links.get(i).getId())
				return links.get(i);
		AbstractLink lk = null;
		for (i = 0; i < nodes.size(); i++) {
			if (!nodes.get(i).isSimple())
				lk = ((AbstractNodeComplex)nodes.get(i)).getLinkById(id);
			if (lk != null)
				return lk;
		}
		return lk;
	}
	
	/**
	 * Sets container to which the network belongs.
	 * @param x container object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setContainer(AbstractContainer x) {
		if (x == null)
			return false;
		container = x;
		return true;
	}
	
	/**
	 * Sets top flag.
	 * @param x top flag.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setTop(boolean x) {
		top = x;
		return true;
	}
	
	/**
	 * Sets simulation sampling period.<br>
	 * Checks that new sampling period is consistent with upper and lower level nodes.
	 * @param x sampling time.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setTP(double x) {
		boolean res = true;
		if ((x < 0) || ((!top) && (getMyNetwork().getTP() > x)))
			return false;
		tp = x;
		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.get(i).getTP() < x)
				res &= nodes.get(i).setTP(x);
		}
		for (int i = 0; i < monitors.size(); i++) {
			if ((monitors.get(i).getType() & AbstractTypes.MASK_MONITOR_CONTROLLER) > 0) {
				AbstractMonitorController mn = (AbstractMonitorController)monitors.get(i);
				if (mn.getTP() < x)
					res &= mn.setTP(x);
			}
		}
		return res;
	}
	
	/**
	 * Sets verbosity mode on/off.
	 * @param val true to turn verbosity mode on, false - off.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setVerbose(boolean val) {
		boolean res = true;
		verbose = val;
		for (int i = 0; i < nodes.size(); i++)
			if ((nodes.get(i).getType() & AbstractTypes.MASK_NETWORK) > 0)
				((AbstractNodeComplex)nodes.get(i)).setVerbose(val);
		return res;
	}
	
	/**
	 * Sets simulation number.<br>
	 * @param x simulation number.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setSimNo(int x) {
		if ((x < 1) || (x == simNo))
			return false;
		resetTimeStep();
		simNo = x;
		if (!top) {
			maxTimeStep = (int)Math.floor((getTop().getTP()*getTop().getMaxTimeStep())/tp);
		}
		boolean res = true;
		int i;
		for (i = 0; i < monitors.size(); i++)
			monitors.get(i).resetTimeStep();
		for (i = 0; i < nodes.size(); i++)
			if (!nodes.get(i).isSimple()) {
				AbstractNodeComplex nd = (AbstractNodeComplex)nodes.get(i);
				res &= nd.setSimNo(x);
			}
			else
				nodes.get(i).resetTimeStep();
		for (i = 0; i < links.size(); i++)
			links.get(i).resetTimeStep();
		return res;
	}
	
	/**
	 * Sets maximum simulation step.<br>
	 * @param x maximum time step.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setMaxTimeStep(int x) {
		if (x < 1)
			return false;
		maxTimeStep = x;
		return true;
	}
	
	/**
	 * Sets database interface.<br>
	 * @param x database interface.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDatabase(DataStorage x) {
		boolean res = true;
		database = x;
		for (int i = 0; i < nodes.size(); i++)
			if (!nodes.get(i).isSimple()) {
				AbstractNodeComplex nd = (AbstractNodeComplex)nodes.get(i);
				res = res & nd.setDatabase(x);
			}
		return res;
	}
	
	/**
	 * Sets control mode On/Off.<br>
	 * @param x true/false value.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setControlled(boolean x) {
		boolean res = true;
		controlled = x;
		for (int i = 0; i < nodes.size(); i++) {
			if (!nodes.get(i).isSimple())
				res &= ((AbstractNodeComplex)nodes.get(i)).setControlled(x);
		}
		return res;
	}
	
	/**
	 * Adds a Monitor to the list.
	 * @param x Monitor.
	 * @return idx index of the added Monitor, <code>-1</code> - if the Monitor could not be added.
	 */
	public synchronized int addMonitor(AbstractMonitor x) {
		int idx = -1;
		if ((x != null) && (monitors.add(x)))
			idx = monitors.size() - 1;
		return idx;
	}
	
	/**
	 * Adds a Node to the list.
	 * @param x Node.
	 * @return idx index of the added Node, <code>-1</code> - if the Node could not be added.
	 */
	public synchronized int addNode(AbstractNode x) {
		int idx = -1;
		if ((x != null) && (nodes.add(x)))
			idx = nodes.size() - 1;
		if (verbose)
			System.out.println("* " + this + " *: adding Node '" + x + "'");
		return idx;
	}
	
	/**
	 * Adds a Link to the list.
	 * @param x Link.
	 * @return idx index of the added Link, <code>-1</code> - if the Link could not be added.
	 */
	public synchronized int addLink(AbstractLink x) {
		int idx = -1;
		if ((x != null) && (links.add(x)))
			idx = links.size() - 1;
		if (verbose)
			System.out.println("* " + this + " *: adding Link '" + x + "'");
		return idx;
	}
	
	/**
	 * Adds OD to the list.
	 * @param x OD.
	 * @return idx index of the added OD, <code>-1</code> - if the OD could not be added.
	 */
	public synchronized int addOD(OD x) {
		int idx = -1;
		if ((x != null) && (odList.add(x)))
			idx = odList.size() - 1;
		return idx;
	}
	
	/**
	 * Adds an error to the configuration error list.
	 * @param x configuration error.
	 * @return idx index of the added OD, <code>-1</code> - if the OD could not be added.
	 */
	public synchronized int addConfigurationError(ErrorConfiguration err) {
		int idx = -1;
		if ((err != null) && (err.getSource() != null) && (err.getMessage() != null) && (cfgErrors.add(err)))
			idx = cfgErrors.size() - 1;
		return idx;
	}
	
	/**
	 * Deletes specified NetworkElement from the list.
	 * @param x Network Element to be deleted.
	 * @return idx index of deleted Network Element, <code>-1</code> - if such Network Element was not found.
	 */
	public synchronized int deleteNetworkElement(AbstractNetworkElement x) {
		int idx = -1;
		if (x != null) {
			if ((x.getType() & AbstractTypes.MASK_MONITOR) > 0)
				idx = deleteMonitor((AbstractMonitor)x);
			else if ((x.getType() & AbstractTypes.MASK_NETWORK) > 0)
				idx = deleteNode((AbstractNode)x);
			else if ((x.getType() & AbstractTypes.MASK_NODE) > 0)
				idx = deleteNode((AbstractNode)x);
			else
				idx = deleteLink((AbstractLink)x);
		}
		return idx;
	}
	
	/**
	 * Deletes specified Monitor from the list. 
	 * @param x Monitor to be deleted.
	 * @return idx index of deleted Monitor, <code>-1</code> - if such Monitor was not found.
	 */
	public synchronized int deleteMonitor(AbstractMonitor x) {
		int idx = monitors.indexOf(x);
		if (idx >= 0)
			monitors.remove(idx);
		return idx;
	}
	
	/**
	 * Deletes specified Node from the list. 
	 * @param x Node to be deleted.
	 * @return idx index of deleted Node, <code>-1</code> - if such Node was not found.
	 */
	public synchronized int deleteNode(AbstractNode x) {
		int idx = nodes.indexOf(x);
		if (idx >= 0) {
			if (verbose)
				System.out.println("* " + this + " *: deleting Node '" + x + "'");
			nodes.remove(idx);
			Vector<AbstractNetworkElement> links = x.getPredecessors();
			int i = 0;
			for (i = 0; i < links.size(); i++)
				links.get(i).deleteSuccessor(x);
			links = x.getSuccessors();
			for (i = 0; i < links.size(); i++)
				links.get(i).deletePredecessor(x);
		}
		return idx;
	}
	
	/**
	 * Deletes specified Link from the list. 
	 * @param x Link to be deleted.
	 * @return idx index of deleted Link, <code>-1</code> - if such Link was not found.
	 */
	public synchronized int deleteLink(AbstractLink x) {
		int idx = links.indexOf(x);
		if (idx >= 0) {
			if (verbose)
				System.out.println("* " + this + " *: deleting Link '" + x + "'");
			links.remove(idx);
			AbstractNetworkElement nd = x.getBeginNode();
			if (nd != null)
				nd.deleteSuccessor(x);
			nd = x.getEndNode();
			if (nd != null)
				nd.deletePredecessor(x);
		}
		return idx;
	}
	
	/**
	 * Replaces given Monitor with the new one.
	 * @param oldm Monitor to be replaced.
	 * @param newm new Monitor.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	protected synchronized boolean replaceMonitor(AbstractMonitor oldm, AbstractMonitor newm) {
		int idx = monitors.indexOf(oldm);
		if (idx < 0)
			return false;
		monitors.remove(idx);
		monitors.add(idx, newm);
		return true;
	}
	
	/**
	 * Replaces given Node with the new one.
	 * @param oldn Node to be replaced.
	 * @param newn new Node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	protected synchronized boolean replaceNode(AbstractNode oldn, AbstractNode newn) {
		int idx = nodes.indexOf(oldn);
		if (idx < 0)
			return false;
		nodes.remove(idx);
		nodes.add(idx, newn);
		return true;
	}
	
	/**
	 * Replaces given Link with the new one.
	 * @param oldl Link to be replaced.
	 * @param newl new Link.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	protected synchronized boolean replaceLink(AbstractLink oldl, AbstractLink newl) {
		int idx = links.indexOf(oldl);
		if (idx < 0)
			return false;
		links.remove(idx);
		links.add(idx, newl);
		return true;
	}
	
	/**
	 * Replaces given Network Element with the new one.
	 * @param oldne Network Element to be replaced.
	 * @param newne new Network Element.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean replaceNetworkElement(AbstractNetworkElement oldne, AbstractNetworkElement newne) {
		if ((oldne == null) || (newne == null))
			return false;
		boolean res = false;
		if (((oldne.getType() & AbstractTypes.MASK_MONITOR) > 0) && ((newne.getType() & AbstractTypes.MASK_MONITOR) > 0))
			res = replaceMonitor((AbstractMonitor)oldne, (AbstractMonitor)newne);
		if (((oldne.getType() & AbstractTypes.MASK_NETWORK) > 0) && ((newne.getType() & AbstractTypes.MASK_NETWORK) > 0))
			res = replaceNode((AbstractNode)oldne, (AbstractNode)newne);
		if (((oldne.getType() & AbstractTypes.MASK_NODE) > 0) && ((newne.getType() & AbstractTypes.MASK_NODE) > 0))
			res = replaceNode((AbstractNode)oldne, (AbstractNode)newne);
		if (((oldne.getType() & AbstractTypes.MASK_LINK) > 0) && ((newne.getType() & AbstractTypes.MASK_LINK) > 0))
			res = replaceLink((AbstractLink)oldne, (AbstractLink)newne);
		if (res) {
			int i;
			Vector<AbstractNetworkElement> nelist = oldne.getPredecessors();
			for (i = 0; i < nelist.size(); i++)
				nelist.get(i).replaceSuccessor(oldne, newne);
			nelist = oldne.getSuccessors();
			for (i = 0; i < nelist.size(); i++)
				nelist.get(i).replacePredecessor(oldne, newne);
		}
		return res;
	}
	
	/**
	 * Clears the OD list.
	 */
	public synchronized void clearMyODList() {
		odList.clear();
		return;
	}
	
	/**
	 * Copies data from given Network to a current one.
	 * @param x given Network Element.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean copyData(AbstractNetworkElement x) {
		boolean res = super.copyData(x);
		if ((!res) || ((x.getType() & AbstractTypes.MASK_NETWORK) == 0))
			return false;
		AbstractNodeComplex ntwk = (AbstractNodeComplex)x;
		container = ntwk.getContainer();
		controlled = ntwk.isControlled();
		top = ntwk.isTop();
		database = ntwk.getDatabase();
		simNo = ntwk.getSimNo();
		tp = ntwk.getTP();
		maxTimeStep = ntwk.getMaxTimeStep();
		monitors = ntwk.getMonitors();
		nodes = ntwk.getNodes();
		links = ntwk.getLinks();
		odList = ntwk.getODList();
		verbose = ntwk.getVerbose();
		return res;
	}
	
}
