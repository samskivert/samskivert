//
// $Id: FinishedPanel.java,v 1.2 2001/06/07 08:37:47 mdb Exp $

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
    public FinishedPanel (Entry entry)
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
            { "Import another CD", "Edit/categorize CDs" };
        String[] actions = new String[] { "import", "edit" };

        for (int i = 0; i < labels.length; i++) {
            JButton abutton = new JButton(labels[i]);
            abutton.setActionCommand(actions[i]);
            abutton.addActionListener(this);
            ppanel.add(abutton);
        }

        // add our panel to the main group
        add(ppanel);

        // save this guy for later
        _entry = entry;
    }

    public void wasAddedToFrame (ImporterFrame frame)
    {
	frame.addControlButton("Another", "import", this);
	frame.addControlButton("Exit", "exit", this);

	// keep a handle on this for later
	_frame = frame;
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("exit")) {
	    System.exit(0);

	} else if (cmd.equals("import")) {
	    _frame.setPanel(new InsertCDPanel());

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    protected ImporterFrame _frame;
    protected Entry _entry;
}
