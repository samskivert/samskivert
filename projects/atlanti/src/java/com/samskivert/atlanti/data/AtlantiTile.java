//
// $Id: AtlantiTile.java,v 1.1 2001/10/10 03:35:02 mdb Exp $

package com.threerings.venison;

import java.awt.Graphics;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.cocktail.cher.dobj.DSet;

/**
 * Represents a single tile in play on the Venison game board.
 */
public class VenisonTile
    implements DSet.Element, VenisonTileCodes
{
    /** The starting tile. */
    public static final VenisonTile STARTING_TILE =
        new VenisonTile(CITY_ONE_ROAD_STRAIGHT, false, NORTH, 0, 0);

    /** The tile type. */
    public int type;

    /** Whether this tile has a shield on it. */
    public boolean hasShield;

    /** The tile's orientation. */
    public int orientation;

    /** The tile's x and y coordinates. */
    public int x,  y;

    /**
     * Constructs a tile with all of the supplied tile information.
     */
    public VenisonTile (int type, boolean hasShield, int orientation,
                        int x, int y)
    {
        this.type = type;
        this.hasShield = hasShield;
        this.orientation = orientation;
        this.x = x;
        this.y = y;
    }

    /**
     * Constructs a blank tile, suitable for unserialization.
     */
    public VenisonTile ()
    {
        // nothing doing
    }

    /**
     * Paints this tile to the specified graphics context at its assigned
     * location, accounting for the supplied x and y offsets of the
     * origin.
     *
     * @param g the graphics context to use when painting the tile.
     * @param xoff the offset (in tile units) of the origin in the x
     * direction.
     * @param yoff the offset (in tile units) of the origin in the y
     * direction.
     */
    public void paint (Graphics g, int xoff, int yoff)
    {
        // compute our screen coordinates
        int sx = (x + xoff) * TILE_WIDTH;
        int sy = (y + yoff) * TILE_HEIGHT;

        // draw a rectangle
        g.drawRect(sx, sy, TILE_WIDTH, TILE_HEIGHT);

        // and draw our tile id in the middle for now (we'll eventually
        // draw tile images)
        g.drawString(type + "/" + x + "/" + y, sx + 20, sy + 20);
    }

    // documentation inherited
    public Object getKey ()
    {
        // our key is our coordinates conflated into one integer
        return new Integer((x + 128) * 256 + y + 128);
    }

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        out.writeInt(type);
        out.writeBoolean(hasShield);
        out.writeInt(orientation);
        out.writeInt(x);
        out.writeInt(y);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        type = in.readInt();
        hasShield = in.readBoolean();
        orientation = in.readInt();
        x = in.readInt();
        y = in.readInt();
    }
}
