/**
 * @(#)PanelControllerALINEA.java
 */

package aurora.hwc.control;

import javax.swing.*;

import aurora.util.SpringUtilities;


/**
 * Panel for editing ALINEA properties.
 * @author Alex Kurzhanskiy
 * @version $Id: PanelControllerALINEA.java,v 1.1.4.1.4.1 2009/08/19 20:11:34 akurzhan Exp $
 */
public final class PanelControllerALINEA extends AbstractSimpleControllerPanel {
	private static final long serialVersionUID = 6013620151807393177L;
	
	private JCheckBox upstream = new JCheckBox("Feedback from upstream");
	private JSpinner gain;
	

	/**
	 * Fills the panel with ALINEA specific fields.
	 */
	protected void fillPanel() {
		Box prmp = Box.createVerticalBox();
		prmp.setBorder(BorderFactory.createTitledBorder("Parameters"));
		JLabel l;
		JPanel pP = new JPanel(new SpringLayout());
		l = new JLabel(" ", JLabel.TRAILING);
		pP.add(l);
		upstream.setSelected(((ControllerALINEA)controller).upstream);
		pP.add(upstream);
		l = new JLabel("Gain:", JLabel.TRAILING);
		pP.add(l);
		gain = new JSpinner(new SpinnerNumberModel(((ControllerALINEA)controller).gain, 0.0, 99999.99, 1.0));
		gain.setEditor(new JSpinner.NumberEditor(gain, "####0.00"));
		l.setLabelFor(gain);
		pP.add(gain);
		SpringUtilities.makeCompactGrid(pP, 2, 2, 2, 2, 2, 2);
		prmp.add(pP);
		add(prmp);
		return;
	}

	/**
	 * Saves controller properties.
	 */
	public synchronized void save() {
		((ControllerALINEA)controller).upstream = upstream.isSelected();
		((ControllerALINEA)controller).gain = (Double)gain.getValue();
		super.save();
		return;
	}
	
}
