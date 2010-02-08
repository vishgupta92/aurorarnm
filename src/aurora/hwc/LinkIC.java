/**
 * @(#)LinkIC.java
 */

package aurora.hwc;

import aurora.*;


/**
 * Interconnect Link.
 * <br>Allowed begin node (predecessor): NodeFreeway, NodeHighway, NodeHWCNetwork.
 * <br>Allowed end node (successor): NodeFreeway, NodeHighway, NodeHWCNetwork.
 * 
 * @see NodeFreeway, NodeHihway, NodeHWCNetwork
 * 
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class LinkIC extends AbstractLinkHWC {
	private static final long serialVersionUID = -8451041344485824646L;


	public LinkIC() { }
	public LinkIC(int id) { this.id = id; }
	public LinkIC(int id, DynamicsHWC dyn) { this.id = id; myDynamics = dyn; }
	
	
	/**
	 * Returns type.
	 */
	public final int getType() {
		return TypesHWC.LINK_INTERCONNECT;
	}
	
	/**
	 * Returns type description.
	 */
	public final String getTypeString() {
		return "Interconnect";
	}
	
	/**
	 * Validates interconnect Link configuration.<br>
	 * Checks begin and end node types.
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
				(type != TypesHWC.NODE_HIGHWAY) &&
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
				(type != TypesHWC.NODE_HIGHWAY) &&
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