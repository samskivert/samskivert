//
// $Id: RipTest.java,v 1.4 2001/03/18 06:58:55 mdb Exp $

package robodj.convert;

import com.samskivert.net.cddb.*;

public class RipTest
{
    public static void lookupCDDB (String hostname, Ripper.TrackInfo[] info)
	throws Exception
    {
	CDDB cddb = new CDDB();

	long discid = RipUtil.computeDiscId(info);
	String cdid = Long.toString(discid, 0x10);

	// create an array with the track offsets
	int[] offsets = new int[info.length];
	for (int i = 0; i < info.length; i++) {
	    offsets[i] = info[i].offset;
	}

	int length = RipUtil.computeDiscLength(info);

	try {
            System.out.println("connecting to: " + hostname);
	    String rsp = cddb.connect(hostname);

	    // set the timeout to 30 seconds
	    cddb.setTimeout(30*1000);

	    // try a test query
	    CDDB.Entry[] entries = cddb.query(cdid, offsets, length);

	    if (entries == null || entries.length == 0) {
		System.out.println("No match for " + cdid + ".");

	    } else {
		for (int i = 0; i < entries.length; i++) {
		    System.out.println("Match " + entries[i].category + "/" +
				       entries[i].cdid + "/" +
				       entries[i].title);
		}

		CDDB.Detail detail = cddb.read(entries[0].category,
					       entries[0].cdid);
		System.out.println("Title: " + detail.title);
		for (int i = 0; i < detail.trackNames.length; i++) {
		    System.out.println(pad(i) + ": " + detail.trackNames[i]);
		}
		System.out.println("Extended data: " + detail.extendedData);
		for (int i = 0; i < detail.extendedTrackData.length; i++) {
		    System.out.println(pad(i) + ": " +
				       detail.extendedTrackData[i]);
		}
	    }

	} finally {
	    cddb.close();
	}
    }

    protected static String pad (int value)
    {
	return ((value > 9) ? "" : " ") + value;
    }

    public static void main (String[] args)
    {
	try {
	    Ripper ripper = new CDParanoiaRipper();
	    Ripper.TrackInfo[] info = ripper.getTrackInfo();
	    for (int i = 0; i < info.length; i++) {
		System.out.println((i+1) + ": " + info[i].offset + ", " +
		    info[i].length);
	    }

	    if (info.length == 0) {
		System.out.println("Matched no tracks?!");

	    } else {
		// try the CDDB lookup
		lookupCDDB("ca.freedb.org", info);

		// rip a track, for fun
		int track = 2;
		System.out.println("Ripping track " + track +
				   " [" + info[track-1].offset +
				   ", " + info[track-1].length + "]...");

		// report our progress
		ConversionProgressListener listener =
		    new ConversionProgressListener() {
		    public void updateProgress (int percentDone)
		    {
			System.out.println("Percent done: " +
					   percentDone + "%");
		    }
		};
		ripper.ripTrack(info, track, "/tmp/track" + track + ".wav",
				listener);
	    }

	} catch (Exception e) {
	    System.err.println("Error: " + e);
	}
    }
}
