//
// $Id: ImporterFrame.java,v 1.1 2000/12/10 07:02:09 mdb Exp $

package robodj.importer;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

import com.samskivert.swing.*;

public class ImporterFrame extends JFrame
{
    public ImporterFrame ()
    {
	super("RoboDJ CD Importer");

	_top = new JPanel();
	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	_top.setLayout(gl);

	// give ourselves a wee bit of a border
	_top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	// create a container for our control buttons
	_buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

	// stick it into the frame
	_top.add(_buttonPanel, GroupLayout.FIXED);

	// now add our top-level panel (we'd not use this if we could set
	// a border on the content pane returned by the frame... alas)
	getContentPane().add(_top, BorderLayout.CENTER);
    }

    public void setPanel (ImporterPanel panel)
    {
	boolean repack = true;

	// clear out any old panel
	if (_panel != null) {
	    _top.remove(_panel);
	    // don't repack if there was an old panel
	    repack = false;
	}

	// clear out old control buttons
	clearControlButtons();

	// then stick the new panel in there
	if (panel != null) {
	    _panel = panel;
	    _panel.setBackground(Color.yellow);
	    _top.add(_panel, 0);
	    // let the panel know that it was added
	    _panel.wasAddedToFrame(this);
	}

	// and possibly repack ourselves
	if (repack) {
	    pack();
	} else {
	    validate();
	}
    }

    public JButton addControlButton (String label, String command,
				     ActionListener target)
    {
	JButton abutton = new JButton(label);
	abutton.setActionCommand(command);
	abutton.addActionListener(target);
	_buttonPanel.add(abutton);
	return abutton;
    }

    public void clearControlButtons ()
    {
	_buttonPanel.removeAll();
    }

    protected ImporterPanel _panel;
    protected JPanel _top;
    protected JPanel _buttonPanel;
}
