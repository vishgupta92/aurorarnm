/**
 * @(#)LinkDummy.java
 */

package aurora.hwc;


/**
 * Dummy Link.
 * @author Alex Kurzhanskiy
 * @version $Id$
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
	
	/**
	 * Returns letter code of the Link type.
	 */
	public String getTypeLetterCode() {
		return "D";
	}
	
	/**
	 * Returns type description.
	 */
	public final String getTypeString() {
		return "Dummy Link";
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
