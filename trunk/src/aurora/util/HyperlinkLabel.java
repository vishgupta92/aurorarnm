/**
 * @(#)HyperlinkLabel.java
 */

package aurora.util;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;


/**
 * Implementation of 
 * @author Alex Kurzhanskiy
 * @version $Id: HyperlinkLabel.java,v 1.1.4.1 2008/10/16 03:37:14 akurzhan Exp $
 */
public class HyperlinkLabel extends JLabel implements MouseListener {
	private static final long serialVersionUID = 5167616594614061634L;
	
	private URL url = null;
  
	
	public HyperlinkLabel(String label){
		super(label);
		addMouseListener(this);
	}
	public HyperlinkLabel(String label, URL url){
		this(label);
		this.url = url;
		setText("<html><a href=\"\">" + label + "</a></html>");
		setToolTipText("Go to: " + url.getRef());
	}
	public HyperlinkLabel(String label, String tip, URL url){
		this(label, url);
		setToolTipText(tip);
	}
  
	
	public void setURL(URL url){ this.url = url; }
  
	public URL getURL(){ return url; }

	public void mouseClicked(MouseEvent e) {
		HyperlinkLabel self = (HyperlinkLabel) e.getSource();
		if(self.url == null)
			return;
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE))
				try{
					desktop.browse(url.toURI());
					return;
				}
			catch(Exception exp){ }
		}
		JOptionPane.showMessageDialog(this, "Cannot launch browser...\n Please, visit\n" + url.getRef() , "", JOptionPane.INFORMATION_MESSAGE);
		return;
	}

	public void mouseEntered(MouseEvent e) {
		e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return;
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		return;
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		return;
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		return;
	}
  
}

