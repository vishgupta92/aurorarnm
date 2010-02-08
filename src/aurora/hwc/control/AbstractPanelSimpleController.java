/**
 * @(#)AbstractSimpleControllerPanel.java
 */

package aurora.hwc.control;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import aurora.*;
import aurora.hwc.*;
import aurora.util.*;


/**
 * Base class for simple controller editing panels.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public abstract class AbstractPanelSimpleController extends AbstractPanelController implements ActionListener {
	private static final long serialVersionUID = 4408308365725315058L;

	protected AbstractQueueController qcontroller = null;
	
	protected boolean[] modified = null;
	protected int index = -1;
	
	protected WindowControllerEditor winCE;
	
	protected JComboBox listQControllers;
	protected JButton buttonProp = new JButton("Properties");
	protected JSpinner lmin;
	protected JSpinner lmax;
	
	
	/**
	 * Fills the panel with controller specific fields.
	 */
	protected abstract void fillPanel();
	
	/**
	 * Initializes controller editing panel.
	 * @param ctrl controller.
	 */
	public void initialize(AbstractController ctrl, boolean[] flags, int idx, AbstractNodeHWC node) {
		if ((ctrl == null) || (node == null))
			return;
		controller = ctrl;
		modified = flags;
		index = idx;
		qcontroller = ((AbstractControllerSimpleHWC)controller).getQController();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		fillPanel();
		Box limp = Box.createVerticalBox();
		JPanel pLim = new JPanel(new SpringLayout());
		pLim.setBorder(BorderFactory.createTitledBorder("Rate Limits (vph)"));
		double mn;
		double mx;
		Vector<Object> lims = ((AbstractControllerSimpleHWC)controller).getLimits();
		if ((lims != null) && (lims.size() == 2)) {
			mn = (Double)((AbstractControllerSimpleHWC)controller).getLimits().get(0);
			mx = (Double)((AbstractControllerSimpleHWC)controller).getLimits().get(1);
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
		limp.add(pLim);
		add(limp);
		Box qctrp = Box.createVerticalBox();
		JPanel pQCL = new JPanel(new FlowLayout());
		pQCL.setBorder(BorderFactory.createTitledBorder("Queue Controller"));
		buttonProp.setEnabled(false);
		listQControllers = new JComboBox();
		listQControllers.addItem("None");
		String[] qctrlClasses = node.getQueueControllerClasses();
		for (int i = 0; i < qctrlClasses.length; i++)
			if ((qcontroller != null) && (qcontroller.getClass().getName().compareTo(qctrlClasses[i]) == 0)) {
				listQControllers.addItem(qcontroller);
				listQControllers.setSelectedIndex(i+1);
				buttonProp.setEnabled(true);
			}
			else
				try {
					Class c = Class.forName(qctrlClasses[i]);
					listQControllers.addItem((AbstractQueueController)c.newInstance());
				}
				catch(Exception e) { }
		listQControllers.addActionListener(this);
		pQCL.add(listQControllers);
		buttonProp.addActionListener(new ButtonEventsListener());
		pQCL.add(buttonProp);
		qctrp.add(pQCL);
		add(qctrp);	
		Box fp = Box.createVerticalBox();
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
		fp.add(pT);
		add(fp);
		winCE = new WindowControllerEditor(this, null);
		winCE.setSize(400,500);
		winCE.setVisible(true);
		return;
	}
	
	/**
	 * Saves controller properties.
	 */
	public synchronized void save() {
		super.save();
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
		((AbstractControllerSimpleHWC)controller).setLimits(lims);
		((AbstractControllerSimpleHWC)controller).setQController(qcontroller);
		if ((modified != null) && (index < modified.length) && (index >= 0))
			modified[index] = true;
		return;
	}
	
	/**
	 * Callback for combo box selection change.
	 * @param e combo box selection change.
	 */
	public void actionPerformed(ActionEvent e) {
		JComboBox cb = (JComboBox)e.getSource();
		if (cb.getSelectedIndex() > 0) {
			qcontroller = (AbstractQueueController)listQControllers.getSelectedItem();
			buttonProp.setEnabled(true);
		}
		else {
			buttonProp.setEnabled(false);
			qcontroller = null;
		}
		return;
	}
	
	
	/**
	 * This class is needed to react to "Properties" button pressed.
	 */
	private class ButtonEventsListener implements ActionListener {

		public void actionPerformed(ActionEvent ae) {
			try {
	    		Class c = Class.forName("aurora.hwc.control.Panel" + qcontroller.getClass().getSimpleName());
	    		AbstractPanelQController qcp = (AbstractPanelQController)c.newInstance();
	    		qcp.initialize(qcontroller);
	    	}
	    	catch(Exception e) { }
			return;
		}
		
	}

}