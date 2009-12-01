/**
 * @(#)AbstractEventPanel.java
 */

package aurora.hwc.gui;

import java.awt.*;
import javax.swing.*;
import aurora.*;
import aurora.hwc.*;
import aurora.util.*;


/**
 * Base class for event editing panels.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractEventPanel.java,v 1.1.2.6.4.1 2009/09/30 23:40:22 akurzhan Exp $
 */
public abstract class AbstractEventPanel extends JPanel {
	private static final long serialVersionUID = 6647543272805258031L;
	
	protected AbstractNetworkElement myNE = null;
	protected EventManager myEventManager = null;
	protected AbstractEvent myEvent = null;
	protected EventTableModel eventTable = null;
	
	protected WindowEventEditor winEE;
	
	protected JTextPane desc = new JTextPane();
	protected JSpinner hh;
	protected JSpinner mm;
	protected JSpinner ss;
	
	
	/**
	 * Fills the panel with event specific fields.
	 */
	protected abstract void fillPanel();
	
	/**
	 * Returns window header with event description.
	 */
	public abstract String getHeader();
	
	/**
	 * Initializes event editing panel.
	 * @param ne Network Element.
	 * @param em Event Manager.
	 * @param etm Event Table model.
	 */
	public abstract void initialize(AbstractNetworkElement ne, EventManager em, EventTableModel etm);
	
	/**
	 * Initializes event editing panel.
	 * @param ne Network Element.
	 * @param em Event Manager.
	 * @param etm Event Table model.
	 * @param evt Event.
	 */
	public abstract void initialize(AbstractNetworkElement ne, EventManager em, EventTableModel etm, AbstractEvent evt);
	
	/**
	 * Initializes event editing panel.
	 * @param ne Network Element.
	 * @param em Event Manager.
	 * @param evt Event.
	 */
	protected void initialize(AbstractNetworkElement ne, EventManager em) {
		myNE = ne;
		myEventManager = em;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel pH = new JPanel(new FlowLayout());
		JLabel lD = new JLabel("<html><font color=\"blue\">" + TypesHWC.typeString(myNE.getType()) + " " + myNE.toString() + "</font></html>");
		pH.add(lD);
		add(pH);
		//Box dp = Box.createVerticalBox();
		JPanel pD = new JPanel(new BorderLayout());
		pD.setBorder(BorderFactory.createTitledBorder("Description"));
		desc.setText(myEvent.getDescription());
		pD.add(new JScrollPane(desc), BorderLayout.CENTER);
		//dp.add(pD);
		add(pD);
		fillPanel();
		//Box fp = Box.createVerticalBox();
		JPanel pT = new JPanel(new FlowLayout());
		pT.setBorder(BorderFactory.createTitledBorder("Activation Time"));
		hh = new JSpinner(new SpinnerNumberModel(Util.getHours(myEvent.getTime()), 0, 99, 1));
		hh.setEditor(new JSpinner.NumberEditor(hh, "00"));
		pT.add(hh);
		pT.add(new JLabel("h "));
		mm = new JSpinner(new SpinnerNumberModel(Util.getMinutes(myEvent.getTime()), 0, 59, 1));
		mm.setEditor(new JSpinner.NumberEditor(mm, "00"));
		pT.add(mm);
		pT.add(new JLabel("m "));
		ss = new JSpinner(new SpinnerNumberModel(Util.getSeconds(myEvent.getTime()), 0, 59.99, 1));
		ss.setEditor(new JSpinner.NumberEditor(ss, "00.##"));
		pT.add(ss);
		pT.add(new JLabel("s"));
		//fp.add(pT);
		add(pT);
		winEE = new WindowEventEditor(this, null);
		winEE.setVisible(true);
		return;
	}
	
	/**
	 * Saves event into the list.
	 */
	public synchronized void save() {
		myEvent.setNEID(myNE.getId());
		myEvent.setEnabled(true);
		myEvent.setDescription(desc.getText());
		int h = (Integer)hh.getValue();
		int m = (Integer)mm.getValue();
		double s = (Double)ss.getValue();
		myEvent.setTime(h + (m/60.0) + (s/3600.0));
		myEventManager.clearEvent(myEvent);
		myEventManager.addEvent(myEvent);
		if (eventTable != null)
			eventTable.updateData();
		return;
	}

}
