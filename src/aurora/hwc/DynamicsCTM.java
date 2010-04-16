/**
 * @(#)DynamicsCTM.java
 */

package aurora.hwc;

import java.io.*;
import aurora.*;
import aurora.util.Util;


/**
 * Implementation of Cell Transmission Model (CTM).
 * @author Alex Kurzhanskiy
 * @version $Id$
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
		double density = x.getDensity().sum().getCenter() * x.getWeavingFactor();
		double cv = Math.min(x.getMaxFlow(), x.getW() * (x.getJamDensity() - density));
		return new AuroraInterval(cv);
	}
	
	/**
	 * Computes allowed lower bound interval of in-flow that given Link can accept.<br>
	 * @param x given Link.
	 * @return allowed in-flow (type <code>AuroraInterval</code>).
	 */
	public Object computeCapacityL(AbstractLinkHWC x) {
		double densityLB = x.getDensity().sum().getLowerBound() * x.getWeavingFactor();
		AuroraInterval cap = x.getMaxFlowRange();
		double lb = Math.min(cap.getLowerBound(), x.getW() * (x.getJamDensityRange().getLowerBound() - densityLB));
		double ub = Math.min(cap.getUpperBound(), x.getW() * (x.getJamDensityRange().getUpperBound() - densityLB));
		cap.setBounds(lb, ub);
		return cap;
	}
	
	/**
	 * Computes allowed upper bound interval of in-flow that given Link can accept.<br>
	 * @param x given Link.
	 * @return allowed in-flow (type <code>AuroraInterval</code>).
	 */
	public Object computeCapacityU(AbstractLinkHWC x) {
		double densityUB = x.getDensity().sum().getUpperBound() * x.getWeavingFactor();
		AuroraInterval cap = x.getMaxFlowRange();
		double lb = Math.min(cap.getLowerBound(), x.getW() * (x.getJamDensityRange().getLowerBound() - densityUB));
		double ub = Math.min(cap.getUpperBound(), x.getW() * (x.getJamDensityRange().getUpperBound() - densityUB));
		cap.setBounds(lb, ub);
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
		AuroraIntervalVector den2 = new AuroraIntervalVector();
		AuroraIntervalVector den3 = new AuroraIntervalVector();
		AbstractNode bnd = x.getBeginNode();
		if (bnd == null) {
			den.copy((AuroraIntervalVector)x.getQueue());
			den.inverseAffineTransform(x.getLength(), 0);
			den2.copy(den);
		}
		else {
			AuroraIntervalVector ifl = new AuroraIntervalVector();
			AuroraIntervalVector ofl = new AuroraIntervalVector();
			ifl.copy((AuroraIntervalVector)bnd.getOutputs().get(bnd.getSuccessors().indexOf(x)));
			AbstractNode end = x.getEndNode();
			if (end == null) {
				ofl = x.getFlow();
				AuroraInterval cap = new AuroraInterval();
				cap.copy(x.getCapacityValue());
				ofl.affineTransform(Math.min(cap.getUpperBound()/ofl.sum().getUpperBound(), 1), 0);
			}
			else
				ofl.copy((AuroraIntervalVector)end.getInputs().get(end.getPredecessors().indexOf(x)));
			den.copy((AuroraIntervalVector)x.getDensity());
			AuroraIntervalVector ifl2 = new AuroraIntervalVector();
			AuroraIntervalVector ofl2 = new AuroraIntervalVector();
			den2.copy(den);
			ifl2.copy(ifl);
			ofl2.copy(ofl);
			den2.affineTransform(x.getWeavingFactor(), 0);
			ifl2.affineTransform(x.getInputWeavingFactor(), 0);
			ofl2.affineTransform(x.getWeavingFactor(), 0);
			//TODO: lb-lb; ub-ub
			ifl.subtract(ofl);
			ifl.affineTransform(x.getMyNetwork().getTP()/x.getLength(), 0);
			den.add(ifl);
			ifl2.subtract(ofl2);
			ifl2.affineTransform(x.getMyNetwork().getTP()/x.getLength(), 0);
			den2.add(ifl2);
		}
		den.constraintLB(0);
		den2.constraintLB(0);
		/*if (x.getJamDensity() < den.sum().getUpperBound()) {
			double s = x.getJamDensity() / den.sum().getUpperBound();
			for (int i = 0; i < den.size(); i++)
				den.get(i).setUpperBound(s * den.get(i).getUpperBound());
		}
		if (x.getJamDensity() < den2.sum().getUpperBound()) {
			double s = x.getJamDensity() / den2.sum().getUpperBound();
			for (int i = 0; i < den2.size(); i++)
				den2.get(i).setUpperBound(s * den2.get(i).getUpperBound());
		}*/
		den3.copy(den);
		double[] owf = x.getOutputWeavingFactors();
		den3.affineTransform(owf, 0);
		double wf3 = den3.sum().getCenter();
		double wf2 = den2.sum().getCenter();
		double dnm = den.sum().getCenter();
		if (dnm > 0.000000001) {
			wf2 = wf2 / dnm;
			wf3 = wf3 / dnm;
		}
		else {
			wf2 = 1;
			wf3 = 1;
		}
		x.setWeavingFactor(Math.max(wf2, wf3));
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
			/* FIXME: unnecessary code
			if (x.getEndNode() == null) {
				if (x.getMaxFlow() < flow.sum().getUpperBound()) {
					double fv = x.getMaxFlow() / flow.sum().getUpperBound();
					for (int i = 0; i < flow.size(); i++)
						flow.get(i).affineTransformUB(fv, 0);
				}
			} */
		}
		double fmax = x.getMaxFlow() - x.getCapacityDrop();
		double lb = flow.sum().getLowerBound();
		double ub = flow.sum().getUpperBound();
		if (fmax < ub) {
			if (fmax < lb) {
				for (int i = 0; i < flow.size(); i++) {
					flow.get(i).constraintUB((fmax/ub)*flow.get(i).getUpperBound());
				}
			}
			else {
				for (int i = 0; i < flow.size(); i++) {
					flow.get(i).setBounds(flow.get(i).getLowerBound(), (fmax/ub)*flow.get(i).getUpperBound());
				}
			}
		}
		return flow;
	}
	
	/**
	 * Computes desired out-flow range from given Link.<br>
	 * Looks at density. If density is below critical, then
	 * the flow is computed from the fundamental diagram;
	 * else - maximum flow is taken.<br>
	 * If given Link is Origin Link (has no begin Node), then
	 * demand and queue determine the value of desired out-flow.
	 * @param x given Link.
	 * @return desired out-flow (type <code>AuroraIntervalVector</code>).
	 */
	public Object computeFlowLU(AbstractLinkHWC x) {
		AuroraIntervalVector flow = new AuroraIntervalVector();
		AbstractNode bnd = x.getBeginNode();
		if (bnd == null)
			flow.copy((AuroraIntervalVector)x.getDemandValue());
		else {
			flow.copy((AuroraIntervalVector)x.getDensity());
			flow.affineTransform(x.getV(), 0);
		}
		AuroraInterval fmax = x.getMaxFlowRange();
		fmax.affineTransform(1, -x.getCapacityDrop());
		double lbc = flow.sum().getLowerBound()/fmax.getLowerBound();
		if (lbc < 1)
			for (int i = 0; i < flow.size(); i++)
				flow.get(i).setLowerBound(lbc * flow.get(i).getLowerBound());
		double ubc = flow.sum().getUpperBound()/fmax.getUpperBound();
		while (ubc < 1 - Util.EPSILON) {
			double ns = 0;
			for (int i = 0; i < flow.size(); i++) {
				double c = 1;
				double v = flow.get(i).getUpperBound();
				if (v > Util.EPSILON)
					c = flow.get(i).getLowerBound()/v;
				if (ubc < c) {
					v = c * v;
					flow.get(i).setUpperBound(v);
					ns += v;
				}
				else
					flow.get(i).setUpperBound(ubc * v);
			}
			ubc = (flow.sum().getUpperBound() - ns)/(fmax.getUpperBound() - ns);
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
		AuroraInterval density = ((AuroraIntervalVector)x.getDensity()).sum();
		density.affineTransform(x.getWeavingFactor(), 0);
		if (density.getUpperBound() < 1) // in lieu of (density == 0)
			return new AuroraInterval(x.getV());
		AuroraInterval ofl = x.getActualFlow().sum();
		double lb, ub;
		AbstractNodeHWC en = (AbstractNodeHWC)x.getEndNode();
		if ((en != null) && (en.isInputUpperBoundFirst())) {
			lb = ofl.getLowerBound() / density.getUpperBound();
			ub = ofl.getUpperBound() / density.getLowerBound();
		}
		else {
			lb = ofl.getLowerBound() / density.getLowerBound();
			ub = ofl.getUpperBound() / density.getUpperBound();
		}
		AuroraInterval v = new AuroraInterval();
		v.setBounds(lb, ub);
		v.constraint(new AuroraInterval(x.getV()/2, x.getV()));
		return v;
	}
	
	/**
	 * Returns letter code of the model type.
	 */
	public String getTypeLetterCode() {
		return "CTM";
	}

}