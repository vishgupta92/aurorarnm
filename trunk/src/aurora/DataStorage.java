/**
 * @(#)DataStorage.java
 */

package aurora;


/**
 * Interface for saving simulation data.
 * @author Alex Kurzhanskiy
 * @version $Id: DataStorage.java,v 1.4.2.1 2007/04/18 23:32:21 akurzhan Exp $
 */
public interface DataStorage {
	
	boolean initialize() throws ExceptionDatabase;
	boolean saveMonitorData(AbstractMonitor x) throws ExceptionDatabase;
	boolean saveNodeData(AbstractNode x) throws ExceptionDatabase;
	boolean saveLinkData(AbstractLink x) throws ExceptionDatabase;
	boolean execute() throws ExceptionDatabase;
	boolean commit() throws ExceptionDatabase;
	boolean close() throws ExceptionDatabase;
	
	boolean newSimulationNumber(AbstractNodeComplex top, String descr) throws ExceptionDatabase;
	boolean saveSimulation(AbstractContainer top, String fname) throws ExceptionDatabase;
	
}
