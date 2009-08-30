/**
 * @(#)AbstractControllerSimple.java
 */

package aurora;

import java.util.*;


/**
 * Base class for simple controllers.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractControllerSimple.java,v 1.3.2.3.2.3.2.4 2009/08/25 20:49:13 akurzhan Exp $
 */
public abstract class AbstractControllerSimple extends AbstractController {
	protected AbstractLink myLink = null;
	protected Object input; // current input value
	protected boolean allowInputSet = false;

	/**
	 * Returns <code>true</code> indicating that it is a simple controller.
	 */
	public final boolean isSimple() {
		return true;
	}
	
	/**
	 * Returns <code>false</code> indicating that it is not a node controller.
	 */
	public final boolean isNode() {
		return false;
	}
	
	/**
	 * Returns <code>false</code> indicating that it is not a complex controller.
	 */
	public final boolean isComplex() {
		return false;
	}
	
	/**
	 * Returns mask for compatible Node types.
	 */
	public int getCompatibleNodeTypes() {
		return AbstractTypes.MASK_NODE;
	}
	
	/**
	 * Returns link to which this controller is assigned.
	 */
	public final AbstractLink getMyLink() {
		return myLink;
	}
	
	/**
	 * Assigns link for this controller.
	 * @param x link.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setMyLink(AbstractLink x) {
		if (x == null)
			return false;
		myLink = x;
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
