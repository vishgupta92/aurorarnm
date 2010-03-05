/**
 * @(#)AbstractControllerSimpleHWC.java
 */

package aurora.hwc.control;

import java.io.*;
import java.util.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import aurora.*;
import aurora.hwc.*;

/**
 * Base class for simple Node controllers.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public abstract class AbstractControllerSimpleHWC extends AbstractControllerSimple {
	private static final long serialVersionUID = -169822305017518736L;
	
	protected AbstractQueueController myQController;
	public boolean urms;								// whether to use the URMS state transitions diagram
	
	public boolean usesensors = false;	// TEMPORARY!
	public SensorLoopDetector mlsensor = null;
	public AbstractLinkHWC mlup = null;
	
	// parameters in the URMS state transition diagram
	public enum State { NonMeterWaiting, NonMeterReady,  NonMeterToMeter, Meter, MeterToNonMeter };	 				 
	protected State URMSstate = null;
	protected URMSLookupTable URMStable = null;
	protected float nonmeteringtime;		// [sec]
	protected float minstartupperiod;		// [sec]
	protected float minmeteringperiod;		// [sec]
	protected float minshutdownperiod;		// [sec]	
	protected StopWatch timer;
	
	public AbstractControllerSimpleHWC() {
		input = (Double)(-1.0);
		actualInput = (Double)(-1.0);
		limits.add((Double)0.0);
		limits.add((Double)99999.99);
	}
	
	/**
	 * Initializes controller from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = super.initFromDOM(p);
		if (!res)
			return res;
		try  {
			if (p.getAttributes().getNamedItem("usesensors") != null)
				usesensors = Boolean.parseBoolean(p.getAttributes().getNamedItem("usesensors").getNodeValue());
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (int i = 0; i < pp.getLength(); i++) {					
					if (pp.item(i).getNodeName().equals("limits")) {
						limits = new Vector<Object>();
						limits.add(Double.parseDouble(pp.item(i).getAttributes().getNamedItem("cmin").getNodeValue()));
						limits.add(Double.parseDouble(pp.item(i).getAttributes().getNamedItem("cmax").getNodeValue()));
					}
					if (pp.item(i).getNodeName().equals("qcontroller")) {
						Class c = Class.forName(pp.item(i).getAttributes().getNamedItem("class").getNodeValue());
						myQController = (AbstractQueueController)c.newInstance();
						res &= myQController.initFromDOM(pp.item(i));
						myQController.setMyController(this);
					}
					
					if (pp.item(i).getNodeName().equals("urmstable")) {
						urms = Boolean.parseBoolean(pp.item(i).getAttributes().getNamedItem("useurms").getNodeValue());
						if(urms){
							URMStable = new URMSLookupTable();
							URMStable.initFromDOM(pp.item(i));
							URMSstate = State.NonMeterReady;
						}
					}
				}
			}
			
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}
	/**
	 * Generates XML description of simple controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		if (limits.size() == 2)
			out.print("<limits cmin=\"" + Double.toString((Double)limits.get(0))+ "\" cmax=\"" + Double.toString((Double)limits.get(1))+ "\" />");
		if (myQController != null)
			myQController.xmlDump(out);
		return;
	}
	
	/**
	 * Additional initialization.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initialize() throws ExceptionConfiguration {
		mlup = getUpML((AbstractNodeHWC) myLink.getEndNode());
		if (usesensors) {
			mlsensor = (SensorLoopDetector) myLink.getMyNetwork().getSensorByLinkId(mlup.getId());
		}
		return super.initialize();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	/**
	 * Computes desired input value for given Node.
	 * @param x given Node.
	 * @return input object.
	 */
	public Object computeInput(AbstractNodeHWC x) {		

		Double flw = (Double) super.computeInput(x);
		
		if(flw!=null)
			return flw;
		
		if(!urms)
			return null;
		
		// Update the URMS state transition diagram
		double MLocc,MLflw,MLspd;
		if(this.usesensors){
			MLocc = mlsensor.Occupancy();
			MLflw = mlsensor.Flow() / mlup.getLanes();
			MLspd = mlsensor.Speed();
		}
		else{
			MLocc = mlup.getOccupancy();
			MLflw = mlup.getFlow().sum().getCenter() / mlup.getLanes();
			MLspd = mlup.getSpeed().getCenter();
		}
		
		switch (URMSstate) 
		{
		case NonMeterWaiting:
			if( timer.elapsed() >= nonmeteringtime ){
				URMSstate = State.NonMeterReady;
				timer.reset();
			}
			break;
			
		case NonMeterReady:
			if( MLocc>URMStable.t_occ.get(1) || MLflw>URMStable.t_flw.get(1) || MLspd<URMStable.t_spd.get(1) ){
				URMSstate = State.NonMeterToMeter;
				timer.reset();
			}
			break;
			
		case NonMeterToMeter:
			if( timer.elapsed()>=minstartupperiod){ // && sufficient gap
				URMSstate = State.Meter;
				timer.reset();
			}
			break;
			
		case Meter:
			if( timer.elapsed()>=minmeteringperiod &&
					( MLocc<URMStable.t_occ.get(0) || MLflw<URMStable.t_flw.get(0) || MLspd>URMStable.t_spd.get(0) ) && !myQController.inOverride ){
				URMSstate = State.MeterToNonMeter;	
				timer.reset();
			}
			break;
			
		case MeterToNonMeter:	
			if( timer.elapsed()>=minshutdownperiod && !myQController.inOverride ){
				URMSstate = State.NonMeterWaiting;
				timer.reset();
			}
			break;
		}
		
		return null;
	}
	
	/**
	 * Returns queue controller.
	 */
	public final AbstractQueueController getQController() {
		return myQController;
	}
	
	/**
	 * Sets queue controller.
	 * @param x queue controller.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public final synchronized boolean setQController(AbstractQueueController x) {
		myQController = x;
		return true;
	}
	
	/**
	 * Implementation of a "deep copy" of the object.
	 */
	public AbstractController deepCopy() {
		AbstractControllerSimpleHWC ctrlCopy = (AbstractControllerSimpleHWC)super.deepCopy();
		if ((ctrlCopy != null) && (myQController != null))
			ctrlCopy.setQController(myQController.deepCopy());
		return ctrlCopy;
	}
	
	protected final AbstractLinkHWC getUpML(AbstractNodeHWC x) {
		int ii = 0;
		for (int i = 0; i < x.getPredecessors().size(); i++)
			if ((x.getPredecessors().get(i).getType() == TypesHWC.LINK_FREEWAY) ||
				(x.getPredecessors().get(i).getType() == TypesHWC.LINK_HIGHWAY)) {
				ii = i;
				break;
			}
		return (AbstractLinkHWC)x.getPredecessors().get(ii);
	}
	
	protected final AbstractLinkHWC getDnML(AbstractNodeHWC x) {
		int ii=0;
		for (int i = 0; i < x.getSuccessors().size(); i++)
			if ((x.getSuccessors().get(i).getType() == TypesHWC.LINK_FREEWAY) ||
				(x.getSuccessors().get(i).getType() == TypesHWC.LINK_HIGHWAY)) {
				ii = i;
				break;
			}
		return (AbstractLinkHWC)x.getSuccessors().get(ii);
	}
	
	protected final Double ApplyQueueControl(Double flw){
		AbstractNodeHWC x = (AbstractNodeHWC)myLink.getEndNode();
		if ((((NodeHWCNetwork)x.getMyNetwork()).hasQControl()) && (myQController != null))		
			return Math.max(flw, (Double)myQController.computeInput(x, (AbstractLinkHWC)myLink));
		else
			return flw;
	}

	protected final Double ApplyLimits(Double flw){
		flw = Math.min((Double)limits.get(1), Math.max(flw, (Double)limits.get(0)));		
		return (Double)Math.max(flw, 0.0);
	}
	
	protected final Double ApplyURMS(Double crate){
		
		if( !urms || URMSstate==null  || URMSstate==State.Meter ){
			return crate;
		}
		
		switch(URMSstate){
		case MeterToNonMeter:
			return URMStable.meteringrate.get(0);	// GG UNITS???
		case NonMeterReady:
		case NonMeterWaiting:
			return Double.POSITIVE_INFINITY;
		case NonMeterToMeter:
			return URMStable.meteringrate.get(1);	// GG UNITS???
		}
		return Double.NaN;
	}
	
	public class StopWatch {
		double starttime;		// [sec]
		public void reset(){
			starttime = myLink.getMyNetwork().getSimTime()*3600f;
		}
		public double elapsed(){
			return myLink.getMyNetwork().getSimTime()*3600f-starttime;	// [sec]
		}
	}
	
	public class URMSLookupTable implements Serializable {
		private static final long serialVersionUID = -7830795185385939811L;
		
		public Vector<Float> t_occ;			// [%] Occupancy thresholds
		public Vector<Float> t_flw; 		// [vphpl] Flow thresholds
		public Vector<Float> t_spd;			// [mph] Speed thresholds
		public Vector<Double> meteringrate;	// [vphpl] metering rate
		
		public boolean initFromDOM(Node p) throws ExceptionConfiguration {
			URMStable.t_occ = new Vector<Float>();
			URMStable.t_flw = new Vector<Float>();
			URMStable.t_spd = new Vector<Float>();
			URMStable.meteringrate  = new Vector<Double>();
			
			float val;
			Double dval;
			try  {
				if (p.hasChildNodes()) {
					NodeList pp = p.getChildNodes();
					for (int i = 0; i < pp.getLength(); i++) {
						if (pp.item(i).getNodeName().equals("nonmeteringtime")) {
							nonmeteringtime = Float.parseFloat(pp.item(i).getAttributes().getNamedItem("value").getNodeValue());
						}
						if (pp.item(i).getNodeName().equals("minstartupperiod")) {
							minstartupperiod = Float.parseFloat(pp.item(i).getAttributes().getNamedItem("value").getNodeValue());
						}
						if (pp.item(i).getNodeName().equals("minmeteringperiod")) {
							minmeteringperiod = Float.parseFloat(pp.item(i).getAttributes().getNamedItem("value").getNodeValue());
						}
						if (pp.item(i).getNodeName().equals("minshutdownperiod")) {
							minshutdownperiod = Float.parseFloat(pp.item(i).getAttributes().getNamedItem("value").getNodeValue());
						}
						if (pp.item(i).getNodeName().equals("row")) {
							val = Float.parseFloat(pp.item(i).getAttributes().getNamedItem("occ").getNodeValue());
							if(val>=0)
								URMStable.t_occ.add(val);
							else
								URMStable.t_occ.add(Float.NaN);

							val = Float.parseFloat(pp.item(i).getAttributes().getNamedItem("spd").getNodeValue());
							if(val>=0)
								URMStable.t_spd.add(val);
							else
								URMStable.t_spd.add(Float.NaN);

							val = Float.parseFloat(pp.item(i).getAttributes().getNamedItem("flw").getNodeValue());
							if(val>=0)
								URMStable.t_flw.add(val);
							else
								URMStable.t_flw.add(Float.NaN);
							
							dval = Double.parseDouble(pp.item(i).getAttributes().getNamedItem("rate").getNodeValue());
							if(val>=0)
								URMStable.meteringrate.add(dval);
							else
								URMStable.meteringrate.add(Double.NaN);
						}
						
					}
				}
			}
			catch(Exception e) {
				throw new ExceptionConfiguration(e.getMessage());
			}
			return true;
		}
	}
	
	
}
