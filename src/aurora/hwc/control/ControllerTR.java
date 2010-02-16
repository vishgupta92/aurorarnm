/**
 * @(#)ControllerTR.java 
 */

package aurora.hwc.control;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import aurora.*;
import aurora.hwc.*;


/**
 * Traffic responsive controller implementation.
 * @author Gabriel Gomes, Alex Kurzhanskiy
 * @version $Id$
 */
public class ControllerTR extends AbstractControllerSimpleHWC {
	private static final long serialVersionUID = -4901478322713228948L;
	
	// thresholds
	private Vector<Double> t_dns = new Vector<Double>(); // density
	private Vector<Double> t_flw = new Vector<Double>(); // flow
	private Vector<Double> t_spd = new Vector<Double>(); // speed
	
	private Vector<Double> rate = new Vector<Double>();

	
	/**
	 * Returns controller description.
	 */
	public String getDescription() {
		return "Traffic Responsive";
	}
	
	/**
	 * Returns letter code of the controller type.
	 */
	public final String getTypeLetterCode() {
		return "TR";
	}
	
	/**
	 * Returns mask for compatible Node types.
	 */
	public final int getCompatibleNodeTypes() {
		return (((~TypesHWC.MASK_NODE) & TypesHWC.NODE_FREEWAY) | ((~TypesHWC.MASK_NODE) & TypesHWC.NODE_HIGHWAY));
	}
	
	/**
	 * Initializes the traffic responsive controller from given DOM structure.
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
					if (pp.item(i).getNodeName().equals("level")) {
						int id = Integer.parseInt(pp.item(i).getAttributes().getNamedItem("id").getNodeValue());
						double density = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("density").getNodeValue());
						double flow = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("flow").getNodeValue());
						double speed = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("speed").getNodeValue());
						double rate = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("rate").getNodeValue());
						insertEntry(id, density, flow, speed, rate);
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
	 * Generates XML description of the TOD controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		for (int i = 0; i < t_dns.size(); i++)
			out.println("<level id=\"" + i + "\" density=\"" + t_dns.get(i) + "\" flow=\"" + t_flw.get(i) + "\" speed=\"" + t_spd.get(i) + "\" rate=\"" + rate.get(i) + "\" />");
		out.print("</controller>\n");
		return;
	}

	/**
	 * Computes desired input flow for given Node.
	 * @param xx given Node.
	 * @return input flow.
	 */
	public synchronized Object computeInput(AbstractNodeSimple xx) {
		Double flw = (Double)super.computeInput(xx);
		if (flw != null)
			return flw;
		if (input == null)
			input = (Double)limits.get(1);
		
		double MLocc,MLflw,MLspd;
		if(this.usesensors){
			MLocc = mlsensor.Occupancy();
			MLflw = mlsensor.Flow() / mlup.getLanes();
			MLspd = mlsensor.Speed();
		}
		else{
			MLocc = mlup.getOccupancy();
			MLflw = mlup.getFlow().sum().getCenter() / mlup.getLanes();
			MLspd = mlup.getSpeed().getCenter();
		}
	    int ii =  findFirstGreaterThan(URMStable.t_occ,MLocc);
	    int jj =  findFirstGreaterThan(URMStable.t_flw,MLflw);
	    int kk =  findFirstLessThan(URMStable.t_spd,MLspd);
	    Double TRrate = URMStable.meteringrate.get( Math.max( Math.max(ii,jj) , kk ) );
		flw = ApplyURMS(TRrate);
		flw = ApplyQueueControl(flw);
		input = ApplyLimits(flw);
		return input;

	}
	
	private int findFirstLessThan(Vector<Float> v,double x){
		if(URMStable.meteringrate.get(0).isNaN())
			return -1;
		for(int j=1;j<v.size();j++){
			if( v.get(j)<x)
				return j;
		}
		return v.size()-1;
	}

	private int findFirstGreaterThan(Vector<Float> v,double x){
		if(URMStable.meteringrate.get(0).isNaN())
			return -1;
		for(int j=1;j<v.size();j++){
			if( v.get(j)>x)
				return j;
		}
		return v.size()-1;
		
	}

	/**
	 * Returns vector of density thresholds.
	 */
	public Vector<Double> getDensityThresholds() {
		return t_dns;
	}
	
	/**
	 * Returns vector of flow thresholds.
	 */
	public Vector<Double> getFlowThresholds() {
		return t_flw;
	}
	
	/**
	 * Returns vector of speed thresholds.
	 */
	public Vector<Double> getSpeedThresholds() {
		return t_spd;
	}
	
	/**
	 * Returns vector of rates.
	 */
	public Vector<Double> getRates() {
		return rate;
	}
	
	/**
	 * Inserts entry into the threshold table.
	 * @param id level ID.
	 * @param dns density.
	 * @param flw flow.
	 * @param spd speed.
	 * @param rt controller flow rate.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean insertEntry(int id, double dns, double flw, double spd, double rt) {
		if ((id < 0) || (dns < 0) || (flw < 0) || (spd < 0) || (rt < 0))
			return false;
		int idx = t_dns.size();
		if (id < idx)
			idx = id;
		t_dns.insertElementAt(dns, idx);
		t_flw.insertElementAt(flw, idx);
		t_spd.insertElementAt(spd, idx);
		rate.insertElementAt(rt, idx);
		return true;
	}
	
	/**
	 * Delete entry from the threshold table.
	 * @param id level ID.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean deleteEntry(int id) {
		if ((id < 0) || (id >= t_dns.size()))
			return false;
		t_dns.remove(id);
		t_flw.remove(id);
		t_spd.remove(id);
		rate.remove(id);
		return true;
	}
	
	/**
	 * Sets all the threshold data.
	 * @param dns vector of density thresholds.
	 * @param flw vector of flow thresholds.
	 * @param spd vector of speed thresholds.
	 * @param rt vector of controller flow rates.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setData(Vector<Double> dns, Vector<Double> flw, Vector<Double> spd, Vector<Double> rt) {
		if ((dns == null) || (flw == null) || (spd == null) || (rt == null) ||
			(dns.size() != flw.size()) || (dns.size() != spd.size()) || (dns.size() != rt.size()))
			return false;
		t_dns = dns;
		t_flw = flw;
		t_spd = spd;
		rate = rt;
		return true;
	}
	
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Controller.
	 */
	public String toString() {
		return "Traffic Responsive";
	}	
	
}
