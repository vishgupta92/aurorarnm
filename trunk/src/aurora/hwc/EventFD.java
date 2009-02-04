/**
 * @(#)EventFD.java 
 */

package aurora.hwc;

import java.io.*;
import org.w3c.dom.*;
import aurora.*;
import aurora.util.Util;


/**
 * Event that changes the fundamental diagram parameters
 * on the given Link.
 * @author Alex Kurzhanskiy
 * @version $Id: EventFD.java,v 1.4.2.5.2.1 2008/12/02 03:02:55 akurzhan Exp $
 */
public final class EventFD extends AbstractEvent {
	private static final long serialVersionUID = 2419264559064698446L;
	
	protected double flowMax = 2400;
	protected double densityCritical = 40;
	protected double densityJam = 160;

	
	public EventFD() { description = "Fundamental Diagram change at Link"; }
	public EventFD(int neid) {
		this();
		this.neid = neid;
	}
	public EventFD(int neid, double fmax, double rhoc, double rhoj) {
		this(neid);
		if (fmax >= 0.0)
			flowMax = fmax;
		if ((rhoc >= 0.0) && (rhoj >= 0.0) && (rhoc <= rhoj)) {
			densityCritical = rhoc;
			densityJam = rhoj;
		}
	}
	public EventFD(int neid, double fmax, double rhoc, double rhoj, double tstamp) {
		this(neid, fmax, rhoc, rhoj);
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
					if (pp.item(i).getNodeName().equals("fd")) {
						NamedNodeMap attr = pp.item(i).getAttributes();
						flowMax = Double.parseDouble(attr.getNamedItem("flowMax").getNodeValue());
						densityCritical = Double.parseDouble(attr.getNamedItem("densityCritical").getNodeValue());
						densityJam = Double.parseDouble(attr.getNamedItem("densityJam").getNodeValue());
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
		if ((flowMax < 0) || (densityCritical < 0) || (densityJam < 0) || (densityJam < densityCritical))
			res = false;
		return res;
	}

	/**
	 * Generates XML description of the fundamental diagram Event.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		out.print("<event class=\"" + this.getClass().getName() + "\" neid=\"" + Integer.toString(neid) + "\" tstamp=\"" + Double.toString(tstamp) + "\" enabled=\"" + Boolean.toString(enabled) + "\">");
		out.print("<description>" + description + "</description>");
		out.print("<fd densityCritical=\"" + Double.toString(densityCritical) + "\" densityJam=\"" + Double.toString(densityJam) + "\" flowMax=\"" + Double.toString(flowMax) + "\"/>");
		out.print("</event>");
		return;
	}
	
	/**
	 * Changes the fundamental diagram values for the assigned Link.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionEvent
	 */
	public boolean activate(AbstractNodeComplex top) throws ExceptionEvent {
		if (top == null)
			return false;
		super.activate(top);
		if (!enabled)
			return enabled;
		AbstractLink alnk = top.getLinkById(neid);
		if (alnk == null)
			throw new ExceptionEvent("Link (" + Integer.toString(neid) + ") not found.");
		System.out.println("Event! Time " + Util.time2string(tstamp) + ": " + description);
		double dc = ((AbstractLinkHWC)alnk).getCriticalDensity();
		double dj = ((AbstractLinkHWC)alnk).getJamDensity();
		double fm = ((AbstractLinkHWC)alnk).getMaxFlow();
		boolean res = ((AbstractLinkHWC)alnk).setFD(flowMax, densityCritical, densityJam);
		densityCritical = dc;
		densityJam = dj;
		flowMax = fm;
		return res;
	}
	
	/**
	 * Changes the fundamental diagram values for the assigned Link back to what it was.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionEvent
	 */
	public boolean deactivate(AbstractNodeComplex top) throws ExceptionEvent {
		if (top == null)
			return false;
		if (!enabled)
			return enabled;
		AbstractLink alnk = top.getLinkById(neid);
		if (alnk == null)
			throw new ExceptionEvent("Link (" + Integer.toString(neid) + ") not found.");
		System.out.println("Event rollback! Time " + Util.time2string(tstamp) + ": " + description);
		double dc = ((AbstractLinkHWC)alnk).getCriticalDensity();
		double dj = ((AbstractLinkHWC)alnk).getJamDensity();
		double fm = ((AbstractLinkHWC)alnk).getMaxFlow();
		boolean res = ((AbstractLinkHWC)alnk).setFD(flowMax, densityCritical, densityJam);
		densityCritical = dc;
		densityJam = dj;
		flowMax = fm;
		return res;
	}
	
	/**
	 * Returns maximum flow that can be achieved in the link.
	 */
	public final double getMaxFlow() {
		return flowMax;
	}
	
	/**
	 * Returns critical density for the link.
	 */
	public final double getCriticalDensity() {
		return densityCritical;
	}
	
	/**
	 * Returns jam density for the link.
	 */
	public final double getJamDensity() {
		return densityJam;
	}
	
	/**
	 * Returns free flow speed.
	 */
	public final double getV() { // free-flow speed
		return (flowMax / densityCritical);
	}
	
	/**
	 * Returns congestion wave speed.
	 */
	public final double getW() { // congestion wave speed
		return (flowMax / (densityJam - densityCritical));
	}
	
	/**
	 * Assigns parameters of the fundamental diagram.
	 * @param fmax maximum flow.
	 * @param rhoc critical density.
	 * @param rhoj jam density.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setFD(double fmax, double rhoc, double rhoj) {
		boolean res = false;
		if ((fmax >= 0.0) && (rhoc >= 0.0) && (rhoj >= 0.0) && (rhoc <= rhoj)) {
			flowMax = fmax;
			densityCritical = rhoc;
			densityJam = rhoj;
			res = true;
		}
		return res;
	}
	
	/**
	 * Modifies fundamental diagram by assigning free flow speed.<br>
	 * Maximum flow and jam density remain untouched.
	 * @param x free flow speed.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setV(double x) { // max flow & jam density are fixed
		if (x > 0.0)
			densityCritical = flowMax / x;
		else
			return false;
		return true;
	}
	
	/**
	 * Modifies fundamental diagram by assigning congestion wave speed.<br>
	 * Maximum flow and critical density remain untouched.
	 * @param x congestion wave speed.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setW(double x) { // max flow & critical density are fixed
		if (x > 0.0)
			densityJam = densityCritical + (flowMax / x);
		else
			return false;
		return true;
	}

}