//
// $Id: PiecenUtil.java,v 1.1 2001/12/18 11:59:09 mdb Exp $

package com.threerings.venison;

import java.awt.Image;

import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.UniformTileSet;
import com.threerings.media.tile.NoSuchTileException;

public class PiecenUtil
{
    /**
     * Loads up the piecen images using the supplied tile manager.
     */
    public static void init (TileManager tmgr)
    {
        // load up the piecen tiles
        UniformTileSet piecenSet = new UniformTileSet();
        piecenSet.setTileCount(PIECEN_TYPES);
        piecenSet.setWidth(PIECEN_WIDTH);
        piecenSet.setHeight(PIECEN_HEIGHT);
        piecenSet.setImagePath(PIECEN_IMG_PATH);
        piecenSet.setImageProvider(tmgr);

        // fetch the tile images
        _images = new Image[PIECEN_TYPES];
        for (int i = 0; i < PIECEN_TYPES; i++) {
            try {
                _images[i] = piecenSet.getTileImage(i);
            } catch (NoSuchTileException nste) {
                Log.warning("Unable to obtain piecen tile [id=" + i + "].");
            }
        }
    }

    /**
     * Returns the piecen image for the specified piecen color.
     */
    public static Image getPiecenImage (int color)
    {
        return _images[color];
    }

    /** Our piecen images. */
    protected static Image[] _images;

    /** The number of different colors of piecen. */
    protected static final int PIECEN_TYPES = 6;

    /** The width of the piecen image in pixels. */
    protected static final int PIECEN_WIDTH = 16;

    /** The height of the piecen image in pixels. */
    protected static final int PIECEN_HEIGHT = 16;

    /** The path to the piecen image (relative to the resource
     * directory). */
    protected static final String PIECEN_IMG_PATH = "media/piecens.png";
}
