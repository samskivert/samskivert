//
// $Id: L3Encoder.java,v 1.2 2003/10/10 19:33:16 mdb Exp $

package robodj.convert;

import java.io.*;

/**
 * An encoder implementation that uses the Fraunhofer-IIS L3ENC mp3
 * encoding program.
 */
public class L3Encoder implements Encoder
{
    public void encodeTrack (String source, String dest,
			     ConversionProgressListener listener)
	throws ConvertException
    {
	StringBuffer cmd = new StringBuffer("nice l3enc");
	cmd.append(" ").append(source); // add input file name
	cmd.append(" ").append(dest); // add output file name
	cmd.append(" -br 128000"); // request 128kbps encoding

	try {
	    // fork off a cdparanoia process to read the TOC
	    Runtime rt = Runtime.getRuntime();
	    Process encproc = rt.exec(cmd.toString());

	    InputStream in = encproc.getErrorStream();
	    BufferedInputStream bin = new BufferedInputStream(in);
	    DataInputStream din = new DataInputStream(bin);

	    // read output from the subprocess and chuck it for now
	    while (din.readLine() != null) {
		// la la la
	    }

	    // check the return value of the process
	    try {
		int retval = encproc.waitFor();
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

	} catch (IOException ioe) {
	    throw new ConvertException(
		"Error communicating with encoder:\n" + ioe);
	}
    }
}
