//
// $Id: EntryList.java,v 1.1 2001/06/05 16:42:38 mdb Exp $

package robodj.chooser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;

import com.samskivert.swing.*;
import com.samskivert.swing.util.*;

import robodj.Log;
import robodj.repository.*;

public class EntryList
    extends JPanel
    implements TaskObserver, ActionListener
{
    public EntryList ()
    {
	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
	setLayout(gl);

        // create the pane that will hold the buttons
        gl = new VGroupLayout(GroupLayout.NONE);
        gl.setOffAxisPolicy(GroupLayout.EQUALIZE);
        _bpanel = new JPanel(gl);

	// give ourselves a wee bit of a border
	_bpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // put it into a scrolling pane
	JScrollPane bscroll = new JScrollPane(_bpanel);
        add(bscroll);

        // add our navigation button
        _upbut = new JButton("Up");
        _upbut.setActionCommand("up");
        _upbut.addActionListener(this);
        add(_upbut, GroupLayout.FIXED);

        // start up the task that reads the CD info from the database
  	TaskMaster.invokeMethodTask("readEntries", this, this);
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
                Chooser.scontrol.append(_entry.songs[i].location);
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
        throws SQLException
    {
        return Chooser.repository.getEntries("");
    }

    /**
     * Reads the entries from the database.
     */
    public void readSongs ()
        throws SQLException
    {
        Chooser.repository.populateSongs(_entry);
    }

    /**
     * Reads the entries from the database and plays them all.
     */
    public void readAndPlay ()
        throws SQLException
    {
        Chooser.repository.populateSongs(_entry);
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
            GroupLayout gl = new HGroupLayout(GroupLayout.NONE);
            gl.setOffAxisPolicy(GroupLayout.STRETCH);
            gl.setJustification(GroupLayout.LEFT);
            hpanel.setLayout(gl);

            JButton button;

            button = new JButton("Play");
            button.setActionCommand("playall");
            button.addActionListener(this);
            button.putClientProperty("entry", entries[i]);
            hpanel.add(button);

            button = new JButton(entries[i].title);
            button.setActionCommand("browse");
            button.addActionListener(this);
            button.putClientProperty("entry", entries[i]);
            hpanel.add(button);

            _bpanel.add(hpanel);
        }
    }

    protected void populateSong (Entry entry)
    {
        // enable the up button because we're looking at a song
        _upbut.setEnabled(true);

        // clear out any existing children
        _bpanel.removeAll();

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
            button.setActionCommand("play");
            button.addActionListener(this);
            button.putClientProperty("song", entry.songs[i]);
            _bpanel.add(button);
        }
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
            Chooser.scontrol.append(song.location);

        } else if (cmd.equals("up")) {
            populateEntries(_entries);

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    protected JPanel _bpanel;
    protected JButton _upbut;

    protected Entry[] _entries;
    protected Entry _entry;
}
