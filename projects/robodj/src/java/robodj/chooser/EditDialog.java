//
// $Id: EditDialog.java,v 1.1 2001/07/26 00:24:22 mdb Exp $

package robodj.chooser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import java.sql.SQLException;

import com.samskivert.swing.*;
import com.samskivert.swing.util.*;

import robodj.Log;
import robodj.repository.Entry;
import robodj.repository.EntryEditor;

public class EditDialog
    extends JDialog
    implements TaskObserver, ActionListener
{
    public EditDialog (Entry entry)
    {
        setTitle("Edit " + entry.title);

        // keep this around for later
        _entry = entry;

        // we create a top-level panel to manage everything
	JPanel top = new JPanel();
	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	top.setLayout(gl);

	// give ourselves a wee bit of a border
	top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // create our entry editor
        _editor = new EntryEditor(Chooser.model, entry);
        top.add(_editor);

        // create some control buttons
        gl = new HGroupLayout(GroupLayout.NONE);
        gl.setJustification(GroupLayout.RIGHT);
        JPanel btnPanel = new JPanel(gl);
        btnPanel.add(createControlButton("Update", "update"));
        btnPanel.add(createControlButton("Revert", "revert"));
        btnPanel.add(createControlButton("Cancel", "cancel"));
        top.add(btnPanel, GroupLayout.FIXED);

	getContentPane().add(top, BorderLayout.CENTER);
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("update")) {
            // flush the edits to the entry
            _editor.applyToEntry();
            // do the update in a separate task
            TaskMaster.invokeMethodTask("updateEntry", this, this);

        } else if (cmd.equals("revert")) {
            _editor.reset();

        } else if (cmd.equals("cancel")) {
            // get the hell out of dodge
            dispose();

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    /**
     * Updates the entry in the repository.
     */
    public void updateEntry ()
        throws SQLException
    {
        Chooser.model.updateEntry(_entry);
    }

    public void taskCompleted (String name, Object result)
    {
	if (name.equals("updateEntry")) {
            // we're done updating, we can go away now
            dispose();
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
        JOptionPane.showMessageDialog(this, msg, "Error",
                                      JOptionPane.ERROR_MESSAGE); 
        Log.logStackTrace(exception);
    }

    protected JButton createControlButton (String label, String action)
    {
        JButton btn = new JButton(label);
        btn.setActionCommand(action);
        btn.addActionListener(this);
        return btn;
    }

    protected Entry _entry;
    protected EntryEditor _editor;
}
