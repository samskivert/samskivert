//
// $Id: SongItem.java,v 1.1 2003/05/04 18:16:06 mdb Exp $

package robodj.chooser;

import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import com.samskivert.swing.HGroupLayout;
import com.samskivert.util.StringUtil;

import robodj.repository.Song;
import robodj.util.ButtonUtil;

/** 
 * Displays a particular song (used by the playlist and the entry list).
 */
public class SongItem extends Item
{
    /** Configures this song item for playlist mode. */
    public static final int PLAYLIST = 0;

    /** Configures this song item for browser mode. */
    public static final int BROWSER = 1;

    // playlist mode actions
    public static final String SKIP_TO = "song:skip_to";
    public static final String REMOVE = "song:remove";

    // browser mode actions
    public static final String PLAY = "song:play";

    // ubiquitous actions
    public static final String TOGGLE_VOTE = "song:toggle_vote";

    /** Used depending on where this song is being displayed. */
    public Object extra;

    /**
     * Creates a song item configured with the supplied song and specified
     * mode.
     */
    public SongItem (Song song, int mode)
    {
        _song = song;

        // configure our layout manager
        HGroupLayout gl = new HGroupLayout(HGroupLayout.NONE);
        gl.setJustification(HGroupLayout.LEFT);
        setLayout(gl);

        // create our control buttons
        JButton button;
        switch (mode) {
        case PLAYLIST:
            // add a button for skipping to this song
            button = ButtonUtil.createControlButton(
                SKIP_TO_TIP, SKIP_TO, _skipToIcon, true);
            button.putClientProperty("song", _song);
            add(button, HGroupLayout.FIXED);

            // add a button for removing this song
            button = ButtonUtil.createControlButton(
                REMOVE_TIP, REMOVE, _removeIcon, true);
            button.putClientProperty("song", _song);
            add(button, HGroupLayout.FIXED);

            break;

        case BROWSER:
            // add a button for playing the song
            button = ButtonUtil.createControlButton(
                PLAY_SONG_TIP, PLAY, _playIcon, true);
            button.putClientProperty(SONG_PROP, _song);
            add(button, HGroupLayout.FIXED);
            break;

        default:
            System.err.println("Unknown song mode: " + mode);
            break;
        }

        // add a vote button
        _voteButton = ButtonUtil.createControlButton(
            TOGGLE_VOTE_TIP, TOGGLE_VOTE, _noVoteIcon, true);
        _voteButton.putClientProperty(SONG_PROP, song);
        add(_voteButton, HGroupLayout.FIXED);

        // add the song title
        _trackLabel = new JLabel(_song.title);
        add(_trackLabel);

        // update our display based on our votes
        update();
    }

    public void setIsPlaying (boolean isPlaying)
    {
        _trackLabel.setForeground(isPlaying ? Color.red : Color.black);
    }

    public void update ()
    {
        if (!StringUtil.blank(_song.votes)) {
            _trackLabel.setFont(_hasVotesFont);
            _trackLabel.setToolTipText(
                "<html><table><tr><td>" + _song.title + "</td></tr>" +
                "<tr><td>Votes: " + _song.votes + "</td></tr>" +
                "</table></html>");
        } else {
            _trackLabel.setFont(_nameFont);
            _trackLabel.setToolTipText(_song.title);
        }

        switch (_song.hasVoted(Chooser.frame.getUser(false))) {
        case -1: _voteButton.setIcon(_nayVoteIcon); break;
        case 1: _voteButton.setIcon(_yeaVoteIcon); break;
        default:
        case 0: _voteButton.setIcon(_noVoteIcon); break;
        }

        repaint();
    }

    public Song getSong ()
    {
        return _song;
    }

    public static Song getSong (Object source)
    {
        return (Song)((JButton)source).getClientProperty(SONG_PROP);
    }

    protected Song _song;
    protected JButton _voteButton;
    protected JLabel _trackLabel;

    protected static final String SONG_PROP = "song";

    protected static final String SKIP_TO_TIP =
        "Skip to this song";
    protected static final String REMOVE_TIP =
        "Remove this song from the playlist";

    protected static final String PLAY_SONG_TIP =
        "Append this song to the playlist";

    protected static final String TOGGLE_VOTE_TIP =
        "Toggle your vote for this song";

    protected static ImageIcon _skipToIcon =
        ButtonUtil.getIcon(ICON_ROOT + "skip.png");
    protected static ImageIcon _removeIcon =
        ButtonUtil.getIcon(ICON_ROOT + "remove_song.png");

    protected static ImageIcon _yeaVoteIcon =
        ButtonUtil.getIcon(ICON_ROOT + "yea_vote.png");
    protected static ImageIcon _nayVoteIcon =
        ButtonUtil.getIcon(ICON_ROOT + "nay_vote.png");
    protected static ImageIcon _noVoteIcon =
        ButtonUtil.getIcon(ICON_ROOT + "no_vote.png");
}
