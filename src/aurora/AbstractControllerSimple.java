/**
 * @(#)AbstractControllerSimple.java
 */

package aurora;

import java.util.*;


/**
 * Base class for simple controllers.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractControllerSimple.java,v 1.3.2.3.2.2 2009/01/14 01:43:32 akurzhan Exp $
 */
public abstract class AbstractControllerSimple extends AbstractController {
	protected Vector<Object> limits = new Vector<Object>(); // input limits
	protected Object input; // current input value
	protected boolean allowInputSet = false;
	protected boolean dependent = false; 

	
	/**
	 * Returns <code>true</code> indicating that it is a simple controller.
	 */
	public final boolean isSimple() {
		return true;
	}
	
	/**
	 * Checks if this simple controller depends on a complex one
	 */
	public final boolean isDependent() {
		return dependent;
	}
	
	/**
	 * Returns input limits.
	 */
	public final Vector<Object> getLimits() {
		return limits;
	}
	
	/**
	 * Returns mask for compatible Node types.
	 */
	public int getCompatibleNodeTypes() {
		return AbstractTypes.MASK_NODE;
	}
	
	/**
	 * Sets input limits.
	 * @param x limits.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setLimits(Vector<Object> x) {
		if (x == null)
			return false;
		limits = x;
		return true;
	}
	
	/**
	 * Sets dependent status.
	 * @param x dependent flag.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDependent(boolean x) {
		dependent = x;
		return true;
	}
	
	/**
	 * Computes desired input value for given Node.
	 * @param x given Node.
	 * @return input object.
	 */
	public synchronized Object computeInput(AbstractNodeSimple x) {
		if (ts == 0) {
			ts = x.getTS();
			input = null;
			return null;
		}
		int period = (int)Math.round((double)(x.getTop().getTP()/tp));
		if (period == 0)
			period = 1;
		if ((x.getTS() - ts) < period)
			return input;
		return null;
	}
	
	/**
	 * Sets input.
	 * Used to set the actual input as opposed to the one
	 * recommended by the controller.
	 * @param inpt input object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setInput(Object inpt) {
		boolean res = allowInputSet;
		if (allowInputSet) {
			input = inpt;
			allowInputSet = false;
		}
		return res;
	}

}
