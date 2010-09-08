/**
 * @(#)WindowSettingsHWC.java
 */

package aurora.hwc.common;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import javax.swing.*;
import javax.swing.table.*;
import aurora.*;
import aurora.hwc.*;
import aurora.common.*;


/**
 * HWC settings window.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class WindowSettingsHWC extends WindowSettings {
	private static final long serialVersionUID = -3435014467278491221L;
	
	
	protected JPanel vtpanel = null;
	protected JTable vttable;
	protected VTTableModel vttablemodel = new VTTableModel();
	
	protected JButton buttonAdd = new JButton("Add");
	protected JButton buttonDelete = new JButton("Delete");
	protected final static String cmdAdd = "addPressed";
	protected final static String cmdDelete = "deletePressed";


	public WindowSettingsHWC() { }
	public WindowSettingsHWC(AbstractContainer ctnr, CommonMain parent) {
		super(ctnr, parent);
		mySettings = new SimulationSettingsHWC();
		if (mySystem != null)
			mySettings.copyData(mySystem.getMySettings());
		vtpanel = createVTTab();
		tabbedPane.add("Vehicle Types", vtpanel);
	}
	
	
	/**
	 * Creates vehicle types tab.
	 */
	private JPanel createVTTab() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		panel.setBorder(BorderFactory.createTitledBorder("Vehicle Types and Weights"));
		// table
		vttable = new JTable(vttablemodel);	
		vttable.setPreferredScrollableViewportSize(new Dimension(200, 50));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 100;
		c.weightx = 0.5;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 0;
		panel.add(new JScrollPane(vttable), c);
		c.ipady = 0; 
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridx = 0;
		panel.add(buttonAdd, c);
		c.gridx = 1;
		panel.add(buttonDelete, c);
		// configure buttons
		buttonAdd.setEnabled(true);
		buttonAdd.addActionListener(new ButtonAddDeleteListener());
		buttonAdd.setActionCommand(cmdAdd);
		buttonDelete.setEnabled(true);
		buttonDelete.addActionListener(new ButtonAddDeleteListener());
		buttonDelete.setActionCommand(cmdDelete);
		if (mainWindow != null) {
			buttonAdd.setVisible(false);
			buttonDelete.setVisible(false);
		}
		else {
			buttonAdd.setVisible(true);
			buttonDelete.setVisible(true);
		}
		return panel;
	}
	
	
	/**
	 * Class needed for displaying vehicle types table.
	 */
	private class VTTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 5434622263487022961L;

		public String getColumnName(int col) {
	        if (col == 0)
	        	return "Type";
	        return "Weight";
	    }

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return ((SimulationSettingsHWC)mySettings).countVehicleTypes();
		}

		public void addrow(String type, double weight) {
			if (((SimulationSettingsHWC)mySettings).addVehicleType(type, weight)) {
				vtpanel.setBorder(BorderFactory.createTitledBorder("*Vehicle Types and Weights"));
				modifiedSettings = true;
				fireTableStructureChanged();
			}
			return;
		}

		public void deleterow(int row) {
			if (((SimulationSettingsHWC)mySettings).removeVehicleType(row)) {
				vtpanel.setBorder(BorderFactory.createTitledBorder("*Vehicle Types and Weights"));
				modifiedSettings = true;
				fireTableStructureChanged();
			}
			return;
		}
		
		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row >= getRowCount()) || (column < 0) || (column > 1) )
				return null;
			if (column == 0)
				return ((SimulationSettingsHWC)mySettings).getVehicleTypes().get(row);
			NumberFormat form = NumberFormat.getInstance();
			form.setMaximumFractionDigits(2);
			return form.format(((SimulationSettingsHWC)mySettings).getVehicleWeights()[row]);
		}
		
		public boolean isCellEditable(int row, int column) {
			if ((column == 0) || (column == 1))
				return true;
			return false;
		}
		
		public void setValueAt(Object value, int row, int column) {
			if ((row < 0) || (row >= getRowCount()))
				return;
			if (column == 0) {
				((SimulationSettingsHWC)mySettings).setVehicleTypeName((String)value, row);
				vtpanel.setBorder(BorderFactory.createTitledBorder("*Vehicle Types and Weights"));
				modifiedSettings = true;
			}
			if (column == 1) {
				try {
					double v = Double.parseDouble((String)value);
					if ((v >= 1.0) && (v <= 999.99)) {
						((SimulationSettingsHWC)mySettings).setVehicleTypeWeight(v, row);
						vtpanel.setBorder(BorderFactory.createTitledBorder("*Vehicle Types and Weights"));
						modifiedSettings = true;
					}
				}
				catch(Exception e) { }
			}
			fireTableRowsUpdated(row, row);
			return;
		}
	}
	
	
	/**
	 * This class is needed to react to "Add" & "Delete" buttons pressed.
	 */
	private class ButtonAddDeleteListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			String cmd = ae.getActionCommand();
			if (cmdAdd.equals(cmd)) {
				try {
					WindowAdd winAdd = new WindowAdd(null);
					winAdd.setVisible(true);
	    		}
	    		catch(Exception e) { }
			}
			if (cmdDelete.equals(cmd))
				vttablemodel.deleterow(vttable.getSelectedRow());
	    	return;
		}
	}

	
	/**
	 * This class implements an editor widow for an entry to a TOD table.
	 */
	private class WindowAdd extends JDialog {
		private static final long serialVersionUID = -3533359258753065790L;
		
		private JTextField vt;
		private JSpinner ww;
		
		private final static String localOK = "okPressed";
		private final static String localCancel = "cancelPressed";

		public WindowAdd() { }
		public WindowAdd(JFrame parent) {
			super(parent, "New");
			setSize(300, 180);
			setLocationRelativeTo(parent);
			setModal(true);
			setContentPane(createForm());
		}
		
		private JPanel createForm() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			// Type 
			JPanel pT = new JPanel(new BorderLayout());
			pT.setBorder(BorderFactory.createTitledBorder("Type"));
			vt = new JTextField();
			vt.setText("General");
			pT.add(vt);
			panel.add(pT);
			// Weight
			JPanel pR = new JPanel(new BorderLayout());
			pR.setBorder(BorderFactory.createTitledBorder("Weight"));
			ww = new JSpinner(new SpinnerNumberModel(1.0, 1.0, 999.99, 1));
			ww.setEditor(new JSpinner.NumberEditor(ww, "##0.##"));
			pR.add(ww);
			panel.add(pR);
			
			JPanel bp = new JPanel(new FlowLayout());
			JButton bOK = new JButton("    OK    ");
			bOK.setActionCommand(localOK);
			bOK.addActionListener(new ButtonEventsListener());
			JButton bCancel = new JButton("Cancel");
			bCancel.setActionCommand(localCancel);
			bCancel.addActionListener(new ButtonEventsListener());
			bp.add(bOK);
			bp.add(bCancel);			
			panel.add(bp);
			return panel;
		}
		
		private class ButtonEventsListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				if (localOK.equals(cmd)) {
					String tv = vt.getText();
					if ((tv == null) || tv.isEmpty())
						tv = "General";
					double wv = (Double)ww.getValue();
					if (wv < 1.0)
						wv = 1.0;
					vttablemodel.addrow(tv, wv);
				}
				setVisible(false);
				dispose();
				return;
			}
		}
	}
}