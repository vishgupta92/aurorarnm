/**
 * 
 */
package aurora.hwc.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.decorators.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.transform.*;

import aurora.AbstractContainer;
import aurora.Point;
import aurora.hwc.AbstractNodeHWC;
import aurora.hwc.NodeUJSignal;
import aurora.hwc.control.signal.BulbColor;
import aurora.hwc.control.signal.SignalManager;
import aurora.hwc.util.EdgeLinkHWC;
import aurora.hwc.util.LayoutHWC;
import aurora.hwc.util.VertexNodeSIG;

/**
 * @author gomes
 *
 */
public class WindowNodeSignal extends WindowNode {
	private static final long serialVersionUID = -7370372059751365616L;

	private NodeUJSignal myNode;

	// live intersection tab
	private Box liveintPanel = Box.createVerticalBox();
	private Graph g;
	private VisualizationViewer visViewer;
	private Point[] geoBounds = new Point[2];
	
	public WindowNodeSignal() { }
	public WindowNodeSignal(AbstractContainer ctnr, AbstractNodeHWC nd, TreePane tp) {
		super(ctnr,nd,tp);
		setSize(450, 650);
		myNode = (NodeUJSignal) nd;
		
		// Configuration tab ================================================

		// basic signal settings panel ......................................
		JPanel parampanel = new JPanel(new GridLayout(1, 0)); 
		parampanel.setBorder(BorderFactory.createTitledBorder("Basic signal settings"));
		parampanel.setMaximumSize(new Dimension(800,150));
		final JTable paramtab = new JTable(new paramTableModel());
		paramtab.setPreferredScrollableViewportSize(new Dimension(200, 150));
		paramtab.getColumnModel().getColumn(0).setMaxWidth(100);
		for(int i=1;i<paramtab.getColumnCount();i++)
			paramtab.getColumnModel().getColumn(i).setMaxWidth(40);
		parampanel.add(new JScrollPane(paramtab));
		confPanel.add(parampanel);

		// Detector stations panel ...........................................
		JPanel spanel = new JPanel(new GridLayout(1, 0)); 
		spanel.setBorder(BorderFactory.createTitledBorder("Detector stations"));
		spanel.setMaximumSize(new Dimension(800,80));
		final JTable sensortab = new JTable(new sensorTableModel());
		sensortab.setPreferredScrollableViewportSize(new Dimension(500, 80));
		sensortab.getColumnModel().getColumn(0).setMaxWidth(100);
		for(int i=1;i<sensortab.getColumnCount();i++)
			sensortab.getColumnModel().getColumn(i).setMaxWidth(40);
	
		spanel.add(new JScrollPane(sensortab));
		confPanel.add(spanel);

		// Live intersection tab ===========================================

		// phase indications panel .........................................
		JPanel phaseindpanel = new JPanel(new GridLayout(1, 0)); 
		phaseindpanel.setBorder(BorderFactory.createTitledBorder("Phase indications"));
		final JTable phaseindtab = new JTable(new phaseindTableModel());
//		phaseindtab.setPreferredScrollableViewportSize(new Dimension(500,40));
		phaseindtab.getColumnModel().getColumn(0).setMaxWidth(100);
		for(int i=1;i<phaseindtab.getColumnCount();i++)
			phaseindtab.getColumnModel().getColumn(i).setMaxWidth(40);
		phaseindpanel.add(new JScrollPane(phaseindtab));
//		phaseindpanel.setMaximumSize(new Dimension(50,10000));
		phaseindpanel.setPreferredSize(new Dimension(500,20));
		liveintPanel.add(phaseindpanel);
		
		// detector calls panel ...........................................
		JPanel detcallspanel = new JPanel(new GridLayout(1, 0)); 
		detcallspanel.setBorder(BorderFactory.createTitledBorder("Detector stations"));
		final JTable detcallstab = new JTable(new detcallTableModel());
//		detcallstab.setPreferredScrollableViewportSize(new Dimension(500, 80));
		detcallstab.getColumnModel().getColumn(0).setMaxWidth(100);
		for(int i=1;i<detcallstab.getColumnCount();i++)
			detcallstab.getColumnModel().getColumn(i).setMaxWidth(40);
		detcallspanel.add(new JScrollPane(detcallstab));
//		detcallspanel.setMaximumSize(new Dimension(500,80));
		detcallspanel.setPreferredSize(new Dimension(500,50));
		liveintPanel.add(detcallspanel);
		
		// controller commands panel .......................................
		JPanel contrlcompanel = new JPanel(new GridLayout(1, 0)); 
		contrlcompanel.setBorder(BorderFactory.createTitledBorder("Controller commands"));
		final JTable controlcomtab = new JTable(new contrlcomTableModel());
		//controlcomtab.setPreferredScrollableViewportSize(new Dimension(500, 40));
		controlcomtab.getColumnModel().getColumn(0).setMaxWidth(100);
		for(int i=1;i<controlcomtab.getColumnCount();i++)
			controlcomtab.getColumnModel().getColumn(i).setMaxWidth(40);
		contrlcompanel.add(new JScrollPane(controlcomtab));
//		contrlcompanel.setMaximumSize(new Dimension(500,40));
		contrlcompanel.setPreferredSize(new Dimension(500,20));
		liveintPanel.add(contrlcompanel);

		// canflicting phase calls panel ...................................
		JPanel confcallpanel = new JPanel(new GridLayout(1, 0)); 
		confcallpanel.setBorder(BorderFactory.createTitledBorder("Conflicting phase calls"));
		final JTable confcalltab = new JTable(new confcallTableModel());
//		confcalltab.setPreferredScrollableViewportSize(new Dimension(500, 40));
		confcalltab.getColumnModel().getColumn(0).setMaxWidth(100);
		for(int i=1;i<confcalltab.getColumnCount();i++)
			confcalltab.getColumnModel().getColumn(i).setMaxWidth(40);
		confcallpanel.add(new JScrollPane(confcalltab));
//		confcallpanel.setMaximumSize(new Dimension(500,40));
		confcallpanel.setPreferredSize(new Dimension(500,20));
		liveintPanel.add(confcallpanel);
		
		// signal graphic panel ...........................................
		JPanel graphpanel = new JPanel(new GridLayout(1, 0)); 
		graphpanel.setBorder(BorderFactory.createTitledBorder("Signal graphic"));
		buildGraph();
		PluggableRenderer renderer = new PluggableRenderer();	
		LayoutHWC layout = new LayoutHWC(this,true);
		DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
		graphMouse.setZoomAtMouse(true);
		visViewer = new VisualizationViewer(layout, renderer);
		visViewer.setGraphMouse(graphMouse);
		visViewer.setBackground(Color.white);
		renderer.setVertexPaintFunction(new VertexPaintFunctionSIG());
		renderer.setVertexShapeFunction(new VertexShapeFunctionSIG());
		renderer.setEdgeShapeFunction(new EdgeShape.Line());
		renderer.setEdgeStrokeFunction(new EdgeStrokeFunctionSIG());
		renderer.setEdgePaintFunction(new GradientEdgePaintFunctionHWC(new PickableEdgePaintFunction(visViewer.getPickedState(), Color.black, Color.cyan), visViewer, visViewer));
		renderer.setEdgeArrowFunction(new EdgeArrowFunctionSIG());	
		graphpanel.add(visViewer);
//		graphpanel.setMaximumSize(new Dimension(500,100));
		graphpanel.setPreferredSize(new Dimension(500,150));
		liveintPanel.add(graphpanel);
		
		tabbedPane.add("Live intersection",liveintPanel);
	}
	
	/**
	 * Updates the graph display.
	 */
	public synchronized void updateView() {
		super.updateView();
		visViewer.repaint();
		liveintPanel.repaint();
		return;
	}
	
	/**
	 * Resets the graph display.
	 */
	public synchronized void resetView() {
		super.resetView();
		visViewer.repaint();
		liveintPanel.repaint();
		return;
	}
	
	/**
	 * Returns graph.
	 */
	public Graph getGraph() {
		return g;
	}
	
	/**
	 * Returns geometric bounds for the graph.
	 */
	public Point[] getGeoBounds() {
		return geoBounds;
	}
	
	/**
	 * Builds the JUNG graph from internal network structure
	 */
	private void buildGraph() {
		int i;
		double W = 1;
		double r = 0;
		double L = 10;
		
		g = new DirectedSparseGraph();
		g.getEdgeConstraints().remove(Graph.NOT_PARALLEL_EDGE);

		g.addVertex(new VertexNodeSIG(new Point(0,0,0),false));
		
		Vector<Boolean> usedphases = myNode.getSigMan().getVecProtected();
		
		boolean noNorth = !usedphases.get(7) && !usedphases.get(2) && !usedphases.get(0) && !usedphases.get(3);
		boolean noSouth = !usedphases.get(6) && !usedphases.get(3) && !usedphases.get(4) && !usedphases.get(7);
		boolean noEast  = !usedphases.get(1) && !usedphases.get(4) && !usedphases.get(2) && !usedphases.get(5);
		boolean noWest  = !usedphases.get(0) && !usedphases.get(5) && !usedphases.get(6) && !usedphases.get(1);

		Point[] bPt = new Point[8];
		Point[] dir = new Point[8];
		bPt[0] = new Point(-4*W-r,+1*W,0);		// phase 1 in
		dir[0] = new Point(1,0,0);
		bPt[1] = new Point(+4*W+r,-3*W,0);		// phase 2 in
		dir[1] = new Point(-1,0,0);
		bPt[2] = new Point(-1*W,-4*W-r,0);		// phase 3 in
		dir[2] = new Point(0,+1,0);
		bPt[3] = new Point(+3*W,+4*W+r,0);		// phase 4 in
		dir[3] = new Point(0,-1,0);
		bPt[4] = new Point(+4*W+r,-1*W,0);		// phase 5 in
		dir[4] = new Point(-1,0,0);
		bPt[5] = new Point(-4*W-r,+3*W,0);		// phase 6 in
		dir[5] = new Point(1,0,0);
		bPt[6] = new Point(+1*W,+4*W+r,0);		// phase 7 in
		dir[6] = new Point(0,-1,0);
		bPt[7] = new Point(-3*W,-4*W-r,0);		// phase 8 in
		dir[7] = new Point(0,+1,0);

		// in
		for(i=0;i<8;i++){
			if(usedphases.get(i)){
				VertexNodeSIG i1 = new VertexNodeSIG(bPt[i],true,myNode,i);
				g.addVertex(i1);
				Point ePt;
				ePt = bPt[i].minus(dir[i].times(L));
				VertexNodeSIG  i2 = new VertexNodeSIG(ePt,false);
				g.addVertex(i2);
				g.addEdge(new EdgeLinkHWC(i2, i1,true));
			}
		}
		
		// out
		for(i=0;i<8;i++){
			if( noNorth && (i==6 || i==3) )
				continue;
			if( noSouth && (i==7 || i==2) )
				continue;
			if( noEast && (i==0 || i==5) )
				continue;
			if( noWest && (i==1 || i==4) )
				continue;
			Point x1 = bPt[i].plus(dir[i].times(8*W+2*r));
			VertexNodeSIG  o1 = new VertexNodeSIG(x1,false);
			g.addVertex(o1);
			Point x2 = x1.plus(dir[i].times(L));
			VertexNodeSIG  o2 = new VertexNodeSIG(x2,false);
			g.addVertex(o2);
			g.addEdge(new EdgeLinkHWC(o2, o1,true));
		}
	
		drawPavement(!noEast ,bPt[1],dir[1],W,L);
		drawPavement(!noSouth,bPt[3],dir[3],W,L);
		drawPavement(!noWest ,bPt[5],dir[5],W,L);
		drawPavement(!noNorth,bPt[7],dir[7],W,L);

		geoBounds[0] = new Point(0,0,0);
		geoBounds[1] = new Point(0,0,0);
				
		geoBounds[0].x -= 1.0*(4*W+L);
		geoBounds[0].y += 1.0*(4*W+L);
		geoBounds[1].x += 1.0*(4*W+L);
		geoBounds[1].y -= 1.0*(4*W+L);
		
		return;
	}
	
	/**
	 * called by buildGraph
	 */
	private void drawPavement(boolean itexists,Point bPt,Point dir,double W,double L) {

		Point zdir = new Point(0,0,1);
		Point d = dir.crossprod(zdir).times(W);

		Point p1 = new Point(bPt.minus(d));
		Point p2 = new Point(p1.plus(d.times(4)));
		Point p3 = new Point(p2.plus(d.times(4)));
	
		if(itexists){
			VertexNodeSIG v1 = new VertexNodeSIG(p1,false);
			g.addVertex(v1);
			VertexNodeSIG v2 = new VertexNodeSIG(p1.minus(dir.times(L)),false);
			g.addVertex(v2);
			g.addEdge(new EdgeLinkHWC(v1,v2,false));
			
			VertexNodeSIG v3 = new VertexNodeSIG(p2,false);
			g.addVertex(v3);
			VertexNodeSIG v4 = new VertexNodeSIG(p2.minus(dir.times(L)),false);
			g.addVertex(v4);
			g.addEdge(new EdgeLinkHWC(v3,v4,false));
	
			VertexNodeSIG v5 = new VertexNodeSIG(p3,false);
			g.addVertex(v5);
			VertexNodeSIG v6 = new VertexNodeSIG(p3.minus(dir.times(L)),false);
			g.addVertex(v6);
			g.addEdge(new EdgeLinkHWC(v5,v6,false));
		}
		else{
			VertexNodeSIG v1 = new VertexNodeSIG(p1,false);
			g.addVertex(v1);
			VertexNodeSIG v5 = new VertexNodeSIG(p3,false);
			g.addVertex(v5);
			g.addEdge(new EdgeLinkHWC(v1,v5,false));
		}
			
	}
	
	/**
	 * Class needed to paint vertices.
	 */
	private final class VertexPaintFunctionSIG implements VertexPaintFunction {
		public VertexPaintFunctionSIG() { }
		public Paint getDrawPaint(Vertex v) {
			return getFillPaint(v);
		}
		public Paint getFillPaint(Vertex v) {
			VertexNodeSIG z = (VertexNodeSIG) v;
			if(z.getDisplay()){
				BulbColor x = z.getSignalColor();
				switch(x){
				case GREEN:
					return Color.GREEN;
				case YELLOW:
					return Color.YELLOW;
				case RED:
					return Color.RED;
				default:
					return Color.BLACK;
				}
			}
			else
				return null;
		}
	}

	/**
	 * Class needed to set vertex size.
	 */
	private final class VertexShapeFunctionSIG implements VertexShapeFunction {
		public VertexShapeFunctionSIG(){};
		public Shape getShape(Vertex v) {
			VertexShapeFactory x = new VertexShapeFactory();
			return x.getRectangle(v);
		}

	}
	
	
	/**
	 * Class needed for defining the thickness of edges.
	 */
	private final static class EdgeStrokeFunctionSIG implements EdgeStrokeFunction {
		public EdgeStrokeFunctionSIG() { }
		public Stroke getStroke(Edge e) {
			Stroke s;
			if(((EdgeLinkHWC) e).getIsThick())
				s = new BasicStroke(13.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND);
			else 
				s = new BasicStroke(5.0f);
			return s;
		}
    }
	
	
	/**
	 * Class needed for errasing edge arrows.
	 */
	private final static class EdgeArrowFunctionSIG implements EdgeArrowFunction {
		public EdgeArrowFunctionSIG() { }
		public Shape getArrow(Edge e) {
			return ArrowFactory.getWedgeArrow(0.0f,0.0f); 
		}
    }
	
	
	/**
	 * Class needed to paint the edge.
	 */
	private final class GradientEdgePaintFunctionHWC extends GradientEdgePaintFunction {
		public GradientEdgePaintFunctionHWC(EdgePaintFunction defaultEdgePaintFunction, HasGraphLayout vv, LayoutTransformer transformer) {
			super(Color.WHITE, Color.BLACK, vv, transformer);
		}
		protected Color getColor1(Edge e) {
			return getColor2(e);
		}
		protected Color getColor2(Edge e) {
			if(((EdgeLinkHWC) e).getIsThick())
				return Color.GRAY;
			else 
				return Color.BLACK;
		}
	}
	
	/**
	 * Class needed for displaying table of parameters.
	 */
	
	private class paramTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -8955728768309531170L;

		public int getColumnCount() { return 9; }
		public int getRowCount() { return 6; }
		
		public String getColumnName(int col) {
	        String buf = null;
	        switch(col) {
	        case 0:	buf = ""; break;
	        case 1: buf = "1"; break;
	        case 2: buf = "2"; break;
	        case 3: buf = "3"; break;
	        case 4: buf = "4"; break;
	        case 5: buf = "5"; break;
	        case 6: buf = "6"; break;
	        case 7: buf = "7"; break;
	        case 8: buf = "8"; break;
	        }
			return buf;
	    }
		
		public Object getValueAt(int row, int column) {
			String buf = null;
			SignalManager s = myNode.getSigMan();
			
			if (column == 0)
				switch(row){
				case 0: buf = "protected"; break;
				case 1: buf = "permissive"; break;
				case 2: buf = "recall"; break;
				case 3: buf = "mingreen"; break;
				case 4: buf = "yellowtime"; break;
				case 5: buf = "redcleartime"; break;
				}
			else{
				switch(row){
				case 0: 
					if( s.getVecProtected().get(column-1) )
						buf = "1";
					else
						buf = "0";
					break;
				case 1: 
					if( s.getVecPermissive().get(column-1) )
						buf = "1";
					else
						buf = "0";
					break;
				case 2:
					if( s.getVecRecall().get(column-1) )
						buf = "1";
					else
						buf = "0";
					break;
				case 3: 
					buf = s.getVecMingreen().get(column-1).toString(); 
					break;
				case 4: 
					buf = s.getVecYellowTime().get(column-1).toString(); 
					break;
				case 5: 
					buf = s.getVecRedClearTime().get(column-1).toString(); 
					break;
				}
			}
			return  buf;
		}
		
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
	}

	/**
	 * Class needed for displaying table of sensors.
	 */
	private class sensorTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -3425534706225726636L;

		public int getColumnCount() { return 9; }
		public int getRowCount() { return 2; }
		
		public String getColumnName(int col) {
	        String buf = null;
	        switch(col) {
	        case 0:	buf = ""; break;
	        case 1: buf = "1"; break;
	        case 2: buf = "2"; break;
	        case 3: buf = "3"; break;
	        case 4: buf = "4"; break;
	        case 5: buf = "5"; break;
	        case 6: buf = "6"; break;
	        case 7: buf = "7"; break;
	        case 8: buf = "8"; break;
	        }
			return buf;
	    }
		
		public Object getValueAt(int row, int column) {
			String buf = null;
			SignalManager s = myNode.getSigMan();
			
			if (column == 0)
				switch(row){
				case 0: buf = "Stopline loops"; break;
				case 1: buf = "Approach loops"; break;
				}
			else{
				switch(row){
				case 0: 
					if( s.Phase(column-1).StoplineStation()!=null )
						buf = s.Phase(column-1).StoplineStation().LoopIds().toString();
					else
						buf = "-";
					break;
				case 1: 
					if( s.Phase(column-1).ApproachStation()!=null )
						buf = s.Phase(column-1).ApproachStation().LoopIds().toString();
					else
						buf = "-";
					break;
				}
			}
			return  buf;
		}
		
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}
	
	/**
	 * Class needed for displaying table of phase indications.
	 */
	private class phaseindTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -411786610657260260L;

		public int getColumnCount() { return 9; }
		public int getRowCount() { return 2; }
		
		public String getColumnName(int col) {
	        String buf = null;
	        switch(col) {
	        case 0:	buf = ""; break;
	        case 1: buf = "1"; break;
	        case 2: buf = "2"; break;
	        case 3: buf = "3"; break;
	        case 4: buf = "4"; break;
	        case 5: buf = "5"; break;
	        case 6: buf = "6"; break;
	        case 7: buf = "7"; break;
	        case 8: buf = "8"; break;
	        }
			return buf;
	    }
		
		public Object getValueAt(int row, int column) {
			String buf = null;
			SignalManager s = myNode.getSigMan();
			
			if (column == 0)
				switch(row){
				case 0: buf = "Color"; break;
				case 1: buf = "Timer"; break;
				}
			else{
				if(s.Phase(column-1).Protected()){
					switch(row){
					case 0: 
						switch(s.Phase(column-1).BulbColor()){
						case GREEN:
							buf = "G";
							break;
						case YELLOW:
							buf = "Y";
							break;
						case RED:
							buf = "R";
							break;
						}
						//buf = s.Phase(column-1).BulbColor().toString();
						break;
					case 1: 
						buf = String.format("%.1f", s.Phase(column-1).BulbTimer().GetTime());
						break;
					}
				}
				else{
					buf = "-";
				}

			}
			return  buf;
		}
		
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
	}

	/**
	 * Class needed for displaying table of detector stations.
	 */
	private class detcallTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1830479282880718132L;

		public int getColumnCount() { return 9; }
		public int getRowCount() { return 4; }
		
		public String getColumnName(int col) {
	        String buf = null;
	        switch(col) {
	        case 0:	buf = ""; break;
	        case 1: buf = "1"; break;
	        case 2: buf = "2"; break;
	        case 3: buf = "3"; break;
	        case 4: buf = "4"; break;
	        case 5: buf = "5"; break;
	        case 6: buf = "6"; break;
	        case 7: buf = "7"; break;
	        case 8: buf = "8"; break;
	        }
			return buf;
	    }
		
		public Object getValueAt(int row, int column) {
			String buf = null;
			SignalManager s = myNode.getSigMan();
			
			if (column == 0)
				switch(row){
				case 0: buf = "Stopline call"; break;
				case 1: buf = "Stopline count"; break;
				case 2: buf = "Approach call"; break;
				case 3: buf = "Approach count"; break;
				}
			else{
				switch(row){
				case 0: 
					if( s.Phase(column-1).StoplineStation()!=null )
						if( s.Hasstoplinecall(column-1) )
							buf = "1";
						else
							buf = "0";
					else
						buf = "-";
					break;
				case 1:
					if( s.Phase(column-1).StoplineStation()!=null )
						buf = ((Integer) s.Phase(column-1).StoplineStation().MaxLoopCount()).toString();
					else
						buf = "-";
					break;
				case 2: 
					if( s.Phase(column-1).ApproachStation()!=null )
						if( s.Hasapproachcall(column-1) )
							buf = "1";
						else
							buf = "0";
					else
						buf = "-";
					break;
				case 3:					
					if( s.Phase(column-1).ApproachStation()!=null )
						buf = ((Integer) s.Phase(column-1).ApproachStation().MaxLoopCount()).toString();
					else
						buf = "-";
					break;
				}
			}
			return  buf;
		}
		
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
	}

	/**
	 * Class needed for displaying table of controller commands.
	 */
	private class contrlcomTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -6105262674893325963L;

		public int getColumnCount() { return 9; }
		public int getRowCount() { return 2; }
		
		public String getColumnName(int col) {
	        String buf = null;
	        switch(col) {
	        case 0:	buf = ""; break;
	        case 1: buf = "1"; break;
	        case 2: buf = "2"; break;
	        case 3: buf = "3"; break;
	        case 4: buf = "4"; break;
	        case 5: buf = "5"; break;
	        case 6: buf = "6"; break;
	        case 7: buf = "7"; break;
	        case 8: buf = "8"; break;
	        }
			return buf;
	    }
		
		public Object getValueAt(int row, int column) {
			String buf = null;
			SignalManager s = myNode.getSigMan();
			
			if (column == 0)
				switch(row){
				case 0: buf = "Hold"; break;
				case 1: buf = "Force off"; break;
				}
			else{
				
				if(s.Phase(column-1).Protected()){
					switch(row){
					case 0: 
						if( s.DisplayHold(column-1) )
							buf = "1";
						else
							buf = "0";
						break;
					case 1: 
						if( s.DisplayForceOff(column-1) )
							buf = "1";
						else
							buf = "0";
						break;
					}
				}
				else{
					buf = "-";
				}
			}
			return  buf;
		}
		
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
	}

	/**
	 * Class needed for displaying table of conflicting calls.
	 */
	private class confcallTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -6342025991170660226L;

		public int getColumnCount() { return 9; }
		public int getRowCount() { return 2; }
		
		public String getColumnName(int col) {
	        String buf = null;
	        switch(col) {
	        case 0:	buf = ""; break;
	        case 1: buf = "1"; break;
	        case 2: buf = "2"; break;
	        case 3: buf = "3"; break;
	        case 4: buf = "4"; break;
	        case 5: buf = "5"; break;
	        case 6: buf = "6"; break;
	        case 7: buf = "7"; break;
	        case 8: buf = "8"; break;
	        }
			return buf;
	    }
		
		public Object getValueAt(int row, int column) {
			String buf = null;
			SignalManager s = myNode.getSigMan();
			
			if (column == 0)
				switch(row){
				case 0: buf = "Present"; break;
				case 1: buf = "Timer"; break;
				}
			else{
				if(s.Phase(column-1).Protected()){
					switch(row){
					case 0: 
						if( s.Hasconflictingcall(column-1) )
							buf = "1";
						else
							buf = "0";
						break;
					case 1: 
						buf = ((Float) s.Conflictingcalltime(column-1)).toString();
						break;
					}
				}
				else{
					buf = "-";
				}	
			}
			return  buf;
		}
		
		public boolean isCellEditable(int row, int column) {
			return false;
		}	
	}

}

				
				
