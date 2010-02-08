/**
 * @(#)PanelEventSRM.java 
 */

package aurora.hwc.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import aurora.*;
import aurora.hwc.*;


/**
 * Form for split ratio matrix change event.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class PanelEventSRM extends AbstractEventPanel {
	private static final long serialVersionUID = 3362186089540442355L;
	
	private int nIn = 0;
	private int nOut = 0;
	private AuroraIntervalVector[][] srm = null;
	private srmRWTableModel srmTM = new srmRWTableModel();
	
	
	/**
	 * Initializes split ratio matrix change event editing panel.
	 * @param ne Network Element.
	 * @param em Event Manager.
	 * @param etm Event Table model.
	 */
	public synchronized void initialize(AbstractNetworkElement ne, EventManager em, EventTableModel etm) {
		eventTable = etm;
		myEvent = new EventSRM();
		AbstractNodeHWC nd = (AbstractNodeHWC)ne;
		nIn = nd.getInputs().size();
		nOut = nd.getOutputs().size();
		srm = nd.getSplitRatioMatrix();
		initialize(ne, em);
	}
	
	/**
	 * Initializes split ratio matrix change event editing panel.
	 * @param ne Network Element.
	 * @param em Event Manager.
	 * @param etm Event Table model.
	 * @param evt Event.
	 */
	public synchronized void initialize(AbstractNetworkElement ne, EventManager em, EventTableModel etm, AbstractEvent evt) {
		eventTable = etm;
		if (evt != null)
			myEvent = evt;
		else
			myEvent = new EventSRM();
		AbstractNodeHWC nd = (AbstractNodeHWC)ne;
		nIn = nd.getInputs().size();
		nOut = nd.getOutputs().size();
		srm = ((EventSRM)myEvent).getSplitRatioMatrix();
		if ((srm.length != nIn) || (srm[0].length != nOut))
			srm = nd.getSplitRatioMatrix();
		initialize(ne, em);
	}

	/**
	 * Fills the panel with event specific fields.
	 */
	protected void fillPanel() {
		JPanel tabp = new JPanel(new GridLayout(1, 0));
		tabp.setBorder(BorderFactory.createTitledBorder("Split Ratio Matrix"));
		JTable srmtab = new JTable(srmTM);
		srmtab.setPreferredScrollableViewportSize(new Dimension(250, 150));
		tabp.add(new JScrollPane(srmtab));
		add(tabp);
		return;
	}

	/**
	 * Returns window header with event description.
	 */
	public String getHeader() {
		return "Split Ratio Matrix";
	}
	
	/**
	 * Saves event.
	 */
	public synchronized void save() {
		((EventSRM)myEvent).setSplitRatioMatrix(srm);
		super.save();
		return;
	}
	
	/**
	 * Class needed for displaying split ratio matrix in a table.
	 */
	private class srmRWTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -3567455539033002997L;

		public String getColumnName(int col) {
	        return " ";
	    }
		
		public int getColumnCount() {
			return (nOut + 1);
		}

		public int getRowCount() {
			return (nIn + 1);
		}
		
		public boolean isCellEditable(int row, int column) {
			if ((row > 0) && (column > 0))
				return true;
			return false;
		}

		public Object getValueAt(int row, int column) {
			AbstractNodeHWC myNode = (AbstractNodeHWC)myNE;
			if (row == 0) {
				if ((column < 1) || (column > srm[0].length))
					return null;
				AbstractNetworkElement ne = myNode.getSuccessors().get(column - 1);
				return "To " + TypesHWC.typeString(ne.getType()) + " " + ne;
			}
			if (column == 0) {
				if ((row < 1) || (row > srm.length))
					return null;
				AbstractNetworkElement ne = myNode.getPredecessors().get(row - 1);
				return "From " + TypesHWC.typeString(ne.getType()) + " " + ne;
			}
			if ((row < 1) || (row > srm.length) || (column < 1) || (column > srm[0].length))
				return null;
			return srm[row - 1][column - 1].toString();
		}
		
		public void setValueAt(Object value, int row, int column) {
			String buf = (String)value;
			int i = row - 1;
			int j = column - 1;
			if ((i < 0) || (i >= nIn) || (j < 0) || (j >= nOut))
				return;
			srm[i][j].setIntervalVectorFromString(buf);
			srmTM.fireTableRowsUpdated(row, row);
			return;
		}
		
	}

}