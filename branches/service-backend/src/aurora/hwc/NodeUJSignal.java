/**
 * @(#)NodeUJSignal.java
 */

package aurora.hwc;

import java.io.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import aurora.*;
import aurora.hwc.control.signal.*;

/**
 * Urban Junction with Signal.
 * <br>Allowed input links (predecessors): LinkStreet, LinkFR, LinkDummy.
 * <br>Allowed output links (successors): LinkStreet, LinkOR, LinkDummy.
 * 
 * @see LinkStreet, LinkOR, LinkFR, LinkDummy
 *  
 * @author Alex Kurzhanskiy, Gabriel Gomes
 * @version $Id$
 */
public final class NodeUJSignal extends AbstractNodeHWC {
	private static final long serialVersionUID = 8638143194434976281L;

	private boolean secondpass = false;
	private SignalManager sigman = null;
	public boolean hasdata = false;

	
	public NodeUJSignal() { 		
		sigman = new SignalManager(this);
	}
	public NodeUJSignal(int id) { 
		this.id = id; 
		sigman = new SignalManager(this);
	}

	
	public SignalManager getSigMan() { return sigman; };

	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		boolean res = sigman.Update();
		res &= super.dataUpdate(ts);		
		return res;
	}
	
	/**
	 * Initializes the simple Node from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = super.initFromDOM(p);
		int i, j;
		Node nodeattr;

		if (secondpass)
		{
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (i = 0; i < pp.getLength(); i++) {
					
					if (pp.item(i).getNodeName().equals("signal")){
						
						hasdata = true;

						if (pp.item(i).hasChildNodes()) {
							NodeList pp2 = pp.item(i).getChildNodes();
							for (j = 0; j < pp2.getLength(); j++){

								if (pp2.item(j).getNodeName().equals("phase")) {

									nodeattr = pp2.item(j).getAttributes().getNamedItem("nema");
									if(nodeattr==null)
										return false;
									int nemaind = NEMAtoIndex(Integer.parseInt(nodeattr.getNodeValue()));
									if (nemaind<0)
										return false;
									res &= sigman.Phase(nemaind).initFromDOM(pp2.item(j));
								}
							}
						}
					}	// end "signal"
				}
			}
			else
				res = false;
		}	// secondpass
		else{
			secondpass = true;
		}
		
		return res;
	}
	
	/**
	 * Generates XML description of the NodeUJSignal.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		if (out == null)
			out = System.out;
		out.print("<node type=\"" + getTypeLetterCode() + "\" id=\"" + id + "\" name=\"" + name + "\">");
		out.print("<description>" + description + "</description>\n");
		out.print("<outputs>");
		for (int i = 0; i < successors.size(); i++)
			out.print("<output id=\"" + successors.get(i).getId() + "\"/>");
		out.print("</outputs>\n<inputs>");
		for (int i = 0; i < predecessors.size(); i++) {
			String buf = "";
			String buf2 = "";
			for (int j = 0; j < successors.size(); j++) {
				if (j > 0) {
					buf += ", ";
					buf2 += ", ";
				}
				buf += splitRatioMatrix[i][j].toString();
				buf2 += Double.toString(weavingFactorMatrix[i][j]);
			}
			out.print("<input id=\"" + predecessors.get(i).getId() + "\">");
			out.print("<splitratios>" + buf + "</splitratios>");
			out.print("<weavingfactors>" + buf2 + "</weavingfactors>");
			if (controllers.get(i) != null)
				controllers.get(i).xmlDump(out);
			out.print("</input>");
		}
		out.print("</inputs>\n");
		if (srmProfile != null)
			out.print("<splitratios tp=\"" + Double.toString(srTP) + "\">\n" + getSplitRatioProfileAsXML() + "</splitratios>\n");
		position.xmlDump(out);
		if (hasdata) {
			out.print("<signal>\n");
			for (int i = 0; i < 8; i++) {	
				sigman.Phase(i).xmlDump(out);
			}
			out.print("</signal>\n");
		}
		out.print("</node>\n");
		return;
	}
	
	/**
	 * Validates Node configuration.<br>
	 * Checks that in- and out-links are of correct types.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ConfigurationException
	 */
	public boolean validate() throws ExceptionConfiguration {
		boolean res = super.validate();
		int type;
		String cnm;
		for (int i = 0; i < predecessors.size(); i++) {
			type = predecessors.get(i).getType();
			cnm = predecessors.get(i).getClass().getName();
			if ((type != TypesHWC.LINK_STREET) &&
				(type != TypesHWC.LINK_OFFRAMP) &&
				(type != TypesHWC.LINK_HIGHWAY) &&
				(type != TypesHWC.LINK_DUMMY)) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "In-Link of wrong type (" + cnm + ")."));
				res = false;
			}
		}
		for (int i = 0; i < successors.size(); i++) {
			type = successors.get(i).getType();
			cnm = successors.get(i).getClass().getName();
			if ((type != TypesHWC.LINK_STREET) &&
				(type != TypesHWC.LINK_ONRAMP) &&
				(type != TypesHWC.LINK_HIGHWAY) &&
				(type != TypesHWC.LINK_DUMMY)) {
				myNetwork.addConfigurationError(new ErrorConfiguration(this, "Out-Link of wrong type (" + cnm + ")."));
				res = false;
			}
		}
		
		// Connect detector stations to sensors
		for(int i = 0; i < 8; i++){
			if(sigman.Phase(i).ApproachStation()!=null)
				res &= sigman.Phase(i).ApproachStation().AssignLoops(sigman.Phase(i).ApproachStationIds);
			if(sigman.Phase(i).StoplineStation()!=null)
				res &= sigman.Phase(i).StoplineStation().AssignLoops(sigman.Phase(i).StoplineStationIds);
		}
		
		return res;
	}
	

	/**
	 * Additional initialization.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration, ExceptionDatabase
	 * @throws ExceptionDatabase 
	 */
	public boolean initialize() throws ExceptionConfiguration, ExceptionDatabase {
		boolean res = super.initialize();
		sigman.initialize();
/*		int i;
		for(i=0;i<8;i++){
			if(sigman.Phase(i).link!=null){
				sigman.attachSimpleController( this , sigman.Phase(i).link);	
			}
		}*/
		return res;
	}
	
	private int NEMAtoIndex(int nema){
		if(nema<1 || nema > 8)
			return -1;
		else
			return nema-1;
	}
	
	/**
	 * Returns type.
	 */
	public final int getType() {
		return TypesHWC.NODE_SIGNAL;
	}
	
	/**
	 * Returns letter code of the Node type.
	 */
	public String getTypeLetterCode() {
		return "S";
	}
	
	/**
	 * Returns type description.
	 */
	public final String getTypeString() {
		return "Signal Intersection";
	}
	
	/**
	 * Returns compatible simple controller type names.
	 */
	public String[] getSimpleControllerTypes() {
		String[] ctrlTypes = {"Simple Signal"};
		return ctrlTypes;
	}
	
	/**
	 * Returns compatible simple controller classes.
	 */
	public String[] getSimpleControllerClasses() {
		String[] ctrlClasses = {"aurora.hwc.control.ControllerSimpleSignal"};
		return ctrlClasses;
	}
	
	
	/*
	public class Phase  {
		private int NEMAid;
		private Vector<AbstractLink> mylinks = new Vector<AbstractLink>();
		private NodeUJSignal signalnode;
		
		public Phase(int nema, NodeUJSignal s){
			NEMAid = nema;
			signalnode = s;
		}
		public void addLink(AbstractLink L){
			mylinks.add(L);
		}
		public int getNEMA() { 
			return NEMAid; 
			};
		public int getNumLinks() { 
			return mylinks.size();
			};
		public AbstractLink getLink(int i) { 
			return mylinks.get(i); 
			};
		public NodeUJSignal getSignalNode(){ 
			return signalnode; 
		};
	}
	*/

}