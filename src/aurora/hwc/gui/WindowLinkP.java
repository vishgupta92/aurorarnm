/**
 * @(#)WindowLinkP.java
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
 * Implementation of Link detail window for prediction.
 * @author Alex Kurzhanskiy
 * @version $Id: $
 */
public final class WindowLinkP extends JInternalFrame implements ActionListener {
	private static final long serialVersionUID = 5416163509574335009L;
	
	private AbstractContainer mySystem;
	private AbstractLinkHWC myLink;
	private boolean sourceLink = false;
	private TreePane treePane;
	private JTabbedPane tabbedPane = new JTabbedPane();
	
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
	private MyXYSeries cdFD = new MyXYSeries("Capacity Drop");
	private JFreeChart fdChart;
	private JLabel labelCapacity = new JLabel();
	private JLabel labelCriticalD = new JLabel();
	private JLabel labelJamD = new JLabel();
	private JLabel labelCapDrop = new JLabel();
	private JLabel labelVff = new JLabel();
	private JLabel labelWc = new JLabel();
	private JComboBox cbSave = new JComboBox();
	private JComboBox listEvents = new JComboBox();;
	private JButton buttonEvents = new JButton("Generate");
	
	private HashMap<String, JMenuItem> cmd2item = new HashMap<String, JMenuItem>();
	private final static String cmdFileSave = "FileSave";
	private final static String cmdToSave = "ToSave";
	
	
	public WindowLinkP() { }
	public WindowLinkP(AbstractContainer ctnr, AbstractLinkHWC lk, TreePane tp) {
		super("Link " + lk.toString(), true, true, true, true);
		mySystem = ctnr;
		myLink = lk;
		treePane = tp;
		if (lk.getBeginNode() == null)
			sourceLink = true;
		//Dimension dims = treePane.getActionPane().getDesktopPane().getSize();
		//setSize((int)Math.round(0.6*dims.getWidth()), (int)Math.round(0.6*dims.getHeight()));
		setSize(370, 500);
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
			fos.println("\"Time\", \"Best Flow (vph)\", \"Worst Flow (vph)\", \"Capacity Low (vph)\", \"Capacity High (vph)\", \"Queue Low\", \"Queue High\", \"Queue Limit\", \"Best Speed (mph)\", \"Worst Speed (mph)\", \"Max. Speed (mph)\", \"Demand Low (vph)\", \"Demand High (vph)\", \"VHT Low\", \"VHT High\", \"Crit. VHT Low\", \"Crit. VHT High\", \"Best Delay (vh)\", \"Worst Delay (vh)\"");
		else
			fos.println("\"Time\", \"Best Flow (vph)\", \"Worst Flow (vph)\", \"Capacity Low (vph)\", \"Capacity High (vph)\", \"Density Low (vpm)\", \"Density High (vpm)\", \"Crit. Density Low (vpm)\", \"Crit. Density High (vpm)\", \"Best Speed (mph)\", \"Worst Speed (mph)\", \"Max. Speed (mph)\", \"Best Travel Time (min)\", \"Worst Travel Time (min)\", \"Min. Tavel Time (min)\", \"VMT Low\", \"VMT High\", \"Max. VMT Low\", \"Max. VMT High\",\"VHT Low\", \"VHT High\",\"Crit. VHT Low\", \"Crit. VHT High\", \"Best Delay (vh)\", \"Worst Delay (vh)\", \"Best Prod. Loss (lmh)\", \"Worst Prod. Loss (lmh)\"");
		int numSteps = perfDataSets[0].getSeries(0).getItemCount();
		for (int i = 0; i < numSteps; i++) {
			Second cts = (Second)simDataSets[0].getSeries(0).getTimePeriod(i);
			double tm = (double)cts.getMinute().getHour().getHour() + (((double)cts.getMinute().getMinute() + ((double)cts.getSecond() / 60.0)) / 60.0);
			fos.print(tm + ", " + simDataSets[0].getSeries(0).getValue(i)); //time, best flow
			fos.print(", " + simDataSets[0].getSeries(1).getValue(i)); //worst flow
			fos.print(", " + simDataSets[0].getSeries(3).getValue(i));  //capacity low
			fos.print(", " + simDataSets[0].getSeries(2).getValue(i));  //capacity high
			fos.print(", " + simDataSets[1].getSeries(1).getValue(i)); //queue or density low
			fos.print(", " + simDataSets[1].getSeries(0).getValue(i)); //queue or density high
			if (!sourceLink)
				fos.print(", " + simDataSets[1].getSeries(3).getValue(i)); //critical density low
			fos.print(", " + simDataSets[1].getSeries(2).getValue(i)); //queue limit or critical density high
			fos.print(", " + simDataSets[2].getSeries(1).getValue(i)); //best speed
			fos.print(", " + simDataSets[2].getSeries(0).getValue(i)); //worst speed
			fos.print(", " + simDataSets[2].getSeries(2).getValue(i)); //max speed
			fos.print(", " + simDataSets[3].getSeries(1).getValue(i)); //demand low or best travel time
			fos.print(", " + simDataSets[3].getSeries(0).getValue(i)); //demand high or worst travel time
			if (!sourceLink) {
				fos.print(", " + simDataSets[3].getSeries(2).getValue(i)); //minimum travel time
				fos.print(", " + perfDataSets[0].getSeries(1).getValue(i)); //vmt low
				fos.print(", " + perfDataSets[0].getSeries(0).getValue(i)); //vmt high
				fos.print(", " + perfDataSets[0].getSeries(3).getValue(i)); //max vmt low
				fos.print(", " + perfDataSets[0].getSeries(2).getValue(i)); //max vmt high
			}
			fos.print(", " + perfDataSets[1].getSeries(1).getValue(i)); //vht low
			fos.print(", " + perfDataSets[1].getSeries(0).getValue(i)); //vht high
			fos.print(", " + perfDataSets[1].getSeries(3).getValue(i)); //critical vht low
			fos.print(", " + perfDataSets[1].getSeries(2).getValue(i)); //critical vht high
			fos.print(", " + perfDataSets[2].getSeries(1).getValue(i)); //best delay
			fos.print(", " + perfDataSets[2].getSeries(0).getValue(i)); //worst delay
			if (!sourceLink) {
				fos.print(", " + perfDataSets[3].getSeries(1).getValue(i)); //best prod. loss
				fos.println(", " + perfDataSets[3].getSeries(0).getValue(i)); //worst prod. loss
			}
			else {
				fos.println("");
			}
		}
		fos.close();
		return;
	}
	
	/**
	 * Updates simulation data.
	 */
	private void updateSimSeries() {
		Second cts = Util.time2second(myLink.getTS()*myLink.getTop().getTP());
		AuroraIntervalVector density;
		AuroraInterval critden;
		double maxspeed;
		AuroraInterval tt;
		double mintt;
		AuroraIntervalVector flow = myLink.getActualFlow();
		AuroraInterval cap = myLink.getCapacityValue();
		AuroraInterval speed = myLink.getSpeed();
		maxspeed = (Double)myLink.getV();
		if (sourceLink) {
			density = myLink.getQueue();
			critden = new AuroraInterval((Double)myLink.getQueueMax());
			tt = ((AuroraIntervalVector)myLink.getDemand()).sum();
			mintt = cap.getLowerBound();
		}
		else {
			density = myLink.getDensity();
			critden = myLink.getCriticalDensityRange();
			tt = new AuroraInterval();
			tt.setBounds(60*(myLink.getLength()/speed.getUpperBound()), 60*(myLink.getLength()/speed.getLowerBound()));
			mintt = 60*(myLink.getLength()/maxspeed);
		}
		try {
			if (myLink.isOutputUpperBoundFirst()) {
				simDataSets[0].getSeries(0).add(cts, flow.sum().getLowerBound());
				simDataSets[0].getSeries(1).add(cts, flow.sum().getUpperBound());
			}
			else {
				simDataSets[0].getSeries(0).add(cts, flow.sum().getUpperBound());
				simDataSets[0].getSeries(1).add(cts, flow.sum().getLowerBound());
			}
			simDataSets[0].getSeries(2).add(cts, cap.getUpperBound());
			simDataSets[0].getSeries(3).add(cts, cap.getLowerBound());
			simDataSets[1].getSeries(0).add(cts, density.sum().getUpperBound());
			simDataSets[1].getSeries(1).add(cts, density.sum().getLowerBound());
			simDataSets[1].getSeries(2).add(cts, critden.getUpperBound());
			simDataSets[1].getSeries(3).add(cts, critden.getLowerBound());
			simDataSets[2].getSeries(0).add(cts, speed.getLowerBound());
			simDataSets[2].getSeries(1).add(cts, speed.getUpperBound());
			simDataSets[2].getSeries(2).add(cts, maxspeed);
			simDataSets[3].getSeries(0).add(cts, tt.getUpperBound());
			simDataSets[3].getSeries(1).add(cts, tt.getLowerBound());
			simDataSets[3].getSeries(2).add(cts, mintt);
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
		simDataSets[0].getSeries(2).clear();
		simDataSets[0].getSeries(3).clear();
		simDataSets[1].getSeries(0).clear();
		simDataSets[1].getSeries(1).clear();
		simDataSets[1].getSeries(2).clear();
		simDataSets[1].getSeries(3).clear();
		simDataSets[2].getSeries(0).clear();
		simDataSets[2].getSeries(1).clear();
		simDataSets[2].getSeries(2).clear();
		simDataSets[3].getSeries(0).clear();
		simDataSets[3].getSeries(1).clear();
		simDataSets[3].getSeries(2).clear();
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
		dataset.addSeries(new TimeSeries("Flow High", Second.class));
		dataset.addSeries(new TimeSeries("Flow Low", Second.class));
		dataset.addSeries(new TimeSeries("Max Flow High", Second.class));
		dataset.addSeries(new TimeSeries("Max Flow Low", Second.class));
		simDataSets[0] = dataset;
		rangeAxis = new NumberAxis("Flow(vph)");
		rangeAxis.setAutoRangeIncludesZero(false);
		rangeAxis.setAutoRangeMinimumSize(1);
		subplot = new XYPlot(simDataSets[0], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.DARK_RED);
		subplot.getRenderer().setSeriesPaint(1, ChartColor.DARK_GREEN);
		subplot.getRenderer().setSeriesPaint(2, Color.BLUE);
		subplot.getRenderer().setSeriesPaint(3, Color.BLUE);
		simPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("Density High", Second.class));
		dataset.addSeries(new TimeSeries("Density Low", Second.class));
		dataset.addSeries(new TimeSeries("Critical Density High", Second.class));
		dataset.addSeries(new TimeSeries("Critical Density Low", Second.class));
		simDataSets[1] = dataset;
		if (!sourceLink)
			rangeAxis = new NumberAxis("Density(vpm)");
		else
			rangeAxis = new NumberAxis("Queue");
		rangeAxis.setAutoRangeIncludesZero(false);
		rangeAxis.setAutoRangeMinimumSize(1);
		subplot = new XYPlot(simDataSets[1], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.DARK_RED);
		subplot.getRenderer().setSeriesPaint(1, ChartColor.DARK_GREEN);
		subplot.getRenderer().setSeriesPaint(2, Color.BLUE);
		subplot.getRenderer().setSeriesPaint(3, Color.BLUE);
		simPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("Speed Low", Second.class));
		dataset.addSeries(new TimeSeries("Speed High", Second.class));
		dataset.addSeries(new TimeSeries("Max Speed", Second.class));
		simDataSets[2] = dataset;
		rangeAxis = new NumberAxis("Speed(mph)");
		rangeAxis.setAutoRangeIncludesZero(false);
		rangeAxis.setAutoRangeMinimumSize(1);
		subplot = new XYPlot(simDataSets[2], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.DARK_RED);
		subplot.getRenderer().setSeriesPaint(1, ChartColor.DARK_GREEN);
		subplot.getRenderer().setSeriesPaint(2, Color.BLUE);
		simPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("Travel Time High", Second.class));
		dataset.addSeries(new TimeSeries("Travel Time Low", Second.class));
		dataset.addSeries(new TimeSeries("Min Travel Time", Second.class));
		simDataSets[3] = dataset; 
		if (!sourceLink)
			rangeAxis = new NumberAxis("Tr.Time(min)");
		else
			rangeAxis = new NumberAxis("Demand(vph)");
		rangeAxis.setAutoRangeIncludesZero(false);
		rangeAxis.setAutoRangeMinimumSize(1);
		subplot = new XYPlot(simDataSets[3], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.DARK_RED);
		subplot.getRenderer().setSeriesPaint(1, ChartColor.DARK_GREEN);
		subplot.getRenderer().setSeriesPaint(2, Color.BLUE);
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
		AuroraInterval vmt = myLink.getSumVMT();
		AuroraInterval maxvmt = myLink.getMaxFlowRange();
		maxvmt.affineTransform(myLink.getLength(), 0);
		maxvmt.affineTransform(tp, 0);
		maxvmt.affineTransform(g, 0);
		AuroraInterval vht = myLink.getSumVHT();
		AuroraInterval delay = myLink.getSumDelay();
		AuroraInterval critvht = new AuroraInterval();
		critvht.setBounds(vht.getLowerBound()-delay.getLowerBound(), vht.getUpperBound()-delay.getUpperBound());
		AuroraInterval ploss = myLink.getSumPLoss();
		try {
			if (vmt.isInverted()) {
				perfDataSets[0].getSeries(0).add(cts, vmt.getLowerBound());
				perfDataSets[0].getSeries(1).add(cts, vmt.getUpperBound());
			}
			else {
				perfDataSets[0].getSeries(0).add(cts, vmt.getUpperBound());
				perfDataSets[0].getSeries(1).add(cts, vmt.getLowerBound());
			}
			perfDataSets[0].getSeries(2).add(cts, maxvmt.getUpperBound());
			perfDataSets[0].getSeries(3).add(cts, maxvmt.getLowerBound());
			perfDataSets[1].getSeries(0).add(cts, vht.getUpperBound());
			perfDataSets[1].getSeries(1).add(cts, vht.getLowerBound());
			perfDataSets[1].getSeries(2).add(cts, critvht.getUpperBound());
			perfDataSets[1].getSeries(3).add(cts, critvht.getLowerBound());
			perfDataSets[2].getSeries(0).add(cts, delay.getUpperBound());
			perfDataSets[2].getSeries(1).add(cts, delay.getLowerBound());
			perfDataSets[3].getSeries(0).add(cts, ploss.getUpperBound());
			perfDataSets[3].getSeries(1).add(cts, ploss.getLowerBound());
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
		perfDataSets[0].getSeries(2).clear();
		perfDataSets[0].getSeries(3).clear();
		perfDataSets[1].getSeries(0).clear();
		perfDataSets[1].getSeries(1).clear();
		perfDataSets[1].getSeries(2).clear();
		perfDataSets[1].getSeries(3).clear();
		perfDataSets[2].getSeries(0).clear();
		perfDataSets[2].getSeries(1).clear();
		perfDataSets[3].getSeries(0).clear();
		perfDataSets[3].getSeries(1).clear();
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
		dataset.addSeries(new TimeSeries("VMT High", Second.class));
		dataset.addSeries(new TimeSeries("VMT Low", Second.class));
		dataset.addSeries(new TimeSeries("Max VMT High", Second.class));
		dataset.addSeries(new TimeSeries("Max VMT Low", Second.class));
		perfDataSets[0] = dataset;
		rangeAxis = new NumberAxis("VMT");
		rangeAxis.setAutoRangeIncludesZero(false);
		rangeAxis.setAutoRangeMinimumSize(1);
		subplot = new XYPlot(perfDataSets[0], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.DARK_RED);
		subplot.getRenderer().setSeriesPaint(1, ChartColor.DARK_GREEN);
		subplot.getRenderer().setSeriesPaint(2, Color.BLUE);
		subplot.getRenderer().setSeriesPaint(3, Color.BLUE);
		perfPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("VHT High", Second.class));
		dataset.addSeries(new TimeSeries("VHT Low", Second.class));
		dataset.addSeries(new TimeSeries("Critical VHT High", Second.class));
		dataset.addSeries(new TimeSeries("Critical VHT Low", Second.class));
		perfDataSets[1] = dataset;
		rangeAxis = new NumberAxis("VHT");
		rangeAxis.setAutoRangeIncludesZero(false);
		rangeAxis.setAutoRangeMinimumSize(1);
		subplot = new XYPlot(perfDataSets[1], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.DARK_RED);
		subplot.getRenderer().setSeriesPaint(1, ChartColor.DARK_GREEN);
		subplot.getRenderer().setSeriesPaint(2, Color.BLUE);
		subplot.getRenderer().setSeriesPaint(3, Color.BLUE);
		perfPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("Delay High", Second.class));
		dataset.addSeries(new TimeSeries("Delay Low", Second.class));
		perfDataSets[2] = dataset;
		rangeAxis = new NumberAxis("Delay(vh)");
		rangeAxis.setAutoRangeIncludesZero(false);
		rangeAxis.setAutoRangeMinimumSize(0.001);
		subplot = new XYPlot(perfDataSets[2], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.DARK_RED);
		subplot.getRenderer().setSeriesPaint(1, ChartColor.DARK_GREEN);
		perfPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("Productivity Loss High", Second.class));
		dataset.addSeries(new TimeSeries("Productivity Loss Low", Second.class));
		perfDataSets[3] = dataset; 
		rangeAxis = new NumberAxis("Prod.Loss(lmh)");
		rangeAxis.setAutoRangeIncludesZero(false);
		rangeAxis.setAutoRangeMinimumSize(0.001);
		subplot = new XYPlot(perfDataSets[3], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.DARK_RED);
		subplot.getRenderer().setSeriesPaint(1, ChartColor.DARK_GREEN);
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
		double cap = (Double)myLink.getMaxFlow();
		double drop = Math.max(0, Math.min(cap, cap - (Double)myLink.getCapacityDrop()));
		double rhoc = (Double)myLink.getCriticalDensity();
		double rhoj = (Double)myLink.getJamDensity();
		double zr = 0;
		if (ffFD.getItemCount() == 0) {
			ffFD.add(0.0, 0.0);
			ffFD.add(0.0, 0.0);
		}
		if (cFD.getItemCount() == 0) {
			cFD.add(0.0, 0.0);
			cFD.add(0.0, 0.0);
		}
		if (cdFD.getItemCount() == 0) {
			cdFD.add(0.0, 0.0);
			cdFD.add(0.0, 0.0);
		}
		ffFD.setDataItem(1, new XYDataItem(rhoc, cap));
		cFD.setDataItem(0, new XYDataItem(rhoc, cap));
		cFD.setDataItem(1, new XYDataItem(rhoj, zr));
		cdFD.setDataItem(0, new XYDataItem(rhoc, drop));
		cdFD.setDataItem(1, new XYDataItem(rhoj, drop));
		ffFD.fireSeriesChanged();
		cFD.fireSeriesChanged();
		cdFD.fireSeriesChanged();
		return;
	}
	
	/**
	 * Makes labels for Configuration tab.
	 */
	private void makeLabels() {
		NumberFormat form = NumberFormat.getInstance();
		form.setMaximumFractionDigits(2);
		labelLength.setText("<html><font color=\"black\">Length:<font color=\"blue\"> " + form.format(myLink.getLength()) + " miles</font></font></html>");
		labelLanes.setText("<html><font color=\"black\">Width:<font color=\"blue\"> " + form.format(myLink.getLanes()) + " lanes</font></font></html>");
		if (sourceLink) {
			double[] knobs = myLink.getDemandKnobs();
			boolean allequal = true;
			String buf = "";
			double val = knobs[0];
			for (int i = 0; i < knobs.length; i++) {
				if (i > 0) {
					buf += "; ";
					if (knobs[i] != val)
						allequal = false;
				}
				buf += form.format(knobs[i]);
			}
			if (allequal)
				labelDemandK.setText("<html><font color=\"black\">Demand Coefficient:<font color=\"blue\"> " + form.format(val) + "</font></font></html>");
			else
				labelDemandK.setText("<html><font color=\"black\">Demand Coefficients:<font color=\"blue\"> " + buf + "</font></font></html>");
			labelQueue.setText("<html><font color=\"black\">Queue Limit:<font color=\"blue\"> " + form.format((Double)myLink.getQueueMax()) + "</font></font></html>");
		}
		double drop = Math.max(0, Math.min((Double)myLink.getMaxFlow(),(Double)myLink.getCapacityDrop()));
		if (drop > 0)
			labelCapDrop.setText("<html><font color=\"black\">Capacity Drop:<font color=\"blue\"> " + form.format(drop) + " vph</font></font></html>");
		else
			labelCapDrop.setText("");
		labelCapacity.setText("<html><font color=\"black\">Capacity:<font color=\"blue\"> " + form.format((Double)myLink.getMaxFlow()) + " vph</font></font></html>");
		labelCriticalD.setText("<html><font color=\"black\">Critical Density:<font color=\"blue\"> " + form.format((Double)myLink.getCriticalDensity()) + " vpm</font></font></html>");
		labelJamD.setText("<html><font color=\"black\">Jam Density:<font color=\"blue\"> " + form.format((Double)myLink.getJamDensity()) + " vpm</font></font></html>");
		labelVff.setText("<html> <font color=\"black\">V:<font color=\"blue\"> " + form.format((Double)myLink.getV()) + " mph</font></font></html>");
		labelWc.setText("<html> <font color=\"black\">W:<font color=\"blue\"> " + form.format((Double)myLink.getW()) + " mph</font></font></html>");
		
	}
	
	/**
	 * Creates fundamental diagram chart.
	 */
	private JFreeChart makeFDChart() {
		updateFDSeries();
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(ffFD);
		dataset.addSeries(cFD);
		dataset.addSeries(cdFD);
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
		plot.getRenderer().setSeriesPaint(2, Color.BLUE);
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
		fdl2.add(labelCapDrop);
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
		
		JPanel psave = new JPanel(new GridLayout(1, 0));
		psave.setBorder(BorderFactory.createTitledBorder("Record State"));
		cbSave.addItem("No");
		cbSave.addItem("Yes");
		if (myLink.toSave())
			cbSave.setSelectedIndex(1);
		else
			cbSave.setSelectedIndex(0);
		cbSave.setActionCommand(cmdToSave);
		cbSave.addActionListener(new ButtonEventsListener());
		psave.add(cbSave);
		confPanel.add(psave);
		
		JPanel events = new JPanel(new GridLayout(1, 1));
		Box events1 = Box.createHorizontalBox();
		events.setBorder(BorderFactory.createTitledBorder("Events"));
		listEvents.addItem("Fundamental Diagram");
		if (sourceLink) {
			listEvents.addItem("Demand Coefficient(s)");
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
	 * This class is needed to react to "Generate" button pressed or combo box change.
	 */
	private class ButtonEventsListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmdToSave.equals(cmd)) {
				myLink.setSave((cbSave.getSelectedIndex() == 1));
				return;
			}
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
