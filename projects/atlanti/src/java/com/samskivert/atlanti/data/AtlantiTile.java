//
// $Id: AtlantiTile.java,v 1.7 2001/10/16 17:12:32 mdb Exp $

package com.threerings.venison;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.util.ArrayList;

import com.samskivert.util.IntTuple;

import com.threerings.presents.dobj.DSet;

/**
 * Represents a single tile in play on the Venison game board.
 */
public class VenisonTile
    implements DSet.Element, TileCodes, Cloneable, Comparable
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

    /** An array of claim group values that correspond to the features of
     * this tile. If a piecen has claimed a feature on this tile or that
     * connects to this tile, it will be represented here by a non-zero
     * claim group in the array slot that corresponds to the claimed
     * feature. */
    public int[] claims;

    /** A reference to our static feature descriptions. */
    public int[] features;

    /** A reference to the piecen on this tile or null if no piecen has
     * been placed on this tile. */
    // public Piecen piecen;

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

        // initialize our feature info
        initFeatures();
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
     * Looks for a feature in this tile that matches the supplied feature
     * edge mask and returns the index of that feature in this tile's
     * {@link #claims} array.
     *
     * @return the index of the matching feature or -1 if no feature
     * matched.
     */
    public int getFeatureIndex (int featureMask)
    {
        // translate the feature mask into our orientation
        featureMask = TileUtil.translateMask(featureMask, -orientation);

        for (int i = 0; i < features.length; i += 2) {
            int fmask = features[i+1];
            if ((fmask & featureMask) != 0) {
                return i/2;
            }
        }

        // no match
        return -1;
    }

    /**
     * Looks for a feature in this tile that matches the supplied feature
     * edge mask and returns the claim group to which that feature belongs
     * (which may be zero).
     *
     * @return the claim group to which the feature that matches the
     * supplied mask belongs, or zero if no feature matched the supplied
     * mask.
     */
    public int getFeatureGroup (int featureMask)
    {
        int fidx = getFeatureIndex(featureMask);
        return fidx < 0 ? 0 : claims[fidx];
    }

    /**
     * Sets the claim group for the feature with the specified index. This
     * also updates the claim group for any piecen that was placed on that
     * feature as well.
     *
     * @param featureIndex the index of the feature to update.
     * @param claimGroup the claim group to associate with the feature.
     */
    public void setFeatureGroup (int featureIndex, int claimGroup)
    {
        Log.info("Setting feature group [tile=" + this +
                 ", fidx=" + featureIndex + ", cgroup=" + claimGroup + "].");
        claims[featureIndex] = claimGroup;

        // TBD: update the piecen
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
    public void paint (Graphics2D g, int xoff, int yoff)
    {
        int tidx = type-1;

        // create our shapes if we haven't already
        if (_shapes[tidx][orientation] == null) {
            createShapes();
        }

        // compute our screen coordinates
        int sx = (x + xoff) * TILE_WIDTH;
        int sy = (y + yoff) * TILE_HEIGHT;

        // translate to our screen coordinates
        g.translate(sx, sy);

        // draw our shapes using the proper orientation
        GeneralPath[] paths = _shapes[tidx][orientation];
        IntTuple[] types = _types[tidx];
        for (int i = 0; i < paths.length; i++) {
            if (claims[types[i].right] != 0) {
                g.setColor(CLAIMED_COLOR_MAP[types[i].left]);
            } else {
                g.setColor(COLOR_MAP[types[i].left]);
            }
            g.fill(paths[i]);
        }

        // draw a rectangular outline
        g.setColor(Color.black);
        g.drawRect(0, 0, TILE_WIDTH, TILE_HEIGHT);

        // translate back out
        g.translate(-sx, -sy);
    }

    /**
     * Returns a copy of this Venison tile object.
     */
    public Object clone ()
    {
        return new VenisonTile(type, hasShield, orientation, x, y);
    }

    /**
     * Used to order tiles (which is done by board position).
     */
    public int compareTo (Object other)
    {
        // we will either be compared to another tile or to a coordinate
        // object
        if (other instanceof VenisonTile) {
            VenisonTile tile = (VenisonTile)other;
            return (tile.x == x) ? y - tile.y : x - tile.x;

        } else if (other instanceof IntTuple) {
            IntTuple coord = (IntTuple)other;
            return (coord.left == x) ? y - coord.right : x - coord.left;

        } else {
            // who knows...
            return -1;
        }
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
        initFeatures();
    }

    /**
     * Initializes this tile's feature management data structures.
     */
    protected void initFeatures ()
    {
        if (type > 0) {
            // grab a reference to our feature information
            features = TileUtil.TILE_FEATURES[type-1];

            // create our claims array
            claims = new int[features.length/2];

        } else {
            Log.warning("Requested to init features without valid type " +
                        this + ".");
            Thread.dumpStack();
        }
    }

    /**
     * Creates the path objects that describe the shapes that make up this
     * tile and sticks it into the appropriate slot in the shapes array.
     */
    protected void createShapes ()
    {
        int tidx = type-1;
        ArrayList polys = new ArrayList();
        ArrayList types = new ArrayList();

        // the first feature is the background color
        Object[] features = (Object[])TileUtil.TILE_FEATURE_GEOMS[tidx];
        IntTuple base = (IntTuple)features[0];

        // add a polygon containing the whole tile
        types.add(base);
        Polygon poly = new Polygon();
        poly.addPoint(0, 0);
        poly.addPoint(TILE_WIDTH, 0);
        poly.addPoint(TILE_WIDTH, TILE_HEIGHT);
        poly.addPoint(0, TILE_HEIGHT);
        polys.add(poly);

        // the remainder are tuple/coordinate pairs
        for (int f = 1; f < features.length; f += 2) {
            IntTuple ftype = (IntTuple)features[f];
            int[] coords = (int[])features[f+1];

            // keep track of this shape's type
            types.add(ftype);

            // if this is a road segment, we need to create a special
            // polygon
            if (ftype.left == ROAD) {
                poly = TileUtil.roadSegmentToPolygon(
                    coords[0], coords[1], coords[2], coords[3]);

            } else {
                // otherwise create the polygon directly from the coords
                poly = new Polygon();
                for (int c = 0; c < coords.length; c += 2) {
                    // scale the coords accordingly
                    int fx = (coords[c] * TILE_WIDTH) / 4;
                    int fy = (coords[c+1] * TILE_HEIGHT) / 4;
                    poly.addPoint(fx, fy);
                }
            }

            polys.add(poly);
        }

        // now create general paths from our polygons and convert
        // everything into the appropriate arrays
        GeneralPath[] paths = new GeneralPath[polys.size()];
        for (int i = 0; i < polys.size(); i++) {
            GeneralPath path = new GeneralPath();
            path.append(((Polygon)polys.get(i)).getPathIterator(null), false);
            path.closePath();
            paths[i] = path;
        }

        // keep the first one around
        _shapes[tidx][NORTH] = paths;

        // and rotate it three times to get the other orientations
        AffineTransform xform = new AffineTransform();
        for (int o = 1; o < 4; o++) {
            xform.translate(TILE_WIDTH, 0);
            xform.rotate(Math.PI/2);
            GeneralPath[] rpaths = new GeneralPath[paths.length];
            for (int i = 0; i < paths.length; i++) {
                rpaths[i] = (GeneralPath)paths[i].clone();
                rpaths[i].transform(xform);
            }
            _shapes[tidx][o] = rpaths;
        }

        // also fill in the feature type info
        _types[tidx] = new IntTuple[types.size()];
        types.toArray(_types[tidx]);
    }

    /**
     * Generates a string representation of this tile instance.
     */
    public String toString ()
    {
        return "[type=" + type + ", shield=" + hasShield +
            ", orient=" + ORIENT_NAMES[orientation] +
            ", pos=" + x + "/" + y + "]";
    }

    /** Path objects that describe closed shapes which be used to render
     * the tiles (one for each orientation of each tile type). */
    protected static GeneralPath[][][] _shapes =
        new GeneralPath[TILE_TYPES][4][];

    /** The feature type of each shape in shapes array. */
    protected static IntTuple[][] _types = new IntTuple[TILE_TYPES][];

    /** Maps feature types to colors. */
    protected static Color[] COLOR_MAP = {
        Color.red, // CITY
        Color.green, // GRASS
        Color.black, // ROAD
        Color.yellow // CLOISTER
    };

    /** Maps feature types to colors for claimed features. */
    protected static Color[] CLAIMED_COLOR_MAP = {
        Color.red.darker(), // CITY
        Color.green.darker(), // GRASS
        Color.black.brighter(), // ROAD
        Color.yellow.darker(), // CLOISTER
    };
}
