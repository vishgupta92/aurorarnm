/**
 * @(#)AbstractMonitorController.java
 */

package aurora;

import java.io.*;

import org.w3c.dom.Node;


/**
 * Partial implementation of Controller Monitor.<br>
 * This Monitor is needed for coordinated control of multiple nodes.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractMonitorController.java,v 1.1.2.1 2008/12/29 05:26:21 akurzhan Exp $
 */
public abstract class AbstractMonitorController extends AbstractMonitor {
	AbstractControllerComplex myController;
	
	
	/**
	 * Initializes control Monitor from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * Generates XML description of the control Monitor.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		if (out == null)
			out = System.out;
		if (myController != null)
			myController.xmlDump(out);
		out.print("</monitor>\n");
		return;
	}
	
	/**
	 * Returns controller.
	 */
	public final AbstractControllerComplex getMyController() {
		return myController;
	}
	
	/**
	 * Returns time period for controller invocation.
	 */
	public final double getTP() {
		if (myController == null)
			return -1;
		return myController.getTP();
	}
	
	/**
	 * Sets controller.
	 * @param x complex controller.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setMyController(AbstractControllerComplex x) {
		if (x != null)
			myController = x;
		else
			return false;
		return true;
	}
	
	/**
	 * Sets time period for controller invocation.
	 * @param x time period.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setTP(double x) {
		boolean res = false;
		if (getMyNetwork().getTP() > x)
			return res;
		for (int i = 0; i < successors.size(); i++) {
			AbstractNode mn = (AbstractNode)successors.get(i);
			res = mn.setTP(x);
			if (!res)
				return res;
		}
		return res;
	}
	
	/**
	 * Copies data from given Monitor to a current one.
	 * @param x given Network Element.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean copyData(AbstractNetworkElement x) {
		boolean res = super.copyData(x);
		if ((!res) || ((x.getType() & AbstractTypes.MASK_MONITOR_CONTROLLER) == 0))
			return false;
		AbstractMonitorController mntr = (AbstractMonitorController)x;
		myController = mntr.getMyController();
		return res;
	}
	
}