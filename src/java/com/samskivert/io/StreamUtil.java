//
// $Id: StreamUtil.java,v 1.1 2003/05/14 21:30:29 ray Exp $

package com.samskivert.io;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.samskivert.Log;

/**
 * Convenience methods for streams.
 */
public class StreamUtil
{
    /**
     * Convenient close for a stream. Use in a finally clause and love life.
     */
    public static void close (InputStream in)
    {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ioe) {
                Log.warning("Error closing input stream [stream=" + in +
                    ", cause=" + ioe + "].");
            }
        }
    }

    /**
     * Convenient close for a stream. Use in a finally clause and love life.
     */
    public static void close (OutputStream out)
    {
        if (out != null) {
            try {
                out.close();
            } catch (IOException ioe) {
                Log.warning("Error closing output stream [stream=" + out +
                    ", cause=" + ioe + "].");
            }
        }
    }
}
