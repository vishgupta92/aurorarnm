/**
 * @(#)EdgeLinkHWC.java 
 */

package aurora.hwc.util;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import aurora.hwc.*;


/**
 * Implementation of edge for JUNG graph. 
 * @author Alex Kurzhanskiy
 * @version $Id: EdgeLinkHWC.java,v 1.1.4.1 2008/10/16 04:27:09 akurzhan Exp $
 */
public class EdgeLinkHWC extends DirectedSparseEdge {
	protected AbstractLinkHWC link;
	
	
	public EdgeLinkHWC(Vertex v1, Vertex v2) {
		super(v1, v2);
	}
	public EdgeLinkHWC(Vertex v1, Vertex v2, AbstractLinkHWC l) {
		super(v1, v2);
		link = l;
	}
	
	
	/**
	 * Returns the Link represented by this edge.
	 */
	public AbstractLinkHWC getLinkHWC() {
		return link;
	}

}
