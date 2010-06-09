package aurora.hwc.control.signal;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aurora.ExceptionConfiguration;
import aurora.hwc.AbstractLinkHWC;
import aurora.hwc.NodeUJSignal;

public class SignalPhase implements Serializable {

	private static final long serialVersionUID = -2241871335828027166L;
	
//	 ========================================================================
//	 FIELDS =================================================================
//	 ========================================================================

	//private AbstractNodeComplex myNetwork;
	private NodeUJSignal myNode;
	private SignalManager mySigMan;

	private int myNEMA;
	private int myNEMAopposing;
	private int myRingGroup;

	public Vector<AbstractLinkHWC> links;
	public Vector<Integer> myControlIndex;

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
		
		links = new Vector<AbstractLinkHWC>();
		myControlIndex = new Vector<Integer>();
		
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

	public void addLink(AbstractLinkHWC L)  { links.add(L); myControlIndex.add(-1); };
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
	
	public Vector<AbstractLinkHWC> getlinks() { return links; };

	public void setValid(boolean x) {valid = x;};
	
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
		for(int i=0;i<links.size();i++)
			mySigMan.myController.setControlInput(myControlIndex.get(i), links.get(i).getCapacityValue().getCenter() );
		bulbcolor = BulbColor.GREEN;
	}
//	-------------------------------------------------------------------------
	public void SetYellow()
	{
		if(!valid) 
			return;
		for(int i=0;i<links.size();i++)
			mySigMan.myController.setControlInput(myControlIndex.get(i),links.get(i).getCapacityValue().getCenter() );
		bulbcolor = BulbColor.YELLOW;
	}
//	-------------------------------------------------------------------------
	public void SetRed()
	{
		if(!valid) 
			return;
		for(int i=0;i<links.size();i++)
			mySigMan.myController.setControlInput(myControlIndex.get(i),0.0);
		bulbcolor = BulbColor.RED;
	}
//	-------------------------------------------------------------------------
	
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {

		boolean res = true;
		int i,j;
		Node nodeattr;
		String str;
		
		nodeattr = p.getAttributes().getNamedItem("protected");
		if(nodeattr!=null)
			protectd = Boolean.parseBoolean(nodeattr.getNodeValue());
		
		nodeattr = p.getAttributes().getNamedItem("permissive");
		if(nodeattr!=null)
			permissive = Boolean.parseBoolean(nodeattr.getNodeValue());
		
		nodeattr = p.getAttributes().getNamedItem("recall");
		if(nodeattr!=null)
			recall = Boolean.parseBoolean(nodeattr.getNodeValue());

		nodeattr = p.getAttributes().getNamedItem("mingreen");
		if(nodeattr!=null){
			str = nodeattr.getNodeValue();
			if(!str.isEmpty())
				mingreen = Float.parseFloat(str);
		}

		nodeattr = p.getAttributes().getNamedItem("yellowtime");
		if(nodeattr!=null){
			str = nodeattr.getNodeValue();
			if(!str.isEmpty())
				yellowtime = Float.parseFloat(str);
		}
		
		nodeattr = p.getAttributes().getNamedItem("redcleartime");
		if(nodeattr!=null){
			str = nodeattr.getNodeValue();
			if(!str.isEmpty())
				redcleartime = Float.parseFloat(str);
		}
		
		boolean haslinks = false;
		if (p.hasChildNodes()) {
			NodeList pp = p.getChildNodes();
			
			for (i= 0; i<pp.getLength(); i++){
				
				if (pp.item(i).getNodeName().equals("links")) {	
					StringTokenizer st = new StringTokenizer(pp.item(i).getTextContent(), ", \t");
					while (st.hasMoreTokens()) {
						AbstractLinkHWC L = (AbstractLinkHWC) myNode.getMyNetwork().getLinkById(Integer.parseInt(st.nextToken()));
						this.addLink(L);	// assign link to phase
						haslinks = true;
					}	
				}
				
				if (pp.item(i).getNodeName().equals("detectorlist")) {
					NodeList pp2 = pp.item(i).getChildNodes();
					for (j= 0; j<pp2.getLength(); j++){

						if (pp2.item(j).getNodeName().equals("detector")) {	
							
							String type = pp2.item(j).getAttributes().getNamedItem("type").getNodeValue();
							StringTokenizer st = new StringTokenizer(pp2.item(j).getTextContent(), ", \t");
							Vector<Integer> id = new Vector<Integer>();
							while (st.hasMoreTokens()) {
								id.add(Integer.parseInt(st.nextToken()));
							}
							DetectorStation d = new DetectorStation(myNode,myNEMA,type);
							res &= addDetectorStation(d,id);
						}
					}
				}
			}
		}
		
		res &= haslinks;
		
		return res;
		
	}
	
//	-------------------------------------------------------------------------
	public void xmlDump(PrintStream out) throws IOException {

		int nemaid = myNEMA+1;
		
		if(!protectd && !permissive){
			out.print("\n<phase nema=\"" + nemaid + "\" ></phase>");
			return;
		}
		
		out.print("\n<phase nema=\"" + nemaid + "\"");
		out.print("\nprotected=\"" + protectd + "\"");
		out.print("\npermissive=\"" + permissive + "\"");
		out.print("\nrecall=\"" + recall + "\"");
		out.print("\nmingreen=\"" + mingreen + "\"");
		out.print("\nyellowtime=\"" + yellowtime + "\"");
		out.print("\nredcleartime=\"" + redcleartime + "\"");
		out.print(" ></phase>");

		/*  GCG FIX THIS
		out.print("\n<links> "  + link.getId() + " </links>");	
			
		out.print("\n<detectorlist>"); 
		if(ApproachStationIds!=null && !ApproachStationIds.isEmpty())
			out.print("<detector type=\"A\"> " + Util.csvstringint(ApproachStationIds) + " </detector>");
		if(StoplineStationIds!=null && !StoplineStationIds.isEmpty())
			out.print("<detector type=\"S\"> " + Util.csvstringint(StoplineStationIds) + " </detector>");
		out.print("\n</detectorlist>"); 
		
		out.print("\n</phase>");
*/
		return;
	}
	
	
}
