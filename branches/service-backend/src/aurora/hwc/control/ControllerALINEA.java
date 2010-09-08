/**
 * @(#)ControllerALINEA.java
 */

package aurora.hwc.control;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import org.w3c.dom.*;
import aurora.*;
import aurora.hwc.*;
import aurora.util.Util;


/**
 * Implementation of ALINEA controller.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class ControllerALINEA extends AbstractControllerSimpleHWC {
	private static final long serialVersionUID = 4440581708032401841L;
	
	public boolean upstream = false;
	public double gain = 50.0;
	
	private Vector<AbstractLinkHWC> MLlinks = new Vector<AbstractLinkHWC>();
	private SensorLoopDetector MLsensor;

	// CONSTRUCTORS ========================================================================
	public ControllerALINEA() { super(); }
	
	public ControllerALINEA(AbstractQueueController qc) {
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
	
	public ControllerALINEA(double cMin, double cMax, AbstractQueueController qc) {
		super();
		if (cMin < cMax) {
			limits.set(0, (Double)cMin);
			limits.set(1, (Double)cMax);
		}
		myQController = qc;
	}
	

	// XMLREAD, VALIDATE, INITIALIZE, XMLDUMP ==============================================

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
						if (pp.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("link")) {
							int lid = Integer.parseInt(pp.item(i).getAttributes().getNamedItem("value").getNodeValue());
							AbstractLinkHWC lk = (AbstractLinkHWC)myLink.getMyNetwork().getTop().getLinkById(lid);
							if (lk != null)
								MLlinks.add(lk);
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
		return res;
	}
	
	/**
	 * Additional initialization.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initialize() throws ExceptionConfiguration {
		AbstractNodeHWC x = (AbstractNodeHWC) myLink.getEndNode();
		if (MLlinks.size() == 0) {
			AbstractLinkHWC lk = null;
			if (upstream)
				lk = getUpML(x);
			else 
				lk = getDnML(x);
			MLlinks.add(lk);
		}
		if(usesensors)
			MLsensor = (SensorLoopDetector) MLlinks.firstElement().getMyNetwork().getSensorByLinkId(MLlinks.firstElement().getId());
		else
			MLsensor = null;
		return super.initialize();
	}

	/**
	 * Generates XML description of the ALINEA controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		out.println("<parameter name=\"upstream\" value=\"" + Boolean.toString(upstream) + "\"/>");
		out.println("<parameter name=\"gain\" value=\"" + Double.toString(gain) + "\"/>");
		for (int i = 0; i < MLlinks.size(); i++)
			out.println("<parameter name=\"link\" value=\"" + MLlinks.get(i).getId() + "\"/>");
		out.println("</controller>");
		return;
	}
	
	// MAIN FUNCTION =======================================================================

	/**
	 * Computes desired input flow for given Node.
	 * @param xx given Node.
	 * @return input flow.
	 */
	public synchronized Object computeInput(AbstractNodeSimple xx) {
		Double flw = (Double)super.computeInput(xx);
		if (flw != null)
			return flw;
		if ((input == null) || (((Double)input) < 0))
			input = (Double)limits.get(1);
		if ((actualInput == null) || (((Double)actualInput) < 0))
			actualInput = (Double)limits.get(1);
		double occ_des,occ_act,AlineaRate;
		occ_des = ((NodeHWCNetwork)myLink.getMyNetwork()).computeMultiLinkCriticalDensity(MLlinks);
		if(usesensors)
			occ_act = MLsensor.Density();
		else
			occ_act = ((NodeHWCNetwork)myLink.getMyNetwork()).computeMultiLinkDensity(MLlinks).sum().getCenter();
		AlineaRate = ((Double)actualInput)  +  gain * (occ_des - occ_act);
		flw = ApplyURMS(AlineaRate);
		flw = ApplyQueueControl(flw);
		input = ApplyLimits(flw);
		allowActualInputSet = true;
		return input;
	}
	
	// GUI =================================================================================

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
	 * Returns letter code of the controller type.
	 */
	public final String getTypeLetterCode() {
		return "ALINEA";
	}
	
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Controller.
	 */
	public String toString() {
		return "ALINEA";
	}
}
