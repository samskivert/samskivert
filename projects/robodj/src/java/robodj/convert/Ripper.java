//
// $Id: Ripper.java,v 1.2 2000/10/30 22:21:11 mdb Exp $

package robodj.convert;

/**
 * The ripper interface is used to manipulate particular CD ripping
 * software in the ways needed by the RoboDJ system.
 */
public interface Ripper
{
    /**
     * Instances of this object are used to communicate the track
     * information of a CD back to the caller.
     */
    public class TrackInfo
    {
	/**
	 * The offset of the start of this track (in frames).
	 */
	public int offset;

	/**
	 * The length of this track (in frames).
	 */
	public int length;
    }

    /**
     * This function should return an array with an entry for each track
     * on the CD (in order) containing the frame offset and length of that
     * track.  This function will be called in another thread and has 30
     * seconds to do its job or it will be declared delinquent and
     * ignored.
     *
     * @exception ConvertException can be thrown if some problem occurs
     * trying to read the CD table of contents (like lack of access to the
     * CDROM device or any other errors).
     */
    public TrackInfo[] getTrackInfo ()
	throws ConvertException;

    /**
     * Instructs the ripper to rip the track with the supplied index into
     * the file specified by target. If the ripper supports progress
     * notification, it should communicate it to the supplied progress
     * listener. The track should be ripped in WAV format.
     *
     * @param index the track number of the track to rip (starting at 1
     * since CDs start counting tracks at 1).
     * @param target the path to the file into which the track should be
     * ripped.
     * @param listener a callback object that should be called to
     * communicate ripping progress. If the listener parameter is null,
     * the caller doesn't want to hear about progress (shame on them).
     *
     * @exception ConvertException  can be  thrown if some  problem occurs
     * trying to rip the specified track (like lack of access to the CDROM
     * device or any other errors).
     */
    public void ripTrack (int index, String target,
			  ConversionProgressListener listener)
	throws ConvertException;
}
