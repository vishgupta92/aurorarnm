package aurora.hwc.config;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Vector;

import com.vividsolutions.jts.geom.Coordinate;

public class GISEdge {
	public Vector<Coordinate>  coordinates;
	public ArrayList<String> successors;
	public ArrayList<String> predecessors;
	protected String id = "";
	protected double lanes = 0.0;
	protected double length= 0.0;
	protected String type = "Unknown";
	
	private String auroraClass;

//	protected double flowMax; // in vph
//	protected double densityCritical; // in vpm
//	protected double densityJam; // in vpm
//	protected double density = 0; // in vpm
//	protected double density0 = 0; // initial value in vpm
//	protected double speed = 0; // mean speed 
//	protected double vht = 0; // vehicle hours traveled per sampling time period
//	protected double vhtSum = 0;
//	protected double vmt = 0; // vehicle miles traveled per sampling time period
//	protected double vmtSum = 0;
//	protected double delay = 0; // delay introduced when speed is below free flow speed
//	protected double delaySum = 0;
//	protected double ploss = 0; // productivity loss
//	protected double plossSum = 0;
//
//	protected double qMax = 100; // max queue size in vehicles
//	protected double qSize = 0; // actual queue size in vehicles
//
//	protected Vector<Double> demand = new Vector<Double>(); // demand profile (values in vph)
//	protected double demandTP = 1.0/12.0; // demand value change period (default: 1/12 hour)
//	protected double demandKnob = 1.0; //coefficient by which the demand value must be multiplied

	protected double[][] splitRatioMatrix;

	public GISEdge(){
		coordinates = new Vector<Coordinate>();
		successors = new ArrayList<String>();
		predecessors = new ArrayList<String>();
		auroraClass = null;
	}

	
	public void setId(String id) {
		this.id = id;
	}

	public void setType(String type) {
		this.type = type;
	}


	public void setLanes(double lanes) {
		this.lanes = lanes;
	}


	public void setLength(double length) {
		this.length = length;
	}


	public void setAuroraClass(String name) {
		this.auroraClass = name;
	}

	/**
	 * Generates XML description of the simple Node.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @return XML buffer.
	 * @throws IOException
	 */
	public String xmlDump(PrintStream out) throws IOException {
		if (this.auroraClass == null) {
			this.auroraClass = "aurora.hwc.LinkStreet";
		}
		String buf = "<link class=\"" + this.auroraClass+ "\" id=\""
				+ id + "\" length=\""
				+ Double.toString(length/5280.0) // convert length from feet to miles 
				+ "\" lanes=\""
				+ Double.toString(lanes) + "\">";
		if (predecessors.size() > 0)
			buf += "<begin id=\""
//					+ Integer.toString(predecessors.firstElement().getId())
				+ predecessors.get(0)
				+ "\"/>";
		if (successors.size() > 0)
			buf += "<end id=\""
//					+ Integer.toString(successors.firstElement().getId())
				+ successors.get(0)
				+ "\"/>";
		String dynamicsName = "aurora.hwc.DynamicsCTM";
			buf += "<dynamics class=\"" + dynamicsName+ "\"/>";
//		buf += "<density>" + Double.toString(density0) + "</density>";
//		buf += "<demand tp=\"" + Double.toString(demandTP) + "\" knob=\""
//				+ Double.toString(demandKnob) + "\">";
//		buf += getDemandVectorAsString();
//		buf += "</demand>";
//		buf += "<qmax>" + Double.toString(qMax) + "</qmax>";
//		buf += "<fd densityCritical =\"" + Double.toString(densityCritical)
//				+ "\" densityJam=\"" + Double.toString(densityJam)
//				+ "\" flowMax=\"" + Double.toString(flowMax) + "\"/>";
		if (out != null)
			out.print(buf);
//		buf += myPosition.xmlDump(out);
		String buf2 = "<position>";
		for (int i = 0; i < coordinates.size(); i++)
			buf2 += "<point x=\"" + Double.toString(coordinates.get(i).x)
					+ "\" y=\"" + Double.toString(- coordinates.get(i).y) 
					+ "\" z=\""
					+ 0.0+ "\"/>";
					// y coordinate in aurora = - (y coordinate in GIS)
//					+ Double.toString(coordinates.get(i).z) + "\"/>";
		buf2 += "</position>";
		if (out != null)
			out.print(buf2);
		buf += buf2;

		if (out != null)
			out.print("</link>");
		buf += "</link>";
		return buf;
	}

}
