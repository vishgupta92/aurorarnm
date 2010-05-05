/**
 * @(#)WindowMonitorZipper.java 
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
import aurora.util.*;


/**
 * Window for Zipper display in the Configurator.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class WindowMonitorZipper extends JInternalFrame implements ActionListener, ChangeListener, DocumentListener {
	private static final long serialVersionUID = 2467662662857665813L;
	
	private AbstractContainer mySystem = null;
	private MonitorZipperHWC myMonitor;
	private TreePane treePane;
	private Vector<AbstractLink> destinations = new Vector<AbstractLink>();
	private Vector<AbstractLink> sources = new Vector<AbstractLink>();
	
	private Box generalPanel = Box.createVerticalBox();
	private JSpinner idSpinner;
	private JTextPane descTxt = new JTextPane();
	private dspTableModel dspTM = new dspTableModel();
	private JTable dsptab = new JTable(dspTM);
	private JPanel pID = new JPanel(new SpringLayout());
	private final static String nmID = "ID";
	private JPanel pDesc = new JPanel(new BorderLayout());
	private JPanel dsp = new JPanel(new GridBagLayout());
	
	private boolean idModified = false;
	private boolean descModified = false;
	private boolean pairsModified = false;
	
	private final static String cmdAdd = "pressedAdd";
	private final static String cmdEdit = "pressedEdit";
	private final static String cmdDelete = "pressedDelete";
	private final static String cmdOK = "pressedOK";
	private final static String cmdCancel = "pressedCancel";
	
	
	public WindowMonitorZipper() { }
	public WindowMonitorZipper(AbstractContainer ctnr, MonitorZipperHWC mntr, TreePane tpane) {
		super("Monitor: " + mntr.toString(), true, true, true, true);
		mySystem = ctnr;
		myMonitor = mntr;
		treePane = tpane;
		Vector<AbstractNetworkElement> predecessors = myMonitor.getPredecessors();
		Vector<AbstractNetworkElement> successors = myMonitor.getSuccessors();
		int sz = Math.min(predecessors.size(), successors.size());
		for (int i = 0; i < sz; i++) {
			destinations.add((AbstractLink)predecessors.get(i));
			sources.add((AbstractLink)successors.get(i));
		}
		setSize(400, 500);
		int n = treePane.getInternalFrameCount();
		setLocation(20*n, 20*n);
		AdapterWindowMonitorZipper listener = new AdapterWindowMonitorZipper();
		addInternalFrameListener(listener);
		addComponentListener(listener);
		JPanel panelMain = new JPanel(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		fillGeneralPanel();
		tabbedPane.add("General", new JScrollPane(generalPanel));
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
		// Destinations/Sources table
		dsp.setBorder(BorderFactory.createTitledBorder("Destination/Source Pairs"));
		dsptab.setPreferredScrollableViewportSize(new Dimension(250, 70));
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.ipady = 100;
		gc.weightx = 0.5;
		gc.gridwidth = 3;
		gc.gridx = 0;
		gc.gridy = 0;
		dsp.add(new JScrollPane(dsptab), gc);
		gc.ipady = 0; 
		gc.gridy = 1;
		gc.gridwidth = 1;
		gc.gridx = 0;
		JButton buttonAdd = new JButton("Add");
		buttonAdd.setActionCommand(cmdAdd);
		buttonAdd.addActionListener(this);
		dsp.add(buttonAdd, gc);
		gc.gridx = 1;
		JButton buttonEdit = new JButton("Edit");
		buttonEdit.setActionCommand(cmdEdit);
		buttonEdit.addActionListener(this);
		dsp.add(buttonEdit, gc);
		gc.gridx = 2;
		JButton buttonDelete = new JButton("Delete");
		buttonDelete.setActionCommand(cmdDelete);
		buttonDelete.addActionListener(this);
		dsp.add(buttonDelete, gc);
		generalPanel.add(dsp);
		dsptab.addMouseListener(new MouseAdapter() { 
	      	  public void mouseClicked(MouseEvent e) { 
		      	    if (e.getClickCount() == 2) {
		      	    	int row = dsptab.rowAtPoint(new Point(e.getX(), e.getY()));
		      	    	int clmn = dsptab.columnAtPoint(new Point(e.getX(), e.getY()));
		      	    	AbstractLink lnk = null;
		      	    	if ((clmn == 0) && (row >= 0) && (row < destinations.size()))
		      	    		lnk = destinations.get(row);
		      	    	else if ((clmn == 1) && (row >= 0) && (row < sources.size()))
		      	    		lnk = sources.get(row);
		      	    	else
		      	    		return;
		      	    	Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
						nelist.add(lnk);
		      	    	treePane.actionSelected(nelist, true, false);
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
		if (pairsModified) {
			myMonitor.getPredecessors().clear();
			myMonitor.getSuccessors().clear();
			for (int i = 0; i < destinations.size(); i++)
				myMonitor.addLinkPair(destinations.get(i), sources.get(i));
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
	 * Reaction to OK/Cancel buttons.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmdAdd.equals(cmd)) {
			WindowAdd winAdd = new WindowAdd(null);
			winAdd.setVisible(true);
		}
		if (cmdEdit.equals(cmd)) {
			int idx = dsptab.getSelectedRow();
			if (idx >= 0) {
				WindowEdit winEdit = new WindowEdit(null, idx);
				winEdit.setVisible(true);
			}
		}
		if (cmdDelete.equals(cmd)) {
			int[] selected = dsptab.getSelectedRows();
			if ((selected != null) && (selected.length > 0)) {
				for (int i = 0; i < selected.length; i++) {
					int idx = selected[i] - i;
					if ((idx >= 0) && (idx < dspTM.getRowCount())) {
						destinations.remove(idx);
						sources.remove(idx);
					}
				}
				pairsModified = true;
				dsp.setBorder(BorderFactory.createTitledBorder("*Destination/Source Pairs"));
				dspTM.fireTableStructureChanged();
			}
		}
		if (cmdOK.equals(cmd)) {
			if (idModified || descModified || pairsModified) {
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
	 * Class needed for displaying destination/source pairs in a table.
	 */
	private class dspTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1370119898238692299L;

		public String getColumnName(int col) {
			if (col == 0)
				return "Destination Links";
	        return "Source Links";
	    }
		
		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return Math.min(destinations.size(), sources.size());
		}

		public Object getValueAt(int row, int column) {
			if (column == 0)
				return destinations.get(row);
			return sources.get(row);
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
	
	
	/**
	 * This class implements an editor widow for a Link pair.
	 */
	private abstract class WindowLinkPair extends JDialog {
		private static final long serialVersionUID = 2867597475178277790L;
		
		private JComboBox dstNets = new JComboBox();
		private JComboBox srcNets = new JComboBox();
		private JComboBox dstLinks = new JComboBox();
		private JComboBox srcLinks = new JComboBox();
		
		private final static String comboDstNet = "destNetCombo";
		private final static String comboSrcNet = "srcNetCombo";
		protected int entryIndex = -1;
		
		public WindowLinkPair() { }
		public WindowLinkPair(JFrame parent, String title) {
			super(parent, title);
			setSize(500, 200);
			setLocationRelativeTo(parent);
			setModal(true);
		}
		
		protected JPanel createForm(AbstractLink destination, AbstractLink source) {
			JPanel panel = new JPanel(new BorderLayout());
			// Network combo boxes initialization
			Vector<AbstractNodeComplex> nets = mySystem.getMyNetwork().getNetworks();
			for (int i = 0; i < nets.size(); i++) {
				dstNets.addItem(nets.get(i));
				srcNets.addItem(nets.get(i));
			}
			Box dataPanel = Box.createHorizontalBox();
			// Destinations
			int idxNet = 0;
			if (destination != null) {
				idxNet = nets.indexOf(destination.getMyNetwork());
			}
			if (!nets.isEmpty())
				dstNets.setSelectedIndex(Math.max(0, idxNet));
			dstNets.addActionListener(new ButtonsComboActionListener());
			dstNets.setActionCommand(comboDstNet);
			AbstractNodeComplex net = (AbstractNodeComplex)dstNets.getSelectedItem();
			Vector<AbstractLink> lnks = net.getDestinationLinks();
			fillLinkComboBox(dstLinks, lnks);
			int idxLnk = 0;
			if (destination != null) {
				idxLnk = lnks.indexOf(destination);
			}
			if (!lnks.isEmpty())
				dstLinks.setSelectedIndex(Math.max(0, idxLnk));
			Box dstPanel = Box.createVerticalBox();
			dstPanel.setBorder(BorderFactory.createTitledBorder("Destinations"));
			JPanel pDstNet = new JPanel(new SpringLayout());
			pDstNet.setBorder(BorderFactory.createTitledBorder("Networks"));
			pDstNet.add(dstNets);
			SpringUtilities.makeCompactGrid(pDstNet, 1, 1, 2, 2, 2, 2);
			dstPanel.add(pDstNet);
			JPanel pDstLnk = new JPanel(new SpringLayout());
			pDstLnk.setBorder(BorderFactory.createTitledBorder("Destination Links"));
			pDstLnk.add(dstLinks);
			SpringUtilities.makeCompactGrid(pDstLnk, 1, 1, 2, 2, 2, 2);
			dstPanel.add(pDstLnk);
			dataPanel.add(dstPanel);
			// Sources
			idxNet = 0;
			if (source != null) {
				idxNet = nets.indexOf(source.getMyNetwork());
			}
			if (!nets.isEmpty())
				srcNets.setSelectedIndex(Math.max(0, idxNet));
			srcNets.addActionListener(new ButtonsComboActionListener());
			srcNets.setActionCommand(comboSrcNet);
			net = (AbstractNodeComplex)srcNets.getSelectedItem();
			lnks = net.getSourceLinks();
			fillLinkComboBox(srcLinks, lnks);
			idxLnk = 0;
			if (source != null) {
				idxLnk = lnks.indexOf(source);
			}
			if (!lnks.isEmpty())
				srcLinks.setSelectedIndex(Math.max(0, idxLnk));
			Box srcPanel = Box.createVerticalBox();
			srcPanel.setBorder(BorderFactory.createTitledBorder("Sources"));
			JPanel pSrcNet = new JPanel(new SpringLayout());
			pSrcNet.setBorder(BorderFactory.createTitledBorder("Networks"));
			pSrcNet.add(srcNets);
			SpringUtilities.makeCompactGrid(pSrcNet, 1, 1, 2, 2, 2, 2);
			srcPanel.add(pSrcNet);
			JPanel pSrcLnk = new JPanel(new SpringLayout());
			pSrcLnk.setBorder(BorderFactory.createTitledBorder("Source Links"));
			pSrcLnk.add(srcLinks);
			SpringUtilities.makeCompactGrid(pSrcLnk, 1, 1, 2, 2, 2, 2);
			srcPanel.add(pSrcLnk);
			dataPanel.add(srcPanel);
			// OK/Cancel buttons
			JPanel bp = new JPanel(new FlowLayout());
			JButton bOK = new JButton("    OK    ");
			bOK.setActionCommand(cmdOK);
			bOK.addActionListener(new ButtonsComboActionListener());
			JButton bCancel = new JButton("Cancel");
			bCancel.setActionCommand(cmdCancel);
			bCancel.addActionListener(new ButtonsComboActionListener());
			bp.add(bOK);
			bp.add(bCancel);
			panel.add(dataPanel, BorderLayout.CENTER);
			panel.add(bp, BorderLayout.SOUTH);
			return panel;
		}
		
		private void fillLinkComboBox(JComboBox cb, Vector<AbstractLink> lnks) {
			if ((cb == null) || (lnks == null))
				return;
			cb.removeAllItems();
			for (int i = 0; i < lnks.size(); i++)
				cb.addItem(lnks.get(i));
			return;
		}
		
		/**
		 * Action listener for buttons and combo boxes.
		 */
		private class ButtonsComboActionListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				if (cmdOK.equals(cmd)) {
					AbstractLink newDest = (AbstractLink)dstLinks.getSelectedItem();
					AbstractLink newSrc = (AbstractLink)srcLinks.getSelectedItem();
					if ((newDest != null) && (newSrc != null))
						if ((entryIndex >= 0) && (entryIndex < dspTM.getRowCount())) {
							destinations.set(entryIndex, newDest);
							sources.set(entryIndex, newSrc);
							dspTM.fireTableDataChanged();
						}
						else {
							destinations.add(newDest);
							sources.add(newSrc);
							dspTM.fireTableStructureChanged();
						}
					pairsModified = true;
					setVisible(false);
					dispose();
				}
				if (cmdCancel.equals(cmd)) {
					setVisible(false);
					dispose();
				}
				if (comboDstNet.equals(cmd)) {
					AbstractNodeComplex net = (AbstractNodeComplex)dstNets.getSelectedItem();
					fillLinkComboBox(dstLinks, net.getDestinationLinks());
				}
				if (comboSrcNet.equals(cmd)) {
					AbstractNodeComplex net = (AbstractNodeComplex)srcNets.getSelectedItem();
					fillLinkComboBox(srcLinks, net.getSourceLinks());
				}
				return;
			}
		}
	}
	
	private final class WindowAdd extends WindowLinkPair {
		private static final long serialVersionUID = 9215245098365958882L;
		
		public WindowAdd() { }
		public WindowAdd(JFrame parent) {
			super(parent, "New");
			setContentPane(new JScrollPane(createForm(null, null)));
		}
	}
	
	private final class WindowEdit extends WindowLinkPair {
		private static final long serialVersionUID = 6346774397645516687L;
		
		public WindowEdit() { }
		public WindowEdit(JFrame parent, int idx) {
			super(parent, "Edit");
			entryIndex = idx;
			AbstractLink dst = null;
			AbstractLink src = null;
			if ((idx >= 0) && (idx < dspTM.getRowCount())) {
				dst = destinations.get(idx);
				src = sources.get(idx);
			}
			setContentPane(new JScrollPane(createForm(dst, src)));
		}
	}

}