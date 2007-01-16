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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.samskivert.jdbc.depot.clause.Where;

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
     * 
     * TODO: There is no reason to store both fields and values here (and doing so probably more
     * than doubles the space the key consumes in the cache). The primary key fields are known
     * by the DepotMarshaller; we should simply check tha the given indices match those, and
     * store the values in the order defined by the DepotMarshaller. The sanity check would be
     * welcome in any case. The only problem with this is that we don't currently have access to
     * a {@link PersistenceContext}. It may be that we should make this class internal to
     * {@link DepotRepository} and only create it ourselves. This would require *lots* more
     * convenience methods in {@link DepotRepository} though.
     */
    public Key (Class<T> pClass, String[] fields, Comparable[] values)
    {
        // TODO: make Where an interface so we don't have to do this ugly super call
        super(null);

        if (fields.length != values.length) {
            throw new IllegalArgumentException("Field and Value arrays must be of equal length.");
        }
        _pClass = pClass;
        _map = new HashMap<String, Comparable>();
        for (int i = 0; i < fields.length; i ++) {
            _map.put(fields[i], values[i]);
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
        boolean first = true;
        for (Map.Entry entry : _map.entrySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(" and ");
            }
            builder.append(entry.getKey());
            builder.append(entry.getValue() == null ? " is null " : " = ? ");
        }
    }

    // from QueryClause
    public int bindArguments (PreparedStatement pstmt, int argIdx)
        throws SQLException
    {
        for (Map.Entry entry : _map.entrySet()) {
            if (entry.getValue() != null) {
                pstmt.setObject(argIdx ++, entry.getValue());
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
        return _map;
    }

    // from CacheInvalidator
    public void invalidate (PersistenceContext ctx)
    {
        ctx.cacheInvalidate(this);
    }

    @Override
    public int hashCode ()
    {
        return _pClass.hashCode() + 31 * _map.hashCode();
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
        return (_pClass == other._pClass && _map.equals(other._map));
    }

    @Override
    public String toString ()
    {
        StringBuilder builder = new StringBuilder("[Key pClass=" + _pClass.getName());
        for (Map.Entry entry : _map.entrySet()) {
            builder.append(", ").append(entry.getKey()).append("=").append(entry.getValue());
        }
        builder.append("]");
        return builder.toString();
    }
    
    protected Class<T> _pClass;
    protected HashMap<String, Comparable> _map;
}
