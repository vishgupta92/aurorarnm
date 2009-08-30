/**
 * @(#)AbstractEvent.java
 */

package aurora;

import java.io.*;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractEvent.java,v 1.3.2.5.2.2 2009/06/14 01:10:24 akurzhan Exp $
 */
public abstract class AbstractEvent implements AuroraConfigurable, Serializable {
	protected String description = null;
	protected double tstamp = 0.0; // timestamp
	protected int neid; // network element id
	protected boolean enabled = true; // to fire or not
	
	protected EventManager myManager = null;

	
	/**
	 * Initializes the event from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if (p == null)
			return !res;
		try  {
			neid = Integer.parseInt(p.getAttributes().getNamedItem("neid").getNodeValue());
			tstamp = Double.parseDouble(p.getAttributes().getNamedItem("tstamp").getNodeValue());
			enabled = Boolean.parseBoolean(p.getAttributes().getNamedItem("enabled").getNodeValue());
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (int i = 0; i < pp.getLength(); i++) {
					if (pp.item(i).getNodeName().equals("description")) {
						String desc = pp.item(i).getTextContent();
						if (!desc.equals("null"))
							description = desc;
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
	
	/**
	 * Generates XML description of an Event.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		out.print("<event class=\"" + this.getClass().getName() + "\" neid=\"" + Integer.toString(neid) + "\" tstamp=\"" + Double.toString(tstamp) + "\" enabled=\"" + Boolean.toString(enabled) + "\">");
		out.print("<description>" + description + "</description>");
		return;
	}
	
	/**
	 * Bogus function that always returns <code>true</code>.
	 */
	public boolean validate() throws ExceptionConfiguration {
		return true;
	}
	
	/**
	 * Activates the event.
	 * @param top top level complex Node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionEvent
	 */
	public boolean activate(AbstractNodeComplex top) throws  ExceptionEvent {
		double currentT = top.getTop().getTP() * top.getTS();
		tstamp = Math.max(tstamp, currentT);
		return true;
	}
	
	/**
	 * Deactivates the event.
	 * @param top top level complex Node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionEvent
	 */
	public abstract boolean deactivate(AbstractNodeComplex top) throws  ExceptionEvent;
	
	/**
	 * Checks if event is enabled.
	 */
	public final boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Returns event description.
	 */
	public final String getDescription() {
		return description;
	}
	
	/**
	 * Returns time stamp
	 */
	public final double getTime() {
		return tstamp;
	}
	
	/**
	 * Returns NE ID on which the event is to happen.
	 */
	public final int getNEID() {
		return neid;
	}
	
	/**
	 * Returns event manager.
	 */
	public final EventManager getEventManager() {
		return myManager;
	}
	
	/**
	 * Enables or disables the event.
	 * @param x boolean value (<code>true</code> to enable, <code>false</code> to disable).
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setEnabled(boolean x) {
		enabled = x;
		return true;
	}
	
	/**
	 * Sets event description.
	 * @param x time stamp.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDescription(String x) {
		description = x;
		return true;
	}
	
	/**
	 * Sets timestamp.
	 * @param x timestamp.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setTime(double x) {
		if (x < 0.0)
			tstamp = 0.0;
		else
			tstamp = x;
		return true;
	}
	
	/**
	 * Sets NE ID.
	 * @param x NE ID.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setNEID(int x) {
		if (x < 0)
			return false;
		neid = x;
		return true;
	}
	
	/**
	 * Sets event manager to which the event belongs.
	 * @param x event manager.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setEventManager(EventManager x) {
		if (x == null)
			return false;
		myManager = x;
		return true;
	}
	
}