package aurora.hwc.util;

import aurora.AbstractNode;
import aurora.Point;
import aurora.hwc.NodeUJSignal;
import aurora.hwc.control.signal.BulbColor;

public class VertexNodeSIG extends VertexNodeHWC {

	private boolean display = false;
	private NodeUJSignal myNodeSig = null;
	private int phaseind = -1;
	
	public VertexNodeSIG() { super(); }
	public VertexNodeSIG(AbstractNode n) {
		super();
		node = n;
		pos = node.getPosition().get();
	}
	public VertexNodeSIG(Point p) {
		super();
		pos = p;
	}
	public VertexNodeSIG(Point p,boolean s) {
		super();
		pos = p;
		display = s;
		myNodeSig = null;
	}
	public VertexNodeSIG(Point p,boolean s,NodeUJSignal n,int phase) {
		super();
		pos = p;
		display = s;
		myNodeSig = n;
		phaseind = phase;
	}

	/**
	 * Returns vertex show value.
	 */
	public boolean getDisplay() {
		return display;
	}
	
	/**
	 * Returns signal display for this vertex
	 */
	public BulbColor getSignalColor() {
		return myNodeSig.getSigMan().getPhaseColor(phaseind);
	}
}
