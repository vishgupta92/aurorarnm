/**
 * @(#)AuroraInterval.java
 */

package aurora;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;


/**
 * Implementation of an interval with basic arithmetic.
 * 
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public class AuroraInterval implements Serializable {
	private static final long serialVersionUID = -7691830786246016694L;
	
	protected double center = 0.0;
	protected double size = 0.0;
	
	
	public AuroraInterval() { }
	public AuroraInterval(double ctr) { center = ctr; }
	public AuroraInterval(double ctr, double sz) {
		this(ctr);
		if (sz >= 0.0)
			size = sz;
	}
	
	
	/**
	 * Return the interval center.
	 */
	public double getCenter() {
		return center;
	}
	
	/**
	 * Return the interval size.
	 */
	public double getSize() {
		return size;
	}
	
	/**
	 * Return the lower bound of the interval.
	 */
	public double getLowerBound() {
		double res = center;
		if (size > 0)
			res = res - (size/2);
		return res;
	}
	
	/**
	 * Return the upper bound of the interval.
	 */
	public double getUpperBound() {
		double res = center;
		if (size > 0)
			res = res + (size/2.0);
		return res;
	}
	
	
	/**
	 * Assigns the interval center.
	 * @param ctr interval center.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setCenter(double ctr) {
		center = ctr;
		return true;
	}
	
	/**
	 * Sets the interval through its center and size.
	 * @param ctr interval center.
	 * @param sz interval size.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setCenter(double ctr, double sz) {
		boolean res = false;
		center = ctr;
		if (sz >= 0.0) {
			size = sz;
			res = true;
		}
		return res;
	}
	
	/**
	 * Assigns the lower bound of the interval.
	 * @param lb lower bound.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setLowerBound(double lb) {
		double ub = getUpperBound();
		if (lb > ub)
			ub = lb;
		return setBounds(lb, ub);
	}
	
	/**
	 * Sets the interval through its lower bound and size.
	 * @param lb lower bound.
	 * @param sz interval size.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setLowerBound(double lb, double sz) {
		boolean res = false;
		if (sz >= 0.0) {
			size = sz;
			res = true;
		}
		center = lb + (size/2.0);
		return res;
	}
	
	/**
	 * Assigns the upper bound of the interval.
	 * @param ub upper bound.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setUpperBound(double ub) {
		double lb = getLowerBound();
		if (ub < lb)
			lb = ub;
		return setBounds(lb, ub);
	}
	
	/**
	 * Sets the interval through its upper bound and size.
	 * @param ub upper bound.
	 * @param sz interval size.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setUpperBound(double ub, double sz) {
		boolean res = false;
		if (sz >= 0.0) {
			size = sz;
			res = true;
		}
		center = ub - (size/2.0);
		return res;
	}
	
	/**
	 * Sets the interval through its bounds.
	 * @param lb lower bound.
	 * @param ub upper bound.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setBounds(double lb, double ub) {
		center = (lb + ub)/2.0;
		size = Math.abs(ub - lb);
		return true;
	}
	
	/**
	 * Makes sure the interval is above the minimal allowed lower bound.
	 * @param lb minimal allowed lower bound.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean constraintLB(double lb) {
		double ub = getUpperBound();
		if (lb > ub)
			return setCenter(lb, 0);
		return setBounds(Math.max(lb, getLowerBound()), ub);
	}
	
	/**
	 * Makes sure the interval is below the maximal allowed upper bound.
	 * @param ub maximal allowed upper bound.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean constraintUB(double ub) {
		double lb = getLowerBound();
		if (ub < lb)
			return setCenter(ub, 0);
		return setBounds(lb, Math.min(ub, getUpperBound()));
	}
	
	/**
	 * Constraints the bounds of the interval to the given interval.
	 * @param v given interval.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean constraint(AuroraInterval v) {
		boolean res = intersection(v);
		if (res)
			return res;
		res = true;
		if (getUpperBound() < v.getLowerBound())
			res &= setLowerBound(v.getLowerBound());
		else
			res &= setUpperBound(v.getUpperBound());
		return res;
	}
	
	/**
	 * Sets the interval from the given text string.
	 * @param buf text string in format: <code>\<center\>(\<size\>)</code>.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setIntervalFromString(String buf) {
		if (buf == null)
			return false;
		StringTokenizer st = new StringTokenizer(buf, "()");
		if (st.countTokens() < 1)
			return false;
		double ctr = Double.parseDouble(st.nextToken());
		double sz = 0;
		if (st.hasMoreTokens())
			sz = Double.parseDouble(st.nextToken());
		return setCenter(ctr, sz);
	}
	
	/**
	 * Sets the interval bounds to those of the given interval.
	 * @param x given interval.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean copy(AuroraInterval x) {
		if (x == null)
			return false;
		center = x.getCenter();
		size = x.getSize();
		return true;
	}
	
	
	/**
	 * Negative of the interval.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean negative() {
		center = - center;
		return true;
	}
	
	/**
	 * Reciprocal of the interval.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean reciprocal() {
		boolean res = false;
		double lb = getLowerBound();
		double ub = getUpperBound();
		if ((lb > 0.0) || (ub < 0.0))  // 0 must be outside the interval
			res = setBounds(1/ub, 1/lb);
		return res;
	}
	
	/**
	 * Affine transformation of the interval.
	 * @param scale scaling factor.
	 * @param shift affine term.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean affineTransform(double scale, double shift) {
		double lb = scale * getLowerBound() + shift;
		double ub = scale * getUpperBound() + shift;
		return setBounds(lb, ub);
	}
	
	/**
	 * Inverse affine transformation of the interval.
	 * @param scale scaling factor.
	 * @param shift affine term.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean inverseAffineTransform(double scale, double shift) {
		if (scale == 0.0)
			return false;
		double lb = (1/scale) * (getLowerBound() - shift);
		double ub = (1/scale) * (getUpperBound() - shift);
		return setBounds(lb, ub);
	}
	
	/**
	 * Affine transformation of the lower bound of the interval.
	 * @param scale scaling factor.
	 * @param shift affine term.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean affineTransformLB(double scale, double shift) {
		return setLowerBound(scale * getLowerBound() + shift);
	}
	
	/**
	 * Inverse affine transformation of the lower bound of the interval.
	 * @param scale scaling factor.
	 * @param shift affine term.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean inverseAffineTransformLB(double scale, double shift) {
		if (scale == 0.0)
			return false;
		return setLowerBound((1/scale) * (getLowerBound() - shift));
	}
	
	/**
	 * Affine transformation of the upper bound of the interval.
	 * @param scale scaling factor.
	 * @param shift affine term.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean affineTransformUB(double scale, double shift) {
		return setUpperBound(scale * getUpperBound() + shift);
	}
	
	/**
	 * Inverse affine transformation of the upper bound of the interval.
	 * @param scale scaling factor.
	 * @param shift affine term.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean inverseAffineTransformUB(double scale, double shift) {
		if (scale == 0.0)
			return false;
		return setUpperBound((1/scale) * (getUpperBound() - shift));
	}
	
	/**
	 * Add given interval to the interval.
	 * @param x interval to be added.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean add(AuroraInterval x) {
		if (x == null)
			return false;
		return setBounds(getLowerBound() + x.getLowerBound(), getUpperBound() + x.getUpperBound());
	}
	
	/**
	 * Subtract given interval from the interval.
	 * @param x interval to be subtracted.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean subtract(AuroraInterval x) {
		if (x == null)
			return false;
		return setBounds(getLowerBound() - x.getUpperBound(), getUpperBound() - x.getLowerBound());
	}
	
	/**
	 * Compute the product given interval with the interval.
	 * @param x given interval.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean product(AuroraInterval x) {
		if (x == null)
			return false;
		double lb = getLowerBound();
		double ub = getUpperBound();
		double xlb = x.getLowerBound();
		double xub = x.getUpperBound();
		lb = Math.min(Math.min(lb*xlb, lb*xub), Math.min(ub*xlb, ub*xub));
		ub = Math.max(Math.max(lb*xlb, lb*xub), Math.max(ub*xlb, ub*xub));
		return setBounds(lb, ub);
	}
	
	/**
	 * Compute the quotient of the interval and the given interval.
	 * @param x given interval.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean quotient(AuroraInterval x) {
		boolean res = false;
		if (x == null)
			return res;
		double xlb = x.getLowerBound();
		double xub = x.getUpperBound();
		if ((xlb > 0.0) || (xub < 0.0)) {  // 0 must be outside the interval
			double lb = getLowerBound();
			double ub = getUpperBound();
			xlb = 1/x.getUpperBound();
			xub = 1/x.getLowerBound();
			lb = Math.min(Math.min(lb*xlb, lb*xub), Math.min(ub*xlb, ub*xub));
			ub = Math.max(Math.max(lb*xlb, lb*xub), Math.max(ub*xlb, ub*xub));
			res = setBounds(lb, ub);
		}
		return res;
	}
	
	/**
	 * Compute the intersection of the interval and the given interval.
	 * 
	 * @param x given interval.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean intersection(AuroraInterval x) {
		if (x == null)
			return false;
		double lb1 = getLowerBound();
		double ub1 = getUpperBound();
		double lb2 = x.getLowerBound();
		double ub2 = x.getUpperBound();
		if ((lb1 > ub2) || (lb2 > ub1))
			return false;
		return setBounds(Math.max(lb1, lb2), Math.min(ub1, ub2));
	}
	
	/**
	 * Chooses random value from the interval
	 * and centers this interval at this value with size 0.
	 */
	public synchronized void randomize() {
		center = getLowerBound() + Math.random() * size;
		size = 0;
		return;
	}
	
	
	/**
	 * Returns text description of the weighted interval.
	 * @return string that describes the weighted interval.
	 */
	public String toStringWithWeight(double w, boolean frmt) {
		NumberFormat form = NumberFormat.getInstance();
		form.setMinimumFractionDigits(0);
		form.setMaximumFractionDigits(4);
		String buf = Double.toString(w*center);
		if (frmt)
			buf = form.format(w*center);
		if (size > 0)
			if (frmt)
				buf += "(" + form.format(w*size) + ")";
			else
				buf += "(" + w*size + ")";
		return buf;
	}
	
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the interval.
	 */
	public String toString() {
		String buf = Double.toString(center);
		if (size > 0)
			buf += "(" + size + ")";
		return buf;
	}

	/**
	 * The same as toString, but with formatting.
	 * @return string that describes the interval.
	 */
	public String toString2() {
		NumberFormat form = NumberFormat.getInstance();
		form.setMinimumFractionDigits(0);
		form.setMaximumFractionDigits(2);
		String buf = form.format(center);
		if (size > 0)
			buf += "(" + form.format(size) + ")";
		return buf;
	}
	
	/**
	 * The same as toString, but with formatting.
	 * @return string that describes the interval.
	 */
	public String toString3() {
		NumberFormat form = NumberFormat.getInstance();
		form.setMinimumFractionDigits(0);
		form.setMaximumFractionDigits(2);
		form.setGroupingUsed(false);
		String buf = form.format(center);
		if (size > 0)
			buf += "(" + form.format(size) + ")";
		return buf;
	}
}


