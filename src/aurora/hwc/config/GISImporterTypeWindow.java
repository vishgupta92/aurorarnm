/**
 * @(#)GISImporterTypeWindoiw.java
 */

package aurora.hwc.config;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;


/**
 * Implementation of type filter.
 * @author Jaimyoung Kwon
 * @version $Id: GISImporterTypeWindow.java,v 1.1.4.1.4.1 2009/10/01 00:13:12 akurzhan Exp $
 */
public class GISImporterTypeWindow extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

	private static final int NROWS = 10;
	private final static String cmdOK= "OK";
	private final static String cmdCancel= "Cancel";

	private DefaultListModel listModel;
	private JTextField valueText;
	private Container content ;
	private JButton yesButton = null;
	private JButton cancelButton = null;
	private boolean answer = false;
	private FeatureCollection featureCollection;
	private JList valueList = null;
	private JList attributeList = null;
	private String attributeName;


	public GISImporterTypeWindow(JFrame frame, boolean modal, FeatureCollection fc) {
		super(frame, "Road Types to Include", modal);

		this.featureCollection = fc;
		FeatureType featureType = featureCollection.getSchema();

		content = getContentPane();
		JPanel attributeListPane = new JPanel();
		attributeListPane.setBorder(BorderFactory.createTitledBorder(
				"Road Type Attribute"));
		/*
		 * attribute list
		 */
		attributeList = getAttributesList(featureType);		
		attributeList.setVisibleRowCount(NROWS);
		attributeListPane.add(new JScrollPane(attributeList));


		/*
		 * attribute value list 
		 */
		JPanel valueListPane = new JPanel();
		valueListPane.setBorder(BorderFactory.createTitledBorder(
				"Attributes to Use " +
				"(Shift for range; Ctrl for multi-selection)"));
		valueListPane.setLayout(new BoxLayout(valueListPane, BoxLayout.Y_AXIS));
		listModel = new DefaultListModel();
		valueList =  new JList(listModel);
		valueList.setSelectedIndex(0);
		valueList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		valueList.setVisibleRowCount(NROWS);
		valueListPane.add(new JScrollPane(valueList));
		valueListPane.add(new JLabel("... or input comma-separated list below:"));
		valueText = new JTextField();
		valueListPane.add(new JScrollPane(valueText));

		/*
		 * OK button and Cancel button
		 */
		JPanel buttonPane = new JPanel();
		buttonPane.setBorder(BorderFactory.createTitledBorder("Accept?"));
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));
		yesButton = new JButton("OK");
		yesButton.addActionListener(this);
		yesButton.setActionCommand(cmdOK);
		buttonPane.add(yesButton);    

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(cmdCancel);
		buttonPane.add(cancelButton);        

		yesButton.setAlignmentX(CENTER_ALIGNMENT);
		cancelButton.setAlignmentX(CENTER_ALIGNMENT);


		content.add(attributeListPane, BorderLayout.WEST);
		content.add(valueListPane, BorderLayout.CENTER);
		content.add(buttonPane, BorderLayout.EAST);

		pack();
		setLocationRelativeTo(frame);
		setVisible(true);
	}

	/**
	 * show names of Attributes in featureType 
	 * 
	 * @param featureType
	 * @param isGeometry
	 */
	private JList getAttributesList(final FeatureType featureType){
		Vector<String> tsv = new Vector<String>();
		final Vector<String> returnv = new Vector<String>();
		for (int i = 0; i < featureType.getAttributeCount(); i++) {
			AttributeType attributeType = featureType.getAttributeType(i);
			if ( !(attributeType instanceof GeometryAttributeType)) {
				tsv.add(attributeType.getLocalName()  + " : " + 
						attributeType.getBinding().getName());
				returnv.add(attributeType.getLocalName());
			}
		}
		final JList list =  new JList(tsv.toArray());
		list.setSelectedIndex(0);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				attributeName = returnv.get(list.getSelectedIndex());
				fillValueList(featureType, attributeName);
			}
		});
		return(list);
	}

	/**
	 * Show unique attribute values in the first 1000 records in the attribute
	 * 	
	 * @param featureType
	 * @param attribute
	 */
	@SuppressWarnings("unchecked")
	private void fillValueList(FeatureType featureType, String attribute){
		listModel.clear();
		HashSet<String> tsv2 = new HashSet<String>();
		if (attribute == null) return;
		int counter = 0;
		
		Iterator <Feature>iterator = featureCollection.iterator();
		for (int count = 0; iterator.hasNext(); count++) {
			Feature feature = iterator.next();
			tsv2.add(feature.getAttribute(attribute).toString());
			if (counter++ > 1000) break;
		}
		featureCollection.close( iterator );
		for(Iterator<String> it = tsv2.iterator(); it.hasNext(); ){
			listModel.addElement(it.next().toString());
		}
	}

	public String getValueText(){
		return(valueText.getText());
	}

	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmdOK.equals(cmd)) {
			answer = true;
			this.setVisible(false);
			return;
		}
		if (cmdCancel.equals(cmd)) {
			answer = false;
			this.setVisible(false);
			return;
		}
	}

	public boolean getAnswer() { 
		return answer; 
	}

	public String getAttributeName() {
		return attributeName;
	}

	public Object[] getAttributeValues() {
		return valueList.getSelectedValues();
	}

}