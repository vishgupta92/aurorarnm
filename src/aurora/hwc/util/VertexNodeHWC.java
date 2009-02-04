/**
 * @(#)VertexNodeHWC.java
 */

package aurora.hwc.util;

import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;
import aurora.*;


/**
 * Implementation of Vertex for JUNG graph.
 * @author Alex Kurzhanskiy
 * @version $Id: VertexNodeHWC.java,v 1.1.4.1 2008/10/16 04:27:09 akurzhan Exp $
 */
public class VertexNodeHWC extends DirectedSparseVertex {
	AbstractNode node = null;
	Point pos;
	
	
	public VertexNodeHWC() { super(); }
	public VertexNodeHWC(AbstractNode n) {
		super();
		node = n;
		pos = node.getPosition().get();
	}
	public VertexNodeHWC(Point p) {
		super();
		pos = p;
	}
	
	
	/**
	 * Returns Node represented by this vertex.
	 */
	public AbstractNode getNodeHWC() {
		return node;
	}
	
	/**
	 * Returns vertex position.
	 */
	public Point getPosition() {
		return pos;
	}
	
	/**
	 * Sets vertex position.
	 * @param p new position.
	 */
	public void setPosition(Point p) {
		if (p != null)
			pos = p;
	}

}