/**
 * @(#)WindowPath.java
 */

package aurora.hwc.gui;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.time.*;
import org.jfree.data.xy.*;
import org.jfree.data.general.*;
import org.jfree.ui.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.decorators.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.*;
import edu.uci.ics.jung.visualization.transform.*;
import edu.uci.ics.jung.visualization.transform.shape.*;
import aurora.*;
import aurora.Point;
import aurora.util.*;
import aurora.hwc.*;
import aurora.hwc.util.*;


/**
 * Implementation of path internal frame.
 * @author Alex Kurzhanskiy
 * @version $Id: WindowPath.java,v 1.1.2.11.2.8 2009/05/31 19:22:50 akurzhan Exp $
 */
public final class WindowPath extends JInternalFrame implements ActionListener {
	private static final long serialVersionUID = -2799899518843864326L;
	
	private AbstractContainer mySystem;
	private PathHWC myPath;
	private TreePane treePane;
	private JToolBar toolbar;
	
	private Graph g;
	private LayoutHWC layout;
	private PluggableRenderer renderer;
	private VisualizationViewer visViewer;
	private DefaultModalGraphMouse graphMouse;
	private LensSupport magnifyViewSupport;
	private Point[] geoBounds = new Point[2];
	private ToolbarListener toolbarListener = new ToolbarListener();
	
	private boolean showSpeeds = false;
	
	private HashMap<String, JMenuItem> cmd2item = new HashMap<String, JMenuItem>();
	private final static String cmdFileSave = "FileSave";
	private final static String cmdZoomIn = "zoomIn";
	private final static String cmdZoomOut = "zoomOut";
	private final static String cmdLens = "LensOnOff";
	private final static String cmdMouseMode = "mouseMode";
	private final static String cmdColorBySpeed = "colorMode";
	
	private int linkCount;
	
	// simulation tab
	private double[][] flowData;
	private double[][] densityData;
	private double[][] speedData;
	private double minFlow = 0;
	private double maxFlow = 1;
	private double minDensity = 0;
	private double maxDensity = 1;
	private double minSpeed = 0;
	private double maxSpeed = 1;
	private double maxTime = 24;
	private boolean newRun = true;
	private int initStep = 0;
	private int tStep = 0;
	private Box simPanel = Box.createVerticalBox();
	private DefaultXYZDataset flowDS = new DefaultXYZDataset();
	private DefaultXYZDataset densityDS = new DefaultXYZDataset();
	private DefaultXYZDataset speedDS = new DefaultXYZDataset();
	private JFreeChart flowChart;
	private JFreeChart densityChart;
	private JFreeChart speedChart;
	
	// performance tab
	private Box perfPanel = Box.createVerticalBox();
	private TimeSeriesCollection[] perfDataSets = new TimeSeriesCollection[5];
	private JFreeChart perfChart;
	
	
	public WindowPath() { }
	public WindowPath(AbstractContainer ctnr, PathHWC pth, TreePane tpane) {
		super("Path: " + pth.toString(), true, true, true, true);
		mySystem = ctnr;
		myPath = pth;
		treePane = tpane;
		linkCount = myPath.getLinkCount();
		//Dimension dims = treePane.getActionPane().getDesktopPane().getSize();
		//setSize((int)Math.round(0.6*dims.getWidth()), (int)Math.round(0.6*dims.getHeight()));
		setSize(400, 500);
		int n = treePane.getInternalFrameCount();
		setLocation(20*n, 20*n);
		AdapterWindowPath listener = new AdapterWindowPath();
		addInternalFrameListener(listener);
		addComponentListener(listener);
		setJMenuBar(createMenuBar());
		buildGraph();
		renderer = new PluggableRenderer();
		layout = new LayoutHWC(this);
		graphMouse = new DefaultModalGraphMouse();
		graphMouse.setZoomAtMouse(true);
		graphMouse.add(new PopupGraphMousePlugin(treePane));
		visViewer = new VisualizationViewer(layout, renderer);
		visViewer.setToolTipFunction(new ToolTipsHWC());
		visViewer.setPickSupport(new ShapePickSupport());
		visViewer.setGraphMouse(graphMouse);
		visViewer.setBackground(Color.white);
		PickedState ps = visViewer.getPickedState();
		ps.addItemListener(new PickedStateListener(treePane, ps));
		magnifyViewSupport = new ViewLensSupport(visViewer, new MagnifyShapeTransformer(visViewer), new ModalLensGraphMouse(new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)));
		LensSupport hyperbolicViewSupport = new ViewLensSupport(visViewer, new HyperbolicShapeTransformer(visViewer), new ModalLensGraphMouse());
		magnifyViewSupport.getLensTransformer().setEllipse(hyperbolicViewSupport.getLensTransformer().getEllipse());
		renderer.setVertexPaintFunction(new VertexPaintFunctionHWC(ps));
		renderer.setVertexIconFunction(new VertexIconAndShapeFunctionHWC(mySystem, ps));
		renderer.setVertexShapeFunction(new VertexIconAndShapeFunctionHWC(mySystem, ps));
		renderer.setVertexStringer(new VertexStringerHWC(ps));
		renderer.setEdgeShapeFunction(new EdgeShape.Line());
		renderer.setEdgeStrokeFunction(new EdgeStrokeFunctionHWC(ps));
		renderer.setEdgePaintFunction(new GradientEdgePaintFunctionHWC(new PickableEdgePaintFunction(visViewer.getPickedState(), Color.black, Color.cyan), visViewer, visViewer));
		renderer.setEdgeStringer(new EdgeStringerHWC(ps));
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		GraphZoomScrollPane scrollPane = new GraphZoomScrollPane(visViewer);
		JPanel panel = new JPanel(new BorderLayout());
		toolbar = makeToolbar();
		panel.add(toolbar, BorderLayout.PAGE_START);
		panel.add(scrollPane, BorderLayout.CENTER);
		tabbedPane.add("Layout", panel);
		fillPerfPanel();
		tabbedPane.add("Performance", new JScrollPane(perfPanel));
		fillSimPanel();
		tabbedPane.add("Contour Plots", new JScrollPane(simPanel));
		getContentPane().add(tabbedPane);
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
		fos.println("\"Time\", \"Travel Time (min)\", \"Min. Travel Time (min)\", \"VMT\", \"Max. VMT\", \"VHT\", \"Critical VHT\", \"Delay (vh)\", \"Prod. Loss (lmh)\"");
		int i, j;
		String aggrBuf = "";
		String pthHdr = "\"Time\"";
		Vector<AbstractLink> links = myPath.getLinkVector();
		for (i = 0; i < linkCount; i++)
			pthHdr += ", \"" + links.get(i).getId() + "\"";
		pthHdr += "\r\n";
		String flowBuf = "\r\n\"Flow Contour\"\r\n" + pthHdr;
		String denBuf = "\r\n\"Density Contour\"\r\n" + pthHdr;
		String speedBuf = "\r\n\"Speed Contour\"\r\n" + pthHdr;
		int numSteps = tStep-initStep;
		for (i = 0; i < numSteps; i++) {
			double tm = (initStep + i) * mySystem.getMySettings().getDisplayTP();
			aggrBuf += tm;
			aggrBuf += ", " + perfDataSets[0].getSeries(0).getValue(i);
			aggrBuf += ", " + perfDataSets[0].getSeries(1).getValue(i);
			aggrBuf += ", " + perfDataSets[1].getSeries(0).getValue(i);
			aggrBuf += ", " + perfDataSets[1].getSeries(1).getValue(i);
			aggrBuf += ", " + perfDataSets[2].getSeries(0).getValue(i);
			aggrBuf += ", " + perfDataSets[2].getSeries(1).getValue(i);
			aggrBuf += ", " + perfDataSets[3].getSeries(0).getValue(i);
			aggrBuf += ", " + perfDataSets[4].getSeries(0).getValue(i);
			fos.println(aggrBuf);
			aggrBuf = "";
		}
		for (i = 0; i < numSteps; i++) {
			double tm = (initStep + i) * mySystem.getMySettings().getDisplayTP();
			flowBuf += tm;
			for (j = 0; j < linkCount; j++)
				flowBuf += ", " + flowData[2][linkCount*(initStep+i) + j];
			fos.println(flowBuf);
			flowBuf = "";
		}
		for (i = 0; i < numSteps; i++) {
			double tm = (initStep + i) * mySystem.getMySettings().getDisplayTP();
			denBuf += tm;
			for (j = 0; j < linkCount; j++)
				denBuf += ", " + densityData[2][linkCount*(initStep+i) + j];
			fos.println(denBuf);
			denBuf = "";
		}
		for (i = 0; i < numSteps; i++) {
			double tm = (initStep + i) * mySystem.getMySettings().getDisplayTP();
			speedBuf += tm;
			for (j = 0; j < linkCount; j++)
				speedBuf += ", " + speedData[2][linkCount*(initStep+i) + j];
			fos.println(speedBuf);
			speedBuf = "";
		}
		fos.close();
		return;
	}
	
	/**
	 * Updates simulation data.
	 */
	private void updateSimSeries() {
		AuroraIntervalVector[] flows = myPath.getFlows();
		AuroraIntervalVector[] densities = myPath.getDensities();
		AuroraInterval[] speeds = myPath.getSpeeds();
		for (int i = 0; i < linkCount; i++) {
			flowData[2][linkCount*tStep + i] = flows[i].sum().getCenter();
			minFlow = treePane.updateMinFlow(flows[i].sum().getCenter());
			maxFlow = treePane.updateMaxFlow(flows[i].sum().getCenter());
			densityData[2][linkCount*tStep + i] = densities[i].sum().getCenter();
			minDensity = treePane.updateMinDensity(densities[i].sum().getCenter());
			maxDensity = treePane.updateMaxDensity(densities[i].sum().getCenter());
			speedData[2][linkCount*tStep + i] = speeds[i].getCenter();
			minSpeed = treePane.updateMinSpeed(speeds[i].getCenter());
			maxSpeed = treePane.updateMaxSpeed(speeds[i].getCenter());
			if (newRun) {
				/*minFlow = flows[i].sum().getCenter();
				minDensity = densities[i].sum().getCenter();
				minSpeed = speeds[i].getCenter();*/
				newRun = false;
			}
		}
		tStep++;
		XYPlot plot;
		PaintScaleLegend psl;
		LookupPaintScale pScale;
		plot = (XYPlot)flowChart.getPlot();
		pScale = flowPaintScale();
		((XYBlockRenderer)plot.getRenderer()).setPaintScale(pScale);
		psl = (PaintScaleLegend)flowChart.getSubtitle(0);
		psl.getAxis().setRange(minFlow, maxFlow);
		psl.setScale(pScale);
		plot = (XYPlot)densityChart.getPlot();
		pScale = densityPaintScale();
		((XYBlockRenderer)plot.getRenderer()).setPaintScale(pScale);
		psl = (PaintScaleLegend)densityChart.getSubtitle(0);
		psl.getAxis().setRange(minDensity, maxDensity);
		psl.setScale(pScale);
		plot = (XYPlot)speedChart.getPlot();
		pScale = speedPaintScale();
		((XYBlockRenderer)plot.getRenderer()).setPaintScale(pScale);
		psl = (PaintScaleLegend)speedChart.getSubtitle(0);
		psl.getAxis().setRange(minSpeed, maxSpeed);
		psl.setScale(pScale);
		return;
	}
	
	/**
	 * Resets simulation data.
	 */
	/*private void resetSimSeries() {
		initDataMatrices();
		XYPlot plot;
		PaintScaleLegend psl;
		LookupPaintScale pScale;
		plot = (XYPlot)flowChart.getPlot();
		pScale = flowPaintScale();
		((XYBlockRenderer)plot.getRenderer()).setPaintScale(pScale);
		psl = (PaintScaleLegend)flowChart.getSubtitle(0);
		psl.getAxis().setRange(minFlow, maxFlow);
		psl.setScale(pScale);
		plot = (XYPlot)densityChart.getPlot();
		pScale = densityPaintScale();
		((XYBlockRenderer)plot.getRenderer()).setPaintScale(pScale);
		psl = (PaintScaleLegend)densityChart.getSubtitle(0);
		psl.getAxis().setRange(minDensity, maxDensity);
		psl.setScale(pScale);
		plot = (XYPlot)speedChart.getPlot();
		pScale = speedPaintScale();
		((XYBlockRenderer)plot.getRenderer()).setPaintScale(pScale);
		psl = (PaintScaleLegend)speedChart.getSubtitle(0);
		psl.getAxis().setRange(minSpeed, maxSpeed);
		psl.setScale(pScale);
		return;
	}*/
	
	/**
	 * Initializes flow, density and speed data matrices.
	 */
	private void initDataMatrices() {
		tStep = (int)Math.floor(mySystem.getMyNetwork().getSimTime()/mySystem.getMySettings().getDisplayTP());
		initStep = tStep;
		newRun = true;
		treePane.resetDataRanges();
		double tp = mySystem.getMySettings().getDisplayTP();
		Vector<AbstractLink> links = myPath.getLinkVector();
		int xSize = linkCount;
		maxTime = Math.min(mySystem.getMySettings().getTSMax()*mySystem.getMyNetwork().getTP(), mySystem.getMySettings().getTimeMax());
		int ySize = (int)Math.floor(maxTime/tp);
		flowData = new double[3][xSize*ySize];
		densityData = new double[3][xSize*ySize];
		speedData = new double[3][xSize*ySize];
		for (int j = 0; j < ySize; j++) {
			double dd = 0.0;
			for (int i = 0; i < xSize; i++) {
				flowData[0][j*xSize + i] = dd;
				flowData[1][j*xSize + i] = j * tp + tp;
				densityData[0][j*xSize + i] = dd;
				densityData[1][j*xSize + i] = j * tp + tp;
				speedData[0][j*xSize + i] = dd;
				speedData[1][j*xSize + i] = j * tp + tp;
				dd += links.get(i).getLength();
			}
		}
		if (flowDS.getSeriesCount() > 0)
			flowDS.removeSeries(flowDS.getSeriesKey(0));
		if (densityDS.getSeriesCount() > 0)
			densityDS.removeSeries(densityDS.getSeriesKey(0));
		if (speedDS.getSeriesCount() > 0)
			speedDS.removeSeries(speedDS.getSeriesKey(0));
		flowDS.addSeries("Flow Contour", flowData);
		densityDS.addSeries("Density Contour", densityData);
		speedDS.addSeries("Speed Contour", speedData);
		return;
	}
	
	/**
	 * Generates paint scale for the flow contour plot.
	 */
	private LookupPaintScale flowPaintScale() {
		if (minFlow >= maxFlow)
			if (minFlow < 1.0)
				maxFlow = minFlow + 1.0;
			else
				minFlow = maxFlow - 1.0;
		LookupPaintScale pScale = new LookupPaintScale(minFlow, maxFlow, Color.white);
        Color[] clr = UtilGUI.byrColorScale();
        double delta = (maxFlow - minFlow)/(clr.length - 1);
        double value = minFlow;
        pScale.add(value, clr[0]);
        value += Double.MIN_VALUE;
        for (int i = 1; i < clr.length; i++) {
        	pScale.add(value, clr[i]);
        	value += delta;
        }
        return pScale;
	}
	
	/**
	 * Generates paint scale for the density contour plot.
	 */
	private LookupPaintScale densityPaintScale() {
		if (minDensity >= maxDensity)
			if (minDensity < 1.0)
				maxDensity = minDensity + 1.0;
			else
				minDensity = maxDensity - 1.0;
		LookupPaintScale pScale = new LookupPaintScale(minDensity, maxDensity, Color.white);
        Color[] clr = UtilGUI.gyrkColorScale();
        double delta = (maxDensity - minDensity)/(clr.length - 1);
        double value = minDensity;
        pScale.add(value, clr[0]);
        value += Double.MIN_VALUE;
        for (int i = 1; i < clr.length; i++) {
        	pScale.add(value, clr[i]);
        	value += delta;
        }
        return pScale;
	}
	
	/**
	 * Generates paint scale for the speed contour plot.
	 */
	private LookupPaintScale speedPaintScale() {
		if (minSpeed >= maxSpeed)
			if (minSpeed < 1.0)
				maxSpeed = minSpeed + 1.0;
			else
				minSpeed = maxSpeed - 1.0;
		LookupPaintScale pScale = new LookupPaintScale(minSpeed, maxSpeed, Color.white);
        Color[] clr = UtilGUI.krygColorScale();
        double delta = (maxSpeed - minSpeed)/(clr.length - 1);
        double value = minSpeed;
        pScale.add(value, clr[0]);
        value += Double.MIN_VALUE;
        for (int i = 1; i < clr.length; i++) {
        	pScale.add(value, clr[i]);
        	value += delta;
        }
        return pScale;
	}
	
	/**
	 * Generates Simulation tab.
	 */
	private void fillSimPanel() {
		initDataMatrices();
		simPanel.removeAll();
		double bw = myPath.getMaxLinkLength();
		ChartPanel cp;
		XYPlot plot;
		XYBlockRenderer renderer;
		LookupPaintScale paintScale;
		PaintScaleLegend psl;
		NumberAxis mileAxis;
		NumberAxis dateAxis;
		NumberAxis scaleAxis;
		mileAxis = new NumberAxis("Mile");
		mileAxis.setRange(0.0, myPath.getLength());
		mileAxis.setLowerMargin(0.0);
		mileAxis.setUpperMargin(0.0);
		dateAxis = new NumberAxis("Hours");
		dateAxis.setUpperMargin(0.0);
		dateAxis.setRange(0.0, maxTime);
		renderer = new XYBlockRenderer();
        renderer.setBlockWidth(bw);
        renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
        paintScale = flowPaintScale();
        renderer.setPaintScale(paintScale);
        plot = new XYPlot(flowDS, mileAxis, dateAxis, renderer);
        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
        flowChart = new JFreeChart(null, plot);
        flowChart.removeLegend();
        scaleAxis = new NumberAxis("Flow (vphl)");
        scaleAxis.setRange(minFlow, maxFlow);
        psl = new PaintScaleLegend(paintScale, scaleAxis);
        psl.setMargin(new RectangleInsets(3, 10, 3, 10));
        psl.setPosition(RectangleEdge.BOTTOM);
        psl.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        psl.setAxisOffset(5.0);
        psl.setPosition(RectangleEdge.RIGHT);
        psl.setFrame(new BlockBorder(Color.GRAY));
        flowChart.addSubtitle(psl);
        cp = new ChartPanel(flowChart);
		cp.setPreferredSize(new Dimension(200, 80));
		simPanel.add(cp);
		
		mileAxis = new NumberAxis("Mile");
		mileAxis.setRange(0.0, myPath.getLength());
		mileAxis.setLowerMargin(0.0);
		mileAxis.setUpperMargin(0.0);
		dateAxis = new NumberAxis("Hours");
		dateAxis.setUpperMargin(0.0);
		dateAxis.setRange(0.0, maxTime);
		renderer = new XYBlockRenderer();
        renderer.setBlockWidth(bw);
        renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
        paintScale = densityPaintScale();
        renderer.setPaintScale(paintScale);
        plot = new XYPlot(densityDS, mileAxis, dateAxis, renderer);
        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
        densityChart = new JFreeChart(null, plot);
        densityChart.removeLegend();
        scaleAxis = new NumberAxis("Density (vpml)");
        scaleAxis.setRange(minDensity, maxDensity);
        psl = new PaintScaleLegend(paintScale, scaleAxis);
        psl.setMargin(new RectangleInsets(3, 10, 3, 10));
        psl.setPosition(RectangleEdge.BOTTOM);
        psl.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        psl.setAxisOffset(5.0);
        psl.setPosition(RectangleEdge.RIGHT);
        psl.setFrame(new BlockBorder(Color.GRAY));
        densityChart.addSubtitle(psl);
        cp = new ChartPanel(densityChart);
		cp.setPreferredSize(new Dimension(200, 80));
		simPanel.add(cp);
		mileAxis = new NumberAxis("Mile");
		mileAxis.setRange(0.0, myPath.getLength());
		mileAxis.setLowerMargin(0.0);
		mileAxis.setUpperMargin(0.0);
		dateAxis = new NumberAxis("Hours");
		dateAxis.setUpperMargin(0.0);
		dateAxis.setRange(0.0, maxTime);
		renderer = new XYBlockRenderer();
        renderer.setBlockWidth(bw);
        renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
        paintScale = speedPaintScale();
        renderer.setPaintScale(paintScale);
        plot = new XYPlot(speedDS, mileAxis, dateAxis, renderer);
        plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
        speedChart = new JFreeChart(null, plot);
        speedChart.removeLegend();
        scaleAxis = new NumberAxis("Speed (mph)");
        scaleAxis.setRange(minSpeed, maxSpeed);
        psl = new PaintScaleLegend(paintScale, scaleAxis);
        psl.setMargin(new RectangleInsets(3, 10, 3, 10));
        psl.setPosition(RectangleEdge.BOTTOM);
        psl.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        psl.setAxisOffset(5.0);
        psl.setPosition(RectangleEdge.RIGHT);
        psl.setFrame(new BlockBorder(Color.GRAY));
        speedChart.addSubtitle(psl);
        cp = new ChartPanel(speedChart);
		cp.setPreferredSize(new Dimension(200, 80));
		simPanel.add(cp);
		return;
	}
	
	/**
	 * Updates performance data.
	 */
	private void updatePerfSeries() {
		Vector<AbstractLink> links = myPath.getLinkVector();
		AbstractLinkHWC myLink = (AbstractLinkHWC)links.firstElement();
		Second cts = Util.time2second(myLink.getTS()*myLink.getTop().getTP());
		double tp = myLink.getMyNetwork().getTP();
		double g = mySystem.getMySettings().getDisplayTP() / tp;
		double vmt = 0.0;
		double maxvmt = 0.0;
		double vht = 0.0;
		double delay = 0.0;
		double critvht = 0.0;
		double ploss = 0.0;
		for (int i = 0; i < links.size(); i++) {
			myLink = (AbstractLinkHWC)links.get(i);
			vmt += myLink.getSumVMT();
			maxvmt += g * ((Double)myLink.getMaxFlow()) * myLink.getLength() * tp;
			vht += myLink.getSumVHT();
			delay += myLink.getSumDelay();
			critvht += myLink.getSumVHT() - myLink.getSumDelay();
			ploss += myLink.getSumPLoss();
		}
		try {
			perfDataSets[0].getSeries(0).add(cts, 60 * myPath.getTravelTime());
			perfDataSets[0].getSeries(1).add(cts, 60 * myPath.getMinTravelTime());
			perfDataSets[1].getSeries(0).add(cts, vmt);
			perfDataSets[1].getSeries(1).add(cts, maxvmt);
			perfDataSets[2].getSeries(0).add(cts, vht);
			perfDataSets[2].getSeries(1).add(cts, critvht);
			perfDataSets[3].getSeries(0).add(cts, delay);
			perfDataSets[4].getSeries(0).add(cts, ploss);
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
		perfDataSets[2].getSeries(1).clear();
		perfDataSets[3].getSeries(0).clear();
		perfDataSets[4].getSeries(0).clear();
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
		dataset.addSeries(new TimeSeries("Travel Time", Second.class));
		dataset.addSeries(new TimeSeries("Min Travel Tim", Second.class));
		perfDataSets[0] = dataset;
		rangeAxis = new NumberAxis("Tr.Time(min)");
		rangeAxis.setAutoRangeIncludesZero(false);
		subplot = new XYPlot(perfDataSets[0], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.VERY_DARK_YELLOW);
		subplot.getRenderer().setSeriesPaint(1, Color.RED);
		perfPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("VMT", Second.class));
		dataset.addSeries(new TimeSeries("Max VMT", Second.class));
		perfDataSets[1] = dataset;
		rangeAxis = new NumberAxis("VMT");
		rangeAxis.setAutoRangeIncludesZero(false);
		subplot = new XYPlot(perfDataSets[1], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.VERY_DARK_CYAN);
		subplot.getRenderer().setSeriesPaint(1, Color.RED);
		perfPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("VHT", Second.class));
		dataset.addSeries(new TimeSeries("Critical VHT", Second.class));
		perfDataSets[2] = dataset;
		rangeAxis = new NumberAxis("VHT");
		rangeAxis.setAutoRangeIncludesZero(false);
		subplot = new XYPlot(perfDataSets[2], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.VERY_DARK_GREEN);
		subplot.getRenderer().setSeriesPaint(1, Color.RED);
		perfPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("Delay", Second.class));
		perfDataSets[3] = dataset;
		rangeAxis = new NumberAxis("Delay(vh)");
		rangeAxis.setAutoRangeIncludesZero(false);
		subplot = new XYPlot(perfDataSets[3], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.VERY_DARK_RED);
		perfPlot.add(subplot);
		dataset = new TimeSeriesCollection();
		dataset.addSeries(new TimeSeries("Productivity Loss", Second.class));
		perfDataSets[4] = dataset; 
		rangeAxis = new NumberAxis("Prod.Loss(lmh)");
		rangeAxis.setAutoRangeIncludesZero(false);
		subplot = new XYPlot(perfDataSets[4], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.VERY_DARK_BLUE);
		perfPlot.add(subplot);
		ValueAxis axis = perfPlot.getDomainAxis();
		axis.setAutoRange(true);
		perfChart = new JFreeChart(null, null, perfPlot, false);
		ChartPanel cp = new ChartPanel(perfChart);
		cp.setPreferredSize(new Dimension(300, 300));
		perfPanel.add(cp);
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
	 * Action performed when window is resized.
	 */
	private synchronized void resize() {
		//layout.update();
		return;
	}
	
	/**
	 * Updates the graph display.
	 */
	public synchronized void updateView() {
		cmd2item.get(cmdFileSave).setEnabled(true);
		visViewer.repaint();
		if (!mySystem.getMyStatus().isStopped()) {
			updateSimSeries();
			updatePerfSeries();
		}
		return;
	}
	
	/**
	 * Resets the graph display.
	 */
	public synchronized void resetView() {
		cmd2item.get(cmdFileSave).setEnabled(false);
		visViewer.repaint();
		//resetSimSeries();
		fillSimPanel();
		resetPerfSeries();
		return;
	}
	
	/**
	 * Returns graph.
	 */
	public Graph getGraph() {
		return g;
	}
	
	/**
	 * Returns geometric bounds for the graph.
	 */
	public Point[] getGeoBounds() {
		return geoBounds;
	}
	
	/**
	 * Picks minimum of X, Y and Z coordinates
	 */
	private Point makeMinPoint(Point p1, Point p2) {
		Point p = new Point();
		p.x = Math.min(p1.x, p2.x);
		p.y = Math.min(p1.y, p2.y);
		p.z = Math.min(p1.z, p2.z);
		return p;
	}

	/**
	 * Picks maximum of X, Y and Z coordinates
	 */
	private Point makeMaxPoint(Point p1, Point p2) {
		Point p = new Point();
		p.x = Math.max(p1.x, p2.x);
		p.y = Math.max(p1.y, p2.y);
		p.z = Math.max(p1.z, p2.z);
		return p;
	}
	
	/**
	 * Builds the JUNG graph from internal network structure
	 */
	private void buildGraph() {
		int i;
		g = new DirectedSparseGraph();
		HashMap<AbstractNode, VertexNodeHWC> n2v = new HashMap<AbstractNode, VertexNodeHWC>();
		// vertices come first
		Vector<AbstractNode> nodes = myPath.getNodeVector();
		for (i = 0; i < nodes.size(); i++) {
			AbstractNode node = nodes.get(i);
			VertexNodeHWC v = new VertexNodeHWC(node);
			n2v.put(node, v);
			if (i == 0) {
				geoBounds[0] = node.getPosition().get();
				geoBounds[1] = node.getPosition().get();
			}
			else {
				geoBounds[0] = makeMinPoint(geoBounds[0], node.getPosition().get());
				geoBounds[1] = makeMaxPoint(geoBounds[1], node.getPosition().get());
			}
			g.addVertex(v);
		}
		geoBounds[0].x -= 0.010;
		geoBounds[0].y -= 0.010;
		geoBounds[1].x += 0.010;
		geoBounds[1].y += 0.010;
		// then, the edges
		Vector<AbstractLink> links = new Vector<AbstractLink>();
		Vector<AbstractLink> plnks = myPath.getLinkVector();
		for (i = 0; i < plnks.size(); i++) {
			links.add(plnks.get(i));
			/*Vector<AbstractLink> vl;
			int j;
			if (i == 0) {
				vl = plnks.get(i).getInPeers();
				for (j = 0; j < vl.size(); j++)
					links.add(vl.get(j));
				continue;
			}
			if (i == (plnks.size() - 1))
				continue;
			vl = plnks.get(i).getOutPeers();
			for (j = 0; j < vl.size(); j++)
				links.add(vl.get(j));
			vl = plnks.get(i).getInPeers();
			for (j = 0; j < vl.size(); j++)
				links.add(vl.get(j));*/
		}
		for (i = 0; i < links.size(); i++) {
			AbstractLinkHWC link = (AbstractLinkHWC)links.get(i);
			AbstractNode bn = link.getBeginNode();
			VertexNodeHWC bv = null;
			if (bn != null)
				bv = n2v.get(bn);
			if (bv == null) {
				if (bn == null)
					bv = new VertexNodeHWC(link.getPosition().get().get(0));
				else
					bv = new VertexNodeHWC(bn.getPosition().get());
				geoBounds[0] = makeMinPoint(geoBounds[0], bv.getPosition());
				geoBounds[1] = makeMaxPoint(geoBounds[1], bv.getPosition());
				g.addVertex(bv);
			}
			AbstractNode en = link.getEndNode();
			VertexNodeHWC ev = null;
			if (en != null)
				ev = n2v.get(en);
			if (ev == null) {
				if (en == null)
					ev = new VertexNodeHWC(link.getPosition().get().get(1));
				else
					ev = new VertexNodeHWC(en.getPosition().get());
				geoBounds[0] = makeMinPoint(geoBounds[0], ev.getPosition());
				geoBounds[1] = makeMaxPoint(geoBounds[1], ev.getPosition());
				g.addVertex(ev);
			}
			g.addEdge(new EdgeLinkHWC(bv, ev, link));
		}
		return;
	}
	
	/**
	 * Creates toolbar button with specified icon and alternative text.
	 * @param imagePath
	 * @param actionCommand 
	 * @param toolTipText
	 * @param altText
	 * @return button object.
	 */
	private JButton makeToolbarButton(String imagePath, String actionCommand, String toolTipText, String altText) {
		ImageIcon icon = UtilGUI.createImageIcon(imagePath, altText);
		JButton button = new JButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(new ToolbarListener());
		if (icon != null)
			button.setIcon(icon);
		else
			button.setText(altText);
		return button;
	}
	
	/**
	 * Creates toolbar toggle button with specified icon and alternative text.
	 * @param imagePath
	 * @param actionCommand 
	 * @param toolTipText
	 * @param altText
	 * @return button object.
	 */
	private JToggleButton makeToolbarToggleButton(String imagePath, String actionCommand, String toolTipText, String altText) {
		ImageIcon icon = UtilGUI.createImageIcon(imagePath, altText);
		JToggleButton button = new JToggleButton();
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		button.addActionListener(toolbarListener);
		if (icon != null)
			button.setIcon(icon);
		else
			button.setText(altText);
		return button;
	}
	
	private JToolBar makeToolbar() {
		JToolBar tb = new JToolBar();
		tb.add(makeToolbarToggleButton("/icons/finger.jpg", cmdMouseMode, "Picking/Transforming", "PM"));
		//tb.add(makeToolbarToggleButton("/icons/lens.jpg", cmdLens, "Magnifying glass", "MG"));
		tb.add(makeToolbarButton("/icons/zoomin.gif", cmdZoomIn, "Zoom-in", "+"));
		tb.add(makeToolbarButton("/icons/zoomout.gif", cmdZoomOut, "Zoom-out", "-"));
		JCheckBox cb = new JCheckBox("Color by Speed");
		cb.setActionCommand(cmdColorBySpeed);
		cb.setToolTipText("Makes links color determined by speed");
		cb.addActionListener(toolbarListener);
		tb.add(cb);
		return tb;
	}
	
	/**
	 * Computes total in-flow for given Node.
	 */
	private static AuroraIntervalVector totalInFlow(AbstractNode nd) {
		AuroraIntervalVector sum = new AuroraIntervalVector();
		if ((nd.getType() & TypesHWC.MASK_NODE) > 0)
			sum = ((AbstractNodeHWC)nd).totalInput();
		else {
			sum.copy((AuroraIntervalVector)nd.getInputs().get(0));
			for (int i = 1; i < nd.getInputs().size(); i++) {
				AuroraIntervalVector o = (AuroraIntervalVector)nd.getInputs().get(i);
				if (o != null)
					sum.add(o);
			}
		}
		double[] w = ((SimulationSettingsHWC)nd.getMyNetwork().getContainer().getMySettings()).getVehicleWeights();
		sum.inverseAffineTransform(w, 0);
		return sum;
	}
	
	/**
	 * Computes total out-flow for given Node.
	 */
	private static AuroraIntervalVector totalOutFlow(AbstractNode nd) {
		AuroraIntervalVector sum = new AuroraIntervalVector();
		if ((nd.getType() & TypesHWC.MASK_NODE) > 0)
			sum = ((AbstractNodeHWC)nd).totalOutput();
		else {
			sum.copy((AuroraIntervalVector)nd.getOutputs().get(0));
			for (int i = 1; i < nd.getOutputs().size(); i++) {
				AuroraIntervalVector o = (AuroraIntervalVector)nd.getOutputs().get(i);
				if (o != null)
					sum.add(o);
			}
		}
		double[] w = ((SimulationSettingsHWC)nd.getMyNetwork().getContainer().getMySettings()).getVehicleWeights();
		sum.inverseAffineTransform(w, 0);
		return sum;
	}
	
	/**
	 * Makes HTML label for given Node.
	 * @param nd Node object.
	 * @return label.
	 */
	private static String htmlNodeLabel(AbstractNode nd, String clr) {
		if (nd == null)
			return null;
		String txt = "<html><font color=\"" + clr + "\"><b><u>" + nd.toString() + "</u></b>";
		txt += " (" + TypesHWC.typeString(nd.getType()) + ")";
		String desc = nd.getDescription();
		if (desc != null)
			txt += "<p>" + desc;
		txt += "<br><b>In-flow:</b> " + totalInFlow(nd).toString2() + " vph";
		txt += "<br><b>Out-flow:</b> " + totalOutFlow(nd).toString2() + " vph";
		txt += "</font></html>";
		return txt;
	}

	/**
	 * Makes HTML label for given Link.
	 * @param lk Link object.
	 * @param
	 * @return label.
	 */
	private static String htmlLinkLabel(AbstractLinkHWC lk, String clr) {
		NumberFormat form = NumberFormat.getInstance();
		form.setMinimumFractionDigits(2);
		form.setMaximumFractionDigits(2);
		if (lk == null)
			return null;
		String txt = "<html><font color=\"" + clr + "\"><b><u>" + lk.toString() + "</u></b> (" + TypesHWC.typeString(lk.getType()) + ")";
		txt += "<br><b>Length:</b> " + form.format((Double)lk.getLength()) + " mi";
		txt += "<br><b>Width:</b> " + form.format(lk.getLanes()) + " lanes";
		txt += "<br><b>Density:</b> " + lk.getDensity().toString2() + " vpm";
		txt += "<br><b>Speed:</b> " + form.format(((AuroraInterval)lk.getSpeed()).getCenter()) + " mph";
		if (lk.getBeginNode() == null) {
			txt += "<br><b>Demand:</b> " + lk.getDemand().toString2() + " vph";
			txt += "<br><b>Queue:</b> " + lk.getQueue().toString2();
		}
		txt += "</font></html>";
		return txt;
	}

	
	/**
	 * Class needed to display tooltips.
	 */
	private final static class ToolTipsHWC extends DefaultToolTipFunction {
		
		public String getToolTipText(Vertex v) {
			return htmlNodeLabel(((VertexNodeHWC)v).getNodeHWC(), "black");
		}
		
		public String getToolTipText(Edge e) {
			return htmlLinkLabel(((EdgeLinkHWC)e).getLinkHWC(), "black");
		}
	}
	
	/**
	 * Class needed to paint vertices.
	 */
	private final class VertexPaintFunctionHWC implements VertexPaintFunction {
		PickedInfo pi;
		
		public VertexPaintFunctionHWC() { }
		public VertexPaintFunctionHWC(PickedInfo pi) { this.pi = pi; }

		public Paint getDrawPaint(Vertex v) {
			return Color.BLACK;
		}

		public Paint getFillPaint(Vertex v) {
			AbstractNode nd = ((VertexNodeHWC)v).getNodeHWC();
			if (nd == null)
				return Color.WHITE;
			if (pi.isPicked(v))
				return Color.CYAN;
			return Color.BLUE;
		}
		
	}
	
	
	/**
	 * Class that is needed to display vertices as icons.
	 */
	private final static class VertexIconAndShapeFunctionHWC extends AbstractVertexShapeFunction implements VertexIconFunction, VertexShapeFunction, VertexSizeFunction {
		private AbstractContainer mySystem = null;
		PickedInfo pi = null;
		
		public VertexIconAndShapeFunctionHWC() {
			setSizeFunction(this);
		}

		public VertexIconAndShapeFunctionHWC(AbstractContainer x, PickedInfo p) {
			mySystem = x;
			pi = p;
			setSizeFunction(this);
		}
		
		public Icon getIcon(ArchetypeVertex v) {
			if ((mySystem != null) && (!mySystem.getMyStatus().isStopped()))
				return null;
			AbstractNode nd = ((VertexNodeHWC)v).getNodeHWC();
			if (nd == null)
				return null;
			if ((pi != null) && (pi.isPicked(v)))
				return null;
			return UtilGUI.createImageIcon("/icons/" + nd.getClass().getSimpleName() + "_s.gif");
			//return null;
		}

		public Shape getShape(Vertex v) {
			AbstractNode nd = ((VertexNodeHWC)v).getNodeHWC();
			if (nd == null)
				return factory.getEllipse(v);
			switch(nd.getType()) {
			case TypesHWC.NETWORK_HWC: return factory.getRegularStar(v, 5);
			case TypesHWC.NODE_FREEWAY: return factory.getRegularPolygon(v, 3);
			case TypesHWC.NODE_HIGHWAY: return factory.getRegularPolygon(v, 4);
			case TypesHWC.NODE_SIGNAL: return factory.getRegularPolygon(v, 5);
			case TypesHWC.NODE_STOP: return factory.getRegularPolygon(v, 8);
			default:
			}
			return factory.getEllipse(v);
		}

		public int getSize(Vertex v) {
			if ((mySystem != null) && (!mySystem.getMyStatus().isStopped()))
				return 1;
			AbstractNode nd = ((VertexNodeHWC)v).getNodeHWC();
			if (nd == null)
				return 5;
			return DEFAULT_SIZE;
		}
		
	}
	
	
	/**
	 * Class needed to display labels on vertices.
	 */
	private final static class VertexStringerHWC implements VertexStringer {
		private PickedInfo pi;
		
		public VertexStringerHWC() { }
		public VertexStringerHWC(PickedInfo pi) { this.pi = pi; }

		public String getLabel(ArchetypeVertex v) {
			if (pi.isPicked(v))
				return htmlNodeLabel(((VertexNodeHWC)v).getNodeHWC(), "gray");
			return null;
		}	
	}
	
	
	/**
	 * Class needed for defining the thickness of edges.
	 */
	private final static class EdgeStrokeFunctionHWC implements EdgeStrokeFunction {
		private final static float[] dotting = {1.0f, 3.0f};
		private PickedInfo pi;
		
		public EdgeStrokeFunctionHWC() { }
		public EdgeStrokeFunctionHWC(PickedInfo pi) { this.pi = pi; }
		
		public Stroke getStroke(Edge e) {
			AbstractLinkHWC l = ((EdgeLinkHWC)e).getLinkHWC();
			double v;
			if (l == null)
				v = 1.0;
			else
				v = 2*l.getLanes();
			Stroke s;
			if (pi.isPicked(e))
				s = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, dotting, 0f);
			else
				s = new BasicStroke((float)v);
			return s;
		}
		
    }
	
	
	/**
	 * Class needed to paint the edge.
	 */
	private final class GradientEdgePaintFunctionHWC extends GradientEdgePaintFunction {
		protected boolean fill_edge = false;
        
		public GradientEdgePaintFunctionHWC(EdgePaintFunction defaultEdgePaintFunction, HasGraphLayout vv, LayoutTransformer transformer) {
			super(Color.WHITE, Color.BLACK, vv, transformer);
		}
        
		public void useFill(boolean b) {
			fill_edge = b;
		}
        
		public Paint getDrawPaint(Edge e) {
			return super.getDrawPaint(e);
		}
        
		protected Color getColor1(Edge e) {
			return getColor2(e);
		}
		
		protected Color getColor2(Edge e) {
			EdgeLinkHWC el = (EdgeLinkHWC)e;
			Color c;
			if (showSpeeds) {
				double speed = ((AuroraInterval)el.getLinkHWC().getSpeed()).getCenter();
				double vff = (Double)el.getLinkHWC().getV();
				c = UtilGUI.kgColor((int)Math.floor(10*(speed/vff)));
			}
			else {
				double den = ((AuroraIntervalVector)el.getLinkHWC().getDensity()).sum().getCenter();
				double cden = (Double)el.getLinkHWC().getCriticalDensity();
				double jden = (Double)el.getLinkHWC().getJamDensity() - cden;
				if (den < cden)
					c = UtilGUI.gyColor((int)Math.floor(10*(den/cden)));
				else {
					den = den - cden;
					cden = 0.3*jden;
					if (den < cden)
						c = UtilGUI.yrColor((int)Math.floor(10*(den/cden)));
					else
						c = UtilGUI.rkColor((int)Math.floor(10*((den-cden)/(jden-cden))));
				}
			}
			return c;
		}
        
		public Paint getFillPaint(Edge e) {
			return null;
		}
	}
	
	
	/**
	 * Class needed to display labels on vertices.
	 */
	private final static class EdgeStringerHWC implements EdgeStringer {
		private PickedInfo pi;
		
		public EdgeStringerHWC() { }
		public EdgeStringerHWC(PickedInfo pi) { this.pi = pi; }

		public String getLabel(ArchetypeEdge e) {
			if (pi.isPicked(e))
				return htmlLinkLabel(((EdgeLinkHWC)e).getLinkHWC(), "gray");
			return null;
		}	
	}	
	
	
	/**
	 * Listener for picking graph elements.
	 */
	private class PickedStateListener implements ItemListener {
		private TreePane tree;
		private PickedInfo pi;
		
		public PickedStateListener() { }
		public PickedStateListener(TreePane tree, PickedInfo pi) {
			this.tree = tree;
			this.pi = pi;
		}
		
		public void itemStateChanged(ItemEvent e) {
			if (e.getItem() instanceof VertexNodeHWC) {
				VertexNodeHWC v = (VertexNodeHWC)e.getItem();
				AbstractNode nd = v.getNodeHWC();
				if ((!pi.isPicked(v)) || (nd == null))
					return;
				tree.actionSelected(nd, false);
			}
			else {
				EdgeLinkHWC el = (EdgeLinkHWC)e.getItem();
				AbstractLinkHWC lk = el.getLinkHWC();
				if ((!pi.isPicked(el)) || (lk == null))
					return;
				tree.actionSelected(lk, false);
			}
			return;
		}
		
	}
	
	
	/**
	 * Listener for toolbar button actions.
	 */
	private class ToolbarListener implements ActionListener {
		ScalingControl crossoverScaler = new CrossoverScalingControl();
		ScalingControl layoutScaler = new LayoutScalingControl();
		ScalingControl scaler = crossoverScaler;
		
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmdZoomIn.equals(cmd)) {
				scaler.scale(visViewer, 1.1f, visViewer.getCenter());
				return;
			}
			if (cmdZoomOut.equals(cmd)) {
				scaler.scale(visViewer, 1/1.1f, visViewer.getCenter());
				return;
			}
			if (cmdLens.equals(cmd)) {
				JToggleButton b = (JToggleButton)e.getSource();
				if (b.isSelected()) {
					b.setSelected(true);
					magnifyViewSupport.activate(true);
					scaler = layoutScaler;
				}
				else {
					b.setSelected(false);
					magnifyViewSupport.deactivate();
					scaler = crossoverScaler;
				}
				return;
			}
			if (cmdMouseMode.equals(cmd)) {
				JToggleButton b = (JToggleButton)e.getSource();
				if (b.isSelected()) {
					b.setSelected(true);
					graphMouse.setMode(Mode.PICKING);
				}
				else {
					b.setSelected(false);
					graphMouse.setMode(Mode.TRANSFORMING);
				}
				return;
			}
			if (cmdColorBySpeed.equals(cmd)) {
				JCheckBox cb = (JCheckBox)e.getSource();
				showSpeeds = cb.isSelected();
				visViewer.repaint();
				return;
			}
			return;
		}
	}
	
	
	/**
	 * Listener for mouse actions - reacts for right button pressed. 
	 */
	private class PopupGraphMousePlugin extends AbstractPopupGraphMousePlugin implements MouseListener {
		private TreePane tree;
        
		public PopupGraphMousePlugin() {
			this(MouseEvent.BUTTON3_MASK);
		}
		public PopupGraphMousePlugin(int modifiers) {
			super(modifiers);
		}
		public PopupGraphMousePlugin(TreePane tree) {
			this();
			this.tree = tree;
		}

		protected void handlePopup(MouseEvent e) {
			final VisualizationViewer vv = (VisualizationViewer)e.getSource();
			Point2D p = vv.inverseViewTransform(e.getPoint());
			PickSupport pickSupport = vv.getPickSupport();
			if(pickSupport != null) {
				final Vertex v = pickSupport.getVertex(p.getX(), p.getY());
				if(v != null) {
					JPopupMenu popup = new JPopupMenu();
					popup.add(new AbstractAction("Details") {
						private static final long serialVersionUID = 6446100558227596716L;
						public void actionPerformed(ActionEvent e) {
							AbstractNode nd = ((VertexNodeHWC)v).getNodeHWC();
							if (nd == null)
								return;
							tree.actionSelected(nd, true);
						}
					});
					popup.show(vv, e.getX(), e.getY());
				} else {
					final Edge edge = pickSupport.getEdge(p.getX(), p.getY());
					if(edge != null) {
						JPopupMenu popup = new JPopupMenu();
						popup.add(new AbstractAction("Details") {
							private static final long serialVersionUID = 733018466287907239L;
							public void actionPerformed(ActionEvent e) {
								EdgeLinkHWC el = (EdgeLinkHWC)edge;
								AbstractLinkHWC lk = el.getLinkHWC();
								if (lk == null)
									return;
								tree.actionSelected(lk, true);
							}
						});
						popup.show(vv, e.getX(), e.getY());
					}
				}
			}
		}
	}
	
	
	/**
	 * Class needed for proper closing of internal path windows.
	 */
	private class AdapterWindowPath extends InternalFrameAdapter implements ComponentListener {
		
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
			resize();
			return;
		}

		public void componentShown(ComponentEvent e) {
			// TODO Auto-generated method stub
			return;
		}
		
	}
	
}