/**
 * @(#)ControllerSIGNAL.java
 */

package aurora.hwc.control;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import org.w3c.dom.*;
import aurora.*;
import aurora.hwc.*;


/**
 * Implementation of SIGNAL controller.
 * @author Andy Chow
 * @version $Id: ControllerSIGNAL.java,v 1.1.4.1.2.1 2008/12/11 20:42:37 akurzhan Exp $
 */
public final class ControllerSIGNAL extends AbstractControllerHWC {
	private static final long serialVersionUID = 4440581708032401841L;
	
	public boolean Signal = false;		// false - RED; true - GREEN

	public Vector<Double> green = new Vector<Double>(); // green profile (values in hour)
	public Vector<Double> offset = new Vector<Double>(); // offset profile (values in hour)
	public Vector<Double> cycle_time = new Vector<Double>(); // cycle_time profile (values in hour)
	
	public double sim_green;				// Green splits in simulation time steps
	public double sim_offset;				// Offsets in simulation time steps 
	public double sim_cycle_time;			// Cycle time in simulation time steps 
	
	public ControllerSIGNAL() { input = (Double)(-1.0); }
		
	/**
	 * Initializes the SIGNAL controller from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if (p == null)
			return !res;
		try  {
			tp = Double.parseDouble(p.getAttributes().getNamedItem("tp").getNodeValue());	// tp: frequency of plan change
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (int i = 0; i < pp.getLength(); i++) {
					if (pp.item(i).getNodeName().equals("green")) 
						setGreenVector(pp.item(i).getTextContent());
					if (pp.item(i).getNodeName().equals("offset")) 
						setOffsetVector(pp.item(i).getTextContent());				
					if (pp.item(i).getNodeName().equals("cycle_time")) 
						setCycletimeVector(pp.item(i).getTextContent());			
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
	 * Generates XML description of the SIGNAL controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		out.print("<controller class=\"" + this.getClass().getName() + "\" tp=\"" + Double.toString(tp) + "\">");
//		out.print("<parameter name=\"cycle_time\" value=\"" + Double.toString(cycle_time) + "\"/>");
//		out.print("<parameter name=\"green\" value=\"" + Double.toString(green) + "\"/>");
//		out.print("<parameter name=\"offset\" value=\"" + Double.toString(offset) + "\"/>");
		out.print("</controller>");
		return;
	}
	
	/**
	 * Computes controlled input flow for given Signal Node.
	 * @param xx given Signal Node.
	 * @return input flow.
	 */
	public synchronized Object computeInput(AbstractNodeSimple xx) {
		AbstractNodeHWC x = (AbstractNodeHWC)xx;
		Double flw = (Double)super.computeInput(x);		// flw - controlled inflow to the signal node 
		if (flw != null)
			return flw;
		int idx = x.getControllers().indexOf(this);
		AbstractLinkHWC lnk = (AbstractLinkHWC)x.getPredecessors().get(idx);
		if ((Double)input < 0.0)
			input = (Double)lnk.getMaxFlow();
		int ii = 0;		// link index 
		AbstractLinkHWC tlk;
		
		for (int i = 0; i < x.getPredecessors().size(); i++)
			if ((x.getPredecessors().get(i).getType() == TypesHWC.LINK_FREEWAY) ||
				(x.getPredecessors().get(i).getType() == TypesHWC.LINK_HIGHWAY)) {
				ii = i;
				break;
			}
		tlk = (AbstractLinkHWC)x.getPredecessors().get(ii);		// tlk - upstream link 

		
		// TOD signal plan 		
		double time = x.getMyNetwork().getSimTime();
		double sample_time = x.getMyNetwork().getTP();
		
		int idxtime = (int)Math.floor(time/tp);
		
		int n = green.size() - 1; // max index of the green profile, also for offset and cycle time

		if ((idxtime < 0) || (idxtime > n)) 
			idxtime = n;
		
		if (n < 0) { //empty
			sim_green = 0.0;
			sim_cycle_time = 0.0; 
			sim_offset = 0.0;
		}		
		else {
			sim_green = green.get(idxtime)/(3600.0*sample_time);
			sim_offset = offset.get(idxtime)/(3600.0*sample_time);
			sim_cycle_time = cycle_time.get(idxtime)/(3600.0*sample_time);
		}
		
		double c = (x.getTS() - sim_offset)/sim_cycle_time;	// to count number of cycles passed 
		c = Math.max(c, 0.0);
		
		Signal = false;					
		if ((x.getTS() >= sim_offset + sim_cycle_time*Math.floor((Double)c)) && 
			(x.getTS() <= (sim_offset + sim_green) + sim_cycle_time*Math.floor((Double)c))) 	
			Signal = true;		
			
		if (!Signal)		// A RED phase			
			input = 0.0;
		else 				// A GREEN phase 
			input = tlk.getFlow().sum().getUpperBound();  		
		return input;			 			
	}
	
	/**
	 * Returns controller description.
	 */
	public final String getDescription() {
		NumberFormat form = NumberFormat.getInstance();
		form.setMinimumFractionDigits(0);
		form.setMaximumFractionDigits(0);
		String buf = "SIGNAL";// (period = " + Util.time2string(tp) + "; cycle = " + form.format(cycle_time) + "(s)" 
		           // + "; green = " + form.format(green) + "(s)" + "; offset = " + form.format(offset) + "(s)";
		return buf;
	}
	
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Controller.
	 */
	public String toString() {
		return "SIGNAL";
	}

	
	/**
	 * Sets green vector from string.
	 * @param buf text buffer.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setGreenVector(String buf) {
		if (buf == null)
			return false;
		StringTokenizer st = new StringTokenizer(buf, ", \t");
		if (st.hasMoreTokens())
			green.clear();
		while (st.hasMoreTokens())
			green.add(Double.parseDouble(st.nextToken()));
		return true;
	}
	
	
	/**
	 * Sets offset vector from string.
	 * @param buf text buffer.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setOffsetVector(String buf) {
		if (buf == null)
			return false;
		StringTokenizer st = new StringTokenizer(buf, ", \t");
		if (st.hasMoreTokens())
			offset.clear();
		while (st.hasMoreTokens())
			offset.add(Double.parseDouble(st.nextToken()));
		return true;
	}
	
	/**
	 * Sets cycle_time vector from string.
	 * @param buf text buffer.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setCycletimeVector(String buf) {
		if (buf == null)
			return false;
		StringTokenizer st = new StringTokenizer(buf, ", \t");
		if (st.hasMoreTokens())
			cycle_time.clear();
		while (st.hasMoreTokens())
			cycle_time.add(Double.parseDouble(st.nextToken()));
		return true;
	}
	
		
	/**
	 * Returns green vector as text buffer. 
	 * @return buf text buffer.
	 */
	public final String getGreenVectorAsString() {
		String buf = new String();
		int n = green.size();
		if (n < 1)
			buf += "0.0";
		else {
			buf += Double.toString(green.get(0));
			for (int i = 1; i < n; i++)
				buf += ", " + Double.toString(green.get(i));
		}
		return buf;
	}
	
	/**
	 * Returns cycle_time vector as text buffer. 
	 * @return buf text buffer.
	 */
	public final String getCycletimeVectorAsString() {
		String buf = new String();
		int n = cycle_time.size();
		if (n < 1)
			buf += "0.0";
		else {
			buf += Double.toString(cycle_time.get(0));
			for (int i = 1; i < n; i++)
				buf += ", " + Double.toString(cycle_time.get(i));
		}
		return buf;
	}
	
	/**
	 * Returns offset vector as text buffer. 
	 * @return buf text buffer.
	 */
	public final String getOffsetVectorAsString() {
		String buf = new String();
		int n = offset.size();
		if (n < 1)
			buf += "0.0";
		else {
			buf += Double.toString(offset.get(0));
			for (int i = 1; i < n; i++)
				buf += ", " + Double.toString(offset.get(i));
		}
		return buf;
	}

	
	
	
	
}