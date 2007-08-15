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
import java.util.Arrays;
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
    implements SQLExpression, CacheKey, CacheInvalidator, Serializable
{
    /** An expression that contains our key columns and values. */
    public static class WhereCondition<U extends PersistentRecord> implements SQLExpression
    {
        public WhereCondition (Class<U> pClass, Comparable[] values)
        {
            _pClass = pClass;
            _values = values;
        }

        // from SQLExpression
        public void accept (ExpressionVisitor builder)
            throws Exception
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

        public Comparable[] getValues ()
        {
            return _values;
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
            WhereCondition other = (WhereCondition) obj;
            return _pClass == other._pClass && Arrays.equals(_values, other._values);
        }

        @Override
        public int hashCode ()
        {
            return _pClass.hashCode() ^ Arrays.hashCode(_values);
        }

        protected Class<U> _pClass;
        protected Comparable[] _values;
    }

    /** The expression that identifies our row. */
    public final WhereCondition<T> condition;

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
        if (fields.length != values.length) {
            throw new IllegalArgumentException("Field and Value arrays must be of equal length.");
        }

        // build a local map of field name -> field value
        Map<String, Comparable> map = new HashMap<String, Comparable>();
        for (int i = 0; i < fields.length; i ++) {
            map.put(fields[i], values[i]);
        }

        // look up the cached primary key fields for this object
        String[] keyFields = getKeyFields(pClass);

        // now extract the values in field order and ensure none are extra or missing
        Comparable[] newValues = new Comparable[fields.length];
        for (int ii = 0; ii < keyFields.length; ii++) {
            newValues[ii] = map.remove(keyFields[ii]);
            // make sure we were provided with a value for this primary key field
            if (newValues[ii] == null) {
                throw new IllegalArgumentException("Missing value for key field: " + keyFields[ii]);
            }
        }

        // finally make sure we were not given any fields that are not in fact primary key fields
        if (map.size() > 0) {
            throw new IllegalArgumentException(
                "Non-key columns given: " +  StringUtil.join(map.keySet().toArray(), ", "));
        }

        condition = new WhereCondition<T>(pClass, newValues);
    }

    // from SQLExpression
    public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
    {
        classSet.add(condition.getPersistentClass());
    }

    // from SQLExpression
    public void accept (ExpressionVisitor builder)
        throws Exception
    {
        builder.visit(this);
    }

    // from CacheKey
    public String getCacheId ()
    {
        return condition.getPersistentClass().getName();
    }

    // from CacheKey
    public Serializable getCacheKey ()
    {
        return this; // TODO: Optimally return a special class here containing only _values.
    }

    // from CacheInvalidator
    public void validateFlushType (Class<?> pClass)
    {
        if (!pClass.equals(condition.getPersistentClass())) {
            throw new IllegalArgumentException(
                "Class mismatch between persistent record and cache invalidator " +
                "[record=" + pClass.getSimpleName() +
                ", invtype=" + condition.getPersistentClass().getSimpleName() + "].");
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
        validateTypesMatch(pClass, condition.getPersistentClass());
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
        return condition.equals(((Key) obj).condition);
    }

    @Override
    public int hashCode ()
    {
        return condition.hashCode();
    }

    @Override
    public String toString ()
    {
        StringBuilder builder = new StringBuilder(condition.getPersistentClass().getSimpleName());
        builder.append("(");
        String[] keyFields = getKeyFields(condition.getPersistentClass());
        for (int ii = 0; ii < keyFields.length; ii ++) {
            if (ii > 0) {
                builder.append(", ");
            }
            builder.append(keyFields[ii]).append("=").append(condition.getValues()[ii]);
        }
        builder.append(")");
        return builder.toString();
    }

    /**
     * Returns an array containing the names of the primary key fields for the supplied persistent
     * class. The values are introspected and cached for the lifetime of the VM.
     */
    protected static String[] getKeyFields (Class<?> pClass)
    {
        String[] fields = _keyFields.get(pClass);
        if (fields == null) {
            ArrayList<String> kflist = new ArrayList<String>();
            for (Field field : pClass.getFields()) {
                // look for @Id fields
                if (field.getAnnotation(Id.class) != null) {
                    kflist.add(field.getName());
                }
            }
            _keyFields.put(pClass, fields = kflist.toArray(new String[kflist.size()]));
        }
        return fields;
    }

    /** A (never expiring) cache of primary key field names for all persistent classes (of which
     * there are merely dozens, so we don't need to worry about expiring). */
    protected static HashMap<Class<?>,String[]> _keyFields = new HashMap<Class<?>,String[]>();
}
