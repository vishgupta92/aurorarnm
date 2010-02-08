/**
 * @(#)AuroraIntervalVector.java 
 */

package aurora;

import java.io.Serializable;
import java.util.*;


/**
 * Implementation of the interval vector type.
 * 
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public class AuroraIntervalVector implements Serializable {
	private static final long serialVersionUID = 8459142305862530744L;
	
	
	protected AuroraInterval[] data = new AuroraInterval[1];
	
	
	public AuroraIntervalVector() {
		data[0] = new AuroraInterval();
	}
	
	public AuroraIntervalVector(int sz) {
		data = new AuroraInterval[sz];
		for (int i = 0; i < sz; i++)
			data[i] = new AuroraInterval();
	}
	
	public AuroraIntervalVector(int sz, AuroraInterval v) {
		data = new AuroraInterval[sz];
		for (int i = 0; i < sz; i++) {
			data[i] = new AuroraInterval();
			data[i].copy(v);
		}
	}
	
	public AuroraIntervalVector(Vector<AuroraInterval> v) {
		if ((v == null) || (v.isEmpty())) {
			data[0] = new AuroraInterval();
			return;
		}
		int sz = v.size();
		data = new AuroraInterval[sz];
		for (int i = 0; i < sz; i++) {
			data[i] = new AuroraInterval();
			data[i].copy(v.get(i));
		}
	}
	
	public AuroraIntervalVector(AuroraInterval[] v) {
		if ((v == null) || (v.length == 0)) {
			data[0] = new AuroraInterval();
			return;
		}
		int sz = v.length;
		data = new AuroraInterval[sz];
		for (int i = 0; i < sz; i++) {
			data[i] = new AuroraInterval();
			data[i].copy(v[i]);
		}
	}

	
	/**
	 * Returns the size of the interval vector.
	 */
	public int size() {
		return data.length;
	}
	
	/**
	 * Returns the interval in given position.
	 */
	public AuroraInterval get(int idx) {
		if ((idx < 0) || (idx >= data.length))
			return null;
		return data[idx];
	}
	
	/**
	 * Returns array of interval centers.
	 */
	public double[] getCenters() {
		double[] res = new double[data.length];
		for (int i = 0; i < data.length; i++)
			res[i] = data[i].getCenter();
		return res;
	}
	
	/**
	 * Returns array of interval lower bounds.
	 */
	public double[] getLowerBounds() {
		double[] res = new double[data.length];
		for (int i = 0; i < data.length; i++)
			res[i] = data[i].getLowerBound();
		return res;
	}
	
	/**
	 * Returns array of interval upper bounds.
	 */
	public double[] getUpperBounds() {
		double[] res = new double[data.length];
		for (int i = 0; i < data.length; i++)
			res[i] = data[i].getUpperBound();
		return res;
	}
	
	/**
	 * Returns index of the interval with minimum center.
	 */
	public int minCenter() {
		double v = Double.MAX_VALUE;
		int idx = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i].getCenter() < v) {
				idx = i;
				v = data[i].getCenter();
			}
		return idx;
	}
	
	/**
	 * Returns index of the interval with minimum size.
	 */
	public int minSize() {
		double v = Double.MAX_VALUE;
		int idx = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i].getSize() < v) {
				idx = i;
				v = data[i].getSize();
			}
		return idx;
	}
	
	/**
	 * Returns index of the interval with minimum lower bound.
	 */
	public int minLowerBound() {
		double v = Double.MAX_VALUE;
		int idx = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i].getLowerBound() < v) {
				idx = i;
				v = data[i].getLowerBound();
			}
		return idx;
	}
	
	/**
	 * Returns index of the interval with minimum upper bound.
	 */
	public int minUpperBound() {
		double v = Double.MAX_VALUE;
		int idx = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i].getUpperBound() < v) {
				idx = i;
				v = data[i].getUpperBound();
			}
		return idx;
	}
	
	/**
	 * Returns index of the interval with maximum center.
	 */
	public int maxCenter() {
		double v = -Double.MAX_VALUE;
		int idx = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i].getCenter() > v) {
				idx = i;
				v = data[i].getCenter();
			}
		return idx;
	}
	
	/**
	 * Returns index of the interval with maximum size.
	 */
	public int maxSize() {
		double v = -1;
		int idx = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i].getSize() > v) {
				idx = i;
				v = data[i].getSize();
			}
		return idx;
	}
	
	/**
	 * Returns index of the interval with maximum lower bound.
	 */
	public int maxLowerBound() {
		double v = -Double.MAX_VALUE;
		int idx = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i].getLowerBound() > v) {
				idx = i;
				v = data[i].getLowerBound();
			}
		return idx;
	}
	
	/**
	 * Returns index of the interval with maximum upper bound.
	 */
	public int maxUpperBound() {
		double v = -Double.MAX_VALUE;
		int idx = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i].getUpperBound() > v) {
				idx = i;
				v = data[i].getUpperBound();
			}
		return idx;
	}
	
	/**
	 * Returns sum of intervals in the vector.
	 */
	public synchronized AuroraInterval sum() {
		AuroraInterval res = new AuroraInterval();
		for (int i = 0; i < data.length; i++)
			res.add(data[i]);
		return res;
	}
	
	/**
	 * Returns product of intervals in the vector.
	 */
	public synchronized AuroraInterval product() {
		AuroraInterval res = new AuroraInterval();
		res.copy(data[0]);
		for (int i = 1; i < data.length; i++)
			res.product(data[i]);
		return res;
	}
	
	/**
	 * Returns intersection of intervals in the vector.
	 */
	public synchronized AuroraInterval intersection() {
		AuroraInterval res = new AuroraInterval();
		res.copy(data[0]);
		boolean s = true;
		int i = 0;
		while ((s == true) && (i < data.length))
			s &= res.product(data[i++]);
		if (!s)
			return null;
		return res;
	}
	
	
	/**
	 * Sets all the vector elements to the given interval.
	 * @param v given interval.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean set(AuroraInterval v) {
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].copy(v);
		return res;
	}
	
	/**
	 * Sets the vector element at the given position to the given interval.
	 * @param v given interval.
	 * @param idx element index.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean set(AuroraInterval v, int idx) {
		if ((idx < 0) || (idx >= data.length))
			return false;
		return data[idx].copy(v);
	}
	
	/**
	 * Sets the interval vector data from vector.
	 * @param v given vector.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean set(Vector<AuroraInterval> v) {
		if ((v == null) || v.isEmpty())
			return false;
		int sz = v.size();
		boolean res = true;
		data = new AuroraInterval[sz];
		for (int i = 0; i < sz; i++) {
			data[i] = new AuroraInterval();
			res &= data[i].copy(v.get(i));
		}
		return res;
	}
	
	/**
	 * Sets the interval vector data from array.
	 * @param v given array.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean set(AuroraInterval[] v) {
		if ((v == null) || (v.length == 0))
			return false;
		int sz = v.length;
		boolean res = true;
		data = new AuroraInterval[sz];
		for (int i = 0; i < sz; i++) {
			data[i] = new AuroraInterval();
			res &= data[i].copy(v[i]);
		}
		return res;
	}
	
	/**
	 * Sets the interval vector from the given text string.<br>
	 * Size of the interval vector is predefined.
	 * @param buf text string in format: <code>v1:v2:...:vN</code>.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setIntervalVectorFromString(String buf) {
		if (buf == null)
			return false;
		StringTokenizer st = new StringTokenizer(buf, ":");
		if (st.countTokens() < 1)
			return false;
		boolean res = true;
		int sz = Math.min(data.length, st.countTokens());
		for (int i = 0; i < sz; i++)
			res &= data[i].setIntervalFromString(st.nextToken());
		for (int i = sz; i < data.length; i++)
			res &= data[i].setCenter(0.0);
		return res;
	}
	
	/**
	 * Sets the interval vector from the given text string.
	 * @param buf text string in format: <code>v1:v2:...:vN</code>.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setRawIntervalVectorFromString(String buf) {
		if (buf == null)
			return false;
		StringTokenizer st = new StringTokenizer(buf, ":");
		int sz = st.countTokens();
		if (sz < 1)
			return false;
		boolean res = true;
		data = new AuroraInterval[sz];
		for (int i = 0; i < sz; i++) {
			data[i] = new AuroraInterval();
			res &= data[i].setIntervalFromString(st.nextToken());
		}
		return res;
	}
	
	/**
	 * Assigns the lower bound of the intervals in the vector.
	 * @param lb lower bound.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setLowerBound(double lb) {
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].setLowerBound(lb);
		return res;
	}
	
	/**
	 * Assigns the lower bounds of the intervals in the vector.
	 * @param lb array of lower bounds.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setLowerBound(double[] lb) {
		if (lb == null)
			return false;
		boolean res = true;
		int sz = Math.min(data.length, lb.length);
		for (int i = 0; i < sz; i++)
			res &= data[i].setLowerBound(lb[i]);
		return res;
	}
	
	/**
	 * Assigns the lower bounds of the intervals in the vector.
	 * @param lb vector of lower bounds.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setLowerBound(Vector<Double> lb) {
		if (lb == null)
			return false;
		boolean res = true;
		int sz = Math.min(data.length, lb.size());
		for (int i = 0; i < sz; i++)
			res &= data[i].setLowerBound(lb.get(i));
		return res;
	}
	
	/**
	 * Assigns the upper bound of the intervals in the vector.
	 * @param ub upper bound.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setUpperBound(double ub) {
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].setUpperBound(ub);
		return res;
	}
	
	/**
	 * Assigns the upper bounds of the intervals in the vector.
	 * @param ub array of upper bounds.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setUpperBound(double[] ub) {
		if (ub == null)
			return false;
		boolean res = true;
		int sz = Math.min(data.length, ub.length);
		for (int i = 0; i < sz; i++)
			res &= data[i].setLowerBound(ub[i]);
		return res;
	}
	
	/**
	 * Assigns the upper bounds of the intervals in the vector.
	 * @param ub vector of upper bounds.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setUpperBound(Vector<Double> ub) {
		if (ub == null)
			return false;
		boolean res = true;
		int sz = Math.min(data.length, ub.size());
		for (int i = 0; i < sz; i++)
			res &= data[i].setLowerBound(ub.get(i));
		return res;
	}
	
	/**
	 * Makes sure the intervals in the vector are above the minimal allowed lower bound.
	 * @param lb minimal allowed lower bound.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean constraintLB(double lb) {
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].constraintLB(lb);
		return res;
	}
	
	/**
	 * Makes sure the intervals in the vector are above the minimal allowed lower bounds.
	 * @param lb array of minimal allowed lower bounds.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean constraintLB(double[] lb) {
		if (lb == null)
			return false;
		boolean res = true;
		int sz = Math.min(data.length, lb.length);
		for (int i = 0; i < sz; i++)
			res &= data[i].constraintLB(lb[i]);
		return res;
	}
	
	/**
	 * Makes sure the intervals in the vector are above the minimal allowed lower bounds.
	 * @param lb vector of minimal allowed lower bounds.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean constraintLB(Vector<Double> lb) {
		if (lb == null)
			return false;
		boolean res = true;
		int sz = Math.min(data.length, lb.size());
		for (int i = 0; i < sz; i++)
			res &= data[i].constraintLB(lb.get(i));
		return res;
	}
	
	/**
	 * Makes sure the intervals in the vector are below the maximal allowed upper bound.
	 * @param ub maximal allowed upper bound.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean constraintUB(double ub) {
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].constraintUB(ub);
		return res;
	}
	
	/**
	 * Makes sure the intervals in the vector are below the maximal allowed upper bounds.
	 * @param ub array of maximal allowed upper bounds.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean constraintUB(double[] ub) {
		if (ub == null)
			return false;
		boolean res = true;
		int sz = Math.min(data.length, ub.length);
		for (int i = 0; i < sz; i++)
			res &= data[i].constraintUB(ub[i]);
		return res;
	}
	
	/**
	 * Makes sure the intervals in the vector are below the maximal allowed upper bounds.
	 * @param ub vector of maximal allowed upper bounds.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean constraintUB(Vector<Double> ub) {
		if (ub == null)
			return false;
		boolean res = true;
		int sz = Math.min(data.length, ub.size());
		for (int i = 0; i < sz; i++)
			res &= data[i].constraintUB(ub.get(i));
		return res;
	}
	
	/**
	 * Constraints the bounds of the intervals in the vector to the given interval.
	 * @param v given interval.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean constraint(AuroraInterval v) {
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].constraint(v);
		return res;
	}
	
	/**
	 * Constraints the bounds of the intervals in the vector to intervals in the given vector.
	 * @param v given interval vector.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean constraint(AuroraIntervalVector v) {
		if (v == null)
			return false;
		boolean res = true;
		int sz = Math.min(v.size(), data.length);
		for (int i = 0; i < sz; i++)
			res &= data[i].constraint(v.get(i));
		return res;
	}
	
	/**
	 * Sets given interval vector data to the interval vector.
	 * @param v given interval vector.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean copy(AuroraIntervalVector v) {
		if ((v == null) || (v.size() == 0))
			return false;
		int sz = v.size();
		boolean res = true;
		data = new AuroraInterval[sz];
		for (int i = 0; i < sz; i++) {
			data[i] = new AuroraInterval();
			res &= data[i].copy(v.get(i));
		}
		return res;
	}
	
	/**
	 * Adds an interval to the vector.
	 * @param v the interval to be added.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean addInterval(AuroraInterval v) {
		int sz = data.length;
		AuroraInterval[] dt = new AuroraInterval[sz + 1];
		for (int i = 0; i < sz; i++)
			dt[i] = data[i];
		dt[sz] = new AuroraInterval();
		boolean res = dt[sz].copy(v);
		data = dt;
		return res;
	}
	
	/**
	 * Removes an element from the vector specified by its index.
	 * @param idx element index.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean removeInterval(int idx) {
		if ((idx < 0) || (idx >= data.length))
			return false;
		int sz = data.length - 1;
		AuroraInterval[] dt = new AuroraInterval[sz];
		for (int i = 0; i < idx; i++)
			dt[i] = data[i];
		for (int i = (idx + 1); i < sz; i++)
			dt[i] = data[i];
		data = dt;
		return true;
	}
	
	
	/**
	 * Negative of the intervals in the vector.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean negative() {
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].negative();
		return res;
	}
	
	/**
	 * Reciprocal of the intervals in the vector.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean reciprocal() {
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].reciprocal();
		return res;
	}
	
	/**
	 * Affine transformation of the intervals in the vector.
	 * @param scale scaling factor.
	 * @param shift affine term.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean affineTransform(double scale, double shift) {
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].affineTransform(scale, shift);
		return res;
	}
	
	/**
	 * Affine transformation of the intervals in the vector.
	 * @param scale vector of scaling factors.
	 * @param shift affine term.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean affineTransform(double[] scale, double shift) {
		if (scale == null)
			return false;
		boolean res = true;
		int sz = Math.min(data.length, scale.length);
		for (int i = 0; i < sz; i++)
			res &= data[i].affineTransform(scale[i], shift);
		return res;
	}
	
	/**
	 * Affine transformation of the intervals in the vector.
	 * @param scale scaling factor.
	 * @param shift vector of affine terms.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean affineTransform(double scale, double[] shift) {
		if (shift == null)
			return false;
		boolean res = true;
		int sz = Math.min(data.length, shift.length);
		for (int i = 0; i < sz; i++)
			res &= data[i].affineTransform(scale, shift[i]);
		return res;
	}
	
	/**
	 * Affine transformation of the intervals in the vector.
	 * @param scale vector of scaling factors.
	 * @param shift vector of affine terms.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean affineTransform(double[] scale, double[] shift) {
		boolean res = affineTransform(scale, 0);
		res &= affineTransform(1, shift);
		return res;
	}
	
	/**
	 * Inverse affine transformation of the intervals in the vector.
	 * @param scale scaling factor.
	 * @param shift affine term.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean inverseAffineTransform(double scale, double shift) {
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].inverseAffineTransform(scale, shift);
		return res;
	}
	
	/**
	 * Inverse affine transformation of the intervals in the vector.
	 * @param scale vector of scaling factors.
	 * @param shift affine term.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean inverseAffineTransform(double[] scale, double shift) {
		if (scale == null)
			return false;
		boolean res = true;
		int sz = Math.min(data.length, scale.length);
		for (int i = 0; i < sz; i++)
			res &= data[i].inverseAffineTransform(scale[i], shift);
		return res;
	}
	
	/**
	 * Inverse affine transformation of the intervals in the vector.
	 * @param scale scaling factor.
	 * @param shift vector of affine terms.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean inverseAffineTransform(double scale, double[] shift) {
		if ((shift == null) || (shift.length != data.length))
			return false;
		boolean res = true;
		int sz = Math.min(data.length, shift.length);
		for (int i = 0; i < sz; i++)
			res &= data[i].inverseAffineTransform(scale, shift[i]);
		return res;
	}
	
	/**
	 * Inverse affine transformation of the intervals in the vector.
	 * @param scale vector of scaling factors.
	 * @param shift vector of affine terms.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean inverseAffineTransform(double[] scale, double[] shift) {
		boolean res = inverseAffineTransform(1, shift);
		res &= inverseAffineTransform(scale, 0);
		return res;
	}
	
	/**
	 * Add given interval vector to the interval vector.
	 * @param v interval vector to be added.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean add(AuroraIntervalVector v) {
		if ((v == null) || (data.length != v.size()))
			return false;
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].add(v.get(i));
		return res;
	}
	
	/**
	 * Add given interval to the intervals in the vector.
	 * @param v interval to be added.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean add(AuroraInterval v) {
		if (v == null)
			return false;
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].add(v);
		return res;
	}
	
	/**
	 * Add intervals in the given vector to the intervals in the vector.
	 * @param v given interval vector.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean add(AuroraInterval[] v) {
		if ((v == null) || (v.length != data.length))
			return false;
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].add(v[i]);
		return res;
	}
	
	/**
	 * Subtract given interval vector from the interval vector.
	 * @param v interval vector to be subtracted.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean subtract(AuroraIntervalVector v) {
		if ((v == null) || (data.length != v.size()))
			return false;
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].subtract(v.get(i));
		return res;
	}
	
	/**
	 * Subtract given interval from the intervals in the vector.
	 * @param v interval to be subtracted.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean subtract(AuroraInterval v) {
		if (v == null)
			return false;
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].subtract(v);
		return res;
	}
	
	/**
	 * Subtract given interval from the intervals in the vector.
	 * @param x given interval vector.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean subtract(AuroraInterval[] v) {
		if ((v == null) || (v.length != data.length))
			return false;
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].subtract(v[i]);
		return res;
	}
	
	/**
	 * Compute pairwise products between intervals in the given interval vector and the interval vector.   
	 * @param v interval vector to be added.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean product(AuroraIntervalVector v) {
		if ((v == null) || (data.length != v.size()))
			return false;
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].product(v.get(i));
		return res;
	}
	
	/**
	 * Compute products of the given interval and the intervals in the vector.
	 * @param v given interval.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean product(AuroraInterval v) {
		if (v == null)
			return false;
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].product(v);
		return res;
	}
	
	/**
	 * Compute products of the intervals in the given vectors and the intervals in the vector.
	 * @param v given interval vector.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean product(AuroraInterval[] v) {
		if ((v == null) || (v.length != data.length))
			return false;
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].product(v[i]);
		return res;
	}
	
	/**
	 * Compute quotients of the intervals in the vector and the intervals from the given vector.
	 * @param v interval vector to be added.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean quotient(AuroraIntervalVector v) {
		if ((v == null) || (data.length != v.size()))
			return false;
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].quotient(v.get(i));
		return res;
	}
	
	/**
	 * Compute quotients of the given interval and the intervals in the vector.
	 * @param v given interval.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean quotient(AuroraInterval v) {
		if (v == null)
			return false;
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].quotient(v);
		return res;
	}
	
	/**
	 * Compute quotients of the intervals in the given vectors and the intervals in the vector.
	 * @param v given interval vector.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean quotient(AuroraInterval[] v) {
		if ((v == null) || (v.length != data.length))
			return false;
		boolean res = true;
		for (int i = 0; i < data.length; i++)
			res &= data[i].quotient(v[i]);
		return res;
	}
	
	/**
	 * Redistributes the vector according to the vector of fractions that sum up to one.
	 * @param r array of desired fractions of the total.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean redistribute(double[] r) {
		if ((r == null) || (r.length != data.length))
			return false;
		for (int i = 0; i < r.length; i++)
			if (r[i] < 0)
				return false;
		boolean res = true;
		double total = sum().getCenter();
		for (int i = 0; i < data.length; i++)
			res &= data[i].setCenter(total*r[i]);
		return res;
	}
	
	/**
	 * Randomizes each interval in the vector.
	 */
	public synchronized void randomize() {
		for (int i = 0; i < data.length; i++)
			data[i].randomize();
		return;
	}
	
	/**
	 * Returns text description of the weighted interval vector.
	 * @return string that describes the weighted interval vector.
	 */
	public String toStringWithWeights(double[] w, boolean frmt) {
		String buf = "";
		int sz = data.length - 1;
		double ww = 1.0;
		for (int i = 0; i < sz; i++) {
			ww = 1.0;
			if ((w != null) && (i < w.length))
				ww = w[i];
			if (data[i] != null)
				buf += data[i].toStringWithWeight(ww, frmt) + ":";
		}
		if ((w != null) && (sz < w.length))
			ww = w[sz];
		else
			ww = 1.0;
		buf += data[sz].toStringWithWeight(ww, frmt);
		return buf;
	}
	
	/**
	 * Returns text description of the inversely weighted interval vector.
	 * @return string that describes the inversely weighted interval vector.
	 */
	public String toStringWithInverseWeights(double[] w, boolean frmt) {
		String buf = "";
		int sz = data.length - 1;
		double ww = 1.0;
		for (int i = 0; i < sz; i++) {
			ww = 1.0;
			if ((w != null) && (i < w.length) && (w[i] != 0.0))
				ww = 1/w[i];
			if (data[i] != null)
				buf += data[i].toStringWithWeight(ww, frmt) + ":";
		}
		if ((w != null) && (sz < w.length) && (w[sz] != 0.0))
			ww = 1/w[sz];
		else
			ww = 1.0;
		buf += data[sz].toStringWithWeight(ww, frmt);
		return buf;
	}
	
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the interval vector.
	 */
	public String toString() {
		String buf = "";
		int sz = data.length - 1;
		for (int i = 0; i < sz; i++)
			if (data[i] != null)
				buf += data[i].toString() + ":";
		buf += data[sz].toString();
		return buf;
	}
	
	/**
	 * The same as toString, but with formatting.
	 * @return string that describes the interval vector.
	 */
	public String toString2() {
		String buf = "";
		int sz = data.length - 1;
		for (int i = 0; i < sz; i++)
			if (data[i] != null)
				buf += data[i].toString2() + ":";
		buf += data[sz].toString2();
		return buf;
	}
	
	/**
	 * The same as toString, but with formatting.
	 * @return string that describes the interval vector.
	 */
	public String toString3() {
		String buf = "";
		int sz = data.length - 1;
		for (int i = 0; i < sz; i++)
			if (data[i] != null)
				buf += data[i].toString3() + ":";
		buf += data[sz].toString3();
		return buf;
	}
	
}
