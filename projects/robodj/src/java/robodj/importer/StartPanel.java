//
// $Id: StartPanel.java,v 1.3 2002/03/03 21:17:03 mdb Exp $

package robodj.importer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.*;

import com.samskivert.swing.VGroupLayout;

public class StartPanel extends ImporterPanel
    implements ActionListener
{
    public StartPanel ()
    {
	super(new GridLayout(1, 2));

	// create our image for the left hand side
	ClassLoader cl = getClass().getClassLoader();
	URL cdurl = cl.getResource("robodj/importer/cd.jpg");
	ImageIcon cdicon = new ImageIcon(cdurl);
	JLabel cdlabel = new JLabel(cdicon);
	add(cdlabel);

        // allow the user to select whether they want to rip a CD or
	// import music directly
        VGroupLayout vgl = new VGroupLayout();
        vgl.setOffAxisJustification(VGroupLayout.LEFT);
        JPanel bpanel = new JPanel(vgl);
        bpanel.add(_rip = new JRadioButton("Rip a CD (insert CD now)"));
        _rip.setMnemonic(KeyEvent.VK_R);
        _rip.setSelected(true);
        bpanel.add(_imp = new JRadioButton("Import music"));
        _imp.setMnemonic(KeyEvent.VK_M);
        add(bpanel);

        ButtonGroup group = new ButtonGroup();
        group.add(_imp);
        group.add(_rip);

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
            if (_rip.isSelected()) {
                _frame.pushPanel(new CDDBLookupPanel());
            } else {
                _frame.pushPanel(new ImportMusicPanel());
            }

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    protected JRadioButton _imp, _rip;
    protected ImporterFrame _frame;
}
