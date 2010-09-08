/**
 * @(#)PanelEventNetworkControl.java 
 */

package aurora.hwc.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import aurora.*;
import aurora.hwc.*;


/**
 * Form for control settings change event.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class PanelEventNetworkControl extends AbstractEventPanel implements ItemListener {
	private static final long serialVersionUID = -7281591587053819591L;
	
	private JCheckBox cbControl = new JCheckBox("Mainline Control");
	private JCheckBox cbQControl = new JCheckBox("Queue Control");
	
	
	/**
	 * Initializes control settings change event editing panel.
	 * @param ne Network Element.
	 * @param em Event Manager.
	 * @param etm Event Table model.
	 */
	public synchronized void initialize(AbstractNetworkElement ne, EventManager em, EventTableModel etm) {
		eventTable = etm;
		myEvent = new EventNetworkControl();
		((EventNetworkControl)myEvent).setControl(((NodeHWCNetwork)ne).isControlled());
		((EventNetworkControl)myEvent).setQControl(((NodeHWCNetwork)ne).hasQControl());
		initialize(ne, em);
		return;
	}
	
	/**
	 * Initializes control settings change event editing panel.
	 * @param ne Network Element.
	 * @param em Event Manager.
	 * @param etm Event Table model.
	 * @param evt Event.
	 */
	public synchronized void initialize(AbstractNetworkElement ne, EventManager em, EventTableModel etm, AbstractEvent evt) {
		eventTable = etm;
		if (evt != null)
			myEvent = evt;
		else
			myEvent = new EventNetworkControl();
		initialize(ne, em);
		return;
	}

	/**
	 * Fills the panel with event specific fields.
	 */
	protected void fillPanel() {
		boolean cv = ((EventNetworkControl)myEvent).hasControl();
		cbControl.setSelected(cv);
		cbControl.addItemListener(this);
		cbQControl.setSelected(((EventNetworkControl)myEvent).hasQControl());
		cbQControl.setEnabled(cv);
		//Box sp = Box.createVerticalBox();
		JPanel pCB = new JPanel(new GridLayout(2, 0));
		pCB.setBorder(BorderFactory.createTitledBorder("Control Settings"));
		pCB.add(cbControl);
		pCB.add(cbQControl);
		//sp.add(pCB);
		add(pCB);
		return;
	}

	/**
	 * Returns window header with event description.
	 */
	public String getHeader() {
		return "Control State";
	}
	
	/**
	 * Saves event.
	 */
	public synchronized void save() {
		((EventNetworkControl)myEvent).setControl(cbControl.isSelected());
		((EventNetworkControl)myEvent).setQControl(cbQControl.isSelected());
		super.save();
		return;
	}

	/**
	 * Reaction to control checkbox change.
	 */
	public void itemStateChanged(ItemEvent e) {
		cbQControl.setEnabled(cbControl.isSelected());
		return;
	}

}