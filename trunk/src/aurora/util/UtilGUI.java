/**
 * @(#)UtilGUI.java
 */

package aurora.util;

import java.awt.*;
import javax.swing.*;

import aurora.*;


/**
 * Utilities for GUI work.
 * @author Alex Kurzhanskiy
 * @version $Id: UtilGUI.java,v 1.1.2.4 2007/05/18 04:39:42 akurzhan Exp $
 */
public class UtilGUI {
	
	/**
	 * Center a Window, Frame, JFrame or Dialog, etc.
	 * @param w Window.
	 */
	public static void center(Window w) {
		Dimension us = w.getSize();
		Dimension them = Toolkit.getDefaultToolkit().getScreenSize();
		w.setLocation(((them.width - us.width)/2), ((them.height - us.height)/2));
		return;
	}

	/**
	 * Creates icon.
	 * @param path relative path.
	 */
	public static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = AbstractNetworkElement.class.getResource(path);
        if (imgURL != null)
            return new ImageIcon(imgURL);
        return null;
	}
	
	/**
	 * Creates icon.
	 * @param path relative path.
	 * @param atxt alternative text.
	 */
	public static ImageIcon createImageIcon(String path, String atxt) {
		java.net.URL imgURL = AbstractNetworkElement.class.getResource(path);
        if (imgURL != null)
            return new ImageIcon(imgURL, atxt);
        return null;
	}
	
	/**
	 * Returns n-dimensional array of colors for given nx3 integer array of RGB values. 
	 */
	public static Color[] getColorScale(int[][] rgb) {
		if (rgb == null)
			return null;
		Color[] clr = new Color[rgb.length];
		for (int i = 0; i < rgb.length; i++) {
			float[] hsb =  Color.RGBtoHSB(rgb[i][0], rgb[i][1], rgb[i][2], null);
			clr[i] = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
		}
		return clr;
	}
	
	/**
	 * Returns blue-yellow-red color scale.
	 */
	public static Color[] byrColorScale() {
		int[][] rgb = {
				{0, 0, 0},
				{0, 0, 159},
				{0, 0, 191},
				{0, 0, 223},
				{0, 0, 255},
				{0, 32, 255},
				{0, 64, 255},
				{0, 96, 255},
				{0, 128, 255},
				{0, 159, 255},
				{0, 191, 255},
				{0, 223, 255},
				{0, 255, 255},
				{32, 255, 223},
				{64, 255, 191},
				{96, 255, 159},
				{128, 255, 128},
				{159, 255, 96},
				{191, 255, 64},
				{223, 255, 32},
				{255, 255, 0},
				{255, 223, 0},
				{255, 191, 0},
				{255, 159, 0},
				{255, 128, 0},
				{255, 96, 0},
				{255, 64, 0},
				{255, 32, 0},
				{255, 0, 0},
				{223, 0, 0},
				{191, 0, 0},
		};
		return getColorScale(rgb);
	}
	
	/**
	 * Returns green-yellow-red-black color scale.
	 */
	public static Color[] gyrkColorScale() {
		int[][] rgb = {
				{0, 0, 0},
				{0, 164, 0},
				{19, 174, 0},
				{41, 184, 0},
				{65, 194, 0},
				{91, 204, 0},
				{119, 215, 0},
				{150, 225, 0},
				{183, 235, 0},
				{218, 245, 0},
				{255, 255, 0},
				{255, 202, 0},
				{255, 180, 0},
				{255, 157, 0},
				{255, 135, 0},
				{255, 112, 0},
				{255, 90, 0},
				{255, 67, 0},
				{255, 45, 0},
				{255, 22, 0},
				{255, 0, 0},
				{229, 0, 0},
				{206, 0, 0},
				{183, 0, 0},
				{160, 0, 0},
				{137, 0, 0},
				{113, 0, 0},
				{90, 0, 0},
				{67, 0, 0},
				{44, 0, 0},
				{21, 0, 0}
			};
		return getColorScale(rgb);
	}
	
	/**
	 * Returns black-red-yellow-green color scale.
	 */
	public static Color[] krygColorScale() {
		int[][] rgb = {
				{0, 0, 0},
				{21, 0, 0},
				{44, 0, 0},
				{67, 0, 0},
				{90, 0, 0},
				{113, 0, 0},
				{137, 0, 0},
				{160, 0, 0},
				{183, 0, 0},
				{206, 0, 0},
				{229, 0, 0},
				{255, 0, 0},
				{255, 22, 0},
				{255, 45, 0},
				{255, 67, 0},
				{255, 90, 0},
				{255, 112, 0},
				{255, 135, 0},
				{255, 157, 0},
				{255, 180, 0},
				{255, 202, 0},
				{255, 255, 0},
				{218, 245, 0},
				{183, 235, 0},
				{150, 225, 0},
				{119, 215, 0},
				{91, 204, 0},
				{65, 194, 0},
				{41, 184, 0},
				{19, 174, 0},
				{0, 164, 0}
			};
		return getColorScale(rgb);
	}
	
	/**
	 * Returns color based on 0-9 scale ranging from green to yellow.
	 */
	public static Color gyColor(int i) {
		int[][] rgb = {
						{0, 164, 0},
						{19, 174, 0},
						{41, 184, 0},
						{65, 194, 0},
						{91, 204, 0},
						{119, 215, 0},
						{150, 225, 0},
						{183, 235, 0},
						{218, 245, 0},
						{255, 255, 0}
					};
		int ii = 0;
		if (i > 9)
			ii = 9;
		else
			ii = Math.max(i, ii);
		float[] hsb = Color.RGBtoHSB(rgb[ii][0], rgb[ii][1], rgb[ii][2], null);
		return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
	}
	
	/**
	 * Returns color based on 0-9 scale ranging from yellow to red.
	 */
	public static Color yrColor(int i) {
		int[][] rgb = {
						{255, 202, 0},
						{255, 180, 0},
						{255, 157, 0},
						{255, 135, 0},
						{255, 112, 0},
						{255, 90, 0},
						{255, 67, 0},
						{255, 45, 0},
						{255, 22, 0},
						{255, 0, 0}
					};
		int ii = 0;
		if (i > 9)
			ii = 9;
		else
			ii = Math.max(i, ii);
		float[] hsb =  Color.RGBtoHSB(rgb[ii][0], rgb[ii][1], rgb[ii][2], null);
		return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
	}
	
	/**
	 * Returns color based on 0-9 scale ranging from red to black.
	 */
	public static Color rkColor(int i) {
		int[][] rgb = {
						{229, 0, 0},
						{206, 0, 0},
						{183, 0, 0},
						{160, 0, 0},
						{137, 0, 0},
						{113, 0, 0},
						{90, 0, 0},
						{67, 0, 0},
						{44, 0, 0},
						{21, 0, 0}
					};
		int ii = 0;
		if (i > 9)
			ii = 9;
		else
			ii = Math.max(i, ii);
		float[] hsb =  Color.RGBtoHSB(rgb[ii][0], rgb[ii][1], rgb[ii][2], null);
		return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
	}
	
	/**
	 * Returns color based on 0-9 scale ranging from black to green.
	 */
	public static Color kgColor(int i) {
		int[][] rgb = {
						{21, 0, 0},
						{99, 0, 0},
						{177, 0, 0},
						{255, 0, 0},
						{255, 85, 0},
						{255, 170, 0},
						{255, 255, 0},
						{150, 225, 0},
						{65, 194, 0},
						{0, 164, 0}
					};
		int ii = 0;
		if (i > 9)
			ii = 9;
		else
			ii = Math.max(i, ii);
		float[] hsb =  Color.RGBtoHSB(rgb[ii][0], rgb[ii][1], rgb[ii][2], null);
		return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
	}
	
}