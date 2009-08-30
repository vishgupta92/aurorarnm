/**
 * @(#)TypesHWC.java
 */

package aurora.hwc;

import aurora.AbstractTypes;


/**
 * Type constants used by Aurora HWC.
 * @author Alex Kurzhanskiy
 * @version $Id: TypesHWC.java,v 1.1.2.2.2.2.2.2 2009/08/10 19:53:30 akurzhan Exp $
 */
public final class TypesHWC extends AbstractTypes {
	// Complex Nodes
	public final static int NETWORK_HWC = MASK_NETWORK | 1;
	
	// Simple Nodes
	public final static int NODE_TERMINAL = MASK_NODE | 1;
	public final static int NODE_FREEWAY = MASK_NODE | 2;
	public final static int NODE_HIGHWAY = MASK_NODE | 4;
	public final static int NODE_SIGNAL = MASK_NODE | 8;
	public final static int NODE_STOP = MASK_NODE | 16;
	
	// Links
	public final static int LINK_DUMMY = MASK_LINK | 1;
	public final static int LINK_FREEWAY = MASK_LINK | 2;
	public final static int LINK_HOV = MASK_LINK | 4;
	public final static int LINK_HIGHWAY = MASK_LINK | 8;
	public final static int LINK_ONRAMP = MASK_LINK | 16;
	public final static int LINK_OFFRAMP = MASK_LINK | 32;
	public final static int LINK_INTERCONNECT = MASK_LINK | 64;
	public final static int LINK_STREET = MASK_LINK | 128;
	
	// Monitors
	public final static int MONITOR_HWC = MASK_MONITOR_CONTROLLER | 1;

	// Sensors
	public final static int SENSOR_LOOPDETECTOR = MASK_SENSOR | 1;
	
	public final static int[] nodeSimpleTypeArray() {
		int[] types = {NODE_FREEWAY, NODE_HIGHWAY, NODE_SIGNAL, NODE_STOP};
		return types;
	}
	
	public final static int[] nodeTypeArray() {
		int[] types = {NODE_FREEWAY, NODE_HIGHWAY, NODE_SIGNAL, NODE_STOP, NETWORK_HWC};
		return types;
	}
	
	public final static int[] linkTypeArray() {
		int[] types = {LINK_FREEWAY, LINK_HIGHWAY, LINK_HOV, LINK_ONRAMP, LINK_OFFRAMP, LINK_INTERCONNECT, LINK_STREET, LINK_DUMMY};
		return types;
	}
	
	public final static int[] monitorTypeArray() {
		int[] types = {MASK_MONITOR_CONTROLLER, MASK_MONITOR_EVENT, MASK_MONITOR_ZIPPER};
		return types;
	}
	
	public final static String typeString(int type) {
		switch(type) {
		case NETWORK_HWC: return "Network";
		case NODE_FREEWAY: return "Freeway Node";
		case NODE_HIGHWAY: return "Highway Node";
		case NODE_SIGNAL: return "Signal Junction";
		case NODE_STOP: return "Stop Junction";
		case LINK_DUMMY: return "Dummy Link";
		case LINK_FREEWAY: return "Freeway";
		case LINK_HOV: return "HOV";
		case LINK_HIGHWAY: return "Highway";
		case LINK_ONRAMP: return "On-Ramp";
		case LINK_OFFRAMP: return "Off-Ramp";
		case LINK_INTERCONNECT: return "Interconnect";
		case LINK_STREET: return "Street";
		case MASK_MONITOR_CONTROLLER: return "Control Monitor";
		case MASK_MONITOR_EVENT: return "Event Monitor";
		case MASK_MONITOR_ZIPPER: return "Zipper Monitor";
		}
		return "Unknown type";
	}
	
	public final static String typeClassName(int type) {
		switch(type) {
		case NETWORK_HWC: return "aurora.hwc.NodeHWCNetwork";
		case NODE_FREEWAY: return "aurora.hwc.NodeFreeway";
		case NODE_HIGHWAY: return "aurora.hwc.NodeHighway";
		case NODE_SIGNAL: return "aurora.hwc.NodeUJSignal";
		case NODE_STOP: return "aurora.hwc.NodeUJStop";
		case LINK_DUMMY: return "aurora.hwc.LinkDummy";
		case LINK_FREEWAY: return "aurora.hwc.LinkFwML";
		case LINK_HOV: return "aurora.hwc.LinkFwHOV";
		case LINK_HIGHWAY: return "aurora.hwc.LinkHw";
		case LINK_ONRAMP: return "aurora.hwc.LinkOR";
		case LINK_OFFRAMP: return "aurora.hwc.LinkFR";
		case LINK_INTERCONNECT: return "aurora.hwc.LinkIC";
		case LINK_STREET: return "aurora.hwc.LinkStreet";
		case MASK_MONITOR_CONTROLLER: return "aurora.hwc.MonitorControllerHWC";
		case MASK_MONITOR_EVENT: return "aurora.hwc.MonitorEventHWC";
		case MASK_MONITOR_ZIPPER: return "aurora.hwc.MonitorZipperHWC";
		}
		return "aurora.hwc.Unknown";
	}
	
}