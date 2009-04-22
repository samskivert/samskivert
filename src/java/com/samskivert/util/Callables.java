//
// $Id$

package com.samskivert.util;

import java.util.concurrent.Callable;

/**
 * Utilities for Callables.
 */
public class Callables
{
    /**
     * Return a {@link Callable} that merely returns the specified value.
     * No exception will ever be thrown.
     */
    public static <V> Callable<V> asCallable (final V value)
    {
        return new Callable<V>() {
            public V call () {
                return value;
            }
        };
    }
}
