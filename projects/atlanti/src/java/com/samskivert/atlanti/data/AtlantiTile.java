//
// $Id: AtlantiTile.java,v 1.4 2001/10/12 20:34:13 mdb Exp $

package com.threerings.venison;

import java.awt.Color;
import java.awt.Graphics;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.presents.dobj.DSet;

/**
 * Represents a single tile in play on the Venison game board.
 */
public class VenisonTile
    implements DSet.Element, TileCodes, Cloneable
{
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
     * Constructs a tile with the type information set, but in the default
     * <code>NORTH</code> orientation and with no position.
     */
    public VenisonTile (int type, boolean hasShield)
    {
        this(type, hasShield, NORTH, 0, 0);
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
        g.setColor(Color.black);
        g.drawRect(sx, sy, TILE_WIDTH, TILE_HEIGHT);

        // and draw our tile id in the middle for now (we'll eventually
        // draw tile images)
        String txt = type + "/" + x + "/" + y + "/" +
            ORIENT_NAMES[orientation];
        g.drawString(txt, sx + 20, sy + 20);

        // draw little dots indicating the color of our sides
        for (int i = 0; i < 4; i++) {
            // adjust for our orientation
            switch (TileUtil.getEdge(type, (i+(4-orientation))%4)) {
            case GRASS:
                g.setColor(Color.green);
                break;
            case ROAD:
                g.setColor(Color.white);
                break;
            case CITY:
                g.setColor(Color.red);
                break;
            }
            switch (i) {
            case NORTH:
                g.fillOval(sx + TILE_WIDTH/2 - 2, sy + 2, 4, 4);
                break;
            case EAST:
                g.fillOval(sx + TILE_WIDTH - 6, sy + TILE_HEIGHT/2 - 2, 4, 4);
                break;
            case SOUTH:
                g.fillOval(sx + TILE_WIDTH/2 - 2, sy + TILE_HEIGHT - 6, 4, 4);
                break;
            case WEST:
                g.fillOval(sx + 2, sy + TILE_HEIGHT/2 - 2, 4, 4);
                break;
            }
        }
    }

    /**
     * Returns a copy of this Venison tile object.
     */
    public Object clone ()
    {
        return new VenisonTile(type, hasShield, orientation, x, y);
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

    public String toString ()
    {
        return "[type=" + type + ", shield=" + hasShield +
            ", orient=" + ORIENT_NAMES[orientation] +
            ", pos=" + x + "/" + y + "]";
    }
}
