//
// $Id: TileGeometryTest.java,v 1.4 2001/12/18 11:58:53 mdb Exp $

package com.threerings.venison;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.samskivert.swing.util.SwingUtil;

import com.threerings.resource.ResourceManager;
import com.threerings.media.ImageManager;
import com.threerings.media.tile.TileManager;

/**
 * A simple class for testing the tile geometry specifications by drawing
 * them.
 */
public class TileGeometryTest
    extends JPanel implements TileCodes
{
    public TileGeometryTest ()
    {
        for (int i = 0; i < TILE_TYPES; i++) {
            _tiles[i] = new VenisonTile(i+1, true, NORTH, i % 5, i / 5);
        }
    }

    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        // paint our tiles
        for (int i = 0; i < _tiles.length; i++) {
            _tiles[i].paint((Graphics2D)g, 0, 0);
        }
    }

    public Dimension getPreferredSize ()
    {
        // we want to be five tiles wide by four tiles tall
        return new Dimension(TILE_WIDTH * 5, TILE_HEIGHT * 4);
    }

    public static void main (String[] args)
    {
        JFrame frame = new JFrame("Tile geometry test");

        ResourceManager rmgr = new ResourceManager(null, "rsrc");
        ImageManager imgr = new ImageManager(rmgr, frame);
        TileManager tmgr = new TileManager(imgr);

        VenisonTile.setTileManager(tmgr);
        VenisonTile.piecenDebug = true;
        PiecenUtil.init(tmgr);

        // quit if we're closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        TileGeometryTest panel = new TileGeometryTest();
        frame.getContentPane().add(panel);
        frame.pack();
        SwingUtil.centerWindow(frame);
        frame.show();
    }

    protected VenisonTile[] _tiles = new VenisonTile[TILE_TYPES];
}
