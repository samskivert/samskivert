//
// $Id: IntField.java,v 1.2 2003/11/25 04:08:39 eric Exp $

package com.samskivert.swing;

import javax.swing.JTextField;

import com.samskivert.Log;
import com.samskivert.swing.util.SwingUtil.DocumentValidator;
import com.samskivert.swing.util.SwingUtil;

/**
 * A text field that will only take digit entries within the optional
 * specified range.
 */
public class IntField extends JTextField
    implements SwingUtil.DocumentValidator
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

        // register ourselves as the validator
        SwingUtil.setDocumentHelpers(this, this, null);
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
        setText("" + value);
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

    // from interface SwingUtil.DocumentValidator
    public boolean isValid (String text)
    {
        if ("".equals(text)) {
            return true;
        }

        try {
            int value = Integer.parseInt(text);

            if (value > _maxValue) {
                setText("" + _maxValue);
            }

            return ((value >= _minValue) && (value <= _maxValue));
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    protected int _minValue;
    protected int _maxValue;
}
