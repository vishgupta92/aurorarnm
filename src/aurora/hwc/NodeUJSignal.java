/**
 * @(#)NodeUJSignal.java
 */

package aurora.hwc;

import aurora.*;


/**
 * Urban Junction with Signal.
 * <br>Allowed input links (predecessors): LinkStreet, LinkFR, LinkDummy.
 * <br>Allowed output links (successors): LinkStreet, LinkOR, LinkDummy.
 * 
 * @see LinkStreet, LinkOR, LinkFR, LinkDummy
 *  
 * @author Alex Kurzhanskiy
 * @version $Id: NodeUJSignal.java,v 1.9.2.2.2.1 2009/01/14 18:52:34 akurzhan Exp $
 */
public final class NodeUJSignal extends AbstractNodeHWC {
	private static final long serialVersionUID = 8638143194434976281L;


	public NodeUJSignal() { }
	public NodeUJSignal(int id) { this.id = id; }
	
	
	/**
	 * Returns type.
	 */
	public final int getType() {
		return TypesHWC.NODE_SIGNAL;
	}
	
	/**
	 * Returns compatible simple controller type names.
	 */
	public String[] getSimpleControllerTypes() {
		String[] ctrlTypes = {"Simple Signal"};
		return ctrlTypes;
	}
	
	/**
	 * Returns compatible simple controller classes.
	 */
	public String[] getSimpleControllerClasses() {
		String[] ctrlClasses = {"aurora.hwc.control.ControllerSimpleSignal"};
		return ctrlClasses;
	}
	
	/**
	 * Validates Node configuration.<br>
	 * Checks that in- and out-links are of correct types.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ConfigurationException
	 */
	public boolean validate() throws ExceptionConfiguration {
		int i;
		boolean res = super.validate();
		int type;
		String cnm;
		for (i = 0; i < predecessors.size(); i++) {
			type = predecessors.get(i).getType();
			cnm = predecessors.get(i).getClass().getName();
			if ((type != TypesHWC.LINK_STREET) &&
				(type != TypesHWC.LINK_OFFRAMP) &&
				(type != TypesHWC.LINK_HIGHWAY) &&
				(type != TypesHWC.LINK_DUMMY)) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "In-Link of wrong type (" + cnm + ")."));
				res = false;
				//throw new ExceptionConfiguration(this, "In-Link of wrong type (" + cnm + ").");
			}
		}
		for (i = 0; i < successors.size(); i++) {
			type = successors.get(i).getType();
			cnm = successors.get(i).getClass().getName();
			if ((type != TypesHWC.LINK_STREET) &&
				(type != TypesHWC.LINK_ONRAMP) &&
				(type != TypesHWC.LINK_HIGHWAY) &&
				(type != TypesHWC.LINK_DUMMY)) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "Out-Link of wrong type (" + cnm + ")."));
				res = false;
				//throw new ExceptionConfiguration(this, "Out-Link of wrong type (" + cnm + ").");
			}
		}
		return res;
	}

}