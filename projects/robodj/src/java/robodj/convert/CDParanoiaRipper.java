//
// $Id: CDParanoiaRipper.java,v 1.1 2000/10/30 21:08:53 mdb Exp $

package robodj.rip;

import java.io.*;
import java.util.ArrayList;

import gnu.regexp.*;

/**
 * A ripper implementation that uses cdparanoia to do it's job.
 */
public class CDParanoiaRipper implements Ripper
{
    public TrackInfo[] getTrackInfo () throws RipException
    {
	try {
	    // fork off a cdparanoia process to read the TOC
	    Runtime rt = Runtime.getRuntime();
	    Process tocproc = rt.exec("cdparanoia -Q");

	    InputStream in = tocproc.getErrorStream();
	    BufferedInputStream bin = new BufferedInputStream(in);
	    DataInputStream din = new DataInputStream(bin);

	    String inline;
	    StringBuffer input = new StringBuffer();
	    ArrayList flist = new ArrayList();

	    // an input line that we're interested in looks something like
	    // this:
	    //
	    //   1.    17980 [03:59.55]        0 [00:00.00]    no   no  2
	    RE regex =
		new RE("^\\s*\\d+\\.\\s+(\\d+)\\s\\[\\S*\\]\\s+(\\d+)");

	    while ((inline = din.readLine()) != null) {
		// skip blank lines and lines that are in the header
		if (inline.trim().length() == 0 ||
		    inline.indexOf("mit.edu") != -1 ||
		    inline.indexOf("cdparanoia") == 0) {
		    continue;
		}

		// System.out.println("Matching: " + inline);

		// keep track of all of the input in case we need to
		// report an error later
		input.append(inline).append("\n");

		// see if we match our regular expression
		REMatch match = regex.getMatch(inline);
		if (match != null) {
		    flist.add(match.toString(1));
		    flist.add(match.toString(2));
		}
	    }

	    // check the return value of the process
	    try {
		int retval = tocproc.waitFor();
		if (retval != 0) {
		    // ship off the error output from cdparanoia
		    throw new RipException(input.toString());
		}

	    } catch (InterruptedException ie) {
		// why we were interrupted I can only speculate, but we'll
		// go ahead and freak out anyway
		throw new RipException("Interrupted while waiting for " +
				       "cdparanoia process to exit.");
	    }

	    // parse the frame offsets and stick them in an array
	    TrackInfo[] frames = new TrackInfo[flist.size()/2];
	    for (int i = 0; i < frames.length; i++) {
		try {
		    frames[i] = new TrackInfo();
		    frames[i].length =
			Integer.parseInt((String)flist.get(2*i));
		    frames[i].offset =
			Integer.parseInt((String)flist.get(2*i+1));

		    // for some reason, cdparanoia reports track offsets
		    // starting from zero but CDDB assumes they start at 2
		    // seconds, so we have to adjust... sigh.
		    frames[i].offset += 2 * RipUtil.FRAMES_PER_SECOND;

		} catch (NumberFormatException nfe) {
		    throw new RipException("Bogus frame value for track " +
					   (i+1) + ": " + nfe.getMessage() +
					   "\n\n" + input);
		}
	    }

	    return frames;

	} catch (REException ree) {
	    throw new RipException("Can't compile regular expression?! " +
				   ree);

	} catch (IOException ioe) {
	    throw new RipException("Error talking to rip process: " + ioe);
	}
    }
}
