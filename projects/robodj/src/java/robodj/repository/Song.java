//
// $Id: Song.java,v 1.2 2003/05/04 18:16:07 mdb Exp $

package robodj.repository;

import com.samskivert.util.StringUtil;

/**
 * A song maps approximately to an individual piece of music. In most
 * cases repository entries are created from albums which contain some
 * number of songs. Some special cases involve mixed CDs with one big song
 * or streaming Internet music sources. In these cases, the entry tends to
 * be comprised of a single song which represents the entire contents of
 * the entry.
 */
public class Song
{
    /** The unique identifier for this song. */
    public int songid;

    /** The unique identifier of the entry to which this song maps. */
    public int entryid;

    /**
     * The position of this song on the media (counting from 1 since
     * that's what people do on the actual media). I'd call it index but
     * that's a reserved word in SQL.
     */
    public int position;

    /** The title of this song. */
    public String title;

    /** The location of the media for this song. */
    public String location;

    /**
     * The duration of this song (in seconds) or -1 if the duration is
     * unknown.
     */
    public int duration;

    /** A comma separated list of the "users" that have voted for this
     * song where "users" is the short string (initials) provided by a
     * client when voting for a song. */
    public String votes;

    /**
     * Adds a voter to the votes string for this entry, not including a
     * voter that already exists in the voter set.
     *
     * @return true if the voter set was changed as a result of this
     * addition, false if not.
     */
    public boolean addVote (String voter)
    {
        // handle our first vote separately
        if (StringUtil.blank(votes)) {
            votes = voter;
            _votes = null; // clear our cached votes
            return true;
        }

        // make sure this voter has not already cast their ballot
        if (hasVoted(voter)) {
            return false;
        }

        votes = (votes + "," + voter);
        _votes = null; // clear our cached votes
        return true;
    }

    /**
     * Removes a voter from the votes string for this song.
     *
     * @return true if the voter set was changed as a result of this
     * addition, false if not.
     */
    public boolean clearVote (String voter)
    {
        String[] votes = getVotes();
        StringBuffer nvotes = new StringBuffer();
        boolean saw = false;
        for (int ii = 0; ii < votes.length; ii++) {
            if (votes[ii].equalsIgnoreCase(voter)) {
                saw = true;
            } else {
                if (nvotes.length() > 0) {
                    nvotes.append(",");
                }
                nvotes.append(votes[ii]);
            }
        }
        if (saw) {
            this.votes = nvotes.toString();
            _votes = null;
        }
        return saw;
    }

    /**
     * Returns the {@link #votes} field broken up into an array of strings
     * with one for each "vote".
     */
    public String[] getVotes ()
    {
        if (_votes == null) {
            if (StringUtil.blank(votes)) {
                _votes = new String[0];
            } else {
                _votes = StringUtil.split(votes, ",");
            }
        }
        return _votes;
    }

    /**
     * Returns true if the specified user has voted.
     */
    public boolean hasVoted (String voter)
    {
        String[] votes = getVotes();
        for (int ii = 0; ii < votes.length; ii++) {
            if (votes[ii].equalsIgnoreCase(voter)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a string representation of this instance.
     */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }

    /** The decoded voter array for this song. */
    protected transient String[] _votes;
}
