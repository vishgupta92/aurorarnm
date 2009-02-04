/**
 * @(#)WindowNetwork.java
 */

package aurora.hwc.config;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.SpringLayout;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;

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
 * @version $Id: WindowNetwork.java,v 1.1.4.1.2.2 2009/01/11 19:44:14 akurzhan Exp $
 */
public final class WindowNetwork extends JInternalFrame implements ChangeListener, DocumentListener {
	private static final long serialVersionUID = 4998289522222624440L;
	
	private AbstractContainer mySystem;
	private AbstractNodeComplex myNetwork;
	private TreePane treePane;
	private JToolBar toolbar;
	private JTabbedPane tabbedPane = new JTabbedPane();
	
	private double scaleCoeff = 1.0;
	private boolean filterON = false;
	
	// monitors panel
	private monitorTableModel monitorTM = new monitorTableModel();
	private JTable montab = new JTable(monitorTM);
	
	// configuration panel
	private JSpinner idSpinner;
	private JTextField nameText = new JTextField();
	private JTextPane descText = new JTextPane();
	private JCheckBox cbCtrl = new JCheckBox();
	private JCheckBox cbQCtrl = new JCheckBox();
	private JSpinner hh;
	private JSpinner mm;
	private JSpinner ss;
	private final static String nmID = "ID";
	private final static String nmControl = "Control";
	private final static String nmTS = "TS";
	
	private Graph g;
	private LayoutHWC layout;
	private PluggableRenderer renderer;
	private VisualizationViewer visViewer;
	private PickedState pickedState;
	//private DefaultModalGraphMouse graphMouse;
	private EditingModalGraphMouse graphMouse;
	private LensSupport magnifyViewSupport;
	private Point[] geoBounds = new Point[2];
	private ToolbarListener toolbarListener = new ToolbarListener();
	
	private Vector<AbstractNetworkElement> pickedNE = new Vector<AbstractNetworkElement>();
	private HashMap<AbstractNetworkElement, VertexNodeHWC> ne2vrtx = new HashMap<AbstractNetworkElement, VertexNodeHWC>();
	private HashMap<AbstractNetworkElement, EdgeLinkHWC> ne2edge = new HashMap<AbstractNetworkElement, EdgeLinkHWC>();
	
	//private final static String cmdNewNode = "newNode";
	//private final static String cmdEditNE = "editNE";
	//private final static String cmdDeleteNE = "deleteNE";
	private final static String cmdFixLayout = "FixLayout";
	private final static String cmdZoomIn = "zoomIn";
	private final static String cmdZoomOut = "zoomOut";
	private final static String cmdLens = "LensOnOff";
	private final static String cmdMouseMode = "mouseMode";
	private final static String cmdFilterMode = "filterMode";
	private final static String cmdButtonOK = "pressedOK";
	private final static String cmdButtonCancel = "pressedCancel";
	private final static String cmdButtonAdd = "addButtonPressed";
	private final static String cmdButtonDelete = "deleteButtonPressed";
	
	// Configuration panel
	private JPanel pID = new JPanel(new SpringLayout());
	private JPanel pNm = new JPanel(new SpringLayout());
	private JPanel pDesc = new JPanel(new BorderLayout());
	private JPanel pCtrl = new JPanel(new FlowLayout());
	private JPanel pT = new JPanel(new FlowLayout());
	private boolean idModified = false;
	private boolean nameModified = false;
	private boolean descModified = false;
	private boolean ctrlModified = false;
	private boolean tsModified = false;
	
	
	public WindowNetwork() { }
	public WindowNetwork(AbstractContainer ctnr, AbstractNodeComplex ne, TreePane tpane) {
		super("Network " + ne.toString(), true, true, true, true);
		mySystem = ctnr;
		myNetwork = ne;
		treePane = tpane;
		//Dimension dims = treePane.getActionPane().getDesktopPane().getSize();
		//setSize((int)Math.round(0.6*dims.getWidth()), (int)Math.round(0.6*dims.getHeight()));
		setSize(500, 450);
		int n = treePane.getInternalFrameCount();
		setLocation(20*n, 20*n);
		AdapterWindowNetwork listener = new AdapterWindowNetwork();
		addInternalFrameListener(listener);
		addComponentListener(listener);
		g = new DirectedSparseGraph();
		g.getEdgeConstraints().remove(Graph.NOT_PARALLEL_EDGE);
		buildGraph();
		renderer = new PluggableRenderer();
		layout = new LayoutHWC(this);
		//graphMouse = new DefaultModalGraphMouse();
		graphMouse = new EditingModalGraphMouse();
		graphMouse.setMode(Mode.TRANSFORMING);
		graphMouse.setZoomAtMouse(true);
		graphMouse.add(new PopupGraphMousePlugin());
		visViewer = new VisualizationViewer(layout, renderer);
		visViewer.setToolTipFunction(new ToolTipsHWC());
		visViewer.setPickSupport(new ShapePickSupport());
		visViewer.setGraphMouse(graphMouse);
		visViewer.setBackground(Color.white);
		pickedState = visViewer.getPickedState();
		pickedState.addItemListener(new PickedStateListener(treePane, pickedState));
		magnifyViewSupport = new ViewLensSupport(visViewer, new MagnifyShapeTransformer(visViewer), new ModalLensGraphMouse(new LensMagnificationGraphMousePlugin(1.f, 6.f, .2f)));
		LensSupport hyperbolicViewSupport = new ViewLensSupport(visViewer, new HyperbolicShapeTransformer(visViewer), new ModalLensGraphMouse());
		magnifyViewSupport.getLensTransformer().setEllipse(hyperbolicViewSupport.getLensTransformer().getEllipse());
		renderer.setVertexPaintFunction(new VertexPaintFunctionHWC(pickedState));
		renderer.setVertexIconFunction(new VertexIconAndShapeFunctionHWC(mySystem, pickedState));
		renderer.setVertexShapeFunction(new VertexIconAndShapeFunctionHWC(mySystem, pickedState));
		renderer.setVertexStringer(new VertexStringerHWC(pickedState));
		renderer.setEdgeShapeFunction(new EdgeShape.Line());
		renderer.setEdgeStrokeFunction(new EdgeStrokeFunctionHWC(pickedState));
		renderer.setEdgePaintFunction(new GradientEdgePaintFunctionHWC(new PickableEdgePaintFunction(visViewer.getPickedState(), Color.black, Color.cyan), visViewer, visViewer, pickedState));
		renderer.setEdgeStringer(new EdgeStringerHWC(pickedState));
		GraphZoomScrollPane scrollPane = new GraphZoomScrollPane(visViewer);
		JPanel panel = new JPanel(new BorderLayout());
		toolbar = makeToolbar();
		panel.add(toolbar, BorderLayout.PAGE_START);
		panel.add(scrollPane, BorderLayout.CENTER);
		tabbedPane.add("Layout", panel);
		tabbedPane.add("Monitors", fillMonPanel());
		tabbedPane.add("Configuration", fillConfPanel());
		getContentPane().add(tabbedPane);
	}

	

	/**
	 * Generates Monitors tab.
	 */
	private JPanel fillMonPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		JPanel monPanel = new JPanel(new GridBagLayout());
		montab.setPreferredScrollableViewportSize(new Dimension(250, 150));
		montab.addMouseListener(new MouseAdapter() { 
	      	  public void mouseClicked(MouseEvent e) { 
		      	    if (e.getClickCount() == 2) {
		      	    	int row = montab.rowAtPoint(new java.awt.Point(e.getX(), e.getY()));
		      	    	AbstractMonitor mon = null;
		      	    	Vector<AbstractMonitor> monitors = myNetwork.getMonitors();
		      	    	if ((row >= 0) && (row < monitors.size()))
		      	    		mon = monitors.get(row);
		      	    	Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
						nelist.add(mon);
		      	    	treePane.actionSelected(nelist, true, false);
		      	    }
		      	    return;
		      	  }
		        });
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.ipady = 100;
		gc.weightx = 0.5;
		gc.gridwidth = 2;
		gc.gridx = 0;
		gc.gridy = 0;
		monPanel.add(new JScrollPane(montab), gc);
		gc.ipady = 0; 
		gc.gridy = 1;
		gc.gridwidth = 1;
		gc.gridx = 0;
		JButton buttonAdd = new JButton("Add");
		buttonAdd.setActionCommand(cmdButtonAdd);
		buttonAdd.addActionListener(new ButtonEventsListener());
		monPanel.add(buttonAdd, gc);
		gc.gridx = 1;
		JButton buttonDelete = new JButton("Delete");
		buttonDelete.setActionCommand(cmdButtonDelete);
		buttonDelete.addActionListener(new ButtonEventsListener());
		monPanel.add(buttonDelete, gc);		
		// OK, Cancel buttons
		JPanel bp = new JPanel(new FlowLayout());
		JButton bOK = new JButton("    OK    ");
		bOK.setActionCommand(cmdButtonOK);
		bOK.addActionListener(new ButtonEventsListener());
		JButton bCancel = new JButton("Cancel");
		bCancel.setActionCommand(cmdButtonCancel);
		bCancel.addActionListener(new ButtonEventsListener());
		bp.add(bOK);
		bp.add(bCancel);
		bp.setVisible(false);
		// add all subpanels to panel
		panel.add(monPanel, BorderLayout.CENTER);
		panel.add(bp, BorderLayout.SOUTH);
		return panel;
	}
	
	/**
	 * Updates configuration panel with data from Aurora system.
	 */
	private void updateConfPanel() {
		idSpinner.setValue(myNetwork.getId());
		nameText.setText(myNetwork.getName());
		descText.setText(myNetwork.getDescription());
		cbCtrl.setSelected(myNetwork.isControlled());
		cbQCtrl.setSelected(((NodeHWCNetwork)myNetwork).hasQControl());
		hh.setValue(Util.getHours(myNetwork.getTP()));
		mm.setValue(Util.getMinutes(myNetwork.getTP()));
		ss.setValue(Util.getSeconds(myNetwork.getTP()));
		setTitle(myNetwork.toString());
		idModified = false;
		nameModified = false;
		descModified = false;
		ctrlModified = false;
		tsModified = false;
		pID.setBorder(BorderFactory.createTitledBorder("ID"));
		pNm.setBorder(BorderFactory.createTitledBorder("Name"));
		pDesc.setBorder(BorderFactory.createTitledBorder("Description"));
		pCtrl.setBorder(BorderFactory.createTitledBorder("Control"));
		pT.setBorder(BorderFactory.createTitledBorder("Sampling Period"));
		return;
	}
	
	/**
	 * Generates Configuration tab.
	 */
	private JPanel fillConfPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		Box confPanel = Box.createVerticalBox();
		// ID
		pID.setBorder(BorderFactory.createTitledBorder("ID"));
		idSpinner = new JSpinner(new SpinnerNumberModel(myNetwork.getId(), -999999999, 999999999, 1));
		idSpinner.setEditor(new JSpinner.NumberEditor(idSpinner, "#######0"));
		idSpinner.setName(nmID);
		idSpinner.addChangeListener(this);
		pID.add(idSpinner);
		SpringUtilities.makeCompactGrid(pID, 1, 1, 2, 2, 2, 2);
		confPanel.add(pID);
		// Name
		pNm.setBorder(BorderFactory.createTitledBorder("Name"));
		nameText.getDocument().addDocumentListener(this);
		pNm.add(nameText);
		SpringUtilities.makeCompactGrid(pNm, 1, 1, 2, 2, 2, 2);
		confPanel.add(pNm);
		// Description
		pDesc.setBorder(BorderFactory.createTitledBorder("Description"));
		JScrollPane descScroll = new JScrollPane(descText);
		descText.getStyledDocument().addDocumentListener(this);
		pDesc.add(descScroll, BorderLayout.CENTER);
		confPanel.add(pDesc); 
		// Control
		pCtrl.setBorder(BorderFactory.createTitledBorder("Control"));
		pCtrl.add(new JLabel("Mainline Control", JLabel.TRAILING));
		cbCtrl.setName(nmControl);
		cbCtrl.addChangeListener(this);
		pCtrl.add(cbCtrl);
		pCtrl.add(new JLabel("     Queue Control", JLabel.TRAILING));
		cbQCtrl.setName(nmControl);
		cbQCtrl.addChangeListener(this);
		pCtrl.add(cbQCtrl);
		cbCtrl.addItemListener(new CBEventsListener());
		confPanel.add(pCtrl);
		// Sampling Period
		pT.setBorder(BorderFactory.createTitledBorder("Sampling Period"));
		hh = new JSpinner(new SpinnerNumberModel(Util.getHours(myNetwork.getTP()), 0, 99, 1));
		hh.setEditor(new JSpinner.NumberEditor(hh, "00"));
		hh.setName(nmTS);
		hh.addChangeListener(this);
		pT.add(hh);
		pT.add(new JLabel("h "));
		mm = new JSpinner(new SpinnerNumberModel(Util.getMinutes(myNetwork.getTP()), 0, 59, 1));
		mm.setEditor(new JSpinner.NumberEditor(mm, "00"));
		mm.setName(nmTS);
		mm.addChangeListener(this);
		pT.add(mm);
		pT.add(new JLabel("m "));
		ss = new JSpinner(new SpinnerNumberModel(Util.getSeconds(myNetwork.getTP()), 0, 59.99, 1));
		ss.setEditor(new JSpinner.NumberEditor(ss, "00.##"));
		ss.setName(nmTS);
		ss.addChangeListener(this);
		pT.add(ss);
		pT.add(new JLabel("s"));
		confPanel.add(pT);
		updateConfPanel();
		cbQCtrl.setEnabled(cbCtrl.isSelected());
		// OK, Cancel buttons
		JPanel bp = new JPanel(new FlowLayout());
		JButton bOK = new JButton("    OK    ");
		bOK.setActionCommand(cmdButtonOK);
		bOK.addActionListener(new ButtonEventsListener());
		JButton bCancel = new JButton("Cancel");
		bCancel.setActionCommand(cmdButtonCancel);
		bCancel.addActionListener(new ButtonEventsListener());
		bp.add(bOK);
		bp.add(bCancel);
		// add all subpanels to panel
		panel.add(confPanel, BorderLayout.CENTER);
		panel.add(bp, BorderLayout.SOUTH);
		return panel;
	}

	
	/**
	 * Action performed before closing the frame.
	 */
	private void close() {
		Vector<AbstractNetworkElement> ntwks = new Vector<AbstractNetworkElement>();
		ntwks.add(myNetwork);
		treePane.removeFrame(this, ntwks);
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
	 * Assigns picked state to given Network Elements.
	 * @param nearr array of Network Elements.
	 */
	public synchronized void setPicked(Vector<AbstractNetworkElement> nearr) {
		if (nearr == null) 
			return;
		pickedState.clearPickedVertices();
		pickedState.clearPickedEdges();
		for (int i = 0; i < nearr.size(); i++) {
			VertexNodeHWC v = ne2vrtx.get(nearr.get(i));
			if (v != null) {
				pickedState.pick(v, true);
				continue;
			}
			EdgeLinkHWC e = ne2edge.get(nearr.get(i));
			if (e != null) {
				pickedState.pick(e, true);
			}
		}	
		return;
	}
	
	/**
	 * Updates the graph display.
	 */
	public synchronized void updateView() {
		visViewer.repaint();
		updateConfPanel();
		return;
	}
	
	/**
	 * Resets the graph display.
	 */
	public synchronized void resetView() {
		visViewer.repaint();
		updateConfPanel();
		return;
	}
	
	/**
	 * Refreshes the network display.
	 */
	public synchronized void refreshNetwork() {
		buildGraph();
		layout.setGeoBounds(geoBounds);
		layout.update();
		visViewer.repaint();
		updateConfPanel();
		monitorTM.fireTableStructureChanged();
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
		g.removeAllEdges();
		g.removeAllVertices();
		ne2vrtx.clear();
		ne2edge.clear();
		pickedNE.clear();
		// vertices come first
		Vector<AbstractNode> nodes = myNetwork.getNodes();
		for (i = 0; i < nodes.size(); i++) {
			AbstractNode node = nodes.get(i);
			Vector<AbstractNetworkElement> nelist = node.getPredecessors();
			int j;
			boolean goodNode = !filterON;
			for (j = 0; ((j < nelist.size()) && !goodNode); j++)
				if (nelist.get(j).getType() == (treePane.getLinkFilter() & nelist.get(j).getType()))
					goodNode = true;
			nelist = node.getSuccessors();
			for (j = 0; ((j < nelist.size()) && !goodNode); j++)
				if (nelist.get(j).getType() == (treePane.getLinkFilter() & nelist.get(j).getType()))
					goodNode = true;
			if (!goodNode)
				continue;
			MiscUtil.processNode(node);
			VertexNodeHWC v = new VertexNodeHWC(node);
			ne2vrtx.put(node, v);
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
		if (nodes.isEmpty() || (geoBounds[0].equals(geoBounds[1]))) {
			geoBounds[0] = new Point();
			geoBounds[1] = new Point();
		}
		geoBounds[0].x -= 0.010;
		geoBounds[0].y -= 0.010;
		geoBounds[1].x += 0.010;
		geoBounds[1].y += 0.010;
		// then, the edges
		Vector<AbstractLink> links = myNetwork.getLinks();
		for (i = 0; i < links.size(); i++) {
			AbstractLinkHWC link = (AbstractLinkHWC)links.get(i);
			if (filterON && (link.getType() != (treePane.getLinkFilter() & link.getType())))
				continue;
			MiscUtil.processLink(link);
			AbstractNode bn = link.getBeginNode();
			AbstractNode en = link.getEndNode();
			VertexNodeHWC bv, ev;
			if (bn != null) {
				bv = ne2vrtx.get(bn);
			}
			else {
				bv = new VertexNodeHWC(link.getPosition().get().get(0));
				geoBounds[0] = makeMinPoint(geoBounds[0], bv.getPosition());
				geoBounds[1] = makeMaxPoint(geoBounds[1], bv.getPosition());
				g.addVertex(bv);
			}
			if (en != null)
				ev = ne2vrtx.get(en);
			else {
				ev = new VertexNodeHWC(link.getPosition().get().get(1));
				geoBounds[0] = makeMinPoint(geoBounds[0], ev.getPosition());
				geoBounds[1] = makeMaxPoint(geoBounds[1], ev.getPosition());
				g.addVertex(ev);
			}
			EdgeLinkHWC e = new EdgeLinkHWC(bv, ev, link);
			g.addEdge(e);
			ne2edge.put(link, e);
		}
		MiscUtil.resetValues();
		return;
	}
	
	/**
	 * Fix the network layout.
	 */
	@SuppressWarnings("unchecked")
	private final void fixLayout() {
		Set<ArchetypeGraph> vertices = g.getVertices();
		if (vertices.isEmpty())
			return;
		Iterator<ArchetypeGraph> iter;
		for(iter = vertices.iterator(); iter.hasNext();) {
			VertexNodeHWC vrtx = (VertexNodeHWC)iter.next();
			vrtx.getPosition();
			Point pos = new Point(layout.getX(vrtx), layout.getY(vrtx), 0.0);
			vrtx.setPosition(pos);
			AbstractNode nd = vrtx.getNodeHWC();
			if (nd != null) {
				nd.getPosition().set(pos);
			}
			else {
				boolean begin = false;
				Set<ArchetypeGraph> edges = vrtx.getInEdges();
				if (edges.isEmpty()) {
					edges = vrtx.getOutEdges();
					begin = true;
				}
				if (edges.isEmpty())
					continue;
				Iterator<ArchetypeGraph> iter2 = edges.iterator();
				EdgeLinkHWC edge = (EdgeLinkHWC)iter2.next();
				if (begin)
					edge.getLinkHWC().getPosition().setBegin(pos);
				else
					edge.getLinkHWC().getPosition().setEnd(pos);
			}
		}
		refreshNetwork();
		mySystem.getMyStatus().setSaved(false);
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
		//tb.add(makeToolbarButton("/icons/addnode.gif", cmdNewNode, "New node", "NN"));
		//tb.add(makeToolbarButton("/icons/editicon.gif", cmdEditNE, "Edit", "E"));
		//tb.add(makeToolbarButton("/icons/deleteicon.gif", cmdDeleteNE, "Delete", "D"));
		tb.add(makeToolbarButton("/icons/importicon.gif", cmdFixLayout, "Fix Layout", "Fs"));
		tb.add(makeToolbarToggleButton("/icons/filtericon.gif", cmdFilterMode, "Filter On/Off", "~"));
		tb.add(makeToolbarToggleButton("/icons/finger.jpg", cmdMouseMode, "Picking/Transforming", "PM"));
		tb.add(makeToolbarButton("/icons/zoomin.gif", cmdZoomIn, "Zoom-in", "+"));
		tb.add(makeToolbarButton("/icons/zoomout.gif", cmdZoomOut, "Zoom-out", "-"));
		return tb;
	}
	
	/**
	 * Makes HTML label for given Node.
	 * @param nd Node object.
	 * @return label.
	 */
	private static String htmlNodeLabel(AbstractNode nd, String clr) {
		NumberFormat form = NumberFormat.getInstance();
		form.setMinimumFractionDigits(2);
		form.setMaximumFractionDigits(2);
		if (nd == null)
			return null;
		String txt = "<html><font color=\"" + clr + "\"><b><u>" + nd.toString() + "</u></b>";
		txt += " (" + TypesHWC.typeString(nd.getType()) + ")";
		String desc = nd.getDescription();
		if (desc != null)
			txt += "<p>" + desc;
		txt += "<br><b>In-links:</b> " + nd.getPredecessors().size();
		txt += "<br><b>Out-links:</b> " + nd.getSuccessors().size();
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
		txt += "<br><b>Capacity:</b> " + form.format((Double)lk.getMaxFlow()) + " vph";
		txt += "<br><b>Vff:</b> " + form.format((Double)lk.getV()) + " mph";
		txt += "<br><b>Wc:</b> " + form.format((Double)lk.getW()) + " mph";
		txt += "</font></html>";
		return txt;
	}

	
	/**
	 * Reaction to spinner and text field changes.
	 */
	public void stateChanged(ChangeEvent e) {
		String nm = ((JComponent)e.getSource()).getName();
		if (nm.equals(nmID)) {
			idModified = true;
			pID.setBorder(BorderFactory.createTitledBorder("*ID"));
		}
		if (nm.equals(nmControl)) {
			ctrlModified = true;
			pCtrl.setBorder(BorderFactory.createTitledBorder("*Control"));
		}
		if (nm.equals(nmTS)) {
			tsModified = true;
			pT.setBorder(BorderFactory.createTitledBorder("*Sampling Period"));
		}
		return;
	}
	
	/**
	 * Reaction to name or description update.
	 */
	private void txtUpdate(DocumentEvent e) {
		if (e.getDocument().equals(descText.getDocument())) {
			descModified = true;
			pDesc.setBorder(BorderFactory.createTitledBorder("*Description"));
		}
		if (e.getDocument().equals(nameText.getDocument())) {
			nameModified = true;
			pNm.setBorder(BorderFactory.createTitledBorder("*Name"));
		}
		return;
	}
	
	/**
	 * Reaction to character change in description.
	 */
	public void changedUpdate(DocumentEvent e) {
		txtUpdate(e);
		return;
	}
	
	/**
	 * Reaction to character insertion into description.
	 */
	public void insertUpdate(DocumentEvent e) {
		txtUpdate(e);
		return;
	}
	
	/**
	 * Reaction to character deletion from description.
	 */
	public void removeUpdate(DocumentEvent e) {
		txtUpdate(e);
		return;
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
				return Color.ORANGE;
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
		private PickedInfo pi = null;
		
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
			if (false && (pi != null) && (pi.isPicked(e)))
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
		private PickedInfo pi = null;
        
		public GradientEdgePaintFunctionHWC(EdgePaintFunction defaultEdgePaintFunction, HasGraphLayout vv, LayoutTransformer transformer) {
			super(Color.WHITE, Color.BLACK, vv, transformer);
		}
		
		public GradientEdgePaintFunctionHWC(EdgePaintFunction defaultEdgePaintFunction, HasGraphLayout vv, LayoutTransformer transformer, PickedInfo pi) {
			super(Color.WHITE, Color.BLACK, vv, transformer);
			this.pi = pi;
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
			if ((pi != null) && (pi.isPicked(e)))
				return Color.MAGENTA;
			else
				return Color.DARK_GRAY;
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
			AbstractNetworkElement ne = null;
			if (e.getItem() instanceof VertexNodeHWC) {
				VertexNodeHWC v = (VertexNodeHWC)e.getItem();
				ne = v.getNodeHWC();
				if (ne == null)
					return;
				if (!pi.isPicked(v))
					pickedNE.remove(ne);
				else
					pickedNE.add(ne);
			}
			else {
				EdgeLinkHWC el = (EdgeLinkHWC)e.getItem();
				ne = el.getLinkHWC();
				if (ne == null)
					return;
				if (!pi.isPicked(el))
					pickedNE.remove(ne);
				else
					pickedNE.add(ne);
			}
			tree.actionSelected(pickedNE, false, false);
			return;
		}
		
	}
	
	
	/**
	 * Listener for toolbar button actions.
	 */
	private class ToolbarListener implements ActionListener {
		private ScalingControl crossoverScaler = new CrossoverScalingControl();
		private ScalingControl layoutScaler = new LayoutScalingControl();
		private ScalingControl scaler = crossoverScaler;
		
		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmdFixLayout.equals(cmd)) {
				fixLayout();
				return;
			}
			if (cmdZoomIn.equals(cmd)) {
				scaler.scale(visViewer, 1.1f, visViewer.getCenter());
				scaleCoeff = 1.1 * scaleCoeff;
				return;
			}
			if (cmdZoomOut.equals(cmd)) {
				scaler.scale(visViewer, 1/1.1f, visViewer.getCenter());
				scaleCoeff = (1/1.1) * scaleCoeff;
				return;
			}
			if (cmdFilterMode.equals(cmd)) {
				filterON = !filterON;
				refreshNetwork();
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
			return;
		}
	}
	
	
	/**
	 * Listener for mouse actions - reacts for right button pressed. 
	 */
	private class PopupGraphMousePlugin extends AbstractPopupGraphMousePlugin implements MouseListener {
        
		public PopupGraphMousePlugin() {
			this(MouseEvent.BUTTON3_MASK);
		}
		public PopupGraphMousePlugin(int modifiers) {
			super(modifiers);
		}

		protected void handlePopup(MouseEvent e) {
			final VisualizationViewer vv = (VisualizationViewer)e.getSource();
			Point2D p = vv.inverseViewTransform(e.getPoint());
			//Point2D tp = new Point2D.Double(p.getX()/scaleCoeff, p.getY()/scaleCoeff);
			Point2D invP = layout.inverseTransform(p);
			final Point pntPos = new Point(invP.getX(), invP.getY(), 0.0);
			PickSupport pickSupport = vv.getPickSupport();
			JPopupMenu popup = new JPopupMenu();
			if(pickSupport != null) {
				final Vertex v = pickSupport.getVertex(p.getX(), p.getY());
				Set picked = pickedState.getPickedVertices();
				if (v != null) { // mouse points to a node
					final AbstractNode mynd = ((VertexNodeHWC)v).getNodeHWC();
					if ((!picked.isEmpty()) && (mynd != null) && ((!pickedState.isPicked(v)) || (picked.size() > 1))){
						JMenu inlinkMenu = new JMenu("New In-Link");
						JMenu outlinkMenu = new JMenu("New Out-Link");
						for(Iterator iterator=picked.iterator(); iterator.hasNext(); ) {
							final AbstractNode nd = ((VertexNodeHWC)iterator.next()).getNodeHWC();
							if ((nd != null) && (!mynd.equals(nd))) {
								inlinkMenu.add(new AbstractAction(nd + " > " + mynd) {
									private static final long serialVersionUID = 7367383855400541984L;
									public void actionPerformed(ActionEvent e) {
										new PanelNewLink(treePane, myNetwork, nd, mynd);
									}
								});
								outlinkMenu.add(new AbstractAction(mynd + " > " + nd) {
									private static final long serialVersionUID = -8913310541671922218L;
									public void actionPerformed(ActionEvent e) {
										new PanelNewLink(treePane, myNetwork, mynd, nd);
									}
								});
							}
						}
						popup.add(inlinkMenu);
						popup.add(outlinkMenu);
					}
					popup.add(new AbstractAction("Edit") {
						private static final long serialVersionUID = -5680606490556010616L;
						public void actionPerformed(ActionEvent e) {
							if (!pickedNE.isEmpty())
								treePane.actionSelected(pickedNE, true, false);
							else {
								Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
								nelist.add(mynd);
								treePane.actionSelected(nelist, true, false);
							}
						}
					});
					popup.add(new AbstractAction("Delete") {
						private static final long serialVersionUID = -8082073959660637944L;

						public void actionPerformed(ActionEvent e) {
							if (!pickedNE.isEmpty())
								treePane.deleteComponents(pickedNE);
							else {
								Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
								nelist.add(mynd);
								treePane.deleteComponents(nelist);
							}
						}
					});
				} else { // mouse points to an edge
					final Edge edge = pickSupport.getEdge(p.getX(), p.getY());
					if(edge != null) {
						final AbstractLink lnk = ((EdgeLinkHWC)edge).getLinkHWC();
						if (!picked.isEmpty()) {
							JMenu bnMenu = new JMenu("Assign Begin Node");
							JMenu enMenu = new JMenu("Assign End Node");
							for(Iterator iterator=picked.iterator(); iterator.hasNext(); ) {
								final AbstractNode nd = ((VertexNodeHWC)iterator.next()).getNodeHWC();
								if (nd != null) {
									bnMenu.add(new AbstractAction(nd.toString()) {
										private static final long serialVersionUID = 5386209712445517511L;
										public void actionPerformed(ActionEvent e) {
											lnk.assignBeginNode(nd);
											refreshNetwork();
											mySystem.getMyStatus().setSaved(false);
										}
									});
									enMenu.add(new AbstractAction(nd.toString()) {
										private static final long serialVersionUID = -291823517280217546L;
										public void actionPerformed(ActionEvent e) {
											lnk.assignEndNode(nd);
											refreshNetwork();
											mySystem.getMyStatus().setSaved(false);
										}
									});
								}
							}
							popup.add(bnMenu);
							popup.add(enMenu);
						}
						popup.add(new AbstractAction("Edit") {
							private static final long serialVersionUID = 6803143756731278913L;
							public void actionPerformed(ActionEvent e) {
								if (!pickedNE.isEmpty())
									treePane.actionSelected(pickedNE, true, false);
								else {
									AbstractNetworkElement ne = ((EdgeLinkHWC)edge).getLinkHWC();
									if (ne != null) {
										Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
										nelist.add(ne);
										treePane.actionSelected(nelist, true, false);
									}
								}
							}
						});
						popup.add(new AbstractAction("Delete") {
							private static final long serialVersionUID = -2003945380719910046L;
							public void actionPerformed(ActionEvent e) {
								if (!pickedNE.isEmpty())
									treePane.deleteComponents(pickedNE);
								else {
									AbstractNetworkElement ne = ((EdgeLinkHWC)edge).getLinkHWC();
									if (ne != null) {
										Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
										nelist.add(ne);
										treePane.deleteComponents(nelist);
									}
								}
							}
						});
					} else { // mouse points to empty spot
						popup.add(new AbstractAction("New Node") {
							private static final long serialVersionUID = -2605498054729689751L;
							public void actionPerformed(ActionEvent e) {
								new PanelNewNode(treePane, myNetwork, pntPos);
							}
						});
						if (!picked.isEmpty()) { // some nodes are picked
							JMenu sourceMenu = new JMenu("New Source Link");
							JMenu destinationMenu = new JMenu("New Destination Link");
							for(Iterator iterator=picked.iterator(); iterator.hasNext(); ) {
								final AbstractNode nd = ((VertexNodeHWC)iterator.next()).getNodeHWC();
								if (nd != null) {
									sourceMenu.add(new AbstractAction("_ > " + nd) {
										private static final long serialVersionUID = -6148699867883234841L;
										public void actionPerformed(ActionEvent e) {
											new PanelNewLink(treePane, myNetwork, pntPos, nd);
										}
									});
									destinationMenu.add(new AbstractAction(nd + " > _") {
										private static final long serialVersionUID = 5386209712445517511L;
										public void actionPerformed(ActionEvent e) {
											new PanelNewLink(treePane, myNetwork, nd, pntPos);
										}
									});
								}
							}
							popup.add(sourceMenu);
							popup.add(destinationMenu);
						}
						if (!pickedNE.isEmpty()) {
							popup.add(new AbstractAction("Edit") {
								private static final long serialVersionUID = -5049918080955461830L;
								public void actionPerformed(ActionEvent e) {
									treePane.actionSelected(pickedNE, true, false);
								}
							});
							popup.add(new AbstractAction("Delete") {
								private static final long serialVersionUID = -7380675337275537668L;
								public void actionPerformed(ActionEvent e) {
									treePane.deleteComponents(pickedNE);
								}
							});
						}
					}
				}
			}
			if(popup.getComponentCount() > 0)
				popup.show(vv, e.getX(), e.getY());
		}
	}
	
	
	/**
	 * This class is needed to react to Mainline Control checkbox toggled.
	 */
	private class CBEventsListener implements ItemListener {

		public void itemStateChanged(ItemEvent e) {
			cbQCtrl.setEnabled(cbCtrl.isSelected());
			return;
		}
		
	}
	
	
	/**
	 * This class is needed to react to "Add", "Delete, "OK" or "Cancel" buttons pressed.
	 */
	private class ButtonEventsListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmdButtonAdd.equals(cmd)) {
				new PanelNewMonitor(treePane, myNetwork);
			}
			if (cmdButtonDelete.equals(cmd)) {
				int[] selected = montab.getSelectedRows();
				if ((selected != null) && (selected.length > 0)) {
					Vector<AbstractMonitor> monitors = myNetwork.getMonitors();
					Vector<AbstractNetworkElement> deleted = new Vector<AbstractNetworkElement>(); 
					for (int i = 0; i < selected.length; i++)
						deleted.add(monitors.get(selected[i]));
					treePane.deleteComponents(deleted);
				}
			}
			if (cmdButtonOK.equals(cmd)) {
				if (idModified)
					myNetwork.setId((Integer)idSpinner.getValue());
				if (nameModified)
					myNetwork.setName(nameText.getText());
				if (descModified)
					myNetwork.setDescription(descText.getText());
				if (ctrlModified)
					((NodeHWCNetwork)myNetwork).setControlled(cbCtrl.isSelected(), cbQCtrl.isSelected());
				if (tsModified) {
					int h = (Integer)hh.getValue();
					int m = (Integer)mm.getValue();
					double s = (Double)ss.getValue();
					myNetwork.setTP(h + (m/60.0) + (s/3600.0));
				}
				updateConfPanel();
				tabbedPane.setSelectedIndex(0);
				treePane.updateNE(myNetwork);
				mySystem.getMyStatus().setSaved(false);
			}
			if (cmdButtonCancel.equals(cmd)) {
				updateConfPanel();
				tabbedPane.setSelectedIndex(0);
			}
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
			return;
		}

		public void componentMoved(ComponentEvent e) {
			return;
		}

		public void componentResized(ComponentEvent e) {
			resize();
			return;
		}

		public void componentShown(ComponentEvent e) {
			return;
		}
		
	}
	
	
	/**
	 * Class needed for displaying destination/source pairs in a table.
	 */
	private class monitorTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 5381847825485619476L;

		public String getColumnName(int col) {
			if (col == 0)
				return "Monitor";
			if (col == 1)
				return "Description";
	        return "Enabled";
	    }
		
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return myNetwork.getMonitors().size();
		}

		public Object getValueAt(int row, int column) {
			if (column == 0)
				return myNetwork.getMonitors().get(row);
			if (column == 1)
				return myNetwork.getMonitors().get(row).getDescription();
			return myNetwork.getMonitors().get(row).isEnabled();
		}
		
		public boolean isCellEditable(int row, int column) {
			if (column == 2)
				return true;
			return false;
		}
		
	    @SuppressWarnings("unchecked")
		public Class getColumnClass(int c) {
	    	Class cl = String.class;
	    	try {
	    		cl = getValueAt(0, c).getClass();
	    	}
	    	catch(Exception e) { }
	       return cl;
	    }
	    
	    public void setValueAt(Object value, int row, int col) {
	    	if ((value == null) || (col != 2))
	    		return;
	    	AbstractMonitor mon = myNetwork.getMonitors().get(row);
	    	if (mon != null) {
	    		mon.setEnabled((Boolean)value);
	    		mySystem.getMyStatus().setSaved(false);
	    	}
	    	return;
	    }
	}
	
}