//
// $Id: Item.java,v 1.1 2003/05/04 18:16:06 mdb Exp $

package robodj.chooser;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.samskivert.swing.util.SwingUtil;

import robodj.util.ButtonUtil;

/**
 * The base class for items displayed in an entry list or playlist.
 */
public class Item extends JPanel
{
    // documentation inherited
    protected void paintChildren (Graphics g)
    {
        Graphics2D gfx = (Graphics2D)g;
        Object key = SwingUtil.activateAntiAliasing(gfx);
        super.paintChildren(g);
        SwingUtil.restoreAntiAliasing(gfx, key);
    }

    protected static Font _nameFont = new Font("Dialog", Font.PLAIN, 12);
    protected static Font _hasVotesFont =
        new Font("Dialog", Font.ITALIC, 12);

    protected static final String ICON_ROOT = "/robodj/chooser/images/";

    protected static ImageIcon _playIcon =
        ButtonUtil.getIcon(ICON_ROOT + "play.png");
}
