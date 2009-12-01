/**
 * @(#)mySerializableTest.java
 */

import java.io.*;
import aurora.*;
import aurora.hwc.*;
import aurora.hwc.control.*;
//import aurora.test.*;


/**
 * Let's see how we can save our configuration.
 * @author Alex Kurzhanskiy
 * @version $Id: mySerializableTest.java,v 1.2.2.2.2.2.2.2 2009/10/18 00:58:29 akurzhan Exp $
 */
public final class mySerializableTest {
	static String DATAFILE = "c:\\tmp\\aurora_out.dat";
	static String FILENAME = "c:\\tmp\\aurora_2.xml";
	//static i210SimpleSetup tst = new i210SimpleSetup();
	static NodeHWCNetwork myNetwork = new NodeHWCNetwork(); // = tst.getMyNetwork();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (myNetwork.validate())
			System.out.println("Configuration ok...");
		else {
			System.err.println("Configuration error! Exiting...");
			return;
		}
		
		ControllerALINEA ctrl = new ControllerALINEA(0.0, 5000, new QProportional(6.9));
		ctrl.setTP(1.0/12.0);
		NodeFreeway nd = (NodeFreeway)myNetwork.getNodeById(12);
		LinkOR lk = (LinkOR)myNetwork.getLinkById(11);
		nd.setSimpleController(ctrl, lk);
		
		EventManager emgr = new EventManager();
		
		emgr.addEvent(new EventQueueMax(8, 50, 7650));
		//emgr.addEvent(new EventDemand(3, 11000, 3760));
		emgr.addEvent(new EventFD(7, 2200, 40, 160, 5400));
		//double[][] srm = {{0.1, 0.9}};
		//emgr.addEvent(new EventSRM(14, srm, 4324));
		emgr.addEvent(new EventControllerSimple(21, ctrl, 20, 1004));
		
		System.out.println("Creating container...");
		ContainerHWC ctnr = new ContainerHWC(myNetwork, emgr);
		
		System.out.println("Saving...");
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATAFILE));
		oos.writeObject(ctnr);
		oos.close();
		
		System.out.println("Loading...");
		ObjectInputStream is = new ObjectInputStream(new FileInputStream(DATAFILE));
		ContainerHWC ctnr2 = (ContainerHWC)is.readObject();
		is.close();
		
		PrintStream os = new PrintStream(new FileOutputStream(FILENAME));
		//os.println(ctnr.xmlDump(null));
		System.out.println("Dumping XML...");
		ctnr2.xmlDump(os);
		os.close();
		System.out.println("Configuration saved in XML...");
		
	}

}
