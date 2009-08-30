/**
 * @(#)NodeUJSignal.java
 */

package aurora.hwc;

import java.io.*;
import java.util.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import aurora.*;
import aurora.hwc.control.signal.*;
import aurora.util.*;


/**
 * Urban Junction with Signal.
 * <br>Allowed input links (predecessors): LinkStreet, LinkFR, LinkDummy.
 * <br>Allowed output links (successors): LinkStreet, LinkOR, LinkDummy.
 * 
 * @see LinkStreet, LinkOR, LinkFR, LinkDummy
 *  
 * @author Alex Kurzhanskiy, Gabriel Gomes
 * @version $Id: NodeUJSignal.java,v 1.9.2.2.2.1.2.2 2009/06/19 21:09:28 gomes Exp $
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
		int nemaind, nema;
		int i, j;

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
							
								if (pp2.item(j).getNodeName().equals("protected")) 
									res &= sigman.setProtected(readIntString(pp2.item(j)));
							
								if (pp2.item(j).getNodeName().equals("permissive")) 
									res &= sigman.setPermissive(readIntString(pp2.item(j)));
							
								if (pp2.item(j).getNodeName().equals("recall")) 
									res &= sigman.setRecall(readIntString(pp2.item(j)));
							
								if (pp2.item(j).getNodeName().equals("mingreen"))
									res &= sigman.setMinGreen(readFloatString(pp2.item(j)));
							
								if (pp2.item(j).getNodeName().equals("yellowtime"))
									res &= sigman.setYellowTime(readFloatString(pp2.item(j)));
							
								if (pp2.item(j).getNodeName().equals("redcleartime"))
									res &= sigman.setRedClearTime(readFloatString(pp2.item(j)));

								if (pp2.item(j).getNodeName().equals("phases")) {
									if (pp2.item(j).hasChildNodes()) {
										NodeList pp3 = pp2.item(j).getChildNodes();
										for (int k = 0; k < pp3.getLength(); k++){
											if (pp3.item(k).getNodeName().equals("phase")) {			
												nema = Integer.parseInt(pp3.item(k).getAttributes().getNamedItem("nema").getNodeValue());
												nemaind = NEMAtoIndex(nema);
												if (nemaind<0 || nemaind>7){
													res = false;
													continue;
												}
												NodeList pp4 = pp3.item(k).getChildNodes();
												for (int z = 0; z < pp4.getLength(); z++) {
													if (pp4.item(z).getNodeName().equals("link")){
														StringTokenizer st = new StringTokenizer(pp4.item(z).getTextContent(), ", \t");
														while (st.hasMoreTokens()) {
															AbstractLinkHWC L = (AbstractLinkHWC) myNetwork.getLinkById(Integer.parseInt(st.nextToken()));
															sigman.Phase(nemaind).assignLink(L);	// assign link to phase
															if(!sigman.attachSimpleController(this,L)){	// create a simple controller
																res = false;
																continue;
															}
														}	
													}
												}
											}
										}
									}
									else
										res = false;
								}

								if (pp2.item(j).getNodeName().equals("detector")) {
									nema = Integer.parseInt(pp2.item(j).getAttributes().getNamedItem("nema").getNodeValue());
									nemaind = NEMAtoIndex(nema);
									if (nemaind<0 || nemaind>7){
										res = false;
										continue;
									}
									String pos = pp2.item(j).getAttributes().getNamedItem("pos").getNodeValue();
									Vector<Integer> id = new Vector<Integer>();
									StringTokenizer st = new StringTokenizer(pp2.item(j).getAttributes().getNamedItem("id").getNodeValue(), ", \t");
									while (st.hasMoreTokens()) {
										id.add(Integer.parseInt(st.nextToken()));
									}
									DetectorStation d = new DetectorStation(this,nemaind,pos);
									res &= sigman.Phase(nemaind).addDetectorStation(d,id);
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
		out.print("\n<node class=\"" + this.getClass().getName() + "\" id=\"" + id + "\" name=\"" + name + "\">\n");
		super.xmlDump(out);
		if(!hasdata){
			out.print("</node>\n");
			return;
		}
		out.print("<signal>\n");
		out.print("<protected>" + Util.csvstringbool(sigman.getVecProtected()) +  "</protected>\n");
		out.print("<permissive>" + Util.csvstringbool(sigman.getVecPermissive()) +  "</permissive>\n");
		out.print("<recall>" + Util.csvstringbool(sigman.getVecRecall()) +  "</recall>\n");
		out.print("<mingreen>" + Util.csvstringflt(sigman.getVecMingreen()) +  "</mingreen>\n");
		out.print("<yellowtime>" + Util.csvstringflt(sigman.getVecYellowTime() ) +  "</yellowtime>\n");
		out.print("<redcleartime>" + Util.csvstringflt(sigman.getVecRedClearTime() ) +  "</redcleartime>\n");
		out.print("<phases>\n");
		for(int i = 0; i < 8; i++){
			// GG FIX THIS
			out.print("<phase nema=\"" + (i+1) + "\"> <link> " + sigman.Phase(i).getlink().getId() + " </link> </phase>\n");
		}
		out.print("</phases>\n");
		// GCG FIX THIS
		for(int i = 0; i < 8; i++)
			out.print("<detector nema=\"" + (i+1) + "\" pos=\"S\" id=\"" + sigman.Phase(i).StoplineStationIds.get(0) + "\"> </detector>\n");
		for(int i = 0; i < 8; i++)
			out.print("<detector nema=\"" + (i+1) + "\" pos=\"A\" id=\"" + sigman.Phase(i).ApproachStationIds.get(0) + "\"> </detector>\n");
		out.print("</signal>\n");
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
	

	@Override
	public void resetTimeStep() {
		super.resetTimeStep();
		sigman.resetTimeStep();
	}
	
	
	private Vector<Integer> readIntString(Node p){
		StringTokenizer st = new StringTokenizer(p.getTextContent(), ", \t");
		Vector<Integer> x = new Vector<Integer>();
		while (st.hasMoreTokens()) {
			x.add(Integer.parseInt(st.nextToken()));
		}
		return x;
	}
	
	private Vector<Float> readFloatString(Node p){
		StringTokenizer st = new StringTokenizer(p.getTextContent(), ", \t");
		Vector<Float> x = new Vector<Float>();
		while (st.hasMoreTokens()) {
			x.add(Float.parseFloat(st.nextToken()));
		}
		return x;
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