/**
 * @(#)NodeFreeway.java
 */

package aurora.hwc;

import aurora.*;


/**
 * Freeway Node.
 * <br>Allowed input links (predecessors): LinkFwML, LinkFwHOV, LinkOR, LinkDummy.
 * <br>Allowed output links (successors): LinkFwML, LinkHOV, LinkFR, LinkDummy.
 * 
 * @see LinkFwML, LinkFwHOV, LinkOR, LinkFR, LinkDummy
 *  
 * @author Alex Kurzhanskiy
 * @version $Id: NodeFreeway.java,v 1.11.2.3.2.1 2009/01/14 18:52:34 akurzhan Exp $
 */
public final class NodeFreeway extends AbstractNodeHWC {
	private static final long serialVersionUID = -3841872997290136430L;


	public NodeFreeway() { }
	public NodeFreeway(int id) { this.id = id; }
	
	
	/**
	 * Returns type.
	 */
	public final int getType() {
		return TypesHWC.NODE_FREEWAY;
	}
	
	/**
	 * Returns compatible simple controller type names.
	 */
	public String[] getSimpleControllerTypes() {
		String[] ctrlTypes = {"ALINEA",
							  "TOD"};
		return ctrlTypes;
	}
	
	/**
	 * Returns compatible simple controller classes.
	 */
	public String[] getSimpleControllerClasses() {
		String[] ctrlClasses = {"aurora.hwc.control.ControllerALINEA",
								"aurora.hwc.control.ControllerTOD"};
		return ctrlClasses;
	}
	
	/**
	 * Validates Node configuration.<br>
	 * Checks that in- and out-links are of correct types.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean validate() throws ExceptionConfiguration {
		int i;
		boolean res = super.validate();
		int type;
		String cnm;
		for (i = 0; i < predecessors.size(); i++) {
			type = predecessors.get(i).getType();
			cnm = predecessors.get(i).getClass().getName();
			if ((type != TypesHWC.LINK_FREEWAY) &&
				(type != TypesHWC.LINK_HOV) &&
				(type != TypesHWC.LINK_INTERCONNECT) &&
				(type != TypesHWC.LINK_ONRAMP) &&
				(type != TypesHWC.LINK_DUMMY)) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "In-Link of wrong type (" + cnm + ")."));
				res = false;
				//throw new ExceptionConfiguration(this, "In-Link of wrong type (" + cnm + ").");
			}
		}
		for (i = 0; i < successors.size(); i++) {
			type = successors.get(i).getType();
			cnm = successors.get(i).getClass().getName();
			if ((type != TypesHWC.LINK_FREEWAY) &&
				(type != TypesHWC.LINK_HOV) &&
				(type != TypesHWC.LINK_INTERCONNECT) &&
				(type != TypesHWC.LINK_OFFRAMP) &&
				(type != TypesHWC.LINK_DUMMY)) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "Out-Link of wrong type (" + cnm + ")."));
				res = false;
				//throw new ExceptionConfiguration(this, "Out-Link of wrong type (" + cnm + ").");
			}
		}
		return res;
	}

}