/**
 * @(#)WindowNode.java
 */

package aurora.hwc.gui;

import java.awt.*;
import java.awt.Point;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.time.*;
import org.jfree.data.general.*;
import aurora.*;
import aurora.hwc.*;
import aurora.hwc.control.*;
import aurora.util.*;


/**
 * Implementation of Node detail window.
 * @author Alex Kurzhanskiy
 * @version $Id: WindowNode.java,v 1.1.2.8.2.7.2.5 2009/08/19 20:42:45 akurzhan Exp $
 */
public class WindowNode extends JInternalFrame {
	private static final long serialVersionUID = -2651266581287806400L;
	
	private AbstractContainer mySystem;
	private AbstractNodeHWC myNode;
	private TreePane treePane;
	protected JTabbedPane tabbedPane = new JTabbedPane();
	private int nIn;
	private int nOut;
	private String[] ctrlTypes;
	private String[] ctrlClasses;
	
	// simulation tab
	private Box simPanel = Box.createVerticalBox();
	private TimeSeriesCollection[] simDataSets = new TimeSeriesCollection[2];
	private JFreeChart simChart;
	
	// configuration tab
	protected Box confPanel = Box.createVerticalBox();
	private JComboBox listEvents;
	private JButton buttonEvents = new JButton("Generate");
	private ctrlROTableModel ctrlTM = new ctrlROTableModel();
	private srmROTableModel srmTM = new srmROTableModel();
	
	
	public WindowNode() { }
	public WindowNode(AbstractContainer ctnr, AbstractNodeHWC nd, TreePane tp) {
		super("Node " + nd.toString(), true, true, true, true);
		mySystem = ctnr;
		myNode = nd;
		treePane = tp;
		nIn = nd.getInputs().size();
		nOut = nd.getOutputs().size();
		ctrlTypes = myNode.getSimpleControllerTypes();
		ctrlClasses = myNode.getSimpleControllerClasses();
		//Dimension dims = treePane.getActionPane().getDesktopPane().getSize();
		//setSize((int)Math.round(0.6*dims.getWidth()), (int)Math.round(0.6*dims.getHeight()));
		setSize(300, 400);
		int n = treePane.getInternalFrameCount();
		setLocation(20*n, 20*n);
		AdapterWindowNode listener = new AdapterWindowNode();
		addInternalFrameListener(listener);
		addComponentListener(listener);
		fillSimPanel();
		fillConfPanel();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.add("Simulation", new JScrollPane(simPanel));
		tabbedPane.add("Configuration", new JScrollPane(confPanel));
		setContentPane(tabbedPane);
	}
	
	/**
	 * Updates simulation data.
	 */
	private void updateSimSeries() {
		int i;
		AuroraIntervalVector o = new AuroraIntervalVector();
		Second cts = Util.time2second(myNode.getTS()*myNode.getTop().getTP());
		try {
			simDataSets[0].getSeries(0).add(cts, myNode.totalInput().sum().getCenter());
			for (i = 1; i <= nIn; i++) {
				o.copy((AuroraIntervalVector)myNode.getInputs().get(i - 1));
				if (o != null)
					simDataSets[0].getSeries(i).add(cts, o.sum().getCenter());
			}
			simDataSets[1].getSeries(0).add(cts, myNode.totalOutput().sum().getCenter());
			for (i = 1; i <= nOut; i++) {
				o.copy((AuroraIntervalVector)myNode.getOutputs().get(i - 1));
				if (o != null)
					simDataSets[1].getSeries(i).add(cts, o.sum().getCenter());
			}
		}
		catch(SeriesException e) {}
		return;
	}
	
	/**
	 * Resets simulation data.
	 */
	private void resetSimSeries() {
		int i;
		for (i = 0; i <= nIn; i++)
			simDataSets[0].getSeries(i).clear();
		for (i = 0; i <= nOut; i++)
			simDataSets[1].getSeries(i).clear();
		return;
	}
	
	/**
	 * Generates Simulation tab.
	 */
	private void fillSimPanel() {
		int i;
		TimeSeriesCollection dataset;
		NumberAxis rangeAxis;
		XYPlot subplot;
		CombinedDomainXYPlot simPlot = new CombinedDomainXYPlot(new DateAxis("Time"));
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("Total In-Flow", Second.class));
		for (i = 0; i < nIn; i++) {
			AbstractNetworkElement ne = myNode.getPredecessors().get(i);
			dataset.addSeries(new TimeSeries("From " + TypesHWC.typeString(ne.getType()) + " " + ne.getId(), Second.class));
		}
		simDataSets[0] = dataset;
		rangeAxis = new NumberAxis("In-Flow(vph)");
		rangeAxis.setAutoRangeIncludesZero(false);
		subplot = new XYPlot(simDataSets[0], null, rangeAxis, new StandardXYItemRenderer());
		simPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("Total Out-Flow", Second.class));
		for (i = 0; i < nOut; i++) {
			AbstractNetworkElement ne = myNode.getSuccessors().get(i);
			dataset.addSeries(new TimeSeries("To " + TypesHWC.typeString(ne.getType()) + " " + ne.getId(), Second.class));
		}
		simDataSets[1] = dataset;
		rangeAxis = new NumberAxis("Out-Flow(vph)");
		rangeAxis.setAutoRangeIncludesZero(false);
		subplot = new XYPlot(simDataSets[1], null, rangeAxis, new StandardXYItemRenderer());
		simPlot.add(subplot);
		ValueAxis axis = simPlot.getDomainAxis();
		axis.setAutoRange(true);
		simChart = new JFreeChart(null, simPlot);
		ChartPanel cp = new ChartPanel(simChart);
		cp.setPreferredSize(new Dimension(200, 300));
		simPanel.add(cp);
		return;
	}
	
	/**
	 * Generates Configuration tab.
	 */
	private void fillConfPanel() {
		final Vector<AbstractControllerSimple> controllers = myNode.getSimpleControllers();
		JPanel desc = new JPanel(new GridLayout(1, 0));
		desc.setBorder(BorderFactory.createTitledBorder("Description"));
		desc.add(new JLabel("<html><font color=\"blue\">" + myNode.getDescription() + "</font></html>"));
		confPanel.add(desc);
		
		JPanel ctrl = new JPanel(new GridLayout(1, 0));
		ctrl.setBorder(BorderFactory.createTitledBorder("Input Controllers"));
		final JTable ctrltab = new JTable(ctrlTM);
		ctrltab.setPreferredScrollableViewportSize(new Dimension(250, 70));
		ctrl.add(new JScrollPane(ctrltab));
		confPanel.add(ctrl);
		ctrltab.addMouseListener(new MouseAdapter() { 
	      	  public void mouseClicked(MouseEvent e) {
	      		if (!mySystem.getMyStatus().isStopped())
	      			return;
	      	    if (e.getClickCount() == 2) {
	      	    	int row = ctrltab.rowAtPoint(new Point(e.getX(), e.getY()));
	      	    	if (controllers.get(row) == null)
	      	    		return;
	      	    	try {
	    	    		Class c = Class.forName("aurora.hwc.control.Panel" + controllers.get(row).getClass().getSimpleName());
	    	    		AbstractSimpleControllerPanel cp = (AbstractSimpleControllerPanel)c.newInstance();
	    	    		cp.initialize((AbstractControllerHWC)controllers.get(row), null, -1, myNode);
	    	    	}
	    	    	catch(Exception xpt) { }
	      	    }
	      	    return;
	      	  }
	        });
		setUpControllerColumn(ctrltab, ctrltab.getColumnModel().getColumn(1));
		
		JPanel srm = new JPanel(new GridLayout(1, 0));
		srm.setBorder(BorderFactory.createTitledBorder("Split Ratio Matrix"));
		final JTable srmtab = new JTable(srmTM);
		srmtab.setPreferredScrollableViewportSize(new Dimension(250, 70));
		srm.add(new JScrollPane(srmtab));
		confPanel.add(srm);
		srmtab.addMouseListener(new MouseAdapter() { 
	      	  public void mouseClicked(MouseEvent e) { 
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
		      	    	treePane.actionSelected(lnk, true);
		      	    }
		      	    return;
		      	  }
		        });
		JPanel events = new JPanel(new GridLayout(1, 0));
		Box events1 = Box.createHorizontalBox();
		events.setBorder(BorderFactory.createTitledBorder("Events"));
		listEvents = new JComboBox();
		listEvents.addItem("Controller");
		listEvents.addItem("Split Ratio Matrix");
		events1.add(listEvents);
		buttonEvents.addActionListener(new ButtonEventsListener());
		events1.add(buttonEvents);
		events.add(events1);
		confPanel.add(events);
		return;
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
	 * Updates displayed data.
	 */
	public void updateView() {
		srmTM.fireTableRowsUpdated(1, nIn);
		if (mySystem.getMyStatus().isStopped())
			return;
		updateSimSeries();
		return;
	}
	
	/**
	 * Resets displayed data.
	 */
	public void resetView() {
		resetSimSeries();
		srmTM.fireTableRowsUpdated(1, nIn);
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
	 * Class needed for displaying table of controllers.
	 */
	private class ctrlROTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -8310974555648802182L;

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

		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row >= nIn) || (column < 0) || (column > 2))
				return null;
			if (column == 0)
				return (TypesHWC.typeString(myNode.getPredecessors().get(row).getType()) + " " + myNode.getPredecessors().get(row).toString());
			AbstractControllerHWC ctrl = (AbstractControllerHWC)myNode.getSimpleControllers().get(row);
			if (ctrl == null)
				return "None";
			if (column == 1)
				return ctrl.getDescription();
			QueueController qc = ctrl.getQController();
			if (qc == null)
				return "None";
			return qc.getDescription();
		}
		
		public boolean isCellEditable(int row, int column) {
			if ((mySystem.getMyStatus().isStopped()) && (column == 1) && (row >= 0) && (row < nIn)) {
				if ((myNode.getSimpleControllers().get(row) == null) || (!myNode.getSimpleControllers().get(row).isDependent()))
				return true;
			}
			return false;
		}
		
		public void setValueAt(Object value, int row, int column) {
			if ((row < 0) || (row >= nIn) || (column != 1) || (value == null))
				return;
			int idx = -1;
			for (int i = 0; i < ctrlTypes.length; i++)
				if (ctrlTypes[i].compareTo((String)value) == 0)
					idx = i;
			if (idx < 0)
				myNode.setSimpleController(null, row);
			else
				try {
					Class c = Class.forName(ctrlClasses[idx]);
					myNode.setSimpleController((AbstractControllerHWC)c.newInstance(), row);
				}
				catch(Exception e) {
					JOptionPane.showMessageDialog(null, "Cannot create Controller of type '" + ctrlClasses[idx] + "'.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			fireTableRowsUpdated(row, row);
			return;
		}
		
	}
	
	
	/**
	 * Class needed for displaying split ratio matrix in a table.
	 */
	private class srmROTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 7602085793295670923L;

		public String getColumnName(int col) {
	        return " ";
	    }
		
		public int getColumnCount() {
			return (nOut + 1);
		}

		public int getRowCount() {
			return (nIn + 1);
		}

		public Object getValueAt(int row, int column) {
			AuroraIntervalVector[][] srm = myNode.getSplitRatioMatrix();
			if (row == 0) {
				if ((column < 1) || (column > srm[0].length))
					return null;
				AbstractNetworkElement ne = myNode.getSuccessors().get(column - 1);
				return "To " + TypesHWC.typeString(ne.getType()) + " " + ne;
			}
			if (column == 0) {
				if ((row < 1) || (row > srm.length))
					return null;
				AbstractNetworkElement ne = myNode.getPredecessors().get(row - 1);
				return "From " + TypesHWC.typeString(ne.getType()) + " " + ne;
			}
			if ((row < 1) || (row > srm.length) || (column < 1) || (column > srm[0].length))
				return null;
			return srm[row - 1][column - 1].toString2();
		}
		
		public boolean isCellEditable(int row, int column) {
			if ((!mySystem.getMyStatus().isStopped()) || (row <= 0) || (column <= 0))
				return false;
			return true;
		}
		
		public void setValueAt(Object value, int row, int column) {
			AuroraIntervalVector[][] srm = myNode.getSplitRatioMatrix();
			String buf = (String)value;
			int i = row - 1;
			int j = column - 1;
			if ((i < 0) || (i >= nIn) || (j < 0) || (j >= nOut))
				return;
			srm[i][j].setIntervalVectorFromString(buf);
			if (myNode.setSplitRatioMatrix(srm))
				fireTableRowsUpdated(row, row);
			return;
		}
		
	}
	
	
	/**
	 * This class is needed to react to "Generate" button pressed.
	 */
	private class ButtonEventsListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			EventTableModel etm = (EventTableModel)((TableSorter)treePane.getActionPane().getEventsTable().getModel()).getTableModel();
			AbstractEventPanel ep;
			switch(listEvents.getSelectedIndex()) {
			case 1:
				ep = new PanelEventSRM();
				break;
			default:
				ep = new PanelEventControllerSimple();
			}
			ep.initialize(myNode, mySystem.getMyEventManager(), etm);
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