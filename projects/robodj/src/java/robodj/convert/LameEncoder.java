//
// $Id: LameEncoder.java,v 1.1 2001/03/18 06:58:55 mdb Exp $

package robodj.convert;

import java.io.*;
import java.util.ArrayList;

import gnu.regexp.*;

/**
 * An encoder implementation that uses LAME to do it's job.
 */
public class LameEncoder implements Encoder
{
    public void encodeTrack (String source, String dest,
			     ConversionProgressListener listener)
	throws ConvertException
    {
	StringBuffer cmd = new StringBuffer("lame");
	cmd.append(" --nohist"); // we don't want histogram output
	cmd.append(" -v"); // request variable bitrate encoding
	cmd.append(" ").append(source); // add source file name
	cmd.append(" ").append(dest); // add output file name

	// we'll need this for later
	RE regex;
	try {
            // a line of input looks like this
            //     0/1273   ( 0%)|    0:00/    0:00...
	    regex = new RE("^\\s*(\\d+)/(\\d+)\\s*\\(.*");
	} catch (REException ree) {
	    throw new ConvertException("Can't compile regexp?! " + ree);
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
	    int lastReported = 0;

	    while ((out = din.readLine()) != null) {
		// if they don't care about the output then neither do we
		if (listener == null) {
		    continue;
		}

		// parse the output
		REMatch match = regex.getMatch(out);
		if (match != null) {
		    int offset = -1, total = -1;
		    try {
			offset = Integer.parseInt(match.toString(1));
			total = Integer.parseInt(match.toString(2));
		    } catch (NumberFormatException nfe) {
			System.err.println("Malformed position info: " + out);
			continue;
		    }

		    int pctDone = (100 * offset) / total;
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
			"Encoder failed: " + retval);
		}

	    } catch (InterruptedException ie) {
		// why we were interrupted I can only speculate, but we'll
		// go ahead and freak out anyway
		throw new ConvertException("Interrupted while waiting for " +
					   "encoder process to exit.");
	    }

	    // if everything was successful, report completion
            if (lastReported < 100) {
                listener.updateProgress(100);
            }

	} catch (IOException ioe) {
	    throw new ConvertException(
		"Error communicating with encoder:\n" + ioe);
	}
    }
}
