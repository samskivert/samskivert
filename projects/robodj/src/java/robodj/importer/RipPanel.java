//
// $Id: RipPanel.java,v 1.1 2000/12/10 07:02:09 mdb Exp $

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
	JLabel cdlabel = new JLabel("Ripping", JLabel.CENTER);
	cdlabel.setFont(new Font("Helvetica", Font.BOLD, 24));
	add(cdlabel, GroupLayout.FIXED);

	// save this stuff for later
	_info = info;
	_entry = entry;
    }

    public void wasAddedToFrame (ImporterFrame frame)
    {
	// keep this for later
	_frame = frame;

	frame.addControlButton("Cancel", "cancel", this);
	_next = frame.addControlButton("Next...", "next", this);
	_next.setEnabled(false);
    }

    public void taskCompleted (String name, Object result)
    {
	if (name.equals("getinfo")) {

	} else if (name.equals("cddb_lookup")) {
	}
    }

    public void taskFailed (String name, Throwable exception)
    {
	if (name.equals("getinfo")) {

	} else if (name.equals("cddb_lookup")) {
	}
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("cancel")) {
	    System.exit(0);

	} else if (cmd.equals("next")) {

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    protected ImporterFrame _frame;
    protected JButton _next;

    protected Ripper.TrackInfo[] _info;
    protected Entry _entry;
}
