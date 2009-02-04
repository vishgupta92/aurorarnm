/**
 * @(#)ErrorConfiguration.java
 */

package aurora;

import java.io.*;


/**
 * This class contains description of a configuration error.
 * @author Alex Kurzhanskiy
 * @version $Id: ErrorConfiguration.java,v 1.1.4.1 2008/10/16 04:27:07 akurzhan Exp $
 */
public class ErrorConfiguration implements Serializable {
	private static final long serialVersionUID = -3607508781090330495L;
	
	protected Object srcObj = null;
	protected String message = "";
	
	
	public ErrorConfiguration() { }
	public ErrorConfiguration(Object src, String msg) {
		srcObj = src;
		if (msg != null)
			message = msg;
	}
	
	
	/**
	 * Returns the error source.
	 */
	public Object getSource() {
		return srcObj;
	}
	
	/**
	 * Returns the error message.
	 */
	public String getMessage() {
		return message;
	}

}
