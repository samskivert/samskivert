//
// $Id: SimpleSlider.java,v 1.2 2002/09/04 01:28:09 shaper Exp $

package com.samskivert.swing;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Displays a slider with a label on the left explaining what the slider
 * does and a label on the right displaying the slider's current value.
 */
public class SimpleSlider extends JPanel
    implements ChangeListener
{
    /**
     * Creates a simple slider with the specified configuration.
     *
     * @param label the text to display to the left of the slider.
     * @param min the slider's minimium value.
     * @param max the slider's maximum value.
     * @param value the slider's starting value.
     */
    public SimpleSlider (String label, int min, int max, int value)
    {
        setLayout(new HGroupLayout(HGroupLayout.STRETCH));
        add(_label = new JLabel(label), HGroupLayout.FIXED);
        add(_slider = new JSlider(min, max, value));
        _slider.addChangeListener(this);
        add(_value = new JLabel(Integer.toString(min)), HGroupLayout.FIXED);
    }

    // documentation inherited
    public void stateChanged (ChangeEvent e)
    {
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
            _value.setText(Integer.toString(source.getValue()));
        }
    }

    // documentation inherited
    public void setFont (Font font)
    {
        super.setFont(font);

        if (_label != null) {
            _label.setFont(font);
            _slider.setFont(font);
            _value.setFont(font);
        }
    }

    /**
     * Updates the label displayed to the left of the slider.
     */
    public void setLabel (String label)
    {
        _label.setText(label);
    }

    /**
     * Returns the current value of the slider.
     */
    public int getValue ()
    {
        return _slider.getValue();
    }

    /**
     * Sets the slider's current value.
     */
    public void setValue (int value)
    {
        _slider.setValue(value);
        _value.setText(Integer.toString(value));
    }

    /**
     * Sets the slider's minimum value.
     */
    public void setMinimum (int minimum)
    {
        _slider.setMinimum(minimum);
    }

    /**
     * Sets the slider's maximum value.
     */
    public void setMaximum (int maximum)
    {
        _slider.setMaximum(maximum);
    }

    protected JLabel _label;
    protected JSlider _slider;
    protected JLabel _value;
}
