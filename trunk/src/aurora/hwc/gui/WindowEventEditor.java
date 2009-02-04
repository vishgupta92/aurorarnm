/**
 * @(#)WindowEventEditor.java
 */

package aurora.hwc.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * Implementation of Event Editor window.
 * @author Alex Kurzhanskiy
 * @version $Id: WindowEventEditor.java,v 1.1.2.2 2007/05/02 16:06:12 akurzhan Exp $
 */
public final class WindowEventEditor extends JDialog {
	private static final long serialVersionUID = -5783609920269884443L;
	
	private AbstractEventPanel myEP;
	private final static String cmdOK = "pressedOK";
	private final static String cmdCancel = "pressedCancel";
	
	
	public WindowEventEditor() { }
	public WindowEventEditor(AbstractEventPanel ep, JFrame parent) {
		super(parent, "Event: " + ep.getHeader());
		myEP = ep;
		setSize(300, 400);
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
		panel.add(myEP, BorderLayout.CENTER);
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
				myEP.save();
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