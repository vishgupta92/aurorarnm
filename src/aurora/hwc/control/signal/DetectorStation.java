package aurora.hwc.control.signal;

import java.util.Vector;

import aurora.hwc.NodeUJSignal;
import aurora.hwc.SensorLoopDetector;

public class DetectorStation {

//	 ========================================================================
//	 FIELDS =================================================================
//	 ========================================================================
	
	int myNEMA;
	DetectorStationType myType = null;
	NodeUJSignal myNode;
	int numloops;
	Vector<SensorLoopDetector> loop = new Vector<SensorLoopDetector>();
	boolean gotcall;
	int[] loopcount;
//	boolean[] loopoccupied;	

//	 ========================================================================
//	 CONSTRUCTORS ===========================================================
//	 ========================================================================

	public DetectorStation() { 
	}

//	-------------------------------------------------------------------------
	public DetectorStation(NodeUJSignal n,int nema,String c) { 
		myNode = n;
		myNEMA = nema;
		if (c.equals("A"))
			myType = DetectorStationType.APPROACH;
		else if (c.equals("S"))
			myType = DetectorStationType.STOPLINE;
		gotcall = false;
	}

//	 ========================================================================
//	 INTERFACE ==============================================================
//	 ========================================================================
	
	public int NumLoops() 		{return numloops; };
//	-------------------------------------------------------------------------
	public boolean GotCall() 	{return gotcall; };
//	-------------------------------------------------------------------------
	public int LoopCount(int i) {return loopcount[i]; };
//	-------------------------------------------------------------------------
	public boolean AssignLoops(Vector<Integer> id) {
		numloops = id.size();
		if(numloops==0 || !loop.isEmpty())
			return false;
		loopcount = new int[numloops];
//		loopoccupied = new boolean[numloops];
		for(int i=0;i<numloops;i++){
			SensorLoopDetector d = (SensorLoopDetector) myNode.getMyNetwork().getSensorById(id.get(i));
			loop.add(d);
		}
		FlushCallsAndCount();
		return true;
	}
//	-------------------------------------------------------------------------
	public Vector<Integer> LoopIds(){
		Vector<Integer> allids = new Vector<Integer>();
		for(int i=0;i<numloops;i++){
			allids.add(loop.get(i).getId());
		}
		return allids;
	}
//	-------------------------------------------------------------------------
	public int MaxLoopCount(){
		int maxloopcount = 0;
		for(int i=0;i<numloops;i++)
			if( loopcount[i] > maxloopcount )
				maxloopcount = loopcount[i];
		return maxloopcount;
	}
//	-------------------------------------------------------------------------
	public void FlushCallsAndCount(){
		int i;
		for(i=0;i<numloops;i++){
			loopcount[i] = 0;
			loop.get(i).ResetCount();
		}
		gotcall = false;
	}
//	-------------------------------------------------------------------------
	
	
//	 ========================================================================
//	 METHODS ================================================================
//	 ========================================================================

//	-------------------------------------------------------------------------
	public void UpdateCallsAndCounts(){
		int i;
		double tp = myNode.getMyNetwork().getTP();
		for(i=0;i<numloops;i++){
			loopcount[i] = loop.get(i).Count();
			gotcall = gotcall || loop.get(i).ReceivedCall(tp) || loopcount[i]>0;
		}
	}
//	-------------------------------------------------------------------------

	
}
