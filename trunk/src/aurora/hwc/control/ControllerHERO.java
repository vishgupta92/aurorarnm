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
 * @author Gabriel Gomes, Alex Kurzhanskiy
 * @version $Id: ControllerHERO.java,v 1.1.2.3 2009/08/26 02:24:08 akurzhan Exp $
 */
public class ControllerHERO extends AbstractControllerComplex {
	private static final long serialVersionUID = 2984412645520124561L;
	
	Vector<HERORampInfo> onramps = new Vector<HERORampInfo>();
	
	public ControllerHERO() {
		limits.add(new Double(0));
		limits.add(new Double(99999.99));
	}
	
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
		try {
			NodeList pp = p.getChildNodes();
			for (int i = 0; i < pp.getLength(); i++) {	
				if (pp.item(i).getNodeName().equals("limits")) {
					Node n = null;
					if ((n = pp.item(i).getAttributes().getNamedItem("cmin")) != null)
						limits.set(0 , Double.parseDouble(n.getNodeValue()));	
					if((n = pp.item(i).getAttributes().getNamedItem("cmax")) != null)
						limits.set(1 , Double.parseDouble(n.getNodeValue()));	
				}
				if (pp.item(i).getNodeName().equals("onramps")) {
					NodeList pp2 = pp.item(i).getChildNodes();
					for (int j = 0; j < pp2.getLength(); j++){
						if (pp2.item(j).getNodeName().equals("onramp")) {
							Node n = null;
							AbstractLink lnk = null;
							double gain = 50.0;
							double thr_a = 0.5;
							double thr_d = 0.5;
							if ((n = pp2.item(j).getAttributes().getNamedItem("id")) != null)
								lnk = (AbstractLink)myMonitor.getSuccessorById(Integer.parseInt(n.getNodeValue()));
							if (lnk != null) {
								if ((n = pp2.item(j).getAttributes().getNamedItem("gain")) != null)
									gain = Double.parseDouble(n.getNodeValue());
								if ((n = pp2.item(j).getAttributes().getNamedItem("activation_threshold")) != null)
									thr_a = Double.parseDouble(n.getNodeValue());
								if ((n = pp2.item(j).getAttributes().getNamedItem("deactivation_threshold")) != null)
									thr_d = Double.parseDouble(n.getNodeValue());
								onramps.add(new HERORampInfo(lnk, gain, thr_a, thr_d));
							}
						}
					}
				}
			}
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		if (res)
			res &= initialize();
		return res;
	}
	
	/**
	 * Generates XML description of the HERO controller.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		super.xmlDump(out);
		if (limits.size() == 2)
			out.print("<limits cmin=\"" + Double.toString((Double)limits.get(0))+ "\" cmax=\"" + Double.toString((Double)limits.get(1))+ "\" />");
		out.print("<onramps>\n");
		for(int i = 0; i < onramps.size(); i++){
			out.println("<onramp id=\"" + onramps.get(i).getLink().getId() + "\" gain=\"" + onramps.get(i).getGain() + "\" activation_threshold=\"" + onramps.get(i).getActivationThreshold() + "\" deactivation_threshold=\"" + onramps.get(i).getDeactivationThreshold() + "\" />");
		}
		out.print("</onramps>\n");
		out.print("</controller>");
		return;
	}
	
	/**
	 * Initializes slave controllers.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public boolean initialize() {
		Vector<AbstractNetworkElement> nes = myMonitor.getSuccessors();
		int i = 0;
		while (i < onramps.size()) {
			if (nes.indexOf(onramps.get(i).getLink()) < 0) {
				AbstractLink lnk = onramps.get(i).getLink();
				AbstractNodeSimple nd = (AbstractNodeSimple)lnk.getEndNode();
				if ((nd != null) && (nd.getSimpleController(lnk) != null)) {
					nd.getSimpleController(lnk).setDependent(false);
					nd.setSimpleController(null, lnk);
				}
				onramps.remove(i);
			}
			else
				i++;
		}
		for(i = 0; i < nes.size(); i++) {
			if ((nes.get(i).getType() & TypesHWC.MASK_LINK) == 0)
				continue;
			AbstractLink lnk = (AbstractLink)nes.get(i);
			if (getOnrampInfoById(lnk.getId()) == null)
				onramps.insertElementAt(new HERORampInfo(lnk), i);
			AbstractNodeSimple nd = (AbstractNodeSimple)lnk.getEndNode();
			ControllerALINEA ctrl = new ControllerALINEA();
			if ((nd.getSimpleController(lnk) == null) || (!nd.getSimpleController(lnk).getClass().getName().equals(ctrl.getClass().getName()))) {
				if (nd.getSimpleController(lnk) != null)
					nd.getSimpleController(lnk).setDependent(false);
				nd.setSimpleController(ctrl, lnk);
			}
		}
		return true;
	}

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
		return onramps;
	}
	
	/**
	 * Returns on-ramp info by on-ramp id. 
	 */
	public HERORampInfo getOnrampInfoById(int id) {
		HERORampInfo ri = null;
		for (int i = 0; i < onramps.size(); i++)
			if (onramps.get(i).getLink().getId() == id) {
				ri = onramps.get(i);
				break;
			}
		return ri;
	}
	
	/**
	 * Overrides <code>java.lang.Object.toString()</code>.
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
		private double gain = 50.0;
		private double thr_a = 0.5; // activation threshold
		private double thr_d = 0.5; // deactivation threshold
		
		
		public HERORampInfo() { }
		public HERORampInfo(AbstractLink lnk) { link = lnk; }
		public HERORampInfo(AbstractLink lnk, double g, double a, double d) {
			link = lnk;
			if (g >= 0)
				gain = g;
			if ((a >= 0) && (a <= 1))
				thr_a = a;
			if ((d >= 0) && (d <= 1))
				thr_d = d;
		}
		
		
		public AbstractLink getLink() {
			return link;
		}
		
		public double getGain() {
			return gain;
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
				gain = g;
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