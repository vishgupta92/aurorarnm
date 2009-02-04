/**
 * @(#)PanelNewNode.java
 */

package aurora.hwc.config;

import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import aurora.*;
import aurora.hwc.*;
import aurora.util.*;


/**
 * New Node form to be displayed during the Node creation.
 * @author Alex Kurzhanskiy
 * @version $Id: PanelNewNode.java,v 1.1.4.1.2.2 2009/01/15 05:05:44 akurzhan Exp $
 */
public final class PanelNewNode extends AbstractNewNetworkElementPanel implements ActionListener {
	private static final long serialVersionUID = -4528657558983743245L;

	private String defaultName = "New Node";
	private int[] types;
	private JSpinner idSpinner;
	private JTextField nameText = new JTextField();
	private JTextPane descText = new JTextPane();
	private JComboBox listTypes = new JComboBox();
	private JTextField uriText = new JTextField();
	private JButton buttonBrowse = new JButton("Browse");
	private JPanel pBrowse = new JPanel(new SpringLayout());
	private final static String comboTypeList = "typeListCombo";
	private final static String cmdBrowse = "buttonBrowse";

	
	
	public PanelNewNode() { }
	public PanelNewNode(TreePane tpane, AbstractNodeComplex myNetwork, Point pointPosition) {
		super(tpane, myNetwork, pointPosition);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// ID
		JPanel pPrm = new JPanel(new SpringLayout());
		pPrm.setBorder(BorderFactory.createTitledBorder("ID"));
		idSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 99999999, 1));
		idSpinner.setEditor(new JSpinner.NumberEditor(idSpinner, "#######0"));
		pPrm.add(idSpinner);
		SpringUtilities.makeCompactGrid(pPrm, 1, 1, 2, 2, 2, 2);
		add(pPrm);
		// Name
		pPrm = new JPanel(new SpringLayout());
		pPrm.setBorder(BorderFactory.createTitledBorder("Name"));
		nameText.setText(defaultName);
		pPrm.add(nameText);
		SpringUtilities.makeCompactGrid(pPrm, 1, 1, 2, 2, 2, 2);
		add(pPrm);
		// Description
		pPrm = new JPanel(new BorderLayout());
		pPrm.setBorder(BorderFactory.createTitledBorder("Description"));
		JScrollPane descScroll = new JScrollPane(descText);
		pPrm.add(descScroll, BorderLayout.CENTER);
		add(pPrm);
		// Type
		pPrm = new JPanel(new SpringLayout());
		pPrm.setBorder(BorderFactory.createTitledBorder("Type"));
		types = TypesHWC.nodeTypeArray();
		for (int i = 0; i < types.length; i++)
			listTypes.addItem(TypesHWC.typeString(types[i]));
		listTypes.setActionCommand(comboTypeList);
		listTypes.addActionListener(this);
		pPrm.add(listTypes);
		SpringUtilities.makeCompactGrid(pPrm, 1, 1, 1, 2, 2, 2);
		add(pPrm);
		pBrowse.setBorder(BorderFactory.createTitledBorder("Existing Network"));
		pBrowse.add(uriText);
		buttonBrowse.setActionCommand(cmdBrowse);
		buttonBrowse.addActionListener(this);
		pBrowse.add(buttonBrowse);
		SpringUtilities.makeCompactGrid(pBrowse, 1, 2, 2, 2, 2, 2);
		pBrowse.setVisible(false);
		add(pBrowse);
		new WindowNewNetworkElement(this);
	}
	
	
	/**
	 * Specifies the window header.
	 */
	public String getHeader() {
		return defaultName;
	}
	

	/**
	 * Creates new Node in the given Network.
	 */
	public void create() {
		AbstractNode nnd = null;
		int idx = listTypes.getSelectedIndex();
		if ((types[idx] == TypesHWC.NETWORK_HWC) && (!uriText.getText().isEmpty())) {
			ContainerHWC mysys = new ContainerHWC();
			mysys.applicationConfiguration();
			boolean err = false;
			try {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(uriText.getText());
				mysys.initFromDOM(doc.getChildNodes().item(0));
			}
			catch(Exception e) {
				err = true;
				String buf = e.getMessage();
				if ((buf == null) || (buf.equals("")))
					buf = "Unknown error...";
				JOptionPane.showMessageDialog(this, buf, e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
			if (!err) {
				AbstractNodeComplex mynet = mysys.getMyNetwork();
				mynet.setContainer(myNetwork.getContainer());
				mynet.setTop(false);
				nnd = mynet;
			}
		}
		if (nnd == null) {
			try {
				Class c = Class.forName(TypesHWC.typeClassName(types[idx]));
				nnd = (AbstractNode)c.newInstance();
				if (types[idx] == TypesHWC.NETWORK_HWC)
					((NodeHWCNetwork)nnd).setContainer(myNetwork.getContainer());
			}
			catch(Exception e) {
				JOptionPane.showMessageDialog(this, "Cannot create Node of type '" + TypesHWC.typeClassName(types[idx]) + "'.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			nnd.setId((Integer)idSpinner.getValue());
			nnd.setName(nameText.getText());
			nnd.setDescription(descText.getText());
		}
		nnd.setMyNetwork(myNetwork);
		nnd.getPosition().set(pointPosition);
		myNetwork.addNode(nnd);
		tpane.addNodeComponent(nnd, myNetwork);
		return;
	}

	/**
	 * Reaction to 'Browse' button and changes in type list selection.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmdBrowse.equals(cmd)) {
			JFileFilter filter = new JFileFilter();
			filter.addType("xml");
			filter.setDescription("Aurora files (*.xml)");
			JFileChooser fc = new JFileChooser("Open File");
			fc.setFileFilter(filter);
			fc.setCurrentDirectory(tpane.getMainPane().getCurrentDir());
			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
				uriText.setText("file:" + fc.getSelectedFile().getAbsolutePath());
			tpane.getMainPane().setCurrentDir(fc.getCurrentDirectory());
		}
		if (comboTypeList.equals(cmd)) {
			int idx = listTypes.getSelectedIndex();
			if ((idx >= 0) && (types[idx] == TypesHWC.NETWORK_HWC))
				pBrowse.setVisible(true);
			else
				pBrowse.setVisible(false);
		}
		return;
	}

}
