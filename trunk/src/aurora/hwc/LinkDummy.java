/**
 * @(#)LinkDummy.java
 */

package aurora.hwc;


/**
 * Dummy Link.
 * @author Alex Kurzhanskiy
 * @version $Id: LinkDummy.java,v 1.8.2.1 2008/10/16 04:27:08 akurzhan Exp $
 */
public final class LinkDummy extends AbstractLinkHWC {
	private static final long serialVersionUID = 2061517905572701027L;

	
	public LinkDummy() { }
	public LinkDummy(int id) { this.id = id; }
	public LinkDummy(int id, DynamicsHWC dyn) { this.id = id; myDynamics = dyn; }
	
	
	/**
	 * Returns type.
	 */
	public final int getType() {
		return TypesHWC.LINK_DUMMY;
	}
	
	@Override
	public boolean dataUpdate(int ts) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean validate() {
		// TODO Auto-generated method stub
		return true;
	}

}
