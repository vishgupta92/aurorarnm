/**
 * @(#)WindowMonitorController.java 
 */

package aurora.hwc.gui;

import java.awt.*;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import aurora.*;
import aurora.hwc.*;
import aurora.hwc.control.*;


/**
 * Window for Control Monitor display in the Simulator.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class WindowMonitorController extends JInternalFrame implements ActionListener, ChangeListener {
	private static final long serialVersionUID = -4670962979656887216L;
	
	private MonitorControllerHWC myMonitor;
	private AbstractControllerComplex myController = null;
	private TreePane treePane;
	
	private JCheckBox cbEnabled = new JCheckBox("Enabled");
	private boolean enabled = true;
	
	private JTable montable;
	private MonTableModel montablemodel = new MonTableModel();
	private JTable ctrltable;
	private CtrlTableModel ctrltablemodel = new CtrlTableModel();
	private JComboBox listCControllers;
	private JButton buttonProp = new JButton("Properties");
	
	private final static String cmdCtrlList = "pressedCtrlList";
	private final static String cmdCtrlProp = "pressedCtrlProp";
	
	private Box confPanel = Box.createVerticalBox();
	
	
	public WindowMonitorController() { }
	public WindowMonitorController(MonitorControllerHWC mntr, TreePane tpane) {
		super("Monitor: " + mntr.toString(), true, true, true, true);
		myMonitor = mntr;
		myController = myMonitor.getMyController();
		treePane = tpane;
		enabled = myMonitor.isEnabled();
		setSize(400, 500);
		int n = treePane.getInternalFrameCount();
		setLocation(20*n, 20*n);
		AdapterWindowMonitorController listener = new AdapterWindowMonitorController();
		addInternalFrameListener(listener);
		addComponentListener(listener);
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		fillConfigurationPanel();
		tabbedPane.add("Configuration", new JScrollPane(confPanel));
		getContentPane().add(tabbedPane);
	}
	
	
	/**
	 * Generates Configuration tab.
	 */
	private void fillConfigurationPanel() {
		JPanel desc = new JPanel(new GridLayout(2, 0));
		desc.setBorder(BorderFactory.createTitledBorder("Description"));
		desc.add(new JLabel("<html><font color=\"blue\">" + myMonitor.getDescription() + "</font></html>"));
		desc.add(cbEnabled);
		cbEnabled.setSelected(enabled);
		cbEnabled.addChangeListener(this);
		confPanel.add(desc);
		JPanel mlpanel = new JPanel(new GridLayout(1, 0));
		mlpanel.setBorder(BorderFactory.createTitledBorder("Monitored Network Elements"));
		montable = new JTable(montablemodel);
		montable.setPreferredScrollableViewportSize(new Dimension(200, 100));
		montable.getColumnModel().getColumn(0).setPreferredWidth(140);
		montable.getColumnModel().getColumn(1).setPreferredWidth(60);
		montable.addMouseListener(new MouseAdapter() { 
	      	  public void mouseClicked(MouseEvent e) { 
		      	    if (e.getClickCount() == 2) {
		      	    	int row = montable.rowAtPoint(new Point(e.getX(), e.getY()));
		      	    	AbstractNetworkElement ne = null;
		      	    	if ((row >= 0) && (row < myMonitor.getPredecessors().size()))
		      	    		ne = myMonitor.getPredecessors().get(row);
		      	    	else
		      	    		return;
		      	    	treePane.actionSelected(ne, true);
		      	    }
		      	    return;
		      	  }
		        });
		mlpanel.add(new JScrollPane(montable));
		confPanel.add(mlpanel);
		JPanel cpanel = new JPanel(new GridLayout(1, 0));
		cpanel.setBorder(BorderFactory.createTitledBorder("Controlleded Network Elements"));
		ctrltable = new JTable(ctrltablemodel);
		ctrltable.setPreferredScrollableViewportSize(new Dimension(200, 100));
		ctrltable.getColumnModel().getColumn(0).setPreferredWidth(140);
		ctrltable.getColumnModel().getColumn(1).setPreferredWidth(60);
		ctrltable.addMouseListener(new MouseAdapter() { 
	      	  public void mouseClicked(MouseEvent e) { 
		      	    if (e.getClickCount() == 2) {
		      	    	int row = ctrltable.rowAtPoint(new Point(e.getX(), e.getY()));
		      	    	AbstractNetworkElement ne = null;
		      	    	if ((row >= 0) && (row < myMonitor.getSuccessors().size()))
		      	    		ne = myMonitor.getSuccessors().get(row);
		      	    	else
		      	    		return;
		      	    	treePane.actionSelected(ne, true);
		      	    }
		      	    return;
		      	  }
		        });
		cpanel.add(new JScrollPane(ctrltable));
		confPanel.add(cpanel);
		JPanel pcl = new JPanel(new FlowLayout());
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
		confPanel.add(pcl);
		
		return;
	}
	
	
	/**
	 * Action performed before closing the frame.
	 */
	private void close() {
		treePane.removeFrame(this);
		return;
	}
	
	/**
	 * Reaction to buttons and combo boxes.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmdCtrlProp.equals(cmd)) {
			try {
	    		Class c = Class.forName("aurora.hwc.control.Panel" + myController.getClass().getSimpleName());
	    		AbstractPanelController cp = (AbstractPanelController)c.newInstance();
	    		cp.initialize(myController, null);
	    	}
	    	catch(Exception ex) { }
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
			myMonitor.setMyController(myController);
		}
		return;
	}
	
	/**
	 * Reaction to checkbox changes.
	 */
	public void stateChanged(ChangeEvent e) {
		if (myMonitor.getMyNetwork().getContainer().getMyStatus().isStopped()) {
			enabled = cbEnabled.isSelected();
			myMonitor.setEnabled(enabled);
		}
		else
			cbEnabled.setSelected(enabled);
		return;
	}
	
	
	/**
	 * Class needed for proper closing of internal Monitor windows.
	 */
	private class AdapterWindowMonitorController extends InternalFrameAdapter implements ComponentListener {
		
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
		private static final long serialVersionUID = -7601588619466450570L;

		public String getColumnName(int col) {
			switch(col) {
	        case 0:
	        	return "Name";
	        default:
	        	return "Type";
	        }
	    }
		
		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return myMonitor.getPredecessors().size();
		}

		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row >= myMonitor.getPredecessors().size()))
				return null;
			switch(column) {
			case 0:
				return myMonitor.getPredecessors().get(row);
			default:
				return TypesHWC.typeString(myMonitor.getPredecessors().get(row).getType());
			}
		}
		
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}
	
	
	/**
	 * Class needed for controlled Network Elements in a table.
	 */
	private class CtrlTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 2990798418405255672L;

		public String getColumnName(int col) {
			switch(col) {
	        case 0:
	        	return "Name";
	        default:
	        	return "Type";
	        }
	    }
		
		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return myMonitor.getSuccessors().size();
		}

		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row >= myMonitor.getSuccessors().size()))
				return null;
			switch(column) {
			case 0:
				return myMonitor.getSuccessors().get(row);
			default:
				return TypesHWC.typeString(myMonitor.getSuccessors().get(row).getType());
			}
		}
		
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	
}
