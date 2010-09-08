/**
 * @(#)WindowSettings.java
 */

package aurora.common;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import aurora.*;
import aurora.util.*;


/**
 * Base class for settings windows.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public class WindowSettings extends JDialog implements ActionListener, ChangeListener {
	private static final long serialVersionUID = 6936519653061073792L;
	
	protected CommonMain mainWindow = null;
	protected AbstractContainer mySystem = null;
	protected SimulationSettings mySettings = new SimulationSettings();
	
	protected JTabbedPane tabbedPane = new JTabbedPane();
	
	// simulation tab
	protected JPanel pDUP = new JPanel(new FlowLayout());
	protected JPanel pIST = new JPanel(new FlowLayout());
	protected JPanel pMST = new JPanel(new FlowLayout());
	protected JPanel pISS = new JPanel(new SpringLayout());
	protected JPanel pMSS = new JPanel(new SpringLayout());
	protected JPanel pT = new JPanel(new SpringLayout());
	protected JPanel pM = new JPanel(new SpringLayout());
	protected JSpinner hDUP;
	protected JSpinner mDUP;
	protected JSpinner sDUP;
	protected JSpinner hIST;
	protected JSpinner mIST;
	protected JSpinner sIST;
	protected JSpinner hMST;
	protected JSpinner mMST;
	protected JSpinner sMST;
	protected JSpinner spinISS;
	protected JSpinner spinMSS;
	protected JSpinner spinT;
	protected JComboBox comboM;
	
	protected final static String nmDUP = "DispUpdatePeriod";
	protected final static String nmIST = "InitSimTime";
	protected final static String nmMST = "MaxSimTime";
	protected final static String nmISS = "InitSimStep";
	protected final static String nmMSS = "MaxSimStep";
	protected final static String nmTimeout = "Timeout";
	protected final static String cmdMode = "Mode";
	protected boolean modifiedDUP = false;
	protected boolean modifiedIST = false;
	protected boolean modifiedMST = false;
	protected boolean modifiedM = false;
	protected boolean modifiedSettings = false;
	
	
	
	protected final static String cmdOK = "pressedOK";
	protected final static String cmdCancel = "pressedCancel";
	
	
	public WindowSettings() { }
	public WindowSettings(AbstractContainer ctnr, CommonMain parent) {
		super((JFrame)null, "Settings Editor");
		mySystem = ctnr;
		if (mySystem != null)
			mySettings.copyData(mySystem.getMySettings());
		if (parent != null)
			mainWindow = parent;
		setSize(300, 450);
		setLocationRelativeTo(null);
		setModal(true);
		JPanel panelMain = new JPanel(new BorderLayout());
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.add("Simulation", createSimulationTab());
		// OK, Cancel buttons
		JPanel bp = new JPanel(new FlowLayout());
		JButton bOK = new JButton("    OK    ");
		bOK.setActionCommand(cmdOK);
		bOK.addActionListener(this);
		JButton bCancel = new JButton("Cancel");
		bCancel.setActionCommand(cmdCancel);
		bCancel.addActionListener(this);
		bp.add(bOK);
		bp.add(bCancel);
		// add all sub-panels to panel
		panelMain.add(tabbedPane, BorderLayout.CENTER);
		panelMain.add(bp, BorderLayout.SOUTH);
		setContentPane(panelMain);
	}
	
	
	/**
	 * Creates simulation tab.
	 */
	private JPanel createSimulationTab() {
		JPanel panel = new JPanel(new BorderLayout());
		// Boxes
		Box fp = Box.createVerticalBox();
		JPanel pSSP = new JPanel(new FlowLayout());
		pSSP.setBorder(BorderFactory.createTitledBorder("Sampling Period"));
		pSSP.add(new JLabel("<html><font color=\"blue\">" + Util.time2string(mySystem.getMyNetwork().getTP()) + "</font></html>"));
		fp.add(pSSP);
		// display update period
		pDUP.setBorder(BorderFactory.createTitledBorder("Display Update Period"));
		hDUP = new JSpinner(new SpinnerNumberModel(Util.getHours(mySettings.getDisplayTP()), 0, 99, 1));
		hDUP.setEditor(new JSpinner.NumberEditor(hDUP, "00"));
		hDUP.setName(nmDUP);
		hDUP.addChangeListener(this);
		pDUP.add(hDUP);
		pDUP.add(new JLabel("h "));
		mDUP = new JSpinner(new SpinnerNumberModel(Util.getMinutes(mySettings.getDisplayTP()), 0, 59, 1));
		mDUP.setEditor(new JSpinner.NumberEditor(mDUP, "00"));
		mDUP.setName(nmDUP);
		mDUP.addChangeListener(this);
		pDUP.add(mDUP);
		pDUP.add(new JLabel("m "));
		sDUP = new JSpinner(new SpinnerNumberModel(Util.getSeconds(mySettings.getDisplayTP()), 0, 59.99, 1));
		sDUP.setEditor(new JSpinner.NumberEditor(sDUP, "00.##"));
		sDUP.setName(nmDUP);
		sDUP.addChangeListener(this);
		pDUP.add(sDUP);
		pDUP.add(new JLabel("s"));
		fp.add(pDUP);
		// simulation start time
		pIST.setBorder(BorderFactory.createTitledBorder("Initial Simulation Time"));
		hIST = new JSpinner(new SpinnerNumberModel(Util.getHours(mySettings.getTimeInitial()), 0, 99, 1));
		hIST.setEditor(new JSpinner.NumberEditor(hIST, "00"));
		hIST.setName(nmIST);
		hIST.addChangeListener(this);
		pIST.add(hIST);
		pIST.add(new JLabel("h "));
		mIST = new JSpinner(new SpinnerNumberModel(Util.getMinutes(mySettings.getTimeInitial()), 0, 59, 1));
		mIST.setEditor(new JSpinner.NumberEditor(mIST, "00"));
		mIST.setName(nmIST);
		mIST.addChangeListener(this);
		pIST.add(mIST);
		pIST.add(new JLabel("m "));
		sIST = new JSpinner(new SpinnerNumberModel(Util.getSeconds(mySettings.getTimeInitial()), 0, 59.99, 1));
		sIST.setEditor(new JSpinner.NumberEditor(sIST, "00.##"));
		sIST.setName(nmIST);
		sIST.addChangeListener(this);
		pIST.add(sIST);
		pIST.add(new JLabel("s"));
		fp.add(pIST);
		// simulation end time
		pMST.setBorder(BorderFactory.createTitledBorder("Maximum Simulation Time"));
		hMST = new JSpinner(new SpinnerNumberModel(Util.getHours(mySettings.getTimeMax()), 0, 99, 1));
		hMST.setEditor(new JSpinner.NumberEditor(hMST, "00"));
		hMST.setName(nmMST);
		hMST.addChangeListener(this);
		pMST.add(hMST);
		pMST.add(new JLabel("h "));
		mMST = new JSpinner(new SpinnerNumberModel(Util.getMinutes(mySettings.getTimeMax()), 0, 59, 1));
		mMST.setEditor(new JSpinner.NumberEditor(mMST, "00"));
		mMST.setName(nmMST);
		mMST.addChangeListener(this);
		pMST.add(mMST);
		pMST.add(new JLabel("m "));
		sMST = new JSpinner(new SpinnerNumberModel(Util.getSeconds(mySettings.getTimeMax()), 0, 59.99, 1));
		sMST.setEditor(new JSpinner.NumberEditor(sMST, "00.##"));
		sMST.setName(nmMST);
		sMST.addChangeListener(this);
		pMST.add(sMST);
		pMST.add(new JLabel("s"));
		fp.add(pMST);
		// initial simulation step
		pISS.setBorder(BorderFactory.createTitledBorder("Initial Simulation Step"));
		spinISS = new JSpinner(new SpinnerNumberModel(mySettings.getTSInitial(), 0, 9999999, 1));
		spinISS.setEditor(new JSpinner.NumberEditor(spinISS));
		spinISS.setName(nmISS);
		spinISS.addChangeListener(this);
		pISS.add(spinISS);
		SpringUtilities.makeCompactGrid(pISS, 1, 1, 2, 2, 2, 2);
		fp.add(pISS);
		// maximum simulation step
		pMSS.setBorder(BorderFactory.createTitledBorder("Maximum Simulation Step"));
		spinMSS = new JSpinner(new SpinnerNumberModel(mySettings.getTSMax(), 1, 9999999, 1));
		spinMSS.setEditor(new JSpinner.NumberEditor(spinMSS));
		spinMSS.setName(nmMSS);
		spinMSS.addChangeListener(this);
		pMSS.add(spinMSS);
		SpringUtilities.makeCompactGrid(pMSS, 1, 1, 2, 2, 2, 2);
		fp.add(pMSS);
		// timeout
		pT.setBorder(BorderFactory.createTitledBorder("Timeout (milliseconds)"));
		spinT = new JSpinner(new SpinnerNumberModel(mySettings.getTimeout(), 1, 9999999, 1));
		spinT.setEditor(new JSpinner.NumberEditor(spinT));
		spinT.setName(nmTimeout);
		spinT.addChangeListener(this);
		pT.add(spinT);
		SpringUtilities.makeCompactGrid(pT, 1, 1, 2, 2, 2, 2);
		fp.add(pT);
		if (!mySystem.isSimulation()) {
			pM.setBorder(BorderFactory.createTitledBorder("Operation Mode"));
			comboM = new JComboBox();
			comboM.addItem("Simulation");
			comboM.addItem("Prediction");
			if (mySettings.isPrediction())
				comboM.setSelectedIndex(1);
			comboM.setActionCommand(cmdMode);
			comboM.addActionListener(this);
			pM.add(comboM);
			SpringUtilities.makeCompactGrid(pM, 1, 1, 2, 2, 2, 2);
			fp.add(pM);
		}
		panel.add(new JScrollPane(fp), BorderLayout.CENTER);
		return panel;
	}
	
	
	/**
	 * Reaction to parameter change.
	 */
	public void stateChanged(ChangeEvent e) {
		String nm = ((JComponent)e.getSource()).getName();
		if (nm.equals(nmDUP)) {
			modifiedDUP = true;
			pDUP.setBorder(BorderFactory.createTitledBorder("*Display Update Period"));
		}
		if (nm.equals(nmIST)) {
			modifiedIST = true;
			pIST.setBorder(BorderFactory.createTitledBorder("*Initial Simulation Time"));
		}
		if (nm.equals(nmMST)) {
			modifiedMST = true;
			pMST.setBorder(BorderFactory.createTitledBorder("*Maximum Simulation Time"));
		}
		if (nm.equals(nmISS)) {
			mySettings.setTSInitial((Integer)spinISS.getValue());
			pISS.setBorder(BorderFactory.createTitledBorder("*Initial Simulation Step"));
		}
		if (nm.equals(nmMSS)) {
			mySettings.setTSMax((Integer)spinMSS.getValue());
			pMSS.setBorder(BorderFactory.createTitledBorder("*Maximum Simulation Step"));
		}
		if (nm.equals(nmTimeout)) {
			mySettings.setTimeout((Integer)spinT.getValue());
			pT.setBorder(BorderFactory.createTitledBorder("*Timeout (milliseconds)"));
		}
		modifiedSettings = true;
		return;
	}
	
	
	/**
	 * Reaction to OK/Cancel buttons.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmdMode.equals(cmd)) {
			modifiedM = true;
			modifiedSettings = true;
			pM.setBorder(BorderFactory.createTitledBorder("*Operation Mode"));
			return;
		}
		if (cmdOK.equals(cmd) && modifiedSettings) {
			if (mainWindow == null)
				mySystem.getMyStatus().setSaved(false);
			if ((mainWindow != null) && (!((aurora.hwc.gui.MainPane)mainWindow).checkSaved()))
				return;
			int h, m;
			double s;
			if (modifiedDUP) {
				h = (Integer)hDUP.getValue();
				m = (Integer)mDUP.getValue();
				s = (Double)sDUP.getValue();
				mySettings.setDisplayTP(h + (m/60.0) + (s/3600.0));
				if (mySettings.getDisplayTP() < mySystem.getMyNetwork().getTP())
					mySettings.setDisplayTP(mySystem.getMyNetwork().getTP());
			}
			if (modifiedIST) {
				h = (Integer)hIST.getValue();
				m = (Integer)mIST.getValue();
				s = (Double)sIST.getValue();
				mySettings.setTimeInitial(h + (m/60.0) + (s/3600.0));
			}
			if (modifiedMST) {
				h = (Integer)hMST.getValue();
				m = (Integer)mMST.getValue();
				s = (Double)sMST.getValue();
				mySettings.setTimeMax(h + (m/60.0) + (s/3600.0));
			}
			if (modifiedM) {
				mySettings.setPrediction(comboM.getSelectedIndex() == 1);
			}
			mySystem.setMySettings(mySettings);
			if (mainWindow != null)
				((aurora.hwc.gui.MainPane)mainWindow).resetAll();
		}
		setVisible(false);
		dispose();
		return;
	}
	
}