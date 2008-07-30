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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.samskivert.jdbc.depot.Key.WhereCondition;
import com.samskivert.jdbc.depot.annotation.Computed;
import com.samskivert.jdbc.depot.clause.DeleteClause;
import com.samskivert.jdbc.depot.clause.FieldDefinition;
import com.samskivert.jdbc.depot.clause.FieldOverride;
import com.samskivert.jdbc.depot.clause.ForUpdate;
import com.samskivert.jdbc.depot.clause.FromOverride;
import com.samskivert.jdbc.depot.clause.GroupBy;
import com.samskivert.jdbc.depot.clause.InsertClause;
import com.samskivert.jdbc.depot.clause.Join;
import com.samskivert.jdbc.depot.clause.Limit;
import com.samskivert.jdbc.depot.clause.OrderBy;
import com.samskivert.jdbc.depot.clause.SelectClause;
import com.samskivert.jdbc.depot.clause.UpdateClause;
import com.samskivert.jdbc.depot.clause.Where;
import com.samskivert.jdbc.depot.expression.ColumnExp;
import com.samskivert.jdbc.depot.expression.ExpressionVisitor;
import com.samskivert.jdbc.depot.expression.FunctionExp;
import com.samskivert.jdbc.depot.expression.LiteralExp;
import com.samskivert.jdbc.depot.expression.SQLExpression;
import com.samskivert.jdbc.depot.expression.ValueExp;
import com.samskivert.jdbc.depot.operator.Conditionals.Exists;
import com.samskivert.jdbc.depot.operator.Conditionals.In;
import com.samskivert.jdbc.depot.operator.Conditionals.IsNull;
import com.samskivert.jdbc.depot.operator.Conditionals.FullTextMatch;
import com.samskivert.jdbc.depot.operator.Logic.Not;
import com.samskivert.jdbc.depot.operator.SQLOperator.BinaryOperator;
import com.samskivert.jdbc.depot.operator.SQLOperator.MultiOperator;

/**
 * Implements the base functionality of the SQL-building pass of {@link SQLBuilder}. Dialectal
 * subclasses of this should be created and returned from {@link SQLBuilder#getBuildVisitor()}.
 *
 * This class is intimately paired with {#link BindVisitor}.
 */
public abstract class BuildVisitor implements ExpressionVisitor
{
    public String getQuery ()
    {
        return _builder.toString();
    }

    public void visit (FromOverride override)
        throws Exception
    {
        _builder.append(" from " );
        List<Class<? extends PersistentRecord>> from = override.getFromClasses();
        for (int ii = 0; ii < from.size(); ii++) {
            if (ii > 0) {
                _builder.append(", ");
            }
            appendTableName(from.get(ii));
            _builder.append(" as ");
            appendTableAbbreviation(from.get(ii));
        }
    }

    public void visit (FieldDefinition definition)
        throws Exception
    {
        definition.getDefinition().accept(this);
        if (_enableAliasing) {
            _builder.append(" as ");
            appendIdentifier(definition.getField());
        }
    }

    public void visit (WhereCondition<? extends PersistentRecord> whereCondition)
        throws Exception
    {
        Class<? extends PersistentRecord> pClass = whereCondition.getPersistentClass();
        String[] keyFields = Key.getKeyFields(pClass);
        List<Comparable<?>> values = whereCondition.getValues();
        for (int ii = 0; ii < keyFields.length; ii ++) {
            if (ii > 0) {
                _builder.append(" and ");
            }
            // A Key's WHERE clause must mirror what's actually retrieved for the persistent
            // object, so we turn on overrides here just as we do when expanding SELECT fields
            boolean saved = _enableOverrides;
            _enableOverrides = true;
            appendRhsColumn(pClass, keyFields[ii]);
            _enableOverrides = saved;
            _builder.append(values.get(ii) == null ? " is null " : " = ? ");
        }
    }

    public void visit (Key<? extends PersistentRecord> key)
        throws Exception
    {
        _builder.append(" where ");
        key.condition.accept(this);
    }

    public void visit (MultiKey<? extends PersistentRecord> key)
        throws Exception
    {
        _builder.append(" where ");
        boolean first = true;
        for (Map.Entry<String, Comparable<?>> entry : key.getSingleFieldsMap().entrySet()) {
            if (first) {
                first = false;
            } else {
                _builder.append(" and ");
            }
            // A MultiKey's WHERE clause must mirror what's actually retrieved for the persistent
            // object, so we turn on overrides here just as we do when expanding SELECT fields
            boolean saved = _enableOverrides;
            _enableOverrides = true;
            appendRhsColumn(key.getPersistentClass(), entry.getKey());
            _enableOverrides = saved;
            _builder.append(entry.getValue() == null ? " is null " : " = ? ");
        }
        if (!first) {
            _builder.append(" and ");
        }
        appendRhsColumn(key.getPersistentClass(), key.getMultiField());
        _builder.append(" in (");

        Comparable<?>[] values = key.getMultiValues();
        for (int ii = 0; ii < values.length; ii ++) {
            if (ii > 0) {
                _builder.append(", ");
            }
            _builder.append("?");
        }
        _builder.append(")");
    }

    public void visit (FunctionExp functionExp)
        throws Exception
    {
        _builder.append(functionExp.getFunction());
        _builder.append("(");
        SQLExpression[] arguments = functionExp.getArguments();
        for (int ii = 0; ii < arguments.length; ii ++) {
            if (ii > 0) {
                _builder.append(", ");
            }
            arguments[ii].accept(this);
        }
        _builder.append(")");
    }

    public void visit (MultiOperator multiOperator)
        throws Exception
    {
        SQLExpression[] conditions = multiOperator.getConditions();
        for (int ii = 0; ii < conditions.length; ii++) {
            if (ii > 0) {
                _builder.append(" ").append(multiOperator.operator()).append(" ");
            }
            _builder.append("(");
            conditions[ii].accept(this);
            _builder.append(")");
        }
    }

    public void visit (BinaryOperator binaryOperator)
        throws Exception
    {
        _builder.append('(');
        binaryOperator.getLeftHandSide().accept(this);
        _builder.append(binaryOperator.operator());
        binaryOperator.getRightHandSide().accept(this);
        _builder.append(')');
    }

    public void visit (IsNull isNull)
        throws Exception
    {
        isNull.getColumn().accept(this);
        _builder.append(" is null");
    }

    public void visit (In in)
        throws Exception
    {
        in.getColumn().accept(this);
        _builder.append(" in (");
        Comparable<?>[] values = in.getValues();
        for (int ii = 0; ii < values.length; ii ++) {
            if (ii > 0) {
                _builder.append(", ");
            }
            _builder.append("?");
        }
        _builder.append(")");
    }

    public abstract void visit (FullTextMatch match)
        throws Exception;

    public void visit (ColumnExp columnExp)
        throws Exception
    {
        appendRhsColumn(columnExp.getPersistentClass(), columnExp.getField());
    }

    public void visit (Not not)
        throws Exception
    {
        _builder.append(" not (");
        not.getCondition().accept(this);
        _builder.append(")");
    }

    public void visit (GroupBy groupBy)
        throws Exception
    {
        _builder.append(" group by ");

        SQLExpression[] values = groupBy.getValues();
        for (int ii = 0; ii < values.length; ii++) {
            if (ii > 0) {
                _builder.append(", ");
            }
            values[ii].accept(this);
        }
    }

    public void visit (ForUpdate forUpdate)
        throws Exception
    {
        _builder.append(" for update ");
    }

    public void visit (OrderBy orderBy)
        throws Exception
    {
        _builder.append(" order by ");

        SQLExpression[] values = orderBy.getValues();
        OrderBy.Order[] orders = orderBy.getOrders();
        for (int ii = 0; ii < values.length; ii++) {
            if (ii > 0) {
                _builder.append(", ");
            }
            values[ii].accept(this);
            _builder.append(" ").append(orders[ii]);
        }
    }

    public void visit (Where where)
        throws Exception
    {
        _builder.append(" where ");
        where.getCondition().accept(this);
    }

    public void visit (Join join)
        throws Exception
    {
        switch (join.getType()) {
        case INNER:
            _builder.append(" inner join " );
            break;
        case LEFT_OUTER:
            _builder.append(" left outer join " );
            break;
        case RIGHT_OUTER:
            _builder.append(" right outer join " );
            break;
        }
        appendTableName(join.getJoinClass());
        _builder.append(" as ");
        appendTableAbbreviation(join.getJoinClass());
        _builder.append(" on ");
        join.getJoinCondition().accept(this);
    }

    public void visit (Limit limit)
        throws Exception
    {
        _builder.append(" limit ? offset ? ");
    }

    public void visit (LiteralExp literalExp)
        throws Exception
    {
        _builder.append(literalExp.getText());
    }

    public void visit (ValueExp valueExp)
        throws Exception
    {
        _builder.append("?");
    }

    public void visit (Exists<? extends PersistentRecord> exists)
        throws Exception
    {
        _builder.append("exists ");
        exists.getSubClause().accept(this);
    }

    public void visit (SelectClause<? extends PersistentRecord> selectClause)
        throws Exception
    {
        Class<? extends PersistentRecord> pClass = selectClause.getPersistentClass();
        boolean isInner = _innerClause;
        _innerClause = true;

        if (isInner) {
            _builder.append("(");
        }
        _builder.append("select ");

        if (_definitions.containsKey(pClass)) {
            throw new IllegalArgumentException(
                "Can not yet nest SELECTs on the same persistent record.");
        }

        Map<String, FieldDefinition> definitionMap = new HashMap<String, FieldDefinition>();
        for (FieldDefinition definition : selectClause.getFieldDefinitions()) {
            definitionMap.put(definition.getField(), definition);
        }
        _definitions.put(pClass, definitionMap);

        try {
            // iterate over the fields we're filling in and figure out whence each one comes
            boolean skip = true;

            // while expanding column names in the SELECT query, do aliasing and expansion
            _enableAliasing = _enableOverrides = true;

            for (String field : selectClause.getFields()) {
                if (!skip) {
                    _builder.append(", ");
                }
                skip = false;

                int len = _builder.length();
                appendRhsColumn(pClass, field);

                // if nothing was added, don't add a comma
                if (_builder.length() == len) {
                    skip = true;
                }
            }

            // then stop
            _enableAliasing = _enableOverrides = false;

            if (selectClause.getFromOverride() != null) {
                selectClause.getFromOverride().accept(this);

            } else {
                Computed computed = _types.getMarshaller(pClass).getComputed();
                Class<? extends PersistentRecord> tClass;
                if (computed != null && !PersistentRecord.class.equals(computed.shadowOf())) {
                    tClass = computed.shadowOf();
                } else if (_types.getTableName(pClass) != null) {
                    tClass = pClass;
                } else {
                    throw new SQLException("Query on @Computed entity with no FromOverrideClause.");
                }
                _builder.append(" from ");
                appendTableName(tClass);
                _builder.append(" as ");
                appendTableAbbreviation(tClass);
            }

            for (Join clause : selectClause.getJoinClauses()) {
                clause.accept(this);
            }
            if (selectClause.getWhereClause() != null) {
                selectClause.getWhereClause().accept(this);
            }
            if (selectClause.getGroupBy() != null) {
                selectClause.getGroupBy().accept(this);
            }
            if (selectClause.getOrderBy() != null) {
                selectClause.getOrderBy().accept(this);
            }
            if (selectClause.getLimit() != null) {
                selectClause.getLimit().accept(this);
            }
            if (selectClause.getForUpdate() != null) {
                selectClause.getForUpdate().accept(this);
            }

        } finally {
            _definitions.remove(pClass);
        }
        if (isInner) {
            _builder.append(")");
        }
    }

    public void visit (UpdateClause<? extends PersistentRecord> updateClause)
        throws Exception
    {
        if (updateClause.getWhereClause() == null) {
            throw new SQLException("I dare not currently perform UPDATE without a WHERE clause.");
        }
        Class<? extends PersistentRecord> pClass = updateClause.getPersistentClass();
        _innerClause = true;

        _builder.append("update ");
        appendTableName(pClass);
        _builder.append(" as ");
        appendTableAbbreviation(pClass);
        _builder.append(" set ");

        String[] fields = updateClause.getFields();
        Object pojo = updateClause.getPojo();
        SQLExpression[] values = updateClause.getValues();
        for (int ii = 0; ii < fields.length; ii ++) {
            if (ii > 0) {
                _builder.append(", ");
            }
            appendLhsColumn(pClass, fields[ii]);

            _builder.append(" = ");
            if (pojo != null) {
                _builder.append("?");
            } else {
                values[ii].accept(this);
            }
        }
        updateClause.getWhereClause().accept(this);
    }

    public void visit (DeleteClause<? extends PersistentRecord> deleteClause)
        throws Exception
    {
        _builder.append("delete from ");
        appendTableName(deleteClause.getPersistentClass());
        _builder.append(" as ");
        appendTableAbbreviation(deleteClause.getPersistentClass());
        _builder.append(" ");
        deleteClause.getWhereClause().accept(this);
    }

    public void visit (InsertClause<? extends PersistentRecord> insertClause)
        throws Exception
    {
        Class<? extends PersistentRecord> pClass = insertClause.getPersistentClass();
        DepotMarshaller<?> marsh = _types.getMarshaller(pClass);
        _innerClause = true;

        String[] fields = marsh.getColumnFieldNames();

        _builder.append("insert into ");
        appendTableName(insertClause.getPersistentClass());
        _builder.append(" (");
        for (int ii = 0; ii < fields.length; ii ++) {
            if (ii > 0) {
                _builder.append(", ");
            }
            appendLhsColumn(pClass, fields[ii]);
        }
        _builder.append(") values(");

        Set<String> idFields = insertClause.getIdentityFields();
        for (int ii = 0; ii < fields.length; ii++) {
            if (ii > 0) {
                _builder.append(", ");
            }
            if (idFields.contains(fields[ii])) {
                _builder.append("DEFAULT");
            } else {
                _builder.append("?");
            }
        }
        _builder.append(")");
    }

    protected abstract void appendIdentifier (String field);

    protected void appendTableName (Class<? extends PersistentRecord> type)
    {
        appendIdentifier(_types.getTableName(type));
    }

    protected void appendTableAbbreviation (Class<? extends PersistentRecord> type)
    {
        appendIdentifier(_types.getTableAbbreviation(type));
    }

    // Constructs a name used for assignment in e.g. INSERT/UPDATE. This is the SQL
    // equivalent of an lvalue; something that can appear to the left of an equals sign.
    // We do not prepend this identifier with a table abbreviation, nor do we expand
    // field overrides, shadowOf declarations, or the like: it is just a column name.
    protected void appendLhsColumn (Class<? extends PersistentRecord> type, String field)
        throws Exception
    {
        DepotMarshaller<?> dm = _types.getMarshaller(type);
        FieldMarshaller<?> fm = dm.getFieldMarshaller(field);
        if (dm == null) {
            throw new IllegalArgumentException(
                "Unknown field on persistent record [record=" + type + ", field=" + field + "]");
        }

        appendIdentifier(fm.getColumnName());
    }

    // Appends an expression for the given field on the given persistent record; this can
    // appear in a SELECT list, in WHERE clauses, etc, etc.
    protected void appendRhsColumn (Class<? extends PersistentRecord> type, String field)
        throws Exception
    {
        DepotMarshaller<?> dm = _types.getMarshaller(type);
        FieldMarshaller<?> fm = dm.getFieldMarshaller(field);
        if (dm == null) {
            throw new IllegalArgumentException(
                "Unknown field on persistent record [record=" + type + ", field=" + field + "]");
        }

        Map<String, FieldDefinition> fieldOverrides = _definitions.get(type);
        if (fieldOverrides != null) {
            // first, see if there's a field override
            FieldDefinition override = fieldOverrides.get(field);

            if (override != null) {
                boolean useOverride;
                if (override instanceof FieldOverride) {
                    if (fm.getComputed() != null || dm.getComputed() != null) {
                        throw new IllegalArgumentException(
                            "FieldOverride cannot be used on @Computed field: " + field);
                    }
                    useOverride = _enableOverrides;
                } else if (fm.getComputed() == null && dm.getComputed() == null) {
                    throw new IllegalArgumentException(
                        "FieldDefinition must not be used on concrete field: " + field);
                } else {
                    useOverride = true;
                }

                if (useOverride) {
                    // If a FieldOverride's target is in turn another FieldOverride, the second
                    // one is ignored. As an example, when creating ItemRecords from CloneRecords,
                    // we make Item.itemId = Clone.itemId. We also make Item.parentId = Item.itemId
                    // and would be dismayed to find Item.parentID = Item.itemId = Clone.itemId.

                    boolean saved = _enableOverrides;
                    _enableOverrides = false;
                    override.accept(this);
                    _enableOverrides = saved;
                    return;
                }
            }
        }

        Computed entityComputed = dm.getComputed();

        // figure out the class we're selecting from unless we're otherwise overriden:
        // for a concrete record, simply use the corresponding table; for a computed one,
        // default to the shadowed concrete record, or null if there isn't one
        Class<? extends PersistentRecord> tableClass;
        if (entityComputed == null) {
            tableClass = type;

        } else if (!PersistentRecord.class.equals(entityComputed.shadowOf())) {
            tableClass = entityComputed.shadowOf();

        } else {
            tableClass = null;
        }

        // handle the field-level @Computed annotation, if there is one
        Computed fieldComputed = fm.getComputed();
        if (fieldComputed != null) {
            // check if the computed field has a literal SQL definition
            if (fieldComputed.fieldDefinition().length() > 0) {
                _builder.append(fieldComputed.fieldDefinition());
                if (_enableAliasing) {
                    _builder.append(" as ");
                    appendIdentifier(field);
                }
                return;
            }

            // or if we can simply ignore the field
            if (!fieldComputed.required()) {
                return;
            }

            // else see if there's an overriding shadowOf definition
            if (fieldComputed.shadowOf() != null) {
                tableClass = fieldComputed.shadowOf();
            }
        }

        // if we get this far we hopefully have a table to select from
        if (tableClass != null) {
            appendTableAbbreviation(tableClass);
            _builder.append(".");
            appendIdentifier(fm.getColumnName());
            return;
        }

        // else owie
        throw new IllegalArgumentException(
            "Persistent field has no definition [class=" + type + ", field=" + field + "]");
    }

    protected BuildVisitor (DepotTypes types)
    {
        _types = types;
    }

    protected DepotTypes _types;

    /** A StringBuilder to hold the constructed SQL. */
    protected StringBuilder _builder = new StringBuilder();

    /** A mapping of field overrides per persistent record. */
    protected Map<Class<? extends PersistentRecord>, Map<String, FieldDefinition>> _definitions=
        new HashMap<Class<? extends PersistentRecord>, Map<String,FieldDefinition>>();

    /** A flag that's set to true for inner SELECT's */
    protected boolean _innerClause = false;

    protected boolean _enableOverrides = false;

    protected boolean _enableAliasing = false;
}
