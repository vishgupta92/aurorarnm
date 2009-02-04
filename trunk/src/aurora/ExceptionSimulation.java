/**
 * @(#)ExceptionSimulation.java
 */

package aurora;

import java.lang.Exception;


/**
 * Exception thrown if simulation error occurs.
 * 
 * @author Alex Kurzhanskiy
 * @version $Id: ExceptionSimulation.java,v 1.2 2007/02/10 23:26:56 akurzhan Exp $
 */
public class ExceptionSimulation extends Exception {
	private static final long serialVersionUID = 9118982666121373770L;

	public ExceptionSimulation() { super(); }
	public ExceptionSimulation(String s) { super(s); }
	public ExceptionSimulation(AbstractNetworkElement x, String s) {
		super(x.getClass().getSimpleName() + " (" + Integer.toString(x.getId()) + "): " + s);
	}
	
}