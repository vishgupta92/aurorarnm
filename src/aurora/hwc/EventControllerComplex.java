/**
 * @(#)EventControllerComplex.java
 */

package aurora.hwc;

import java.io.*;
import org.w3c.dom.*;

import aurora.*;
import aurora.util.Util;


/**
 * Event that changes complex controller at given Monitor.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class EventControllerComplex extends AbstractEvent {
	private static final long serialVersionUID = 4872783738180737465L;
	
	protected AbstractControllerComplex controller = null;
	
	
	public EventControllerComplex() { description = "Complex Controller change at Monitor"; }
	public EventControllerComplex(int neid) {
		this();
		this.neid = neid;
	}
	public EventControllerComplex(int neid, AbstractControllerComplex ctrl) {
		this(neid);
		if (ctrl != null)
			controller = ctrl;
	}
	public EventControllerComplex(int neid, AbstractControllerComplex ctrl, double tstamp) {
		this(neid, ctrl);
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
		boolean res = super.initFromDOM(p);
		if (!res)
			return res;
		try  {
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (int i = 0; i < pp.getLength(); i++) {
					; //TODO
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
	 * Generates XML description of the max queue Event.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		if (controller != null)
			controller.xmlDump(out);
		out.print("</event>");
		return;
	}
	
	/**
	 * Changes controller for the assigned Monitor.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionEvent
	 */
	public final boolean activate(AbstractNodeComplex top) throws ExceptionEvent {
		if (top == null)
			return false;
		super.activate(top);
		if (!enabled)
			return enabled;
		AbstractMonitorController mon = (AbstractMonitorController)top.getMonitorById(neid);
		if (mon == null)
			throw new ExceptionEvent("Monitor (" + Integer.toString(neid) + ") not found.");
		System.out.println("Event! Time " + Util.time2string(tstamp) + ": " + description);
		AbstractControllerComplex ctrl = mon.getMyController();
		boolean res = mon.setMyController(controller);
		controller = ctrl;
		return res;
	}
	
	/**
	 * Changes controller for the assigned Monitor back to what it was.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionEvent
	 */
	public final boolean deactivate(AbstractNodeComplex top) throws ExceptionEvent {
		if (top == null)
			return false;
		if (!enabled)
			return enabled;
		AbstractMonitorController mon = (AbstractMonitorController)top.getMonitorById(neid);
		if (mon == null)
			throw new ExceptionEvent("Monitor (" + Integer.toString(neid) + ") not found.");
		System.out.println("Event rollback! Time " + Util.time2string(tstamp) + ": " + description);
		AbstractControllerComplex ctrl = mon.getMyController();
		boolean res = mon.setMyController(controller);
		controller = ctrl;
		return res;
	}
	
	/**
	 * Returns type description. 
	 */
	public final String getTypeString() {
		return "Complex Controller";
	}
	
	/**
	 * Returns letter code of the event type.
	 */
	public final String getTypeLetterCode() {
		return "CCONTROL";
	}
	
	/**
	 * Returns controller.
	 */
	public final AbstractControllerComplex getController() {
		return controller;
	}

}
