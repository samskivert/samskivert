//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2006 Michael Bayne
// 
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.util;

import java.util.Map;

/**
 * A useful building block for implementing one's own {@link Map} classes. Sun
 * has this same damned code in AbstractMap and have idiotically declared it
 * package protected, with a genius comment saying "This should be made public
 * as soon as possible. It greatly simplifies the task of implementing Map." No
 * doubt that comment was added half a decade ago. Thanks guys!
 */
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
        if (!(o instanceof Map.Entry)) {
            return false;
        }
        Map.Entry e = (Map.Entry)o;
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
