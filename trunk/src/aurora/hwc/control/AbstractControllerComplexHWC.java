/**
 * @(#)AbstractControllerComplex.java
 */

package aurora.hwc.control;

import java.util.*;
import org.w3c.dom.*;
import aurora.*;


/**
 * Base class for road network complex controllers.
 * @author Gabriel Gomes, Alex Kurzhanskiy
 * @version $Id$
 */
public abstract class AbstractControllerComplexHWC extends AbstractControllerComplex {
	private static final long serialVersionUID = 5002253704711958957L;

	public boolean usesensors = false;	// TEMPORARY!
	protected Vector<Double> limits = new Vector<Double>(); // input limits

	
	/**
	 * Initializes controller from given DOM structure.
	 * @param p DOM node.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration
	 */
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = super.initFromDOM(p);
		if (!res)
			return res;
		int i;
		Node n = null;
		try {
			if (p.getAttributes().getNamedItem("usesensors") != null)
				usesensors = Boolean.parseBoolean(p.getAttributes().getNamedItem("usesensors").getNodeValue());
			NodeList pp = p.getChildNodes();
			for (i = 0; i < pp.getLength(); i++) {
				if (pp.item(i).getNodeName().equals("limits")) {
					if ((n = pp.item(i).getAttributes().getNamedItem("cmin")) != null)
						limits.set(0, Double.parseDouble(n.getNodeValue()));
					if ((n = pp.item(i).getAttributes().getNamedItem("cmax")) != null)
						limits.set(1, Double.parseDouble(n.getNodeValue()));
				}
			}
		} catch (Exception e) {
			res = false;
			throw new ExceptionConfiguration(e.getMessage());
		}

		return res;
	}

}
