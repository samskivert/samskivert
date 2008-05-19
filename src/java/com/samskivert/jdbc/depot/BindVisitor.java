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
import java.util.Map;
import java.util.Set;

import com.samskivert.jdbc.depot.Key.WhereCondition;
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
import com.samskivert.jdbc.depot.operator.Conditionals.In;
import com.samskivert.jdbc.depot.operator.Conditionals.IsNull;
import com.samskivert.jdbc.depot.operator.Conditionals.FullTextMatch;
import com.samskivert.jdbc.depot.operator.Logic.Not;
import com.samskivert.jdbc.depot.operator.SQLOperator.BinaryOperator;
import com.samskivert.jdbc.depot.operator.SQLOperator.MultiOperator;

/**
 * Implements the base functionality of the argument-binding pass of {@link SQLBuilder}. Dialectal
 * subclasses of this should be created and returned from {@link SQLBuilder#getBindVisitor()}.
 *
 * This class is intimately paired with {#link BuildVisitor}.
 */
public class BindVisitor implements ExpressionVisitor
{
    public void visit (FromOverride override)
        throws Exception
    {
    }

    public void visit (FieldDefinition fieldOverride)
        throws Exception
    {
    }

    public void visit (WhereCondition<? extends PersistentRecord> whereCondition)
        throws Exception
    {
        for (Comparable value : whereCondition.getValues()) {
            if (value != null) {
                _stmt.setObject(_argIdx ++, value);
            }
        }
    }

    public void visit (Key key)
        throws Exception
    {
        key.condition.accept(this);
    }

    public void visit (MultiKey<? extends PersistentRecord> key)
        throws Exception
    {
        for (Map.Entry entry : key.getSingleFieldsMap().entrySet()) {
            if (entry.getValue() != null) {
                _stmt.setObject(_argIdx ++, entry.getValue());
            }
        }
        Comparable[] values = key.getMultiValues();
        for (int ii = 0; ii < values.length; ii++) {
            _stmt.setObject(_argIdx ++, values[ii]);
        }
    }

    public void visit (FunctionExp functionExp)
        throws Exception
    {
        visit(functionExp.getArguments());
    }

    public void visit (EpochSeconds epochSeconds)
        throws Exception
    {
        epochSeconds.getArgument().accept(this);
    }

    public void visit (MultiOperator multiOperator)
        throws Exception
    {
        visit(multiOperator.getConditions());
    }

    public void visit (BinaryOperator binaryOperator) throws Exception
    {
        binaryOperator.getLeftHandSide().accept(this);
        binaryOperator.getRightHandSide().accept(this);
    }

    public void visit (IsNull isNull) throws Exception
    {
    }

    public void visit (In in) throws Exception
    {
        Comparable[] values = in.getValues();
        for (int ii = 0; ii < values.length; ii++) {
            _stmt.setObject(_argIdx ++, values[ii]);
        }
    }

    public void visit (FullTextMatch match) throws Exception
    {
        // we never get here
    }

    public void visit (ColumnExp columnExp) throws Exception
    {
        // no arguments
    }

    public void visit (Not not) throws Exception
    {
        not.getCondition().accept(this);
    }

    public void visit (GroupBy groupBy) throws Exception
    {
        visit(groupBy.getValues());
    }

    public void visit (ForUpdate forUpdate) throws Exception
    {
        // do nothing
    }

    public void visit (OrderBy orderBy) throws Exception
    {
        visit(orderBy.getValues());
    }

    public void visit (Where where) throws Exception
    {
        where.getCondition().accept(this);
    }

    public void visit (Join join) throws Exception
    {
        join.getJoinCondition().accept(this);
    }

    public void visit (Limit limit) throws Exception
    {
        _stmt.setObject(_argIdx++, limit.getCount());
        _stmt.setObject(_argIdx++, limit.getOffset());
    }

    public void visit (LiteralExp literalExp) throws Exception
    {
        // do nothing
    }

    public void visit (ValueExp valueExp) throws Exception
    {
        // workaround for postgres bug, fixed in next release:
        // http://archives.postgresql.org/pgsql-jdbc/2007-06/msg00080.php
        if (valueExp.getValue() instanceof Byte) {
            _stmt.setByte(_argIdx ++, (Byte) valueExp.getValue());
        } else {
            _stmt.setObject(_argIdx ++, valueExp.getValue());
        }
    }

    public void visit (Exists<? extends PersistentRecord> exists)
        throws Exception
    {
        exists.getSubClause().accept(this);
    }

    public void visit (SelectClause<? extends PersistentRecord> selectClause)
        throws Exception
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

    public void visit (UpdateClause<? extends PersistentRecord> updateClause) throws Exception
    {
        DepotMarshaller marsh = _types.getMarshaller(updateClause.getPersistentClass());

        // bind the update arguments
        String[] fields = updateClause.getFields();
        Object pojo = updateClause.getPojo();
        if (pojo != null) {
            for (int ii = 0; ii < fields.length; ii ++) {
                marsh.getFieldMarshaller(fields[ii]).readFromObject(pojo, _stmt, _argIdx++);
            }
        } else {
            visit(updateClause.getValues());
        }
        updateClause.getWhereClause().accept(this);
    }

    public void visit (DeleteClause<? extends PersistentRecord> deleteClause) throws Exception
    {
        deleteClause.getWhereClause().accept(this);
    }

    public void visit (InsertClause<? extends PersistentRecord> insertClause) throws Exception
    {
        DepotMarshaller marsh = _types.getMarshaller(insertClause.getPersistentClass());

        Object pojo = insertClause.getPojo();
        Set<String> idFields = insertClause.getIdentityFields();
        for (String field : marsh.getColumnFieldNames()) {
            if (!idFields.contains(field)) {
                marsh.getFieldMarshaller(field).readFromObject(pojo, _stmt, _argIdx ++);
            }
        }
    }

    protected BindVisitor (DepotTypes types, PreparedStatement stmt)
    {
        _types = types;
        _stmt = stmt;
        _argIdx = 1;
    }

    protected void visit (SQLExpression[] expressions)
        throws Exception
    {
        for (int ii = 0; ii < expressions.length; ii ++) {
            expressions[ii].accept(this);
        }
    }

    protected DepotTypes _types;
    protected PreparedStatement _stmt;
    protected int _argIdx;
}
