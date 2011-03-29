/**
 * 
 */
package aurora.hwc.gui;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import aurora.AbstractContainer;
import aurora.hwc.SensorLoopDetector;

/**
 * @author gomes
 *
 */
public class WindowSensor extends JInternalFrame {
	private static final long serialVersionUID = -7092282187940545254L;

	private SensorLoopDetector mySensor;
	private TreePane treePane;
	private Box myPanel = Box.createVerticalBox();

	private JLabel labelLink = new JLabel();
	private JLabel labelCoordinates = new JLabel();
	private JLabel labelLinkPosition = new JLabel();
	private JLabel labelFlow = new JLabel();
	private JLabel labelDensity = new JLabel();
	private JLabel labelSpeed = new JLabel();
	private JLabel labelOccupancy = new JLabel();
	private JLabel labelisOccupied = new JLabel();
	
	public WindowSensor() { }
	public WindowSensor(AbstractContainer ctnr, SensorLoopDetector sn, TreePane tp) {
		super(sn.toString(), true, true, true, true);

		mySensor = sn;
		treePane = tp;
		
		setSize(250, 220);
		int n = treePane.getInternalFrameCount();
		setLocation(20*n, 20*n);
		AdapterWindowSensor listener = new AdapterWindowSensor();
		addInternalFrameListener(listener);
		addComponentListener(listener);
		fillPanel();
		return;
	}
	
	/**
	 * Makes labels for Sensor window.
	 */
	private void makeFixedLabels() {
		NumberFormat form = NumberFormat.getInstance();
		form.setMaximumFractionDigits(2);
		labelLink.setText("<html><font color=\"gray\"><b>Link:</b><font color=\"blue\"> " + form.format(mySensor.getLink().getId()) + " </font></font></html>");
		String X = form.format(mySensor.getPositionX());
		String Y = form.format(mySensor.getPositionY());
		String Z = form.format(mySensor.getPositionZ());
		labelCoordinates.setText("<html><font color=\"gray\"><b>Coordinates:</b><font color=\"blue\"> [ " + X + " , " + Y + " , " + Z + " ] </font></font></html>");
		labelLinkPosition.setText("<html><font color=\"gray\"><b>Link position [feet]:</b><font color=\"blue\"> " + form.format(mySensor.getOffsetInLink()*5280.0) + " </font></font></html>");
	}
	/**
	 * Makes labels for Sensor window.
	 */
	private void makeVariableLabels() {
		NumberFormat form = NumberFormat.getInstance();
		form.setMaximumFractionDigits(1);
		labelFlow.setText("<html><font color=\"gray\"><b>Flow [veh/hour]:</b><font color=\"blue\"> " + form.format(mySensor.Flow()) + " </font></font></html>");
		labelDensity.setText("<html><font color=\"gray\"><b>Density [veh/mile]:</b><font color=\"blue\"> " + form.format(mySensor.Density()) + " </font></font></html>");
		labelSpeed.setText("<html><font color=\"gray\"><b>Speed [mile/hour]:</b><font color=\"blue\"> " + form.format(mySensor.Speed()) + " </font></font></html>");
		labelOccupancy.setText("<html><font color=\"gray\"><b>Occupancy [%]:</b><font color=\"blue\"> " + form.format(mySensor.Occupancy()*100.0) + "</font></font></html>");
	}
	
	/**
	 * Generates panel.
	 */
	private void fillPanel() {
		makeFixedLabels();
		makeVariableLabels();
		Box loc = Box.createHorizontalBox();
		loc.setBorder(BorderFactory.createTitledBorder("Location"));
		Box l1 = Box.createVerticalBox();
		l1.add(labelLink);
		l1.add(labelCoordinates);
		l1.add(labelLinkPosition);
		loc.add(l1);
		myPanel.add(loc);
		Box meas = Box.createHorizontalBox();
		meas.setBorder(BorderFactory.createTitledBorder("Measurements"));
		Box m1 = Box.createVerticalBox();

		m1.add(labelFlow);
		m1.add(labelDensity);
		m1.add(labelSpeed);
		m1.add(labelOccupancy);
		m1.add(labelisOccupied);
		meas.add(m1);
		myPanel.add(meas);
		add(myPanel);
	}
	
	/**
	 * Updates displayed data.
	 */
	public void updateView() {
		makeVariableLabels();
		return;
	}
	
	/**
	 * Action performed before closing the frame.
	 */
	private void close() {
		treePane.removeFrame(this);
		return;
	}
	
	/**
	 * Class needed for proper closing of internal node windows.
	 */
	private class AdapterWindowSensor extends InternalFrameAdapter implements ComponentListener {
		
		/**
		 * Function that is called when user closes the window.
		 * @param e internal frame event.
		 */
		public void internalFrameClosing(InternalFrameEvent e) {
			close();
			return;
		}
		
		public void componentHidden(ComponentEvent e) {
			// TODO Auto-generated method stub
			return;
		}

		public void componentMoved(ComponentEvent e) {
			// TODO Auto-generated method stub
			return;
		}

		public void componentResized(ComponentEvent e) {
			// TODO Auto-generated method stub
			return;
		}

		public void componentShown(ComponentEvent e) {
			// TODO Auto-generated method stub
			return;
		}
		
	}
}
