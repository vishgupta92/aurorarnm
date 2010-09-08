/**
 * @(#)LinkStreet.java
 */

package aurora.hwc;

import aurora.*;


/**
 * City Street.
 * <br>Allowed begin node (predecessor): NodeUJSignal, NodeUJStop, NodeHWCNetwork.
 * <br>Allowed end node (successor): NodeUJSignal, NodeUJStop, NodeHWCNetwork.
 * 
 * @see NodeFreeway, NodeHWCNetwork
 * 
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class LinkStreet extends AbstractLinkHWC {
	private static final long serialVersionUID = -2031395419474929817L;


	public LinkStreet() { }
	public LinkStreet(int id) { this.id = id; }
	public LinkStreet(int id, DynamicsHWC dyn) { this.id = id; myDynamics = dyn; }
	
	
	/**
	 * Returns type.
	 */
	public final int getType() {
		return TypesHWC.LINK_STREET;
	}
	
	/**
	 * Returns letter code of the Link type.
	 */
	public String getTypeLetterCode() {
		return "ST";
	}
	
	/**
	 * Returns type description.
	 */
	public final String getTypeString() {
		return "Street";
	}
	
	/**
	 * Validates Street Link configuration.<br>
	 * Checks if begin and end nodes are of correct type.
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
				(type != TypesHWC.NODE_SIGNAL) &&
				(type != TypesHWC.NODE_STOP) &&
				(type != TypesHWC.NODE_TERMINAL)) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "End Node of wrong type (" + cnm + ")."));
				res = false;
				//throw new ExceptionConfiguration(this, "End Node of wrong type (" + cnm + ").");
			}
		}
		return res;
	}

}
