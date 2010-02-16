/**
 * @(#)AbstractContainer.java
 */

package aurora;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;


/**
 * Base class for the top object that contains pointers
 * to all the Aurora system configuration.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public abstract class AbstractContainer implements AuroraConfigurable, Serializable {
	private static final long serialVersionUID = 5225922999390390092L;
	
	protected AbstractNodeComplex myNetwork = null;
	protected EventManager myEventManager = null;
	protected SimulationSettings mySettings = null;
	protected SimulationStatus myStatus = new SimulationStatus();
	protected boolean isSim = true;
	protected boolean isBatch = false;
	protected HashMap<String, String> ne_type2classname = new HashMap<String, String>();
	protected HashMap<String, String> evt_type2classname = new HashMap<String, String>();
	protected HashMap<String, String> ctr_type2classname = new HashMap<String, String>();
	protected HashMap<String, String> ne_type2classname_ext = new HashMap<String, String>();
	protected HashMap<String, String> evt_type2classname_ext = new HashMap<String, String>();
	protected HashMap<String, String> ctr_type2classname_ext = new HashMap<String, String>();

	
	/**
	 * Initializes the container contents from given DOM structure.
	 * @param p top level DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if ((p == null) || (!p.hasChildNodes()))
			return !res;
		myNetwork = null;
		myEventManager = new EventManager();
		res &= myEventManager.setContainer(this);
		try {
			for (int i = 0; i < p.getChildNodes().getLength(); i++)
				if (p.getChildNodes().item(i).getNodeName().equals("settings")) {
					if (mySettings == null) {
						Class c = Class.forName(p.getChildNodes().item(i).getAttributes().getNamedItem("class").getNodeValue());
						mySettings = (SimulationSettings)c.newInstance();
					}
					res &= mySettings.initFromDOM(p.getChildNodes().item(i));
				}
			if (mySettings == null)
				mySettings = new SimulationSettings();
			for (int i = 0; i < p.getChildNodes().getLength(); i++) {
				if (p.getChildNodes().item(i).getNodeName().equals("network")) {
					if (myStatus != null)
						myStatus.setSaved(true);
					Class c = Class.forName(p.getChildNodes().item(i).getAttributes().getNamedItem("class").getNodeValue());
					myNetwork = (AbstractNodeComplex)c.newInstance();
					res &= myNetwork.setContainer(this);
					res &= myNetwork.initFromDOM(p.getChildNodes().item(i));
				}
				if (p.getChildNodes().item(i).getNodeName().equals("EventList")) {
					res &= myEventManager.initFromDOM(p.getChildNodes().item(i));
				}
			}
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		if (myNetwork == null)
			throw new ExceptionConfiguration("No network specified in the configuration file.");
		if (myStatus == null)
			myStatus = new SimulationStatus();
		myStatus.setSaved(true);
		myStatus.setStopped(true);
		if (mySettings.getDisplayTP() < myNetwork.getTP())
			mySettings.setDisplayTP(myNetwork.getTP());
		return res;
	}
	
	/**
	 * Generates XML description of the Aurora system configuration.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (myNetwork != null)
			myNetwork.xmlDump(out);
		if (mySettings != null) {
			out.print("<settings class=\"" + mySettings.getClass().getName() + "\">\n");
			mySettings.xmlDump(out);
			out.print("</settings>\n");
		}
		if (myEventManager != null) {
			out.print("<EventList>\n");
			myEventManager.xmlDump(out);
			out.print("</EventList>\n");
		}
		return;
	}
	
	/**
	 * Updates the state of the Aurora system.
	 * @param ts new time step.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation, ExceptionEvent
	 */
	public abstract boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation, ExceptionEvent;
	
	/**
	 * Resets the state of the Aurora system.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionEvent
	 */
	public abstract boolean initialize() throws ExceptionConfiguration, ExceptionDatabase, ExceptionEvent;
	
	/**
	 * Fills in the default type letter code to class name maps.
	 */
	public abstract void defaultTypes2Classnames();

	/**
	 * Bogus function that always returns <code>true</code>.
	 */
	public boolean validate() throws ExceptionConfiguration {
		boolean res = myNetwork.validate();
		Vector<ErrorConfiguration> errs = myNetwork.getConfigurationErrors();
		if (!errs.isEmpty()) {
			ErrorConfiguration e = errs.firstElement();
			if (e.getSource() instanceof AbstractNetworkElement)
				throw new ExceptionConfiguration((AbstractNetworkElement)e.getSource(), e.getMessage());
			else
				throw new ExceptionConfiguration(e.getMessage()); 
		}
		return res;
	}
	
	/**
	 * Returns <code>true</code> if current application runs simulation, <code>false</code> - otherwise.
	 */
	public final boolean isSimulation() {
		return isSim;
	}
	
	/**
	 * Returns <code>true</code> if current application runs in batch mode, <code>false</code> - otherwise.
	 */
	public final boolean isBatch() {
		return isBatch;
	}
	
	/**
	 * Returns top level complex Node. 
	 */
	public final AbstractNodeComplex getMyNetwork() {
		return myNetwork;
	}
	
	/**
	 * Tells if the simulation can be resumed.
	 * @param ts current time step.
	 */
	public final boolean canResume(int ts) {
		return ((ts < mySettings.getTSMax()) && ((ts * myNetwork.getTP()) < mySettings.getTimeMax()));
	}
	
	/**
	 * Returns the event manager object.
	 */
	public final EventManager getMyEventManager() {
		return myEventManager;
	}
	
	/**
	 * Returns the settings object.
	 */
	public final SimulationSettings getMySettings() {
		return mySettings;
	}
	
	/**
	 * Returns the status object.
	 */
	public final SimulationStatus getMyStatus() {
		return myStatus;
	}
	
	/**
	 * Returns Network Element class name for the specified type letter code.
	 */
	public String neType2Classname(String xx) {
		String x = xx.toUpperCase();
		String s = ne_type2classname_ext.get(x);
		if (s == null)
			s = ne_type2classname.get(x);
		return s;
	}
	
	/**
	 * Returns event class name for the specified type letter code.
	 */
	public String evtType2Classname(String xx) {
		String x = xx.toUpperCase();
		String s = evt_type2classname_ext.get(x);
		if (s == null)
			s = evt_type2classname.get(x);
		return s;
	}
	
	/**
	 * Returns controller class name for the specified type letter code.
	 */
	public String ctrType2Classname(String xx) {
		String x = xx.toUpperCase();
		String s = ctr_type2classname_ext.get(x);
		if (s == null)
			s = ctr_type2classname.get(x);
		return s;
	}
	
	/**
	 * Set application type to simulation.
	 */
	public synchronized void applicationSimulation() {
		isSim = true;
		return;
	}
	
	/**
	 * Set application type to configuration.
	 */
	public synchronized void applicationConfiguration() {
		isSim = false;
		return;
	}
	
	/**
	 * Set batch mode.
	 */
	public synchronized void batchMode() {
		isBatch = true;
		return;
	}
	
	/**
	 * Sets top level complex Node.
	 * @param ntwk complex Node object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setMyNetwork(AbstractNodeComplex ntwk) {
		myNetwork = ntwk;
		return true;
	}
	
	/**
	 * Sets event manager object.
	 * @param emgr event manager object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setMyEventManager(EventManager emgr) {
		myEventManager = emgr;
		return true;
	}
	
	/**
	 * Sets settings object.
	 * @param st settings object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setMySettings(SimulationSettings st) {
		mySettings = st;
		return true;
	}
	
	/**
	 * Sets status object.
	 * @param emgr settings object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setMyStatus(SimulationStatus st) {
		myStatus = st;
		return true;
	}

}