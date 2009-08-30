package aurora.hwc.control.signal;

import java.io.IOException;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aurora.AbstractNodeComplex;
import aurora.ExceptionConfiguration;
import aurora.ExceptionDatabase;
import aurora.ExceptionSimulation;
import aurora.hwc.NodeUJSignal;

public class ControllerActuated extends BaseSignalController {
	private static final long serialVersionUID = -8233560098363094720L;

//	 ========================================================================
//	 FIELDS =================================================================
//	 ========================================================================
	protected Vector< Vector<ASC_Parameters> > P;					// [numintersections X 8]
	protected Vector<SignalPhase> phaseA;							// [numintersections]
	protected Vector<SignalPhase> phaseB;							// [numintersections]
	protected Vector< Vector<Boolean> > externalforceoffrequest;	// [numintersections X 8]
	protected Vector< Vector<Boolean> > externalholdrequest;		// [numintersections X 8]

//	 ========================================================================
//	 CONSTRUCTORS ===========================================================
//	 ========================================================================
	public ControllerActuated() {
		P = new Vector< Vector<ASC_Parameters> >();
		phaseA = new Vector<SignalPhase>();				
		phaseB = new Vector<SignalPhase>();				
		externalforceoffrequest = new Vector< Vector<Boolean> >();
		externalholdrequest = new Vector< Vector<Boolean> >();
	};

//	 ========================================================================
//	 INTERFACE ==============================================================
//	 ========================================================================

	// These are needed for ControllerCoordinated
	public void setRequestforceoff(int i,int j,boolean v){ return; }
	public void setRequesthold(int i,int j,boolean v){ return;}
	public float Syncpoint(int i){ return -1.0f; }
	
	public SignalPhase PhaseA(int index) {return phaseA.get(index);};
	public SignalPhase PhaseB(int index) {return phaseB.get(index);};
	
//	 ========================================================================
//	 METHODS ================================================================
//	 ========================================================================
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		if(!super.dataUpdate(ts))
			return false;
		
		boolean doneA, doneB;
		int barrA=-1;
		int barrB=-1;;
		int nextA=-1;
		int nextB=-1;
		int index, j;
		SignalPhase pA, pB;
		
		for(index=0;index<numintersections;index++){

			SignalManager s = intersection.get(index).getSigMan();

			s.RefreshDisplayValues();
			
			pA = phaseA.get(index);
			pB = phaseB.get(index);

			// Update all phases
			for(j=0;j<8;j++)
				UpdateASCPhase(s.Phase(j));

			doneA = pA==null || P.get(index).get(pA.MyNEMA()).done || !pA.Valid();
			doneB = pB==null || P.get(index).get(pB.MyNEMA()).done || !pB.Valid();

			// requestforceoff=true . done=true
			doneA = doneA || externalforceoffrequest.get(index).get(pA.MyNEMA());
			doneB = doneB || externalforceoffrequest.get(index).get(pB.MyNEMA());

			// requesthold=true -> done=false
			if(pA!=null)
				doneA = doneA && !externalholdrequest.get(index).get(pA.MyNEMA());
			if(pB!=null)
				doneB = doneB && !externalholdrequest.get(index).get(pB.MyNEMA());

			if(doneA){
				nextPhaseAndBarrier x = GetNextNEMA(pA,pB);
				nextA = x.phase;
				barrA = x.barrier;
			}

			if(doneB){
				nextPhaseAndBarrier x = GetNextNEMA(pB,pA);
				nextB = x.phase;
				barrB = x.barrier;
			}

	// One phase is done and it skips no barriers ...............................
			// -> simply start the next phase.
			if( doneA && !doneB && barrA==0 ){
				FinishStartOnePhase(pA,s.Phase(nextA),1,-1,-1);
				continue;
			}

			if( doneB && !doneA && barrB==0 ){
				FinishStartOnePhase(pB,s.Phase(nextB),2,-1,-1);
				continue;
			}

			// Two phases are done and... ...............................................
			if( doneA && doneB ){
								
				nextA = Math.abs(nextA);
				nextB = Math.abs(nextB);

				// they skip the same number of barriers ................................
				// finish both with a common yellow and red time.
				if( barrA == barrB ){
					FinishStartTwoPhases(pA,s.Phase(nextA),pB,s.Phase(nextB));
					continue;
				}

				// Only one skips at leasts one barrier .................................
				// Only transition the non-skipping phase.
				if( barrA==0 && barrB!=0 ){
					FinishStartOnePhase(pA,s.Phase(nextA),1,-1,-1);
					continue;
				}

				if( barrB==0 && barrA!=0 ){
					FinishStartOnePhase(pB,s.Phase(nextB),2,-1,-1);
					continue;
				}

				// one skips one, the other two barriers .................................
				// Transition both over only one barrier using common yellow and red times.
				if( barrA==1 && barrB==2 ){
					nextB = GetCompatible(s.Phase(nextA));
					FinishStartTwoPhases(pA,s.Phase(nextA),pB,s.Phase(nextB));
					continue;
				}

				if( barrB==1 && barrA==2 ){
					nextA = GetCompatible(s.Phase(nextB));
					FinishStartTwoPhases(pA,s.Phase(nextA),pB,s.Phase(nextB));
					continue;
				}
			}
		}
		
		// remove external requests
		for(index=0;index<numintersections;index++){
			for(j=0;j<8;j++){
				externalholdrequest.get(index).set(j , false);
				externalforceoffrequest.get(index).set(j , false);
			}
		}
		
		return true;
	}
//	 --------------------------------------------------------------------------
	private void FinishStartTwoPhases(SignalPhase oldphaseA, SignalPhase newphaseA,SignalPhase oldphaseB, SignalPhase newphaseB)
	{
		float r,y;
		float yA=0;
		float yB=0;
		float rA=0;
		float rB=0;
		
		// Find yellow and red clearance times as max of both rings
		if( oldphaseA!=null ){
			yA = oldphaseA.Yellowtime();
			rA = oldphaseA.Redcleartime();
		}
		if( oldphaseB!=null ){
			yB = oldphaseB.Yellowtime();
			rB = oldphaseB.Redcleartime();
		}
		
		y = Math.max( yA , yB );
		r = Math.max( rA , rB );

		FinishStartOnePhase(oldphaseA,newphaseA,1,y,r);
		FinishStartOnePhase(oldphaseB,newphaseB,2,y,r);
	}
//	 --------------------------------------------------------------------------
	void FinishStartOnePhase(SignalPhase oldphase, SignalPhase newphase,int AorB, float y, float r)
	{
		ASC_Parameters oldP = null;
		ASC_Parameters newP = null;
		int index = -1;
		
		if(oldphase!=null){
			index = interIndexToId.indexOf(oldphase.MyNode().getId());
			oldP = P.get(index).get(oldphase.MyNEMA());
		}

		if(newphase!=null){
			index = interIndexToId.indexOf(newphase.MyNode().getId());
			newP = P.get(index).get(newphase.MyNEMA());
		}

		if(y<0 && oldphase!=null)
			y = oldphase.Yellowtime();

		if(r<0 && oldphase!=null)
			r = oldphase.Redcleartime();

		if( oldphase==newphase && newP!=null){
			newP.ascstate = ASCstate.ASC_HOLDTIME;
			newP.done = false;
		}
		else{

			if(oldphase!=null && oldP!=null){
				// force off old phase
				oldP.ascstate = ASCstate.ASC_YELLOWRED;
				oldphase.MySigMan().IssueForceOff(oldphase.MyNEMA(),y,r);
				oldP.done = false;
	
				 // old phase: flush approach calls to begin count for next green
				oldphase.MySigMan().FlushApproachCalls(oldphase.MyNEMA());
				
				 // old phase: flush stopline phase calls, remove my phase request
				oldphase.MySigMan().FlushStoplineCalls(oldphase.MyNEMA());
	
				// remove externalforceoffrequest 
				//externalforceoffrequest[inter][oldphase.NEMA()] = false;
			}
			
			// start new phase
			if(newP!=null && newphase!=null){
				newP.ascstate = ASCstate.ASC_WAIT;
				newphase.MySigMan().IssueHold(newphase.MyNEMA());
			}
			
			switch(AorB){
			case 1:
				phaseA.set(index,newphase);
				break;
			case 2:
				phaseB.set(index,newphase);
				break;
			}
		}
	}
//	 --------------------------------------------------------------------------
	void UpdateASCPhase(SignalPhase m)
	{
		double temp;
		int myNEMA = m.MyNEMA();
		int index = interIndexToId.indexOf(m.MyNode().getId());
		
		//ASC_Parameters p = P.get(m.Int().MyIndex()).get(myNEMA);
		ASC_Parameters p = P.get(index).get(myNEMA);
		SignalManager s = m.MySigMan();

		float bulbt = (float) m.BulbTimer().GetTime();

	 	if(!m.Valid())
			return;

		if(!m.Protected()){
			if(m.Permissive())
				return;
			else{
				return;
			}
		}
		
		switch( p.ascstate){

		// .............................................................................................
		case ASC_WAIT:

			// -- EXIT CONDITION --
			if( s.Permithold(myNEMA) ){

				p.ascstate = ASCstate.ASC_HOLDTIME;

				p.extensiontimer.Reset();
				p.done = false;

				// reset permissible gap function
				p.permissiblegap = p.maxgap;

				// calculate variable initial green
				float x=0;
				if(s.Phase(myNEMA).ApproachStation()!=null)
					x = (float)( s.Phase(myNEMA).ApproachStation().MaxLoopCount()*p.addpervehicle );
				p.initialgreen = Math.min( Math.max( x , m.Mingreen()) , p.maxinitial );

				// Flush approach calls to start counting externsions
				s.FlushApproachCalls(myNEMA);
			}
			break;

		// .............................................................................................
		case ASC_HOLDTIME:

			if(p.done)
				break;

			// -- DO : Update permitted gap --
			if( s.Hasconflictingcall(myNEMA) && bulbt>m.Mingreen()){			
				if( p.reduceevery>0.0 ){
					temp = Math.max(m.Mingreen(),s.Conflictingcalltime(myNEMA)-m.BulbTimer().StartTime());
					temp = Math.floor( (bulbt-temp)/p.reduceevery );
					temp = p.maxgap - p.reducegapby*temp;
					p.permissiblegap = Math.max( p.mingap , (float) temp );
				}
				else{
					p.permissiblegap = p.maxgap;
				}
			}

			// -- DO : Renew extension --
			if(s.Hasapproachcall(myNEMA)){
				if( (bulbt<p.initialgreen) || (bulbt>p.initialgreen-0.0001 && p.extensiontimer.GetTime()<p.permissiblegap) ){
					p.extensiontimer.Reset();
					s.FlushApproachCalls(myNEMA);
				}
			}
		
			// -- EXIT CONDITION: GAP OUT --
			if( bulbt>p.initialgreen-0.0001 && p.extensiontimer.GetTime()>p.extension-0.0001 ){
				p.done = true;
			}

			// -- EXIT CONDITION: MAX OUT --
			if( bulbt> p.maxgreen-0.0001 ){
				p.done = true;
			}

			break;

		// .............................................................................................
		case ASC_YELLOWRED:
			break;
		}
	}
//	 --------------------------------------------------------------------------
	private nextPhaseAndBarrier GetNextNEMA(SignalPhase n,SignalPhase m)		// GCG CHECK THAT THIS WORKS
	{
		int b,count;
		SignalPhase next;
		int nextNEMA;
		nextPhaseAndBarrier r = new nextPhaseAndBarrier();
		
		if(n==null){
			r.barrier = 1;
			r.phase = GetCompatible(m);
			return r;
		}
		
		int index = interIndexToId.indexOf(n.MyNode().getId());
		nextNEMA = P.get(index).get(n.MyNEMA()).myNEMAnext;
		next = n.MySigMan().Phase(nextNEMA);

		b=0;
		count=0;
		while(count<3 && IsNextSkip(n) ){
			if( next.RingGroup() != n.RingGroup() )
				b++;
			count++;
			n = next;
			index = interIndexToId.indexOf(n.MyNode().getId());
			nextNEMA = P.get(index).get(n.MyNEMA()).myNEMAnext;
			next = n.MySigMan().Phase(nextNEMA);
		}

		if( next.RingGroup() != n.RingGroup() )
			b++;
		
		r.phase = nextNEMA;
		r.barrier = b;
		return r;
	}
//	 --------------------------------------------------------------------------
	private boolean IsNextSkip(SignalPhase n)
	{
		int index = interIndexToId.indexOf(n.MyNode().getId());
		ASC_Parameters thisP = P.get(index).get(n.MyNEMA());
		SignalPhase next = n.MySigMan().Phase(thisP.myNEMAnext);
		
		if(!next.Valid())
			return true;

		if(!next.Protected())
			return true;

		if(next.Recall())
			return false;

		if( next.MySigMan().Hasstoplinecall(next.MyNEMA()) )
			return false;

		return true;
	}
//	 --------------------------------------------------------------------------
	private int GetCompatible(SignalPhase n)
	{
		int through=-1;
		int left=-1;
		int alternative =-1;

		switch(n.MyNEMA()){
		case NEMA._1:
		case NEMA._2:
			through = NEMA._6;
			left	= NEMA._5;
			alternative = -NEMA._8;
			break;
		case NEMA._3:
		case NEMA._4:
			through = NEMA._8;
			left	= NEMA._7;
			alternative = -NEMA._6;
			break;
		case NEMA._5:
		case NEMA._6:
			through = NEMA._2;
			left	= NEMA._1;
			alternative = -NEMA._4;
			break;
		case NEMA._7:
		case NEMA._8:
			through = NEMA._4;
			left	= NEMA._3;
			alternative = -NEMA._2;
			break;
		}

		if(n.MySigMan().Phase(through).Protected())
			return through;
		
		if(n.MySigMan().Phase(left).Protected())
			return left;

		return alternative;
	}
	
//	 ========================================================================
//	 IO =====================================================================
//	 ========================================================================
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
 		boolean res = super.initFromDOM(p);
 		if(!res)
 			return res;
		if (p == null)
			return false;
		try  {
			int i,j,id,s;
			Vector<Float> floatVec = new Vector<Float>();
			Vector<Boolean> boolVec = new Vector<Boolean>();
			Vector <Boolean> falseVec = new Vector <Boolean>();
			for(i=0;i<8;i++)	
				falseVec.add(false);
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (i = 0; i < pp.getLength(); i++) {
					if (pp.item(i).getNodeName().equals("intersection")){
						id = Integer.parseInt(pp.item(i).getAttributes().getNamedItem("id").getNodeValue());
						NodeUJSignal thisnode = (NodeUJSignal) myMonitor.getMyNetwork().getNodeById(id);
						if(thisnode==null)
							return false;
						externalforceoffrequest.add(falseVec);
						externalholdrequest.add(falseVec);
						Vector<ASC_Parameters> newP = new Vector<ASC_Parameters>();
						for(j=0;j<8;j++)
							newP.add(new ASC_Parameters());
						if (pp.item(i).hasChildNodes()) {
							NodeList pp2 = pp.item(i).getChildNodes();
							for (j = 0; j < pp2.getLength(); j++){
								if (pp2.item(j).getNodeName().equals("lagleft")) {
									if(check8bool( pp2.item(j).getTextContent(),boolVec))
										for(s=0;s<8;s++)
											newP.get(s).lagleft = boolVec.get(s);
									else
										res = false;	
								}
								if (pp2.item(j).getNodeName().equals("maxinitial")) {
									if(check8float( pp2.item(j).getTextContent(),floatVec))
										for(s=0;s<8;s++)
											newP.get(s).maxinitial = floatVec.get(s);
									else
										res = false;	
								}
								if (pp2.item(j).getNodeName().equals("extension")) {
									if(check8float( pp2.item(j).getTextContent(),floatVec))
										for(s=0;s<8;s++)
											newP.get(s).extension= floatVec.get(s);
									else
										res = false;	
								}
								if (pp2.item(j).getNodeName().equals("maxgap")) {
									if(check8float( pp2.item(j).getTextContent(),floatVec))
										for(s=0;s<8;s++)
											newP.get(s).maxgap= floatVec.get(s);
									else
										res = false;	
								}
								if (pp2.item(j).getNodeName().equals("mingap")) {
									if(check8float( pp2.item(j).getTextContent(),floatVec))
										for(s=0;s<8;s++)
											newP.get(s).mingap= floatVec.get(s);
									else
										res = false;	
								}
								if (pp2.item(j).getNodeName().equals("addpervehicle")) {
									if(check8float( pp2.item(j).getTextContent(),floatVec))
										for(s=0;s<8;s++)
											newP.get(s).addpervehicle= floatVec.get(s);
									else
										res = false;	
								}
								if (pp2.item(j).getNodeName().equals("reducegapby")) {
									if(check8float( pp2.item(j).getTextContent(),floatVec))
										for(s=0;s<8;s++)
											newP.get(s).reducegapby= floatVec.get(s);
									else
										res = false;	
								}
								if (pp2.item(j).getNodeName().equals("reduceevery")) {
									if(check8float( pp2.item(j).getTextContent(),floatVec))
										for(s=0;s<8;s++)
											newP.get(s).reduceevery= floatVec.get(s);
									else
										res = false;	
								}
								if (pp2.item(j).getNodeName().equals("maxgreen")) {
									if(check8float( pp2.item(j).getTextContent(),floatVec))
										for(s=0;s<8;s++)
											newP.get(s).maxgreen= floatVec.get(s);
									else
										res = false;	
								}
							}
						}
						P.add(newP);
					}
				}
			}
			phaseA.setSize(intersection.size());
			phaseB.setSize(intersection.size());
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}
//	 --------------------------------------------------------------------------
	public void xmlDump(PrintStream out, int indentlevel) throws IOException {
		// TODO Auto-generated method stub
	}

//	 ========================================================================
//	 INITIALIZATION =========================================================
//	 ========================================================================
	public boolean validate() throws ExceptionConfiguration {
		
		boolean res = super.validate();
		int index, intid;
		
		for(index=0;index<numintersections;index++){
			
			intid = interIndexToId.get(index);
			NodeUJSignal node = intersection.get(index);
			SignalManager s = node.getSigMan();
			
			if(    s.Phase(NEMA._2).Protected() && s.Phase(NEMA._2).Valid() 
				&& s.Phase(NEMA._6).Protected() && s.Phase(NEMA._6).Valid() ){
				if(!validateIntersection(intid,NEMA._2,NEMA._6))
					res = false;
			}
			else 
			if(    s.Phase(NEMA._4).Protected() && s.Phase(NEMA._4).Valid() 
			    && s.Phase(NEMA._8).Protected() && s.Phase(NEMA._8).Valid() ){
				if(!validateIntersection(intid,NEMA._4,NEMA._8))
					res = false;
			}
			else{
				//g_logfile.WriteString("ERROR: Either phases 2 and 6 or 4 and 8 must be protected and valid");
				res = false;
			}
		}
		return res;
		
	}
//	 --------------------------------------------------------------------------
	protected boolean validateIntersection(int intid, int nemaA,int nemaB)
	{
		int i;	
		int index = interIndexToId.indexOf(intid);
		SignalManager s = intersection.get(index).getSigMan();
		Vector< ASC_Parameters > X = P.get(index);

		// Check maxinitial<=maxgreen 
		for(i=0;i<8;i++){
			if( X.get(i).maxinitial > X.get(i).maxgreen ){
				//str.Format("Error in param_asc.txt, node %s, phase %d : Maxgreen cannot be smaller than mingreen or maxinitial.\n",s.Phase(i).Int().NodeID(),NEMAnum(s.Phase(i).NEMA()));
				//g_logfile.WriteString(str);
				return false;
			}
		}

		// Check maxinitial>=mingreen 
		for(i=0;i<8;i++){
			if( X.get(i).maxinitial<s.Phase(i).Mingreen() ){
				//str.Format("Error in param_asc.txt, node %s, phase %d : Maxinitial cannot be smaller than mingreen.\n",s.Phase(i).Int().NodeID(),NEMAnum(s.Phase(i).NEMA()));
				//g_logfile.WriteString(str);
				return false;
			}
		}

		// Link phases in Ring A
		if( X.get(NEMA._1).lagleft ){
			X.get(NEMA._2).myNEMAnext = NEMA._1;
			if( X.get(NEMA._3).lagleft ){
				X.get(NEMA._4).myNEMAnext = NEMA._3;
				X.get(NEMA._3).myNEMAnext = NEMA._2;
				X.get(NEMA._1).myNEMAnext = NEMA._4;
			}
			else{
				X.get(NEMA._3).myNEMAnext = NEMA._4;
				X.get(NEMA._4).myNEMAnext = NEMA._2;
				X.get(NEMA._1).myNEMAnext = NEMA._3;
			}
		}
		else{
			X.get(NEMA._1).myNEMAnext = NEMA._2;
			if( X.get(NEMA._3).lagleft ){
				X.get(NEMA._4).myNEMAnext = NEMA._3;
				X.get(NEMA._3).myNEMAnext = NEMA._1;
				X.get(NEMA._2).myNEMAnext = NEMA._4;
			}
			else{
				X.get(NEMA._3).myNEMAnext = NEMA._4;
				X.get(NEMA._4).myNEMAnext = NEMA._1;
				X.get(NEMA._2).myNEMAnext = NEMA._3;
			}
		}
 
		// Link phases in Ring B
		if( X.get(NEMA._5).lagleft ){
			X.get(NEMA._6).myNEMAnext = NEMA._5;
			if( X.get(NEMA._7).lagleft ){
				X.get(NEMA._8).myNEMAnext = NEMA._7;
				X.get(NEMA._7).myNEMAnext = NEMA._6;
				X.get(NEMA._5).myNEMAnext = NEMA._8;
			}
			else{
				X.get(NEMA._7).myNEMAnext = NEMA._8;
				X.get(NEMA._8).myNEMAnext = NEMA._6;
				X.get(NEMA._5).myNEMAnext = NEMA._7;
			}
		}
		else{
			X.get(NEMA._5).myNEMAnext = NEMA._6;
			if( X.get(NEMA._7).lagleft ){
				X.get(NEMA._8).myNEMAnext = NEMA._7;
				X.get(NEMA._7).myNEMAnext = NEMA._5;
				X.get(NEMA._6).myNEMAnext = NEMA._8;
			}
			else{
				X.get(NEMA._7).myNEMAnext = NEMA._8;
				X.get(NEMA._8).myNEMAnext = NEMA._5;
				X.get(NEMA._6).myNEMAnext = NEMA._7;
			}
		}

		// initialize phases 
		/*
		for(i=0;i<8;i++){
			X.get(i).ascstate = ASCstate.ASC_YELLOWRED;
			X.get(i).done = false; 
			X.get(i).permissiblegap = X.get(i).maxgap;
			X.get(i).initialgreen = s.Phase(i).Mingreen();
			X.get(i).actualyellow = 0.0f;
			X.get(i).actualredclear = 0.0f;
			X.get(i).createextensiontimer(intersection.get(index).getMyNetwork());
			X.get(i).extensiontimer.Reset();
		}

		phaseA.set(index, s.Phase(nemaA));
		phaseB.set(index, s.Phase(nemaB));
		X.get(nemaA).ascstate = ASCstate.ASC_WAIT;
		X.get(nemaB).ascstate = ASCstate.ASC_WAIT;

		s.IssueHold(nemaA);
		s.IssueHold(nemaB);
		*/

		return true;
	}
	

	private void resetIntersection(int intid, int nemaA,int nemaB) {

		int i;	
		int index = interIndexToId.indexOf(intid);
		SignalManager s = intersection.get(index).getSigMan();
		Vector< ASC_Parameters > X = P.get(index);
		
		// initialize phases 
		for(i=0;i<8;i++){
			X.get(i).ascstate = ASCstate.ASC_YELLOWRED;
			X.get(i).done = false; 
			X.get(i).permissiblegap = X.get(i).maxgap;
			X.get(i).initialgreen = s.Phase(i).Mingreen();
			X.get(i).actualyellow = 0.0f;
			X.get(i).actualredclear = 0.0f;
			X.get(i).createextensiontimer(intersection.get(index).getMyNetwork());
			X.get(i).extensiontimer.Reset();
		}

		phaseA.set(index, s.Phase(nemaA));
		phaseB.set(index, s.Phase(nemaB));
		X.get(nemaA).ascstate = ASCstate.ASC_WAIT;
		X.get(nemaB).ascstate = ASCstate.ASC_WAIT;

		s.IssueHold(nemaA);
		s.IssueHold(nemaB);

	}
	

//	 ========================================================================
//	 PRIVATE CLASSES ========================================================
//	 ========================================================================
	public class ASC_Parameters {
		
		// ASC input parameters
		public boolean lagleft;
		public float maxgreen;
		public float maxinitial;
		public float extension;
		public float maxgap;
		public float mingap;
		public float addpervehicle;
		public float reducegapby;
		public float reduceevery;

		// ASC variables
		public ASCstate ascstate;
		public boolean done;
		public float permissiblegap;
		public float initialgreen;
		public float actualyellow;
		public float actualredclear;
		public Timer extensiontimer;
		public int myNEMAnext;
		
		public ASC_Parameters(){};	
		public ASC_Parameters(AbstractNodeComplex n){ createextensiontimer(n); };
		public void createextensiontimer(AbstractNodeComplex n){ extensiontimer = new Timer(n); };
	}
//	 --------------------------------------------------------------------------
	private enum ASCstate {
		ASC_WAIT,ASC_HOLDTIME,ASC_YELLOWRED
	}
//	 --------------------------------------------------------------------------
	private boolean check8float( String str , Vector<Float> x)
	{
		x.clear();
		StringTokenizer st = new StringTokenizer(str, ", \t");
		int j = 0;
		while (st.hasMoreTokens()) {
			x.add(Float.parseFloat( st.nextToken() ) );
			j++;
		}
		if(x.size()!=8)
			return false;
		return true;
	}
//	 --------------------------------------------------------------------------
	private boolean check8bool( String str , Vector<Boolean> x)
	{
		x.clear();
		StringTokenizer st = new StringTokenizer(str, ", \t");
		int z,j = 0;
		while (st.hasMoreTokens()) {
			z = Integer.parseInt(st.nextToken());
			x.add( z>0 );
			j++;
		}
		if(x.size()!=8)
			return false;
		return true;
	}
//	 --------------------------------------------------------------------------
	/*
	private void ResetSignals()
	{
		for(int i=0;i<intersection.size();i++){
			intersection.get(i).getSigMan().Initialize();
		}	
	}
	*/
//	 --------------------------------------------------------------------------
	private class nextPhaseAndBarrier{
		int phase;
		int barrier;
	}
//	 --------------------------------------------------------------------------
	@Override
	public void resetTimeStep() {
		
		int index,intid,i;

		super.resetTimeStep();
		
		for(index=0;index<numintersections;index++){
			
			intid = interIndexToId.get(index);
			NodeUJSignal node = intersection.get(index);
			SignalManager s = node.getSigMan();
			
			if(    s.Phase(NEMA._2).Protected() && s.Phase(NEMA._2).Valid() 
				&& s.Phase(NEMA._6).Protected() && s.Phase(NEMA._6).Valid() ){
				resetIntersection(intid,NEMA._2,NEMA._6);
			}
			else 
			if(    s.Phase(NEMA._4).Protected() && s.Phase(NEMA._4).Valid() 
			    && s.Phase(NEMA._8).Protected() && s.Phase(NEMA._8).Valid() ){
				resetIntersection(intid,NEMA._4,NEMA._8);
			}
		}

	}

}
