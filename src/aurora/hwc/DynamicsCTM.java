/**
 * @(#)DynamicsCTM.java
 */

package aurora.hwc;

import java.io.*;
import aurora.*;


/**
 * Implementation of Cell Transmission Model (CTM).
 * @author Alex Kurzhanskiy
 * @version $Id: DynamicsCTM.java,v 1.4.2.5.2.7 2008/12/11 20:42:37 akurzhan Exp $
 */
public class DynamicsCTM implements DynamicsHWC, Serializable {
	private static final long serialVersionUID = 1295134323359426734L;

	/**
	 * Computes allowed in-flow that given Link can accept.<br>
	 * In-flow cannot exceed maximum flow for this Link.
	 * If given Link is Destination Link, then its maximum flow is returned.
	 * @param x given Link.
	 * @return allowed in-flow (type <code>AuroraInterval</code>).
	 */
	public Object computeCapacity(AbstractLinkHWC x) {
		AuroraInterval cap = x.getDensity().sum();
		cap.negative();
		cap.affineTransform(1, x.getJamDensity());
		cap.affineTransform(x.getW(), 0);
		cap.constraint(new AuroraInterval(x.getMaxFlow()/2, x.getMaxFlow()));
		return cap;
	}

	/**
	 * Computes traffic density in the given Link.<br>
	 * New value of density is computed from the current density,
	 * incoming and outgoing flows.
	 * This value is nonnegative and does not exceed jam density.
	 * @param x given Link.
	 * @return traffic density (type <code>AuroraIntervalVector</code>).
	 */
	public Object computeDensity(AbstractLinkHWC x) {
		AuroraIntervalVector den = new AuroraIntervalVector();
		AbstractNode bnd = x.getBeginNode();
		if (bnd == null) {
			den.copy((AuroraIntervalVector)x.getQueue());
			den.inverseAffineTransform(x.getLength(), 0);
		}
		else {
			AuroraIntervalVector ifl = new AuroraIntervalVector();
			AuroraIntervalVector ofl = new AuroraIntervalVector();
			ifl.copy((AuroraIntervalVector)bnd.getOutputs().get(bnd.getSuccessors().indexOf(x)));
			AbstractNode end = x.getEndNode();
			if (end == null) {
				ofl = x.getDensity();
				ofl.affineTransform(x.getV(), 0);
				AuroraInterval cap = new AuroraInterval();
				cap.copy(x.getCapacityValue());
				ofl.affineTransform(Math.min(cap.getUpperBound()/ofl.sum().getUpperBound(), 1), 0);
			}
			else
				ofl.copy((AuroraIntervalVector)end.getInputs().get(end.getPredecessors().indexOf(x)));
			den.copy((AuroraIntervalVector)x.getDensity());
			ifl.subtract(ofl);
			ifl.affineTransform(x.getMyNetwork().getTP()/x.getLength(), 0);
			den.add(ifl);
		}
		den.constraintLB(0);
		if (x.getJamDensity() < den.sum().getUpperBound()) {
			double s = x.getJamDensity() / den.sum().getUpperBound();
			for (int i = 0; i < den.size(); i++)
				den.get(i).setUpperBound(s * den.get(i).getUpperBound());
		}
		return den;
	}

	/**
	 * Computes desired out-flow from given Link.<br>
	 * Looks at density. If density is below critical, then
	 * the flow is computed from the fundamental diagram;
	 * else - maximum flow is taken.<br>
	 * If given Link is Origin Link (has no begin Node), then
	 * demand and queue determine the value of desired out-flow.
	 * @param x given Link.
	 * @return desired out-flow (type <code>AuroraIntervalVector</code>).
	 */
	public Object computeFlow(AbstractLinkHWC x) {
		AuroraIntervalVector flow = new AuroraIntervalVector();
		AbstractNode bnd = x.getBeginNode();
		if (bnd == null)
			flow.copy((AuroraIntervalVector)x.getDemandValue());
		else {
			flow.copy((AuroraIntervalVector)x.getDensity());
			flow.affineTransform(x.getV(), 0);
			if (x.getEndNode() == null) {
				if (x.getMaxFlow() < flow.sum().getUpperBound()) {
					double fv = x.getMaxFlow() / flow.sum().getUpperBound();
					for (int i = 0; i < flow.size(); i++)
						flow.get(i).affineTransformUB(fv, 0);
				}
			}
		}
		return flow;
	}

	/**
	 * Computes traffic speed in given Link.<br>
	 * Speed depends on the density in the Link and
	 * flow that leaves the Link, and it cannot be
	 * greater than the free flow speed.
	 * @param x given Link.
	 * @return traffic speed (type <code>AuroraInterval</code>).
	 */
	public Object computeSpeed(AbstractLinkHWC x) {
		AuroraInterval rho = ((AuroraIntervalVector)x.getDensity()).sum();
		if (rho.getUpperBound() < 1) // in lieu of (rho == 0)
			return new AuroraInterval(x.getV());
		AuroraInterval v = x.getActualFlow().sum();
		v.quotient(rho);
		v.constraint(new AuroraInterval(x.getV()/2, x.getV()));
		return v;
	}

}