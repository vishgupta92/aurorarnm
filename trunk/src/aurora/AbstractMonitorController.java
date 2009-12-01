/**
 * @(#)AbstractMonitorController.java
 */

package aurora;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;


/**
 * Partial implementation of Controller Monitor.<br>
 * This Monitor is needed for coordinated control of multiple nodes.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractMonitorController.java,v 1.1.2.2.2.11 2009/10/01 05:49:01 akurzhan Exp $
 */
public abstract class AbstractMonitorController extends AbstractMonitor {
	private static final long serialVersionUID = 8329990843622747175L;
	
	protected AbstractControllerComplex myController;
	
	/**
	 * Initializes control Monitor from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = super.initFromDOM(p);
		if (!res)
			return res;
		try {
			NodeList pp = p.getChildNodes();
			for (int i = 0; i < pp.getLength(); i++) {
				if (pp.item(i).getNodeName().equals("monitored")) {
					NodeList ppp = pp.item(i).getChildNodes();
					for (int j = 0; j < ppp.getLength(); j++) {
						if (ppp.item(j).getNodeName().equals("nodes")) {
							StringTokenizer st = new StringTokenizer(ppp.item(j).getTextContent(), ", \t");
							while (st.hasMoreTokens()) {
								int nid = Integer.parseInt(st.nextToken());
								res &= predecessors.add(myNetwork.getNodeById(nid));
							}
						}
						if (ppp.item(j).getNodeName().equals("links")) {
							StringTokenizer st = new StringTokenizer(ppp.item(j).getTextContent(), ", \t");
							while (st.hasMoreTokens()) {
								int lid = Integer.parseInt(st.nextToken());
								res &= predecessors.add(myNetwork.getLinkById(lid));
							}
						}
					}
				}
				if (pp.item(i).getNodeName().equals("controlled")) {
					NodeList ppp = pp.item(i).getChildNodes();
					for (int j = 0; j < ppp.getLength(); j++) {
						if (ppp.item(j).getNodeName().equals("monitors")) {
							StringTokenizer st = new StringTokenizer(ppp.item(j).getTextContent(), ", \t");
							while (st.hasMoreTokens()) {
								int mid = Integer.parseInt(st.nextToken());
								res &= successors.add(myNetwork.getMonitorById(mid));
							}
						}
						if (ppp.item(j).getNodeName().equals("nodes")) {
							StringTokenizer st = new StringTokenizer(ppp.item(j).getTextContent(), ", \t");
							while (st.hasMoreTokens()) {
								int nid = Integer.parseInt(st.nextToken());
								res &= successors.add(myNetwork.getNodeById(nid));
							}
						}
						if (ppp.item(j).getNodeName().equals("links")) {
							StringTokenizer st = new StringTokenizer(ppp.item(j).getTextContent(), ", \t");
							while (st.hasMoreTokens()) {
								int lid = Integer.parseInt(st.nextToken());
								res &= successors.add(myNetwork.getLinkById(lid));
							}
						}
					}
				}
				if (pp.item(i).getNodeName().equals("controller")) {
					Class c = Class.forName(pp.item(i).getAttributes().getNamedItem("class").getNodeValue());
					myController = (AbstractControllerComplex)c.newInstance();
					myController.setMyMonitor(this);
					res &= myController.initFromDOM(pp.item(i));
				}
			}
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}
	
	/**
	 * Generates XML description of the control Monitor.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		if (out == null)
			out = System.out;
		out.print("<monitored>\n<nodes>");
		boolean cf = false;
		for (int i = 0; i < predecessors.size(); i++)
			if (((predecessors.get(i).getType() & AbstractTypes.MASK_NODE) > 0) || ((predecessors.get(i).getType() & AbstractTypes.MASK_NETWORK) > 0)) {
				if (cf)
					out.print(", ");
				out.print(predecessors.get(i).getId());
				cf = true;
			}
		out.print("</nodes>\n<links>");
		cf = false;
		for (int i = 0; i < predecessors.size(); i++)
			if ((predecessors.get(i).getType() & AbstractTypes.MASK_LINK) > 0) {
				if (cf)
					out.print(", ");
				out.print(predecessors.get(i).getId());
				cf = true;
			}
		out.print("</links>\n</monitored>\n<controlled>\n<monitors>");
		cf = false;
		for (int i = 0; i < successors.size(); i++)
			if ((successors.get(i).getType() & AbstractTypes.MASK_MONITOR) > 0) {
				if (cf)
					out.print(", ");
				out.print(successors.get(i).getId());
				cf = true;
			}
		out.print("</monitors>\n<nodes>");
		cf = false;
		for (int i = 0; i < successors.size(); i++)
			if (((successors.get(i).getType() & AbstractTypes.MASK_NODE) > 0) || ((successors.get(i).getType() & AbstractTypes.MASK_NETWORK) > 0)) {
				if (cf)
					out.print(", ");
				out.print(successors.get(i).getId());
				cf = true;
			}
		out.print("</nodes>\n<links>");
		cf = false;
		for (int i = 0; i < successors.size(); i++)
			if ((successors.get(i).getType() & AbstractTypes.MASK_LINK) > 0) {
				if (cf)
					out.print(", ");
				out.print(successors.get(i).getId());
				cf = true;
			}
		out.print("</links>\n</controlled>\n");
		if (myController != null)
			myController.xmlDump(out);
		out.print("</monitor>\n");
		return;
	}

	@Override
	public boolean validate() throws ExceptionConfiguration {
		boolean res = super.validate();
		if(myController!=null)
			res &= myController.validate();
		return res;
	}

	/**
	 * Fires complex controller.<br>
	 * @param ts time step.
	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		boolean res = super.dataUpdate(ts);
		if ((!res) || (counter != 1))
			return res;
		if (myController != null)
			res &= myController.dataUpdate(ts);
		return res;
	}

	/**
	 * Returns controller.
	 */
	public final AbstractControllerComplex getMyController() {
		return myController;
	}
	
	/**
	 * Returns permitted complex controller types.
	 */
	public abstract String[] getComplexControllerTypes();
	
	/**
	 * Returns permitted complex controller classes.
	 */
	public abstract String[] getComplexControllerClasses();
	
	/**
	 * Returns time period for controller invocation.
	 */
	public final double getTP() {
		if (myController == null)
			return -1;
		return myController.getTP();
	}
	
	/**
	 * Sets controller.
	 * @param x complex controller.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setMyController(AbstractControllerComplex x) {
		if (x == null) {
			for (int i = 0; i < successors.size(); i++) {
				if ((successors.get(i).getType() & AbstractTypes.MASK_LINK) > 0) {
					AbstractLink lnk = (AbstractLink)successors.get(i);
					AbstractNode nd = lnk.getEndNode();
					if (nd != null) {
						if ((nd.isSimple()) && (((AbstractNodeSimple)nd).getSimpleController(lnk) != null)) {
							((AbstractNodeSimple)nd).getSimpleController(lnk).setDependent(false);
							((AbstractNodeSimple)nd).setSimpleController(null, lnk);
						}
					}
				}
				if ((successors.get(i).getType() & AbstractTypes.MASK_NODE) > 0) {
					AbstractNodeSimple nd = (AbstractNodeSimple)successors.get(i);
					if (nd.getNodeController() != null) {
						nd.getNodeController().setDependent(false);
						nd.setNodeController(null);
					}
				}
				if ((successors.get(i).getType() & AbstractTypes.MASK_MONITOR_CONTROLLER) > 0) {
					AbstractMonitorController mon = (AbstractMonitorController)successors.get(i);
					if (mon.getMyController() != null) {
						mon.getMyController().setDependent(false);
						mon.setMyController(null);
					}
				}
			}
			return true;
		}
		myController = x;
		myController.setMyMonitor(this);
		try {
			myController.initialize();
		}
		catch(Exception e) { }
		return true;
	}
	
	/**
	 * Sets time period for controller invocation.
	 * @param x time period.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setTP(double x) {
		boolean res = false;
		if (getMyNetwork().getTP() > x)
			return res;
		for (int i = 0; i < successors.size(); i++) {
			AbstractNode mn = (AbstractNode)successors.get(i);
			res = mn.setTP(x);
			if (!res)
				return res;
		}
		return res;
	}
	
	/**
	 * Adds Network Elements to the monitored list.
	 * @param nes vector of Network Elements to be added to the monitored list.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean addMonitoredNE(Vector<AbstractNetworkElement> nes) {
		if (nes == null)
			return false;
		predecessors.addAll(nes);
		return true;
	}
	
	/**
	 * Adds Network Elements to the controlled list.
	 * @param nes vector of Network Elements to be added to the controlled list.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean addControlledNE(Vector<AbstractNetworkElement> nes) {
		if (nes == null)
			return false;
		successors.addAll(nes);
		return true;
	}
	
	/**
	 * Delete Network Elements from the monitored list.
	 * @param nes vector of Network Elements to be deleted from the monitored list.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean deleteMonitoredNE(Vector<AbstractNetworkElement> nes) {
		if (nes == null)
			return false;
		for (int i = 0; i < nes.size(); i++)
			predecessors.remove(nes.get(i));
		return true;
	}
	
	/**
	 * Delete Network Elements from the controlled list.
	 * @param nes vector of Network Elements to be deleted from the controlled list.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean deleteControlledNE(Vector<AbstractNetworkElement> nes) {
		if (nes == null)
			return false;
		for (int i = 0; i < nes.size(); i++)
			successors.remove(nes.get(i));
		return true;
	}
	
	/**
	 * Deletes specified NE from the list of predecessors. 
	 * @param x predecessor NE to be deleted.
	 * @return idx index of deleted predecessor, <code>-1</code> - if such NE was not found.
	 */
	public synchronized int deletePredecessor(AbstractNetworkElement x) {
		int idx = super.deletePredecessor(x);
		if (myController != null)
			try {
				myController.initialize();
			}
			catch(Exception e) { }
		return idx;
	}
	
	/**
	 * Deletes specified NE from the list of successors. 
	 * @param x successor NE to be deleted.
	 * @return idx index of deleted successor, <code>-1</code> - if such NE was not found.
	 */
	public synchronized int deleteSuccessor(AbstractNetworkElement x) {
		int idx = super.deleteSuccessor(x);
		if (myController != null)
			try {
				myController.initialize();
			}
			catch(Exception e) { }
		return idx;
	}
	
	/**
	 * Copies data from given Monitor to a current one.
	 * @param x given Network Element.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean copyData(AbstractNetworkElement x) {
		boolean res = super.copyData(x);
		if ((!res) || ((x.getType() & AbstractTypes.MASK_MONITOR_CONTROLLER) == 0))
			return false;
		AbstractMonitorController mntr = (AbstractMonitorController)x;
		myController = mntr.getMyController();
		return res;
	}
	
	/**
	 * Additional initialization.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration, ExceptionDatabase
	 */
	public boolean initialize() throws ExceptionConfiguration, ExceptionDatabase {
		boolean res = super.initialize();
		if (myController != null)
			myController.initialize();
		return res;
	}

	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Control Monitor.
	 */
	public String toString() {
		String buf = "Control Monitor (" + id + ")";
		return buf;
	}
	
	public int getMonitoredIndexById (Integer id){
		if(id==null)
			return -1;
		for(int i=0;i<predecessors.size();i++)
			if(predecessors.get(i).id==id)
				return i;
		return -1;
	}

	public int getControlledIndexById (int id){
		for(int i=0;i<successors.size();i++)
			if(successors.get(i).id==id)
				return i;
		return -1;
	}
}