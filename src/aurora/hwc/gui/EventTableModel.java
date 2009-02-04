/**
 * @(#)EventTableModel.java 
 */

package aurora.hwc.gui;

import java.util.*;
import javax.swing.table.AbstractTableModel;
import aurora.*;
import aurora.util.Util;


/**
 * Implementation of the displayed event table.
 * @author Alex Kurzhanskiy
 * @version $Id: EventTableModel.java,v 1.1.2.4 2007/04/28 03:59:20 akurzhan Exp $
 */
public final class EventTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -6770730636520245114L;

	private String[] columnNames = {"Time",
									"Type",
									"NE Id",
									"Description",
									"Enabled"};
	private Object[][] data = {
			{new Double(0.0), "Some Event", new Integer(0), "***", new Boolean(true)}
	};
	private EventManager myEventManager;
	
	
	public EventTableModel() { }
	public EventTableModel(EventManager em) {
		myEventManager = em;
		updateData();
	}
	
	
	/**
	 * Checks if the cell is editable.
	 */
	public boolean isCellEditable(int row, int col) {
		if (col == (data[0].length - 1))
			return true;
		return false;
	}
	
	/**
	 * Returns number of columns.
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * Returns number of rows.
	 */
	public int getRowCount() {
		return data.length;
	}
	
	/**
	 * Returns column name.
	 */
	public String getColumnName(int col) {
        return columnNames[col];
    }

	/**
	 * Returns object at given row, given column.
	 * @param rowIndex
	 * @param columnIndex
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data[rowIndex][columnIndex];
	}
	
	/**
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    @SuppressWarnings("unchecked")
	public Class getColumnClass(int c) {
    	Class cl = String.class;
    	try {
    		cl = getValueAt(0, c).getClass();
    	}
    	catch(Exception e) { }
       return cl;
    }
    
    /**
     * Sets value for a given cell.
     */
    public void setValueAt(Object value, int row, int col) {
    	if (col != (data[0].length - 1))
    		return;
    	Vector<AbstractEvent> ev = myEventManager.getAllEvents();
    	if ((row >= 0) && (row < ev.size()))
    		ev.get(row).setEnabled((Boolean)value);
    	updateData();
    	return;
    }

    /**
     * Function called when table data has to be updated.
     */
    public void updateData() {
    	Vector<AbstractEvent> events;
    	events = myEventManager.getAllEvents();
    	int m = events.size();
    	data = new Object[m][columnNames.length];
    	for (int i = 0; i < m; i++) {
    		AbstractEvent evt = events.get(i);
    		data[i][0] = (String)Util.time2string(evt.getTime());
    		data[i][1] = (String)evt.getClass().getSimpleName();
    		data[i][2] = (Integer)evt.getNEID();
    		if (evt != null)
    			data[i][3] = (String)evt.getDescription();
    		else
    			data[i][3] = (String)"-";
    		data[i][4] = new Boolean(evt.isEnabled());
    	}
    	fireTableDataChanged();
    	return;
    }
    
    /**
     * Function called when an event is selected.
     * @param idx row index
     */
    public void eventSelected(int idx, AbstractNodeComplex ntwk) {
    	AbstractEvent evt = myEventManager.getEvent(idx);
    	if (evt == null)
    		return;
    	int neid = evt.getNEID();
    	AbstractNetworkElement ne;
    	ne = ntwk.getMonitorById(neid);
    	if (ne == null)
    		ne = ntwk.getNodeById(neid);
    	if (ne == null)
    		ne = ntwk.getLinkById(neid);
    	if (ne == null)
    		return;
    	try {
    		Class c = Class.forName("aurora.hwc.gui.Panel" + evt.getClass().getSimpleName());
    		AbstractEventPanel ep = (AbstractEventPanel)c.newInstance();
    		ep.initialize(ne, myEventManager, this, evt);
    	}
    	catch(Exception e) { }
    	return;
    }

}
