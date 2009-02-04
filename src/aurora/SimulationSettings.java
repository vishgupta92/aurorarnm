/**
 * @(#)SimulationSettings.java 
 */

package aurora;

import java.awt.*;
import java.io.*;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;


/**
 * Class for program settings description.
 * @author Alex Kurzhanskiy
 * @version $Id: SimulationSettings.java,v 1.1.2.6.2.2 2008/11/23 23:34:34 akurzhan Exp $
 */
public class SimulationSettings implements AuroraConfigurable, Serializable {
	private static final long serialVersionUID = -5690805735719405L;
	
	protected static PrintStream outputStream = System.out;
	protected static PrintStream errorStream = System.err;
	protected static Dimension windowSize = new Dimension();
	protected double displayTP = 1.0/12.0;
	protected double timeMax = 24;
	protected int tsMax = 100000;
	protected int timeout = 1000;
	
	
	/**
	 * Initializes settings from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if (p == null)
			return !res;
		try  {
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (int i = 0; i < pp.getLength(); i++) {
					if (pp.item(i).getNodeName().equals("display")) {
						displayTP = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("tp").getNodeValue());
						timeout = Integer.parseInt(pp.item(i).getAttributes().getNamedItem("timeout").getNodeValue());
						tsMax = Integer.parseInt(pp.item(i).getAttributes().getNamedItem("tsMax").getNodeValue());
						timeMax = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("timeMax").getNodeValue());
					}
					if (pp.item(i).getNodeName().equals("include")) {
						Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp.item(i).getAttributes().getNamedItem("uri").getNodeValue());
						initFromDOM(doc.getChildNodes().item(0));
					}
				}
			}
			else
				res = false;
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}
	
	/**
	 * Generates XML description of the application settings.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		out.print("<display tp=\"" + Double.toString(displayTP) + "\" timeout=\"" + Integer.toString(timeout) + "\" tsMax=\"" + Integer.toString(tsMax) + "\" timeMax=\"" + Double.toString(timeMax) + "\" />\n");
		return;
	}
	
	/**
	 * Bogus function that always returns <code>true</code>.
	 */
	public boolean validate() throws ExceptionConfiguration {
		return true;
	}
	
	/**
	 * Returns output stream.
	 */
	public PrintStream getOutputStream() {
		return outputStream;
	}
	
	/**
	 * Returns error stream.
	 */
	public PrintStream getErrorStream() {
		return errorStream;
	}
	
	/**
	 * Returns window size.
	 */
	public Dimension getWindowSize() {
		return windowSize;
	}
	
	/**
	 * Returns display update period
	 */
	public double getDisplayTP() {
		return displayTP;
	}
	
	/**
	 * Returns maximum simulation time.
	 */
	public double getTimeMax() {
		return timeMax;
	}

	/**
	 * Returns maximum simulation step.
	 */
	public int getTSMax() {
		return tsMax;
	}
	
	/**
	 * Returns timeout between updates.
	 */
	public int getTimeout() {
		return timeout;
	}
	
	/**
	 * Sets output stream.
	 * @param os output stream.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setOutputStream(PrintStream os) {
		if (os == null)
			return false;
		outputStream = os;
		return true;
	}
	
	/**
	 * Sets error stream.
	 * @param es error stream.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setErrorStream(PrintStream es) {
		if (es == null)
			return false;
		errorStream = es;
		return true;
	}
	
	/**
	 * Sets the window size.
	 * @param dims window size.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setWindowSize(Dimension dims) {
		if (dims == null)
			return false;
		windowSize = dims;
		return true;
	}
	
	/**
	 * Sets display update period.
	 * @param tp time period.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setDisplayTP(double tp) {
		if (tp <= 0.0)
			return false;
		displayTP = tp;
		return true;
	}
	
	/**
	 * Sets max simulation time.
	 * @param tmax max time value.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setTimeMax(double tmax) {
		if (tmax <= 0.0)
			return false;
		timeMax = tmax;
		return true;
	}

	/**
	 * Sets max simulation step.
	 * @param ts max step value.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setTSMax(int ts) {
		if (ts <= 0)
			return false;
		tsMax = ts;
		return true;
	}
	
	/**
	 * Sets timeout between updates.
	 * @param dt timeout in milliseconds.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise. 
	 */
	public synchronized boolean setTimeout(int dt) {
		if (dt <= 0)
			return false;
		timeout = dt;
		return true;
	}
	
	/**
	 * Copies data from the given settings object to the current one.
	 * @param x given settings object.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean copyData(SimulationSettings x) {
		if (x == null)
			return false;
		displayTP = x.getDisplayTP();
		timeMax = x.getTimeMax();
		tsMax = x.getTSMax();
		timeout = x.getTimeout();
		outputStream = x.getOutputStream();
		errorStream = x.getErrorStream();
		return true;
	}
	
}