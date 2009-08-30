package aurora.hwc.control.signal;

import java.util.Vector;

import aurora.hwc.AbstractLinkHWC;
import aurora.hwc.NodeUJSignal;

public class SignalPhase {


//	 ========================================================================
//	 FIELDS =================================================================
//	 ========================================================================

	//private AbstractNodeComplex myNetwork;
	private NodeUJSignal myNode;
	private SignalManager mySigMan;
	public int myControlIndex;

	private int myNEMA;
	private int myNEMAopposing;
	private int myRingGroup;

	public AbstractLinkHWC link;

	// Basic characteristics
	private boolean valid;
	private boolean protectd;
	private boolean isthrough;
	private boolean recall;
	private boolean permissive;

	// Signalling parameters
	private float mingreen;
	private float yellowtime;
	private float redcleartime;
	public float actualyellowtime;
	public float actualredcleartime;

	// timers
	public Timer bulbtimer;

	// State
	private BulbColor bulbcolor;

	// Detectors
	private DetectorStation ApproachStation = null;
	private DetectorStation StoplineStation = null;
	public Vector<Integer> ApproachStationIds;
	public Vector<Integer> StoplineStationIds;

//	 ========================================================================
//	 CONSTRUCTORS ===========================================================
//	 ========================================================================

	public SignalPhase(){};

	public SignalPhase(NodeUJSignal n,SignalManager s,int xNEMA){
		myNode = n;
		mySigMan = s;
		myNEMA = xNEMA;

		myNEMAopposing = -1;
		myRingGroup = -1;
		bulbtimer = new Timer(myNode);
		bulbcolor = BulbColor.RED;
	
		permissive = false;
		valid = false;
		protectd = false;
		isthrough = false;
	
		mingreen = -1.0f;
		yellowtime = -1.0f;
		redcleartime = -1.0f;

		//bulbtimer.SetTo( (float) (-redcleartime-myNetwork.getTP()) ); // GG FIX THIS: This line produces an error because myNetwork=null.

		actualyellowtime = yellowtime;
		actualredcleartime = redcleartime;

		switch(myNEMA){
		case NEMA._1:
			myNEMAopposing =  NEMA._2;
			isthrough = false;
			myRingGroup = 0;
			break;
		case NEMA._2:
			myNEMAopposing =  NEMA._1;
			isthrough = true;
			myRingGroup = 0;
			break;
		case NEMA._3:
			myNEMAopposing =  NEMA._4;
			isthrough = false;
			myRingGroup = 1;
			break;
		case NEMA._4:
			myNEMAopposing =  NEMA._3;
			isthrough = true;
			myRingGroup = 1;
			break;
		case NEMA._5:
			myNEMAopposing =  NEMA._6;
			isthrough = false;
			myRingGroup = 0;
			break;
		case NEMA._6:
			myNEMAopposing = NEMA._5;
			isthrough = true;
			myRingGroup = 0;
			break;
		case NEMA._7:
			myNEMAopposing = NEMA._8;
			isthrough = false;
			myRingGroup = 1;
			break;
		case NEMA._8:
			myNEMAopposing = NEMA._7;
			isthrough = true;
			myRingGroup = 1;
			break;
		}
	};

//	 ========================================================================
//	 INTERFACE ==============================================================
//	 ========================================================================

	public int MyNEMA() { return myNEMA; };
	public int MyNEMAopposing() { return myNEMAopposing; };
	public int MyRingGroup() { return myRingGroup; };
	public NodeUJSignal MyNode() { return myNode; };
	public SignalManager MySigMan() { return mySigMan; };

	public void assignLink(AbstractLinkHWC L) {link = L;};
	public float Mingreen()					{ return mingreen; };
	public float Yellowtime()				{ return yellowtime; };
	public float Redcleartime()				{ return redcleartime; };
	public boolean Valid()					{ return valid; };
	public boolean Protected()				{ return protectd; };
	public boolean Isthrough()				{ return isthrough; };
	public boolean Recall()					{ return recall; };
	public boolean Permissive()				{ return permissive; };
	public Timer BulbTimer()				{ return bulbtimer; };
	public BulbColor BulbColor()			{ return bulbcolor; };
	public int RingGroup()					{ return this.myRingGroup;};
	
	public DetectorStation ApproachStation(){ return ApproachStation;};
	public DetectorStation StoplineStation(){ return StoplineStation;};
	
	public AbstractLinkHWC getlink() { return link; };

	public void setValid(boolean x) {valid = x;};
	public void setProtected(boolean x) {protectd = x;};
	public void setRecall(boolean x) {recall = x;};
	public void setPermissive(boolean x) {permissive = x;};
	public void setMingreen(float x) {mingreen = x;};
	public void setYellowtime(float x) {yellowtime = x;};
	public void setRedcleartime(float x) {redcleartime = x;};
	
//	 ========================================================================
//	 METHODS ================================================================
//	 ========================================================================
	public boolean addDetectorStation(DetectorStation d,Vector<Integer> id){
		if(d==null)
			return false;
		if(d.myType==DetectorStationType.APPROACH && ApproachStation==null){
			ApproachStation = d;
			ApproachStationIds = id;
		}
		if(d.myType==DetectorStationType.STOPLINE && StoplineStation==null){
			StoplineStation = d;
			StoplineStationIds = id;
		}
		return true;
	}
//	 -------------------------------------------------------------------------------------------------
	public void UpdateDetectorStations(){
		if(ApproachStation!=null){
			ApproachStation.UpdateCallsAndCounts();
		}
		if(StoplineStation!=null){
			StoplineStation.UpdateCallsAndCounts();
		}		
	}
	
//	 -------------------------------------------------------------------------------------------------
	public void ProcessCommand(boolean goG,boolean goY)
	{
		double bulbt = bulbtimer.GetTime();

		if(!valid)
			return;

		if(!protectd){
			if(permissive)
				return;
			else{
				SetRed();
				return;
			}
		}

		switch(bulbcolor){

		// .............................................................................................
		case GREEN:

			SetGreen();
			mySigMan.permitopposinghold[myNEMA] = false;

			// Force off 
			if( goY ){ 
				SetYellow();
				//bulbcolor = BulbColor.YELLOW;
				bulbtimer.Reset();
				//FlushAllStationCallsAndConflicts();
			}

			break;

		// .............................................................................................
		case YELLOW:
			
			SetYellow();
			mySigMan.permitopposinghold[myNEMA] = false;

			// if timer>=yellowtime-EPS, go to red (Set permissive opposing left turn to yellow), reset timer
			if( bulbt>=actualyellowtime-0.001 ){
				SetRed();
				//bulbcolor = BulbColor.RED;
				bulbtimer.Reset();
			}
			break;

		// .............................................................................................
		case RED:

			SetRed();

			if( bulbt>redcleartime-myNode.getMyNetwork().getTP()*3600f-0.001 && !goG )
				mySigMan.permitopposinghold[myNEMA] = true;
			else
				mySigMan.permitopposinghold[myNEMA] = false;

			// if hold, set to green, go to green, etc.
			if( goG ){ 
				SetGreen();
				bulbtimer.Reset();

				// Unregister calls (for reading conflicting calls)
				//FlushAllStationCallsAndConflicts(); // GCG ?????
			}

			break;
		}
	}
//	-------------------------------------------------------------------------
	public void SetGreen()
	{
		if(!valid) 
			return;
		mySigMan.myController.setControlInput(myControlIndex, link.getFlow().sum().getCenter());
		bulbcolor = BulbColor.GREEN;
	}
//	-------------------------------------------------------------------------
	public void SetYellow()
	{
		if(!valid) 
			return;
		mySigMan.myController.setControlInput(myControlIndex, link.getFlow().sum().getCenter());
		bulbcolor = BulbColor.YELLOW;
	}
//	-------------------------------------------------------------------------
	public void SetRed()
	{
		if(!valid) 
			return;
		mySigMan.myController.setControlInput(myControlIndex,0.0);
		bulbcolor = BulbColor.RED;
	}
//	-------------------------------------------------------------------------
	
}
