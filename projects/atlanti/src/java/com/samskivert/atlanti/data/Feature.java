//
// $Id: Feature.java,v 1.6 2002/12/12 05:51:54 mdb Exp $

package com.samskivert.atlanti.data;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

import com.samskivert.util.StringUtil;

import com.samskivert.atlanti.Log;
import com.samskivert.atlanti.util.FeatureUtil;
import com.samskivert.atlanti.util.TileUtil;

/**
 * Represents all of the necessary information for a particular feature of
 * a particular tile.
 */
public class Feature
    implements TileCodes
{
    /** Maps piecen color codes to colors. */
    public static Color[] PIECEN_COLOR_MAP = {
        new Color(0x0246B7), // BLUE
        new Color(0x028A12), // GREEN
        new Color(0xF47A02), // ORANGE
        new Color(0xC20292), // MAGENTA
        new Color(0xB90202), // RED
        new Color(0xFECA11), // YELLOW
    };

    /** The type of this feature. */
    public int type;

    /** The edge mask associated with this feature. */
    public int edgeMask;

    /** The location at which a piecen on this feature would be drawn (for
     * each of the four tile orientations. */
    public Point2D[] piecenSpots = new Point2D[4];

    /** The polygons used to render and hit test this feature (one for
     * each orientation). */
    public GeneralPath[] polys = new GeneralPath[4];

    /**
     * Constructs a feature with a feature description array containing
     * all of the necessary feature information.
     */
    public Feature (int[] desc)
    {
        type = desc[0];
        edgeMask = desc[1];

        // fetch our natural piecen spot, scale it and adjust for half units
        int px = (desc[2] * TILE_WIDTH) / 4;
        if (px < 0) {
            px *= -1;
            px -= TILE_WIDTH/8;
        }
        int py = (desc[3] * TILE_HEIGHT) / 4;
        if (py < 0) {
            py *= -1;
            py -= TILE_HEIGHT/8;
        }
        // oh, just a teeny hack for aesthetic shield placement
        if (type == -1) {
            px += 2; py += 2;
        }
        piecenSpots[NORTH] = new Point2D.Float(px, py);

        // create our natural feature polygon
        if (type == ROAD) {
            // roads are handled specially; they are either one or two
            // segment roads
            if (desc.length == 8) {
                polys[NORTH] = roadSegmentToPolygon(
                    desc[4], desc[5], desc[6], desc[7]);

            } else if (desc.length == 10) {
                polys[NORTH] = roadSegmentToPolygon(
                    desc[4], desc[5], desc[6], desc[7], desc[8], desc[9]);

            } else {
                Log.warning("Feature constructed with bogus road geometry " +
                            "[desc=" + StringUtil.toString(desc) + "].");
            }

        } else {
            GeneralPath poly = new GeneralPath();
            for (int i = 4; i < desc.length; i += 2) {
                // scale the coords accordingly
                int fx = (desc[i] * TILE_WIDTH) / 4;
                int fy = (desc[i+1] * TILE_HEIGHT) / 4;
                if (i == 4) {
                    poly.moveTo(fx, fy);
                } else {
                    poly.lineTo(fx, fy);
                }
            }
            poly.closePath();
            polys[NORTH] = poly;
        }

        // now create the three other orientations
        AffineTransform xform = new AffineTransform();
        for (int orient = 1; orient < 4; orient++) {
            // rotate the xform into the next orientation
            xform.translate(TILE_WIDTH, 0);
            xform.rotate(Math.PI/2);

            // transform the polygon
            polys[orient] = (GeneralPath)polys[NORTH].clone();
            polys[orient].transform(xform);

            // transform the piecen spot
            piecenSpots[orient] = new Point2D.Float();
            xform.transform(piecenSpots[NORTH], piecenSpots[orient]);
        }
    }

    /**
     * Returns true if the feature contains the supplied mouse coordinates
     * (which should be relative to the tile origin) given the supplied
     * orientation information.
     */
    public boolean contains (int mouseX, int mouseY, int orientation)
    {
        return polys[orientation].contains(mouseX, mouseY);
    }

    /**
     * Paints this feature to the specified graphics context with the
     * specified orientation, accounting for the supplied x and y offsets
     * of the origin. The graphics context is assumed to be appropriately
     * translated so that we may render this feature relative to the
     * origin and have it painted in the proper place.
     *
     * @param g the graphics context to use when painting the feature.
     * @param orientation the orientation at which to paint this feature.
     * @param claimGroup the claim group to which this feature belongs (or
     * zero if it belongs to no claim group).
     */
    public void paint (Graphics2D g, int orientation, int claimGroup)
    {
        // set the color according to the claim group
        g.setColor(claimGroup > 0 ? CLAIMED_COLOR_MAP[type] :
                   COLOR_MAP[type]);

        // now render the appropriate polygon for this orientation
        g.fill(polys[orientation]);

//          // paint our features for debugging (they aren't rotated and thus
//          // are only valid in the natural orientation)
//          int w = TILE_WIDTH, hw = w/2, qw = hw/2, tqw = 3*qw;
//          int h = TILE_HEIGHT, hh = h/2, qh = hh/2, tqh = 3*qh;
//          g.setColor(Color.gray);

//          if ((edgeMask & FeatureUtil.NORTH_F) != 0) {
//              g.drawLine(hw, 0, hw, qh);
//          }
//          if ((edgeMask & FeatureUtil.EAST_F) != 0) {
//              g.drawLine(tqw, hh, w, hh);
//          }
//          if ((edgeMask & FeatureUtil.SOUTH_F) != 0) {
//              g.drawLine(hw, tqh, hw, h);
//          }
//          if ((edgeMask & FeatureUtil.WEST_F) != 0) {
//              g.drawLine(0, hh, qw, hh);
//          }

//          if ((edgeMask & FeatureUtil.NNE_F) != 0) {
//              g.drawLine(tqw, 0, tqw, qh);
//          }
//          if ((edgeMask & FeatureUtil.NNW_F) != 0) {
//              g.drawLine(qw, 0, qw, qh);
//          }
//          if ((edgeMask & FeatureUtil.SSE_F) != 0) {
//              g.drawLine(tqw, tqh, tqw, h);
//          }
//          if ((edgeMask & FeatureUtil.SSW_F) != 0) {
//              g.drawLine(qw, tqh, qw, h);
//          }

//          if ((edgeMask & FeatureUtil.ENE_F) != 0) {
//              g.drawLine(tqw, qh, w, qh);
//          }
//          if ((edgeMask & FeatureUtil.ESE_F) != 0) {
//              g.drawLine(tqw, tqh, w, tqh);
//          }
//          if ((edgeMask & FeatureUtil.WNW_F) != 0) {
//              g.drawLine(0, qh, qw, qh);
//          }
//          if ((edgeMask & FeatureUtil.WSW_F) != 0) {
//              g.drawLine(0, tqh, qw, tqh);
//          }
    }

    /**
     * Paints a piecen in a position indicating that it is placed on this
     * feature. The graphics context is assumed to be appropriately
     * translated so that we may render this feature relative to the
     * origin and have it painted in the proper place.
     *
     * @param g the graphics context to use when painting the feature.
     * @param orientation the orientation at which to paint this feature.
     * @param image the piecen image to be painted.
     * @param claimGroup the claim group to which this piecen belongs (or
     * zero if it belongs to no claim group).
     */
    public void paintPiecen (Graphics2D g, int orientation, Image image,
                             int claimGroup)
    {
        Point2D point = piecenSpots[orientation];
        int iwidth = image.getWidth(null);
        int iheight = image.getHeight(null);

        // render the piecen image slightly transparent
        Composite ocomp = g.getComposite();
        g.setComposite(ALPHA_PLACING);
        g.drawImage(image, (int)(point.getX() - iwidth/2),
                    (int)(point.getY() - iheight/2), null);
        g.setComposite(ocomp);

//          // for now, draw the claim group next to the piecen
//          g.drawString(Integer.toString(claimGroup),
//                       (float)(point.getX() - swidth/2 + 2),
//                       (float)(point.getY() - sheight/2 - 2));
    }

    /**
     * Massages a road segment (specified in tile feature coordinates)
     * into a polygon (in screen coordinates) that can be used to render
     * or hit test the road. The coordinates must obey the following
     * constraints: (x1 < x2 and y1 == y2) or (x1 == x2 and y1 < y2) or
     * (x1 < x2 and y1 > y2).
     *
     * @return a polygon representing the road segment (with origin at 0,
     * 0).
     */
    protected static GeneralPath roadSegmentToPolygon (
        int x1, int y1, int x2, int y2)
    {
        // first convert the coordinates into screen coordinates
        x1 = (x1 * TILE_WIDTH) / 4;
        y1 = (y1 * TILE_HEIGHT) / 4;
        x2 = (x2 * TILE_WIDTH) / 4;
        y2 = (y2 * TILE_HEIGHT) / 4;

        GeneralPath poly = new GeneralPath();
        int dx = 4, dy = 4;

        // figure out what sort of line segment it is
        if (x1 == x2) { // vertical
            // make adjustments to ensure that we stay inside the tile
            // bounds
            if (y1 == 0) {
                y1 += dy;
            }
            if (y2 == TILE_HEIGHT) {
                y2 -= dy;
            }
            poly.moveTo(x1 - dx, y1 - dy);
            poly.lineTo(x1 + dx, y1 - dy);
            poly.lineTo(x2 + dx, y2 + dy);
            poly.lineTo(x2 - dx, y2 + dy);

        } else if (y1 == y2) { // horizontal
            // make adjustments to ensure that we stay inside the tile
            // bounds
            if (x1 == 0) {
                x1 += dx;
            }
            if (x2 == TILE_WIDTH) {
                x2 -= dx;
            }
            poly.moveTo(x1 - dx, y1 - dy);
            poly.lineTo(x1 - dx, y1 + dy);
            poly.lineTo(x2 + dx, y2 + dy);
            poly.lineTo(x2 + dx, y2 - dy);

        } else { // diagonal
            poly.moveTo(x1 - dx, y1);
            poly.lineTo(x1 + dx, y1);
            poly.lineTo(x2, y2 + dy);
            poly.lineTo(x2, y2 - dy);
        }

        poly.closePath();
        return poly;
    }

    /**
     * Massages a road segment (specified in tile feature coordinates)
     * into a polygon (in screen coordinates) that can be used to render
     * or hit test the road. The coordinates must obey the following
     * constraints: (y1 == y2) and (y2 > y3) and (x2 == x3).
     *
     * @return a polygon representing the road segment (with origin at 0,
     * 0).
     */
    protected static GeneralPath roadSegmentToPolygon (
        int x1, int y1, int x2, int y2, int x3, int y3)
    {
        // first convert the coordinates into screen coordinates
        x1 = (x1 * TILE_WIDTH) / 4;
        y1 = (y1 * TILE_HEIGHT) / 4;
        x2 = (x2 * TILE_WIDTH) / 4;
        y2 = (y2 * TILE_HEIGHT) / 4;
        x3 = (x3 * TILE_WIDTH) / 4;
        y3 = (y3 * TILE_HEIGHT) / 4;

        GeneralPath poly = new GeneralPath();
        int dx = 4, dy = 4;

        // figure out what sort of road segment it is
        if (x1 < x2) { // left turn
            poly.moveTo(x1, y1-dy);
            poly.lineTo(x2+dx, y2-dy);
            poly.lineTo(x3+dx, y3);
            poly.lineTo(x3-dx, y3);
            poly.lineTo(x2-dx, y2+dy);
            poly.lineTo(x1, y1+dy);

        } else { // right turn
            poly.moveTo(x1, y1-dy);
            poly.lineTo(x2-dx, y2-dy);
            poly.lineTo(x3-dx, y3);
            poly.lineTo(x3+dx, y3);
            poly.lineTo(x2+dx, y2+dy);
            poly.lineTo(x1, y1+dy);
        }

        poly.closePath();
        return poly;
    }

    /**
     * Generates a string representation of this feature.
     */
    public String toString ()
    {
        return "[type=" + FeatureUtil.typeToString(type) +
            ", edgeMask=" + FeatureUtil.edgeMaskToString(edgeMask) + "]";
    }

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
        Color.darkGray, // ROAD
        Color.yellow.darker(), // CLOISTER
    };

    /** For rendering piecens with alpha. */
    protected static final Composite ALPHA_PLACING =
	AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
}
