/**
 * @(#)AbstractMonitorZipper.java
 */

package aurora;


import java.io.*;
import org.w3c.dom.*;


/**
 * Partial implementation of Zipper Monitor.<br>
 * This Monitor is needed to glue two subnetworks together.
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractMonitorZipper.java,v 1.1.2.5.2.2 2009/09/30 23:51:17 akurzhan Exp $
 */
public abstract class AbstractMonitorZipper extends AbstractMonitor {
	private static final long serialVersionUID = -6823494198602290832L;
	

	/**
	 * Initializes Zipper from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = super.initFromDOM(p);
		if (!res)
			return res;
		try {
			for (int i = 0; i < p.getChildNodes().getLength(); i++) {
				if (p.getChildNodes().item(i).getNodeName().equals("LinkPairs"))
					if (p.getChildNodes().item(i).hasChildNodes()) {
						NodeList pp = p.getChildNodes().item(i).getChildNodes();
						for (int j = 0; j < pp.getLength(); j++)
							if (pp.item(j).getNodeName().equals("pair")) {
								int olk = Integer.parseInt(pp.item(j).getAttributes().getNamedItem("outlink").getNodeValue());
								int ilk = Integer.parseInt(pp.item(j).getAttributes().getNamedItem("inlink").getNodeValue());
								res &= (addLinkPair(myNetwork.getLinkById(olk), myNetwork.getLinkById(ilk)) >= 0);
							}
					}
			}
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}

	/**
	 * Generates XML description of the Zipper.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		if (out == null)
			out = System.out;
		out.print("<LinkPairs>\n");
		int sz = Math.min(predecessors.size(), successors.size());
		for (int i = 0; i < sz; i++)
			out.print("<pair outlink=\"" + predecessors.get(i).getId() + "\" inlink=\"" + successors.get(i).getId() + "\" />\n");
		out.print("</LinkPairs></monitor>\n");
		return;
	}
	
	/**
	 * Validates the Zipper configuration.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean validate() throws ExceptionConfiguration {
		boolean res = true;
		if (predecessors.size() != successors.size()) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Number of Out-Links does not match number of In-Links."));
			res = false;
		}
		for (int i = 0; i < predecessors.size(); i++) {
			if ((predecessors.get(i).getType() & AbstractTypes.MASK_LINK) == 0) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "NE " + predecessors.get(i).getId() + " is not a Link."));
				res = false;
			}
			if ((successors.get(i).getType() & AbstractTypes.MASK_LINK) == 0) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "NE " + successors.get(i).getId() + " is not a Link."));
				res = false;
			}
			if (!predecessors.get(i).getSuccessors().isEmpty()) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "Link " + predecessors.get(i).getId() + " is not destination."));
				res = false;
			}
			if (!successors.get(i).getPredecessors().isEmpty()) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "Link " + successors.get(i).getId() + " is not source."));
				res = false;
			}
		}
		return res;
	}
	
	/**
	 * Adds a Link pair to the list.
	 * @param ol Out-Link.
	 * @param il In-Link.
	 * @return index of the added pair, <code>-1</code> - if the pair could not be added.
	 */
	public synchronized int addLinkPair(AbstractLink ol, AbstractLink il) {
		int idx = -1;
		if ((ol == null) || (il == null) || (!ol.getSuccessors().isEmpty()) || (!il.getPredecessors().isEmpty()))
			return idx;
		if (addPredecessor(ol) < 0)
			return idx;
		idx = predecessors.size() - 1;
		if (addSuccessor(il) < 0) {
			predecessors.remove(idx);
			idx = -1;
		}
		return idx;
	}
	
	/**
	 * Deletes specified Link pair from the list. 
	 * @param idx index of the pair to be deleted.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean deleteLinkPair(int idx) {
		boolean res = true;
		if ((idx < 0) || (idx >= predecessors.size()) || (idx >= successors.size()))
			return !res;
		predecessors.remove(idx);
		successors.remove(idx);
		return res;
	}
	
	/**
	 * Deletes specified NE from the list of predecessors. 
	 * @param x predecessor NE to be deleted.
	 * @return idx index of deleted predecessor, <code>-1</code> - if such NE was not found.
	 */
	public synchronized int deletePredecessor(AbstractNetworkElement x) {
		int idx = super.deletePredecessor(x);
		if (idx >= 0)
			successors.remove(idx);
		return idx;
	}
	
	/**
	 * Deletes specified NE from the list of successors. 
	 * @param x successor NE to be deleted.
	 * @return idx index of deleted successor, <code>-1</code> - if such NE was not found.
	 */
	public synchronized int deleteSuccessor(AbstractNetworkElement x) {
		int idx = super.deleteSuccessor(x);
		if (idx >= 0)
			predecessors.remove(idx);
		return idx;
	}
	
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Zipper Monitor.
	 */
	public String toString() {
		String buf = "Zipper (" + id + ")";
		return buf;
	}

}
