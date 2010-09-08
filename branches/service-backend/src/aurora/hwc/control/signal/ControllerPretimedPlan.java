package aurora.hwc.control.signal;

import java.io.Serializable;
import java.util.Vector;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import aurora.ExceptionConfiguration;
import aurora.hwc.NodeUJSignal;

public class ControllerPretimedPlan implements Serializable {
	private static final long serialVersionUID = -5877782995977921568L;
	
	//	 ========================================================================
//	 FIELDS =================================================================
//	 ========================================================================
	public ControllerPretimed myController;
	public int myID;
	public float cyclelength;
	
	public int numinters;
	public Vector<ControllerPretimedIntersectionPlan> intersplan;
	
//	 ========================================================================
//	 CONSTRUCTORS ===========================================================
//	 ========================================================================
	public ControllerPretimedPlan(ControllerPretimed m,int planid,float c) 
	{
		myController = m;
		myID = planid;
		cyclelength = c;
		intersplan = new Vector<ControllerPretimedIntersectionPlan>();
		numinters = 0;
	}
// --------------------------------------------------------------------------
	public int AddIntersectionPlan(SignalManager s,float c,float o){
		intersplan.add(new ControllerPretimedIntersectionPlan(s,c,o));
		numinters++;
		return intersplan.size()-1;
	}
//	 ========================================================================
//	 INTERFACE ==============================================================
//	 ========================================================================
	public int getID(){ return myID; };
//	 --------------------------------------------------------------------------
	public int getNumInters() { return numinters; };
//	 --------------------------------------------------------------------------
	public Vector<ControllerPretimedIntersectionPlan> getIntersPlan(){ return intersplan; };
//	 --------------------------------------------------------------------------
	public ControllerPretimedIntersectionPlan getPlanByIntID(int intid){ 
		for(int i=0;i<intersplan.size();i++){
			if(intersplan.get(i).getIntId()==intid)
				return intersplan.get(i);
		}
		return null; 
	};
	
//	 ========================================================================
//	 METHODS ================================================================
//	 ========================================================================
	public void ImplementPlan(float simtime,boolean coordmode){

		int i,j;
		float itime;
		float reltime;
		boolean issyncphase;
		ControllerCoordinated c = myController.coordcont;

		// Master clock .............................
		itime =  simtime % cyclelength;	// GCG CHECK THIS
		
		// Loop through intersections ...............
		for(i=0;i<intersplan.size();i++){

			ControllerPretimedIntersectionPlan intplan = intersplan.get(i);

			// Compute time relative to offset ...............................
			reltime = itime - intplan.offset;
			if(reltime<0)
				reltime += cyclelength;

			for( j=0;j<8;j++ ){

				// Used for non-actuated pre-timed
				if( !coordmode ){
					if( reltime==intplan.holdpoint[j] )
						intplan.mySigMan.IssueHold(j);

					if( reltime==intplan.forceoffpoint[j] )
						intplan.mySigMan.IssueForceOff(j,intplan.mySigMan.Phase(j).actualyellowtime,intplan.mySigMan.Phase(j).actualredcleartime);
				}

				// Used for coordinated actuated.
				if( coordmode ){

					if( !intplan.mySigMan.Phase(j).Protected() )
						continue;

					issyncphase = j==intplan.movA.get(0) || j==intplan.movB.get(0);

					// Non-persisting forceoff request at forceoffpoint
					if( reltime==intplan.forceoffpoint[j] )
						c.setRequestforceoff(i, j, true);

					// Hold request for sync phase if
					// currently both sync phases are active
					// and not reached syncpoint
					if( issyncphase && 
						c.PhaseA(i)!=null && c.PhaseA(i).MyNEMA()==intplan.movA.get(0) && 
						c.PhaseB(i)!=null && c.PhaseB(i).MyNEMA() == intplan.movB.get(0) &&
						reltime!= c.Syncpoint(i) )
						c.setRequesthold(i, j, true);
				}
			}
		}
	}

//	 --------------------------------------------------------------------------
	/*
	T_PRE_Plan& operator=(const T_PRE_Plan& that)
	{
		if(this==&that)
			return *this;

		AlgorithmPretimedIntersectionPlan iinters = new AlgorithmPretimedIntersectionPlan[that.numinters];

		for(int i=0;i<that.numinters;i++){
			AlgorithmPretimedIntersectionPlan a = iinters[i];
			AlgorithmPretimedIntersectionPlan b = that.inters[i];

			a.hasdata = b.hasdata;	
			a.offset =b.offset;		
			a.numstages = b.numstages;			
			a.movA = b.movA;
			a.movB = b.movB;	
			a.greentime = b.greentime;
			a.actualyellow = b.actualyellow;
			a.actualredclear = b.actualredclear;
			a.stagelength = b.stagelength;
			a.holdpoint = b.holdpoint;
			a.forceoffpoint = b.forceoffpoint;
			a.myPlan = b.myPlan;
			a.myInt = b.myInt;
			a.cyclelength = b.cyclelength;
		}

		if( inters!=NULL )
			delete [] inters;
		inters = iinters;
		myID = that.myID;
		cyclelength = that.cyclelength;
		numinters = that.numinters;

		return *this;
	}
	*/

//	-------------------------------------------------------------------------
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {

		boolean res = true;
		if (p == null)
			return false;
		try  {
			
			int i;
			int intid;
			float offset;
			int intplanind;
			SignalManager sigman;
			
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (i = 0; i < pp.getLength(); i++) {
					if (pp.item(i).getNodeName().equals("intersection")){
						intid = Integer.parseInt(pp.item(i).getAttributes().getNamedItem("id").getNodeValue());
						offset = Float.parseFloat(pp.item(i).getAttributes().getNamedItem("offset").getNodeValue());
						sigman = ((NodeUJSignal) myController.getMyMonitor().getMyNetwork().getNodeById(intid)).getSigMan();
						intplanind = AddIntersectionPlan(sigman,cyclelength,offset);
						res &= intersplan.get(intplanind).initFromDOM(pp.item(i));
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
	public boolean validate(){
		int i;
		boolean res = true;
		for(i=0;i<intersplan.size();i++)
			res &= intersplan.get(i).validate();
		return res;
	}
//	-------------------------------------------------------------------------
	
	
}
