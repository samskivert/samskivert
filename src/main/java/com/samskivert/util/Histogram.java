//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.Arrays;

/**
 * Used for tracking a set of values that fall into a discrete range of values.
 */
public class Histogram
    implements Cloneable
{
    /**
     * Constructs a histogram that will track values with a granularity equal to the
     * <code>bucketWidth</code> from <code>minValue</code> to <code>minValue +
     * bucketWidth*bucketCount</code>.
     */
    public Histogram (int minValue, int bucketWidth, int bucketCount)
    {
        _minValue = minValue;
        _maxValue = minValue + bucketWidth*bucketCount;
        _bucketWidth = bucketWidth;
        _buckets = new int[bucketCount];
    }

    /**
     * Registers a value with this histogram.
     */
    public void addValue (int value)
    {
        if (value < _minValue) {
            _buckets[0]++;
        } else if (value >= _maxValue) {
            _buckets[_buckets.length-1]++;
        } else {
            _buckets[(value-_minValue)/_bucketWidth]++;
        }
        _count++;
    }

    /**
     * Returns the total number of values in the histogram.
     */
    public int size ()
    {
        return _count;
    }

    /**
     * Clears the values from this histogram.
     */
    public void clear ()
    {
        Arrays.fill(_buckets, 0);
    }

    /**
     * Returns the array containing the bucket values. The zeroth element contains the count of all
     * values less than <code>minValue</code>, the subsequent <code>bucketCount</code> elements
     * contain the count of values falling into those buckets and the last element contains values
     * greater than or equal to <code>maxValue</code>.
     */
    public int[] getBuckets ()
    {
        return _buckets;
    }

    /**
     * Generates a terse summary of the count and contents of the values in this histogram.
     */
    public String summarize ()
    {
        StringBuilder buf = new StringBuilder();
        buf.append(_count).append(":");
        for (int ii = 0; ii < _buckets.length; ii++) {
            if (ii > 0) {
                buf.append(",");
            }
            buf.append(_buckets[ii]);
        }
        return buf.toString();
    }

    @Override // from Object
    public Histogram clone ()
    {
        try {
            Histogram chisto = (Histogram)super.clone();
            chisto._buckets = _buckets.clone();
            return chisto;
        } catch (CloneNotSupportedException cnse) {
            throw new AssertionError(cnse);
        }
    }

    @Override // from Object
    public String toString ()
    {
        return "[min=" + _minValue + ", max=" + _maxValue + ", bwidth=" + _bucketWidth +
            ", buckets=" + StringUtil.toString(_buckets) + "]";
    }

    /** The minimum value tracked by this histogram. */
    protected int _minValue;

    /** The maximum value tracked by this histogram. */
    protected int _maxValue;

    /** The size of each histogram bucket. */
    protected int _bucketWidth;

    /** The total number of values. */
    protected int _count;

    /** The histogram buckets. */
    protected int[] _buckets;
}
