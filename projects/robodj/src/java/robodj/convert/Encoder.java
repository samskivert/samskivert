//
// $Id: Encoder.java,v 1.1 2000/10/30 22:21:11 mdb Exp $

package robodj.convert;

/**
 * The encoder interface is used to manipulate particular MP3 encoding
 * software in the ways needed by the RoboDJ system.
 */
public interface Encoder
{
    /**
     * Instructs the encoder to encode the specified track.
     *
     * @param source the path to the source file that should be encoded.
     * @param dest the path to the destination file which should be
     * created by the encoder.
     * @param listener a callback object that should be called to
     * communicate encoding progress. If the listener parameter is null,
     * the caller doesn't want to hear about progress (shame on them).
     *
     * @exception ConvertException can be thrown if anything fails during
     * the encoding process.
     */
    public void encodeTrack (String source, String dest,
			     ConversionProgressListener listener)
	throws ConvertException;
}
