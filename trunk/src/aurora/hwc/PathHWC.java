/**
 * @(#)PathHWC.java
 */

package aurora.hwc;

import aurora.*;


/**
 * HWC specific Path class.
 * @author Alex Kurzhanskiy
 * @version $Id: PathHWC.java,v 1.1.2.3.4.5 2009/03/21 22:20:10 akurzhan Exp $
 */
public class PathHWC extends Path {
	private static final long serialVersionUID = 7614255724050230035L;
	
	/**
	 * Returns instantaneous travel time in hours.
	 */
	public double getTravelTime() {
		double tt = 0.0;
		for (int i = 0; i < linkCount; i++) {
			if ((i < (linkCount - 1)) &&
				(((AbstractNodeHWC)linkSequence.get(i).getEndNode()).getSplitRatio(linkSequence.get(i), linkSequence.get(i+1)).sum().getCenter() <= Double.MIN_VALUE)) {
					tt = Double.MAX_VALUE;
					break;
				}
			tt += ((AbstractLinkHWC)linkSequence.get(i)).getTravelTime();
		}
		if ((tt == Double.NaN) || (tt == Double.POSITIVE_INFINITY) || (tt > 24))
			tt = 24; //Double.MAX_VALUE;
		return tt;
	}
	
	/**
	 * Returns minimal travel time in hours.
	 */
	public double getMinTravelTime() {
		double tt = 0.0;
		for (int i = 0; i < linkCount; i++) {
			if ((i < (linkCount - 1)) &&
				(((AbstractNodeHWC)linkSequence.get(i).getEndNode()).getSplitRatio(linkSequence.get(i), linkSequence.get(i+1)).sum().getCenter() <= Double.MIN_VALUE)) {
					tt = Double.MAX_VALUE;
					break;
				}
			tt += ((AbstractLinkHWC)linkSequence.get(i)).getMinTravelTime();
		}
		if ((tt == Double.NaN) || (tt == Double.POSITIVE_INFINITY) || (tt > 24))
			tt = 24; //Double.MAX_VALUE;
		return tt;
	}
	
	/**
	 * Returns array of flows that go from Link to Link in the Path.
	 */
	public AuroraIntervalVector[] getFlows() {
		AuroraIntervalVector flows[] = new AuroraIntervalVector[linkCount];
		for (int i = 0; i < linkCount; i++) {
			AbstractLinkHWC lk = (AbstractLinkHWC)linkSequence.get(i);
			flows[i] = lk.getActualFlow();
			flows[i].affineTransform(1/lk.getLanes(), 0);
		}
		return flows;
	}
	
	/**
	 * Returns array of densities for every Link in the Path.
	 */
	public AuroraIntervalVector[] getDensities() {
		AuroraIntervalVector densities[] = new AuroraIntervalVector[linkCount];
		for (int i = 0; i < linkCount; i++) {
			AbstractLinkHWC lk = (AbstractLinkHWC)linkSequence.get(i);
			densities[i] = lk.getDensity();
			densities[i].affineTransform(1/lk.getLanes(), 0);
		}
		return densities;
	}
	
	/**
	 * Returns array of speeds for every Link in the Path.
	 */
	public AuroraInterval[] getSpeeds() {
		AuroraInterval speeds[] = new AuroraInterval[linkCount];
		for (int i = 0; i < linkCount; i++)
			speeds[i] = ((AbstractLinkHWC)linkSequence.get(i)).getSpeed();
		return speeds;
	}

}
