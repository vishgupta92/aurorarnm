/**
 * @(#)Point.java
 */

package aurora;

import java.io.*;


/**
 * Implementation of a point in R^3.
 * @author Alex Kurzhanskiy
 * @version $Id: Point.java,v 1.2.2.3 2008/10/16 04:27:07 akurzhan Exp $
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
}
