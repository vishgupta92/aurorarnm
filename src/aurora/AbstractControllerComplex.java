/**
 * @(#)AbstractControllerComplex.java
 */

package aurora;


/**
 * This class is a base for Sytem Wide Controllers.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractControllerComplex.java,v 1.1.2.2 2007/04/18 22:52:55 akurzhan Exp $
 */
public abstract class AbstractControllerComplex extends AbstractController {
	AbstractMonitor myMonitor;
	
	
	public final boolean isSimple() {
		return false;
	}
	
	public final AbstractMonitor getMyMonitor() {
		return myMonitor;
	}
	
	public synchronized boolean setMyMonitor(AbstractMonitor x) {
		if (x != null)
			myMonitor = x;
		else
			return false;
		return true;
	}

}
