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
 * @version $Id$
 */
public final class EventFD extends AbstractEvent {
	private static final long serialVersionUID = 2419264559064698446L;
	
	protected double flowMax = 2400;
	protected double intervalSize = 0;
	protected double capacityDrop = 0;
	protected double densityCritical = 40;
	protected double densityJam = 160;
	protected double fraction = -1;

	
	public EventFD() { description = "Fundamental Diagram change at Link"; }
	public EventFD(int neid) {
		this();
		this.neid = neid;
	}
	public EventFD(int neid, double fmax, double rhoc, double rhoj, double cdrp) {
		this(neid);
		if (fmax >= 0.0) {
			flowMax = fmax;
			capacityDrop = Math.max(0, Math.min(fmax, cdrp));
		}
		if ((rhoc >= 0.0) && (rhoj >= 0.0) && (rhoc <= rhoj)) {
			densityCritical = rhoc;
			densityJam = rhoj;
		}
	}
	public EventFD(int neid, double fmax, double rhoc, double rhoj, double cdrp, double tstamp) {
		this(neid, fmax, rhoc, rhoj, cdrp);
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
					if (pp.item(i).getNodeName().equals("fd")) {
						NamedNodeMap attr = pp.item(i).getAttributes();
						AuroraInterval fm = new AuroraInterval();
						fm.setIntervalFromString(attr.getNamedItem("flowMax").getNodeValue());
						flowMax = fm.getCenter();
						intervalSize = fm.getSize();
						densityCritical = Double.parseDouble(attr.getNamedItem("densityCritical").getNodeValue());
						densityJam = Double.parseDouble(attr.getNamedItem("densityJam").getNodeValue());
						Node cdp = attr.getNamedItem("capacityDrop");
						if (cdp != null)
							capacityDrop = Double.parseDouble(cdp.getNodeValue());
						Node cp = attr.getNamedItem("fdScale");
						if (cp != null) {
							fraction = Double.parseDouble(cp.getNodeValue());
							if (fraction >= 0)
								fraction = Math.max(0.0001, fraction);
						}
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
		super.xmlDump(out);
		AuroraInterval fm = new AuroraInterval(flowMax, intervalSize);
		out.print("<fd densityCritical=\"" + Double.toString(densityCritical) + "\" densityJam=\"" + Double.toString(densityJam) + "\" flowMax=\"" + fm.toString() + "\"");
		if (capacityDrop > 0)
			out.print(" capacityDrop=\"" + Double.toString(capacityDrop) + "\"");
		if (fraction > 0)
			out.print(" fdScale=\"" + Double.toString(fraction) + "\"");
		out.print(" /></event>");
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
		double sz = ((AbstractLinkHWC)alnk).getMaxFlowRange().getSize();
		double cd = ((AbstractLinkHWC)alnk).getCapacityDrop();
		boolean res = true;
		if (fraction > 0) {
			res &= ((AbstractLinkHWC)alnk).setFD(fraction * fm, fraction * dc, fraction * dj, fraction * cd);
			res &= ((AbstractLinkHWC)alnk).setMaxFlowRange(fraction * sz);
		}
		else {
			res &= ((AbstractLinkHWC)alnk).setFD(flowMax, densityCritical, densityJam, capacityDrop);
			res &= ((AbstractLinkHWC)alnk).setMaxFlowRange(sz);
		}
		res &= ((AbstractLinkHWC)alnk).randomizeFD();
		densityCritical = dc;
		densityJam = dj;
		flowMax = fm;
		intervalSize = sz;
		capacityDrop = cd;
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
		double sz = ((AbstractLinkHWC)alnk).getMaxFlowRange().getSize();
		double cd = ((AbstractLinkHWC)alnk).getCapacityDrop();
		boolean res = ((AbstractLinkHWC)alnk).setFD(flowMax, densityCritical, densityJam, capacityDrop);
		res &= ((AbstractLinkHWC)alnk).setMaxFlowRange(sz);
		res &= ((AbstractLinkHWC)alnk).randomizeFD();
		densityCritical = dc;
		densityJam = dj;
		flowMax = fm;
		intervalSize = sz;
		capacityDrop = cd;
		return res;
	}
	
	/**
	 * Returns type description. 
	 */
	public final String getTypeString() {
		return "Fundamental Diagram";
	}
	
	/**
	 * Returns capacity of the link.
	 */
	public final double getMaxFlow() {
		return flowMax;
	}
	
	/**
	 * Returns capacity interval size.
	 */
	public final double getIntervalSize() {
		return intervalSize;
	}
	
	/**
	 * Returns capacity drop at congestion.
	 */
	public final double getCapacityDrop() {
		return capacityDrop;
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
	public synchronized boolean setFD(double fmax, double rhoc, double rhoj, double cdrp) {
		boolean res = false;
		if ((fmax >= 0.0) && (rhoc >= 0.0) && (rhoj >= 0.0) && (rhoc <= rhoj)) {
			flowMax = fmax;
			capacityDrop = Math.max(0, Math.min(fmax, cdrp));
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
		if (x <= 0.0)
			return false;
		densityCritical = flowMax / x;
		return true;
	}
	
	/**
	 * Modifies fundamental diagram by assigning congestion wave speed.<br>
	 * Maximum flow and critical density remain untouched.
	 * @param x congestion wave speed.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setW(double x) { // max flow & critical density are fixed
		if (x <= 0.0)
			return false;
		densityJam = densityCritical + (flowMax / x);
		return true;
	}
	
	/**
	 * Sets capacity interval size.
	 * @param x interval size.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setIntervalSize(double x) {
		if (x < 0.0)
			return false;
		intervalSize = x;
		return true;
	}

}