//
// $Id: UploadMusicPanel.java,v 1.7 2003/05/04 18:16:07 mdb Exp $

package robodj.importer;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.samskivert.net.cddb.CDDB;
import com.samskivert.swing.*;
import com.samskivert.swing.util.*;
import com.samskivert.util.ArrayUtil;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;

import org.farng.mp3.*;
import org.farng.mp3.id3.*;

import robodj.convert.*;
import robodj.repository.*;
import com.samskivert.util.CollectionUtil;

public class UploadMusicPanel extends ImporterPanel
    implements ActionListener, TaskObserver
{
    public UploadMusicPanel (Entry[] entries)
    {
	GroupLayout gl = new HGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	setLayout(gl);

        // save these for later
        _entries = entries;

	// create our label for the left hand side
	JLabel cdlabel = new JLabel("Upload Music", JLabel.CENTER);
	cdlabel.setFont(new Font("Helvetica", Font.BOLD, 24));
	add(cdlabel, GroupLayout.FIXED);

        // display status on looking up similar albums
        GroupLayout vgl = new VGroupLayout(GroupLayout.STRETCH);
        vgl.setOffAxisPolicy(GroupLayout.STRETCH);
        _spanel = new JPanel(vgl);

        JLabel sl = new JLabel("Status", JLabel.LEFT);
        _spanel.add(sl, GroupLayout.FIXED);

	_status.setText("Checking for similar music...\n");
	_spanel.add(new JScrollPane(_status));

        // add our panel to the main group
        add(_spanel);
    }

    public void wasAddedToFrame (ImporterFrame frame, boolean popped)
    {
	// keep this for later
	_frame = frame;

	frame.addControlButton("Cancel", "cancel", this);
	_back = frame.addControlButton("Back", "back", this);
	_next = frame.addControlButton("Upload", "upload", this);

        // fire up the similarity checker task
        TaskMaster.invokeTask("checkrepo", new TaskAdapter() {
            public Object invoke () throws Exception {
                checkForSimilar();
                return null;
            }
        }, this);
    }

    public void taskCompleted (String name, Object result)
    {
	if (name.equals("checkrepo")) {
            // display the entries and their similarity matches
            remove(_spanel);
            JPanel panel = GroupLayout.makeVBox(
                GroupLayout.NONE, GroupLayout.TOP, GroupLayout.EQUALIZE);
            ((GroupLayout)panel.getLayout()).setOffAxisJustification(
                GroupLayout.LEFT);
            panel.add(new JLabel("Select the albums to upload..."));
            panel.add(new JSeparator());
            for (int ii = 0; ii < _entries.length; ii++) {
                panel.add(new EntryUploader(
                              _entries[ii], (ArrayList)
                              _similar.get(_entries[ii].entryid)));
            }
            add(new JScrollPane(panel));

	    // enable the next button
	    _next.setEnabled(true);

	    // relay everything out
	    validate();

	} else if (name.equals("upload")) {
            _next.setEnabled(true);
            _next.setActionCommand("next");
            _next.setText("Next...");
	}
    }

    public void taskFailed (String name, Throwable exception)
    {
	if (name.equals("checkrepo")) {
	    _status.append("\nUnable to check repository for " +
                           "similar music:\n\n" + exception.getMessage());

        } else if (name.equals("readid3")) {
	    _status.append("\nFailed while reading ID3 tags:\n\n" +
                           exception.getMessage());
	}
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("cancel")) {
	    System.exit(0);

	} else if (cmd.equals("back")) {
            // pop back to the last panel
            _frame.popPanel();

	} else if (cmd.equals("upload")) {
	    _next.setEnabled(false);
            _back.setEnabled(false);
            TaskMaster.invokeTask("upload", new TaskAdapter() {
                public Object invoke () throws Exception {
                    performUpload();
                    return null;
                }
            }, this);

	} else if (cmd.equals("next")) {
	    _frame.pushPanel(new FinishedPanel());

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    protected void checkForSimilar ()
    {
        for (int ii = 0; ii < _entries.length; ii++) {
            Entry entry = _entries[ii];
            postAsyncStatus("Checking for music like '" + entry.title +
                            "' - '" +entry.artist + "'.");
            ArrayList similar = new ArrayList();
            try {
                String eartist = StringUtil.replace(entry.artist, "'", "\\'");
                Entry[] artist =
                    Importer.repository.getEntries(
                        "where soundex(artist) = soundex('" + eartist + "')");
                CollectionUtil.addAll(similar, artist);
                String etitle = StringUtil.replace(entry.title, "'", "\\'");
                Entry[] title =
                    Importer.repository.getEntries(
                        "where soundex(title) = soundex('" + etitle + "')");
                CollectionUtil.addAll(similar, title);
                _similar.put(entry.entryid, similar);

            } catch (Exception e) {
                postAsyncStatus("Choked checking repository: " + e);
            }
        }
    }

    protected void performUpload ()
    {
    }

    protected ImporterFrame _frame;
    protected JButton _next, _back;
    protected JPanel _spanel;

    protected Entry[] _entries;
    protected HashIntMap _similar = new HashIntMap();
}
