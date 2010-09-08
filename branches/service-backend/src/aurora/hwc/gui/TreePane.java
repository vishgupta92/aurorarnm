/**
 * @(#)TreePane.java
 */

package aurora.hwc.gui;

import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import aurora.*;
import aurora.hwc.*;
import aurora.util.*;


/**
 * Tree view of the Aurora network.
 * @author Alex Kurzhanskiy, Gabriel Gomes
 * @version $Id$
 */
public class TreePane extends JPanel {
	private static final long serialVersionUID = -855000922287970148L;
	
	private ContainerHWC mySystem = null;
	private ActionPane actionPane;
    private JTree tree;
    private HashMap<AbstractNetworkElement, JInternalFrame> ne2win = new HashMap<AbstractNetworkElement, JInternalFrame>();
    private HashMap<JInternalFrame, AbstractNetworkElement> win2ne = new HashMap<JInternalFrame, AbstractNetworkElement>();
    private HashMap<AbstractNetworkElement, DefaultMutableTreeNode> ne2tn = new HashMap<AbstractNetworkElement, DefaultMutableTreeNode>();
    private HashMap<Path, JInternalFrame> pth2win = new HashMap<Path, JInternalFrame>();
    private HashMap<JInternalFrame, Path> win2pth = new HashMap<JInternalFrame, Path>();
    private HashMap<Path, DefaultMutableTreeNode> pth2tn = new HashMap<Path, DefaultMutableTreeNode>();
    
    private File currentDir = new File(AuroraConstants.DEFAULT_HOME);
    
    private double minFlow = 0;
	private double maxFlow = 1;
	private double minDensity = 0;
	private double maxDensity = 1;
	private double minSpeed = 0;
	private double maxSpeed = 1;
    
    
    public TreePane() { }
    public TreePane(ContainerHWC ctnr) {
        super(new GridLayout(1,0));
        mySystem = ctnr;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(mySystem.getMyNetwork());
        ne2tn.put(mySystem.getMyNetwork(), root);
        fillTreeData(root, mySystem.getMyNetwork());
        tree = new JTree(root);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addMouseListener(new MouseAdapter() { 
        	  public void mouseClicked(MouseEvent e) { 
        	    if (e.getClickCount() == 2) {
        	      TreePath path = tree.getPathForLocation(e.getX(), e.getY()); 
        	      if (path != null) { 
        	    	 Object comp = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
        	    	 if (comp instanceof AbstractNetworkElement)
        	        	 actionSelected((AbstractNetworkElement)comp, true);
        	    	 if (comp instanceof Path)
        	        	 actionSelected((Path)comp, true);
        	      }
        	    }
        	  } 
        	}); 
        ToolTipManager.sharedInstance().registerComponent(tree);
        tree.setCellRenderer(new MyRenderer());
        tree.collapseRow(0);
        JScrollPane treeView = new JScrollPane(tree);
        actionPane = new ActionPane(mySystem, this);
        //actionPane.getDesktopPane().addComponentListener(new DesktopFrameListener(this));
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(treeView);
        splitPane.setRightComponent(actionPane);
        Dimension minimumSize = new Dimension(50, 50);
        actionPane.setMinimumSize(minimumSize);
        treeView.setMinimumSize(minimumSize);
        splitPane.setDividerLocation((int)Math.round(0.20*mySystem.getMySettings().getWindowSize().getWidth()));
        add(splitPane);
    }
    
    /**
     * Opens Network internal frame.
     */
    private synchronized void openWindowNetwork(AbstractNetworkElement ne, ImageIcon icon) {
    	JInternalFrame wn;
    	if (mySystem.getMySettings().isPrediction())
    		wn = new WindowNetworkP(mySystem, (AbstractNodeComplex)ne, this);
    	else
    		wn = new WindowNetwork(mySystem, (AbstractNodeComplex)ne, this);
		wn.setFrameIcon(icon);
		wn.setVisible(true);
		actionPane.getDesktopPane().add(wn);
		try {
			wn.setSelected(true);
		}
		catch(java.beans.PropertyVetoException e) { }
		ne2win.put(ne, wn);
		win2ne.put(wn, ne);
    	return;
    }
    
    /**
     * Opens Zipper Monitor internal frame.
     */
    private synchronized void openWindowMonitorZipper(AbstractNetworkElement ne, ImageIcon icon) {
    	WindowMonitorZipper wn = new WindowMonitorZipper((MonitorZipperHWC)ne, this);
		wn.setFrameIcon(icon);
		wn.setVisible(true);
		actionPane.getDesktopPane().add(wn);
		try {
			wn.setSelected(true);
		}
		catch(java.beans.PropertyVetoException e) { }
		ne2win.put(ne, wn);
		win2ne.put(wn, ne);
    	return;
    }
    
    /**
     * Opens Control Monitor internal frame.
     */
    private synchronized void openWindowMonitorController(AbstractNetworkElement ne, ImageIcon icon) {
    	WindowMonitorController wn = new WindowMonitorController((MonitorControllerHWC)ne, this);
		wn.setFrameIcon(icon);
		wn.setVisible(true);
		actionPane.getDesktopPane().add(wn);
		try {
			wn.setSelected(true);
		}
		catch(java.beans.PropertyVetoException e) { }
		ne2win.put(ne, wn);
		win2ne.put(wn, ne);
    	return;
    }
    
    /**
     * Opens Node internal frame.
     */
    private synchronized void openWindowNode(AbstractNetworkElement ne, ImageIcon icon) {
    	JInternalFrame wn;
    	if (mySystem.getMySettings().isPrediction())
    		wn = new WindowNodeP(mySystem, (AbstractNodeHWC)ne, this);
    	else
    		wn = new WindowNode(mySystem, (AbstractNodeHWC)ne, this);
		wn.setFrameIcon(icon);
		wn.setVisible(true);
		actionPane.getDesktopPane().add(wn);
		try {
			wn.setSelected(true);
		}
		catch(java.beans.PropertyVetoException e) { }
		ne2win.put(ne, wn);
		win2ne.put(wn, ne);
    	return;
    }
    
    /**
     * Opens Signal internal frame.
     */
    private synchronized void openWindowNodeSignal(AbstractNetworkElement ne, ImageIcon icon) {
    	WindowNodeSignal wn = new WindowNodeSignal(mySystem, (AbstractNodeHWC)ne, this);
		wn.setFrameIcon(icon);
		wn.setVisible(true);
		actionPane.getDesktopPane().add(wn);
		try {
			wn.setSelected(true);
		}
		catch(java.beans.PropertyVetoException e) { }
		ne2win.put(ne, wn);
		win2ne.put(wn, ne);
    	return;
    }
    
    /**
     * Opens Link internal frame.
     */
    private synchronized void openWindowLink(AbstractNetworkElement ne, ImageIcon icon) {
    	JInternalFrame wn;
    	if (mySystem.getMySettings().isPrediction())
    		wn = new WindowLinkP(mySystem, (AbstractLinkHWC)ne, this);
    	else
    		wn = new WindowLink(mySystem, (AbstractLinkHWC)ne, this);
		wn.setFrameIcon(icon);
		wn.setVisible(true);
		actionPane.getDesktopPane().add(wn);
		try {
			wn.setSelected(true);
		}
		catch(java.beans.PropertyVetoException e) { }
		ne2win.put(ne, wn);
		win2ne.put(wn, ne);
    	return;
    }
    
    /**
     * Opens Sensor internal frame.
     */
    private synchronized void openWindowSensor(AbstractNetworkElement ne, ImageIcon icon) {
    	WindowSensor wn = new WindowSensor(mySystem, (SensorLoopDetector)ne, this);
		wn.setFrameIcon(icon);
		wn.setVisible(true);
		actionPane.getDesktopPane().add(wn);
		try {
			wn.setSelected(true);
		}
		catch(java.beans.PropertyVetoException e) { }
		ne2win.put(ne, wn);
		win2ne.put(wn, ne);
    	return;
    }
    
    /**
     * Opens Path internal frame.
     */
    private synchronized void openWindowPath(Path pth, ImageIcon icon) {
    	JInternalFrame wn;
    	if (mySystem.getMySettings().isPrediction())
    		wn = new WindowPathP(mySystem, (PathHWC)pth, this);
    	else
    		wn = new WindowPath(mySystem, (PathHWC)pth, this);
		wn.setFrameIcon(icon);
		wn.setVisible(true);
		actionPane.getDesktopPane().add(wn);
		try {
			wn.setSelected(true);
		}
		catch(java.beans.PropertyVetoException e) { }
		pth2win.put(pth, wn);
		win2pth.put(wn, pth);
    	return;
    }
    

    /**
     * Action triggered by selecting Network Element from the tree.
     */
    public synchronized void actionSelected(AbstractNetworkElement ne, boolean openwin) {
    	if (ne != null) {
    		TreePath path = new TreePath(ne2tn.get(ne).getPath());
    		tree.setSelectionPath(path);
    	}
    	if (!openwin)
    		return;
    	JInternalFrame frame = ne2win.get(ne);
    	if (frame != null) {
    		try {
    			frame.setIcon(false);
    			frame.setSelected(true);
    		}
    		catch(java.beans.PropertyVetoException e) { }
    		return;
    	}
    	ImageIcon icon = UtilGUI.createImageIcon("/icons/" + ne.getClass().getSimpleName() + ".gif");
    	switch(ne.getType()) {
    	case TypesHWC.NETWORK_HWC:
    		openWindowNetwork(ne, icon);
    		return;
    	case TypesHWC.LINK_FREEWAY:
    	case TypesHWC.LINK_HIGHWAY:
    	case TypesHWC.LINK_HOV:
    	case TypesHWC.LINK_ONRAMP:
    	case TypesHWC.LINK_OFFRAMP:
    	case TypesHWC.LINK_INTERCONNECT:
    	case TypesHWC.LINK_STREET:
    		openWindowLink(ne, icon);
    		return;
    	case TypesHWC.NODE_FREEWAY:
    	case TypesHWC.NODE_HIGHWAY:
    	case TypesHWC.NODE_STOP:
    		openWindowNode(ne, icon);
    		return;
    	case TypesHWC.NODE_SIGNAL:
    		openWindowNodeSignal(ne, icon);
    		return;
    	case AbstractTypes.MASK_MONITOR_ZIPPER:
    		openWindowMonitorZipper(ne, icon);
    		return;
    	case TypesHWC.MASK_MONITOR_CONTROLLER:
    		openWindowMonitorController(ne, icon);
    		return;
    	case TypesHWC.SENSOR_LOOPDETECTOR:
    		openWindowSensor(ne, icon);
    		return;
    	default:
    		
    	}
    	return;
    }
    
    /**
     * Action triggered by selecting Network Element from the tree.
     */
    public synchronized void actionSelected(Path pth, boolean openwin) {
    	if (pth != null) {
    		TreePath path = new TreePath(pth2tn.get(pth).getPath());
    		tree.setSelectionPath(path);
    	}
    	if (!openwin)
    		return;
    	JInternalFrame frame = pth2win.get(pth);
    	if (frame != null) {
    		try {
    			frame.setIcon(false);
    			frame.setSelected(true);
    		}
    		catch(java.beans.PropertyVetoException e) { }
    		return;
    	}
    	ImageIcon icon = UtilGUI.createImageIcon("/icons/" + pth.getClass().getSimpleName() + ".gif");
    	openWindowPath(pth, icon);
    	return;
    }
    
    /**
     * Removes specified frame from the list.
     * @param frame
     */
    public synchronized void removeFrame(JInternalFrame frame) {
    	if (frame == null)
    		return;
    	AbstractNetworkElement ne = win2ne.get(frame);
    	if (ne != null) {
    		win2ne.remove(frame);
    		ne2win.remove(ne);
    		return;
    	}
    	Path pth = win2pth.get(frame);
    	if (pth != null) {
    		win2pth.remove(frame);
    		pth2win.remove(pth);
    	}
    	return;
    }
    
    /**
     * Resizes all internal frames according to new desktop size.
     */
    public synchronized void resizeFrames() {
    	Set<JInternalFrame> frames = win2ne.keySet();
    	Iterator<JInternalFrame> iter;
    	int i = 0;
    	for (iter = frames.iterator(); iter.hasNext();) {
    		JInternalFrame frame = iter.next();
    		Dimension dims1 = actionPane.getDesktopPane().getSize();
    		Dimension dims2 = frame.getSize();
    		int x = (int)Math.floor(Math.min(dims1.getWidth(), dims2.getWidth()));
    		int y = (int)Math.floor(Math.min(dims1.getHeight(), dims2.getHeight()));
    		frame.setSize(new Dimension(x, y));
    		frame.setLocation(20*i, 20*i);
    		i++;
    	}
    	frames = win2pth.keySet();
    	i = 0;
    	for (iter = frames.iterator(); iter.hasNext();) {
    		JInternalFrame frame = iter.next();
    		Dimension dims1 = actionPane.getDesktopPane().getSize();
    		Dimension dims2 = frame.getSize();
    		int x = (int)Math.floor(Math.min(dims1.getWidth(), dims2.getWidth()));
    		int y = (int)Math.floor(Math.min(dims1.getHeight(), dims2.getHeight()));
    		frame.setSize(new Dimension(x, y));
    		frame.setLocation(20*i, 20*i);
    		i++;
    	}
    	return;
    }
    
    /**
     * Returns the action pane.
     */
    public ActionPane getActionPane() {
    	return actionPane;
    }
    
    /**
     * Returns number of internal frames.
     */
    public int getInternalFrameCount() {
    	return ne2win.size() + pth2win.size();
    }
    
    /**
     * Fills in the tree using the data from given complex Node.
     * @param root root of the subtree.
     * @param ntwk complex Node.
     */
    private void fillTreeData(DefaultMutableTreeNode root, AbstractNodeComplex ntwk) {
    	int i;
    	DefaultMutableTreeNode list = null;
    	DefaultMutableTreeNode item = null;
    	Vector<AbstractNode> nodes = ntwk.getNodes();
    	if (nodes.size() > 0) { // has nodes
    		list = new DefaultMutableTreeNode("Nodes");
    		root.add(list);
    		for (i = 0; i < nodes.size(); i++) {
    			item = new DefaultMutableTreeNode(nodes.get(i));
    			if (!nodes.get(i).isSimple())
    				fillTreeData(item, (AbstractNodeComplex)nodes.get(i));
    			list.add(item);
    			ne2tn.put(nodes.get(i), item);
    		}
        }
    	Vector<AbstractLink> links = ntwk.getLinks();
    	if (links.size() > 0) { // has links
    		list = new DefaultMutableTreeNode("Links");
    		root.add(list);
    		for (i = 0; i < links.size(); i++) {
    			item = new DefaultMutableTreeNode(links.get(i));
    			list.add(item);
    			ne2tn.put(links.get(i), item);
    		}
    	}
    	Vector<AbstractMonitor> monitors = ntwk.getMonitors();
    	if (monitors.size() > 0) { // has monitors
    		list = new DefaultMutableTreeNode("Monitors");
    		root.add(list);
    		for (i = 0; i < monitors.size(); i++) {
    			item = new DefaultMutableTreeNode(monitors.get(i));
    			list.add(item);
    			ne2tn.put(monitors.get(i), item);
    		}
    	}
    	Vector<AbstractSensor> sensors = ntwk.getSensors();
    	if (sensors.size() > 0) { // has sensors
    		list = new DefaultMutableTreeNode("Sensors");
    		root.add(list);
    		for (i = 0; i < sensors.size(); i++) {
    			item = new DefaultMutableTreeNode(sensors.get(i));
    			list.add(item);
    			ne2tn.put(sensors.get(i), item);
    		}
    	}
    	Vector<OD> ods = ntwk.getODList();
    	if (!ods.isEmpty()) // has ODs
    		addODs(root, ods);
    	return;
    }
    
    /**
     * Adds ODs to the tree.
     * @param root root of the subtree.
     * @param odlist vector of ODs.
     */
    private void addODs(DefaultMutableTreeNode root, Vector<OD> odlist) {
    	DefaultMutableTreeNode odl = new DefaultMutableTreeNode("ODs");
    	root.add(odl);
    	for (int i = 0; i < odlist.size(); i++) {
    		DefaultMutableTreeNode oditem = new DefaultMutableTreeNode(odlist.get(i));
    		odl.add(oditem);
    		Vector<Path> pthlist = odlist.get(i).getPathList();
    		if (!pthlist.isEmpty())
    	   		for (int j = 0; j < pthlist.size(); j++) {
    	   			DefaultMutableTreeNode pthitem = new DefaultMutableTreeNode(pthlist.get(j));
    	   			oditem.add(pthitem);
    	   			pth2tn.put(pthlist.get(j), pthitem);
    	   		}
    	}
    	return;
    }
    
    /**
     * Updates configuration panels in network windows.
     */
    public synchronized void updateConfig() {
    	Set<JInternalFrame> frames = win2ne.keySet();
    	Iterator<JInternalFrame> iter;
    	for (iter = frames.iterator(); iter.hasNext();) {
    		JInternalFrame frame = iter.next();
    		AbstractNetworkElement ne = win2ne.get(frame);
    		if ((ne.getType() & TypesHWC.MASK_NETWORK) > 0) {
    			if (mySystem.getMySettings().isPrediction())
    				((WindowNetworkP)frame).updateView();
    			else
    				((WindowNetwork)frame).updateView();
    		}
    	}
    	return;
    }
    
    /**
     * Updates view in all frames.
     */
    public synchronized void updateView() {
    	Set<JInternalFrame> frames = win2ne.keySet();
    	Iterator<JInternalFrame> iter;
    	for (iter = frames.iterator(); iter.hasNext();) {
    		JInternalFrame frame = iter.next();
    		AbstractNetworkElement ne = win2ne.get(frame);
    		if ((ne.getType() & TypesHWC.MASK_NETWORK) > 0) {
    			if (mySystem.getMySettings().isPrediction())
    				((WindowNetworkP)frame).updateView();
    			else
    				((WindowNetwork)frame).updateView();
    		}
    		if ((ne.getType() & TypesHWC.MASK_NODE) > 0) {
    			if (mySystem.getMySettings().isPrediction())
    				((WindowNodeP)frame).updateView();
    			else
    				((WindowNode)frame).updateView();
    		}
    		if ((ne.getType() & TypesHWC.MASK_LINK) > 0) {
    			if (mySystem.getMySettings().isPrediction())
    				((WindowLinkP)frame).updateView();
    			else
    				((WindowLink)frame).updateView();
    		}
			if ((ne.getType() & TypesHWC.MASK_SENSOR) > 0)
    			((WindowSensor)frame).updateView();
    	}
    	frames = win2pth.keySet();
    	for (iter = frames.iterator(); iter.hasNext();) {
    		JInternalFrame frame = iter.next();
    		if (mySystem.getMySettings().isPrediction())
    			((WindowPathP)frame).updateView();
    		else
    			((WindowPath)frame).updateView();
    	}
    	((EventTableModel)((TableSorter)actionPane.getEventsTable().getModel()).getTableModel()).updateData();
    	return;
    }
    
    /**
     * Resets view in all frames.
     */
    public synchronized void resetView() {
    	Set<JInternalFrame> frames = win2ne.keySet();
    	Iterator<JInternalFrame> iter;
    	for (iter = frames.iterator(); iter.hasNext();) {
    		JInternalFrame frame = iter.next();
    		AbstractNetworkElement ne = win2ne.get(frame);
    		if ((ne.getType() & TypesHWC.MASK_NETWORK) > 0) {
    			if (mySystem.getMySettings().isPrediction())
    				((WindowNetworkP)frame).resetView();
    			else
    				((WindowNetwork)frame).resetView();
    		}
    		if ((ne.getType() & TypesHWC.MASK_NODE) > 0) {
    			if (mySystem.getMySettings().isPrediction())
    				((WindowNodeP)frame).resetView();
    			else
    				((WindowNode)frame).resetView();
    		}
    		if ((ne.getType() & TypesHWC.MASK_LINK) > 0) {
    			if (mySystem.getMySettings().isPrediction())
    				((WindowLinkP)frame).resetView();
    			else
    				((WindowLink)frame).resetView();
    		}
    	}
    	frames = win2pth.keySet();
    	for (iter = frames.iterator(); iter.hasNext();) {
    		JInternalFrame frame = iter.next();
    		if (mySystem.getMySettings().isPrediction())
    			((WindowPathP)frame).resetView();
    		else
    			((WindowPath)frame).resetView();
    	}
    	((EventTableModel)((TableSorter)actionPane.getEventsTable().getModel()).getTableModel()).updateData();
    	return;
    }
    
    /**
     * Updates minimum flow.
     */
    public double updateMinFlow(double x) {
    	minFlow = Math.min(minFlow, x);
    	return minFlow;
    }
    
    /**
     * Updates maximum flow.
     */
    public double updateMaxFlow(double x) {
    	maxFlow = Math.max(maxFlow, x);
    	return maxFlow;
    }
    
    /**
     * Updates minimum density.
     */
    public double updateMinDensity(double x) {
    	minDensity = Math.min(minDensity, x);
    	return minDensity;
    }
    
    /**
     * Updates maximum density.
     */
    public double updateMaxDensity(double x) {
    	maxDensity = Math.max(maxDensity, x);
    	return maxDensity;
    }
    
    /**
     * Updates minimum speed.
     */
    public double updateMinSpeed(double x) {
    	minSpeed = Math.min(minSpeed, x);
    	return minSpeed;
    }
    
    /**
     * Updates maximum speed.
     */
    public double updateMaxSpeed(double x) {
    	maxSpeed = Math.max(maxSpeed, x);
    	return maxSpeed;
    }
    
    /**
     * Resets ranges for flow, density and speed. 
     */
    public void resetDataRanges() {
    	minFlow = Double.MAX_VALUE;
		maxFlow = 0.0;
		minDensity = Double.MAX_VALUE;
		maxDensity = 0.0;
		minSpeed = Double.MAX_VALUE;
		maxSpeed = 0.0;
    	return;
    }
    
    /**
     * Stops running threads.
     */
    public void stop() {
    	actionPane.stop();
    }
    
    /**
     * Opens a .csv file to save numerical data.
     * @return fos output stream.
     */
    public PrintStream openCSV() {
    	PrintStream fos = null;
    	String ext = "csv";
		JFileFilter filter = new JFileFilter();
		filter.addType(ext);
		filter.setDescription("Comma separated values (*." + ext + ")");
		JFileChooser fc = new JFileChooser("Save Data");
		fc.setFileFilter(filter);
		fc.setCurrentDirectory(currentDir);
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			currentDir = fc.getCurrentDirectory();
			File fp = fc.getSelectedFile();
			if (fp.exists()) {
				int res = JOptionPane.showConfirmDialog(this, "File '" + fp.getName() + "' exists. Overwrite?", "Confirmation", JOptionPane.YES_NO_OPTION);
				if (res == JOptionPane.NO_OPTION)
					return fos;
			}
			try {
				String fpath = fp.getAbsolutePath();
				if (!fp.getName().endsWith(ext))
					fpath += "." + ext;
				fos = new PrintStream(new FileOutputStream(fpath));
			}
			catch(Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
		}
    	return fos;
    }
    
    
    /**
     * Renderer class for displaying icons.
     */
    private class MyRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = -6313767474632662236L;

		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    		Object myobj = ((DefaultMutableTreeNode)value).getUserObject();
    		String name = myobj.getClass().getSimpleName();
    		ImageIcon icon = UtilGUI.createImageIcon("/icons/" + name + ".gif");
    		if (icon != null)
    			setIcon(icon);
    		return this;
    	}
    }
    
}