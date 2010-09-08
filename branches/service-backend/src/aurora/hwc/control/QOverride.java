/**
 * @(#)QOverride.java
 */

package aurora.hwc.control;

import java.io.*;
import java.text.NumberFormat;
import org.w3c.dom.*;
import aurora.*;
import aurora.hwc.*;


/**
 * Implementation of Queue Override queue controller.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class QOverride extends AbstractQueueController implements Serializable, Cloneable {
	private static final long serialVersionUID = -5580684114768655078L;

	public double delta = 120.00;
	
	public QOverride() { }
	public QOverride(double delta) { this.delta = delta; }
	
	/**
	 * Initializes the Queue Override controller from given DOM structure.
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
					if (pp.item(i).getNodeName().equals("parameter"))
						if (pp.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("delta"))
							delta = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("value").getNodeValue());
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
	 * Generates XML description of the Queue Override controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		out.print("<parameter name=\"delta\" value=\"" + Double.toString(delta) + "\"/>");
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
		double flw = 0.0;
		if (usesensors) {
			inOverride = queuesensor.Density() > lk.getCriticalDensity();
		}
		else {
			if (lk.getPredecessors().size() == 0)
				inOverride = ((AuroraIntervalVector)lk.getQueue()).sum().getCenter() > lk.getQueueMax();
			else
				inOverride = ((((AuroraIntervalVector)lk.getDensity()).sum().getCenter()) * lk.getLength()) > lk.getQueueMax();
		}
		if (inOverride)
			flw = lk.getActualFlow().sum().getCenter() + (delta * lk.getLanes());
		return (Double)flw;
	}
	
	/**
	 * Returns Queue Override controller description.
	 */
	public String getDescription() {
		NumberFormat form = NumberFormat.getInstance();
		form.setMinimumFractionDigits(2);
		form.setMaximumFractionDigits(2);
		return "Queue Override (delta  = " + form.format(delta) + ")";
	}
	
	/**
	 * Implementation of a "deep copy" of the object.
	 */
	public AbstractQueueController deepCopy() {
		QOverride qcCopy = null;
		try {
			qcCopy = (QOverride)clone();
		}
		catch(Exception e) { }
		return qcCopy;
	}
	
	/**
	 * Returns letter code of the queue controller type.
	 */
	public final String getTypeLetterCode() {
		return "QUEUEOVERRIDE";
	}
	
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Controller.
	 */
	public String toString() {
		return "Queue Override";
	}
	
}