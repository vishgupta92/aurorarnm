/**
 * @(#)WindowMonitorControl.java 
 */

package aurora.hwc.config;

import java.awt.*;
import java.awt.Point;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import aurora.*;
import aurora.hwc.*;
import aurora.hwc.control.AbstractPanelController;
import aurora.util.*;


/**
 * Window for Control Monitor display in the Configurator.
 * @author Alex Kurzhanskiy
 * @version $Id: WindowMonitorController.java,v 1.1.2.3 2009/10/01 05:49:01 akurzhan Exp $
 */
public final class WindowMonitorController extends JInternalFrame implements ActionListener, ChangeListener, DocumentListener {
	private static final long serialVersionUID = 6444306218621414981L;
	
	
	private AbstractContainer mySystem = null;
	private MonitorControllerHWC myMonitor = null;
	private AbstractControllerComplex myController = null;
	private TreePane treePane;
	private Vector<AbstractNetworkElement> monitored = new Vector<AbstractNetworkElement>();
	private Vector<AbstractNetworkElement> controlled = new Vector<AbstractNetworkElement>();
	
	private Box generalPanel = Box.createVerticalBox();
	private Box monitorPanel = Box.createVerticalBox();
	private JPanel mlpanel = new JPanel(new GridBagLayout());
	private Box controlPanel = Box.createVerticalBox();
	private JPanel cpanel = new JPanel(new GridBagLayout());
	private JPanel pcl = new JPanel(new FlowLayout());
	private JSpinner idSpinner;
	private JTextPane descTxt = new JTextPane();
	private JPanel pID = new JPanel(new SpringLayout());
	private final static String nmID = "ID";
	private JPanel pDesc = new JPanel(new BorderLayout());
	
	private JTable montable;
	private MonTableModel montablemodel = new MonTableModel();
	private JTable ctrltable;
	private CtrlTableModel ctrltablemodel = new CtrlTableModel();
	private JComboBox listCControllers;
	private JButton buttonProp = new JButton("Properties");
	
	private boolean idModified = false;
	private boolean descModified = false;
	private boolean monlistModified = false;
	private boolean ctrllistModified = false;
	private boolean ctrlModified = false;
	
	private final static String cmdMonAdd = "pressedMonAdd";
	private final static String cmdMonDelete = "pressedMonDelete";
	private final static String cmdCtrlAdd = "pressedCtrlAdd";
	private final static String cmdCtrlDelete = "pressedCtrlDelete";
	private final static String cmdCtrlProp = "pressedCtrlProp";
	private final static String cmdCtrlList = "pressedCtrlList";
	private final static String cmdOK = "pressedOK";
	private final static String cmdCancel = "pressedCancel";
	
	
	public WindowMonitorController() { }
	public WindowMonitorController(AbstractContainer ctnr, MonitorControllerHWC mntr, TreePane tpane) {
		super("Monitor: Control", true, true, true, true);
		mySystem = ctnr;
		myMonitor = mntr;
		myController = myMonitor.getMyController();
		treePane = tpane;
		Vector<AbstractNetworkElement> predecessors = myMonitor.getPredecessors();
		for (int i = 0; i < predecessors.size(); i++)
			monitored.add(predecessors.get(i));
		Vector<AbstractNetworkElement> successors = myMonitor.getSuccessors();
		for (int i = 0; i < successors.size(); i++)
			controlled.add(successors.get(i));
		setSize(400, 500);
		int n = treePane.getInternalFrameCount();
		setLocation(20*n, 20*n);
		AdapterWindowMonitorControl listener = new AdapterWindowMonitorControl();
		addInternalFrameListener(listener);
		addComponentListener(listener);
		JPanel panelMain = new JPanel(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		fillGeneralPanel();
		fillMonitorPanel();
		fillControlPanel();
		tabbedPane.add("General", new JScrollPane(generalPanel));
		tabbedPane.add("Monitor", new JScrollPane(monitorPanel));
		tabbedPane.add("Control", new JScrollPane(controlPanel));
		// OK, Cancel buttons
		JPanel bp = new JPanel(new FlowLayout());
		JButton bOK = new JButton("    OK    ");
		bOK.setActionCommand(cmdOK);
		bOK.addActionListener(this);
		JButton bCancel = new JButton("Cancel");
		bCancel.setActionCommand(cmdCancel);
		bCancel.addActionListener(this);
		bp.add(bOK);
		bp.add(bCancel);
		// add all subpanels to panel
		panelMain.add(tabbedPane, BorderLayout.CENTER);
		panelMain.add(bp, BorderLayout.SOUTH);
		setContentPane(panelMain);
	}
	
	
	/**
	 * Returns my Monitor.
	 */
	public AbstractMonitor getMyMonitor() {
		return myMonitor;
	}
	
	/**
	 * Generates General tab.
	 */
	private void fillGeneralPanel() {
		// ID
		pID.setBorder(BorderFactory.createTitledBorder("ID"));
		idSpinner = new JSpinner(new SpinnerNumberModel(myMonitor.getId(), -999999999, 999999999, 1));
		idSpinner.setEditor(new JSpinner.NumberEditor(idSpinner, "#######0"));
		idSpinner.setName(nmID);
		idSpinner.addChangeListener(this);
		pID.add(idSpinner);
		SpringUtilities.makeCompactGrid(pID, 1, 1, 2, 2, 2, 2);
		generalPanel.add(pID);
		// Description
		pDesc.setBorder(BorderFactory.createTitledBorder("Description"));
		descTxt.setText(myMonitor.getDescription());
		descTxt.getStyledDocument().addDocumentListener(this);
		pDesc.add(new JScrollPane(descTxt));
		generalPanel.add(pDesc);
		return;
	}
	
	/**
	 * Generates Monitor tab.
	 */
	private void fillMonitorPanel() {
		GridBagConstraints c = new GridBagConstraints();
		mlpanel.setBorder(BorderFactory.createTitledBorder("Monitored Network Elements"));
		// table
		montable = new JTable(montablemodel);
		montable.setPreferredScrollableViewportSize(new Dimension(200, 200));
		montable.getColumnModel().getColumn(0).setPreferredWidth(5);
		montable.getColumnModel().getColumn(1).setPreferredWidth(160);
		montable.getColumnModel().getColumn(2).setPreferredWidth(35);
		montable.addMouseListener(new MouseAdapter() { 
	      	  public void mouseClicked(MouseEvent e) { 
		      	    if (e.getClickCount() == 2) {
		      	    	int row = montable.rowAtPoint(new Point(e.getX(), e.getY()));
		      	    	AbstractNetworkElement ne = null;
		      	    	if ((row >= 0) && (row < myMonitor.getPredecessors().size()))
		      	    		ne = myMonitor.getPredecessors().get(row);
		      	    	else
		      	    		return;
		      	    	Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
		      	    	nelist.add(ne);
		      	    	treePane.actionSelected(nelist, true, false);
		      	    }
		      	    return;
		      	  }
		        });
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 100;
		c.weightx = 0.5;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 0;
		mlpanel.add(new JScrollPane(montable), c);
		c.ipady = 0; 
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridx = 0;
		// buttons
		JButton buttonAdd = new JButton("Add");
		buttonAdd.setEnabled(true);
		buttonAdd.setActionCommand(cmdMonAdd);
		buttonAdd.addActionListener(this);
		mlpanel.add(buttonAdd, c);
		c.gridx = 1;
		JButton buttonDelete = new JButton("Delete");
		buttonDelete.setEnabled(true);
		buttonDelete.setActionCommand(cmdMonDelete);
		buttonDelete.addActionListener(this);
		mlpanel.add(buttonDelete, c);
		monitorPanel.add(mlpanel);
		return;
	}
	
	/**
	 * Generates Control tab.
	 */
	private void fillControlPanel() {
		GridBagConstraints c = new GridBagConstraints();
		cpanel.setBorder(BorderFactory.createTitledBorder("Controlled Network Elements"));
		// table
		ctrltable = new JTable(ctrltablemodel);
		ctrltable.setPreferredScrollableViewportSize(new Dimension(200, 150));
		ctrltable.getColumnModel().getColumn(0).setPreferredWidth(5);
		ctrltable.getColumnModel().getColumn(1).setPreferredWidth(160);
		ctrltable.getColumnModel().getColumn(2).setPreferredWidth(35);
		ctrltable.addMouseListener(new MouseAdapter() { 
	      	  public void mouseClicked(MouseEvent e) { 
		      	    if (e.getClickCount() == 2) {
		      	    	int row = ctrltable.rowAtPoint(new Point(e.getX(), e.getY()));
		      	    	AbstractNetworkElement ne = null;
		      	    	if ((row >= 0) && (row < myMonitor.getSuccessors().size()))
		      	    		ne = myMonitor.getSuccessors().get(row);
		      	    	else
		      	    		return;
		      	    	Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
		      	    	nelist.add(ne);
		      	    	treePane.actionSelected(nelist, true, false);
		      	    }
		      	    return;
		      	  }
		        });
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 100;
		c.weightx = 0.5;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 0;
		cpanel.add(new JScrollPane(ctrltable), c);
		c.ipady = 0; 
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridx = 0;
		// buttons
		JButton buttonAdd = new JButton("Add");
		buttonAdd.setEnabled(true);
		buttonAdd.setActionCommand(cmdCtrlAdd);
		buttonAdd.addActionListener(this);
		cpanel.add(buttonAdd, c);
		c.gridx = 1;
		JButton buttonDelete = new JButton("Delete");
		buttonDelete.setEnabled(true);
		buttonDelete.setActionCommand(cmdCtrlDelete);
		buttonDelete.addActionListener(this);
		cpanel.add(buttonDelete, c);
		controlPanel.add(cpanel);
		// controller list
		buttonProp.setEnabled(false);
		buttonProp.setActionCommand(cmdCtrlProp);
		buttonProp.addActionListener(this);
		pcl.setBorder(BorderFactory.createTitledBorder("Complex Controller"));
		buttonProp.setEnabled(false);
		listCControllers = new JComboBox();
		listCControllers.addItem("None");
		String[] ctrlClasses = myMonitor.getComplexControllerClasses();
		for (int i = 0; i < ctrlClasses.length; i++) {
			if ((myController != null) && (myController.getClass().getName().compareTo(ctrlClasses[i]) == 0)) {
				listCControllers.addItem(myController);
				listCControllers.setSelectedIndex(i+1);
				buttonProp.setEnabled(true);
			}
			else {
				try {
					Class cl = Class.forName(ctrlClasses[i]);
					AbstractControllerComplex cc = (AbstractControllerComplex)cl.newInstance();
					cc.setMyMonitor(myMonitor);
					cc.initialize();
					listCControllers.addItem(cc);
				}
				catch(Exception e) { }
			}
		}
		listCControllers.setActionCommand(cmdCtrlList);
		listCControllers.addActionListener(this);
		pcl.add(listCControllers);
		pcl.add(buttonProp);
		controlPanel.add(pcl);
		return;
	}
		
	/**
	 * Action performed before closing the frame.
	 */
	private void close() {
		Vector<AbstractNetworkElement> monitors = new Vector<AbstractNetworkElement>();
		monitors.add(myMonitor);
		treePane.removeFrame(this, monitors);
		return;
	}
	
	/**
	 * Performs Monitor provisioning.
	 */
	private void provisionMonitorData() {
		if (idModified)
			myMonitor.setId((Integer)idSpinner.getValue());
		if (descModified)
			myMonitor.setDescription(descTxt.getText());
		if (monlistModified) {
			myMonitor.getPredecessors().clear();
			myMonitor.getPredecessors().addAll(monitored);
		}
		if (ctrllistModified) {
			myMonitor.getSuccessors().clear();
			myMonitor.getSuccessors().addAll(controlled);
		}
		if (ctrlModified) {
			myMonitor.setMyController(myController);
		}
		return;
	}
	
	/**
	 * Reaction to description update.
	 */
	private void descUpdate(DocumentEvent e) {
		if (e.getDocument().equals(descTxt.getDocument())) {
			descModified = true;
			pDesc.setBorder(BorderFactory.createTitledBorder("*Description"));
		}
		return;
	}
	
	/**
	 * Reaction to buttons and combo boxes.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmdMonAdd.equals(cmd)) {
			int sz = monitored.size();
			new WindowAddNE(null, "Add Network Elements to Monitored List", this, monitored, false);
			if (sz != monitored.size())
				monlistModified = true;
		}
		if (cmdMonDelete.equals(cmd)) {
			int[] selected = montable.getSelectedRows();
			if ((selected != null) && (selected.length > 0)) {
				for (int i = 0; i < selected.length; i++) {
					int idx = selected[i] - i;
					if ((idx >= 0) && (idx < montablemodel.getRowCount())) {
						monitored.remove(idx);
					}
				}
				monlistModified = true;
			}
		}
		if (monlistModified) {
			montablemodel.fireTableStructureChanged();
			montable.getColumnModel().getColumn(0).setPreferredWidth(5);
			montable.getColumnModel().getColumn(1).setPreferredWidth(160);
			montable.getColumnModel().getColumn(2).setPreferredWidth(35);
			mlpanel.setBorder(BorderFactory.createTitledBorder("*Monitored Network Elements"));
		}
		if (cmdCtrlAdd.equals(cmd)) {
			int sz = controlled.size();
			new WindowAddNE(null, "Add Network Elements to Controlled List", this, controlled, true);
			if (sz != controlled.size())
				ctrllistModified = true;
		}
		if (cmdCtrlDelete.equals(cmd)) {
			int[] selected = ctrltable.getSelectedRows();
			if ((selected != null) && (selected.length > 0)) {
				for (int i = 0; i < selected.length; i++) {
					int idx = selected[i] - i;
					if ((idx >= 0) && (idx < ctrltablemodel.getRowCount())) {
						controlled.remove(idx);
					}
				}
				ctrllistModified = true;
			}
		}
		if (ctrllistModified) {
			ctrltablemodel.fireTableStructureChanged();
			ctrltable.getColumnModel().getColumn(0).setPreferredWidth(5);
			ctrltable.getColumnModel().getColumn(1).setPreferredWidth(160);
			ctrltable.getColumnModel().getColumn(2).setPreferredWidth(35);
			cpanel.setBorder(BorderFactory.createTitledBorder("*Controlled Network Elements"));
		}
		if (cmdCtrlList.equals(cmd)) {
			JComboBox cb = (JComboBox)e.getSource();
			if (cb.getSelectedIndex() > 0) {
				myController = (AbstractControllerComplex)listCControllers.getSelectedItem();
				buttonProp.setEnabled(true);
			}
			else {
				buttonProp.setEnabled(false);
				myController = null;
			}
			ctrlModified = true;
		}
		if (cmdCtrlProp.equals(cmd)) {
			try {
				boolean[] modified = new boolean[1];
				modified[0] = false;
	    		Class c = Class.forName("aurora.hwc.control.Panel" + myController.getClass().getSimpleName());
	    		AbstractPanelController cp = (AbstractPanelController)c.newInstance();
	    		cp.initialize(myController, modified);
	    		if (modified[0])
	    			ctrlModified = true;
	    	}
	    	catch(Exception ex) { }
		}
		if (ctrlModified)
			pcl.setBorder(BorderFactory.createTitledBorder("*Complex Controller"));
		if (cmdOK.equals(cmd)) {
			if (idModified || descModified || monlistModified || ctrllistModified || ctrlModified) {
				provisionMonitorData();
				mySystem.getMyStatus().setSaved(false);
			}
			if (idModified) {
				Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
				nelist.add(myMonitor);
				treePane.modifyMonitorComponents(nelist);
			}
			setVisible(false);
			close();
			dispose();
			return;
		}
		if (cmdCancel.equals(cmd)) {
			setVisible(false);
			close();
			dispose();
			return;
		}
		return;
	}
	
	/**
	 * Reaction to spinner changes.
	 */
	public void stateChanged(ChangeEvent e) {
		String nm = ((JComponent)e.getSource()).getName();
		if (nm.equals(nmID)) {
			idModified = true;
			pID.setBorder(BorderFactory.createTitledBorder("*ID"));
			return;
		}
		return;
	}
	
	/**
	 * Reaction to character change in description.
	 */
	public void changedUpdate(DocumentEvent e) {
		descUpdate(e);
		return;
	}
	
	/**
	 * Reaction to character insertion into description.
	 */
	public void insertUpdate(DocumentEvent e) {
		descUpdate(e);
		return;
	}
	
	/**
	 * Reaction to character deletion from description.
	 */
	public void removeUpdate(DocumentEvent e) {
		descUpdate(e);
		return;
	}
	
	
	/**
	 * Class needed for proper closing of internal Monitor windows.
	 */
	private class AdapterWindowMonitorControl extends InternalFrameAdapter implements ComponentListener {
		
		/**
		 * Function that is called when user closes the window.
		 * @param e internal frame event.
		 */
		public void internalFrameClosing(InternalFrameEvent e) {
			close();
			return;
		}
		
		public void componentHidden(ComponentEvent e) {
			return;
		}

		public void componentMoved(ComponentEvent e) {
			return;
		}

		public void componentResized(ComponentEvent e) {
			return;
		}

		public void componentShown(ComponentEvent e) {
			return;
		}
		
	}
	
	
	/**
	 * Class needed for monitored Network Elements in a table.
	 */
	private class MonTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -3157484519723676367L;

		public String getColumnName(int col) {
			switch(col) {
	        case 0:
	        	return "Order";
	        case 1:
	        	return "Name";
	        default:
	        	return "Type";
	        }
	    }
		
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return monitored.size();
		}

		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row >= monitored.size()))
				return null;
			switch(column) {
			case 0:
				return "" + (row+1);
			case 1:
				return monitored.get(row);
			default:
				return TypesHWC.typeString(monitored.get(row).getType());
			}
		}
		
		public boolean isCellEditable(int row, int column) {
			if (column == 0)
				return true;
			return false;
		}
		
		public void setValueAt(Object value, int row, int column) {
			if ((row < 0) || (row >= monitored.size()) || (column != 0))
				return;
			String buf = (String)value;
			try {
				int v = Integer.parseInt(buf) - 1;
				v = Math.min(Math.max(v, 0), (monitored.size() - 1));
				AbstractNetworkElement ne = monitored.get(row);
				monitored.remove(row);
				monitored.insertElementAt(ne, v);
				if (row != v) {
					monlistModified = true;
					mlpanel.setBorder(BorderFactory.createTitledBorder("*Monitored Network Elements"));
				}
			}
			catch(Exception e) { }
			fireTableRowsUpdated(0, monitored.size()-1);
			return;
		}
	}
	
	
	/**
	 * Class needed for controlled Network Elements in a table.
	 */
	private class CtrlTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -1656026598785719356L;

		public String getColumnName(int col) {
			switch(col) {
	        case 0:
	        	return "Order";
	        case 1:
	        	return "Name";
	        default:
	        	return "Type";
	        }
	    }
		
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return controlled.size();
		}

		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row >= controlled.size()))
				return null;
			switch(column) {
			case 0:
				return "" + (row+1);
			case 1:
				return controlled.get(row);
			default:
				return TypesHWC.typeString(controlled.get(row).getType());
			}
		}
		
		public boolean isCellEditable(int row, int column) {
			if (column == 0)
				return true;
			return false;
		}
		
		public void setValueAt(Object value, int row, int column) {
			if ((row < 0) || (row >= controlled.size()) || (column != 0))
				return;
			String buf = (String)value;
			try {
				int v = Integer.parseInt(buf) - 1;
				v = Math.min(Math.max(v, 0), (controlled.size() - 1));
				AbstractNetworkElement ne = controlled.get(row);
				controlled.remove(row);
				controlled.insertElementAt(ne, v);
				if (row != v) {
					ctrllistModified = true;
					cpanel.setBorder(BorderFactory.createTitledBorder("*Controlled Network Elements"));
				}
			}
			catch(Exception e) { }
			fireTableRowsUpdated(0, controlled.size()-1);
			return;
		}
	}

}