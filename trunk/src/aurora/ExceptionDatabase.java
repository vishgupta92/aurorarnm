/**
 * @(#)ExceptionDatabase.java
 */

package aurora;

import java.lang.Exception;


/**
 * Exception thrown if database error occurs.
 * 
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public class ExceptionDatabase extends Exception {
	private static final long serialVersionUID = -6689163546920029634L;
	
	public ExceptionDatabase() { super(); }
	public ExceptionDatabase(String s) { super(s); }
	public ExceptionDatabase(AbstractNetworkElement x, String s) {
		super(x.getClass().getSimpleName() + " (" + Integer.toString(x.getId()) + "): " + s);
	}
	
}
