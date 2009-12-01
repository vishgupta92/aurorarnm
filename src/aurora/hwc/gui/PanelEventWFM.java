/**
 * @(#)PanelEventWFM.java 
 */

package aurora.hwc.gui;

import java.awt.*;
import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.table.*;
import aurora.*;
import aurora.hwc.*;


/**
 * Form for weaving factor matrix change event.
 * @author Alex Kurzhanskiy
 * @version $Id: PanelEventWFM.java,v 1.1.2.1 2009/11/07 21:19:16 akurzhan Exp $
 */
public final class PanelEventWFM extends AbstractEventPanel {
	private static final long serialVersionUID = 1366583867517745447L;
	
	private int nIn = 0;
	private int nOut = 0;
	private double[][] wfm = null;
	private wfmRWTableModel wfmTM = new wfmRWTableModel();
	
	
	/**
	 * Initializes weaving factor matrix change event editing panel.
	 * @param ne Network Element.
	 * @param em Event Manager.
	 * @param etm Event Table model.
	 */
	public synchronized void initialize(AbstractNetworkElement ne, EventManager em, EventTableModel etm) {
		eventTable = etm;
		myEvent = new EventWFM();
		AbstractNodeHWC nd = (AbstractNodeHWC)ne;
		nIn = nd.getInputs().size();
		nOut = nd.getOutputs().size();
		wfm = nd.getWeavingFactorMatrix();
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
			myEvent = new EventWFM();
		AbstractNodeHWC nd = (AbstractNodeHWC)ne;
		nIn = nd.getInputs().size();
		nOut = nd.getOutputs().size();
		wfm = ((EventWFM)myEvent).getWeavingFactorMatrix();
		if ((wfm.length != nIn) || (wfm[0].length != nOut))
			wfm = nd.getWeavingFactorMatrix();
		initialize(ne, em);
	}

	/**
	 * Fills the panel with event specific fields.
	 */
	protected void fillPanel() {
		JPanel tabp = new JPanel(new GridLayout(1, 0));
		tabp.setBorder(BorderFactory.createTitledBorder("Weaving Factors"));
		JTable wfmtab = new JTable(wfmTM);
		wfmtab.setPreferredScrollableViewportSize(new Dimension(250, 150));
		tabp.add(new JScrollPane(wfmtab));
		add(tabp);
		return;
	}

	/**
	 * Returns window header with event description.
	 */
	public String getHeader() {
		return "Weaving Factors";
	}
	
	/**
	 * Saves event.
	 */
	public synchronized void save() {
		((EventWFM)myEvent).setWeavingFactorMatrix(wfm);
		super.save();
		return;
	}
	
	/**
	 * Class needed for displaying weaving factor matrix in a table.
	 */
	private class wfmRWTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -3270699108447530535L;

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
				if ((column < 1) || (column > wfm[0].length))
					return null;
				AbstractNetworkElement ne = myNode.getSuccessors().get(column - 1);
				return "To " + TypesHWC.typeString(ne.getType()) + " " + ne;
			}
			if (column == 0) {
				if ((row < 1) || (row > wfm.length))
					return null;
				AbstractNetworkElement ne = myNode.getPredecessors().get(row - 1);
				return "From " + TypesHWC.typeString(ne.getType()) + " " + ne;
			}
			if ((row < 1) || (row > wfm.length) || (column < 1) || (column > wfm[0].length))
				return null;
			NumberFormat form = NumberFormat.getInstance();
			form.setMinimumFractionDigits(0);
			form.setMaximumFractionDigits(2);
			form.setGroupingUsed(false);
			return form.format(wfm[row - 1][column - 1]);
		}
		
		public void setValueAt(Object value, int row, int column) {
			String buf = (String)value;
			int i = row - 1;
			int j = column - 1;
			if ((i < 0) || (i >= nIn) || (j < 0) || (j >= nOut))
				return;
			try {
				wfm[i][j] = Double.parseDouble(buf);
			}
			catch(Exception e) {
				wfm[i][j] = 1;
			}
			wfmTM.fireTableRowsUpdated(row, row);
			return;
		}
		
	}

}