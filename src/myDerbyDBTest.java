/**
 * @(#)myDerbyDBTest.java
 */

import aurora.*;
import aurora.hwc.*;
//import aurora.test.*;


/**
 * Experimenting with Derby DB.
 */
public class myDerbyDBTest {
	protected static DataStorage db;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		db = new DBDerby("c:\\tmp");
		db.initialize();
		//i210SimpleSetup tst = new i210SimpleSetup();
		NodeHWCNetwork myNetwork = new NodeHWCNetwork(); // = tst.getMyNetwork();
		
		if (myNetwork.validate())
			System.out.println("Configuration ok...");
		else {
			System.err.println("Configuration error! Exiting...");
			return;
		}
		
		myNetwork.setDatabase(db);
		db.newSimulationNumber(myNetwork, "Testing database for the first time...");
		
		for (int ts = 1; ts < 116; ts++) {
			System.out.println("Iteration " + Integer.toString(ts));
			myNetwork.dataUpdate(ts);
		}
		
		System.out.println("Simulation ok...");
		
		db.saveSimulation(new ContainerHWC(myNetwork), null);
		
		System.out.println("Simulation saved.");
		
	}

}
