/**
 * @(#)PanelControllerHERO.java 
 */

package aurora.hwc.control;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import aurora.hwc.control.ControllerHERO.HERORampInfo;


/**
 * Editor for HERO controller.
 * @author Alex Kurzhanskiy
 * @version $Id: PanelControllerHERO.java,v 1.1.2.3 2009/10/01 05:49:01 akurzhan Exp $
 */
public class PanelControllerHERO extends AbstractPanelController {
	private static final long serialVersionUID = 4241655930420665436L;
	
	Vector<HERORampInfo> onramps = null;
	

	/**
	 * Fills the panel with HERO specific fields.
	 */
	protected void fillPanel() {
		onramps = ((ControllerHERO)controller).getOnrampInfoVector();
		JPanel panel = new JPanel(new GridLayout(1, 0));
		panel.setBorder(BorderFactory.createTitledBorder("On-Ramp Parameters"));
		JTable ptable = new JTable(new ParamTableModel());
		ptable.setPreferredScrollableViewportSize(new Dimension(200, 100));
		panel.add(new JScrollPane(ptable));
		add(panel);
		return;
	}
	
	
	/**
	 * Class needed for displaying HERO parameters in a table.
	 */
	private class ParamTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 4752888886383816447L;

		public String getColumnName(int col) {
			switch(col) {
	        case 0:
	        	return "On-Ramp";
	        case 1:
	        	return "Gain";
	        case 2:
	        	return "Activation Threshold";
	        default:
	        	return "Deactivation Threshold";
	        }
	    }
		
		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return onramps.size();
		}

		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row >= onramps.size()))
				return null;
			switch(column) {
			case 0:
				return onramps.get(row).getLink();
			case 1:
				return onramps.get(row).getGain();
			case 2:
				return onramps.get(row).getActivationThreshold();
			default:
				return onramps.get(row).getDeactivationThreshold();
			}
		}
		
		public boolean isCellEditable(int row, int column) {
			if (column == 0)
				return false;
			return true;
		}
		
		public void setValueAt(Object value, int row, int column) {
			if((column == 0) || (row < 0) || (row >= getRowCount()))
				return;
			String buf = (String)value;
			try {
				double v = Double.parseDouble(buf);
				switch(column) {
				case 1:
					onramps.get(row).setGain(v);
					break;
				case 2:
					onramps.get(row).setActivationThreshold(v);
					break;
				default:
					onramps.get(row).setDeactivationThreshold(v);
					break;
				}
			}
			catch(Exception e) { }
			fireTableRowsUpdated(row, row);
			return;
		}
	}

}
