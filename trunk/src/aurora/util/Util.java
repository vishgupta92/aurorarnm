/**
 * @(#)Util.java
 */

package aurora.util;

import java.text.NumberFormat;
import java.util.Vector;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Second;


/**
 * Some useful utilities.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public class Util {
	public final static double EPSILON = Math.pow(10, -7);
	

	/**
	 * Converts double value of a time stamp to a string suitable for display.
	 * @param t time value in hours.
	 * @return time string.
	 */
	public static String time2string(double t) {
		NumberFormat form = NumberFormat.getInstance();
		form.setMinimumIntegerDigits(2);
		form.setMinimumFractionDigits(0);
		form.setMaximumIntegerDigits(2);
		form.setMaximumFractionDigits(2);
		double stime = t;
		int hours = (int)Math.floor(stime);
		stime = (stime - hours) * 60;
		int min = (int)Math.floor(stime);
		stime = (stime - min) * 60;
		//int sec = (int)Math.floor(stime);
		return form.format(hours) + "h" + form.format(min) + "m" + form.format(stime) + "s"; 
	}
	
	/**
	 * Converts double value of a time stamp to a Second object understood by JFreeChart.
	 * @param t time value in hours.
	 * @return time in JFreeChart Second object.
	 */
	public static Second time2second(double t) {
		double stime = t;
		int hh = (int)Math.floor(stime);
    	Hour h = new Hour(hh, new Day());
    	stime = (stime - hh) * 60;
    	int mm = (int)Math.floor(stime);
    	Minute m = new Minute(mm, h);
    	stime = (stime - mm) * 60;
    	int ss = (int)Math.floor(stime);
        return new Second(ss, m);
	}
	
	/**
	 * Returns full number of hours for given <code>double</code> time value.
	 * @param t time value in hours.
	 * @return <code>int</code> value of hours. 
	 */
	public static int getHours(double t) {
		return (int)Math.floor(t);
	}
	
	/**
	 * Returns full number of minutes for given <code>double</code> time value.
	 * @param t time value in hours.
	 * @return <code>int</code> value of minutes. 
	 */
	public static int getMinutes(double t) {
		double tt = (t - getHours(t)) * 60;
		return (int)Math.floor(tt);
	}

	/**
	 * Returns full number of seconds for given <code>double</code> time value.
	 * @param t time value in hours.
	 * @return <code>double</code> value of seconds. 
	 */
	public static double getSeconds(double t) {
		double tt = (t - getHours(t)) * 60;
		tt = (tt - getMinutes(t)) * 60;
		return Math.min(tt, 59.9);
	}
	
	/**
	 * Returns the row index of the minimal element in the given row of the given matrix.
	 * @param M matrix (2-dimensional array of type <code>double</code>).
	 * @param row row index.
	 * @return index of the element in the row.
	 */
	public static int minInRowIndex(double[][] M, int row) {
		int idx = -1;
		if (M == null)
			return idx;
		int m = M.length;
		int n = M[0].length;
		if ((row < 0) || (row >= m))
			return idx;
		idx = 0;
		for (int i = 1; i < n; i++)
			if (M[row][i] < M[row][idx])
				idx = i;
		return idx;
	}
	
	/**
	 * Returns the row index of the maximal element in the given row of the given matrix.
	 * @param M matrix (2-dimensional array of type <code>double</code>).
	 * @param row row index.
	 * @return index of the element in the row.
	 */
	public static int maxInRowIndex(double[][] M, int row) {
		int idx = -1;
		if (M == null)
			return idx;
		int m = M.length;
		int n = M[0].length;
		if ((row < 0) || (row >= m))
			return idx;
		idx = 0;
		for (int i = 1; i < n; i++)
			if (M[row][i] > M[row][idx])
				idx = i;
		return idx;
	}
	
	/**
	 * Returns the column index of the minimal element in the given column of the given matrix.
	 * @param M matrix (2-dimensional array of type <code>double</code>).
	 * @param col column index.
	 * @return index of the element in the column.
	 */
	public static int minInColumnIndex(double[][] M, int col) {
		int idx = -1;
		if (M == null)
			return idx;
		int m = M.length;
		int n = M[0].length;
		if ((col < 0) || (col >= n))
			return idx;
		idx = 0;
		for (int i = 1; i < m; i++)
			if (M[i][col] < M[idx][col])
				idx = i;
		return idx;
	}
	
	/**
	 * Returns the column index of the maximal element in the given column of the given matrix.
	 * @param M matrix (2-dimensional array of type <code>double</code>).
	 * @param col column index.
	 * @return index of the element in the column.
	 */
	public static int maxInColumnIndex(double[][] M, int col) {
		int idx = -1;
		if (M == null)
			return idx;
		int m = M.length;
		int n = M[0].length;
		if ((col < 0) || (col >= n))
			return idx;
		idx = 0;
		for (int i = 1; i < m; i++)
			if (M[i][col] > M[idx][col])
				idx = i;
		return idx;
	}
	
	/**
	 * Computes sum of nonnegative elements in the given row of the given matrix
	 * @param M matrix (2-dimensional array of type <code>double</code>).
	 * @param row row index.
	 * @return sum of elements.
	 */
	public static double rowSumPositive(double[][] M, int row) {
		double sum = 0.0;
		if ((M == null) || (row < 0) || (row >= M.length))
			return sum;
		for (int i = 0; i < M[0].length; i++)
			if (M[row][i] >= 0)
				sum += M[row][i];
		return sum;
	}
	
	/**
	 * Computes sum of  elements in the given row of the given matrix
	 * @param M matrix (2-dimensional array of type <code>double</code>).
	 * @param row row index.
	 * @return sum of elements.
	 */
	public static double rowSum(double[][] M, int row) {
		double sum = 0.0;
		if ((M == null) || (row < 0) || (row >= M.length))
			return sum;
		for (int i = 0; i < M[0].length; i++)
			sum += M[row][i];
		return sum;
	}
	
	/**
	 * Counts negative elements in the given row of the given matrix.
	 * @param M matrix (2-dimensional array of type <code>double</code>).
	 * @param row row index.
	 * @return number of negative elements.
	 */
	public static int countNegativeElements(double[][] M, int row) {
		int count = 0;
		if ((M == null) || (row < 0) || (row >= M.length))
			return count;
		for (int i = 0; i < M[0].length; i++)
			if (M[row][i] < 0)
				count++;
		return count;
	}
	
	/**
	 * Adjusts matrix elements so that sum of all positive elements in each row
	 * does not exceed 1.
	 * @param M matrix (2-dimensional array of type <code>double</code>).
	 */
	public static void normalizeMatrix(double[][] M) {
		if (M == null)
			return;
		int m = M.length;
		int n = M[0].length;
		for (int i = 0; i < m; i++) {
			boolean hasNegative = false;
			int countNegative = 0;
			int idxNegative = -1;
			double sum = 0.0;
			for (int j = 0; j < n; j++)
				if (M[i][j] < 0) {
					countNegative++;
					idxNegative = j;
					if (countNegative > 1)
						hasNegative = true;
				}
				else
					sum += M[i][j];
			if (countNegative == 1) {
				M[i][idxNegative] = Math.max(0, (1-sum));
				sum += M[i][idxNegative];
			}
			if ((!hasNegative) && (sum == 0.0)) {
				M[i][0] = 1;
				//for (int j = 0; j < n; j++)
					//M[i][j] = -1;
				continue;
			}
			if ((!hasNegative) && (sum < 1.0)) {
				for (int j = 0; j < n; j++)
					M[i][j] = (1/sum) * M[i][j];
				continue;
			}
			if (sum >= 1.0)
				for (int j = 0; j < n; j++)
					if (M[i][j] < 0)
						M[i][j] = 0;
					else
						M[i][j] = (1/sum) * M[i][j];
		}
		return;
	}
	
	/**
	 * Generates a string of space for a given XML indentation level.
	 * @param int n indentation level.
	 * @return <code>String</code> whitespace string. 
	 */
	public static String xmlindent(int n) {
		String a = "   ";
		String q = "";
		for(int i=0;i<n;i++)
			q = q.concat(a);
		return q;
	}

	/**
	 * Generates a comma separated representation of a vector.
	 * @param int n indentation level.
	 * @return <code>String</code> whitespace string. 
	 */
	public static String csvstringint(Vector<Integer> x) {
		String z = "";
		for (int i = 0; i < x.size(); i++)
			if (i == 0)
				z = z.concat(x.get(i).toString());
			else
				z = z.concat("," + x.get(i).toString());
		return z;
	}


	/**
	 * Generates a comma separated representation of a vector.
	 * @param int n indentation level.
	 * @return <code>String</code> whitespace string. 
	 */
	public static String csvstringbool(Vector<Boolean> x) {
		String z = "";
		String q = "";
		for (int i = 0; i < x.size(); i++){
			if(x.get(i))
				q = "1";
			else
				q = "0";
			if (i == 0)
				z = z.concat(q);
			else
				z = z.concat("," + q);
		}
		return z;
	}
	
	/**
	 * Generates a comma separated representation of a vector.
	 * @param int n indentation level.
	 * @return <code>String</code> whitespace string. 
	 */
	public static String csvstringflt(Vector<Float> x) {
		String z = "";
		for (int i = 0; i < x.size(); i++)
			if (i == 0)
				z = z.concat(x.get(i).toString());
			else
				z = z.concat("," + x.get(i).toString());
		return z;
	}

}
