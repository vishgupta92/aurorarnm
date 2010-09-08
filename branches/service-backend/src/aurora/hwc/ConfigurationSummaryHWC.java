/**
 * @(#)ConfigurationSummaryHWC.java 
 */

package aurora.hwc;

import aurora.*;

/**
 * Implementation of HWC configuration summary data structure.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class ConfigurationSummaryHWC extends AbstractConfigurationSummary {
	protected AbstractLinkHWC fLink = null;
	protected AbstractLinkHWC slLink = null;
	protected AbstractLinkHWC ttLink = null;
	
	
	/**
	 * Returns the Link with highest free flow speed.
	 */
	final public AbstractLinkHWC fastestLink() {
		return fLink;
	}
	
	/**
	 * Returns the Link with lowest free flow speed.
	 */
	final public AbstractLinkHWC slowestLink() {
		return slLink;
	}
	
	/**
	 * Returns the Link with minimal possible travel time.
	 */
	final public AbstractLinkHWC minTTLink() {
		return ttLink;
	}
	
	/**
	 * Update the fastest Link.
	 * @param lk the Link which current fastest Link is to be compared with. 
	 */
	public synchronized void updateFastestLink(AbstractLinkHWC lk) {
		if ((lk == null) || (lk.getBeginNode() == null) || (lk.getEndNode() == null))
			return;
		if ((fLink == null) || (lk.getV() > fLink.getV()))
			fLink = lk;
		return;
	}
	
	/**
	 * Update the slowest Link.
	 * @param lk the Link which current slowest Link is to be compared with. 
	 */
	public synchronized void updateSlowestLink(AbstractLinkHWC lk) {
		if ((lk == null) || (lk.getBeginNode() == null) || (lk.getEndNode() == null))
			return;
		if ((slLink == null) || (lk.getV() < slLink.getV()))
			slLink = lk;
		return;
	}
	
	/**
	 * Update the minimal travel time Link.
	 * @param lk the Link which current minimal travel time Link is to be compared with. 
	 */
	public synchronized void updateMinTTLink(AbstractLinkHWC lk) {
		if ((lk == null) || (lk.getBeginNode() == null) || (lk.getEndNode() == null))
			return;
		if ((ttLink == null) || ((lk.getLength()/lk.getV()) < (ttLink.getLength()/ttLink.getV())))
			ttLink = lk;
		
		return;
	}
	
}
