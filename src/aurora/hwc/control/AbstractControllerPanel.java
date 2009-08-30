/**
 * @(#)AbstractControllerPanel.java
 */

package aurora.hwc.control;

import java.awt.*;
import java.util.Vector;

import javax.swing.*;

import aurora.*;
import aurora.util.*;


/**
 * Base class for complex controller editing panels.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractControllerPanel.java,v 1.1.4.1.2.1.2.8 2009/08/25 20:50:24 akurzhan Exp $
 */
public abstract class AbstractControllerPanel extends JPanel {
	protected AbstractController controller = null;
	boolean[] modified = null;

	protected WindowControllerEditor winCE;
	
	protected JSpinner lmin;
	protected JSpinner lmax;
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
		JPanel pLim = new JPanel(new SpringLayout());
		pLim.setBorder(BorderFactory.createTitledBorder("Rate Limits (vph)"));
		double mn;
		double mx;
		Vector<Object> lims = controller.getLimits();
		if ((lims != null) && (lims.size() == 2)) {
			mn = (Double)controller.getLimits().get(0);
			mx = (Double)controller.getLimits().get(1);
		}
		else {
			mn = 0.0;
			mx = 99999.99;
		}
		JLabel l;
		l = new JLabel("Min:", JLabel.TRAILING);
		pLim.add(l);
		lmin = new JSpinner(new SpinnerNumberModel(mn, 0.0, 99999.99, 1.0));
		lmin.setEditor(new JSpinner.NumberEditor(lmin, "####0.00"));
		l.setLabelFor(lmin);
		pLim.add(lmin);
		l = new JLabel("  Max:", JLabel.TRAILING);
		pLim.add(l);
		lmax = new JSpinner(new SpinnerNumberModel(mx, 0.0, 99999.99, 1.0));
		lmax.setEditor(new JSpinner.NumberEditor(lmax, "####0.00"));
		l.setLabelFor(lmax);
		pLim.add(lmax);
		SpringUtilities.makeCompactGrid(pLim, 2, 2, 2, 2, 2, 2);
		add(pLim);
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
		double mn = (Double)lmin.getValue();
		double mx = (Double)lmax.getValue();
		Vector<Object> lims = new Vector<Object>();
		if (mn <= mx) {
			lims.add(lmin.getValue());
			lims.add(lmax.getValue());
		}
		else {
			lims.add(lmax.getValue());
			lims.add(lmin.getValue());
		}
		controller.setLimits(lims);
		int h = (Integer)hh.getValue();
		int m = (Integer)mm.getValue();
		double s = (Double)ss.getValue();
		controller.setTP(h + (m/60.0) + (s/3600.0));
		if (modified != null)
			modified[0] = true;
		return;
	}
	
	

}