/**
 * @(#)WindowNewNetworkElement.java
 */

package aurora.hwc.config;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * Implementation of a dialog window that pops up during the creation
 * of the new Network Element.
 * @author Alex Kurzhanskiy
 * @version $Id: WindowNewNetworkElement.java,v 1.1.4.1.2.1 2009/01/14 19:47:15 akurzhan Exp $
 */
public final class WindowNewNetworkElement extends JDialog {
	private static final long serialVersionUID = 7653734936189912624L;
	
	private AbstractNewNetworkElementPanel newNEPanel;
	private final static String cmdOK = "pressedOK";
	private final static String cmdCancel = "pressedCancel";
	
	public WindowNewNetworkElement() { }
	public WindowNewNetworkElement(AbstractNewNetworkElementPanel newNEPanel) {
		super((JFrame)null, newNEPanel.getHeader());
		this.newNEPanel = newNEPanel;
		setSize(300, 400);
		setLocationRelativeTo(null);
		setModal(true);
		setContentPane(createForm());
		setVisible(true);
	}
	
	/**
	 * Creates the displayable form.
	 */
	private JPanel createForm() {
		JPanel panel = new JPanel(new BorderLayout());
		// OK, Cancel buttons
		JPanel bp = new JPanel(new FlowLayout());
		JButton bOK = new JButton("    OK    ");
		bOK.setActionCommand(cmdOK);
		bOK.addActionListener(new ButtonEventsListener());
		JButton bCancel = new JButton("Cancel");
		bCancel.setActionCommand(cmdCancel);
		bCancel.addActionListener(new ButtonEventsListener());
		bp.add(bOK);
		bp.add(bCancel);
		// add all subpanels to panel
		panel.add(newNEPanel, BorderLayout.CENTER);
		panel.add(bp, BorderLayout.SOUTH);
		return panel;
	}

	
	/**
	 * This class is needed to react to OK/Cancel buttons pressed.
	 */
	private class ButtonEventsListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String cmd = e.getActionCommand();
			if (cmdOK.equals(cmd)) {
				newNEPanel.create();
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
