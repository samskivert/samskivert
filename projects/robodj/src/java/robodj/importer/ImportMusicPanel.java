//
// $Id: ImportMusicPanel.java,v 1.7 2003/05/04 18:16:07 mdb Exp $

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

public class ImportMusicPanel extends ImporterPanel
    implements ActionListener, TaskObserver
{
    public ImportMusicPanel ()
    {
	GroupLayout gl = new HGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	setLayout(gl);

	// create our label for the left hand side
	JLabel cdlabel = new JLabel("Import Music", JLabel.CENTER);
	cdlabel.setFont(new Font("Helvetica", Font.BOLD, 24));
	add(cdlabel, GroupLayout.FIXED);

        // on the right hand side we want a status display to let the user
        // know how the ID3 reading process is going
        GroupLayout vgl = new VGroupLayout(GroupLayout.STRETCH);
        vgl.setOffAxisPolicy(GroupLayout.STRETCH);
        _spanel = new JPanel(vgl);

        JLabel sl = new JLabel("Status", JLabel.LEFT);
        _spanel.add(sl, GroupLayout.FIXED);

        // set up our status display
	_status.setText("Ready to import music...\n");
	_spanel.add(new JScrollPane(_status));

        // add our panel to the main group
        add(_spanel);
    }

    public void wasAddedToFrame (ImporterFrame frame, boolean popped)
    {
	// keep this for later
	_frame = frame;

	frame.addControlButton("Cancel", "cancel", this);
	frame.addControlButton("Back", "back", this);
	_next = frame.addControlButton("Next...", "next", this);
	_next.setEnabled(popped);

        if (popped) {
            // clear out our song arrays
            for (Iterator iter = _byid.values().iterator(); iter.hasNext(); ) {
                ((Entry)iter.next()).songs = null;
            }

        } else  {
            // create a file chooser dialog
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setMultiSelectionEnabled(true);
            chooser.setDialogTitle("Select files and directories to import...");

            int rv = chooser.showDialog(this, "Import music");
            if (rv == JFileChooser.APPROVE_OPTION) {
                // start scanning those directories for MP3 files
                final File[] files = chooser.getSelectedFiles();
                TaskMaster.invokeTask("scan", new TaskAdapter() {
                    public Object invoke () throws Exception {
                        return scanForFiles(files);
                    }
                }, this);

            } else {
                // pop back to the last panel
                _frame.popPanel();
            }
        }
    }

    public void taskCompleted (String name, Object result)
    {
	if (name.equals("scan")) {
            final File[] files = (File[])result;
            // now fire up a task to read the ID3 info from these files
            TaskMaster.invokeTask("readid3", new TaskAdapter() {
                public Object invoke () throws Exception {
                    readID3(files);
                    return null;
                }
            }, this);

	} else if (name.equals("readid3")) {
            // replace the info text with the track info table
            remove(_spanel);
            JPanel bits = new JPanel(new BorderLayout(5, 5));
            bits.add(new JLabel("Edit metadata"), BorderLayout.NORTH);
            JTable table = new JTable(
                new SongTableModel(_byid, _byname, _songs));
            bits.add(new JScrollPane(table), BorderLayout.CENTER);
            bits.add(new JLabel("You will select which albums to upload " +
                                "on the next page..."), BorderLayout.SOUTH);
            add(bits);
            SwingUtil.sizeToContents(table);

	    // enable the next button
	    _next.setEnabled(true);

	    // relay everything out
	    validate();
	}
    }

    public void taskFailed (String name, Throwable exception)
    {
	if (name.equals("scan")) {
	    _status.append("\nUnable to scan for MP3 files:\n\n" +
			     exception.getMessage());

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

	} else if (cmd.equals("next")) {
	    entrifyAndContinue();

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    /**
     * Scans the supplied set of files and directories for all files that
     * appear to be MP3s.
     *
     * @return an array of all matched files.
     */
    protected File[] scanForFiles (File[] seeds)
    {
        ArrayList matches = new ArrayList();
        scanForFiles(seeds, matches);
        return (File[])matches.toArray(new File[matches.size()]);
    }

    protected void scanForFiles (File[] seeds, ArrayList matches)
    {
        for (int ii = 0; ii < seeds.length; ii++) {
            if (seeds[ii].isDirectory()) {
                postAsyncStatus("Scanning " + seeds[ii] + "...");
                scanForFiles(seeds[ii].listFiles(), matches);
            } else if (seeds[ii].getName().toLowerCase().endsWith(".mp3")) {
                matches.add(seeds[ii]);
            }
        }
    }

    protected void readID3 (File[] files)
    {
        for (int ii = 0; ii < files.length; ii++) {
            try {
                MP3File mpf = new MP3File(files[ii]);
                String title = getID3Info(mpf, "TIT2", "title");
                String artist = getID3Info(mpf, "TPE1", "artist");
                String album = getID3Info(mpf, "TALB", "album");
                String track = getID3Info(mpf, "TRCK", "");

                // locate the Entry associated with this album/artist,
                // creating a new one if necessary
                String key = SongTableModel.getKey(album, artist);
                Entry entry = (Entry)_byname.get(key);
                if (entry == null) {
                    entry = new Entry();
                    entry.entryid = _byname.size(); // assign a temporary id
                    entry.artist = nullToUnknown(artist);
                    entry.title = nullToUnknown(album);
                    entry.source = "import";
                    _byname.put(key, entry);
                    _byid.put(entry.entryid, entry);
                    postAsyncStatus("Located album '" + album + "' by '" +
                                    artist + "'.");
                }

                Song song = new Song();
                song.entryid = entry.entryid;
                song.title = StringUtil.blank(title) ?
                    stripSuffix(files[ii].getName()) : title;
                song.location = files[ii].toString();

                // try to figure out what the track number is
                if (StringUtil.blank(track)) {
                    Matcher m = _trackNo.matcher(files[ii].getName());
                    if (m.matches()) {
                        track = m.group(1);
                    }
                }
                try {
                    song.position = Integer.parseInt(track);
                } catch (Throwable t) {
                    song.position = 1;
                }

                // finally slap that song on the list
                _songs.add(song);
                postAsyncStatus("Located song '" + title + "' " +
                                "(" + song.position + ").");

            } catch (Exception e) {
                postAsyncStatus("Failure processing " + files[ii] + ": " + e);
                e.printStackTrace(System.err);
            }
        }
    }

    protected String nullToUnknown (String value)
    {
        return StringUtil.blank(value) ? "Unknown" : value;
    }

    protected String stripSuffix (String value)
    {
        int didx = value.lastIndexOf(".");
        return (didx == -1) ? value : value.substring(0, didx);
    }

    protected String getID3Info (MP3File file, String v2tag, String v1tag)
    {
        // first look for the v2 tag
        AbstractID3v2 tag2 = file.getID3v2Tag();
        String value = null;
        if (tag2 != null) {
            AbstractMP3Fragment frame = tag2.getFrame(v2tag);
            if (frame != null) {
                value = ((AbstractFrameBodyTextInformation)
                         frame.getBody()).getText();
            }
            if (!StringUtil.blank(value)) {
                return value;
            }
        }

        // if that fails, try the v1 tag
        ID3v1 tag1 = file.getID3v1Tag();
        if (tag1 != null) {
            if (v1tag.equals("album")) {
                return tag1.getAlbum();
            } else if (v1tag.equals("artist")) {
                return tag1.getArtist();
            } else if (v1tag.equals("title")) {
                return tag1.getTitle();
            }
        }

        return null;
    }

    protected void entrifyAndContinue ()
    {
        // go through our songs and collect them into their Entry objects
        ArrayList elist = new ArrayList();
        for (Iterator iter = _songs.iterator(); iter.hasNext(); ) {
            Song song = (Song)iter.next();
            Entry entry = (Entry)_byid.get(song.entryid);
            if (entry.songs == null) {
                entry.songs = new Song[] { song };
            } else {
                entry.songs = (Song[])ArrayUtil.append(entry.songs, song);
            }
            if (!elist.contains(entry)) {
                elist.add(entry);
            }
        }

	// create the upload panel and pass the entries along
        Entry[] entries = (Entry[])elist.toArray(new Entry[elist.size()]);
 	_frame.pushPanel(new UploadMusicPanel(entries));
    }

    protected ImporterFrame _frame;
    protected JButton _next;
    protected JPanel _spanel;

    protected HashMap _byname = new HashMap();
    protected HashIntMap _byid = new HashIntMap();
    protected ArrayList _songs = new ArrayList();

    protected static Pattern _trackNo;
    static {
        try {
            _trackNo = Pattern.compile("\\s(\\d+)\\s");
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }
}
