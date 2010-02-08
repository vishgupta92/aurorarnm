/**
 * @(#)AbstractConfigurationSummary.java
 */
package aurora;

/**
 * Base class for configuration summary objects.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public abstract class AbstractConfigurationSummary {
	protected int numMonitors = 0;
	protected int numNetworks = 0;
	protected int numNodes = 0;
	protected int numLinks = 0;
	protected int numSources = 0;
	protected int numDestinations = 0;
	protected AbstractLink lLink = null;
	protected AbstractLink sLink = null;
	
	
	/**
	 * Returns number of Monitors.
	 */
	final public int numberOfMonitors() {
		return numMonitors;
	}

	/**
	 * Returns number of Networks.
	 */
	final public int numberOfNetworks() {
		return numNetworks;
	}

	/**
	 * Returns number of Nodes.
	 */
	final public int numberOfNodes() {
		return numNodes;
	}

	/**
	 * Returns number of Links.
	 */
	final public int numberOfLinks() {
		return numLinks;
	}

	/**
	 * Returns number of source Links.
	 */
	final public int numberOfSources() {
		return numSources;
	}

	/**
	 * Returns number of destination Links.
	 */
	final public int numberOfDestinations() {
		return numDestinations;
	}
	
	/**
	 * Returns the longest Link.
	 */
	final public AbstractLink longestLink() {
		return lLink;
	}

	/**
	 * Returns the shortest Link.
	 */
	final public AbstractLink shortstLink() {
		return sLink;
	}
	
	/**
	 * Increments number of Monitors.
	 */
	public synchronized void incrementMonitors() {
		numMonitors++;
		return;
	}

	/**
	 * Increments number of Networks.
	 */
	public synchronized void incrementNetworks() {
		numNetworks++;
		return;
	}

	/**
	 * Increments number of Nodes.
	 */
	public synchronized void incrementNodes() {
		numNodes++;
		return;
	}

	/**
	 * Increments number of Links.
	 */
	public synchronized void incrementLinks() {
		numLinks++;
		return;
	}

	/**
	 * Increments number of source Links.
	 */
	public synchronized void incrementSources() {
		numSources++;
		return;
	}

	/**
	 * Increments number of destination Links.
	 */
	public synchronized void incrementDestinations() {
		numDestinations++;
		return;
	}
	
	/**
	 * Update the longest Link.
	 * @param lk the Link which current longest Link is to be compared with. 
	 */
	public synchronized void updateLongestLink(AbstractLink lk) {
		if ((lk == null) || (lk.getBeginNode() == null) || (lk.getEndNode() == null))
			return;
		if (lLink == null)
			lLink = lk;
		else
			if (lLink.getLength() < lk.getLength())
				lLink = lk;
		return;
	}
	
	/**
	 * Update the shortest Link.
	 * @param lk the Link which current shortest Link is to be compared with. 
	 */
	public synchronized void updateShortestLink(AbstractLink lk) {
		if ((lk == null) || (lk.getBeginNode() == null) || (lk.getEndNode() == null))
			return;
		if (sLink == null)
			sLink = lk;
		else
			if (sLink.getLength() > lk.getLength())
				sLink = lk;
		return;
	}
	
	/**
	 * Resets stistics values.
	 */
	public synchronized void reset() {
		numMonitors = 0;
		numNetworks = 0;
		numNodes = 0;
		numLinks = 0;
		numSources = 0;
		numDestinations = 0;
		return;
	}

}
