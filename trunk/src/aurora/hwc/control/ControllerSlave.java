/**
 * @(#)ControllerSlave.java
 */

package aurora.hwc.control;

import java.io.*;
import aurora.*;
import aurora.hwc.*;


/**
 * Implementation of the Slave controller whose output is assigned by the superior Complex Controller.
 * @author Gabriel Gomes, Alex Kurzhanskiy
 * @version $Id: ControllerSlave.java,v 1.1.4.13 2009/11/22 01:17:16 akurzhan Exp $
 */
public class ControllerSlave extends AbstractControllerSimpleHWC {
	private static final long serialVersionUID = -4982384498258794450L;
	
	protected AbstractControllerComplex myComplexController;

	public ControllerSlave() {
		super();
		dependent = true;
	}


	/**
	 * Generates XML description of the Slave controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		out.print("</controller>");
		return;
	}
	
	/**
	 * Computes desired input value for given Node.
	 * @param x given Node.
	 * @return input object.
	 */
	public synchronized Object computeInput(AbstractNodeSimple xx) {
		AbstractNodeHWC x = (AbstractNodeHWC)xx;
		Double flw = (Double)super.computeInput(x);
		if (flw != null)
			return flw;
		Double c = (Double)myComplexController.getControlInput(this);
		flw = ApplyURMS(c);
		flw = ApplyQueueControl(flw);
		input = ApplyLimits(flw);
		allowActualInputSet = true;
		return input;
	}
	
	/**
	 * Returns complex controller.
	 */
	public AbstractControllerComplex getMyComplexController() {
		return myComplexController;
	}
	
	/**
	 * Sets complex controller.
	 * @param x complex controller.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setMyComplexController(AbstractControllerComplex x) {
		if (x == null)
			return false;
		myComplexController = x;
		return true;
	}
	
	/**
	 * Returns controller description.
	 */
	public final String getDescription() {
		return "Slave";
	}

	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Controller.
	 */
	public String toString() {
		return "Slave";
	}

}
