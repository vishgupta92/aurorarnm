/**
 * @(#)AuroraConfigurable.java
 */

package aurora;

import java.io.IOException;
import java.io.PrintStream;
import org.w3c.dom.Node;


/**
 * Functions that need to be implemented by configurable Aurora objects.
 * @author Alex Kurzhanskiy
 * @version $Id: AuroraConfigurable.java,v 1.1.2.2 2008/10/16 04:27:07 akurzhan Exp $
 */
public interface AuroraConfigurable {

	/**
	 * Initializes an object from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration;
	
	/**
	 * Generates XML description of an object.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException;
	
	/**
	 * Validates the object configuration.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean validate() throws ExceptionConfiguration;
	
}