//
// $Id: IntField.java,v 1.8 2004/06/08 21:15:18 ray Exp $

package com.samskivert.swing;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JTextField;

import com.samskivert.Log;
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

        // register ourselves as the validator
        SwingUtil.setDocumentHelpers(this, this, this);
        addFocusListener(this);
    }

    /**
     * Return the int that is represented by this field.
     */
    public int getValue ()
    {
        String s = getText();
        if (!"".equals(s)) {
            try {
                return _formatter.parse(s).intValue();
            } catch (ParseException pe) {
                Log.warning("This shouldn't happen.");
            }
        }

        return 0;
    }

    /**
     * Set the text to the value specified.
     */
    public void setValue (int value)
    {
        setText(_formatter.format(value));
    }

    /**
     * Change the current min value.
     */
    public void setMinValue (int minValue)
    {
        _minValue = minValue;
        validateText();
    }

    /**
     * Change the current max value.
     */
    public void setMaxValue (int maxValue)
    {
        _maxValue = maxValue;
        validateText();
    }


    // from interface SwingUtil.DocumentValidator
    public boolean isValid (String text)
    {
        if ("".equals(text)) {
            return true;
        }

        try {
            int value = _formatter.parse(text).intValue();
            return (value <= _maxValue);
        } catch (ParseException nfe) {
            return false;
        }
    }

    // from interface SwingUtil.DocumentTransformer
    public String transform (String text)
    {
        try {
            return _formatter.format(_formatter.parse(text));
        } catch (ParseException nfe) {
            return text;
        }
    }

    // documentation inherited from interface
    public void focusGained (FocusEvent e)
    {
        // nada
    }

    // documentation inherited from interface
    public void focusLost (FocusEvent e)
    {
        validateText();
    }

    /**
     * Ensure that the value we're displaying is between the minimum and
     * the maximum.
     */
    protected void validateText ()
    {
        String text = getText();
        int val;
        try {
            val = _formatter.parse(text).intValue();
        } catch (ParseException pe) {
            val = 0;
        }
        // bound it in
        String validated = _formatter.format(
            Math.min(_maxValue, Math.max(_minValue, val)));
        if (!text.equals(validated)) {
            // only do it if it changes the text- otherwise
            // we booch the focus when this is called from focusLost
            setText(validated);
        }
    }

    protected int _minValue, _maxValue;

    protected NumberFormat _formatter = NumberFormat.getIntegerInstance();
}
