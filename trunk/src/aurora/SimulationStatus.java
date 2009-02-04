/**
 * @(#)SimulationStatus.java
 */

package aurora;

import java.io.Serializable;


/**
 * Class for describing simulation status.
 * @author Alex Kurzhanskiy
 * @version $Id: SimulationStatus.java,v 1.1.2.2 2007/04/14 05:08:41 akurzhan Exp $
 */
public class SimulationStatus  implements Serializable {
	private static final long serialVersionUID = 2380454189887604271L;
	
	protected boolean stopped = true;
	protected boolean saved = true;
	
	
	/**
	 * Checks if the simulation is saved.
	 */
	public boolean isSaved() {
		return saved;
	}
	
	/**
	 * Checks if the simulation is stopped.
	 */
	public boolean isStopped() {
		return stopped;
	}
	
	/**
	 * Sets saved status of the simulation.
	 * @param x boolean value.
	 */
	public synchronized void setSaved(boolean x) {
		saved = x;
		return;
	}
	
	/**
	 * Sets stopped status of the simulation.
	 * @param x boolean value.
	 */
	public synchronized void setStopped(boolean x) {
		stopped = x;
		return;
	}

}
