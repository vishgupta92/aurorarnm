/**
 * @(#)LinkOR.java
 */

package aurora.hwc;

import aurora.*;


/**
 * On-ramp Link.
 * <br>Allowed begin node (predecessor): NodeHWCNetwork, all nodes with base AbstractNodeHWC.
 * <br>Allowed end node (successor): NodeFreeway, NodeHighway, NodeHWCNetwork.
 * 
 * @see AbstractNodeHWC, NodeFreeway, NodeHihway, NodeHWCNetwork
 * 
 * @author Alex Kurzhanskiy
 * @version $Id: LinkOR.java,v 1.12.2.2.4.1 2009/09/11 23:33:54 akurzhan Exp $
 */
public final class LinkOR extends AbstractLinkHWC {
	private static final long serialVersionUID = -8797694520720989699L;


	public LinkOR() { }
	public LinkOR(int id) { this.id = id; }
	public LinkOR(int id, DynamicsHWC dyn) { this.id = id; myDynamics = dyn; }
	
	
	/**
	 * Returns type.
	 */
	public final int getType() {
		return TypesHWC.LINK_ONRAMP;
	}
	
	/**
	 * Returns type description.
	 */
	public final String getTypeString() {
		return "On-Ramp";
	}
	
	/**
	 * Validates on-ramp Link configuration.<br>
	 * Checks begin and end node types.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean validate() throws ExceptionConfiguration {
		boolean res = super.validate();
		int type;
		String cnm;
		if (predecessors.size() > 0) {
			type = predecessors.firstElement().getType();
			cnm = predecessors.firstElement().getClass().getName();
			if ((type != TypesHWC.NETWORK_HWC) &&
				(type != TypesHWC.NODE_FREEWAY) &&
				(type != TypesHWC.NODE_HIGHWAY) &&
				(type != TypesHWC.NODE_SIGNAL) &&
				(type != TypesHWC.NODE_STOP) &&
				(type != TypesHWC.NODE_TERMINAL)) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "Begin Node of wrong type (" + cnm + ")."));
				res = false;
				//throw new ExceptionConfiguration(this, "Begin Node of wrong type (" + cnm + ").");
			}
		}
		if (successors.size() > 0) {
			type = successors.firstElement().getType();
			cnm = successors.firstElement().getClass().getName();
			if ((type != TypesHWC.NETWORK_HWC) &&
				(type != TypesHWC.NODE_FREEWAY) &&
				(type != TypesHWC.NODE_HIGHWAY) &&
				(type != TypesHWC.NODE_TERMINAL)) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "End Node of wrong type (" + cnm + ")."));
				res = false;
				//throw new ExceptionConfiguration(this, "End Node of wrong type (" + cnm + ").");
			}
		}
		return res;
	}

}