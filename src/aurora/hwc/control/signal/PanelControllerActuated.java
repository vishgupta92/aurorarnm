package aurora.hwc.control.signal;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import aurora.hwc.MonitorControllerHWC;
import aurora.hwc.control.AbstractControllerComplexPanel;
import aurora.hwc.control.signal.ControllerActuated.ASC_Parameters;

public class PanelControllerActuated extends AbstractControllerComplexPanel {
	private static final long serialVersionUID = -4506383623896255722L;


	private ControllerActuated controller;
	private JComboBox IntCombo = new JComboBox();
	private JTable paramtab;
	private PlanTableModel plantablemodel = new PlanTableModel();

	public void initialize(MonitorControllerHWC X) {
		super.initialize(X);
		String name = X.getMyController().getClass().getSimpleName();
		if(name.equals("ControllerActuated")){
			controller = (ControllerActuated) X.getMyController();
		}
		if(name.equals("ControllerCoordinated")){
			controller = (ControllerActuated) X.getMyController();
		}
		return;
	}
	
	public void fillPanel() {

		GridBagConstraints c = new GridBagConstraints();
		
		ComboListener combolistner = new ComboListener();
		JPanel parampanel = new JPanel(new GridLayout(0,1)); 
		parampanel.setBorder(BorderFactory.createTitledBorder("Controller settings"));

		Box combobox = Box.createHorizontalBox();
		for (int i = 0; i < controller.numintersections; i++){
			IntCombo.addItem(controller.intersection.get(i));
		}
		IntCombo.addActionListener(combolistner);
		IntCombo.setMaximumSize(new Dimension(500,60));
		combobox.add(new JLabel("Intersection #"));
		combobox.add(IntCombo);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.ipady = 0;
		parampanel.add(combobox,c);

		paramtab = new JTable(plantablemodel);
		paramtab.setPreferredScrollableViewportSize(new Dimension(500,100));
		c.gridy = 1;
		parampanel.add(new JScrollPane(paramtab), c);

		add(parampanel);
		
	}

	/**
	 * Class needed for displaying table of parameters.
	 */
	
	private class PlanTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -2063094295497596609L;

		public int getColumnCount() { return 9; }
		public int getRowCount() { return 9; }
		
		public String getColumnName(int col) {
	        String buf = null;
	        switch(col) {
	        case 0:	buf = ""; break;
	        case 1: buf = "1"; break;
	        case 2: buf = "2"; break;
	        case 3: buf = "3"; break;
	        case 4: buf = "4"; break;
	        case 5: buf = "5"; break;
	        case 6: buf = "6"; break;
	        case 7: buf = "7"; break;
	        case 8: buf = "8"; break;
	        }
			return buf;
	    }
		
		public Object getValueAt(int row, int column) {
			String buf = null;
			Vector <ASC_Parameters> p = ((ControllerActuated) myMonitor.getMyController()).P.get(IntCombo.getSelectedIndex());
			
			if (column == 0)
				switch(row){
				case 0: buf = "lagleft"; break;
				case 1: buf = "maxinitial"; break;
				case 2: buf = "extension"; break;
				case 3: buf = "maxgap"; break;
				case 4: buf = "mingap"; break;
				case 5: buf = "addpervehicle"; break;
				case 6: buf = "reducegapby"; break;
				case 7: buf = "reduceevery"; break;
				case 8: buf = "maxgreen"; break;
				}
			else{
				buf = "-";
				switch(row){
				case 0: 
					if( p.get(column-1).lagleft )
						buf = "1";
					else
						buf = "0";
					break;
				case 1: 
					buf = String.format("%.2f",p.get(column-1).maxinitial);
					break;
				case 2:
					buf = String.format("%.2f",p.get(column-1).extension);
					break;
				case 3:
					buf = String.format("%.2f",p.get(column-1).maxgap);
					break;
				case 4:
					buf = String.format("%.2f",p.get(column-1).mingap);
					break;
				case 5:
					buf = String.format("%.2f",p.get(column-1).addpervehicle);
					break;
				case 6:
					buf = String.format("%.2f",p.get(column-1).reducegapby);
					break;
				case 7:
					buf = String.format("%.2f",p.get(column-1).reduceevery);
					break;
				case 8:
					buf = String.format("%.2f",p.get(column-1).maxgreen);
					break;			
				}
			}
			return  buf;
		}
		
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
	}

	
	/**
	 * This class is needed to react to combo changes
	 */
	private class ComboListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			plantablemodel.fireTableDataChanged();
			return;
		}
	}
	
}
