//
// $Id: ImporterFrame.java,v 1.8 2002/03/03 21:17:03 mdb Exp $

package robodj.importer;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;

import com.samskivert.swing.*;

import robodj.Version;

public class ImporterFrame extends JFrame
{
    public ImporterFrame ()
    {
	super("RoboDJ CD Importer " + Version.RELEASE_VERSION);

        // quit if we're closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

	_top = new JPanel();
	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	_top.setLayout(gl);

	// give ourselves a wee bit of a border
	_top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	// create a container for our control buttons
        GroupLayout bgl = new HGroupLayout(GroupLayout.NONE);
        bgl.setJustification(GroupLayout.RIGHT);
	_buttonPanel = new JPanel(bgl);

	// stick it into the frame
	_top.add(_buttonPanel, GroupLayout.FIXED);

	// now add our top-level panel (we'd not use this if we could set
	// a border on the content pane returned by the frame... alas)
	getContentPane().add(_top, BorderLayout.CENTER);
    }

    /**
     * Clears out the panel stack and pushes the specified panel onto the
     * fresh stack.
     */
    public void setPanel (ImporterPanel panel)
    {
        _panels.clear();
        pushPanel(panel);
    }

    /**
     * Pushes the specified panel onto the stack (removing the previous
     * panel and keeping it around in case we want to go back to it.
     */
    public void pushPanel (ImporterPanel panel)
    {
	// push the current panel onto the stack
	if (_panel != null) {
            _panels.add(_panel);
	    _top.remove(_panel);
	}

	// clear out old control buttons
	clearControlButtons();

	// then stick the new panel in there
	if (panel != null) {
	    _panel = panel;
	    _top.add(_panel, 0);
	    // let the panel know that it was added
	    _panel.wasAddedToFrame(this, false);
	}

	// and lay out the new panel
        validate();
        repaint();
    }

    /**
     * Pops back to the previous panel on the stack.
     */
    public void popPanel ()
    {
        // remove the old panel
	if (_panel != null) {
	    _top.remove(_panel);
	}

	// clear out old control buttons
	clearControlButtons();

        // now pop the last panel from the stack
        int pcount = _panels.size();
        if (pcount > 0) {
            _panel = (ImporterPanel)_panels.get(pcount-1);
            _panels.remove(pcount-1);
            _top.add(_panel, 0);
	    // let the panel know that it was added
	    _panel.wasAddedToFrame(this, true);
        }

        // and lay out the new panel
        validate();
        repaint();
    }

    public JButton addControlButton (String label, String command,
				     ActionListener target)
    {
	JButton abutton = new JButton(label);
	abutton.setActionCommand(command);
	abutton.addActionListener(target);
	_buttonPanel.add(abutton);
        // swing doesn't automatically validate after adding/removing
        // children
        _buttonPanel.validate();
	return abutton;
    }

    public void clearControlButtons ()
    {
	_buttonPanel.removeAll();
        // swing doesn't automatically validate after adding/removing
        // children
        _buttonPanel.validate();
    }

    protected ImporterPanel _panel;
    protected JPanel _top;
    protected JPanel _buttonPanel;

    protected ArrayList _panels = new ArrayList();
}
