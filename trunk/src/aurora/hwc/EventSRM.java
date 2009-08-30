/**
 * @(#)EventSRM.java 
 */

package aurora.hwc;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import aurora.*;
import aurora.util.Util;


/**
 * Event that changes split ratio matrix at given simple Node.
 * @author Alex Kurzhanskiy
 * @version $Id: EventSRM.java,v 1.4.2.4.2.3 2009/06/14 01:10:24 akurzhan Exp $
 */
public final class EventSRM extends AbstractEvent {
	private static final long serialVersionUID = -8952099927246173708L;
	
	protected AuroraIntervalVector[][] splitRatioMatrix = null;
	
	
	public EventSRM() { description = "Split Ratio Matrix change at Node"; }
	public EventSRM(int neid) {
		this();
		this.neid = neid;
	}
	public EventSRM(int neid, AuroraIntervalVector[][] srm) {
		this(neid);
		if (srm != null) {
			int m = srm.length;
			int n = srm[0].length;
			splitRatioMatrix = new AuroraIntervalVector[m][n];
			for (int i = 0; i < m; i++)
				for (int j = 0; j < n; j++) {
					splitRatioMatrix[i][j] = new AuroraIntervalVector();
					splitRatioMatrix[i][j].copy(srm[i][j]);
				}
		}
	}
	public EventSRM(int neid, AuroraIntervalVector[][] srm, double tstamp) {
		this(neid, srm);
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
					if (pp.item(i).getNodeName().equals("srm")) {
						if (pp.item(i).hasChildNodes()) {
							NodeList pp2 = pp.item(i).getChildNodes();
							int m = 0;
							int n = 0;
							for (int j = 0; j < pp2.getLength(); j++)
								if (pp2.item(j).getNodeName().equals("splitratios")) {
									StringTokenizer st = new StringTokenizer(pp2.item(j).getTextContent(), ", \t");
									if (st.countTokens() > n)
										n = st.countTokens();
									m++;
								}
							splitRatioMatrix = new AuroraIntervalVector[m][n];
							int sz = ((SimulationSettingsHWC)getEventManager().getContainer().getMySettings()).countVehicleTypes();
							m = 0;
							for (int j = 0; j < pp2.getLength(); j++)
								if (pp2.item(j).getNodeName().equals("splitratios")) {
									StringTokenizer st = new StringTokenizer(pp2.item(j).getTextContent(), ", \t");
									int mm = 0;
									while (st.hasMoreTokens()) {
										splitRatioMatrix[m][mm] = new AuroraIntervalVector(sz);
										splitRatioMatrix[m][mm].setIntervalVectorFromString(st.nextToken());
										mm++;
									}
									while (mm < n) {
										splitRatioMatrix[m][mm] = new AuroraIntervalVector(sz);
										mm++;
									}
									m++;
								}
						}
						else
							res = false;
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
	 * Generates XML description of the split ratio matrix Event.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		out.print("<srm>");
		for (int i = 0; i < splitRatioMatrix.length; i++) {
			String buf = "";
			for (int j = 0; j < splitRatioMatrix[0].length; j++) {
				if (j > 0)
					buf += ", ";
				buf += splitRatioMatrix[i][j].toString();
			}
			out.print("<splitratios>" + buf + "</splitratios>");
		}
		out.print("</srm></event>");
		return;
	}
	
	/**
	 * Changes split ratio matrix for the assigned simple Node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
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
		System.out.println("Event! Time " + Util.time2string(tstamp) + ": " + description);
		AuroraIntervalVector[][] srm = ((AbstractNodeHWC)nd).getSplitRatioMatrix0();
		boolean isNull = true;
		if (splitRatioMatrix != null) {
			int nIn = splitRatioMatrix.length;
			int nOut = splitRatioMatrix[0].length;
			for (int i = 0; i < nIn; i++)
				for (int j = 0; j < nOut; j++)
					if ((splitRatioMatrix[i][j].maxCenter() != 0) || (splitRatioMatrix[i][j].minCenter() != 0))
						isNull = false;
		}
		boolean res;
		if (isNull)
			res = ((AbstractNodeHWC)nd).setSplitRatioMatrix0(null);
		else
			res = ((AbstractNodeHWC)nd).setSplitRatioMatrix0(splitRatioMatrix);
		splitRatioMatrix = srm;
		return res;
	}
	
	/**
	 * Changes split ratio matrix for the assigned simple Node back to what it was.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
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
		System.out.println("Event rollback! Time " + Util.time2string(tstamp) + ": " + description);
		AuroraIntervalVector[][] srm = ((AbstractNodeHWC)nd).getSplitRatioMatrix0();
		boolean res = ((AbstractNodeHWC)nd).setSplitRatioMatrix0(splitRatioMatrix);
		splitRatioMatrix = srm;
		return res;
	}
	
	/**
	 * Returns split ratio matrix.
	 */
	public AuroraIntervalVector[][] getSplitRatioMatrix() {
		if (splitRatioMatrix == null)
			return null;
		int m = splitRatioMatrix.length;
		int n = splitRatioMatrix[0].length;
		AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++) {
				srm[i][j] = new AuroraIntervalVector();
				srm[i][j].copy(splitRatioMatrix[i][j]);
			}
		return srm;
	}

	/**
	 * Sets split ratio matrix.<br>
	 * @param x split ratio matrix.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setSplitRatioMatrix(AuroraIntervalVector[][] x) {
		if (x == null)
			return false;
		int m = x.length;
		int n = x[0].length;
		splitRatioMatrix = new AuroraIntervalVector[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++) {
				splitRatioMatrix[i][j] = new AuroraIntervalVector();
				splitRatioMatrix[i][j].copy(x[i][j]);
			}
		return true;
	}
	
}
