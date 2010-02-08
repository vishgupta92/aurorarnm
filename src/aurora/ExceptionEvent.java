/**
 * @(#)ExceptionEvent.java
 */

package aurora;

import java.lang.Exception;


/**
 * Exception thrown if error in event handling occurs.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public class ExceptionEvent extends Exception {
	private static final long serialVersionUID = -3858549267715738598L;
	
	public ExceptionEvent() { super(); }
	public ExceptionEvent(String s) { super(s); }
	public ExceptionEvent(AbstractNetworkElement x, String s) {
		super(x.getClass().getSimpleName() + " (" + Integer.toString(x.getId()) + "): " + s);
	}
	
}
