package aurora.hwc.control.signal;

import java.io.*;
import java.util.Vector;
import aurora.AbstractControllerComplex;
import aurora.AbstractControllerSimple;
import aurora.AbstractLink;
import aurora.AbstractNodeSimple;
import aurora.ExceptionConfiguration;
import aurora.hwc.AbstractLinkHWC;
import aurora.hwc.NodeUJSignal;

public class SignalManager implements Serializable {
	private static final long serialVersionUID = 7451857824163518318L;
	
//	 ========================================================================
//	 FIELDS =================================================================
//	 ========================================================================

	public NodeUJSignal myNode;
	public BaseSignalController myController;
	
	private Vector<SignalPhase> phase = new Vector<SignalPhase>();
	private boolean validated = false;
	
//	class RightPhase rphase[4];

	// Detector memory
	boolean[] hasstoplinecall = new boolean[8];
	boolean[] hasapproachcall = new boolean[8];
	boolean[] hasconflictingcall = new boolean[8];
	float[] conflictingcalltime = new float[8];

	// Controller memory
	boolean[] hold = new boolean[8];
	boolean[] forceoff = new boolean[8];
	boolean[] display_hold = new boolean[8];
	boolean[] display_forceoff = new boolean[8];

	// Safety
	boolean[] permitopposinghold = new boolean[8];
	boolean[] permithold = new boolean[8];

	int[] numapproachloops = new int[8];
//	int[] maxapproachcalls = new int[8];

//	 ========================================================================
//	 CONSTRUCTORS ===========================================================
//	 ========================================================================
	public SignalManager (NodeUJSignal n) {
		int i;
		myNode = n;
		for(i=0;i<8;i++){
			phase.add( new SignalPhase(myNode,this,i) );
			numapproachloops[i] = 0;
			hasstoplinecall[i] = false;
			hasapproachcall[i] = false;
			hasconflictingcall[i] = false;
			hold[i] = false;
			forceoff[i] = false;
			display_hold[i] = false;
			display_forceoff[i] = false;
			permitopposinghold[i] = true;
			permithold[i] = true;
//			maxapproachcalls[i] = 0;
			conflictingcalltime[i] = 0.0f;
		}
	}

//	 ========================================================================
//	 INTERFACE ==============================================================
//	 ========================================================================

	public BulbColor getPhaseColor(int i){
		if(i>=0 && i<8)
			return phase.get(i).BulbColor();
		else
			return BulbColor.DARK;
	}
//	-------------------------------------------------------------------------
	public SignalPhase Phase(int i) {
		if(i>=0 && i<8)
			return phase.get(i);
		else
			return null;
	}
//	-------------------------------------------------------------------------
	public boolean IssueHold(int nema)
	{
		if(nema>7 || nema<0)
			return false;
		hold[nema]=true;
		display_hold[nema]=true;
		return true;
	}
//	-------------------------------------------------------------------------
	public boolean IssueForceOff(int nema,float y,float r)
	{
		if(nema>7 || nema<0)
			return false;
		forceoff[nema]=true;
		display_forceoff[nema]=true;
		

		if(y>-0.001)
			phase.get(nema).actualyellowtime = y;
		else
			phase.get(nema).actualyellowtime = phase.get(nema).Yellowtime();

		if(r>-0.001)
			phase.get(nema).actualredcleartime = r;
		else
			phase.get(nema).actualredcleartime = phase.get(nema).Redcleartime();

		return true;
	}
//	-------------------------------------------------------------------------
	public boolean HasHold(int nema){
		if(nema>7 || nema<0)
			return false;
		return hold[nema];	
	}
//	-------------------------------------------------------------------------
	public boolean HasForceOff(int nema){
		if(nema>7 || nema<0)
			return false;
		return forceoff[nema];
	}
//	-------------------------------------------------------------------------
	public boolean DisplayHold(int nema){
		if(nema>7 || nema<0)
			return false;
		return display_hold[nema];
	}
//	-------------------------------------------------------------------------
	public boolean DisplayForceOff(int nema){
		if(nema>7 || nema<0)
			return false;

		return display_forceoff[nema];
	}
//	-------------------------------------------------------------------------
	public Vector<Boolean> getVecPermissive(){
		Vector<Boolean> a = new Vector<Boolean>();
		for(int i=0;i<8;i++)
			a.add(Phase(i).Permissive());
		return a;
	}
//	-------------------------------------------------------------------------
	public Vector<Boolean> getVecProtected(){
		Vector<Boolean> a = new Vector<Boolean>();
		for(int i=0;i<8;i++)
			a.add(Phase(i).Protected());
		return a;
	}
//	-------------------------------------------------------------------------
	public Vector<Boolean> getVecRecall(){
		Vector<Boolean> a = new Vector<Boolean>();
		for(int i=0;i<8;i++)
			a.add(Phase(i).Recall());
		return a;
	}
//	-------------------------------------------------------------------------
	public Vector<Float> getVecMingreen(){
		Vector<Float> a = new Vector<Float>();
		for(int i=0;i<8;i++)
			a.add(Phase(i).Mingreen());
		return a;
	}
//	-------------------------------------------------------------------------
	public Vector<Float> getVecYellowTime(){
		Vector<Float> a = new Vector<Float>();
		for(int i=0;i<8;i++)
			a.add(Phase(i).Yellowtime());
		return a;
	}
//	-------------------------------------------------------------------------
	public Vector<Float> getVecRedClearTime(){
		Vector<Float> a = new Vector<Float>();
		for(int i=0;i<8;i++)
			a.add(Phase(i).Redcleartime());
		return a;
	}
//	-------------------------------------------------------------------------
	public boolean Permithold(int nema){
		return permithold[nema];
	}
//	-------------------------------------------------------------------------
	public boolean Hasapproachcall(int nema){
		return hasapproachcall[nema];
	}
//	-------------------------------------------------------------------------
	public boolean Hasconflictingcall(int nema){
		return hasconflictingcall[nema];
	}
//	-------------------------------------------------------------------------
	public float Conflictingcalltime(int nema){
		return conflictingcalltime[nema];
	}
//	-------------------------------------------------------------------------
	public boolean Hasstoplinecall(int nema){
		return hasstoplinecall[nema];
	}

//	-------------------------------------------------------------------------
	public void RefreshDisplayValues()
	{
		int i;
		
		for(i=0;i<8;i++){
			display_hold[i] = false;
			display_forceoff[i] = false;
		}
	}
//	=========================================================================
//	METHODS =================================================================
//	=========================================================================
	public void validate()
	{
		if(validated)
			return;
		
		int i;

		// Attach network to bulb timers
		
		// Flag invalid movements on three-legged intersections
		for(i=0;i<8;i++){
			if(phase.get(i).Protected() || phase.get(i).Permissive()){
				phase.get(i).setValid(true);
			}
			else{
				phase.get(i).setValid(false);
			}
		}

		/*
		for(i=0;i<4;i++){
			if( rphase[i].ToLink==NULL || rphase[i].FromLink==NULL ){
				rphase[i].valid = false;
			}
			else{
				rphase[i].valid = true;
			}
		}
		*/

		// Read number of loops for approach stations
		for(i=0;i<8;i++){
			if( phase.get(i).ApproachStation() != null )
				numapproachloops[i] = phase.get(i).ApproachStation().NumLoops();
			else
				numapproachloops[i] = 0;
		}
		
		validated=true;	
	}
	
	/**
	 * Additional initialization.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration, ExceptionDatabase
	 */
	public boolean initialize() {
		int i,j;
		for(i = 0; i < 8; i++){
			hasstoplinecall[i] = false;
			hasapproachcall[i] = false;
			hasconflictingcall[i] = false;
			hold[i] = false;
			forceoff[i] = false;
			display_hold[i] = false;
			display_forceoff[i] = false;
			permitopposinghold[i] = true;
			permithold[i] = true;
			conflictingcalltime[i] = 0.0f;
			phase.get(i).SetRed();
			phase.get(i).bulbtimer.SetTo(0.0f);
				
			AbstractControllerComplex A = (AbstractControllerComplex) this.myController;
			Vector<AbstractLinkHWC> lnks = phase.get(i).getlinks();
			for(j=0;j<lnks.size();j++){
				AbstractNodeSimple nd = (AbstractNodeSimple)lnks.get(j).getEndNode();
				AbstractControllerSimple aa = nd.getSimpleController(lnks.get(j));
				if(aa!=null){
					int ii = A.getDependentControllerIndexOf(aa);
					phase.get(i).myControlIndex.set(j, ii);
				}
			}
		}
		
		/*
		for(i=0;i<4;i++){
			rphase[i].SetYellow();
		}*/
		return true;
	}
	
	
//	=========================================================================
//	UPDATE ==================================================================
//	=========================================================================
	public boolean Update()
	{
		UpdatePhases();
		BringPhaseCalls();			// GCG: this should strictly be run at the monitor level, before the controller update.
		UpdatePermittedHolds();		// GCG: this should strictly be run at the monitor level, before the controller update.
		UpdateSignalCommands();
		
		return true;
	}

//	-------------------------------------------------------------------------
	private void UpdatePhases()
	{
		for(int i=0;i<8;i++)
			phase.get(i).UpdateDetectorStations();
	}
//	-------------------------------------------------------------------------
	private void BringPhaseCalls()
	{
		int i;
		//int maxlane,j ;
		//DetectorStation d = null;

		// Update stopline calls
		for(i=0;i<8;i++){
			if( phase.get(i).Recall() ){
				hasstoplinecall[i] = true;
				continue;
			}
			if( phase.get(i).StoplineStation()!=null && phase.get(i).StoplineStation().GotCall() )
				hasstoplinecall[i] = true;
			else
				hasstoplinecall[i] = false;
		}

		// Update approach calls
		for(i=0;i<8;i++){
			if( phase.get(i).ApproachStation()!=null && phase.get(i).ApproachStation().GotCall() )
				hasapproachcall[i] = true;
			else
				hasapproachcall[i] = false;
		}

		// Update conflicting calls
		boolean[] currentconflictcall = new boolean[8];
		for(i=0;i<8;i++)
			currentconflictcall[i] = CheckForConflictingCall(i);
		for(i=0;i<8;i++){
			if(  !hasconflictingcall[i] && currentconflictcall[i] )
				conflictingcalltime[i] = (float)(myNode.getMyNetwork().getSimTime()*3600f);
			hasconflictingcall[i] = currentconflictcall[i];
		}

		return;
		
	}
//	-------------------------------------------------------------------------
	private void UpdatePermittedHolds()
	{
		int i,j;
		for(i=0;i<8;i++){
			permithold[i] = true;
			for(j=0;j<8;j++){
				if(!IsCompatible(i,j) && !permitopposinghold[j] ){
					permithold[i] = false;
				}
			}
		}
		return;
	}
//	-------------------------------------------------------------------------
	private void UpdateSignalCommands()
	{
		int i,j;
		boolean[] goG = new boolean[8];
		boolean[] goY = new boolean[8];
		
		// Throw away conflicting hold pairs 
		// (This is purposely drastic to create an error)
		for(i=0;i<8;i++){
			if(hold[i]){
				for(j=i;j<8;j++){
					if( hold[j] && !IsCompatible(i,j) ){
						hold[i] = false;
						hold[j] = false;
					}
				}
			}
		}

		// Deal with simultaneous hold and forceoff (expected by RHODES)
		for(i=0;i<8;i++){
			if( hold[i] && forceoff[i] ){
				forceoff[i] = false;
			}
		}

		// Make local relaying copy
		for(i=0;i<8;i++){
			goG[i] = hold[i];
			goY[i] = forceoff[i];
		}

		// No transition if no permition
		for(i=0;i<8;i++){
			if( !permithold[i] )
				goG[i] = false;
		}

		// No transition if green time < mingreen
		for(i=0;i<8;i++){
			if( goY[i] && phase.get(i).BulbColor()==BulbColor.GREEN  && phase.get(i).bulbtimer.GetTime()<phase.get(i).Mingreen()-0.001){
				goY[i] = false;
			}
		}

		// Update all phases
		for(i=0;i<8;i++){
			phase.get(i).ProcessCommand(goG[i],goY[i]);
		}

		// Remove serviced commands 
		for(i=0;i<8;i++){
			if(phase.get(i).BulbColor()==BulbColor.GREEN)
				hold[i] = false;
			if(phase.get(i).BulbColor()==BulbColor.YELLOW || phase.get(i).BulbColor()==BulbColor.RED)
				forceoff[i] = false;
		}
	
		// Set permissive opposing left turn to yellow
		for(i=0;i<8;i++){
			SignalPhase n = phase.get(i);
			SignalPhase o = phase.get(n.MyNEMAopposing());
			if( (n.BulbColor()==BulbColor.GREEN || n.BulbColor()==BulbColor.YELLOW) && n.Isthrough() && o.Permissive()){
				o.SetYellow();	
			}
			if( !o.Protected() && n.BulbColor()==BulbColor.RED )
				o.SetRed();
		}
	}
	
//	=========================================================================
//	PRIVATE METHODS =========================================================
//	=========================================================================
	private boolean CheckForConflictingCall(int nema)
	{
		if(!phase.get(nema).Protected())
			return false;

		int i;
		for(i=0;i<8;i++)
			if( hasstoplinecall[i] && !IsCompatible(nema,i) )
				return true;
		return false;
	}
//	-------------------------------------------------------------------------
	private boolean IsCompatible(int nemaA,int nemaB)
	{
		if(nemaA==nemaB)
			return true;

		if( !phase.get(nemaA).Protected() || !phase.get(nemaB).Protected() )
			return true;

		switch(nemaA){
		case NEMA._1:
		case NEMA._2:
			if(nemaB==NEMA._5 || nemaB==NEMA._6)
				return true;
			else
				return false;
		case NEMA._3:
		case NEMA._4:
			if(nemaB==NEMA._7 || nemaB==NEMA._8)
				return true;
			else
				return false;
		case NEMA._5:
		case NEMA._6:
			if(nemaB==NEMA._1 || nemaB==NEMA._2)
				return true;
			else
				return false;
		case NEMA._7:
		case NEMA._8:
			if(nemaB==NEMA._3 || nemaB==NEMA._4)
				return true;
			else
				return false;
		}
		return false;
	}
//	-------------------------------------------------------------------------
	public boolean FlushApproachCalls(int nema)
	{
		if(nema>7 || nema<0)
			return false;
		DetectorStation d = phase.get(nema).ApproachStation();
		if(d==null)
			return false;
		d.FlushCallsAndCount();
		return true;
	}
//	-------------------------------------------------------------------------
	public boolean FlushStoplineCalls(int nema)
	{
		if(nema>7 || nema<0)
			return false;
		DetectorStation d = phase.get(nema).StoplineStation();
		if(d==null)
			return false;
		d.FlushCallsAndCount();
		return true;
	}
//	-------------------------------------------------------------------------
	/*
	public boolean LoopIsOccupied(int nema,DetectorStationType AorS,int lane)
	{
		if(nema>7 || nema<0)
			return false;
		
		DetectorStation d;
		switch(AorS){
		case APPROACH:
			d = phase.get(nema).ApproachStation();
			break;
		case STOPLINE:
			d = phase.get(nema).StoplineStation();
			break;
		default:
			return false;
		}
		
		for(int i=0;i<d.numloops;i++)
			if(d.loopoccupied[i])
				return true;
		return false;
	}*/
//	-------------------------------------------------------------------------
/*	public boolean attachSimpleController(NodeUJSignal a,AbstractLinkHWC l){
		ControllerSlave x = new ControllerSlave(this.myController,l);
		if(!a.setSimpleController(x,l))
			return false;
		return true;
	}*/
//	-------------------------------------------------------------------------
	public SignalPhase FindPhaseByLink(AbstractLink L){
		int i,j;
		Vector<AbstractLinkHWC> Z;
		for(i=0;i<8;i++){
			Z = phase.get(i).getlinks();
			for(j=0;j<Z.size();j++){
				if(Z.get(j).getId()==L.getId())
					return phase.get(i);
			}
		}
		return null;
	}
//	-------------------------------------------------------------------------

}
