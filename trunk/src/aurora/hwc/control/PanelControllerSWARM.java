/**
 * @(#)PanelControllerSWARM.java
 */

package aurora.hwc.control;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import aurora.*;
import aurora.hwc.AbstractLinkHWC;
import aurora.hwc.TypesHWC;
import aurora.hwc.control.ControllerSWARM.Zone;


/**
 * Implementation of SWARM editor.
 * @author Gabriel Gomes, Alex Kurzhanskiy
 * @version $Id: PanelControllerSWARM.java,v 1.1.4.9 2009/10/01 05:49:01 akurzhan Exp $
 */
public class PanelControllerSWARM extends AbstractPanelController implements ActionListener {
	private static final long serialVersionUID = 4029100437243156023L;

	//private final static String stswarm1 = "swarm1";	
	//private final static String stswarm2a = "swarm2a";	
	//private final static String stswarm2b = "swarm2b";	
	//private final static String dynbott = "dynbott";	
	//CheckboxListener checkboxListener = new CheckboxListener();
	
	private Vector<Zone> zones = new Vector<Zone>();
	
	private int density_sample_size = 0;
	private double epsilon = 0.0;
	private int forecast_lead_time = 0;
	private double input_var_lane = 0.0;
	private double meas_var_lane = 0.0;
	private double phi = 0.0;
	private double psi = 0.0;
	private int sat_den_multiplier = 0;
	private double sat_smoother = 0;
	private int slope_sample_size = 0;
	
	private JCheckBox cbsw1 = new JCheckBox("SWARM 1");
	private JCheckBox cbsw2a = new JCheckBox("SWARM 2a");
	private JCheckBox cbsw2b = new JCheckBox("SWARM 2b");
	private JCheckBox cbdynbott = new JCheckBox("Dynamic Bottlenecks");
	
	private zoneTabModel zoneTM = new zoneTabModel();
	private JTable zonetab = new JTable(zoneTM);
	private JButton buttonAdd = new JButton("Add");
	private JButton buttonDelete = new JButton("Delete");
	
	private final static String cmdAdd = "pressedAdd";
	private final static String cmdDelete = "pressedDelete";
	
	private paramTabModel paramTM = new paramTabModel();

	/**
	 * Fills the panel with SWARM specific fields.
	 */
	public void fillPanel() {
		// Zone initialization
		ControllerSWARM z = (ControllerSWARM)controller;
		Vector<Zone> cz = z.zones;
		for (int i = 0; i < cz.size(); i++)
			zones.add(cz.get(i).clone());
		// Parameter initialization
		density_sample_size = z.P.SWARM_DENSITY_SAMPLE_SIZE;
		epsilon = z.P.epsilon;
		forecast_lead_time = z.P.SWARM_FORECAST_LEAD_TIME;
		input_var_lane = z.P.input_var_lane;
		meas_var_lane = z.P.meas_var_lane;
		phi = z.P.swarm_phi;
		psi = z.P.swarm_psi;
		sat_den_multiplier = z.P.SWARM_SAT_DEN_NUMBER;
		sat_smoother = z.P.sat_smoother;
		slope_sample_size = z.P.SWARM_SLOPE_SAMPLE_SIZE;
		// Components
		JPanel comp = new JPanel(new FlowLayout());
		comp.setBorder(BorderFactory.createTitledBorder("Components"));
		cbsw1.setSelected(z.P.SWARM1);
		comp.add(cbsw1);
		comp.add(new JLabel("  "));
		cbsw2a.setSelected(z.P.SWARM2A);
		comp.add(cbsw2a);
		comp.add(new JLabel("  "));
		cbsw2b.setSelected(z.P.SWARM2B);
		comp.add(cbsw2b);
		comp.add(new JLabel("  "));
		cbdynbott.setEnabled(false);
		comp.add(cbdynbott);
		add(comp);
		// Zones
		JPanel zone = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		zone.setBorder(BorderFactory.createTitledBorder("Zones"));
		zonetab.setPreferredScrollableViewportSize(new Dimension(400, 30));
		setUpBottleneckColumn();
		setUpFromOnrampColumn();
		setUpToOnrampColumn();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 45;
		c.weightx = 0.5;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 0;
		zone.add(new JScrollPane(zonetab), c);
		c.ipady = 0; 
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridx = 0;
		zone.add(buttonAdd, c);
		c.gridx = 1;
		zone.add(buttonDelete, c);
		// configure buttons
		buttonAdd.setEnabled(true);
		buttonAdd.setActionCommand(cmdAdd);
		buttonAdd.addActionListener(this);
		buttonDelete.setEnabled(true);
		buttonDelete.setActionCommand(cmdDelete);
		buttonDelete.addActionListener(this);
		add(zone);
		// Parameters
		JPanel param = new JPanel(new GridLayout(1, 0));
		param.setBorder(BorderFactory.createTitledBorder("Parameters"));
		final JTable paramtab = new JTable(paramTM);
		paramtab.setPreferredScrollableViewportSize(new Dimension(500, 160));
		param.add(new JScrollPane(paramtab));
		add(param);
		return;
	}
	
	/**
	 * Establishes combo box editor for bottleneck column.
	 * @param clmn
	 */
	private void setUpBottleneckColumn() {
		JComboBox combo = new JComboBox();
		Vector<AbstractNetworkElement> nes = ((AbstractControllerComplex)controller).getMyMonitor().getPredecessors();
		for (int i = 0; i < nes.size(); i++)
			if ((nes.get(i).getType() & TypesHWC.MASK_LINK) > 0)
				combo.addItem(nes.get(i));
		TableColumn clmn = zonetab.getColumnModel().getColumn(1);
		clmn.setCellEditor(new DefaultCellEditor(combo));
		clmn.setCellRenderer(new DefaultTableCellRenderer());
		return;
	}
	
	/**
	 * Establishes combo box editor for 'from onramp' column.
	 * @param clmn
	 */
	private void setUpFromOnrampColumn() {
		JComboBox combo = new JComboBox();
		Vector<AbstractNetworkElement> nes = ((AbstractControllerComplex)controller).getMyMonitor().getSuccessors();
		for (int i = 0; i < nes.size(); i++)
			if ((nes.get(i).getType() & TypesHWC.MASK_LINK) > 0)
				combo.addItem(nes.get(i));
		TableColumn clmn = zonetab.getColumnModel().getColumn(2);
		clmn.setCellEditor(new DefaultCellEditor(combo));
		clmn.setCellRenderer(new DefaultTableCellRenderer());
		return;
	}
	
	/**
	 * Establishes combo box editor for 'from onramp' column.
	 * @param clmn
	 */
	private void setUpToOnrampColumn() {
		JComboBox combo = new JComboBox();
		Vector<AbstractNetworkElement> nes = ((AbstractControllerComplex)controller).getMyMonitor().getSuccessors();
		for (int i = 0; i < nes.size(); i++)
			if ((nes.get(i).getType() & TypesHWC.MASK_LINK) > 0)
				combo.addItem(nes.get(i));
		TableColumn clmn = zonetab.getColumnModel().getColumn(3);
		clmn.setCellEditor(new DefaultCellEditor(combo));
		clmn.setCellRenderer(new DefaultTableCellRenderer());
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
	        case 1: buf = "Bottleneck Link"; break;
	        case 2: buf = "From On-Ramp"; break;
	        case 3: buf = "To On-Ramp"; break;
	        case 4: buf = "Sat. Den. Multiplier"; break;
	        }
			return buf;
	    }
		
		public int getColumnCount() {
			return 5;
		}

		public int getRowCount() {
			return zones.size();
		}

		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row >= getRowCount()) || (column < 0) || (column > getColumnCount()))
				return null;
			if (column == 0)
				return row + 1;
			if (column == 1)
				return zones.get(row).bottleneck;
			if (column == 2)
				return zones.get(row).onramps.firstElement();
			if (column == 3)
				return zones.get(row).onramps.lastElement();
			if (column == 4)
				return zones.get(row).sat_den_multiplier;
			return null;
		}
		
		public boolean isCellEditable(int row, int column) {
			if (column == 0)
				return false;
			return true;
		}
		
		public void setValueAt(Object value, int row, int column) {
			if ((row < 0) || (row >= zones.size()) || (value == null))
				return;
			if (column == 1) {
				zones.get(row).bottleneck = (AbstractLinkHWC) value;
			}
			if (column == 2) {
				zones.get(row).setFromOnramp((AbstractLinkHWC)value);
			}
			if (column == 3) {
				zones.get(row).setToOnramp((AbstractLinkHWC)value);
			}
			if (column == 4) {
				try {
					double v = Double.parseDouble((String)value);
					if (v >= 0)
						zones.get(row).sat_den_multiplier = v;
				}
				catch(Exception e) {}
			}
			fireTableRowsUpdated(row, row);
			return;
		}
		
	}

	/**
	 * Class needed for displaying table of parameters.
	 */
	private class paramTabModel extends AbstractTableModel {
		private static final long serialVersionUID = -8310974555648802182L;

		public String getColumnName(int col) {
	        String buf = null;
			return buf;
	    }
		
		public int getColumnCount() {return 2;}
		public int getRowCount() {return 10;}

		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row >= getRowCount()) || (column < 0) || (column > getColumnCount()))
				return null;
			if (column == 0) {
		        switch(row) {
		        case 0:	return "Density Sample Size";
		        case 1: return "Epsilon";
		        case 2: return "Forecast Time";
		        case 3: return "Input Variance"; 
		        case 4: return "Measurement Variance";
		        case 5: return "Phi"; 
		        case 6: return "Psi"; 
		        case 7: return "Saturation Density Multiplier"; 
		        case 8: return "Saturation smoother"; 
		        case 9: return "Slope Sample Size"; 
		        }
			}
			if (column == 1) {
				switch(row) {
				case 0: return density_sample_size;
				case 1: return epsilon;
				case 2: return forecast_lead_time;
				case 3: return input_var_lane;
				case 4: return meas_var_lane;
				case 5: return phi;
				case 6: return psi;
				case 7: return sat_den_multiplier;
				case 8: return sat_smoother;
				case 9: return slope_sample_size;
				}
			}
			return null;
		}
		
		public boolean isCellEditable(int row, int column) {
			if(column==0)
				return false; 
			return true;
		}
		
		public void setValueAt(Object value, int row, int column) {
			if((column != 1) || (row < 0) || (row >= getRowCount()))
				return;
			String buf = (String)value;
			try {
				double v = Double.parseDouble(buf);
				if ((v >= 0.0) && (v <= 99999.99))
					switch(row) {
					case 0: 
						density_sample_size = (int)v;
						break;
					case 1: 
						epsilon = v;
						break;
					case 2: 
						forecast_lead_time = (int)v;
						break;
					case 3: 
						input_var_lane = v;
						break;
					case 4: 
						meas_var_lane = v;
						break;
					case 5: 
						phi = v;
						break;
					case 6: 
						psi = v;
						break;
					case 7: 
						sat_den_multiplier = (int)v;
						break;
					case 8: 
						sat_smoother = v;
						break;
					case 9: 
						slope_sample_size = (int)v;
						break;
					}
			}
			catch(Exception e) { }
			fireTableRowsUpdated(row, row);
			return;
		}
		
	}
	
	/**
	 * Saves SWARM properties.
	 */
	public synchronized void save() {
		super.save();
		((ControllerSWARM)controller).P.SWARM1 =  cbsw1.isSelected();
		((ControllerSWARM)controller).P.SWARM2A = cbsw2a.isSelected();
		((ControllerSWARM)controller).P.SWARM2B = cbsw2b.isSelected();
		//((ControllerSWARM)controller).set_swarm2b(cbdynbott.isSelected());
		((ControllerSWARM)controller).zones = zones;
		ControllerSWARM z = (ControllerSWARM)controller;
		z.P.SWARM_DENSITY_SAMPLE_SIZE = density_sample_size;
		z.P.epsilon = epsilon;
		z.P.SWARM_FORECAST_LEAD_TIME = forecast_lead_time;
		z.P.input_var_lane = input_var_lane;
		z.P.meas_var_lane = meas_var_lane;
		z.P.swarm_phi = phi;
		z.P.swarm_psi = psi;
		z.P.SWARM_SAT_DEN_NUMBER = sat_den_multiplier;
		z.P.sat_smoother = sat_smoother;
		z.P.SWARM_SLOPE_SAMPLE_SIZE = slope_sample_size;
		return;
	}
	
	/**
	 * Reaction to Add/Delete buttons.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmdAdd.equals(cmd)) {
			Zone z = ((ControllerSWARM)controller).new Zone();
			Vector<AbstractNetworkElement> ml = ((AbstractControllerComplex)controller).getMyMonitor().getPredecessors();
			Vector<AbstractNetworkElement> cl = ((AbstractControllerComplex)controller).getMyMonitor().getSuccessors();
			if (ml.size() > 0)
				z.bottleneck = (AbstractLinkHWC) ml.firstElement();
			if (cl.size() > 0) {
				z.setFromOnramp((AbstractLinkHWC)cl.firstElement());
				z.setToOnramp((AbstractLinkHWC)cl.firstElement());
			}
			z.initialize();
			zones.add(z);
			zoneTM.fireTableStructureChanged();
			setUpBottleneckColumn();
			setUpFromOnrampColumn();
			setUpToOnrampColumn();
		}
		if (cmdDelete.equals(cmd)) {
			try {
				int[] selected = zonetab.getSelectedRows();
				if ((selected != null) && (selected.length > 0))
					for (int i = 0; i < selected.length; i++) {
						int idx = selected[i] - i;
						if ((idx >= 0) && (idx < zones.size())) {
							zones.remove(idx);
							zoneTM.fireTableStructureChanged();
							setUpBottleneckColumn();
							setUpFromOnrampColumn();
							setUpToOnrampColumn();
						}
					}
	    	}
	    	catch(Exception ex) { }
		}
		return;
	}
	
	

	
}
