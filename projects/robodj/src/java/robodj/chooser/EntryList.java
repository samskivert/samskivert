//
// $Id: EntryList.java,v 1.7 2001/09/20 20:42:48 mdb Exp $

package robodj.chooser;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import com.samskivert.jdbc.PersistenceException;
import com.samskivert.swing.*;
import com.samskivert.swing.util.*;

import robodj.Log;
import robodj.repository.*;

public class EntryList
    extends JPanel
    implements TaskObserver, ActionListener, AncestorListener
{
    public EntryList (int categoryid)
    {
	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
	setLayout(gl);

        // create the pane that will hold the buttons
        gl = new VGroupLayout(GroupLayout.NONE);
        gl.setJustification(GroupLayout.TOP);
        _bpanel = new JPanel(gl);

	// give ourselves a wee bit of a border
	_bpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // put it into a scrolling pane
	_scroller = new JScrollPane(_bpanel);
        add(_scroller);

        // add our navigation button
        _upbut = new JButton("Up");
        _upbut.setActionCommand("up");
        _upbut.addActionListener(this);
        add(_upbut, GroupLayout.FIXED);

        // use a special font for our name buttons
        _nameFont = new Font("Helvetica", Font.PLAIN, 10);

        // keep track of the query that we'll use to populate our entries
        // display
        _catid = categoryid;

        // listen to ancestor events
        addAncestorListener(this);
    }

    public void taskCompleted (String name, Object result)
    {
	if (name.equals("readEntries")) {
            // the result should be an array of entry objects with which
            // we can populate our button list
            _entries = (Entry[])result;
            populateEntries(_entries);

	} else if (name.equals("readAndPlay")) {
            for (int i = 0; i < _entry.songs.length; i++) {
                Chooser.scontrol.append(_entry.songs[i].entryid,
                                        _entry.songs[i].songid,
                                        _entry.songs[i].location);
            }

	} else if (name.equals("readSongs")) {
            populateSong(_entry);
        }
    }

    public void taskFailed (String name, Throwable exception)
    {
        String msg;
        if (Exception.class.equals(exception.getClass())) {
            msg = exception.getMessage();
        } else {
            msg = exception.toString();
        }
        JOptionPane.showMessageDialog(this, msg, "Error",
                                      JOptionPane.ERROR_MESSAGE); 
        Log.logStackTrace(exception);
    }

    /**
     * Reads the entries from the database.
     */
    public Entry[] readEntries ()
        throws PersistenceException
    {
        return Chooser.model.getEntries(_catid);
    }

    /**
     * Reads the entries from the database.
     */
    public void readSongs ()
        throws PersistenceException
    {
        Chooser.repository.populateSongs(_entry);
    }

    /**
     * Reads the entries from the database and plays them all.
     */
    public void readAndPlay ()
        throws PersistenceException
    {
        Chooser.repository.populateSongs(_entry);
    }

    /**
     * Updates the category for the displayed entry.
     */
    public void recategorizeEntry ()
        throws PersistenceException
    {
        Chooser.model.recategorize(_entry, _newcatid);
    }

    /**
     * Creates the proper buttons, etc. for each entry.
     */
    protected void populateEntries (Entry[] entries)
    {
        // disable the up button because we're at the top level
        _upbut.setEnabled(false);

        // clear out any existing children
        _bpanel.removeAll();

        // adjust our layout policy
        GroupLayout gl = (GroupLayout)_bpanel.getLayout();
        gl.setOffAxisPolicy(GroupLayout.EQUALIZE);
        gl.setOffAxisJustification(GroupLayout.LEFT);

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

            // create a browse and a play button
            JPanel hpanel = new JPanel();
            gl = new HGroupLayout(GroupLayout.NONE);
            gl.setOffAxisPolicy(GroupLayout.STRETCH);
            gl.setJustification(GroupLayout.LEFT);
            hpanel.setLayout(gl);

            JButton button;
            button = new JButton(entries[i].title);
            button.setFont(_nameFont);
            button.setActionCommand("browse");
            button.addActionListener(this);
            button.putClientProperty("entry", entries[i]);
            button.setToolTipText("Entry ID: " + entries[i].entryid);
            hpanel.add(button);

            button = new JButton("Play");
            button.setActionCommand("playall");
            button.addActionListener(this);
            button.putClientProperty("entry", entries[i]);
            hpanel.add(button);

            _bpanel.add(hpanel);
        }

        // if there were no entries, stick a label in to that effect
        if (_entries.length == 0) {
            _bpanel.add(new JLabel("No entries in this category."));
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

        // we've removed and added components and swing won't properly
        // repaint automatically
        _bpanel.repaint();
    }

    protected void populateSong (Entry entry)
    {
        // enable the up button because we're looking at a song
        _upbut.setEnabled(true);

        // clear out any existing children
        _bpanel.removeAll();

        // adjust our layout policy
        ((GroupLayout)_bpanel.getLayout()).setOffAxisPolicy(
            GroupLayout.NONE);

        // create a combo box and accoutrements for selecting the category
	JPanel catPanel = new JPanel(new HGroupLayout(GroupLayout.STRETCH));
	JLabel catLabel = new JLabel("Set category");
        JComboBox catcombo =
            new JComboBox(ModelUtil.catBoxNames(Chooser.model));

        // configure the combo box
        catcombo.addActionListener(this);
        catcombo.setActionCommand("categorize");
        int catid = Chooser.model.getCategory(entry.entryid);
        int catidx = ModelUtil.getCategoryIndex(Chooser.model, catid);
        catcombo.setSelectedIndex(catidx+1);

        // create an edit button
        JButton ebtn = new JButton("Edit entry");
        ebtn.setActionCommand("edit");
        ebtn.addActionListener(this);

        // wire it all up
	catPanel.add(catLabel, GroupLayout.FIXED);
	catPanel.add(catcombo);
        catPanel.add(ebtn, GroupLayout.FIXED);
	_bpanel.add(catPanel, GroupLayout.FIXED);

        // add a label for the title
        JLabel label = new JLabel(entry.title);
        _bpanel.add(label);

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
            JButton button = new JButton(entry.songs[i].title);
            button.setFont(_nameFont);
            button.setActionCommand("play");
            button.addActionListener(this);
            button.setHorizontalAlignment(JButton.LEFT);
            button.putClientProperty("song", entry.songs[i]);
            _bpanel.add(button);
        }

        // reset our scroll position so that we're displaying the top of
        // the song list
        clearScrollPosition();

        // we've removed and added components and swing won't properly
        // repaint automatically
        _bpanel.repaint();
    }

    protected void clearScrollPosition ()
    {
        BoundedRangeModel model =
            _scroller.getVerticalScrollBar().getModel();
        model.setValue(model.getMinimum());
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("browse")) {
            JButton src = (JButton)e.getSource();
            _entry = (Entry)src.getClientProperty("entry");
            // start up the task that reads this CDs songs from the database
            TaskMaster.invokeMethodTask("readSongs", this, this);

	} else if (cmd.equals("playall")) {
            JButton src = (JButton)e.getSource();
            _entry = (Entry)src.getClientProperty("entry");
            // start up the task that reads this CDs songs from the database
            TaskMaster.invokeMethodTask("readAndPlay", this, this);

        } else if (cmd.equals("play")) {
            JButton src = (JButton)e.getSource();
            Song song = (Song)src.getClientProperty("song");
            Chooser.scontrol.append(song.entryid, song.songid, song.location);

        } else if (cmd.equals("up")) {
            // re-read the category beacuse this entry may have been
            // recategorized
            TaskMaster.invokeMethodTask("readEntries", this, this);

        } else if (cmd.equals("edit")) {
            EditDialog dialog = new EditDialog(_entry);
            dialog.setSize(400, 400);
            SwingUtil.centerWindow(dialog);
            dialog.show();

        } else if (cmd.equals("categorize")) {
            JComboBox cb = (JComboBox)e.getSource();
            String catname = (String)cb.getSelectedItem();
            _newcatid = Chooser.model.getCategoryId(catname);
            // do the recategorization in a separate task
            TaskMaster.invokeMethodTask("recategorizeEntry", this, this);

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    public void ancestorAdded (AncestorEvent e)
    {
        // stick a "loading" label in the list to let the user know
        // what's up
        _bpanel.add(new JLabel("Loading..."));

        // start up the task that reads the CD info from the database
        TaskMaster.invokeMethodTask("readEntries", this, this);
    }

    public void ancestorRemoved (AncestorEvent e)
    {
        // clear out our entry ui elements
        _bpanel.removeAll();
    }

    public void ancestorMoved (AncestorEvent e)
    {
        // nothing doing
    }

    protected JScrollPane _scroller;
    protected JPanel _bpanel;
    protected JButton _upbut;

    protected int _catid;
    protected Entry[] _entries;

    protected Entry _entry;
    protected int _newcatid;

    protected Font _nameFont;
}
