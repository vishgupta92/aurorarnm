/**
 * @(#)PanelNewMonitor.java
 */

package aurora.hwc.config;

import java.awt.BorderLayout;
import javax.swing.*;
import aurora.*;
import aurora.hwc.*;
import aurora.util.*;


/**
 * New Monitor form to be displayed during the Monitor creation.
 * @author Alex Kurzhanskiy
 * @version $Id: PanelNewMonitor.java,v 1.1.2.1 2009/01/11 19:46:29 akurzhan Exp $
 */
public final class PanelNewMonitor extends AbstractNewNetworkElementPanel {
	private static final long serialVersionUID = -8991762453694679366L;
	
	private String defaultName = "New Monitor";
	private int[] types;
	private JSpinner idSpinner;
	private JTextPane descText = new JTextPane();
	private JComboBox listTypes = new JComboBox();

	
	public PanelNewMonitor() { }
	public PanelNewMonitor(TreePane tpane, AbstractNodeComplex myNetwork) {
		super(tpane, myNetwork, new Point());
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// ID
		JPanel pPrm = new JPanel(new SpringLayout());
		pPrm.setBorder(BorderFactory.createTitledBorder("ID"));
		idSpinner = new JSpinner(new SpinnerNumberModel(1, -999999999, 99999999, 1));
		idSpinner.setEditor(new JSpinner.NumberEditor(idSpinner, "#######0"));
		pPrm.add(idSpinner);
		SpringUtilities.makeCompactGrid(pPrm, 1, 1, 2, 2, 2, 2);
		add(pPrm);
		// Description
		pPrm = new JPanel(new BorderLayout());
		pPrm.setBorder(BorderFactory.createTitledBorder("Description"));
		JScrollPane descScroll = new JScrollPane(descText);
		pPrm.add(descScroll, BorderLayout.CENTER);
		add(pPrm);
		// Type
		pPrm = new JPanel(new SpringLayout());
		pPrm.setBorder(BorderFactory.createTitledBorder("Type"));
		types = TypesHWC.monitorTypeArray();
		for (int i = 0; i < types.length; i++)
			listTypes.addItem(TypesHWC.typeString(types[i]));
		pPrm.add(listTypes);
		SpringUtilities.makeCompactGrid(pPrm, 1, 1, 1, 2, 2, 2);
		add(pPrm);
		new WindowNewNetworkElement(this);
	}
	
	
	/**
	 * Specifies the window header.
	 */
	public String getHeader() {
		return defaultName;
	}
	

	/**
	 * Creates new Node in the given Network.
	 */
	public void create() {
		AbstractMonitor mon = null;
		int idx = listTypes.getSelectedIndex();
		try {
			Class c = Class.forName(TypesHWC.typeClassName(types[idx]));
			mon = (AbstractMonitor)c.newInstance();
		}
		catch(Exception e) {
			JOptionPane.showMessageDialog(this, "Cannot create Monitor of type '" + TypesHWC.typeClassName(types[idx]) + "'.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		mon.setId((Integer)idSpinner.getValue());
		mon.setDescription(descText.getText());
		mon.setMyNetwork(myNetwork);
		myNetwork.addMonitor(mon);
		tpane.addMonitorComponent(mon, myNetwork);
		return;
	}

}
