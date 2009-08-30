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
 * @version $Id: EdgeLinkHWC.java,v 1.1.4.1.4.1 2009/06/14 06:45:28 akurzhan Exp $
 */
public class EdgeLinkHWC extends DirectedSparseEdge {
	protected AbstractLinkHWC link;
	protected boolean isthick = false;
	
	
	public EdgeLinkHWC(Vertex v1, Vertex v2) {
		super(v1, v2);
	}
	public EdgeLinkHWC(Vertex v1, Vertex v2, AbstractLinkHWC l) {
		super(v1, v2);
		link = l;
	}
	public EdgeLinkHWC(Vertex v1, Vertex v2, boolean isth) {
		super(v1, v2);
		isthick = isth;
	}
	
	
	/**
	 * Returns the Link represented by this edge.
	 */
	public AbstractLinkHWC getLinkHWC() {
		return link;
	}
	
	/**
	 * Returns the value of isthick
	 */
	public boolean getIsThick() {
		return isthick;
	}

}
