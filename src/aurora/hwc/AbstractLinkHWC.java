/**
 * @(#)AbstractLinkHWC.java
 */

package aurora.hwc;

import java.io.*;
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
 * @version $Id: AbstractLinkHWC.java,v 1.21.2.16.2.14 2009/01/16 01:43:27 akurzhan Exp $
 */
public abstract class AbstractLinkHWC extends AbstractLink {
	protected double lanes = 1.0;
	protected double flowMax = 1800; // in vph
	protected double densityCritical = 30; // in vpm
	protected double densityJam = 150; // in vpm
	
	//protected Vector<Double> densityH = new Vector<Double>();
	//protected Vector<Double> oflowH = new Vector<Double>();
	
	protected AuroraIntervalVector density = new AuroraIntervalVector(); // in vpm
	protected AuroraIntervalVector density0 = new AuroraIntervalVector(); // initial value in vpm
	protected AuroraInterval speed = new AuroraInterval(); // mean speed 
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
	protected double demandTP = 1.0/12.0; // demand value change period (default: 1/12 hour)
	protected double demandKnob = 1.0; //coefficient by which the demand value must be multiplied
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
						density.copy(density0);
					}
					if (pp.item(i).getNodeName().equals("demand")) {
						demandTP = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("tp").getNodeValue());
						demandKnob = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("knob").getNodeValue());
						setDemandVector(pp.item(i).getTextContent());
					}
					if (pp.item(i).getNodeName().equals("capacity")) {
						capacityTP = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("tp").getNodeValue());
						setCapacityVector(pp.item(i).getTextContent());
					}
					if (pp.item(i).getNodeName().equals("qmax"))
						qMax = Double.parseDouble(pp.item(i).getTextContent());
					if (pp.item(i).getNodeName().equals("fd")) {
						flowMax = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("flowMax").getNodeValue());
						densityCritical = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("densityCritical").getNodeValue());
						densityJam = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("densityJam").getNodeValue());
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
		out.print("<link class=\"" + this.getClass().getName() + "\" id=\"" + Integer.toString(id) + "\" length=\"" + Double.toString(length) + "\" lanes=\"" + Double.toString(lanes) + "\">");
		if (predecessors.size() > 0)
			out.print("<begin id=\"" + Integer.toString(predecessors.firstElement().getId()) + "\"/>");
		if (successors.size() > 0)
			out.print("<end id=\"" + Integer.toString(successors.firstElement().getId()) + "\"/>");
		out.print("<dynamics class=\"" + myDynamics.getClass().getName() + "\"/>");
		out.print("<density>" + density.toStringWithInverseWeights(((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).getVehicleWeights()) + "</density>");
		if (!demand.isEmpty()) {
			out.print("<demand tp=\"" + Double.toString(demandTP) + "\" knob=\"" + Double.toString(demandKnob) + "\">");
			out.print(getDemandVectorAsString());
			out.print("</demand>");
		}
		if (!capacity.isEmpty()) {
			out.print("<capacity tp=\"" + Double.toString(capacityTP) + "\">");
			out.print(getCapacityVectorAsString());
			out.print("</capacity>");
		}
		out.print("<qmax>" + Double.toString(qMax) + "</qmax>");
		out.print("<fd densityCritical =\"" + Double.toString(densityCritical) + "\" densityJam=\"" + Double.toString(densityJam) + "\" flowMax=\"" + Double.toString(flowMax) + "\"/>");
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
			if (predecessors.size() == 0) {
				AbstractNode end = (AbstractNode)successors.firstElement(); // assumed != null
				AuroraIntervalVector ofl = (AuroraIntervalVector)(end.getInputs().get(end.getPredecessors().indexOf(this)));
				double tp = myNetwork.getTP();
				qSize.affineTransform(1/tp, 0);
				qSize.add((AuroraIntervalVector)getDemand());
				qSize.subtract(ofl);
				qSize.affineTransform(tp, 0);
			}
			else
				qSize.set(new AuroraInterval());
			for (int i = 0; i < qSize.size(); i++)  // make sure queue has no negative values
				if (qSize.get(i).getUpperBound() < 0.0)
					qSize.get(i).setCenter(0.0, 0.0);
				else
					qSize.get(i).setBounds(Math.max(qSize.get(i).getLowerBound(), 0.0), qSize.get(i).getUpperBound());
			speed.copy((AuroraInterval)myDynamics.computeSpeed(this));
			density = (AuroraIntervalVector)myDynamics.computeDensity(this);
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
				ploss = Math.max((tp * length * (1 - (getActualFlow().sum().getCenter() / flowMax))), 0.0);
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
	 * Resets the simulation time step.
	 */
	public void resetTimeStep() {
		super.resetTimeStep();
		if (getBeginNode() == null) {
			qSize.copy(density0);
			qSize.affineTransform(length, 0);
		}
		else
			qSize = new AuroraIntervalVector(((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes());
		density.copy(density0);
		speed.setCenter(0.0, 0.0);
		vht = 0.0;
		vmt = 0.0;
		delay = 0.0;
		ploss = 0.0;
		vhtSum = 0.0;
		vmtSum = 0.0;
		delaySum = 0.0;
		plossSum = 0.0;
		return;
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
		else
			flw = getFlow();
		return flw;
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
	 * Returns number of lanes in the link.
	 */
	public final double getLanes() {
		return lanes;
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
		return (density.sum().getCenter() / densityJam);
	}
	
	/**
	 * Returns demand.
	 */
	public final AuroraIntervalVector getDemand() {
		double t = myNetwork.getSimTime(); // simulation time (in hours)
		int idx = (int)Math.floor(t/demandTP);
		int n = demand.size() - 1; // max index of the demand profile
		if (n < 0) //empty
			return new AuroraIntervalVector();
		if ((idx < 0) || (idx > n))
			idx = n;
		AuroraIntervalVector dmnd = new AuroraIntervalVector();
		dmnd.copy(demand.get(idx));
		dmnd.affineTransform(demandKnob, 0);
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
			buf += demand.get(0).toStringWithInverseWeights(((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).getVehicleWeights());
			for (int i = 1; i < n; i++)
				buf += ", " + demand.get(i).toStringWithInverseWeights(((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).getVehicleWeights());
		}
		return buf;
	}
	
	/**
	 * Returns the value of the demand knob.
	 */
	public final double getDemandKnob() {
		return demandKnob;
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
		return capacity.get(idx);
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
	 * Assigns maximum flow.
	 * @param x maximum flow.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setMaxFlow(double x) {
		if (x < 0.0)
			return false;
		flowMax = x;
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
	 * Initializes the fundamental diagram with defaults.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean defaultFD() {
		flowMax = 1800 * lanes;
		densityCritical = 30.0 * lanes;
		densityJam = 150 * lanes;
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
			demand.add(v);
		}
		return true;
	}
	
	/**
	 * Sets demand knob value.<br>
	 * @param x demand knob value.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setDemandKnob(double x) {
		if (x >= 0.0)
			demandKnob = x;
		else
			return false;
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
	 * Resets VHT sum.
	 */
	public synchronized void resetSumVHT() {
		vhtSum = 0.0;
		return;
	}
	
	/**
	 * Resets VMT sum.
	 */
	public synchronized void resetSumVMT() {
		vmtSum = 0.0;
		return;
	}
	
	/**
	 * Resets delay sum.
	 */
	public synchronized void resetSumDelay() {
		delaySum = 0.0;
		return;
	}
	
	/**
	 * Resets productivity loss sum.
	 */
	public synchronized void resetSumPLoss() {
		plossSum = 0.0;
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
			setFD(lnk.getMaxFlow(), lnk.getCriticalDensity(), lnk.getJamDensity());
			density = (AuroraIntervalVector)lnk.getDensity();
			density0 = (AuroraIntervalVector)lnk.getInitialDensity();
			qMax = lnk.getQueueMax();
			demandKnob = lnk.getDemandKnob();
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
