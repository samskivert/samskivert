//
// $Id: AtlantiBoard.java,v 1.9 2001/10/17 04:34:14 mdb Exp $

package com.threerings.venison;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.samskivert.swing.Controller;
import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.CollectionUtil;

import com.threerings.presents.dobj.DSet;

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
     * Sets the piecen color to use when creating new piecens.
     */
    public void setNewPiecenColor (int color)
    {
        _newPiecenColor = color;
    }

    /**
     * Sets the tiles to be displayed by this board. Any previous tiles
     * are forgotten and the new tiles are initialized according to their
     * geometry to set up initial claim groups.
     *
     * @param tset the set of {@link VenisonTile} objects to be displayed
     * by this board.
     */
    public void setTiles (DSet tset)
    {
        // clear out our old tiles list
        _tiles.clear();

        // copy the tiles from the set into our local list
        CollectionUtil.addAll(_tiles, tset.elements());

        // sort the list
        Collections.sort(_tiles);

        // recompute our desired dimensions and then have our parent
        // adjust to our changed size
        computeDimensions();
    }

    /**
     * Sets the piecens to be placed on the appropriate tiles of the
     * board. This should only be done when first entering the game room
     * and subsequent piecen placement should be done via {@link
     * #placePiecen}.
     */
    public void setPiecens (DSet piecens)
    {
        //  just iterate over the set placing each of the piecens in turn
        Iterator iter = piecens.elements();
        while (iter.hasNext()) {
            placePiecen((Piecen)iter.next());
        }
    }

    /**
     * Instructs the board to add the specified tile to the display. The
     * tile will have its claims inherited accordingly.
     */
    public void addTile (VenisonTile tile)
    {
        Log.info("Adding tile to board " + tile + ".");

        // if we add a tile that is the same as our most recently placed
        // tile, leave the placed tile. otherwise clear it out
        if (!tile.equals(_placedTile)) {
            _placedTile = null;
        }

        // add the tile
        _tiles.add(tile);

        // resort the list
        Collections.sort(_tiles);

        // have the new tile inherit its claim groups
        TileUtil.inheritClaims(_tiles, tile);

        // recompute our desired dimensions and then have our parent
        // adjust to our changed size
        computeDimensions();
    }

    /**
     * Places the specified piecen on the appropriate tile and updates
     * claim groups as necessary.
     */
    public void placePiecen (Piecen piecen)
    {
        // if we still have a placed tile, we get rid of it
        _placedTile = null;

        Log.info("Placing " + piecen + ".");

        // locate the tile associated with this piecen
        int tidx = _tiles.indexOf(piecen);
        if (tidx != -1) {
            VenisonTile tile = (VenisonTile)_tiles.get(tidx);
            // set the piecen on the tile (supplying our tile list so that
            // the necessary claim group adjustments can be made)
            tile.setPiecen(piecen, _tiles);
            // and repaint
            repaint();

        } else {
            Log.warning("Requested to place piecen for which we could " +
                        "find no associated tile! [piecen=" + piecen + "].");
        }
    }

    /**
     * When a piecen is removed (after scoring it), this method should be
     * called to update the board display.
     */
    public void clearPiecen (Object key)
    {
        // locate the tile associated with this piecen key
        int tsize = _tiles.size();
        for (int i = 0; i < tsize; i++) {
            VenisonTile tile = (VenisonTile)_tiles.get(i);
            if (tile.getKey().equals(key)) {
                // clear the piecen out of the tile
                tile.clearPiecen();
                // and repaint
                repaint();
                // and get on out
                return;
            }
        }

        Log.warning("Requested to clear piecen for which we could " +
                    "find no associated tile! [key=" + key + "].");
    }

    /**
     * Sets the tile to be placed on the board. The tile will be displayed
     * in the square under the mouse cursor where it can be legally placed
     * and its orientation will be determined based on the pointer's
     * proximity to the edges of the target square. When the user clicks
     * the mouse while the tile is in a placeable position, a
     * <code>TILE_PLACED</code> command will be dispatched to the
     * controller in scope. The coordinates and orientation of the tile
     * will be available by fetching the tile back via {@link
     * #getPlacedTile}. The tile provided to this method will not be
     * modified.
     *
     * @param tile the new tile to be placed or null if no tile is to
     * currently be placed.
     */
    public void setTileToBePlaced (VenisonTile tile)
    {
        // make a copy of this tile so that we can play with it
        _placingTile = (VenisonTile)tile.clone();;

        // update our internal state based on this new placing tile
        if (_placingTile != null) {
            updatePlacingInfo(true);
        }

        // and repaint
        repaint();
    }

    /**
     * Returns the last tile placed by the user.
     */
    public VenisonTile getPlacedTile ()
    {
        return _placedTile;
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
        Graphics2D g2 = (Graphics2D)g;

        // center the tile display if we are bigger than we need to be
        g.translate(_tx, _ty);

        // iterate over our tiles, painting each of them
        int tsize = _tiles.size();
        for (int i = 0; i < tsize; i++) {
            VenisonTile tile = (VenisonTile)_tiles.get(i);
            tile.paint(g2, _origX, _origY);
        }

        // if we have a placing tile, draw that one as well
        if (_placingTile != null && _validPlacement) {
            // if the current position is valid, draw the placing tile
            _placingTile.paint(g2, _origX, _origY);

            // draw a green rectangle around the placing tile
            g.setColor(Color.blue);
            int sx = (_placingTile.x + _origX) * TILE_WIDTH;
            int sy = (_placingTile.y + _origY) * TILE_HEIGHT;
            g.drawRect(sx, sy, TILE_WIDTH, TILE_HEIGHT);
        }

        // if we have a recently placed tile, draw that one as well
        if (_placedTile != null) {
            // draw the tile
            _placedTile.paint(g2, _origX, _origY);

            // draw a white rectangle around the placed tile
            g.setColor(Color.white);
            int sx = (_placedTile.x + _origX) * TILE_WIDTH;
            int sy = (_placedTile.y + _origY) * TILE_HEIGHT;
            g.drawRect(sx, sy, TILE_WIDTH, TILE_HEIGHT);
        }

        // undo our translations
        g.translate(-_tx, -_ty);

        g.setColor(Color.black);
        g2.draw(getBounds());
    }

    /** Called by our adapter when the mouse moves. */
    protected void mouseMoved (MouseEvent evt)
    {
        // we always want to know about our last mouse coordinates
        _mouseX = evt.getX() - _tx;
        _mouseY = evt.getY() - _ty;

        if (_placingTile != null) {
            // if we have a tile to be placed, update its coordinates
            if (updatePlacingInfo(false)) {
                repaint();
            }

        } else if (_placedTile != null && _placingPiecen) {
            // if we have a recently placed tile, we're doing piecen
            // placement; first convert the mouse coords into tile coords
            int mx = _mouseX - (_placedTile.x + _origX) * TILE_WIDTH;
            int my = _mouseY - (_placedTile.y + _origY) * TILE_HEIGHT;
            boolean changed = false;

            // now see if we're inside the placing tile
            if (mx >= 0 && mx < TILE_WIDTH && my >= 0 && my < TILE_HEIGHT) {
                int fidx = _placedTile.getFeatureIndex(mx, my);

                // if the feature is not already claimed, we can put a
                // piece there to indicate that it can be claimed
                if (_placedTile.claims[fidx] == 0) {
                    if (_placedTile.piecen == null ||
                        _placedTile.piecen.featureIndex != fidx) {
                        Piecen p = new Piecen(_newPiecenColor, 0, 0, fidx);
                        _placedTile.setPiecen(p, null);
                        changed = true;
                    }

                } else {
                    // we may need to clear out a piecen since we've moved
                    if (_placedTile.piecen != null) {
                        _placedTile.clearPiecen();
                        changed = true;
                    }
                }

            } else {
                // we may need to clear out a piecen since we've moved
                if (_placedTile.piecen != null) {
                    _placedTile.clearPiecen();
                    changed = true;
                }
            }

            if (changed) {
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
            // move the placing tile to the placed tile
            _placedTile = _placingTile;
            _placingTile = null;

            // inherit claims on the placed tile
            TileUtil.inheritClaims(_tiles, _placedTile);

            // post the action
            Controller.postAction(this, TILE_PLACED);

            // move into placing piecen mode
            _placingPiecen = true;

            // recompute our dimensions (which will relayout or repaint)
            computeDimensions();

        } else if (_placingPiecen && _placedTile != null &&
                   _placedTile.piecen != null) {
            // clear out placing piecen mode
            _placingPiecen = false;

            // post the action
            Controller.postAction(this, PIECEN_PLACED);
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
            _validOrients = TileUtil.computeValidOrients(_tiles, _placingTile);

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
        if (_tiles.size() == 0) {
            return new Dimension(100, 100);

        } else {
            return new Dimension(TILE_WIDTH * _width, TILE_HEIGHT * _height);
        }
    }

    /**
     * Determines how big we want to be based on where the tiles have been
     * laid out. This will cause the component to be re-layed out if the
     * dimensions change or repainted if not.
     */
    protected void computeDimensions ()
    {
        int maxX = 0, maxY = 0;
        int minX = 0, minY = 0;

        // if we have a recently placed tile, start with that one
        if (_placedTile != null) {
            minX = maxX = _placedTile.x;
            minY = maxY = _placedTile.y;
        }

        // figure out what our boundaries are
        int tsize = _tiles.size();
        for (int i = 0; i < tsize; i++) {
            VenisonTile tile = (VenisonTile)_tiles.get(i);
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

        // spread our bounds out by one
        minX -= 1; minY -= 1;
        maxX += 1; maxY += 1;

        // keep track of these to know if we've change dimensions
        int oldOrigX = _origX, oldOrigY = _origY;
        int oldWidth = _width, oldHeight = _height;

        // now we can compute our width and the origin offset
        _origX = -minX;
        _origY = -minY;
        _width = maxX - minX + 1;
        _height = maxY - minY + 1;

        if (_origX != oldOrigX || _origY != oldOrigY ||
            oldWidth != _width || oldHeight != _height) {
            // if the dimensions changed, we need to relayout
            revalidate();

        } else {
            // otherwise just repaint
            repaint();
        }
    }

    /** Test code. */
    public static void main (String[] args)
    {
        JFrame frame = new JFrame("Board test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        VenisonBoard board = new VenisonBoard();

        TestDSet set = new TestDSet();
        set.addTile(new VenisonTile(CITY_TWO, true, WEST, 0, 0));
        set.addTile(new VenisonTile(CITY_TWO, false, WEST, -1, 1));
        set.addTile(new VenisonTile(CITY_ONE, false, SOUTH, -1, -1));
        VenisonTile zero = new VenisonTile(CURVED_ROAD, false, WEST, 0, 2);
        set.addTile(zero);
        VenisonTile one = new VenisonTile(TWO_CITY_TWO, false, NORTH, 0, 1);
        set.addTile(one);
        set.addTile(new VenisonTile(CITY_THREE, false, WEST, 1, 1));
        set.addTile(new VenisonTile(CITY_THREE_ROAD, false, EAST, 1, 2));
        set.addTile(new VenisonTile(CITY_THREE, false, NORTH, -1, 0));
        VenisonTile two = new VenisonTile(CITY_ONE, false, EAST, -2, 0);
        set.addTile(two);
        board.setTiles(set);

        VenisonTile placing = new VenisonTile(CITY_TWO, false, NORTH, 0, 0);
        board.setTileToBePlaced(placing);

        // set a feature group to test propagation
        List tiles = new ArrayList();
        CollectionUtil.addAll(tiles, set.elements());
        Collections.sort(tiles);

        zero.setPiecen(new Piecen(Piecen.GREEN, 0, 0, 2), tiles);
        one.setPiecen(new Piecen(Piecen.BLUE, 0, 0, 0), tiles);
        two.setPiecen(new Piecen(Piecen.RED, 0, 0, 1), tiles);

        Log.info("Incomplete road: " +
                 TileUtil.computeFeatureScore(tiles, zero, 2));

        Log.info("Completed city: " +
                 TileUtil.computeFeatureScore(tiles, two, 1));

        Log.info("Incomplete city: " +
                 TileUtil.computeFeatureScore(tiles, one, 2));

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
    protected ArrayList _tiles = new ArrayList();

    /** The tile currently being placed by the user. */
    protected VenisonTile _placingTile;

    /** The last tile being placed by the user. */
    protected VenisonTile _placedTile;

    /** A flag indicating whether or not we're placing a piecen. */
    protected boolean _placingPiecen = false;

    /** Whether or not the current position and orientation of the placing
     * tile is valid. */
    protected boolean _validPlacement = false;

    /** An array indicating which of the four directions are valid
     * placements based on the current position of the placing tile. */
    protected boolean[] _validOrients;

    /** The color to use when creating new piecens. */
    protected int _newPiecenColor = Piecen.BLUE;

    /** Our render offset in pixels. */
    protected int _tx, _ty;

    /** The offset in tile coordinates of the origin. */
    protected int _origX, _origY;

    /** The width and height of the board in tile coordinates. */
    protected int _width, _height;

    /** The last known position of the mouse. */
    protected int _mouseX, _mouseY;
}
