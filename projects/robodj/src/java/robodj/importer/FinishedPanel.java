//
// $Id: FinishedPanel.java,v 1.4 2002/03/03 21:17:03 mdb Exp $

package robodj.importer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.*;

import com.samskivert.swing.*;
import robodj.repository.*;

public class FinishedPanel
    extends ImporterPanel
    implements ActionListener
{
    public FinishedPanel ()
    {
	GroupLayout gl = new HGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	setLayout(gl);

	// create our label for the left hand side
	JLabel cdlabel = new JLabel("Finished", JLabel.CENTER);
	cdlabel.setFont(new Font("Helvetica", Font.BOLD, 24));
	add(cdlabel, GroupLayout.FIXED);

        // create a panel for the option buttons
        GroupLayout vgl = new VGroupLayout(GroupLayout.NONE);
        vgl.setOffAxisPolicy(GroupLayout.NONE);
        vgl.setJustification(GroupLayout.CENTER);
        JPanel ppanel = new JPanel(vgl);

        // create our action buttons
        String[] labels = new String[]
            { "Import more music", "Edit/categorize CDs" };
        String[] actions = new String[] { "import", "edit" };

        for (int i = 0; i < labels.length; i++) {
            JButton abutton = new JButton(labels[i]);
            abutton.setActionCommand(actions[i]);
            abutton.addActionListener(this);
            ppanel.add(abutton);
        }

        // add our panel to the main group
        add(ppanel);
    }

    public void wasAddedToFrame (ImporterFrame frame, boolean popped)
    {
	frame.addControlButton("Exit", "exit", this);
	frame.addControlButton("Another", "import", this);

	// keep a handle on this for later
	_frame = frame;
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("exit")) {
	    System.exit(0);

	} else if (cmd.equals("import")) {
	    _frame.setPanel(new StartPanel());

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    protected ImporterFrame _frame;
}
