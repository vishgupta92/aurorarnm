/**
 * @(#)WindowQControllerEditor.java
 */

package aurora.hwc.control;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
 * Implementation of Queue Controller Editor window.
 * @author Alex Kurzhanskiy
 * @version $Id: WindowQControllerEditor.java,v 1.1.4.1 2008/10/16 04:27:07 akurzhan Exp $
 */
public final class WindowQControllerEditor extends JDialog {
	private static final long serialVersionUID = -5564887467060912284L;
	
	private AbstractQControllerPanel myQCP;
	private final static String cmdOK = "pressedOK";
	private final static String cmdCancel = "pressedCancel";
	
	
	public WindowQControllerEditor() { }
	public WindowQControllerEditor(AbstractQControllerPanel cp, JFrame parent) {
		super(parent, "Queue Controller: " + cp.getHeader());
		myQCP = cp;
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
		panel.add(myQCP, BorderLayout.CENTER);
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
				myQCP.save();
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