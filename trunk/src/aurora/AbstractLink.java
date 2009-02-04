/**
 * @(#)AbstractLink.java
 */

package aurora;

import java.util.Vector;


/**
 * Base class for Links.
 * 
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractLink.java,v 1.12.2.2.2.1 2008/12/29 05:29:32 akurzhan Exp $
 */
public abstract class AbstractLink extends AbstractNetworkElement {
	protected double length;
	
	protected PositionLink myPosition;
	
	
	/**
	 * Validates Link configuration.
	 * <p>Checks that the link has at least one of begin and end nodes,
	 * but not more than one of each.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean validate() throws ExceptionConfiguration {
		boolean res = super.validate();
		int m = predecessors.size(); // if 0, then it is ORIGIN link
		int n = successors.size(); // if 0, then it is DESTINATION link
		if ((m == 0) && (n == 0)) { // both cannot be 0
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "No Nodes attached."));
			res = false;
			//throw new ExceptionConfiguration(this, "No Nodes attached.");
		}
		if (m > 1) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "More than one Begin Node."));
			res = false;
			//throw new ExceptionConfiguration(this, "More than one Begin Node.");
		}
		AbstractNode nd = getBeginNode();
		if ((nd != null) && (nd.getSuccessorById(id) == null)) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Wrong Begin Node (" + nd + ")."));
			res = false;
		}
		if (n > 1) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "More than one End Node."));
			res = false;
			//throw new ExceptionConfiguration(this, "More than one End Node.");
		}
		nd = getEndNode();
		if ((nd != null) && (nd.getPredecessorById(id) == null)) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Wrong End Node (" + nd + ")."));
			res = false;
		}
		return res;
	}
	
	/**
	 * Updates configuration summary.
	 * @param cs configuration summary.
	 */
	public void updateConfigurationSummary(AbstractConfigurationSummary cs) {
		if (cs == null)
			return;
		cs.incrementLinks();
		if (predecessors.isEmpty())
			cs.incrementSources();
		if (successors.isEmpty())
			cs.incrementDestinations();
		cs.updateLongestLink(this);
		cs.updateShortestLink(this);
		return;
	}
	
	
	/**
	 * Returns position of the link.
	 */
	public final PositionLink getPosition() {
		return myPosition;
	}
	
	/**
	 * Returns the length of the link.
	 */
	public final double getLength() {
		return length;
	}
	
	/**
	 * Returns begin node.
	 */
	public final AbstractNode getBeginNode() {
		if (predecessors.size() > 0)
			return (AbstractNode)predecessors.firstElement();
		else
			return null;
	}
	
	/**
	 * Returns end node.
	 */
	public final AbstractNode getEndNode() {
		if (successors.size() > 0)
			return (AbstractNode)successors.firstElement();
		else
			return null;
	}
	
	/**
	 * Returns other input Links for the Node, for which current Link is in-coming.
	 */
	public final Vector<AbstractLink> getInPeers() {
		Vector<AbstractLink> peers = new Vector<AbstractLink>();
		AbstractNode nd = getEndNode();
		if (nd == null)
			return peers;
		Vector<AbstractNetworkElement> nes = nd.getPredecessors();
		for (int i = 0; i < nes.size(); i++)
			if (!nes.get(i).equals(this))
				peers.add((AbstractLink)nes.get(i));
		return peers;
	}
	
	/**
	 * Returns other output Links for the Node, for which current Link is out-going.
	 */
	public final Vector<AbstractLink> getOutPeers() {
		Vector<AbstractLink> peers = new Vector<AbstractLink>();
		AbstractNode nd = getBeginNode();
		if (nd == null)
			return peers;
		Vector<AbstractNetworkElement> nes = nd.getSuccessors();
		for (int i = 0; i < nes.size(); i++)
			if (!nes.get(i).equals(this))
				peers.add((AbstractLink)nes.get(i));
		return peers;
	}
	
	/**
	 * Sets position of the link.
	 * @param x link position.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setPosition(PositionLink x) {
		if (x == null)
			return false;
		else
			myPosition = x;
		return true;
	}
	
	/**
	 * Sets length of the link.
	 * @param x link length.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setLength(double x) {
		if (x >= 0)
			length = x;
		else
			return false;
		return true;
	}
	
	/**
	 * Sets begin node without any modifications to the affected nodes.
	 * @param x - begin node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setBeginNode(AbstractNode x) {
		if (x == null) {
			predecessors.clear();
			return true;
		}
		if (predecessors.isEmpty())
			predecessors.add(x);
		else
			predecessors.set(0, x);
		return true;
	}
	
	/**
	 * Sets end node without any modifications to the affected nodes.
	 * @param x - end node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setEndNode(AbstractNode x) {
		if (x == null) {
			successors.clear();
			return true;
		}
		if (successors.isEmpty())
			successors.add(x);
		else
			successors.set(0, x);
		return true;
	}
	
	/**
	 * Assigns begin node.<br>
	 * Adjusts out-link lists in both, old and new, begin nodes.
	 * @param x - begin node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean assignBeginNode(AbstractNode x) {
		AbstractNode nd = getBeginNode();
		if (nd != null)
			nd.deleteSuccessor(this);
		if ((myNetwork != null) && (myNetwork.getVerbose()))
			System.out.println("* " + this + " *: replacing Begin Node '" + nd + "' with '" + x + "'");
		if (x == null) {
			predecessors.clear();
			return true;
		}
		x.addOutLink(this);
		if (predecessors.isEmpty())
			predecessors.add(x);
		else
			predecessors.set(0, x);
		return true;
	}
	
	/**
	 * Assigns end node.<br>
	 * Adjusts in-link lists in both, old and new, end nodes.
	 * @param x - end node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean assignEndNode(AbstractNode x) {
		AbstractNode nd = getEndNode();
		if (nd != null)
			nd.deletePredecessor(this);
		if ((myNetwork != null) && (myNetwork.getVerbose()))
			System.out.println("* " + this + " *: replacing End Node '" + nd + "' with '" + x + "'");
		if (x == null) {
			successors.clear();
			return true;
		}
		x.addInLink(this);
		if (successors.isEmpty())
			successors.add(x);
		else
			successors.set(0, x);
		return true;
	}

	/**
	 * Deletes specified NE from the list of predecessors. 
	 * @param x predecessor NE to be deleted.
	 * @return idx index of deleted predecessor, <code>-1</code> - if such NE was not found.
	 */
	public synchronized int deletePredecessor(AbstractNetworkElement x) {
		AbstractNode nd = getBeginNode();
		if (nd != null)
			myPosition.setBegin(nd.getPosition().get());
		return super.deletePredecessor(x);
	}

	/**
	 * Deletes specified NE from the list of successors. 
	 * @param x successor NE to be deleted.
	 * @return idx index of deleted successor, <code>-1</code> - if such NE was not found.
	 */
	public synchronized int deleteSuccessor(AbstractNetworkElement x) {
		AbstractNode nd = getEndNode();
		if (nd != null)
			myPosition.setEnd(nd.getPosition().get());
		return super.deleteSuccessor(x);
	}
	
	/**
	 * Copies data from given Link to a current one.
	 * @param x given Network Element.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean copyData(AbstractNetworkElement x) {
		boolean res = super.copyData(x);
		if ((!res) || ((x.getType() & AbstractTypes.MASK_LINK) == 0))
			return false;
		AbstractLink lnk = (AbstractLink)x;
		length = lnk.getLength();
		myPosition = lnk.getPosition();
		return res;
	}

}