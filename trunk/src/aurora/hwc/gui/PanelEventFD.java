/**
 * @(#)PanelEventFD.java 
 */

package aurora.hwc.gui;

import java.awt.*;
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
 * Form for fundamental diagram change event.
 * @author Alex Kurzhanskiy
 * @version $Id: PanelEventFD.java,v 1.1.2.3 2008/10/16 04:27:08 akurzhan Exp $
 */
public final class PanelEventFD extends AbstractEventPanel implements ChangeListener {
	private static final long serialVersionUID = 364874061040182049L;
	
	private MyXYSeries ffFD = new MyXYSeries("Free Flow");
	private MyXYSeries cFD = new MyXYSeries("Congestion");
	private JFreeChart fdChart;
	
	private JSpinner spinMaxFlow;
	private JSpinner spinCritDen;
	private JSpinner spinJamDen;
	private JSpinner spinVff;
	private JSpinner spinWc;
	
	private final static String nmSpinMaxFlow = "spinMaxFlow";
	private final static String nmSpinCritDen = "spinCritDen";
	private final static String nmSpinJamDen = "spinJamDen";
	private final static String nmSpinVff = "spinVff";
	private final static String nmSpinWc = "spinWc";
	
	private double mf;
	private double cd;
	private double jd;
	
	
	/**
	 * Initializes fundamental diagram change event editing panel.
	 * @param ne Network Element.
	 * @param em Event Manager.
	 * @param etm Event Table model.
	 */
	public synchronized void initialize(AbstractNetworkElement ne, EventManager em, EventTableModel etm) {
		eventTable = etm;
		myEvent = new EventFD();
		mf = ((AbstractLinkHWC)ne).getMaxFlow();
		cd = ((AbstractLinkHWC)ne).getCriticalDensity();
		jd = ((AbstractLinkHWC)ne).getJamDensity();
		initialize(ne, em);
	}
	
	/**
	 * Initializes fundamental diagram change event editing panel.
	 * @param ne Network Element.
	 * @param em Event Manager.
	 * @param etm Event Table model.
	 * @param evt Event.
	 */
	public synchronized void initialize(AbstractNetworkElement ne, EventManager em, EventTableModel etm, AbstractEvent evt) {
		eventTable = etm;
		if (evt != null)
			myEvent = evt;
		else
			myEvent = new EventFD();
		mf = ((EventFD)myEvent).getMaxFlow();
		cd = ((EventFD)myEvent).getCriticalDensity();
		jd = ((EventFD)myEvent).getJamDensity();
		initialize(ne, em);
	}
	
	/**
	 * Updates spinner values.
	 */
	private void updateSpinners() {
		spinMaxFlow.setValue((Double)mf);
		spinCritDen.setValue((Double)cd);
		spinJamDen.setValue((Double)jd);
		spinVff.setValue((Double)(mf/cd));
		spinWc.setValue((Double)(mf/(jd - cd)));
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
		ffFD.setDataItem(1, new XYDataItem(cd, mf));
		cFD.setDataItem(0, new XYDataItem(cd, mf));
		cFD.setDataItem(1, new XYDataItem(jd, 0.0));
		ffFD.fireSeriesChanged();
		cFD.fireSeriesChanged();
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
		JFreeChart chart = ChartFactory.createXYLineChart(
							null, // chart title
							"Density (vpm)", // x axis label
							"Flow (vph)", // y axis label
							dataset, // data
							PlotOrientation.VERTICAL,
							false, // include legend
							false, // tooltips
							false // urls
							);
		XYPlot plot = (XYPlot)chart.getPlot();
		plot.getRenderer().setSeriesPaint(0, Color.GREEN);
		plot.getRenderer().setSeriesPaint(1, Color.RED);
		plot.getRenderer().setStroke(new BasicStroke(2));
		return chart;
	}

	/**
	 * Fills the panel with event specific fields.
	 */
	protected void fillPanel() {
		JLabel l;
		Box fdp = Box.createVerticalBox();
		fdp.setBorder(BorderFactory.createTitledBorder("Fundamental Diagram"));
		fdChart = makeFDChart();
		ChartPanel cp = new ChartPanel(fdChart);
		cp.setMinimumDrawWidth(100);
		cp.setMinimumDrawHeight(60);
		cp.setPreferredSize(new Dimension(250, 80));
		fdp.add(new JScrollPane(cp));
		JPanel prmp = new JPanel(new SpringLayout());
		l = new JLabel("Capacity:", JLabel.TRAILING);
		prmp.add(l);
		spinMaxFlow = new JSpinner(new SpinnerNumberModel(mf, 0, 99999, 1.0));
		spinMaxFlow.setEditor(new JSpinner.NumberEditor(spinMaxFlow, "####0.00"));
		spinMaxFlow.addChangeListener(this);
		spinMaxFlow.setName(nmSpinMaxFlow);
		l.setLabelFor(spinMaxFlow);
		prmp.add(spinMaxFlow);
		prmp.add(new JLabel(" "));
		prmp.add(new JLabel(" "));
		l = new JLabel("C.Density:", JLabel.TRAILING);
		prmp.add(l);
		spinCritDen = new JSpinner(new SpinnerNumberModel(cd, 0, 99999, 1.0));
		spinCritDen.setEditor(new JSpinner.NumberEditor(spinCritDen, "####0.00"));
		spinCritDen.addChangeListener(this);
		spinCritDen.setName(nmSpinCritDen);
		l.setLabelFor(spinCritDen);
		prmp.add(spinCritDen);
		l = new JLabel("  V:", JLabel.TRAILING);
		prmp.add(l);
		spinVff = new JSpinner(new SpinnerNumberModel(mf/cd, 0, 200, 1.0));
		spinVff.setEditor(new JSpinner.NumberEditor(spinVff, "#0.00"));
		spinVff.addChangeListener(this);
		spinVff.setName(nmSpinVff);
		l.setLabelFor(spinVff);
		prmp.add(spinVff);
		l = new JLabel("J.Density:", JLabel.TRAILING);
		prmp.add(l);
		spinJamDen = new JSpinner(new SpinnerNumberModel(jd, 0, 99999, 1.0));
		spinJamDen.setEditor(new JSpinner.NumberEditor(spinJamDen, "####0.00"));
		spinJamDen.addChangeListener(this);
		spinJamDen.setName(nmSpinJamDen);
		l.setLabelFor(spinJamDen);
		prmp.add(spinJamDen);
		l = new JLabel("  W:", JLabel.TRAILING);
		prmp.add(l);
		if (jd == cd)
			jd = cd + 1;
		int ulim = (int)Math.max(Math.ceil(mf/(jd - cd)), 999);
		spinWc = new JSpinner(new SpinnerNumberModel(mf/(jd - cd), 0, ulim, 1.0));
		spinWc.setEditor(new JSpinner.NumberEditor(spinWc, "#0.00"));
		spinWc.addChangeListener(this);
		spinWc.setName(nmSpinWc);
		l.setLabelFor(spinWc);
		prmp.add(spinWc);
		SpringUtilities.makeCompactGrid(prmp, 3, 4, 2, 2, 2, 2);
		fdp.add(prmp);
		//add(new JScrollPane(fdp));
		add(fdp);
		return;
	}

	/**
	 * Returns window header with event description.
	 */
	public String getHeader() {
		return "Fundamental Diagram";
	}
	
	/**
	 * Saves event.
	 */
	public synchronized void save() {
		((EventFD)myEvent).setFD(mf, cd, jd);
		super.save();
		return;
	}

	/**
	 * Reaction to value changes in the form.
	 */
	public void stateChanged(ChangeEvent e) {
		String nm = ((JComponent)e.getSource()).getName();
		double x;
		if (nm.equals(nmSpinMaxFlow)) {
			x = (Double)spinMaxFlow.getValue();
			mf = x;
		}
		if (nm.equals(nmSpinCritDen)) {
			x = (Double)spinCritDen.getValue();
			if (x <= jd)
				cd = x;
		}
		if (nm.equals(nmSpinJamDen)) {
			x = (Double)spinJamDen.getValue();
			if (x >= cd)
				jd = x;
		}
		if (nm.equals(nmSpinVff)) {
			x = (Double)spinVff.getValue();
			if (x > 0.0)
				cd = mf / x;
			double xx = (Double)spinWc.getValue();
			if (xx > 0.0)
				jd = cd + (mf /xx);
		}
		if (nm.equals(nmSpinWc)) {
			x = (Double)spinWc.getValue();
			if (x > 0.0)
				jd = cd + (mf / x);
		}
		updateSpinners();
		updateFDSeries();
		return;
	}

}