//
// $Id: Piecen.java,v 1.2 2001/10/17 23:27:52 mdb Exp $

package com.threerings.venison;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.presents.dobj.DSet;

/**
 * A piecen is a person and a piece all rolled into one! Players can play
 * a single piecen on a tile feature as a part of their turn and that
 * piecen then claims that feature and all of the features that are
 * connected to it forming a claim group. Once a group of features has
 * been claimed, no further piecens can be placed on the group. Note,
 * however, that two or more separately claimed feature groups can be
 * joined by placing a tile that connects the previously disconnected
 * groups together. In that case, the groups are merged into a single
 * claim group and all the piecens that were part of the disparate groups
 * become claimees in the new group. At scoring time, the player with the
 * most piecens in a group gets the points for the group. If two or more
 * players have equal numbers of piecens in a group, they each get the
 * points for the group.
 */
public class Piecen
    implements DSet.Element
{
    /** A color constant. */
    public static final int RED = 0;

    /** A color constant. */
    public static final int BLACK = 1;

    /** A color constant. */
    public static final int BLUE = 2;

    /** A color constant. */
    public static final int YELLOW = 3;

    /** A color constant. */
    public static final int GREEN = 4;

    /** The owner of this piecen. */
    public int owner;

    /** The x and y coordinates of the tile on which this piecen is
     * placed. */
    public int x,  y;

    /** The index in the tile's feature array of the feature on which this
     * piecen is placed. */
    public int featureIndex;

    /** The claim group to which this piecen belongs. */
    public int claimGroup;

    /**
     * Construts a piecen with the specified configuration.
     */
    public Piecen (int owner, int x, int y, int featureIndex)
    {
        this.owner = owner;
        this.x = x;
        this.y = y;
        this.featureIndex = featureIndex;
    }

    /**
     * Constructs a blank piecen, suitable for unserialization.
     */
    public Piecen ()
    {
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
        out.writeInt(owner);
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(featureIndex);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        owner = in.readInt();
        x = in.readInt();
        y = in.readInt();
        featureIndex = in.readInt();
    }

    // documentation inherited
    public boolean equals (Object other)
    {
        // we will either be compared to a tile or to a piecen
        if (other == null) {
            return false;

        } else if (other instanceof VenisonTile) {
            VenisonTile tile = (VenisonTile)other;
            return (tile.x == x) ? (y == tile.y) : false;

        } else if (other instanceof Piecen) {
            Piecen piecen = (Piecen)other;
            return (piecen.x == x) ? (y == piecen.y) : false;

        } else {
            // who knows...
            return false;
        }
    }

    /**
     * Generates a string representation of this piecen.
     */
    public String toString ()
    {
        return "[owner=" + owner + ", pos=" + x + "/" + y +
            ", feat=" + featureIndex + ", claim=" + claimGroup + "]";
    }
}
