package aurora.hwc.control.signal;

import java.io.Serializable;

import aurora.AbstractNode;

public class Timer implements Serializable {
	private static final long serialVersionUID = -5807450773286309355L;
	
//	 ========================================================================
//	 FIELDS =================================================================
//	 ========================================================================
	
	private double starttime;
	private AbstractNode myNode = null;

//	 ========================================================================
//	 CONSTRUCTORS ===========================================================
//	 ========================================================================
	public Timer(){};
	public Timer(AbstractNode n){myNode = n;};
	public void setNetork(AbstractNode n){myNode = n;};

//	 ========================================================================
//	 INTERFACE ==============================================================
//	 ========================================================================
	public double StartTime() { return starttime; };
	
//	 ========================================================================
//	 METHODS ================================================================
//	 ========================================================================

	public double GetTime() { 
		return myNode.getMyNetwork().getSimTime()*3600f - starttime;
	}
//	-------------------------------------------------------------------------
	public void Reset() { 
		starttime = myNode.getMyNetwork().getSimTime()*3600f; 
	}
//	-------------------------------------------------------------------------
	public void SetTo(float t) { 
		starttime = myNode.getMyNetwork().getSimTime()*3600f-t;
	}
//	-------------------------------------------------------------------------
}
