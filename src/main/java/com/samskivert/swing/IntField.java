//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.EventQueue;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JTextField;

import javax.swing.event.DocumentEvent;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

/**
 * A text field that will only allow editing of integer values within
 * the specified range.
 */
public class IntField extends JTextField
{
    /**
     * Create an IntField with the range 0 - Integer.MAX_VALUE, with
     * 0 as the initial value.
     */
    public IntField ()
    {
        this(Integer.MAX_VALUE);
    }

    /**
     * Create an IntField with the specified maximum.
     */
    public IntField (int maxValue)
    {
        this(0, maxValue);
    }

    /**
     * Create an IntField with the specified minimum and maximum, with
     * the minimum value initially displayed.
     */
    public IntField (int minValue, int maxValue)
    {
        this(minValue, minValue, maxValue);
    }

    /**
     * Create an IntField with the specified initial, minimum, and
     * maximum values.
     */
    public IntField (int initial, int minValue, int maxValue)
    {
        super(new IntDocument(), format(initial), 5);

        validateMinMax(minValue, maxValue);
        if (initial > maxValue || initial < minValue) {
            throw new IllegalArgumentException("initial value not between " +
                    "min and max");
        }
        _minValue = minValue;
        _maxValue = maxValue;

        setHorizontalAlignment(JTextField.RIGHT);

        // create a document filter which enforces the restrictions on
        // what text may be entered. This is similar to the filter
        // that is configured in SwingUtil.setDocumentHelpers, only the
        // remove operation is modified to do the sneaky highlighting we do.
        final IntDocument doc = (IntDocument) getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override public void remove (FilterBypass fb, int offset, int length)
                throws BadLocationException
            {
                String current = doc.getText(0, doc.getLength());
                String potential = current.substring(0, offset) +
                    current.substring(offset + length);
                // if the bit to be removed is still a valid value, go ahead
                if (unformatSafe(potential) >= _minValue) {
                    transform(fb, current, potential);
                    return;
                }

                // otherwise, we are going to highlight the bit instead
                // of removing it
                int selStart = getSelectionStart();
                if (selStart > 0) {
                    // see if the next highlighted character is a digit
                    char replace = current.charAt(selStart - 1);
                    if (Character.isDigit(replace)) {
                        // and can be zeroed out
                        String zeroed = current.substring(0, selStart - 1) +
                            "0" + current.substring(selStart);
                        // if so, change it to a zero
                        if (unformatSafe(zeroed) >= _minValue) {
                            fb.replace(selStart - 1, 1, "0", null);

                        } else {
                            // otherwise, re-set the entire field to
                            // contain the minimum value, and highlight it
                            String min = format(_minValue);
                            fb.replace(0, current.length(), min, null);
                            setSelectionStart(0);
                            setSelectionEnd(min.length());
                            return;
                        }
                    }
                }
                // grow the selection to encompass one more character
                setSelectionStart(selStart - 1);
            }

            @Override public void insertString (FilterBypass fb, int offset,
                String s, AttributeSet attr)
                throws BadLocationException
            {
                String current = doc.getText(0, doc.getLength());
                String potential = current.substring(0, offset) + s +
                    current.substring(offset);
                transform(fb, current, potential);
            }

            @Override public void replace (FilterBypass fb, int offset, int length,
                String text, AttributeSet attrs)
                throws BadLocationException
            {
                String current = doc.getText(0, doc.getLength());
                String potential = current.substring(0, offset) + text +
                    current.substring(offset + length);
                transform(fb, current, potential);
            }

            protected void transform (FilterBypass fb,
                    String current, String potential)
                throws BadLocationException
            {
                boolean wouldaBeenEqual = current.equals(potential);
                potential = transform(potential);
                boolean selection = (getSelectionEnd() != getSelectionStart());
                // we only change it if it needs changing
                if (!current.equals(potential) ||
                        // or if it would have been the same pre-transforming
                        // and there is a selection (IE undo the selection)
                        (wouldaBeenEqual && selection)) {
                    if (selection) {
                        // undo the selection to not cause an exception
                        setCaretPosition(0);
                    }
                    fb.replace(0, doc.getLength(), potential, null);
                }
            }

            /**
             * Ensure that the specified text is formatted and within
             * the bounds.
             */
            protected String transform (String text)
            {
                // if we're at least the minvalue, everything's ok
                int val = unformatSafe(text);
                if (val >= _minValue) {
                    return format(Math.min(_maxValue, val));
                }

                // otherwise, see if we can append zeros and make a valid value,
                // remembering how many digits we added
                int newVal = val;
                int digits = 0;
                if (newVal > 0) {
                    while (newVal * 10 <= _maxValue) {
                        newVal *= 10;
                        digits++;
                        if (newVal >= _minValue) {
                            break;
                        }
                    }
                }

                // if that didn't work, just set it to the min value and
                // highlight all the digits
                if (newVal < _minValue) {
                    newVal = _minValue;
                    digits = String.valueOf(newVal).length();
                }

                // return the new value, but post an event to immediately
                // highlight the digits that the user did not enter, so
                // that they can type over them
                final String newText = format(newVal);
                final int fdigits = digits;
                EventQueue.invokeLater(new Runnable() {
                    public void run () {
                        String text = getText();
                        if (text.equals(newText)) {
                            int len = text.length();
                            setSelectionEnd(len);
                            int digits = fdigits;
                            for (int ii = len - 1; ii >= 0; ii--) {
                                if (Character.isDigit(text.charAt(ii))) {
                                    --digits;
                                    if (digits == 0) {
                                        setSelectionStart(ii);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
                return newText;
            }
        });
    }

    /**
     * Validate min/max.
     */
    protected void validateMinMax (int minValue, int maxValue)
    {
        if (minValue < 0) {
            throw new IllegalArgumentException("minValue must be at least 0");
        }
        if (maxValue < minValue) {
            throw new IllegalArgumentException(
                "maxValue must be greater than minValue");
        }
    }

    /**
     * Return the int that is represented by this field.
     */
    public int getValue ()
    {
        return unformatSafe(getText());
    }

    /**
     * Set the text to the value specified.
     */
    public void setValue (int value)
    {
        setText(format(value));
    }

    /**
     * Change the current min value.
     */
    public void setMinValue (int minValue)
    {
        validateMinMax(minValue, _maxValue);
        _minValue = minValue;
        validateText();
    }

    /**
     * Change the current max value.
     */
    public void setMaxValue (int maxValue)
    {
        validateMinMax(_minValue, maxValue);
        _maxValue = maxValue;
        validateText();
    }

    /**
     * Ensure that the value we're displaying is between the minimum and
     * the maximum.
     */
    protected void validateText ()
    {
        setText(getText());
    }

    /**
     * Format the specified monetary value into a string.
     * This just puts commas in.
     */
    public static String format (int value)
    {
        return _formatter.format(value);
    }

    /**
     * Parse numbers, with commas being ok.
     */
    public static int unformat (String text)
        throws ParseException
    {
        return _formatter.parse(text).intValue();
    }

    /**
     * Parse numbers, don't throw exceptions.
     */
    public static int unformatSafe (String text)
    {
        try {
            return unformat(text);
        } catch (ParseException pe) {
            return 0;
        }
    }

    /**
     * Our own special Document class.
     */
    protected static class IntDocument extends PlainDocument
    {
        @Override protected void fireRemoveUpdate (DocumentEvent e)
        {
            // suppress: the DocumentFilter implements replace by doing
            // a remove and then an insert. We do not want listeners to ever
            // be notified when the document contains invalid text.
            // (Gosh, it'd be nice if we didn't have to hack this, but
            // the standard implementation has a bunch of methods with
            // package-protected access. Thanks Sun!)
        }
    }

    /** min/max */
    protected int _minValue, _maxValue;

    /** Formats and parses numbers with commas in them. */
    protected static final NumberFormat _formatter = NumberFormat.getIntegerInstance();
}
