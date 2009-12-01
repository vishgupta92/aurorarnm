/**
 * @(#)EventDemand.java 
 */

package aurora.hwc;

import java.io.*;
import java.util.StringTokenizer;
import org.w3c.dom.*;
import aurora.*;
import aurora.util.Util;


/**
 * Event that changes the demand knob value on a given Link.
 * @author Alex Kurzhanskiy
 * @version $Id: EventDemand.java,v 1.4.2.6.2.1.2.3 2009/10/18 01:30:35 akurzhan Exp $
 */
public final class EventDemand extends AbstractEvent {
	private static final long serialVersionUID = 6391985618194128997L;
	
	protected double[] knobs = new double[1];
	
	
	public EventDemand() { knobs[0] = 1; description = "Demand change at Link"; }
	public EventDemand(int neid) {
		this();
		this.neid = neid;
	}
	public EventDemand(int neid, double[] knobs) {
		this(neid);
		this.knobs[0] = 1;
		if ((knobs != null) && (knobs.length > 0)) {
			this.knobs = new double[knobs.length];
			for (int i = 0; i < knobs.length; i++)
				this.knobs[i] = Math.max(0, knobs[i]);
		}
	}
	public EventDemand(int neid, double[] knobs, double tstamp) {
		this(neid, knobs);
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
						setDemandKnobs(pp.item(i).getAttributes().getNamedItem("knob").getNodeValue());
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
		out.print("<demand knob=\"" + getDemandKnobsAsString() + "\" />");
		out.print("</event>");
		return;
	}
	
	/**
	 * Changes the demand value for the assigned Link.
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
		double[] v = ((AbstractLinkHWC)alnk).getDemandKnobs();
		boolean res = ((AbstractLinkHWC)alnk).setDemandKnobs(knobs);
		setDemandKnobs(v);
		return res;
	}
	
	/**
	 * Changes the demand value for the assigned Link back to wah it was.
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
		double[] v = ((AbstractLinkHWC)alnk).getDemandKnobs();
		boolean res = ((AbstractLinkHWC)alnk).setDemandKnobs(knobs);
		setDemandKnobs(v);
		return res;
	}
	
	/**
	 * Returns type description. 
	 */
	public final String getTypeString() {
		return "Demand";
	}
	
	/**
	 * Returns demand knob values.
	 */
	public final double[] getDemandKnobs() {
		double[] v = new double[knobs.length];
		for (int i = 0; i < knobs.length; i++)
			v[i] = knobs[i]; 
		return v;
	}
	
	/**
	 * Returns demand knob values as string.
	 */
	public final String getDemandKnobsAsString() {
		boolean allequal = true;
		String buf = "";
		double val = knobs[0];
		for (int i = 0; i < knobs.length; i++) {
			if (i > 0) {
				buf += ":";
				if (knobs[i] != val)
					allequal = false;
			}
			buf += Double.toString(knobs[i]);
		}
		if (allequal)
			buf = Double.toString(val);
		return buf;
	}
	
	/**
	 * Sets demand knob values.
	 * @param x demand knob values.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDemandKnobs(double[] x) {
		if ((x == null) || (x.length < 1))
			return false;
		knobs = new double[x.length];
		for (int i = 0; i < x.length; i++)
			knobs[i] = Math.max(0, x[i]);
		return true;
	}
	
	/**
	 * Sets demand knob values from given string buffer.
	 * @param x string with column separated demand knob values.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDemandKnobs(String x) {
		if ((x == null) || (x.isEmpty()))
			return false;
		StringTokenizer st = new StringTokenizer(x, ": ");
		if (st.countTokens() < 1)
			return false;
		int sz = st.countTokens();
		knobs = new double[sz];
		for (int i = 0; i < sz; i++) {
			try {
				knobs[i] = Math.max(0, Double.parseDouble(st.nextToken()));
			}
			catch(Exception e) {
				knobs[i] = 1;
			}
		}
		return true;
	}
	
}