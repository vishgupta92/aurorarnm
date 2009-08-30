package aurora.hwc.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import aurora.AbstractContainer;
import aurora.hwc.MonitorControllerHWC;
import aurora.hwc.control.ControllerSWARM;

public class WindowMonitorSWARM extends WindowMonitor {
	private static final long serialVersionUID = -4926655662074878406L;
	
	// controller tab
	private zoneTabModel zoneTM = new zoneTabModel();
	private JLabel label_density_sample_size  	= new JLabel();
	private JLabel label_epsilon 				= new JLabel();
	private JLabel label_forecast_lead_time 	= new JLabel();
	private JLabel label_input_var_lane 		= new JLabel();
	private JLabel label_meas_var_lane 			= new JLabel();
	private JLabel label_phi 					= new JLabel();
	private JLabel label_psi 					= new JLabel();
	private JLabel label_sat_den_multiplier 	= new JLabel();
	private JLabel label_sat_smoother 			= new JLabel();
	private JLabel label_slope_sample_size 		= new JLabel();
	
	public WindowMonitorSWARM() { }
	public WindowMonitorSWARM(AbstractContainer ctnr, MonitorControllerHWC mn, TreePane tp) {
		super(ctnr,mn,tp);
		fillCntrlPanel();
	}

	/**
	 * Generates Controller tab.
	 */
	private void fillCntrlPanel() {
		
		JPanel desc = new JPanel(new GridLayout(1, 0));
		desc.setBorder(BorderFactory.createTitledBorder("Description"));
		desc.add(new JLabel("<html><font color=\"blue\">" + myMonitor.getDescription() + "</font></html>"));
		cntrlPanel.add(desc);

		JPanel zone = new JPanel(new GridLayout(1, 0));
		zone.setBorder(BorderFactory.createTitledBorder("Zones"));
		final JTable zonetab = new JTable(zoneTM);
		zonetab.setPreferredScrollableViewportSize(new Dimension(250, 70));
		zone.add(new JScrollPane(zonetab));
		cntrlPanel.add(zone);

		JPanel prmters = new JPanel(new GridLayout(1, 0));
		prmters.setBorder(BorderFactory.createTitledBorder("Parameters"));
		makeLabels();
		Box g1 = Box.createVerticalBox();
		g1.add(label_density_sample_size);
		g1.add(label_epsilon);
		g1.add(label_forecast_lead_time);
		g1.add(label_input_var_lane);
		g1.add(label_meas_var_lane);
		Box g2 = Box.createVerticalBox();
		g2.add(label_phi);
		g2.add(label_psi);
		g2.add(label_sat_den_multiplier);
		g2.add(label_sat_smoother);
		g2.add(label_slope_sample_size);
		prmters.add(g1);
		prmters.add(g2);
		cntrlPanel.add(prmters);
		return;
	}

	/**
	 * Class needed for displaying table of zones.
	 */
	private class zoneTabModel extends AbstractTableModel {
		private static final long serialVersionUID = -8310974555648802182L;

		public String getColumnName(int col) {
	        String buf = null;
	        switch(col) {
	        case 0:	buf = "Zone"; break;
	        case 1: buf = "Bottleneck link"; break;
	        case 2: buf = "# Onramps"; break;
	        case 3: buf = "From onramp"; break;
	        case 4: buf = "To onramp"; break;
	        }
			return buf;
	    }
		
		public int getColumnCount() {
			return 5;
		}

		public int getRowCount() {
			return ((ControllerSWARM) myMonitor.getMyController()).getNumZones();
		}

		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row >= getRowCount()) || (column < 0) || (column > getColumnCount()))
				return null;
			if (column == 0)
				return row+1;
			if (column == 1)
				return ((ControllerSWARM) myMonitor.getMyController()).getBottleneckID(row);
			if (column == 2)
				return ((ControllerSWARM) myMonitor.getMyController()).getNumOnramps(row);
			if (column == 3)
				return ((ControllerSWARM) myMonitor.getMyController()).getFirstOnramp(row);
			if (column == 4)
				return ((ControllerSWARM) myMonitor.getMyController()).getLastOnramp(row);
			return null;
		}
		
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
		public void setValueAt(Object value, int row, int column) {
			return;
		}
		
	}
	
	/**
	 * Makes labels for Controller tab.
	 */
	private void makeLabels() {
		NumberFormat form = NumberFormat.getInstance();
		form.setMaximumFractionDigits(2);
		ControllerSWARM c = (ControllerSWARM) myMonitor.getMyController();
		label_density_sample_size.setText("<html><font color=\"gray\"><u><b>Dnty sample size:</b></u><font color=\"blue\"> " + form.format(c.get_density_sample_size()) + "</font></font></html>");
		label_epsilon.setText("<html><font color=\"gray\"><u><b>Epsilon:</b></u><font color=\"blue\"> " + form.format(c.get_epsilon()) + "</font></font></html>");
		label_forecast_lead_time.setText("<html><font color=\"gray\"><u><b>Forecast time:</b></u><font color=\"blue\"> " + form.format(c.get_forecast_lead_time()) + "</font></font></html>");
		label_input_var_lane.setText("<html><font color=\"gray\"><u><b>Input variance:</b></u><font color=\"blue\"> " + form.format(c.get_input_var_lane()) + "</font></font></html>");
		label_meas_var_lane.setText("<html><font color=\"gray\"><u><b>Measurement variance:</b></u><font color=\"blue\"> " + form.format(c.get_meas_var_lane()) + "</font></font></html>");
		label_phi.setText("<html><font color=\"gray\"><u><b>Phi:</b></u><font color=\"blue\"> " + form.format(c.get_phi()) + "</font></font></html>");
		label_psi.setText("<html><font color=\"gray\"><u><b>Psi:</b></u><font color=\"blue\"> " + form.format(c.get_psi()) + "</font></font></html>");
		label_sat_den_multiplier.setText("<html><font color=\"gray\"><u><b>Sat dnsty mult.:</b></u><font color=\"blue\"> " + form.format(c.get_sat_den_multiplier()) + "</font></font></html>");
		label_sat_smoother.setText("<html><font color=\"gray\"><u><b>Sat smoother:</b></u><font color=\"blue\"> " + form.format(c.get_sat_smoother()) + "</font></font></html>");
		label_slope_sample_size.setText("<html><font color=\"gray\"><u><b>Slope sample size:</b></u><font color=\"blue\"> " + form.format(c.get_slope_sample_size()) + "</font></font></html>");
	}
	
}
