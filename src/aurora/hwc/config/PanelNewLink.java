/**
 * @(#)PanelNewLink.java
 */

package aurora.hwc.config;

import javax.swing.*;

import aurora.*;
import aurora.hwc.*;
import aurora.util.*;


/**
 * New Link form to be displayed during the Link creation.
 * @author Alex Kurzhanskiy
 * @version $Id: PanelNewLink.java,v 1.1.4.1 2008/10/16 04:27:06 akurzhan Exp $
 */
public final class PanelNewLink extends AbstractNewNetworkElementPanel {
	private static final long serialVersionUID = 5360520680716578067L;
	
	private AbstractNode begin = null;
	private AbstractNode end = null;
	private int[] types;
	private JSpinner idSpinner;
	private JSpinner lengthSpinner;
	private JSpinner widthSpinner;
	private JComboBox listTypes = new JComboBox();

	
	
	public PanelNewLink() { }
	public PanelNewLink(TreePane tpane, AbstractNodeComplex myNetwork, AbstractNode begin, AbstractNode end) {
		// ordinary link
		super(tpane, myNetwork, new Point());
		this.begin = begin;
		this.end = end;
		initPanel();
	}
	public PanelNewLink(TreePane tpane, AbstractNodeComplex myNetwork, AbstractNode begin, Point pointPosition) {
		// destination link
		super(tpane, myNetwork, pointPosition);
		this.begin = begin;
		initPanel();
	}
	public PanelNewLink(TreePane tpane, AbstractNodeComplex myNetwork, Point pointPosition, AbstractNode end) {
		// source link
		super(tpane, myNetwork, pointPosition);
		this.end = end;
		initPanel();
	}
	
	
	/**
	 * Generates default ID for the Link.
	 */
	private int defaultId() {
		int id = 0;
		AbstractNetworkElement ne = null;
		if (begin != null) {
			id = begin.getId() * 10;
			do {
				id++;
				ne = begin.getSuccessorById(id);
			} while (ne != null);
		}
		else if (end != null) {
			id = - end.getId() * 10;
			do {
				id--;
				ne = end.getPredecessorById(id);
			} while (ne != null);
		}
		return id;
	}
	
	/**
	 * Panel initialization.
	 */
	private void initPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// ID
		JPanel pPrm = new JPanel(new SpringLayout());
		pPrm.setBorder(BorderFactory.createTitledBorder("ID"));
		int id = defaultId();
		idSpinner = new JSpinner(new SpinnerNumberModel(id, -999999999, 999999999, 1));
		idSpinner.setEditor(new JSpinner.NumberEditor(idSpinner, "#######0"));
		pPrm.add(idSpinner);
		SpringUtilities.makeCompactGrid(pPrm, 1, 1, 2, 2, 2, 2);
		add(pPrm);
		// Length
		pPrm = new JPanel(new SpringLayout());
		pPrm.setBorder(BorderFactory.createTitledBorder("Length (miles)"));
		lengthSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 999.99, 0.01));
		lengthSpinner.setEditor(new JSpinner.NumberEditor(lengthSpinner, "##0.00"));
		pPrm.add(lengthSpinner);
		SpringUtilities.makeCompactGrid(pPrm, 1, 1, 2, 2, 2, 2);
		add(pPrm);
		// Width
		pPrm = new JPanel(new SpringLayout());
		pPrm.setBorder(BorderFactory.createTitledBorder("Number of Lanes"));
		widthSpinner = new JSpinner(new SpinnerNumberModel(1.0, 1.0, 20.0, 1.0));
		widthSpinner.setEditor(new JSpinner.NumberEditor(widthSpinner, "#0.0"));
		pPrm.add(widthSpinner);
		SpringUtilities.makeCompactGrid(pPrm, 1, 1, 2, 2, 2, 2);
		add(pPrm);
		// Type
		pPrm = new JPanel(new SpringLayout());
		pPrm.setBorder(BorderFactory.createTitledBorder("Type"));
		types = TypesHWC.linkTypeArray();
		for (int i = 0; i < types.length; i++)
			listTypes.addItem(TypesHWC.typeString(types[i]));
		pPrm.add(listTypes);
		SpringUtilities.makeCompactGrid(pPrm, 1, 1, 2, 2, 2, 2);
		add(pPrm);
		new WindowNewNetworkElement(this);
		return;
	}
	
	/**
	 * Specifies the window header.
	 */
	public String getHeader() {
		return "New Link";
	}
	

	/**
	 * Creates new Node in the given Network.
	 */
	public void create() {
		AbstractLink lnk;
		int idx = listTypes.getSelectedIndex();
		try {
			Class c = Class.forName(TypesHWC.typeClassName(types[idx]));
			lnk = (AbstractLink)c.newInstance();
		}
		catch(Exception e) {
			JOptionPane.showMessageDialog(this, "Cannot create Link of type '" + TypesHWC.typeClassName(types[idx]) + "'.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		lnk.setId((Integer)idSpinner.getValue());
		lnk.setPosition(new PositionLink());
		if (begin == null)
			lnk.getPosition().setBegin(pointPosition);
		if (end == null)
			lnk.getPosition().setEnd(pointPosition);
		lnk.setMyNetwork(myNetwork);
		lnk.assignBeginNode(begin);
		lnk.assignEndNode(end);
		lnk.setLength((Double)lengthSpinner.getValue());
		((AbstractLinkHWC)lnk).setDynamics(new DynamicsCTM());
		((AbstractLinkHWC)lnk).setLanes((Double)widthSpinner.getValue());
		((AbstractLinkHWC)lnk).defaultFD();
		myNetwork.addLink(lnk);
		tpane.addLinkComponent(lnk, myNetwork);
		return;
	}

}
