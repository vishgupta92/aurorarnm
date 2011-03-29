/**
 * @(#)AbstractSensor.java
 */
package aurora;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Base class for sensor objects.
 * @author Gabriel Gomes
 * $Id$
 */
public abstract class AbstractSensor extends AbstractNetworkElement {
	private static final long serialVersionUID = -3489585106242370041L;
	
	//protected String name;
	protected PositionNode position = new PositionNode();
	protected float offset_in_link = Float.NaN;
	protected AbstractLink myLink;
	protected String description;
	protected String display_lat;
	protected String display_lng;
	
	/* (non-Javadoc)
	 * @see aurora.AbstractNetworkElement#initFromDOM(org.w3c.dom.Node)
	 */
	@Override
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if ((p == null) || (myNetwork == null))
			return !res;
		try  {
			
			
			Node pp;
			pp = p.getAttributes().getNamedItem("id");
			if(pp!=null)
				id = Integer.parseInt(pp.getNodeValue());

			//pp = p.getAttributes().getNamedItem("name");
			//if(pp!=null)
			//	name = pp.getNodeValue();
			
			pp = p.getAttributes().getNamedItem("offset_in_link");
			if(pp!=null)
				offset_in_link = Float.parseFloat(pp.getNodeValue());

			pp = p.getAttributes().getNamedItem("description");
			if(pp!=null)
				description = pp.getNodeValue();

			pp = p.getAttributes().getNamedItem("display_lat");
			if(pp!=null)
				display_lat = pp.getNodeValue();
			
			pp = p.getAttributes().getNamedItem("display_lng");
			if(pp!=null)
				display_lng = pp.getNodeValue();
			
			
			if(p.hasChildNodes()){

				NodeList c = p.getChildNodes();
			
				for(int i=0;i<c.getLength();i++){
					pp = c.item(i);
					if(pp.getNodeName().equals("links")){
						myLink = myNetwork.getLinkById(Integer.parseInt(pp.getTextContent()));
					}
					
					if(pp.getNodeName().equals("position")){
						
					}
				
					if (pp.getNodeName().equals("position")) {
						position = new PositionNode();
						res &= position.initFromDOM(pp);
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
	
	/**
	 * Additional initialization.
	 * @return <code>true</code> if operation succeeded, <code>false</code> - otherwise.
	 * @throws ExceptionConfiguration, ExceptionDatabase
	 */
	public boolean initialize() throws ExceptionConfiguration, ExceptionDatabase {
		boolean res = super.initialize();
		// TODO: GG NEED TO COMPLETE THIS FUNCTION
		return res;
	}

	/**
	 * Returns position of the Sensor.
	 */
	public final PositionNode getPosition() {
		return position;
	}
	
	/**
	 * Returns X coordinate of the Sensor.
	 */
	public final double getPositionX() {
		return position.p.x ;
	}

	/**
	 * Returns Y coordinate of the Sensor.
	 */
	public final double getPositionY() {
		return position.p.y;
	}

	/**
	 * Returns Z coordinate of the Sensor.
	 */
	public final double getPositionZ() {
		return position.p.z;
	}
	
	/**
	 * Returns position of the Sensor within link.
	 */
	public final double getOffsetInLink() {
		return offset_in_link;
	}

	/**
	 * Returns link that contains the sensor.
	 */
	public final AbstractLink getLink() {
		return myLink;
	}

}
