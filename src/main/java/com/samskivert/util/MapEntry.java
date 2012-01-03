//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.Map;

/**
 * @deprecated As of JDK version 1.6 a replacement is available: java.util.AbstractMap.SimpleEntry.
 */
@Deprecated
public class MapEntry<K,V> implements Map.Entry<K,V>
{
    public MapEntry (K key, V value)
    {
        _key   = key;
        _value = value;
    }

    public MapEntry (Map.Entry<K,V> e)
    {
        _key = e.getKey();
        _value = e.getValue();
    }

    // from interface Map.Entry
    public K getKey ()
    {
        return _key;
    }

    // from interface Map.Entry
    public V getValue ()
    {
        return _value;
    }

    // from interface Map.Entry
    public V setValue (V value)
    {
        V oldValue = _value;
        _value = value;
        return oldValue;
    }

    @Override // from Object
    public boolean equals (Object o)
    {
        if (!(o instanceof Map.Entry<?, ?>)) {
            return false;
        }
        Map.Entry<?, ?> e = (Map.Entry<?, ?>)o;
        return ObjectUtil.equals(_key, e.getKey()) &&
            ObjectUtil.equals(_value, e.getValue());
    }

    @Override // from Object
    public int hashCode ()
    {
        return ((_key == null) ? 0 : _key.hashCode()) ^
            ((_value == null) ? 0 : _value.hashCode());
    }

    @Override // from Object
    public String toString ()
    {
        return _key + "=" + _value;
    }

    protected K _key;
    protected V _value;
}
