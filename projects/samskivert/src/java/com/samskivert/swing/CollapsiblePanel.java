//
// $Id: CollapsiblePanel.java,v 1.5 2002/11/12 08:18:36 mdb Exp $

package com.samskivert.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Icon;
import javax.swing.SwingConstants;

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
        setTrigger(trigger, null, null);
        setTriggerContainer(trigger);
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
        VGroupLayout gl = new VGroupLayout(VGroupLayout.NONE);
        gl.setOffAxisPolicy(VGroupLayout.STRETCH);
        gl.setGap(0);
        gl.setJustification(VGroupLayout.TOP);
        gl.setOffAxisJustification(VGroupLayout.LEFT);
        setLayout(gl);
    }

    /**
     * Set a component which contains the trigger button. The simple case
     * is to just set the trigger button as this component.
     */
    public void setTriggerContainer (JComponent comp)
    {
        setTriggerContainer(comp, new JPanel());
    }

    /**
     * Set a component which contains the trigger button.
     */
    public void setTriggerContainer (JComponent comp, JPanel content)
    {
        // these are our only two components.
        add(comp);
        add(_content = content);

        // and start us out not showing
        setCollapsed(true);
    }

    /**
     * Set the trigger button.
     */
    public void setTrigger (JButton trigger, Icon collapsed, Icon uncollapsed)
    {
        _trigger = trigger;
        _trigger.setHorizontalAlignment(SwingConstants.LEFT);
        _trigger.setHorizontalTextPosition(SwingConstants.RIGHT);
        _downIcon = collapsed;
        _upIcon = uncollapsed;
        _trigger.addActionListener(this);
    }

    /**
     * Set the gap between the trigger button and the rest of the content.
     * Can be negative for an overlapping effect.
     */
    public void setGap (int gap)
    {
        ((VGroupLayout) getLayout()).setGap(gap);
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
            _content.setVisible(false);
            _trigger.setIcon(_downIcon);

        } else {
            _content.setVisible(true);
            _trigger.setIcon(_upIcon);
        }
        revalidate();
    }

    /** The button that triggers collapsion. */
    protected JButton _trigger;

    /** The icons for collapsed and uncollapsed. */
    protected Icon _upIcon, _downIcon;

    /** The who in the what now? */
    protected JPanel _content;
}
