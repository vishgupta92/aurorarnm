/**
 * @(#)ControllerSimpleSignal.java
 */

package aurora.hwc.control;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import org.w3c.dom.*;
import aurora.*;
import aurora.hwc.*;


/**
 * Implementation of simple timed signal controller.
 * @author Andy Chow
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public final class ControllerSimpleSignal extends AbstractControllerHWC {
	private static final long serialVersionUID = 4744505125393458926L;
	
	private boolean greenLight = false;
	
	private double offset = 0.0;
	private double timeOfLastSwitch = 0.0;
	private double green = 1.0; // green period in hours
	private double red = 0.0; // red period in hours
	
	private Vector<CycleDataRow> cycleTable = new Vector<CycleDataRow>();

	/**
	 * Returns controller description.
	 */
	public String getDescription() {
		int cnv = 3600;
		NumberFormat form = NumberFormat.getInstance();
		form.setMaximumFractionDigits(0);
		return "Simple Signal (offset = " + form.format(offset * cnv) + " s; green = " + form.format(green * cnv) + "s; red = " + form.format(red * cnv) + " s)";
	}
	
	/**
	 * Returns mask for compatible Node types.
	 */
	public final int getCompatibleNodeTypes() {
		return (((~TypesHWC.MASK_NODE) & TypesHWC.NODE_SIGNAL) | ((~TypesHWC.MASK_NODE) & TypesHWC.NODE_HIGHWAY));
	}
	
	/**
	 * Returns offset.
	 */
	public double getOffset() {
		return offset;
	}
	
	/**
	 * Returns cycle table.
	 */
	public Vector<CycleDataRow> getCycleTable() {
		return cycleTable;
	}

	/**
	 * Initializes the simple signal controller from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = super.initFromDOM(p);
		if (!res)
			return res;
		try  {
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (int i = 0; i < pp.getLength(); i++) {
					if (pp.item(i).getNodeName().equals("parameter")) {
						if (pp.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("offset"))
							offset = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("value").getNodeValue());
					}
					if (pp.item(i).getNodeName().equals("cycle")) {
						double time = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("time").getNodeValue());
						double green = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("green").getNodeValue());
						double red = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("red").getNodeValue());
						boolean done = false;
						for (int j = 0; j < cycleTable.size(); j++){
							if(cycleTable.get(j).getTime() > time){
								cycleTable.insertElementAt(new CycleDataRow(time, green, red), j);
								done = true;
								break;
							}	
						}
						if(!done)
							cycleTable.add(new CycleDataRow(time, green, red));
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
	 * Computes controlled input flow for given Signal Node.
	 * @param xx given Signal Node.
	 * @return input flow.
	 */
	public synchronized Object computeInput(AbstractNodeSimple xx) {
		AbstractNodeHWC x = (AbstractNodeHWC)xx;
		Double flw = (Double)super.computeInput(x);	// flw - controlled inflow to the signal node 
		if (flw != null)
			return flw;
		double time = xx.getMyNetwork().getSimTime();
		if (time < offset) {
			timeOfLastSwitch = Double.NEGATIVE_INFINITY;
			greenLight = false;
			return new Double(0.0);
		}
		for (int i = 0; i < cycleTable.size(); i++)
			if (time >= cycleTable.get(i).getTime()) {
				green = cycleTable.get(i).getGreen();
				red = cycleTable.get(i).getRed();
			}
		if ((greenLight) && ((time - timeOfLastSwitch) > green)) {
			timeOfLastSwitch = time;
			greenLight = false;
		} else if ((!greenLight) && ((time - timeOfLastSwitch) > red)) {
			timeOfLastSwitch = time;
			greenLight = true;
		}
		if (greenLight)
			flw = (Double)limits.get(1);
		else
			flw = 0.0;
		input = new Double(flw);
		return flw;
	}
	
	/**
	 * Generates XML description of the simple signal controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		out.print("<parameter name=\"offset\" value=\"" + offset + "\"/>");
		for (int i = 0; i < cycleTable.size(); i++)
			out.print("<cycle time=\"" + cycleTable.get(i).getTime() + "\" green=\"" + cycleTable.get(i).getGreen() + "\" red=\"" + cycleTable.get(i).getRed() + "\"/>");
		out.print("</controller>");
		return;
	}
	
	/**
	 * Sets offset.
	 * @param x offset value in hours.
	 */
	public void setOffset(double x) {
		if (x >= 0.0)
			offset = x;
		return;
	}
	
	/**
	 * Sets cycle table.
	 * @param inTable vector of cycle data rows.
	 */
	public void setCycleTable(Vector<CycleDataRow> inTable){
		if (inTable != null)
			cycleTable = inTable;
		return;
	}
	
	/**
	 * Additional optional initialization steps.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initialize() throws ExceptionConfiguration {
		boolean res = super.initialize();
		greenLight = false;
		green = 1.0;
		red = 0.0;
		timeOfLastSwitch = 0.0;
		return res;
	}
	
	/**
	 * Returns letter code of the controller type.
	 */
	public final String getTypeLetterCode() {
		return "SIMPLESIGNAL";
	}
	
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Controller.
	 */
	public String toString() {
		return "Simple Signal";
	}
	
	
	
	/**
	 * This class implements TOD table entry.
	 */
	public class CycleDataRow implements Serializable {
		private static final long serialVersionUID = -1021146674537376048L;
		
		
		private double time;
		private double green;
		private double red;
		
		public CycleDataRow(){ time = 0; green = 1; red = 0; }
		public CycleDataRow(double t, double g, double r) { time = t; green = g; red = r; }
		
		public double getTime() {
			return time;
		}
		
		public double getGreen() {
			return green;
		}
		
		public double getRed() {
			return red;
		}
		
		public void setTime(int h, int m, double s){
			if ((h >= 0) && (m >= 0) && (s >= 0))
				time = h + m/60.0 + s/3600.0;
			return;
		}
		
		public void setTime(double t) {
			if (t >= 0.0)
				time = t;
			return;
		}
		
		public void setGreen(double g){
			if (g >= 0.0) {
				green = g;
			}
			return; 
		}
		
		public void setRed(double r){
			if (r >= 0.0) {
				red = r;
			}
			return; 
		}
		
		public void setCycle(double g, double r){
			if ((g >= 0.0) && (r >= 0.0)) {
				green = g;
				red = r;
			}
			return; 
		}
	}
}
