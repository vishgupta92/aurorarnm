/**
 * @(#)AbstractControllerHWC.java
 */

package aurora.hwc.control;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aurora.*;
import aurora.hwc.*;


/**
 * Base class for simple Node controllers.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public abstract class AbstractControllerHWC extends AbstractControllerSimple {
	private static final long serialVersionUID = -241436581476989974L;
	
	protected AbstractQueueController myQController;
	
	
	public AbstractControllerHWC() {
		input = (Double)(-1.0);
		limits.add((Double)0.0);
		limits.add((Double)99999.99);
	}
	
	
	/**
	 * Initializes controller from given DOM structure.
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
					if (pp.item(i).getNodeName().equals("limits")) {
						limits = new Vector<Object>();
						limits.add(Double.parseDouble(pp.item(i).getAttributes().getNamedItem("cmin").getNodeValue()));
						limits.add(Double.parseDouble(pp.item(i).getAttributes().getNamedItem("cmax").getNodeValue()));
					}
					if (pp.item(i).getNodeName().equals("qcontroller")) {
						Node type_attr = pp.item(i).getAttributes().getNamedItem("type");
						String class_name = null;
						if (type_attr != null)
							class_name = myLink.getMyNetwork().getContainer().ctrType2Classname(type_attr.getNodeValue());
						else
							class_name = pp.item(i).getAttributes().getNamedItem("class").getNodeValue();
						Class c = Class.forName(class_name);
						myQController = (AbstractQueueController)c.newInstance();
						res &= myQController.initFromDOM(pp.item(i));
					}
				}
			}
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}
	/**
	 * Generates XML description of simple controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		if (limits.size() == 2)
			out.print("<limits cmin=\"" + Double.toString((Double)limits.get(0))+ "\" cmax=\"" + Double.toString((Double)limits.get(1))+ "\" />");
		if (myQController != null)
			myQController.xmlDump(out);
		return;
	}
	
	/**
	 * Computes desired input value for given Node.
	 * @param x given Node.
	 * @return input object.
	 */
	public Object computeInput(AbstractNodeHWC x) {
		return super.computeInput(x);
	}
	
	/**
	 * Returns queue controller.
	 */
	public final AbstractQueueController getQController() {
		return myQController;
	}
	
	/**
	 * Sets queue controller.
	 * @param x queue controller.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setQController(AbstractQueueController x) {
		myQController = x;
		return true;
	}
	
	/**
	 * Implementation of a "deep copy" of the object.
	 */
	public AbstractController deepCopy() {
		AbstractControllerHWC ctrlCopy = (AbstractControllerHWC)super.deepCopy();
		if ((ctrlCopy != null) && (myQController != null))
			ctrlCopy.setQController(myQController.deepCopy());
		return ctrlCopy;
	}

}
