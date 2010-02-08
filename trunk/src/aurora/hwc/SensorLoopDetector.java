/**
 * @(#)SensorLoopDetector.java
 */

package aurora.hwc;

import java.io.*;
import org.w3c.dom.Node;
import aurora.*;


/**
 * Implementation of loop detector.
 * @author Gabriel Gomes
 * $Id$
 */
public final class SensorLoopDetector extends AbstractSensor {
	private static final long serialVersionUID = 5743495158840080833L;

//	 ========================================================================
//	 FIELDS =================================================================
//	 ========================================================================
	
	private double cumflow = 0.0;
	private boolean iscounter = true;		// GCG Make this input/output
	private double flow = 0.0;
	private double dens = 0.0;
	private double speed = 0.0;
	private int count = 0;
	
	private double looplength = 0.0;
	private double vehiclelength = 17.0/5280.0;
	private AbstractLinkHWC myHWCLink = null;
	//private AbstractNode bnd;
	//private AbstractNode end;

//	 ========================================================================
//	 INTERFACE ==============================================================
//	 ========================================================================
	
	public boolean IsCounter() 	 { return iscounter; } 
//	-------------------------------------------------------------------------
	public boolean ReceivedCall(double dt){
		double lambda;
		lambda = (speed*dt+vehiclelength+looplength)*dens;
		if( Math.random() < 1-Math.exp(-lambda) )
			return true;
		else
			return false;
	}
//	-------------------------------------------------------------------------
	public double Flow()	 { return flow; }
//	-------------------------------------------------------------------------
	public double Density()	 { return dens; }
//	-------------------------------------------------------------------------
	public double Length()	 { return looplength; }
//	-------------------------------------------------------------------------
	public double Speed()	 { return speed; }
//	-------------------------------------------------------------------------
	public double Occupancy() { return dens / myHWCLink.getJamDensity(); }
//	-------------------------------------------------------------------------
	public int Count() { return count; }
//	-------------------------------------------------------------------------
	public void ResetCount(){
		count = 0;
	}

//	 ========================================================================
//	 METHODS AND OVERRIDES===================================================
//	 ========================================================================
	/**
	 * Updates SensorLoopDetector data.<br>
	 * @param ts time step.
	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		boolean res = true;
		res &= super.dataUpdate(ts);

		flow = myHWCLink.getFlow().sum().getCenter();	
		dens = myHWCLink.getDensity().sum().getCenter();
		speed = myHWCLink.getSpeed().getCenter();
		
		if(iscounter){
			cumflow += flow*myNetwork.getTP();
			if(cumflow>=1.0){
				count   += Math.floor(cumflow);
				cumflow -= Math.floor(cumflow);
			}
		}
		
		return res;
	}
//	-------------------------------------------------------------------------
	/*
	public void interpValues(){

		Object value;
		
		// update flow ...............................

		double ifl,ofl;	
		Object q;
		q = bnd.getOutputs().get(bnd.getSuccessors().indexOf(myLink));		// GCG don't assume bnd!=null
		if(q==null)
			ifl = 0.0;
		else
			ifl = (Double) q;
		
		q = end.getInputs().get(end.getPredecessors().indexOf(myLink));
		if(q==null)
			ofl = 0.0;
		else
			ofl = (Double) q;
		flow = ((myLink.getLength()-linkPosition)*ifl + linkPosition*ofl)/myLink.getLength();
		return flow;

		value = myHWCLink.getActualFlow();
		if(value!=null)
			flow = (Double) value;
		else
			flow = 0.0;
		
		
		// update density ...............................
		value = myHWCLink.getDensity();
		if(value!=null)
			dens = (Double) value;
		else
			dens =  0.0;
		
		// update speed .................................
		if(dens<0.0001)
			speed = (Double) myHWCLink.getSpeed();
		else
			speed = flow/dens;
			
	}
*/
//	-------------------------------------------------------------------------
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * @return string that describes the Sensor.
	 */
	public String toString() {
		String buf = "Loop detector";
		buf += " (" + Integer.toString(id) + ")";
		return buf;
	}
//	-------------------------------------------------------------------------
	/**
	 * Initializes the Link from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if ((p == null) || (myNetwork == null))
			return !res;
		res &= super.initFromDOM(p);
		try  {
			looplength = Double.parseDouble(p.getAttributes().getNamedItem("length").getNodeValue()) / 5280.0;
			myHWCLink = (AbstractLinkHWC) myLink;
			//bnd = myHWCLink.getBeginNode();
			//end = myHWCLink.getEndNode();
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		initialized = true;
		return res;
	}
//	-------------------------------------------------------------------------
	public int getType() {
		return TypesHWC.SENSOR_LOOPDETECTOR;
	}
	
	/**
	 * Returns type description.
	 */
	public final String getTypeString() {
		return "Loop Detector";
	}
//	-------------------------------------------------------------------------
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		out.print("<sensor class=\"" + this.getClass().getName() + "\" id=\"" + id + "\" link=\"" + myLink.getId() + "\" linkposition=\"" + linkPosition + "\" length = \"" + looplength*5280.0 + "\"></sensor>\n"); 
		return;
	}
	@Override
	public void updateConfigurationSummary(AbstractConfigurationSummary cs) {
		// TODO Auto-generated method stub
		
	}

}