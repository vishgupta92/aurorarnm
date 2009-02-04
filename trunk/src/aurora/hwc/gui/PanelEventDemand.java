/**
 * @(#)PanelEventDemand.java 
 */

package aurora.hwc.gui;

import javax.swing.*;
import aurora.*;
import aurora.hwc.*;
import aurora.util.*;


/**
 * Form for demand knob change event.
 * @author Alex Kurzhanskiy
 * @version $Id: PanelEventDemand.java,v 1.1.2.5 2008/10/16 04:27:08 akurzhan Exp $
 */
public final class PanelEventDemand extends AbstractEventPanel {
	private static final long serialVersionUID = 1106771938653639149L;
	
	private JSpinner spinKnob;
	
	
	/**
	 * Initializes demand knob change event editing panel.
	 * @param ne Network Element.
	 * @param em Event Manager.
	 * @param etm Event Table model.
	 */
	public synchronized void initialize(AbstractNetworkElement ne, EventManager em, EventTableModel etm) {
		eventTable = etm;
		myEvent = new EventDemand();
		((EventDemand)myEvent).setDemandKnob((Double)((AbstractLinkHWC)ne).getDemandKnob());
		initialize(ne, em);
		return;
	}
	
	/**
	 * Initializes demand knob change event editing panel.
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
			myEvent = new EventDemand();
		initialize(ne, em);
		return;
	}

	/**
	 * Fills the panel with event specific fields.
	 */
	protected void fillPanel() {
		//Box kp = Box.createVerticalBox();
		JPanel pKnob = new JPanel(new SpringLayout());
		pKnob.setBorder(BorderFactory.createTitledBorder("Demand Coefficient"));
		spinKnob = new JSpinner(new SpinnerNumberModel(((EventDemand)myEvent).getDemandKnob(), 0, 9999, 0.01));
		spinKnob.setEditor(new JSpinner.NumberEditor(spinKnob, "###0.00"));
		pKnob.add(spinKnob);
		SpringUtilities.makeCompactGrid(pKnob, 1, 1, 2, 2, 2, 2);
		//kp.add(pKnob);
		add(pKnob);
		return;
	}

	/**
	 * Returns window header with event description.
	 */
	public String getHeader() {
		return "Demand Coefficient";
	}
	
	/**
	 * Saves event.
	 */
	public synchronized void save() {
		((EventDemand)myEvent).setDemandKnob((Double)spinKnob.getValue());
		super.save();
		return;
	}

}