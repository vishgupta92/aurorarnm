/**
 * @(#)NodeFreeway.java
 */

package aurora.hwc;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aurora.*;


/**
 * Freeway Node.
 * <br>Allowed input links (predecessors): LinkFwML, LinkFwHOV, LinkOR, LinkDummy.
 * <br>Allowed output links (successors): LinkFwML, LinkHOV, LinkFR, LinkDummy.
 * 
 * @see LinkFwML, LinkFwHOV, LinkOR, LinkFR, LinkDummy
 *  
 * @author Alex Kurzhanskiy
 * @version $Id: NodeFreeway.java,v 1.11.2.3.2.1.2.4 2009/11/12 02:33:29 akurzhan Exp $
 */
public final class NodeFreeway extends AbstractNodeHWC {
	private static final long serialVersionUID = -3841872997290136430L;


	protected double postmile = 0;
	
	
	public NodeFreeway() { }
	public NodeFreeway(int id) { this.id = id; }
	
	
	/**
	 * Returns type.
	 */
	public final int getType() {
		return TypesHWC.NODE_FREEWAY;
	}
	
	/**
	 * Returns type description.
	 */
	public final String getTypeString() {
		return "Freeway Node";
	}
	
	/**
	 * Returns post mile.
	 */
	public double getPostmile(){
		return postmile;
	}
	
	/**
	 * Returns compatible simple controller type names.
	 */
	public String[] getSimpleControllerTypes() {
		String[] ctrlTypes = {"ALINEA",
							  "Traffic Responsive",
							  "TOD",
							  "VSL TOD"};
		return ctrlTypes;
	}
	
	/**
	 * Returns compatible simple controller classes.
	 */
	public String[] getSimpleControllerClasses() {
		String[] ctrlClasses = {"aurora.hwc.control.ControllerALINEA",
								"aurora.hwc.control.ControllerTR",
								"aurora.hwc.control.ControllerTOD",
								"aurora.hwc.control.ControllerVSLTOD"};
		return ctrlClasses;
	}
	
	/**
	 * Initializes the Freeway Node from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = super.initFromDOM(p);
		if (res) {
			try  {
				if (p.hasChildNodes()){
					NodeList pp = p.getChildNodes();
					for (int i = 0; i < pp.getLength(); i++)
						if (pp.item(i).getNodeName().equals("postmile")) 
							postmile = Double.parseDouble(pp.item(i).getTextContent());
				}
				else
					res = false;
			}
			catch(Exception e) {
				res = false;
				throw new ExceptionConfiguration(e.getMessage());
			}	
		}
		return res;
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