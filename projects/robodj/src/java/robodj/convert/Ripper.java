//
// $Id: Ripper.java,v 1.1 2000/10/30 21:08:53 mdb Exp $

package robodj.rip;

/**
 * The ripper interface is used to manipulate a particular piece of CD
 * ripping software in the ways needed by the RoboDJ system.
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
     * @exception RipException can be thrown if some problem occurs trying
     * to read the CD table of contents (like lack of access to the CDROM
     * device or any other errors).
     */
    public TrackInfo[] getTrackInfo () throws RipException;
}
