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
 * @version $Id: ControllerSlave.java,v 1.1.4.4 2009/08/16 20:51:13 akurzhan Exp $
 */
public class ControllerSlave extends AbstractControllerHWC {
	private static final long serialVersionUID = -4982384498258794450L;
	
	public int myIndex;
	public AbstractLink myLink;
	public AbstractControllerComplex myComplexController;

	public ControllerSlave() 
	{ 
		super(); 
		dependent = true;
	}
	
	public ControllerSlave(AbstractControllerComplex x, AbstractLink lnk, int idx) {
		super();
		dependent = true;
		myIndex = idx;
		myLink = lnk;
		myComplexController = x;
	}

	public void setIndex(int i){ myIndex = i; };
	
	/**
	 * Computes desired input value for given Node.
	 * @param x given Node.
	 * @return input object.
	 */
	public synchronized Object computeInput(AbstractNodeSimple x) {
		Double flw = (Double)super.computeInput(x);
		if (flw != null)
			return flw;
		int idx = x.getSimpleControllers().indexOf(this);
		AbstractLinkHWC lnk = (AbstractLinkHWC)x.getPredecessors().get(idx);
		flw = (Double)myComplexController.getControlInput(myIndex);
		if ((((NodeHWCNetwork)x.getMyNetwork()).hasQControl()) && (myQController != null)) 
			flw = Math.max(flw, (Double)myQController.computeInput((AbstractNodeHWC)x, lnk));
		input = (Double)Math.max(flw, 0.0);
		allowInputSet = true;
		return input;
	}

	/**
	 * Returns controller description.
	 */
	public final String getDescription() {
		return "Slave";
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
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Controller.
	 */
	public String toString() {
		return "Slave";
	}

}
