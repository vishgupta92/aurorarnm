package aurora.hwc.control.signal;

import java.io.Serializable;
import java.util.Vector;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aurora.ErrorConfiguration;
import aurora.ExceptionConfiguration;

public class ControllerPretimedIntersectionPlan implements Serializable {
	private static final long serialVersionUID = 272304770315813080L;
	
//	 ========================================================================
//	 FIELDS =================================================================
//	 ========================================================================

	SignalManager mySigMan;
	int myIntersectionID;
	
	float offset;			// offset for the intersection
	int numstages;			// number of stages (length of movA, movB and greentime)
	Vector<Integer> movA = new Vector<Integer>();	// list of stages for ring A (-1 means not used)
	Vector<Integer> movB = new Vector<Integer>();	// list of stages for ring B (-1 means not used)
	Vector<Float> greentime = new Vector<Float>();	// green time for each stage
	float[] holdpoint = new float[8];
	float[] forceoffpoint = new float[8];
	float cyclelength;
	//Vector<PlandataRow> planTable;

//	 ========================================================================
//	 INTERFACE ==============================================================
//	 ========================================================================
	public SignalManager getSigMan(){ return mySigMan; };
	public int getIntId() { return myIntersectionID; };
	public float getOffset(){ return offset; };
	public int getNumStages(){ return numstages; };
	public Vector<Integer> getMovA(){ return movA; };
	public Vector<Integer> getMovB(){ return movB; };
	public Vector<Float> getGreenTime(){ return greentime; };
	public float getCycleLength(){ return cyclelength; };
	
//	 ========================================================================
//	 CONSTRUCTORS ===========================================================
//	 ========================================================================
	/*
	public ControllerPretimedIntersectionPlan(SignalManager2 s,float c,float o,Vector<Integer> mA,Vector<Integer> mB,Vector<Float> g) 
	{
		int i;
		
		mySigMan = s;
		offset = o;
		numstages = mA.size();
		cyclelength = c;
		movA = new int[numstages];
		movB = new int[numstages];
		greentime = new float[numstages];
		for(i=0;i<numstages;i++){
			movA[i] = (int) mA.get(i);
			movB[i] = (int) mB.get(i);
			greentime[i] = (float) g.get(i);
		}
		
		holdpoint = new float[8];
		forceoffpoint = new float[8];
		for(i=0;i<8;i++){
			holdpoint[i] = 0.0f;
			forceoffpoint[i] = 0.0f;
		}
	}
	*/
	public ControllerPretimedIntersectionPlan(SignalManager s,float c,float o) 
	{
		mySigMan = s;
		offset = o;
		cyclelength = c;
		numstages = 0;
		myIntersectionID = s.myNode.getId();
	}

//	 ========================================================================
//	 METHODS ================================================================
//	 ========================================================================
	public boolean InNextStage(int p,int s)
	{
		int nextstage;
		
		if(s<0 || s>=numstages || p<0 || p>=8)
			return false;

		nextstage = s+1;

		if(nextstage==numstages)
			nextstage=0;

		if( p==movA.get(nextstage) || p==movB.get(nextstage) )
			return true;
		else
			return false;
	}
//	-------------------------------------------------------------------------
	public int FindFirstPhases(boolean AorB)
	{
		float reltime,tempF;
		int p,k,tempI,nema;

		reltime = cyclelength - offset;

		nema = -1;

		for(k=0;k<numstages;k++){

			if(AorB)
				p=movA.get(k);
			else
				p=movB.get(k);
			
			if(p<0)
				continue;
		
			if( holdpoint[p]<forceoffpoint[p] ){
				if( holdpoint[p]<reltime && forceoffpoint[p]>reltime )
					nema = p;
			}
			else{
				if( holdpoint[p]<reltime || forceoffpoint[p]>reltime )
					nema = p;
			}

		}

		if(nema!=-1)
			return nema;

		// if still not assigned, we are in yellow/redclear time
		// look for one with larger hold time
		tempI = -1;
		tempF = cyclelength;
		for(k=0;k<numstages;k++){

			if(AorB)
				p=movA.get(k);
			else
				p=movB.get(k);

			if(p<0)
				continue;
			
			if(holdpoint[p]>reltime){
				if( holdpoint[p]-reltime < tempF ){
					tempF = holdpoint[p]-reltime;
					tempI = p;
				}
			}
		}

		if( tempI > -0.001 ){
			nema = tempI;
			return nema;
		}

		// If still not assigned it must be that we are in a yellow/redclear
		// period that breaches the sync point. the first phase is the one with the
		// smallest holdpoint
		tempI = -1;
		tempF = cyclelength;
		for(k=0;k<numstages;k++){
			if(AorB)
				p=movA.get(k);
			else
				p=movB.get(k);

			if(p<0)
				continue;
			
			if( holdpoint[p] < tempF ){
				tempF = holdpoint[p];
				tempI = p;
			}
		}

		nema = tempI;
		return nema;
	}
//	-------------------------------------------------------------------------
	boolean SetHoldForceoffPoint()
	{
		int k,p;
		float y,r,yA,yB,rA,rB;
		int mA, mB;
		float stime, etime;
		boolean looparound;
		float totphaselength;
		float[] actualyellow = new float[8];
		float[] actualredclear = new float[8];
		float[] stagelength = new float[numstages];
		
		// Set yellowtimes, redcleartimes, stagelength, totphaselength
		totphaselength = 0;
		for(k=0;k<numstages;k++){

			mA = movA.get(k);
			mB = movB.get(k);
			
			if(mA>=0){
				yA = mySigMan.Phase(mA).Yellowtime();
				rA = mySigMan.Phase(mA).Redcleartime();
			}
			else{
				yA=0;
				rA=0;
			}
			
			if(mB>=0){
				yB = mySigMan.Phase(mB).Yellowtime();
				rB = mySigMan.Phase(mB).Redcleartime();
			}
			else{
				yB=0;
				rB=0;
			}
			
			y = Math.max(yA,yB);
			r = Math.max(rA,rB);

			if( InNextStage(mA,k) ){
				y = yB;
				r = rB;
			}

			if( InNextStage(mB,k) ){
				y = yA;
				r = rA;
			}

			if(mA>=0){
				actualyellow[mA] = y;
				actualredclear[mA] = r;
			}
				
			if(mB>=0){
				actualyellow[mB] = y;
				actualredclear[mB] = r;
			}

			stagelength[k] = greentime.get(k)+y+r;
			totphaselength += greentime.get(k)+y+r;
		}

		// check cycles are long enough .....................................	
		if( cyclelength<totphaselength-0.001 || cyclelength>totphaselength+0.001 ){
			mySigMan.myNode.getMyNetwork().addConfigurationError(new ErrorConfiguration(this, "Pretimed controller: intersection " + myIntersectionID  + ", stage " + k + ", totalstagelength does not equal cyclelength."));
			return false;
		}
		totphaselength = -1;

		for( p=0;p<8;p++ ){

			holdpoint[p]=-1;
			forceoffpoint[p] = -1;

			// first determine whether this phase is in first and last stage
			if( ( movA.get(0)==p || movB.get(0)==p ) && ( movA.get(numstages-1)==p || movB.get(numstages-1)==p ) )
				looparound = true;
			else
				looparound = false;

			// Strategy for looparound phases ...................................
			if( looparound ){

				// find forceoff by forward search
				k=0;
				forceoffpoint[p] = 0;
				stime = 0;
				while( movA.get(k)==p || movB.get(k)==p ){
					forceoffpoint[p] = stime+greentime.get(k);
					stime += stagelength[k];
					k++;
				}

				// find hold by backward search
				k = numstages-1;
				holdpoint[p] = cyclelength;
				etime = cyclelength;
				while( movA.get(k)==p || movB.get(k)==p ){
					holdpoint[p] = etime-stagelength[k];
					etime -= stagelength[k];
					k--;
				}
			}

			// Strategy for non-looparound phases .................................
			if( !looparound ){
				stime = 0;
				for(k=0;k<numstages;k++){
					etime = stime+greentime.get(k);
					if( movA.get(k)==p || movB.get(k)==p ){
						if( holdpoint[p]==-1 ){
							holdpoint[p] = stime ;
							forceoffpoint[p] = etime;
						}
						else{
							forceoffpoint[p] = etime;
						}
					}
					stime += stagelength[k];
				}
			}
		}

		// Correction: offset is with respect to end of first stage, instead of beginning
		float x = Math.min( forceoffpoint[movA.get(0)],forceoffpoint[movB.get(0)] );
		for( p=0;p<8;p++ ){
			if(holdpoint[p]>=0){
				holdpoint[p] -= x;
				if(holdpoint[p]<0)
					holdpoint[p] += cyclelength;
			}
			if(forceoffpoint[p]>=0){
				forceoffpoint[p] -= x;
				if(forceoffpoint[p]<0)
					forceoffpoint[p] += cyclelength;
			}
		}
		return true;
	}
//	-------------------------------------------------------------------------
	public boolean initFromDOM(Node p) throws ExceptionConfiguration 
	{
		boolean res = true;
		if (p == null)
			return false;
		try  {
			int i;
			int mA,mB;
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (i = 0; i < pp.getLength(); i++) {
					if (pp.item(i).getNodeName().equals("stage")){
						mA = NEMA.index(Integer.parseInt(pp.item(i).getAttributes().getNamedItem("movA").getNodeValue()));
						movA.add(mA);
						mB = NEMA.index(Integer.parseInt(pp.item(i).getAttributes().getNamedItem("movB").getNodeValue()));
						movB.add(mB);
						greentime.add( Float.parseFloat(pp.item(i).getAttributes().getNamedItem("greentime").getNodeValue()) );
						numstages++;
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

		int k,p;
		float phaselength;
		boolean res = true;

		// Check that every stage has at least one movement 
		for(k=0;k<numstages;k++)
			if(movA.get(k)<0 && movB.get(k)<0){
				mySigMan.myNode.getMyNetwork().addConfigurationError(new ErrorConfiguration(this, "Pretimed controller: All stages must have at least one valid movement."));
				res = false;
			}
		
		// Check cyclelength is positive
		if(cyclelength<0.001){
			mySigMan.myNode.getMyNetwork().addConfigurationError(new ErrorConfiguration(this, "Pretimed controller: Cycle length must be > 0."));
			res = false;
		}

		res &= SetHoldForceoffPoint();

		// check phase lengths are longer than mingreens
		for( p=0;p<8;p++ ){
			if( holdpoint[p]==-1 )
				continue;
			SignalPhase x = mySigMan.Phase(p);
			phaselength = forceoffpoint[p]-holdpoint[p];
			if(phaselength<0)
				phaselength += cyclelength;
			if( phaselength<x.Mingreen()-0.001 ){
				mySigMan.myNode.getMyNetwork().addConfigurationError(new ErrorConfiguration(this, "Pretimed controller: Phase " + p+ " intersection " + myIntersectionID + " is less than min green."));
				res = false;
			}
		}

		return res;
	}
	//	========================================================================
//	TABLE DISPLAY  =========================================================
//	========================================================================
	/*
	public class PlandataRow {
		private int movA;
		private int movB;
		private double greentime;
		
		public PlandataRow(){ movA = 0; movB = 0;  greentime = 0; }
		public PlandataRow(int A, int B, double g) { movA = A; movB = B;  greentime = g; }
		
		public int getMovA() { return movA; };
		public int getMovB() { return movB; };
		public double getGreenTime() { return greentime; };
		
		public void setMovA(int A){
			if ((A >= 1) && (A <= 8))
				movA = A;
			return;
		}
		public void setMovB(int A){
			if ((A >= 1) && (A <= 8))
				movB = A;
			return;
		}
		public void setGreenTime(double t) {
			if (t >= 0.0)
				greentime = t;
			return;
		}	
	}
*/
	/**
	* Returns TOD table.
	*
	public Vector<PlandataRow> getTable() {
		planTable.clear();
		for(int i=0;i<numstages;i++){
			planTable.add(new PlandataRow(movA.get(i),movB.get(i),greentime.get(i)));
		}
		return planTable;
	}*/
	
}




