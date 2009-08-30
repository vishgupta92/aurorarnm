package aurora.hwc.control.signal;

import java.util.Vector;

import org.w3c.dom.Node;

import aurora.ExceptionConfiguration;
import aurora.ExceptionDatabase;
import aurora.ExceptionSimulation;

public class ControllerCoordinated extends ControllerActuated {
	private static final long serialVersionUID = 7184227738369839593L;

//	 ========================================================================
//	 FIELDS =================================================================
//	 ========================================================================

	private ControllerPretimed pretimed;
	private Vector< Vector < Boolean > > requestforceoff;	// [intersection X 8] ordered by interIndexToId
	private Vector< Vector < Boolean > > requesthold;		// [intersection X 8] ordered by interIndexToId
	private Vector< Float > syncpoint;						// [intersection] ordered by interIndexToId
	

//	 ========================================================================
//	 CONSTRUCTORS ===========================================================
//	 ========================================================================
	public ControllerCoordinated(){
	}

//	 ========================================================================
//	 INTERFACE ==============================================================
//	 ========================================================================
	public void setRequestforceoff(int i,int j,boolean v){requestforceoff.get(i).set(j,v);}
	public void setRequesthold(int i,int j,boolean v){requesthold.get(i).set(j,v);}
	public float Syncpoint(int i){ return syncpoint.get(i); }
	public ControllerPretimed Pretimed() { return pretimed; }
	
//	 ========================================================================
//	 METHODS AND OVERRIDES ==================================================
//	 ========================================================================
	
	public boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {

		// Issue coordination point force offs
		pretimed.dataUpdate(ts);

		// Communicate coordination hold and forceoffs to ASC
		externalforceoffrequest = requestforceoff;
		externalholdrequest = requesthold;

		// Update ASC phases
		if(!super.dataUpdate(ts))
			return false;

		// Communicate serviced coordination holds and force-offs and holds back to coordinator
		requestforceoff = externalforceoffrequest;
		requesthold = externalholdrequest;
		
		return true;
	}	
//	-------------------------------------------------------------------------
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
 		boolean res = super.initFromDOM(p);
 		if(!res)
 			return res;
		pretimed = new ControllerPretimed(this);
		pretimed.setMyMonitor(myMonitor);
		if(!pretimed.initFromDOM(p)) 
			return false;

		return true;
		
	}
//	-------------------------------------------------------------------------
	public boolean validate() throws ExceptionConfiguration {
		int i,j;
		//boolean areequal;
		
		if( !super.validate() )
			return false;

		if(!pretimed.validate())
			return false;

		pretimed.coordmode = true;
		
		// Check that actuated and pretimed junctions coincide
		/*
		Vector<Integer> interIndexToIdCopy = new Vector<Integer>(interIndexToId);
		areequal = true;
		Iterator<NodeUJSignal> it = (Iterator<NodeUJSignal>) pretimed.junctionIDs.iterator();
	    for (; it.hasNext(); ) 
	    	if(!interIndexToIdCopy.removeElement(it.next().getId()))
	    		areequal = false;
	    if(interIndexToIdCopy.size()>0)
	    	areequal = false;
	    if(!areequal)
	    	return false;
		*/
		
	    // allocate syncpoint, requestforceoff, requesthold
		syncpoint = new Vector< Float >();
		requestforceoff = new Vector< Vector < Boolean > > ();
		requesthold = new Vector< Vector < Boolean > > ();
		for(i=0;i<numintersections ;i++){
			syncpoint.add(-1.0f);
			requestforceoff.add(new Vector<Boolean>());
			requesthold.add(new Vector<Boolean>());
			for(j=0;j<8;j++){
				requestforceoff.get(i).add(false);
				requesthold.get(i).add(false);
			}
		}

		SetSyncPoints();
		
		return true;
	}
//	-------------------------------------------------------------------------
	public void SetSyncPoints(){
		float val;
		int intid,index;

		for(index=0;index<numintersections;index++){
			intid = interIndexToId.get(index);
	    	ControllerPretimedIntersectionPlan p = pretimed.getIntPlanByIntId(intid);
	    	val = Math.min(p.forceoffpoint[p.movA.get(0)] , p.forceoffpoint[p.movB.get(0)] );
			syncpoint.set(index,val);
		}
	}
	
}
