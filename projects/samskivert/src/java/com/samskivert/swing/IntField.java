//
// $Id: IntField.java,v 1.5 2003/12/15 18:56:27 mdb Exp $

package com.samskivert.swing;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;

import com.samskivert.Log;
import com.samskivert.swing.util.SwingUtil.DocumentValidator;
import com.samskivert.swing.util.SwingUtil.DocumentTransformer;
import com.samskivert.swing.util.SwingUtil;

/**
 * A text field that will only take digit entries within the optional
 * specified range.
 */
public class IntField extends JTextField
    implements SwingUtil.DocumentValidator, SwingUtil.DocumentTransformer,
               FocusListener
{
    public IntField ()
    {
        this(0, 0, Integer.MAX_VALUE);
    }

    public IntField (int initial, int minValue, int maxValue)
    {
        setValue(initial);

        _minValue = minValue;
        _maxValue = maxValue;

        setHorizontalAlignment(JTextField.RIGHT);
        setColumns(5);

        // register ourselves as the validator and focus listener
        SwingUtil.setDocumentHelpers(this, this, null);
        addFocusListener(this);
    }

    /**
     * Return the int that is represented by this field.
     */
    public int getValue ()
    {
        try {
            return Integer.parseInt(getText());
        } catch (NumberFormatException nfe) {
            Log.warning("This should never, ever, happen");
        }

        return -1;
    }

    /**
     * Set the text to the value specified.
     */
    public void setValue (int value)
    {
        setText(Integer.toString(value));
    }

    /**
     * Change the current min value.
     */
    public void setMinValue (int minValue)
    {
        _minValue = minValue;
        setText(getText()); // jiggle the text
    }

    /**
     * Change the current max value.
     */
    public void setMaxValue (int maxValue)
    {
        _maxValue = maxValue;
        setText(getText()); // jiggle the text
    }

    // from interface SwingUtil.DocumentTransformer
    public String transform (String s)
    {
        int val = Integer.parseInt(s);
        return Integer.toString(Math.min(_maxValue, Math.max(_minValue, val)));
    }

    // from interface SwingUtil.DocumentValidator
    public boolean isValid (String text)
    {
        if ("".equals(text)) {
            return true;
        }

        try {
            int value = Integer.parseInt(text);
            return (value <= _maxValue);
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    // documentation inherited from interface
    public void focusGained (FocusEvent e)
    {
    }

    // documentation inherited from interface
    public void focusLost (FocusEvent e)
    {
        // here we ensure that we're not below our minimum
        setText(transform(getText()));
    }

    protected int _minValue;
    protected int _maxValue;
}
