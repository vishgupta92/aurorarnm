/**
 * @(#)PositionLink.java
 */

package aurora;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;


/**
 * Implementation of link position.
 * @author Alex Kurzhanskiy, Ram Rajagopal
 * @version $Id: PositionLink.java,v 1.5.2.2 2008/10/16 04:27:07 akurzhan Exp $
 */
public class PositionLink implements AuroraConfigurable, Serializable {
	private static final long serialVersionUID = 5237536980592254921L;
	
	protected Vector<Point> pp;
	
	
	public PositionLink() {
		pp = new Vector<Point>();
		pp.add(new Point());
		pp.add(new Point());
	}
	public PositionLink(Vector<Point> x) {
		if (x == null) {
			pp = new Vector<Point>();
			pp.add(new Point());
			pp.add(new Point());
		}
		else {
			pp = x;
			while (pp.size() < 2)
				pp.add(new Point());
		}
	}
	
	
	/**
	 * Initializes the Link position from given DOM structure.
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
				this.pp = new Vector<Point>();
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
						this.pp.add(new Point(x, y, z));
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
	 * Generates XML description of a Link position.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out prit stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		out.print("<position>");
		for (int i = 0; i < pp.size(); i++)
			out.print("<point x=\"" + Double.toString(pp.get(i).x) + "\" y=\"" + Double.toString(pp.get(i).y) + "\" z=\"" + Double.toString(pp.get(i).z) + "\"/>");
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
	 * Returns link position as vector of points.
	 */
	public Vector<Point> get() {
		return pp;
	}
	
	/**
	 * Sets vector of points as new position.<br>
	 * Number of points should not be less than 2.
	 * @param x vector of points.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean set(Vector<Point> x) {
		if ((x == null) || (x.size() < 2))
			return false;
		else
			pp = x;
		return true;
	}
	
	/**
	 * Sets first point of the position vector.
	 * @param p Point object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setBegin(Point p) {
		if (p == null)
			return false;
		pp.remove(0);
		pp.add(0, p);
		return true;
	}
	
	/**
	 * Sets last point of the position vector.
	 * @param p Point object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setEnd(Point p) {
		if (p == null)
			return false;
		pp.remove(pp.size()-1);
		pp.add(p);
		return true;
	}

}