/**
 * @(#)AbstractNewNetworkElementPanel.java
 */

package aurora.hwc.config;

import javax.swing.*;
import aurora.*;


/**
 * Base class for new Network Element panels.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public abstract class AbstractNewNetworkElementPanel extends JPanel {
	private static final long serialVersionUID = -4358604370435744251L;
	
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
