/**
 * @(#)AbstractControllerNode.java
 */

package aurora;


/**
 * This class is a base for Node Controllers.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractControllerNode.java,v 1.1.2.1.2.2 2009/11/22 22:19:35 akurzhan Exp $
 */
public abstract class AbstractControllerNode extends AbstractController {
	private static final long serialVersionUID = -7845344291835328109L;
	
	AbstractNode myNode = null;
	
	
	/**
	 * Updates the state of controller.
	 * @param ts new time step.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		if (ts < 1)
			throw new ExceptionSimulation(null, "Nonpositive time step (" + Integer.toString(ts) + ").");
		if (this.ts == 0) {
			this.ts = ts;
			return true;
		}
		int period = (int)Math.round((double)(tp/myNode.getTop().getTP()));
		if (period == 0)
			period = 1;
		if ((ts - this.ts) < period)
			return false;
		this.ts = ts;
		return true;
	}
	
	/**
	 * Returns <code>false</code> indicating that it is not a simple controller.
	 */
	public final boolean isSimple() {
		return false;
	}
	
	/**
	 * Returns <code>true</code> indicating that it is a node controller.
	 */
	public final boolean isNode() {
		return true;
	}
	
	/**
	 * Returns <code>false</code> indicating that it is not a complex controller.
	 */
	public final boolean isComplex() {
		return false;
	}
	
	/**
	 * Returns node to which this controller belongs.
	 */
	public final AbstractNode getMyNode() {
		return myNode;
	}
	
	/**
	 * Assigns node to which this controller should belong.
	 * @param x monitor.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setMyNode(AbstractNode x) {
		if (x == null)
			return false;
		myNode = x;
		return true;
	}

}
