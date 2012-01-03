//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * Provides a simple way of tracking a resource that should become stale after
 * a certain time period. This is useful for caching data that was expensive to
 * compute and should be cached for some time before being recreated.
 *
 * <p><em>Note:</em> the data will not be unreferenced and thus garbage
 * collectable until it has been requested at least once after it has expired.
 * Thus expiring references must be combined with an {@link LRUHashMap} if
 * memory conservation is also desired.
 */
public class ExpiringReference<T>
{
    /**
     * Creates an expiring reference with the supplied value and expiration time.
     */
    public static <T> ExpiringReference<T> create (T value, long expireMillis)
    {
        return new ExpiringReference<T>(value, expireMillis);
    }

    /**
     * Gets the value from an expiring reference but returns null if the supplied reference
     * reference is null.
     */
    public static <T> T get (ExpiringReference<T> value)
    {
        return (value == null) ? null : value.getValue();
    }

    /**
     * Creates an reference to the specified value that will expire in the
     * specified number of milliseconds.
     */
    public ExpiringReference (T value, long expireMillis)
    {
        _value = value;
        _expires = System.currentTimeMillis() + expireMillis;
    }

    /**
     * Returns the value with which we were created or null if the value has
     * expired.
     */
    public T getValue ()
    {
        // if the value is still around and it's expired, clear it
        if (_value != null && System.currentTimeMillis() >= _expires) {
            _value = null;
        }
        // then return the value (which may be cleared to null by now)
        return _value;
    }

    protected T _value;
    protected long _expires;
}
