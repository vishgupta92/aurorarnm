/**
 * @(#)AbstractControllerPanel.java
 */

package aurora.hwc.control;

import java.awt.*;
import javax.swing.*;
import aurora.*;
import aurora.util.*;


/**
 * Base class for complex controller editing panels.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractPanelController.java,v 1.1.2.1 2009/10/01 05:49:01 akurzhan Exp $
 */
public abstract class AbstractPanelController extends JPanel {
	private static final long serialVersionUID = -2623126512498240411L;
	
	protected AbstractController controller = null;
	boolean[] modified = null;

	protected WindowControllerEditor winCE;
	
	protected JSpinner hh;
	protected JSpinner mm;
	protected JSpinner ss;
	
	
	/**
	 * Fills the panel with controller specific fields.
	 */
	protected abstract void fillPanel();
	
	/**
	 * Returns window header with event description.
	 */
	public final String getHeader() {
		return controller.toString();
	}
	
	/**
	 * Initializes controller editing panel.
	 * @param ctrl controller.
	 */
	public void initialize(AbstractController ctrl, boolean[] flag) {
		if (ctrl == null)
			return;
		controller = ctrl;
		modified = flag;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		fillPanel();
		JPanel pT = new JPanel(new FlowLayout());
		pT.setBorder(BorderFactory.createTitledBorder("Time Period"));
		hh = new JSpinner(new SpinnerNumberModel(Util.getHours(controller.getTP()), 0, 99, 1));
		hh.setEditor(new JSpinner.NumberEditor(hh, "00"));
		pT.add(hh);
		pT.add(new JLabel("h "));
		mm = new JSpinner(new SpinnerNumberModel(Util.getMinutes(controller.getTP()), 0, 59, 1));
		mm.setEditor(new JSpinner.NumberEditor(mm, "00"));
		pT.add(mm);
		pT.add(new JLabel("m "));
		ss = new JSpinner(new SpinnerNumberModel(Util.getSeconds(controller.getTP()), 0, 59.99, 1));
		ss.setEditor(new JSpinner.NumberEditor(ss, "00.##"));
		pT.add(ss);
		pT.add(new JLabel("s"));
		add(pT);
		winCE = new WindowControllerEditor(this, null);
		winCE.setSize(600,500);
		winCE.setVisible(true);
		return;
	}
	
	/**
	 * Saves controller properties.
	 */
	public synchronized void save() {
		int h = (Integer)hh.getValue();
		int m = (Integer)mm.getValue();
		double s = (Double)ss.getValue();
		controller.setTP(h + (m/60.0) + (s/3600.0));
		if (modified != null)
			modified[0] = true;
		return;
	}
	
	

}