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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import com.samskivert.jdbc.depot.clause.DeleteClause;
import com.samskivert.jdbc.depot.clause.FieldDefinition;
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
import com.samskivert.jdbc.depot.expression.EpochSeconds;
import com.samskivert.jdbc.depot.expression.ExpressionVisitor;
import com.samskivert.jdbc.depot.expression.FunctionExp;
import com.samskivert.jdbc.depot.expression.LiteralExp;
import com.samskivert.jdbc.depot.expression.SQLExpression;
import com.samskivert.jdbc.depot.expression.ValueExp;
import com.samskivert.jdbc.depot.operator.Conditionals.Exists;
import com.samskivert.jdbc.depot.operator.Conditionals.FullTextMatch;
import com.samskivert.jdbc.depot.operator.Conditionals.In;
import com.samskivert.jdbc.depot.operator.Conditionals.IsNull;
import com.samskivert.jdbc.depot.operator.Logic.Not;
import com.samskivert.jdbc.depot.operator.SQLOperator.BinaryOperator;
import com.samskivert.jdbc.depot.operator.SQLOperator.MultiOperator;

import com.samskivert.util.StringUtil;

/**
 * Implements the base functionality of the argument-binding pass of {@link SQLBuilder}. Dialectal
 * subclasses of this should be created and returned from {@link SQLBuilder#getBindVisitor()}.
 *
 * This class is intimately paired with {#link BuildVisitor}.
 */
public class BindVisitor implements ExpressionVisitor
{
    public void visit (FromOverride override)
    {
        // nothing needed
    }

    public void visit (FieldDefinition fieldOverride)
    {
        // nothing needed
    }

    public void visit (Key<? extends PersistentRecord> key)
    {
        for (Comparable<?> value : key.getValues()) {
            if (value != null) {
                writeValueToStatement(value);
            }
        }
    }

    public void visit (MultiKey<? extends PersistentRecord> key)
    {
        for (Map.Entry<String, Comparable<?>> entry : key.getSingleFieldsMap().entrySet()) {
            if (entry.getValue() != null) {
                writeValueToStatement(entry.getValue());
            }
        }
        Comparable<?>[] values = key.getMultiValues();
        for (int ii = 0; ii < values.length; ii++) {
            writeValueToStatement(values[ii]);
        }
    }

    public void visit (FunctionExp functionExp)
    {
        visit(functionExp.getArguments());
    }

    public void visit (EpochSeconds epochSeconds)
    {
        epochSeconds.getArgument().accept(this);
    }

    public void visit (MultiOperator multiOperator)
    {
        visit(multiOperator.getConditions());
    }

    public void visit (BinaryOperator binaryOperator)
    {
        binaryOperator.getLeftHandSide().accept(this);
        binaryOperator.getRightHandSide().accept(this);
    }

    public void visit (IsNull isNull)
    {
    }

    public void visit (In in)
    {
        Comparable<?>[] values = in.getValues();
        for (int ii = 0; ii < values.length; ii++) {
            writeValueToStatement(values[ii]);
        }
    }

    public void visit (FullTextMatch match)
    {
        // we never get here
    }

    public void visit (ColumnExp columnExp)
    {
        // no arguments
    }

    public void visit (Not not)
    {
        not.getCondition().accept(this);
    }

    public void visit (GroupBy groupBy)
    {
        visit(groupBy.getValues());
    }

    public void visit (ForUpdate forUpdate)
    {
        // do nothing
    }

    public void visit (OrderBy orderBy)
    {
        visit(orderBy.getValues());
    }

    public void visit (WhereClause where)
    {
        where.getWhereExpression().accept(this);
    }

    public void visit (Join join)
    {
        join.getJoinCondition().accept(this);
    }

    public void visit (Limit limit)
    {
        try {
            _stmt.setInt(_argIdx++, limit.getCount());
            _stmt.setInt(_argIdx++, limit.getOffset());
        } catch (SQLException sqe) {
            throw new DatabaseException("Failed to configure statement with limit clause " +
                                        "[count=" + limit.getCount() +
                                        ", offset=" + limit.getOffset() + "]", sqe);
        }
    }

    public void visit (LiteralExp literalExp)
    {
        // do nothing
    }

    public void visit (ValueExp valueExp)
    {
        writeValueToStatement(valueExp.getValue());
    }

    public void visit (Exists<? extends PersistentRecord> exists)
    {
        exists.getSubClause().accept(this);
    }

    public void visit (SelectClause<? extends PersistentRecord> selectClause)
    {
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
    }

    public void visit (UpdateClause<? extends PersistentRecord> updateClause)
    {
        DepotMarshaller<?> marsh = _types.getMarshaller(updateClause.getPersistentClass());

        // bind the update arguments
        Object pojo = updateClause.getPojo();
        if (pojo != null) {
            for (String field : updateClause.getFields()) {
                try {
                    marsh.getFieldMarshaller(field).getAndWriteToStatement(_stmt, _argIdx++, pojo);
                } catch (Exception e) {
                    throw new DatabaseException(
                        "Failed to read field from persistent record and write it to prepared " +
                        "statement [field=" + field + "]", e);
                }
            }
        } else {
            visit(updateClause.getValues());
        }
        updateClause.getWhereClause().accept(this);
    }

    public void visit (InsertClause<? extends PersistentRecord> insertClause)
    {
        DepotMarshaller<?> marsh = _types.getMarshaller(insertClause.getPersistentClass());
        Object pojo = insertClause.getPojo();
        Set<String> idFields = insertClause.getIdentityFields();
        for (String field : marsh.getColumnFieldNames()) {
            if (!idFields.contains(field)) {
                try {
                    marsh.getFieldMarshaller(field).getAndWriteToStatement(_stmt, _argIdx++, pojo);
                } catch (Exception e) {
                    throw new DatabaseException(
                        "Failed to read field from persistent record and write it to prepared " +
                        "statement [field=" + field + "]", e);
                }
            }
        }
    }

    public void visit (DeleteClause<? extends PersistentRecord> deleteClause)
    {
        deleteClause.getWhereClause().accept(this);
    }

    protected BindVisitor (DepotTypes types, PreparedStatement stmt)
    {
        _types = types;
        _stmt = stmt;
        _argIdx = 1;
    }

    protected void visit (SQLExpression[] expressions)
    {
        for (int ii = 0; ii < expressions.length; ii ++) {
            expressions[ii].accept(this);
        }
    }

    // write the value to the next argument slot in the prepared statement
    protected void writeValueToStatement (Object value)
    {
        try {
            // setObject handles almost all conversions internally, but enums require special care
            if (value instanceof ByteEnum) {
                _stmt.setByte(_argIdx++, ((ByteEnum)value).toByte());
            } else {
                _stmt.setObject(_argIdx++, value);
            }
        } catch (SQLException sqe) {
            throw new DatabaseException("Failed to write value to statement [idx=" + (_argIdx-1) +
                                        ", value=" + StringUtil.safeToString(value) + "]", sqe);
        }
    }

    protected DepotTypes _types;
    protected PreparedStatement _stmt;
    protected int _argIdx;
}
