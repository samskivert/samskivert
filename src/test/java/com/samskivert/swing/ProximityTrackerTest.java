//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

import org.junit.*;
import static org.junit.Assert.*;

import com.samskivert.swing.util.ProximityTracker;

public class ProximityTrackerTest
{
    @Test
    public void runTest ()
    {
        Random rand = new Random();
        ProximityTracker tracker = new ProximityTracker();
        ArrayList<Point> points = new ArrayList<Point>();

        // create 100 random points and add them to the tracker and our
        // comparison list
        for (int i = 0; i < 100; i++) {
            int x = rand.nextInt(MAX_X);
            int y = rand.nextInt(MAX_Y);
            Point p = new Point(x, y);
            tracker.addObject(x, y, p);
            points.add(p);
        }

        // now choose 100 new random points and confirm that the tracker
        // reports the same closest point that our brute force check
        // reports
        for (int i = 0; i < 100; i++) {
            int x = rand.nextInt(MAX_X);
            int y = rand.nextInt(MAX_Y);

            // get the closest point via the tracker
            Point tp = (Point)tracker.findClosestObject(x, y, null);

            // get the closest point via brute force
            Point cp = null;
            int mindist = Integer.MAX_VALUE;
            for (int p = 0; p < points.size(); p++) {
                Point hp = points.get(p);
                int dist = ProximityTracker.distance(hp.x, hp.y, x, y);
                if (dist < mindist) {
                    mindist = dist;
                    cp = hp;
                }
            }

            // the points might actually be different, but in that case,
            // the distances should be equal
            int tdist = ProximityTracker.distance(x, y, tp.x, tp.y);
            int cdist = ProximityTracker.distance(x, y, cp.x, cp.y);

            String tps = tp.x + "," + tp.y;
            String cps = cp.x + "," + cp.y;
            String ps = x + "," + y;
            assertTrue(ps + " => " + cps + " (" + cdist + ") ! " +
                       tps + " (" + tdist + ")",
                       tp.equals(cp) || (tdist == cdist));
        }
    }

    protected static final int MAX_X = 1000;
    protected static final int MAX_Y = 1000;
}
