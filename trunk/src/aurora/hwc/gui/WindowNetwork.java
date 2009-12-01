/**
 * @(#)WindowNetwork.java
 */

package aurora.hwc.gui;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.general.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
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
 * Implementation of network internal frame.
 * @author Alex Kurzhanskiy
 * @version $Id: WindowNetwork.java,v 1.1.2.29.2.10.2.3 2009/10/22 02:58:13 akurzhan Exp $
 */
public final class WindowNetwork extends JInternalFrame implements ActionListener {
	private static final long serialVersionUID = 6582260090722618374L;
	
	private AbstractContainer mySystem;
	private AbstractNodeComplex myNetwork;
	private TreePane treePane;
	private JToolBar toolbar;
	
	//performance tab
	private Box perfPanel = Box.createVerticalBox();
	private TimeSeriesCollection[] perfDataSets = new TimeSeriesCollection[1];
	private JFreeChart perfChart;
	
	// configuration panel
	private Box confPanel = Box.createVerticalBox();
	private JLabel labelTP = new JLabel();
	private JLabel labelControl = new JLabel();
	private JLabel labelQControl = new JLabel();
	private JComboBox listEvents;
	private JButton buttonEvents = new JButton("Generate");
	
	// monitors panel
	private Box monPanel = Box.createVerticalBox();
	
	private Graph g;
	private LayoutHWC layout;
	private PluggableRenderer renderer;
	private VisualizationViewer visViewer;
	private DefaultModalGraphMouse graphMouse;
	private LensSupport magnifyViewSupport;
	private Point[] geoBounds = new Point[2];
	private ToolbarListener toolbarListener = new ToolbarListener();
	
	private boolean showSpeeds = false;
	private double initTime = 0.0;
	
	private HashMap<String, JMenuItem> cmd2item = new HashMap<String, JMenuItem>();
	private final static String cmdFileSave = "FileSave";
	private final static String cmdZoomIn = "zoomIn";
	private final static String cmdZoomOut = "zoomOut";
	private final static String cmdLens = "LensOnOff";
	private final static String cmdMouseMode = "mouseMode";
	private final static String cmdColorBySpeed = "colorMode";
	
	
	public WindowNetwork() { }
	public WindowNetwork(AbstractContainer ctnr, AbstractNodeComplex ne, TreePane tpane) {
		super("Network " + ne.toString(), true, true, true, true);
		mySystem = ctnr;
		initTime = mySystem.getMyNetwork().getSimTime();
		myNetwork = ne;
		treePane = tpane;
		//Dimension dims = treePane.getActionPane().getDesktopPane().getSize();
		//setSize((int)Math.round(0.6*dims.getWidth()), (int)Math.round(0.6*dims.getHeight()));
		setSize(400, 400);
		int n = treePane.getInternalFrameCount();
		setLocation(20*n, 20*n);
		AdapterWindowNetwork listener = new AdapterWindowNetwork();
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
		GraphZoomScrollPane scrollPane = new GraphZoomScrollPane(visViewer);
		JPanel panel = new JPanel(new BorderLayout());
		toolbar = makeToolbar();
		panel.add(toolbar, BorderLayout.PAGE_START);
		panel.add(scrollPane, BorderLayout.CENTER);
		tabbedPane.add("Layout", panel);
		fillPerfPanel();
		tabbedPane.add("Performance", new JScrollPane(perfPanel));
		if (!myNetwork.getMonitors().isEmpty()) {
			fillMonPanel();
			tabbedPane.add("Monitors", new JScrollPane(monPanel));
		}
		fillConfPanel();
		tabbedPane.add("Configuration", new JScrollPane(confPanel));
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
		fos.println("\"Time\", \"Delay (vh)\"");
		int numSteps = perfDataSets[0].getSeries(0).getItemCount();
		for (int i = 0; i < numSteps; i++) {
			double tm = initTime + i*mySystem.getMySettings().getDisplayTP();
			fos.println(tm + ", " + perfDataSets[0].getSeries(0).getValue(i));
		}
		fos.close();
		return;
	}
	
	/**
	 * Updates performance data.
	 */
	private void updatePerfSeries() {
		Second cts = Util.time2second(myNetwork.getSimTime());
		try {
			perfDataSets[0].getSeries(0).add(cts, ((NodeHWCNetwork)myNetwork).getSumDelay());
		}
		catch(SeriesException e) {}
		return;
	}
	
	/**
	 * Resets performance data.
	 */
	private void resetPerfSeries() {
		perfDataSets[0].getSeries(0).clear();
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
		dataset.addSeries(new TimeSeries("Delay", Second.class));
		perfDataSets[0] = dataset;
		rangeAxis = new NumberAxis("Delay(vh)");
		rangeAxis.setAutoRangeIncludesZero(false);
		subplot = new XYPlot(perfDataSets[0], null, rangeAxis, new StandardXYItemRenderer());
		subplot.getRenderer().setSeriesPaint(0, ChartColor.VERY_DARK_RED);
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
	 * Updates labels at the configuration tab
	 */
	private void updateConfLabels() {
		labelTP.setText("<html><font color=\"black\">Sampling Period:<font color=\"blue\"> " + Util.time2string(myNetwork.getTP()) + "</font></font></html>");
		String cvbuf = "Off";
		if (myNetwork.isControlled())
			cvbuf = "On";
		labelControl.setText("<html><font color=\"black\">Mainline Control:<font color=\"blue\"> " + cvbuf + "</font></font></html>");
		String qcvbuf = "Off";
		if (((NodeHWCNetwork)myNetwork).hasQControl())
			qcvbuf = "On";
		labelQControl.setText("<html><font color=\"black\">Queue Control:<font color=\"blue\"> " + qcvbuf + "</font></font></html>");
		return;
	}
	
	/**
	 * Generates Configuration tab.
	 */
	private void fillConfPanel() {
		updateConfLabels();
		JPanel desc = new JPanel(new GridLayout(1, 0));
		desc.setBorder(BorderFactory.createTitledBorder("Description"));
		desc.add(new JScrollPane(new JLabel("<html><pre><font color=\"blue\">" + myNetwork.getDescription() + "</font></pre></html>")));
		confPanel.add(desc);
		JPanel sttngs = new JPanel(new GridLayout(3, 0));
		sttngs.setBorder(BorderFactory.createTitledBorder("Settings"));
		sttngs.add(labelTP);
		sttngs.add(labelControl);
		sttngs.add(labelQControl);
		confPanel.add(sttngs);
		JPanel events = new JPanel(new GridLayout(1, 0));
		Box events1 = Box.createHorizontalBox();
		events.setBorder(BorderFactory.createTitledBorder("Events"));
		listEvents = new JComboBox();
		listEvents.addItem("Network Control");
		events1.add(listEvents);
		buttonEvents.addActionListener(new ButtonEventsListener());
		events1.add(buttonEvents);
		events.add(events1);
		confPanel.add(events);
		return;
	}
	
	/**
	 * Generates monitors panel.
	 */
	private void fillMonPanel() {
		Vector<AbstractMonitor> monitors = myNetwork.getMonitors();
		for (int i = 0; i < monitors.size(); i++) {
			final AbstractMonitor mntr = monitors.get(i);
			JLabel l = new JLabel("<html><a href=\"\">" + mntr + "</a></html>");
			l.setToolTipText("Open Monitor '" + mntr + "'");
			l.addMouseListener(new MouseAdapter() { 
		      	  public void mouseClicked(MouseEvent e) {
	      	    	treePane.actionSelected(mntr, true);
	      	    	return;
		      	  }
		      	  public void mouseEntered(MouseEvent e) {
		    		e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		    		return;
		    	  }
			    });
			monPanel.add(l);
		}
		monPanel.setBorder(BorderFactory.createTitledBorder(""));
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
		updateConfLabels();
		if (!mySystem.getMyStatus().isStopped()) {
			updatePerfSeries();
		}
		return;
	}
	
	/**
	 * Resets the graph display.
	 */
	public synchronized void resetView() {
		cmd2item.get(cmdFileSave).setEnabled(false);
		initTime = mySystem.getMyNetwork().getSimTime();
		visViewer.repaint();
		updateConfLabels();
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
		MiscUtil.processNode(myNetwork);
		g = new DirectedSparseGraph();
		g.getEdgeConstraints().remove(Graph.NOT_PARALLEL_EDGE);
		HashMap<AbstractNode, VertexNodeHWC> n2v = new HashMap<AbstractNode, VertexNodeHWC>();
		// vertices come first
		Vector<AbstractNode> nodes = myNetwork.getNodes();
		for (i = 0; i < nodes.size(); i++) {
			AbstractNode node = nodes.get(i);
			MiscUtil.processNode(node);
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
		Vector<AbstractLink> links = myNetwork.getLinks();
		for (i = 0; i < links.size(); i++) {
			AbstractLinkHWC link = (AbstractLinkHWC)links.get(i);
			MiscUtil.processLink(link);
			AbstractNode bn = link.getBeginNode();
			AbstractNode en = link.getEndNode();
			VertexNodeHWC bv, ev;
			if (bn != null) {
				bv = n2v.get(bn);
			}
			else {
				bv = new VertexNodeHWC(link.getPosition().get().get(0));
				geoBounds[0] = makeMinPoint(geoBounds[0], bv.getPosition());
				geoBounds[1] = makeMaxPoint(geoBounds[1], bv.getPosition());
				g.addVertex(bv);
			}
			if (en != null)
				ev = n2v.get(en);
			else {
				ev = new VertexNodeHWC(link.getPosition().get().get(1));
				geoBounds[0] = makeMinPoint(geoBounds[0], ev.getPosition());
				geoBounds[1] = makeMaxPoint(geoBounds[1], ev.getPosition());
				g.addVertex(ev);
			}
			g.addEdge(new EdgeLinkHWC(bv, ev, link));
		}
		MiscUtil.resetValues();
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
		if ((nd.getType() & AbstractTypes.MASK_NETWORK) == 0) {
			txt += "<br><b>In-flow:</b> " + totalInFlow(nd).toString2() + " vph";
			txt += "<br><b>Out-flow:</b> " + totalOutFlow(nd).toString2() + " vph";
		}
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
		txt += "<br><b>Density:</b> " + lk.getAverageDensity().toString2() + " vpm";
		txt += "<br><b>Speed:</b> " + form.format(((AuroraInterval)lk.getAverageSpeed()).getCenter()) + " mph";
		if (lk.getBeginNode() == null) {
			txt += "<br><b>Demand:</b> " + lk.getDemand().toString2() + " vph";
			txt += "<br><b>Queue:</b> " + lk.getQueue().toString2();
		}
		//txt += "<br><b>Capacity:</b> " + form.format((Double)lk.getMaxFlow()) + " vph";
		//txt += "<br><b>Vff:</b> " + form.format((Double)lk.getV()) + " mph";
		//txt += "<br><b>Wc:</b> " + form.format((Double)lk.getW()) + " mph";
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
				v = 2;//*l.getLanes();
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
				//FIXME
				double speed = ((AuroraInterval)el.getLinkHWC().getSpeed()).getCenter();
				double vff = (Double)el.getLinkHWC().getV();
				c = UtilGUI.kgColor((int)Math.floor(10*(speed/vff)));
			}
			else {
				//FIXME
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
						private static final long serialVersionUID = -9153439563363701352L;
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
							private static final long serialVersionUID = -6771217211181026005L;
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
	 * This class is needed to react to "Generate" button pressed.
	 */
	private class ButtonEventsListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			EventTableModel etm = (EventTableModel)((TableSorter)treePane.getActionPane().getEventsTable().getModel()).getTableModel();
			AbstractEventPanel ep;
			switch(listEvents.getSelectedIndex()) {
			default:
				ep = new PanelEventNetworkControl();
				break;
			}
			ep.initialize(myNetwork, mySystem.getMyEventManager(), etm);
			return;
		}
		
	}
	
	
	/**
	 * Class needed for proper closing of internal network windows.
	 */
	private class AdapterWindowNetwork extends InternalFrameAdapter implements ComponentListener {
		
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