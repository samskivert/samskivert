//
// $Id: CollapsiblePanel.java,v 1.2 2002/07/09 21:52:02 ray Exp $

package com.samskivert.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

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
        setTriggerContainer(trigger);
        setTrigger(trigger);
    }

    /**
     * Construct a collapsible panel with the specified button text.
     */
    public CollapsiblePanel (String triggertext)
    {
        this(new JButton(triggertext));
    }

    /**
     * Create a collapsible panel to which the trigger button will be
     * added later.
     */
    public CollapsiblePanel ()
    {
        _gl = new VGroupLayout(VGroupLayout.NONE);
        _gl.setOffAxisPolicy(VGroupLayout.STRETCH);
        _gl.setGap(0);
        _gl.setJustification(VGroupLayout.TOP);
        _gl.setOffAxisJustification(VGroupLayout.LEFT);
        setLayout(_gl);
    }

    /**
     * Set a component which contains the trigger button.
     * The simple case is to just set the trigger button as this component.
     */
    public void setTriggerContainer (JComponent comp)
    {
        // these are our only two components.
        add(comp);
        add(_content);

        // and start us out not showing
        setCollapsed(true);
    }

    /**
     * Set the trigger button.
     */
    public void setTrigger (JButton trigger)
    {
        _trigger = trigger;
        _text = trigger.getText();
        _trigger.addActionListener(this);
    }

    /**
     * Set the gap between the trigger button and the rest of the content.
     * Can be negative for an overlapping effect.
     */
    public void setGap (int gap)
    {
        _gl.setGap(gap);
        invalidate();
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

    /** Our layout. */
    protected VGroupLayout _gl;

    /** The button that triggers collapsion. */
    protected JButton _trigger;

    /** The original text in the button, oh this's gonna have to change. */
    protected String _text;

    /** The who in the what now? */
    protected JPanel _content = new JPanel();
}
