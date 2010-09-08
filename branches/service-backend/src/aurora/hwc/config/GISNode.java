package aurora.hwc.config;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

public class GISNode {
	public int Counter;
	public Coordinate coordinates;
	public ArrayList<String> successors; // successor links
	public ArrayList<String> predecessors; // predecessor links
	private String description = "";
	private String id = "";
	private String name = "Unknown";
	private String auroraClass = "Unknown";
	protected double[][] splitRatioMatrix;
	protected String type = "";
	public boolean isFreeway = false;

	public GISNode(){
		coordinates = new Coordinate();
		successors = new ArrayList<String>();
		predecessors = new ArrayList<String>();
		auroraClass = null;
		isFreeway = false; 
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return(id);
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return(name);
	}

	public void setDescription(String desc) {
		this.description = desc;
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
			this.auroraClass = "aurora.hwc.NodeUJSignal";
		}
		int i;
			
		String buf = "<node class=\"" + this.auroraClass + "\" id=\""
				+ id + "\" name=\"" + name + "\">";
		buf += "<description>" + description + "</description>";
		buf += "<outputs>";
		for (i = 0; i < successors.size(); i++)
//			buf += "<output id=\"" + successors.get(i).getId() + "\"/>";
			buf += "<output id=\"" + successors.get(i) + "\"/>";
		buf += "</outputs><inputs>";
		if (out != null)
			out.print(buf);
		String buf2 = "";
		String buf3 = "";
		for (i = 0; i < predecessors.size(); i++) {
			for (int j = 0; j < successors.size(); j++) {
				if (j > 0)
					buf3 += ", ";
//				buf3 += Double.toString(splitRatioMatrix[i][j]);
//				buf3 += Double.toString(1.0/((double) successors.size()));
				buf3 += String.format("%.5f", 1.0/((double) successors.size()));
			}
//			buf2 += "<input id=\"" + predecessors.get(i).getId() + "\">";
			buf2 += "<input id=\"" + predecessors.get(i) + "\">";
			buf2 += "<splitratios>" + buf3 + "</splitratios>";
			if (out != null)
				out.print(buf2);
//			if (controllers.get(i) != null)
//				buf2 += controllers.get(i).xmlDump(out);
			if (out != null)
				out.print("</input>");
			buf += (buf2 + "</input>");
			buf2 = "";
			buf3 = "";
		}
		if (out != null)
			out.print("</inputs>");
		buf += "</inputs>";
//		buf += position.xmlDump(out);
		String buf5 = "<position>";
		buf5 += "<point x=\"" + Double.toString(coordinates.x) + "\" y=\""
				+ Double.toString(-coordinates.y) + "\" z=\""
				// y coordinate in aurora = - (y coordinate in GIS)
//				+ Double.toString(coordinates.z) + "\"/>";
		+ 0.0 + "\"/>";
		buf5 += "</position>";
		if (out != null)
			out.print(buf5);
		buf += buf5;

		if (out != null)
			out.print("</node>");
		buf += "</node>";
		return buf;
	}

	public String getDescription() {
		return description;
	}

}
