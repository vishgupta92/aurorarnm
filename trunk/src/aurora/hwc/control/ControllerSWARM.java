/**
 * @(#)ControllerSWARM.java
 */

package aurora.hwc.control;

import java.io.*;
import java.util.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import aurora.*;
import aurora.hwc.*;


/**
 * Implementation of SWARM controller. 
 * @author Gabriel Gomes
 * @version $Id: ControllerSWARM.java,v 1.1.4.14 2009/10/09 23:05:21 akurzhan Exp $
 */
public class ControllerSWARM extends AbstractControllerComplexHWC {

	private static final long serialVersionUID = -7135452153102929923L;

	// Zone information
	public int numZones;
	public Vector<Zone> zones = new Vector<Zone>();
	
	// User parmaeters
	public SWARMParameters P = new SWARMParameters();

	// static in original code
	public int pollingnumber; 			// [-] (SWARM1)

	// outputs
	public double[][] swarm1rate;		// [veh/hour]
	public double[][] swarm2brate; 		// [veh/hour]
	public double[][] swarm2arate; 		// [veh/hour]

	// CONSTRUCTORS ========================================================================
	public ControllerSWARM() {
		super();
		limits.add(new Double(0));
		limits.add(new Double(99999.99));
	}

	// XMLREAD, VALIDATE, INITIALIZE, XMLDUMP ==============================================

	/**
	 * Initializes SWARM controller from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = super.initFromDOM(p);
		if (!res)
			return res;

		int i, j;
		Node n = null;


		try {
			NodeList pp = p.getChildNodes();
			for (i = 0; i < pp.getLength(); i++) {
				if (pp.item(i).getNodeName().equals("components")) {
					if ((n = pp.item(i).getAttributes().getNamedItem("swarm1")) != null)
						P.SWARM1 = Boolean.parseBoolean(n.getNodeValue());
					if ((n = pp.item(i).getAttributes().getNamedItem("swarm2a")) != null)
						P.SWARM2A = Boolean.parseBoolean(n.getNodeValue());
					if ((n = pp.item(i).getAttributes().getNamedItem("swarm2b")) != null)
						P.SWARM2B = Boolean.parseBoolean(n.getNodeValue());
				}
				if (pp.item(i).getNodeName().equals("parameters")) {
					if ((n = pp.item(i).getAttributes().getNamedItem("density_sample_size")) != null)
						P.SWARM_DENSITY_SAMPLE_SIZE = Integer.parseInt(n.getNodeValue());
					if ((n = pp.item(i).getAttributes().getNamedItem("slope_sample_size")) != null)
						P.SWARM_SLOPE_SAMPLE_SIZE = Integer.parseInt(n.getNodeValue());
					if ((n = pp.item(i).getAttributes().getNamedItem("input_var_lane")) != null)
						P.input_var_lane = Double.parseDouble(n.getNodeValue());
					if ((n = pp.item(i).getAttributes().getNamedItem("meas_var_lane")) != null)
						P.meas_var_lane = Double.parseDouble(n.getNodeValue());
					if ((n = pp.item(i).getAttributes().getNamedItem("phi")) != null)
						P.swarm_phi = Double.parseDouble(n.getNodeValue());
					if ((n = pp.item(i).getAttributes().getNamedItem("psi")) != null)
						P.swarm_psi = Double.parseDouble(n.getNodeValue());
					if ((n = pp.item(i).getAttributes().getNamedItem("sat_den_multiplier")) != null)
						P.SWARM_SAT_DEN_NUMBER = Integer.parseInt(n.getNodeValue());
					if ((n = pp.item(i).getAttributes().getNamedItem("forecast_lead_time")) != null)
						P.SWARM_FORECAST_LEAD_TIME = Integer.parseInt(n.getNodeValue());
					if ((n = pp.item(i).getAttributes().getNamedItem("epsilon")) != null)
						P.epsilon = Double.parseDouble(n.getNodeValue());
					if ((n = pp.item(i).getAttributes().getNamedItem("sat_smoother")) != null)
						P.sat_smoother = Double.parseDouble(n.getNodeValue());
				}
				if (pp.item(i).getNodeName().equals("zones")) {

					NodeList pp2 = pp.item(i).getChildNodes();
					Vector<Zone> vz = new Vector<Zone>();
					for (j = 0; j < pp2.getLength(); j++) {
						if (pp2.item(j).getNodeName().equals("zone")) {
							Zone z = new Zone();
							if ((n = pp2.item(j).getAttributes().getNamedItem("bottlenecklink")) != null)
								z.bottleneck = (AbstractLinkHWC) myMonitor.getMyNetwork().getLinkById(Integer.parseInt(n.getNodeValue()));
							if ((n = pp2.item(j).getAttributes().getNamedItem("fromonramp")) != null)
								z.setFromOnramp(myMonitor.getMyNetwork().getLinkById(Integer.parseInt(n.getNodeValue())));
							if ((n = pp2.item(j).getAttributes().getNamedItem("toonramp")) != null)
								z.setToOnramp(myMonitor.getMyNetwork().getLinkById(Integer.parseInt(n.getNodeValue())));
							if ((n = pp2.item(j).getAttributes().getNamedItem("sat_den_multiplier")) != null)
								z.sat_den_multiplier = Double.parseDouble(n.getNodeValue());
							vz.add(z);
						}
					}
					zones = vz;
				}
			}
		} catch (Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}

		return res;
	}

	/**
	 * Validates SWARM configuration.<br>
	 * Checks if dynamics is assigned.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean validate() throws ExceptionConfiguration {
		boolean res = super.validate();
		if (!res)
			return false;
		// additional validation here.
		return true;
	}
	
	/**
	 * Additional initialization.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initialize() throws ExceptionConfiguration {
		boolean res = super.initialize();
		if (!res)
			return false;
		Vector<AbstractNetworkElement> nes = myMonitor.getSuccessors();
		for (int j = 0; j < nes.size(); j++) {
			if ((nes.get(j).getType() & TypesHWC.MASK_LINK) == 0)
				continue;
			AbstractLink lnk = (AbstractLink)nes.get(j);
			if (initialized) {
				ControllerSlave ctrl = new ControllerSlave();
				AbstractNodeSimple nd = (AbstractNodeSimple)lnk.getEndNode();
				if ((nd.getSimpleController(lnk) != null) && (nd.getSimpleController(lnk).getClass().getName().equals(ctrl.getClass().getName()))) {
					ctrl = (ControllerSlave)nd.getSimpleController(lnk);
				}
				else {
					if (nd.getSimpleController(lnk) != null)
						nd.getSimpleController(lnk).setDependent(false);
					res &= nd.setSimpleController(ctrl, lnk);
				}
				ctrl.setMyLink(lnk);
				ctrl.setMyComplexController(this);
				addDependentController(ctrl, new Double(0));
			}
		}
		if (!initialized)
			initialized = true;
		numZones = zones.size();
		P.swarm_vds_sat_den_multiplier.clear();
		for (int i = 0; i < numZones; i++) {
			Zone Z = zones.get(i);
			P.swarm_vds_sat_den_multiplier.add(Z.sat_den_multiplier);
		}
		for (int i = 0; i < numZones; i++) {
			Zone Z = zones.get(i);
			Z.initialize();
		}
		// Allocate outputs
		swarm1rate = new double[numZones][];
		swarm2arate = new double[numZones][];
		swarm2brate = new double[numZones][];
		// controloutput = new double[numZones][];
		for (int i = 0; i < numZones; i++) {
			Zone Z = zones.get(i);
			swarm1rate[i] = new double[Z.numOR];
			swarm2arate[i] = new double[Z.numOR];
			swarm2brate[i] = new double[Z.numOR];
			// controloutput[i] = new double[numOR[i]];
		}
		pollingnumber = 0;
		return res;
	}

	/**
	 * Generates XML description of the SWARM controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		out.print("<components swarm1=\"" + P.SWARM1 + "\" swarm2a=\"" + P.SWARM2A + "\" swarm2b=\"" + P.SWARM2B + "\"/>\n");
		out.print("<parameters density_sample_size=\"" + P.SWARM_DENSITY_SAMPLE_SIZE + "\" epsilon=\"" + P.epsilon + "\" forecast_lead_time=\"" + P.SWARM_FORECAST_LEAD_TIME + "\" input_var_lane=\""
				+ P.input_var_lane + "\" meas_var_lane=\"" + P.meas_var_lane + "\" phi=\"" + P.swarm_phi + "\" psi=\"" + P.swarm_psi + "\" sat_den_multiplier=\"" + P.SWARM_SAT_DEN_NUMBER + "\" sat_smoother=\""
				+ P.sat_smoother + "\" slope_sample_size=\"" + P.SWARM_SLOPE_SAMPLE_SIZE + "\"/>\n");
		out.print("<limits cmax=\"" + limits.get(1) + "\" cmin=\"" + limits.get(0) + "\"/>\n");
		out.print("<zones>\n");
		for (int i = 0; i < zones.size(); i++) {
			out.print("<zone bottlenecklink=\"" + zones.get(i).getBottleneckLink().getId() + "\" ");
			out.print("fromonramp=\"" + zones.get(i).getFromOnramp().getId() + "\" toonramp=\"" + zones.get(i).getToOnramp().getId() + "\" sat_den_multiplier=\"" + zones.get(i).getSaturationDensityMultiplier() + "\"/>\n");
		}
		out.print("</zones>\n");
		out.print("</controller>\n");
		return;
	}	
	
	// MAIN FUNCTION =======================================================================
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {

		int i, j;

		if (!super.dataUpdate(ts))
			return true;

		// Update measurements ....................................
		for(i=0;i<numZones;i++)
			zones.get(i).Data.UpdateMeasurements();

		// Compute saturation density .............................
		if (P.SWARM1 || P.SWARM2B) {
			for(i=0;i<numZones;i++)
				zones.get(i).Data.swarm_saturation_density();
		}

		// Run SWARM 1 ............................................
		if (P.SWARM1)
			swarm1();
		else
			SetToMax(swarm1rate);
		if (P.SWARM2A)
			swarm2a();
		else
			SetToMax(swarm2arate);
		if (P.SWARM2B)
			swarm2b();
		else
			SetToMax(swarm2brate);
		for (i = 0; i < numZones; i++){
			Zone Z = zones.get(i);
			for (j = 0; j < Z.numOR; j++)
				setControlInput(Z.controlindex[j], new Double(Math.min(Math.min(swarm2arate[i][j], swarm2brate[i][j]), swarm1rate[i][j])));
		}
		
		return true;
	}

	// SUBROUTINES =========================================================================

	private void swarm1() {
		swarm_kalman_filter();
		swarm_apportion();
	}



/*	private void new_kalman_filter( long polling_number )
	{
	   int i, j, k, l, vds_id, vds_index;
	   double observation_station_density;
	   double sumt, sumd, sumtd, sumt2, b, temp;
	   int pn;
	   for ( k = 0; k < numZones; k++ ) // k is the bottleneck index. 
	   {
	      // where is my bottleneck vds on my VDS list 
	      vds_index = SW_BOTTLENECK[k].vds_index;  
	      if ( VDS[vds_index].vds_status >=  PCT_GOOD_LANES && VDS[vds_index].occupancy > 0.0f )
	      {
	         pn = SW_BOTTLENECK[k].pn;
	         SW_BOTTLENECK[k].bottleneck_status = 1.0;
	         if( pn < DENSITY_SAMPLE_SIZE ) // pn start from 0. This block is used for first collectable DENSITY_SAMPLE_SIZE polls 
	         {
	             // LocalDensity(k, pn) = NormStationDensity(k, pn ) 
	             SW_BOTTLENECK[k].initial_density[pn] = VDS[vds_index].norm_station_density;
	             // sumd(k) = sumd(k) + NormStationDensity(k, pn ) 
	             SW_BOTTLENECK[k].sumd = SW_BOTTLENECK[k].sumd +
	                                     VDS[vds_index].norm_station_density;
	             // sumd2(k) = sumd2(k) + NormStationDensity(k, pn )^2 
	             SW_BOTTLENECK[k].sumd2 = SW_BOTTLENECK[k].sumd2 +
	                        pow (VDS[vds_index].norm_station_density, 2.0);
	             // RequiredDensity(k) = SaturationDensity(k) * SatDensityMultiplier(k) 
	             SW_BOTTLENECK[k].required_density = VDS[vds_index].saturation_density *
	                                                 VDS[vds_index].sat_density_multiplier;
	             SW_BOTTLENECK[k].forecast_station_density = 0; // not yet 
	         }
	         else // This block is used most of the time 
	         {  
	             // sumd(k) = sumd(k) - LocalDensity(k, 1) + NormStationDensity(k, pn ) 
	             SW_BOTTLENECK[k].sumd = SW_BOTTLENECK[k].sumd -
	                                     SW_BOTTLENECK[k].initial_density[0] +
	                                     VDS[vds_index].norm_station_density;
	             // sumd2(k) = sumd2(k) - LocalDensity(k, 1)^2 + NormStationDensity(k, pn )^2  
	             SW_BOTTLENECK[k].sumd2 = SW_BOTTLENECK[k].sumd2 -
	                                      pow(SW_BOTTLENECK[k].initial_density[0], 2.0) +
	                                      pow(VDS[vds_index].norm_station_density, 2.0);
	             // update LocalDensity. From 0 to (DENSITY_SAMPLE_SIZE-1) 
	             // remove the oldest record, and get the newest record in 
	             for(i = 0; i < DENSITY_SAMPLE_SIZE - 1; i++)
	             {
	                 SW_BOTTLENECK[k].initial_density[i] = SW_BOTTLENECK[k].initial_density[i+1];
	             }
	             SW_BOTTLENECK[k].initial_density[DENSITY_SAMPLE_SIZE-1] = 
	                                      VDS[vds_index].norm_station_density; 
	             // initial Kalman filter estimates 
	             SW_BOTTLENECK[k].post_mean_density = SW_BOTTLENECK[k].sumd / DENSITY_SAMPLE_SIZE;
	             SW_BOTTLENECK[k].post_variance_density = 
	                ( DENSITY_SAMPLE_SIZE*SW_BOTTLENECK[k].sumd2 - pow(SW_BOTTLENECK[k].sumd,2.0 ) /
	                                               ( DENSITY_SAMPLE_SIZE*(DENSITY_SAMPLE_SIZE-1) ) );
	             // Here we use actual values of LocalDensity to feed the filter 
	             for(i = SLOPE_SAMPLE_SIZE; i <= DENSITY_SAMPLE_SIZE; i++)
	             {
	                 sumt = 0; sumt2 = 0; sumd = 0; sumtd = 0;
	                 for(j = i-SLOPE_SAMPLE_SIZE+1; j <= i ; j++)
	                 {
	                    sumt = sumt + j;
	                    sumt2 = sumt2 + j*j;
	                    sumd = sumd + SW_BOTTLENECK[k].initial_density[j-1]; // start from 0 
	                    sumtd = sumtd + j*SW_BOTTLENECK[k].initial_density[j-1];
	                 }
	                 b = (SLOPE_SAMPLE_SIZE*sumtd - sumt*sumd) / (SLOPE_SAMPLE_SIZE*sumt2 - sumt*sumt);
	                 SW_BOTTLENECK[k].pre_mean_density = 
	                          SW_BOTTLENECK[k].post_mean_density*MODEL_PARA_A + b*MODEL_PARA_B;
	                 SW_BOTTLENECK[k].pre_variance_density = pow(MODEL_PARA_A, 2.0)*SW_BOTTLENECK[k].post_variance_density +
	                                                         pow(MODEL_PARA_G, 2.0)*MODEL_PARA_Q;
	                 SW_BOTTLENECK[k].post_variance_density = 1 / ( (1/SW_BOTTLENECK[k].pre_variance_density) +
	                                                                (pow(MODEL_PARA_H, 2.0)/MODEL_PARA_R) );
	                 SW_BOTTLENECK[k].post_mean_density = SW_BOTTLENECK[k].pre_mean_density +
	                               SW_BOTTLENECK[k].post_variance_density*(MODEL_PARA_H/MODEL_PARA_R)*
	                 (SW_BOTTLENECK[k].initial_density[i-1] - MODEL_PARA_H*SW_BOTTLENECK[k].pre_mean_density);
	             }
	             // Here we feed the filter with its previous results 
	             for(i = (DENSITY_SAMPLE_SIZE+1); i <= (DENSITY_SAMPLE_SIZE + FORECAST_LEAD_TIME); i++)
	             {
	                 SW_BOTTLENECK[k].initial_density[i-1] = SW_BOTTLENECK[k].pre_mean_density;
	                 sumt = 0; sumt2 = 0; sumd = 0; sumtd = 0;   
	                 for(j = i - SLOPE_SAMPLE_SIZE + 1 ; j <= i; j++)
	                 {
	                    sumt = sumt + j;
	                    sumt2 = sumt2 + j*j;
	                    sumd = sumd + SW_BOTTLENECK[k].initial_density[j-1];
	                    sumtd = sumtd + j*SW_BOTTLENECK[k].initial_density[j-1];
	                 }
	                 b = (SLOPE_SAMPLE_SIZE*sumtd - sumt*sumd) / (SLOPE_SAMPLE_SIZE*sumt2 - sumt*sumt);
	                 SW_BOTTLENECK[k].pre_mean_density = 
	                          SW_BOTTLENECK[k].post_mean_density*MODEL_PARA_A + b*MODEL_PARA_B;
	                 SW_BOTTLENECK[k].pre_variance_density = pow(MODEL_PARA_A, 2.0)*SW_BOTTLENECK[k].post_variance_density +
	                                                         pow(MODEL_PARA_G, 2.0)*MODEL_PARA_Q;
	                 SW_BOTTLENECK[k].post_variance_density = 1 / ( (1/SW_BOTTLENECK[k].pre_variance_density) +
	                                                                (pow(MODEL_PARA_H, 2.0)/MODEL_PARA_R) );
	                 SW_BOTTLENECK[k].post_mean_density = SW_BOTTLENECK[k].pre_mean_density +
	                               SW_BOTTLENECK[k].post_variance_density*(MODEL_PARA_H/MODEL_PARA_R)*
	                 (SW_BOTTLENECK[k].initial_density[i-1] - MODEL_PARA_H*SW_BOTTLENECK[k].pre_mean_density);
	             }           
	             SW_BOTTLENECK[k].forecast_station_density = SW_BOTTLENECK[k].pre_mean_density;
	             if(SW_BOTTLENECK[k].forecast_station_density > VDS[vds_index].saturation_density *
	                                VDS[vds_index].sat_density_multiplier )
	             {
	                 SW_BOTTLENECK[k].required_density = VDS[vds_index].norm_station_density -
	                             ( SW_BOTTLENECK[k].forecast_station_density - 
	                               (VDS[vds_index].saturation_density * VDS[vds_index].sat_density_multiplier) )  / FORECAST_LEAD_TIME;            
	             }
	             else
	             {
	                 SW_BOTTLENECK[k].required_density = VDS[vds_index].saturation_density * 
	                                                     VDS[vds_index].sat_density_multiplier; 
	             } 
	         } // end ifelse (pn < DENSITY_SAMPLE_SIZE) 
	         if( SW_BOTTLENECK[k].required_density > VDS[vds_index].saturation_density )
	            SW_BOTTLENECK[k].required_density = VDS[vds_index].saturation_density;
	         else if( SW_BOTTLENECK[k].required_density < VDS[vds_index].saturation_density / 2)
	            SW_BOTTLENECK[k].required_density = VDS[vds_index].saturation_density / 2;
	         VDS[vds_index].required_density = SW_BOTTLENECK[k].required_density;
	         if (SW_BOTTLENECK[k].forecast_station_density < 0 )
	            VDS[vds_index].forecast_station_density = 0;
	         else
	            VDS[vds_index].forecast_station_density = SW_BOTTLENECK[k].forecast_station_density;
	         if( pn < DENSITY_SAMPLE_SIZE ) 
	         {
	            SW_BOTTLENECK[k].pn++; // we don't need it counts more than that 
	         }
	      }
	      else
	      {
	         // bottleneck failed (cannot recovery from upstream vds), freeze data ??? 
	         VDS[vds_index].required_density = VDS[vds_index].saturation_density *
	                                           VDS[vds_index].sat_density_multiplier;
	         if( VDS[vds_index].required_density > VDS[vds_index].saturation_density )
	            VDS[vds_index].required_density = VDS[vds_index].saturation_density;
	         else if( VDS[vds_index].required_density < VDS[vds_index].saturation_density / 2)
	            VDS[vds_index].required_density = VDS[vds_index].saturation_density / 2;
	         VDS[vds_index].forecast_station_density = 0;
	         SW_BOTTLENECK[k].bottleneck_status = -1.0;
	         SW_BOTTLENECK[k].pn = 0;  restart collecting sample 
	         SW_BOTTLENECK[k].sumd = 0;
	         SW_BOTTLENECK[k].sumd2 = 0;
	         SW_BOTTLENECK[k].initial_sumd = 0;
	         SW_BOTTLENECK[k].initial_sumd2 = 0;
	         pn = SW_BOTTLENECK[k].pn;
	      }
	      
	   } // next bottleneck
	}
*/
	
	
	private void swarm_kalman_filter() {
		int i, j, k;
		double bndensity, sat_density;
		double temp, sumd, sumd2;
		double input_variance, meas_variance;

		double[][] swarm_densities; 						// [veh/mile/lane] (SWARM1)
		double[][] initial_slope; 						// [???] (SWARM1)
		double[] slope; 									// [???] (SWARM1)
		double[] forecast_slope; 						// [???] (SWARM1)
		double[] density_pre; 							// [???] (SWARM1)
		double[] density_post; 							// [???] (SWARM1)
		double[] variance_pre; 							// [???] (SWARM1)
		double[] variance_post; 							// [???] (SWARM1)
		double[] density_forecast_post; 					// [veh/mile/lane] (SWARM1)
		double[] density_forecast; 						// [???] (SWARM1)
		
		
		swarm_densities = new double[numZones][P.SWARM_DENSITY_SAMPLE_SIZE];
		initial_slope = new double[numZones][P.SWARM_DENSITY_SAMPLE_SIZE];
		slope = new double[numZones];
		forecast_slope = new double[numZones];
		density_pre = new double[numZones];
		density_post = new double[numZones];
		variance_pre = new double[numZones];
		variance_post = new double[numZones];
		density_forecast_post = new double[numZones];
		density_forecast = new double[numZones];
		
		
		pollingnumber++;

		for (i = 0; i < numZones; i++) {

			Zone Z = zones.get(i);
			bndensity = Z.Data.bndent;
			sat_density = Z.bn_saturation_density;
			input_variance = P.input_var_lane * Z.bnlanenum * Z.bnlanenum;
			meas_variance = P.meas_var_lane * Z.bnlanenum * Z.bnlanenum;

			temp = swarm_densities[i][0];
			for (j = 1; j < P.SWARM_DENSITY_SAMPLE_SIZE; j++)
				swarm_densities[i][j-1] = swarm_densities[i][j] - temp;
			swarm_densities[i][j-1] = swarm_densities[i][j-2] + bndensity;

			temp = Z.swarm_slope_densities[0];
			for (j = 1; j < P.SWARM_SLOPE_SAMPLE_SIZE; j++)
				Z.swarm_slope_densities[j - 1] = Z.swarm_slope_densities[j] - temp;
			Z.swarm_slope_densities[j - 1] = Z.swarm_slope_densities[j - 2] + bndensity;

			if (pollingnumber < P.SWARM_DENSITY_SAMPLE_SIZE)
				initial_slope[i][pollingnumber] = swarm_density_slope(i);

			if (pollingnumber == P.SWARM_DENSITY_SAMPLE_SIZE) {
				sumd = 0.;
				sumd2 = 0.;
				for (j = 0; j < P.SWARM_DENSITY_SAMPLE_SIZE; j++) {
					sumd += swarm_densities[i][j];
					sumd2 += swarm_densities[i][j] * swarm_densities[i][j];
				}
				density_post[i] = sumd / (double) P.SWARM_DENSITY_SAMPLE_SIZE;
				variance_post[i] = (sumd2 - 2 * sumd * density_post[i] + density_post[i] * density_post[i]) / (double) P.SWARM_DENSITY_SAMPLE_SIZE;
				slope[i] = swarm_density_slope(i);
				for (j = P.SWARM_DENSITY_SAMPLE_SIZE / 2 + 1; j < P.SWARM_DENSITY_SAMPLE_SIZE; j++) {
					density_pre[i] = density_post[i] + initial_slope[i][j-1];
					variance_pre[i] = variance_post[i] + input_variance;
					variance_post[i] = 1. / (1. / variance_pre[i] + 1. / meas_variance);
					density_post[i] = density_pre[i] + variance_post[i] / meas_variance * (swarm_densities[i][P.SWARM_SLOPE_SAMPLE_SIZE - 1] - density_pre[i]);
				}
			}

			if (pollingnumber > P.SWARM_DENSITY_SAMPLE_SIZE) {
				density_pre[i] = density_post[i] + slope[i];
				variance_pre[i] = variance_post[i] + input_variance;
				variance_post[i] = 1. / (1. / variance_pre[i] + 1. / meas_variance);
				density_post[i] = density_pre[i] + variance_post[i] / meas_variance * (Z.swarm_slope_densities[P.SWARM_SLOPE_SAMPLE_SIZE - 1] - density_pre[i]);
				slope[i] = swarm_density_slope(i);
				for (j = 0; j < P.SWARM_SLOPE_SAMPLE_SIZE; j++)
					Z.swarm_slope_forecast_densities[j] = Z.swarm_slope_densities[j];
				density_forecast_post[i] = density_post[i];
				forecast_slope[i] = slope[i];
				for (k = 0; k < P.SWARM_FORECAST_LEAD_TIME; k++) {
					temp = density_forecast_post[i];
					density_forecast_post[i] = density_forecast_post[i] + forecast_slope[i];
					for (j = 1; j < P.SWARM_SLOPE_SAMPLE_SIZE; j++)
						Z.swarm_slope_densities[j-1] = Z.swarm_slope_densities[j];
					Z.swarm_slope_densities[j-1] = density_forecast_post[i];
					forecast_slope[i] = swarm_density_forecast_slope(i);
				}
				density_forecast[i] = density_forecast_post[i] - temp;
				if ((temp = (density_forecast[i] - sat_density * P.swarm_vds_sat_den_multiplier.get(i))) > 0)
					Z.swarm_bn_required_density = bndensity - temp / (double) P.SWARM_FORECAST_LEAD_TIME;
				else
					Z.swarm_bn_required_density = density_forecast[i] - temp;
				if (bndensity > sat_density)
					Z.swarm_bn_required_density = density_forecast[i] - temp;
			}
		}

	}

	private double swarm_density_slope(int bottleneck_id) {
		double sumt, sumd, sumtd, sumt2, xslope;
		int i, n;

		n = P.SWARM_SLOPE_SAMPLE_SIZE;

		sumt = 0.;
		sumd = 0.;
		sumtd = 0.;
		sumt2 = 0.;

		for (i = 0; i < n; i++) {
			sumt += (i + 1);
			sumd += zones.get(bottleneck_id).swarm_slope_densities[i];
			sumtd += (i + 1) * zones.get(bottleneck_id).swarm_slope_densities[i];
			sumt2 += (i + 1) * (i + 1);
		}

		xslope = (sumtd * n - sumt * sumd) / (sumt2 * n - sumt * sumt);
		return (xslope);

	}

	private double swarm_density_forecast_slope(int bottleneck_id) {
		double sumt, sumd, sumtd, sumt2, xslope;
		int i, n;
		n = P.SWARM_SLOPE_SAMPLE_SIZE;
		sumt = 0.;
		sumd = 0.;
		sumtd = 0.;
		sumt2 = 0.;
		for (i = 0; i < n; i++) {
			sumt += (i + 1);
			sumd += zones.get(bottleneck_id).swarm_slope_forecast_densities[i];
			sumtd += (i + 1) * zones.get(bottleneck_id).swarm_slope_forecast_densities[i];
			sumt2 += (i + 1) * (i + 1);
		}
		xslope = (sumtd * n - sumt * sumd) / (sumt2 * n - sumt * sumt);
		return (xslope);
	}

	private void swarm_apportion() {

		int i, j;
		double pm;
		double mldensity; // [veh/mile/lane]
		double reqdensity; // [veh/mile/lane]
		double[][] desire_rate = new double[numZones][]; // [veh/min]
		double[][] ramp_excess = new double[numZones][]; // [veh/min]
		for (i = 0; i < numZones; i++) {
			desire_rate[i] = new double[zones.get(i).numOR];
			ramp_excess[i] = new double[zones.get(i).numOR];
			for (j = 0; j < zones.get(i).numOR; j++) {
				desire_rate[i][j] = 0.0;
				ramp_excess[i][j] = 0.0;
			}
		}

		for (i = numZones - 1; i >= 0; i--) {

			Zone Z = zones.get(i);
			pm = Z.bnpm;
			reqdensity = Z.swarm_bn_required_density;

			for (j = Z.numOR - 1; j >= 0; j--) {

				mldensity = Z.Data.mlupdent[j] / Z.mluplanenum[j];

				desire_rate[i][j] = Z.Data.orqt[j] - Math.abs(Z.orpm[j] - pm) * (mldensity - reqdensity) - ramp_excess[i][j]; // [veh/min] // according to NET code

				swarm1rate[i][j] = desire_rate[i][j] * 60.0; // [veh/hour]
				swarm1rate[i][j] = Math.min(swarm1rate[i][j], P.MAX_METERING_RATE * Z.orsignum[j]);
				swarm1rate[i][j] = Math.max(swarm1rate[i][j], P.MIN_METERING_RATE * Z.orsignum[j]);

				if (j > 0) {
					ramp_excess[i][j - 1] = P.swarm_phi * (swarm1rate[i][j] / 60.0 - desire_rate[i][j]); // [veh/min]
					if (j == 0) {
						ramp_excess[i][j - 1] *= P.swarm_psi;
					}
				}
				pm = Z.orpm[j];
			}
		}
	}

	private void swarm2a() {

		int i, j;
		double db; // postmile difference [mile]
		double speed; // mainline speed [fps]
		double headway; // mainline headway [feet]
		double density; // mainline density [veh/mile/lane]
		double Tjt; // unbounded metering rate [veh/min]
		double average_vehicle_length = 20; // average vehicle length [feet]
		double speed_reduction; // speed reduction per mile [fps/mile]
		double target_speed_reduction = 50; // target speed reduction [fps]

		for (i = 0; i < numZones; i++) {
			Zone Z = zones.get(i);
			for (j = 0; j < Z.numOR; j++) {
				db = Math.abs(Z.mluppm[j] - Z.mldnpm[j]);
				speed = Z.Data.mlupspeed[j] * 5280.0 / 3600.0;
				density = Z.Data.mlupdent[j] / Z.mluplanenum[j];
				headway = (5280.0 - density * average_vehicle_length) / density / speed;
				speed_reduction = target_speed_reduction / db;
				Tjt = 18.0 * Z.orlanenum[j] - 2.0 * Z.mluplanenum[j] * db * ((5280.0 / ((speed - speed_reduction) * headway + average_vehicle_length)) - density); // [veh/min]
				swarm2arate[i][j] = Tjt * 60.0; // [veh/hr]
				swarm2arate[i][j] = Math.max(swarm2arate[i][j], Z.orsignum[j] * P.MIN_METERING_RATE);
				swarm2arate[i][j] = Math.min(swarm2arate[i][j], Z.orsignum[j] * P.MAX_METERING_RATE);
			}
		}
	}

	private void swarm2b() {

		int i, j;
		double mlupsatdensity; // [veh/mile/lane]
		double mldnsatdensity; // [veh/mile/lane]
		double db; // [mile]
		double minrate; // [veh/min]
		double maxrate; // [veh/min]
		double cmax; // Maximum available storage
		double cdes; // Desired available capacity

		for (i = 0; i < numZones; i++) {
			Zone Z = zones.get(i);
			for (j = 0; j < Z.numOR; j++) {

				mlupsatdensity = Z.mlup_saturation_density[j];
				mldnsatdensity = Z.mldn_saturation_density[j];
				db = Math.abs(Z.mluppm[j] - Z.mldnpm[j]);

				Z.sigma[j] = Z.mluplanenum[j] * db * (Z.Data.mlupdent[j] / Z.mluplanenum[j] + Z.Data.mldndent[j] / Z.mldnlanenum[j]) / 2.; // [veh]
				cmax = Math.max(Z.mluplanenum[j] * db * (mlupsatdensity + mldnsatdensity) / 2. - Z.sigma[j], 0.0); // [veh/min] according to NET code
				cdes = P.epsilon * cmax; // [veh/min];

				minrate = Z.orsignum[j] * P.MIN_METERING_RATE / 60.0; // [veh/min]
				maxrate = Z.orsignum[j] * P.MAX_METERING_RATE / 60.0; // [veh/min]

				if (cdes > minrate) {
					swarm2brate[i][j] = Math.min(cdes, maxrate) * 60.0; // [veh/hr]
				} else {
					swarm2brate[i][j] = Math.max(Math.min(cmax, maxrate), minrate) * 60.0; // [veh/hr]
				}
			}
		}
	}

	private void SetToMax(double[][] X) {
		int i, j;
		for (i = 0; i < numZones; i++)
			for (j = 0; j < zones.get(i).numOR; j++)
				X[i][j] = P.MAX_METERING_RATE * zones.get(i).orsignum[j];
	}
	
	// GUI =================================================================================
	
	public String getDescription() {
		String buf = "SWARM";
		if (P.SWARM1 || P.SWARM2A || P.SWARM2B)
			buf = buf + " ";
		if (P.SWARM1) {
			buf = buf + "1";
			if (P.SWARM2A || P.SWARM2B)
				buf = buf + "/";
		}
		if (P.SWARM2A) {
			buf = buf + "2a";
			if (P.SWARM2B)
				buf = buf + "/";
		}
		if (P.SWARM2B) {
			buf = buf + "2b";
		}
		return buf;
	}

	public String toString() {
		return "SWARM";
	}
	
	
	private AbstractLinkHWC getUpMLbyORLink(AbstractLink or) {
		AbstractNode x = or.getEndNode();
		for (int i = 0; i < x.getPredecessors().size(); i++){
			AbstractLinkHWC L = (AbstractLinkHWC) x.getPredecessors().get(i);
			if ((L.getType() == TypesHWC.LINK_FREEWAY) || (L.getType() == TypesHWC.LINK_HIGHWAY)) {
				return L;
			}
		}
		return null;
	}

	private AbstractLinkHWC getDnMLbyORLink(AbstractLink or) {
		AbstractNode x = or.getEndNode();
		for (int i = 0; i < x.getSuccessors().size(); i++){
			AbstractLinkHWC L = (AbstractLinkHWC) x.getSuccessors().get(i);
			if ((L.getType() == TypesHWC.LINK_FREEWAY) || (L.getType() == TypesHWC.LINK_HIGHWAY)) {
				return L;
			}
		}
		return null;
	}
	
	private AbstractLinkHWC getUpMLbyORId(int orid) {
		return getUpMLbyORLink(myMonitor.getMyNetwork().getLinkById(orid));
	}
	
	private AbstractLinkHWC getDnMLbyORId(int orid) {
		return getDnMLbyORLink(myMonitor.getMyNetwork().getLinkById(orid));
	}
	

	/**
	 * This class implements SWARM Zone.
	 */
	public class Zone {
		public AbstractLinkHWC bottleneck = null;
		private AbstractLink fromRamp = null;
		private AbstractLink toRamp = null;
		public Vector<AbstractLinkHWC> onramps = new Vector<AbstractLinkHWC>();
		public int numOR;
		public double sat_den_multiplier = 1;

		// Traffic data
		public TrafficData Data = new TrafficData();
		
		// geometrics
		public double[] orpm; 		// [mile] postmile for onramp station (SWARM1)
		public int[] orsignum; 		// [-] number of light signals on the onramp (SWARM1,SWARM2a,SWARM2b)
		public int[] orlanenum; 	// [-] number of lanes in onramp station (SWARM2a)
		public double[] mluppm; 	// [mile] postmile for upstream mainline station (SWARM1,SWARM2b)
		public double[] mldnpm; 	// [mile] postmile for downstream mainline station (SWARM1,SWARM2b)
		public int[] mluplanenum; 	// [-] number of lanes in upstream mainline station (SWARM1,SWARM2a,SWARM2b)
		public int[] mldnlanenum; 	// [-] number of lanes in dnstream mainline station (SWARM1,SWARM2a,SWARM2b)
		public int bnlanenum; 		// [-] number of lanes in bottleneck station
		public double bnpm; 		// [mile] postmile for upstream mainline station (SWARM1,SWARM2b)
		
		// variables
		public double[] swarm_slope_densities; 				// [veh/mile/lane] (SWARM1)
		public double[] swarm_slope_forecast_densities; 		// [???] (SWARM1)
		public double swarm_bn_required_density; 				// [veh/mile/lane] (SWARM1)
		public double[] sigma; 								// [???] (SWARM1,SWARM2b)
		public double[] mlup_saturation_density; 				// [veh/mile/lane] (SWARM1,SWARM2b)
		public double[] mldn_saturation_density; 				// [veh/mile/lane] (SWARM1,SWARM2b)
		public double bn_saturation_density; 					// [veh/mile/lane] (SWARM1,SWARM2b)
		
		// maps
		public int[] mlupindex;
		public int[] mldnindex;
		public int[] orindex;
		public int bnindex;
		public int[] controlindex;

		
		public AbstractLink getBottleneckLink() { return bottleneck; }
		public AbstractLink getFromOnramp() { return fromRamp; }
		public AbstractLink getToOnramp() { return toRamp; }
		public double getSaturationDensityMultiplier() { return sat_den_multiplier; }
		
		public void initialize(){
			setOnramps();
			numOR = onramps.size();
			mluppm = new double[numOR];
			mldnpm = new double[numOR];
			orpm = new double[numOR];
			mluplanenum = new int[numOR];
			mldnlanenum = new int[numOR];
			orlanenum = new int[numOR];
			orsignum = new int[numOR];
			swarm_slope_densities = new double[P.SWARM_SLOPE_SAMPLE_SIZE];
			swarm_slope_forecast_densities = new double[P.SWARM_SLOPE_SAMPLE_SIZE];
			sigma = new double[numOR];
			mlup_saturation_density = new double[numOR];
			mldn_saturation_density = new double[numOR];	
			orindex = new int[numOR];
			mlupindex = new int[numOR];
			mldnindex = new int[numOR];
			controlindex = new int[numOR];

			int j;
			AbstractLinkHWC abslnk;
			NodeFreeway up,dn;
			up = (NodeFreeway) bottleneck.getBeginNode();
			dn = (NodeFreeway) bottleneck.getEndNode();
			bnpm =(up.getPostmile() + dn.getPostmile()) / 2.0;
			bnlanenum = (int) bottleneck.getLanes();
			bn_saturation_density = 10.0;// GCG FIX THIS
			for (j = 0; j < numOR; j++) {
				orpm[j] = ((NodeFreeway) onramps.get(j).getEndNode()).getPostmile();
				orlanenum[j] = (int) onramps.get(j).getLanes();
				orsignum[j] = orlanenum[j]; // GCG FIX THIS
				sigma[j] = 1;// GCFIX THIS
				mlup_saturation_density[j] = 0.0;
				mlup_saturation_density[j] = 0.0;
			}
			
			// mainline postmiles = average of upstream and downstream nodes
			for (j = 0; j < numOR; j++) {				
				abslnk = getUpMLbyORLink(onramps.get(j)); 
				if (abslnk != null) {
					up = (NodeFreeway) abslnk.getBeginNode();
					dn = (NodeFreeway) abslnk.getEndNode();
					mluppm[j] = (up.getPostmile() + dn.getPostmile()) / 2.0;
					mluplanenum[j] = (int) ((AbstractLinkHWC) abslnk).getLanes();
				} else {
					mluppm[j] = ((NodeFreeway) onramps.get(j).getEndNode()).getPostmile();
					mluplanenum[j] = -1;
				}

				abslnk = getDnMLbyORLink(onramps.get(j)); 
				if (abslnk != null) {
					up = (NodeFreeway) abslnk.getBeginNode();
					dn = (NodeFreeway) abslnk.getEndNode();
					mldnpm[j] = (up.getPostmile() + dn.getPostmile()) / 2.0;
					mldnlanenum[j] = (int) ((AbstractLinkHWC) abslnk).getLanes();
				} else {
					mldnpm[j] = ((NodeFreeway) onramps.get(j).getEndNode()).getPostmile();
					mldnlanenum[j] = -1;
				}
			}

			// construct  maps
			bnindex = myMonitor.getMonitoredIndexById( bottleneck.getId() );
			for (j = 0; j < numOR; j++) {
				orindex[j] = myMonitor.getMonitoredIndexById(onramps.get(j).getId());
				abslnk = getUpMLbyORId(onramps.get(j).getId()); 
				if(abslnk!=null)
					mlupindex[j] = myMonitor.getMonitoredIndexById(abslnk.getId());
				else
					mlupindex[j] = -1;
				abslnk = getDnMLbyORId(onramps.get(j).getId()); 
				if(abslnk!=null)
					mldnindex[j] = myMonitor.getMonitoredIndexById(abslnk.getId());
				else
					mldnindex[j] = -1;
			}
			
			// GG temporary. If the upstream map is empty, assign the downstream, and vice versa
			for (j = 0; j < numOR; j++) {
				if (mlupindex[j] == -1)
					mlupindex[j] = mldnindex[j];
				if (mldnindex[j] == -1)
					mldnindex[j] = mlupindex[j];
			}
			
			Data.initialize(this);
			
		}
		
		private void setOnramps() {
			if (myMonitor == null)
				return;
			onramps.clear();
			Vector<AbstractNetworkElement> nes = myMonitor.getSuccessors();
			int bi = nes.indexOf(fromRamp);
			int ei = nes.indexOf(toRamp);
			if ((bi >= 0) && (ei >= 0) && (bi <= ei))
				for (int i = bi; i <= ei; i++)
					onramps.add((AbstractLinkHWC)nes.get(i));
			return;
		}
		
		@SuppressWarnings("unchecked")
		public Zone clone() {
			Zone nz = new Zone();
			nz.bottleneck = this.bottleneck;
			nz.sat_den_multiplier = this.sat_den_multiplier;
			nz.setFromOnramp(fromRamp);
			nz.setToOnramp(toRamp);
			nz.onramps = (Vector<AbstractLinkHWC>) this.onramps.clone();
			nz.numOR = this.numOR;
			nz.Data = this.Data.clone();
			nz.initialize();
			return nz;
		}
		
		public void setFromOnramp(AbstractLink lnk) {
			if (lnk == null)
				return;
			fromRamp = lnk;
			if (toRamp == null)
				return;
			setOnramps();
			return;
		}
		
		public void setToOnramp(AbstractLink lnk) {
			if (lnk == null)
				return;
			toRamp = lnk;
			if (fromRamp == null)
				return;
			setOnramps();
			return;
		}
	}
	
	public class SWARMParameters {

		// user input (active algorithms)
		public boolean SWARM1;
		public boolean SWARM2A;
		public boolean SWARM2B;

		// user input (preprocessor defines in original code)
		public int SWARM_DENSITY_SAMPLE_SIZE = 1; 				// [-] (SWARM1)
		public int SWARM_SLOPE_SAMPLE_SIZE = 1; 				// [-] (SWARM1)
		public int SWARM_SAT_DEN_NUMBER = 0; 					// [-] (SWARM2b)
		public int SWARM_FORECAST_LEAD_TIME = 1; 				// [???] (SWARM1)

		// user input (SWARM parameters)
		public double input_var_lane = 0.0; 					// [???] (SWARM1)
		public double meas_var_lane = 0.0; 					// [???] (SWARM1)
		public double swarm_phi = 1.0; 						// [???] (SWARM1)
		public double swarm_psi = 1.0; 						// [???] (SWARM1)
		public double sat_smoother = 1.0; 						// [???] (SWARM1,SWARM2b)
		public double epsilon = 1.0; 							// [-] (SWARM2b)
		// public Vector<Double> postmilerange; // [mile]

		// user input (bottlenecks)
		public Vector<Double> swarm_vds_sat_den_multiplier = new Vector<Double>(); // [???]
		// (SWARM1)

		// previous user input (define internally)
		public int MIN_METERING_RATE = 180; 				// [veh/hr]
		public int MAX_METERING_RATE = 900; 				// [veh/hr]

	}
	
	public class TrafficData {
		
		Zone myzone;
		// measurements
		public double orqt[]; 			// Onramp flow [veh/hour] [zone][ramp] (SWARM1)
		// public double mlqt[]; 			// Mainline flow [veh/hour] (SWARM1,SWARM2b)
		public double mlupspeed[]; 		// Mainline speed [mph] [zone][ramp] (SWARM2a)
		public double mlupdent[]; 		// Mainline density [veh/mile] [zone][ramp][mldn=1,mlup=0] (SWARM2a)
		public double mldndent[]; 		// Mainline density [veh/mile]  [zone][ramp][mldn=1,mlup=0] (SWARM2a)
		public double bndent;
		
		
		public void initialize(Zone z){
			myzone = z;
			mlupspeed = new double[myzone.numOR];
			mlupdent = new double[myzone.numOR];
			mldndent = new double[myzone.numOR];
			orqt = new double[myzone.numOR];
		}
		
		public void UpdateMeasurements() {
			int j;
			if(usesensors){
				bndent = ((MonitorControllerHWC)myMonitor).SensorDensity(myzone.bnindex);
				for (j = 0; j < myzone.numOR; j++) {
					mlupspeed[j] = ((MonitorControllerHWC)myMonitor).SensorSpeed(myzone.mlupindex[j]);
					mlupdent[j] = ((MonitorControllerHWC)myMonitor).SensorDensity(myzone.mlupindex[j]);
					mldndent[j] = ((MonitorControllerHWC)myMonitor).SensorFlow(myzone.mlupindex[j]);
					orqt[j] = ((MonitorControllerHWC)myMonitor).SensorFlow(myzone.orindex[j]);
				}
			}
			else {
				AbstractLinkHWC bnl = ((MonitorControllerHWC)myMonitor).getMonitoredLink(myzone.bnindex);
				if (bnl != null)
					bndent = bnl.getDensity().sum().getCenter();
				for (j = 0; j < myzone.numOR; j++) {
					AbstractLinkHWC lnk = ((MonitorControllerHWC)myMonitor).getMonitoredLink(myzone.mlupindex[j]);
					if (lnk != null) {
						mlupspeed[j] = lnk.getSpeed().getCenter();
						mlupdent[j] = lnk.getDensity().sum().getCenter();
						mldndent[j] = lnk.getFlow().sum().getCenter();
						orqt[j] = lnk.getFlow().sum().getCenter();
					}
				}
			}
		}
		
		public void swarm_saturation_density() {
			int i, j;
			AbstractLinkHWC link = null;
			for (i = 0; i < numZones; i++) {
				Zone Z = zones.get(i);
				link = (AbstractLinkHWC) ((MonitorControllerHWC)myMonitor).getMonitoredLink(Z.bnindex);
				Z.bn_saturation_density = link.getCriticalDensity() / link.getLanes();
				for (j = 0; j < Z.numOR; j++) {
					link = (AbstractLinkHWC) ((MonitorControllerHWC)myMonitor).getMonitoredLink(Z.mlupindex[j]);
					Z.mlup_saturation_density[j] = link.getCriticalDensity() / link.getLanes();
					link = (AbstractLinkHWC) ((MonitorControllerHWC)myMonitor).getMonitoredLink(Z.mldnindex[j]);
					Z.mldn_saturation_density[j] = link.getCriticalDensity() / link.getLanes();
				}
			}

			/*
			 * int i, j, k; double D4, D3, D2, D2Q, DQ, d, q, d2, new_sat;
			 * 
			 * Vector< Vector<Double> > sat_den_den = new Vector< Vector<Double>
			 * >(); Vector< Vector<Double> > sat_den_flow = new Vector<
			 * Vector<Double> >();
			 * 
			 * for (i=0; i<totmlnum; i++) { sat_den_den.add(new Vector<Double>());
			 * sat_den_flow.add(new Vector<Double>()); for (j=0;
			 * j<SWARM_SAT_DEN_NUMBER; j++) { sat_den_den.get(i).add(0.0);
			 * sat_den_flow.get(i).add(0.0); } }
			 * 
			 * j=0;
			 * 
			 * for (i=0; i<totmlnum; i++) { if (mlqt[i] >= mllanenum[i]*MAX_FLOW/2)
			 * { j = (++sat_den_data_number[i]);
			 * sat_den_den.get(i).set(j,mldent[i]);
			 * sat_den_flow.get(i).set(j,mlqt[i]); }
			 * 
			 * if (j >= SWARM_SAT_DEN_NUMBER) { D4 = 0; D3 = 0; D2 = 0; D2Q = 0; DQ
			 * = 0; for (k=0; k<j; k++) { d = sat_den_den.get(i).get(k); q =
			 * sat_den_flow.get(i).get(k); d2 = d*d; D4 += d2*d2; D3 += d*d2; D2 +=
			 * d2; D2Q += d2*q; DQ += d*q; }
			 * 
			 * new_sat = (D3*D2Q - D4*DQ)/2./(D2*D2Q - D3*DQ);
			 * 
			 * if ((new_sat > 30.*mllanenum[i]) && (new_sat < 90.*mllanenum[i])) {
			 * swarm_vds_saturation_density[i] = sat_smoother*new_sat + (1. -
			 * sat_smoother)*swarm_vds_saturation_density[i]; }
			 * 
			 * sat_den_data_number[i] = 0; } }
			 */
		}

		public TrafficData clone(){
			TrafficData nt = new TrafficData();
			nt.initialize(this.myzone);
			return nt;
		}
	}
	


}
