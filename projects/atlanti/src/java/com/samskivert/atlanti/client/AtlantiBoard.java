//
// $Id: AtlantiBoard.java,v 1.4 2001/10/12 20:34:13 mdb Exp $

package com.threerings.venison;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Iterator;

import com.samskivert.swing.Controller;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.presents.dobj.DSet;

import com.threerings.venison.Log;

/**
 * Displays the tiles that make up the Venison board.
 */
public class VenisonBoard
    extends JPanel implements TileCodes, VenisonCodes
{
    /** The command posted when a tile is placed by the user on the
     * board. */
    public static final String TILE_PLACED_CMD = "tile_placed";

    /**
     * Constructs a Venison board.
     */
    public VenisonBoard ()
    {
        // create mouse adapters that will let us know when interesting
        // mouse events happen
        addMouseListener(new MouseAdapter() {
            public void mouseClicked (MouseEvent evt) {
                VenisonBoard.this.mouseClicked(evt);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved (MouseEvent evt) {
                VenisonBoard.this.mouseMoved(evt);
            }
        });
    }

    /**
     * Sets the tiles that are displayed by this board. Causes the board
     * to recompute its size based on the new tile layout.
     *
     * @param tiles the set of {@link VenisonTile} objects to be displayed
     * by this board.
     */
    public void setTiles (DSet tiles)
    {
        // grab the new tiles
        _tiles = tiles;

        // update our display
        refreshTiles();
    }

    /**
     * Instructs the board to refresh its display in case changes have
     * occurred in the tiles set.
     */
    public void refreshTiles ()
    {
        Log.info("Refreshing tiles " + _tiles + ".");

        // recompute our desired dimensions and then have our parent
        // adjust to our changed size
        if (_tiles != null) {
            computeDimensions();
        }

        // we may need to revalidate if our dimensions changed
        revalidate();

        // we also have to repaint in case our dimensions didn't change
        repaint();
    }

    /**
     * Sets the tile to be placed on the board. The tile will be displayed
     * in the square under the mouse cursor where it can be legally placed
     * and its orientation will be determined based on the pointer's
     * proximity to the edges of the target square. When the user clicks
     * the mouse while the tile is in a placeable position, a
     * <code>TILE_PLACED</code> command will be dispatched to the
     * controller in scope. The coordinates and orientation of the tile
     * will have been set to the values specified by the user.
     *
     * @param tile the new tile to be placed or null if no tile is to
     * currently be placed.
     */
    public void setTileToBePlaced (VenisonTile tile)
    {
        _placingTile = tile;
        // update our internal state based on this new placing tile
        if (_placingTile != null) {
            updatePlacingInfo(true);
        }
        // and repaint
        repaint();
    }

    // documentation inherited
    public void layout ()
    {
        super.layout();

        // compute our translation coordinates based on our size
        _tx = (getWidth() - TILE_WIDTH * _width)/2;
        _ty = (getHeight() - TILE_HEIGHT * _height)/2;
    }

    // documentation inherited
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        // center the tile display if we are bigger than we need to be
        g.translate(_tx, _ty);

        // iterate over our tiles, painting each of them
        if (_tiles != null) {
            Iterator iter = _tiles.elements();
            while (iter.hasNext()) {
                VenisonTile tile = (VenisonTile)iter.next();
                tile.paint(g, _origX, _origY);
            }
        }

        // if we have a placing tile, draw that one as well
        if (_placingTile != null && _validPlacement) {
            // if the current position is valid, draw the placing tile
            _placingTile.paint(g, _origX, _origY);

            // draw a green rectangle around the placing tile
            g.setColor(Color.green);
            int sx = (_placingTile.x + _origX) * TILE_WIDTH;
            int sy = (_placingTile.y + _origY) * TILE_HEIGHT;
            g.drawRect(sx, sy, TILE_WIDTH, TILE_HEIGHT);
        }

        // undo our translations
        g.translate(-_tx, -_ty);
    }

    /** Called by our adapter when the mouse moves. */
    protected void mouseMoved (MouseEvent evt)
    {
        // we always want to know about our last mouse coordinates
        _mouseX = evt.getX() - _tx;
        _mouseY = evt.getY() - _ty;

        // if we have a tile to be placed, update its coordinates
        if (_placingTile != null) {
            if (updatePlacingInfo(false)) {
                repaint();
            }
        }
    }

    /** Called by our adapter when the mouse is clicked. */
    protected void mouseClicked (MouseEvent evt)
    {
        // if we have a placing tile and it's in a valid position, we want
        // to dispatch an action letting the controller know that the user
        // placed it
        if (_placingTile != null && _validPlacement) {
            // clear out the placing tile
            _placingTile = null;

            // post the action
            Controller.postAction(this, TILE_PLACED);

            // and repaint
            repaint();
        }
    }

    /**
     * Updates the coordinates and orientation of the placing tile based
     * on the last known coordinates of the mouse and returns true if the
     * coordinates or orientation changed from their previous values.
     */
    protected boolean updatePlacingInfo (boolean force)
    {
        boolean changed = false;

        // convert mouse coordinates into tile coordinates and offset them
        // by the origin
        int x = divFloor(_mouseX, TILE_WIDTH) - _origX;
        int y = divFloor(_mouseY, TILE_HEIGHT) - _origY;

        // if these are different than the values currently in the placing
        // tile, update the tile coordinates
        if (_placingTile.x != x || _placingTile.y != y || force) {
            // update the coordinates of the tile
            _placingTile.x = x;
            _placingTile.y = y;

            // we've changed the display, so make a note of it
            changed = true;

            // we also need to recompute the valid orientations for the
            // tile in this new position
            _validOrients = TileUtil.computeValidOrients(
                _tiles.elements(), _placingTile);

            // if we've changed positions, clear out our valid placement
            // flag
            _validPlacement = false;
        }

        // determine if we should change the orientation based on the
        // position of the mouse within the tile boundaries
        int rx = _mouseX % TILE_WIDTH;
        int ry = _mouseY % TILE_HEIGHT;
        int orient = coordToOrient(rx, ry);

        // scan for a legal orientation that is closest to our desired
        // orientation
        for (int i = 0; i < 4; i++) {
            int candOrient = (orient+i)%4;
            if (_validOrients[candOrient]) {
                if (_placingTile.orientation != candOrient) {
                    _placingTile.orientation = candOrient;
                    changed = true;
                }
                _validPlacement = true;
                break;
            }
        }

        return changed;
    }

    /**
     * Converts mouse coordinates which are relative to a particular tile,
     * into an orientation based on the position within that tile. A tile
     * is divided up into four quadrants by lines connecting its four
     * corners. If the tile is in a quadrant closes to an edge, it is
     * converted to the orientation corresponding with that edge.
     *
     * @param rx the mouse coordinates modulo tile width.
     * @param ry the mouse coordinates modulo tile height.
     *
     * @return the orientation desired for the tile in which the mouse
     * resides.
     */
    protected int coordToOrient (int rx, int ry)
    {
        if (rx > ry) {
            if (rx > (TILE_HEIGHT - ry)) {
                return EAST;
            } else {
                return NORTH;
            }
        } else {
            if (rx > (TILE_HEIGHT - ry)) {
                return SOUTH;
            } else {
                return WEST;
            }
        }
    }

    /** Divides the two integers returning the floor of the divided value
     * rather than its truncation. */
    protected static int divFloor (int value, int divisor)
    {
        return (int)Math.floor((double)value/divisor);
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
        if (_tiles != null) {
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
        }

        // spread our bounds out by one
        minX -= 1; minY -= 1;
        maxX += 1; maxY += 1;

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
        set.addTile(new VenisonTile(CITY_TWO, false, WEST, 0, 0));
        set.addTile(new VenisonTile(CITY_FOUR, false, NORTH, 0, 1));
        set.addTile(new VenisonTile(CITY_THREE, false, NORTH, 1, 1));
        set.addTile(new VenisonTile(CITY_THREE_ROAD, false, NORTH, 1, 2));
        set.addTile(new VenisonTile(CITY_THREE, false, NORTH, -1, 0));
        set.addTile(new VenisonTile(CITY_FOUR, false, NORTH, -2, 0));
        board.setTiles(set);

        VenisonTile placing = new VenisonTile(CITY_TWO, false, NORTH, 0, 0);
        board.setTileToBePlaced(placing);

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

    /** The tile currently being placed by the user. */
    protected VenisonTile _placingTile;

    /** Whether or not the current position and orientation of the placing
     * tile is valid. */
    protected boolean _validPlacement = false;

    /** An array indicating which of the four directions are valid
     * placements based on the current position of the placing tile. */
    protected boolean[] _validOrients;

    /** Our render offset in pixels. */
    protected int _tx, _ty;

    /** The offset in tile coordinates of the origin. */
    protected int _origX, _origY;

    /** The width and height of the board in tile coordinates. */
    protected int _width, _height;

    /** The last known position of the mouse. */
    protected int _mouseX, _mouseY;
}
