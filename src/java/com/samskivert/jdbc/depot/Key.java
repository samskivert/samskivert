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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.samskivert.jdbc.depot.annotation.Id;
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
    /** An expression that contains our key columns and values. */
    public static class WhereCondition<U extends PersistentRecord>
        implements SQLExpression, Serializable
    {
        public WhereCondition (Class<U> pClass, ArrayList<Comparable<?>> values)
        {
            _pClass = pClass;
            _values = values;
        }

        // from SQLExpression
        public void accept (ExpressionVisitor builder)
        {
            builder.visit(this);
        }

        // from SQLExpression
        public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
        {
        }

        public Class<U> getPersistentClass ()
        {
            return _pClass;
        }

        public ArrayList<Comparable<?>> getValues ()
        {
            return _values;
        }

        @Override public boolean equals (Object obj)
        {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            WhereCondition<?> other = (WhereCondition<?>) obj;
            return _pClass == other._pClass && _values.equals(other.getValues());
        }

        @Override public int hashCode ()
        {
            return _pClass.hashCode() ^ _values.hashCode();
        }

        protected Class<U> _pClass;
        protected ArrayList<Comparable<?>> _values; // List is not Serializable
    }

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

        // build a local map of field name -> field value
        Map<String, Comparable<?>> map = new HashMap<String, Comparable<?>>();
        for (int i = 0; i < fields.length; i ++) {
            map.put(fields[i], values[i]);
        }

        // look up the cached primary key fields for this object
        String[] keyFields = KeyUtil.getKeyFields(pClass);

        // now extract the values in field order and ensure none are extra or missing
        ArrayList<Comparable<?>> newValues = new ArrayList<Comparable<?>>();
        for (int ii = 0; ii < keyFields.length; ii++) {
            Comparable<?> nugget = map.remove(keyFields[ii]);
            if (nugget == null) {
                // make sure we were provided with a value for this primary key field
                throw new IllegalArgumentException("Missing value for key field: " + keyFields[ii]);
            }
            if (nugget instanceof Serializable) {
                newValues.add(nugget);
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

        _condition = new WhereCondition<T>(pClass, newValues);
    }

    /**
     * Returns the persistent class for which we represent a key.
     */
    public Class<T> getPersistentClass ()
    {
        return _condition.getPersistentClass();
    }

    /**
     * Returns the values bound to this key.
     */
    public ArrayList<Comparable<?>> getValues ()
    {
        return _condition.getValues();
    }

    // from WhereClause
    public SQLExpression getWhereExpression ()
    {
        return _condition;
    }

    // from SQLExpression
    public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
    {
        classSet.add(_condition.getPersistentClass());
    }

    // from SQLExpression
    public void accept (ExpressionVisitor builder)
    {
        builder.visit(this);
    }

    // from CacheKey
    public String getCacheId ()
    {
        return _condition.getPersistentClass().getName();
    }

    // from CacheKey
    public Serializable getCacheKey ()
    {
        return _condition.getValues();
    }

    // from ValidatingCacheInvalidator
    public void validateFlushType (Class<?> pClass)
    {
        if (!pClass.equals(_condition.getPersistentClass())) {
            throw new IllegalArgumentException(
                "Class mismatch between persistent record and cache invalidator " +
                "[record=" + pClass.getSimpleName() +
                ", invtype=" + _condition.getPersistentClass().getSimpleName() + "].");
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
        validateTypesMatch(pClass, _condition.getPersistentClass());
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
        return _condition.equals(((Key<?>) obj)._condition);
    }

    @Override
    public int hashCode ()
    {
        return _condition.hashCode();
    }

    @Override
    public String toString ()
    {
        StringBuilder builder = new StringBuilder(_condition.getPersistentClass().getSimpleName());
        builder.append("(");
        String[] keyFields = KeyUtil.getKeyFields(_condition.getPersistentClass());
        for (int ii = 0; ii < keyFields.length; ii ++) {
            if (ii > 0) {
                builder.append(", ");
            }
            builder.append(keyFields[ii]).append("=").append(_condition.getValues().get(ii));
        }
        builder.append(")");
        return builder.toString();
    }

    /** The expression that identifies our row. */
    protected final WhereCondition<T> _condition;
}
