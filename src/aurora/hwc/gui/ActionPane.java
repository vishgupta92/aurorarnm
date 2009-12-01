/**
 * @(#)ActionPane.java
 */

package aurora.hwc.gui;

import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import aurora.hwc.*;
import aurora.util.*;


/**
 * Implementation of action pane split vertically into
 * a desktop and table panes.
 * @author Alex Kurzhanskiy
 * @version $Id: ActionPane.java,v 1.1.2.11.4.2 2009/10/18 05:17:14 akurzhan Exp $
 */
public final class ActionPane extends JPanel {
	private static final long serialVersionUID = -2436965790211986294L;
	private ContainerHWC mySystem = null;
	private TreePane tree = null;
	private JDesktopPane desktopPane = new JDesktopPane();
    private JTabbedPane tablePane = new JTabbedPane();
    private JTable fevents;
    protected JTextArea console = new JTextArea();
    protected BufferedReader iis;
    protected boolean stopped = false;
   
    
    public ActionPane() { }
    public ActionPane(ContainerHWC ctnr, TreePane tr) {
        super(new GridLayout(1,0));
        mySystem = ctnr;
        tree = tr;
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(desktopPane);
        splitPane.setBottomComponent(tablePane);
        Dimension minimumSize = new Dimension(50, 50);
        desktopPane.setMinimumSize(minimumSize);
        desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        desktopPane.setBackground(Color.GRAY);
        tablePane.setMinimumSize(minimumSize);
        tablePane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        TableSorter sorter = new TableSorter(new EventTableModel(mySystem.getMyEventManager()));
        fevents = new JTable(sorter);
        sorter.setTableHeader(fevents.getTableHeader());
        fevents.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        fevents.addMouseListener(new MouseAdapter() { 
      	  public void mouseClicked(MouseEvent e) { 
      	    if (e.getClickCount() == 2) {
      	    	TreePane tr = null;
      	    	int row = fevents.rowAtPoint(new Point(e.getX(), e.getY()));
      	    	if (fevents.columnAtPoint(new Point(e.getX(), e.getY())) == 2)
      	    		tr = tree;
      	    	mySystem.getMyStatus().setStopped(true);
      	    	try {
      	    		TableSorter ts = (TableSorter)fevents.getModel();
      	    		((EventTableModel)ts.getTableModel()).eventSelected(ts.modelIndex(row), mySystem.getMyNetwork(), tr);
      	    	}
      	    	catch(Exception excpt) { }
      	    }
      	    return;
      	  }
        });
        tablePane.addTab("Events", new JScrollPane(fevents));
        console.setFont(new Font("Helvetica", Font.PLAIN, 11));
        tablePane.addTab("Console", new JScrollPane(console));
        catchIO();
        splitPane.setDividerLocation((int)Math.round(0.75*mySystem.getMySettings().getWindowSize().getHeight()));
        add(splitPane);
    }
    
    public void catchIO() {
    	try {
        	PipedInputStream is = new PipedInputStream();
        	PipedOutputStream os = new PipedOutputStream(is);
        	iis = new BufferedReader(new InputStreamReader(is, "ISO8859_1"));
        	mySystem.getMySettings().setOutputStream(new PrintStream(os));
        	System.setOut(mySystem.getMySettings().getOutputStream());
        	//System.setErr(mySystem.getMySettings().getOutputStream());
        }
        catch(Exception e) {
        	JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
        }
        new Thread() {
        	public void run() {
        		try {
        			String line;
        			while(((line = iis.readLine()) != null) && (!stopped)) {
        				console.append(line + "\n");
        				console.setCaretPosition(console.getText().length()); // put caret at the end
        			}
        		}
        		catch(Exception e) {
        			catchIO();
        		}
        	}
        }.start();
        return;
    }
    
    /**
     * Returns the table of events.
     */
    public JTable getEventsTable() {
    	return fevents;
    }

    /**
     * Returns the desktop subframe.
     */
    JDesktopPane getDesktopPane() {
    	return desktopPane;
    }
    
    /**
     * Stops running threads.
     */
    public void stop() {
    	stopped = true;
    	// To avoid memory leak, write something to stdout
    	System.out.println("Cleanup!"); 
    }
    
}