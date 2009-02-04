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
 * @version $Id: DynamicsHWC.java,v 1.4 2007/01/28 03:57:17 akurzhan Exp $
 */
public interface DynamicsHWC {
	public Object computeFlow(AbstractLinkHWC x);
	public Object computeSpeed(AbstractLinkHWC x);
	public Object computeCapacity(AbstractLinkHWC x);
	public Object computeDensity(AbstractLinkHWC x);
}
