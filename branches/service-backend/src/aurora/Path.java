/**
 * @(#)Path.java
 */

package aurora;

import java.io.*;
import java.util.*;
import org.w3c.dom.Node;


/**
 * Implementation of Path as sequence of Links.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public class Path implements AuroraConfigurable, Serializable {
	private static final long serialVersionUID = 368831207374115827L;
	
	protected OD myOD = null;
	protected String name = "Path";
	protected Vector<AbstractLink> linkSequence = new Vector<AbstractLink>();
	protected int linkCount;
	
	
	public Path() { }
	public Path(OD od) { myOD = od; }
	
	
	/**
	 * Initializes an object from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if (p == null)
			return res;
		name = p.getAttributes().getNamedItem("name").getNodeValue();
		StringTokenizer st = new StringTokenizer(p.getTextContent(), ", \t");
		while (st.hasMoreTokens()) {
			AbstractLink lk = myOD.getMyNetwork().getLinkById(Integer.parseInt(st.nextToken()));
			if (lk != null) {
				if ((myOD.getMyNetwork().getContainer().isSimulation()) && (myOD.getMyNetwork().getContainer().isBatch()))
					lk.setSave(true);
				linkSequence.add(lk);
			}
			else
				res = false;
		}
		linkCount = linkSequence.size();
		return res;
	}
	
	/**
	 * Generates XML description of an object.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		out.print("<path name=\"" + name + "\">");
		for (int i = 0; i < linkCount; i++)
			if (i == 0)
				out.print(linkSequence.get(i).getId());
			else
				out.print("," + linkSequence.get(i).getId());
		out.print("</path>");
		return;
	}
	
	/**
	 * Validates the object configuration.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean validate() throws ExceptionConfiguration {
		boolean res = true;
		if (linkSequence.isEmpty())
			return !res;
		for (int i = 0; i < linkCount; i++) {
			if ((i == 0) || (linkSequence.get(i-1).getEndNode() == null))
				continue;
			res &= (linkSequence.get(i).getBeginNode() != null); 
			if (res)
				res &= linkSequence.get(i).getBeginNode().equals(linkSequence.get(i-1).getEndNode());
			if (!res)
				throw new ExceptionConfiguration("Invalid Path (" + name + "): wrong Link sequence (...," + linkSequence.get(i-1).getId() + "," + linkSequence.get(i).getId() + ",...).");
		}
		return res;
	}
	
	/**
	 * Returns the name
	 */
	public final String getName() {
		return name;
	}
	
	/**
	 * Returns a sequence of Links that compose the Path as a vector.
	 */
	public final Vector<AbstractLink> getLinkVector() {
		return linkSequence;
	}
	
	/**
	 * Returns number of Links in the Path.
	 */
	public final int getLinkCount() {
		return linkCount;
	}
	
	/**
	 * Returns a seuence of Nodes that belong to the Path.
	 */
	public final Vector<AbstractNode> getNodeVector() {
		Vector<AbstractNode> nodes = new Vector<AbstractNode>();
		AbstractNode nd;
		for (int i = 0; i < linkCount; i++) {
			if (i == 0) {
				nd = linkSequence.get(0).getBeginNode();
				if (nd != null)
					nodes.add(nd);
			}
			nd = linkSequence.get(i).getEndNode();
			if (nd != null)
				nodes.add(nd);
		}
		return nodes;
	}
	
	/**
	 * Returns the link by index.
	 */
	public final AbstractLink getLink(int idx) {
		if ((!linkSequence.isEmpty()) && (idx >= 0) && (idx < linkSequence.size()))
			return linkSequence.get(idx);
		return null;
	}
	
	/**
	 * Returns first Link in the Path.
	 */
	public final AbstractLink getBegin() {
		if (!linkSequence.isEmpty())
			return linkSequence.firstElement();
		return null;
	}
	
	/**
	 * Returns last Link in the Path.
	 */
	public final AbstractLink getEnd() {
		if (!linkSequence.isEmpty())
			return linkSequence.get(linkCount - 1);
		return null;
	}
	
	/**
	 * Returns Path length.
	 */
	public final double getLength() {
		double l = 0.0;
		for (int i = 0; i < linkCount; i++)
			l += linkSequence.get(i).getLength();
		return l;
	}
	
	/**
	 * Returns maximum Link length in the Path.
	 */
	public final double getMaxLinkLength() {
		double l = 0.0;
		for (int i = 0; i < linkCount; i++)
			l = Math.max(l, linkSequence.get(i).getLength());
		return l;
	}
	
	/**
	 * Returns minimum Link length in the Path.
	 */
	public final double getMinLinkLength() {
		double l = 0.0;
		for (int i = 0; i < linkCount; i++)
			l = Math.min(l, linkSequence.get(i).getLength());
		return l;
	}
	
	/**
	 * Checks if the Path contains source Link.
	 */
	public final boolean hasSource() {
		if (!linkSequence.isEmpty())
			return linkSequence.firstElement().getPredecessors().isEmpty();
		return false;
		
	}
	
	/**
	 * Checks if the Path contains destination Link.
	 */
	public final boolean hasDestination() {
		if (!linkSequence.isEmpty())
			return linkSequence.get(linkCount - 1).getSuccessors().isEmpty();
		return false;
		
	}
	
	/**
	 * Checks if given Link belongs to the Path.
	 * @param lk Link.
	 * @return <code>true</code> if the Link is part of the Path, <code>false</code> - otherwise.
	 */
	public final boolean doesBelong(AbstractLink lk) {
		return (linkSequence.indexOf(lk) >= 0);
	}
	
	/**
	 * Checks if given Node belongs to the Path.<br>
	 * We say that given Node belongs to the Path
	 * if one of its incoming and one of its outgoing Links belong to this Path.
	 * @param nd Node.
	 * @return <code>true</code> if the Node is part of the Path, <code>false</code> - otherwise.
	 */
	public final boolean doesBelong(AbstractNode nd) {
		int i;
		boolean pr = false;
		Vector<AbstractNetworkElement> pred = nd.getPredecessors();
		for (i = 0; i < pred.size(); i++)
			if (linkSequence.indexOf(pred.get(i)) >= 0) {
				pr = true;
				break;
			}
		boolean sr = false;
		Vector<AbstractNetworkElement> succ = nd.getSuccessors();
		for (i = 0; i < succ.size(); i++)
			if (linkSequence.indexOf(succ.get(i)) >= 0) {
				sr = true;
				break;
			}
		return (pr | sr);
	}
	
	/**
	 * Sets OD for the Path.
	 * @param od OD object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setMyOD(OD od) {
		myOD = od;
		return true;
	}

	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Path.
	 */
	public String toString() {
		return name;
	}
	
}
