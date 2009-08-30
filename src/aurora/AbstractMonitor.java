/**
 * @(#)AbstractMonitor.java
 */

package aurora;

import java.io.*;
import org.w3c.dom.*;


/**
 * This abstract class is a base for Network Monitors.
 * Monitor is a virtual network element:
 *  it can see its predecessors (nodes and links)
 *  and successors (nodes), but nodes and links cannot see monitors;
 *  monitors cannot see other monitors.
 * Monitors can be only seen by the network nodes, to which they belong.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractMonitor.java,v 1.1.2.2.2.6.2.3 2009/06/17 00:03:50 akurzhan Exp $
 */
public abstract class AbstractMonitor extends AbstractNetworkElement {
	protected String description = "Monitor";
	protected boolean enabled = true;
	protected int counter = 0;
	
	
	/**
	 * Initializes Monitor from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if (p == null)
			return !res;
		try {
			id = Integer.parseInt(p.getAttributes().getNamedItem("id").getNodeValue());
			Node ep = p.getAttributes().getNamedItem("enabled");
			if (ep != null)
				enabled = Boolean.parseBoolean(ep.getNodeValue());
			for (int i = 0; i < p.getChildNodes().getLength(); i++)
				if (p.getChildNodes().item(i).getNodeName().equals("description"))
					description = p.getChildNodes().item(i).getTextContent();
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}
	
	/**
	 * Updates Monitor data.
	 * @param ts new time step.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		boolean res = true;
		if ((counter == 0) || (counter == 3)) {
			counter = 1;
			res = super.dataUpdate(ts);
		}
		else
			counter++;
		return res;
	}
	
	/**
	 * Updates configuration summary.
	 * @param cs configuration summary.
	 */
	public void updateConfigurationSummary(AbstractConfigurationSummary cs) {
		if (cs == null)
			return;
		cs.incrementMonitors();
		return;
	}
	
	/**
	 * Generates XML description of the Monitor.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		out.print("<monitor class=\"" + this.getClass().getName() + "\" id=\"" + id + "\" enabled=\"" + enabled + "\">\n");
		out.print("<description>" + description + "</description>\n");
		return;
	}
	
	/**
	 * Copies data from given Monitor to a current one.
	 * @param x given Network Element.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean copyData(AbstractNetworkElement x) {
		boolean res = super.copyData(x);
		if ((!res) || ((x.getType() & AbstractTypes.MASK_MONITOR) == 0))
			return false;
		AbstractMonitor mntr = (AbstractMonitor)x;
		description = mntr.getDescription();
		enabled = mntr.isEnabled();
		return res;
	}
	
	/**
	 * Returns Monitor description.
	 */
	public final String getDescription() {
		return description;
	}
	
	/**
	 * Returns <code>true</code> if enabled, <code>false</code> otherwise.
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Sets Monitor description.
	 * @param x description.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDescription(String x) {
		if (x == null)
			return false;
		description = x;
		return true;
	}
	
	/**
	 * Enable/Disable monitor.
	 * @param x <code>true/false</code> parameter.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setEnabled(boolean x) {
		enabled = x;
		return true;
	}
	
	/**
	 * Resets the simulation time step.
	 */
	public synchronized void resetTimeStep() {
		super.resetTimeStep();
		counter = 0;
		return;
	}
}