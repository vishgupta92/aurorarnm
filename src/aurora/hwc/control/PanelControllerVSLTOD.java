/**
 * @(#)PanelControllerVSLTOD.java
 */

package aurora.hwc.control;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import aurora.util.*;
import aurora.hwc.control.ControllerVSLTOD.TODdataRow;

/**
 * Panel for editing VSL TOD controller properties.
 * @author Alex Kurzhanskiy
 * @version $Id: PanelControllerVSLTOD.java,v 1.1.2.1 2009/11/12 02:36:57 akurzhan Exp $
 */
public class PanelControllerVSLTOD extends AbstractPanelSimpleController {
	private static final long serialVersionUID = 2067612590605300660L;
	
	private JButton buttonAdd = new JButton("Add");
	private JButton buttonDelete = new JButton("Delete");
	
	private JTable todtable;
	private TODTableModel todtablemodel = new TODTableModel();
	private Vector<TODdataRow> toddata = new Vector<TODdataRow>();
	
	
	/**
	 * Fills the panel with VSL TOD specific fields.
	 */
	protected void fillPanel() {
		if (controller != null) {
			Vector<TODdataRow> td = ((ControllerVSLTOD)controller).getTable(); 
			for (int i = 0; i < td.size(); i++) {
				TODdataRow tdr = ((ControllerVSLTOD)controller).new TODdataRow(td.get(i).getTime(), td.get(i).getSpeed());
				toddata.add(tdr);
			}
		}
		JPanel tabpanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		tabpanel.setBorder(BorderFactory.createTitledBorder("Rate Schedule"));
		// table
		todtable = new JTable(todtablemodel);	
		todtable.setPreferredScrollableViewportSize(new Dimension(200, 50));
		todtable.addMouseListener(new MouseAdapter() { 
			public void mouseClicked(MouseEvent e) { 
				if (e.getClickCount() == 2) {
					int row = todtable.rowAtPoint(new Point(e.getX(), e.getY()));
					if ((row > toddata.size() - 1) || (row < 0))
						return;	
					try {
						WindowEdit winEdit = new WindowEdit(null, toddata.get(row));
						winEdit.setVisible(true);
						todtablemodel.deleterow(row);
						todtablemodel.addrow(winEdit.getMyRow());
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
		tabpanel.add(new JScrollPane(todtable), c);
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
		((ControllerVSLTOD)controller).setTable(toddata);
		super.save();
		return;
	}
	
	
	/**
	 * Class needed for displaying TOD table.
	 */
	private class TODTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 3008291406189812112L;

		public String getColumnName(int col) {
	        if (col == 0)
	        	return "Start Time";
	        return "Speed (mph)";
	    }

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return toddata.size();
		}

		public void addrow(TODdataRow x) {
			boolean done = false;
			for (int i = 0; i < toddata.size(); i++){
				if(toddata.get(i).getTime() > x.getTime()){
					toddata.insertElementAt(x, i);
					done = true;
					break;
				}	
			}
			if(!done)
				toddata.add(x);
			fireTableStructureChanged();
		}

		public void deleterow(int i){
			toddata.removeElementAt(i);
			fireTableStructureChanged();
		}
		
		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row > toddata.size() - 1) || (column < 0) || (column > 1) )
				return null;
			NumberFormat form = NumberFormat.getInstance();
			String s;
			if(column == 0)
				s = Util.time2string(toddata.get(row).getTime());
			else {
				form.setMaximumFractionDigits(2);
				s = form.format(toddata.get(row).getSpeed());
			}
			return s;
		}
		
		public boolean isCellEditable(int row, int column) {
			if (column == 1)
				return true;
			return false;
		}
		
		public void setValueAt(Object value, int row, int column) {
			if ((row < 0) || (row >= toddata.size()) || (column != 1))
				return;
			String buf = (String)value;
			try {
				double v = Double.parseDouble(buf);
				if ((v >= 10.0) && (v <= 99.99))
					toddata.get(row).setSpeed(v);
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
				int[] selected = todtable.getSelectedRows();
				if ((selected != null) && (selected.length > 0))
					for (int i = 0; i < selected.length; i++) {
						int idx = selected[i] - i;
						if ((idx >= 0) && (idx < toddata.size()))
							todtablemodel.deleterow(idx);
					}
	    	}
	    	catch(Exception e) { }
	    	return;
		}		
	}
	
	
	/**
	 * This class implements an editor widow for an entry to a VSL TOD table.
	 */
	private abstract class WindowVSLTOD extends JDialog {
		private static final long serialVersionUID = -3788895906010404938L;
		
		private JSpinner hh;
		private JSpinner mm;
		private JSpinner ss;
		private JSpinner speed;
		protected TODdataRow myrow = ((ControllerVSLTOD)controller).new TODdataRow();
		protected boolean toAdd = true;
		private final static String cmdOK = "pressedOK";
		private final static String cmdCancel = "pressedCancel";

		public WindowVSLTOD(JFrame parent, String title) {
			super(parent, title);
			setSize(300, 180);
			setLocationRelativeTo(parent);
			setModal(true);
		}
		
		public TODdataRow getMyRow() {
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
			// Speed
			JPanel pR = new JPanel(new BorderLayout());
			pR.setBorder(BorderFactory.createTitledBorder("Speed (mph)"));
			speed = new JSpinner(new SpinnerNumberModel(Math.round(myrow.getSpeed()), 0.0, 100, 5));
			speed.setEditor(new JSpinner.NumberEditor(speed, "####0"));
			pR.add(speed);
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
					myrow.setSpeed((Double)speed.getValue());
					if (toAdd)
						todtablemodel.addrow(myrow);
					else
						todtablemodel.fireTableDataChanged();
						
				}
				setVisible(false);
				dispose();
				return;
			}
		}
	}
	
	private final class WindowAdd extends WindowVSLTOD {
		private static final long serialVersionUID = -6891343668767740020L;

		public WindowAdd(JFrame parent) {
			super(parent, "New");
			setContentPane(createForm());
		}
	}
	
	private final class WindowEdit extends WindowVSLTOD {
		private static final long serialVersionUID = -9002212494401339334L;

		public WindowEdit(JFrame parent,TODdataRow x) {
			super(parent, "Edit");
			if (x != null) {
				myrow = x;
				toAdd = false;
			}
			setContentPane(createForm());
		}
	}
	
}
