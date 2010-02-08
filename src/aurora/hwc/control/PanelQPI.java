/**
 * @(#)PanelQPI.java
 */

package aurora.hwc.control;

import java.awt.*;
import javax.swing.*;
import aurora.util.SpringUtilities;


/**
 * Panel for Proportional controller.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class PanelQPI extends AbstractPanelQController {
	private static final long serialVersionUID = 622910022204435368L;
	
	private JSpinner kp;
	private JSpinner ki;
	

	/**
	 * Fills the panel with Queue Proportional specific fields.
	 */
	protected void fillPanel() {
		JPanel prmp = new JPanel(new GridLayout(5, 1));
		prmp.add(new JLabel(" "));
		prmp.add(new JLabel(" "));
		JPanel pP = new JPanel(new SpringLayout());
		pP.setBorder(BorderFactory.createTitledBorder("Parameters"));
		JLabel l = new JLabel("Kp:", JLabel.TRAILING);
		pP.add(l);
		kp = new JSpinner(new SpinnerNumberModel(((QPI)qcontroller).Kp, 0.0, 99.99, 0.01));
		kp.setEditor(new JSpinner.NumberEditor(kp, "#0.00"));
		l.setLabelFor(kp);
		pP.add(kp);
		l = new JLabel("Ki:", JLabel.TRAILING);
		pP.add(l);
		ki = new JSpinner(new SpinnerNumberModel(((QPI)qcontroller).Ki, 0.0, 99.99, 0.01));
		ki.setEditor(new JSpinner.NumberEditor(ki, "#0.00"));
		l.setLabelFor(ki);
		pP.add(ki);
		SpringUtilities.makeCompactGrid(pP, 2, 2, 2, 2, 2, 2);
		prmp.add(pP);
		prmp.add(new JLabel(" "));
		prmp.add(new JLabel(" "));
		add(prmp);
		return;
	}

	/**
	 * Saves controller properties
	 */
	public synchronized void save() {
		((QPI)qcontroller).Kp = (Double)kp.getValue();
		((QPI)qcontroller).Ki = (Double)ki.getValue();
		return;
	}

}