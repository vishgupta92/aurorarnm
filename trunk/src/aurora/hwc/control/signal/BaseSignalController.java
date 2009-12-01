package aurora.hwc.control.signal;

import java.io.IOException;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.Vector;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aurora.AbstractLink;
import aurora.AbstractNetworkElement;
import aurora.AbstractNodeSimple;
import aurora.ExceptionConfiguration;
import aurora.hwc.NodeUJSignal;
import aurora.hwc.TypesHWC;
import aurora.hwc.control.AbstractControllerComplexHWC;
import aurora.hwc.control.ControllerSlave;

public class BaseSignalController extends AbstractControllerComplexHWC {
	private static final long serialVersionUID = 6659839156661203567L;

	protected int numintersections;
	protected Vector<Integer> interIndexToId;				// [numintersections] id map for all vectors
	protected Vector< NodeUJSignal > intersection;			// [numintersections]
	
	public BaseSignalController() {
		interIndexToId = new Vector<Integer>();
		intersection = new Vector< NodeUJSignal >();
	};
	
	@Override
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
 		boolean res = true;
		if (p == null)
			return false;
		try  {
			int i;
			String buf;
			int id;
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (i = 0; i < pp.getLength(); i++) {
					if (pp.item(i).getNodeName().equals("intersections")){
						buf = pp.item(i).getTextContent();
						StringTokenizer st = new StringTokenizer(buf, ", \t");
						numintersections = 0;
						interIndexToId.clear();
						intersection.clear();
						while (st.hasMoreTokens()) {
							id = Integer.parseInt(st.nextToken());
							NodeUJSignal thisnode = (NodeUJSignal) myMonitor.getMyNetwork().getNodeById(id);
							if(thisnode==null)
								return false;
							numintersections++;
							interIndexToId.add(id);
							intersection.add(thisnode);
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

	@Override
	public void xmlDump(PrintStream out) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean validate() throws ExceptionConfiguration {
		boolean res = super.validate();
		if(!res)
			return res;

		int index;
		for(index=0;index<numintersections;index++){
			
			NodeUJSignal node = intersection.get(index);
			
			SignalManager s = node.getSigMan();
			s.myController = this;
			
/*	    	for(i=0;i<node.getSimpleControllers().size();i++){
	    		ControllerSlave c = (ControllerSlave) node.getSimpleControllers().get(i);
	    		if(c==null)
	    			continue;
	    		c.setMyComplexController(this);
	    		AbstractLink L = (AbstractLink) node.getPredecessors().get(i);
	    		SignalPhase p = node.getSigMan().FindPhaseByLink(L);
	    		if(p==null)
	    			continue;
	    		addDependentController(c, new Double(0));
	    		p.myControlIndex = dependentControllers.indexOf(c);
	    	}*/
			s.validate();
		}
		return true;
	}

	@Override
	public String getDescription() {
		return "Base Signal Controller";
	}

	@Override
	public boolean initialize() throws ExceptionConfiguration {
		boolean res = super.initialize();
		
		int i;
		Vector<AbstractNetworkElement> nes = myMonitor.getSuccessors();
		for (i = 0; i < nes.size(); i++) {
			if ((nes.get(i).getType() & TypesHWC.MASK_LINK) == 0)
				continue;
			AbstractLink lnk = (AbstractLink)nes.get(i);
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
		
		return res;
	}


}
