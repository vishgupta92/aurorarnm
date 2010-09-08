/**
 * @(#)SimulationSettingsHWC.java
 */

package aurora.hwc;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import aurora.*;


/**
 * HWC specific simulation settings.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public class SimulationSettingsHWC extends SimulationSettings {
	private static final long serialVersionUID = 6276880264620748221L;
	
	protected Vector<String> vtypes = new Vector<String>();
	protected Vector<Double> weights = new Vector<Double>();
	

	public SimulationSettingsHWC() {
		vtypes.add("General");
		weights.add(1.0);
	}
	
	/**
	 * Return the number of vehicle types.
	 */
	public int countVehicleTypes() {
		return vtypes.size();
	}
	
	/**
	 * Return list of types.
	 */
	public Vector<String> getVehicleTypes() {
		return vtypes;
	}
	
	/**
	 * Return array of vehicle weights.
	 */
	public double[] getVehicleWeights() {
		int sz = weights.size();
		if (sz < 1)
			return null;
		double[] wa = new double[sz];
		for (int i = 0; i < sz; i++)
			wa[i] = weights.get(i);
		return wa;
	}
	
	
	/**
	 * Initializes settings from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = super.initFromDOM(p);
		if (!res)
			return res;
		try {
			for (int i = 0; i < p.getChildNodes().getLength(); i++) {
				if (p.getChildNodes().item(i).getNodeName().equals("VehicleTypes"))
					if (p.getChildNodes().item(i).hasChildNodes()) {
						NodeList pp = p.getChildNodes().item(i).getChildNodes();
						for (int j = 0; j < pp.getLength(); j++)
							if (pp.item(j).getNodeName().equals("vtype")) {
								String tnm = pp.item(j).getAttributes().getNamedItem("name").getNodeValue();
								double tw = Double.parseDouble(pp.item(j).getAttributes().getNamedItem("weight").getNodeValue());
								addVehicleType(tnm, tw);
							}
					}

			}
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		if (countVehicleTypes() > 1)
			removeVehicleType(0);
		return res;
	}
	
	
	/**
	 * Generates XML description of the application settings.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		out.print("<VehicleTypes>\n");
		for (int i = 0; i < vtypes.size(); i++)
			out.print("<vtype name=\"" + vtypes.get(i) + "\" weight=\"" + weights.get(i) + "\" />\n");
		out.print("</VehicleTypes>\n");
		return;
	}
	
	/**
	 * Creates new data header.
	 * @param fpath directory for temporary file.
	 */
	public synchronized boolean createDataHeader() {
		boolean res = super.createDataHeader();
		if (!res)
			return res;
		String vf = "";
		tmpDataOutput.println("Vehicle Type, Weight");
		for (int i = 0; i < vtypes.size(); i++) {
			if (i > 0)
				vf += ":";
			tmpDataOutput.println(vtypes.get(i) + ", " + weights.get(i));
			vf += vtypes.get(i);
		}
		tmpDataOutput.print("\nEntry Format, Value Format, FD Format\nDensity_Value;In-Flow_Value;Out-Flow_Value;FD;Queue_Limit;Weaving_Factor, "
				+ vf + ", Capacity:Critical_Density:Jam_Density\n");
		
		return true;
	}
	
	/**
	 * Creates new temporary data file.
	 * @param fpath directory for temporary file.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean createNewTmpDataFile(File fpath) throws IOException {
		boolean res = super.createNewTmpDataFile(fpath);
		if ((!res) || (tmpDataOutput == null))
			return false;
		res = createDataHeader();
		return res;
	}
	

	/**
	 * Sets vehicle type name at the given position in the list.
	 * @param type vehicle type name.
	 * @param idx index of the position.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setVehicleTypeName(String type, int idx) {
		if ((type == null) || type.isEmpty() || (idx < 0) || (idx >= vtypes.size()))
			return false;
		vtypes.set(idx, type);
		return true;
	}
	
	/**
	 * Sets vehicle type weight at the given position in the list.
	 * @param w vehicle type weight.
	 * @param idx index of the position.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setVehicleTypeWeight(double w, int idx) {
		if ((w < 1.0) || (idx < 0) || (idx >= vtypes.size()))
			return false;
		weights.set(idx, w);
		return true;
	}
	
	
	/**
	 * Adds vehicle type and its weight to the list.
	 * @param type vehicle type.
	 * @param weight vehicle weight.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean addVehicleType(String type, double weight) {
		String buf = type;
		if (buf.isEmpty())
			buf = "General";
		vtypes.add(buf);
		weights.add(Math.max(1.0, weight));
		return true;
	}
	
	/**
	 * Removes vehicle type and its weight from the given position in the list.
	 * @param idx index.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean removeVehicleType(int idx) {
		if ((idx < 0) || (idx >= vtypes.size()) || (vtypes.size() < 2))
			return false;
		vtypes.remove(idx);
		weights.remove(idx);
		return true;
	}
	
	/**
	 * Copies data from the given settings object to the current one.
	 * @param x given settings object.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean copyData(SimulationSettings x) {
		boolean res = super.copyData(x);
		if (!res)
			return !res;
		Vector<String> tps = ((SimulationSettingsHWC)x).getVehicleTypes();
		double[] vw = ((SimulationSettingsHWC)x).getVehicleWeights();
		vtypes.clear();
		weights.clear();
		for (int i = 0; i < tps.size(); i++) {
			vtypes.add(tps.get(i));
			weights.add(vw[i]);
		}
		return res;
	}
	
}
