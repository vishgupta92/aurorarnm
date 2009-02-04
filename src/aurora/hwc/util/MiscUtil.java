/**
 * @(#)MiscUtil.java
 */

package aurora.hwc.util;

import java.text.NumberFormat;
import aurora.*;
import aurora.hwc.*;


/**
 * Miscellaneous routines to process Nodes and Links.
 * @author Alex Kurzhanskiy
 * @version $Id: MiscUtil.java,v 1.1.4.1.2.2 2008/12/03 02:15:42 akurzhan Exp $
 */
public final class MiscUtil {
	private static double dx = 10.0;
	private static double dt = 1.0;
	private static int nodeCount = 0;
	private static int linkCount = 0;
	private static int srcCount = 0;
	private static int dstCount = 0;
	private static double pmStart = 0.0;
	
	
	/**
	 * Calls processing routines for the HWC node.
	 * @param node Node object.
	 */
	public static void processNode(AbstractNode node) {
		if (node.isSimple())
			//computeNodes();
			;//generateDemandProfile(node);
		else
			;
		return;
	}
	
	/**
	 * Calls processing routines for the HWC link.
	 * @param link Link object.
	 */
	public static void processLink(AbstractLinkHWC link) {
		//computeLinks(link);
		//if (link.getBeginNode() == null)
			//sourceLinkDump(link);
		//if (link.getType() == TypesHWC.LINK_FREEWAY)
			//generateCell(link);
		return;
	}
	
	public static void resetValues() {
		dx = 10.0;
		dt = 1.0;
		nodeCount = 0;
		linkCount = 0;
		srcCount = 0;
		dstCount = 0;
		pmStart = 0.0;
		return;
	}
	
	
	
	
	
	/**
	 * Computes maximum sampling period allowed.
	 */
	public static void computeMaxSamplingTime(AbstractLinkHWC link) {
		if (dx < link.getLength())
			dx = link.getLength();
		double ts = link.getLength() / link.getV();
		if (ts < dt)
			dt = ts;
		System.err.println(dx + "\t" + dt + "\t" + dt * 3600);
		return;
	}
	
	/**
	 * Prints out source links.
	 */
	public static void sourceLinkDump(AbstractLinkHWC link) {
		NumberFormat form = NumberFormat.getInstance();
		form.setMinimumIntegerDigits(2);
		form.setMaximumIntegerDigits(2);
		Point p = link.getPosition().get().get(0);
		double lng = 1200000 - p.x;
		double lat = 300000 - p.y;
		AbstractNodeHWC nd = (AbstractNodeHWC)link.getEndNode();
		int d1 = (int)Math.floor(lat/10000);
		lat -= d1*10000;
		int m1 = (int)Math.floor(lat/100);
		lat -= m1*100;
		int s1 = (int)Math.floor(lat);
		int d2 = (int)Math.floor(lng/10000);
		lat -= d2*10000;
		int m2 = (int)Math.floor(lng/100);
		lat -= m2*100;
		int s2 = (int)Math.floor(lng);
		System.err.println(link.getId() + "\t" + d1 + " " + form.format(m1) + " " + form.format(s1) + "\t" + "-" + d2 + " " + form.format(m2) + " " + form.format(s2) + "\t" + nd.getDescription() + " (" + nd.getId() + ") " + nd.getName());
		//System.err.print(link.getId() + " ");
		return;
	}
	
	/**
	 * Generate cell structure for CTMSIM.
	 */
	public static void generateCell(AbstractLinkHWC link) {
		AbstractNodeHWC bn = (AbstractNodeHWC)link.getBeginNode();
		AbstractNodeHWC en = (AbstractNodeHWC)link.getEndNode();
		AbstractLinkHWC rmp;
		int nor, i;
		System.err.println("cell.PMstart = " + pmStart + ";");
		pmStart += link.getLength();
		System.err.println("cell.PMend = " + pmStart + ";");
		System.err.println("cell.lanes = " + link.getLanes() + ";");
		System.err.println("cell.FDfmax = " + link.getMaxFlow() + ";");
		System.err.println("cell.FDrhocrit = " + link.getCriticalDensity() + ";");
		System.err.println("cell.FDrhojam = " + link.getJamDensity() + ";");
		if ((bn != null) && (bn.getInputs().size() > 1)) {
			System.err.println("cell.ORname = '" + bn.getName() + "';");
			double lanes = 0.0;
			double fmax = 0.0;
			double qsize = 0.0;
			double knob = 0.0;
			nor = bn.getInputs().size();
			for (i = 1; i < nor; i++) {
				rmp = (AbstractLinkHWC)bn.getPredecessors().get(i);
				lanes += rmp.getLanes();
				fmax += rmp.getMaxFlow();
				qsize += rmp.getQueueMax();
				knob += rmp.getDemandKnob();
			}
			knob = knob / (nor-1);
			System.err.println("cell.ORlanes = " + lanes + ";");
			System.err.println("cell.ORfmax = " +  fmax + ";");
			System.err.println("cell.ORqsize = " +  qsize + ";");
			System.err.println("cell.ORgamma = 1;");
			System.err.println("cell.ORxi = 1;");
			System.err.println("cell.ORknob = " + knob + ";");
		}
		else {
			System.err.println("cell.ORname = [];");
			System.err.println("cell.ORlanes = 0;");
			System.err.println("cell.ORfmax = 0;");
			System.err.println("cell.ORqsize = 0;");
			System.err.println("cell.ORgamma = 0;");
			System.err.println("cell.ORxi = 0;");
			System.err.println("cell.ORknob = 0;");
		}
		System.err.println("cell.ORflow = 0;");
		System.err.println("cell.ORmlcontroller = [];");
		System.err.println("cell.ORqcontroller = [];");
		if ((en != null) && (en.getOutputs().size() > 1)) {
			System.err.println("cell.FRname = '" + en.getName() + "';");
			double lanes = 0.0;
			double fmax = 0.0;
			nor = en.getOutputs().size();
			for (i = 1; i < nor; i++) {
				rmp = (AbstractLinkHWC)en.getSuccessors().get(i);
				lanes += rmp.getLanes();
				fmax += rmp.getMaxFlow();
			}
			System.err.println("cell.FRlanes = " + lanes + ";");
			System.err.println("cell.FRfmax = " + fmax + ";");
			System.err.println("cell.FRbeta = " + (1-(en.getSplitRatioMatrix()[0][0]).get(0).getCenter()) + ";");
			System.err.println("cell.FRknob = 1;");
		}
		else {
			System.err.println("cell.FRname = [];");
			System.err.println("cell.FRlanes = 0;");
			System.err.println("cell.FRfmax = 0;");
			System.err.println("cell.FRbeta = 0;");
			System.err.println("cell.FRknob = 0;");
		}
		System.err.println("celldata = [celldata cell]");
		System.err.println("");
	}
	
	/**
	 * Generates demand profile for CTMSIM.
	 */
	public static void generateDemandProfile(AbstractNode node) {
		double[] dp = new double[24];
		int i;
		for (i = 0; i < 24; i++)
			dp[i] = 0.0;
		for (int j = 1; j < node.getPredecessors().size(); j++) {
			AbstractLinkHWC l = (AbstractLinkHWC)node.getPredecessors().get(j);
			for (i = 0; i < 24; i++)
				if (i < l.getDemandVector().size())
					dp[i] += l.getDemandVector().get(i).sum().getCenter();
				else
					dp[i] += l.getDemandVector().get(0).sum().getCenter();
		}
		for (i = 0; i < 24; i++)
			System.err.print(dp[i] + " ");
		System.err.println(";");
	}
	
	/**
	 * Computes nodes.
	 */
	@SuppressWarnings("unused")
	private static void computeNodes() {
		nodeCount++;
		return;
	}
	
	/**
	 * Computes nodes.
	 */
	@SuppressWarnings("unused")
	private static void computeLinks(AbstractLinkHWC link) {
		linkCount++;
		if (link.getBeginNode() == null) {
			srcCount++;
			//System.err.print(link.getId() + " ");
		}
		if (link.getEndNode() == null)
			dstCount++;
		/*System.err.println("Nodes: " + nodeCount);
		System.err.println("Links: " + linkCount);
		System.err.println("Sources: " + srcCount);
		System.err.println("Destinations: " + dstCount);
		System.err.println("");*/
		return;
	}

}