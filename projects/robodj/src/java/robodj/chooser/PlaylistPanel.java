//
// $Id: PlaylistPanel.java,v 1.1 2001/07/12 23:06:55 mdb Exp $

package robodj.chooser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import com.samskivert.swing.*;

public class PlaylistPanel
    extends JPanel
    implements ActionListener
{
    public PlaylistPanel ()
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
	JScrollPane bscroll = new JScrollPane(_bpanel);
        add(bscroll);

        // add our navigation button
        _clearbut = new JButton("Clear");
        _clearbut.setActionCommand("clear");
        _clearbut.addActionListener(this);
        add(_clearbut, GroupLayout.FIXED);
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("...")) {

        } else if (cmd.equals("clear")) {
            Chooser.scontrol.clear();
        }
    }

    protected JPanel _bpanel;
    protected JButton _clearbut;
}
