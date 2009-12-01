/**
 * @(#)MonitorControllerHWC.java
 */

package aurora.hwc;

import java.util.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aurora.*;


/**
 * Implementation of control monitor for road networks.
 * @author Gabriel Gomes
 * $Id: MonitorControllerHWC.java,v 1.1.2.10 2009/10/08 04:55:04 gomes Exp $
 */
public class MonitorControllerHWC extends AbstractMonitorController {
	private static final long serialVersionUID = -5116887026275839462L;

	private Vector<SensorLoopDetector> sensors = new Vector<SensorLoopDetector>();
	private Vector<Integer> sensorIds = new Vector<Integer>();
	

	/**
	 * Initializes the Control Monitor from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = super.initFromDOM(p);
		if (!res)
			return res;
		try {
			NodeList pp = p.getChildNodes();
			for (int i = 0; i < pp.getLength(); i++) {
				if (pp.item(i).getNodeName().equals("sensors")) {
					StringTokenizer st = new StringTokenizer(pp.item(i).getTextContent(), ", \t");
					while (st.hasMoreTokens()) {
						res &= sensorIds.add( Integer.parseInt(st.nextToken()) );
					}					
				}
			}
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}

	/**
	 * Validates the Control Monitor configuration.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean validate() throws ExceptionConfiguration {
		for(int i=0;i<sensorIds.size();i++){
			AbstractSensor s = myNetwork.getSensorById(sensorIds.get(i));
			if(s==null)
				return false;
			sensors.add((SensorLoopDetector) s);
		}
		sensorIds.clear();
		boolean res = super.validate();
		return res;
	}

		
	// SENSORS ----------------------------------------------
	public SensorLoopDetector getSensor(int index){
		return sensors.get(index);
	}

	public int getSensorIndexByLinkId(int linkid){
		for (int i = 0; i < sensorIds.size(); i++) {
			if (sensors.get(i).getLink().getId() == linkid)
				return i;
		}
		return -1;
	}
	
	public int getSensorIndexBySensorId(int sensorid){
		for (int i = 0; i < sensorIds.size(); i++) {
			if (sensors.get(i).getId() == sensorid)
				return i;
		}
		return -1;
	}

	//-------------------- FIXME BEGIN: This code must be removed from the Monitor Object --------------------------
	
	public double SensorFlow(int index) { 
		return sensors.get(index).Flow(); 
	}
	
	public double SensorDensity(int index)	{ 
		return sensors.get(index).Density(); 
	}

	public double SensorLength(int index) { 
		return sensors.get(index).Length(); 
	}

	public double SensorSpeed(int index) { 
		return sensors.get(index).Speed();  
	}
	
	public double SensorOccupancy(int index) { 
		return sensors.get(index).Occupancy(); 
	}
	
	//-------------------- FIXME END --------------------------
	

	/**
	 * Returns monitored Link identified by its index.
	 * @param index index of the monitored Network Element.
	 * @return monitored link if the index corresponds to a link, <code>null</code> otherwise.
	 */
	public AbstractLinkHWC getMonitoredLink(int index){
		if (index < 0)
			return null;
		AbstractNetworkElement ne = predecessors.get(index);
		if ((ne != null) && ((ne.getType() & TypesHWC.MASK_LINK) > 0))
			return (AbstractLinkHWC)ne;
		return null;
	}
	
	/**
	 * Returns index of the monitored Link.
	 * @param id Link ID.
	 * @return Link index in the monitored list, or <code>-1</code> if it is not there.
	 */
	public int getMonitoredLinkIndexById(int id){
		for (int i = 0; i < predecessors.size(); i++) {
			if ((predecessors.get(i).getId() == id) && ((predecessors.get(i).getType() & TypesHWC.MASK_LINK) > 0))
				return i;
		}
		return -1;
	}

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
	/**
	 * Returns type description.
	 */
	public final String getTypeString() {
		return "Control Monitor";
	}
	
}
