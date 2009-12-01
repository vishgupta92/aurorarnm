/**
 * @(#)AbstractNetworkElement.java
 */

package aurora;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;


/**
 * Base class for Nodes, Links and Monitors.
 * 
 * @see AbstractNode, AbstractLink, AbstractMonitor.
 * 
 * @author Alex Kurzhanskiy
 * @version $Id: AbstractNetworkElement.java,v 1.24.2.5.2.2.2.5 2009/11/22 22:19:35 akurzhan Exp $
 */
public abstract class AbstractNetworkElement implements AuroraConfigurable, Serializable {
	private static final long serialVersionUID = -1658852405459300241L;
	
	protected boolean initialized = false;
	protected int id;
	protected int ts = 0; // time step of the simulation
	protected int saveState = 0; // indicates if state should be saved (3 - to be saved, 0 - not to be saved, 2 - transition from 0 to 3, 1 - transition from 3 to 0)

	protected Vector<AbstractNetworkElement> predecessors = new Vector<AbstractNetworkElement>();
	protected Vector<AbstractNetworkElement> successors = new Vector<AbstractNetworkElement>();

	protected AbstractNodeComplex myNetwork;
	
	
	public AbstractNetworkElement() { }
	public AbstractNetworkElement(int id) { this.id = id; }
	

	/**
	 * Initializes NE from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public abstract boolean initFromDOM(Node p) throws ExceptionConfiguration;
	
	/**
	 * Additional initialization.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration, ExceptionDatabase
	 */
	public boolean initialize() throws ExceptionConfiguration, ExceptionDatabase {
		ts = 0;
		return true;
	}
	
	/**
	 * Generates XML description of the NE.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public abstract void xmlDump(PrintStream out) throws IOException;
	
	/**
	 * Updates the state of NE according to the current state of its neighbors.
	 * @param ts new time step.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		if (ts < 1)
			throw new ExceptionSimulation(this, "Nonpositive time step (" + Integer.toString(ts) + ").");
		if (this.ts == 0) {
			this.ts = ts;
			return true;
		}
		int period = (int)Math.round((double)(myNetwork.getTP()/getTop().getTP()));
		if (period == 0)
			period = 1;
		if ((ts - this.ts) < period)
			return false;
		this.ts = ts;
		return true;
	}
		
	/**
	 * Validates the NE configuration.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean validate() throws ExceptionConfiguration {
		//if (id < 0) /* We should allow negative IDs */
			//throw new ExceptionConfiguration(this, "Invalid identifier (" + Integer.toString(id) + ").");
		if (myNetwork == null)
			throw new ExceptionConfiguration(this, "Network not assigned.");
		return true;
	}
	
	/**
	 * Updates configuration summary.
	 * @param cs configuration summary.
	 */
	public abstract void updateConfigurationSummary(AbstractConfigurationSummary cs);
	
	/**
	 * Returns true if the state is to be saved, false - otherwise.
	 */
	public final boolean toSave() {
		if (saveState > 1)
			return true;
		return false;
	}
	/**
	 * Returns identifier of a NE.
	 */
	public final int getId() {
		return id;
	}
	
	/**
	 * Returns type of a NE
	 */
	public abstract int getType();
	
	/**
	 * Returns type description of a NE
	 */
	public abstract String getTypeString();
	
	/**
	 * Returns time step.
	 */
	public final int getTS() {
		return ts;
	}

	/**
	 * Returns vector of NE that are graph predecessors of current NE
	 */
	public final Vector<AbstractNetworkElement> getPredecessors() {
		return predecessors; 
	}
	
	/**
	 * Returns vector of NE that are graph successors of current NE
	 */
	public final Vector<AbstractNetworkElement> getSuccessors() {
		return successors;
	}
	
	/**
	 * Returns predecessor by its ID, null if not found.
	 * @param id
	 */
	public final AbstractNetworkElement getPredecessorById(int id) {
		for (int i = 0; i < predecessors.size(); i++)
			if (predecessors.get(i).getId() == id)
				return predecessors.get(i);
		return null;
	}

	/**
	 * Returns successor by its ID, null if not found.
	 * @param id
	 */
	public final AbstractNetworkElement getSuccessorById(int id) {
		for (int i = 0; i < successors.size(); i++)
			if (successors.get(i).getId() == id)
				return successors.get(i);
		return null;
	}

	/**
	 * Returns Complex Node of which current NE is part.
	 */
	public final AbstractNodeComplex getMyNetwork() {
		return myNetwork;
	}
	
	/**
	 * Returns top level Complex Node.
	 */
	public final AbstractNodeComplex getTop() {
		AbstractNodeComplex prnt = myNetwork;
		while (!prnt.isTop())
			prnt = prnt.getMyNetwork(); 
		return prnt;
	}
	
	/**
	 * Adds NE to the list of predecessors for current NE.
	 * @param x NE that is graph predecessor for the current NE.
	 * @return idx index of the added NE, <code>-1<code> - if the NE could not be added.
	 */
	public synchronized int addPredecessor(AbstractNetworkElement x) {
		int idx = -1;
		if ((x != null) && (predecessors.add(x)))
			idx = predecessors.size() - 1;
		return idx;
	}
	
	/**
	 * Adds NE to the list of successors for current NE.
	 * @param x NE that is graph successor for the current NE.
	 * @return idx index of the added NE, <code>-1<code> - if the NE could not be added.
	 */
	public synchronized int addSuccessor(AbstractNetworkElement x) {
		int idx = -1;
		if ((x != null) && (successors.add(x)))
			idx = successors.size() - 1;
		return idx;
	}

	/**
	 * Deletes specified NE from the list of predecessors. 
	 * @param x predecessor NE to be deleted.
	 * @return idx index of deleted predecessor, <code>-1</code> - if such NE was not found.
	 */
	public synchronized int deletePredecessor(AbstractNetworkElement x) {
		int idx = predecessors.indexOf(x);
		if (idx >= 0)
			predecessors.remove(idx);
		return idx;
	}
	
	/**
	 * Deletes specified NE from the list of successors. 
	 * @param x successor NE to be deleted.
	 * @return idx index of deleted successor, <code>-1</code> - if such NE was not found.
	 */
	public synchronized int deleteSuccessor(AbstractNetworkElement x) {
		int idx = successors.indexOf(x);
		if (idx >= 0)
			successors.remove(idx);
		return idx;
	}
	
	/**
	 * Assigns Complex Node of which current NE is part.
	 * @param x Complex Node.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean setMyNetwork(AbstractNodeComplex x) {
		if (x != null)
			myNetwork = x;
		else
			return false;
		return true;
	}
	
	/**
	 * Assigns ID.
	 * @param id ID.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean setId(int id) {
		this.id = id;
		return true;
	}
	
	/**
	 * Sets save state mode.
	 * @param x save state mode.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public final synchronized boolean setSave(boolean x) {
		if (!getMyNetwork().getContainer().isSimulation())
			if (x)
				saveState = 3;
			else
				saveState = 0;
		else
			if (x)
				switch (saveState) {
				case 1: 
				case 3: saveState = 3; break;
				default: saveState = 2; break;
				}
			else
				switch (saveState) {
				case 1: 
				case 3: saveState = 1; break;
				default: saveState = 0; break;
				}
		return true;
	}
	
	/**
	 * Replaces given predecessor with the new one.
	 * @param oldne old predecessor.
	 * @param newne new predecessor.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean replacePredecessor(AbstractNetworkElement oldne, AbstractNetworkElement newne) {
		if ((oldne == null) || (newne == null))
			return false;
		int idx = predecessors.indexOf(oldne);
		if (idx < 0)
			return false;
		predecessors.remove(idx);
		predecessors.add(idx, newne);
		return true;
	}
	
	/**
	 * Replaces given successor with the new one.
	 * @param oldne old successor.
	 * @param newne new successor.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean replaceSuccessor(AbstractNetworkElement oldne, AbstractNetworkElement newne) {
		if ((oldne == null) || (newne == null))
			return false;
		int idx = successors.indexOf(oldne);
		if (idx < 0)
			return false;
		successors.remove(idx);
		successors.add(idx, newne);
		return true;
	}
	
	public synchronized void deleteDeadNeighbors() {
		int i = 0;
		while (i < predecessors.size()) {
			if ((myNetwork.getNodeById(predecessors.get(i).getId()) == null) &&
				(myNetwork.getLinkById(predecessors.get(i).getId()) == null) &&
				(myNetwork.getMonitorById(predecessors.get(i).getId()) == null) &&
				(myNetwork.getSensorById(predecessors.get(i).getId()) == null))
				predecessors.remove(i);
			else
				i++;
		}
		i = 0;
		while (i < successors.size()) {
			if ((myNetwork.getNodeById(successors.get(i).getId()) == null) &&
				(myNetwork.getLinkById(successors.get(i).getId()) == null) &&
				(myNetwork.getMonitorById(successors.get(i).getId()) == null) &&
				(myNetwork.getSensorById(successors.get(i).getId()) == null))
				successors.remove(i);
			else
				i++;
		}
	}
	
	/**
	 * Copies data from given Network Element to a current one.
	 * @param x given Network Element.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean copyData(AbstractNetworkElement x) {
		if (x == null)
			return false;
		id = x.getId();
		predecessors = x.getPredecessors();
		successors = x.getSuccessors();
		myNetwork = x.getMyNetwork();
		return true;
	}
	
}
