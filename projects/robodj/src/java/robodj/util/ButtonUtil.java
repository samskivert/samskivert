//
// $Id: ButtonUtil.java,v 1.2 2003/05/04 18:16:07 mdb Exp $

package robodj.util;

import java.awt.Image;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.samskivert.swing.Controller;

import robodj.Log;

/**
 * Routines for creating buttons with icon images.
 */
public class ButtonUtil
{
    public static ImageIcon getIcon (String iconPath)
    {
        try {
            InputStream imgin = ButtonUtil.class.getResourceAsStream(iconPath);
            if (imgin != null) {
                Image image = ImageIO.read(imgin);
                return new ImageIcon(image);
            }

        } catch (IOException ioe) {
            Log.warning("Unable to load icon [path=" + iconPath +
                        ", error=" + ioe + "].");

        } catch (Exception e) {
            Log.warning("Error loading icon [path=" + iconPath + "].");
            Log.logStackTrace(e);
        }
        return null;
    }

    public static JButton createControlButton (
        String tooltip, String action, String iconPath)
    {
        return createControlButton(tooltip, action, iconPath, false);
    }

    public static JButton createControlButton (
        String tooltip, String action, String iconPath, boolean borderless)
    {
        return createControlButton(
            tooltip, action, getIcon(iconPath), borderless);
    }

    public static JButton createControlButton (
        String tooltip, String action, ImageIcon icon)
    {
        return createControlButton(tooltip, action, icon, false);
    }

    public static JButton createControlButton (
        String tooltip, String action, ImageIcon icon, boolean borderless)
    {
        JButton cbut = new JButton(icon);
        cbut.setActionCommand(action);
        cbut.addActionListener(Controller.DISPATCHER);
        cbut.setToolTipText(tooltip);
        // clear out that annoying fat border that swing uses
        cbut.setBorder(borderless ? BorderFactory.createEmptyBorder() :
                       BorderFactory.createEtchedBorder());
        return cbut;
    }
}
