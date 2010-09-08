/**
 * @(#)JFileFilter.java
 */

package aurora.util;

import java.io.File;
import java.util.*;
import javax.swing.filechooser.FileFilter;


/**
 * Simple <code>FileFilter</code> class that works by filename extension. 
 * @author Alex Kurzhanskiy
 * @version $Id$
 */
public class JFileFilter extends FileFilter {
	protected String description;
	protected ArrayList<String> exts = new ArrayList<String>();
	
	
	/**
	 * Adds new extension.
	 */
	public void addType(String s) {
		exts.add(s);
		return;
	}
	
	/**
	 * Returns <code>true</code> if the given file
	 * is accepted by this filter.
	 */
	public boolean accept(File f) {
		if (f.isDirectory())
			return true;
		else
			if (f.isFile()) {
				Iterator it = exts.iterator();
				while (it.hasNext())
					if (f.getName().endsWith((String)it.next()))
							return true;
			}
		return false;
	}

	/**
	 * Returns printable description of this filter.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets printable description of this filter.
	 */
	public void setDescription(String s) {
		description = s;
		return;
	}
}
