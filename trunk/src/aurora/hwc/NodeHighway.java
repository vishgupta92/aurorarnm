/**
 * @(#)NodeHighway.java
 */

package aurora.hwc;

import aurora.*;


/**
 * Highway Node.
 * <br>Allowed input links (predecessors): LinkHw, LinkOR, LinkDummy.
 * <br>Allowed output links (successors): LinkHw, LinkFR, LinkDummy.
 * 
 * @see LinkHw, LinkOR, LinkFR, LinkDummy
 *  
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class NodeHighway extends AbstractNodeHWC {
	private static final long serialVersionUID = -8167340117759326547L;


	public NodeHighway() { }
	public NodeHighway(int id) { this.id = id; }
	
	
	/**
	 * Returns type.
	 */
	public final int getType() {
		return TypesHWC.NODE_HIGHWAY;
	}
	
	/**
	 * Returns letter code of the Node type.
	 */
	public String getTypeLetterCode() {
		return "H";
	}
	
	/**
	 * Returns type description.
	 */
	public final String getTypeString() {
		return "Highway Node";
	}
	
	/**
	 * Returns compatible simple controller type names.
	 */
	public String[] getSimpleControllerTypes() {
		String[] ctrlTypes = {"ALINEA",
							  "Traffic Responsive",
							  "TOD",
							  "VSL TOD",
							  "Simple Signal"};
		return ctrlTypes;
	}
	
	/**
	 * Returns compatible simple controller classes.
	 */
	public String[] getSimpleControllerClasses() {
		String[] ctrlClasses = {"aurora.hwc.control.ControllerALINEA",
								"aurora.hwc.control.ControllerTR",
								"aurora.hwc.control.ControllerTOD",
								"aurora.hwc.control.ControllerVSLTOD",
								"aurora.hwc.control.ControllerSimpleSignal"};
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
			if ((type != TypesHWC.LINK_HIGHWAY) &&
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
			if ((type != TypesHWC.LINK_HIGHWAY) &&
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