/**
 * @(#)ControllerALINEA.java
 */

package aurora.hwc.control;

import java.io.*;
import java.text.NumberFormat;
import org.w3c.dom.*;
import aurora.*;
import aurora.hwc.*;
import aurora.util.Util;


/**
 * Implementation of ALINEA controller.
 * @author Alex Kurzhanskiy
 * @version $Id: ControllerALINEA.java,v 1.1.4.1.2.5.2.1 2009/08/16 19:16:40 akurzhan Exp $
 */
public final class ControllerALINEA extends AbstractControllerHWC {
	private static final long serialVersionUID = 4440581708032401841L;
	
	public boolean upstream = false;
	public double gain = 50.0;

	
	public ControllerALINEA() { super(); }
	public ControllerALINEA(QueueController qc) {
		super();
		myQController = qc;
	}
	public ControllerALINEA(double cMin, double cMax) {
		super();
		if (cMin < cMax) {
			limits.set(0, (Double)cMin);
			limits.set(1, (Double)cMax);
		}
		myQController = null;
	}
	public ControllerALINEA(double cMin, double cMax, QueueController qc) {
		super();
		if (cMin < cMax) {
			limits.set(0, (Double)cMin);
			limits.set(1, (Double)cMax);
		}
		myQController = qc;
	}
	
	
	/**
	 * Initializes the ALINEA controller from given DOM structure.
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
					if (pp.item(i).getNodeName().equals("parameter")) {
						if (pp.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("gain"))
							gain = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("value").getNodeValue());
						if (pp.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("upstream"))
							upstream = Boolean.parseBoolean(pp.item(i).getAttributes().getNamedItem("value").getNodeValue());
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
	 * Generates XML description of the ALINEA controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		out.print("<parameter name=\"upstream\" value=\"" + Boolean.toString(upstream) + "\"/>");
		out.print("<parameter name=\"gain\" value=\"" + Double.toString(gain) + "\"/>");
		out.print("</controller>");
		return;
	}
	
	/**
	 * Computes desired input flow for given Node.
	 * @param xx given Node.
	 * @return input flow.
	 */
	public synchronized Object computeInput(AbstractNodeSimple xx) {
		AbstractNodeHWC x = (AbstractNodeHWC)xx;
		Double flw = (Double)super.computeInput(x);
		if (flw != null)
			return flw;
		int idx = x.getSimpleControllers().indexOf(this);
		AbstractLinkHWC lnk = (AbstractLinkHWC)x.getPredecessors().get(idx);
		if (input == null)
			input = (Double)limits.get(1);
		int ii = 0;
		AbstractLinkHWC tlk;
		if (upstream) { // ATTENTION: not entirely clear how to choose in-link
			for (int i = 0; i < x.getPredecessors().size(); i++)
				if ((x.getPredecessors().get(i).getType() == TypesHWC.LINK_FREEWAY) ||
					(x.getPredecessors().get(i).getType() == TypesHWC.LINK_HIGHWAY)) {
					ii = i;
					break;
				}
			tlk = (AbstractLinkHWC)x.getPredecessors().get(ii);
		}
		else {
			double sr = 0.0;
			for (int i = 0; i < x.getOutputs().size(); i++) {
				double val = 0.0;
				if (x.getOutputs().get(i) != null)
					val = ((AuroraIntervalVector)x.getOutputs().get(i)).sum().getUpperBound();
				if (val > sr) {
					ii = i;
					sr = val;
				}
			}
			tlk = (AbstractLinkHWC)x.getSuccessors().get(ii);
		}
		double occ_des = tlk.getCriticalDensity();// / tlk.getJamDensity();
		double occ_act = ((AuroraIntervalVector)tlk.getDensity()).sum().getCenter();//tlk.getOccupancy();
		flw = ((Double)input)  +  gain * (occ_des - occ_act);
		if ((((NodeHWCNetwork)xx.getMyNetwork()).hasQControl()) && (myQController != null)) 
			flw = Math.max(flw, (Double)myQController.computeInput(x, lnk));
		flw = Math.min((Double)limits.get(1), Math.max(flw, (Double)limits.get(0)));
		input = (Double)Math.max(flw, 0.0);
		allowInputSet = true;
		return input;
	}
	
	/**
	 * Returns controller description.
	 */
	public final String getDescription() {
		NumberFormat form = NumberFormat.getInstance();
		form.setMinimumFractionDigits(2);
		form.setMaximumFractionDigits(2);
		String buf = "ALINEA (period = " + Util.time2string(tp) + "; gain = " + form.format(gain);
		if (upstream)
			buf += "; upstream)";
		else
			buf += ")";
		return buf;
	}
	
	/**
	 * Returns mask for compatible Node types.
	 */
	public final int getCompatibleNodeTypes() {
		return (((~TypesHWC.MASK_NODE) & TypesHWC.NODE_FREEWAY) | ((~TypesHWC.MASK_NODE) & TypesHWC.NODE_HIGHWAY));
	}
	
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Controller.
	 */
	public String toString() {
		return "ALINEA";
	}
}
