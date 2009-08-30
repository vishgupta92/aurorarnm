/**
 * @(#)TreePane.java
 */

package aurora.hwc.config;

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
 * @author Alex Kurzhanskiy
 * @version $Id: TreePane.java,v 1.1.4.1.2.4.2.3 2009/08/26 01:15:33 akurzhan Exp $
 */
public class TreePane extends JPanel {
	private static final long serialVersionUID = 3976137906223422920L;
	
	private ContainerHWC mySystem = null;
	private int linkFilter = 0;
    private JTree tree;
    private HashMap<AbstractNetworkElement, JInternalFrame> ne2win = new HashMap<AbstractNetworkElement, JInternalFrame>();
    private HashMap<JInternalFrame, Integer> win2netype = new HashMap<JInternalFrame, Integer>();
    private HashMap<AbstractNetworkElement, DefaultMutableTreeNode> ne2tn = new HashMap<AbstractNetworkElement, DefaultMutableTreeNode>();
    protected MainPane mainPane = null;
	protected ActionPane actionPane;

    
    public TreePane() { }
    public TreePane(ContainerHWC ctnr, MainPane mp) {
        super(new GridLayout(1,0));
        mySystem = ctnr;
        mainPane = mp;
        linkFilter = defaultLinkFilter();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(mySystem.getMyNetwork());
        ne2tn.put(mySystem.getMyNetwork(), root);
        fillTreeData(root, mySystem.getMyNetwork());
        tree = new JTree(root);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.addKeyListener(new KeyListener() {
        	public void keyPressed(KeyEvent evt) {
        		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
        			TreePath ptharr[] = tree.getSelectionPaths();
        			Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
        			for (int i = 0; i < ptharr.length; i++)
        				if (ptharr[i] != null) {
        					Object comp = ((DefaultMutableTreeNode)ptharr[i].getLastPathComponent()).getUserObject();
        					if (comp instanceof AbstractNetworkElement)
        						nelist.add((AbstractNetworkElement)comp);
        				}
        			actionSelected(nelist, true, true);
        		}
        	}
			public void keyReleased(KeyEvent e) { }
			public void keyTyped(KeyEvent e) { }
        });
        tree.addMouseListener(new MouseAdapter() { 
        	  public void mouseClicked(MouseEvent e) { 
        	    if (e.getClickCount() == 2) {
        	      TreePath path = tree.getPathForLocation(e.getX(), e.getY()); 
        	      if (path != null) { 
        	    	 Object comp = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
        	    	 if (comp instanceof AbstractNetworkElement) {
        	    		 Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
        	    		 nelist.add((AbstractNetworkElement)comp);
        	    		 actionSelected(nelist, true, true);
        	    	 }
        	      }
        	    }
        	  } 
        	}); 
        ToolTipManager.sharedInstance().registerComponent(tree);
        tree.setCellRenderer(new MyRenderer());
        tree.collapseRow(0);
        JScrollPane treeView = new JScrollPane(tree);
        actionPane = new ActionPane(this, mySystem);
        mySystem.getMyNetwork().setVerbose(true);
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
     * Returns main pane.
     */
    public MainPane getMainPane() {
    	return mainPane;
    }
    
    /**
     * Opens Control Monitor internal frame.
     */
    private synchronized void openWindowMonitorController(AbstractNetworkElement ne, ImageIcon icon) {
    	WindowMonitorController wn = new WindowMonitorController(mySystem, (MonitorControllerHWC)ne, this);
		wn.setFrameIcon(icon);
		wn.setVisible(true);
		actionPane.getDesktopPane().add(wn);
		try {
			wn.setSelected(true);
		}
		catch(java.beans.PropertyVetoException e) { }
		ne2win.put(ne, wn);
		win2netype.put(wn, TypesHWC.MASK_MONITOR_CONTROLLER);
    	return;
    }
    
    /**
     * Opens Zipper Monitor internal frame.
     */
    private synchronized void openWindowMonitorZipper(AbstractNetworkElement ne, ImageIcon icon) {
    	WindowMonitorZipper wn = new WindowMonitorZipper(mySystem, (MonitorZipperHWC)ne, this);
		wn.setFrameIcon(icon);
		wn.setVisible(true);
		actionPane.getDesktopPane().add(wn);
		try {
			wn.setSelected(true);
		}
		catch(java.beans.PropertyVetoException e) { }
		ne2win.put(ne, wn);
		win2netype.put(wn, TypesHWC.MASK_MONITOR_ZIPPER);
    	return;
    }
    
    /**
     * Opens Network internal frame.
     */
    private synchronized void openWindowNetwork(AbstractNetworkElement ne, ImageIcon icon) {
    	WindowNetwork wn = new WindowNetwork(mySystem, (AbstractNodeComplex)ne, this);
		wn.setFrameIcon(icon);
		wn.setVisible(true);
		actionPane.getDesktopPane().add(wn);
		try {
			wn.setSelected(true);
		}
		catch(java.beans.PropertyVetoException e) { }
		ne2win.put(ne, wn);
		win2netype.put(wn, TypesHWC.MASK_NETWORK);
    	return;
    }
    
    /**
     * Opens Node internal frame.
     */
    private synchronized void openWindowNode(Vector<AbstractNetworkElement> nodes, ImageIcon icon) {
    	if ((nodes == null) || nodes.isEmpty())
    		return;
    	while (!nodes.isEmpty()) {
    		Vector<AbstractNetworkElement> sublist = new Vector<AbstractNetworkElement>();
    		AbstractNetworkElement ne = nodes.firstElement(); 
    		nodes.remove(0);
    		sublist.add(ne);
    		int m = ne.getPredecessors().size();
    		int n = ne.getSuccessors().size();
    		int ii = 0;
    		while (ii < nodes.size()) {
    			AbstractNetworkElement x = nodes.get(ii); 
    			if ((m == x.getPredecessors().size()) && (n == x.getSuccessors().size())) {
    				nodes.remove(ii);
    				sublist.add(x);
    			}
    			else
    				ii++;
    		}
    		WindowNode wn = new WindowNode(mySystem, sublist, this);
    		wn.setFrameIcon(icon);
    		wn.setVisible(true);
    		actionPane.getDesktopPane().add(wn);
    		try {
    			wn.setSelected(true);
    		}
    		catch(java.beans.PropertyVetoException e) { }
    		for (int i = 0; i < sublist.size(); i++)
    			ne2win.put(sublist.get(i), wn);
    		win2netype.put(wn, TypesHWC.MASK_NODE);
    	}
    	return;
    }
    
    /**
     * Opens Link internal frame.
     */
    private synchronized void openWindowLink(Vector<AbstractNetworkElement> links, ImageIcon icon) {
    	if ((links == null) || links.isEmpty())
    		return;
    	WindowLink wn = new WindowLink(mySystem, links, this);
		wn.setFrameIcon(icon);
		wn.setVisible(true);
		actionPane.getDesktopPane().add(wn);
		try {
			wn.setSelected(true);
		}
		catch(java.beans.PropertyVetoException e) { }
		for (int i = 0; i < links.size(); i++)
			ne2win.put(links.get(i), wn);
		win2netype.put(wn, TypesHWC.MASK_LINK);
    	return;
    }

    /**
     * Action triggered by selecting Network Element from the tree.
     */
    public synchronized void actionSelected(Vector<AbstractNetworkElement> nelist, boolean openwin, boolean selflag) {
    	if ((nelist == null) || (nelist.isEmpty()))
    		return;
    	TreePath[] ptharr = new TreePath[nelist.size()];
    	Vector<AbstractNetworkElement> nodes = new Vector<AbstractNetworkElement>();
    	Vector<AbstractNetworkElement> links = new Vector<AbstractNetworkElement>();
    	JInternalFrame frame = ne2win.get(nelist.get(0));
    	if (frame != null) {
    		try {
    			frame.setIcon(false);
    			frame.setSelected(true);
    		}
    		catch(java.beans.PropertyVetoException e) { }
    		//return;
    	}
    	for (int i = 0; i < nelist.size(); i++) {
    		DefaultMutableTreeNode myTN = ne2tn.get(nelist.get(i));
    		if (myTN == null) {
    			JOptionPane.showMessageDialog(this, TypesHWC.typeString(nelist.get(i).getType()) + " '" + nelist.get(i).toString() + "' does not exist.", "Warning!", JOptionPane.WARNING_MESSAGE);
    			continue;
    		}
    		if (ne2win.get(nelist.get(i)) != null)
    			continue;
    		if ((nelist.get(i).getType() == TypesHWC.MASK_MONITOR_CONTROLLER)) {
    	    	ImageIcon icon = UtilGUI.createImageIcon("/icons/monitoredit.jpg");
    	    	openWindowMonitorController(nelist.get(i), icon);
    		}
    		if ((nelist.get(i).getType() == TypesHWC.MASK_MONITOR_ZIPPER)) {
    	    	ImageIcon icon = UtilGUI.createImageIcon("/icons/monitoredit.jpg");
    	    	openWindowMonitorZipper(nelist.get(i), icon);
    		}
    		if ((nelist.get(i).getType() & TypesHWC.MASK_NETWORK) > 0) {
    	    	ImageIcon icon = UtilGUI.createImageIcon("/icons/networkedit.jpg");
    	    	openWindowNetwork(nelist.get(i), icon);
    		}
    		if ((nelist.get(i).getType() & TypesHWC.MASK_NODE) > 0)
    			nodes.add(nelist.get(i));
    		if ((nelist.get(i).getType() & TypesHWC.MASK_LINK) > 0)
    			links.add(nelist.get(i));
    		if (ne2tn.get(nelist.get(i)) != null)
    			ptharr[i] = new TreePath(myTN.getPath());
    	}
    	tree.setSelectionPaths(ptharr);
    	tree.scrollPathToVisible(ptharr[0]);
    	if (selflag) {
    		Set<JInternalFrame> frames = win2netype.keySet();
    		Iterator<JInternalFrame> iter;
    		for (iter = frames.iterator(); iter.hasNext();) {
    			JInternalFrame frm = iter.next();
    			if ((win2netype.get(frm) & TypesHWC.MASK_NETWORK) > 0)
    				((WindowNetwork)frm).setPicked(nelist);
    		}
    	}
    	if (!openwin)
    		return;
    	if (!nodes.isEmpty()) {
    		ImageIcon icon = UtilGUI.createImageIcon("/icons/nodeedit.jpg");
    		openWindowNode(nodes, icon);
    	}
    	if (!links.isEmpty()) {
    		ImageIcon icon = UtilGUI.createImageIcon("/icons/linkedit.jpg");
    		openWindowLink(links, icon);
    	}
    	return;
    }
    
    /**
     * Removes specified frame from the list.
     * @param frame
     */
    public synchronized void removeFrame(JInternalFrame frame, Vector<AbstractNetworkElement> nelist) {
    	if ((frame == null) || (nelist == null) || nelist.isEmpty())
    		return;
    	win2netype.remove(frame);
    	for (int i = 0; i < nelist.size(); i++)
    		ne2win.remove(nelist.get(i));
    	System.gc();
    	return;
    }
    
    /**
     * Resizes all internal frames according to new desktop size.
     */
    public synchronized void resizeFrames() {
    	Set<JInternalFrame> frames = win2netype.keySet();
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
    	return win2netype.size();
    }
    
    /**
     * Returns Link filter.
     */
    public int getLinkFilter() {
    	return linkFilter;
    }
    
    /**
     * Returns default Link filter.
     */
    public int defaultLinkFilter() {
    	int[] ltypes = TypesHWC.linkTypeArray();
    	int filter = 0;
    	for (int i = 0; i < ltypes.length; i++)
    		filter |= ltypes[i];
    	return filter;
    }
    
    /**
     * Sets Link filter.
     * @param filter.
     */
    public synchronized void setLinkFilter(int filter) {
    	linkFilter = filter;
    	resetView();
    	return;
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
    	list = new DefaultMutableTreeNode("Nodes");
    	root.add(list);
    	for (i = 0; i < nodes.size(); i++) {
    		item = new DefaultMutableTreeNode(nodes.get(i));
    		if (!nodes.get(i).isSimple())
    			fillTreeData(item, (AbstractNodeComplex)nodes.get(i));
    		list.add(item);
    		ne2tn.put(nodes.get(i), item);
        }
    	Vector<AbstractLink> links = ntwk.getLinks();
    	list = new DefaultMutableTreeNode("Links");
    	root.add(list);
    	for (i = 0; i < links.size(); i++) {
    		item = new DefaultMutableTreeNode(links.get(i));
    		list.add(item);
    		ne2tn.put(links.get(i), item);
    	}
    	Vector<AbstractMonitor> monitors = ntwk.getMonitors();
    	list = new DefaultMutableTreeNode("Monitors");
    	root.add(list);
    	for (i = 0; i < monitors.size(); i++) {
    		item = new DefaultMutableTreeNode(monitors.get(i));
    		list.add(item);
    		ne2tn.put(monitors.get(i), item);
    	}
    	return;
    }
    
    /**
     * Update and set selected given Network Element.
     * @param ne Network Element.
     */
    public void updateNE(AbstractNetworkElement ne) {
    	if (ne == null)
    		return;
    	DefaultMutableTreeNode tn = ne2tn.get(ne);
    	if (tn == null)
    		return;
    	((DefaultTreeModel)tree.getModel()).reload(tn);
    	TreePath tnpath = new TreePath(tn.getPath());
    	tree.setSelectionPath(tnpath);
    	tree.scrollPathToVisible(tnpath);
    	return;
    }
    
    /**
     * Adds Node component to the tree.
     * @param nd new Node.
     * @param ntwk network to which new Node must belong.
     */
    public void addNodeComponent(AbstractNode nd, AbstractNodeComplex ntwk) {
    	DefaultMutableTreeNode grndPrnt = ne2tn.get(ntwk);
    	if ((grndPrnt == null) || (nd == null))
    		return;
    	DefaultMutableTreeNode tn = new DefaultMutableTreeNode(nd);
    	DefaultMutableTreeNode prnt = (DefaultMutableTreeNode)grndPrnt.getFirstChild(); // should be 'Nodes' subfolder
    	if (prnt == null)
    		return;
    	if ((nd.getType() & TypesHWC.MASK_NETWORK) > 0)
    		fillTreeData(tn, (AbstractNodeComplex)nd);
    	prnt.add(tn);
    	((DefaultTreeModel)tree.getModel()).reload(prnt);
    	ne2tn.put(nd, tn);
    	TreePath tnpath = new TreePath(tn.getPath());
    	tree.setSelectionPath(tnpath);
    	tree.scrollPathToVisible(tnpath);
    	JInternalFrame win = ne2win.get(ntwk);
    	if (win != null) {
    		Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
    		nelist.add(nd);
    		((WindowNetwork)win).refreshNetwork();
    		((WindowNetwork)win).setPicked(nelist);
    	}
    	mySystem.getMyStatus().setSaved(false);
    	return;
    }
    
    /**
     * Adds Link component to the tree.
     * @param lk new Link.
     * @param ntwk network to which new Link must belong.
     */
    public void addLinkComponent(AbstractLink lk, AbstractNodeComplex ntwk) {
    	DefaultMutableTreeNode grndPrnt = ne2tn.get(ntwk);
    	if ((grndPrnt == null) || (lk == null))
    		return;
    	DefaultMutableTreeNode tn = new DefaultMutableTreeNode(lk);
    	DefaultMutableTreeNode prnt = (DefaultMutableTreeNode)grndPrnt.getChildAt(1); // should be 'Links' subfolder
    	if (prnt == null)
    		return;
    	prnt.add(tn);
    	((DefaultTreeModel)tree.getModel()).reload(prnt);
    	ne2tn.put(lk, tn);
    	TreePath tnpath = new TreePath(tn.getPath());
    	tree.setSelectionPath(tnpath);
    	tree.scrollPathToVisible(tnpath);
    	JInternalFrame win = ne2win.get(ntwk);
    	if (win != null) {
    		Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
    		nelist.add(lk);
    		((WindowNetwork)win).refreshNetwork();
    		((WindowNetwork)win).setPicked(nelist);
    	}
    	mySystem.getMyStatus().setSaved(false);
    	return;
    }
    
    /**
     * Adds Monitor component to the tree.
     * @param mon new Monitor.
     * @param ntwk network to which new Monitor must belong.
     */
    public void addMonitorComponent(AbstractMonitor mon, AbstractNodeComplex ntwk) {
    	DefaultMutableTreeNode grndPrnt = ne2tn.get(ntwk);
    	if ((grndPrnt == null) || (mon == null))
    		return;
    	DefaultMutableTreeNode tn = new DefaultMutableTreeNode(mon);
    	DefaultMutableTreeNode prnt = (DefaultMutableTreeNode)grndPrnt.getChildAt(2); // should be 'Monitors' subfolder
    	if (prnt == null)
    		return;
    	prnt.add(tn);
    	((DefaultTreeModel)tree.getModel()).reload(prnt);
    	ne2tn.put(mon, tn);
    	TreePath tnpath = new TreePath(tn.getPath());
    	tree.setSelectionPath(tnpath);
    	tree.scrollPathToVisible(tnpath);
    	JInternalFrame win = ne2win.get(ntwk);
    	if (win != null) {
    		Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
    		nelist.add(mon);
    		((WindowNetwork)win).refreshNetwork();
    		((WindowNetwork)win).setPicked(nelist);
    	}
    	mySystem.getMyStatus().setSaved(false);
    	return;
    }
    
    /**
     * Modifies specified Nodes in the tree.
     * @param nelist vector of Network Elements to be modified.
     */
    public void modifyNodeComponents(Vector<AbstractNetworkElement> nelist) {
    	if ((nelist == null) || (nelist.isEmpty()))
    		return;
    	DefaultMutableTreeNode prnt = null;
    	DefaultMutableTreeNode tn = null;
    	DefaultMutableTreeNode newtn = null;
    	int i = nelist.size();
    	TreePath[] ptharr = new TreePath[nelist.size()];
    	while (--i >= 0) {
    		AbstractNode nd = (AbstractNode)nelist.get(i);
    		AbstractNode newnd = nd.getMyNetwork().getNodeById(nd.getId());
    		newtn = new DefaultMutableTreeNode(newnd);
    		tn = ne2tn.get(nd);
    		if ((newnd == null) || (tn == null))
    			continue;
    		prnt = (DefaultMutableTreeNode)tn.getParent();
    		if (prnt == null)
    			continue;
    		ne2tn.remove(nd);
    		ne2tn.put(newnd, newtn);
    		int idx = ((DefaultTreeModel)tree.getModel()).getIndexOfChild(prnt, tn);
    		((DefaultTreeModel)tree.getModel()).removeNodeFromParent(tn);
    		prnt.insert(newtn, idx);
    		((DefaultTreeModel)tree.getModel()).reload(prnt);
    		ptharr[i] = new TreePath(newtn.getPath());
    	}
    	if (newtn == null)
    		return;
    	tree.setSelectionPaths(ptharr);
    	tree.scrollPathToVisible(ptharr[0]);
    	resetView();
    	return;
    }
    
    /**
     * Modifies specified Links in the tree.
     * @param nelist vector of Network Elements to be modified.
     */
    public void modifyLinkComponents(Vector<AbstractNetworkElement> nelist) {
    	if ((nelist == null) || (nelist.isEmpty()))
    		return;
    	DefaultMutableTreeNode prnt = null;
    	DefaultMutableTreeNode tn = null;
    	DefaultMutableTreeNode newtn = null;
    	int i = nelist.size();
    	TreePath[] ptharr = new TreePath[nelist.size()];
    	while (--i >= 0) {
    		AbstractLink lk = (AbstractLink)nelist.get(i);
    		AbstractLink newlk = lk.getMyNetwork().getLinkById(lk.getId());
    		newtn = new DefaultMutableTreeNode(newlk);
    		tn = ne2tn.get(lk);
    		if ((newlk == null) || (tn == null))
    			continue;
    		prnt = (DefaultMutableTreeNode)tn.getParent();
    		if (prnt == null)
    			continue;
    		ne2tn.remove(lk);
    		ne2tn.put(newlk, newtn);
    		int idx = ((DefaultTreeModel)tree.getModel()).getIndexOfChild(prnt, tn);
    		((DefaultTreeModel)tree.getModel()).removeNodeFromParent(tn);
    		prnt.insert(newtn, idx);
    		((DefaultTreeModel)tree.getModel()).reload(prnt);
    		ptharr[i] = new TreePath(newtn.getPath());
    	}
    	if (newtn == null)
    		return;
    	tree.setSelectionPaths(ptharr);
    	tree.scrollPathToVisible(ptharr[0]);
    	resetView();
    	return;
    }
    
    /**
     * Modifies specified Monitors in the tree.
     * @param nelist vector of Network Elements to be modified.
     */
    public void modifyMonitorComponents(Vector<AbstractNetworkElement> nelist) {
    	if ((nelist == null) || (nelist.isEmpty()))
    		return;
    	DefaultMutableTreeNode prnt = null;
    	DefaultMutableTreeNode tn = null;
    	DefaultMutableTreeNode newtn = null;
    	int i = nelist.size();
    	TreePath[] ptharr = new TreePath[nelist.size()];
    	while (--i >= 0) {
    		AbstractMonitor mn = (AbstractMonitor)nelist.get(i);
    		AbstractMonitor newmn = mn.getMyNetwork().getMonitorById(mn.getId());
    		newtn = new DefaultMutableTreeNode(newmn);
    		tn = ne2tn.get(mn);
    		if ((newmn == null) || (tn == null))
    			continue;
    		prnt = (DefaultMutableTreeNode)tn.getParent();
    		if (prnt == null)
    			continue;
    		ne2tn.remove(mn);
    		ne2tn.put(newmn, newtn);
    		int idx = ((DefaultTreeModel)tree.getModel()).getIndexOfChild(prnt, tn);
    		((DefaultTreeModel)tree.getModel()).removeNodeFromParent(tn);
    		prnt.insert(newtn, idx);
    		((DefaultTreeModel)tree.getModel()).reload(prnt);
    		ptharr[i] = new TreePath(newtn.getPath());
    	}
    	if (newtn == null)
    		return;
    	tree.setSelectionPaths(ptharr);
    	tree.scrollPathToVisible(ptharr[0]);
    	resetView();
    	return;
    }
    
    /**
     * Deletes network from the tree.
     * @param ntwk Network object.
     */
    private void deleteNetwork(AbstractNodeComplex ntwk) {
    	if (ntwk == null)
    		return;
    	Vector<AbstractNode> nodes = ntwk.getNodes();
    	for (int i = 0; i < nodes.size(); i++)
    		if ((nodes.get(i).getType() & TypesHWC.MASK_NETWORK) > 0)
    			deleteNetwork((AbstractNodeComplex)nodes.get(i));
    		else {
    			ne2tn.remove(nodes.get(i));
    			ne2win.remove(nodes.get(i));
    		}
    	Vector<AbstractLink> links = ntwk.getLinks();
    	for (int i = 0; i < links.size(); i++) {
    		ne2tn.remove(links.get(i));
    		ne2win.remove(links.get(i));
    	}
    	Vector<AbstractMonitor> monitors = ntwk.getMonitors();
    	for (int i = 0; i < monitors.size(); i++) {
    		ne2tn.remove(monitors.get(i));
    		ne2win.remove(monitors.get(i));
    	}
    	return;
    }
    
    /**
     * Deletes specified Network Elements from the tree.
     * @param nelist vector of Network Elements to be deleted.
     */
    public void deleteComponents(Vector<AbstractNetworkElement> nelist) {
    	if ((nelist == null) || nelist.isEmpty())
    		return;
    	boolean unsaved = false;
    	String dstr = "Are you sure you want to delete ";
    	if (nelist.size() == 1)
    		dstr += TypesHWC.typeString(nelist.firstElement().getType()) + " '" + nelist.firstElement() + "'?";
    	else
    		dstr += "the selected Network Elements?";
    	if (JOptionPane.showConfirmDialog(this, dstr, "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return;
    	while (!nelist.isEmpty()) {
    		AbstractNetworkElement ne = nelist.firstElement();
    		nelist.remove(0);
    		if ((ne2win.get(ne) != null) ||
    			(((ne.getType() & TypesHWC.MASK_NETWORK) > 0) && ((AbstractNodeComplex)ne).isTop())) {
    			String buf = "Cannot delete " + TypesHWC.typeString(ne.getType()) + " '" + ne + "'.\nIt is open for editing.";
    			JOptionPane.showMessageDialog(this, buf, "Warning!", JOptionPane.WARNING_MESSAGE);
    			continue;
    		}
    		if ((ne.getType() & TypesHWC.MASK_NETWORK) > 0)
    			deleteNetwork((AbstractNodeComplex)ne); 
    		unsaved = true;
    		ne.getTop().deleteNetworkElement(ne);
    		DefaultMutableTreeNode tn = ne2tn.get(ne);
    		((DefaultTreeModel)tree.getModel()).removeNodeFromParent(tn);
    		ne2tn.remove(ne);
    	}
    	if (unsaved) {
    		mySystem.getMyStatus().setSaved(false);
    		Set<JInternalFrame> frames = win2netype.keySet();
    		Iterator<JInternalFrame> iter;
    		for (iter = frames.iterator(); iter.hasNext();) {
    			JInternalFrame frm = iter.next();
    			if ((win2netype.get(frm) & TypesHWC.MASK_NETWORK) > 0)
    				((WindowNetwork)frm).refreshNetwork();
    		}
    	}
    	return;
    }
    
    /**
     * Updates configuration panels in network windows.
     */
    public synchronized void updateConfig() {
    	/*Set<JInternalFrame> frames = win2ne.keySet();
    	Iterator<JInternalFrame> iter;
    	for (iter = frames.iterator(); iter.hasNext();) {
    		JInternalFrame frame = iter.next();
    		AbstractNetworkElement ne = win2ne.get(frame);
    		if ((ne.getType() & TypesHWC.MASK_NETWORK) > 0)
    			((WindowNetwork)frame).updateView();
    	}*/
    	return;
    }
    
    /**
     * Updates view in all frames.
     */
    public synchronized void updateView() {
    	/*Set<JInternalFrame> frames = win2netype.keySet();
    	Iterator<JInternalFrame> iter;
    	for (iter = frames.iterator(); iter.hasNext();) {
    		JInternalFrame frame = iter.next();
    		AbstractNetworkElement ne = win2ne.get(frame);
    		if ((ne.getType() & TypesHWC.MASK_NETWORK) > 0)
    			((WindowNetwork)frame).updateView();
    		if ((ne.getType() & TypesHWC.MASK_NODE) > 0)
    			((WindowNode)frame).updateView();
    		if ((ne.getType() & TypesHWC.MASK_LINK) > 0)
    			((WindowLink)frame).updateView();
    	}*/
    	return;
    }
    
    /**
     * Resets view in all network windows.
     */
    public synchronized void resetView() {
    	Set<JInternalFrame> frames = win2netype.keySet();
    	Iterator<JInternalFrame> iter;
    	for (iter = frames.iterator(); iter.hasNext();) {
    		JInternalFrame frame = iter.next();
    		if ((win2netype.get(frame) & TypesHWC.MASK_NETWORK) > 0)
    			((WindowNetwork)frame).refreshNetwork();
    	}
    	return;
    }
    
    /**
     * Stops running threads.
     */
    public void stop() {
    	actionPane.stop();
    }
    
    
    /**
     * Renderer class for displaying icons.
     */
    private class MyRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = -7580744360077628535L;

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