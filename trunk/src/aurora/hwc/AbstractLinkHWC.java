/**
 * @(#)AbstractLinkHWC.java
 */

package aurora.hwc;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;

import org.w3c.dom.*;
import aurora.*;
import aurora.util.*;


/**
 * Base class for all links in a highway network.
 * 
 * @see LinkFwML, LinkFwHOV, LinkHw, LinkOR, LinkFR, LinkIc, LinkStreet, LinkDummy
 * 
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractLinkHWC.java,v 1.21.2.16.2.20.2.27 2009/11/28 02:33:27 akurzhan Exp $
 */
public abstract class AbstractLinkHWC extends AbstractLink {
	private static final long serialVersionUID = 7448808131935808045L;
	
	protected double lanes = 1.0;
	protected double flowMax = 1800; // in vph
	protected AuroraInterval flowMaxRange = new AuroraInterval(flowMax, 0);
	protected double currentWeavingFactor = 1.0;
	protected double inputWeavingFactor = 1.0;
	protected double[] outputWeavingFactors = null;
	protected double capacityDrop = 0; // in vph
	protected double densityCritical = 30; // in vpm
	protected double densityJam = 150; // in vpm
	
	//protected Vector<Double> densityH = new Vector<Double>();
	//protected Vector<Double> oflowH = new Vector<Double>();
	
	protected int tsV = 0;
	protected int tsCount = 0;
	protected boolean resetAllSums = true;
	
	protected AuroraIntervalVector density0 = new AuroraIntervalVector(); // initial value in vpm
	protected AuroraIntervalVector density = new AuroraIntervalVector(); // in vpm
	protected AuroraIntervalVector densitySum = new AuroraIntervalVector(); // in vpm
	protected AuroraIntervalVector inflowSum = new AuroraIntervalVector(); // in vph
	protected AuroraIntervalVector outflowSum = new AuroraIntervalVector(); // in vph
	protected AuroraInterval speed = new AuroraInterval(); // mean speed 
	protected AuroraInterval speedSum = new AuroraInterval(); // mean speed 
	protected double vht = 0; // vehicle hours traveled per sampling time period
	protected double vhtSum = 0;
	protected double vmt = 0; // vehicle miles traveled per sampling time period
	protected double vmtSum = 0;
	protected double delay = 0; // delay introduced when speed is below free flow speed
	protected double delaySum = 0;
	protected double ploss = 0; // productivity loss
	protected double plossSum = 0;
	
	protected double qMax = 100; // max queue size in vehicles
	protected AuroraIntervalVector qSize = new AuroraIntervalVector(); // actual queue size in vehicles
	
	protected Vector<AuroraIntervalVector> demand = new Vector<AuroraIntervalVector>(); // demand profile (values in vph)
	protected Vector<AuroraIntervalVector> procDemand = new Vector<AuroraIntervalVector>(); // demand profile (values in vph)
	protected double demandTP = 1.0/12.0; // demand value change period (default: 1/12 hour)
	protected double[] demandKnobs = new double[1]; //coefficients by which the demand values must be multiplied
	protected AuroraIntervalVector extDemandVal = null; // demand value that can be set externally, say, by a monitor
	
	protected Vector<AuroraInterval> capacity = new Vector<AuroraInterval>(); // capacity profile (values in vph)
	protected double capacityTP = 1.0/12.0;  // capacity value change period (default: 1/12 hour)
	protected AuroraInterval extCapVal = null; // capacity value that can be set externally, say, by a monitor
	
	protected DynamicsHWC myDynamics;
	
	
	/**
	 * Initializes the Link from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if ((p == null) || (myNetwork == null))
			return !res;
		try  {
			id = Integer.parseInt(p.getAttributes().getNamedItem("id").getNodeValue());
			length = Double.parseDouble(p.getAttributes().getNamedItem("length").getNodeValue());
			lanes = Double.parseDouble(p.getAttributes().getNamedItem("lanes").getNodeValue());
			Node sa = p.getAttributes().getNamedItem("record");
			if ((sa != null) && Boolean.parseBoolean(sa.getNodeValue()))
				saveState = 3;
			if (lanes < 1.0)
				lanes = 1.0;
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (int i = 0; i < pp.getLength(); i++) {
					if (pp.item(i).getNodeName().equals("begin"))
						setBeginNode(myNetwork.getNodeById(Integer.parseInt(pp.item(i).getAttributes().getNamedItem("id").getNodeValue())));
					if (pp.item(i).getNodeName().equals("end"))
						setEndNode(myNetwork.getNodeById(Integer.parseInt(pp.item(i).getAttributes().getNamedItem("id").getNodeValue())));
					if (pp.item(i).getNodeName().equals("dynamics")) {
						Class c = Class.forName(pp.item(i).getAttributes().getNamedItem("class").getNodeValue());
						myDynamics = (DynamicsHWC)c.newInstance();
					}
					if (pp.item(i).getNodeName().equals("density")) {
						setInitialDensity(pp.item(i).getTextContent());
					}
					if (pp.item(i).getNodeName().equals("demand")) {
						demandTP = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("tp").getNodeValue());
						if (demandTP > 24) // sampling period in seconds
							demandTP = demandTP/3600;
						setDemandKnobs(pp.item(i).getAttributes().getNamedItem("knob").getNodeValue());
						setDemandVector(pp.item(i).getTextContent());
					}
					if (pp.item(i).getNodeName().equals("capacity")) {
						capacityTP = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("tp").getNodeValue());
						if (capacityTP > 24) // sampling period in seconds
							capacityTP = capacityTP/3600;
						setCapacityVector(pp.item(i).getTextContent());
					}
					if (pp.item(i).getNodeName().equals("qmax"))
						qMax = Double.parseDouble(pp.item(i).getTextContent());
					if (pp.item(i).getNodeName().equals("fd")) {
						flowMaxRange.setIntervalFromString(pp.item(i).getAttributes().getNamedItem("flowMax").getNodeValue());
						double cd = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("densityCritical").getNodeValue());
						double jd = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("densityJam").getNodeValue());
						if (!setFD(flowMaxRange.getCenter(), cd, jd))
							defaultFD();
						if (myNetwork.getContainer().isSimulation())
							randomizeFD();
						Node cdp = pp.item(i).getAttributes().getNamedItem("capacityDrop");
						if (cdp != null)
							capacityDrop = Math.max(0, Math.min(flowMax, Double.parseDouble(cdp.getNodeValue())));
					}
					if (pp.item(i).getNodeName().equals("position")) {
						myPosition = new PositionLink();
						res &= myPosition.initFromDOM(pp.item(i));
					}
				}
			}
			else
				res = false;
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		initialized = true;
		return res;
	}
	
	/**
	 * Generates XML description of the Link.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		boolean ss = false;
		if ((saveState == 3) || (saveState == 2))
			ss = true;
		/*/FIXME
		if (demand.size() > 1) {
			for (int i = 0; i < 48; i++)
				demand.remove(0);
			//System.err.println(demand.size() + "\t" + procDemand.size());
		}//*/
		out.print("<link class=\"" + this.getClass().getName() + "\" id=\"" + Integer.toString(id) + "\" length=\"" + Double.toString(length) + "\" lanes=\"" + Double.toString(lanes) + "\" record=\"" + ss + "\">");
		if (predecessors.size() > 0)
			out.print("<begin id=\"" + Integer.toString(predecessors.firstElement().getId()) + "\"/>");
		if (successors.size() > 0)
			out.print("<end id=\"" + Integer.toString(successors.firstElement().getId()) + "\"/>");
		out.print("<dynamics class=\"" + myDynamics.getClass().getName() + "\"/>");
		out.print("<density>" + density.toStringWithInverseWeights(((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).getVehicleWeights(), false) + "</density>");
		if (!demand.isEmpty()) {
			out.print("<demand tp=\"" + Double.toString(demandTP) + "\" knob=\"" + getDemandKnobsAsString() + "\">");
			out.print(getDemandVectorAsString());
			out.print("</demand>");
		}
		if (!capacity.isEmpty()) {
			out.print("<capacity tp=\"" + Double.toString(capacityTP) + "\">");
			out.print(getCapacityVectorAsString());
			out.print("</capacity>");
		}
		out.print("<qmax>" + Double.toString(qMax) + "</qmax>");
		out.print("<fd densityCritical =\"" + densityCritical + "\" densityJam=\"" + densityJam + "\" flowMax=\"" + flowMaxRange.toString() + "\" capacityDrop=\"" + capacityDrop + "\"/>");
		myPosition.xmlDump(out);
		out.print("</link>\n");
		return;
	}

	/**
	 * Updates Link data.<br>
	 * @param ts time step.
	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		DataStorage db = myNetwork.getDatabase();
		if (db != null)
			db.saveLinkData(this);
		boolean res = super.dataUpdate(ts);
		if (res) {
			if (resetAllSums)
				resetSums();
			PrintStream os = null;
			if ((ts == 1) || (((ts - tsV) * myNetwork.getTop().getTP()) >= myNetwork.getTop().getContainer().getMySettings().getDisplayTP())) {
				tsV = ts;
				resetAllSums = true;
				if (saveState == 3)
					os = myNetwork.getContainer().getMySettings().getTmpDataOutput();
			}
			tsCount++;
			if (predecessors.size() == 0) {
				AbstractNode end = (AbstractNode)successors.firstElement(); // assumed != null
				AuroraIntervalVector ofl = (AuroraIntervalVector)(end.getInputs().get(end.getPredecessors().indexOf(this)));
				double tp = myNetwork.getTP();
				qSize.affineTransform(1/tp, 0);
				qSize.add((AuroraIntervalVector)getDemand());
				qSize.subtract(ofl);
				qSize.affineTransform(tp, 0);
				inflowSum.add(getDemand());
			}
			else {
				qSize.set(new AuroraInterval());
				inflowSum.add((AuroraIntervalVector)getBeginNode().getOutputs().get(getBeginNode().getSuccessors().indexOf(this)));
			}
			outflowSum.add(getActualFlow());
			if (os != null) {
				NumberFormat form = NumberFormat.getInstance();
				form.setMinimumFractionDigits(0);
				form.setMaximumFractionDigits(2);
				form.setGroupingUsed(false);
				os.print(", " + getAverageDensity().toString3() + ";"
						+ getAverageInFlow().toString3() + ";"
						+ getAverageOutFlow().toString3() + ";"
						+ form.format(flowMax) + ":" + form.format(densityCritical) + ":" + form.format(densityJam) + ";"
						+ form.format(qMax) + ";"
						+ form.format(currentWeavingFactor));
			}
			for (int i = 0; i < qSize.size(); i++)  // make sure queue has no negative values
				if (qSize.get(i).getUpperBound() < 0.0)
					qSize.get(i).setCenter(0.0, 0.0);
				else
					qSize.get(i).setBounds(Math.max(qSize.get(i).getLowerBound(), 0.0), qSize.get(i).getUpperBound());
			speed.copy((AuroraInterval)myDynamics.computeSpeed(this));
			speedSum.add(speed);
			density = (AuroraIntervalVector)myDynamics.computeDensity(this);
			densitySum.add(density);
			if (successors.size() == 0)
				speed = (AuroraInterval)myDynamics.computeSpeed(this);
			double tp = myNetwork.getTP();
			if (predecessors.size() == 0)
				vht = qSize.sum().getCenter() * tp;
			else
				vht = density.sum().getCenter() * length * tp;
			vhtSum += vht;
			vmt = density.sum().getCenter() * speed.getCenter() * length * tp;
			vmtSum += vmt;
			delay = Math.max((vht - (vmt/getV())), 0.0);
			delaySum += delay;
			((NodeHWCNetwork)myNetwork).addToTotalDelay(delay);
			if (density.sum().getCenter() <= densityCritical)
				ploss = 0;
			else
				ploss = Math.max((tp * length * lanes * (1 - (getActualFlow().sum().getCenter() / flowMax))), 0.0);
			plossSum += ploss;
		}
		return res;
	}
	
	/**
	 * Updates configuration summary.
	 * @param cs configuration summary.
	 */
	public void updateConfigurationSummary(AbstractConfigurationSummary cs) {
		if (cs == null)
			return;
		super.updateConfigurationSummary(cs);
		((ConfigurationSummaryHWC)cs).updateFastestLink(this);
		((ConfigurationSummaryHWC)cs).updateSlowestLink(this);
		((ConfigurationSummaryHWC)cs).updateMinTTLink(this);
		return;
	}
	
	/**
	 * Additional initialization.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration, ExceptionDatabase
	 */
	public boolean initialize() throws ExceptionConfiguration, ExceptionDatabase {
		boolean res = super.initialize();
		if (getBeginNode() == null) {
			qSize.copy(density0);
			qSize.affineTransform(length, 0);
		}
		else
			qSize = new AuroraIntervalVector(((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes());
		density.copy(density0);
		speed.setCenter(0.0, 0.0);
		resetSums();
		if (saveState == 1)
			saveState = 0;
		if (saveState == 2)
			saveState = 3;
		tsV = 0;
		return res;
	}
	
	/**
	 * Validates Link configuration.<br>
	 * Checks if dynamics is assigned.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean validate() throws ExceptionConfiguration {
		boolean res = super.validate();
		if (myDynamics == null) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "No dynamics assigned."));
			res = false;
			//throw new ExceptionConfiguration(this, "No dynamics assigned.");
		}
		return res;
	}
	
	/**
	 * Returns dynamics.
	 */
	public DynamicsHWC getDynamics() {
		return myDynamics;
	}
	
	/**
	 * Returns flow generated by the link.
	 */
	public AuroraIntervalVector getFlow() {
		return (AuroraIntervalVector)myDynamics.computeFlow(this);
	}
	
	/**
	 * Returns flow that actually leaves the link.
	 */
	public AuroraIntervalVector getActualFlow() {
		AuroraIntervalVector flw = new AuroraIntervalVector();
		AbstractNode en = getEndNode();
		if (en != null)
			flw.copy((AuroraIntervalVector)en.getInputs().get(en.getPredecessors().indexOf(this)));
		else {
			flw = getFlow();
			flw.affineTransform(Math.min(getCapacityValue().getUpperBound()/flw.sum().getUpperBound(), 1), 0);
		}
		return flw;
	}
	
	/**
	 * Returns average input flow.
	 */
	public AuroraIntervalVector getAverageInFlow() {
		AuroraIntervalVector aif = new AuroraIntervalVector();
		aif.copy(inflowSum);
		if (tsCount > 0)
			aif.affineTransform(1.0/tsCount, 0);
		return aif;
	}
	
	/**
	 * Returns average output flow.
	 */
	public AuroraIntervalVector getAverageOutFlow() {
		AuroraIntervalVector aof = new AuroraIntervalVector();
		aof.copy(outflowSum);
		if (tsCount > 0)
			aof.affineTransform(1.0/tsCount, 0);
		return aof;
	}
	
	/**
	 * Returns maximum flow that can be accepted from upstream node.
	 */
	public AuroraInterval getCapacity() {
		return (AuroraInterval)myDynamics.computeCapacity(this);
	}
	
	/**
	 * Returns mean speed of traffic in the link.
	 */
	public AuroraInterval getSpeed() {
		AuroraInterval s = new AuroraInterval();
		s.copy(speed);
		return s;
	}
	
	/**
	 * Returns average mean speed of traffic in the link.
	 */
	public AuroraInterval getAverageSpeed() {
		AuroraInterval as = new AuroraInterval();
		as.copy(speedSum);
		if (tsCount > 0)
			as.affineTransform(1.0/tsCount, 0);
		return as;
	}
	
	/**
	 * Returns number of lanes in the link.
	 */
	public final double getLanes() {
		return lanes;
	}
	
	/**
	 * Returns the width of the link.
	 */
	public final double getWidth() {
		return getLanes();
	}
	
	/**
	 * Returns capacity drop.
	 */
	public final double getCapacityDrop() {
		return capacityDrop;
	}
	
	/**
	 * Returns the weaving factor.
	 */
	public final double getWeavingFactor() {
		return currentWeavingFactor;
	}
	
	/**
	 * Returns the input weaving factor.
	 */
	public final double getInputWeavingFactor() {
		return inputWeavingFactor;
	}
	
	/**
	 * Returns the output weaving factors.
	 */
	public final double[] getOutputWeavingFactors() {
		if ((outputWeavingFactors != null) && (outputWeavingFactors.length > 0)) {
			int n = outputWeavingFactors.length;
			double[] owf = new double[n];
			for (int i = 0; i < n; i++)
				owf[i] = outputWeavingFactors[i];
			return owf;
		}
		return null;
	}
	
	/**
	 * Returns capacity range.
	 */
	public final AuroraInterval getMaxFlowRange() {
		return new AuroraInterval(flowMax, flowMaxRange.getSize());
	}
	
	/**
	 * Returns maximum flow that can be achieved in the link.
	 */
	public final double getMaxFlow() {
		return flowMax;
	}
	
	/**
	 * Returns critical density for the link.
	 */
	public final double getCriticalDensity() {
		return densityCritical;
	}
	
	/**
	 * Returns jam density for the link.
	 */
	public final double getJamDensity() {
		return densityJam;
	}
	
	/**
	 * Returns queue size.
	 */
	public final AuroraIntervalVector getQueue() {
		AuroraIntervalVector q = new AuroraIntervalVector();
		q.copy(qSize);
		return q;
	}
	
	/**
	 * Returns queue limit.
	 */
	public final double getQueueMax() {
		return qMax;
	}
	
	/**
	 * Returns free flow speed.
	 */
	public final double getV() { // free-flow speed
		return (flowMax / densityCritical);
	}
	
	/**
	 * Returns congestion wave speed.
	 */
	public final double getW() { // congestion wave speed
		return (flowMax / (densityJam - densityCritical));
	}
	
	/**
	 * Returns initial density.
	 */
	public final AuroraIntervalVector getInitialDensity() {
		AuroraIntervalVector den = new AuroraIntervalVector();
		den.copy(density0);
		return den;
	}
	
	/**
	 * Returns density.
	 */
	public final AuroraIntervalVector getDensity() {
		AuroraIntervalVector den = new AuroraIntervalVector();
		den.copy(density);
		return den;
	}

	/**
	 * Returns average density.
	 */
	public final AuroraIntervalVector getAverageDensity() {
		AuroraIntervalVector ad = new AuroraIntervalVector();
		ad.copy(densitySum);
		if (tsCount > 0)
			ad.affineTransform(1.0/tsCount, 0);
		return ad;
	}
	
	
	/**
	 * Returns VHT.
	 */
	public final double getVHT() {
		return vht;
	}
	
	/**
	 * Returns sum of VHT.
	 */
	public final double getSumVHT() {
		return vhtSum;
	}
	
	/**
	 * Returns VMT.
	 */
	public final double getVMT() {
		return vmt;
	}
	
	/**
	 * Returns sum of VMT.
	 */
	public final double getSumVMT() {
		return vmtSum;
	}
	
	/**
	 * Returns delay.
	 */
	public final double getDelay() {
		return delay;
	}
	
	/**
	 * Returns sum of delay.
	 */
	public final double getSumDelay() {
		return delaySum;
	}
	
	/**
	 * Returns productivity loss.
	 */
	public final double getPLoss() {
		return ploss;
	}
	
	/**
	 * Returns sum of productivity loss.
	 */
	public final double getSumPLoss() {
		return plossSum;
	}
	
	/**
	 * Returns occupancy.
	 */
	public final double getOccupancy() {
		return Math.min(1, (currentWeavingFactor * density.sum().getCenter()) / densityJam);
	}
	
	/**
	 * Returns demand.
	 */
	public final AuroraIntervalVector getDemand() {
		double t = myNetwork.getSimTime(); // simulation time (in hours)
		int idx = (int)Math.floor(t/demandTP);
		int n = procDemand.size() - 1; // max index of the demand profile
		if (n < 0) //empty
			return new AuroraIntervalVector();
		if ((idx < 0) || (idx > n))
			idx = n;
		AuroraIntervalVector dmnd = new AuroraIntervalVector();
		dmnd.copy(procDemand.get(idx));
		dmnd.affineTransform(demandKnobs, 0);
		return dmnd;
	}
	
	/**
	 * Returns demand value taking into account possible external demand.
	 */
	public final AuroraIntervalVector getDemandValue() {
		if (extDemandVal != null)
			return extDemandVal;
		AuroraIntervalVector ord = new AuroraIntervalVector();
		AuroraIntervalVector orq = new AuroraIntervalVector();
		ord.copy((AuroraIntervalVector)getDemand());
		orq.copy((AuroraIntervalVector)getQueue());
		orq.affineTransform(1/getMyNetwork().getTP(), 0);
		ord.add(orq);
		double dv = ord.sum().getCenter();
		if (getMaxFlow() < dv)
			ord.affineTransform(getMaxFlow()/dv, 0);
		return ord;
	}
	
	/**
	 * Returns demand value taking into account possible external demand.
	 * Do not impose capacity restriction.
	 */
	public final AuroraIntervalVector getDemandValue2() {
		if (extDemandVal != null)
			return extDemandVal;
		AuroraIntervalVector ord = new AuroraIntervalVector();
		AuroraIntervalVector orq = new AuroraIntervalVector();
		ord.copy((AuroraIntervalVector)getDemand());
		orq.copy((AuroraIntervalVector)getQueue());
		orq.affineTransform(1/getMyNetwork().getTP(), 0);
		ord.add(orq);
		return ord;
	}
	
	/**
	 * Returns demand vector.
	 */
	public final Vector<AuroraIntervalVector> getDemandVector() {
		return demand;
	}
	
	/**
	 * Returns demand vector as text buffer. 
	 * @return buf text buffer.
	 */
	public final String getDemandVectorAsString() {
		String buf = new String();
		int n = demand.size();
		if (n < 1)
			buf += "0.0";
		else {
			buf += demand.get(0).toStringWithInverseWeights(((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).getVehicleWeights(), false);
			for (int i = 1; i < n; i++)
				buf += ", " + demand.get(i).toStringWithInverseWeights(((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).getVehicleWeights(), false);
		}
		return buf;
	}
	
	/**
	 * Returns demand knob values.
	 */
	public final double[] getDemandKnobs() {
		double[] v = new double[demandKnobs.length];
		for (int i = 0; i < demandKnobs.length; i++)
			v[i] = demandKnobs[i]; 
		return v;
	}
	
	/**
	 * Returns demand knob values as string.
	 */
	public final String getDemandKnobsAsString() {
		boolean allequal = true;
		String buf = "";
		double val = demandKnobs[0];
		for (int i = 0; i < demandKnobs.length; i++) {
			if (i > 0) {
				buf += ":";
				if (demandKnobs[i] != val)
					allequal = false;
			}
			buf += Double.toString(demandKnobs[i]);
		}
		if (allequal)
			buf = Double.toString(val);
		return buf;
	}
	
	/**
	 * Returns the frequency of demand value change.
	 */
	public final double getDemandTP() {
		return demandTP;
	}
	
	/**
	 * Returns the demand value set by external entity.
	 */
	public final AuroraIntervalVector getExternalDemandValue() {
		return extDemandVal;
	}
	
	/**
	 * Returns downstream capacity.
	 */
	public final AuroraInterval getCapacityValue() {
		if (extCapVal != null)
			return extCapVal;
		double t = myNetwork.getSimTime(); // simulation time (in hours)
		int idx = (int)Math.floor(t/capacityTP);
		int n = capacity.size() - 1; // max index of the capacity profile
		if ((idx < 0) || (idx > n))
			idx = n;
		if ((idx < 0) || (getEndNode() != null))
			return new AuroraInterval(flowMax);
		AuroraInterval cv = new AuroraInterval();
		cv.copy(capacity.get(idx));
		return cv;
	}
	
	/**
	 * Returns capacity vector.
	 */
	public final Vector<AuroraInterval> getCapacityVector() {
		return capacity;
	}
	
	/**
	 * Returns capacity vector as text buffer. 
	 * @return buf text buffer.
	 */
	public final String getCapacityVectorAsString() {
		String buf = new String();
		int n = capacity.size();
		if (n > 0) {
			buf += capacity.get(0).toString();
			for (int i = 1; i < n; i++)
				buf += ", " + capacity.get(i).toString();
		}
		return buf;
	}
	
	/**
	 * Returns the capacity value set by external entity.
	 */
	public final double getCapacityTP() {
		return capacityTP;
	}
	
	/**
	 * Returns the frequency of capacity value change.
	 */
	public final AuroraInterval getExternalCapacityValue() {
		return extCapVal;
	}
	
	/**
	 * Returns instantaneous travel time.
	 */
	public final double getTravelTime() {
		double tt = 0.0;
		if (getBeginNode() != null)
			tt = length/speed.getCenter();
		else {
			if (qSize.sum().getCenter() > Util.EPSILON);
				tt = qSize.sum().getCenter()/(getActualFlow().sum().getCenter());
		}
		if ((tt == Double.NaN) || (tt == Double.POSITIVE_INFINITY))
			tt = Double.MAX_VALUE;
		return Math.max(tt, getMinTravelTime());
	}
	
	/**
	 * Returns minimal travel time.
	 */
	public final double getMinTravelTime() {
		double tt = 0.0;
		if (getBeginNode() != null)
			if (getV() > 0.0)
				tt = length/getV();
			else
				tt = Double.MAX_VALUE;
		return tt;
	}
	
	/**
	 * Assigns Network of which current Link is part.
	 * @param x Complex Node.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean setMyNetwork(AbstractNodeComplex x) {
		boolean res = super.setMyNetwork(x);
		if (!res)
			return res;
		int sz = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();
		demandKnobs = new double[sz];
		for (int i = 0; i < sz; i++)
			demandKnobs[i] = 1;
		return true;
	}
	
	/**
	 * Assigns queue limit.
	 * @param x queue limit.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setQueueMax(double x) {
		if (x >= 0.0)
			qMax = x;
		else
			return false;
		return true;
	}
	
	/**
	 * Assigns number of lanes.<br>
	 * Adjusts the fundamental diagram accordingly.
	 * @param x number of lanes.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setLanes(double x) {
		if (x < 1.0)
			x = 1.0;
		if (lanes > 0) {
			flowMax = flowMax * (x/lanes);
			densityCritical = densityCritical * (x/lanes);
			densityJam = densityJam * (x/lanes);
		}
		lanes = x;
		return true;
	}
	
	/**
	 * Assigns capacity drop.
	 * @param x capacity drop.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setCapacityDrop(double x) {
		if (x < 0.0)
			return false;
		capacityDrop = x;
		return true;
	}
	
	/**
	 * Assigns weaving factor.
	 * @param x weaving factor.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setWeavingFactor(double x) {
		if (x < 1.0)
			return false;
		currentWeavingFactor = x;
		return true;
	}
	
	/**
	 * Assigns input flow weaving factor.
	 * @param x input flow weaving factor.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setInputWeavingFactor(double x) {
		if (x < 1.0)
			return false;
		inputWeavingFactor = x;
		return true;
	}
	
	/**
	 * Assigns output flow weaving factors.
	 * @param x output flow weaving factors.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setOutputWeavingFactors(double[] x) {
		if ((x == null) || (x.length != density.size()))
			return false;
		if (outputWeavingFactors == null)
			outputWeavingFactors = new double[x.length];
		for (int i = 0; i < outputWeavingFactors.length; i++)
			outputWeavingFactors[i] = x[i];
		return true;
	}
	
	/**
	 * Assigns range for maximum flow.
	 * @param x interval size maximum flow.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setMaxFlowRange(double x) {
		if ((x < 0.0) || (x > (flowMax/2)))
			return false;
		flowMaxRange.setCenter(flowMax, x);
		return true;
	}
	
	/**
	 * Assigns maximum flow.
	 * @param x maximum flow.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setMaxFlow(double x) {
		if (x < 0.0)
			return false;
		flowMax = x;
		flowMaxRange.setCenter(flowMax, Math.min(flowMaxRange.getSize(), 2*flowMax));
		return true;
	}
	
	/**
	 * Assigns critical density.
	 * @param x critical density.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setCriticalDensity(double x) {
		if ((x >= 0.0) && (x <= densityJam))
			densityCritical = x;
		else
			return false;
		return true;
	}
	
	/**
	 * Assigns jam density.
	 * @param x jam density.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setJamDensity(double x) {
		if (x >= densityCritical)
			densityJam = x;
		else
			return false;
		return true;
	}
	
	/**
	 * Assigns dynamics.
	 * @param dyn dynamics object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDynamics(DynamicsHWC dyn) {
		if (dyn == null)
			return false;
		myDynamics = dyn;
		return true;
	}
	
	/**
	 * Sets random maximum flow value and adjusts fundamental diagram accordingly.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean randomizeFD() {
		flowMaxRange.constraintLB(0);
		flowMaxRange.randomize();
		double fm = flowMaxRange.getCenter();
		double cd = fm/getV();
		double jd = cd + (fm/getW());
		return setFD(fm, cd, jd);
	}
	
	/**
	 * Assigns parameters of the fundamental diagram.
	 * @param fmax maximum flow.
	 * @param rhoc critical density.
	 * @param rhoj jam density.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setFD(double fmax, double rhoc, double rhoj) {
		boolean res = false;
		if ((fmax >= 0.0) && (rhoc >= 0.0) && (rhoj >= 0.0) && (rhoc <= rhoj)) {
			flowMax = fmax;
			densityCritical = rhoc;
			densityJam = rhoj;
			res = true;
		}
		return res;
	}
	
	/**
	 * Assigns parameters of the fundamental diagram.
	 * @param fmax maximum flow.
	 * @param rhoc critical density.
	 * @param rhoj jam density.
	 * @param capd capacity drop;
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setFD(double fmax, double rhoc, double rhoj, double capd) {
		boolean res = false;
		if ((fmax >= 0.0) && (rhoc >= 0.0) && (rhoj >= 0.0) && (rhoc <= rhoj)) {
			flowMax = fmax;
			densityCritical = rhoc;
			densityJam = rhoj;
			capacityDrop = Math.max(0, Math.min(fmax, capd));
			flowMaxRange.setCenter(flowMax, Math.min(flowMaxRange.getSize(), 2*flowMax));
			res = true;
		}
		return res;
	}
	
	/**
	 * Initializes the fundamental diagram with defaults.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean defaultFD() {
		flowMax = 1800 * lanes;
		flowMaxRange.setCenter(flowMax, 0);
		densityCritical = 30.0 * lanes;
		densityJam = 150 * lanes;
		capacityDrop = 0.0;
		return true;
	}
	
	/**
	 * Modifies fundamental diagram by assigning free flow speed.<br>
	 * Maximum flow and congestion wave speed remain untouched.
	 * @param x free flow speed.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setV(double x) { // max flow & jam density are fixed
		double w = flowMax / (densityJam - densityCritical);
		if (x > 0.0) {
			densityCritical = flowMax / x;
			densityJam = densityCritical + (flowMax / w);
		}
		else
			return false;
		return true;
	}
	
	/**
	 * Modifies fundamental diagram by assigning congestion wave speed.<br>
	 * Maximum flow and critical density remain untouched.
	 * @param x congestion wave speed.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setW(double x) { // max flow & critical density are fixed
		if (x > 0.0)
			densityJam = densityCritical + (flowMax / x);
		else
			return false;
		return true;
	}
	
	/**
	 * Sets initial density value.<br>
	 * Used to set initial condition.
	 * @param x density value.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setInitialDensity(AuroraIntervalVector x) {
		if (x == null)
			return false;
		boolean res = density0.copy(x);
		density0.constraintLB(0);
		double s = densityJam / density.sum().getUpperBound();
		if (s < 1)
			for (int i = 0; i < density0.size(); i++)
				density0.get(i).setUpperBound(s * density0.get(i).getUpperBound());
		density.copy(density0);
		return res;
	}
	
	/**
	 * Sets initial density value from text buffer.<br>
	 * Used to set initial condition.
	 * @param buf density value as text buffer.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setInitialDensity(String buf) {
		if ((buf == null) || buf.isEmpty())
			return false;
		density0 = new AuroraIntervalVector(((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes());
		if (myNetwork.getContainer().isSimulation())
			density0.setIntervalVectorFromString(buf);
		else
			density0.setRawIntervalVectorFromString(buf);
		density0.affineTransform(((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).getVehicleWeights(), 0);
		density0.constraintLB(0);
		double s = densityJam / density.sum().getUpperBound();
		if (s < 1)
			for (int i = 0; i < density0.size(); i++)
				density0.get(i).setUpperBound(s * density0.get(i).getUpperBound());
		density.copy(density0);
		return true;
	}
	
	/**
	 * Sets demand vector.<br>
	 * @param x demand vector.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDemandVector(Vector<AuroraIntervalVector> x) {
		if (x == null)
			return false;
		demand = x;
		return true;
	}
	
	/**
	 * Sets demand vector from string.
	 * @param buf text buffer.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDemandVector(String buf) {
		if (buf == null)
			return false;
		StringTokenizer st = new StringTokenizer(buf, ", \t");
		demand.clear();
		while (st.hasMoreTokens()) {
			AuroraIntervalVector v = new AuroraIntervalVector(((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes());
			if (myNetwork.getContainer().isSimulation())
				v.setIntervalVectorFromString(st.nextToken());
			else
				v.setRawIntervalVectorFromString(st.nextToken());
			v.affineTransform(((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).getVehicleWeights(), 0);
			v.constraintLB(0);
			/*// Redistribute demand
			double[] rv = {0.7, 0.3};
			v.redistribute(rv);*/
			/*// Generate uncertainty in demands
			for (int i = 0; i < v.size(); i++) {
				double sz = v.get(i).getCenter() * 0.1;
				v.get(i).setCenter(v.get(i).getCenter(), sz);
			}*/
			demand.add(v);
			AuroraIntervalVector v2 = new AuroraIntervalVector();
			v2.copy(v);
			if (myNetwork.getContainer().isSimulation())
				v2.randomize();
			procDemand.add(v2);
		}
		return true;
	}
	
	/**
	 * Sets demand knob values.
	 * @param x demand knob values.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDemandKnobs(double[] x) {
		if ((x == null) || (x.length < 1))
			return false;
		int sz = Math.min(x.length, demandKnobs.length);
		for (int i = 0; i < sz; i++)
			demandKnobs[i] = Math.max(0, x[i]);
		if (sz == 1)
			for (int i = sz; i < demandKnobs.length; i++)
				demandKnobs[i] = demandKnobs[0];
		return true;
	}
	
	/**
	 * Sets demand knob values from given string buffer.
	 * @param x string with column separated demand knob values.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDemandKnobs(String x) {
		if ((x == null) || (x.isEmpty()))
			return false;
		StringTokenizer st = new StringTokenizer(x, ": ");
		if (st.countTokens() < 1)
			return false;
		int sz = Math.min(demandKnobs.length, st.countTokens());
		for (int i = 0; i < sz; i++) {
			try {
				demandKnobs[i] = Math.max(0, Double.parseDouble(st.nextToken()));
			}
			catch(Exception e) {
				demandKnobs[i] = 1;
			}
		}
		for (int i = sz; i < demandKnobs.length; i++) {
			if (sz == 1)
				demandKnobs[i] = demandKnobs[0];
			else
				demandKnobs[i] = 1;
		}
		return true;
	}
	
	/**
	 * Sets demand change frequency.<br>
	 * @param x demand sampling period.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDemandTP(double x) {
		if (x < myNetwork.getTP())
			return false;
		demandTP = x;
		return true;
	}
	
	/**
	 * Sets external demand value.
	 * @param x external demand value.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setExternalDemandValue(AuroraIntervalVector x) {
		if (x == null) {
			extDemandVal = x;
			return true;
		}
		extDemandVal = new AuroraIntervalVector();
		extDemandVal.copy(x);
		extDemandVal.constraintLB(0);
		return true;
	}
	
	/**
	 * Sets capacity vector.<br>
	 * @param x capacity vector.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setCapacityVector(Vector<AuroraInterval> x) {
		if (x == null)
			return false;
		capacity = x;
		for (int i = 0; i < capacity.size(); i++)
			capacity.get(i).constraintLB(0);
		return true;
	}
	
	/**
	 * Sets capacity vector from string.
	 * @param buf text buffer.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setCapacityVector(String buf) {
		boolean res = true;
		if (buf == null)
			return false;
		StringTokenizer st = new StringTokenizer(buf, ", \t");
		capacity.clear();
		while (st.hasMoreTokens()) {
			AuroraInterval v = new AuroraInterval();
			v.setIntervalFromString(st.nextToken());
			v.constraintLB(0);
			capacity.add(v);
		}
		return res;
	}
	
	/**
	 * Sets capacity change frequency.
	 * @param x capacity sampling period.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setCapacityTP(double x) {
		if (x < myNetwork.getTP())
			return false;
		capacityTP = x;
		return true;
	}
	
	/**
	 * Sets external capacity value.
	 * @param x external capacity value.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setExternalCapacityValue(AuroraInterval x) {
		if (x == null) {
			extCapVal = x;
			return true;
		}
		extCapVal = new AuroraInterval();
		extCapVal.copy(x);
		extCapVal.constraintLB(0);
		return true;
	}
	
	/**
	 * Resets VMT, VHT, Delay, Productivity Loss, In/Out Flow, Density and Speed sums.
	 */
	public synchronized void resetSums() {
		vht = 0;
		vmt = 0;
		delay = 0;
		ploss = 0;
		vmtSum = 0;
		vhtSum = 0;
		delaySum = 0;
		plossSum = 0;
		tsCount = 0;
		int sz = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();
		densitySum = new AuroraIntervalVector(sz);
		inflowSum = new AuroraIntervalVector(sz);
		outflowSum = new AuroraIntervalVector(sz);
		speedSum = new AuroraInterval();
		resetAllSums = false;
		return;
	}
	
	/**
	 * Adjust vector data according to new vehicle weights.
	 * @param w array of weights.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean adjustWeightedData(double[] w) {
		double[] ow = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).getVehicleWeights();
		if ((w == null) || (ow == null))
			return false;
		boolean res = qSize.inverseAffineTransform(ow, 0);
		res &= density.inverseAffineTransform(ow, 0);
		res &= density0.inverseAffineTransform(ow, 0);
		res &= qSize.affineTransform(w, 0);
		res &= density.affineTransform(w, 0);
		res &= density0.affineTransform(w, 0);
		for (int i = 0; i < demand.size(); i++) {
			res &= demand.get(i).inverseAffineTransform(ow, 0);
			res &= demand.get(i).affineTransform(w, 0);
		}
		if (extDemandVal != null) {
			res &= extDemandVal.inverseAffineTransform(ow, 0);
			res &= extDemandVal.affineTransform(w, 0);
		}
		return res;
	}
	
	/**
	 * Copies data from the given Link to the current one.
	 * @param x given Network Element.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean copyData(AbstractNetworkElement x) {
		boolean res = super.copyData(x);
		if (res) {
			AbstractLinkHWC lnk = (AbstractLinkHWC)x;
			lanes = lnk.getLanes();
			myDynamics = lnk.getDynamics();
			setFD(lnk.getMaxFlow(), lnk.getCriticalDensity(), lnk.getJamDensity(), lnk.getCapacityDrop());
			density = (AuroraIntervalVector)lnk.getDensity();
			density0 = (AuroraIntervalVector)lnk.getInitialDensity();
			qMax = lnk.getQueueMax();
			setDemandKnobs(lnk.getDemandKnobs());
			demandTP = lnk.getDemandTP();
			demand = lnk.getDemandVector();
			capacityTP = lnk.getCapacityTP();
			capacity = lnk.getCapacityVector();
		}
		return res;
	}
	
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Link.
	 */
	public String toString() {
		String buf = "";
		AbstractNode bn = getBeginNode();
		AbstractNode en = getEndNode();
		if (bn != null) 
			buf += bn.getName();
		else
			buf += "";
		buf += " > ";
		if (en != null)
			buf += en.getName();
		else
			buf += "";
		buf += " (" + Integer.toString(id) + ")";
		return buf;
	}
	
}
