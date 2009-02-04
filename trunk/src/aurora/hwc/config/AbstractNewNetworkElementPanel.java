/**
 * @(#)AbstractNewNetworkElementPanel.java
 */

package aurora.hwc.config;

import javax.swing.*;
import aurora.*;


/**
 * Base class for new Network Element panels.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractNewNetworkElementPanel.java,v 1.1.4.1 2008/10/16 04:27:06 akurzhan Exp $
 */
public abstract class AbstractNewNetworkElementPanel extends JPanel {
	protected TreePane tpane;
	protected AbstractNodeComplex myNetwork;
	protected Point pointPosition = null;
	
	public AbstractNewNetworkElementPanel() { }
	public AbstractNewNetworkElementPanel(TreePane tpane, AbstractNodeComplex myNetwork, Point pointPosition) {
		this.tpane = tpane;
		this.myNetwork = myNetwork;
		this.pointPosition = pointPosition;
	}
	

	/**
	 * Returns header for the window.
	 */
	public abstract String getHeader();
	
	/**
	 * Creates new Network Element.
	 */
	public abstract void create();

}
