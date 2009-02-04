/**
 * @(#)WindowFilter.java
 */

package aurora.hwc.config;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import aurora.hwc.*;
import aurora.util.*;


/**
 * Implementation of Filter window.
 * @author Alex Kurzhanskiy
 * @version $Id: WindowFilter.java,v 1.1.4.1 2008/10/16 04:27:06 akurzhan Exp $
 */
public final class WindowFilter extends JDialog implements ActionListener {
	private static final long serialVersionUID = -8646624624075264324L;
	
	private TreePane treePane = null;
	private int[] linkTypes;
	private Vector<JCheckBox> ltCB = new Vector<JCheckBox>();
	
	private final static String cmdOK = "pressedOK";
	private final static String cmdCancel = "pressedCancel";
	
	
	public WindowFilter() { }
	public WindowFilter(TreePane tpane, JFrame parent) {
		super(parent, "Filter Editor");
		treePane = tpane;
		linkTypes = TypesHWC.linkTypeArray();
		setSize(300, 400);
		setLocationRelativeTo(parent);
		setModal(true);
		JPanel panelMain = new JPanel(new BorderLayout());
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
		// add all subpanels to panel
		panelMain.add(fillLinkFilter(), BorderLayout.CENTER);
		panelMain.add(bp, BorderLayout.SOUTH);
		setContentPane(panelMain);
	}
	
	
	/**
	 * Creates Link filter panel.
	 */
	private JPanel fillLinkFilter() {
		//JPanel panel = new JPanel(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel pCB = new JPanel(new SpringLayout());
		pCB.setBorder(BorderFactory.createTitledBorder("Displayable Link Types"));
		int filter = 0;
		if (treePane != null)
			filter = treePane.getLinkFilter();
		for(int i = 0; i < linkTypes.length; i++) {
			JCheckBox cb = new JCheckBox();
			if ((filter & linkTypes[i]) == linkTypes[i])
				cb.setSelected(true);
			else
				cb.setSelected(false);
			pCB.add(cb);
			pCB.add(new JLabel(TypesHWC.typeString(linkTypes[i])));
			ltCB.add(cb);
		}
		SpringUtilities.makeCompactGrid(pCB, linkTypes.length, 2, 10, 10, 2, 10);
		//panel.add(pCB);
		return pCB;
	}
	
	/**
	 * Reaction to OK/Cancel buttons pressed.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmdOK.equals(cmd)) {
			int filter = 0;
			for (int i = 0; i < ltCB.size(); i++)
				if (ltCB.get(i).isSelected())
					filter |= linkTypes[i];
			if (treePane != null)
				treePane.setLinkFilter(filter);
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
