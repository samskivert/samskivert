//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006-2007 Michael Bayne, Pär Winzell
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.samskivert.jdbc.depot.expression.ExpressionVisitor;
import com.samskivert.jdbc.depot.expression.SQLExpression;
import com.samskivert.util.StringUtil;

/**
 * A special form of {@link WhereClause} that uniquely specifies a single database row and
 * thus also a single persistent object. Because it implements both {@link CacheKey} and
 * {@link CacheInvalidator} it also uniquely indexes into the cache and knows how to invalidate
 * itself upon modification. This class is created by many {@link DepotMarshaller} methods as
 * a convenience, and may also be instantiated explicitly.
 */
public class Key<T extends PersistentRecord> extends WhereClause
    implements SQLExpression, CacheKey, ValidatingCacheInvalidator, Serializable
{
    /**
     * Constructs a new single-column {@code Key} with the given value.
     */
    public Key (Class<T> pClass, String ix, Comparable<?> val)
    {
        this(pClass, new String[] { ix }, new Comparable[] { val });
    }

    /**
     * Constructs a new two-column {@code Key} with the given values.
     */
    public Key (Class<T> pClass, String ix1, Comparable<?> val1,
                String ix2, Comparable<?> val2)
    {
        this(pClass, new String[] { ix1, ix2 }, new Comparable[] { val1, val2 });
    }

    /**
     * Constructs a new three-column {@code Key} with the given values.
     */
    public Key (Class<T> pClass, String ix1, Comparable<?> val1,
                String ix2, Comparable<?> val2, String ix3, Comparable<?> val3)
    {
        this(pClass, new String[] { ix1, ix2, ix3 }, new Comparable[] { val1, val2, val3 });
    }

    /**
     * Constructs a new multi-column {@code Key} with the given values.
     */
    public Key (Class<T> pClass, String[] fields, Comparable<?>[] values)
    {
        if (fields.length != values.length) {
            throw new IllegalArgumentException("Field and Value arrays must be of equal length.");
        }

        // keep this for posterity
        _pClass = pClass;

        // build a local map of field name -> field value
        Map<String, Comparable<?>> map = new HashMap<String, Comparable<?>>();
        for (int i = 0; i < fields.length; i ++) {
            map.put(fields[i], values[i]);
        }

        // look up the cached primary key fields for this object
        String[] keyFields = KeyUtil.getKeyFields(pClass);

        // now extract the values in field order and ensure none are extra or missing
        _values = new ArrayList<Comparable<?>>();
        for (int ii = 0; ii < keyFields.length; ii++) {
            Comparable<?> nugget = map.remove(keyFields[ii]);
            if (nugget == null) {
                // make sure we were provided with a value for this primary key field
                throw new IllegalArgumentException("Missing value for key field: " + keyFields[ii]);
            }
            if (nugget instanceof Serializable) {
                _values.add(nugget);
                continue;
            }
            throw new IllegalArgumentException(
                "Non-serializable argument [key=" + keyFields[ii] + ", arg=" + nugget + "]");
        }

        // finally make sure we were not given any fields that are not in fact primary key fields
        if (map.size() > 0) {
            throw new IllegalArgumentException(
                "Non-key columns given: " +  StringUtil.join(map.keySet().toArray(), ", "));
        }
    }

    /**
     * Returns the persistent class for which we represent a key.
     */
    public Class<T> getPersistentClass ()
    {
        return _pClass;
    }

    /**
     * Returns the values bound to this key.
     */
    public List<Comparable<?>> getValues ()
    {
        return _values;
    }

    // from WhereClause
    public SQLExpression getWhereExpression ()
    {
        throw new UnsupportedOperationException("Key is bound specially.");
    }

    // from SQLExpression
    public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
    {
        classSet.add(_pClass);
    }

    // from SQLExpression
    public void accept (ExpressionVisitor builder)
    {
        builder.visit(this);
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

    // from ValidatingCacheInvalidator
    public void validateFlushType (Class<?> pClass)
    {
        if (!pClass.equals(_pClass)) {
            throw new IllegalArgumentException(
                "Class mismatch between persistent record and cache invalidator " +
                "[record=" + pClass.getSimpleName() + ", invtype=" + _pClass.getSimpleName() + "].");
        }
    }

    // from CacheInvalidator
    public void invalidate (PersistenceContext ctx)
    {
        ctx.cacheInvalidate(this);
    }

    @Override // from WhereClause
    public void validateQueryType (Class<?> pClass)
    {
        super.validateQueryType(pClass);
        validateTypesMatch(pClass, _pClass);
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
        return _values.equals(((Key<?>) obj)._values);
    }

    @Override
    public int hashCode ()
    {
        return _values.hashCode();
    }

    @Override
    public String toString ()
    {
        StringBuilder builder = new StringBuilder(_pClass.getSimpleName());
        builder.append("(");
        String[] keyFields = KeyUtil.getKeyFields(_pClass);
        for (int ii = 0; ii < keyFields.length; ii ++) {
            if (ii > 0) {
                builder.append(", ");
            }
            builder.append(keyFields[ii]).append("=").append(_values.get(ii));
        }
        builder.append(")");
        return builder.toString();
    }

    /** The persistent record type for which we are a key. */
    protected final Class<T> _pClass;

    /** The expression that identifies our row. */
    protected final ArrayList<Comparable<?>> _values; // must declare as ArrayList for Serializable
}
