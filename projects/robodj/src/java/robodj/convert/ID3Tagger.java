//
// $Id: ID3Tagger.java,v 1.2 2002/03/03 06:04:23 mdb Exp $

package robodj.convert;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.util.StreamUtils;

/**
 * A tagger implementation that uses 'id3v2' to do it's job.
 */
public class ID3Tagger implements Tagger
{
    // documentation inherited
    public void idTrack (String target, String artist, String album,
                         String title, int trackNo)
	throws ConvertException
    {
        String[] cmdarray = new String[] {
            "id3v2", "--song", title, "--artist", artist, "--album", album,
            "--track", Integer.toString(trackNo), target };

        try {
            Runtime rt = Runtime.getRuntime();
            Process ripproc = rt.exec(cmdarray);

	    InputStream in = ripproc.getErrorStream();
            String output = StreamUtils.streamAsString(in);

            // check the return value of the process
            try {
                int retval = ripproc.waitFor();
                if (retval != 0) {
                    // ship off the error output from id3
                    throw new ConvertException(
                        "id3v2 failed: " + output);
                }

            } catch (InterruptedException ie) {
                // why we were interrupted I can only speculate, but we'll
                // go ahead and freak out anyway
                throw new ConvertException("Interrupted while waiting for " +
                                           "encoder process to exit.");
            }

	} catch (IOException ioe) {
	    throw new ConvertException(
		"Error communicating with encoder:\n" + ioe);
	}
    }

    public static void main (String[] args)
    {
        ID3Tagger tagger = new ID3Tagger();
        if (args.length < 5) {
            System.err.println("Usage: ID3Tagger target artist album " +
                               "title trackNo");
            System.exit(-1);
        }

        int trackNo = 0;
        try {
            trackNo = Integer.parseInt(args[4]);
        } catch (NumberFormatException nfe) {
            System.err.println("Invalid track number: " + args[4]);
            System.exit(-1);
        }

        try {
            tagger.idTrack(args[0], args[1], args[2], args[3], trackNo);
        } catch (ConvertException ce) {
            ce.printStackTrace(System.err);
        }
    }
}
