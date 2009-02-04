/**
 * @(#)AbstractQControllerPanel.java
 */

package aurora.hwc.control;

import javax.swing.*;


/**
 * Base class for queue controller editing panels.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractQControllerPanel.java,v 1.1.4.1 2008/10/16 04:27:08 akurzhan Exp $
 */
public abstract class AbstractQControllerPanel extends JPanel {
	protected QueueController qcontroller = null;
	
	protected WindowQControllerEditor winQCE;
	
	
	/**
	 * Fills the panel with controller specific fields.
	 */
	protected abstract void fillPanel();
	
	/**
	 * Returns window header with event description.
	 */
	public final String getHeader() {
		return qcontroller.toString();
	}
	
	/**
	 * Initializes controller editing panel.
	 * @param qctrl queue controller.
	 */
	public void initialize(QueueController qctrl) {
		qcontroller = qctrl;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		fillPanel();
		winQCE = new WindowQControllerEditor(this, null);
		winQCE.setVisible(true);
		return;
	}
	
	/**
	 * Saves queue controller properties.
	 */
	public abstract void save();
	
}