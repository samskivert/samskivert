//
// $Id: RipUtil.java,v 1.3 2003/10/24 18:34:28 mdb Exp $

package robodj.convert;

import com.samskivert.util.StringUtil;

/**
 * This class contains utility functions related to ripping.
 */
public class RipUtil
{
    /**
     * Compact discs contain this many frames per second.
     */
    public static int FRAMES_PER_SECOND = 75;

    /**
     * Computes the CDDB disc id for the specified CD using the supplied
     * track info.
     */
    public static long computeDiscId (Ripper.TrackInfo[] info)
    {
	// first we sum all the digits in the numbers that represent the
	// number of seconds for each track
	long digits = 0;
	for (int i = 0; i < info.length; i++) {
	    int secs = info[i].offset/FRAMES_PER_SECOND;
	    digits += addDigits(secs);
	}

	// determine the total number of seconds of all the tracks on the
	// CD (doing so by subtracting the offset of the end of the disc
	// from the offset of the beginning which is how the CDDB
	// algorithm does it)
	long totsecs = (long)computeDiscLength(info);

	// finally combine these two values with the number of tracks on
	// the disc into the actual disc id
	return ((digits & 0xFF) << 24) | (totsecs << 8) | info.length;
    }

    /**
     * @return the length of the CD (in seconds) as computed from the
     * supplied frame information.
     */
    public static int computeDiscLength (Ripper.TrackInfo[] info)
    {
        if (info == null || info.length == 0) {
            String errmsg = "Must have at least one track to compute disc " +
                "length [info=" + StringUtil.toString(info) + "]";
            throw new IllegalArgumentException(errmsg);
        }
	return (info[info.length-1].offset +
		info[info.length-1].length -
		info[0].offset)/FRAMES_PER_SECOND;
    }

    /**
     * @return the sum of the digits in the supplied number (eg. 6 for 15,
     * 8 for 251, etc.).
     */
    protected static int addDigits (int value)
    {
	int rv = 0;
	while (value > 0) {
	    rv += value%10;
	    value /= 10;
	}
	return rv;
    }
}
