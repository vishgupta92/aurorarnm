/**
 * @(#)PanelQOverride.java
 */

package aurora.hwc.control;

import java.awt.*;
import javax.swing.*;
import aurora.util.SpringUtilities;


/**
 * Panel for Queue Override controller.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class PanelQOverride extends AbstractPanelQController {
	private static final long serialVersionUID = -7314364725453530074L;
	
	private JSpinner delta;
	

	/**
	 * Fills the panel with Queue Override specific fields.
	 */
	protected void fillPanel() {
		JPanel prmp = new JPanel(new GridLayout(5, 1));
		prmp.add(new JLabel(" "));
		prmp.add(new JLabel(" "));
		JPanel pP = new JPanel(new SpringLayout());
		pP.setBorder(BorderFactory.createTitledBorder("Parameter"));
		JLabel l = new JLabel("Delta:", JLabel.TRAILING);
		pP.add(l);
		delta = new JSpinner(new SpinnerNumberModel(((QOverride)qcontroller).delta, 0.0, 99999.99, 1.0));
		delta.setEditor(new JSpinner.NumberEditor(delta, "####0.00"));
		l.setLabelFor(delta);
		pP.add(delta);
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
		((QOverride)qcontroller).delta = (Double)delta.getValue();
		return;
	}

}