//
// $Id: TileGeometryTest.java,v 1.1 2001/10/15 19:55:15 mdb Exp $

package com.threerings.venison;

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.IntTuple;

/**
 * A simple class for testing the tile geometry specifications by drawing
 * them.
 */
public class TileGeometryTest
    extends JPanel implements TileCodes
{
    public TileGeometryTest ()
    {
        ArrayList polys = new ArrayList();
        ArrayList colors = new ArrayList();

        // create polygons from the various tile features
        for (int i = 1; i < TileUtil.TILE_FEATURES.length; i++) {
            // convert tile index into x and y coordinates (in tile
            // feature coords which will be converted to screen coords)
            int x = 4 * (i % 5), y = 4 * (i / 5);

            // the first feature is the background color
            Object[] features = (Object[])TileUtil.TILE_FEATURES[i];
            IntTuple base = (IntTuple)features[0];

            // add a polygon containing the whole tile colored with the
            // background color
            colors.add(COLOR_MAP[base.left]);
            Polygon poly = new Polygon();
            poly.addPoint(((x + 0) * TILE_WIDTH) / 4,
                          ((y + 0) * TILE_HEIGHT) / 4);
            poly.addPoint(((x + 4) * TILE_WIDTH) / 4,
                          ((y + 0) * TILE_HEIGHT) / 4);
            poly.addPoint(((x + 4) * TILE_WIDTH) / 4,
                          ((y + 4) * TILE_HEIGHT) / 4);
            poly.addPoint(((x + 0) * TILE_WIDTH) / 4,
                          ((y + 4) * TILE_HEIGHT) / 4);
            polys.add(poly);

            // the remainder are tuple/coordinate pairs
            for (int f = 1; f < features.length; f += 2) {
                IntTuple type = (IntTuple)features[f];
                int[] coords = (int[])features[f+1];

                // create a color for this polygon
                colors.add(COLOR_MAP[type.left]);

                // if this is a road segment, we need to create a special
                // polygon
                if (type.left == ROAD) {
                    poly = TileUtil.roadSegmentToPolygon(
                        coords[0], coords[1], coords[2], coords[3]);
                    // translate the polygon into our coordinate space
                    poly.translate((x * TILE_WIDTH)/4, (y * TILE_HEIGHT)/4);

                } else {
                    // otherwise create the polygon directly from the coords
                    poly = new Polygon();
                    for (int c = 0; c < coords.length; c += 2) {
                        // translate and scale the coords accordingly
                        int fx = ((x + coords[c]) * TILE_WIDTH) / 4;
                        int fy = ((y + coords[c+1]) * TILE_HEIGHT) / 4;
                        poly.addPoint(fx, fy);
                    }
                }

                polys.add(poly);
            }
        }

        // create our arrays
        _polys = new Polygon[polys.size()];
        polys.toArray(_polys);
        _colors = new Color[colors.size()];
        colors.toArray(_colors);
    }

    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        // paint our polygons
        for (int i = 0; i < _polys.length; i++) {
            g.setColor(_colors[i]);
            g.fillPolygon(_polys[i]);
        }

        // outline the tiles
        g.setColor(Color.black);
        for (int i = 0; i < 20; i++) {
            int x = i % 5, y = i / 5;
            g.drawRect(TILE_WIDTH * x, TILE_HEIGHT * y,
                       TILE_WIDTH, TILE_HEIGHT);
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
        // quit if we're closed
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        TileGeometryTest panel = new TileGeometryTest();
        frame.getContentPane().add(panel);
        frame.pack();
        SwingUtil.centerWindow(frame);
        frame.show();
    }

    protected Polygon[] _polys;
    protected Color[] _colors;

    protected static Color[] COLOR_MAP = {
        Color.red, // CITY
        Color.green, // GRASS
        Color.black // ROAD
    };
}
