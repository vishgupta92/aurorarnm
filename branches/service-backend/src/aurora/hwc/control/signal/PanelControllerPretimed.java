package aurora.hwc.control.signal;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;

import aurora.hwc.MonitorControllerHWC;
import aurora.hwc.control.AbstractPanelControllerComplex;
import aurora.hwc.control.signal.ControllerPretimed;
import aurora.hwc.control.signal.ControllerPretimedIntersectionPlan;
import aurora.hwc.control.signal.ControllerPretimedPlan;
import aurora.hwc.control.signal.ControllerPretimed.PRETIMEDdataRow;
import aurora.util.Util;

public class PanelControllerPretimed extends AbstractPanelControllerComplex {
	private static final long serialVersionUID = 3149391884970185477L;


	private ControllerPretimed controller;	
	private ControllerPretimedPlan currplan;
	private int currintindex;
	
	private JButton buttonAdd = new JButton("Add");
	private JButton buttonDelete = new JButton("Delete");
	private JComboBox PlansCombo = new JComboBox();;
	private JComboBox IntCombo = new JComboBox();;
	private JTable todtable;
	private TODTableModel todtablemodel = new TODTableModel();
	private Vector<PRETIMEDdataRow> toddata = new Vector<PRETIMEDdataRow>();
	private JTable plantable;
	private PlanTableModel plantablemodel = new PlanTableModel();


	public void initialize(MonitorControllerHWC X) {
		super.initialize(X);
		String name = X.getMyController().getClass().getSimpleName();
		if(name.equals("ControllerPretimed")){
			controller = (ControllerPretimed) X.getMyController();
		}
		if(name.equals("ControllerCoordinated")){
			controller = ((ControllerCoordinated) X.getMyController()).Pretimed();
		}
		return;
	}
	
	public void fillPanel() {

		if (controller != null) {
			Vector<PRETIMEDdataRow> td = controller.getTable(); 
			for (int i = 0; i < td.size(); i++) {
				PRETIMEDdataRow tdr = controller.new PRETIMEDdataRow(td.get(i).getStartTime(),td.get(i).getPlanID());
				toddata.add(tdr);
			}
		}
		
		// TOD table panel .........................................................
		JPanel tabpanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		tabpanel.setBorder(BorderFactory.createTitledBorder("Time-of-Day Schedule"));
		
		// table
		todtable = new JTable(todtablemodel);	
		todtable.setPreferredScrollableViewportSize(new Dimension(500, 50));
		todtable.addMouseListener(new MouseAdapter() { 
			public void mouseClicked(MouseEvent e) { 
				if (e.getClickCount() == 2) {
					int row = todtable.rowAtPoint(new Point(e.getX(), e.getY()));
					if ((row > controller.getNumPeriods() - 1) || (row < 0))
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
		c.ipady = 10;
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
		
		// Add Delete buttons
		buttonAdd.setEnabled(true);
		buttonAdd.addActionListener(new ButtonAddListener());
		buttonDelete.setEnabled(true);
		buttonDelete.addActionListener(new ButtonDeleteListener());

		// Plan view panel ................................................................
		ComboListener combolistner = new ComboListener();
		JPanel viewpanel = new JPanel(new GridBagLayout());
		viewpanel.setBorder(BorderFactory.createTitledBorder("View Pretimed Plan"));

		// plan and intersection combo
		Box combobox = Box.createHorizontalBox();
		for(int i=0;i<controller.getNumPlans();i++){
			PlansCombo.addItem(controller.getPlan().get(i).getID());
		}
		PlansCombo.addActionListener(combolistner);
		combobox.add(new JLabel("Plan #"));
		combobox.add(PlansCombo);
		currplan = controller.getPlan().get((Integer)PlansCombo.getSelectedIndex());
		for(int i=0;i< currplan.getNumInters() ;i++){
			IntCombo.addItem(currplan.getIntersPlan().get(i).getSigMan().myNode.getId());
		}
		currintindex = IntCombo.getSelectedIndex();
		IntCombo.addActionListener(combolistner);
		combobox.add(new JLabel("Intersection #"));
		combobox.add(IntCombo);

		c.gridx = 0;
		c.gridy = 0;
		c.ipady = 0;
		viewpanel.add(combobox,c);

		// table
		plantable = new JTable(plantablemodel);	
		plantable.setPreferredScrollableViewportSize(new Dimension(500, 100));
		c.gridy = 1;
		viewpanel.add(new JScrollPane(plantable), c);

		Box box = Box.createVerticalBox();
		box.add(tabpanel);
		box.add(viewpanel);
		add(box);
		
		return;
	}


	/**
	 * Class needed for displaying tod table.
	 */
	private class TODTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -3979101791317093447L;

		public String getColumnName(int col) {
	        if (col == 0)
	        	return "Start Time";
	        return "Plan ID";
	    }

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return toddata.size();
		}

		public void addrow(PRETIMEDdataRow x){
			boolean done = false;
			for (int i = 0; i < toddata.size(); i++){
				if(toddata.get(i).getStartTime() > x.getStartTime()){
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
			this.fireTableStructureChanged();
		}
		
		public Object getValueAt(int row, int column) {
			if ((row < 0) || (row > toddata.size() - 1) || (column < 0) || (column > 1) )
				return null;
			NumberFormat form = NumberFormat.getInstance();
			String s;
			if(column == 0){
				s = Util.time2string(toddata.get(row).getStartTime());
			}
			else {
				form.setMaximumFractionDigits(0);
				s = form.format(toddata.get(row).getPlanID());
			}
			return s;
		}
		
		public boolean isCellEditable(int row, int column) {
			return false;

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
				controller.setTable(toddata);
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
				int i = todtable.getSelectedRow();
				if ((i > toddata.size() - 1) || (i < 0))
					return;	
				todtablemodel.deleterow(i);
				controller.setTable(toddata);
	    	}
	    	catch(Exception e) { }
	    	return;
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

	/**
	 * This class implements an editor widow for an entry to a TOD table.
	 */
	private abstract class WindowPRErow extends JDialog {
		private static final long serialVersionUID = -8078595059018983414L;

		private JSpinner hh;
		private JSpinner mm;
		private JSpinner ss;
		private JSpinner pid;
		private PRETIMEDdataRow myrow = controller.new PRETIMEDdataRow();
		private final static String cmdOK = "pressedOK";
		private final static String cmdCancel = "pressedCancel";

		public WindowPRErow(JFrame parent, String title) {
			super(parent, title);
			setSize(300, 180);
			setLocationRelativeTo(parent);
			setModal(true);
			setContentPane(createForm());
		}

		public WindowPRErow(JFrame parent, String title, PRETIMEDdataRow inrow) {
			super(parent, title);
			myrow = inrow;
			setSize(300, 180);
			setLocationRelativeTo(parent);
			setModal(true);
			setContentPane(createForm());
		}
		
		public PRETIMEDdataRow getMyRow() {
			return myrow;
		}
		
		private JPanel createForm() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			
			// Time 
			JPanel pT = new JPanel(new FlowLayout());
			pT.setBorder(BorderFactory.createTitledBorder("Start Time"));
			hh = new JSpinner(new SpinnerNumberModel(Util.getHours(myrow.getStartTime()), 0, 99, 1));
			hh.setEditor(new JSpinner.NumberEditor(hh, "00"));	
			pT.add(hh);
			pT.add(new JLabel("h "));
			mm = new JSpinner(new SpinnerNumberModel(Util.getMinutes(myrow.getStartTime()), 0, 59, 1));
			mm.setEditor(new JSpinner.NumberEditor(mm, "00"));
			pT.add(mm);
			pT.add(new JLabel("m "));
			ss = new JSpinner(new SpinnerNumberModel(Util.getSeconds(myrow.getStartTime()), 0, 59, 1));
			ss.setEditor(new JSpinner.NumberEditor(ss, "00"));
			pT.add(ss);
			pT.add(new JLabel("s"));
			panel.add(pT);
			
			// Plan id
			JPanel pR = new JPanel(new BorderLayout());
			pR.setBorder(BorderFactory.createTitledBorder("Plan ID"));
			pid = new JSpinner(new SpinnerNumberModel(myrow.getPlanID(), 0.0, 99999.99, 1));
			pid.setEditor(new JSpinner.NumberEditor(pid, "####0.##"));
			pR.add(pid);
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
					double newplan = (Double)pid.getValue();
					if( ( newplan==0 && controller.getCoordMode() ) ||		// asc coordinated case
						( controller.getPlanInd((int)newplan)>=0 ) ){		// pretimed only case
						myrow.setStartTime((Integer)hh.getValue(),(Integer)mm.getValue(),(Integer)ss.getValue());
						myrow.setPlanID(newplan);
						todtablemodel.addrow(myrow);
					}
					setVisible(false);
					dispose();
					return;
				}
				if (cmdCancel.equals(cmd)) {
					setVisible(false);
					dispose();
					return;
				}
				return;
			}
		}
	}
	
	private final class WindowAdd extends WindowPRErow {
		private static final long serialVersionUID = -2411923362676291860L;
		public WindowAdd(JFrame parent) {
			super(parent, "New");
		}
	}
	
	private final class WindowEdit extends WindowPRErow {
		private static final long serialVersionUID = -3418864048397054006L;
		public WindowEdit(JFrame parent,PRETIMEDdataRow x) {
			super(parent, "Edit", x);
		}
	}
	
	private class PlanTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -2669013880150286793L;

		public String getColumnName(int col) {
			String s = "";
	        if (col == 0)
	        	s = "Phase A";
	        if (col == 1)
	        	s = "Phase B";
	        if (col == 2)
	        	s = "Green Time";
	        return s;
	    }

		public int getColumnCount() { return 3; }

		public int getRowCount() {
			currplan = controller.getPlan().get((Integer)PlansCombo.getSelectedIndex());
			currintindex = IntCombo.getSelectedIndex();
			return currplan.getIntersPlan().get(currintindex).getNumStages();
		}

		public Object getValueAt(int row, int column) {
			currplan = controller.getPlan().get((Integer)PlansCombo.getSelectedIndex());
			currintindex = IntCombo.getSelectedIndex();
			ControllerPretimedIntersectionPlan p = currplan.getIntersPlan().get(currintindex);
			if( row<0 || row>p.getNumStages()-1 )
				return null;
			String s="";
			
			Integer i;
			if(column == 0){
				i = p.getMovA().get(row)+1;
				s =i.toString();
			}
			if(column == 1){
				i = p.getMovB().get(row)+1;
				s = i.toString();
			}
			if(column == 2)
				s =p.getGreenTime().get(row).toString();
			return s;
		}
		
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

}
