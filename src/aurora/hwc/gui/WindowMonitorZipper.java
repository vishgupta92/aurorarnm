/**
 * @(#)WindowMonitorZipper.java 
 */

package aurora.hwc.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import aurora.hwc.*;


/**
 * Window for Zipper display in the Simulator.
 * @author Alex Kurzhanskiy
 * @version $Id: WindowMonitorZipper.java,v 1.1.2.4 2009/01/06 00:03:52 akurzhan Exp $
 */
public final class WindowMonitorZipper extends JInternalFrame implements ChangeListener {
	private static final long serialVersionUID = -3264811695543765319L;
	
	private MonitorZipperHWC myMonitor;
	private TreePane treePane;
	private dspROTableModel dspTM = new dspROTableModel();
	private Box confPanel = Box.createVerticalBox();
	
	private JCheckBox cbEnabled = new JCheckBox("Enabled");
	private boolean enabled = true;
	
	
	public WindowMonitorZipper() { }
	public WindowMonitorZipper(MonitorZipperHWC mntr, TreePane tpane) {
		super("Monitor: " + mntr.toString(), true, true, true, true);
		myMonitor = mntr;
		treePane = tpane;
		enabled = myMonitor.isEnabled();
		setSize(400, 500);
		int n = treePane.getInternalFrameCount();
		setLocation(20*n, 20*n);
		AdapterWindowMonitorZipper listener = new AdapterWindowMonitorZipper();
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
		JPanel dsp = new JPanel(new GridLayout(1, 0));
		dsp.setBorder(BorderFactory.createTitledBorder("Destination/Source Pairs"));
		final JTable dsptab = new JTable(dspTM);
		dsptab.setPreferredScrollableViewportSize(new Dimension(250, 70));
		dsp.add(new JScrollPane(dsptab));
		confPanel.add(dsp);
		dsptab.addMouseListener(new MouseAdapter() { 
	      	  public void mouseClicked(MouseEvent e) { 
		      	    if (e.getClickCount() == 2) {
		      	    	int row = dsptab.rowAtPoint(new Point(e.getX(), e.getY()));
		      	    	int clmn = dsptab.columnAtPoint(new Point(e.getX(), e.getY()));
		      	    	AbstractLinkHWC lnk = null;
		      	    	if ((clmn == 0) && (row >= 0) && (row < myMonitor.getPredecessors().size()))
		      	    		lnk = (AbstractLinkHWC)myMonitor.getPredecessors().get(row);
		      	    	else if ((clmn == 1) && (row >= 0) && (row < myMonitor.getSuccessors().size()))
		      	    		lnk = (AbstractLinkHWC)myMonitor.getSuccessors().get(row);
		      	    	else
		      	    		return;
		      	    	treePane.actionSelected(lnk, true);
		      	    }
		      	    return;
		      	  }
		        });
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
	 * Class needed for displaying destination/source pairs in a table.
	 */
	private class dspROTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -1656350523370830721L;

		public String getColumnName(int col) {
			if (col == 0)
				return "Destination Links";
	        return "Source Links";
	    }
		
		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return Math.min(myMonitor.getPredecessors().size(), myMonitor.getSuccessors().size());
		}

		public Object getValueAt(int row, int column) {
			if (column == 0)
				return myMonitor.getPredecessors().get(row).toString();
			return myMonitor.getSuccessors().get(row).toString();
		}
		
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
	}
	
	
	/**
	 * Class needed for proper closing of internal Monitor windows.
	 */
	private class AdapterWindowMonitorZipper extends InternalFrameAdapter implements ComponentListener {
		
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

}
