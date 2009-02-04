/**
 * @(#)QueueController.java
 */

package aurora.hwc.control;

import java.io.*;
import org.w3c.dom.*;
import aurora.*;
import aurora.hwc.*;


/**
 * Interface for on-ramp queue controllers.
 * @author Alex Kurzhanskiy
 * @version $Id: QueueController.java,v 1.1.4.1 2008/10/16 04:27:08 akurzhan Exp $
 */
public interface QueueController {
	
	public boolean initFromDOM(Node p) throws ExceptionConfiguration;
	public Object computeInput(AbstractNodeHWC nd, AbstractLinkHWC lk);
	public void xmlDump(PrintStream out) throws IOException;
	public String getDescription();
	public QueueController deepCopy();
	
}
