//
// $Id: ButtonUtil.java,v 1.1 2002/03/03 20:56:12 mdb Exp $

package robodj.util;

import java.awt.Image;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

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
        String tooltip, String action, String iconPath, ActionListener al)
    {
        return createControlButton(tooltip, action, iconPath, al, false);
    }

    public static JButton createControlButton (
        String tooltip, String action, String iconPath,
        ActionListener al, boolean borderless)
    {
        return createControlButton(
            tooltip, action, getIcon(iconPath), al, borderless);
    }

    public static JButton createControlButton (
        String tooltip, String action, ImageIcon icon, ActionListener al)
    {
        return createControlButton(tooltip, action, icon, al, false);
    }

    public static JButton createControlButton (
        String tooltip, String action, ImageIcon icon,
        ActionListener al, boolean borderless)
    {
        JButton cbut = new JButton(icon);
        cbut.setActionCommand(action);
        cbut.addActionListener(al);
        cbut.setToolTipText(tooltip);
        // clear out that annoying fat border that swing uses
        cbut.setBorder(borderless ? BorderFactory.createEmptyBorder() :
                       BorderFactory.createEtchedBorder());
        return cbut;
    }
}
