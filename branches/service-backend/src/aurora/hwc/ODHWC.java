/**
 * @(#)ODHWC.java
 */

package aurora.hwc;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aurora.*;


/**
 * HWC specific OD class.
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public class ODHWC extends OD {
	private static final long serialVersionUID = 2277962064100432249L;

	
	/**
	 * Initializes an OD from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if (p == null)
			return res;
		origin = myNetwork.getNodeById(Integer.parseInt(p.getAttributes().getNamedItem("begin").getNodeValue()));
		destination = myNetwork.getNodeById(Integer.parseInt(p.getAttributes().getNamedItem("end").getNodeValue()));
		if (!p.hasChildNodes())
			return res;
		NodeList pp = p.getChildNodes();
		try {
			for (int i = 0; i < pp.getLength(); i++) {
				if (pp.item(i).getNodeName().equals("PathList")) {
					if (pp.item(i).hasChildNodes()) {
						NodeList pp2 = pp.item(i).getChildNodes();
						for (int j = 0; j < pp2.getLength(); j++) {
							if (pp2.item(j).getNodeName().equals("path")) {
								Path pth = new PathHWC();
								pth.setMyOD(this);
								pth.initFromDOM(pp2.item(j));
								pathList.add(pth);
							}
							if (pp2.item(j).getNodeName().equals("include")) {
								Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pp2.item(j).getAttributes().getNamedItem("uri").getNodeValue());
								NodeList ipp = doc.getChildNodes().item(0).getChildNodes();
								for (int ii = 0; ii < ipp.getLength(); ii++)
									if (ipp.item(ii).getNodeName().equals("path")) {
										Path pth = new PathHWC();
										pth.setMyOD(this);
										pth.initFromDOM(ipp.item(ii));
										pathList.add(pth);
									}
							}
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
	
}
