/**
 * @(#)AbstractControllerHWC.java
 */

package aurora.hwc.control;

import java.io.IOException;
import java.io.PrintStream;

import aurora.*;
import aurora.hwc.*;


/**
 * Base class for simple Node controllers.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractControllerHWC.java,v 1.1.4.1 2008/10/16 04:27:08 akurzhan Exp $
 */
public abstract class AbstractControllerHWC extends AbstractControllerSimple {
	protected QueueController myQController;
	
	
	public AbstractControllerHWC() {
		input = (Double)(-1.0);
		limits.add((Double)0.0);
		limits.add((Double)99999.99);
	}
	
	
	/**
	 * Generates XML description of the ALINEA controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		out.print("<controller class=\"" + this.getClass().getName() + "\" tp=\"" + Double.toString(tp) + "\">");
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
	public final QueueController getQController() {
		return myQController;
	}
	
	/**
	 * Sets queue controller.
	 * @param x queue controller.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setQController(QueueController x) {
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
