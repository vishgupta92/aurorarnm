/**
 * @(#)EventQueueMax.java 
 */

package aurora.hwc;

import java.io.*;
import org.w3c.dom.*;
import aurora.*;
import aurora.util.Util;


/**
 * Event that changes queue limit on the assigned Link.
 * @author Alex Kurzhanskiy
 * @version $Id: EventQueueMax.java,v 1.4.2.4.2.1 2008/12/02 03:07:20 akurzhan Exp $
 */
public final class EventQueueMax extends AbstractEvent {
	private static final long serialVersionUID = 7217681145706831869L;
	
	protected double qMax = 100.0;
	
	
	public EventQueueMax() { description = "Queue Limit change at Link"; }
	public EventQueueMax(int neid) {
		this();
		this.neid = neid;
	}
	public EventQueueMax(int neid, double qmax) {
		this(neid);
		if (qmax >= 0.0)
			qMax = qmax;
	}
	public EventQueueMax(int neid, double qmax, double tstamp) {
		this(neid, qmax);
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
					if (pp.item(i).getNodeName().equals("qmax"))
						qMax = Double.parseDouble(pp.item(i).getTextContent());
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
		out.print("<qmax>" + Double.toString(qMax) + "</qmax>");
		out.print("</event>");
		return;
	}
	
	/**
	 * Changes the queue limit for the assigned Link.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionEvent
	 */
	public final boolean activate(AbstractNodeComplex top) throws ExceptionEvent {
		if (top == null)
			return false;
		super.activate(top);
		if (!enabled)
			return enabled;
		AbstractLink alnk = top.getLinkById(neid);
		if (alnk == null)
			throw new ExceptionEvent("Link (" + Integer.toString(neid) + ") not found.");
		System.out.println("Event! Time " + Util.time2string(tstamp) + ": " + description);
		double v = ((AbstractLinkHWC)alnk).getQueueMax();
		boolean res = ((AbstractLinkHWC)alnk).setQueueMax(qMax);
		qMax = v;
		return res;
	}
	
	/**
	 * Changes the queue limit for the assigned Link back to what it was.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionEvent
	 */
	public final boolean deactivate(AbstractNodeComplex top) throws ExceptionEvent {
		if (top == null)
			return false;
		if (!enabled)
			return enabled;
		AbstractLink alnk = top.getLinkById(neid);
		if (alnk == null)
			throw new ExceptionEvent("Link (" + Integer.toString(neid) + ") not found.");
		System.out.println("Event rollback! Time " + Util.time2string(tstamp) + ": " + description);
		double v = ((AbstractLinkHWC)alnk).getQueueMax();
		boolean res = ((AbstractLinkHWC)alnk).setQueueMax(qMax);
		qMax = v;
		return res;
	}
	
	/**
	 * Returns queue limit.
	 */
	public final double getQueueMax() {
		return qMax;
	}

	/**
	 * Sets queue limit.<br>
	 * @param x queue limit value.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setQueueMax(double x) {
		if (x >= 0.0)
			qMax = x;
		else
			return false;
		return true;
	}
	
}