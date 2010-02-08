/**
 * @(#)QPI.java
 */

package aurora.hwc.control;

import java.io.*;
import java.text.NumberFormat;
import org.w3c.dom.*;
import aurora.*;
import aurora.hwc.*;


/**
 * Implementation of PI queue controller.
 * @author Gabriel Gomes, Alex Kurzhanskiy
 * @version $Id$
 */
public final class QPI extends AbstractQueueController implements Serializable, Cloneable {
	private static final long serialVersionUID = -2675153457120198296L;
	
	public double Kp = 1.0;		// proportional gain
	public double Ki = 1.0;		// integral gain
	
	
	public QPI() { }
	public QPI(double kp, double ki) { this.Kp = kp; this.Ki = ki; }
	
	/**
	 * Initializes the PI queue controller from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = super.initFromDOM(p);
		if (p == null)
			return !res;
		try  {
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (int i = 0; i < pp.getLength(); i++) {
					if (pp.item(i).getNodeName().equals("parameter")) {
						if (pp.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("kp")) 
							Kp = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("value").getNodeValue());
						if (pp.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("ki")) 
							Ki = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("value").getNodeValue());
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
	 * Generates XML description of the PI Queue controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		out.print("<parameter name=\"kp\" value=\"" + Double.toString(Kp) + "\"/>");
		out.print("<parameter name=\"ki\" value=\"" + Double.toString(Ki) + "\"/>");
		out.print("</qcontroller>");
		return;
	}

	/**
	 * Computes desired input Link flow.<br>
	 * The flow value depends on the queue size and queue limit.
	 * @param nd Node to which the controller belongs.
	 * @param lk input Link.
	 * @return desired flow.  
	 */
	public Object computeInput(AbstractNodeHWC nd, AbstractLinkHWC lk) {
		double queue;
		if (lk.getPredecessors().size() == 0)
			queue = ((AuroraIntervalVector)lk.getQueue()).sum().getCenter();
		else
			queue = (((AuroraIntervalVector)lk.getDensity()).sum().getCenter()) * lk.getLength();
		double qmax = lk.getQueueMax();
		inOverride = queue > qmax;
		double r_i = (Ki / lk.getMyNetwork().getTP()) * (queue - qmax) + lk.getActualFlow().sum().getCenter();
		double flw = 0.0;
		if (inOverride)
			flw = (Kp / lk.getMyNetwork().getTP()) * (queue - qmax) + r_i;
		return (Double)Math.max(flw, 0.0);
	}
	
	/**
	 * Returns controller description.
	 */
	public String getDescription() 
	{
		NumberFormat form = NumberFormat.getInstance();
		form.setMinimumFractionDigits(2);
		form.setMaximumFractionDigits(2);
		return "PI (Kp = " + form.format(Kp) + ", Ki = " + form.format(Ki) + ")";
	}
	
	/**
	 * Implementation of a "deep copy" of the object.
	 */
	public AbstractQueueController deepCopy() {
		QPI qcCopy = null;
		try {
			qcCopy = (QPI)clone();
		}
		catch(Exception e) { }
		return qcCopy;
	}
	
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Controller.
	 */
	public String toString() {
		return "PI Control";
	}

}