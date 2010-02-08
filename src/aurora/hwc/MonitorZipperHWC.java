/**
 * @(#)MonitorZipperHWC.java
 */

package aurora.hwc;

import aurora.*;


/**
 * Implements Zipper Monitor for road networks.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class MonitorZipperHWC extends AbstractMonitorZipper {
	private static final long serialVersionUID = -4854911548364714781L;

	/**
	 * Updates data on the subordinate Link pairs.
	 * @param ts new time step.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		boolean res = super.dataUpdate(ts);
		if (!res)
			return res;
		for (int i = 0; i < predecessors.size(); i++) {
			AbstractLinkHWC ol = (AbstractLinkHWC)predecessors.get(i);
			AbstractLinkHWC il = (AbstractLinkHWC)successors.get(i);
			if (counter == 1)
				if (enabled)
					il.setExternalDemandValue(ol.getFlow());
				else
					il.setExternalDemandValue(null);
			if (counter == 2)
				if (enabled)
					ol.setExternalCapacityValue(il.getActualFlow().sum());
				else
					ol.setExternalCapacityValue(null);
		}
		return res;
	}
	
	/**
	 * Returns the monitor type.
	 */
	public int getType() {
		return TypesHWC.MASK_MONITOR_ZIPPER;
	}
	
	/**
	 * Returns type description.
	 */
	public final String getTypeString() {
		return "Zipper Monitor";
	}

}
