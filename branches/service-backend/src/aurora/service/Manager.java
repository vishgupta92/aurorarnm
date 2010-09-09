/**
 * Classes for using aurora simulator as a service back-end.
 */
package aurora.service;

import java.io.*;
import java.net.URI;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.InputSource;

import org.w3c.dom.*;

import aurora.*;
import aurora.common.*;
import aurora.hwc.*;
import aurora.hwc.common.*;
import aurora.util.*;

/**
 * Main class for running aurora simulator as a service
 * @author vjoel
 *
 */
public class Manager {
	/**
	 * 
	 */
	public Manager() {
		// TODO Auto-generated constructor stub
	}
	
	public String run_once(String input_xml, String outfile, Updatable updater, int period) {
		ContainerHWC mySystem = new ContainerHWC();
		mySystem.batchMode();
		try {
	        InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(input_xml));
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			mySystem.initFromDOM(doc.getChildNodes().item(0));
			mySystem.validate();
		}
		catch(Exception e) {
			return "Error: Failed to parse xml: " + e.getMessage();
		}
		File data = new File(outfile);
		try {
			if ((!mySystem.getMySettings().setTmpDataFile(data)) || (!mySystem.getMySettings().createDataHeader())) {
				return "Error: Failed to open data output file!";
			}
		}
		catch(Exception e) {
			return "Error: Failed to open data output file: " + e.getMessage();
		}
		try {
			mySystem.initialize();
		}
		catch(Exception e) {
			mySystem.getMySettings().getTmpDataOutput().close();
			return "Error: Failed to initialize: " + e.getMessage();
		}
		boolean res = true;
		mySystem.getMyStatus().setStopped(false);
		mySystem.getMyStatus().setSaved(false);
		int ts = mySystem.getMyNetwork().getTS();
		while ((!mySystem.getMyStatus().isStopped()) && res) {
			try {
				res = mySystem.dataUpdate(++ts);
			}
			catch(Exception e) {
				mySystem.getMySettings().getTmpDataOutput().close();
				return "Simulation failed on time step " + ts + ": " + e.getMessage();
			}
			if (updater != null && ts % period == 0) {
				updater.notify_update("step="+ts);
			}
		}
		if (!res)
			return "Simulation failed on time step " + ts;
		mySystem.getMySettings().getTmpDataOutput().close();
		
		return("Done!");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
        	runBatch(args[0], args[1]);
		}
		else {
			System.err.println("Arguments: infile outfile");
		}
	}
	
	private static void runBatch(String infile, String outfile) {
		File config = new File(infile);
		ContainerHWC mySystem = new ContainerHWC();
		mySystem.batchMode();
		try {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("file:" + config.getAbsolutePath());
			mySystem.initFromDOM(doc.getChildNodes().item(0));
			mySystem.validate();
		}
		catch(Exception e) {
			String buf = e.getMessage();
			if ((buf == null) || (buf.equals("")))
				buf = "Failed to read configuration file!";
			System.err.println(buf + "\nExiting...");
			return;
		}
		File data = new File(outfile);
		try {
			if ((!mySystem.getMySettings().setTmpDataFile(data)) || (!mySystem.getMySettings().createDataHeader())) {
				System.err.println("Failed to open data output file!\nExiting...");
				return;
			}
		}
		catch(Exception e) {
			String buf = e.getMessage();
			if ((buf == null) || (buf.equals("")))
				buf = "Failed to open data output file!";
			System.err.println(buf + "\nExiting...");
			return;
		}
		try {
			mySystem.initialize();
		}
		catch(Exception e) {
			String buf = e.getMessage();
			if ((buf == null) || (buf.equals("")))
				buf = "Initialization failed!";
			System.err.println(buf + "\nExiting...");
			mySystem.getMySettings().getTmpDataOutput().close();
			return;
		}
		boolean res = true;
		mySystem.getMyStatus().setStopped(false);
		mySystem.getMyStatus().setSaved(false);
		int ts = mySystem.getMyNetwork().getTS();
		while ((!mySystem.getMyStatus().isStopped()) && res) {
			try {
				res = mySystem.dataUpdate(++ts);
			}
			catch(Exception e) {
				String buf = e.getMessage();
				if ((buf == null) || (buf.equals("")))
					buf = "Simulation failed on time step " + ts + "!";
				System.err.println(buf + "\nExiting...");
				mySystem.getMySettings().getTmpDataOutput().close();
				return;
			}
		}
		if (!res)
			System.err.println("Simulation failed on time step " + ts + "!\nExiting...");
		mySystem.getMySettings().getTmpDataOutput().close();
		System.out.println("Done!");
	}

}
