//
// $Id: Piece.java,v 1.1 2001/07/09 09:27:40 mdb Exp $

package com.samskivert.chuchu;

public class Piece implements Cloneable
{
    /** Compass orientation constant. */
    public static final int NORTH = 0;

    /** Compass orientation constant. */
    public static final int EAST = 1;

    /** Compass orientation constant. */
    public static final int SOUTH = 2;

    /** Compass orientation constant. */
    public static final int WEST = 3;

    /** Compass orientation of this piece. */
    public int orientation;

    /** X coordinate of this piece. */
    public int x;

    /** Y coordinate of this piece. */
    public int y;

    /** The fractional offset of a unit for this piece. */
    public int offset;

    /** Indicates whether this piece is active. */
    public boolean active = true;

    /**
     * Constructs a new piece with the specified orientation and
     * coordinates.
     */
    public Piece (int orientation, int x, int y)
    {
        this.orientation = orientation;
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the board index occupied by this piece.
     */
    public int getIndex ()
    {
        return y * Board.WIDTH + x;
    }

    /**
     * Initializes this piece with the values from the supplied piece.
     */
    public final void init (Piece other)
    {
        this.orientation = other.orientation;
        this.x = other.x;
        this.y = other.y;
        this.offset = 0;
        this.active = true;
    }

    /**
     * Returns a clone of this piece instance.
     */
    public Object clone ()
    {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnse) {
            return null;
        }
    }

    public String toString ()
    {
        return "[orient=" + orientation + ", x=" + x + ", y=" + y +
            ", offset=" + offset + ", active=" + active + "]";
    }

    /**
     * Converts a direction constant into a character suitable for
     * printing in a board description.
     */
    public static char directionToChar (int direction, boolean upper)
    {
        return upper ? UPPER_DIRS[direction] : LOWER_DIRS[direction];
    }

    /**
     * Converts the letters n, e, w, and s into the proper direction code
     * for their respective directions.
     */
    public static int charToDirection (char pos)
    {
        if (pos == 'N' || pos == 'n') {
            return Piece.NORTH;
        } else if (pos == 'E' || pos == 'e') {
            return Piece.EAST;
        } else if (pos == 'W' || pos == 'w') {
            return Piece.WEST;
        } else if (pos == 'S' || pos == 's') {
            return Piece.SOUTH;
        } else {
            String msg = "Invalid direction character '" + pos + "'.";
            throw new RuntimeException(msg);
        }
    }

    protected static final char[] UPPER_DIRS = { 'N', 'E', 'S', 'W' };
    protected static final char[] LOWER_DIRS = { 'n', 'e', 's', 'w' };
}
