//
// $Id: FancyPanel.java,v 1.1 2004/01/26 16:10:55 mdb Exp $

package robodj.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Shape;

import com.samskivert.swing.ScrollablePanel;

/**
 * A {@link JPanel} that displays an image in its background.
 */
public class FancyPanel extends ScrollablePanel
{
    public FancyPanel (Image image)
    {
        _image = image;
    }

    public FancyPanel (LayoutManager lmgr, Image image)
    {
        super(lmgr);
        _image = image;
    }

    public void paintComponent (Graphics gfx)
    {
        int x = 0, y = 0;
        int width = this.getWidth();
        int height = this.getHeight();
        int iwidth = _image.getWidth(null),
            iheight = _image.getHeight(null);
        int xnum = width / iwidth, xplus = width % iwidth;
        int ynum = height / iheight, yplus = height % iheight;
        Shape oclip = gfx.getClip();

        for (int ii=0; ii < ynum; ii++) {
            // draw the full copies of the image across
            int xx = x;
            for (int jj=0; jj < xnum; jj++) {
                gfx.drawImage(_image, xx, y, null);
                xx += iwidth;
            }

            if (xplus > 0) {
                gfx.clipRect(xx, y, xplus, iheight);
                gfx.drawImage(_image, xx, y, null);
                gfx.setClip(oclip);
            }

            y += iheight;
        }

        if (yplus > 0) {
            int xx = x;
            for (int jj=0; jj < xnum; jj++) {
                gfx.clipRect(xx, y, iwidth, yplus);
                gfx.drawImage(_image, xx, y, null);
                gfx.setClip(oclip);
                xx += iwidth;
            }

            if (xplus > 0) {
                gfx.clipRect(xx, y, xplus, yplus);
                gfx.drawImage(_image, xx, y, null);
                gfx.setClip(oclip);
            }
        }

        super.paintComponent(gfx);
    }

    protected Image _image;
}
