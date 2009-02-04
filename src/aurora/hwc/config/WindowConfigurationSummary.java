/**
 * @(#)WindowConfigurationSummary.java
 */

package aurora.hwc.config;

import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import javax.swing.*;
import aurora.hwc.*;
import aurora.util.*;


/**
 * Window displaying the road network summary.
 * @author Alex Kurzhanskiy
 * @version $Id: WindowConfigurationSummary.java,v 1.1.4.1 2008/10/16 04:27:06 akurzhan Exp $
 */
public final class WindowConfigurationSummary extends JDialog implements ActionListener {
	private static final long serialVersionUID = -6238778669235911294L;
	
	protected ConfigurationSummaryHWC cfgS = null;
	
	
	public WindowConfigurationSummary() { }
	public WindowConfigurationSummary(ConfigurationSummaryHWC cs, JFrame parent) {
		super(parent, "Configuration Summary");
		cfgS = cs;
		if (cs == null)
			return;
		setSize(450, 400);
		setLocationRelativeTo(parent);
		setModal(true);
		JPanel panelMain = new JPanel(new BorderLayout());
		// OK button
		JPanel bp = new JPanel(new FlowLayout());
		JButton bOK = new JButton("    OK    ");
		bOK.addActionListener(this);
		bp.add(bOK);
		panelMain.add(genDataPanel(), BorderLayout.CENTER);
		panelMain.add(bp, BorderLayout.SOUTH);
		setContentPane(panelMain);
		setVisible(true);
	}
	
	
	/**
	 * Generates displayable data.
	 */
	private JPanel genDataPanel() {
		NumberFormat form = NumberFormat.getInstance();
		form.setMinimumFractionDigits(2);
		form.setMaximumFractionDigits(2);
		JPanel panel = new JPanel(new BorderLayout());
		Box dp = Box.createVerticalBox();
		JPanel b1 = new JPanel(new SpringLayout());
		b1.add(new JLabel("<html><font color=\"gray\"><u><b>Number of Networks:</b></u></font></html>"));
		b1.add(new JLabel("<html><font color=\"blue\"><b>" + cfgS.numberOfNetworks() + "</b></font></html>"));
		b1.add(new JLabel("<html><font color=\"gray\"><u><b>Number of Nodes:</b></u></font></html>"));
		b1.add(new JLabel("<html><font color=\"blue\"><b>" + cfgS.numberOfNodes() + "</b></font></html>"));
		b1.add(new JLabel("<html><font color=\"gray\"><u><b>Number of Links:</b></u></font></html>"));
		b1.add(new JLabel("<html><font color=\"blue\"><b>" + cfgS.numberOfLinks() + "</b></font></html>"));
		b1.add(new JLabel("<html><font color=\"gray\"><u><b>Number of Sources:</b></u></font></html>"));
		b1.add(new JLabel("<html><font color=\"blue\"><b>" + cfgS.numberOfSources() + "</b></font></html>"));
		b1.add(new JLabel("<html><font color=\"gray\"><u><b>Number of Destinations:</b></u></font></html>"));
		b1.add(new JLabel("<html><font color=\"blue\"><b>" + cfgS.numberOfDestinations() + "</b></font></html>"));
		SpringUtilities.makeCompactGrid(b1, 5, 2, 2, 10, 2, 10);
		dp.add(b1);
		JPanel b2 = new JPanel(new SpringLayout());
		b2.add(new JLabel("<html><font color=\"gray\"><u><b>Longest Link:</b></u></font></html>"));
		b2.add(new JLabel("<html><font color=\"blue\"><b>" + form.format(cfgS.longestLink().getLength()) + "mi [" + cfgS.longestLink() + "]</b></font></html>"));
		b2.add(new JLabel("<html><font color=\"gray\"><u><b>Shortest Link:</b></u></font></html>"));
		b2.add(new JLabel("<html><font color=\"blue\"><b>" + form.format(cfgS.shortstLink().getLength()) + "mi [" + cfgS.shortstLink() + "]</b></font></html>"));
		b2.add(new JLabel("<html><font color=\"gray\"><u><b>Fastest Link:</b></u></font></html>"));
		b2.add(new JLabel("<html><font color=\"blue\"><b>" + form.format(cfgS.fastestLink().getV()) + "mph [" + cfgS.fastestLink() + "]</b></font></html>"));
		b2.add(new JLabel("<html><font color=\"gray\"><u><b>Slowest Link:</b></u></font></html>"));
		b2.add(new JLabel("<html><font color=\"blue\"><b>" + form.format(cfgS.slowestLink().getV()) + "mph [" + cfgS.slowestLink() + "]</b></font></html>"));
		b2.add(new JLabel("<html><font color=\"gray\"><u><b>Shortest Travel Time:</b></u></font></html>"));
		b2.add(new JLabel("<html><font color=\"blue\"><b>" + Util.time2string(cfgS.minTTLink().getLength()/cfgS.minTTLink().getV()) + " [" + cfgS.minTTLink() + "]</b></font></html>"));
		SpringUtilities.makeCompactGrid(b2, 5, 2, 2, 20, 2, 10);
		dp.add(b2);
		panel.add(new JScrollPane(dp), BorderLayout.CENTER);
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
