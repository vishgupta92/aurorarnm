/**
 * @(#)PanelEventQueueMax.java 
 */

package aurora.hwc.gui;

import javax.swing.*;
import aurora.*;
import aurora.hwc.*;
import aurora.util.*;


/**
 * Form for queue limit change event.
 * @author Alex Kurzhanskiy
 * @version $Id: PanelEventQueueMax.java,v 1.1.2.4 2008/10/16 04:27:09 akurzhan Exp $
 */
public final class PanelEventQueueMax extends AbstractEventPanel {
	private static final long serialVersionUID = 1222004371856565961L;
	
	private JSpinner spinQLimit;
	
	
	/**
	 * Initializes queue limit change event editing panel.
	 * @param ne Network Element.
	 * @param em Event Manager.
	 * @param etm Event Table model.
	 */
	public void initialize(AbstractNetworkElement ne, EventManager em, EventTableModel etm) {
		eventTable = etm;
		myEvent = new EventQueueMax();
		((EventQueueMax)myEvent).setQueueMax((Double)((AbstractLinkHWC)ne).getQueueMax());
		initialize(ne, em);
		return;
	}
	
	/**
	 * Initializes queue limit change event editing panel.
	 * @param ne Network Element.
	 * @param em Event Manager.
	 * @param etm Event Table model.
	 * @param evt Event.
	 */
	public void initialize(AbstractNetworkElement ne, EventManager em, EventTableModel etm, AbstractEvent evt) {
		eventTable = etm;
		if (evt != null)
			myEvent = evt;
		else
			myEvent = new EventQueueMax();
		initialize(ne, em);
		return;
	}

	/**
	 * Fills the panel with event specific fields.
	 */
	protected void fillPanel() {
		//Box qlp = Box.createVerticalBox();
		JPanel pQL = new JPanel(new SpringLayout());
		pQL.setBorder(BorderFactory.createTitledBorder("Queue Limit"));
		spinQLimit = new JSpinner(new SpinnerNumberModel(((EventQueueMax)myEvent).getQueueMax(), 0, 99999, 1));
		spinQLimit.setEditor(new JSpinner.NumberEditor(spinQLimit, "###0"));
		pQL.add(spinQLimit);
		SpringUtilities.makeCompactGrid(pQL, 1, 1, 2, 2, 2, 2);
		//qlp.add(pQL);
		add(pQL);
		return;
	}

	/**
	 * Returns window header with event description.
	 */
	public String getHeader() {
		return "Queue Limit";
	}
	
	/**
	 * Saves event.
	 */
	public synchronized void save() {
		((EventQueueMax)myEvent).setQueueMax((Double)spinQLimit.getValue());
		super.save();
		return;
	}

}
