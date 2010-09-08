/**
 * @(#)DynamicsHWC.java
 */

package aurora.hwc;

/**
 * Interface for highway link dynamics description.
 * 
 * @see DynamicsCTM
 * 
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public interface DynamicsHWC {
	public Object computeFlow(AbstractLinkHWC x);
	public Object computeFlowL(AbstractLinkHWC x);
	public Object computeFlowU(AbstractLinkHWC x);
	public Object computeSpeed(AbstractLinkHWC x);
	public Object computeCapacity(AbstractLinkHWC x);
	public Object computeCapacityL(AbstractLinkHWC x);
	public Object computeCapacityU(AbstractLinkHWC x);
	public Object computeDensity(AbstractLinkHWC x);
	public String getTypeLetterCode();
}
