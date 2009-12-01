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
 * @version $Id: ControllerALINEA.java,v 1.1.4.1.2.5.2.5 2009/11/22 01:28:33 akurzhan Exp $
 */
public final class ControllerALINEA extends AbstractControllerSimpleHWC {
	private static final long serialVersionUID = 4440581708032401841L;
	
	public boolean upstream = false;
	public double gain = 50.0;
	
	private AbstractLinkHWC MLlink;
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
		if (upstream)
			MLlink = getUpML(x);
		else 
			MLlink = getDnML(x);
		if(usesensors)
			MLsensor = (SensorLoopDetector) MLlink.getMyNetwork().getSensorByLinkId(MLlink.getId());
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
		out.print("<parameter name=\"upstream\" value=\"" + Boolean.toString(upstream) + "\"/>");
		out.print("<parameter name=\"gain\" value=\"" + Double.toString(gain) + "\"/>");
		out.print("</controller>");
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
		occ_des = MLlink.getCriticalDensity();
		if(usesensors)
			occ_act = MLsensor.Density();
		else
			occ_act = MLlink.getDensity().sum().getCenter();
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
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Controller.
	 */
	public String toString() {
		return "ALINEA";
	}
}
