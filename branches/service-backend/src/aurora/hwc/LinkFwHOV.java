/**
 * @(#)LinkFwHOV.java
 */

package aurora.hwc;

import aurora.*;


/**
 * Freeway HOV.
 * <br>Allowed begin node (predecessor): NodeFreeway, NodeHWCNetwork.
 * <br>Allowed end node (successor): NodeFreeway, NodeHWCNetwork.
 * 
 * @see NodeFreeway, NodeHWCNetwork
 * 
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class LinkFwHOV extends AbstractLinkHWC {
	private static final long serialVersionUID = 808446413439649397L;


	public LinkFwHOV() { }
	public LinkFwHOV(int id) { this.id = id; }
	public LinkFwHOV(int id, DynamicsHWC dyn) { this.id = id; myDynamics = dyn; }
	
	
	/**
	 * Returns type.
	 */
	public final int getType() {
		return TypesHWC.LINK_HOV;
	}
	
	/**
	 * Returns letter code of the Link type.
	 */
	public String getTypeLetterCode() {
		return "HOV";
	}
	
	/**
	 * Returns type description.
	 */
	public final String getTypeString() {
		return "HOV";
	}
	
	/**
	 * Validates Freeway HOV Link configuration.<br>
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