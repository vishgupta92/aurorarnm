/**
 * @(#)GISImporterGetGraph.java
 */

package aurora.hwc.config;

import java.util.*;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;


/**
 * Converter of shapes into graph.
 * @author Jaimyoung Kwon
 * @version $Id$
 */
public class GISImporterGetGraph {

	//private final static String  oppositePrefix = "opposite."; 
	private final static String  oppositePrefix = "-"; 
	private HashSet<String> nodeWhitelist = new HashSet<String>();
	private HashSet<String> nodeBlacklist= new HashSet<String>();
	private HashMap<String, GISNode> nodes = new HashMap<String, GISNode>();
	private HashMap<String, GISEdge> links = new HashMap<String, GISEdge>();

	/**
	 * - use only the first and last coordinate points. May want to keep all the curves?
	 *  
	 * @param featureCollection
	 */
	public void processGraphTANA(Iterator <Feature> iterator) {
		//This variable tracks the index of the current node (1, 2, 3,...)
		int maxNodeId = 0;
		//.. and this hash lets us look up the hard-to-read lat-lon node-id (e.g. "123.456789,23.432545,0") to
		// the integer node Id (tracked by maxNodeId)
		HashMap<String, String> nodeNameLookup = 
			new HashMap<String, String>();
		
		nodes.clear();
		links.clear();

		for (int count = 0; iterator.hasNext(); count++) {
			Feature feature = (Feature) iterator.next();

			// take care of a few known exceptions
			if (((String) feature.getAttribute("NAME_TYPE")).equals("G")) continue;
			if ((""+feature.getAttribute("F_ZLEV")).equals("-9") &&
					(""+feature.getAttribute("T_ZLEV")).equals("-9") ) continue;
			// value -9 in the ZLEV field is used to indicate that the name in the Name field is an alternate name.
			if ((""+feature.getAttribute("FT_COST")).equals("-1") &&
					(""+feature.getAttribute("TF_COST")).equals("-1"))	continue;
			
			Geometry geometry = feature.getDefaultGeometry();
			Coordinate[] coord = geometry.getCoordinates();
			int m = coord.length;

			// human readable node name.g. "WILLOW AVE" 
			String name = feature.getAttribute("NAME")+ " "	+ feature.getAttribute("TYPE");
			String edgeId = feature.getAttribute("DYNAMAP_ID").toString(); //feature.getID();   


			// This looks like "133.233234,123.232458,0". We are expecting them to be UNIQUE up to rounding.
			String fromNodeId_ = String.format("%.6f,%.6f,", 
					coord[0].x, coord[0].y)+ feature.getAttribute("F_ZLEV");
			String toNodeId_= String.format("%.6f,%.6f,", 
					coord[m-1].x, coord[m-1].y)+feature.getAttribute("T_ZLEV");
			// If they haven't appeared so far, we define integer node index.
			if (!nodeNameLookup.containsKey(fromNodeId_)){
				maxNodeId ++;
				nodeNameLookup.put(fromNodeId_, ""+maxNodeId);
			}
			if (!nodeNameLookup.containsKey(toNodeId_)){
				maxNodeId ++;
				nodeNameLookup.put(toNodeId_, ""+maxNodeId);
			}

			// Now we work with integer node names.
			String fromNodeId = nodeNameLookup.get(fromNodeId_);
			String toNodeId = nodeNameLookup.get(toNodeId_);

			// The  "The selected portion of the highway is a one-way in the segment's "to-from" direction 
			if (((String) feature.getAttribute("ONE_WAY")).equals("TF")){
				// swap the from and to nodes
				String tmp = fromNodeId;
				fromNodeId = toNodeId;
				toNodeId = tmp;
				// swap the from and to coordinates
				Coordinate[]  tmp2 = coord.clone();
				for(int i=0; i<m; i++)
					coord[i]= tmp2[m-i-1];
			}

			// Define the link/edge now.
			GISEdge link = new GISEdge();
			// We use only the first and last coordinates, turning all edges/links into a straight line.
			for(int j =0; j<coord.length; j++) link.coordinates.add(coord[j]);
			link.setId( edgeId);
			link.setLanes(0.0); 
			link.setLength(((Double) feature.getAttribute("SEG_LEN"))*5280.0);

			// If the "from Node" is not defined, define it.
			if (nodes.get(fromNodeId) == null){
				GISNode fromNode = new GISNode();
				fromNode.setId(fromNodeId);
				fromNode.setName(name); 
				fromNode.setDescription(name); 
				fromNode.Counter ++;
				fromNode.coordinates = coord[0];
				nodes.put(fromNodeId, fromNode);
			} 
			else {
				GISNode node = nodes.get(fromNodeId);
				node.setName(node.getName() + ":" + name);
				node.setDescription(node.getDescription() + ":" + name); 
			}
			// ... and add the current edge as the successor to the "from node" 
			nodes.get(fromNodeId).successors.add(edgeId);
			// ... and add the "from node" as the predecessor to the current edge
			link.predecessors.add(fromNodeId);

			// If the "to Node" is not defined, define it.
			if (nodes.get(toNodeId) == null){
				GISNode toNode = new GISNode();
				toNode.setId(toNodeId);
				toNode.setName(name); 
				toNode.setDescription(name); 
				toNode.Counter ++;
				toNode.coordinates = coord[m-1];
				nodes.put(toNodeId, toNode);
			}
			else {
				GISNode node = nodes.get(toNodeId);
				node.setName(node.getName() + ":" + name);
				node.setDescription(node.getDescription() + ":" + name); 
			}
			// ... and add the current edge as the predecessor to the "from node" 
			nodes.get(toNodeId).predecessors.add(edgeId);
			// ... and add the "to node" as the successor to the current edge
			link.successors.add(toNodeId);

			
			// takes care of the Link Type; is it highway/freeway or arterials, etc.?
			String acc = ""+feature.getAttribute("ACC");
			if (acc.equals("1") ||acc.equals("2") ||acc.equals("3")){ 
				// the link is a highway link; 
				link.setAuroraClass("aurora.hwc.LinkFwML");
				nodes.get(fromNodeId).setAuroraClass("aurora.hwc.NodeFreeway");
				nodes.get(fromNodeId).isFreeway = true;
				nodes.get(toNodeId).isFreeway = true;
				nodes.get(toNodeId).setAuroraClass("aurora.hwc.NodeFreeway");
			} else {
			}
			links.put(edgeId, link);

			// for two-way street, add one more link for the opposite direction
			if (!((String) feature.getAttribute("ONE_WAY")).equals("FT") &&
					!((String) feature.getAttribute("ONE_WAY")).equals("TF")){
				GISEdge oppositeLink = new GISEdge();
				edgeId = oppositePrefix + edgeId ; 
				for(int j =m -1; j>=0; j--) 
					oppositeLink.coordinates.add(coord[j]);

				oppositeLink.setId( edgeId);
				oppositeLink.setLength(((Double) feature.getAttribute("SEG_LEN"))*5280.0);
				nodes.get(fromNodeId).predecessors.add(edgeId);
				oppositeLink.successors.add(fromNodeId);
				nodes.get(toNodeId).successors.add(edgeId);
				oppositeLink.predecessors.add(toNodeId);
				links.put(edgeId, oppositeLink);
			}
		}


		assignLinkTypes();
		validate();
		return;
	}

	/**
	 * @param features
	 */
	public void processGraphSANDAG(Iterator <Feature> iterator) {
		nodes.clear();
		links.clear();
		for (int count = 0; iterator.hasNext(); count++) {
			Feature feature = (Feature) iterator.next();
			String edgeId = ""+feature.getAttribute("UserId"); // may not be of type LONG! feature.getID();  

			GISEdge link = new GISEdge();
			Coordinate[] coord = feature.getDefaultGeometry().getCoordinates();
			int m = coord.length;
			for(int j =0; j<m; j++) 
				link.coordinates.add(coord[j]);
			link.setId( edgeId.toString());
			link.setLanes(((Long)feature.getAttribute("ABLNO")).doubleValue()); 
			link.setLength((Double) feature.getAttribute("LENGTH"));

			String name = (String) feature.getAttribute("NM");
			//String fromName = (String) feature.getAttribute("FXNM");
			//String toName = (String) feature.getAttribute("TXNM");

//			Define fromNode
			String fromNodeId = ""+feature.getAttribute("FNODE_");
			if (nodes.get(fromNodeId) == null){
				GISNode fromNode = new GISNode();
				fromNode.coordinates = coord[0];
				fromNode.setId(fromNodeId.toString());
				fromNode.setName(name);
				fromNode.setDescription(name);
				nodes.put(fromNodeId, fromNode);
			}
			else {
				GISNode node = nodes.get(fromNodeId);
				node.setName(node.getName() + ":" + name);
				node.setDescription(node.getDescription() + ":" + name); 
			}
			nodes.get(fromNodeId).successors.add(edgeId);
			link.predecessors.add(fromNodeId);

//			Define toNode
			String toNodeId = ""+feature.getAttribute("TNODE_");
			if (nodes.get(toNodeId) == null){
				GISNode toNode = new GISNode();
				toNode.coordinates = coord[m-1];
				toNode.setId(toNodeId.toString());
				toNode.setName(name);
				toNode.setDescription(name );
				nodes.put(toNodeId, toNode);
			}
			else {
				GISNode node = nodes.get(toNodeId);
				node.setName(node.getName() + ":" + name);
				node.setDescription(node.getDescription() + ":" + name); 
			}
			nodes.get(toNodeId).predecessors.add(edgeId);
			link.successors.add(toNodeId);

			if ((Long) feature.getAttribute("CCSTYLE") == 1){
				link.setAuroraClass("aurora.hwc.LinkFwML");
				nodes.get(fromNodeId).setAuroraClass("aurora.hwc.NodeFreeway");
				nodes.get(fromNodeId).isFreeway = true;
				nodes.get(toNodeId).isFreeway = true;
				nodes.get(toNodeId).setAuroraClass("aurora.hwc.NodeFreeway");
			} else {
			}

			links.put(edgeId, link);

			// for two-way street, add one more link for the opposite direction
			if (((Long) feature.getAttribute("IWAY")) == 2){
				GISEdge oppositeLink = new GISEdge();
				edgeId = oppositePrefix + edgeId ; 
				for(int j =m -1; j>=0; j--) 
					oppositeLink.coordinates.add(coord[j]);
				oppositeLink.setId( edgeId);
				link.setLanes(((Long)feature.getAttribute("BALNO")).doubleValue()); 
				oppositeLink.setLength((Double) feature.getAttribute("LENGTH"));
				nodes.get(fromNodeId).predecessors.add(edgeId);
				oppositeLink.successors.add(fromNodeId);
				nodes.get(toNodeId).successors.add(edgeId);
				oppositeLink.predecessors.add(toNodeId);
				links.put(edgeId, oppositeLink);
			}
		}
		assignLinkTypes();
		validate();
	}


	private void assignLinkTypes() {
		/**
		 * Assign link and node types 
		 * Road types heuristics:
		 * for each edge do:
		 *  if CCSTYLE = 1; do
		 *   the link is "LinkFwML"; 
		 *   both from and end nodes are "NodeFreeway"
		 *  end if
		 * done
		 * for each edge do:
		 *  if any predecessor node is "NodeFreeway"; link is "LinkOR" 
		 *  else if any successor node is "NodeFreeway"; link is "LinkFR"
		 *  else link is
		 * done 
		 * All links that are left undefined are "LinkStreet"
		 * All nodes that are left undefined are "NodeUJSignal"  
		 * TODO: "LinkIC"; need to incorporate freeway # in the node name
		 * TODO: "NodeUJSignal" and "NodeUJStop"; sandag data has this info but in general we don't have this info
		 */
		/*		
		LinkFwML.java
		LinkFR.java
		LinkOR.java
		LinkIC.java
		LinkStreet.java
		NodeFreeway.java
		NodeUJSignal.java
		NodeUJStop.java

		LinkDummy.java
		LinkFwHOV.java
		LinkHw.java
		NodeHighway.java
		NodeHWCNetwork.java
		 */
		for(Iterator linkIterator = links.keySet().iterator(); linkIterator.hasNext(); ){
			GISEdge link = (GISEdge) links.get(linkIterator.next());
			boolean fromFreeway = false;
			boolean toFreeway = false;

			for(Iterator <String> it = link.predecessors.iterator(); it.hasNext(); ){
				if (nodes.get(it.next()).isFreeway) fromFreeway = true;
			}
			for(Iterator <String> it = link.successors.iterator(); it.hasNext(); ){
				if (nodes.get(it.next()).isFreeway) toFreeway = true;
			}
			if (fromFreeway & toFreeway) link.setAuroraClass("aurora.hwc.LinkFwML");
			if (fromFreeway & !toFreeway) link.setAuroraClass("aurora.hwc.LinkFR");
			if (!fromFreeway & toFreeway) link.setAuroraClass("aurora.hwc.LinkOR");
		}

	}

	/*
	 * Aurora requires nodes to have at least one input and at least one output.
	 * The links, on the other hand, may be 
	 * open-ended - sources (no begin node), sinks (no end node).
	 * 
	 * This routine add nodes that satisfy the condition to "nodeWhitelist" and 
	 * add those that don't to "nodeBlacklist"
	 *   
	 */
	private void validate() {
		for(Iterator iterator = nodes.keySet().iterator(); iterator.hasNext();){
			String key = (String) iterator.next();
			GISNode gisNode = nodes.get(key); 
			if (gisNode.predecessors.size() > 0 && gisNode.successors.size() > 0){
				nodeWhitelist.add(key);
			} else {
				nodeBlacklist.add(key);
			}
		}
	}

	public static final HashSet<String> publicValidate(HashMap<String, GISNode> nodes) {
		HashSet<String> nodeWhitelist = new HashSet<String>();
		for(Iterator iterator = nodes.keySet().iterator(); iterator.hasNext();){
			String key = (String) iterator.next();
			GISNode gisNode = nodes.get(key); 
			if (gisNode.predecessors.size() > 0 && gisNode.successors.size() > 0){
				nodeWhitelist.add(key);
			} else {
				//nodeBlacklist.add(key);
			}
		}
		return(nodeWhitelist);
	}

	public HashMap<String, GISNode> getNodes() {
		return nodes;
	}

	public HashMap<String, GISEdge> getEdges() {
		return links;
	}

	public HashSet<String> getWhiteList() {
		return nodeWhitelist ;
	}

	@SuppressWarnings("unchecked")
	public int processGraph(String targetGIS, boolean useInternal,
			FeatureCollection featureCollection, ArrayList<Feature> features) {
		Iterator <Feature> iterator;
		int return_value = 0;
		if (useInternal) {
			iterator = features.iterator();
		} 
		else {
			iterator = featureCollection.iterator();
		}


		if (targetGIS.equals("TANA")){
			processGraphTANA(iterator);
		} 
		else if (targetGIS.equals("SANDAG")){
			processGraphSANDAG(iterator);
		} 
		else {
			return_value = 1;
		}
		if (!useInternal) {
			featureCollection.close( iterator);
		}
		return return_value;
	}
}
/*
 * TODO: no speed information.
 * TODO: still very large redundance??
 * TODO: can simplifyedge work with TANA data?
 * TODO: what's in nodeName? how is it different from nodeDesc?
 * TODO: link description is more natural than node desc!
*/