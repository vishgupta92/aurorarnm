/**
 * @(#)ControllerTOD.java 
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
 * @author Gabriel Gomes
 * @version $Id: ControllerTOD.java,v 1.1.4.1.2.2 2009/01/14 01:46:25 akurzhan Exp $
 */
public class ControllerTOD extends AbstractControllerHWC {
	private static final long serialVersionUID = 4741791843202408263L;

	private Vector<TODdataRow> todTable = new Vector<TODdataRow>();
	
	
	/**
	 * Returns controller description.
	 */
	public String getDescription() {
		return "TOD (" + todTable.size() + " entries)";
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
	 * Initializes the TOD controller from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if (p == null)
			return !res;
		try  {
			tp = Double.parseDouble(p.getAttributes().getNamedItem("tp").getNodeValue());
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (int i = 0; i < pp.getLength(); i++) {
					if (pp.item(i).getNodeName().equals("todrate")) {
						double time = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("time").getNodeValue());
						double rate = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("rate").getNodeValue());
						boolean done = false;
						for (int j = 0; j < todTable.size(); j++){
							if(todTable.get(j).getTime() > time){
								todTable.insertElementAt(new TODdataRow(time, rate), j);
								done = true;
								break;
							}	
						}
						if(!done)
							todTable.add(new TODdataRow(time, rate));
					}
					if (pp.item(i).getNodeName().equals("limits")) {
						limits = new Vector<Object>();
						limits.add(Double.parseDouble(pp.item(i).getAttributes().getNamedItem("cmin").getNodeValue()));
						limits.add(Double.parseDouble(pp.item(i).getAttributes().getNamedItem("cmax").getNodeValue()));
					}
					if (pp.item(i).getNodeName().equals("qcontroller")) {
						Class c = Class.forName(pp.item(i).getAttributes().getNamedItem("class").getNodeValue());
						myQController = (QueueController)c.newInstance();
						res &= myQController.initFromDOM(pp.item(i));
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
	 * Generates XML description of the ALINEA controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		for (int i = 0; i < todTable.size(); i++)
			out.print("<todrate time=\"" + todTable.get(i).getTime() + "\" rate=\"" + todTable.get(i).getRate() + "\"/>");
		out.print("</controller>");
		return;
	}

	/**
	 * Computes desired input flow for given Node.
	 * @param xx given Node.
	 * @return input flow.
	 */
	public synchronized Object computeInput(AbstractNodeSimple xx) {
		Double flw = (Double)super.computeInput(xx);		// flw - controlled inflow to the signal node 
		if (flw != null)
			return flw;
		int idx = xx.getControllers().indexOf(this);
		AbstractLinkHWC lnk = (AbstractLinkHWC)xx.getPredecessors().get(idx);
		double time = ((AbstractNodeHWC)xx).getMyNetwork().getSimTime();
		if ((todTable.isEmpty()) || (time < todTable.get(0).getTime()))
			flw = (Double)limits.get(1);
		else
			for (int i = 0; i < todTable.size(); i++)
				if (time >= todTable.get(i).getTime())
					flw = todTable.get(i).getRate();
		if ((((NodeHWCNetwork)xx.getMyNetwork()).hasQControl()) && (myQController != null)) 
			flw = Math.max(flw, (Double)myQController.computeInput((AbstractNodeHWC)xx, lnk));
		flw = Math.min((Double)limits.get(1), Math.max(flw, (Double)limits.get(0)));
		flw = Math.max(flw, 0.0);
		input = new Double(flw);
		return flw;
	}
	
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Controller.
	 */
	public String toString() {
		return "TOD";
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
		private double rate;
		
		public TODdataRow(){ time = 0; rate = 0; }
		public TODdataRow(double t, double r) { time = t; rate = r; }
		
		public double getTime() {
			return time;
		}
		
		public double getRate() {
			return rate;
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
		
		public void setRate(double x){
			if (x >= 0.0)
				rate = x;
			return; 
		}
	}


}



