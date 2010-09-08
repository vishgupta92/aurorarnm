/**
 * @(#)Point.java
 */

package aurora;

import java.io.*;


/**
 * Implementation of a point in R^3.
 * @author Alex Kurzhanskiy, Gabriel Gomes
 * @version $Id$
 */
public class Point implements Serializable {
	private static final long serialVersionUID = -3330701858833084352L;
	
	public double x;
	public double y;
	public double z;
	
	public Point() { x = 0; y = 0; z = 0; }
	public Point(double x) { this.x = x; y = 0; z = 0; }
	public Point(double x, double y) { this.x = x; this.y = y; z = 0; }
	public Point(double x, double y, double z) {
		this.x = x;
		//this.x = adjustMinSec(x);
		this.y = y;
		//this.y = adjustMinSec(y);
		this.z = z;
	}
	public Point(Point m) {this.x = m.x; this.y = m.y; this.z = m.z;  }
	
	@SuppressWarnings("unused")
	private double adjustMinSec(double x) {
		double a = 1.0;
		if (x < 0.0)
			a = -1.0;
		double v = Math.abs(x);
		double deg = Math.floor(v/10000.0);
		double min = Math.floor((v - deg * 10000.0)/100.0);
		double sec = Math.floor((v - deg * 10000.0) - min * 100.0);
		return a * (deg + (min/60.0) + (sec/3600.0));
	}
	
	public Point plus(Point m) {
	    return new Point(x+m.x,y+m.y,z+m.z);
	}
	
	public Point minus(Point m) {
	    return new Point(x-m.x,y-m.y,z-m.z);
	}
	
	public Point times(double s) {
	    return new Point(s*x,s*y,s*z);
	}

	public double twonorm() {
	    return Math.sqrt( x*x + y*y + z*z );
	}

	public double dist(Point m) {
	    return this.minus(m).twonorm();
	}

	/* 
	 * 	returns cross product this X input.
	 */
	public Point crossprod(Point m) {
		Point a = new Point();
		a.x = this.y*m.z - this.z*m.y;
		a.y = this.z*m.x - this.x*m.z;
		a.z = this.x*m.y - this.y*m.x;
		return a;
	}
}
