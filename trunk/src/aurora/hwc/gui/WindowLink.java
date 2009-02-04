/**
 * @(#)WindowLink.java
 */

package aurora.hwc.gui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.*;
import org.jfree.data.general.*;

import aurora.*;
import aurora.util.*;
import aurora.hwc.*;
import aurora.hwc.util.*;


/**
 * Implementation of Link detail window.
 * @author Alex Kurzhanskiy
 * @version $Id: WindowLink.java,v 1.1.2.16.2.5 2008/12/11 20:42:37 akurzhan Exp $
 */
public final class WindowLink extends JInternalFrame implements ActionListener {
	private static final long serialVersionUID = -4659414889018345624L;
	
	private AbstractContainer mySystem;
	private AbstractLinkHWC myLink;
	private boolean sourceLink = false;
	private TreePane treePane;
	private JTabbedPane tabbedPane = new JTabbedPane();
	
	private double initTime = 0.0;
	
	// simulation tab
	private Box simPanel = Box.createVerticalBox();
	private TimeSeriesCollection[] simDataSets = new TimeSeriesCollection[4];
	private JFreeChart simChart;
	
	// performance tab
	private Box perfPanel = Box.createVerticalBox();
	private TimeSeriesCollection[] perfDataSets = new TimeSeriesCollection[4];
	private JFreeChart perfChart;

	// configuration tab
	private Box confPanel = Box.createVerticalBox();
	private JLabel labelLanes = new JLabel();
	private JLabel labelLength = new JLabel();
	private JLabel labelDemandK = new JLabel();
	private JLabel labelQueue = new JLabel();
	private MyXYSeries ffFD = new MyXYSeries("Free Flow");
	private MyXYSeries cFD = new MyXYSeries("Congestion");
	private JFreeChart fdChart;
	private JLabel labelCapacity = new JLabel();
	private JLabel labelCriticalD = new JLabel();
	private JLabel labelJamD = new JLabel();
	private JLabel labelVff = new JLabel();
	private JLabel labelWc = new JLabel();
	private JComboBox listEvents;
	private JButton buttonEvents = new JButton("Generate");
	
	private HashMap<String, JMenuItem> cmd2item = new HashMap<String, JMenuItem>();
	private final static String cmdFileSave = "FileSave";
	
	
	public WindowLink() { }
	public WindowLink(AbstractContainer ctnr, AbstractLinkHWC lk, TreePane tp) {
		super("Link " + lk.toString(), true, true, true, true);
		mySystem = ctnr;
		initTime = mySystem.getMyNetwork().getSimTime();
		myLink = lk;
		treePane = tp;
		if (lk.getBeginNode() == null)
			sourceLink = true;
		//Dimension dims = treePane.getActionPane().getDesktopPane().getSize();
		//setSize((int)Math.round(0.6*dims.getWidth()), (int)Math.round(0.6*dims.getHeight()));
		setSize(350, 450);
		int n = treePane.getInternalFrameCount();
		setLocation(20*n, 20*n);
		AdapterWindowLink listener = new AdapterWindowLink();
		addInternalFrameListener(listener);
		addComponentListener(listener);
		setJMenuBar(createMenuBar());
		fillSimPanel();
		fillPerfPanel();
		fillConfPanel();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.add("Simulation", new JScrollPane(simPanel));
		tabbedPane.add("Performance", new JScrollPane(perfPanel));
		tabbedPane.add("Configuration", new JScrollPane(confPanel));
		setContentPane(tabbedPane);
	}
	
	
	/**
	 * Creates menu bar.
	 * @return menu bar.
	 */
	private JMenuBar createMenuBar() {
		JMenuItem item;
		JMenu submenu;
		JMenuBar menu = new JMenuBar();
		submenu = new JMenu("File");
		item = new JMenuItem("Save...");
        item.addActionListener(this);
		item.setEnabled(false);
		item.setActionCommand(cmdFileSave);
		cmd2item.put(cmdFileSave, item);
		submenu.add(item);
		menu.add(submenu);
		return menu;
	}
	
	/**
	 * Processes menu item actions.
	 * @param e action event.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmdFileSave.equals(cmd)) {
			saveSimulationData();
			return;
		}
		return;
	}

	/**
	 * Saves simulation displayable data in a file. 
	 */
	private void saveSimulationData() {
		PrintStream fos = treePane.openCSV();
		if (fos == null)
			return;
		if (sourceLink)
			fos.println("\"Time\", \"Flow (vph)\", \"Capacity (vph)\", \"Queue\", \"Queue Limit\", \"Speed (mph)\", \"Max. Speed (mph)\", \"Demand (vph)\", \"Capacity (vph)\", \"VMT\", \"Max. VMT\", \"VHT\", \"Crit. VHT\", \"Delay (vh)\", \"Prod. Loss (lmh)\"");
		else
			fos.println("\"Time\", \"Flow (vph)\", \"Capacity (vph)\", \"Density (vpm)\", \"Crit. Density (vpm)\", \"Speed (mph)\", \"Max. Speed (mph)\", \"Travel Time (min)\", \"Min. Tavel Time (min)\", \"VMT\", \"Max. VMT\", \"VHT\", \"Crit. VHT\", \"Delay (vh)\", \"Prod. Loss (lmh)\"");
		int numSteps = perfDataSets[0].getSeries(0).getItemCount();
		for (int i = 0; i < numSteps; i++) {
			double tm = initTime + i*mySystem.getMySettings().getDisplayTP();
			fos.print(tm + ", " + simDataSets[0].getSeries(0).getValue(i)); //time, flow
			fos.print(", " + simDataSets[0].getSeries(1).getValue(i));  //capacity
			fos.print(", " + simDataSets[1].getSeries(0).getValue(i)); //queue or density
			fos.print(", " + simDataSets[1].getSeries(1).getValue(i)); //queue limit or critical density
			fos.print(", " + simDataSets[2].getSeries(0).getValue(i)); //speed
			fos.print(", " + simDataSets[2].getSeries(1).getValue(i)); //max speed
			fos.print(", " + simDataSets[3].getSeries(0).getValue(i)); //travel time
			fos.print(", " + simDataSets[3].getSeries(1).getValue(i)); //minimum travel time
			fos.print(", " + perfDataSets[0].getSeries(0).getValue(i)); //vmt
			fos.print(", " + perfDataSets[0].getSeries(1).getValue(i)); //max vmt
			fos.print(", " + perfDataSets[1].getSeries(0).getValue(i)); //vht
			fos.print(", " + perfDataSets[1].getSeries(1).getValue(i)); //critical vht
			fos.print(", " + perfDataSets[2].getSeries(0).getValue(i)); //delay
			fos.println(", " + perfDataSets[3].getSeries(0).getValue(i)); //prod. loss
		}
		fos.close();
		return;
	}
	
	/**
	 * Updates simulation data.
	 */
	private void updateSimSeries() {
		Second cts = Util.time2second(myLink.getTS()*myLink.getTop().getTP());
		double cap;
		AuroraIntervalVector density;
		double critden;
		AuroraInterval speed;
		double maxspeed;
		double tt;
		double mintt;
		AuroraIntervalVector flow = myLink.getActualFlow();
		cap = (Double)myLink.getMaxFlow();
		speed = myLink.getSpeed();
		maxspeed = (Double)myLink.getV();
		if (sourceLink) {
			density = myLink.getQueue();
			critden = (Double)myLink.getQueueMax();
			tt = ((AuroraIntervalVector)myLink.getDemand()).sum().getCenter();
			mintt = cap;
		}
		else {
			density = myLink.getDensity();
			critden = (Double)myLink.getCriticalDensity();
			tt = 60*(myLink.getLength()/speed.getCenter());
			mintt = 60*(myLink.getLength()/maxspeed);
		}
		try {
			simDataSets[0].getSeries(0).add(cts, flow.sum().getCenter());
			simDataSets[0].getSeries(1).add(cts, cap);
			simDataSets[1].getSeries(0).add(cts, density.sum().getCenter());
			simDataSets[1].getSeries(1).add(cts, critden);
			simDataSets[2].getSeries(0).add(cts, speed.getCenter());
			simDataSets[2].getSeries(1).add(cts, maxspeed);
			simDataSets[3].getSeries(0).add(cts, tt);
			simDataSets[3].getSeries(1).add(cts, mintt);
		}
		catch(SeriesException e) {}
		return;
	}
	
	/**
	 * Resets simulation data.
	 */
	private void resetSimSeries() {
		simDataSets[0].getSeries(0).clear();
		simDataSets[0].getSeries(1).clear();
		simDataSets[1].getSeries(0).clear();
		simDataSets[1].getSeries(1).clear();
		simDataSets[2].getSeries(0).clear();
		simDataSets[2].getSeries(1).clear();
		simDataSets[3].getSeries(0).clear();
		simDataSets[3].getSeries(1).clear();
		return;
	}
	
	/**
	 * Generates Simulation tab.
	 */
	private void fillSimPanel() {
		TimeSeriesCollection dataset;
		NumberAxis rangeAxis;
		XYPlot subplot;
		CombinedDomainXYPlot simPlot = new CombinedDomainXYPlot(new DateAxis("Time"));
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("Flow", Second.class));
		dataset.addSeries(new TimeSeries("Max Flow", Second.class));
		simDataSets[0] = dataset;
		rangeAxis = new NumberAxis("Flow(vph)");
		rangeAxis.setAutoRangeIncludesZero(false);
		subplot = new XYPlot(simDataSets[0], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, Color.BLACK);
		subplot.getRenderer().setSeriesPaint(1, Color.RED);
		simPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("Density", Second.class));
		dataset.addSeries(new TimeSeries("Critical Density", Second.class));
		simDataSets[1] = dataset;
		if (!sourceLink)
			rangeAxis = new NumberAxis("Density(vpm)");
		else
			rangeAxis = new NumberAxis("Queue");
		rangeAxis.setAutoRangeIncludesZero(false);
		subplot = new XYPlot(simDataSets[1], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, Color.BLUE);
		subplot.getRenderer().setSeriesPaint(1, Color.RED);
		simPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("Speed", Second.class));
		dataset.addSeries(new TimeSeries("Max Speed", Second.class));
		simDataSets[2] = dataset;
		rangeAxis = new NumberAxis("Speed(mph)");
		rangeAxis.setAutoRangeIncludesZero(false);
		subplot = new XYPlot(simDataSets[2], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.DARK_GREEN);
		subplot.getRenderer().setSeriesPaint(1, Color.RED);
		simPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("Travel Time", Second.class));
		dataset.addSeries(new TimeSeries("Min Travel Time", Second.class));
		simDataSets[3] = dataset; 
		if (!sourceLink)
			rangeAxis = new NumberAxis("Tr.Time(min)");
		else
			rangeAxis = new NumberAxis("Demand(vph)");
		rangeAxis.setAutoRangeIncludesZero(false);
		subplot = new XYPlot(simDataSets[3], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.VERY_DARK_YELLOW);
		subplot.getRenderer().setSeriesPaint(1, Color.RED);
		simPlot.add(subplot);
		ValueAxis axis = simPlot.getDomainAxis();
		axis.setAutoRange(true);
		simChart = new JFreeChart(null, null, simPlot, false);
		ChartPanel cp = new ChartPanel(simChart);
		cp.setPreferredSize(new Dimension(200, 300));
		simPanel.add(cp);
		return;
	}
	
	/**
	 * Updates performance data.
	 */
	private void updatePerfSeries() {
		Second cts = Util.time2second(myLink.getTS()*myLink.getTop().getTP());
		double tp = myLink.getMyNetwork().getTP();
		double g = mySystem.getMySettings().getDisplayTP() / tp;
		double vmt = myLink.getSumVMT();
		double maxvmt = g * ((Double)myLink.getMaxFlow()) * myLink.getLength() * tp;
		double vht = myLink.getSumVHT();
		double delay = myLink.getSumDelay();
		double critvht = vht - delay;
		double ploss = myLink.getSumPLoss();
		try {
			perfDataSets[0].getSeries(0).add(cts, vmt);
			perfDataSets[0].getSeries(1).add(cts, maxvmt);
			perfDataSets[1].getSeries(0).add(cts, vht);
			perfDataSets[1].getSeries(1).add(cts, critvht);
			perfDataSets[2].getSeries(0).add(cts, delay);
			perfDataSets[3].getSeries(0).add(cts, ploss);
		}
		catch(SeriesException e) {}
		return;
	}
	
	/**
	 * Resets performance data.
	 */
	private void resetPerfSeries() {
		perfDataSets[0].getSeries(0).clear();
		perfDataSets[0].getSeries(1).clear();
		perfDataSets[1].getSeries(0).clear();
		perfDataSets[1].getSeries(1).clear();
		perfDataSets[2].getSeries(0).clear();
		perfDataSets[3].getSeries(0).clear();
		return;
	}
	
	/**
	 * Generates Performance tab.
	 */
	private void fillPerfPanel() {
		TimeSeriesCollection dataset;
		NumberAxis rangeAxis;
		XYPlot subplot;
		CombinedDomainXYPlot perfPlot = new CombinedDomainXYPlot(new DateAxis("Time"));
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("VMT", Second.class));
		dataset.addSeries(new TimeSeries("Max VMT", Second.class));
		perfDataSets[0] = dataset;
		rangeAxis = new NumberAxis("VMT");
		rangeAxis.setAutoRangeIncludesZero(false);
		subplot = new XYPlot(perfDataSets[0], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.VERY_DARK_CYAN);
		subplot.getRenderer().setSeriesPaint(1, Color.RED);
		perfPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("VHT", Second.class));
		dataset.addSeries(new TimeSeries("Critical VHT", Second.class));
		perfDataSets[1] = dataset;
		rangeAxis = new NumberAxis("VHT");
		rangeAxis.setAutoRangeIncludesZero(false);
		subplot = new XYPlot(perfDataSets[1], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.VERY_DARK_GREEN);
		subplot.getRenderer().setSeriesPaint(1, Color.RED);
		perfPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("Delay", Second.class));
		perfDataSets[2] = dataset;
		rangeAxis = new NumberAxis("Delay(vh)");
		rangeAxis.setAutoRangeIncludesZero(false);
		subplot = new XYPlot(perfDataSets[2], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.VERY_DARK_BLUE);
		perfPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("Productivity Loss", Second.class));
		perfDataSets[3] = dataset; 
		rangeAxis = new NumberAxis("Prod.Loss(lmh)");
		rangeAxis.setAutoRangeIncludesZero(false);
		subplot = new XYPlot(perfDataSets[3], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.VERY_DARK_RED);
		perfPlot.add(subplot);
		ValueAxis axis = perfPlot.getDomainAxis();
		axis.setAutoRange(true);
		perfChart = new JFreeChart(null, null, perfPlot, false);
		ChartPanel cp = new ChartPanel(perfChart);
		cp.setPreferredSize(new Dimension(200, 300));
		perfPanel.add(cp);
		return;
	}
	
	/**
	 * Updates fundamental diagram data.
	 */
	private void updateFDSeries() {
		if (ffFD.getItemCount() == 0) {
			ffFD.add(0.0, 0.0);
			ffFD.add(0.0, 0.0);
		}
		if (cFD.getItemCount() == 0) {
			cFD.add(0.0, 0.0);
			cFD.add(0.0, 0.0);
		}
		ffFD.setDataItem(1, new XYDataItem((Double)myLink.getCriticalDensity(), (Double)myLink.getMaxFlow()));
		cFD.setDataItem(0, new XYDataItem((Double)myLink.getCriticalDensity(), (Double)myLink.getMaxFlow()));
		cFD.setDataItem(1, new XYDataItem((Double)myLink.getJamDensity(), new Double(0.0)));
		ffFD.fireSeriesChanged();
		cFD.fireSeriesChanged();
		return;
	}
	
	/**
	 * Makes labels for Configuration tab.
	 */
	private void makeLabels() {
		NumberFormat form = NumberFormat.getInstance();
		form.setMaximumFractionDigits(2);
		labelLength.setText("<html><font color=\"gray\"><u><b>Length:</b></u><font color=\"blue\"> " + form.format(myLink.getLength()) + " miles</font></font></html>");
		labelLanes.setText("<html><font color=\"gray\"><u><b>Width:</b></u><font color=\"blue\"> " + form.format(myLink.getLanes()) + " lanes</font></font></html>");
		if (sourceLink) {
			labelDemandK.setText("<html><font color=\"gray\"><u><b>Demand Coefficient:</b></u><font color=\"blue\"> " + form.format((Double)myLink.getDemandKnob()) + "</font></font></html>");
			labelQueue.setText("<html><font color=\"gray\"><u><b>Queue Limit:</b></u><font color=\"blue\"> " + form.format((Double)myLink.getQueueMax()) + "</font></font></html>");
		}
		labelCapacity.setText("<html><font color=\"gray\"><u><b>Capacity:</b></u><font color=\"blue\"> " + form.format((Double)myLink.getMaxFlow()) + " vph</font></font></html>");
		labelCriticalD.setText("<html><font color=\"gray\"><u><b>Critical Density:</b></u><font color=\"blue\"> " + form.format((Double)myLink.getCriticalDensity()) + " vpm</font></font></html>");
		labelJamD.setText("<html><font color=\"gray\"><u><b>Jam Density:</b></u><font color=\"blue\"> " + form.format((Double)myLink.getJamDensity()) + " vpm</font></font></html>");
		labelVff.setText("<html> <font color=\"gray\"><u><b>V:</b></u><font color=\"blue\"> " + form.format((Double)myLink.getV()) + " mph</font></font></html>");
		labelWc.setText("<html> <font color=\"gray\"><u><b>W:</b></u><font color=\"blue\"> " + form.format((Double)myLink.getW()) + " mph</font></font></html>");
		
	}
	
	/**
	 * Creates fundamental diagram chart.
	 */
	private JFreeChart makeFDChart() {
		updateFDSeries();
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(ffFD);
		dataset.addSeries(cFD);
		JFreeChart chart = ChartFactory.createXYLineChart(
							null, // chart title
							"Density (vpm)", // x axis label
							"Flow (vph)", // y axis label
							dataset, // data
							PlotOrientation.VERTICAL,
							false, // include legend
							false, // tooltips
							false // urls
							);
		XYPlot plot = (XYPlot)chart.getPlot();
		plot.getRenderer().setSeriesPaint(0, Color.GREEN);
		plot.getRenderer().setSeriesPaint(1, Color.RED);
		plot.getRenderer().setStroke(new BasicStroke(2));
		return chart;
	}
	
	/**
	 * Generates Configuration tab.
	 */
	private void fillConfPanel() {
		makeLabels();
		JPanel pNeighbors = new JPanel(new SpringLayout());
		pNeighbors.setBorder(BorderFactory.createTitledBorder(""));
		int cnt = 0;
		if (myLink.getPredecessors().size() > 0) {
			final AbstractNodeHWC nd = (AbstractNodeHWC)myLink.getBeginNode();
			pNeighbors.add(new JLabel("Begin Node: "));
			JLabel l = new JLabel("<html><a href=\"\">" + nd + "</a></html>");
			l.setToolTipText("Open Node '" + nd + "'");
			l.addMouseListener(new MouseAdapter() { 
		      	  public void mouseClicked(MouseEvent e) {
	      	    	treePane.actionSelected(nd, true);
	      	    	return;
		      	  }
		      	  public void mouseEntered(MouseEvent e) {
		    		e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		    		return;
		    	  }
			    });
			pNeighbors.add(l);
			cnt++;
		}
		if (myLink.getSuccessors().size() > 0) {
			final AbstractNodeHWC nd = (AbstractNodeHWC)myLink.getEndNode();
			pNeighbors.add(new JLabel("   End Node: "));
			JLabel l = new JLabel("<html><a href=\"\">" + nd + "</a></html>");
			l.setToolTipText("Open Node '" + nd + "'");
			l.addMouseListener(new MouseAdapter() { 
		      	  public void mouseClicked(MouseEvent e) {
	      	    	treePane.actionSelected(nd, true);
	      	    	return;
		      	  }
		      	  public void mouseEntered(MouseEvent e) {
		    		e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		    		return;
		    	  }
			    });
			pNeighbors.add(l);
			cnt++;
		}
		SpringUtilities.makeCompactGrid(pNeighbors, cnt, 2, 2, 2, 2, 2);
		confPanel.add(pNeighbors);
		
		Box general = Box.createHorizontalBox();
		general.setBorder(BorderFactory.createTitledBorder("General"));
		Box g1 = Box.createVerticalBox();
		g1.add(labelLength);
		g1.add(labelLanes);
		Box g2 = Box.createVerticalBox();
		g2.add(labelDemandK);
		g2.add(labelQueue);
		general.add(g1);
		general.add(g2);
		confPanel.add(general);
		
		Box fd = Box.createVerticalBox();
		fd.setBorder(BorderFactory.createTitledBorder("Fundamental Diagram"));
		Box fdl = Box.createHorizontalBox();
		Box fdl1 = Box.createVerticalBox();
		fdl1.add(labelCapacity);
		fdl1.add(labelCriticalD);
		fdl1.add(labelJamD);
		Box fdl2 = Box.createVerticalBox();
		fdl2.add(new JLabel());
		fdl2.add(labelVff);
		fdl2.add(labelWc);
		fdl.add(fdl1);
		fdl.add(fdl2);
		fdChart = makeFDChart();
		ChartPanel cp = new ChartPanel(fdChart);
		cp.setMinimumDrawWidth(100);
		cp.setMinimumDrawHeight(60);
		cp.setPreferredSize(new Dimension(250, 100));
		fd.add(new JScrollPane(cp));
		fd.add(fdl);
		confPanel.add(fd);
		
		JPanel events = new JPanel(new GridLayout(1, 1));
		Box events1 = Box.createHorizontalBox();
		events.setBorder(BorderFactory.createTitledBorder("Events"));
		listEvents = new JComboBox();
		listEvents.addItem("Fundamental Diagram");
		if (sourceLink) {
			listEvents.addItem("Demand Coefficient");
			listEvents.addItem("Queue Limit");
		}
		events1.add(listEvents);
		buttonEvents.addActionListener(new ButtonEventsListener());
		events1.add(buttonEvents);
		events.add(events1);
		confPanel.add(events);
		return;
	}
	
	/**
	 * Updates displayed data.
	 */
	public void updateView() {
		cmd2item.get(cmdFileSave).setEnabled(true);
		if (mySystem.getMyStatus().isStopped())
			return;
		updateSimSeries();
		updatePerfSeries();
		updateFDSeries();
		makeLabels();
		return;
	}
	
	/**
	 * Resets displayed data.
	 */
	public void resetView() {
		cmd2item.get(cmdFileSave).setEnabled(false);
		initTime = mySystem.getMyNetwork().getSimTime();
		resetSimSeries();
		resetPerfSeries();
		updateFDSeries();
		makeLabels();
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
	 * This class is needed to react to "Generate" button pressed.
	 */
	private class ButtonEventsListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			EventTableModel etm = (EventTableModel)((TableSorter)treePane.getActionPane().getEventsTable().getModel()).getTableModel();
			AbstractEventPanel ep;
			switch(listEvents.getSelectedIndex()) {
			case 1:
				ep = new PanelEventDemand();
				break;
			case 2:
				ep = new PanelEventQueueMax();
				break;
			default:
				ep = new PanelEventFD();
				break;
			}
			ep.initialize(myLink, mySystem.getMyEventManager(), etm);
			return;
		}
		
	}
	
	
	/**
	 * Class needed for proper closing of internal link windows.
	 */
	private class AdapterWindowLink extends InternalFrameAdapter implements ComponentListener {
		
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
