/**
 * @(#)PanelControllerSimpleSignal.java
 */

package aurora.hwc.control;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import aurora.util.*;
import aurora.hwc.control.ControllerSimpleSignal.CycleDataRow;

/**
 * Panel for editing simple signal controller properties.
 * @author Alex Kurzhanskiy
 * @version $Id: PanelControllerSimpleSignal.java,v 1.1.4.1.2.4 2009/01/08 18:59:30 akurzhan Exp $
 */
public class PanelControllerSimpleSignal extends AbstractControllerPanel {
	private static final long serialVersionUID = 2397624987665897798L;
	
	private JButton buttonAdd = new JButton("Add");
	private JButton buttonDelete = new JButton("Delete");
	
	private int conversion = 3600;
	private JSpinner offset;
	private JTable cycletable;
	private CycleTableModel cycletablemodel = new CycleTableModel();
	private Vector<CycleDataRow> cycledata = new Vector<CycleDataRow>();
	
	
	/**
	 * Fills the panel with simple signal specific fields.
	 */
	protected void fillPanel() {
		if (controller != null) {
			Vector<CycleDataRow> cd = ((ControllerSimpleSignal)controller).getCycleTable(); 
			for (int i = 0; i < cd.size(); i++) {
				CycleDataRow cdr = ((ControllerSimpleSignal)controller).new CycleDataRow(cd.get(i).getTime(), cd.get(i).getGreen(), cd.get(i).getRed());
				cycledata.add(cdr);
			}
		}
		// offset
		JPanel pO = new JPanel(new BorderLayout());
		pO.setBorder(BorderFactory.createTitledBorder("Offset (sec.)"));
		offset = new JSpinner(new SpinnerNumberModel(((ControllerSimpleSignal)controller).getOffset() * conversion, 0.0, 99999.99, 10));
		offset.setEditor(new JSpinner.NumberEditor(offset, "####0.##"));
		pO.add(offset);
		add(pO);
		// table
		JPanel tabpanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		tabpanel.setBorder(BorderFactory.createTitledBorder("Cycle Schedule"));
		cycletable = new JTable(cycletablemodel);	
		cycletable.setPreferredScrollableViewportSize(new Dimension(200, 50));
		cycletable.addMouseListener(new MouseAdapter() { 
			public void mouseClicked(MouseEvent e) { 
				if (e.getClickCount() == 2) {
					int row = cycletable.rowAtPoint(new Point(e.getX(), e.getY()));
					if ((row > cycledata.size() - 1) || (row < 0))
						return;	
					try {
						WindowEdit winEdit = new WindowEdit(null, cycledata.get(row));
						winEdit.setVisible(true);
						cycletablemodel.deleterow(row);
						cycletablemodel.addrow(winEdit.getMyRow());
			    	}
			    	catch(Exception excp) { }
				}
			}
		});  
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipady = 100;
		c.weightx = 0.5;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 0;
		tabpanel.add(new JScrollPane(cycletable), c);
		c.ipady = 0; 
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridx = 0;
		tabpanel.add(buttonAdd, c);
		c.gridx = 1;
		tabpanel.add(buttonDelete, c);
		add(tabpanel);
		// configure buttons
		buttonAdd.setEnabled(true);
		buttonAdd.addActionListener(new ButtonAddListener());
		buttonDelete.setEnabled(true);
		buttonDelete.addActionListener(new ButtonDeleteListener());
		return;
	}
	
	
	/**
	 * Saves controller properties.
	 */
	public synchronized void save() {
		((ControllerSimpleSignal)controller).setOffset(((Double)offset.getValue()) / conversion);
		((ControllerSimpleSignal)controller).setCycleTable(cycledata);
		super.save();
		return;
	}
	
	/**
	 * Class needed for displaying tod table.
	 */
	private class CycleTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -4024510556384951370L;

		public String getColumnName(int col) {
	        if (col == 0)
	        	return "Start Time";
	        if (col == 1)
	        	return "Green (sec.)";
	        return "Red (sec.)";
	    }

		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return cycledata.size();
		}

		public void addrow(CycleDataRow x){
			boolean done = false;
			for (int i = 0; i < cycledata.size(); i++){
				if(cycledata.get(i).getTime() > x.getTime()){
					cycledata.insertElementAt(x, i);
					done = true;
					break;
				}	
			}
			if(!done)
				cycledata.add(x);
			fireTableStructureChanged();
		}

		public void deleterow(int i){
			cycledata.removeElementAt(i);
			fireTableStructureChanged();
		}
		
		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row > cycledata.size() - 1) || (column < 0) || (column > 2) )
				return null;
			NumberFormat form = NumberFormat.getInstance();
			form.setMaximumFractionDigits(2);
			String s;
			if (column == 0)
				s = Util.time2string(cycledata.get(row).getTime());
			else if (column == 1)
				s = form.format(cycledata.get(row).getGreen() * conversion);
			else
				s = form.format(cycledata.get(row).getRed() * conversion);
			return s;
		}
		
		public boolean isCellEditable(int row, int column) {
			if (column == 1)
				return true;
			return false;
		}
		
		public void setValueAt(Object value, int row, int column) {
			if ((row < 0) || (row >= cycledata.size()) || (column < 1) || (column > 2))
				return;
			String buf = (String)value;
			try {
				double v = Double.parseDouble(buf);
				if ((v >= 0.0) && (v <= 99999.99))
					if (column == 1)
						cycledata.get(row).setGreen(v / conversion);
					else
						cycledata.get(row).setRed(v / conversion);
			}
			catch(Exception e) { }
			fireTableRowsUpdated(row, row);
			return;
		}
	}		


	/**
	 * This class is needed to react to "Add" button pressed.
	 */
	private class ButtonAddListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			try {
				WindowAdd winAdd = new WindowAdd(null);
				winAdd.setVisible(true);
	    	}
	    	catch(Exception e) { }
	    	return;
		}
	}

	
	/**
	 * This class is needed to react to "Delete" button pressed.
	 */
	private class ButtonDeleteListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			try {
				int[] selected = cycletable.getSelectedRows();
				if ((selected != null) && (selected.length > 0))
					for (int i = 0; i < selected.length; i++) {
						System.err.println(selected[i]);
						int idx = selected[i] - i;
						if ((idx >= 0) && (idx < cycledata.size()))
							cycletablemodel.deleterow(idx);
					}
	    	}
	    	catch(Exception e) { }
	    	return;
		}		
	}
	
	
	/**
	 * This class implements an editor widow for an entry to a cycle table.
	 */
	private abstract class WindowCycle extends JDialog {
		private static final long serialVersionUID = 7012766021584707048L;
		
		private JSpinner hh;
		private JSpinner mm;
		private JSpinner ss;
		private JSpinner green;
		private JSpinner red;
		protected CycleDataRow myrow = ((ControllerSimpleSignal)controller).new CycleDataRow();
		protected boolean toAdd = true;
		private final static String cmdOK = "pressedOK";
		private final static String cmdCancel = "pressedCancel";

		public WindowCycle() { }
		public WindowCycle(JFrame parent, String title) {
			super(parent, title);
			setSize(300, 240);
			setLocationRelativeTo(parent);
			setModal(true);
		}
		
		public CycleDataRow getMyRow() {
			return myrow;
		}
		
		protected JPanel createForm() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			// Time 
			JPanel pT = new JPanel(new FlowLayout());
			pT.setBorder(BorderFactory.createTitledBorder("Start Time"));
			hh = new JSpinner(new SpinnerNumberModel(Util.getHours(myrow.getTime()), 0, 99, 1));
			hh.setEditor(new JSpinner.NumberEditor(hh, "00"));	
			pT.add(hh);
			pT.add(new JLabel("h "));
			mm = new JSpinner(new SpinnerNumberModel(Util.getMinutes(myrow.getTime()), 0, 59, 1));
			mm.setEditor(new JSpinner.NumberEditor(mm, "00"));
			pT.add(mm);
			pT.add(new JLabel("m "));
			ss = new JSpinner(new SpinnerNumberModel(Util.getSeconds(myrow.getTime()), 0, 59, 1));
			ss.setEditor(new JSpinner.NumberEditor(ss, "00"));
			pT.add(ss);
			pT.add(new JLabel("s"));
			panel.add(pT);
			// Green
			JPanel pG = new JPanel(new BorderLayout());
			pG.setBorder(BorderFactory.createTitledBorder("Green (sec.)"));
			green = new JSpinner(new SpinnerNumberModel(myrow.getGreen() * conversion, 0.0, 99999.99, 1));
			green.setEditor(new JSpinner.NumberEditor(green, "####0.##"));
			pG.add(green);
			panel.add(pG);
			// Red
			JPanel pR = new JPanel(new BorderLayout());
			pR.setBorder(BorderFactory.createTitledBorder("Red (sec.)"));
			red = new JSpinner(new SpinnerNumberModel(myrow.getRed() * conversion, 0.0, 99999.99, 1));
			red.setEditor(new JSpinner.NumberEditor(red, "####0.##"));
			pR.add(red);
			panel.add(pR);
			
			JPanel bp = new JPanel(new FlowLayout());
			JButton bOK = new JButton("    OK    ");
			bOK.setActionCommand(cmdOK);
			bOK.addActionListener(new ButtonEventsListener());
			JButton bCancel = new JButton("Cancel");
			bCancel.setActionCommand(cmdCancel);
			bCancel.addActionListener(new ButtonEventsListener());
			bp.add(bOK);
			bp.add(bCancel);			
			panel.add(bp);
			return panel;
		}
		
		private class ButtonEventsListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				if (cmdOK.equals(cmd)) {
					myrow.setTime((Integer)hh.getValue(),(Integer)mm.getValue(),(Double)ss.getValue());
					myrow.setCycle((Double)green.getValue() / conversion, (Double)red.getValue() / conversion);
					if (toAdd)
						cycletablemodel.addrow(myrow);
					else
						cycletablemodel.fireTableDataChanged();
				}
				setVisible(false);
				dispose();
				return;
			}
		}
	}
	
	private final class WindowAdd extends WindowCycle {
		private static final long serialVersionUID = -2533465176473872986L;
		
		public WindowAdd() { }
		public WindowAdd(JFrame parent) {
			super(parent, "New");
			setContentPane(createForm());
		}
	}
	
	private final class WindowEdit extends WindowCycle {
		private static final long serialVersionUID = -9090898426405999714L;
		
		public WindowEdit() { }
		public WindowEdit(JFrame parent, CycleDataRow x) {
			super(parent, "Edit");
			if (x != null) {
				myrow = x;
				toAdd = false;
			}
			setContentPane(createForm());
		}
	}
	
}
