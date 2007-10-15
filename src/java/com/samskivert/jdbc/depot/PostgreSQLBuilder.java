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

import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.LiaisonRegistry;
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
import com.samskivert.jdbc.depot.operator.Conditionals.FullTextMatch;
import com.samskivert.util.ArrayUtil;
import com.samskivert.util.StringUtil;

import com.samskivert.Log;

public class PostgreSQLBuilder
    extends SQLBuilder
{
    public class PGBuildVisitor extends BuildVisitor
    {
        @Override
        public void visit (FullTextMatch match)
            throws Exception
        {
            appendIdentifier("ftsCol_" + match.getName());
            _builder.append(" @@ TO_TSQUERY('default', ?)");
        }

        protected PGBuildVisitor (DepotTypes types)
        {
            super(types);
        }

        protected void appendIdentifier (String field)
        {
            _builder.append("\"").append(field).append("\"");
        }
    }

    public class PGBindVisitor extends BindVisitor
    {
        @Override
        public void visit (FullTextMatch match)
            throws Exception
        {
            // The tsearch2 engine takes queries on the form
            //   (foo&bar)|goop
            // so in this first simple implementation, we just take the user query, chop it into
            // words by space/punctuation and 'or' those together like so:
            //   'ho! who goes there?' -> 'ho|who|goes|there'

            String[] searchTerms = match.getQuery().toLowerCase().split("\\W+");
            if (searchTerms.length > 0 && searchTerms[0].length() == 0) {
                searchTerms = ArrayUtil.splice(searchTerms, 0, 1);
            }
            String query = StringUtil.join(searchTerms, "|");
            _stmt.setString(_argIdx ++, query);
        }

        protected PGBindVisitor (DepotTypes types, PreparedStatement stmt)
        {
            super(types, stmt);
        }
    }

    public PostgreSQLBuilder (DepotTypes types)
    {
        super(types);
    }

    @Override
    public void getFtsIndexes (
        Iterable<String> columns, Iterable<String> indexes, Set<String> target)
    {
        for (String column : columns) {
            if (column.startsWith("ftsCol_")) {
                target.add(column.substring("ftsCol_".length()));
            }
        }
    }

    @Override
    public <T extends PersistentRecord> boolean addFullTextSearch (
        Connection conn, DepotMarshaller<T> marshaller, FullTextIndex fts)
        throws SQLException
    {
        Class<T> pClass = marshaller.getPersistentClass();
        DatabaseLiaison liaison = LiaisonRegistry.getLiaison(conn);

        String[] fields = fts.fieldNames();

        String table = marshaller.getTableName();
        String column = "ftsCol_" + fts.name();
        String index = table + "_ftsIx_" + fts.name();
        String trigger = table + "_ftsTrig_" + fts.name();

        // build the UPDATE
        StringBuilder initColumn = new StringBuilder("UPDATE ").
            append(liaison.tableSQL(table)).append(" SET ").append(liaison.columnSQL(column)).
            append(" = TO_TSVECTOR('default', ");

        for (int ii = 0; ii < fields.length; ii ++) {
            if (ii > 0) {
                initColumn.append(" || ' ' || ");
            }
            initColumn.append("COALESCE(").
                append(liaison.columnSQL(_types.getColumnName(pClass, fields[ii]))).
                append(", '')");
        }
        initColumn.append(")");

        // build the CREATE TRIGGER
        StringBuilder createTrigger = new StringBuilder("CREATE TRIGGER ").
            append(liaison.columnSQL(trigger)).append(" BEFORE UPDATE OR INSERT ON ").
            append(liaison.tableSQL(table)).append(" FOR EACH ROW EXECUTE PROCEDURE tsearch2(").
            append(liaison.columnSQL(column)).append(", ");

        for (int ii = 0; ii < fields.length; ii ++) {
            if (ii > 0) {
                createTrigger.append(", ");
            }
            createTrigger.append(liaison.columnSQL(_types.getColumnName(pClass, fields[ii])));
        }
        createTrigger.append(")");

        // build the CREATE INDEX
        StringBuilder createIndex = new StringBuilder("CREATE INDEX ").
            append(liaison.columnSQL(index)).append(" ON " ).append(liaison.tableSQL(table)).
            append(" USING GIST(").append(liaison.columnSQL(column)).append(")");

        Statement stmt = conn.createStatement();
        try {
            Log.info(
                "Adding full-text search column, index and trigger: " + column + ", " +
                index + ", " + trigger);
            liaison.addColumn(conn, table, column, "TSVECTOR", true);
            stmt.executeUpdate(initColumn.toString());
            stmt.executeUpdate(createIndex.toString());
            stmt.executeUpdate(createTrigger.toString());

        } finally {
            JDBCUtil.close(stmt);
        }
        return true;
    }

    @Override
    public boolean isPrivateColumn (String column)
    {
        // filter out any column that we created as part of FTS support
        return column.startsWith("ftsCol_");
    }

    @Override
    protected BuildVisitor getBuildVisitor ()
    {
        return new PGBuildVisitor(_types);
    }

    @Override
    protected BindVisitor getBindVisitor (PreparedStatement stmt)
    {
        return new PGBindVisitor(_types, stmt);
    }

    @Override
    protected <T> String getColumnType (FieldMarshaller fm, int length)
    {
        if (fm instanceof ByteMarshaller) {
            return "SMALLINT";
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
                return "TIME";
            } else if (ftype.equals(Timestamp.class)) {
                return "TIMESTAMP";
            } else if (ftype.equals(Blob.class)) {
                return "BYTEA";
            } else if (ftype.equals(Clob.class)) {
                return "TEXT";
            } else {
                throw new IllegalArgumentException(
                    "Don't know how to create SQL for " + ftype + ".");
            }
        } else if (fm instanceof ByteArrayMarshaller) {
            return "BYTEA";
        } else if (fm instanceof IntArrayMarshaller) {
            return "BYTEA";
        } else if (fm instanceof ByteEnumMarshaller) {
            return "SMALLINT";
        } else if (fm instanceof BooleanMarshaller) {
            return "BOOLEAN";
        } else {
            throw new IllegalArgumentException("Unknown field marshaller type: " + fm.getClass());
        }
    }
}
