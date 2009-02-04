/**
 * @(#)WindowNode.java
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
import aurora.hwc.control.*;
import aurora.util.*;


/**
 * Implementation of Node Editor.
 * @author Alex Kurzhanskiy
 * @version $Id: WindowNode.java,v 1.1.4.1.2.2 2009/01/14 18:52:34 akurzhan Exp $
 */
public final class WindowNode extends JInternalFrame implements ActionListener, ChangeListener, DocumentListener {
	private static final long serialVersionUID = -2721130730581469904L;

	private AbstractContainer mySystem = null;
	private Vector<AbstractNetworkElement> nodeList;
	private TreePane treePane;
	
	AbstractNodeHWC myNode;
	
	private int nIn = 0;
	private int nOut = 0;
	private AuroraIntervalVector[][] srm = null;
	private ctrlRWTableModel ctrlTM = new ctrlRWTableModel();
	private srmRWTableModel srmTM = new srmRWTableModel();
	private JComboBox typeList = new JComboBox();
	private JSpinner idSpinner;
	private JTextField nameTxt = new JTextField();
	private JTextPane descTxt = new JTextPane();
	private JTextPane srProfile = new JTextPane();
	private JPanel pTypes = new JPanel(new SpringLayout());
	private JPanel pID = new JPanel(new SpringLayout());
	private JPanel pName = new JPanel(new SpringLayout());
	private JPanel pDesc = new JPanel(new BorderLayout());
	private JPanel pCtrl = new JPanel(new BorderLayout());
	private JPanel pSRM = new JPanel(new BorderLayout());
	private JPanel pT = new JPanel(new FlowLayout());
	private JPanel pSRP = new JPanel(new BorderLayout());
	private JSpinner hh;
	private JSpinner mm;
	private JSpinner ss;
	private final static String nmTypeList = "TypeList";
	private final static String nmID = "ID";
	private final static String nmTP = "TimePeriod";
	private boolean typeModified = false;
	private boolean idModified = false;
	private boolean nameModified = false;
	private boolean descModified = false;
	private boolean ctrlModified = false;
	private boolean srmModified = false;
	private boolean tpModified = false;
	private boolean srpModified = false;
	
	private int[] nodeTypes;
	private AbstractControllerHWC[] controllers;
	private String[] ctrlTypes;
	private String[] ctrlClasses;
	private boolean[] controllersModified;
	
	private final static String cmdOK = "pressedOK";
	private final static String cmdCancel = "pressedCancel";
	
	
	public WindowNode() { }
	public WindowNode(AbstractContainer ctnr, Vector<AbstractNetworkElement> nelist, TreePane tpane) {
		super("Node Editor", true, true, true, true);
		mySystem = ctnr;
		nodeList = nelist;
		treePane = tpane;
		myNode = (AbstractNodeHWC)nodeList.firstElement();
		nIn = myNode.getPredecessors().size();
		nOut = myNode.getSuccessors().size();
		controllers = new AbstractControllerHWC[nIn];
		controllersModified = new boolean[nIn];
		ctrlTypes = myNode.getSimpleControllerTypes();
		ctrlClasses = myNode.getSimpleControllerClasses();
		Vector<AbstractControllerSimple> ctrls = myNode.getControllers();
		for (int i = 0; i < nIn; i++) {
			controllers[i] = (AbstractControllerHWC)ctrls.get(i);
			controllersModified[i] = false;
		}
		setSize(400, 450);
		int n = treePane.getInternalFrameCount();
		setLocation(20*n, 20*n);
		AdapterWindowNode listener = new AdapterWindowNode();
		addInternalFrameListener(listener);
		addComponentListener(listener);
		JPanel panelMain = new JPanel(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.add("Nodes", fillTabNodes());
		tabbedPane.add("General", fillTabGeneral());
		tabbedPane.add("In / Out", fillTabInOut());
		tabbedPane.add("Split Ratios", fillTabSRProfile());
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
	 * Creates nodes tab.
	 */
	private JPanel fillTabNodes() {
		JPanel panel = new JPanel(new BorderLayout());
		Box nodePanel = Box.createVerticalBox();
		// Node list
		JPanel pNd = new JPanel(new BorderLayout());
		pNd.setBorder(BorderFactory.createTitledBorder("Edited Nodes"));
		String txt = "";
		for (int i = 0; i < nodeList.size(); i++)
			txt += (i+1) + ") " + nodeList.get(i) + "\n";
		JTextPane ndTxt = new JTextPane();
		JScrollPane scrlPn = new JScrollPane(ndTxt);
		ndTxt.setText(txt);
		ndTxt.setEditable(false);
		pNd.add(scrlPn, BorderLayout.CENTER);
		nodePanel.add(pNd);
		// Node types
		nodeTypes = TypesHWC.nodeSimpleTypeArray();
		int sidx = 0;
		for (int i = 0; i < nodeTypes.length; i++) {
			typeList.addItem(TypesHWC.typeString(nodeTypes[i]));
			if (nodeTypes[i] == myNode.getType())
				sidx = i;
		}
		typeList.setSelectedIndex(sidx);
		typeList.setActionCommand(nmTypeList);
		typeList.addActionListener(this);
		pTypes.setBorder(BorderFactory.createTitledBorder("Node Type"));
		pTypes.add(typeList);
		SpringUtilities.makeCompactGrid(pTypes, 1, 1, 2, 2, 2, 2);
		nodePanel.add(pTypes);
		panel.add(nodePanel);
		return panel;
	}
	
	/**
	 * Creates general parameters tab.
	 */
	private JPanel fillTabGeneral() {
		JPanel panel = new JPanel(new BorderLayout());
		Box genPanel = Box.createVerticalBox();
		// ID if there is a single node
		if (nodeList.size() == 1) {
			pID.setBorder(BorderFactory.createTitledBorder("ID"));
			idSpinner = new JSpinner(new SpinnerNumberModel(myNode.getId(), -999999999, 999999999, 1));
			idSpinner.setEditor(new JSpinner.NumberEditor(idSpinner, "#######0"));
			idSpinner.setName(nmID);
			idSpinner.addChangeListener(this);
			pID.add(idSpinner);
			SpringUtilities.makeCompactGrid(pID, 1, 1, 2, 2, 2, 2);
			genPanel.add(pID);
		}
		// Name
		pName.setBorder(BorderFactory.createTitledBorder("Name"));
		nameTxt.setText(myNode.getName());
		nameTxt.getDocument().addDocumentListener(this);
		pName.add(nameTxt);
		SpringUtilities.makeCompactGrid(pName, 1, 1, 2, 2, 2, 2);
		genPanel.add(pName);
		// Description
		pDesc.setBorder(BorderFactory.createTitledBorder("Description"));
		descTxt.setText(myNode.getDescription());
		descTxt.getStyledDocument().addDocumentListener(this);
		pDesc.add(new JScrollPane(descTxt));
		genPanel.add(pDesc);
		panel.add(genPanel);
		return panel;
	}
	
	/**
	 * Creates in / out tab.
	 */
	private JPanel fillTabInOut() {
		JPanel panel = new JPanel(new BorderLayout());
		Box ioPanel = Box.createVerticalBox();
		// Control
		pCtrl.setBorder(BorderFactory.createTitledBorder("Input Controllers"));
		final JTable ctrltab = new JTable(ctrlTM);
		ctrltab.addMouseListener(new MouseAdapter() { 
	      	  public void mouseClicked(MouseEvent e) { 
	      	    if (e.getClickCount() == 2) {
	      	    	int row = ctrltab.rowAtPoint(new Point(e.getX(), e.getY()));
	      	    	if (controllers[row] == null)
	      	    		return;
	      	    	try {
	    	    		Class c = Class.forName("aurora.hwc.control.Panel" + controllers[row].getClass().getSimpleName());
	    	    		AbstractControllerPanel cp = (AbstractControllerPanel)c.newInstance();
	    	    		cp.initialize((AbstractControllerHWC)controllers[row], myNode);
	    	    		controllersModified[row] = true;
	    	    	}
	    	    	catch(Exception xpt) { }
	      	    }
	      	    return;
	      	  }
	        });
		setUpControllerColumn(ctrltab, ctrltab.getColumnModel().getColumn(1));
		ctrltab.setPreferredScrollableViewportSize(new Dimension(300, 150));
		pCtrl.add(new JScrollPane(ctrltab));
		ioPanel.add(pCtrl);
		// Split Ratio Matrix
		pSRM.setBorder(BorderFactory.createTitledBorder("Split Ratio Matrix"));
		srm = myNode.getSplitRatioMatrix();
		final JTable srmtab = new JTable(srmTM);
		srmtab.setPreferredScrollableViewportSize(new Dimension(300, 150));
		srmtab.addMouseListener(new MouseAdapter() { 
	      	  public void mouseClicked(MouseEvent e) {
	      		    if (nodeList.size() > 1)
	      		    	return;
		      	    if (e.getClickCount() == 2) {
		      	    	int row = srmtab.rowAtPoint(new Point(e.getX(), e.getY()));
		      	    	int clmn = srmtab.columnAtPoint(new Point(e.getX(), e.getY()));
		      	    	AbstractLinkHWC lnk = null;
		      	    	if ((row > 0) && (clmn == 0))
		      	    		lnk = (AbstractLinkHWC)myNode.getPredecessors().get(row-1);
		      	    	else if ((clmn > 0) && (row == 0))
		      	    		lnk = (AbstractLinkHWC)myNode.getSuccessors().get(clmn-1);
		      	    	else
		      	    		return;
		      	    	Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
						nelist.add(lnk);
		      	    	treePane.actionSelected(nelist, true, false);
		      	    }
		      	    return;
		      	  }
		        });
		pSRM.add(new JScrollPane(srmtab));
		ioPanel.add(pSRM);
		panel.add(ioPanel);
		return panel;
	}
	
	/**
	 * Creates split ratio profile tab.
	 */
	private JPanel fillTabSRProfile() {
		AbstractNodeHWC nd = (AbstractNodeHWC)nodeList.firstElement();
		JPanel panel = new JPanel(new BorderLayout());
		Box srpPanel = Box.createVerticalBox();
		// Sampling Period
		pT.setBorder(BorderFactory.createTitledBorder("Sampling Period"));
		hh = new JSpinner(new SpinnerNumberModel(Util.getHours(nd.getSplitRatioTP()), 0, 99, 1));
		hh.setEditor(new JSpinner.NumberEditor(hh, "00"));
		hh.setName(nmTP);
		hh.addChangeListener(this);
		pT.add(hh);
		pT.add(new JLabel("h "));
		mm = new JSpinner(new SpinnerNumberModel(Util.getMinutes(nd.getSplitRatioTP()), 0, 59, 1));
		mm.setEditor(new JSpinner.NumberEditor(mm, "00"));
		mm.setName(nmTP);
		mm.addChangeListener(this);
		pT.add(mm);
		pT.add(new JLabel("m "));
		ss = new JSpinner(new SpinnerNumberModel(Util.getSeconds(nd.getSplitRatioTP()), 0, 59.99, 1));
		ss.setEditor(new JSpinner.NumberEditor(ss, "00.##"));
		ss.setName(nmTP);
		ss.addChangeListener(this);
		pT.add(ss);
		pT.add(new JLabel("s"));
		srpPanel.add((pT));
		// Split Ratio Profile
		pSRP.setBorder(BorderFactory.createTitledBorder("Split Ratio Profile"));
		srProfile.setText(nd.getSplitRatioProfileAsText());
		srProfile.getStyledDocument().addDocumentListener(this);
		pSRP.add(new JScrollPane(srProfile));
		srpPanel.add(pSRP);
		panel.add(srpPanel);
		return panel;
	}
	
	/**
	 * Establishes combo box editor for controller column.
	 * @param table 
	 * @param ctrlColumn
	 */
	private void setUpControllerColumn(JTable table, TableColumn ctrlColumn) {
		JComboBox combo = new JComboBox();
		combo.addItem("None");
		for (int i = 0; i < ctrlTypes.length; i++)
			combo.addItem(ctrlTypes[i]);
		ctrlColumn.setCellEditor(new DefaultCellEditor(combo));
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		ctrlColumn.setCellRenderer(renderer);
		return;
	}
	
	/**
	 * Action performed before closing the frame.
	 */
	private void close() {
		treePane.removeFrame(this, nodeList);
		return;
	}
	
	/**
	 * Performs node provisioning.
	 */
	private void provisionNodeData() {
		for (int i = 0; i < nodeList.size(); i++) {
			AbstractNodeHWC nd = (AbstractNodeHWC)nodeList.get(i);
			if (idModified)
				nd.setId((Integer)idSpinner.getValue());
			if (nameModified)
				nd.setName(null);
			if (descModified)
				nd.setDescription(descTxt.getText());
			if (ctrlModified) {
				if (nd.getPredecessors().size() != nIn)
					continue;
				for (int j = 0; j < nIn; j++)
					if (controllersModified[j]) {
						if (controllers[j] != null)
							nd.setController((AbstractControllerSimple)controllers[j].deepCopy(), j);
						else
							nd.setController(null, j);
					}
			}
			if (srmModified) {
				if ((nd.getPredecessors().size() != nIn) || (nd.getSuccessors().size() != nOut))
					continue;
				nd.setSplitRatioMatrix(srm);
			}
			if (typeModified) {
				AbstractNodeHWC newnd = null;
				try {
 					Class c = Class.forName(TypesHWC.typeClassName(nodeTypes[typeList.getSelectedIndex()]));
 					newnd = (AbstractNodeHWC)c.newInstance();
				}
				catch(Exception e) {
					JOptionPane.showMessageDialog(this, "Cannot create Node of type '" + TypesHWC.typeClassName(nodeTypes[typeList.getSelectedIndex()]) + "'.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				newnd.copyData(nd);
				nd.getMyNetwork().replaceNetworkElement(nd, newnd);
			}
			if (tpModified) {
				int h = (Integer)hh.getValue();
				int m = (Integer)mm.getValue();
				double s = (Double)ss.getValue();
				nd.setSplitRatioTP(h + (m/60.0) + (s/3600.0));
			}
			if (srpModified)
				nd.setSplitRatioProfile(srProfile.getText());
		}
		return;
	}
	
	/**
	 * Reaction to OK/Cancel buttons pressed and changes in Type combo box.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmdOK.equals(cmd)) {
			if (typeModified || idModified || nameModified || descModified || ctrlModified || srmModified || tpModified || srpModified) {
				provisionNodeData();
				mySystem.getMyStatus().setSaved(false);
			}
			if (typeModified || idModified)
				treePane.modifyNodeComponents(nodeList);
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
		if (cmd.equals(nmTypeList)) {
			typeModified = true;
			pTypes.setBorder(BorderFactory.createTitledBorder("*Node Type"));
			return;
		}
		return;
	}
	
	/**
	 * 
	 * Reaction to spinner and text field changes.
	 */
	public void stateChanged(ChangeEvent e) {
		String nm = ((JComponent)e.getSource()).getName();
		if (nm.equals(nmID)) {
			idModified = true;
			pID.setBorder(BorderFactory.createTitledBorder("*ID"));
			return;
		}
		if (nm.equals(nmTP)) {
			tpModified = true;
			pT.setBorder(BorderFactory.createTitledBorder("*Sampling Period"));
			return;
		}
		return;
	}
	
	/**
	 * Reaction to name, description or split ratio profile update.
	 */
	private void descUpdate(DocumentEvent e) {
		if (e.getDocument().equals(descTxt.getDocument())) {
			descModified = true;
			pDesc.setBorder(BorderFactory.createTitledBorder("*Description"));
		}
		if (e.getDocument().equals(nameTxt.getDocument())) {
			nameModified = true;
			pName.setBorder(BorderFactory.createTitledBorder("*Name"));
		}
		if (e.getDocument().equals(srProfile.getDocument())) {
			srpModified = true;
			pSRP.setBorder(BorderFactory.createTitledBorder("*Split Ratio Profile"));
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
	 * Class needed for displaying table of controllers.
	 */
	private class ctrlRWTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 7308368519709561307L;

		public String getColumnName(int col) {
	        String buf = null;
	        switch(col) {
	        case 0:	buf = "In-Link"; break;
	        case 1: buf = "Controller"; break;
	        case 2: buf = "Queue Controller"; break;
	        }
			return buf;
	    }
		
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return nIn;
		}
		
		public boolean isCellEditable(int row, int column) {
			if (column == 1)
				return true;
			return false;
		}

		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row >= nIn) || (column < 0) || (column > 2))
				return null;
			if (column == 0) {
				String prfx = "";
				if (controllersModified[row])
					prfx = "*";
				if (nodeList.size() > 1)
					return (prfx + "Link " + (row + 1));
				else
					return (prfx + TypesHWC.typeString(myNode.getPredecessors().get(row).getType()) + " " + myNode.getPredecessors().get(row).toString());
			}
			AbstractControllerHWC ctrl = controllers[row];
			if (ctrl == null)
				return "None";
			if (column == 1)
				return ctrl.getDescription();
			QueueController qc = ctrl.getQController();
			if (qc == null)
				return "None";
			return qc.getDescription();
		}
		
		public void setValueAt(Object value, int row, int column) {
			if ((row < 0) || (row >= nIn) || (column != 1) || (value == null))
				return;
			int idx = -1;
			for (int i = 0; i < ctrlTypes.length; i++)
				if (ctrlTypes[i].compareTo((String)value) == 0)
					idx = i;
			if (idx < 0)
				controllers[row] = null;
			else
				try {
					Class c = Class.forName(ctrlClasses[idx]);
					controllers[row] = (AbstractControllerHWC)c.newInstance();
				}
				catch(Exception e) {
					JOptionPane.showMessageDialog(null, "Cannot create Controller of type '" + ctrlClasses[idx] + "'.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			fireTableRowsUpdated(row, row);
			ctrlModified = true;
			controllersModified[row] = true;
			pCtrl.setBorder(BorderFactory.createTitledBorder("*Input Controllers"));
			return;
		}
		
	}
	
	
	/**
	 * Class needed for displaying split ratio matrix in a table.
	 */
	private class srmRWTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -1335328934560165846L;

		public String getColumnName(int col) {
	        return " ";
	    }
		
		public int getColumnCount() {
			return (nOut + 1);
		}

		public int getRowCount() {
			return (nIn + 1);
		}
		
		public boolean isCellEditable(int row, int column) {
			if ((row > 0) && (column > 0))
				return true;
			return false;
		}

		public Object getValueAt(int row, int column) {
			if (row == 0) {
				if ((column < 1) || (column > srm[0].length))
					return null;
				if (nodeList.size() > 1)
					return "To Out-Link " + column;
				AbstractNetworkElement ne = myNode.getSuccessors().get(column - 1);
				return "To " + TypesHWC.typeString(ne.getType()) + " " + ne;
			}
			if (column == 0) {
				if ((row < 1) || (row > srm.length))
					return null;
				if (nodeList.size() > 1)
					return "From In-Link " + row;
				AbstractNetworkElement ne = myNode.getPredecessors().get(row - 1);
				return "From " + TypesHWC.typeString(ne.getType()) + " " + ne;
			}
			if ((row < 1) || (row > srm.length) || (column < 1) || (column > srm[0].length))
				return null;
			return srm[row - 1][column - 1].toString();
		}
		
		public void setValueAt(Object value, int row, int column) {
			String buf = (String)value;
			int i = row - 1;
			int j = column - 1;
			if ((i < 0) || (i >= nIn) || (j < 0) || (j >= nOut))
				return;
			srm[i][j].setRawIntervalVectorFromString(buf);
			fireTableRowsUpdated(row, row);
			srmModified = true;
			pSRM.setBorder(BorderFactory.createTitledBorder("*Split Ratio Matrix"));
			return;
		}
		
	}
	
	
	/**
	 * Class needed for proper closing of internal node windows.
	 */
	private class AdapterWindowNode extends InternalFrameAdapter implements ComponentListener {
		
		/**
		 * Function that is called when user closes the window.
		 * @param e internal frame event.
		 */
		public void internalFrameClosing(InternalFrameEvent e) {
			close();
			return;
		}
		
		public void componentHidden(ComponentEvent e) {
			// TODO Auto-generated method stub
			return;
		}

		public void componentMoved(ComponentEvent e) {
			// TODO Auto-generated method stub
			return;
		}

		public void componentResized(ComponentEvent e) {
			// TODO Auto-generated method stub
			return;
		}

		public void componentShown(ComponentEvent e) {
			// TODO Auto-generated method stub
			return;
		}
		
	}
	
}
