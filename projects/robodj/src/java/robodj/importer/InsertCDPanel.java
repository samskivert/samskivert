//
// $Id: InsertCDPanel.java,v 1.3 2002/03/03 21:17:03 mdb Exp $

package robodj.importer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.*;

public class InsertCDPanel
    extends ImporterPanel
    implements ActionListener
{
    public InsertCDPanel ()
    {
	super(new GridLayout(1, 2));

	// create our image for the left hand side
	ClassLoader cl = getClass().getClassLoader();
	URL cdurl = cl.getResource("robodj/importer/cd.jpg");
	ImageIcon cdicon = new ImageIcon(cdurl);
	JLabel cdlabel = new JLabel(cdicon);
	add(cdlabel);

	// create the "insert cd" text label for the right hand side
	JLabel ilabel = new JLabel("Please insert the CD...",
				   JLabel.CENTER);
	add(ilabel);

	// give ourselves a wee bit of a border
	setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    public void wasAddedToFrame (ImporterFrame frame, boolean popped)
    {
	frame.addControlButton("Cancel", "cancel", this);
	frame.addControlButton("Next...", "next", this);

	// keep a handle on this for later
	_frame = frame;
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("cancel")) {
	    System.exit(0);

	} else if (cmd.equals("next")) {
	    _frame.pushPanel(new CDDBLookupPanel());

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    protected ImporterFrame _frame;
}
