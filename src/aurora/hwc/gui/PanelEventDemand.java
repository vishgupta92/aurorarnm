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
 * @version $Id: PanelEventDemand.java,v 1.1.2.5.4.1 2009/10/18 00:58:29 akurzhan Exp $
 */
public final class PanelEventDemand extends AbstractEventPanel {
	private static final long serialVersionUID = 1106771938653639149L;
	
	//private JSpinner spinKnob;
	private JTextField dcTF;
	
	
	/**
	 * Initializes demand knob change event editing panel.
	 * @param ne Network Element.
	 * @param em Event Manager.
	 * @param etm Event Table model.
	 */
	public synchronized void initialize(AbstractNetworkElement ne, EventManager em, EventTableModel etm) {
		eventTable = etm;
		myEvent = new EventDemand();
		((EventDemand)myEvent).setDemandKnobs(((AbstractLinkHWC)ne).getDemandKnobs());
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
		JPanel pKnob = new JPanel(new SpringLayout());
		pKnob.setBorder(BorderFactory.createTitledBorder("Demand Coefficient"));
		dcTF = new JTextField(((EventDemand)myEvent).getDemandKnobsAsString());
		pKnob.add(dcTF);
		SpringUtilities.makeCompactGrid(pKnob, 1, 1, 2, 2, 2, 2);
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
		((EventDemand)myEvent).setDemandKnobs(dcTF.getText());
		super.save();
		return;
	}

}