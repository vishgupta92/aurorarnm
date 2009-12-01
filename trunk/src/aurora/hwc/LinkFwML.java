/**
 * @(#)LinkFwML.java
 */

package aurora.hwc;

import aurora.*;


/**
 * Freeway Mainline.
 * <br>Allowed begin node (predecessor): NodeFreeway, NodeHWCNetwork.
 * <br>Allowed end node (successor): NodeFreeway, NodeHWCNetwork.
 * 
 * @see NodeFreeway, NodeHWCNetwork
 * 
 * @author Alex Kurzhanskiy
 * @version $Id: LinkFwML.java,v 1.12.2.2.4.1 2009/09/11 23:33:54 akurzhan Exp $
 */
public final class LinkFwML extends AbstractLinkHWC {
	private static final long serialVersionUID = -4510519067401787436L;


	public LinkFwML() { }
	public LinkFwML(int id) { this.id = id; }
	public LinkFwML(int id, DynamicsHWC dyn) { this.id = id; myDynamics = dyn; }
	
	
	/**
	 * Returns type.
	 */
	public final int getType() {
		return TypesHWC.LINK_FREEWAY;
	}
	
	/**
	 * Returns type description.
	 */
	public final String getTypeString() {
		return "Freeway";
	}
	
	/**
	 * Validates Freeway Mainline Link configuration.<br>
	 * Checks if begin and end nodes are of correct type.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean validate() throws ExceptionConfiguration {
		boolean res = super.validate();
		String cnm;
		int type;
		if (predecessors.size() > 0) {
			type = predecessors.firstElement().getType();
			cnm = predecessors.firstElement().getClass().getName();
			if ((type != TypesHWC.NODE_FREEWAY) &&
				(type != TypesHWC.NODE_TERMINAL) &&
				(type != TypesHWC.NETWORK_HWC)) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "Begin Node of wrong type (" + cnm + ")."));
				res = false;
				//throw new ExceptionConfiguration(this, "Begin Node of wrong type (" + cnm + ").");
			}
		}
		if (successors.size() > 0) {
			type = successors.firstElement().getType();
			cnm = successors.firstElement().getClass().getName();
			if ((type != TypesHWC.NODE_FREEWAY) &&
				(type != TypesHWC.NODE_TERMINAL) &&
				(type != TypesHWC.NETWORK_HWC)) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "End Node of wrong type (" + cnm + ")."));
				res = false;
				//throw new ExceptionConfiguration(this, "End Node of wrong type (" + cnm + ").");
			}
		}
		return res;
	}

}