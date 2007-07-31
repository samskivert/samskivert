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
import java.util.List;
import java.util.Map;

import com.samskivert.jdbc.depot.Key.WhereCondition;
import com.samskivert.jdbc.depot.annotation.Computed;
import com.samskivert.jdbc.depot.clause.DeleteClause;
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

    public void visit (FieldOverride fieldOverride)
        throws Exception
    {
        fieldOverride.getOverride().accept(this);
        _builder.append(" as ");
        appendField(fieldOverride.getField());
    }

    public void visit (WhereCondition<? extends PersistentRecord> whereCondition)
        throws Exception
    {
        String[] keyFields = Key.getKeyFields(whereCondition.getPersistentClass());
        Comparable[] values = whereCondition.getValues();
        for (int ii = 0; ii < keyFields.length; ii ++) {
            if (ii > 0) {
                _builder.append(" and ");
            }
            appendColumn(whereCondition.getPersistentClass(), keyFields[ii]);
            _builder.append(values[ii] == null ? " is null " : " = ? ");
        }
    }

    public void visit (Key key)
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
        for (Map.Entry<String, Comparable> entry : key.getSingleFieldsMap().entrySet()) {
            if (first) {
                first = false;
            } else {
                _builder.append(" and ");
            }
            appendColumn(key.getPersistentClass(), entry.getKey());
            _builder.append(entry.getValue() == null ? " is null " : " = ? ");
        }
        if (!first) {
            _builder.append(" and ");
        }
        appendColumn(key.getPersistentClass(), key.getMultiField());
        _builder.append(" in (");

        Comparable[] values = key.getMultiValues();
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
        binaryOperator.getLeftHandSide().accept(this);
        _builder.append(binaryOperator.operator());
        binaryOperator.getRightHandSide().accept(this);
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
        Comparable[] values = in.getValues();
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
        appendTableAbbreviation(columnExp.getPersistentClass());
        _builder.append(".");
        appendColumn(columnExp.getPersistentClass(), columnExp.getField());
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

        Computed entityComputed = pClass.getAnnotation(Computed.class);

        // iterate over the fields we're filling in and figure out whence each one comes
        boolean skip = true;
        for (String field : selectClause.getFields()) {
            if (!skip) {
                _builder.append(", ");
            }
            skip = false;

            // first, see if there's a field override
            FieldOverride override = selectClause.lookupOverride(field);
            if (override != null) {
                override.accept(this);
                continue;
            }

            // figure out the class we're selecting from unless we're otherwise overriden:
            // for a concrete record, simply use the corresponding table; for a computed one,
            // default to the shadowed concrete record, or null if there isn't one

            Class<? extends PersistentRecord> tableClass;
            if (entityComputed == null) {
                tableClass = pClass;
            } else if (!PersistentRecord.class.equals(entityComputed.shadowOf())) {
                tableClass = entityComputed.shadowOf();
            } else {
                tableClass = null;
            }

            // handle the field-level @Computed annotation, if there is one
            FieldMarshaller fm = _types.getMarshaller(pClass).getFieldMarshaller(field);
            if (fm == null) {
                throw new IllegalArgumentException(
                    "could not find marshaller for field: " + field);
            }
            Computed fieldComputed = fm.getComputed();
            if (fieldComputed != null) {
                // check if the computed field has a literal SQL definition
                if (fieldComputed.fieldDefinition().length() > 0) {
                    _builder.append(fieldComputed.fieldDefinition()).append(" as ");
                    appendField(field);
                    continue;
                }

                // or if we can simply ignore the field
                if (!fieldComputed.required()) {
                    skip = true;
                    continue;
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
                appendColumn(tableClass, field);
                continue;
            }

            // else owie
            throw new IllegalArgumentException(
                "Persistent field has no definition [class=" +
                selectClause.getPersistentClass() + ", field=" + field + "]");
        }

        if (selectClause.getFromOverride() != null) {
            selectClause.getFromOverride().accept(this);

        } else if (_types.getTableName(pClass) != null) {
            _builder.append(" from ");
            appendTableName(pClass);
            _builder.append(" as ");
            appendTableAbbreviation(pClass);

        } else {
            throw new SQLException("Query on @Computed entity with no FromOverrideClause.");
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
        if (isInner) {
            _builder.append(")");
        }
    }

    public void visit (UpdateClause<? extends PersistentRecord> updateClause)
        throws Exception
    {
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
            appendColumn(pClass, fields[ii]);

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
        DepotMarshaller marsh = _types.getMarshaller(pClass);
        _innerClause = true;

        String[] fields = marsh.getColumnFieldNames();

        _builder.append("insert into ");
        appendTableName(insertClause.getPersistentClass());
        _builder.append(" (");
        for (int ii = 0; ii < fields.length; ii ++) {
            if (ii > 0) {
                _builder.append(", ");
            }
            appendColumn(pClass, fields[ii]);
        }
        _builder.append(") values(");

        String ixField = insertClause.getIndexField();
        for (int ii = 0; ii < fields.length; ii++) {
            if (ii > 0) {
                _builder.append(", ");
            }
            if (ixField != null && ixField.equals(fields[ii])) {
                _builder.append("DEFAULT");
            } else {
                _builder.append("?");
            }
        }
        _builder.append(")");
    }

    protected abstract void appendTableName (Class<? extends PersistentRecord> type);
    protected abstract void appendTableAbbreviation (Class<? extends PersistentRecord> type);
    protected abstract void appendColumn (Class<? extends PersistentRecord> type, String field);
    protected abstract void appendField (String field);

    protected BuildVisitor (DepotTypes types)
    {
        _types = types;
        _builder = new StringBuilder();
        _innerClause = false;
    }

    protected DepotTypes _types;

    /** A StringBuilder to hold the constructed SQL. */
    protected StringBuilder _builder;

    /** A flag that's set to true for inner SELECT's */
    protected boolean _innerClause;
}
