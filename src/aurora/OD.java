/**
 * @(#)OD.java
 */

package aurora;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;


/**
 * This class describes Origin-Destination pair
 * and maintains the list of possible paths
 * from the Origin to the Destination.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public abstract class OD implements AuroraConfigurable, Serializable {
	private static final long serialVersionUID = 127827305723133212L;
	
	protected AbstractNodeComplex myNetwork = null;
	protected AbstractNode origin = null;
	protected AbstractNode destination = null;
	protected Vector<Path> pathList = new Vector<Path>();


	public OD() { }
	public OD(AbstractNodeComplex ntwk) { myNetwork = ntwk; }
	
	
	/**
	 * Initializes an OD from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public abstract boolean initFromDOM(Node p) throws ExceptionConfiguration;

	/**
	 * Generates XML description of the OD.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		out.print("<od begin=\"" + origin.getId() + "\" end=\"" + destination.getId() + "\">\n");
		out.print("<PathList>\n");
		for (int i = 0; i < pathList.size(); i++)
			pathList.get(i).xmlDump(out);
		out.print("</PathList>\n</od>\n");
		return;
	}
	
	/**
	 * Validates the OD configuration.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean validate() throws ExceptionConfiguration {
		AbstractLink lk;
		boolean res = true;
		for (int i = 0; i < pathList.size(); i++) {
			lk = pathList.get(i).getBegin();
			if (lk.getBeginNode() != null)
				res = origin.equals(lk.getBeginNode());
			else
				res = origin.equals(lk.getEndNode());
			if (!res)
				throw new ExceptionConfiguration("Path (" + pathList.get(i).toString() + ") has wrong begin Link (" + pathList.get(i).getBegin().getId() + ").");
			lk = pathList.get(i).getEnd();
			if (lk.getEndNode() != null)
				res = destination.equals(lk.getEndNode());
			else
				res = destination.equals(lk.getBeginNode());
			if (!res)
				throw new ExceptionConfiguration("Path (" + pathList.get(i).toString() + ") has wrong end Link (" + pathList.get(i).getEnd().getId() + ").");
		}
		return res;
	}
	
	/**
	 * Returns Network to which the Origin and the Destination belong.
	 */
	public final AbstractNodeComplex getMyNetwork() {
		return myNetwork;
	}
	
	/**
	 * Returns Path list as vector.
	 */
	public final Vector<Path> getPathList() {
		return pathList;
	}
	
	/**
	 * Sets top level complex Node.
	 * @param ntwk complex Node object.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setMyNetwork(AbstractNodeComplex ntwk) {
		myNetwork = ntwk;
		return true;
	}
	
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the OD.
	 */
	public String toString() {
		return origin.getName() + " > " + destination.getName();
	}

}
