/**
 * 
 */
package aurora.hwc.control;

import javax.swing.*;

import aurora.hwc.*;

/**
 * @author gomes
 *
 */
public abstract class AbstractPanelControllerComplex extends JPanel{
	private static final long serialVersionUID = -6376505358134660847L;
	
	protected MonitorControllerHWC myMonitor;
	
	public void initialize(MonitorControllerHWC X) {
		myMonitor = X;
	}
	
	/**
	 * Fills the panel with controller specific fields.
	 */
	public abstract void fillPanel();
	
}
