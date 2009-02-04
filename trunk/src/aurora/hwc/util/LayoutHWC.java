/**
 * @(#)LayoutHWC.java
 */

package aurora.hwc.util;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.util.*;
import javax.swing.*;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.utils.*;
import edu.uci.ics.jung.visualization.*;
import aurora.*;


/**
 * Implementation of HWC Layout.
 * @author Alex Kurzhansky
 * @version $Id: LayoutHWC.java,v 1.1.4.1 2008/10/16 04:27:09 akurzhan Exp $
 */
public class LayoutHWC extends AbstractLayout {
	private static final Object HWC_KEY  = "HWC_Visualization_Key";
    private Object key = null;
    private int currentIteration;
    private int mMaxIterations = 1;
	private AffineTransform mTr;
	private Point[] gBounds;
	private JInternalFrame winParent;
	
	public LayoutHWC(aurora.hwc.gui.WindowNetwork w) {
		super(w.getGraph());
		gBounds = w.getGeoBounds();
		winParent = w;
		currentIteration = 0;
	}
	public LayoutHWC(aurora.hwc.config.WindowNetwork w) {
		super(w.getGraph());
		gBounds = w.getGeoBounds();
		winParent = w;
		currentIteration = 0;
	}
	public LayoutHWC(aurora.hwc.gui.WindowPath w) {
		super(w.getGraph());
		gBounds = w.getGeoBounds();
		winParent = w;
		currentIteration = 0;
	}
	
    
    public Object getKey() {
        if (key == null) key = new Pair(this, HWC_KEY);
        return key;
    }

    public HWCVertexData getHLData(Vertex v) {
    	return (HWCVertexData) (v.getUserDatum(getKey()));
    }
	
    
    protected void initialize_local_vertex(Vertex v) {
    	if (v.getUserDatum(getKey()) == null) {
    		v.addUserDatum(getKey(), new HWCVertexData(), UserData.REMOVE);
    	}
    }
    
    protected void initialize_local() {
    	geoInitialize();
    }
    
   
    private void geoInitialize() {
    	Point[] pp = gBounds; 	
    	double xMin = pp[0].x; 
    	double xMax = pp[1].x; 
    	double yMin = pp[0].y;
    	double yMax = pp[1].y; 
    	double deltaX = xMax - xMin; 
    	double deltaY = yMax - yMin; 
    	//Dimension d = getCurrentSize();
    	Dimension d = winParent.getSize();
    	double width = d.getWidth();
    	double height = d.getHeight();
    	double xRatio = Math.min(0.9, 45.0/width);
    	double yRatio = Math.min(0.9, 125.0/height);
    	double sX = width*(1-xRatio)/deltaX; 
    	double sY = height*(1-yRatio)/deltaY;
    	mTr = new AffineTransform(); 
    	mTr.setToIdentity();
    	mTr.translate(5, 5);
    	mTr.scale(sX, sY);
    	mTr.translate(-xMin, -yMin);
    	return;
    }
    
   
    protected void geoInitializeLocation(Vertex v, Coordinates coord) 
    {
    	Point p = ((VertexNodeHWC)v).getPosition();
    	Point2D p2DSrc = new Point2D.Double(p.x, p.y);
    	Point2D p2DDst = new Point2D.Double(); 
    	mTr.transform(p2DSrc , p2DDst);
    	coord.setX(p2DDst.getX());
    	coord.setY(p2DDst.getY());
    	return;
    }
    
    public synchronized Point2D inverseTransform(Point2D dst) {
    	Point2D src = new Point2D.Double();
    	try {
    		mTr.inverseTransform(dst, src);
    	} catch (Exception e) {
    		src = dst;
    	}
    	return src;
    }
    
    public synchronized void update() {
    	try {
    		initialize_local();
    		for (Iterator iter = getGraph().getVertices().iterator(); iter.hasNext();) {
    			Vertex v = (Vertex)iter.next();
    			Coordinates coord = getCoordinates(v);
    			if (coord == null) {
    				coord = new Coordinates();
    				v.addUserDatum(getBaseKey(), coord, UserData.REMOVE);
    			}
    			geoInitializeLocation(v, coord);                    
    			initialize_local_vertex(v);
    		}
    	}
    	catch(ConcurrentModificationException cme) {
    		update();
    	}
    	return;
    }

    public synchronized void advancePositions() { 
    	currentIteration++; 
    	this.update(); 
    } 

    /**
     * This one is an incremental visualization.
     */
	public boolean isIncremental() {
		return true;
	}
	
	/**
	 * Sets maximum number of iterations.
	 * @param maxIterations
	 */
	public void setMaxIterations(int maxIterations) {
        mMaxIterations = maxIterations;
        return;
    }
	
	/**
	 * Sets min/max bounds for graph display
	 * @param x array of two points - min and max.
	 */
	public void setGeoBounds(Point[] x) {
		if ((x != null) && (x.length == 2))
			gBounds = x;
		return;
	}
	
    /**
     * Returns true once the current iteration has passed the maximum count,
     * <tt>MAX_ITERATIONS</tt>.
     */
	public boolean incrementsAreDone() {
		if (currentIteration > mMaxIterations) {
			return true; 
		} 
		return false;
	}
	
	
	public static class HWCVertexData {
        private DoubleMatrix1D disp;

        public HWCVertexData() {
        	initialize();
        }

        public void initialize() {
        	disp = new DenseDoubleMatrix1D(2);
        }

        public double getXDisp() {
        	return disp.get(0);
        }

        public double getYDisp() {
        	return disp.get(1);
        }

        public void setDisp(double x, double y) {
        	disp.set(0, x);
        	disp.set(1, y);
        }

        public void incrementDisp(double x, double y) {
        	disp.set(0, disp.get(0) + x);
        	disp.set(1, disp.get(1) + y);
        }

        public void decrementDisp(double x, double y) {
        	disp.set(0, disp.get(0) - x);
        	disp.set(1, disp.get(1) - y);
        }
	}
}