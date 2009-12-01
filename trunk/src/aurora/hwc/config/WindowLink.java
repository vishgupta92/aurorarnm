/**
 * @(#)WindowLink.java
 */

package aurora.hwc.config;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;
import aurora.*;
import aurora.util.*;
import aurora.hwc.*;
import aurora.hwc.util.*;


/**
 * Implementation of Link Editor.
 * @author Alex Kurzhanskiy
 * @version $Id: WindowLink.java,v 1.1.4.1.2.6.2.4 2009/10/18 00:58:29 akurzhan Exp $
 */
public final class WindowLink extends JInternalFrame implements ActionListener, ChangeListener {
	private static final long serialVersionUID = -55251513483090686L;
	
	private AbstractContainer mySystem = null;
	private Vector<AbstractNetworkElement> linkList;
	private TreePane treePane;
	
	private MyXYSeries ffFD = new MyXYSeries("Free Flow");
	private MyXYSeries cFD = new MyXYSeries("Congestion");
	private MyXYSeries cdFD = new MyXYSeries("Capacity Drop");
	private JFreeChart fdChart;
	
	private double mf;
	private double cr;
	private double drp;
	private double cd;
	private double jd;
	
	private JComboBox cbSave = new JComboBox();
	private JComboBox typeList = new JComboBox();
	private JSpinner idSpinner;
	private JSpinner lengthSpinner;
	private JSpinner widthSpinner;
	private JSpinner qlimSpinner;
	private JTextField densityTF;
	private JTextField dcTF;
	private JSpinner capacitySpinner;
	private JSpinner crSpinner;
	private JSpinner capdropSpinner;
	private JSpinner dencritSpinner;
	private JSpinner denjamSpinner;
	private JSpinner vffSpinner;
	private JSpinner wcSpinner;
	private JSpinner hh;
	private JSpinner mm;
	private JSpinner ss;
	private JSpinner hhC;
	private JSpinner mmC;
	private JSpinner ssC;
	private JTextPane demandProfile = new JTextPane();
	private JTextPane capacityProfile = new JTextPane();
	private JPanel pTypes = new JPanel(new SpringLayout());
	private JPanel pSave = new JPanel(new SpringLayout());
	private JPanel pID = new JPanel(new SpringLayout());
	private JPanel pLngth = new JPanel(new SpringLayout());
	private JPanel pWdth = new JPanel(new SpringLayout());
	private JPanel pQLim = new JPanel(new SpringLayout());
	private JPanel pDen = new JPanel(new SpringLayout());
	private Box fdp = Box.createVerticalBox();
	private JPanel pCR = new JPanel(new SpringLayout());
	private JPanel pDC = new JPanel(new SpringLayout());
	private JPanel pT = new JPanel(new FlowLayout());
	private JPanel pDP = new JPanel(new BorderLayout());
	private JPanel pTC = new JPanel(new FlowLayout());
	private JPanel pCP = new JPanel(new BorderLayout());
	private final static String nmTypeList = "TypeList";
	private final static String nmToSave = "ToSave";
	private final static String nmID = "ID";
	private final static String nmLength = "LengthSpin";
	private final static String nmWidth = "WidthSpin";
	private final static String nmQLim = "QLimSpin";
	private final static String nmCapacity = "CapacitySpin";
	private final static String nmCapRange = "CapRangeSpin";
	private final static String nmCapDrop = "CapDropSpin";
	private final static String nmDenCrit = "CriticalDensitySpin";
	private final static String nmDenJam = "JamDensitySpin";
	private final static String nmVff = "VFreeFlowSpin";
	private final static String nmWc = "CongestionWaveSpeedSpin";
	private final static String nmTP = "TimePeriod";
	private final static String nmTPC = "TimePeriodC";
	private boolean idModified = false;
	private boolean lengthModified = false;
	private boolean widthModified = false;
	private boolean qlimModified = false;
	private boolean denModified = false;
	private boolean typeModified = false;
	private boolean fdModified = false;
	private boolean dcModified = false;
	private boolean tpModified = false;
	private boolean dpModified = false;
	private boolean ctpModified = false;
	private boolean cpModified = false;
	private boolean crModified = false;
	private boolean saveModified = false;
	
	private int[] linkTypes;
	
	private final static String cmdOK = "pressedOK";
	private final static String cmdCancel = "pressedCancel";
	
	
	public WindowLink() { }
	public WindowLink(AbstractContainer ctnr, Vector<AbstractNetworkElement> nelist, TreePane tpane) {
		super("Link Editor", true, true, true, true);
		mySystem = ctnr;
		linkList = nelist;
		treePane = tpane;
		setSize(360, 450);
		int n = treePane.getInternalFrameCount();
		setLocation(20*n, 20*n);
		AdapterWindowLink listener = new AdapterWindowLink();
		addInternalFrameListener(listener);
		addComponentListener(listener);
		JPanel panelMain = new JPanel(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.add("Links", fillTabLinks());
		tabbedPane.add("General", fillTabGeneral());
		tabbedPane.add("Fundamental Diagram", fillTabFD());
		tabbedPane.add("Demand", fillTabDemand());
		tabbedPane.add("Capacity", fillTabCapacity());
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
		panelMain.add(tabbedPane, BorderLayout.CENTER);
		panelMain.add(bp, BorderLayout.SOUTH);
		setContentPane(panelMain);
	}
	
	
	/**
	 * Creates links tab.
	 */
	private JPanel fillTabLinks() {
		AbstractLinkHWC lnk = (AbstractLinkHWC)linkList.firstElement();
		JPanel panel = new JPanel(new BorderLayout());
		Box linkPanel = Box.createVerticalBox();
		// Link list
		JPanel pLnks = new JPanel(new BorderLayout());
		pLnks.setBorder(BorderFactory.createTitledBorder("Edited Links"));
		String txt = "";
		for (int i = 0; i < linkList.size(); i++)
			txt += (i+1) + ") " + linkList.get(i) + "\n";
		JTextPane lnksTxt = new JTextPane();
		JScrollPane scrlPn = new JScrollPane(lnksTxt);
		lnksTxt.setText(txt);
		lnksTxt.setEditable(false);
		pLnks.add(scrlPn, BorderLayout.CENTER);
		linkPanel.add(pLnks);
		// Link types
		linkTypes = TypesHWC.linkTypeArray();
		int sidx = 0;
		for (int i = 0; i < linkTypes.length; i++) {
			typeList.addItem(TypesHWC.typeString(linkTypes[i]));
			if (linkTypes[i] == lnk.getType())
				sidx = i;
		}
		typeList.setSelectedIndex(sidx);
		typeList.setActionCommand(nmTypeList);
		typeList.addActionListener(this);
		pTypes.setBorder(BorderFactory.createTitledBorder("Link Type"));
		pTypes.add(typeList);
		SpringUtilities.makeCompactGrid(pTypes, 1, 1, 2, 2, 2, 2);
		linkPanel.add(pTypes);
		// Record
		cbSave.addItem("No");
		cbSave.addItem("Yes");
		if (lnk.toSave())
			cbSave.setSelectedIndex(1);
		else
			cbSave.setSelectedIndex(0);
		cbSave.setActionCommand(nmToSave);
		cbSave.addActionListener(this);
		pSave.setBorder(BorderFactory.createTitledBorder("Record State"));
		pSave.add(cbSave);
		SpringUtilities.makeCompactGrid(pSave, 1, 1, 2, 2, 2, 2);
		linkPanel.add(pSave);
		// Neighbors
		if (linkList.size() == 1) {
			JPanel pNeighbors = new JPanel(new SpringLayout());
			pNeighbors.setBorder(BorderFactory.createTitledBorder(""));
			int cnt = 0;
			if (lnk.getPredecessors().size() > 0) {
				final AbstractNodeHWC nd = (AbstractNodeHWC)lnk.getBeginNode();
				pNeighbors.add(new JLabel("Begin Node: "));
				JLabel l = new JLabel("<html><a href=\"\">" + nd + "</a></html>");
				l.setToolTipText("Open Node '" + nd + "'");
				l.addMouseListener(new MouseAdapter() { 
			      	  public void mouseClicked(MouseEvent e) {
			      		Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
						nelist.add(nd);
		      	    	treePane.actionSelected(nelist, true, false);
		      	    	return;
			      	  }
			      	  public void mouseEntered(MouseEvent e) {
			    		e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			    		return;
			    	  }
				    });
				pNeighbors.add(l);
				cnt++;
			}
			if (lnk.getSuccessors().size() > 0) {
				final AbstractNodeHWC nd = (AbstractNodeHWC)lnk.getEndNode();
				pNeighbors.add(new JLabel("   End Node: "));
				JLabel l = new JLabel("<html><a href=\"\">" + nd + "</a></html>");
				l.setToolTipText("Open Node '" + nd + "'");
				l.addMouseListener(new MouseAdapter() { 
			      	  public void mouseClicked(MouseEvent e) {
			      		Vector<AbstractNetworkElement> nelist = new Vector<AbstractNetworkElement>();
						nelist.add(nd);
		      	    	treePane.actionSelected(nelist, true, false);
		      	    	return;
			      	  }
			      	  public void mouseEntered(MouseEvent e) {
			    		e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			    		return;
			    	  }
				    });
				pNeighbors.add(l);
				cnt++;
			}
			SpringUtilities.makeCompactGrid(pNeighbors, cnt, 2, 2, 2, 2, 2);
			linkPanel.add(pNeighbors);
		}
		panel.add(linkPanel);
		return panel;
	}
	
	/**
	 * Creates general parameters tab.
	 */
	private JPanel fillTabGeneral() {
		AbstractLinkHWC lnk = (AbstractLinkHWC)linkList.firstElement();
		JPanel panel = new JPanel(new BorderLayout());
		Box genPanel = Box.createVerticalBox();
		// ID if there is a single link
		if (linkList.size() == 1) {
			pID.setBorder(BorderFactory.createTitledBorder("ID"));
			idSpinner = new JSpinner(new SpinnerNumberModel(lnk.getId(), -999999999, 999999999, 1));
			idSpinner.setEditor(new JSpinner.NumberEditor(idSpinner, "#######0"));
			idSpinner.setName(nmID);
			idSpinner.addChangeListener(this);
			pID.add(idSpinner);
			SpringUtilities.makeCompactGrid(pID, 1, 1, 2, 2, 2, 2);
			genPanel.add(pID);
		}
		// Length
		pLngth.setBorder(BorderFactory.createTitledBorder("Length (miles)"));
		lengthSpinner = new JSpinner(new SpinnerNumberModel(lnk.getLength(), 0.0, 999.99, 0.01));
		lengthSpinner.setEditor(new JSpinner.NumberEditor(lengthSpinner, "##0.00"));
		lengthSpinner.setName(nmLength);
		lengthSpinner.addChangeListener(this);
		pLngth.add(lengthSpinner);
		SpringUtilities.makeCompactGrid(pLngth, 1, 1, 2, 2, 2, 2);
		genPanel.add(pLngth);
		// Width (number of Lanes)
		pWdth.setBorder(BorderFactory.createTitledBorder("Number of Lanes"));
		widthSpinner = new JSpinner(new SpinnerNumberModel(lnk.getLanes(), 1.0, 20.0, 1.0));
		widthSpinner.setEditor(new JSpinner.NumberEditor(widthSpinner, "#0.0"));
		widthSpinner.setName(nmWidth);
		widthSpinner.addChangeListener(this);
		pWdth.add(widthSpinner);
		SpringUtilities.makeCompactGrid(pWdth, 1, 1, 2, 2, 2, 2);
		genPanel.add(pWdth);
		// Queue Limit
		pQLim.setBorder(BorderFactory.createTitledBorder("Queue Limit"));
		qlimSpinner = new JSpinner(new SpinnerNumberModel(lnk.getQueueMax(), 0, 99999, 1.0));
		qlimSpinner.setEditor(new JSpinner.NumberEditor(qlimSpinner, "###0"));
		qlimSpinner.setName(nmQLim);
		qlimSpinner.addChangeListener(this);
		pQLim.add(qlimSpinner);
		SpringUtilities.makeCompactGrid(pQLim, 1, 1, 2, 2, 2, 2);
		genPanel.add(pQLim);
		// Initial Density
		pDen.setBorder(BorderFactory.createTitledBorder("Initial Density (vpm)"));
		densityTF = new JTextField(((AuroraIntervalVector)lnk.getInitialDensity()).toStringWithInverseWeights(((SimulationSettingsHWC)mySystem.getMySettings()).getVehicleWeights(), true));
		densityTF.getDocument().addDocumentListener(new DensityChangeListener());
		pDen.add(densityTF);
		SpringUtilities.makeCompactGrid(pDen, 1, 1, 2, 2, 2, 2);
		genPanel.add(pDen);
		panel.add(genPanel);
		return panel;
	}
	
	/**
	 * Updates spinner values.
	 */
	private void updateSpinners() {
		capacitySpinner.setValue((Double)mf);
		capdropSpinner.setValue((Double)drp);
		dencritSpinner.setValue((Double)cd);
		denjamSpinner.setValue((Double)jd);
		vffSpinner.setValue((Double)(mf/cd));
		wcSpinner.setValue((Double)(mf/(jd - cd)));
		return;
	}
	
	/**
	 * Updates fundamental diagram data.
	 */
	private void updateFDSeries() {
		if (ffFD.getItemCount() == 0) {
			ffFD.add(0.0, 0.0);
			ffFD.add(0.0, 0.0);
		}
		if (cFD.getItemCount() == 0) {
			cFD.add(0.0, 0.0);
			cFD.add(0.0, 0.0);
		}
		if (cdFD.getItemCount() == 0) {
			cdFD.add(0.0, 0.0);
			cdFD.add(0.0, 0.0);
		}
		ffFD.setDataItem(1, new XYDataItem(cd, mf));
		cFD.setDataItem(0, new XYDataItem(cd, mf));
		cFD.setDataItem(1, new XYDataItem(jd, 0.0));
		cdFD.setDataItem(0, new XYDataItem(cd, mf - drp));
		cdFD.setDataItem(1, new XYDataItem(jd, mf - drp));
		ffFD.fireSeriesChanged();
		cFD.fireSeriesChanged();
		cdFD.fireSeriesChanged();
		return;
	}
	
	/**
	 * Creates fundamental diagram chart.
	 */
	private JFreeChart makeFDChart() {
		updateFDSeries();
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(ffFD);
		dataset.addSeries(cFD);
		dataset.addSeries(cdFD);
		JFreeChart chart = ChartFactory.createXYLineChart(
							null, // chart title
							"Density (vpml)", // x axis label
							"Flow (vphl)", // y axis label
							dataset, // data
							PlotOrientation.VERTICAL,
							false, // include legend
							false, // tooltips
							false // urls
							);
		XYPlot plot = (XYPlot)chart.getPlot();
		plot.getRenderer().setSeriesPaint(0, Color.GREEN);
		plot.getRenderer().setSeriesPaint(1, Color.RED);
		plot.getRenderer().setSeriesPaint(2, Color.BLUE);
		plot.getRenderer().setStroke(new BasicStroke(2));
		return chart;
	}

	/**
	 * Creates fundamental diagram tab.
	 */
	private JPanel fillTabFD() {
		AbstractLinkHWC lnk = (AbstractLinkHWC)linkList.firstElement();
		mf = lnk.getMaxFlow() / lnk.getLanes();
		drp = lnk.getCapacityDrop() / lnk.getLanes();
		drp = Math.max(0, Math.min(mf, drp));
		cd = lnk.getCriticalDensity() / lnk.getLanes();
		jd = lnk.getJamDensity() / lnk.getLanes();
		JPanel panel = new JPanel(new BorderLayout());
		Box genPanel = Box.createVerticalBox();
		fdp.setBorder(BorderFactory.createTitledBorder("Fundamental Diagram per Lane"));
		fdChart = makeFDChart();
		ChartPanel cp = new ChartPanel(fdChart);
		cp.setMinimumDrawWidth(100);
		cp.setMinimumDrawHeight(60);
		cp.setPreferredSize(new Dimension(250, 80));
		fdp.add(new JScrollPane(cp));
		JPanel prmp = new JPanel(new SpringLayout());
		JLabel l = new JLabel("Capacity:", JLabel.TRAILING);
		prmp.add(l);
		capacitySpinner = new JSpinner(new SpinnerNumberModel(mf, 0, 99999, 1.0));
		capacitySpinner.setEditor(new JSpinner.NumberEditor(capacitySpinner, "####0.00"));
		capacitySpinner.addChangeListener(this);
		capacitySpinner.setName(nmCapacity);
		l.setLabelFor(capacitySpinner);
		prmp.add(capacitySpinner);
		l = new JLabel("Cap.Drop:", JLabel.TRAILING);
		prmp.add(l);
		capdropSpinner = new JSpinner(new SpinnerNumberModel(drp, 0, 99999, 1.0));
		capdropSpinner.setEditor(new JSpinner.NumberEditor(capdropSpinner, "####0.00"));
		capdropSpinner.addChangeListener(this);
		capdropSpinner.setName(nmCapDrop);
		l.setLabelFor(capdropSpinner);
		prmp.add(capdropSpinner);
				l = new JLabel("C.Density:", JLabel.TRAILING);
		prmp.add(l);
		dencritSpinner = new JSpinner(new SpinnerNumberModel(cd, 0, 99999, 1.0));
		dencritSpinner.setEditor(new JSpinner.NumberEditor(dencritSpinner, "####0.00"));
		dencritSpinner.addChangeListener(this);
		dencritSpinner.setName(nmDenCrit);
		l.setLabelFor(dencritSpinner);
		prmp.add(dencritSpinner);
		l = new JLabel("  V:", JLabel.TRAILING);
		prmp.add(l);
		vffSpinner = new JSpinner(new SpinnerNumberModel(mf/cd, 0, 200, 1.0));
		vffSpinner.setEditor(new JSpinner.NumberEditor(vffSpinner, "#0.00"));
		vffSpinner.addChangeListener(this);
		vffSpinner.setName(nmVff);
		l.setLabelFor(vffSpinner);
		prmp.add(vffSpinner);
		l = new JLabel("J.Density:", JLabel.TRAILING);
		prmp.add(l);
		denjamSpinner = new JSpinner(new SpinnerNumberModel(jd, 0, 99999, 1.0));
		denjamSpinner.setEditor(new JSpinner.NumberEditor(denjamSpinner, "####0.00"));
		denjamSpinner.addChangeListener(this);
		denjamSpinner.setName(nmDenJam);
		l.setLabelFor(denjamSpinner);
		prmp.add(denjamSpinner);
		l = new JLabel("  W:", JLabel.TRAILING);
		prmp.add(l);
		if (jd == cd)
			jd = cd + 1;
		int ulim = (int)Math.max(Math.ceil(mf/(jd - cd)), 999);
		wcSpinner = new JSpinner(new SpinnerNumberModel(mf/(jd - cd), 0, ulim, 1.0));
		wcSpinner.setEditor(new JSpinner.NumberEditor(wcSpinner, "#0.00"));
		wcSpinner.addChangeListener(this);
		wcSpinner.setName(nmWc);
		l.setLabelFor(wcSpinner);
		prmp.add(wcSpinner);
		SpringUtilities.makeCompactGrid(prmp, 3, 4, 2, 2, 2, 2);
		fdp.add(prmp);
		genPanel.add(fdp);
		// Capacity uncertainty
		pCR.setBorder(BorderFactory.createTitledBorder("Capacity Uncertainty +/- (vphl)"));
		crSpinner = new JSpinner(new SpinnerNumberModel((lnk.getMaxFlowRange().getSize()/lnk.getLanes())/2, 0.0, 999.99, 1.0));
		crSpinner.setEditor(new JSpinner.NumberEditor(crSpinner, "##0.00"));
		crSpinner.setName(nmCapRange);
		crSpinner.addChangeListener(this);
		pCR.add(crSpinner);
		SpringUtilities.makeCompactGrid(pCR, 1, 1, 2, 2, 2, 2);
		genPanel.add(pCR);
		panel.add(genPanel);
		return panel;
	}
	
	
	/**
	 * Creates demand tab.
	 */
	private JPanel fillTabDemand() {
		AbstractLinkHWC lnk = (AbstractLinkHWC)linkList.firstElement();
		JPanel panel = new JPanel(new BorderLayout());
		Box demandPanel = Box.createVerticalBox();
		// Demand Coefficient(s)
		pDC.setBorder(BorderFactory.createTitledBorder("Demand Coefficient(s)"));
		dcTF = new JTextField(lnk.getDemandKnobsAsString());
		dcTF.getDocument().addDocumentListener(new DCChangeListener());
		pDC.add(dcTF);
		SpringUtilities.makeCompactGrid(pDC, 1, 1, 2, 2, 2, 2);
		demandPanel.add(pDC);
		// Sampling Period
		pT.setBorder(BorderFactory.createTitledBorder("Sampling Period"));
		hh = new JSpinner(new SpinnerNumberModel(Util.getHours(lnk.getDemandTP()), 0, 99, 1));
		hh.setEditor(new JSpinner.NumberEditor(hh, "00"));
		hh.setName(nmTP);
		hh.addChangeListener(this);
		pT.add(hh);
		pT.add(new JLabel("h "));
		mm = new JSpinner(new SpinnerNumberModel(Util.getMinutes(lnk.getDemandTP()), 0, 59, 1));
		mm.setEditor(new JSpinner.NumberEditor(mm, "00"));
		mm.setName(nmTP);
		mm.addChangeListener(this);
		pT.add(mm);
		pT.add(new JLabel("m "));
		ss = new JSpinner(new SpinnerNumberModel(Util.getSeconds(lnk.getDemandTP()), 0, 59.99, 1));
		ss.setEditor(new JSpinner.NumberEditor(ss, "00.##"));
		ss.setName(nmTP);
		ss.addChangeListener(this);
		pT.add(ss);
		pT.add(new JLabel("s"));
		demandPanel.add((pT));
		// Demand Profile
		pDP.setBorder(BorderFactory.createTitledBorder("Demand Profile"));
		demandProfile.setText(lnk.getDemandVectorAsString());
		demandProfile.getStyledDocument().addDocumentListener(new DPChangeListener());
		pDP.add(new JScrollPane(demandProfile));
		demandPanel.add(pDP);
		panel.add(demandPanel);
		return panel;
	}
	
	/**
	 * Creates capacity tab.
	 */
	private JPanel fillTabCapacity() {
		AbstractLinkHWC lnk = (AbstractLinkHWC)linkList.firstElement();
		JPanel panel = new JPanel(new BorderLayout());
		Box capPanel = Box.createVerticalBox();
		// Sampling Period
		pTC.setBorder(BorderFactory.createTitledBorder("Sampling Period"));
		hhC = new JSpinner(new SpinnerNumberModel(Util.getHours(lnk.getDemandTP()), 0, 99, 1));
		hhC.setEditor(new JSpinner.NumberEditor(hh, "00"));
		hhC.setName(nmTPC);
		hhC.addChangeListener(this);
		pTC.add(hhC);
		pTC.add(new JLabel("h "));
		mmC = new JSpinner(new SpinnerNumberModel(Util.getMinutes(lnk.getDemandTP()), 0, 59, 1));
		mmC.setEditor(new JSpinner.NumberEditor(mmC, "00"));
		mmC.setName(nmTPC);
		mmC.addChangeListener(this);
		pTC.add(mmC);
		pTC.add(new JLabel("m "));
		ssC = new JSpinner(new SpinnerNumberModel(Util.getSeconds(lnk.getDemandTP()), 0, 59.99, 1));
		ssC.setEditor(new JSpinner.NumberEditor(ssC, "00.##"));
		ssC.setName(nmTPC);
		ssC.addChangeListener(this);
		pTC.add(ssC);
		pTC.add(new JLabel("s"));
		capPanel.add((pTC));
		// Capacity Profile
		pCP.setBorder(BorderFactory.createTitledBorder("Capacity Profile"));
		capacityProfile.setText(lnk.getCapacityVectorAsString());
		capacityProfile.getStyledDocument().addDocumentListener(new CPChangeListener());
		pCP.add(new JScrollPane(capacityProfile));
		capPanel.add(pCP);
		panel.add(capPanel);
		return panel;
	}
	
	/**
	 * Action performed before closing the frame.
	 */
	private void close() {
		treePane.removeFrame(this, linkList);
		return;
	}
	
	/**
	 * Performs link provisioning.
	 */
	private void provisionLinkData() {
		for (int i = 0; i < linkList.size(); i++) {
			AbstractLinkHWC lnk = (AbstractLinkHWC)linkList.get(i);
			if (idModified)
				lnk.setId((Integer)idSpinner.getValue());
			if (lengthModified)
				lnk.setLength((Double)lengthSpinner.getValue());
			if (widthModified)
				lnk.setLanes((Double)widthSpinner.getValue());
			if (qlimModified)
				lnk.setQueueMax((Double)qlimSpinner.getValue());
			if (denModified) {
				AuroraIntervalVector denVec = new AuroraIntervalVector();
				denVec.setRawIntervalVectorFromString(densityTF.getText());
				denVec.affineTransform(((SimulationSettingsHWC)mySystem.getMySettings()).getVehicleWeights(), 0);
				lnk.setInitialDensity(denVec);
			}
			if (fdModified)
				lnk.setFD(mf*lnk.getLanes(), cd*lnk.getLanes(), jd*lnk.getLanes(), drp*lnk.getLanes());
			if (crModified)
				lnk.setMaxFlowRange(cr*lnk.getLanes()*2);
			if (dcModified)
				lnk.setDemandKnobs(dcTF.getText());
			if (dpModified)
				lnk.setDemandVector(demandProfile.getText());
			if (cpModified)
				lnk.setCapacityVector(capacityProfile.getText());
			if (tpModified) {
				int h = (Integer)hh.getValue();
				int m = (Integer)mm.getValue();
				double s = (Double)ss.getValue();
				lnk.setDemandTP(h + (m/60.0) + (s/3600.0));
			}
			if (ctpModified) {
				int h = (Integer)hhC.getValue();
				int m = (Integer)mmC.getValue();
				double s = (Double)ssC.getValue();
				lnk.setCapacityTP(h + (m/60.0) + (s/3600.0));
			}
			if (typeModified) {
				AbstractLink newlk = null;
				try {
 					Class c = Class.forName(TypesHWC.typeClassName(linkTypes[typeList.getSelectedIndex()]));
					newlk = (AbstractLinkHWC)c.newInstance();
				}
				catch(Exception e) {
					JOptionPane.showMessageDialog(this, "Cannot create Link of type '" + TypesHWC.typeClassName(linkTypes[typeList.getSelectedIndex()]) + "'.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				newlk.copyData(lnk);
				lnk.getTop().replaceNetworkElement(lnk, newlk);
			}
			if (saveModified)
				lnk.setSave((cbSave.getSelectedIndex() == 1));
			try {
				lnk.initialize();
			}
			catch(Exception e) { }
		}
		return;
	}
	
	/**
	 * Reaction to OK/Cancel buttons pressed and changes in Type combo box.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmdOK.equals(cmd)) {
			if (idModified || lengthModified || widthModified || qlimModified || denModified || fdModified || crModified || dcModified || dpModified || tpModified || cpModified || ctpModified || typeModified || saveModified) {
				provisionLinkData();
				mySystem.getMyStatus().setSaved(false);
			}
			if (idModified || typeModified)
				treePane.modifyLinkComponents(linkList);
			setVisible(false);
			close();
			dispose();
			return;
		}
		if (cmdCancel.equals(cmd)) {
			setVisible(false);
			close();
			dispose();
			return;
		}
		if (cmd.equals(nmTypeList)) {
			typeModified = true;
			pTypes.setBorder(BorderFactory.createTitledBorder("*Link Type"));
			return;
		}
		if (cmd.equals(nmToSave)) {
			saveModified = true;
			pSave.setBorder(BorderFactory.createTitledBorder("*Record State"));
			return;
		}
		return;	
	}

	/**
	 * Reaction to spinner and text field changes.
	 */
	public void stateChanged(ChangeEvent e) {
		String nm = ((JComponent)e.getSource()).getName();
		if (nm.equals(nmID)) {
			idModified = true;
			pID.setBorder(BorderFactory.createTitledBorder("*ID"));
			return;
		}
		if (nm.equals(nmLength)) {
			lengthModified = true;
			pLngth.setBorder(BorderFactory.createTitledBorder("*Length (miles)"));
			return;
		}
		if (nm.equals(nmWidth)) {
			widthModified = true;
			pWdth.setBorder(BorderFactory.createTitledBorder("*Number of Lanes"));
			return;
		}
		if (nm.equals(nmQLim)) {
			qlimModified = true;
			pQLim.setBorder(BorderFactory.createTitledBorder("*Queue Limit"));
			return;
		}
		if (nm.equals(nmTP)) {
			tpModified = true;
			pT.setBorder(BorderFactory.createTitledBorder("*Sampling Period"));
			return;
		}
		if (nm.equals(nmTPC)) {
			ctpModified = true;
			pTC.setBorder(BorderFactory.createTitledBorder("*Sampling Period"));
			return;
		}
		boolean fdc = false;
		double x;
		if (nm.equals(nmCapacity)) {
			x = (Double)capacitySpinner.getValue();
			mf = x;
			fdc = true;
		}
		if (nm.equals(nmCapDrop)) {
			x = (Double)capdropSpinner.getValue();
			if ((x >= 0) && (x <= mf)) {
				drp = x;
				fdc = true;
			}
		}
		if (nm.equals(nmDenCrit)) {
			x = (Double)dencritSpinner.getValue();
			if (x <= jd)
				cd = x;
			fdc = true;
		}
		if (nm.equals(nmDenJam)) {
			x = (Double)denjamSpinner.getValue();
			if (x >= cd)
				jd = x;
			fdc = true;
		}
		if (nm.equals(nmVff)) {
			x = (Double)vffSpinner.getValue();
			if (x > 0.0)
				cd = mf / x;
			double xx = (Double)wcSpinner.getValue();
			if (xx > 0.0)
				jd = cd + (mf /xx);
			fdc = true;
		}
		if (nm.equals(nmWc)) {
			x = (Double)wcSpinner.getValue();
			if (x > 0.0)
				jd = cd + (mf / x);
			fdc = true;
		}
		if (fdc) {
			fdModified = true;
			updateSpinners();
			updateFDSeries();
			fdp.setBorder(BorderFactory.createTitledBorder("*Fundamental Diagram per Lane"));
		}
		if (nm.equals(nmCapRange)) {
			crModified = true;
			cr = (Double)crSpinner.getValue();
			pCR.setBorder(BorderFactory.createTitledBorder("*Capacity Uncertainty +/- (vphl)"));
		}
		return;
	}
	
	/**
	 * Reaction to density update.
	 */
	private void densityUpdate() {
		denModified = true;
		pDen.setBorder(BorderFactory.createTitledBorder("*Initial Density (vpm)"));
		return;
	}
	
	/**
	 * Reaction to demand profile update.
	 */
	private void dcUpdate() {
		dcModified = true;
		pDC.setBorder(BorderFactory.createTitledBorder("*Demand Coefficient(s)"));
		return;
	}
	
	/**
	 * Reaction to demand profile update.
	 */
	private void dpUpdate() {
		dpModified = true;
		pDP.setBorder(BorderFactory.createTitledBorder("*Demand Profile"));
		return;
	}
	
	/**
	 * Reaction to capacity profile update.
	 */
	private void cpUpdate() {
		cpModified = true;
		pCP.setBorder(BorderFactory.createTitledBorder("*Capacity Profile"));
		return;
	}
	
	
	/**
	 * Document listener for density field.
	 */
	private class DensityChangeListener implements DocumentListener {
		public void changedUpdate(DocumentEvent e) {
			densityUpdate();
			return;
		}
		
		public void insertUpdate(DocumentEvent e) {
			densityUpdate();
			return;
		}
		
		public void removeUpdate(DocumentEvent e) {
			densityUpdate();
			return;
		}
	}
	
	
	/**
	 * Document listener for demand coefficient(s).
	 */
	private class DCChangeListener implements DocumentListener {
		public void changedUpdate(DocumentEvent e) {
			dcUpdate();
			return;
		}
		
		public void insertUpdate(DocumentEvent e) {
			dcUpdate();
			return;
		}
		
		public void removeUpdate(DocumentEvent e) {
			dcUpdate();
			return;
		}
	}
	
	
	/**
	 * Document listener for demand profile.
	 */
	private class DPChangeListener implements DocumentListener {
		public void changedUpdate(DocumentEvent e) {
			dpUpdate();
			return;
		}
		
		public void insertUpdate(DocumentEvent e) {
			dpUpdate();
			return;
		}
		
		public void removeUpdate(DocumentEvent e) {
			dpUpdate();
			return;
		}
	}
	
	
	/**
	 * Document listener for capacity profile.
	 */
	private class CPChangeListener implements DocumentListener {
		public void changedUpdate(DocumentEvent e) {
			cpUpdate();
			return;
		}
		
		public void insertUpdate(DocumentEvent e) {
			cpUpdate();
			return;
		}
		
		public void removeUpdate(DocumentEvent e) {
			cpUpdate();
			return;
		}
	}
	
	
	/**
	 * Class needed for proper closing of internal link windows.
	 */
	private class AdapterWindowLink extends InternalFrameAdapter implements ComponentListener {
		
		/**
		 * Function that is called when user closes the window.
		 * @param e internal frame event.
		 */
		public void internalFrameClosing(InternalFrameEvent e) {
			close();
			return;
		}
		
		public void componentHidden(ComponentEvent e) {
			return;
		}

		public void componentMoved(ComponentEvent e) {
			return;
		}

		public void componentResized(ComponentEvent e) {
			return;
		}

		public void componentShown(ComponentEvent e) {
			return;
		}
		
	}
	
}
