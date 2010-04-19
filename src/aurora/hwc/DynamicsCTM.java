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
		double density = x.getDensity().sum().getCenter() * x.getWeavingFactor().getCenter();
		double cv = Math.min(x.getMaxFlow(), x.getW() * (x.getJamDensity() - density));
		return new AuroraInterval(cv);
	}
	
	/**
	 * Computes allowed lower bound interval of in-flow that given Link can accept.<br>
	 * @param x given Link.
	 * @return allowed in-flow (type <code>AuroraInterval</code>).
	 */
	public Object computeCapacityL(AbstractLinkHWC x) {
		double wf = x.getWeavingFactor().getLowerBound();
		if (x.isWFUpperBoundFirst())
			wf = x.getWeavingFactor().getUpperBound();
		double densityLB = x.getDensity().sum().getLowerBound() * wf;
		AuroraInterval cap = x.getMaxFlowRange();
		double lb = Math.min(cap.getLowerBound(), x.getW() * (x.getJamDensityRange().getLowerBound() - densityLB));
		double ub = Math.min(cap.getUpperBound(), x.getW() * (x.getJamDensityRange().getUpperBound() - densityLB));
		cap.setBounds(lb, ub);
		cap.constraintLB(0);
		return cap;
	}
	
	/**
	 * Computes allowed upper bound interval of in-flow that given Link can accept.<br>
	 * @param x given Link.
	 * @return allowed in-flow (type <code>AuroraInterval</code>).
	 */
	public Object computeCapacityU(AbstractLinkHWC x) {
		double wf = x.getWeavingFactor().getUpperBound();
		if (x.isWFUpperBoundFirst())
			wf = x.getWeavingFactor().getLowerBound();
		double densityUB = x.getDensity().sum().getUpperBound() * wf;
		AuroraInterval cap = x.getMaxFlowRange();
		double lb = Math.min(cap.getLowerBound(), x.getW() * (x.getJamDensityRange().getLowerBound() - densityUB));
		double ub = Math.min(cap.getUpperBound(), x.getW() * (x.getJamDensityRange().getUpperBound() - densityUB));
		cap.setBounds(lb, ub);
		cap.constraintLB(0);
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
		AbstractNode bnd = x.getBeginNode();
		if (bnd == null) {
			den.copy((AuroraIntervalVector)x.getQueue());
			den.inverseAffineTransform(x.getLength(), 0);
			den2.copy(den);
		}
		else {
			AuroraIntervalVector ifl = new AuroraIntervalVector();
			AuroraIntervalVector ofl = new AuroraIntervalVector();
			double[] ifL, ifU, ofL, ofU;
			double wfL, wfU, iwfL, iwfU;
			ifl.copy((AuroraIntervalVector)bnd.getOutputs().get(bnd.getSuccessors().indexOf(x)));
			if (x.isInputUpperBoundFirst()) {
				ifL = ifl.getUpperBounds();
				ifU = ifl.getLowerBounds();
			}
			else {
				ifL = ifl.getLowerBounds();
				ifU = ifl.getUpperBounds();
			}
			if (x.isIWFUpperBoundFirst()) {
				iwfL = x.getInputWeavingFactor().getUpperBound();
				iwfU = x.getInputWeavingFactor().getLowerBound();
			}
			else {
				iwfL = x.getInputWeavingFactor().getLowerBound();
				iwfU = x.getInputWeavingFactor().getUpperBound();
			}
			if (x.isWFUpperBoundFirst()) {
				wfL = x.getWeavingFactor().getUpperBound();
				wfU = x.getWeavingFactor().getLowerBound();
			}
			else {
				wfL = x.getWeavingFactor().getLowerBound();
				wfU = x.getWeavingFactor().getUpperBound();
			}
			AbstractNode end = x.getEndNode();
			if (end == null) {
				ofl = x.getFlow();
				AuroraInterval cap = x.getCapacityValue();
				double lbc = cap.getUpperBound()/ofl.sum().getLowerBound();
				double ubc = cap.getLowerBound()/ofl.sum().getUpperBound();
				ofL = ofl.getLowerBounds();
				ofU = ofl.getUpperBounds();
				for (int i = 0; i < ofL.length; i++) {
					if (lbc < 0)
						ofL[i] = lbc * ofL[i];
					if (ubc < 0)
						ofU[i] = ubc * ofU[i];
					if (Math.abs(ofL[i] - ofU[i]) < Util.EPSILON) // to avoid rounding errors
						ofL[i] = ofU[i];
				}
			}
			else {
				ofl.copy((AuroraIntervalVector)end.getInputs().get(end.getPredecessors().indexOf(x)));
				if (x.isOutputUpperBoundFirst()) {
					ofL = ofl.getUpperBounds();
					ofU = ofl.getLowerBounds();
				}
				else {
					ofL = ofl.getLowerBounds();
					ofU = ofl.getUpperBounds();
				}
			}
			den.copy((AuroraIntervalVector)x.getDensity());
			den2.copy(den);
			double dtdx = x.getMyNetwork().getTP()/x.getLength();
			for (int i = 0; i < den.size(); i++) {
				double dlb = den.get(i).getLowerBound() + dtdx * (ifL[i] - ofL[i]);
				double dub = den.get(i).getUpperBound() + dtdx * (ifU[i] - ofU[i]);
				double dlb2 = wfL*den.get(i).getLowerBound() + dtdx * (iwfL*ifL[i] - wfL*ofL[i]);
				double dub2 = wfU*den.get(i).getUpperBound() + dtdx * (iwfU*ifU[i] - wfU*ofU[i]);
				if (Math.abs(dub - dlb) < Util.EPSILON) { // avoid rounding error
					dub = dlb;
					dub2 = dlb2;
				}
				den.get(i).setBounds(dlb, dub);
				den.get(i).constraintLB(0);
				den2.get(i).setBounds(dlb2, dub2);
				den2.get(i).constraintLB(0);
			}
		}
		double dL = den.sum().getLowerBound();
		double dU = den.sum().getUpperBound();
		double wf2L = den2.sum().getLowerBound();
		double wf2U = den2.sum().getUpperBound();
		AuroraIntervalVector den3 = new AuroraIntervalVector();
		den3.copy(den);
		double[] owf = x.getOutputWeavingFactors();
		den3.affineTransform(owf, 0);
		double wf3L = den3.sum().getLowerBound();
		double wf3U = den3.sum().getUpperBound();
		if (dL > Util.EPSILON) {
			wf2L = wf2L / dL;
			wf3L = wf3L / dL;
		}
		else {
			wf2L = 1;
			wf3L = 1;
		}
		if (dU > Util.EPSILON) {
			wf2U = wf2U / dU;
			wf3U = wf3U / dU;
		}
		else {
			wf2U = 1;
			wf3U = 1;
		}
		wf2L = Math.max(wf2L, wf3L);
		wf2U = Math.max(wf2U, wf3U);
		x.inverseWFBounds(wf2U < wf2L);
		x.setWeavingFactor(new AuroraInterval((wf2L+wf2U)/2, Math.abs(wf2U-wf2L)));
		return den;
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
	public Object computeFlow(AbstractLinkHWC x) {
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
		double lbc = fmax.getLowerBound()/flow.sum().getLowerBound();
		double ubc = fmax.getUpperBound()/flow.sum().getUpperBound();
		if (lbc < 1)
			for (int i = 0; i < flow.size(); i++)
				flow.get(i).setLowerBound(lbc * flow.get(i).getLowerBound());
		int count = 0;
		while ((ubc < 1) && (count < flow.size())) {
			double ns = 0;
			count = 0;
			for (int i = 0; i < flow.size(); i++) {
				double c = 1;
				double v = flow.get(i).getUpperBound();
				if (v > Util.EPSILON)
					c = flow.get(i).getLowerBound()/v;
				if (ubc <= c) {
					v = c * v;
					flow.get(i).setUpperBound(v);
					ns += v;
				}
				else {
					flow.get(i).setUpperBound(ubc * v);
					count++;
				}
				flow.get(i).toLower(Util.EPSILON);
			}
			if (flow.sum().getUpperBound() - ns < Util.EPSILON)
				ubc = 1;
			else
				ubc = (fmax.getUpperBound() - ns)/(flow.sum().getUpperBound() - ns);
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
		density.product(x.getWeavingFactor());
		if (density.getUpperBound() < 1) // in lieu of (density == 0)
			return new AuroraInterval(x.getV());
		AuroraInterval ofl = x.getActualFlow().sum();
		double lb, ub;
		AbstractNodeHWC en = (AbstractNodeHWC)x.getEndNode();
		if ((en != null) && (x.isOutputUpperBoundFirst())) {
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