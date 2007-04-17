//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne
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

package com.samskivert.jdbc.depot;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.samskivert.jdbc.depot.annotation.Id;
import com.samskivert.jdbc.depot.clause.Where;
import com.samskivert.util.StringUtil;

/**
 * A special form of {@link Where} clause that uniquely specifices a single database row and
 * thus also a single persistent object. Because it implements both {@link CacheKey} and
 * {@link CacheInvalidator} it also uniquely indexes into the cache and knows how to invalidate
 * itself upon modification. This class is created by many {@link DepotMarshaller} methods as
 * a convenience, and may also be instantiated explicitly.
 */
public class Key<T extends PersistentRecord> extends Where
    implements CacheKey, CacheInvalidator
{
    /**
     * Constructs a new single-column {@code Key} with the given value.
     */
    public Key (Class<T> pClass, String ix, Comparable val)
    {
        this(pClass, new String[] { ix }, new Comparable[] { val });
    }

    /**
     * Constructs a new two-column {@code Key} with the given values.
     */
    public Key (Class<T> pClass, String ix1, Comparable val1,
                String ix2, Comparable val2)
    {
        this(pClass, new String[] { ix1, ix2 }, new Comparable[] { val1, val2 });
    }

    /**
     * Constructs a new three-column {@code Key} with the given values.
     */
    public Key (Class<T> pClass, String ix1, Comparable val1,
                String ix2, Comparable val2, String ix3, Comparable val3)
    {
        this(pClass, new String[] { ix1, ix2, ix3 }, new Comparable[] { val1, val2, val3 });
    }

    /**
     * Constructs a new multi-column {@code Key} with the given values.
     */
    public Key (Class<T> pClass, String[] fields, Comparable[] values)
    {
        // TODO: make Where an interface so we don't have to do this ugly super call
        super(null);
        _pClass = pClass;

        if (fields.length != values.length) {
            throw new IllegalArgumentException("Field and Value arrays must be of equal length.");
        }

        // build a local map of field name -> field value
        Map<String, Comparable> map = new HashMap<String, Comparable>();
        for (int i = 0; i < fields.length; i ++) {
            map.put(fields[i], values[i]);
        }

        // then introspect on the persistent record and iterate over its actual fields
        _fields = new ArrayList<String>(fields.length);
        _values = new ArrayList<Comparable>(fields.length);
        for (Field field : _pClass.getFields()) {
            // look for @Id fields
            if (field.getAnnotation(Id.class) != null) {
                String fName = field.getName();
                // make sure we were provided with a value for this primary key field
                if (map.containsKey(fName) == false) {
                    throw new IllegalArgumentException("Missing value for key field: " + fName);
                }
                _fields.add(fName);
                _values.add(map.get(fName));
                map.remove(fName);
            }
        }
        // finally make sure we were not given any fields that are not in fact primary key fields
        if (map.size() > 0) {
            throw new IllegalArgumentException(
                "Non-key columns given: " +  StringUtil.join(map.keySet().toArray(), ", "));
        }
    }

    // from QueryClause
    public Collection<Class<? extends PersistentRecord>> getClassSet ()
    {
        ArrayList<Class<? extends PersistentRecord>> set =
            new ArrayList<Class<? extends PersistentRecord>>();
        set.add(_pClass);
        return set;
    }

    // from QueryClause
    public void appendClause (ConstructedQuery<?> query, StringBuilder builder)
    {
        builder.append(" where ");
        for (int ii = 0; ii < _fields.size(); ii ++) {
            if (ii > 0) {
                builder.append(" and ");
            }
            builder.append(_fields.get(ii)).append(_values.get(ii) == null ? " is null " : " = ? ");
        }
    }

    // from QueryClause
    public int bindArguments (PreparedStatement pstmt, int argIdx)
        throws SQLException
    {
        for (int ii = 0; ii < _fields.size(); ii ++) {
            if (_values.get(ii) != null) {
                pstmt.setObject(argIdx ++, _values.get(ii));
            }
        }
        return argIdx;
    }

    // from CacheKey
    public String getCacheId ()
    {
        return _pClass.getName();
    }

    // from CacheKey
    public Serializable getCacheKey ()
    {
        return _values;
    }

    // from CacheInvalidator
    public void invalidate (PersistenceContext ctx)
    {
        ctx.cacheInvalidate(this);
    }

    @Override
    public int hashCode ()
    {
        return _pClass.hashCode() + 31 * _values.hashCode();
    }

    @Override
    public boolean equals (Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Key other = (Key) obj;
        return _pClass == other._pClass && _values.equals(other._values);
    }

    @Override
    public String toString ()
    {
        StringBuilder builder = new StringBuilder("[Key pClass=" + _pClass.getName());
        for (int ii = 0; ii < _fields.size(); ii ++) {
            builder.append(", ").append(_fields.get(ii)).append("=").append(_values.get(ii));
        }
        builder.append("]");
        return builder.toString();
    }


    protected Class<T> _pClass;
    protected ArrayList<String> _fields;
    protected ArrayList<Comparable> _values;
}
