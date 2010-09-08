/**
 * @(#)WindowAbout.java
 */

package aurora.hwc.common;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import aurora.*;
import aurora.hwc.*;
import aurora.util.*;


/**
 * Window displaying the 'about' info.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class WindowAbout extends JDialog implements ActionListener {
	private static final long serialVersionUID = 4945102996643747877L;

	private String text = "";

	public WindowAbout() { }
	public WindowAbout(String text, JFrame parent) {
		super(parent, "About...");
		this.text = text;
		setSize(450, 450);
		setLocationRelativeTo(parent);
		setModal(true);
		JPanel panelMain = new JPanel(new BorderLayout());
		// OK button
		JPanel bp = new JPanel(new FlowLayout());
		JButton bOK = new JButton("    OK    ");
		bOK.addActionListener(this);
		bp.add(bOK);
		panelMain.add(genAboutPanel(), BorderLayout.CENTER);
		panelMain.add(bp, BorderLayout.SOUTH);
		setContentPane(panelMain);
		setVisible(true);
	}
	
	
	/**
	 * Generates displayable info.
	 */
	private JPanel genAboutPanel() {
		URL url = null;
		JPanel panel = new JPanel(new BorderLayout());
		Box b = Box.createVerticalBox();
		JPanel p;
		p = new JPanel(new FlowLayout());
		p.add(new JLabel(UtilGUI.createImageIcon("/icons/aurora_logo0.gif")));
		b.add(p);
		p = new JPanel(new FlowLayout());
		p.add(new JLabel("<html><h2>" + AuroraHWCConstants.NAME + "</h2></html>"));
		b.add(p);
		p = new JPanel(new SpringLayout());
		p.add(new JLabel("Version: "));
		p.add(new JLabel(AuroraHWCConstants.VERSION));
		p.add(new JLabel("Module: "));
		p.add(new JLabel(text));
		p.add(new JLabel("External packages used: "));
		p.add(new JLabel());
		p.add(new JLabel());
		try {
			url = new URL("http://jung.sourceforge.net/");
		}
		catch(Exception e) { url = null; }
		p.add(new HyperlinkLabel("JUNG", url));
		p.add(new JLabel());
		try {
			url = new URL("http://jfree.org");
		}
		catch(Exception e) { url = null; }
		p.add(new HyperlinkLabel("JFreeChart", url));
		SpringUtilities.makeCompactGrid(p, 5, 2, 2, 3, 2, 2);
		b.add(new JScrollPane(p));
		p = new JPanel(new FlowLayout());
		p.add(new JLabel("<html><p><p>" + AuroraConstants.COPYRIGHT + "</html>"));
		b.add(p);
		p = new JPanel(new FlowLayout());
		p.add(new JLabel("Please, visit us at "));
		try {
			url = new URL(AuroraConstants.CONTACT_WWW);
		}
		catch(Exception e) { url = null; }
		p.add(new HyperlinkLabel(url.toString(), url));
		b.add(p);
		panel.add(b);
		return panel;
	}
	
	/**
	 * Reaction to OK button pressed.
	 */
	public void actionPerformed(ActionEvent e) {
		setVisible(false);
		dispose();
		return;
	}

}
