//
// $Id: CDParanoiaRipper.java,v 1.7 2003/10/10 21:25:46 mdb Exp $

package robodj.convert;

import java.io.*;
import java.util.ArrayList;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * A ripper implementation that uses cdparanoia to do it's job.
 */
public class CDParanoiaRipper implements Ripper
{
    public TrackInfo[] getTrackInfo ()
	throws ConvertException
    {
	// an input line that we're interested in looks something like
	// this:
	//
	//   1.    17980 [03:59.55]        0 [00:00.00]    no   no  2
	Pattern regex;
	try {
	    regex = Pattern.compile("^\\s*\\d+\\.\\s+(\\d+)\\s\\[\\S*\\]\\s+(\\d+)");
	} catch (PatternSyntaxException pse) {
	    throw new ConvertException("Can't compile regexp?! " + pse);
	}

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

	    while ((inline = din.readLine()) != null) {
		// skip blank lines and lines that are in the header
		if (inline.trim().length() == 0 ||
		    inline.indexOf("xiph.org") != -1 ||
		    inline.indexOf("mit.edu") != -1 ||
		    inline.indexOf("cdparanoia") == 0) {
		    continue;
		}

		// keep track of all of the input in case we need to
		// report an error later
		input.append(inline).append("\n");

		// see if we match our regular expression
		Matcher match = regex.match(inline);
		if (match.matches()) {
		    flist.add(inline.substring(match.start(0), match.end(0)));
		    flist.add(inline.substring(match.start(1), match.end(1)));
		}
	    }

	    // check the return value of the process
	    try {
		int retval = tocproc.waitFor();
		if (retval != 0) {
		    // ship off the error output from cdparanoia
		    throw new ConvertException(input.toString());
		}

	    } catch (InterruptedException ie) {
		// why we were interrupted I can only speculate, but we'll
		// go ahead and freak out anyway
		throw new ConvertException("Interrupted while waiting for " +
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
		    throw new ConvertException(
			"Bogus frame value for track " + (i+1) + ": " +
			nfe.getMessage() + "\n\n" + input);
		}
	    }

	    return frames;

	} catch (IOException ioe) {
	    throw new ConvertException(
		"Error communicating with ripper:\n" + ioe);
	}
    }

    public void ripTrack (TrackInfo[] info, int index, String target,
			  ConversionProgressListener listener)
	throws ConvertException
    {
	StringBuffer cmd = new StringBuffer("nice cdparanoia");
	cmd.append(" -w"); // request output in WAV format
	cmd.append(" -e"); // request progress information to stderr
	cmd.append(" ").append(index); // add track number
	cmd.append(" ").append(target); // add output file name

	// we'll need this for later
	Pattern regex;
	try {
	    regex = Pattern.compile("^##: -?\\d+ \\[(\\S+)\\] @ (\\d+)");
	} catch (PatternSyntaxException pse) {
	    throw new ConvertException("Can't compile regexp?! " + pse);
	}

	try {
	    // fork off a cdparanoia process to read the TOC
	    Runtime rt = Runtime.getRuntime();
	    Process ripproc = rt.exec(cmd.toString());

	    InputStream in = ripproc.getErrorStream();
	    BufferedInputStream bin = new BufferedInputStream(in);
	    DataInputStream din = new DataInputStream(bin);

	    // read output from the subprocess
	    String out;

	    // figure out what our progress boundaries are
	    long tstart = info[index-1].offset * MAGIC_FACTOR;
	    long tlength = info[index-1].length * MAGIC_FACTOR;
	    int lastReported = 0;

	    while ((out = din.readLine()) != null) {
		// if they don't care about the output then neither do we
		if (listener == null) {
		    continue;
		}

		// parse the output
		Matcher match = regex.match(out);
		if (match.matches()) {
		    String action = out.substring(match.start(0), match.end(0));
		    long offset = -1L;
		    try {
			String ostr = out.substring(match.start(1), match.end(1));
			offset = Long.parseLong(ostr);
		    } catch (NumberFormatException nfe) {
			// System.err.println("Malformed position info: " + out);
			continue;
		    }

		    int pctDone = (int)(100 * (offset - tstart) / tlength);
		    if (pctDone - lastReported >= 1) {
			listener.updateProgress(pctDone);
			lastReported = pctDone;
		    }

		} else {
		    // System.out.println("Couldn't match: " + out);
		}
	    }

	    // check the return value of the process
	    try {
		int retval = ripproc.waitFor();
		if (retval != 0) {
		    // ship off the error output from cdparanoia
		    throw new ConvertException(
			"Ripper failed: " + retval);
		}

	    } catch (InterruptedException ie) {
		// why we were interrupted I can only speculate, but we'll
		// go ahead and freak out anyway
		throw new ConvertException("Interrupted while waiting for " +
					   "ripper process to exit.");
	    }

	    // if everything was successful, report completion
	    listener.updateProgress(100);

	} catch (IOException ioe) {
	    throw new ConvertException(
		"Error communicating with ripper:\n" + ioe);
	}
    }

    // cdparanoia reports length in sectors and progress in 
    protected static final int MAGIC_FACTOR = 1176;
}
