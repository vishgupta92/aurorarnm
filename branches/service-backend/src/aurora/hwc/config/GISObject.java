/**
 * @(#)GISObject.java
 */

package aurora.hwc.config;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JOptionPane;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.filter.Filter;
import aurora.Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * The core of GIS Importer.
 * @author Jaimyoung Kwon
 * @version $Id$
 */
public class GISObject {
	private FeatureCollection featureCollection;
	private FeatureSource featureSource;
	private String targetGIS = "Unknown" ; // TANA or SANDAG 
//	affected: export to XML; edge simplify
//	not affected: type filter (user interaction); exrpot to GIS	

	private ArrayList<Feature> features = new ArrayList<Feature>();
	private GeometryFactory fact = new GeometryFactory();
	private WKTReader wktRdr = new WKTReader(fact);

	private int converted = 0;
	private static GISImporter jFrame;

	//	whether to use internal feature list or FeatureStore;
	//	case 1: raw GIS; road type filtering: useInternal = false
	//	case 2: simplifyEdge is performed: useInternal = true
	boolean useInternal = false;

	public GISObject(GISImporter importer) {
		jFrame = importer;
	}

	/**
	 * This method opens the shapefile in the url and return correct DataStore
	 * 
	 * - featureCollection is set up
	 * - features is cleared
	 * - targetGIS is set as "TANA" or "SANDAG"
	 * - useInternal is set false
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public void openShapefile(URL shape) throws IOException{
		Map<String, URL> params = new HashMap<String, URL>();
		params.put("url", shape );

		DataStore dataStore = DataStoreFinder.getDataStore( params );
		//myLog("Using datastore " + dataStore.toString());
		myLog("GIS File: " + shape);
		String typeName = dataStore.getTypeNames()[0]; // gives "hwycov03_ARC"
		featureSource = dataStore.getFeatureSource(typeName);
		featureCollection = featureSource.getFeatures();
		myLog("Read GIS file with "+ featureCollection.size() +" features.");

		targetGIS = "Unknown";
		FeatureType featureType = featureSource.getSchema();

		int natt = featureType.getAttributeCount();
		for (int i = 0; i < natt; i++) {
			AttributeType attributeType = featureType.getAttributeType(i);
			if (attributeType.getLocalName().startsWith("ACC")){
				targetGIS = "TANA";
				myLog("TANA GIS DATA");
				break;
			}
			if (attributeType.getLocalName().startsWith("CCSTYLE")){
				targetGIS = "SANDAG";
				myLog("SANDAG GIS DATA");
				break;
			}
		}
		if (targetGIS.equals("Unknown")){
			myLog("WARNING: The GIS data is neither TANA nor SANDAG data");
			myLog("WARNING: Edge simplification; XML export; and GIS export will not work properly");
		}

		useInternal = false; // reset this for the new GIS data 
		features.clear();

		return;
	}

	/**
	 * This method filter by roadway type
	 * 
	 * - featureCollection is re-set up to point to the subFeatureCollection
	 * 
	 * @param attributeName
	 * @param avs
	 */
	public void typeFilter(String attributeName, Object[] avs) {
		String query = attributeName + " == '" + avs[0].toString()+"'";
		for( int i = 1; i < avs.length; i++){
//			query += " or "+ attributeName + " == " + avs[i].toString();	
			query += " or "+ attributeName + " == '" + avs[i].toString()+ "'";
		}
		myLog("Using query string:\n" + query);

		Filter filter = null;
		try {
			filter = CQL.toFilter(query);
		} catch (CQLException e) {
			e.printStackTrace();
		}
		myLog("Original size: " + featureCollection.size());
		featureCollection = 
			featureCollection.subCollection(filter);
		myLog("Size after road type filter: " + featureCollection.size());

	}

	/**
	 * 
	 * @param newFile
	 */
	public void exportToXML(File newFile){

		String fpath = newFile.getAbsolutePath();
		PrintStream oos;
		try {
			oos = new PrintStream(new FileOutputStream(fpath));
			xmlDump(oos);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		myLog("Exported XML in "+newFile.toString() + ".");
	}

	HashMap<String, GISNode> nodes = null;
	HashMap<String,GISEdge> links = null;
	HashSet<String> nodeWhitelist = null;

	private final int convert(){
		GISImporterGetGraph ggg = new GISImporterGetGraph();
		int res= ggg.processGraph(targetGIS, useInternal, featureCollection, features);
		if (res != 0){
			myLog("Graph conversion failed!");
			if (res == 1) {
				myLog(" Incorrect GIS type: either SANDAG or TANA can be processed in this version");
			}
			return res;
		}
		nodes = ggg.getNodes();
		links = ggg.getEdges();
		nodeWhitelist = ggg.getWhiteList();
		converted = 1;
		return 0;
	}
	
	/**
	 * Generates XML description of the Aurora system configuration.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * Based on the following files:
	 *  aurora.hwc.ContainerHWC.java
	 *  aurora.hwc.AbstractLinkHWC.java, 
	 *  aurora.hwc.AbstractNodeHWC.java
	 *  
	 * @param out print stream.
	 * @return XML buffer.
	 * @throws IOException
	 */
	private String xmlDump(PrintStream out) throws IOException {
		if (converted == 0){ convert();}
		
		nodeWhitelist = GISImporterGetGraph.publicValidate(nodes);
		
		String buf = "";
		
		myLog("Number of nodes: " + nodes.size());
		myLog("Number of links: " + links.size());
		myLog("Number of nodes used (whitelisted): " + nodeWhitelist.size());

		String className = "aurora.hwc.NodeHWCNetwork";
		String name = "Network Imported via GIS Importer";
		String top = "true";
		String id = "00000";
		String controlled = "false";
		String tp = "0.00138888";
		String description = "Corridor imported from GIS";

		out.print( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.print( "<AuroraRNM>");
		out.print( "<network class=\"" + className 
				+ "\" id=\"" + id 
				+ "\" name=\""	+ name 
				+ "\" top=\"" + top 
				+ "\" controlled=\"" + controlled
				+ "\" tp=\"" + tp + "\">");
		out.print("<description>" + description + "</description>");

		positionXMLDump(out);


//		XMLDump Node List
		out.print("<NodeList>");
		for (Iterator it = nodeWhitelist.iterator(); it.hasNext();) {
			nodes.get(it.next()).xmlDump(out);
			out.print("\n");
		}
		out.print("</NodeList>");


//		XMLDump Link List
		out.print("<LinkList>");
		for (Iterator i = links.keySet().iterator(); i.hasNext();){
			//	 We dump all links, without considering nodeWhitelist. 
			// So, some links may be "orphans" without corresponding input and output nodes  
			links.get(i.next()).xmlDump(out);
			out.print("\n");
		}
		out.print("</LinkList>");

		out.print("</network>");
		out.print("</AuroraRNM>");
		return buf;
	}

	/**
	 * 
	 * - featureCollection is internalized into features
	 * - features contain simplified geometry (many links are removed) 
	 * - geometry in features are all turned into straight line (from curves); we may not want this.
	 * 
	 * TODO: This currently handle redundant ONE-WAY roads (TWO-way streets are not considered)
	 *     
	 */
	public void simplifyEdges() {
		if (featureCollection == null) {
			myLog("featureCollection is empty!");
			return;
		}

		if (features == null || features.size()==0) {
			internalize();
			if (features == null || features.size()==0) {
				myLog("Feature array is empty. Stopping...");
				return;
			}
		}

		if (converted == 0){ convert(); }
		
		myLog("# Number of Edges before simplification: " + links.size());
		myLog("# Number of Nodes before simplification: " + nodes.size());

		Object[] nodeIds = nodes.keySet().toArray();
		for(int i=0; i< nodeIds.length; i++){
			String nodeId = (String) nodeIds[i];
			GISNode gisNode = nodes.get(nodeId); 
			
			Integer f = gisNode.predecessors.size();
			Integer t = gisNode.successors.size();

			System.out.println("node: " + nodeId + ":" + f + ":" + t);
			
			if (f == 1 && t == 1){
				String inEdgeId = gisNode.predecessors.get(0);
				String outEdgeId = gisNode.successors.get(0);
				GISEdge inEdge = links.get(inEdgeId);
				GISEdge outEdge = links.get(outEdgeId);
				if (inEdge.equivalent(outEdge)){
					String upNodeId = inEdge.predecessors.get(0);
					String downNodeId = outEdge.successors.get(0);
					
					// prevent deleting cycles
					if (upNodeId.equals(downNodeId)) {continue;}

					System.out.println(">> removing: " + inEdgeId + " > "
							+ nodeId + " > " + outEdgeId);
					mergeEdges(inEdgeId, outEdgeId, nodeId);
					
					nodes.remove(nodeId);
				}
			}
			
			if (f == 2 && t == 2){
				String ac_1 = gisNode.predecessors.get(0);
				String ac_2 = gisNode.predecessors.get(1);
				String _ac1 = gisNode.successors.get(0);
				String _ac2 = gisNode.successors.get(1);

				if (links.get(ac_1).equivalent(links.get(ac_2)) &&
						links.get(ac_1).equivalent(links.get(_ac2)) &&
						links.get(_ac1).equivalent(links.get(ac_2)) &&
						links.get(_ac1).equivalent(links.get(_ac2))
						){

					System.out.println(">> removing: " 
							+ ac_1 + " > "
							+ ac_2 + " > "
							+ _ac1 + " > "
							+ _ac2 + " > "
							+ nodeId );

					String upNodeId1 = links.get(ac_1).predecessors.get(0);
					String upNodeId2 = links.get(ac_2).predecessors.get(0);
					String downNodeId1 = links.get(_ac1).successors.get(0);
					String downNodeId2 = links.get(_ac2).successors.get(0);

				if ( 
				     upNodeId1.equals(downNodeId1) && //# ac_1 _ac1
				     upNodeId2.equals(downNodeId2) //# ac_2 _ac2
				     ){
				    
//				    #  _ac2       ac_1
//				    # <---- node <-----
//				    # ---->      ----->
//				    #  ac_2       _ac1

					mergeEdges(ac_1, _ac2, nodeId);
					mergeEdges(ac_2, _ac1, nodeId);

					nodes.remove(nodeId);
				} else if (
					 upNodeId1.equals(downNodeId2) && //# ac_1 _ac2
					 upNodeId2.equals(downNodeId1)//# ac_2  _ac1
				     ){
					
//				    #  _ac1       ac_1
//				    # <---- node <-----
//				    # ---->      ----->
//				    #  ac_2       _ac2

					mergeEdges(ac_1, _ac1, nodeId);
					mergeEdges(ac_2, _ac2, nodeId);

					nodes.remove(nodeId);
				}
				else {
				}
					   
			    }
			}
		}
		myLog("# Number of Edges AFTER simplification: " + links.size());
		myLog("# Number of Nodes AFTER simplification: " + nodes.size());
		return;
		
	}

//	# 
//	# make the edge 1 (upstream edge) absorb edge 2 (downstream edge)
//	# 
	private void mergeEdges(String inEdgeId, String outEdgeId, String nodeId) {
		GISEdge inEdge = links.get(inEdgeId);
		GISEdge outEdge = links.get(outEdgeId);

	    //String upNodeId = inEdge.predecessors.get(0);
	    String downNodeId = outEdge.successors.get(0);
	    
	    //# Modify incoming edge to point to down node
	    inEdge.successors.set(0, downNodeId);
	    inEdge.length += outEdge.length;

	    //# Modify the downstream incoming edge ID
	    GISNode downNode = nodes.get(downNodeId);
	    downNode.predecessors.remove(
	    		downNode.predecessors.indexOf(outEdgeId)
	    		);
	    downNode.predecessors.add(inEdgeId);

		Vector<Coordinate> c1 = inEdge.coordinates; 
		Vector<Coordinate> c2 = outEdge.coordinates;

		c1.setSize(2);
		c1.get(1).x = c2.get(c2.size()-1).x;
		c1.get(1).y = c2.get(c2.size()-1).y;
		inEdge.coordinates = c1;

	    //# Delete the current node and the out edge
	    links.remove(outEdgeId);
	}

	/**
	 * 
	 * - featureCollection is internalized into features
	 * - features contain simplified geometry (many links are removed) 
	 * - geometry in features are all turned into straight line (from curves); we may not want this.
	 * 
	 * TODO: Currently doesn't handle non-SANDAG data
	 * TODO: This currently handle redundant ONE-WAY roads (TWO-way streets are not considered)
	 *     
	 */
	public void simplifyEdges_old() {
		if (featureCollection == null) {
			myLog("featureCollection is empty!");
			return;
		}

//		if (!targetGIS.equals("SANDAG") ) {
//			myLog("Only SANDAG GIS file can be processed");
//			return;
//		}

		if (features == null || features.size()==0) {
			internalize();
			if (features == null || features.size()==0) {
				myLog("Feature array is empty. Stopping...");
				return;
			}
		}

		for (int i = 0; i < features.size(); i++) {
			Feature feature = (Feature) features.get(i);
			try {
				feature.setAttribute("FNODE_",1);
			} catch (IllegalAttributeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		HashMap<String, Integer> fromCounter = new HashMap<String, Integer>(); 
		HashMap<String, Integer> toCounter = new HashMap<String, Integer>(); 
//		fromCounter<i,j>: node i appears j times as from node
//		toCounter<i,j>: node i appears j times as to node
		for( int i=0; i<features.size(); i++) {
			Feature feature = (Feature) features.get(i);
			String fromNode = myLongToString((Long) feature.getAttribute("FNODE_"));
			Integer fromOld = fromCounter.get(fromNode);
			fromCounter.put(fromNode, fromOld == null ? 1 : fromOld +1);

			String toNode = myLongToString((Long) feature.getAttribute("TNODE_"));
			Integer toOld = toCounter.get(toNode);
			toCounter.put(toNode, toOld == null ? 1 : toOld +1);
		}

		/**
		 * decides which nodes to remove:
		 * if nodes appear only once as a from and to node, remove it
		 */
		HashSet<String> nodesToRemove = new HashSet<String>();
		for(Iterator<String> i = fromCounter.keySet().iterator(); i.hasNext(); ){
			String key = i.next();
			Integer f = fromCounter.get(key);
			Integer t = toCounter.get(key);
			if (f != null && t != null && f == 1 && t == 1){
				nodesToRemove.add(key);
			}
		}

		/**
		 * actual removal of redundant edges
		 */


		ArrayList<String> fromNodes = new ArrayList<String>();
		ArrayList<String> toNodes = new ArrayList<String>();
		for (int i = 0; i < features.size(); i++) {
			Feature feature = (Feature) features.get(i);
			fromNodes.add(myLongToString((Long) feature.getAttribute("FNODE_")));
			toNodes.add(myLongToString((Long) feature.getAttribute("TNODE_")));
		}

		for (Iterator<String> i = nodesToRemove.iterator(); i.hasNext();) {
			String key = i.next();
			int a = toNodes.indexOf((String) key);
			int b = fromNodes.indexOf((String) key);
			// Origin -> a -> NODE -> b -> Dest  
			Feature f1 = features.get(a);
			Feature f2 = features.get(b);
			Coordinate[] c1 = f1.getDefaultGeometry().getCoordinates();
			Coordinate[] c2 = f2.getDefaultGeometry().getCoordinates();
			try {
				f1.setAttribute("TNODE_", f2.getAttribute("TNODE_"));
				toNodes.set(a, toNodes.get(b));
				f1.setAttribute("LENGTH", 
						(Double) f1.getAttribute("LENGTH")+
						(Double) f2.getAttribute("LENGTH"));
				String wktA = "MULTILINESTRING ((" + c1[0].x + " " + c1[0].y
				+ ", " + c2[c2.length-1].x + " " + c2[c2.length-1].y + "))";
				f1.setDefaultGeometry(wktRdr.read(wktA));
			} catch (IllegalAttributeException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			features.remove(b);
			toNodes.remove(b);
			fromNodes.remove(b);
		}

		myLog("# Number of 'from' nodes: " + fromCounter.size());
		myLog("# Number of 'to' nodes: " + toCounter.size());
		myLog("# Number of nodes removed: " + nodesToRemove.size());
		myLog("# Number of Edges (features) left after simplification: " + features.size());

	}

	public FeatureCollection getFeatureCollection() {
		return featureCollection;
	}

	public FeatureSource getFeatureSource() {
		return featureSource;
	}

	private String myLongToString(Long attribute) {
		// TODO Auto-generated method stub
		return Long.toString(attribute);
	}

	/**
	 * Push what's in the featureCollection to internal collection of features.
	 * This shouldn't be run when featureCollection is large.
	 */
	private void internalize(){
		myLog("Internalizing GIS data to internal feature vector...");
		features.clear();
		if (featureCollection.size() > 100000){
			int n = JOptionPane.showConfirmDialog(jFrame,
					"The GIS has "+ featureCollection.size() + 
					" features in it. Are you sure you want to convert it into internal structure without" +
					" filtering?",
					"Internalization of GIS features",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (n != JOptionPane.YES_OPTION){
				return;
			}
		}
		/*		TODO: here's what I want to do. there are many
		 * GIS-dependent junk like FNODE, NLANES, etc. 
		 * can we just push them to some auxiliary data structure
		 * so that we don't need to say FNODE, etc. ever again?
		 * HashMap gisAppendix<String, > = new HashMap <String, >();
		 * 		Collection gisAppenappendix feature.getID(); 
		 */ 

		Iterator iterator = featureCollection.iterator();
		for (int count = 0; iterator.hasNext(); count++) {
			Feature feature = (Feature) iterator.next();
			features.add(feature);
		}
		featureCollection.close( iterator );
		myLog("Internalization complete. Feature array has "+ features.size() + " features.");

		useInternal = true; // now, exporting has to use internal structure!		
	}

	/**
	 * Generates XML description of a Node position.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out prit stream.
	 * @return XML buffer.
	 * @throws IOException
	 */

	private String positionXMLDump(PrintStream out) throws IOException {
		Point p = new Point();
		p.x = p.y = p.z = 0.0;
		String buf = "<position>";
		buf += "<point x=\"" + Double.toString(p.x) + "\" y=\"" + 
		Double.toString(p.y) + "\" z=\"" + Double.toString(p.z) + "\"/>";
		buf += "</position>";
		if (out != null)
			out.print(buf);
		return buf;
	}

	/**
	 * debug output for a feature
	 */
	public void debug(){
		myLog("\n==Attributes of the GIS shapes==");
		FeatureType featureType = featureSource.getSchema();
		showAttributes(featureType, Boolean.TRUE); // show geomtry feature
		showAttributes(featureType, Boolean.FALSE); // show non-geomtry feature
		if (false){
			myLog("\n==First five records==");
			Iterator iterator = featureCollection.iterator();
			for (int count = 0; iterator.hasNext() & count < 5; count++) {
				Feature feature = (Feature) iterator.next();
				debug(feature);
			}
			featureCollection.close( iterator );
		}
		myLog("");
	}
	/**
	 * show names of Attributes in featureType 
	 * 
	 * @param featureType
	 * @param isGeometry
	 */
	private static void showAttributes(FeatureType featureType, Boolean isGeometry){
		for (int i = 0; i < featureType.getAttributeCount(); i++) {
			AttributeType attributeType = featureType.getAttributeType(i);
			if ( isGeometry ?
					(attributeType instanceof GeometryAttributeType)
					: !(attributeType instanceof GeometryAttributeType)) {
				myLog("* " + attributeType.getBinding().getName()+ "\t" 
						+ attributeType.getLocalName());
			}
		}
	}


	private void debug(Feature feature) {
		String out="";
		for (int i = 0; i < feature.getNumberOfAttributes(); i++) {
			Object attribute = feature.getAttribute(i);
			if (!(attribute instanceof Geometry)) {
				out += "\t" + attribute;
			}
		}
		myLog(out);
		myLog(feature.getID() + "\t" + feature.getDefaultGeometry());
		//		geometry in wkt format
	}

	public void exportToGIS(File newFile) {

		try {
			DataStore newDataStore = new ShapefileDataStore(newFile.toURI().toURL());
			newDataStore.createSchema(featureCollection.getSchema());
			FeatureWriter outFeatureWriter = newDataStore.getFeatureWriter(
					newDataStore.getTypeNames()[0], Transaction.AUTO_COMMIT);

			if (useInternal){
				for (int i = 0; i < features.size(); i++) {
					Feature feature = (Feature) features.get(i);
					Feature f = outFeatureWriter.next();
					Object[] att = null;
					att = feature.getAttributes(att);
					for (int n = 0; n < att.length; n++) {
						f.setAttribute(n, att[n]);
					}
					outFeatureWriter.write();
				}
				outFeatureWriter.close();
				myLog("Number of exported features :" + features.size());

			} else {
				Iterator iterator = featureCollection.iterator();
				for (;iterator.hasNext(); ) {
					Feature feature = (Feature) iterator.next();
					Feature f = outFeatureWriter.next();
					Object[] att = null;
					att = feature.getAttributes(att);
					for (int n = 0; n < att.length; n++) {
						f.setAttribute(n, att[n]);
					}
					outFeatureWriter.write();
				}
				featureCollection.close( iterator );
				outFeatureWriter.close();
				myLog("Number of exported features :" + featureCollection.size());

			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (IllegalAttributeException e) {
			e.printStackTrace();
		}
		myLog("Saved GIS in " + newFile.toString() + ".");		
	}

	private static  void myLog(String logmessage) {
		jFrame.getLogText().append(logmessage + "\n");
		jFrame.getLogText().setCaretPosition(jFrame.getLogText().getText().length());
	}
}
