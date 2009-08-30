/**
 * 
 */
package aurora.hwc.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import aurora.*;
import aurora.hwc.*;
import aurora.hwc.control.*;

/**
 * @author gomes
 *
 */
public class WindowMonitor extends JInternalFrame {
	private static final long serialVersionUID = 3976086040334830890L;

	protected AbstractContainer mySystem;
	protected MonitorControllerHWC myMonitor;
	protected TreePane treePane;
	protected JTabbedPane tabbedPane = new JTabbedPane();

	// monitor tab
	protected Box monitorPanel = Box.createVerticalBox();
	protected linkTabModel linkTM = new linkTabModel();
	
	// controller tab
	protected Box cntrlPanel = Box.createVerticalBox();
	
	public WindowMonitor() { }
	public WindowMonitor(AbstractContainer ctnr, MonitorControllerHWC mn, TreePane tp) {
		super(mn.getDescription(), true, true, true, true);
		mySystem = ctnr;
		myMonitor = mn;
		treePane = tp;
		setSize(600, 400);
		int n = treePane.getInternalFrameCount();
		setLocation(20*n, 20*n);
		AdapterWindowMonitor listener = new AdapterWindowMonitor();
		addInternalFrameListener(listener);
		addComponentListener(listener);
		fillMonitorPanel();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.add("Monitor", new JScrollPane(monitorPanel));
		
		try {
			Class obj = myMonitor.getMyController().getClass();
			Class c = Class.forName(obj.getPackage().getName() + ".Panel" + obj.getSimpleName());
			AbstractControllerPanel controlPanel = (AbstractControllerPanel)c.newInstance();
			controlPanel.initialize(myMonitor.getMyController(), null);
			tabbedPane.add("Controller",new JScrollPane(controlPanel));
		}
		catch(Exception e) {
			JOptionPane.showMessageDialog(this, "Cannot open controller window.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		setContentPane(tabbedPane);
	}
	
	/**
	 * Generates Controller tab.
	 */
	//private void fillCntrlPanel() {
	//	myMonitor.getMyController().fillMonitorPanel(cntrlPanel);
	//	return;
	//}
	
	/**
	 * Generates Monitor tab.
	 */
	private void fillMonitorPanel() {

		JPanel links = new JPanel(new GridLayout(1, 0));
		links.setBorder(BorderFactory.createTitledBorder("Links"));
		final JTable linktab = new JTable(linkTM);
		linktab.setPreferredScrollableViewportSize(new Dimension(250, 70));
		links.add(new JScrollPane(linktab));
		monitorPanel.add(links);
		return;
	}
	
	/**
	 * Updates displayed data.
	 */
	public void updateView() {
		if (mySystem.getMyStatus().isStopped())
			return;
		return;
	}
	
	/**
	 * Resets displayed data.
	 */
	public void resetView() {
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
	 * Class needed for proper closing of internal monitor windows.
	 */
	private class AdapterWindowMonitor extends InternalFrameAdapter implements ComponentListener {
		
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
	
	/**
	 * Class needed for displaying table of monitored links.
	 */
	private class linkTabModel extends AbstractTableModel {
		private static final long serialVersionUID = -8310974555648802182L;

		public String getColumnName(int col) {
	        String buf = null;
	        switch(col) {
	        case 0:	buf = ""; break;
	        case 1: buf = "Link ID"; break;
	        case 2: buf = "Description"; break;
	        }
			return buf;
	    }
		
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return myMonitor.getNumLinks();
		}

		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row >= getRowCount()) || (column < 0) || (column > getColumnCount()))
				return null;
			if (column == 0)
				return row+1;
			if (column == 1)
				return myMonitor.getLinkID(row);
			if (column == 2)
				return myMonitor.getLinkString(row);
			return null;
		}
		
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
		public void setValueAt(Object value, int row, int column) {
			return;
		}
		
	}
	
}