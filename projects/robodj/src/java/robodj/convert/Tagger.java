//
// $Id: Tagger.java,v 1.1 2002/03/03 05:25:10 mdb Exp $

package robodj.convert;

/**
 * The tagger interface is used to set the ID3 tags in MP3 files.
 */
public interface Tagger
{
    /**
     * Sets the ID3 tags for the specified target MP3 file for the
     * supplied information: artist, album name, track title and track
     * number.
     */
    public void idTrack (String target, String artist, String album,
                         String title, int trackNo)
	throws ConvertException;
}
