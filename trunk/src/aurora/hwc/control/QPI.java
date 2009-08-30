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
 * @version $Id: QPI.java,v 1.1.2.1 2009/07/29 21:11:23 akurzhan Exp $
 */
public final class QPI implements QueueController, Serializable, Cloneable {
	private static final long serialVersionUID = -2675153457120198296L;
	
	public double kp = 1.0;
	public double ki = 1.0;
	
	
	public QPI() { }
	public QPI(double kp, double ki) { this.kp = kp; this.ki = ki; }
	
	
	/**
	 * Initializes the PI queue controller from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if (p == null)
			return !res;
		try  {
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (int i = 0; i < pp.getLength(); i++) {
					if (pp.item(i).getNodeName().equals("parameter")) {
						if (pp.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("kp")) 
							kp = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("value").getNodeValue());
						if (pp.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("ki")) 
							ki = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("value").getNodeValue());
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
		if (out == null)
			out = System.out;
		out.print("<qcontroller class=\"" + this.getClass().getName() + "\">");
		out.print("<parameter name=\"kp\" value=\"" + Double.toString(kp) + "\"/>");
		out.print("<parameter name=\"ki\" value=\"" + Double.toString(ki) + "\"/>");
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
		double qmax = lk.getQueueMax();
		double qest = lk.getDensity().sum().getCenter();
		//TODO
		return (Double)0.0;
	}
	
	/**
	 * Returns controller description.
	 */
	public String getDescription() {
		NumberFormat form = NumberFormat.getInstance();
		form.setMinimumFractionDigits(2);
		form.setMaximumFractionDigits(2);
		return "PI (Kp = " + form.format(kp) + ", Ki = " + form.format(ki) + ")";
	}
	
	/**
	 * Implementation of a "deep copy" of the object.
	 */
	public QueueController deepCopy() {
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