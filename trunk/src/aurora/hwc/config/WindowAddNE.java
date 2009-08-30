/**
 * @(#)WindowAddNE.java 
 */

package aurora.hwc.config;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import aurora.*;


/**
 * Window for adding Network Elements to Control Monitor.
 * @author Alex Kurzhanskiy
 * @version $Id: WindowAddNE.java,v 1.1.2.2 2009/08/16 20:10:27 akurzhan Exp $
 */
public final class WindowAddNE extends JDialog implements ActionListener {
	private static final long serialVersionUID = -2547337843477632230L;
	
	private Vector<AbstractNetworkElement> neList = new Vector<AbstractNetworkElement>();
	private Vector<AbstractMonitor> monitors = new Vector<AbstractMonitor>();
	private Vector<AbstractNode> nodes = new Vector<AbstractNode>();
	private Vector<AbstractLink> links = new Vector<AbstractLink>();
	private Vector<Boolean> monChecked = new Vector<Boolean>();
	private Vector<Boolean> ndChecked = new Vector<Boolean>();
	private Vector<Boolean> lnkChecked = new Vector<Boolean>();
	
	private AbstractMonitor myMonitor = null;
	
	private Box generalPanel = Box.createVerticalBox();
	private Box monitorPanel = Box.createVerticalBox();
	private Box nodePanel = Box.createVerticalBox();
	private Box linkPanel = Box.createVerticalBox();
	
	private JTable mtable;
	private MonTabModel mtabmodel = new MonTabModel();
	private JTable ntable;
	private NdTabModel ntabmodel = new NdTabModel();
	private JTable ltable;
	private LnkTabModel ltabmodel = new LnkTabModel();

	private final static String cmdOK = "pressedOK";
	private final static String cmdCancel = "pressedCancel";
	
	
	public WindowAddNE() { }
	public WindowAddNE(JFrame parent, String title, WindowMonitorController wmc, Vector<AbstractNetworkElement> nes, boolean controlled) {
		super(parent, title);
		setSize(400, 500);
		setLocationRelativeTo(parent);
		setModal(true);
		if (nes != null)
			neList = nes;
		if (wmc != null)
			myMonitor = wmc.getMyMonitor();
		AbstractNodeComplex ntwk = myMonitor.getMyNetwork();
		if (controlled) {
			Vector<AbstractMonitor> mons = ntwk.getMonitors();
			for (int i = 0; i < mons.size(); i++) {
				if ((neList.indexOf(mons.get(i)) < 0) && (!myMonitor.equals(mons.get(i)))) {
					monitors.add(mons.get(i));
					monChecked.add(new Boolean(false));
				}
			}
		}
		Vector<AbstractNode> nds = ntwk.getNodes();
		for (int i = 0; i < nds.size(); i++) {
			if (neList.indexOf(nds.get(i)) < 0) {
				nodes.add(nds.get(i));
				ndChecked.add(new Boolean(false));
			}
		}
		Vector<AbstractLink> lnks = ntwk.getLinks();
		for (int i = 0; i < nds.size(); i++) {
			if (neList.indexOf(lnks.get(i)) < 0) {
				if ((controlled) && (lnks.get(i).getEndNode() == null))
					continue;
				else {
					links.add(lnks.get(i));
					lnkChecked.add(new Boolean(false));
				}
			}
		}
		JPanel panelMain = new JPanel(new BorderLayout());
		fillPanel();
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
		panelMain.add(generalPanel, BorderLayout.CENTER);
		panelMain.add(bp, BorderLayout.SOUTH);
		setContentPane(panelMain);
		setVisible(true);
	}
	
	
	/**
	 * Generates main panel.
	 */
	private void fillPanel() {
		GridBagConstraints c = new GridBagConstraints();
		monitorPanel.setBorder(BorderFactory.createTitledBorder("Monitors"));
		nodePanel.setBorder(BorderFactory.createTitledBorder("Nodes"));
		linkPanel.setBorder(BorderFactory.createTitledBorder("Links"));
		// tables
		mtable = new JTable(mtabmodel);
		mtable.setPreferredScrollableViewportSize(new Dimension(200, 200));
		mtable.getColumnModel().getColumn(0).setPreferredWidth(180);
		mtable.getColumnModel().getColumn(1).setPreferredWidth(20);
		monitorPanel.add(new JScrollPane(mtable), c);
		ntable = new JTable(ntabmodel);
		ntable.setPreferredScrollableViewportSize(new Dimension(200, 200));
		ntable.getColumnModel().getColumn(0).setPreferredWidth(180);
		ntable.getColumnModel().getColumn(1).setPreferredWidth(20);
		nodePanel.add(new JScrollPane(ntable), c);
		ltable = new JTable(ltabmodel);
		ltable.setPreferredScrollableViewportSize(new Dimension(200, 200));
		ltable.getColumnModel().getColumn(0).setPreferredWidth(180);
		ltable.getColumnModel().getColumn(1).setPreferredWidth(20);
		linkPanel.add(new JScrollPane(ltable), c);
		if (monitors.size() > 0)
			generalPanel.add(monitorPanel);
		if (nodes.size() > 0)
			generalPanel.add(nodePanel);
		if (links.size() > 0)
			generalPanel.add(linkPanel);
		return;
	}
		
	/**
	 * Reaction to OK/Cancel buttons.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmdOK.equals(cmd)) {
			for (int i = 0; i < monitors.size(); i++)
				if (monChecked.get(i))
					neList.add(monitors.get(i));
			for (int i = 0; i < nodes.size(); i++)
				if (ndChecked.get(i))
					neList.add(nodes.get(i));
			for (int i = 0; i < links.size(); i++)
				if (lnkChecked.get(i))
					neList.add(links.get(i));
			setVisible(false);
			dispose();
			return;
		}
		if (cmdCancel.equals(cmd)) {
			setVisible(false);
			dispose();
			return;
		}
		return;
	}
	
	
	/**
	 * Class needed for monitors in a table.
	 */
	private class MonTabModel extends AbstractTableModel {
		private static final long serialVersionUID = -5894438056261761682L;

		public String getColumnName(int col) {
			switch(col) {
	        case 0:
	        	return "Name";
	        default:
	        	return "Selected";
	        }
	    }
		
		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return monitors.size();
		}

		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row >= monitors.size()))
				return null;
			switch(column) {
			case 0:
				return monitors.get(row);
			default:
				return monChecked.get(row);
			}
		}
		
		/**
	     * JTable uses this method to determine the default renderer/
	     * editor for each cell.  If we didn't implement this method,
	     * then the last column would contain text ("true"/"false"),
	     * rather than a check box.
	     */
	    @SuppressWarnings("unchecked")
		public Class getColumnClass(int c) {
	    	Class cl = String.class;
	    	try {
	    		cl = getValueAt(0, c).getClass();
	    	}
	    	catch(Exception e) { }
	       return cl;
	    }
		
		public boolean isCellEditable(int row, int column) {
			if (column == 1)
				return true;
			return false;
		}
		
		public void setValueAt(Object value, int row, int column) {
			if ((row < 0) || (row >= monitors.size()) || (column != 1))
				return;
			try {
				monChecked.set(row, (Boolean)value);
			}
			catch(Exception e) { }
			fireTableRowsUpdated(row, row);
			return;
		}
	}
	
	
	/**
	 * Class needed for nodes in a table.
	 */
	private class NdTabModel extends AbstractTableModel {
		private static final long serialVersionUID = 2528890830267682904L;

		public String getColumnName(int col) {
			switch(col) {
	        case 0:
	        	return "Name";
	        default:
	        	return "Selected";
	        }
	    }
		
		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return nodes.size();
		}

		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row >= nodes.size()))
				return null;
			switch(column) {
			case 0:
				return nodes.get(row);
			default:
				return ndChecked.get(row);
			}
		}
		
		/**
	     * JTable uses this method to determine the default renderer/
	     * editor for each cell.  If we didn't implement this method,
	     * then the last column would contain text ("true"/"false"),
	     * rather than a check box.
	     */
	    @SuppressWarnings("unchecked")
		public Class getColumnClass(int c) {
	    	Class cl = String.class;
	    	try {
	    		cl = getValueAt(0, c).getClass();
	    	}
	    	catch(Exception e) { }
	       return cl;
	    }
		
		public boolean isCellEditable(int row, int column) {
			if (column == 1)
				return true;
			return false;
		}
		
		public void setValueAt(Object value, int row, int column) {
			if ((row < 0) || (row >= nodes.size()) || (column != 1))
				return;
			try {
				ndChecked.set(row, (Boolean)value);
			}
			catch(Exception e) { }
			fireTableRowsUpdated(row, row);
			return;
		}
	}
	
	
	/**
	 * Class needed for links in a table.
	 */
	private class LnkTabModel extends AbstractTableModel {
		private static final long serialVersionUID = -5414979002815328999L;

		public String getColumnName(int col) {
			switch(col) {
	        case 0:
	        	return "Name";
	        default:
	        	return "Selected";
	        }
	    }
		
		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return links.size();
		}

		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row >= links.size()))
				return null;
			switch(column) {
			case 0:
				return links.get(row);
			default:
				return lnkChecked.get(row);
			}
		}
		
		/**
	     * JTable uses this method to determine the default renderer/
	     * editor for each cell.  If we didn't implement this method,
	     * then the last column would contain text ("true"/"false"),
	     * rather than a check box.
	     */
	    @SuppressWarnings("unchecked")
		public Class getColumnClass(int c) {
	    	Class cl = String.class;
	    	try {
	    		cl = getValueAt(0, c).getClass();
	    	}
	    	catch(Exception e) { }
	       return cl;
	    }
		
		public boolean isCellEditable(int row, int column) {
			if (column == 1)
				return true;
			return false;
		}
		
		public void setValueAt(Object value, int row, int column) {
			if ((row < 0) || (row >= links.size()) || (column != 1))
				return;
			try {
				lnkChecked.set(row, (Boolean)value);
			}
			catch(Exception e) { }
			fireTableRowsUpdated(row, row);
			return;
		}
	}

}