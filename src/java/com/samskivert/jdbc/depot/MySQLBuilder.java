//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne, Pär Winzell
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

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Set;

import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.depot.FieldMarshaller.BooleanMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.ByteArrayMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.ByteEnumMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.ByteMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.DoubleMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.FloatMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.IntArrayMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.IntMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.LongMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.ObjectMarshaller;
import com.samskivert.jdbc.depot.FieldMarshaller.ShortMarshaller;
import com.samskivert.jdbc.depot.annotation.FullTextIndex;
import com.samskivert.jdbc.depot.clause.DeleteClause;
import com.samskivert.jdbc.depot.expression.ColumnExp;
import com.samskivert.jdbc.depot.expression.EpochSeconds;
import com.samskivert.jdbc.depot.operator.Conditionals.FullTextMatch;

import static com.samskivert.Log.log;

public class MySQLBuilder
    extends SQLBuilder
{
    public class MSBuildVisitor extends BuildVisitor
    {
        @Override public void visit (FullTextMatch match)
            throws Exception
        {
            _builder.append("match(");
            Class<? extends PersistentRecord> pClass = match.getPersistentRecord();
            String[] fields =
                _types.getMarshaller(pClass).getFullTextIndex(match.getName()).fields();
            for (int ii = 0; ii < fields.length; ii ++) {
                if (ii > 0) {
                    _builder.append(", ");
                }
                new ColumnExp(pClass, fields[ii]).accept(this);
            }
            _builder.append(") against (? in boolean mode)");
        }

        @Override public void visit (DeleteClause<? extends PersistentRecord> deleteClause)
            throws Exception
        {
            _builder.append("delete from ");
            appendTableName(deleteClause.getPersistentClass());
            _builder.append(" ");

            // MySQL can't do DELETE FROM SomeTable AS T1, so we turn off abbreviations briefly.
            boolean savedFlag = _types.getUseTableAbbreviations();
            _types.setUseTableAbbreviations(false);
            try {
                deleteClause.getWhereClause().accept(this);
            } finally {
                _types.setUseTableAbbreviations(savedFlag);
            }
        }

        public void visit (EpochSeconds epochSeconds)
            throws Exception
        {
            _builder.append("unix_timestamp(");
            epochSeconds.getArgument().accept(this);
            _builder.append(")");
        }

        protected MSBuildVisitor (DepotTypes types)
        {
            super(types);
        }

        @Override protected void appendTableName (Class<? extends PersistentRecord> type)
        {
            _builder.append(_types.getTableName(type));
        }

        @Override protected void appendTableAbbreviation (Class<? extends PersistentRecord> type)
        {
            _builder.append(_types.getTableAbbreviation(type));
        }

        @Override protected void appendIdentifier (String field)
        {
            _builder.append(field);
        }
    }

    public class MSBindVisitor extends BindVisitor
    {
        protected MSBindVisitor (DepotTypes types, PreparedStatement stmt)
        {
            super(types, stmt);
        }

        @Override public void visit (FullTextMatch match)
            throws Exception
        {
            _stmt.setString(_argIdx ++, match.getQuery());
        }
    }

    public MySQLBuilder (DepotTypes types)
    {
        super(types);
    }

    @Override
    public void getFtsIndexes (
        Iterable<String> columns, Iterable<String> indexes, Set<String> target)
    {
        for (String index : indexes) {
            if (index.startsWith("ftsIx_")) {
                target.add(index.substring("ftsIx_".length()));
            }
        }
    }

    @Override
    public <T extends PersistentRecord> boolean addFullTextSearch (
        Connection conn, DepotMarshaller<T> marshaller, FullTextIndex fts)
        throws SQLException
    {
        Class<T> pClass = marshaller.getPersistentClass();
        StringBuilder update = new StringBuilder("ALTER TABLE ").
            append(marshaller.getTableName()).append(" ADD FULLTEXT INDEX ftsIx_").
            append(fts.name()).append(" (");
        String[] fields = fts.fields();
        for (int ii = 0; ii < fields.length; ii ++) {
            if (ii > 0) {
                update.append(", ");
            }
            update.append(_types.getColumnName(pClass, fields[ii]));
        }
        update.append(")");

        Statement stmt = conn.createStatement();
        try {
            log.info("Adding full-text search index: ftsIx_" + fts.name());
            stmt.executeUpdate(update.toString());
        } finally {
            JDBCUtil.close(stmt);
        }
        return true;
    }

    @Override
    public boolean isPrivateColumn (String column)
    {
        // The MySQL builder does not yet have any private columns.
        return false;
    }

    @Override
    protected String getBooleanDefault ()
    {
        return "0";
    }

    @Override
    protected BuildVisitor getBuildVisitor ()
    {
        return new MSBuildVisitor(_types);
    }

    @Override
    protected BindVisitor getBindVisitor (PreparedStatement stmt)
    {
        return new MSBindVisitor(_types, stmt);
    }

    @Override
    protected <T> String getColumnType (FieldMarshaller<?> fm, int length)
    {
        if (fm instanceof ByteMarshaller) {
            return "TINYINT";
        } else if (fm instanceof ShortMarshaller) {
            return "SMALLINT";
        } else if (fm instanceof IntMarshaller) {
            return "INTEGER";
        } else if (fm instanceof LongMarshaller) {
            return "BIGINT";
        } else if (fm instanceof FloatMarshaller) {
            return "FLOAT";
        } else if (fm instanceof DoubleMarshaller) {
            return "DOUBLE";
        } else if (fm instanceof ObjectMarshaller) {
            Class<?> ftype = fm.getField().getType();
            if (ftype.equals(Byte.class)) {
                return "SMALLINT";
            } else if (ftype.equals(Short.class)) {
                return "SMALLINT";
            } else if (ftype.equals(Integer.class)) {
                return "INTEGER";
            } else if (ftype.equals(Long.class)) {
                return "BIGINT";
            } else if (ftype.equals(Float.class)) {
                return "FLOAT";
            } else if (ftype.equals(Double.class)) {
                return "DOUBLE";
            } else if (ftype.equals(String.class)) {
                if (length < (1 << 15)) {
                    return "VARCHAR(" + length + ")";
                }
                return "TEXT";
            } else if (ftype.equals(Date.class)) {
                return "DATE";
            } else if (ftype.equals(Time.class)) {
                return "DATETIME";
            } else if (ftype.equals(Timestamp.class)) {
                return "DATETIME";
            } else if (ftype.equals(Blob.class)) {
                return "BYTEA";
            } else if (ftype.equals(Clob.class)) {
                return "TEXT";
            } else {
                throw new IllegalArgumentException(
                    "Don't know how to create SQL for " + ftype + ".");
            }
        } else if (fm instanceof ByteArrayMarshaller) {
            // semi-arbitrarily use VARBINARY() up to 32767
            if (length < (1 << 15)) {
                return "VARBINARY(" + length + ")";
            }
            // use BLOB to 65535
            if (length < (1 << 16)) {
                return "BLOB";
            }
            if (length < (1 << 24)) {
                return "MEDIUMBLOB";
            }
            return "LONGBLOB";

        } else if (fm instanceof IntArrayMarshaller) {
            return "BLOB";
        } else if (fm instanceof ByteEnumMarshaller) {
            return "TINYINT";
        } else if (fm instanceof BooleanMarshaller) {
            return "TINYINT";
        } else {
            throw new IllegalArgumentException("Unknown field marshaller type: " + fm.getClass());
        }
    }
}
