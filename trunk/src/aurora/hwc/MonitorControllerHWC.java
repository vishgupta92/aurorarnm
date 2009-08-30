/**
 * @(#)MonitorControllerHWC.java
 */

package aurora.hwc;

import java.util.*;
import aurora.*;


/**
 * Implementation of control monitor for road networks.
 * @author Gabriel Gomes
 * $Id: MonitorControllerHWC.java,v 1.1.2.4 2009/08/14 04:04:51 akurzhan Exp $
 */
public class MonitorControllerHWC extends AbstractMonitorController {
	private static final long serialVersionUID = -5116887026275839462L;
	
	
	private Vector<AbstractLink> links = new Vector<AbstractLink>();
	
	// This class will disappear. links will become predecessors, control ramps will become successors, in AbstractMonitorController.

	/**
	 * Returns Monitor type.
	 */
	public int getType() {
		return TypesHWC.MASK_MONITOR_CONTROLLER;
	}
	
	/**
	 * Returns permitted complex controller types.
	 */
	public String[] getComplexControllerTypes() {
		String[] cTypes = {"SWARM",
						   "HERO"};
		return cTypes;
	}
	
	/**
	 * Returns permitted complex controller classes.
	 */
	public String[] getComplexControllerClasses() {
		String[] cClasses = {"aurora.hwc.control.ControllerSWARM",
		   					 "aurora.hwc.control.ControllerHERO"};
		return cClasses;
	}
	
	public int getLinkIndex(int id){
		for(int i=0;i<links.size();i++){
			if(links.get(i).getId()==id)
				return i;
		}
		return -1;
	}
	
	public double getSpeedByIndex(int index){
		Double z = -1.0;
		if(index>=0 && index<links.size())
			z = ((AbstractLinkHWC)links.get(index)).getSpeed().getCenter();
		return z;
	}

	public double getDensityByIndex(int index){
		Double z = -1.0;
		if(index>=0 && index<links.size())
			z = ((AbstractLinkHWC)links.get(index)).getDensity().sum().getCenter();
		return z;
	}
	
	public double getFlowByIndex(int index){
		Double z = -1.0;
		if(index>=0 && index<links.size())
			z = ((AbstractLinkHWC)links.get(index)).getFlow().sum().getCenter();
		return z;
	}

	public double getSpeedByID(int id){
		Double z = -1.0;
		AbstractLink L = myNetwork.getLinkById(id);
		if(L!=null)
			z = ((AbstractLinkHWC) L).getSpeed().getCenter();
		return z;
	}
	
	public double getFlowByID(int id){
		Double z = -1.0;
		AbstractLink L = myNetwork.getLinkById(id);
		if(L!=null)
			z = ((AbstractLinkHWC) L).getFlow().sum().getCenter();
		return z;
	}
	
	public double getDensityByID(int id){
		Double z = -1.0;
		AbstractLink L = myNetwork.getLinkById(id);
		if(L!=null)
			z = ((AbstractLinkHWC) L).getDensity().sum().getCenter();
		return z;
	}
	
	public int getNumLinks(){
		return links.size();
	}
	
	public int getLinkID(int index){
		if(index<0 || index>=links.size())
			return -1;
		else
			return links.get(index).getId();
	}

	public String getLinkString(int index){
		if(index<0 || index>=links.size())
			return "";
		else
			return links.get(index).toString();
	}
	
	
	public int getNumNodes(){
		return 0;
	}
	
	public int getNodeID(int index){
		return -1;
	}
	
}
