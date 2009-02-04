/**
 * @(#)EventControllerSimple.java
 */

package aurora.hwc;

import java.io.*;
import org.w3c.dom.*;
import aurora.*;
import aurora.util.Util;


/**
 * Event that changes local controller for given Link
 * at the assigned Node.
 * @author Alex Kurzhanskiy
 * @version $Id: EventControllerSimple.java,v 1.4.2.5 2008/10/16 04:27:08 akurzhan Exp $
 */
public final class EventControllerSimple extends AbstractEvent {
	private static final long serialVersionUID = -4895477288111895032L;
	
	protected AbstractControllerSimple controller = null;
	protected int linkId = -1;
	
	
	public EventControllerSimple() { description = "Local Controller change at Node"; }
	public EventControllerSimple(int neid) {
		this();
		this.neid = neid;
	}
	public EventControllerSimple(int neid, AbstractControllerSimple ctrl,  int lkid) {
		this(neid);
		linkId = lkid;
		if (ctrl != null)
			controller = ctrl;
	}
	public EventControllerSimple(int neid, AbstractControllerSimple ctrl,  int lkid, double tstamp) {
		this(neid, ctrl, lkid);
		if (tstamp >= 0.0)
			this.tstamp = tstamp;
	}
	
	
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
					if (pp.item(i).getNodeName().equals("lkid"))
						linkId = Integer.parseInt(pp.item(i).getTextContent());
					if (pp.item(i).getNodeName().equals("controller")) {
						Class c = Class.forName(pp.item(i).getAttributes().getNamedItem("class").getNodeValue());
						controller = (AbstractControllerSimple)c.newInstance();
						res &= controller.initFromDOM(pp.item(i));
					}
				}
			}
			else
				res = false;
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}

	/**
	 * Generates XML description of the max queue Event.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		out.print("<event class=\"" + this.getClass().getName() + "\" neid=\"" + Integer.toString(neid) + "\" tstamp=\"" + Double.toString(tstamp) + "\" enabled=\"" + Boolean.toString(enabled) + "\">");
		out.print("<description>" + description + "</description>");
		out.print("<lkid>" + Integer.toString(linkId) + "</lkid>");
		if (controller != null)
			controller.xmlDump(out);
		out.print("</event>");
		return;
	}
	
	/**
	 * Changes controller for given Link at the assigned simple Node.
	 * @return <code>true</code> if operation succeded, <code>false</code> - otherwise.
	 * @throws ExceptionEvent
	 */
	public final boolean activate(AbstractNodeComplex top) throws ExceptionEvent {
		if (top == null)
			return false;
		super.activate(top);
		if (!enabled)
			return enabled;
		AbstractNode nd = top.getNodeById(neid);
		if (nd == null)
			throw new ExceptionEvent("Node (" + Integer.toString(neid) + ") not found.");
		if (!nd.isSimple())
			throw new ExceptionEvent(nd, "Wrong type.");
		AbstractLink lk = top.getLinkById(linkId);
		if (lk == null)
			throw new ExceptionEvent("Link (" + Integer.toString(linkId) + ") not found.");
		System.out.println("Event! Time " + Util.time2string(tstamp) + ": " + description);
		AbstractControllerSimple ctrl = ((AbstractNodeSimple)nd).getController(lk);
		boolean res = ((AbstractNodeSimple)nd).setController(controller, lk);
		controller = ctrl;
		return res;
	}
	
	/**
	 * Changes controller for given Link at the assigned simple Node back to what it was.
	 * @return <code>true</code> if operation succeded, <code>false</code> - otherwise.
	 * @throws ExceptionEvent
	 */
	public final boolean deactivate(AbstractNodeComplex top) throws ExceptionEvent {
		if (top == null)
			return false;
		if (!enabled)
			return enabled;
		AbstractNode nd = top.getNodeById(neid);
		if (nd == null)
			throw new ExceptionEvent("Node (" + Integer.toString(neid) + ") not found.");
		if (!nd.isSimple())
			throw new ExceptionEvent(nd, "Wrong type.");
		AbstractLink lk = top.getLinkById(linkId);
		if (lk == null)
			throw new ExceptionEvent("Link (" + Integer.toString(linkId) + ") not found.");
		System.out.println("Event rollback! Time " + Util.time2string(tstamp) + ": " + description);
		AbstractControllerSimple ctrl = ((AbstractNodeSimple)nd).getController(lk);
		boolean res = ((AbstractNodeSimple)nd).setController(controller, lk);
		controller = ctrl;
		return res;
	}
	
	/**
	 * Returns controller.
	 */
	public final AbstractControllerSimple getController() {
		return controller;
	}
	
	/**
	 * Returns Link identifier.
	 */
	public final int getLinkId() {
		return linkId;
	}
	
	/**
	 * Sets controller.<br>
	 * @param x simple controller object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setController(AbstractControllerSimple x) {
		controller = x;
		return true;
	}
	
	/**
	 * Sets Link identifier.<br>
	 * @param x Link Id.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setLinkId(int x) {
		if (x < 1)
			return false;
		linkId = x;
		return true;
	}

}
