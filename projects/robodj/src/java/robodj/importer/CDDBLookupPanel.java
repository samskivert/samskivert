//
// $Id: CDDBLookupPanel.java,v 1.1 2000/12/10 07:02:09 mdb Exp $

package robodj.importer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.*;

import com.samskivert.net.cddb.CDDB;
import com.samskivert.swing.util.*;
import com.samskivert.swing.*;
import com.samskivert.util.StringUtil;

import robodj.convert.*;
import robodj.repository.*;

public class CDDBLookupPanel
    extends ImporterPanel
    implements ActionListener, TaskObserver
{
    public CDDBLookupPanel ()
    {
	GroupLayout gl = new HGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	setLayout(gl);

	// create our label for the left hand side
	JLabel cdlabel = new JLabel("CDDB Lookup", JLabel.CENTER);
	cdlabel.setFont(new Font("Helvetica", Font.BOLD, 24));
	add(cdlabel, GroupLayout.FIXED);

	// create the "insert cd" text label for the right hand side
	_infoText = new JTextArea("Reading CD information...");
	_infoText.setLineWrap(true);
	_infoText.setEditable(false);
	add(_infoText);
    }

    public void wasAddedToFrame (ImporterFrame frame)
    {
	// keep this for later
	_frame = frame;

	frame.addControlButton("Cancel", "cancel", this);
	_next = frame.addControlButton("Next...", "next", this);
	_next.setEnabled(false);

	// create our info task and set it a running
	Task infoTask = new Task()
	{
	    public Object invoke ()
		throws Exception
	    {
		Ripper ripper = new CDParanoiaRipper();
		return ripper.getTrackInfo();
	    }

	    public boolean abort ()
	    {
		return false;
	    }
	};
	TaskMaster.invokeTask("getinfo", infoTask, this);
    }

    public void taskCompleted (String name, Object result)
    {
	if (name.equals("getinfo")) {
	    _info = (Ripper.TrackInfo[])result;
	    final Ripper.TrackInfo[] info = _info;
	    _infoText.append("\nRead info for " + _info.length +
			     " tracks.\nDoing CDDB lookup...");

	    // create our cddb lookup task and start it up
	    Task infoTask = new Task()
	    {
		public Object invoke ()
		    throws Exception
		{
		    return CDDBUtil.doCDDBLookup("us.cddb.com", info);
		}

		public boolean abort ()
		{
		    return false;
		}
	    };
	    TaskMaster.invokeTask("cddb_lookup", infoTask, this);

	} else if (name.equals("cddb_lookup")) {
	    // get our hands on the CDDB details
	    CDDB.Detail[] details = (CDDB.Detail[])result;

	    // replace the info text with the CD info editor
	    remove(_infoText);
	    _infoEditor = new CDInfoEditor(details, _info.length);
	    add(_infoEditor);

	    // enable the next button
	    _next.setEnabled(true);

	    // relay everything out
	    validate();
	}
    }

    public void taskFailed (String name, Throwable exception)
    {
	if (name.equals("getinfo")) {
	    _infoText.append("\n\nUnable to read CD info:\n\n" +
			     exception.getMessage());

	} else if (name.equals("cddb_lookup")) {
	    _infoText.append("\n\nCDDB lookup failed: " +
			     exception.getMessage());
	}
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("cancel")) {
	    System.exit(0);

	} else if (cmd.equals("next")) {
	    extractEntryAndContinue();

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    protected void extractEntryAndContinue ()
    {
	Entry entry = new Entry();
	entry.title = _infoEditor.getTitle();
	if (!validate(entry.title, "Title cannot be blank.")) {
	    return;
	}

	entry.artist = _infoEditor.getArtist();
	if (!validate(entry.artist, "Artist cannot be blank.")) {
	    return;
	}

	String[] names = _infoEditor.getTrackNames();
	entry.songs = new Song[names.length];
	for (int i = 0; i < names.length; i++) {
	    if (!validate(names[i], "Track " + (i+1) +
			  " cannot be blank.")) {
		return;
	    }
	    Song song = (entry.songs[i] = new Song());
	    song.title = names[i];
	    song.position = i+1;
	}

	// create the rip panel and pass the entry and info along
	RipPanel panel = new RipPanel(_info, entry);
	_frame.setPanel(panel);
    }

    protected boolean validate (String value, String errmsg)
    {
	if (StringUtil.blank(value)) {
	    JOptionPane.showMessageDialog(SwingUtilities.getRootPane(this),
					  errmsg, "Invalid data provided",
					  JOptionPane.WARNING_MESSAGE);
	    return false;
	}

	return true;
    }

    protected ImporterFrame _frame;
    protected JButton _next;
    protected JTextArea _infoText;
    protected CDInfoEditor _infoEditor;

    protected Ripper.TrackInfo[] _info;
}
