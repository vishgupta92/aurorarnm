/**
 * @(#)ControllerVSLTOD.java 
 */

package aurora.hwc.control;

import java.io.*;
import java.util.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import aurora.*;
import aurora.hwc.*;


/**
 * TOD controller implementation.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public class ControllerVSLTOD extends AbstractControllerSimpleHWC {
	private static final long serialVersionUID = -7251723277193008260L;
	
	private Vector<TODdataRow> todTable = new Vector<TODdataRow>();

	/**
	 * Initializes the VSL TOD controller from given DOM structure.
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
					if (pp.item(i).getNodeName().equals("todspeed")) {
						double time = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("time").getNodeValue());
						double speed = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("speed").getNodeValue());
						boolean done = false;
						for (int j = 0; j < todTable.size(); j++){
							if(todTable.get(j).getTime() > time){
								todTable.insertElementAt(new TODdataRow(time, speed), j);
								done = true;
								break;
							}	
						}
						if(!done)
							todTable.add(new TODdataRow(time, speed));
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
	 * Generates XML description of the VSL TOD controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		for (int i = 0; i < todTable.size(); i++)
			out.print("<todspeed time=\"" + todTable.get(i).getTime() + "\" speed=\"" + todTable.get(i).getSpeed() + "\"/>");
		out.print("</controller>");
		return;
	}

	/**
	 * Computes desired input flow for given Node.
	 * @param xx given Node.
	 * @return input flow.
	 */
	public synchronized Object computeInput(AbstractNodeSimple xx) {
		AbstractNodeHWC x = (AbstractNodeHWC)xx;
		double density = ((AbstractLinkHWC)myLink).getDensity().sum().getCenter();
		double F = ((AbstractLinkHWC)myLink).getMaxFlow();
		double time = x.getMyNetwork().getSimTime();
		Double flw = (Double)0.0;
		if ((todTable.isEmpty()) || (time < todTable.get(0).getTime()) || (density < 0.0000001))
			flw = (Double)limits.get(1);
		else
			for (int i = 0; i < todTable.size(); i++)
				if (time >= todTable.get(i).getTime()) {
					double speed = todTable.get(i).getSpeed();
					flw = density * speed;
					double rj = ((AbstractLinkHWC)myLink).getJamDensity();
					double w = ((AbstractLinkHWC)myLink).getW();
					F = speed * ((w * rj) / (speed + w));
				}
		//flw = Math.min(flw, F);  // reduce link capacity
		flw = ApplyURMS(flw);
		flw = ApplyQueueControl(flw);
		input = ApplyLimits(flw);
		return input;
	}

	/**
	 * Returns controller description.
	 */
	public String getDescription() {
		return "VSL TOD (" + todTable.size() + " entries)";
	}
	
	/**
	 * Returns mask for compatible Node types.
	 */
	public final int getCompatibleNodeTypes() {
		return (((~TypesHWC.MASK_NODE) & TypesHWC.NODE_FREEWAY) | ((~TypesHWC.MASK_NODE) & TypesHWC.NODE_HIGHWAY));
	}
	
	/**
	 * Returns TOD table.
	 */
	public Vector<TODdataRow> getTable() {
		return todTable;
	}
	
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Controller.
	 */
	public String toString() {
		return "VSL TOD";
	}	
	
	/**
	 * Sets TOD table.
	 * @param inTable vector of TOD data rows.
	 */
	public void setTable(Vector<TODdataRow> inTable){
		if (inTable != null)
			todTable = inTable;
		return;
	}
	
	/**
	 * This class implements TOD table entry.
	 */
	public class TODdataRow {
		private double time;
		private double speed;
		
		public TODdataRow(){ time = 0; speed = 60; }
		public TODdataRow(double t, double s) { time = t; speed = s; }
		
		public double getTime() {
			return time;
		}
		
		public double getSpeed() {
			return speed;
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
		
		public void setSpeed(double x){
			if (x >= 0.0)
				speed = x;
			return; 
		}
	}


}
