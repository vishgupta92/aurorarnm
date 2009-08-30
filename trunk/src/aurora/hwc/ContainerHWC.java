/**
 * @(#)ContainerHWC.java
 */

package aurora.hwc;

import java.io.*;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

import aurora.*;


/**
 * Top object that contains pointers
 * to all the Aurora system configuration.
 * @author Alex Kurzhanskiy
 * @version $Id: ContainerHWC.java,v 1.3.2.14.2.6.2.1 2009/06/14 22:41:42 akurzhan Exp $
 */
public final class ContainerHWC extends AbstractContainer {
	private static final long serialVersionUID = 2277054116304494673L;


	public ContainerHWC() { }
	public ContainerHWC(AbstractNodeComplex ntwk) { myNetwork = ntwk; }
	public ContainerHWC(EventManager emgr) { myEventManager = emgr; }
	public ContainerHWC(AbstractNodeComplex ntwk, EventManager emgr) {
		myNetwork = ntwk;
		myEventManager = emgr;
	}
	

	/**
	 * Initializes demand profile from DOM structure.
	 */
	private boolean initDemandProfileFromDOM(Node p) throws Exception {
		if (p == null)
			return false;
		if (p.hasChildNodes()) {
			NodeList pp = p.getChildNodes();
			for (int j = 0; j < pp.getLength(); j++) {
				if (pp.item(j).getNodeName().equals("demand")) {
					int lkid = Integer.parseInt(pp.item(j).getAttributes().getNamedItem("id").getNodeValue());
					double demandTP = Double.parseDouble(pp.item(j).getAttributes().getNamedItem("tp").getNodeValue());
					if (demandTP > 24) // sampling period in seconds
						demandTP = demandTP/3600;
					double demandKnob = Double.parseDouble(pp.item(j).getAttributes().getNamedItem("knob").getNodeValue());
					AbstractLinkHWC lk = (AbstractLinkHWC)myNetwork.getLinkById(lkid);
					if (lk != null) {
						lk.setDemandKnob(demandKnob);
						lk.setDemandTP(demandTP);
						lk.setDemandVector(pp.item(j).getTextContent());
					}
				}
				if (pp.item(j).getNodeName().equals("include")) {
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp.item(j).getAttributes().getNamedItem("uri").getNodeValue());
					for (int i = 0; i < doc.getChildNodes().getLength(); i++)
						if (doc.getChildNodes().item(i).getNodeName().equals("DemandProfile"))
							initDemandProfileFromDOM(doc.getChildNodes().item(i));
				}
			}
		}
		return true;
	}
	
	/**
	 * Initializes split ratio profile from DOM structure.
	 */
	private boolean initSRProfileFromDOM(Node p) throws Exception {
		if (p == null)
			return false;
		if (p.hasChildNodes()) {
			NodeList pp = p.getChildNodes();
			for (int j = 0; j < pp.getLength(); j++) {
				if (pp.item(j).getNodeName().equals("splitratios")) {
					int nid = Integer.parseInt(pp.item(j).getAttributes().getNamedItem("id").getNodeValue());
					AbstractNodeHWC nd = (AbstractNodeHWC)myNetwork.getNodeById(nid);
					if (nd != null)
						nd.initSplitRatioProfileFromDOM(pp.item(j));
				}
				if (pp.item(j).getNodeName().equals("include")) {
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp.item(j).getAttributes().getNamedItem("uri").getNodeValue());
					for (int i = 0; i < doc.getChildNodes().getLength(); i++)
						if (doc.getChildNodes().item(i).getNodeName().equals("SRProfile"))
							initSRProfileFromDOM(doc.getChildNodes().item(i));
				}
			}
		}
		return true;
	}
	
	/**
	 * Initializes capacity profile from DOM structure.
	 */
	private boolean initCapacityProfileFromDOM(Node p) throws Exception {
		if (p == null)
			return false;
		if (p.hasChildNodes()) {
			NodeList pp = p.getChildNodes();
			for (int j = 0; j < pp.getLength(); j++) {
				if (pp.item(j).getNodeName().equals("capacity")) {
					int lkid = Integer.parseInt(pp.item(j).getAttributes().getNamedItem("id").getNodeValue());
					double capacityTP = Double.parseDouble(pp.item(j).getAttributes().getNamedItem("tp").getNodeValue());
					if (capacityTP > 24) // sampling period in seconds
						capacityTP = capacityTP/3600;
					AbstractLinkHWC lk = (AbstractLinkHWC)myNetwork.getLinkById(lkid);
					if (lk != null) {
						lk.setCapacityTP(capacityTP);
						lk.setCapacityVector(pp.item(j).getTextContent());
					}
				}
				if (pp.item(j).getNodeName().equals("include")) {
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp.item(j).getAttributes().getNamedItem("uri").getNodeValue());
					for (int i = 0; i < doc.getChildNodes().getLength(); i++)
						if (doc.getChildNodes().item(i).getNodeName().equals("CapacityProfile"))
							initCapacityProfileFromDOM(doc.getChildNodes().item(i));
				}
			}
		}
		return true;
	}
	
	/**
	 * Initializes the container contents from given DOM structure.
	 * @param p top level DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		mySettings = new SimulationSettingsHWC();
		boolean res = super.initFromDOM(p);
		if (!res)
			return !res;
		try {
			for (int i = 0; i < p.getChildNodes().getLength(); i++) {
				if (p.getChildNodes().item(i).getNodeName().equals("DemandProfile"))
					res &= initDemandProfileFromDOM(p.getChildNodes().item(i));
				if (p.getChildNodes().item(i).getNodeName().equals("SRProfile"))
					res &= initSRProfileFromDOM(p.getChildNodes().item(i));
				if (p.getChildNodes().item(i).getNodeName().equals("CapacityProfile"))
					res &= initCapacityProfileFromDOM(p.getChildNodes().item(i));
			}
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}
	
	/**
	 * Generates XML description of the Aurora system configuration.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		out.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<AuroraRNM>\n");
		super.xmlDump(out);
		out.print("</AuroraRNM>\n");
		return;
	}
	
	/**
	 * Updates the state of the Aurora system.
	 * @param ts new time step.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation, ExceptionEvent
	 */
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation, ExceptionEvent {
		boolean res = true;
		// check if we're still below max simulation step and simulation time
		if ((ts > mySettings.getTSMax()) || ((ts * myNetwork.getTP()) > mySettings.getTimeMax())) {
			myStatus.setStopped(true);
			return true;
		}
		// activate events that are due
		myEventManager.activateCurrentEvents(myNetwork, ts * myNetwork.getTP());
		// make simulation step
		res &= myNetwork.dataUpdate(ts);
		return res;
	}
	
	/**
	 * Resets the state of the Aurora system.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionEvent
	 */
	public synchronized boolean dataReset() throws ExceptionDatabase, ExceptionEvent {
		boolean res = true;
		myStatus.setStopped(true);
		myStatus.setSaved(true);
		// roll back events
		myEventManager.deactivateCurrentEvents(myNetwork, 0.0);
		// set maximum simulation step
		double maxTime = Math.min(getMySettings().getTSMax()*myNetwork.getTP(), getMySettings().getTimeMax());
		myNetwork.setMaxTimeStep((int)Math.floor(maxTime/myNetwork.getTP()));
		// reset network
		res &= myNetwork.setSimNo(myNetwork.getSimNo() + 1);
		return res;
	}
	
	/**
	 * Sets settings object.
	 * @param st settings object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setMySettings(SimulationSettings st) {
		boolean res = true;
		if (st != null)
			res &= ((NodeHWCNetwork)myNetwork).adjustWeightedData(((SimulationSettingsHWC)st).getVehicleWeights());
		res &= super.setMySettings(st);
		return res;
	}
	
}