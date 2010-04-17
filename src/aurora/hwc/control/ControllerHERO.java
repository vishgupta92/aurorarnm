/**
 * @(#)ControllerHERO.java
 */

package aurora.hwc.control;

import java.io.*;
import java.util.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import aurora.*;
import aurora.hwc.*;

/**
 * Implementation of HERO controller.
 * 
 * @author Gabriel Gomes, Alex Kurzhanskiy
 * @version $Id: ControllerHERO.java,v 1.1.2.3 2009/08/26 02:24:08 akurzhan Exp
 *          $
 */
public class ControllerHERO extends AbstractControllerComplexHWC {
	private static final long serialVersionUID = 2984412645520124561L;

	// reference maps
	private int numonramps;
	private Vector<HERORampInfo> rampinfo = new Vector<HERORampInfo>();
		
	// state
	private Vector<Boolean> coordwithupstrm = new Vector<Boolean>(); // true if onramp is coordinating

	// CONSTRUCTORS ========================================================================
	public ControllerHERO() {
		limits.add(new Double(0));
		limits.add(new Double(99999.99));
	}
	
	// XMLREAD, VALIDATE, INITIALIZE, XMLDUMP ==============================================
	/**
	 * Initializes the HERO controller from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = super.initFromDOM(p);
		if (!res)
			return res;

		int i;
		try {
			NodeList pp = p.getChildNodes();
			for (i = 0; i < pp.getLength(); i++) {

				if (pp.item(i).getNodeName().equals("onramps")) {
					NodeList pp2 = pp.item(i).getChildNodes();
					for (int j = 0; j < pp2.getLength(); j++) {
						if (pp2.item(j).getNodeName().equals("onramp")) {
							Node n = null;
							AbstractLink lnk = null;
							double gainAlinea = 50.0;
							double gainHERO = 0.9;
							double thr_a = 0.5;
							double thr_d = 0.5;
							if ((n = pp2.item(j).getAttributes().getNamedItem("id")) != null)
								lnk = (AbstractLink) myMonitor.getSuccessorById(Integer.parseInt(n.getNodeValue()));
							if (lnk != null) {
								if ((n = pp2.item(j).getAttributes().getNamedItem("gain_Alinea")) != null)
									gainAlinea = Double.parseDouble(n.getNodeValue());
								if ((n = pp2.item(j).getAttributes().getNamedItem("gain_HERO")) != null)
									gainHERO = Double.parseDouble(n.getNodeValue());
								if ((n = pp2.item(j).getAttributes().getNamedItem("activation_threshold")) != null)
									thr_a = Double.parseDouble(n.getNodeValue());
								if ((n = pp2.item(j).getAttributes().getNamedItem("deactivation_threshold")) != null)
									thr_d = Double.parseDouble(n.getNodeValue());
								rampinfo.add(new HERORampInfo(lnk, gainAlinea, gainHERO, thr_a, thr_d));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}

		return res;
	}

	/**
	 * Additional initialization.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initialize() throws ExceptionConfiguration  {
		boolean res = super.initialize();
		if (!res)
			return false;
		Vector<AbstractNetworkElement> nes = myMonitor.getSuccessors();
		int i = 0;
		while (i < rampinfo.size()) {
			if (nes.indexOf(rampinfo.get(i).getLink()) < 0) {
				AbstractLink lnk = rampinfo.get(i).getLink();
				AbstractNodeSimple nd = (AbstractNodeSimple)lnk.getEndNode();
				if ((nd != null) && (nd.getSimpleController(lnk) != null)) {
					nd.getSimpleController(lnk).setDependent(false);
					nd.setSimpleController(null, lnk);
				}
				rampinfo.remove(i);
			}
			else
				i++;
		}		
		
		for (i = 0; i < nes.size(); i++) {
			if ((nes.get(i).getType() & TypesHWC.MASK_LINK) == 0)
				continue;
			AbstractLink lnk = (AbstractLink)nes.get(i);
			if (getOnrampInfoById(lnk.getId()) == null)
				rampinfo.insertElementAt(new HERORampInfo(lnk), i);
			if (initialized) {
				ControllerSlave ctrl = new ControllerSlave();
				AbstractNodeSimple nd = (AbstractNodeSimple)lnk.getEndNode();
				if ((nd.getSimpleController(lnk) != null) && (nd.getSimpleController(lnk).getClass().getName().equals(ctrl.getClass().getName()))) {
					ctrl = (ControllerSlave)nd.getSimpleController(lnk);
				}
				else {
					if (nd.getSimpleController(lnk) != null)
						nd.getSimpleController(lnk).setDependent(false);
					res &= nd.setSimpleController(ctrl, lnk);
				}
				ctrl.setMyLink(lnk);
				ctrl.setMyComplexController(this);
				addDependentController(ctrl, new Double(0));
			}
		}
		if (!initialized)
			initialized = true;
		numonramps = rampinfo.size();
		for (i = 0; i < numonramps; i++) {
			coordwithupstrm.add(false);
			AbstractLinkHWC orlink = (AbstractLinkHWC)rampinfo.get(i).link; 
			AbstractLinkHWC mllink = (AbstractLinkHWC)getUpMLbyORLink(orlink);
			if(mllink==null)
				mllink = (AbstractLinkHWC) getDnMLbyORLink(orlink);
			rampinfo.get(i).MLlinkindex = ((MonitorControllerHWC)myMonitor).getMonitoredLinkIndexById(mllink.getId());
			rampinfo.get(i).MLsensorindex = ((MonitorControllerHWC)myMonitor).getSensorIndexByLinkId(mllink.getId());
		}

		return res;
	}
	
	/**
	 * Generates XML description of the HERO controller.<br>
	 * If the print stream is specified, then XML buffer is written to the
	 * stream.
	 * 
	 * @param out
	 *            print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		if (limits.size() == 2)
			out.print("<limits cmin=\"" + Double.toString((Double) limits.get(0)) + "\" cmax=\"" + Double.toString((Double) limits.get(1)) + "\" />");
		out.print("<onramps>\n");
		for (int i = 0; i < rampinfo.size(); i++) {
			out.println("<onramp id=\"" + rampinfo.get(i).getLink().getId() + "\" gain=\"" + rampinfo.get(i).getGain() + "\" activation_threshold=\"" + rampinfo.get(i).getActivationThreshold()
					+ "\" deactivation_threshold=\"" + rampinfo.get(i).getDeactivationThreshold() + "\" />");
		}
		out.print("</onramps>\n");
		out.print("</controller>");
		return;
	}
	
	/**
	 * Returns letter code of the controller type.
	 */
	public final String getTypeLetterCode() {
		return "HERO";
	}
	
	private AbstractLinkHWC getUpMLbyORLink(AbstractLink or) {
		AbstractNode x = or.getEndNode();
		for (int i = 0; i < x.getPredecessors().size(); i++){
			AbstractLinkHWC L = (AbstractLinkHWC) x.getPredecessors().get(i);
			if ((L.getType() == TypesHWC.LINK_FREEWAY) || (L.getType() == TypesHWC.LINK_HIGHWAY)) {
				return L;
			}
		}
		return null;
	}

	private AbstractLinkHWC getDnMLbyORLink(AbstractLink or) {
		AbstractNode x = or.getEndNode();
		for (int i = 0; i < x.getSuccessors().size(); i++){
			AbstractLinkHWC L = (AbstractLinkHWC) x.getSuccessors().get(i);
			if ((L.getType() == TypesHWC.LINK_FREEWAY) || (L.getType() == TypesHWC.LINK_HIGHWAY)) {
				return L;
			}
		}
		return null;
	}
	
	// MAIN FUNCTION =======================================================================
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {

		if (!super.dataUpdate(ts))
			return true;

		int i;
		Double AlineaRate, HERORate;
		HERORampInfo thisonramp, dnonramp;
		
		// Determine which onramps request help from upstream
		for (i = 0; i < numonramps; i++) {
	
			thisonramp = rampinfo.get(i);

			if (i < numonramps - 1)
				dnonramp = rampinfo.get(i+1);
			else
				dnonramp = null;

			if (ActivationConditions(thisonramp))
				coordwithupstrm.set(i, true);

			if (DeactivationConditions(thisonramp))
				coordwithupstrm.set(i, false);
		}
		for (i = 0; i < numonramps; i++) {
			thisonramp = rampinfo.get(i);
			AbstractNodeHWC nd = (AbstractNodeHWC)thisonramp.link.getEndNode();
			AlineaRate = ((Double)nd.getSimpleController(thisonramp.link).getActualInput());
			AbstractLinkHWC mllnk = ((MonitorControllerHWC)myMonitor).getMonitoredLink(thisonramp.MLlinkindex);
			if (mllnk != null) {
				double crit_dens = mllnk.getCriticalDensity();
				double actl_dens;
				if(usesensors){
					SensorLoopDetector mlsensor = ((MonitorControllerHWC) myMonitor).getSensor(thisonramp.MLsensorindex);
					actl_dens = mlsensor.Density();
				}
				else
					actl_dens = mllnk.getDensity().sum().getCenter();
				AlineaRate += thisonramp.gainAlinea * (crit_dens - actl_dens);
			}
			// Condition for assisting a downstream on-ramp
			if (i < numonramps - 1 && coordwithupstrm.get(i + 1)) {
				dnonramp = rampinfo.get(i+1);
				HERORate = ComputeHERORate(thisonramp,dnonramp);
				setControlInput(i, Math.min(AlineaRate, HERORate));
			} else {
				setControlInput(i, AlineaRate);
			}

		}

		return true;

	}

	// SUBROUTINES =========================================================================	
	private boolean ActivationConditions( HERORampInfo onrampinfo ) {
		boolean c1 = false;
		boolean c2 = false;
		AbstractLinkHWC orlink = (AbstractLinkHWC) onrampinfo.getLink();
		AbstractLinkHWC mllink = ((MonitorControllerHWC) myMonitor).getMonitoredLink(onrampinfo.MLlinkindex);
		double mlocc = 0.0;
		if(usesensors){
			SensorLoopDetector mlsensor = ((MonitorControllerHWC) myMonitor).getSensor(onrampinfo.MLsensorindex);
			if(mlsensor!=null)	
				mlocc = mlsensor.Occupancy();
		}
		else
			mlocc = mllink.getOccupancy().getCenter();
		c1 = orlink.getQueue().sum().getCenter() / orlink.getQueueMax() > onrampinfo.getActivationThreshold();
		c2 = mlocc > 0.9 * mllink.getCriticalDensity();
		return c1 && c2;		
	}
	
	private boolean DeactivationConditions( HERORampInfo onrampinfo ) {
		boolean c1 = false;
		boolean c2 = false;
		AbstractLinkHWC orlink = (AbstractLinkHWC) onrampinfo.getLink();
		AbstractLinkHWC mllink = ((MonitorControllerHWC) myMonitor).getMonitoredLink(onrampinfo.MLlinkindex);
		double mlocc = 0.0;
		if(usesensors){
			SensorLoopDetector mlsensor = ((MonitorControllerHWC) myMonitor).getSensor(onrampinfo.MLsensorindex);
			if(mlsensor!=null)	
				mlocc = mlsensor.Occupancy();
		}
		else
			mlocc = mllink.getOccupancy().getCenter();
		c1 = orlink.getQueue().sum().getCenter() / orlink.getQueueMax() > onrampinfo.getActivationThreshold();
		c2 = mlocc < 0.8 * mllink.getCriticalDensity();
		return c1 || c2;		
	}

	private Double ComputeHERORate(HERORampInfo thisorinfo, HERORampInfo dnorinfo) {
		AbstractLinkHWC orlink = (AbstractLinkHWC) thisorinfo.getLink();
		AbstractLinkHWC dnorlink = (AbstractLinkHWC) dnorinfo.getLink();

		Double ORQ = orlink.getQueue().sum().getCenter();
		Double ORdnQ = dnorlink.getQueue().sum().getCenter();
		Double wmin = orlink.getQueueMax() * (ORQ + ORdnQ) / (orlink.getQueueMax() + dnorlink.getQueueMax());
		return -thisorinfo.gainHERO * (wmin - ORQ) + orlink.getDemand().sum().getCenter(); // GG
																				// UNITS!!!
	}

	// GUI =================================================================================
	/**
	 * Returns controller description.
	 */
	public String getDescription() {
		String buf = "HERO";
		return buf;
	}

	/**
	 * Returns on-ramp info vector.
	 */
	public Vector<HERORampInfo> getOnrampInfoVector() {
		return rampinfo;
	}

	/**
	 * Returns on-ramp info by on-ramp id.
	 */
	public HERORampInfo getOnrampInfoById(int id) {
		HERORampInfo ri = null;
		for (int i = 0; i < rampinfo.size(); i++)
			if (rampinfo.get(i).getLink().getId() == id) {
				ri = rampinfo.get(i);
				break;
			}
		return ri;
	}

	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
	 * 
	 * @return string that describes the Controller.
	 */
	public String toString() {
		return "HERO";
	}

	/**
	 * Class for describing an on-ramp in HERO.
	 */
	public class HERORampInfo {
		private AbstractLink link; // controlled on-ramp
		private double gainAlinea = 50.0;
		private double gainHERO = 0.9;
		private double thr_a = 0.5; // activation threshold
		private double thr_d = 0.5; // deactivation threshold
		public int MLsensorindex;
		public int MLlinkindex;

		public HERORampInfo() {
		}

		public HERORampInfo(AbstractLink lnk) {
			link = lnk;
		}

		public HERORampInfo(AbstractLink lnk, double gA, double gH, double a, double d) {
			link = lnk;
			if (gA >= 0)
				gainAlinea = gA;
			if ((gH >= 0) && (gH <= 1))
				gainHERO = gH;
			if ((a >= 0) && (a <= 1))
				thr_a = a;
			if ((d >= 0) && (d <= 1))
				thr_d = d;
		}

		public AbstractLink getLink() {
			return link;
		}

		public double getGain() {
			return gainAlinea;
		}

		public double getActivationThreshold() {
			return thr_a;
		}

		public double getDeactivationThreshold() {
			return thr_d;
		}

		public void setLink(AbstractLink lnk) {
			if (lnk != null)
				link = lnk;
			return;
		}

		public void setGain(double g) {
			if (g >= 0)
				gainAlinea = g;
			return;
		}

		public void setActivationThreshold(double a) {
			if ((a >= 0) && (a <= 1))
				thr_a = a;
			return;
		}

		public void setDeactivationThreshold(double d) {
			if ((d >= 0) && (d <= 1))
				thr_d = d;
			return;
		}
	}

}