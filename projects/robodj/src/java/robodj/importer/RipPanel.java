//
// $Id: RipPanel.java,v 1.7 2002/03/03 21:17:03 mdb Exp $

package robodj.importer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.*;

import com.samskivert.net.cddb.CDDB;
import com.samskivert.swing.util.*;
import com.samskivert.swing.*;
import com.samskivert.util.StringUtil;

import robodj.Log;
import robodj.convert.*;
import robodj.repository.*;

public class RipPanel
    extends ImporterPanel
    implements ActionListener, TaskObserver
{
    public RipPanel (Ripper.TrackInfo[] info, Entry entry)
    {
	GroupLayout gl = new HGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	setLayout(gl);

	// create our label for the left hand side
	JLabel cdlabel = new JLabel("Converting", JLabel.CENTER);
	cdlabel.setFont(new Font("Helvetica", Font.BOLD, 24));
	add(cdlabel, GroupLayout.FIXED);

        // create a panel for the progress display
        GroupLayout vgl = new VGroupLayout(GroupLayout.STRETCH);
        vgl.setOffAxisPolicy(GroupLayout.STRETCH);
        JPanel ppanel = new JPanel(vgl);

        // create the overall progress indicator
        JLabel opl = new JLabel("Overall progress", JLabel.LEFT);
        ppanel.add(opl, GroupLayout.FIXED);
        // overall progress assumes equal time spent ripping and encoding
        // each track on the disc. not entirely accurate, but reasonable
        // for giving a general idea of how far along we are
        _oprogress = new JProgressBar(0, 200*info.length);
        ppanel.add(_oprogress, GroupLayout.FIXED);

        // create the task progress indicator
        JLabel tpl = new JLabel("Track progress", JLabel.LEFT);
        ppanel.add(tpl, GroupLayout.FIXED);
        _progress = new JProgressBar(0, 100);
        ppanel.add(_progress, GroupLayout.FIXED);

        // create the status log
        JLabel sl = new JLabel("Status", JLabel.LEFT);
        ppanel.add(sl, GroupLayout.FIXED);
	_statusLog = new JTextArea();
	_statusLog.setLineWrap(true);
	_statusLog.setEditable(false);
	// make something for it to scroll around in
	ppanel.add(new JScrollPane(_statusLog));

        // add our panel to the main group
        add(ppanel);

	// save this stuff for later
	_info = info;
	_entry = entry;
    }

    protected void postAsyncStatus (final String status)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run ()
            {
                _statusLog.append(status + "\n");
            }
        });
    }

    protected void postAsyncProgress (final int overallComplete,
                                      final int taskComplete)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run ()
            {
                // update our progress indicators
                _oprogress.setValue(overallComplete);
                _progress.setValue(taskComplete);
            }
        });
    }

    protected static String createTempPath (int trackno, String ext)
    {
        return "/tmp/track" + trackno + "." + ext;
    }

    /** Handles the ripping and encoding of the tracks. */
    protected class RipperTask
        implements Task, ConversionProgressListener
    {
        public RipperTask (Ripper.TrackInfo[] info, Entry entry)
        {
            _info = info;
            _entry = entry;
        }

        public Object invoke ()
            throws Exception
        {
            Ripper ripper = new CDParanoiaRipper();
            Encoder encoder = new LameEncoder();
            Tagger tagger = new ID3Tagger();

            for (int i = 0; i < _info.length; i++) {
                _track = i+1;

                // rip the track
                _encoding = false;

                // it takes a second or so for the ripper to start
                // reporting progress, so we clear out the progress
                // indicator so that the 100% progress of the previous
                // track doesn't look like it applies to this new track
                // while we're spinning up
                updateProgress(0);

                // start the ripping
                postAsyncStatus("Ripping track " + _track + "...");
                String tpath = createTempPath(_track, "wav");
                try {
                    ripper.ripTrack(_info, _track, tpath, this);

                } catch (Exception e) {
                    String errmsg = "Failure occurred while ripping track " +
                        _track + ". Attempting to encode anyway. You can " +
                        "try re-ripping this track later with the " +
                        "'quickrip' script provided with the importer. The " +
                        "branchdir and entryid needed by quickrip will be " +
                        "reported at the completion of the ripping process.";
                    postAsyncStatus(errmsg);
                }

                // then encode it
                _encoding = true;
                updateProgress(0);
                postAsyncStatus("Encoding track " + _track + "...");
                String dpath = createTempPath(_track, "mp3");
                encoder.encodeTrack(tpath, dpath, this);

                // finally ID3 tag it
                postAsyncStatus("Tagging track " + _track + "...");
                tagger.idTrack(dpath, _entry.artist, _entry.title,
                               _entry.songs[i].title, _track);

                // remove the source wav file
                File tfile = new File(tpath);
                if (!tfile.delete()) {
                    postAsyncStatus("Unable to remove temp file " +
                                    "for track " + _track + ".");
                }
            }

            return null;
        }

        public boolean abort ()
        {
            // no abort presently
            return false;
        }            

        public void updateProgress (int percentComplete)
        {
            // first add completeness for tracks we've already done
            int overallComplete = (_track-1) * 200;
            // if we're encoding, then we finished ripping this track
            if (_encoding) {
                overallComplete += 100;
            }
            // finally add the percent complete for the current task
            overallComplete += percentComplete;
            // and pass it all on to the UI for display
            postAsyncProgress(overallComplete, percentComplete);
        }

        protected Ripper.TrackInfo[] _info;
        protected Entry _entry;
        protected int _track;
        protected boolean _encoding;
    }

    /** Commits the completed entry into the repository. */
    protected class CommitterTask
        implements Task
    {
        public CommitterTask (Entry entry, Ripper.TrackInfo[] info)
        {
            _entry = entry;
            _info = info;
        }

        public Object invoke ()
            throws Exception
        {
            int entryid = -1;

            try {
                // determine the target directory for the mp3 files
                String rbase =
                    Importer.config.getProperty("repository.basedir");
                if (StringUtil.blank(rbase)) {
                    throw new Exception("No mp3 repository directory " +
                                        "specified.");
                }

                // ensure the repository directory exists
                File rbf = new File(rbase);
                if (!rbf.exists() || !rbf.isDirectory()) {
                    throw new Exception("Repository base directory '" +
                                        rbase + "' is not valid.");
                }

                postAsyncStatus("Creating repository directory.");
                // create the hash directory
                int hcode = _entry.title.hashCode();
                if (hcode < 0) {
                    hcode *= -1;
                }
                System.out.println(Integer.toHexString(255 % 0xFF));
                String hash = Integer.toHexString(hcode % 0xFF);
                StringBuffer tpath = new StringBuffer(rbase);
                if (tpath.charAt(tpath.length()-1) != '/') {
                    tpath.append("/");
                }
                tpath.append(hash);

                File tdir = new File(tpath.toString());
                if (!tdir.exists()) {
                    if (!tdir.mkdir()) {
                        throw new Exception("Unable to create target " +
                                            "directory: " + tpath);
                    }

                } else {
                    // make sure it's a directory
                    if (!tdir.isDirectory()) {
                        throw new Exception("Target directory '" + tpath +
                                            "' is not valid.");
                    }
                }

                // report the branch directory
                postAsyncStatus("Branch directory: " + hash);

                // fill in the entry source since we ripped this from CD
                _entry.source = Importer.ENTRY_SOURCE;

                // fill in blanks for the locations of the tunes because
                // we won't know the name of the directory until we are
                // assigned an entry id
                for (int i = 0; i < _entry.songs.length; i++) {
                    _entry.songs[i].location = "";
                }

                postAsyncStatus("Creating database entry.");
                // insert the entry into the repository so that we get the
                // proper entry id which we'll need to put the tracks in their
                // proper place in the repository
                RetryableTask iop = new RetryableTask() {
                    public void invoke () throws Exception
                    {
                        Importer.repository.insertEntry(_entry);
                    }
                };
                iop.invokeTask(_frame, DB_FAILURE_MSG);
                // track this so that we can clean up in case of failure
                entryid = _entry.entryid;

                // now that we have an entry id, fully construct the
                // target path
                tpath.append("/").append(_entry.entryid);
                tdir = new File(tpath.toString());
                if (!tdir.mkdir()) {
                    throw new Exception("Unable to create target " +
                                        "directory: " + tpath);
                }

                // report the entry id
                postAsyncStatus("Entry ID: " + _entry.entryid);

                postAsyncStatus("Moving tracks into repository directory.");
                // move the tracks into the target directory
                tpath.append("/");
                for (int i = 0; i < _entry.songs.length; i++) {
                    int tno = i+1;

                    // figure out the necessary paths
                    String npath = tpath.toString() + pad(tno) + ".mp3";
                    String opath = createTempPath(tno, "mp3");

                    // move the file
                    File ofile = new File(opath);
                    File nfile = new File(npath);
                    if (!ofile.renameTo(nfile)) {
                        throw new Exception("Unable to move track " + tno +
                                            " to target location: " + npath);
                    }

                    // and update the song object
                    _entry.songs[i].location = npath;
                }

                // finally update the entry in the database
                RetryableTask uop = new RetryableTask() {
                    public void invoke () throws Exception
                    {
                        Importer.repository.updateEntry(_entry);
                    }
                };
                uop.invokeTask(_frame, DB_FAILURE_MSG);
                // and clear out the entryid to indicate success
                entryid = -1;

                postAsyncStatus("Import complete.");
                return null;

            } finally {
                // if we got halfway through creating our entry, remove it
                // from the database
                if (entryid != -1) {
                    Importer.repository.deleteEntry(_entry);
                }
            }
        }

        public boolean abort ()
        {
            // we don't support aborting. this shouldn't take long at all
            return false;
        }

        protected Entry _entry;
        protected Ripper.TrackInfo[] _info;
    }

    public void wasAddedToFrame (ImporterFrame frame, boolean popped)
    {
	// keep this for later
	_frame = frame;

        // add our next and cancel buttons
	_cancel = frame.addControlButton("Cancel", "cancel", this);
	_next = frame.addControlButton("Next...", "next", this);
	_next.setEnabled(popped);

        if (!popped) {
            // create our info task and set it a running
            Task ripTask = new RipperTask(_info, _entry);
            TaskMaster.invokeTask("convert", ripTask, this);
        }
    }

    public void taskCompleted (String name, Object result)
    {
	if (name.equals("convert")) {
            // yay! we're all done. commit the entry to the repository
            Task commitTask = new CommitterTask(_entry, _info);
            TaskMaster.invokeTask("commit", commitTask, this);

	} else if (name.equals("commit")) {
            // we're all done. fix up the buttons and call it good
            _cancel.setEnabled(false);
            _next.setEnabled(true);
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
        _statusLog.append("Error: " + msg + "\n");
        Log.logStackTrace(exception);
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("cancel")) {
	    System.exit(0);

	} else if (cmd.equals("next")) {
	    _frame.pushPanel(new FinishedPanel(_entry));

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    protected static String pad (int trackno)
    {
        if (trackno < 10) {
            return "0" + trackno;
        } else {
            return Integer.toString(trackno);
        }
    }

    protected ImporterFrame _frame;
    protected JButton _next;
    protected JButton _cancel;
    protected JLabel _actionLabel;

    protected JProgressBar _oprogress;
    protected JProgressBar _progress;
    protected JTextArea _statusLog;

    protected Ripper.TrackInfo[] _info;
    protected Entry _entry;

    protected static final String DB_FAILURE_MSG =
        "An error occurred while communicating with the database. You " +
        "may wish to examine the following error message, remedy the " +
        "problem with the database and retry the operation. Or you can " +
        "abort the operation and cancel the import process entirely.";
}
