//
// $Id$

package com.samskivert.util;

import java.util.NoSuchElementException;

public class RangeIntSet extends AbstractIntSet
{
    /**
     * Construct a set of ints in the range from low to high, <b>inclusive</b>.
     */
    public RangeIntSet (int lowValue, int highValue)
    {
        if (lowValue > highValue) {
            throw new IllegalArgumentException("lowValue is greater than highValue");
        }
        _low = lowValue;
        _high = highValue;
    }

    @Override
    public boolean contains (int value)
    {
        return (value >= _low) && (value <= _high);
    }

    @Override
    public int size ()
    {
        long size = ((long) _high) - _low + 1;
        return (size > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int)size;
    }

    public Interator interator ()
    {
        return new AbstractInterator() {
            public boolean hasNext ()
            {
                return _curValid;
            }

            public int nextInt ()
            {
                if (!_curValid) {
                    throw new NoSuchElementException();
                }
                // check for the end before we increment _cur, in case _high is MAX_VALUE
                if (_cur == _high) {
                    _curValid = false;
                }
                return _cur++;
            }

            protected int _cur = _low;
            protected boolean _curValid = true;
        };
    }

    protected int _low;
    protected int _high;
}
