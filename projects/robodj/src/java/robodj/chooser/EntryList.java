//
// $Id: EntryList.java,v 1.13 2003/05/04 18:16:06 mdb Exp $

package robodj.chooser;

import java.awt.Font;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import com.samskivert.io.PersistenceException;
import com.samskivert.swing.*;
import com.samskivert.swing.util.*;

import robodj.Log;
import robodj.repository.*;
import robodj.util.ButtonUtil;

public abstract class EntryList extends JScrollPane
    implements ControllerProvider
{
    public EntryList ()
    {
        // create the pane that will hold the buttons
        GroupLayout gl = new VGroupLayout(GroupLayout.NONE);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        gl.setJustification(GroupLayout.TOP);
        gl.setGap(2);
        _bpanel = new ScrollablePanel() {
            // make the playlist fit the width of the scrolling viewport
            public boolean getScrollableTracksViewportWidth () {
                return true;
            }
        };
        _bpanel.setLayout(gl);

	// give ourselves a wee bit of a border
	_bpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // set our viewport view
        setViewportView(_bpanel);

        // use a special font for our name buttons
        _titleFont = new Font("Helvetica", Font.BOLD, 14);

        // create our controller
        _controller = createController();
    }

    public Controller getController ()
    {
        return _controller;
    }

    /** Creates the controller for this entry list. */
    protected abstract Controller createController ();

    /** The string to display when there are no matching results. */
    protected abstract String getEmptyString ();

    /**
     * Creates the proper buttons, etc. for each entry.
     */
    protected void populateEntries (Entry[] entries)
    {
        // clear out any existing children
        _bpanel.removeAll();

        // sort our entries
        Comparator ecomp = new Comparator() {
            public int compare (Object o1, Object o2) {
                Entry e1 = (Entry)o1, e2 = (Entry)o2;
                int rv = e1.artist.compareTo(e2.artist);
                return rv == 0 ? e1.title.compareTo(e2.title) : rv;
            }
            public boolean equals (Object o1) {
                return o1.equals(this);
            }
        };
        Arrays.sort(entries, ecomp);

        // and add buttons for every entry
        String artist = null;
        for (int i = 0; i < entries.length; i++) {
            if (!entries[i].artist.equals(artist)) {
                artist = entries[i].artist;
                JLabel label = new JLabel(entries[i].artist);
                _bpanel.add(label);
            }

            // create an entry item for this entry
            _bpanel.add(new EntryItem(entries[i]));
        }

        // if there were no entries, stick a label in to that effect
        if (entries.length == 0) {
            _bpanel.add(new JLabel(getEmptyString()));
        }

        // reset our scroll position so that we're displaying the top of
        // the entry list. we'd like to save our scroll position and
        // restore it when the user clicks "up", but as we're rebuilding
        // the entry display the scroll bar is still configured for the
        // old contents and we're not around when it gets configured with
        // the new contents, so we can't ensure that the scrollbar has
        // been properly configured before we adjust its position back to
        // where we were... oh the complication.
        clearScrollPosition();

        // we've removed and added components so we need to revalidate
        revalidate();
        repaint();
    }

    protected void populateSong (Entry entry)
    {
        // clear out any existing children
        _bpanel.removeAll();

        GroupLayout gl = new HGroupLayout(HGroupLayout.STRETCH);
        gl.setJustification(GroupLayout.LEFT);
	JPanel header = new JPanel(gl);

        // add the album title
        JLabel label = new JLabel(entry.title);
        label.setToolTipText(entry.title);
        label.setFont(_titleFont);
        header.add(label);

        // add a button for getting the heck out of here
        JButton upbtn = ButtonUtil.createControlButton(
            UP_TIP, "up", ButtonUtil.getIcon(UP_ICON_PATH), true);
        header.add(upbtn, GroupLayout.FIXED);

        // create an edit button
        JButton ebtn = ButtonUtil.createControlButton(
            EDIT_TIP, "edit", ButtonUtil.getIcon(EDIT_ICON_PATH), true);
        header.add(ebtn, GroupLayout.FIXED);

        // add a combo box for categorizing
        JComboBox catcombo =
            new JComboBox(ModelUtil.catBoxNames(Chooser.model));
	header.add(catcombo, GroupLayout.FIXED);

        // configure the combo box
        catcombo.addActionListener(Controller.DISPATCHER);
        catcombo.setActionCommand("categorize");
        int catid = Chooser.model.getCategory(entry.entryid);
        int catidx = ModelUtil.getCategoryIndex(Chooser.model, catid);
        catcombo.setSelectedIndex(catidx+1);

	_bpanel.add(header, GroupLayout.FIXED);

        // sort the songs by position
        Comparator scomp = new Comparator() {
            public int compare (Object o1, Object o2) {
                Song s1 = (Song)o1, s2 = (Song)o2;
                return s1.position - s2.position;
            }
            public boolean equals (Object o1) {
                return o1.equals(this);
            }
        };
        Arrays.sort(entry.songs, scomp);

        // and add buttons for every song
        for (int i = 0; i < entry.songs.length; i++) {
            _bpanel.add(new SongItem(entry.songs[i], SongItem.BROWSER));
        }

        // reset our scroll position so that we're displaying the top of
        // the song list
        clearScrollPosition();

        // we've removed and added components so we need to revalidate
        revalidate();
        repaint();
    }

    protected void clearScrollPosition ()
    {
        BoundedRangeModel model = getVerticalScrollBar().getModel();
        model.setValue(model.getMinimum());
    }

    protected Controller _controller;
    protected JPanel _bpanel;

    protected Font _titleFont;

    protected static final String UP_TIP =
        "Back up to albums listing";
    protected static final String EDIT_TIP =
        "Edit the album information";

    protected static final String ICON_ROOT = "/robodj/chooser/images/";
    protected static final String UP_ICON_PATH = ICON_ROOT + "up.png";
    protected static final String EDIT_ICON_PATH = ICON_ROOT + "edit.png";
}
