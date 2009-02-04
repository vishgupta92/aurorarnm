/**
* @(#)ExceptionConfiguration.java
*/

package aurora;

import java.lang.Exception;


/**
* Exception thrown if an error in configuration found.
* 
* @author Alex Kurzhanskiy
* @version $Id: ExceptionConfiguration.java,v 1.2 2007/02/10 23:26:56 akurzhan Exp $
*/
public class ExceptionConfiguration extends Exception {
	private static final long serialVersionUID = 4432213481848132765L;

	public ExceptionConfiguration() { super(); }
	public ExceptionConfiguration(String s) { super(s); }
	public ExceptionConfiguration(AbstractNetworkElement x, String s) {
		super(x.getClass().getSimpleName() + " (" + Integer.toString(x.getId()) + "): " + s);
	}
	
}
