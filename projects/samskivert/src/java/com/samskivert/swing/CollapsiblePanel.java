//
// $Id: CollapsiblePanel.java,v 1.1 2002/07/04 04:42:11 ray Exp $

package com.samskivert.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JButton;

/**
 * A panel that contains a button which will collapse the rest of the content.
 */
public class CollapsiblePanel extends JPanel
    implements ActionListener
{
    /**
     * Construct a collapsible panel with the specified button as
     * the trigger. The text of the button will be used as the triggertext.
     */
    public CollapsiblePanel (JButton trigger)
    {
        _trigger = trigger;
        _text = trigger.getText();

        VGroupLayout gl = new VGroupLayout(VGroupLayout.NONE);
        gl.setOffAxisPolicy(VGroupLayout.STRETCH);
        gl.setGap(0);
        gl.setJustification(VGroupLayout.TOP);
        gl.setOffAxisJustification(VGroupLayout.LEFT);
        setLayout(gl);

        _trigger.addActionListener(this);

        // these are our only two components.
        add(_trigger);
        add(_content);

        // and start us out showing
        setCollapsed(false);
    }

    /**
     * Construct a collapsible panel with the specified button text.
     */
    public CollapsiblePanel (String triggertext)
    {
        this(new JButton(triggertext));
    }

    /**
     * Get the content panel for filling in with sweet content goodness.
     */
    public JPanel getContent ()
    {
        return _content;
    }

    // documentation from interface ActionListener
    public void actionPerformed (ActionEvent e)
    {
        if (e.getSource() == _trigger) {
            setCollapsed(!isCollapsed());
        }
    }

    /**
     * Is the panel collapsed?
     */
    public boolean isCollapsed ()
    {
        return !_content.isVisible();
    }

    /**
     * Set the collapsion state.
     */
    public void setCollapsed (boolean collapse)
    {
        if (collapse) {
            _content.hide();
            _trigger.setText("+ " + _text);
        } else {
            _content.show();
            _trigger.setText("- " + _text);
        }
    }

    /** The button that triggers collapsion. */
    protected JButton _trigger;

    /** The original text in the button, oh this's gonna have to change. */
    protected String _text;

    /** The who in the what now? */
    protected JPanel _content = new JPanel();
}
