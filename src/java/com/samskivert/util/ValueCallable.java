//
// $Id$

package com.samskivert.util;

import java.util.concurrent.Callable;

/**
 * A Callable that just returns a predefined value.
 */
public class ValueCallable<V>
    implements Callable<V>
{
    /**
     * Factory to create a ValueCallable from a value while avoiding having to specify the type.
     * TODO: I think I want <? super V> in the return value, not sure how to make it.
     */
    public static <V> ValueCallable<V> create (V value)
    {
        return new ValueCallable<V>(value);
    }

    /**
     * Construct a ValueCallable.
     */
    public ValueCallable (V value)
    {
        _value = value;
    }

    // from interface Callable
    public V call ()
    {
        return _value;
    }

    /** The value we'll be returning. */
    protected V _value;
}
