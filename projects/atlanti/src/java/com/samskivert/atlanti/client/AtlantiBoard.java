//
// $Id: AtlantiBoard.java,v 1.1 2001/10/10 03:35:02 mdb Exp $

package com.threerings.venison;

import java.awt.*;
import javax.swing.*;
import java.util.Iterator;

import com.samskivert.swing.util.SwingUtil;

import com.threerings.cocktail.cher.dobj.DSet;

/**
 * Displays the tiles that make up the Venison board.
 */
public class VenisonBoard
    extends JPanel implements VenisonTileCodes
{
    /**
     * Sets the tiles that are displayed by this board. Causes the board
     * to recompute its size based on the new tile layout.
     *
     * @param tiles the set of {@link VenisonTile} objects to be displayed
     * by this board.
     */
    public void setTiles (DSet tiles)
    {
        _tiles = tiles;

        // we need to recompute our desired dimensions and then have our
        // parent adjust to our changed size
        computeDimensions();
        revalidate();
    }

    // documentation inherited
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        // center the tile display if we are bigger than we need to be
        int tx = (getWidth() - TILE_WIDTH * _width)/2;
        int ty = (getHeight() - TILE_HEIGHT * _height)/2;
        g.translate(tx, ty);

        // iterate over our tiles, painting each of them
        Iterator iter = _tiles.elements();
        while (iter.hasNext()) {
            VenisonTile tile = (VenisonTile)iter.next();
            tile.paint(g, _origX, _origY);
        }

        // undo our translations
        g.translate(-tx, -ty);
    }

    // documentation inherited
    public Dimension getPreferredSize ()
    {
        if (_tiles == null) {
            return new Dimension(100, 100);

        } else {
            return new Dimension(TILE_WIDTH * _width, TILE_HEIGHT * _height);
        }
    }

    /**
     * Determines how big we want to be based on where the tiles have been
     * laid out.
     */
    protected void computeDimensions ()
    {
        int maxX = 0, maxY = 0;
        int minX = 0, minY = 0;

        // figure out what our boundaries are
        Iterator iter = _tiles.elements();
        while (iter.hasNext()) {
            VenisonTile tile = (VenisonTile)iter.next();
            if (tile.x > maxX) {
                maxX = tile.x;
            } else if (tile.x < minX) {
                minX = tile.x;
            }
            if (tile.y > maxY) {
                maxY = tile.y;
            } else if (tile.y < minY) {
                minY = tile.y;
            }
        }

        // now we can compute our width and the origin offset
        _origX = -minX;
        _origY = -minY;
        _width = maxX - minX + 1;
        _height = maxY - minY + 1;
    }

    /** Test code. */
    public static void main (String[] args)
    {
        JFrame frame = new JFrame("Board test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        VenisonBoard board = new VenisonBoard();

        TestDSet set = new TestDSet();
        set.addTile(new VenisonTile(CITY_FOUR, false, NORTH, 0, 1));
        set.addTile(new VenisonTile(CITY_THREE, false, NORTH, 1, 1));
        set.addTile(new VenisonTile(CITY_THREE_ROAD, false, NORTH, 1, 2));
        set.addTile(new VenisonTile(CITY_THREE, false, NORTH, -1, 0));
        set.addTile(new VenisonTile(CITY_FOUR, false, NORTH, -2, 0));
        board.setTiles(set);

        frame.getContentPane().add(board, BorderLayout.CENTER);
        frame.pack();
        SwingUtil.centerWindow(frame);
        frame.show();
    }

    protected static class TestDSet extends DSet
    {
        public TestDSet ()
        {
            super(VenisonTile.class);
        }

        public void addTile (VenisonTile tile)
        {
            add(tile);
        }
    }

    /** A reference to our tile set. */
    protected DSet _tiles;

    /** The offset in tile coordinates of the origin. */
    protected int _origX, _origY;

    /** The width and height of the board in tile coordinates. */
    protected int _width, _height;
}
