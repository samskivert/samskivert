//
// $Id: CDDBUtil.java,v 1.1 2000/12/10 07:02:09 mdb Exp $

package robodj.importer;

import com.samskivert.net.cddb.*;
import robodj.convert.Ripper;
import robodj.convert.RipUtil;

public class CDDBUtil
{
    /**
     * Does a CDDB lookup for the supplied CD information and returns an
     * array of disc info objects containing the name information returned
     * from CDDB.
     *
     * @return An array of detail objects, one per match or a zero length
     * array if no matches were found.
     */
    public static CDDB.Detail[] doCDDBLookup (String hostname,
					      Ripper.TrackInfo[] info)
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
	    String rsp = cddb.connect(hostname);

	    // set the timeout to 30 seconds
	    cddb.setTimeout(30*1000);

	    // try a test query
	    CDDB.Entry[] entries = cddb.query(cdid, offsets, length);
	    CDDB.Detail[] details;

	    if (entries == null || entries.length == 0) {
		details = new CDDB.Detail[0];

	    } else {
		details = new CDDB.Detail[entries.length];
		for (int i = 0; i < entries.length; i++) {
		    details[i] = cddb.read(entries[i].category,
					   entries[i].cdid);
		}
	    }

	    return details;

	} finally {
	    cddb.close();
	}
    }
}
