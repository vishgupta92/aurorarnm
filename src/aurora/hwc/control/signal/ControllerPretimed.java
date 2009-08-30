package aurora.hwc.control.signal;

import java.io.*;
import java.util.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aurora.ErrorConfiguration;
import aurora.ExceptionConfiguration;
import aurora.ExceptionDatabase;
import aurora.ExceptionSimulation;
import aurora.util.Util;

public class ControllerPretimed extends BaseSignalController {
	private static final long serialVersionUID = -6882226238935473286L;

//	 ========================================================================
//	 FIELDS =================================================================
//	 ========================================================================
	ControllerCoordinated coordcont;
	boolean coordmode;					  // true if this is used for coordination (softforceoff only)
	int numplans;						  // total number of defined plans
	Vector<ControllerPretimedPlan> plan;  // array of plans
	Vector<Integer> plansequence;		  // Ordered list of plans to implement 
	Vector<Integer> planstarttime;		  // Implementation times (first should be 0, should be increasing)
	float transdelay;					  // transition time between plans.
	int cplan;							  // current plan id
	int cperiod;						  // current index to planstarttime and plansequence
	//public HashSet<NodeUJSignal> junctionIDs;
	
//	 ========================================================================
//	 CONSTRUCTORS ===========================================================
//	 ========================================================================
	public ControllerPretimed() {
		plansequence = new Vector<Integer>();
		planstarttime = new Vector<Integer>();
		numplans = 0;
		plan = new Vector<ControllerPretimedPlan>();
	};
//	 ------------------------------------------------------------------------
	public ControllerPretimed(ControllerCoordinated m) {
		this();
		coordcont = m;
	};
//	 ------------------------------------------------------------------------
	public int AddPlan(int planid,float cyclelength){
		plan.add(new ControllerPretimedPlan(this,planid,cyclelength));
		numplans++;
		return plan.size()-1;
	}
//	 ========================================================================
//	 INTERFACE ==============================================================
//	 ========================================================================
	public boolean getCoordMode() { return coordmode; };
//	 ------------------------------------------------------------------------
	public int getNumPlans() { return numplans; };
//	 ------------------------------------------------------------------------
	public Vector<ControllerPretimedPlan> getPlan() {return plan; };
//	 ------------------------------------------------------------------------
	public int getNumPeriods(){ return planstarttime.size();	};
//	 ------------------------------------------------------------------------
	public Vector<Integer> getPlanSequence(){ return plansequence;	};
//	 ------------------------------------------------------------------------
	public Vector<Integer> getPlanStarttime(){ return planstarttime; };
//	 ------------------------------------------------------------------------
	public float getTransDelay(){ return transdelay; };
//	 ------------------------------------------------------------------------
	public int getCurrPlan(){ return cplan;};
//	 ------------------------------------------------------------------------
	public int getCurrPeriod(){ return cperiod; };
//	-------------------------------------------------------------------------
	public ControllerPretimedIntersectionPlan getIntPlanByIntId(int id){
		ControllerPretimedPlan currplan = plan.get(cplan);
		for(int i=0;i<currplan.numinters;i++){
			ControllerPretimedIntersectionPlan z = currplan.intersplan.get(i);
			if(z.myIntersectionID==id)
				return z;
		}
		return null;
	}
//	-------------------------------------------------------------------------
	public int getPlanInd(int planid)
	{
		int i;
		if(planid==0)
			return -1;
		for(i=0;i<numplans;i++){
			if(plan.get(i).myID==planid)
				return i;
		}
		return -1;
	}
//	-------------------------------------------------------------------------
	public int getStage(int planind, int inters,int nema)
	{
		int s;
		for(s=0;s<plan.get(planind).intersplan.get(inters).numstages;s++){
			ControllerPretimedIntersectionPlan p = plan.get(planind).intersplan.get(inters);
			if( p.movA.get(s) == nema || p.movB.get(s) == nema )
				return s;
		}
		return -1;
	}
//	 ========================================================================
//	 METHODS ================================================================
//	 ========================================================================
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {

		if(!super.dataUpdate(ts))
			return false;
		
		float simtime = (float) (3600.0*myMonitor.getMyNetwork().getSimTime());

		// time to switch plans .....................................
		if( cperiod < planstarttime.size()-1 ){
			if( simtime > planstarttime.get(cperiod+1) + transdelay -0.001 ){
				cperiod++;
				cplan = getPlanInd(plansequence.get(cperiod));
				if(plansequence.get(cperiod)==0){
					// GCG asc.ResetSignals();  GG FIX THIS
				}
				if(coordmode)
					coordcont.SetSyncPoints();
					
			}
		}

		if( plansequence.get(cperiod)==0 )
			ImplementASC();
		else
			plan.get(cplan).ImplementPlan(simtime,coordmode);
		
		return true;
	}
//	-------------------------------------------------------------------------
	void ImplementASC()
	{
		//asc.Update();
	}
//	-------------------------------------------------------------------------
	boolean IsPretimedPlanID(int id)
	{
		int i;
		for(i=0;i<numplans;i++){
			if(plan.get(i).myID==id)
				return true;
		}
		return false;
	}
//	 ========================================================================
//	 I/O ====================================================================
//	 ========================================================================
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
 		boolean res = super.initFromDOM(p);
 		if(!res)
 			return res;
		if (p == null)
			return false;
		try  {
			int i, j;
			int planid;
			float cyclelength;
			int planindex;
			
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();

				for (i = 0; i < pp.getLength(); i++) {
					
					if (pp.item(i).getNodeName().equals("parameters")){
						if (pp.item(i).hasChildNodes()) {
							NodeList pp2 = pp.item(i).getChildNodes();
							for (j = 0; j < pp2.getLength(); j++){
								
								if (pp2.item(j).getNodeName().equals("planstarttime")) {
									Node x = pp2.item(j).getAttributes().getNamedItem("value");
									if(x==null)
										return false;
									StringTokenizer st = new StringTokenizer(x.getNodeValue(), ", \t");
									while (st.hasMoreTokens()) {
										planstarttime.add(Integer.parseInt(st.nextToken()));
									}
								}

								if (pp2.item(j).getNodeName().equals("plansequence")) {
									Node x = pp2.item(j).getAttributes().getNamedItem("value");
									if(x==null)
										return false;
									StringTokenizer st = new StringTokenizer(x.getNodeValue(), ", \t");
									while (st.hasMoreTokens()) {
										plansequence.add(Integer.parseInt(st.nextToken()));
									}
								}

								if (pp2.item(j).getNodeName().equals("transdelay")) {
									Node x = pp2.item(j).getAttributes().getNamedItem("value");
									if(x==null)
										return false;
									transdelay = Float.parseFloat(x.getNodeValue());
								}
							}	// loop pp2
						}
						else
							res = false;
					}	// end "parameters"

					if (pp.item(i).getNodeName().equals("plan")){
						planid = Integer.parseInt(pp.item(i).getAttributes().getNamedItem("id").getNodeValue());
						cyclelength = Float.parseFloat(pp.item(i).getAttributes().getNamedItem("cyclelength").getNodeValue());
						planindex = AddPlan(planid,cyclelength);
						res &= plan.get(planindex).initFromDOM(pp.item(i));
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
//	-------------------------------------------------------------------------
	public boolean validate() throws ExceptionConfiguration {

		int i;
		boolean res = super.validate();
		
		if(!res)
			return res;
		
		if(planstarttime.size()!=plansequence.size()){
			myMonitor.getMyNetwork().addConfigurationError(new ErrorConfiguration(this, "Pretimed controller: planstarttime and plansequaence must have equal length"));
			res = false;
		}
		
		// Check planstarttime[0]=0, and rest are strictly increasing.............
		if(planstarttime.get(0)!=0){
			myMonitor.getMyNetwork().addConfigurationError(new ErrorConfiguration(this, "Pretimed controller: First planstarttime must be 0"));
			res = false;
		}
		
		if(planstarttime.size()>1)
			for(i=1;i<planstarttime.size();i++)
				if(planstarttime.get(i)<=planstarttime.get(i-1)){
					myMonitor.getMyNetwork().addConfigurationError(new ErrorConfiguration(this, "Pretimed controller: planstarttime must be strictly increasing"));
					res = false;
				}

		// Check that plansequence has valid plan ids ............................
		for(i=0;i<plansequence.size();i++)
			if( !IsPretimedPlanID(plansequence.get(i)) && plansequence.get(i)!=0 ){
				myMonitor.getMyNetwork().addConfigurationError(new ErrorConfiguration(this, "Pretimed controller: Invalid plan id in plansequence."));
				res = false;
			}
	        
		// Initialize intersection plans
		for(i=0;i<numplans;i++){
			if(plan.get(i).myID==0){
				myMonitor.getMyNetwork().addConfigurationError(new ErrorConfiguration(this, "Pretimed controller: plan may not have id=0"));
				res = false;
			}
			res &= plan.get(i).validate();
		}
		
		return res;
	}
//	-------------------------------------------------------------------------
	public void xmlDump(PrintStream out,int indentlevel) throws IOException {

		int i,j,k;
		String x1 = Util.xmlindent(indentlevel);
		String x2 = Util.xmlindent(indentlevel+1);
		String x3 = Util.xmlindent(indentlevel+2);
		String x4 = Util.xmlindent(indentlevel+3);
		
		out.print(x1 + "<controller class=\"" + this.getClass().getName() + "\">\n");
		out.print(x2 + "<parameters>\n");
		out.print(x3 + "<plansequence value=\"" + Util.csvstringint(plansequence) + "\"> </plansequence>\n");
		out.print(x3 + "<planstarttime value=\"" + Util.csvstringint(planstarttime) + "\"> </planstarttime>\n");
		out.print(x3 + "<transdelay value=\"" + transdelay + "\"> </transdelay>\n");
		out.print(x2 + "</parameters>\n");
		
		for(i=0;i<numplans;i++){
			ControllerPretimedPlan p = getPlan().get(i);
			out.print(x2 + "<plan id=\"" + p.myID + "\" cyclelength=\"" + p.cyclelength + "\">\n");
			for(j=0;j<p.numinters;j++){
				ControllerPretimedIntersectionPlan z = p.getIntersPlan().get(j);
				out.print(x3 + "<intersection id=\"" + z.myIntersectionID + "\" offset=\"" + z.offset + "\">\n");
				for(k=0;k<z.numstages;k++){
					out.print(x4 + "<stage movA=\"" + (z.movA.get(k)+1) + "\" movB=\"" + (z.movB.get(k)+1) + "\" greentime=\"" + z.greentime.get(k) + "\"> </stage>\n");
				}
				out.print(x3 + "</intersection>\n");
			}
			out.print(x2 + "</plan>\n");
		}
		out.print(x1 + "</controller>\n");
		return;
	
	}
//	 ========================================================================
//	 OVERRIDES ==============================================================
//	 ========================================================================
		public String getDescription() {
		return "Pretimed Controller";
	}
//	 ========================================================================
//	 TABLE DISPLAY  =========================================================
//	 ========================================================================
	public class PRETIMEDdataRow {
		private int planid;
		private double starttime;
		
		public PRETIMEDdataRow(){ planid = 0; starttime = 0; }
		public PRETIMEDdataRow(double s, int p) { planid = p; starttime = s; }
		
		public int getPlanID() {
			return planid;
		}
		
		public double getStartTime() {
			return starttime;
		}
		
		public void setStartTime(int h, int m, int s){
			if ((h >= 0) && (m >= 0) && (s >= 0))
				starttime = h + m/60.0 + s/3600.0;
			return;
		}
		
		public void setStartTime(double t) {
			if (t >= 0.0)
				starttime = t;
			return;
		}
		
		public void setPlanID(double x){
			if (x >= 0.0)
				planid = (int) x;
			return; 
		}	
	}
//	-------------------------------------------------------------------------
	public Vector<PRETIMEDdataRow> getTable() {
		Vector<PRETIMEDdataRow> x = new Vector<PRETIMEDdataRow>();
		for(int i=0;i<plansequence.size();i++){
			x.add( new PRETIMEDdataRow(planstarttime.get(i)/3600.0,plansequence.get(i)));
		}
		return x;
	}
//	-------------------------------------------------------------------------
	public void setTable(Vector<PRETIMEDdataRow> inTable){
		if (inTable == null)
			return;
		planstarttime.clear();
		plansequence.clear();
		double x;
		for(int i=0;i<inTable.size();i++){
			plansequence.add(inTable.get(i).getPlanID());
			x = inTable.get(i).getStartTime();
			planstarttime.add((int)(3600.0*x));
		}
	}
//	-------------------------------------------------------------------------
	public synchronized void resetTimeStep() {
		
		super.resetTimeStep();
		
		cperiod= 0;
		cplan = getPlanInd(plansequence.get(0));


		/*  GCG CHECK THIS
		if(asc!=null){
			if(!asc.Initialize())
				return false;
		} */

		
		return;
	}

}
