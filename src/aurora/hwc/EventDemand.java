/**
 * @(#)EventDemand.java 
 */

package aurora.hwc;

import java.io.*;
import org.w3c.dom.*;
import aurora.*;
import aurora.util.Util;


/**
 * Event that changes the demand knob value on a given Link.
 * @author Alex Kurzhanskiy
 * @version $Id: EventDemand.java,v 1.4.2.6.2.1 2009/06/14 01:10:24 akurzhan Exp $
 */
public final class EventDemand extends AbstractEvent {
	private static final long serialVersionUID = 6391985618194128997L;
	
	protected double knob = 1.0;
	
	
	public EventDemand() { description = "Demand Coefficient change at Link"; }
	public EventDemand(int neid) {
		this();
		this.neid = neid;
	}
	public EventDemand(int neid, double knob) {
		this(neid);
		if (knob >= 0.0)
			this.knob = knob;
	}
	public EventDemand(int neid, double knob, double tstamp) {
		this(neid, knob);
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
					if (pp.item(i).getNodeName().equals("demand"))
						knob = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("knob").getNodeValue());
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
	 * Generates XML description of the demand Event.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		out.print("<demand knob=\"" + Double.toString(knob) + "\" />");
		out.print("</event>");
		return;
	}
	
	/**
	 * Changes the demand value for the assigned Link.
	 * @return <code>true</code> if operation succeded, <code>false</code> - otherwise.
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
		double v = ((AbstractLinkHWC)alnk).getDemandKnob();
		boolean res = ((AbstractLinkHWC)alnk).setDemandKnob(knob);
		knob = v;
		return res;
	}
	
	/**
	 * Changes the demand value for the assigned Link back to wah it was.
	 * @return <code>true</code> if operation succeded, <code>false</code> - otherwise.
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
		double v = ((AbstractLinkHWC)alnk).getDemandKnob();
		boolean res = ((AbstractLinkHWC)alnk).setDemandKnob(knob);
		knob = v;
		return res;
	}
	
	/**
	 * Returns demand.
	 */
	public final double getDemandKnob() {
		return knob;
	}
	
	/**
	 * Sets demand coefficient.<br>
	 * @param x demand coefficient value.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDemandKnob(double x) {
		if (x >= 0.0)
			knob = x;
		else
			return false;
		return true;
	}
	
}