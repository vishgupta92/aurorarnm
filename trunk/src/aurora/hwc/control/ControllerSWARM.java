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
 * @version $Id: ControllerSWARM.java,v 1.1.4.9 2009/08/22 01:33:39 akurzhan Exp $
 */
public class ControllerSWARM extends AbstractControllerComplex {
	private static final long serialVersionUID = -7135452153102929923L;
 
	protected Vector<Double> limits = new Vector<Double>(); // input limits
	
	// GG Check that we are not using getLinkById in any update functions (too slow). Create lookup maps.
	
	// links
	Vector<Integer> bottlenecklinkID = new Vector<Integer>();
	Vector<Vector <Integer> > onramplinkID = new Vector<Vector <Integer> >();
	
	// monitor map
	int[][] mlup2monitor;
	int[][] mldn2monitor;
	int[][] or2monitor;
	int[]   bn2monitor;
	
	// monitored controller map
	int[][] controlindex;
	
	// size variables
	private Vector<Zone> zones = new Vector<Zone>();
	private int numZones;
	private int[] numOR;
	
	// user input (active algorithms)
	private boolean SWARM1;
	private boolean SWARM2A;
	private boolean SWARM2B;
	
	// user input (preprocessor defines in original code)
	private int SWARM_DENSITY_SAMPLE_SIZE = -1;		// [-] (SWARM1)	
	private int SWARM_SLOPE_SAMPLE_SIZE = -1;		// [-] (SWARM1)
	private int SWARM_SAT_DEN_NUMBER = -1;			// [-] (SWARM2b)
	private int SWARM_FORECAST_LEAD_TIME = -1;		// [???] (SWARM1)
	
	// user input (SWARM parameters)
	private double input_var_lane = -1.0;			// [???]		(SWARM1)	
	private double meas_var_lane = -1.0;			// [???]		(SWARM1)
	private double swarm_phi = -1.0;				// [???]		(SWARM1)
	private double swarm_psi = -1.0;				// [???]		(SWARM1)
	private double sat_smoother = -1.0;				// [???] 		(SWARM1,SWARM2b)
	private double epsilon = -1.0;					// [-]			(SWARM2b)
	//private Vector<Double> postmilerange;			// [mile]
	
	// user input (bottlenecks)
	private Vector<Double> swarm_vds_sat_den_multiplier = new Vector<Double>();	// [???]		(SWARM1)
	
	// previous user input (define internally)
	private int MIN_METERING_RATE;		// [veh/hr]
	private int MAX_METERING_RATE ;		// [veh/hr]
	
	// measurements
	private double orqt[][];			// Onramp flow		[veh/hour]		[zone][ramp]				(SWARM1)
	//private double mlqt[];			// Mainline flow	[veh/hour]									(SWARM1,SWARM2b)	
	private double mlupspeed[][];		// Mainline speed	[mph]			[zone][ramp]				(SWARM2a)
	private double mlupdent[][];		// Mainline density [veh/mile]		[zone][ramp][mldn=1,mlup=0]	(SWARM2a)	
	private double mldndent[][];		// Mainline density [veh/mile]		[zone][ramp][mldn=1,mlup=0]	(SWARM2a)	
	private double bndent[];

	// geometrics
	private double[][] orpm;			// [mile] postmile for onramp station 					(SWARM1)
	private int[][] orsignum;			// [-] number of light signals on the onramp			(SWARM1,SWARM2a,SWARM2b)
	private int[][] orlanenum;			// [-] number of lanes in onramp station				(SWARM2a)
	private double[][] mluppm;			// [mile] postmile for upstream mainline station 		(SWARM1,SWARM2b)
	private double[][] mldnpm;			// [mile] postmile for downstream mainline station 		(SWARM1,SWARM2b)
	private int[][] mluplanenum;		// [-] number of lanes in upstream mainline station		(SWARM1,SWARM2a,SWARM2b)	
	private int[][] mldnlanenum;		// [-] number of lanes in dnstream mainline station		(SWARM1,SWARM2a,SWARM2b)
	private int[] bnlanenum;			// [-] number of lanes in bottleneck station	
	private double[] bnpm;				// [mile] postmile for upstream mainline station 		(SWARM1,SWARM2b)
	
	// variables
	private double[][] swarm_slope_densities;				// [veh/mile/lane] 		(SWARM1)
	private double[][] swarm_slope_forecast_densities;		// [???]				(SWARM1)
	private double[][] swarm_densities;						// [veh/mile/lane] 		(SWARM1)
	private double[][] initial_slope;						// [???]				(SWARM1)
	private double[] slope;									// [???]				(SWARM1)
	private double[] forecast_slope;						// [???]				(SWARM1)
	private double[] density_pre;							// [???]				(SWARM1)
	private double[] density_post;							// [???]				(SWARM1)
	private double[] variance_pre;							// [???]				(SWARM1)
	private double[] variance_post;							// [???]				(SWARM1)
	private double[] density_forecast_post;					// [veh/mile/lane] 		(SWARM1)
	private double[] density_forecast;						// [???]				(SWARM1)
	private double[] swarm_bn_required_density;				// [veh/mile/lane]		(SWARM1)
	private double[][] sigma;								// [???]				(SWARM1,SWARM2b)
	private double[][] mlup_saturation_density;				// [veh/mile/lane]  	(SWARM1,SWARM2b)
	private double[][] mldn_saturation_density;				// [veh/mile/lane]  	(SWARM1,SWARM2b)
	private double[] bn_saturation_density;					// [veh/mile/lane]  	(SWARM1,SWARM2b)
	
	// static in original code
	private int pollingnumber;				// [-]		(SWARM1)

	// outputs
	private double[][] swarm1rate;		// [veh/hour]
	private double[][] swarm2brate;		// [veh/hour]
	private double[][] swarm2arate;		// [veh/hour]
	//
	
	
	// INTERFACE --------------------------------------------------------------------
	public int get_density_sample_size(){ return SWARM_DENSITY_SAMPLE_SIZE ; }
	public double get_epsilon(){ return epsilon; }
	public int get_forecast_lead_time(){ return SWARM_FORECAST_LEAD_TIME; }
	public double get_input_var_lane(){ return input_var_lane; }
	public double get_meas_var_lane(){ return meas_var_lane; }
	public double get_phi(){ return swarm_phi; }
	public double get_psi(){ return swarm_psi; }
	public int get_sat_den_multiplier(){ return SWARM_SAT_DEN_NUMBER; }
	public double get_sat_smoother(){ return sat_smoother; }
	public int get_slope_sample_size(){ return SWARM_SLOPE_SAMPLE_SIZE ; }
	public boolean get_swarm1(){ return SWARM1; };
	public boolean get_swarm2a(){ return SWARM2A; };
	public boolean get_swarm2b(){ return SWARM2B; };
	public int getNumZones(){ return numZones; }
	public Vector<Zone> getZones() { return zones; }

	public int getNumOnramps(int i){
		if(i<0 || i>=numZones)
			return -1;
		else
			return numOR[i];
	}

	public int getFirstOnramp(int i){
		if(i<0 || i>=numZones)
			return -1;
		else
			return onramplinkID.get(i).firstElement();
	}

	public int getLastOnramp(int i){
		if(i<0 || i>=numZones)
			return -1;
		else
			return onramplinkID.get(i).lastElement();	
	}
	
	public int getBottleneckID(int i){
		if(i<0 || i>=numZones)
			return -1;
		else
			return bottlenecklinkID.get(i);
	}

	public void set_density_sample_size(int x){ SWARM_DENSITY_SAMPLE_SIZE=x; }
	public void set_epsilon(double x){ epsilon=x; }
	public void set_forecast_lead_time(int x){ SWARM_FORECAST_LEAD_TIME=x; }
	public void set_input_var_lane(double x){ input_var_lane=x; }
	public void set_meas_var_lane(double x){ meas_var_lane=x; }
	public void set_phi(double x){ swarm_phi=x; }
	public void set_psi(double x){ swarm_psi=x; }
	public void set_sat_den_multiplier(int x){ SWARM_SAT_DEN_NUMBER=x; }
	public void set_sat_smoother(double x){ sat_smoother=x; }
	public void set_slope_sample_size(int x){ SWARM_SLOPE_SAMPLE_SIZE=x; }
	public void set_swarm1(boolean x){ SWARM1 = x; };
	public void set_swarm2a(boolean x){ SWARM2A = x; };
	public void set_swarm2b(boolean x){ SWARM2B = x; };
	
	public void setZones(Vector<Zone> x) {
		if (x == null)
			return;
		zones = x;
		// Everything below is temporary (hopefully)
		numZones = zones.size();
		numOR = new int[zones.size()];
		bottlenecklinkID.clear();
		onramplinkID.clear();
		swarm_vds_sat_den_multiplier.clear();
		for (int i = 0; i < zones.size(); i++) {
			bottlenecklinkID.add(zones.get(i).getBottleneckLink().getId());
			swarm_vds_sat_den_multiplier.add(zones.get(i).getSaturationDensityMultiplier());
			Vector<AbstractLink> ors = zones.get(i).getOnramps();
			numOR[i] = ors.size();
			Vector<Integer> orids = new Vector<Integer>();
			for (int j = 0; j < ors.size(); j++)
				orids.add(ors.get(j).getId());
			onramplinkID.add(orids);
		}
		return;
	}
	
	// CONSTRUCTION -----------------------------------------------------------------	
	public ControllerSWARM() {
		super();
		limits.add(0.0);
		limits.add(0.0);
	}
	
	public ControllerSWARM(AbstractMonitor m) {
		super();
		myMonitor = (AbstractMonitorController) m;
		limits.add(0.0);
		limits.add(0.0);
	}
	
	// MAIN FUNCTION ----------------------------------------------------------------
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		
		int i,j;
	
		boolean res = super.dataUpdate(ts);
		if (res) {

			// Update measurements ....................................
			UpdateMeasurements();
			
			// Compute saturation density .............................
			if( SWARM1 || SWARM2B ){
				swarm_saturation_density();
			}
		
			// Run SWARM 1 ............................................
			if(SWARM1)
				swarm1();
			else
				SetToMax(swarm1rate);
			
			if(SWARM2A)
				swarm2a();
			else
				SetToMax(swarm2arate);
			
			if(SWARM2B)
				swarm2b();
			else
				SetToMax(swarm2brate);

			for (i=0;i<numZones;i++)
				for (j=0;j<numOR[i];j++)
					setControlInput(controlindex[i][j], Math.min( Math.min(swarm2arate[i][j] , swarm2brate[i][j]) , swarm1rate[i][j] ) );
		}
		return res;
	}

	// SUBROUTINES ------------------------------------------------------------------

	private void UpdateMeasurements(){
		int i,j;
		for(i=0;i<numZones;i++){
			bndent[i] = ((MonitorControllerHWC) myMonitor).getDensityByIndex(bn2monitor[i]);
			for(j=0;j<numOR[i];j++){
				mlupspeed[i][j] = ((MonitorControllerHWC) myMonitor).getSpeedByIndex(mlup2monitor[i][j]);
				mlupdent[i][j]  = ((MonitorControllerHWC) myMonitor).getDensityByIndex(mlup2monitor[i][j]);
				mldndent[i][j]  = ((MonitorControllerHWC) myMonitor).getFlowByIndex(mlup2monitor[i][j]);
				orqt[i][j] 		= ((MonitorControllerHWC) myMonitor).getFlowByIndex(or2monitor[i][j]);
			}
		}
	}
	
	private void swarm1(){
		swarm_kalman_filter();
		swarm_apportion();
	}
	
	private void swarm_saturation_density(){
		int i,j,linkid;
		AbstractNodeComplex myNetwork = ((MonitorControllerHWC) myMonitor).getMyNetwork();
		AbstractLinkHWC link = null;
		for(i=0;i<numZones;i++){
			linkid =  ((MonitorControllerHWC) myMonitor).getLinkID(bn2monitor[i]);
			link = (AbstractLinkHWC) myNetwork.getLinkById(linkid);
			bn_saturation_density[i] = link.getCriticalDensity() / link.getLanes();
			for(j=0;j<numOR[i];j++){
				linkid =  ((MonitorControllerHWC) myMonitor).getLinkID(mlup2monitor[i][j]);
				link = (AbstractLinkHWC) myNetwork.getLinkById(linkid);
				mlup_saturation_density[i][j] = link.getCriticalDensity() / link.getLanes();
				linkid =  ((MonitorControllerHWC) myMonitor).getLinkID(mldn2monitor[i][j]);
				link = (AbstractLinkHWC) myNetwork.getLinkById(linkid);
				mldn_saturation_density[i][j] = link.getCriticalDensity() / link.getLanes();
			}
		}
		
		/*
		int i, j, k;
		double D4, D3, D2, D2Q, DQ, d, q, d2, new_sat;

		Vector< Vector<Double> > sat_den_den = new Vector< Vector<Double> >();
		Vector< Vector<Double> > sat_den_flow = new Vector< Vector<Double> >();
		
		for (i=0; i<totmlnum; i++) {
			sat_den_den.add(new Vector<Double>());
			sat_den_flow.add(new Vector<Double>());
			for (j=0; j<SWARM_SAT_DEN_NUMBER; j++) {
				sat_den_den.get(i).add(0.0);
				sat_den_flow.get(i).add(0.0);
			}
		}
		
		j=0;

		for (i=0; i<totmlnum; i++) {
			if (mlqt[i] >= mllanenum[i]*MAX_FLOW/2) 
			{
				j = (++sat_den_data_number[i]);
				sat_den_den.get(i).set(j,mldent[i]);
				sat_den_flow.get(i).set(j,mlqt[i]);
			}

			if (j >= SWARM_SAT_DEN_NUMBER)
			{
				D4 = 0;
				D3 = 0;
				D2 = 0;
				D2Q = 0;
				DQ = 0;
				for (k=0; k<j; k++)
				{
					d = sat_den_den.get(i).get(k);
					q = sat_den_flow.get(i).get(k);
					d2 = d*d;
					D4 += d2*d2;
					D3 += d*d2;
					D2 += d2;
					D2Q += d2*q;
					DQ += d*q;
				}
							
				new_sat = (D3*D2Q - D4*DQ)/2./(D2*D2Q - D3*DQ);

				if ((new_sat > 30.*mllanenum[i]) && (new_sat < 90.*mllanenum[i])) {
					swarm_vds_saturation_density[i] = sat_smoother*new_sat + (1. - sat_smoother)*swarm_vds_saturation_density[i];
				}

				sat_den_data_number[i] = 0;
			}
		}
		*/
	}

	private void swarm_kalman_filter() 
	{
		int i, j, k;
		int bnlanes;
		double bndensity, sat_density;
		double temp, sumd, sumd2;
		double input_variance, meas_variance;

		pollingnumber++;

		for (i=0; i<numZones; i++) {

			bnlanes = bnlanenum[i];
			bndensity = bndent[i];
			sat_density = bn_saturation_density[i];
			input_variance = input_var_lane*bnlanes*bnlanes;
			meas_variance = meas_var_lane*bnlanes*bnlanes;

			temp = swarm_densities[i][0];
			for (j=1; j<SWARM_DENSITY_SAMPLE_SIZE; j++)
				swarm_densities[i][j-1] = swarm_densities[i][j] - temp;
			swarm_densities[i][j-1] = swarm_densities[i][j-2] + bndensity;

			temp = swarm_slope_densities[i][0];
			for (j=1; j<SWARM_SLOPE_SAMPLE_SIZE; j++)
				swarm_slope_densities[i][j-1] = swarm_slope_densities[i][j] - temp;
			swarm_slope_densities[i][j-1] = swarm_slope_densities[i][j-2] + bndensity;
			
			if (pollingnumber < SWARM_DENSITY_SAMPLE_SIZE)
				initial_slope[i][pollingnumber] = swarm_density_slope(i);

			if (pollingnumber == SWARM_DENSITY_SAMPLE_SIZE) {
				sumd = 0.; sumd2 = 0.;
				for (j = 0; j < SWARM_DENSITY_SAMPLE_SIZE; j++) {
					sumd += swarm_densities[i][j];
					sumd2 += swarm_densities[i][j]*swarm_densities[i][j];
				}
				density_post[i] = sumd/(double) SWARM_DENSITY_SAMPLE_SIZE;
				variance_post[i] = ( sumd2 - 2*sumd*density_post[i] + density_post[i]*density_post[i])/(double) SWARM_DENSITY_SAMPLE_SIZE;
				slope[i] = swarm_density_slope(i);
				for (j = SWARM_DENSITY_SAMPLE_SIZE/2+1; j< SWARM_DENSITY_SAMPLE_SIZE; j++) {
					density_pre[i] = density_post[i] + initial_slope[i][j-1];
					variance_pre[i] = variance_post[i] + input_variance;
					variance_post[i] = 1./(1./variance_pre[i] + 1./meas_variance);
					density_post[i] = density_pre[i] + variance_post[i]/meas_variance*(swarm_densities[i][SWARM_SLOPE_SAMPLE_SIZE-1] - density_pre[i]);
				}
			}

			if (pollingnumber > SWARM_DENSITY_SAMPLE_SIZE) {
				density_pre[i] = density_post[i] + slope[i];
				variance_pre[i] = variance_post[i] + input_variance;
				variance_post[i] = 1./(1./variance_pre[i] + 1./meas_variance);
				density_post[i] = density_pre[i] + variance_post[i]/meas_variance*(swarm_slope_densities[i][SWARM_SLOPE_SAMPLE_SIZE-1] - density_pre[i]);
				slope[i] = swarm_density_slope(i);
				for (j=0; j<SWARM_SLOPE_SAMPLE_SIZE; j++)
					swarm_slope_forecast_densities[i][j] = swarm_slope_densities[i][j];
				density_forecast_post[i] = density_post[i];
				forecast_slope[i] = slope[i];
				for (k=0; k<SWARM_FORECAST_LEAD_TIME; k++) {
					temp = density_forecast_post[i];
					density_forecast_post[i] = density_forecast_post[i] + forecast_slope[i];
					for (j=1; j<SWARM_SLOPE_SAMPLE_SIZE; j++)
						swarm_slope_densities[i][j-1] = swarm_slope_densities[i][j];
					swarm_slope_densities[i][j-1] = density_forecast_post[i];
					forecast_slope[i] = swarm_density_forecast_slope(i);
				}
				density_forecast[i] = density_forecast_post[i] - temp;
				if ( ( temp = (density_forecast[i] - sat_density*swarm_vds_sat_den_multiplier.get(i)) ) > 0)
					swarm_bn_required_density[i] = bndensity - temp/(double) SWARM_FORECAST_LEAD_TIME;
				else 
					swarm_bn_required_density[i] = density_forecast[i] - temp;
				if ( bndensity > sat_density )
					swarm_bn_required_density[i] = density_forecast[i] - temp;
			}
		}
		
	}
	
	private double swarm_density_slope(int bottleneck_id){
		double sumt, sumd, sumtd, sumt2, xslope;
		int i, n;

		n = SWARM_SLOPE_SAMPLE_SIZE;

		sumt = 0.;
		sumd = 0.;
		sumtd = 0.;
		sumt2 = 0. ;

		for (i=0; i<n; i++) {
			sumt += (i+1);
			sumd += swarm_slope_densities[bottleneck_id][i];
			sumtd += (i+1)*swarm_slope_densities[bottleneck_id][i];
			sumt2 += (i+1)*(i+1);
		}

		xslope = (sumtd*n - sumt*sumd)/(sumt2*n - sumt*sumt);
		return(xslope);
	
	}
	
	private double swarm_density_forecast_slope(int bottleneck_id){
		double sumt, sumd, sumtd, sumt2, xslope;
		int i, n;
		n = SWARM_SLOPE_SAMPLE_SIZE;
		sumt = 0.;
		sumd = 0.;
		sumtd = 0.;
		sumt2 = 0. ;
		for (i=0; i<n; i++) {
			sumt += (i+1);
			sumd += swarm_slope_forecast_densities[bottleneck_id][i];
			sumtd += (i+1)*swarm_slope_forecast_densities[bottleneck_id][i];
			sumt2 += (i+1)*(i+1);
		}
		xslope = (sumtd*n - sumt*sumd)/(sumt2*n - sumt*sumt);
		return(xslope);
	}

	private void swarm_apportion() {
		
		int i, j;
		double pm;
		double mldensity;				// [veh/mile/lane]
		double reqdensity;				// [veh/mile/lane]
		double[][] desire_rate = new double[numZones][];	// [veh/min]
		double[][] ramp_excess = new double[numZones][];	// [veh/min]
		for(i=0;i<numZones;i++){
			desire_rate[i] = new double[numOR[i]];
			ramp_excess[i] = new double[numOR[i]];
			for(j=0;j<numOR[i];j++){
				desire_rate[i][j] = 0.0;
				ramp_excess[i][j] = 0.0;
			}
		}
		
		for (i=numZones-1; i>=0; i--) {
			
			pm = bnpm[i];
			reqdensity = swarm_bn_required_density[i];
			
			for(j=numOR[i]-1;j>=0;j--){
				
				mldensity = mlupdent[i][j]/mluplanenum[i][j];
				
				desire_rate[i][j] = orqt[i][j] - Math.abs(orpm[i][j] - pm)*(mldensity - reqdensity) - ramp_excess[i][j]; // [veh/min] according to NET code

				swarm1rate[i][j] = desire_rate[i][j]*60.0; 				// [veh/hour]
				swarm1rate[i][j] = Math.min( swarm1rate[i][j] , MAX_METERING_RATE*orsignum[i][j]);
				swarm1rate[i][j] = Math.max( swarm1rate[i][j] , MIN_METERING_RATE*orsignum[i][j]);
				
				if (j > 0) {
					ramp_excess[i][j-1] = swarm_phi*(swarm1rate[i][j]/60.0 - desire_rate[i][j]);	// [veh/min]
					if (j == 0 ){
						ramp_excess[i][j-1] *= swarm_psi;
					}
				}
				pm = orpm[i][j];
			}
		}
	}
	
	private void swarm2a() {

		int i,j;
		double db;    								// postmile difference [mile]
		double speed;								// mainline speed [fps]
		double headway;								// mainline headway [feet]
		double density;								// mainline density [veh/mile/lane]
		double Tjt;									// unbounded metering rate [veh/min]
		double average_vehicle_length = 20; 		// average vehicle length [feet]
		double speed_reduction;						// speed reduction per mile [fps/mile]
		double target_speed_reduction = 50; 		// target speed reduction [fps]
		
		for(i=0;i<numZones;i++){
			for(j=0;j<numOR[i];j++){
				db = Math.abs(mluppm[i][j] - mldnpm[i][j]);
				speed = mlupspeed[i][j]*5280.0/3600.0;
				density = mlupdent[i][j] /mluplanenum[i][j];
				headway = ( 5280.0 - density*average_vehicle_length ) / density /speed;
				speed_reduction =  target_speed_reduction / db;
				Tjt = 18.0*orlanenum[i][j]-2.0*mluplanenum[i][j]*db*((5280.0/((speed-speed_reduction)*headway+average_vehicle_length))-density);	// [veh/min]
				swarm2arate[i][j] = Tjt*60.0;		// [veh/hr]
				swarm2arate[i][j] = Math.max( swarm2arate[i][j] , orsignum[i][j]*MIN_METERING_RATE );
				swarm2arate[i][j] = Math.min( swarm2arate[i][j] , orsignum[i][j]*MAX_METERING_RATE );		
			}
		}
	}
	
	private void swarm2b() {
		
		int i, j;
		double mlupsatdensity;		// [veh/mile/lane]
		double mldnsatdensity;		// [veh/mile/lane]
		double db;					// [mile]
		double minrate;		// [veh/min]
		double maxrate;		// [veh/min]
		double cmax;		// Maximum available storage
		double cdes;		// Desired available capacity

		for (i=0;i<numZones;i++) {
			for (j=0;j<numOR[i];j++) {
				
				mlupsatdensity = mlup_saturation_density[i][j];
				mldnsatdensity = mldn_saturation_density[i][j];
				db = Math.abs(mluppm[i][j] - mldnpm[i][j]);
				
				sigma[i][j] = mluplanenum[i][j] * db * ( mlupdent[i][j]/mluplanenum[i][j] + mldndent[i][j]/mldnlanenum[i][j])/2.;		// [veh]
				cmax = Math.max( mluplanenum[i][j]*db*(mlupsatdensity + mldnsatdensity)/2. - sigma[i][j] , 0.0 );						// [veh/min] according to NET code
				cdes = epsilon*cmax;			// [veh/min];

				minrate = orsignum[i][j]*MIN_METERING_RATE/60.0;	// [veh/min]
				maxrate = orsignum[i][j]*MAX_METERING_RATE/60.0;	// [veh/min]
				
				if (cdes > minrate) {
					swarm2brate[i][j] = Math.min(cdes, maxrate) * 60.0;		// [veh/hr]
				}
				else {
					swarm2brate[i][j] = Math.max( Math.min(cmax, maxrate), minrate) * 60.0;		// [veh/hr]
				}
			}
		}
	}
	
	// AUXILIARY -------------------------------------------------------------------

	private int UpstreamMainlineLink(int ORid) {
		AbstractLink OR = myMonitor.getMyNetwork().getLinkById(ORid);
		if(OR==null || OR.getType()!=TypesHWC.LINK_ONRAMP)
			return -1;
		
		AbstractNode n = OR.getEndNode();
		if(n==null)
			return -1;

		boolean foundit = false;
		AbstractNetworkElement X = null;
		for (int i = 0; i < n.getPredecessors().size(); i++){
			if ((n.getPredecessors().get(i).getType()==TypesHWC.LINK_FREEWAY) || (n.getPredecessors().get(i).getType()==TypesHWC.LINK_HIGHWAY)) {
				if(foundit)
					return -1; // multiple solutions
				X = n.getPredecessors().get(i);
				foundit = true;
			}
		}
		if(foundit)
			return X.getId();
		else 
			return -1;
	}
	
	private int DnstreamMainlineLink(int ORid) {
		AbstractLink OR = myMonitor.getMyNetwork().getLinkById(ORid);
		if(OR==null || OR.getType()!=TypesHWC.LINK_ONRAMP)
			return -1;
		
		AbstractNode n = OR.getEndNode();
		if(n==null)
			return -1;

		boolean foundit = false;
		AbstractNetworkElement X = null;
		for (int i = 0; i < n.getSuccessors().size(); i++){
			if ((n.getSuccessors().get(i).getType()==TypesHWC.LINK_FREEWAY) || (n.getSuccessors().get(i).getType()==TypesHWC.LINK_HIGHWAY)) {
				if(foundit)
					return -1; // multiple solutions
				X = n.getSuccessors().get(i);
				foundit = true;
			}
		}
		if(foundit)
			return X.getId();
		else 
			return -1;
	}	
	
	private void SetToMax(double[][] X){
		int i,j;
		for (i=0;i<numZones;i++)
			for (j=0;j<numOR[i];j++)
				X[i][j] = MAX_METERING_RATE*orsignum[i][j];	
	}
	
	public String getDescription() {
		String buf = "SWARM";
		if(SWARM1 || SWARM2A || SWARM2B)
			buf = buf + " ";
		if(SWARM1){
			buf = buf + "1";
			if(SWARM2A || SWARM2B)
				buf = buf + "/";
		}
		if(SWARM2A){
			buf = buf + "2a";
			if(SWARM2B)
				buf = buf + "/";
		}
		if(SWARM2B){
			buf = buf + "2b";
		}
		return buf;
	}
	
	public String toString() {
		return "SWARM";
	}

	@Override
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = super.initFromDOM(p);
		if (!res)
			return res;
		
		int i, j;
		Node n = null;

		bottlenecklinkID= new Vector<Integer>();
		onramplinkID = new Vector<Vector <Integer> >();
		
		try{
			NodeList pp = p.getChildNodes();
			
			for (i = 0; i < pp.getLength(); i++) {	
				if (pp.item(i).getNodeName().equals("components")) {
					if((n=pp.item(i).getAttributes().getNamedItem("swarm1"))!=null)
						SWARM1 = Boolean.parseBoolean(n.getNodeValue());
					if((n=pp.item(i).getAttributes().getNamedItem("swarm2a"))!=null)
						SWARM2A = Boolean.parseBoolean(n.getNodeValue());
					if((n=pp.item(i).getAttributes().getNamedItem("swarm2b"))!=null)
						SWARM2B = Boolean.parseBoolean(n.getNodeValue());
				}
				if (pp.item(i).getNodeName().equals("parameters")) {
					if((n=pp.item(i).getAttributes().getNamedItem("density_sample_size"))!=null)
						SWARM_DENSITY_SAMPLE_SIZE = Integer.parseInt(n.getNodeValue());
					if((n=pp.item(i).getAttributes().getNamedItem("slope_sample_size"))!=null)
						SWARM_SLOPE_SAMPLE_SIZE = Integer.parseInt(n.getNodeValue());
					if((n=pp.item(i).getAttributes().getNamedItem("input_var_lane"))!=null)
						input_var_lane = Double.parseDouble(n.getNodeValue());
					if((n=pp.item(i).getAttributes().getNamedItem("meas_var_lane"))!=null)
						meas_var_lane = Double.parseDouble(n.getNodeValue());
					if((n=pp.item(i).getAttributes().getNamedItem("phi"))!=null)
						swarm_phi = Double.parseDouble(n.getNodeValue());
					if((n=pp.item(i).getAttributes().getNamedItem("psi"))!=null)
						swarm_psi = Double.parseDouble(n.getNodeValue());
					if((n=pp.item(i).getAttributes().getNamedItem("sat_den_multiplier"))!=null)
						SWARM_SAT_DEN_NUMBER = Integer.parseInt(n.getNodeValue());
					if((n=pp.item(i).getAttributes().getNamedItem("forecast_lead_time"))!=null)
						SWARM_FORECAST_LEAD_TIME = Integer.parseInt(n.getNodeValue());
					if((n=pp.item(i).getAttributes().getNamedItem("epsilon"))!=null)
						epsilon = Double.parseDouble(n.getNodeValue());
					if((n=pp.item(i).getAttributes().getNamedItem("sat_smoother"))!=null)
						sat_smoother= Double.parseDouble(n.getNodeValue());						
				}
				if (pp.item(i).getNodeName().equals("limits")) {
					if((n=pp.item(i).getAttributes().getNamedItem("cmin"))!=null)
						limits.set(0 , Double.parseDouble(n.getNodeValue()));	
					if((n=pp.item(i).getAttributes().getNamedItem("cmax"))!=null)
						limits.set(1 , Double.parseDouble(n.getNodeValue()));	
				}
				if (pp.item(i).getNodeName().equals("zones")) {

					NodeList pp2 = pp.item(i).getChildNodes();
					Vector<Zone> vz = new Vector<Zone>();
					for (j = 0; j < pp2.getLength(); j++){
						if (pp2.item(j).getNodeName().equals("zone")) {
							Zone z = new Zone();
							if ((n = pp2.item(j).getAttributes().getNamedItem("bottlenecklink")) != null)
								z.setBottleneckLink(myMonitor.getMyNetwork().getLinkById(Integer.parseInt(n.getNodeValue())));
							if ((n = pp2.item(j).getAttributes().getNamedItem("fromonramp")) != null)
								z.setFromOnramp(myMonitor.getMyNetwork().getLinkById(Integer.parseInt(n.getNodeValue())));
							if ((n = pp2.item(j).getAttributes().getNamedItem("toonramp")) != null)
								z.setToOnramp(myMonitor.getMyNetwork().getLinkById(Integer.parseInt(n.getNodeValue())));
							if ((n = pp2.item(j).getAttributes().getNamedItem("sat_den_multiplier")) != null)
								z.setSaturationDensityMultiplier(Double.parseDouble(n.getNodeValue()));
							vz.add(z);
						}
					}
					setZones(vz);
				}
			}
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		
		if(res)
			res &= Initialize();

		return res;
	}

	private boolean Initialize(){
		
		int i,j;
		AbstractLink abslnk = null;
		Vector <AbstractNetworkElement> vnetel = null;
		Vector<Vector <Integer> > upmainlinelinkID = new Vector< Vector<Integer> >();
		Vector<Vector <Integer> > dnmainlinelinkID = new Vector< Vector<Integer> >();
		NodeFreeway up = null;
		NodeFreeway dn = null;
		NodeFreeway mid = null;
		boolean res = true;
		
		numOR = new int[numZones];
		for(i=0;i<numZones;i++)
			numOR[i]=onramplinkID.get(i).size();
		
		// Read user input .................
		MIN_METERING_RATE = 180;
		MAX_METERING_RATE = 900;
		
		// Allocate variables:
		swarm_slope_densities 			= new double[numZones][SWARM_SLOPE_SAMPLE_SIZE];
		swarm_slope_forecast_densities 	= new double[numZones][SWARM_SLOPE_SAMPLE_SIZE];
		swarm_densities 			 	= new double[numZones][SWARM_DENSITY_SAMPLE_SIZE];
		initial_slope 				 	= new double[numZones][SWARM_DENSITY_SAMPLE_SIZE];
		bn_saturation_density 			= new double[numZones];
		slope 					 	 	= new double[numZones];
		forecast_slope 				 	= new double[numZones];
		density_pre 				 	= new double[numZones];
		density_post 				 	= new double[numZones];
		variance_pre 				 	= new double[numZones];
		variance_post 				 	= new double[numZones];
		density_forecast_post 		 	= new double[numZones];
		density_forecast 			 	= new double[numZones];
		swarm_bn_required_density 	 	= new double[numZones];
		sigma  						 	= new double[numZones][];
		mlup_saturation_density 		= new double[numZones][];
		mldn_saturation_density 		= new double[numZones][];
		for(i=0;i<numZones;i++){
			sigma[i] 				   = new double[numOR[i]];
			mlup_saturation_density[i] = new double[numOR[i]];
			mldn_saturation_density[i] = new double[numOR[i]];
		}
		
		controlindex = new int[numZones][];
		for(i=0;i<numZones;i++)
			controlindex[i] = new int[numOR[i]];
		
		// Allocate monitor maps
		bn2monitor = new int[numZones];
		or2monitor = new int[numZones][];
		mlup2monitor = new int[numZones][]; 
		mldn2monitor = new int[numZones][]; 
		for(i=0;i<numZones;i++){
			or2monitor[i] = new int[numOR[i]];
			mlup2monitor[i] = new int[numOR[i]];
			mldn2monitor[i] = new int[numOR[i]];
		}

		// Allocate geometry
		bnpm 		= new double[numZones];
		mluppm 		= new double[numZones][];
		mldnpm 		= new double[numZones][];
		orpm 		= new double[numZones][];
		bnlanenum 	= new int[numZones];
		mluplanenum = new int[numZones][];
		mldnlanenum = new int[numZones][];
		orlanenum 	= new int[numZones][];
		orsignum 	= new int[numZones][];
		for(i=0;i<numZones;i++){
			mluppm[i] 	   = new double[numOR[i]];
			mldnpm[i] 	   = new double[numOR[i]];
			orpm[i] 	   = new double[numOR[i]];
			mluplanenum[i] = new int[numOR[i]];
			mldnlanenum[i] = new int[numOR[i]];
			orlanenum[i]   = new int[numOR[i]];
			orsignum[i]    = new int[numOR[i]];
		}
		
		// Allocate outputs
		swarm1rate    = new double[numZones][];
		swarm2arate	  = new double[numZones][];
		swarm2brate	  = new double[numZones][];
		//controloutput = new double[numZones][];
		for(i=0;i<numZones;i++){
			swarm1rate[i] 	 = new double[numOR[i]];
			swarm2arate[i] 	 = new double[numOR[i]];
			swarm2brate[i] 	 = new double[numOR[i]];
			//controloutput[i] = new double[numOR[i]];
		}

		bndent  = new double[numZones];
		mlupspeed = new double[numZones][];
		mlupdent  = new double[numZones][];
		mldndent  = new double[numZones][];
		orqt 	= new double[numZones][];
		for(i=0;i<numZones;i++){
			mlupspeed[i] = new double[numOR[i]];
			mlupdent[i]  = new double[numOR[i]];
			mldndent[i]  = new double[numOR[i]];
			orqt[i] 	 = new double[numOR[i]];
		}

		// Initialize .............................................................
		pollingnumber = 0;
		
		// Generate simple controllers on each onramp.
		if (!initialize())
			return false;
		for (i = 0; i < zones.size(); i++) {
			Vector<AbstractLink> ors = zones.get(i).getOnramps();
			for (j = 0; j < ors.size(); j++) {
				controloutput.add(new Double(0));
				AbstractNodeSimple nd = (AbstractNodeSimple)ors.get(j).getEndNode();
				int idx = nd.getPredecessors().indexOf(ors.get(j));
				controlindex[i][j] = idx;
				if (controlindex[i][j] < 0)
					return false;
			}
		}
		
		for(i=0;i<numZones;i++){
			abslnk = myMonitor.getMyNetwork().getLinkById(bottlenecklinkID.get(i));
			up = (NodeFreeway) abslnk.getBeginNode();
			dn = (NodeFreeway) abslnk.getEndNode();
			bnpm[i] = (up.getPostmile() + dn.getPostmile())/2.0;
			bnlanenum[i] = (int) ((AbstractLinkHWC) abslnk).getLanes();
			bn_saturation_density[i] = 10.0;// GCG FIX THIS
			for(j=0;j<numOR[i];j++){
				abslnk = myMonitor.getMyNetwork().getLinkById(onramplinkID.get(i).get(j));		// onramp link
				vnetel= abslnk.getSuccessors();											// onramp end node
				if(vnetel.size()!=1){
					return false;
				}
				else{
					orpm[i][j] =  ((NodeFreeway) vnetel.get(0)).getPostmile();	
				}
				orlanenum[i][j] = (int) ((AbstractLinkHWC) abslnk).getLanes();
				orsignum[i][j] = orlanenum[i][j];		// GCG FIX THIS
				sigma[i][j] = 1;// GCG FIX THIS
				mlup_saturation_density[i][j] = 0.0;
				mlup_saturation_density[i][j] = 0.0;
			}
		}
				
		// Upstream and downstream mainline links ................................
		for(i=0;i<numZones;i++){
			upmainlinelinkID.add( new Vector<Integer>() );
			dnmainlinelinkID.add( new Vector<Integer>() );
			for(j=0;j<numOR[i];j++){
				upmainlinelinkID.get(i).add(UpstreamMainlineLink(onramplinkID.get(i).get(j)));
				dnmainlinelinkID.get(i).add(DnstreamMainlineLink(onramplinkID.get(i).get(j)));
			}
		}

		// mainline postmiles = average of upstream and downstream nodes
		for(i=0;i<numZones;i++){
			for(j=0;j<numOR[i];j++){
				mid = (NodeFreeway) myMonitor.getMyNetwork().getLinkById(onramplinkID.get(i).get(j)).getEndNode();	// onramp end node
				abslnk =  myMonitor.getMyNetwork().getLinkById(upmainlinelinkID.get(i).get(j));	// up mainline link
				if(abslnk!=null){
					up = (NodeFreeway) abslnk.getPredecessors().get(0);
					dn = (NodeFreeway) abslnk.getSuccessors().get(0);
					mluppm[i][j] = (up.getPostmile() + dn.getPostmile())/2.0;
					mluplanenum[i][j] = (int) ((AbstractLinkHWC) abslnk).getLanes();
				}
				else{
					mluppm[i][j] = mid.getPostmile();
					mluplanenum[i][j] = -1;
				}
				
				abslnk =  myMonitor.getMyNetwork().getLinkById(dnmainlinelinkID.get(i).get(j));	// dn mainline link
				if(abslnk!=null){
					up = (NodeFreeway) abslnk.getPredecessors().get(0);
					dn = (NodeFreeway) abslnk.getSuccessors().get(0);
					mldnpm[i][j] = (up.getPostmile() + dn.getPostmile())/2.0;
					mldnlanenum[i][j] = (int) ((AbstractLinkHWC) abslnk).getLanes();
				}
				else{
					mldnpm[i][j] = mid.getPostmile();
					mldnlanenum[i][j] = -1;
				}
			}
		}	
		
		// construct monitor maps
		for(i=0;i<numZones;i++){
			bn2monitor[i] = ((MonitorControllerHWC) myMonitor).getLinkIndex(bottlenecklinkID.get(i)); 
			for(j=0;j<numOR[i];j++){
				or2monitor[i][j]   = ((MonitorControllerHWC) myMonitor).getLinkIndex(onramplinkID.get(i).get(j)); 
				mlup2monitor[i][j] = ((MonitorControllerHWC) myMonitor).getLinkIndex(upmainlinelinkID.get(i).get(j));  
				mldn2monitor[i][j] = ((MonitorControllerHWC) myMonitor).getLinkIndex(dnmainlinelinkID.get(i).get(j));    
			}
		}
		
		// GG temporary. If the upstream map is empty, assign the downstream 
		for(i=0;i<numZones;i++){
			for(j=0;j<numOR[i];j++){
				if(mlup2monitor[i][j]==-1)
					mlup2monitor[i][j]=mldn2monitor[i][j];  
			}
		}
		
		
		return res;
		
	}
	
	/**
	 * Initializes slave controllers.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public boolean initialize() {
		Vector<AbstractNetworkElement> nes = myMonitor.getSuccessors();	
		for(int j = 0; j < nes.size(); j++) {
			if ((nes.get(j).getType() & TypesHWC.MASK_LINK) == 0)
				continue;
			AbstractLink lnk = (AbstractLink)nes.get(j);
			AbstractNodeSimple nd = (AbstractNodeSimple)lnk.getEndNode();
			int idx = nd.getPredecessors().indexOf(lnk);
			ControllerSlave ctrl = new ControllerSlave(this, lnk, idx);
			if ((nd.getSimpleController(lnk) == null) || (!nd.getSimpleController(lnk).getClass().getName().equals(ctrl.getClass().getName()))) {
				if (nd.getSimpleController(lnk) != null)
					nd.getSimpleController(lnk).setDependent(false);
				nd.setSimpleController(ctrl, lnk);
			}
		}
		return true;
	}

	/**
	 * Generates XML description of the SWARM controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		out.print("<components swarm1=\"" + SWARM1 + "\" swarm2a=\"" + SWARM2A + "\" swarm2b=\"" + this.SWARM2B + "\"/>\n");
		out.print("<parameters density_sample_size=\"" + SWARM_DENSITY_SAMPLE_SIZE + "\" epsilon=\"" + epsilon + "\" forecast_lead_time=\"" + SWARM_FORECAST_LEAD_TIME + "\" input_var_lane=\"" + input_var_lane + "\" meas_var_lane=\"" + meas_var_lane + "\" phi=\"" + swarm_phi + "\" psi=\"" + swarm_psi + "\" sat_den_multiplier=\"" + SWARM_SAT_DEN_NUMBER + "\" sat_smoother=\"" + sat_smoother + "\" slope_sample_size=\"" + SWARM_SLOPE_SAMPLE_SIZE + "\"/>\n");
		out.print("<limits cmax=\"" + limits.get(1) + "\" cmin=\"" +  limits.get(0) + "\"/>\n");
		out.print("<zones>\n");
		for(int i = 0; i < zones.size(); i++){
			out.print("<zone bottlenecklink=\"" + zones.get(i).getBottleneckLink().getId() + "\" ");
			out.print("fromonramp=\"" + zones.get(i).getFromOnramp().getId() + "\" toonramp=\"" + zones.get(i).getToOnramp().getId() + "\" sat_den_multiplier=\"" + zones.get(i).getSaturationDensityMultiplier() + "\"/>\n");
		}
		out.print("</zones>\n");
		out.print("</controller>\n");
		return;
	}
	
	
	
	/**
	 * This class implements SWARM Zone.
	 */
	public class Zone {
		private AbstractLink bottleneck = null;
		private AbstractLink fromRamp = null;
		private AbstractLink toRamp = null;
		private Vector<AbstractLink> onramps = new Vector<AbstractLink>(); 
		private double sdm = 1;
		
		public AbstractLink getBottleneckLink() { return bottleneck; }
		public Vector<AbstractLink> getOnramps() { return onramps; }
		public AbstractLink getFromOnramp() { return fromRamp; }
		public AbstractLink getToOnramp() { return toRamp; }
		public double getSaturationDensityMultiplier() { return sdm; }
		
		private void setOnramps() {
			if (myMonitor == null)
				return;
			onramps.clear();
			Vector<AbstractNetworkElement> nes = myMonitor.getSuccessors();
			int bi = nes.indexOf(fromRamp);
			int ei = nes.indexOf(toRamp);
			if ((bi >= 0) && (ei >= 0) && (bi <= ei))
				for (int i = bi; i <= ei; i++)
					onramps.add((AbstractLink)nes.get(i));
			return;
		}
		
		public void setBottleneckLink(AbstractLink lnk) {
			if (lnk != null)
				bottleneck = lnk;
			return;
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
		
		public void setSaturationDensityMultiplier(double x) {
			if (x >= 0)
				sdm = x;
			return;
		}
		
		public Zone clone() {
			Zone nz = new Zone();
			nz.setBottleneckLink(bottleneck);
			nz.setFromOnramp(fromRamp);
			nz.setToOnramp(toRamp);
			nz.setSaturationDensityMultiplier(sdm);
			return nz;
		}
	}
	
}
