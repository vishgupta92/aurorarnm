/**
 * @(#)PanelQProportional.java
 */

package aurora.hwc.control;

import java.awt.*;
import javax.swing.*;
import aurora.util.SpringUtilities;


/**
 * Panel for Proportional controller.
 * @author Alex Kurzhanskiy
 * @version $Id: PanelQProportional.java,v 1.1.4.1 2008/10/16 04:27:08 akurzhan Exp $
 */
public final class PanelQProportional extends AbstractQControllerPanel {
	private static final long serialVersionUID = 6672021053676871289L;
	
	private JSpinner kp;
	

	/**
	 * Fills the panel with Queue Proportional specific fields.
	 */
	protected void fillPanel() {
		JPanel prmp = new JPanel(new GridLayout(5, 1));
		prmp.add(new JLabel(" "));
		prmp.add(new JLabel(" "));
		JPanel pP = new JPanel(new SpringLayout());
		pP.setBorder(BorderFactory.createTitledBorder("Parameter"));
		JLabel l = new JLabel("Kp:", JLabel.TRAILING);
		pP.add(l);
		kp = new JSpinner(new SpinnerNumberModel(((QProportional)qcontroller).kp, 0.0, 99.99, 0.01));
		kp.setEditor(new JSpinner.NumberEditor(kp, "#0.00"));
		l.setLabelFor(kp);
		pP.add(kp);
		SpringUtilities.makeCompactGrid(pP, 1, 2, 2, 2, 2, 2);
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
		((QProportional)qcontroller).kp = (Double)kp.getValue();
		return;
	}

}