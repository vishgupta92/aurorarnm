/**
 * @(#)ExceptionSimulation.java
 */

package aurora;

import java.lang.Exception;


/**
 * Exception thrown if simulation error occurs.
 * 
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public class ExceptionSimulation extends Exception {
	private static final long serialVersionUID = 9118982666121373770L;

	public ExceptionSimulation() { super(); }
	public ExceptionSimulation(String s) { super(s); }
	public ExceptionSimulation(AbstractNetworkElement x, String s) {
		super(x.getClass().getSimpleName() + " (" + Integer.toString(x.getId()) + "): " + s);
	}
	
}