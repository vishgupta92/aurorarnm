/**
 * @(#)PanelEventControllerSimple.java 
 */

package aurora.hwc.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import aurora.*;
import aurora.hwc.*;
import aurora.hwc.control.*;
import aurora.util.*;


/**
 * Form for simple controller change event.
 * @author Alex Kurzhanskiy
 * @version $Id: PanelEventControllerSimple.java,v 1.1.2.6.2.2.2.3 2009/10/01 05:49:01 akurzhan Exp $
 */
public final class PanelEventControllerSimple extends AbstractEventPanel implements ActionListener {
	private static final long serialVersionUID = 2103920304528351836L;
	
	private int linkId = -1;
	private AbstractControllerSimpleHWC controller = null;
	
	private JComboBox listInLinks;
	private JComboBox listControllers;
	private JButton buttonProp = new JButton("Properties");

	
	/**
	 * Initializes simple controller change event editing panel.
	 * @param ne Network Element.
	 * @param em Event Manager.
	 * @param etm Event Table model.
	 */
	public synchronized void initialize(AbstractNetworkElement ne, EventManager em, EventTableModel etm) {
		eventTable = etm;
		myEvent = new EventControllerSimple();
		initialize(ne, em);
		controller = (AbstractControllerSimpleHWC)((AbstractNodeSimple)ne).getSimpleControllers().firstElement();
		return;
	}
	
	/**
	 * Initializes simple controller change event editing panel.
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
			myEvent = new EventControllerSimple();
		controller = (AbstractControllerSimpleHWC)((EventControllerSimple)myEvent).getController();
		linkId = ((EventControllerSimple)myEvent).getLinkId();
		initialize(ne, em);
		return;
	}

	/**
	 * Fills the panel with event specific fields.
	 */
	protected void fillPanel() {
		//Box lnkp = Box.createVerticalBox();
		JPanel pLL = new JPanel(new SpringLayout());
		pLL.setBorder(BorderFactory.createTitledBorder("Input Links"));
		listInLinks = new JComboBox();
		Vector<AbstractNetworkElement> links = myNE.getPredecessors();
		for (int i = 0; i < links.size(); i++) {
			listInLinks.addItem(links.get(i));
			if (links.get(i).getId() == linkId)
				listInLinks.setSelectedIndex(i);
		}
		pLL.add(listInLinks);
		SpringUtilities.makeCompactGrid(pLL, 1, 1, 2, 2, 2, 2);
		//lnkp.add(pLL);
		add(pLL);
		//Box ctrp = Box.createVerticalBox();
		JPanel pCL = new JPanel(new FlowLayout());
		pCL.setBorder(BorderFactory.createTitledBorder("Simple Controller"));
		buttonProp.setEnabled(false);
		listControllers = new JComboBox();
		listControllers.addItem("None");
		String[] ctrlClasses = ((AbstractNodeHWC)myNE).getSimpleControllerClasses();
		for (int i = 0; i < ctrlClasses.length; i++)
			if ((controller != null) && (controller.getClass().getName().compareTo(ctrlClasses[i]) == 0)) {
				listControllers.addItem(controller);
				listControllers.setSelectedIndex(i+1);
				buttonProp.setEnabled(true);
			}
			else
				try {
					Class c = Class.forName(ctrlClasses[i]);
					listControllers.addItem((AbstractControllerSimpleHWC)c.newInstance());
				}
				catch(Exception e) { }
		listControllers.addActionListener(this);
		pCL.add(listControllers);
		buttonProp.addActionListener(new ButtonEventsListener());
		pCL.add(buttonProp);
		//ctrp.add(pCL);
		add(pCL);
		return;
	}

	/**
	 * Returns window header with event description.
	 */
	public String getHeader() {
		return "Simple Controller";
	}
	
	/**
	 * Saves event.
	 */
	public synchronized void save() {
		int lkid = ((AbstractNetworkElement)listInLinks.getSelectedItem()).getId();
		((EventControllerSimple)myEvent).setController(controller);
		((EventControllerSimple)myEvent).setLinkId(lkid);
		super.save();
		return;
	}
	
	/**
	 * Callback for combo box selection change.
	 * @param e combo box selection change.
	 */
	public void actionPerformed(ActionEvent e) {
		JComboBox cb = (JComboBox)e.getSource();
		if (cb.getSelectedIndex() > 0) {
			controller = (AbstractControllerSimpleHWC)listControllers.getSelectedItem();
			buttonProp.setEnabled(true);
		}
		else {
			buttonProp.setEnabled(false);
			controller = null;
		}
		return;
	}
	
	
	/**
	 * This class is needed to react to "Properties" button pressed.
	 */
	private class ButtonEventsListener implements ActionListener {

		public void actionPerformed(ActionEvent ae) {
			try {
	    		Class c = Class.forName("aurora.hwc.control.Panel" + controller.getClass().getSimpleName());
	    		AbstractPanelSimpleController cp = (AbstractPanelSimpleController)c.newInstance();
	    		cp.initialize((AbstractControllerSimpleHWC)controller, null, -1, (AbstractNodeHWC)myNE);
	    	}
	    	catch(Exception e) { }
	    	return;
		}
		
	}

}