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
	protected JPanel pMST = new JPanel(new FlowLayout());
	protected JPanel pMSS = new JPanel(new SpringLayout());
	protected JPanel pT = new JPanel(new SpringLayout());
	protected JSpinner hDUP;
	protected JSpinner mDUP;
	protected JSpinner sDUP;
	protected JSpinner hMST;
	protected JSpinner mMST;
	protected JSpinner sMST;
	protected JSpinner spinMSS;
	protected JSpinner spinT;
	
	protected final static String nmDUP = "DispUpdatePeriod";
	protected final static String nmMST = "SimTimeMax";
	protected final static String nmMSS = "SimStepMax";
	protected final static String nmTimeout = "Timeout";
	protected boolean modifiedDUP = false;
	protected boolean modifiedMST = false;
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
		setSize(300, 400);
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
		pMSS.setBorder(BorderFactory.createTitledBorder("Maximum Simulation Step"));
		spinMSS = new JSpinner(new SpinnerNumberModel(mySettings.getTSMax(), 1, 9999999, 1));
		spinMSS.setEditor(new JSpinner.NumberEditor(spinMSS));
		spinMSS.setName(nmMSS);
		spinMSS.addChangeListener(this);
		pMSS.add(spinMSS);
		SpringUtilities.makeCompactGrid(pMSS, 1, 1, 2, 2, 2, 2);
		fp.add(pMSS);
		pT.setBorder(BorderFactory.createTitledBorder("Timeout (milliseconds)"));
		spinT = new JSpinner(new SpinnerNumberModel(mySettings.getTimeout(), 1, 9999999, 1));
		spinT.setEditor(new JSpinner.NumberEditor(spinT));
		spinT.setName(nmTimeout);
		spinT.addChangeListener(this);
		pT.add(spinT);
		SpringUtilities.makeCompactGrid(pT, 1, 1, 2, 2, 2, 2);
		fp.add(pT);
		panel.add(fp, BorderLayout.CENTER);
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
		if (nm.equals(nmMST)) {
			modifiedMST = true;
			pMST.setBorder(BorderFactory.createTitledBorder("*Maximum Simulation Time"));
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
			if (modifiedMST) {
				h = (Integer)hMST.getValue();
				m = (Integer)mMST.getValue();
				s = (Double)sMST.getValue();
				mySettings.setTimeMax(h + (m/60.0) + (s/3600.0));
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