//
// $Id: EncodeTest.java,v 1.1 2001/03/18 06:58:55 mdb Exp $

package robodj.convert;

import com.samskivert.net.cddb.*;

public class EncodeTest
{
    public static void main (String[] args)
    {
        if (args.length < 2) {
            System.err.println("Usage: EncodeTest source.wav dest.mp3");
            System.exit(-1);
        }

	try {
	    Encoder encoder = new LameEncoder();
            ConversionProgressListener listener =
                new ConversionProgressListener() {
		    public void updateProgress (int percentDone)
		    {
			System.out.println("Percent done: " +
					   percentDone + "%");
		    }
		};
            encoder.encodeTrack(args[0], args[1], listener);

	} catch (Exception e) {
	    System.err.println("Error: " + e);
	}
    }
}
