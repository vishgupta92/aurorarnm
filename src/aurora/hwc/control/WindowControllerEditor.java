/**
 * @(#)WindowControllerEditor.java
 */

package aurora.hwc.control;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * Implementation of Controller Editor window.
 * @author Alex Kurzhanskiy
 * @version $Id: WindowControllerEditor.java,v 1.1.4.1.4.3 2009/10/01 05:49:01 akurzhan Exp $
 */
public final class WindowControllerEditor extends JDialog {
	private static final long serialVersionUID = -4237759832291252238L;
	
	private AbstractPanelController myCP;
	private final static String cmdOK = "pressedOK";
	private final static String cmdCancel = "pressedCancel";
	
	
	public WindowControllerEditor() { }
	public WindowControllerEditor(AbstractPanelController cp, JFrame parent) {
		super(parent, "Controller: " + cp.getHeader());
		myCP = cp;
		setSize(600, 600);
		setLocationRelativeTo(parent);
		setModal(true);
		setContentPane(createForm());
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
		panel.add(myCP, BorderLayout.CENTER);
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
				myCP.save();
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