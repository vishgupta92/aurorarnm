/**
 * @(#)EventNetworkControl.java 
 */

package aurora.hwc;

import java.io.*;
import org.w3c.dom.*;
import aurora.*;
import aurora.util.Util;


/**
 * Event that changes mainline and queue control modes on a given complex Node.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class EventNetworkControl extends AbstractEvent {
	private static final long serialVersionUID = -8985021937408207013L;
	
	protected boolean controlled = true;
	protected boolean qControl = false;
	
	
	public EventNetworkControl() { description = "Control state change"; }
	public EventNetworkControl(int neid) {
		this();
		this.neid = neid;
	}
	public EventNetworkControl(int neid, boolean cv) {
		this(neid);
		controlled = cv;
	}
	public EventNetworkControl(int neid, boolean cv, boolean qcv) {
		this(neid, cv);
		qControl = qcv;
	}
	public EventNetworkControl(int neid, boolean cv, boolean qcv, double tstamp) {
		this(neid, cv, qcv);
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
					if (pp.item(i).getNodeName().equals("control")) {
						controlled = Boolean.parseBoolean(pp.item(i).getAttributes().getNamedItem("mainline").getNodeValue());
						qControl = Boolean.parseBoolean(pp.item(i).getAttributes().getNamedItem("queue").getNodeValue());
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
	 * Generates XML description of the network control Event.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		out.print("<control mainline=\"" + controlled + "\" queue=\"" + qControl + "\" />");
		out.print("</event>");
		return;
	}
	
	/**
	 * Changes the control mode for a given network.
	 * @return <code>true</code> if operation succeded, <code>false</code> - otherwise.
	 * @throws ExceptionEvent
	 */
	public final boolean activate(AbstractNodeComplex top) throws ExceptionEvent {
		if (top == null)
			return false;
		super.activate(top);
		if (!enabled)
			return enabled;
		NodeHWCNetwork ntwk;
		ntwk = (NodeHWCNetwork)top.getNodeById(neid);
		if (ntwk == null)
			throw new ExceptionEvent("Network (" + neid + ") not found.");
		System.out.println("Event! Time " + Util.time2string(tstamp) + ": " + description);
		boolean cv = ntwk.isControlled();
		boolean qcv = ntwk.hasQControl();
		boolean res = ntwk.setControlled(controlled, qControl);
		controlled = cv;
		qControl = qcv;
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
		NodeHWCNetwork ntwk;
		if (top.getId() == neid)
			ntwk = (NodeHWCNetwork)top;
		else
			ntwk = (NodeHWCNetwork)top.getNodeById(neid);
		if (ntwk == null)
			throw new ExceptionEvent("Network (" + neid + ") not found.");
		System.out.println("Event rollback! Time " + Util.time2string(tstamp) + ": " + description);
		boolean cv = ntwk.isControlled();
		boolean qcv = ntwk.hasQControl();
		boolean res = ntwk.setControlled(controlled, qControl);
		controlled = cv;
		qControl = qcv;
		return res;
	}
	
	/**
	 * Returns type description. 
	 */
	public final String getTypeString() {
		return "Network Control";
	}
	
	/**
	 * Returns letter code of the event type.
	 */
	public final String getTypeLetterCode() {
		return "TCONTROL";
	}
	
	/**
	 * Returns <code>true</code> if control is on, <code>false</code> - otherwise.
	 */
	public final boolean hasControl() {
		return controlled;
	}
	
	/**
	 * Returns <code>true</code> if queue control is on, <code>false</code> otherwise.
	 */
	public boolean hasQControl() {
		return qControl;
	}
	
	/**
	 * Sets mainline control mode On/Off.
	 * @param x true/false value..
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setControl(boolean x) {
		controlled = x;
		return true;
	}
	
	/**
	 * Sets queue control mode On/Off.
	 * @param x true/false value..
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setQControl(boolean x) {
		qControl = x;
		return true;
	}
	
}