/**
 * @(#)AbstractSensor.java
 */
package aurora;

import org.w3c.dom.Node;

/**
 * Base class for sensor objects.
 * @author Gabriel Gomes
 * $Id$
 */
public abstract class AbstractSensor extends AbstractNetworkElement {
	private static final long serialVersionUID = -3489585106242370041L;
	
	protected Point position = new Point();
	protected double linkPosition;
	protected AbstractLink myLink;
	
	/* (non-Javadoc)
	 * @see aurora.AbstractNetworkElement#initFromDOM(org.w3c.dom.Node)
	 */
	@Override
	public boolean initFromDOM(Node p) throws ExceptionConfiguration {
		boolean res = true;
		if ((p == null) || (myNetwork == null))
			return !res;
		try  {
			id = Integer.parseInt(p.getAttributes().getNamedItem("id").getNodeValue());
			myLink = myNetwork.getLinkById(Integer.parseInt(p.getAttributes().getNamedItem("link").getNodeValue()));
			linkPosition = Double.parseDouble(p.getAttributes().getNamedItem("linkposition").getNodeValue());
			
			AbstractNode nd = myLink.getBeginNode();
			Point bPos;
			if( nd==null ){
				PositionLink plk = myLink.getPosition();
				bPos = plk.pp.get(0);
			}
			else{
				bPos = nd.position.get();
			}
				
			//Point ePos = myLink.getEndNode().position.get();
			
			position.x = bPos.x;		// GCG FIX THIS
			position.y = bPos.y;		// GCG FIX THIS
			position.z = bPos.z;		// GCG FIX THIS

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
	public final Point getPosition() {
		return position;
	}
	
	/**
	 * Returns X coordinate of the Sensor.
	 */
	public final double getPositionX() {
		return position.x;
	}

	/**
	 * Returns Y coordinate of the Sensor.
	 */
	public final double getPositionY() {
		return position.y;
	}

	/**
	 * Returns Z coordinate of the Sensor.
	 */
	public final double getPositionZ() {
		return position.z;
	}
	
	/**
	 * Returns position of the Sensor within link.
	 */
	public final double getLinkPosition() {
		return linkPosition;
	}

	/**
	 * Returns link that contains the sensor.
	 */
	public final AbstractLink getLink() {
		return myLink;
	}

}
