/**
 * @(#)MyXYSeries.java 
 */

package aurora.hwc.util;

import org.jfree.data.xy.*;


/**
 * This class is needed to allow setting data items.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public class MyXYSeries extends XYSeries {
	private static final long serialVersionUID = -1267563111409694460L;


	public MyXYSeries(Comparable arg) {
		super(arg);
	}
	
	
	/**
	 * Sets data item.
	 * @param idx list index.
	 * @param item data item.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	@SuppressWarnings("unchecked")
	public boolean setDataItem(int idx, XYDataItem item) {
		if ((idx < 0) || (idx >= data.size()) || (item == null))
			return false;
		data.set(idx, item);
		return true;
	}
	
}
