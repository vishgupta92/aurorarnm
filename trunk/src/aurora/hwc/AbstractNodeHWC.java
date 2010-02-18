/**
 * @(#)AbstractNodeHWC.java
 */

package aurora.hwc;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import aurora.*;
import aurora.util.*;


/**
 * Base class for all simple highway nodes.
 * 
 * @see NodeFreeway, NodeHighway, NodeUJSignal, NodeUJStop
 * 
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public abstract class AbstractNodeHWC extends AbstractNodeSimple {
	private static final long serialVersionUID = 3609519064761135698L;
	
	protected double[][] weavingFactorMatrix = null;
	protected AuroraIntervalVector[][] splitRatioMatrix = null;
	protected AuroraIntervalVector[][] splitRatioMatrix0 = null;
	protected Vector<AuroraIntervalVector[][]> srmProfile = new Vector<AuroraIntervalVector[][]>();
	protected double srTP = 1.0/12.0; // split ratio matrix change period (default: 1/12 hour)
	
	
	
	/**
	 * Initializes the simple Node from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if ((p == null) || (myNetwork == null))
			return !res;
		if (initialized) {
			try  {
				if (p.hasChildNodes()) {
					NodeList pp = p.getChildNodes();
					Vector<String> srbuf = new Vector<String>();
					Vector<String> wfbuf = new Vector<String>();
					int i, j;
					int m = 0;
					int n = 0;
					for (i = 0; i < pp.getLength(); i++) {
						if (pp.item(i).getNodeName().equals("inputs"))
							if (pp.item(i).hasChildNodes()) {
								NodeList pp2 = pp.item(i).getChildNodes();
								for (j = 0; j < pp2.getLength(); j++)
									if (pp2.item(j).getNodeName().equals("input")) {
										AbstractControllerSimple ctrl = null;
										AbstractLink lk = myNetwork.getLinkById(Integer.parseInt(pp2.item(j).getAttributes().getNamedItem("id").getNodeValue()));
										if (pp2.item(j).hasChildNodes()) {
											NodeList pp3 = pp2.item(j).getChildNodes();
											for (int k = 0; k < pp3.getLength(); k++) {
												if (pp3.item(k).getNodeName().equals("splitratios"))
													srbuf.add(pp3.item(k).getTextContent());
												if (pp3.item(k).getNodeName().equals("weavingfactors"))
													wfbuf.add(pp3.item(k).getTextContent());
												if (pp3.item(k).getNodeName().equals("controller")) {
													Node type_attr = pp3.item(k).getAttributes().getNamedItem("type");
													String class_name = null;
													if (type_attr != null)
														class_name = myNetwork.getContainer().ctrType2Classname(type_attr.getNodeValue());
													else
														class_name = pp3.item(k).getAttributes().getNamedItem("class").getNodeValue();
													Class c = Class.forName(class_name);
													ctrl = (AbstractControllerSimple)c.newInstance();
													ctrl.setMyLink(lk);
													res &= ctrl.initFromDOM(pp3.item(k));
												}
											}
										}
										addInLink(lk, ctrl);
										m++;
									}
							}
						if (pp.item(i).getNodeName().equals("outputs"))
							if (pp.item(i).hasChildNodes()) {
								NodeList pp2 = pp.item(i).getChildNodes();
								for (j = 0; j < pp2.getLength(); j++)
									if (pp2.item(j).getNodeName().equals("output")) {
										addOutLink(myNetwork.getLinkById(Integer.parseInt(pp2.item(j).getAttributes().getNamedItem("id").getNodeValue())));
										n++;
									}
							}
						if (pp.item(i).getNodeName().equals("splitratios"))
							initSplitRatioProfileFromDOM(pp.item(i));
					}
					if ((wfbuf.size() > 0) && (n > 0)) {
						weavingFactorMatrix = new double[wfbuf.size()][n];
						for (i = 0; i < wfbuf.size(); i++) {
							StringTokenizer st = new StringTokenizer(wfbuf.get(i), ", \t");
							j = 0;
							while ((st.hasMoreTokens()) && (j < n)) {
								try {
									weavingFactorMatrix[i][j] = Double.parseDouble(st.nextToken());
								}
								catch(Exception e) {
									weavingFactorMatrix[i][j] = 1;
								}
								j++;
							}
						}
					}
					else {
						weavingFactorMatrix = new double[m][n];
						for (i = 0; i < m; i++)
							for (j = 0; j < n; j++)
								weavingFactorMatrix[i][j] = 1;
					}
					if ((srbuf.size() > 0) && (n > 0)) {
						splitRatioMatrix = new AuroraIntervalVector[srbuf.size()][n];
						splitRatioMatrix0 = new AuroraIntervalVector[srbuf.size()][n];
						int sz = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();
						for (i = 0; i < srbuf.size(); i++) {
							StringTokenizer st = new StringTokenizer(srbuf.get(i), ", \t");
							j = 0;
							while ((st.hasMoreTokens()) && (j < n)) {
								String srvtxt = st.nextToken();
								AuroraIntervalVector srv = new AuroraIntervalVector();
								srv.setRawIntervalVectorFromString(srvtxt);
								int rsz = srv.size();
								if (myNetwork.getContainer().isSimulation()) {
									srv = new AuroraIntervalVector(sz);
									srv.setIntervalVectorFromString(srvtxt);
									for (int idx = rsz; idx < sz; idx++)
										srv.get(idx).copy(srv.get(rsz-1));
								}
								splitRatioMatrix[i][j] = srv;
								splitRatioMatrix0[i][j] = new AuroraIntervalVector();
								splitRatioMatrix0[i][j].copy(srv);
								j++;
							}
							while (j < n) {
								if (myNetwork.getContainer().isSimulation()) {
									splitRatioMatrix[i][j] = new AuroraIntervalVector(sz);
									splitRatioMatrix0[i][j] = new AuroraIntervalVector(sz);
								}
								else {
									splitRatioMatrix[i][j] = new AuroraIntervalVector();
									splitRatioMatrix0[i][j] = new AuroraIntervalVector();
								}
								j++;
							}
						}
					}
				}
				else
					res = false;
			}
			catch(Exception e) {
				res = false;
				throw new ExceptionConfiguration(e.getMessage());
			}
			return res;
		}
		try  {
			id = Integer.parseInt(p.getAttributes().getNamedItem("id").getNodeValue());
			name = p.getAttributes().getNamedItem("name").getNodeValue();
			if (p.hasChildNodes()) {
				NodeList pp = p.getChildNodes();
				for (int i = 0; i < pp.getLength(); i++) {
					if (pp.item(i).getNodeName().equals("description")) {
						description = pp.item(i).getTextContent();
						if (description.equals("null"))
							description = null;
					}
					if (pp.item(i).getNodeName().equals("position")) {
						position = new PositionNode();
						res &= position.initFromDOM(pp.item(i));
					}
				}
			}
			else
				res = false;
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		initialized = true;
		return res;
	}
	
	/**
	 * Initializes the split ratio profile from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initSplitRatioProfileFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		srmProfile.clear();
		int m = getPredecessors().size();
		int n = getSuccessors().size();
		if ((p == null) || (!p.hasChildNodes()) || (m <= 0) || (n <= 0))
			return !res;
		try {
			srTP = Double.parseDouble(p.getAttributes().getNamedItem("tp").getNodeValue());
			if (srTP > 24) // sampling period in seconds
				srTP = srTP/3600;
			NodeList pp = p.getChildNodes();
			for (int ii = 0; ii < pp.getLength(); ii++)
				if (pp.item(ii).getNodeName().equals("srm")) {
					AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
					String bufM = pp.item(ii).getTextContent();
					StringTokenizer st1 = new StringTokenizer(bufM, ";");
					int sz = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();
					int i = -1;
					while ((st1.hasMoreTokens()) && (++i < m)) {
						String bufR = st1.nextToken();
						StringTokenizer st2 = new StringTokenizer(bufR, ", ");
						int j = -1;
						while ((st2.hasMoreTokens()) && (++j < n)) {
							String srvtxt = st2.nextToken();
							AuroraIntervalVector srv = new AuroraIntervalVector();
							srv.setRawIntervalVectorFromString(srvtxt);
							int rsz = srv.size();
							if (myNetwork.getContainer().isSimulation()) {
								srv = new AuroraIntervalVector(sz);
								srv.setIntervalVectorFromString(srvtxt);
								for (int idx = rsz; idx < sz; idx++)
									srv.get(idx).copy(srv.get(rsz-1));
							}
							srm[i][j] = srv;
						}
						while (++j < n) {
							if (myNetwork.getContainer().isSimulation())
								srm[i][j] = new AuroraIntervalVector(sz);
							else
								srm[i][j] = new AuroraIntervalVector();
						}
					}
					int lastIndex = i;
					while (++i < m) {
						for (int j = 0; j < n; j++) {
							AuroraIntervalVector srv = new AuroraIntervalVector();
							srv.copy(srm[lastIndex][j]);
							srm[i][j] = srv;
						}
							
					}
					srmProfile.add(srm);
				}
		}
		catch(Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}
		return res;
	}
	
	/**
	 * Generates XML description of the simple Node.<br>
	 * If the print stream is specified, then XML buffer is written to the stream.
	 * @param out print stream.
	 * @throws IOException
	 */
	public void xmlDump(PrintStream out) throws IOException {
		int i;
		if (out == null)
			out = System.out;
		/*/ FIXME
		if (srmProfile.size() > 1) {
			for (i = 0; i < 48; i++)
				srmProfile.remove(0);
			//System.err.println(srmProfile.size());
		}//*/
		out.print("<node type=\"" + getTypeLetterCode() + "\" id=\"" + id + "\" name=\"" + name + "\">");
		out.print("<description>" + description + "</description>\n");
		out.print("<outputs>");
		for (i = 0; i < successors.size(); i++)
			out.print("<output id=\"" + successors.get(i).getId() + "\"/>");
		out.print("</outputs>\n<inputs>");
		for (i = 0; i < predecessors.size(); i++) {
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
		out.print("</node>\n");
		return;
	}
	
	/**
	 * Updates Node data.<br>
	 * Invokes controllers if there are any,
	 * computes input and output flows based on densities in the incoming
	 * and outgoing links and split ratio matrix.
	 * @param ts time step.
	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	public synchronized boolean dataUpdate(int ts) throws ExceptionDatabase, ExceptionSimulation {
		return dataUpdate3(ts);
	}
	
	/**
	 * Implementation of raw greedy policy.
	 * @param ts time step.
	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	@SuppressWarnings("unused")
	private synchronized boolean dataUpdate0(int ts) throws ExceptionDatabase, ExceptionSimulation {
		boolean res = super.dataUpdate(ts);
		if (!res)
			return res;
		//
		// Set current split ratio matrix
		//
		int idx = Math.max(0, (int)Math.floor(myNetwork.getSimTime()/srTP));
		if (splitRatioMatrix0 != null)
			setSplitRatioMatrix(splitRatioMatrix0);
		else if (!srmProfile.isEmpty())
			setSplitRatioMatrix(srmProfile.get(Math.min(idx, srmProfile.size()-1)));
		int nIn = predecessors.size(); // number of inputs
		int nOut = successors.size(); // number of outputs
		int nTypes = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();  // number of vehicle types
		//
		// Initialize input demands
		//
		AuroraIntervalVector[] inDemand = new AuroraIntervalVector[nIn];
		for (int i = 0; i < nIn; i++) {
			inDemand[i] = ((AbstractLinkHWC)predecessors.get(i)).getFlow();
			if ((myNetwork.isControlled()) && (controllers.get(i) != null)) {
				AuroraInterval sumDemand = inDemand[i].sum();
				double controllerRate = (Double)controllers.get(i).computeInput(this);
				if (controllerRate < sumDemand.getUpperBound()) { // adjust inputs according to controller rates
					double c = controllerRate / sumDemand.getUpperBound();
					for (int ii = 0; ii < nTypes; ii++)
						inDemand[i].get(ii).setUpperBound(c * inDemand[i].get(ii).getUpperBound());
				}
			}
		}
		//
		// Initialize output capacities
		//
		AuroraInterval[] outCapacity = new AuroraInterval[nOut];
		for (int j = 0; j < nOut; j++)
			outCapacity[j] = ((AbstractLinkHWC)successors.get(j)).getCapacity();
		//
		// Initialize split ratio matrix taking into account multiple vehicle types
		//
		// 1. Fill in the values
		double[][] srm = new double[nIn * nTypes][nOut];
		for (int j = 0; j < nOut; j++)
			for (int i = 0; i < nIn; i++)
				for (int ii = 0; ii < nTypes; ii++)
					srm[i + ii*nIn][j] = splitRatioMatrix[i][j].get(ii).getCenter();
		// 2. Make sure split ratio matrix is valid
		Util.normalizeMatrix(srm);
		// 3. Record outputs with undefined split ratios
		Vector<Integer> badColumns = new Vector<Integer>();
		for (int j = 0; j < nOut; j++) {
			boolean badColumn = false;
			for (int i = 0; i < nIn*nTypes; i++)
				if (srm[i][j] < 0)
					if (Util.countNegativeElements(srm, i) > 1)
						badColumn = true;
					else
						srm[i][j] = Math.max(0, 1 - Util.rowSumPositive(srm, i));
			if (badColumn) // save indices of the outputs that have undefined split ratios
				badColumns.add(j);	
		}
		// 4. Collect info about available share for undefined split ratios
		//    and the number of undefined split ratios for each input
		double[][] inputsSRInfo = new double[nIn*nTypes][2];
		for (int i = 0; i < nIn*nTypes; i++) {
			inputsSRInfo[i][0] = 1;  // remaining share for a split ratio
			inputsSRInfo[i][1] = Util.countNegativeElements(srm, i);  // number of undefined split ratios
		}
		//
		// Reduce input demand according to capacities on the outputs for which all split ratios are known
		//
		for (int j = 0; j < nOut; j++) {
			if (badColumns.indexOf((Integer)j) > -1)
				continue;
			AuroraInterval sumIns = new AuroraInterval();
			Vector<Integer> contributors = new Vector<Integer>();
			// compute total input demand assigned to output 'j'
			for (int i = 0; i < nIn; i++) {
				boolean isContributor = false;
				for (int ii = 0; ii < nTypes; ii++) {
					AuroraInterval val = new AuroraInterval();
					val.copy(inDemand[i].get(ii));
					val.affineTransform(srm[i + ii*nIn][j], 0);
					sumIns.add(val);
					if (val.getUpperBound() > 0.1)
						isContributor = true;
					inputsSRInfo[i + ii*nIn][0] -= srm[i + ii*nIn][j];
				} // vehicle types 'for' loop
				if (isContributor)
					contributors.add((Integer)i);
			} // row 'for' loop
			// adjust inputs to capacity
			double lbc = 1;
			double ubc = 1;
			if (outCapacity[j].getLowerBound() < sumIns.getLowerBound())
				lbc = outCapacity[j].getLowerBound() / sumIns.getLowerBound();
			if (outCapacity[j].getUpperBound() < sumIns.getUpperBound())
				ubc = outCapacity[j].getUpperBound() / sumIns.getUpperBound();
			if ((lbc < 1) || (ubc < 1)) {
				for (int i = 0; i < contributors.size(); i++) {
					for (int ii = 0; ii < nTypes; ii++) {
						if ((inDemand[contributors.get(i)].get(ii).getSize() == 0) && (lbc == ubc))
							inDemand[contributors.get(i)].get(ii).affineTransform(lbc, 0);
						else {
							inDemand[contributors.get(i)].get(ii).affineTransformLB(lbc, 0);
							inDemand[contributors.get(i)].get(ii).affineTransformUB(ubc, 0);
						}
					} // vehicle types 'for' loop
				} // contributors 'for' loop
			} // 'if'
		} // column 'for' loop
		//
		// Process outputs with undefined split ratios
		//
		// 1. Adjust inputs according to available capacities for known split ratios
		double[] remainingCap = new double[badColumns.size()];
		for (int j = 0; j < badColumns.size(); j++) {
			AuroraInterval sumIns = new AuroraInterval();
			Vector<Integer> contributors = new Vector<Integer>();
			for (int i = 0; i < nIn; i++) {
				boolean isContributor = false;
				for (int ii = 0; ii < nTypes; ii++) {
					AuroraInterval val = new AuroraInterval();
					val.copy(inDemand[i].get(ii));
					double sr = srm[i + ii*nIn][badColumns.get(j)];
					if (sr >= 0) {
						val.affineTransform(sr, 0);
						sumIns.add(val);
						if (val.getUpperBound() > 0.1)
							isContributor = true;
					}
				} // vehicle types 'for' loop
				if (isContributor)
					contributors.add((Integer)i);
			} // row 'for' loop
			double cc = 1;
			if (outCapacity[badColumns.get(j)].getCenter() < sumIns.getCenter())
				cc = outCapacity[badColumns.get(j)].getCenter() / sumIns.getCenter();
			if (cc < 1) {
				for (int i = 0; i < contributors.size(); i++)
					for (int ii = 0; ii < nTypes; ii++)
						inDemand[contributors.get(i)].get(ii).affineTransform(cc, 0);
				remainingCap[j] = 0.0;
			}
			else
				remainingCap[j] = outCapacity[badColumns.get(j)].getCenter() - sumIns.getCenter();
		}
		// 2a. Fill in available capacities respecting the specified split ratio bounds
		for (int j = 0; j < badColumns.size(); j++) {
			for (int i = 0; i < nIn; i++)
				for (int ii = 0; ii < nTypes; ii++)
					if (srm[i + ii*nIn][badColumns.get(j)] < 0) {
						double demand = inDemand[i].get(ii).getCenter();
						if (demand <= 0) {
							srm[i + ii*nIn][badColumns.get(j)] = inputsSRInfo[i + ii*nIn][0];
							inputsSRInfo[i + ii*nIn][0] = 0;
							continue;
						}	
						double sr = Math.max(0, Math.min(Math.min(remainingCap[j] / demand, inputsSRInfo[i + ii*nIn][0]), Math.abs(srm[i + ii*nIn][badColumns.get(j)])));
						//sr = Math.max(0, Math.min(remainingCap[j] / demand, inputsSRInfo[i + ii*nIn][0]));
						inputsSRInfo[i + ii*nIn][0] -= sr;
						inputsSRInfo[i + ii*nIn][0] = Math.max(inputsSRInfo[i + ii*nIn][0], 0);
						remainingCap[j] -= sr * demand;
						remainingCap[j] = Math.max(remainingCap[j], 0);
						srm[i + ii*nIn][badColumns.get(j)] = sr;
					}
		}
		// 2b. Fill in the rest of available capacities
		for (int j = 0; j < badColumns.size(); j++) {
			for (int i = 0; i < nIn; i++)
				for (int ii = 0; ii < nTypes; ii++)
					if ((splitRatioMatrix[i][badColumns.get(j)].get(ii).getCenter() < 0) &&
							(inputsSRInfo[i + ii*nIn][1] > 0)) {
						double demand = inDemand[i].get(ii).getCenter();
						if (demand <= 0) {
							srm[i + ii*nIn][badColumns.get(j)] += inputsSRInfo[i + ii*nIn][0];
							inputsSRInfo[i + ii*nIn][0] = 0;
							continue;
						}
						double sr = Math.max(0, Math.min(remainingCap[j] / demand, inputsSRInfo[i + ii*nIn][0]));
						inputsSRInfo[i + ii*nIn][0] -= sr;
						inputsSRInfo[i + ii*nIn][0] = Math.max(inputsSRInfo[i + ii*nIn][0], 0);
						remainingCap[j] -= sr * demand;
						remainingCap[j] = Math.max(remainingCap[j], 0);
						srm[i + ii*nIn][badColumns.get(j)] += sr;
					}
		}
		// 3. Assign the remaining split ratio shares
		for (int j = 0; j < badColumns.size(); j++) {
			AuroraInterval sumIns = new AuroraInterval();
			Vector<Integer> contributors = new Vector<Integer>();
			for (int i = 0; i < nIn; i++) {
				boolean isContributor = false;
				for (int ii = 0; ii < nTypes; ii++) {
					if ((splitRatioMatrix[i][badColumns.get(j)].get(ii).getCenter() < 0) &&
						(inputsSRInfo[i + ii*nIn][1] > 0))
						srm[i + ii*nIn][badColumns.get(j)] += inputsSRInfo[i + ii*nIn][0]/inputsSRInfo[i + ii*nIn][1];
					AuroraInterval val = new AuroraInterval();
					val.copy(inDemand[i].get(ii));
					double sr = srm[i + ii*nIn][badColumns.get(j)];
					if (sr >= 0) {
						val.affineTransform(sr, 0);
						sumIns.add(val);
						if (val.getUpperBound() > 0.1)
							isContributor = true;
					}
				} // vehicle types 'for' loop
				if (isContributor)
					contributors.add((Integer)i);
			} // row 'for' loop
			double cc = 1;
			if (outCapacity[badColumns.get(j)].getCenter() < sumIns.getCenter())
				cc = outCapacity[badColumns.get(j)].getCenter() / sumIns.getCenter();
			if (cc < 1)
				for (int i = 0; i < contributors.size(); i++)
					for (int ii = 0; ii < nTypes; ii++)
						inDemand[contributors.get(i)].get(ii).affineTransform(cc, 0);
		}
		//
		// Assign split ratios
		//
		for (int j = 0; j < nOut; j++)
			for (int i = 0; i < nIn; i++)
				for (int ii = 0; ii < nTypes; ii++) {
					splitRatioMatrix[i][j].get(ii).setCenter(srm[i + ii*nIn][j], 0);
				}
		//
		// Assign input flows
		//
		for (int i = 0; i < nIn; i++) {
			inputs.set(i, inDemand[i]);
			AbstractControllerSimple ctrl = controllers.get(i);
			if (ctrl != null)
				ctrl.setActualInput(new Double(inDemand[i].sum().getUpperBound()));
		}
		//
		// Assign output flows
		//
		for (int j = 0; j < nOut; j++) {
			AuroraIntervalVector outFlow = new AuroraIntervalVector(nTypes);
			for (int i = 0; i < nIn; i++) {
				double[] splitRatios = new double[nTypes];
				for (int ii = 0; ii < nTypes; ii++)
					splitRatios[ii] = srm[i + ii*nIn][j];
				AuroraIntervalVector flw = new AuroraIntervalVector(nTypes);
				flw.copy(inDemand[i]);
				flw.affineTransform(splitRatios, 0);
				outFlow.add(flw);
			}
			outputs.set(j, outFlow);
		}
		return res;
	}
	
	/**
	 * Implementation greedy policy with proportional distribution of excess demand.
	 * @param ts time step.
	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	@SuppressWarnings("unused")
	private synchronized boolean dataUpdate1(int ts) throws ExceptionDatabase, ExceptionSimulation {
		boolean res = super.dataUpdate(ts);
		if (!res)
			return res;
		//
		// Set current split ratio matrix
		//
		int idx = Math.max(0, (int)Math.floor(myNetwork.getSimTime()/srTP));
		if (splitRatioMatrix0 != null)
			setSplitRatioMatrix(splitRatioMatrix0);
		else if (!srmProfile.isEmpty())
			setSplitRatioMatrix(srmProfile.get(Math.min(idx, srmProfile.size()-1)));
		int nIn = predecessors.size(); // number of inputs
		int nOut = successors.size(); // number of outputs
		int nTypes = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();  // number of vehicle types
		//
		// Initialize input demands
		//
		AuroraIntervalVector[] inDemand = new AuroraIntervalVector[nIn];
		for (int i = 0; i < nIn; i++) {
			inDemand[i] = ((AbstractLinkHWC)predecessors.get(i)).getFlow();
			if ((myNetwork.isControlled()) && (controllers.get(i) != null)) {
				AuroraInterval sumDemand = inDemand[i].sum();
				double controllerRate = (Double)controllers.get(i).computeInput(this);
				if (controllerRate < sumDemand.getUpperBound()) { // adjust inputs according to controller rates
					double c = controllerRate / sumDemand.getUpperBound();
					for (int ii = 0; ii < nTypes; ii++)
						inDemand[i].get(ii).setUpperBound(c * inDemand[i].get(ii).getUpperBound());
				}
			}
		}
		//
		// Initialize output capacities
		//
		AuroraInterval[] outCapacity = new AuroraInterval[nOut];
		for (int j = 0; j < nOut; j++)
			outCapacity[j] = ((AbstractLinkHWC)successors.get(j)).getCapacity();
		//
		// Initialize split ratio matrix taking into account multiple vehicle types
		//
		// 1. Fill in the values
		double[][] srm = new double[nIn * nTypes][nOut];
		for (int j = 0; j < nOut; j++)
			for (int i = 0; i < nIn; i++)
				for (int ii = 0; ii < nTypes; ii++)
					srm[i + ii*nIn][j] = splitRatioMatrix[i][j].get(ii).getCenter();
		// 2. Make sure split ratio matrix is valid
		Util.normalizeMatrix(srm);
		// 3. Record outputs with undefined split ratios
		Vector<Integer> badColumns = new Vector<Integer>();
		for (int j = 0; j < nOut; j++) {
			boolean badColumn = false;
			for (int i = 0; i < nIn*nTypes; i++)
				if (srm[i][j] < 0)
					if (Util.countNegativeElements(srm, i) > 1)
						badColumn = true;
					else
						srm[i][j] = Math.max(0, 1 - Util.rowSumPositive(srm, i));
			if (badColumn) // save indices of the outputs that have undefined split ratios
				badColumns.add(j);	
		}
		// 4. Collect info about available share for undefined split ratios
		//    and the number of undefined split ratios for each input
		double[][] inputsSRInfo = new double[nIn*nTypes][2];
		for (int i = 0; i < nIn*nTypes; i++) {
			inputsSRInfo[i][0] = 1;  // remaining share for a split ratio
			inputsSRInfo[i][1] = Util.countNegativeElements(srm, i);  // number of undefined split ratios
		}
		//
		// Reduce input demand according to capacities on the outputs for which all split ratios are known
		//
		for (int j = 0; j < nOut; j++) {
			if (badColumns.indexOf((Integer)j) > -1)
				continue;
			AuroraInterval sumIns = new AuroraInterval();
			Vector<Integer> contributors = new Vector<Integer>();
			// compute total input demand assigned to output 'j'
			for (int i = 0; i < nIn; i++) {
				boolean isContributor = false;
				for (int ii = 0; ii < nTypes; ii++) {
					AuroraInterval val = new AuroraInterval();
					val.copy(inDemand[i].get(ii));
					val.affineTransform(srm[i + ii*nIn][j], 0);
					sumIns.add(val);
					if (val.getUpperBound() > 0.1)
						isContributor = true;
					inputsSRInfo[i + ii*nIn][0] -= srm[i + ii*nIn][j];
				} // vehicle types 'for' loop
				if (isContributor)
					contributors.add((Integer)i);
			} // row 'for' loop
			// adjust inputs to capacity
			double lbc = 1;
			double ubc = 1;
			if (outCapacity[j].getLowerBound() < sumIns.getLowerBound())
				lbc = outCapacity[j].getLowerBound() / sumIns.getLowerBound();
			if (outCapacity[j].getUpperBound() < sumIns.getUpperBound())
				ubc = outCapacity[j].getUpperBound() / sumIns.getUpperBound();
			if ((lbc < 1) || (ubc < 1)) {
				for (int i = 0; i < contributors.size(); i++) {
					for (int ii = 0; ii < nTypes; ii++) {
						if ((inDemand[contributors.get(i)].get(ii).getSize() == 0) && (lbc == ubc))
							inDemand[contributors.get(i)].get(ii).affineTransform(lbc, 0);
						else {
							inDemand[contributors.get(i)].get(ii).affineTransformLB(lbc, 0);
							inDemand[contributors.get(i)].get(ii).affineTransformUB(ubc, 0);
						}
					} // vehicle types 'for' loop
				} // contributors 'for' loop
			} // 'if'
		} // column 'for' loop
		//
		// Process outputs with undefined split ratios
		//
		// 1. Adjust inputs according to available capacities for known split ratios
		double[] remainingCap = new double[badColumns.size()];
		for (int j = 0; j < badColumns.size(); j++) {
			AuroraInterval sumIns = new AuroraInterval();
			Vector<Integer> contributors = new Vector<Integer>();
			for (int i = 0; i < nIn; i++) {
				boolean isContributor = false;
				for (int ii = 0; ii < nTypes; ii++) {
					AuroraInterval val = new AuroraInterval();
					val.copy(inDemand[i].get(ii));
					double sr = srm[i + ii*nIn][badColumns.get(j)];
					if (sr >= 0) {
						val.affineTransform(sr, 0);
						sumIns.add(val);
						if (val.getUpperBound() > 0.1)
							isContributor = true;
					}
				} // vehicle types 'for' loop
				if (isContributor)
					contributors.add((Integer)i);
			} // row 'for' loop
			double cc = 1;
			if (outCapacity[badColumns.get(j)].getCenter() < sumIns.getCenter())
				cc = outCapacity[badColumns.get(j)].getCenter() / sumIns.getCenter();
			if (cc < 1) {
				for (int i = 0; i < contributors.size(); i++)
					for (int ii = 0; ii < nTypes; ii++)
						inDemand[contributors.get(i)].get(ii).affineTransform(cc, 0);
				remainingCap[j] = 0.0;
			}
			else
				remainingCap[j] = outCapacity[badColumns.get(j)].getCenter() - sumIns.getCenter();
		}
		// 2a. Fill in available capacities respecting the specified split ratio bounds
		for (int j = 0; j < badColumns.size(); j++) {
			for (int i = 0; i < nIn; i++)
				for (int ii = 0; ii < nTypes; ii++)
					if (srm[i + ii*nIn][badColumns.get(j)] < 0) {
						double demand = inDemand[i].get(ii).getCenter();
						if (demand <= 0) {
							srm[i + ii*nIn][badColumns.get(j)] = inputsSRInfo[i + ii*nIn][0];
							inputsSRInfo[i + ii*nIn][0] = 0;
							continue;
						}	
						double sr = Math.max(0, Math.min(Math.min(remainingCap[j] / demand, inputsSRInfo[i + ii*nIn][0]), Math.abs(srm[i + ii*nIn][badColumns.get(j)])));
						inputsSRInfo[i + ii*nIn][0] -= sr;
						inputsSRInfo[i + ii*nIn][0] = Math.max(inputsSRInfo[i + ii*nIn][0], 0);
						remainingCap[j] -= sr * demand;
						remainingCap[j] = Math.max(remainingCap[j], 0);
						srm[i + ii*nIn][badColumns.get(j)] = sr;
					}
		}
		// 2b. Fill in the rest of available capacities
		for (int j = 0; j < badColumns.size(); j++) {
			for (int i = 0; i < nIn; i++)
				for (int ii = 0; ii < nTypes; ii++)
					if ((splitRatioMatrix[i][badColumns.get(j)].get(ii).getCenter() < 0) &&
							(inputsSRInfo[i + ii*nIn][1] > 0)) {
						double demand = inDemand[i].get(ii).getCenter();
						if (demand <= 0) {
							srm[i + ii*nIn][badColumns.get(j)] += inputsSRInfo[i + ii*nIn][0];
							inputsSRInfo[i + ii*nIn][0] = 0;
							continue;
						}
						double sr = Math.max(0, Math.min(remainingCap[j] / demand, inputsSRInfo[i + ii*nIn][0]));
						inputsSRInfo[i + ii*nIn][0] -= sr;
						inputsSRInfo[i + ii*nIn][0] = Math.max(inputsSRInfo[i + ii*nIn][0], 0);
						remainingCap[j] -= sr * demand;
						remainingCap[j] = Math.max(remainingCap[j], 0);
						srm[i + ii*nIn][badColumns.get(j)] += sr;
					}
		}
		// 3. Assign the remaining split ratio shares proportionally to capacities
		for (int i = 0; i < nIn; i++) {
			for (int ii = 0; ii < nTypes; ii++) {
				AuroraInterval totalCapacity = new AuroraInterval(); 
				for (int j = 0; j < badColumns.size(); j++) {
					if ((splitRatioMatrix[i][badColumns.get(j)].get(ii).getCenter() < 0) &&
							(inputsSRInfo[i + ii*nIn][1] > 0))
						totalCapacity.add(outCapacity[badColumns.get(j)]);
				}
				for (int j = 0; j < badColumns.size(); j++) {
					if ((splitRatioMatrix[i][badColumns.get(j)].get(ii).getCenter() < 0) &&
							(inputsSRInfo[i + ii*nIn][1] > 0)) {
						if (totalCapacity.getCenter() > 0)
							srm[i + ii*nIn][badColumns.get(j)] += inputsSRInfo[i + ii*nIn][0] * (outCapacity[badColumns.get(j)].getCenter()/totalCapacity.getCenter());
						else
							srm[i + ii*nIn][badColumns.get(j)] += inputsSRInfo[i + ii*nIn][0]/inputsSRInfo[i + ii*nIn][1];
					}
				}
			} // vehicle types 'for' loop
		} // row 'for' loop
		// 4. Scale down inputs according to capacities if necessary 
		for (int j = 0; j < badColumns.size(); j++) {
			AuroraInterval sumIns = new AuroraInterval();
			Vector<Integer> contributors = new Vector<Integer>();
			for (int i = 0; i < nIn; i++) {
				boolean isContributor = false;
				for (int ii = 0; ii < nTypes; ii++) {
					AuroraInterval val = new AuroraInterval();
					val.copy(inDemand[i].get(ii));
					if (srm[i + ii*nIn][badColumns.get(j)] >= 0) {
						val.affineTransform(srm[i + ii*nIn][badColumns.get(j)], 0);
						sumIns.add(val);
						if (val.getUpperBound() > 0.1)
							isContributor = true;
					}
				} // vehicle types 'for' loop
				if (isContributor)
					contributors.add((Integer)i);
			} // row 'for' loop
			double cc = 1;
			if (outCapacity[badColumns.get(j)].getCenter() < sumIns.getCenter())
				cc = outCapacity[badColumns.get(j)].getCenter() / sumIns.getCenter();
			if (cc < 1)
				for (int i = 0; i < contributors.size(); i++)
					for (int ii = 0; ii < nTypes; ii++)
						inDemand[contributors.get(i)].get(ii).affineTransform(cc, 0);
		}
		//
		// Assign split ratios
		//
		for (int j = 0; j < nOut; j++)
			for (int i = 0; i < nIn; i++)
				for (int ii = 0; ii < nTypes; ii++) {
					splitRatioMatrix[i][j].get(ii).setCenter(srm[i + ii*nIn][j], 0);
				}
		//
		// Assign input flows
		//
		for (int i = 0; i < nIn; i++) {
			inputs.set(i, inDemand[i]);
			AbstractControllerSimple ctrl = controllers.get(i);
			if (ctrl != null)
				ctrl.setActualInput(new Double(inDemand[i].sum().getUpperBound()));
		}
		//
		// Assign output flows
		//
		for (int j = 0; j < nOut; j++) {
			AuroraIntervalVector outFlow = new AuroraIntervalVector(nTypes);
			for (int i = 0; i < nIn; i++) {
				double[] splitRatios = new double[nTypes];
				for (int ii = 0; ii < nTypes; ii++)
					splitRatios[ii] = srm[i + ii*nIn][j];
				AuroraIntervalVector flw = new AuroraIntervalVector(nTypes);
				flw.copy(inDemand[i]);
				flw.affineTransform(splitRatios, 0);
				outFlow.add(flw);
			}
			outputs.set(j, outFlow);
		}
		return res;
	}
	
	/**
	 * Implementation greedy policy with fair distribution of demand.
	 * (Algorithm of Ajith Muralidharan).
	 * @param ts time step.
	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	@SuppressWarnings("unused")
	private synchronized boolean dataUpdate2(int ts) throws ExceptionDatabase, ExceptionSimulation {
		boolean res = super.dataUpdate(ts);
		if (!res)
			return res;
		//
		// Set current split ratio matrix
		//
		int idx = Math.max(0, (int)Math.floor(myNetwork.getSimTime()/srTP));
		if (splitRatioMatrix0 != null)
			setSplitRatioMatrix(splitRatioMatrix0);
		else if (!srmProfile.isEmpty())
			setSplitRatioMatrix(srmProfile.get(Math.min(idx, srmProfile.size()-1)));
		int nIn = predecessors.size(); // number of inputs
		int nOut = successors.size(); // number of outputs
		int nTypes = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();  // number of vehicle types
		//
		// Initialize input demands
		//
		AuroraIntervalVector[] inDemand = new AuroraIntervalVector[nIn];
		for (int i = 0; i < nIn; i++) {
			inDemand[i] = ((AbstractLinkHWC)predecessors.get(i)).getFlow();
			if ((myNetwork.isControlled()) && (controllers.get(i) != null)) {
				AuroraInterval sumDemand = inDemand[i].sum();
				double controllerRate = (Double)controllers.get(i).computeInput(this);
				if (controllerRate < sumDemand.getUpperBound()) { // adjust inputs according to controller rates
					double c = controllerRate / sumDemand.getUpperBound();
					for (int ii = 0; ii < nTypes; ii++)
						inDemand[i].get(ii).setUpperBound(c * inDemand[i].get(ii).getUpperBound());
				}
			}
		}
		//
		// Initialize output capacities
		//
		AuroraInterval[] outCapacity = new AuroraInterval[nOut];
		for (int j = 0; j < nOut; j++)
			outCapacity[j] = ((AbstractLinkHWC)successors.get(j)).getCapacity();
		//
		// Initialize split ratio matrix taking into account multiple vehicle types
		//
		// 1. Fill in the values
		double[][] srm = new double[nIn * nTypes][nOut];
		for (int j = 0; j < nOut; j++)
			for (int i = 0; i < nIn; i++)
				for (int ii = 0; ii < nTypes; ii++)
					srm[i + ii*nIn][j] = splitRatioMatrix[i][j].get(ii).getCenter();
		// 2. Make sure split ratio matrix is valid
		Util.normalizeMatrix(srm);
		// 3. Record outputs with undefined split ratios
		Vector<Integer> badColumns = new Vector<Integer>();
		for (int j = 0; j < nOut; j++) {
			boolean badColumn = false;
			for (int i = 0; i < nIn*nTypes; i++)
				if (srm[i][j] < 0)
					if (Util.countNegativeElements(srm, i) > 1)
						badColumn = true;
					else
						srm[i][j] = Math.max(0, 1 - Util.rowSumPositive(srm, i));
			if (badColumn) // save indices of the outputs that have undefined split ratios
				badColumns.add(j);	
		}
		// 4. Collect info about available share for undefined split ratios
		//    and the number of undefined split ratios for each input
		double[][] inputsSRInfo = new double[nIn*nTypes][2];
		for (int i = 0; i < nIn*nTypes; i++) {
			inputsSRInfo[i][1] = Util.countNegativeElements(srm, i);  // number of undefined split ratios
			if (inputsSRInfo[i][1] < 1)
				inputsSRInfo[i][0] = 0;
			else
				inputsSRInfo[i][0] = 1; // remaining share for a split ratio
		}
		//
		// Reduce input demand according to capacities on the outputs for which all split ratios are known
		//
		for (int j = 0; j < nOut; j++) {
			if (badColumns.indexOf((Integer)j) > -1)
				continue;
			AuroraInterval sumIns = new AuroraInterval();
			Vector<Integer> contributors = new Vector<Integer>();
			// compute total input demand assigned to output 'j'
			for (int i = 0; i < nIn; i++) {
				boolean isContributor = false;
				for (int ii = 0; ii < nTypes; ii++) {
					AuroraInterval val = new AuroraInterval();
					val.copy(inDemand[i].get(ii));
					val.affineTransform(srm[i + ii*nIn][j], 0);
					sumIns.add(val);
					if (val.getUpperBound() > 0.0001)
						isContributor = true;
					inputsSRInfo[i + ii*nIn][0] -= srm[i + ii*nIn][j];
				} // vehicle types 'for' loop
				if (isContributor)
					contributors.add((Integer)i);
			} // row 'for' loop
			// adjust inputs to capacity
			double lbc = 1;
			double ubc = 1;
			if (outCapacity[j].getLowerBound() < sumIns.getLowerBound())
				lbc = outCapacity[j].getLowerBound() / sumIns.getLowerBound();
			if (outCapacity[j].getUpperBound() < sumIns.getUpperBound())
				ubc = outCapacity[j].getUpperBound() / sumIns.getUpperBound();
			if ((lbc < 1) || (ubc < 1)) {
				for (int i = 0; i < contributors.size(); i++) {
					for (int ii = 0; ii < nTypes; ii++) {
						if ((inDemand[contributors.get(i)].get(ii).getSize() == 0) && (lbc == ubc))
							inDemand[contributors.get(i)].get(ii).affineTransform(lbc, 0);
						else {
							inDemand[contributors.get(i)].get(ii).affineTransformLB(lbc, 0);
							inDemand[contributors.get(i)].get(ii).affineTransformUB(ubc, 0);
						}
					} // vehicle types 'for' loop
				} // contributors 'for' loop
			} // 'if'
		} // column 'for' loop
		//
		// Process outputs with undefined split ratios if there are any
		//
		if (!badColumns.isEmpty()) {
			// 1. Reduce inputs according to available capacities for known split ratios
			double[] remainingCap = new double[badColumns.size()];
			for (int j = 0; j < badColumns.size(); j++) {
				AuroraInterval sumIns = new AuroraInterval();
				Vector<Integer> contributors = new Vector<Integer>();
				for (int i = 0; i < nIn; i++) {
					boolean isContributor = false;
					for (int ii = 0; ii < nTypes; ii++) {
						AuroraInterval val = new AuroraInterval();
						val.copy(inDemand[i].get(ii));
						double sr = srm[i + ii*nIn][badColumns.get(j)];
						if (sr >= 0) {
							val.affineTransform(sr, 0);
							sumIns.add(val);
							if (val.getUpperBound() > 0.0001)
								isContributor = true;
						}
						else
							srm[i + ii*nIn][badColumns.get(j)] = 0.0;
					} // vehicle types 'for' loop
					if (isContributor)
						contributors.add((Integer)i);
				} // row 'for' loop
				double cc = 1;
				if (outCapacity[badColumns.get(j)].getCenter() < sumIns.getCenter())
					cc = outCapacity[badColumns.get(j)].getCenter() / sumIns.getCenter();
				if (cc < 1) {
					for (int i = 0; i < contributors.size(); i++)
						for (int ii = 0; ii < nTypes; ii++)
							inDemand[contributors.get(i)].get(ii).affineTransform(cc, 0);
					remainingCap[j] = 0.0;
				}
				else
					remainingCap[j] = outCapacity[badColumns.get(j)].getCenter() - sumIns.getCenter();
			}
			// 2. Fill in the remaining capacity uniformly
			for (int i = 0; i < nIn; i++) {
				for (int ii = 0; ii < nTypes; ii++) {
					double demand = inDemand[i].get(ii).getCenter();
					double totalRemainingCap = 10.0;
					while ((totalRemainingCap > 1) && (inputsSRInfo[i + ii*nIn][0] > 0)) {
						totalRemainingCap = 0.0;
						double minRatio = 1.0;
						double maxRatio = 0.0;
						int minIndex = 0;
						for (int j = 0; j < badColumns.size(); j++) {
							if (splitRatioMatrix[i][badColumns.get(j)].get(ii).getCenter() >= 0)
								continue;
							totalRemainingCap += remainingCap[j];
							double a2cRatio;
							if (outCapacity[badColumns.get(j)].getCenter() > 0)
								a2cRatio = (outCapacity[badColumns.get(j)].getCenter() - remainingCap[j]) / outCapacity[badColumns.get(j)].getCenter();
							else
								a2cRatio = 1.0;
							if (a2cRatio < minRatio) {
								minRatio = a2cRatio;
								minIndex = j;
							}
							if (a2cRatio > maxRatio)
								maxRatio = a2cRatio;
						} // column 'for' loop
						double flow;
						flow = maxRatio * outCapacity[badColumns.get(minIndex)].getCenter() - outCapacity[badColumns.get(minIndex)].getCenter() + remainingCap[minIndex];
						if (flow < 0.001)
							break; //flow = remainingCap[minIndex];
						flow = Math.min(flow, inputsSRInfo[i + ii*nIn][0]*demand);
						double sr;
						if (demand > 0.001)
							sr = flow / demand;
						else
							sr = inputsSRInfo[i + ii*nIn][0];
						srm[i + ii*nIn][badColumns.get(minIndex)] += sr;
						inputsSRInfo[i + ii*nIn][0] -= sr;
						inputsSRInfo[i + ii*nIn][0] = Math.max(0, inputsSRInfo[i + ii*nIn][0]);
						remainingCap[minIndex] -= flow;
						remainingCap[minIndex] = Math.max(0, remainingCap[minIndex]);
						totalRemainingCap -= flow;
						totalRemainingCap = Math.max(0, totalRemainingCap);
					} // 'while' loop
				} // vehicle types 'for' loop
			} // row 'for' loop
			// 3. Assign the remaining split ratio shares proportionally to capacities
			for (int i = 0; i < nIn; i++) {
				for (int ii = 0; ii < nTypes; ii++) {
					AuroraInterval totalCapacity = new AuroraInterval(); 
					for (int j = 0; j < badColumns.size(); j++) {
						if ((splitRatioMatrix[i][badColumns.get(j)].get(ii).getCenter() < 0) &&
							(inputsSRInfo[i + ii*nIn][1] > 0))
							totalCapacity.add(outCapacity[badColumns.get(j)]);
					}
					for (int j = 0; j < badColumns.size(); j++) {
						if ((splitRatioMatrix[i][badColumns.get(j)].get(ii).getCenter() < 0) &&
							(inputsSRInfo[i + ii*nIn][1] > 0)) {
							if (totalCapacity.getCenter() > 0)
								srm[i + ii*nIn][badColumns.get(j)] += inputsSRInfo[i + ii*nIn][0] * (outCapacity[badColumns.get(j)].getCenter()/totalCapacity.getCenter());
							else
								srm[i + ii*nIn][badColumns.get(j)] += inputsSRInfo[i + ii*nIn][0]/inputsSRInfo[i + ii*nIn][1];
						}
					}
				} // vehicle types 'for' loop
			} // row 'for' loop
			// 4. Reduce inputs according to capacities if necessary 
			for (int j = 0; j < badColumns.size(); j++) {
				AuroraInterval sumIns = new AuroraInterval();
				Vector<Integer> contributors = new Vector<Integer>();
				for (int i = 0; i < nIn; i++) {
					boolean isContributor = false;
					for (int ii = 0; ii < nTypes; ii++) {
						AuroraInterval val = new AuroraInterval();
						val.copy(inDemand[i].get(ii));
						if (srm[i + ii*nIn][badColumns.get(j)] >= 0) {
							val.affineTransform(srm[i + ii*nIn][badColumns.get(j)], 0);
							sumIns.add(val);
							if (val.getUpperBound() > 0.0001)
								isContributor = true;
						}
					} // vehicle types 'for' loop
					if (isContributor)
						contributors.add((Integer)i);
				} // row 'for' loop
				double cc = 1;
				if (outCapacity[badColumns.get(j)].getCenter() < sumIns.getCenter())
					cc = outCapacity[badColumns.get(j)].getCenter() / sumIns.getCenter();
				if (cc < 1)
					for (int i = 0; i < contributors.size(); i++)
						for (int ii = 0; ii < nTypes; ii++)
							inDemand[contributors.get(i)].get(ii).affineTransform(cc, 0);
			}
		} // end of processing undefined split ratios
		//
		// Assign split ratios
		//
		for (int j = 0; j < nOut; j++)
			for (int i = 0; i < nIn; i++)
				for (int ii = 0; ii < nTypes; ii++) {
					splitRatioMatrix[i][j].get(ii).setCenter(srm[i + ii*nIn][j], 0);
				}
		//
		// Assign input flows
		//
		for (int i = 0; i < nIn; i++) {
			inputs.set(i, inDemand[i]);
			AbstractControllerSimple ctrl = controllers.get(i);
			if (ctrl != null)
				ctrl.setActualInput(new Double(inDemand[i].sum().getUpperBound()));
		}
		//
		// Assign output flows
		//
		for (int j = 0; j < nOut; j++) {
			AuroraIntervalVector outFlow = new AuroraIntervalVector(nTypes);
			for (int i = 0; i < nIn; i++) {
				double[] splitRatios = new double[nTypes];
				for (int ii = 0; ii < nTypes; ii++)
					splitRatios[ii] = srm[i + ii*nIn][j];
				AuroraIntervalVector flw = new AuroraIntervalVector(nTypes);
				flw.copy(inDemand[i]);
				flw.affineTransform(splitRatios, 0);
				outFlow.add(flw);
			}
			outputs.set(j, outFlow);
		}
		return res;
	}

	/**
	 * Implementation greedy policy with fair distribution of demand.<br>
	 * New implementation - unabridged.
	 * (Algorithm of Ajith Muralidharan).
	 * @param ts time step.
	 * @return <code>true</code> if all went well, <code>false</code> - otherwise.
	 * @throws ExceptionDatabase, ExceptionSimulation
	 */
	private synchronized boolean dataUpdate3(int ts) throws ExceptionDatabase, ExceptionSimulation {
		boolean res = super.dataUpdate(ts);
		if (!res)
			return res;
		//
		// Set current split ratio matrix
		//
		int idx = Math.max(0, (int)Math.floor(myNetwork.getSimTime()/srTP));
		if (splitRatioMatrix0 != null)
			setSplitRatioMatrix(splitRatioMatrix0);
		else if (!srmProfile.isEmpty())
			setSplitRatioMatrix(srmProfile.get(Math.min(idx, srmProfile.size()-1)));
		int nIn = predecessors.size(); // number of inputs
		int nOut = successors.size(); // number of outputs
		int nTypes = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();  // number of vehicle types
		//
		// Initialize input demands
		//
		AuroraIntervalVector[] inDemand = new AuroraIntervalVector[nIn];
		for (int i = 0; i < nIn; i++) {
			inDemand[i] = ((AbstractLinkHWC)predecessors.get(i)).getFlow();
			if ((myNetwork.isControlled()) && (controllers.get(i) != null)) {
				AuroraInterval sumDemand = inDemand[i].sum();
				double controllerRate = (Double)controllers.get(i).computeInput(this);
				if (controllerRate < sumDemand.getUpperBound()) { // adjust inputs according to controller rates
					double c = controllerRate / sumDemand.getUpperBound();
					for (int ii = 0; ii < nTypes; ii++)
						inDemand[i].get(ii).setUpperBound(c * inDemand[i].get(ii).getUpperBound());
				}
			}
		}
		//
		// Initialize output capacities
		//
		AuroraInterval[] outCapacity = new AuroraInterval[nOut];
		for (int j = 0; j < nOut; j++)
			outCapacity[j] = ((AbstractLinkHWC)successors.get(j)).getCapacity();
		//
		// Initialize split ratio matrix taking into account multiple vehicle types
		//
		// 1. Fill in the values
		double[][] srm = new double[nIn * nTypes][nOut];
		for (int j = 0; j < nOut; j++)
			for (int i = 0; i < nIn; i++)
				for (int ii = 0; ii < nTypes; ii++)
					srm[i + ii*nIn][j] = splitRatioMatrix[i][j].get(ii).getCenter();
		// 2. Make sure split ratio matrix is valid
		Util.normalizeMatrix(srm);
		// 3. Record outputs with undefined split ratios
		Vector<Integer> badColumns = new Vector<Integer>();
		for (int j = 0; j < nOut; j++) {
			boolean badColumn = false;
			for (int i = 0; i < nIn*nTypes; i++)
				if (srm[i][j] < 0)
					if (Util.countNegativeElements(srm, i) > 1)
						badColumn = true;
					else
						srm[i][j] = Math.max(0, 1 - Util.rowSumPositive(srm, i));
			if (badColumn) // save indices of the outputs that have undefined split ratios
				badColumns.add(j);	
		}
		// 4. Collect info about available share for undefined split ratios
		//    and the number of undefined split ratios for each input
		double[][] inputsSRInfo = new double[nIn*nTypes][2];
		for (int i = 0; i < nIn*nTypes; i++) {
			inputsSRInfo[i][1] = Util.countNegativeElements(srm, i);  // number of undefined split ratios
			if (inputsSRInfo[i][1] < 1)
				inputsSRInfo[i][0] = 0;
			else
				inputsSRInfo[i][0] = 1; // remaining share for a split ratio
		}
		//
		// Reduce input demand according to capacities on the outputs for which all split ratios are known
		//
		for (int j = 0; j < nOut; j++) {
			if (badColumns.indexOf((Integer)j) > -1)
				continue;
			AuroraInterval sumIns = new AuroraInterval();
			Vector<Integer> contributors = new Vector<Integer>();
			// compute total input demand assigned to output 'j'
			for (int i = 0; i < nIn; i++) {
				boolean isContributor = false;
				for (int ii = 0; ii < nTypes; ii++) {
					AuroraInterval val = new AuroraInterval();
					val.copy(inDemand[i].get(ii));
					val.affineTransform(srm[i + ii*nIn][j], 0);
					sumIns.add(val);
					if (val.getUpperBound() > 0.0001)
						isContributor = true;
					inputsSRInfo[i + ii*nIn][0] -= srm[i + ii*nIn][j];
				} // vehicle types 'for' loop
				if (isContributor)
					contributors.add((Integer)i);
			} // row 'for' loop
			// adjust inputs to capacity
			double lbc = 1;
			double ubc = 1;
			if (outCapacity[j].getLowerBound() < sumIns.getLowerBound())
				lbc = outCapacity[j].getLowerBound() / sumIns.getLowerBound();
			if (outCapacity[j].getUpperBound() < sumIns.getUpperBound())
				ubc = outCapacity[j].getUpperBound() / sumIns.getUpperBound();
			if ((lbc < 1) || (ubc < 1)) {
				for (int i = 0; i < contributors.size(); i++) {
					for (int ii = 0; ii < nTypes; ii++) {
						if ((inDemand[contributors.get(i)].get(ii).getSize() == 0) && (lbc == ubc))
							inDemand[contributors.get(i)].get(ii).affineTransform(lbc, 0);
						else {
							inDemand[contributors.get(i)].get(ii).affineTransformLB(lbc, 0);
							inDemand[contributors.get(i)].get(ii).affineTransformUB(ubc, 0);
						}
					} // vehicle types 'for' loop
				} // contributors 'for' loop
			} // 'if'
		} // column 'for' loop
		//
		// Process outputs with undefined split ratios if there are any
		//
		if (!badColumns.isEmpty()) {
			// 1. Reduce inputs according to available capacities for known split ratios
			double[] outDemand = new double[badColumns.size()];
			for (int j = 0; j < badColumns.size(); j++) {
				AuroraInterval sumIns = new AuroraInterval();
				for (int i = 0; i < nIn; i++) {
					for (int ii = 0; ii < nTypes; ii++) {
						AuroraInterval val = new AuroraInterval();
						val.copy(inDemand[i].get(ii));
						double sr = srm[i + ii*nIn][badColumns.get(j)];
						if (sr >= 0) {
							val.affineTransform(sr, 0);
							sumIns.add(val);
						}
						else
							srm[i + ii*nIn][badColumns.get(j)] = 0.0;
					} // vehicle types 'for' loop
				} // row 'for' loop
				outDemand[j] = sumIns.getCenter();
			}
			// 2. Fill in the remaining capacity uniformly
			for (int i = 0; i < nIn; i++) {
				for (int ii = 0; ii < nTypes; ii++) {
					double demand = inDemand[i].get(ii).getCenter();
					while (inputsSRInfo[i + ii*nIn][0] > 0.00000001) {
						double minRatio = 100.0;
						double maxRatio = 0.0;
						int minIndex = 0;
						for (int j = 0; j < badColumns.size(); j++) {
							if (splitRatioMatrix[i][badColumns.get(j)].get(ii).getCenter() >= 0)
								continue;
							double a2cRatio;
							if (outCapacity[badColumns.get(j)].getCenter() > 0)
								a2cRatio = outDemand[j] / outCapacity[badColumns.get(j)].getCenter();
							else
								a2cRatio = 1.0;
							if (a2cRatio < minRatio) {
								minRatio = a2cRatio;
								minIndex = j;
							}
							if (a2cRatio > maxRatio)
								maxRatio = a2cRatio;
						} // column 'for' loop
						if ((maxRatio - minRatio) < 0.0000001)
							break;
						double flow;
						flow = maxRatio * outCapacity[badColumns.get(minIndex)].getCenter() - outDemand[minIndex];
						flow = Math.min(flow, inputsSRInfo[i + ii*nIn][0]*demand);
						double sr;
						if (demand > 0.0001)
							sr = flow / demand;
						else
							sr = inputsSRInfo[i + ii*nIn][0];
						srm[i + ii*nIn][badColumns.get(minIndex)] += sr;
						inputsSRInfo[i + ii*nIn][0] -= sr;
						inputsSRInfo[i + ii*nIn][0] = Math.max(0, inputsSRInfo[i + ii*nIn][0]);
						outDemand[minIndex] += flow;
						if (sr < 0.000000001)
							break;
					} // 'while' loop
				} // vehicle types 'for' loop
			} // row 'for' loop
			// 3. Assign the remaining split ratio shares proportionally to capacities
			for (int i = 0; i < nIn; i++) {
				for (int ii = 0; ii < nTypes; ii++) {
					AuroraInterval totalCapacity = new AuroraInterval(); 
					for (int j = 0; j < badColumns.size(); j++) {
						if ((splitRatioMatrix[i][badColumns.get(j)].get(ii).getCenter() < 0) &&
							(inputsSRInfo[i + ii*nIn][1] > 0))
							totalCapacity.add(outCapacity[badColumns.get(j)]);
					}
					for (int j = 0; j < badColumns.size(); j++) {
						if ((splitRatioMatrix[i][badColumns.get(j)].get(ii).getCenter() < 0) &&
							(inputsSRInfo[i + ii*nIn][1] > 0)) {
							if (totalCapacity.getCenter() > 0)
								srm[i + ii*nIn][badColumns.get(j)] += inputsSRInfo[i + ii*nIn][0] * (outCapacity[badColumns.get(j)].getCenter()/totalCapacity.getCenter());
							else
								srm[i + ii*nIn][badColumns.get(j)] += inputsSRInfo[i + ii*nIn][0]/inputsSRInfo[i + ii*nIn][1];
						}
					}
				} // vehicle types 'for' loop
			} // row 'for' loop
			// 4. Reduce inputs according to capacities if necessary 
			for (int j = 0; j < badColumns.size(); j++) {
				AuroraInterval sumIns = new AuroraInterval();
				Vector<Integer> contributors = new Vector<Integer>();
				for (int i = 0; i < nIn; i++) {
					boolean isContributor = false;
					for (int ii = 0; ii < nTypes; ii++) {
						AuroraInterval val = new AuroraInterval();
						val.copy(inDemand[i].get(ii));
						if (srm[i + ii*nIn][badColumns.get(j)] >= 0) {
							val.affineTransform(srm[i + ii*nIn][badColumns.get(j)], 0);
							sumIns.add(val);
							if (val.getUpperBound() > 0.0001)
								isContributor = true;
						}
					} // vehicle types 'for' loop
					if (isContributor)
						contributors.add((Integer)i);
				} // row 'for' loop
				double cc = 1;
				if (outCapacity[badColumns.get(j)].getCenter() < sumIns.getCenter())
					cc = outCapacity[badColumns.get(j)].getCenter() / sumIns.getCenter();
				if (cc < 1)
					for (int i = 0; i < contributors.size(); i++)
						for (int ii = 0; ii < nTypes; ii++)
							inDemand[contributors.get(i)].get(ii).affineTransform(cc, 0);
			}
		} // end of processing undefined split ratios
		//
		// Assign split ratios
		//
		for (int j = 0; j < nOut; j++)
			for (int i = 0; i < nIn; i++)
				for (int ii = 0; ii < nTypes; ii++) {
					splitRatioMatrix[i][j].get(ii).setCenter(srm[i + ii*nIn][j], 0);
				}
		//
		// Assign input flows
		//
		for (int i = 0; i < nIn; i++) {
			inputs.set(i, inDemand[i]);
			AbstractControllerSimple ctrl = controllers.get(i);
			if (ctrl != null)
				ctrl.setActualInput(new Double(inDemand[i].sum().getUpperBound()));
		}
		//
		// Assign output weaving factors
		//
		for (int i = 0; i < nIn; i++) {
			double[] owf = new double[nTypes];
			for (int ii = 0; ii < nTypes; ii++) {
				double wf = 1;
				for (int j = 0; j < nOut; j++)
					wf += (Math.max(1, -weavingFactorMatrix[i][j]) - 1) * srm[i + ii*nIn][j];
				owf[ii] = wf;
			}
			((AbstractLinkHWC)predecessors.get(i)).setOutputWeavingFactors(owf);
		}
		//
		// Assign output flows
		//
		for (int j = 0; j < nOut; j++) {
			AuroraIntervalVector outFlow = new AuroraIntervalVector(nTypes);
			AuroraIntervalVector outFlow2 = new AuroraIntervalVector(nTypes);
			for (int i = 0; i < nIn; i++) {
				double[] splitRatios = new double[nTypes];
				for (int ii = 0; ii < nTypes; ii++)
					splitRatios[ii] = srm[i + ii*nIn][j];
				AuroraIntervalVector flw = new AuroraIntervalVector(nTypes);
				AuroraIntervalVector flw2 = new AuroraIntervalVector(nTypes);
				flw.copy(inDemand[i]);
				flw.affineTransform(splitRatios, 0);
				outFlow.add(flw);
				flw2.copy(flw);
				flw2.affineTransform(Math.max(1, weavingFactorMatrix[i][j]), 0);
				outFlow2.add(flw2);
			}
			outputs.set(j, outFlow);
			double nm = outFlow2.sum().getCenter();
			double dnm = outFlow.sum().getCenter();
			if (nm > 0.000000001)
				nm = nm / dnm;
			else
				nm = 1;
			((AbstractLinkHWC)successors.get(j)).setInputWeavingFactor(nm);
		}
		return res;
	}
	
	/**
	 * Validates Node configuration.<br>
	 * Checks if the dimensions of the split ratio matrix are correct.
	 * @return <code>true</code> if configuration is correct, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean validate() throws ExceptionConfiguration {
		boolean res = super.validate();
		if (weavingFactorMatrix == null) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Weaving factor matrix not assigned."));
			res = false;
			return res;
		}
		if (weavingFactorMatrix.length != inputs.size()) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Weaving factor matrix inputs (" + Integer.toString(weavingFactorMatrix.length) + ") does not match number of in-links (" + Integer.toString(inputs.size()) + ")."));
			res = false;
		}
		if (weavingFactorMatrix[0].length != outputs.size()) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Weaving factor matrix outputs (" + Integer.toString(weavingFactorMatrix[0].length) + ") does not match number of out-links (" + Integer.toString(outputs.size()) + ")."));
			res = false;
		}
		if (splitRatioMatrix == null) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Split ratio matrix not assigned."));
			res = false;
			return res;
		}
		if (splitRatioMatrix.length != inputs.size()) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Split ratio matrix inputs (" + Integer.toString(splitRatioMatrix.length) + ") does not match number of in-links (" + Integer.toString(inputs.size()) + ")."));
			res = false;
		}
		if (splitRatioMatrix[0].length != outputs.size()) {
			myNetwork.addConfigurationError(new ErrorConfiguration(this, "Split ratio matrix outputs (" + Integer.toString(splitRatioMatrix[0].length) + ") does not match number of out-links (" + Integer.toString(outputs.size()) + ")."));
			res = false;
		}
		return res;
	}
	
	
	/**
	 * Returns compatible simple controller type names.
	 */
	public String[] getSimpleControllerTypes() {
		String[] ctrlTypes = {"ALINEA",
							  "Traffic Responsive",
							  "TOD",
							  "VSL TOD",
							  "Simple Signal"};
		return ctrlTypes;
	}
	
	/**
	 * Returns compatible simple controller classes.
	 */
	public String[] getSimpleControllerClasses() {
		String[] ctrlClasses = {"aurora.hwc.control.ControllerALINEA",
								"aurora.hwc.control.ControllerTR",
								"aurora.hwc.control.ControllerTOD",
								"aurora.hwc.control.ControllerVSLTOD",
								"aurora.hwc.control.ControllerSimpleSignal"};
		return ctrlClasses;
	}
	
	/**
	 * Returns compatible queue controller type names.
	 */
	public String[] getQueueControllerTypes() {
		String[] qcTypes = {"Queue Override",
							"Proportional",
							"PI Control"};
		return qcTypes;
	}
	
	/**
	 * Returns compatible queue controller classes.
	 */
	public String[] getQueueControllerClasses() {
		String[] qcClasses = {"aurora.hwc.control.QOverride",
							  "aurora.hwc.control.QProportional",
							  "aurora.hwc.control.QPI"};
		return qcClasses;
	}
	
	/**
	 * Returns weaving factor matrix.
	 */
	public double[][] getWeavingFactorMatrix() {
		if (weavingFactorMatrix == null)
			return null;
		int m = weavingFactorMatrix.length;
		int n = weavingFactorMatrix[0].length;
		double[][] wfm = new double[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				wfm[i][j] = weavingFactorMatrix[i][j];
		return wfm;
	}
	
	/**
	 * Returns split ratio matrix.
	 */
	public AuroraIntervalVector[][] getSplitRatioMatrix() {
		if (splitRatioMatrix == null)
			return null;
		int m = splitRatioMatrix.length;
		int n = splitRatioMatrix[0].length;
		AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++) {
				srm[i][j] = new AuroraIntervalVector();
				srm[i][j].copy(splitRatioMatrix[i][j]);
			}
		return srm;
	}
	
	/**
	 * Returns split ratio matrix template.
	 */
	public AuroraIntervalVector[][] getSplitRatioMatrix0() {
		if (splitRatioMatrix0 == null)
			return null;
		int m = splitRatioMatrix0.length;
		int n = splitRatioMatrix0[0].length;
		AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++) {
				srm[i][j] = new AuroraIntervalVector();
				srm[i][j].copy(splitRatioMatrix0[i][j]);
			}
		return srm;
	}
	
	/**
	 * Returns split ratio profile.
	 */
	public Vector<AuroraIntervalVector[][]> getSplitRatioProfile() {
		return srmProfile;
	}
	
	/**
	 * Returns split ratio profile as text buffer.
	 */
	public String getSplitRatioProfileAsText() {
		String buf = "";
		for (int i = 0; i < srmProfile.size(); i++) {
			AuroraIntervalVector[][] srm = srmProfile.get(i);
			int m = 0;
			int n = 0;
			if (srm != null) {
				m = srm.length;
				n = srm[0].length;
			}
			if (i > 0)
				buf += "\n";
			for (int j = 0; j < m; j++) {
				if (j > 0)
					buf += "; ";
				for (int k = 0; k < n; k++) {
					if (k > 0)
						buf += ", ";
					buf += srm[j][k].toString();
				}
			}
		}
		return buf;
	}
	
	/**
	 * Returns split ratio profile as text buffer.
	 */
	public String getSplitRatioProfileAsXML() {
		String buf = "";
		for (int i = 0; i < srmProfile.size(); i++) {
			AuroraIntervalVector[][] srm = srmProfile.get(i);
			int m = 0;
			int n = 0;
			if (srm != null) {
				m = srm.length;
				n = srm[0].length;
			}
			buf += "<srm>";
			for (int j = 0; j < m; j++) {
				if (j > 0)
					buf += "; ";
				for (int k = 0; k < n; k++) {
					if (k > 0)
						buf += ", ";
					buf += srm[j][k].toString();
				}
			}
			buf += "</srm>\n";
		}
		return buf;
	}
	
	/**
	 * Returns split ratio sampling period.
	 */
	public double getSplitRatioTP() {
		return srTP;
	}
	
	/**
	 * Returns the split ration for given pair of input and output Links.
	 * @param in input Link.
	 * @param out output Link.
	 * @return corresponding value from the split ratio matrix.
	 */
	public AuroraIntervalVector getSplitRatio(AbstractLink in, AbstractLink out) {
		AuroraIntervalVector sr = null;
		int i = getPredecessors().indexOf(in);
		int j = getSuccessors().indexOf(out);
		if ((i >= 0) && (j >= 0)) {
			sr = new AuroraIntervalVector();
			sr.copy(splitRatioMatrix[i][j]);
		}
		return sr;
	}
	
	/**
	 * Sets weaving factor matrix.
	 * @param x weaving factor matrix.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public boolean setWeavingFactorMatrix(double[][] x) {
		if (x == null)
			return false;
		int m = x.length;
		int n = x[0].length;
		if ((m != inputs.size()) || (n != outputs.size()))
			return false;
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				weavingFactorMatrix[i][j] = x[i][j];
		return true;
	}
	
	/**
	 * Sets split ratio matrix.
	 * @param x split ratio matrix.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public boolean setSplitRatioMatrix(AuroraIntervalVector[][] x) {
		if (x == null)
			return false;
		int m = x.length;
		int n = x[0].length;
		if ((m != inputs.size()) || (n != outputs.size()))
			return false;
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++)
				splitRatioMatrix[i][j].copy(x[i][j]);
		return true;
	}
	
	/**
	 * Sets split ratio matrix template.
	 * @param x split ratio matrix.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public boolean setSplitRatioMatrix0(AuroraIntervalVector[][] x) {
		if (x == null)
			if (!srmProfile.isEmpty()) {
				splitRatioMatrix0 = null;
				return true;
			}
			else
				return false;
		int m = x.length;
		int n = x[0].length;
		if ((m != inputs.size()) || (n != outputs.size()))
			return false;
		splitRatioMatrix0 = new AuroraIntervalVector[m][n];
		for (int i = 0; i < m; i++)
			for (int j = 0; j < n; j++) {
				splitRatioMatrix0[i][j] = new AuroraIntervalVector();
				splitRatioMatrix0[i][j].copy(x[i][j]);
			}
		return true;
	}
	
	/**
	 * Sets split ratio profile from text.
	 * @param buf text buffer. Each line of this text describes a split ratio matrix,
	 * and matrix rows are separated by ';' as in MATLAB.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setSplitRatioProfile(String buf) {
		int m = getPredecessors().size();
		int n = getSuccessors().size();
		if ((buf == null) || (m <= 0) || (n <= 0))
			return false;
		StringTokenizer st1 = new StringTokenizer(buf, "\n");
		if (st1.hasMoreTokens())
			srmProfile.clear();
		while (st1.hasMoreTokens()) {
			AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
			String bufM = st1.nextToken();
			StringTokenizer st2 = new StringTokenizer(bufM, ";");
			int i = -1;
			while ((st2.hasMoreTokens()) && (++i < m)) {
				String bufR = st2.nextToken();
				StringTokenizer st3 = new StringTokenizer(bufR, ", ");
				int j = -1;
				int sz = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();
				while ((st3.hasMoreTokens()) && (++j < n)) {
					String srvtxt = st3.nextToken();
					AuroraIntervalVector srv = new AuroraIntervalVector();
					srv.setRawIntervalVectorFromString(srvtxt);
					int rsz = srv.size();
					if (myNetwork.getContainer().isSimulation()) {
						srv = new AuroraIntervalVector(sz);
						srv.setIntervalVectorFromString(srvtxt);
						for (int idx = rsz; idx < sz; idx++)
							srv.get(idx).copy(srv.get(rsz-1));
					}
					srm[i][j] = srv;
				}
				while (++j < n) {
					if (myNetwork.getContainer().isSimulation())
						srm[i][j] = new AuroraIntervalVector(sz);
					else
						srm[i][j] = new AuroraIntervalVector();
				}
			}
			srmProfile.add(srm);
		}
		return true;
	}
	
	/**
	 * Sets split ratio matrix change frequency.<br>
	 * @param x split ratio sampling period.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 */
	public synchronized boolean setSplitRatioTP(double x) {
		if (x >= myNetwork.getTP())
			srTP = x;
		else
			return false;
		return true;
	}
	
	/**
	 * Computes the sum of input flows.
	 * @return total input flow object.
	 */
	public AuroraIntervalVector totalInput() {
		AuroraIntervalVector sum = new AuroraIntervalVector();
		sum.copy((AuroraIntervalVector)inputs.get(0));
		for (int i = 1; i < inputs.size(); i++) {
			AuroraIntervalVector o = (AuroraIntervalVector)inputs.get(i);
			if (o != null)
				sum.add(o);
		}
		return sum;
	}
	
	/**
	 * Computes the sum of output flows.
	 * @return total input flow object.
	 */
	public AuroraIntervalVector totalOutput() {
		AuroraIntervalVector sum = new AuroraIntervalVector();
		sum.copy((AuroraIntervalVector)outputs.get(0));
		for (int i = 1; i < outputs.size(); i++) {
			AuroraIntervalVector o = (AuroraIntervalVector)outputs.get(i);
			if (o != null)
				sum.add(o);
		}
		return sum;
	}
	
	/**
	 * Adds input Link to the list.
	 * @param x input Link.
	 * @param c corresponding simple controller.
	 * @return index of the added Link, <code>-1</code> - if the Link could not be added.
	 */
	public synchronized int addInLink(AbstractLink x, AbstractControllerSimple c) {
		int idx = super.addInLink(x, c);
		if (idx >= 0) {
			int m = predecessors.size();
			int n = successors.size();
			if ((m < 1) || (n < 1)) {
				splitRatioMatrix = null;
				return idx;
			}
			int sz = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();
			double[][] wfm = new double[m][n];
			AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
			for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					wfm[i][j] = 1;
					srm[i][j] = new AuroraIntervalVector(sz, new AuroraInterval(1/n, 0));
				}
			}
			splitRatioMatrix = srm;
		}
		return idx;
	}

	/**
	 * Adds input Link to the list.
	 * @param x input Link.
	 * @return index of the added Link, <code>-1</code> - if the Link could not be added.
	 */
	public synchronized int addInLink(AbstractLink x) {
		return addInLink(x, null);
	}
	
	/**
	 * Adds output Link to the list.
	 * @param x output Link.
	 * @return index of the added Link, <code>-1</code> - if the Link could not be added.
	 */
	public synchronized int addOutLink(AbstractLink x) {
		int idx = super.addOutLink(x);
		if (idx >= 0) {
			int m = predecessors.size();
			int n = successors.size();
			if ((m < 1) || (n < 1)) {
				splitRatioMatrix = null;
				return idx;
			}
			int sz = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();
			double[][] wfm = new double[m][n];
			AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
			for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					wfm[i][j] = 1;
					srm[i][j] = new AuroraIntervalVector(sz, new AuroraInterval(1/n, 0));
				}
			}
			splitRatioMatrix = srm;
		}
		return idx;
	}
	
	/**
	 * Deletes specified NE from the list of predecessors. 
	 * @param x predecessor NE to be deleted.
	 * @return idx index of deleted predecessor, <code>-1</code> - if such NE was not found.
	 */
	public synchronized int deletePredecessor(AbstractNetworkElement x) {
		int idx = super.deletePredecessor(x);
		int m = predecessors.size();
		int n = successors.size();
		if ((m < 1) || (n < 1)) {
			splitRatioMatrix = null;
			return idx;
		}
		int sz = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();
		double[][] wfm = new double[m][n];
		AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				wfm[i][j] = 1;
				srm[i][j] = new AuroraIntervalVector(sz, new AuroraInterval(1/n, 0));
			}
		}
		splitRatioMatrix = srm;
		return idx;
	}
	
	/**
	 * Deletes specified NE from the list of successors. 
	 * @param x successor NE to be deleted.
	 * @return idx index of deleted successor, <code>-1</code> - if such NE was not found.
	 */
	public synchronized int deleteSuccessor(AbstractNetworkElement x) {
		int idx = super.deleteSuccessor(x);
		int m = predecessors.size();
		int n = successors.size();
		if ((m < 1) || (n < 1)) {
			splitRatioMatrix = null;
			return idx;
		}
		int sz = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).countVehicleTypes();
		double[][] wfm = new double[m][n];
		AuroraIntervalVector[][] srm = new AuroraIntervalVector[m][n];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				wfm[i][j] = 1;
				srm[i][j] = new AuroraIntervalVector(sz, new AuroraInterval(1/n, 0));
			}
		}
		splitRatioMatrix = srm;
		return idx;
	}
	
	/**
	 * Adjust vector data according to new vehicle weights.
	 * @param w array of weights.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean adjustWeightedData(double[] w) {
		double[] ow = ((SimulationSettingsHWC)myNetwork.getContainer().getMySettings()).getVehicleWeights();
		if ((w == null) || (ow == null))
			return false;
		boolean res = true;
		for (int i = 0; i < inputs.size(); i++) {
			if (inputs.get(i) != null) {
				res &= ((AuroraIntervalVector)inputs.get(i)).inverseAffineTransform(ow, 0);
				res &= ((AuroraIntervalVector)inputs.get(i)).affineTransform(w, 0);
			}
		}
		for (int i = 0; i < outputs.size(); i++) {
			if (outputs.get(i) != null) {
				res &= ((AuroraIntervalVector)outputs.get(i)).inverseAffineTransform(ow, 0);
				res &= ((AuroraIntervalVector)outputs.get(i)).affineTransform(w, 0);
			}
		}
		return res;
	}
	
	/**
	 * Additional initialization.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration, ExceptionDatabase
	 */
	public boolean initialize() throws ExceptionConfiguration, ExceptionDatabase {
		boolean res = super.initialize();
		if (!srmProfile.isEmpty())
			splitRatioMatrix0 = null;
		boolean srmDefined = true;
		if (splitRatioMatrix != null) {
			int nIn = splitRatioMatrix.length;
			int nOut = splitRatioMatrix[0].length;
			for (int i = 0; i < nIn; i++)
				for (int j = 0; j < nOut; j++)
					if (splitRatioMatrix[i][j].minCenter() < 0)
						srmDefined = false;
		}
		if (srmDefined)
			splitRatioMatrix0 = null;
		return res;
	}
	
	/**
	 * Copies data from given Node to a current one.
	 * @param x given Network Element.
	 * @return <code>true</code> if successful, <code>false</code> - otherwise.
	 */
	public synchronized boolean copyData(AbstractNetworkElement x) {
		boolean res = super.copyData(x);
		if (res) {
			AbstractNodeHWC nd = (AbstractNodeHWC)x;
			weavingFactorMatrix = nd.getWeavingFactorMatrix();
			splitRatioMatrix = nd.getSplitRatioMatrix();
			splitRatioMatrix0 = nd.getSplitRatioMatrix0();
		}
		return res;
	}
	
}
