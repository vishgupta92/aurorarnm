/**
 * @(#)PositionNode.java
 */

package aurora;

import java.io.*;
import org.w3c.dom.*;


/**
 * Implementation of Node position.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public class PositionNode implements AuroraConfigurable, Serializable {
	private static final long serialVersionUID = -8636945405668106591L;
	
	protected Point p;
	
	
	public PositionNode() { p = new Point(); }
	public PositionNode(Point x) { 
		if (x == null)
			p = new Point();
		else
			p = x;
	}
	
	
	/**
	 * Initializes the Node position from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if (p == null)
			return !res;
		try  {
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (int i = 0; i < pp.getLength(); i++)
					if (pp.item(i).getNodeName().equals("point")) {
						double x = 0.0;
						double y = 0.0;
						double z = 0.0;
						NamedNodeMap nnm = pp.item(i).getAttributes();
						if (nnm.getNamedItem("x") != null)
							x = Double.parseDouble(nnm.getNamedItem("x").getNodeValue());
						if (nnm.getNamedItem("y") != null)
							y = Double.parseDouble(nnm.getNamedItem("y").getNodeValue());
						if (nnm.getNamedItem("z") != null)
							z = Double.parseDouble(nnm.getNamedItem("z").getNodeValue());
						this.p = new Point(x, y, z);
					}
			}
			else
				res = false;
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}
	
	/**
	 * Generates XML description of a Node position.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out prit stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		out.print("<position>");
		out.print("<point x=\"" + Double.toString(p.x) + "\" y=\"" + Double.toString(p.y) + "\" z=\"" + Double.toString(p.z) + "\"/>");
		out.print("</position>");
		return;
	}
	
	/**
	 * Bogus function that always returns <code>true</code>.
	 */
	public boolean validate() throws ExceptionConfiguration {
		return true;
	}
	
	/**
	 * Returns a point representing position.
	 */
	public Point get() {
		return p;
	}
	
	/**
	 * Sets a point as new position.
	 * @param x point.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean set(Point x) {
		if (x == null)
			return false;
		else
			p = x;
		return true;
	}
}