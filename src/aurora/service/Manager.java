/**
 * Classes for using aurora simulator as a service back-end.
 */
package aurora.service;

import java.io.*;
import java.net.URI;
import java.util.*;
import javax.xml.parsers.*;

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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {
			String infile = args[0];
			String outfile = args[1];

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

}
