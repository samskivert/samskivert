//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc.jora;

import java.io.Serializable;
import java.util.*;
import java.sql.*;
import java.lang.reflect.*;

import com.samskivert.util.StringUtil;

/**
 * Used to establish mapping between corteges of database tables and java classes. this class is
 * responsible for constructing SQL statements for extracting, updating and deleting records of
 * the database table.
 */
public class Table<T>
{
    /**
     * Constructor for table object. Make association between Java class and
     * database table.
     *
     * @param clazz the class that represents a row entry.
     * @param tableName name of database table mapped on this Java class
     * @param key table's primary key. This parameter is used in UPDATE/DELETE
     * operations to locate record in the table.
     * @param mixedCaseConvert whether or not to convert mixed case field
     * names into underscore separated uppercase column names.
     */
    public Table (Class<T> clazz, String tableName, String key,
                  boolean mixedCaseConvert)
    {
        String[] keys = {key};
        init(clazz, tableName, keys, mixedCaseConvert);
    }

    /**
     * Constructor for table object. Make association between Java class and
     * database table.
     *
     * @param clazz the class that represents a row entry.
     * @param tableName name of database table mapped on this Java class
     * @param key table's primary key. This parameter is used in UPDATE/DELETE
     * operations to locate record in the table.
     */
    public Table (Class<T> clazz, String tableName, String key) {
        String[] keys = {key};
        init(clazz, tableName, keys, false);
    }

    /**
     * Constructor for table object. Make association between Java class and
     * database table.
     *
     * @param clazz the class that represents a row entry.
     * @param tableName name of database table mapped on this Java class
     * @param keys table primary keys. This parameter is used in UPDATE/DELETE
     * operations to locate record in the table.
     */
    public Table (Class<T> clazz, String tableName, String[] keys)
    {
        init(clazz, tableName, keys, false);
    }

    /**
     * Constructor for table object. Make association between Java class and
     * database table.
     *
     * @param clazz the class that represents a row entry.
     * @param tableName name of database table mapped on this Java class
     * @param keys table primary keys. This parameter is used in UPDATE/DELETE
     * operations to locate record in the table.
     * @param mixedCaseConvert whether or not to convert mixed case field
     * names into underscore separated uppercase column names.
     */
    public Table (Class<T> clazz, String tableName, String[] keys,
                  boolean mixedCaseConvert)
    {
        init(clazz, tableName, keys, mixedCaseConvert);
    }

    /**
     * Returns the SQL name of the table on which we operate.
     */
    public String getName ()
    {
        return name;
    }

    /**
     * Select records from database table according to search condition
     *
     * @param condition valid SQL condition expression started with WHERE or
     * empty string if all records should be fetched.
     */
    public final Cursor<T> select (Connection conn, String condition)
    {
        String query = "select " + listOfFields + " from " + name +
            " " + condition;
        return new Cursor<T>(this, conn, query);
    }

    /**
     * Select records from database table according to search condition
     * including the specified (comma separated) extra tables into the SELECT
     * clause to facilitate a join in determining the key.
     *
     * @param tables the (comma separated) names of extra tables to include in
     * the SELECT clause.
     * @param condition valid SQL condition expression started with WHERE.
     */
    public final Cursor<T> select (Connection conn, String tables,
                                   String condition)
    {
        String query = "select " + qualifiedListOfFields +
            " from " + name + "," + tables + " " + condition;
        return new Cursor<T>(this, conn, query);
    }

    /**
     * Select records from database table according to search condition
     * including the specified (comma separated) extra tables into the SELECT
     * clause to facilitate a join in determining the key. To facilitate
     * situations where data from multiple tables is being combined into a
     * single object, the fields will not be qualified with the primary table
     * name.
     *
     * @param tables the (comma separated) names of extra tables to include in
     * the SELECT clause.
     * @param condition valid SQL condition expression started with WHERE.
     */
    public final Cursor<T> join (Connection conn, String tables,
                                 String condition)
    {
        String query = "select " + listOfFields +
            " from " + name + "," + tables + " " + condition;
        return new Cursor<T>(this, conn, query);
    }

    /**
     * Like {@link #join} but does a straight join with the specified table.
     */
    public final Cursor<T> straightJoin (Connection conn, String table,
                                         String condition)
    {
        String query = "select " + listOfFields +
            " from " + name + " straight_join " + table + " " + condition;
        return new Cursor<T>(this, conn, query);
    }

    /**
     * Select records from database table using <I>obj</I> object as template.
     *
     * @param obj example object for search: selected objects should match all
     * non-null fields.
     */
    public final Cursor<T> queryByExample (Connection conn, T obj)
    {
        return new Cursor<T>(this, conn, obj, null, false);
    }

    /**
     * Select records from database table using <I>obj</I> object as template
     * for selection.
     *
     * @param obj example object for search.
     * @param mask field mask indicating which fields in the example object
     * should be used when building the query.
     */
    public final Cursor<T> queryByExample (Connection conn, T obj,
                                           FieldMask mask)
    {
        return new Cursor<T>(this, conn, obj, mask, false);
    }

    /**
     * The same as the queryByExample, but string fields for the obj are
     * matched using 'like' instead of equals, which allows you to send % in to
     * do matching.
     */
    public final Cursor<T> queryByLikeExample (Connection conn, T obj)
    {
        return new Cursor<T>(this, conn, obj, null, true);
    }

    /**
     * The same as the queryByExample, but string fields for the obj are
     * matched using 'like' instead of equals, which allows you to send % in to
     * do matching.
     */
    public final Cursor<T> queryByLikeExample (Connection conn, T obj,
                                               FieldMask mask)
    {
        return new Cursor<T>(this, conn, obj, mask, true);
    }

    /**
     * Insert new record in the table.  Values of inserted record fields are
     * taken from specified object.
     *
     * @param obj object specifying values of inserted record fields
     */
    public synchronized void insert (Connection conn, T obj)
        throws SQLException
    {
        StringBuilder sql = new StringBuilder(
            "insert into " + name + " (" + listOfFields + ") values (?");
        for (int i = 1; i < nColumns; i++) {
            sql.append(",?");
        }
        sql.append(")");
        PreparedStatement insertStmt = conn.prepareStatement(sql.toString());
        bindUpdateVariables(insertStmt, obj, null);
        insertStmt.executeUpdate();
        insertStmt.close();
    }

    /**
     * Insert several new records in the table. Values of inserted records
     * fields are taken from objects of specified array.
     *
     * @param objects array with objects specifying values of inserted record
     * fields
     */
    public synchronized void insert (Connection conn, T[] objects)
        throws SQLException
    {
        StringBuilder sql = new StringBuilder(
            "insert into " + name + " (" + listOfFields + ") values (?");
        for (int i = 1; i < nColumns; i++) {
            sql.append(",?");
        }
        sql.append(")");
        PreparedStatement insertStmt = conn.prepareStatement(sql.toString());
        for (int i = 0; i < objects.length; i++) {
            bindUpdateVariables(insertStmt, objects[i], null);
            insertStmt.addBatch();
        }
        insertStmt.executeBatch();
        insertStmt.close();
    }

    /**
     * Returns a field mask that can be configured and used to update subsets
     * of entire objects via calls to {@link #update(Connection,Object,FieldMask)}.
     */
    public FieldMask getFieldMask ()
    {
        return fMask.clone();
    }

    /**
     * Update record in the table using table's primary key to locate record in
     * the table and values of fields of specified object <I>obj</I> to alter
     * record fields.
     *
     * @param obj object specifying value of primary key and new values of
     * updated record fields
     *
     * @return number of objects actually updated
     */
    public int update (Connection conn, T obj)
        throws SQLException
    {
        return update(conn, obj, null);
    }

    /**
     * Update record in the table using table's primary key to locate record in
     * the table and values of fields of specified object <I>obj</I> to alter
     * record fields. Only the fields marked as modified in the supplied field
     * mask will be updated in the database.
     *
     * @param obj object specifying value of primary key and new values of
     * updated record fields
     * @param mask a {@link FieldMask} instance configured to indicate which of
     * the object's fields are modified and should be written to the database.
     *
     * @return number of objects actually updated
     */
    public synchronized int update (Connection conn, T obj, FieldMask mask)
        throws SQLException
    {
        int nUpdated = 0;
        String sql = "update " + name + " set " +
            (mask != null ? buildListOfAssignments(mask) : listOfAssignments) +
            buildUpdateWhere();
        PreparedStatement ustmt = conn.prepareStatement(sql);
        int column = bindUpdateVariables(ustmt, obj, mask);
        for (int i = 0; i < primaryKeys.length; i++) {
            int fidx = primaryKeyIndices[i];
            fields[fidx].bindVariable(ustmt, obj, column+i+1);
        }
        nUpdated = ustmt.executeUpdate();
        ustmt.close();
        return nUpdated;
    }

    /**
     * Update set of records in the table using table's primary key to locate
     * record in the table and values of fields of objects from specified array
     * <I>objects</I> to alter record fields.
     *
     * @param objects array of objects specifying primary keys and and new
     * values of updated record fields
     *
     * @return number of objects actually updated
     */
    public synchronized int update (Connection conn, T[] objects)
        throws SQLException
    {
        if (primaryKeys == null) {
            throw new IllegalStateException(
                "No primary key for table " + name + ".");
        }

        int nUpdated = 0;
        String sql = "update " + name + " set " + listOfAssignments +
            buildUpdateWhere();
        PreparedStatement updateStmt = conn.prepareStatement(sql);
        for (int i = 0; i < objects.length; i++) {
            int column = bindUpdateVariables(updateStmt, objects[i], null);
            for (int j = 0; j < primaryKeys.length; j++) {
                int fidx = primaryKeyIndices[j];
                fields[fidx].bindVariable(
                    updateStmt, objects[i], column+1+j);
            }
            updateStmt.addBatch();
        }
        int rc[] = updateStmt.executeBatch();
        for (int k = 0; k < rc.length; k++) {
            nUpdated += rc[k];
        }
        updateStmt.close();
        return nUpdated;
    }

    /**
     * Delete record with specified value of primary key from the table.
     *
     * @param obj object containing value of primary key.
     */
    public synchronized int delete (Connection conn, T obj)
        throws SQLException
    {
        if (primaryKeys == null) {
            throw new IllegalStateException(
                "No primary key for table " + name + ".");
        }
        int nDeleted = 0;
        StringBuilder sql = new StringBuilder(
            "delete from " + name + " where " + primaryKeys[0] + " = ?");
        for (int i = 1; i < primaryKeys.length; i++) {
            sql.append(" and ").append(primaryKeys[i]).append(" = ?");
        }
        PreparedStatement deleteStmt = conn.prepareStatement(sql.toString());
        for (int i = 0; i < primaryKeys.length; i++) {
            fields[primaryKeyIndices[i]].bindVariable(deleteStmt, obj,i+1);
        }
        nDeleted = deleteStmt.executeUpdate();
        deleteStmt.close();
        return nDeleted;
    }

    /**
     * Delete records with specified primary keys from the table.
     *
     * @param objects array of objects containing values of primary key.
     *
     * @return number of objects actually deleted
     */
    public synchronized int delete (Connection conn, T[] objects)
        throws SQLException
    {
        if (primaryKeys == null) {
            throw new IllegalStateException(
                "No primary key for table " + name + ".");
        }
        int nDeleted = 0;
        StringBuilder sql = new StringBuilder(
            "delete from " + name + " where " + primaryKeys[0] + " = ?");
        for (int i = 1; i < primaryKeys.length; i++) {
            sql.append(" and ").append(primaryKeys[i]).append(" = ?");
        }
        PreparedStatement deleteStmt = conn.prepareStatement(sql.toString());
        for (int i = 0; i < objects.length; i++) {
            for (int j = 0; j < primaryKeys.length; j++) {
                fields[primaryKeyIndices[j]].bindVariable(
                    deleteStmt, objects[i], j+1);
            }
            deleteStmt.addBatch();
        }
        int rc[] = deleteStmt.executeBatch();
        for (int k = 0; k < rc.length; k++) {
            nDeleted += rc[k];
        }
        deleteStmt.close();
        return nDeleted;
    }

    @Override
    public String toString ()
    {
        return "[name=" + name +
            ", primaryKeys=" + StringUtil.toString(primaryKeys) + "]";
    }

    /**
     * Separator of name components of compound field. For example, if Java
     * class contains component "location" of Point class, which has two
     * components "x" and "y", then database table should have columns
     * "location_x" and "location_y" (if '_' is used as separator).
     */
    public static final String fieldSeparator = "_";

    protected final void init (Class<T> clazz, String tableName, String[] keys,
                               boolean mixedCaseConvert)
    {
        name = tableName;
        this.mixedCaseConvert = mixedCaseConvert;
        _rowClass = clazz;
        primaryKeys = keys;
        listOfFields = "";
        qualifiedListOfFields = "";
        listOfAssignments = "";
        ArrayList<FieldDescriptor> fieldsVector =
            new ArrayList<FieldDescriptor>();
        nFields = buildFieldsList(fieldsVector, _rowClass, "");
        fields = fieldsVector.toArray(new FieldDescriptor[nFields]);
        fMask = new FieldMask(fields);

        try {
            constructor = _rowClass.getDeclaredConstructor(new Class<?>[0]);
            setBypass.invoke(constructor, bypassFlag);
        } catch(Exception ex) {}

        if (keys != null && keys.length > 0) {
            primaryKeyIndices = new int[keys.length];
            for (int j = keys.length; --j >= 0;) {
                int i = nFields;
                while (--i >= 0) {
                    if (fields[i].name.equals(keys[j])) {
                        if (!fields[i].isAtomic()) {
                            throw new IllegalArgumentException(
                                "Non-atomic primary key provided");
                        }
                        primaryKeyIndices[j] = i;
                        break;
                    }
                }
                if (i < 0) {
                    throw new NoSuchFieldError("No such field '" + keys[j]
                                               + "' in table " + name);
                }
            }
        }
    }

    protected final String convertName (String name)
    {
        if (mixedCaseConvert) {
            return StringUtil.unStudlyName(name);
        } else {
            return name;
        }
    }

    protected final int buildFieldsList (ArrayList<FieldDescriptor> buf,
                                         Class<?> _rowClass, String prefix)
    {
        Field[] f = _rowClass.getDeclaredFields();

        Class<?> superclass = _rowClass;
        while ((superclass = superclass.getSuperclass()) != null) {
            Field[] inheritedFields = superclass.getDeclaredFields();
            Field[] allFields = new Field[inheritedFields.length + f.length];
            System.arraycopy(inheritedFields, 0, allFields, 0,
                             inheritedFields.length);
            System.arraycopy(f,0, allFields, inheritedFields.length, f.length);
            f = allFields;
        }

        try {
            for (int i = f.length; --i>= 0;) {
                setBypass.invoke(f[i], bypassFlag);
            }
        } catch (IllegalAccessException iae) {
            System.err.println("Failed to set bypass attribute: " + iae);
        } catch (InvocationTargetException ite) {
            System.err.println("Failed to set bypass attribute: " + ite);
        }

        int n = 0;
        for (int i = 0; i < f.length; i++) {
            if ((f[i].getModifiers()&(Modifier.TRANSIENT|Modifier.STATIC))==0)
            {
                String name = f[i].getName();
                Class<?> fieldClass = f[i].getType();
                String fullName = prefix + convertName(name);
                FieldDescriptor fd = new FieldDescriptor(f[i], fullName);
                int type;

                buf.add(fd);
                n += 1;

                String c = fieldClass.getName();
                if (c.equals("byte")) type = FieldDescriptor.t_byte;
                else if (c.equals("short")) type = FieldDescriptor.t_short;
                else if (c.equals("int")) type = FieldDescriptor.t_int;
                else if (c.equals("long")) type = FieldDescriptor.t_long;
                else if (c.equals("float")) type = FieldDescriptor.t_float;
                else if (c.equals("double")) type = FieldDescriptor.t_double;
                else if (c.equals("boolean")) type = FieldDescriptor.t_boolean;
                else if (c.equals("java.lang.Byte"))
                    type = FieldDescriptor.tByte;
                else if (c.equals("java.lang.Short"))
                    type = FieldDescriptor.tShort;
                else if (c.equals("java.lang.Integer"))
                    type = FieldDescriptor.tInteger;
                else if (c.equals("java.lang.Long"))
                    type = FieldDescriptor.tLong;
                else if (c.equals("java.lang.Float"))
                    type = FieldDescriptor.tFloat;
                else if (c.equals("java.lang.Double"))
                    type = FieldDescriptor.tDouble;
                else if (c.equals("java.lang.Boolean"))
                    type = FieldDescriptor.tBoolean;
                else if (c.equals("java.math.BigDecimal"))
                    type = FieldDescriptor.tDecimal;
                else if (c.equals("java.lang.String"))
                    type = FieldDescriptor.tString;
                else if (fieldClass.equals(BYTE_PROTO.getClass()))
                    type = FieldDescriptor.tBytes;
                else if (c.equals("java.sql.Date"))
                    type = FieldDescriptor.tDate;
                else if (c.equals("java.sql.Time"))
                    type = FieldDescriptor.tTime;
                else if (c.equals("java.sql.Timestamp"))
                    type = FieldDescriptor.tTimestamp;
                else if (c.equals("java.lang.InputStream"))
                    type = FieldDescriptor.tStream;
                else if (c.equals("java.sql.BlobLocator"))
                    type = FieldDescriptor.tBlob;
                else if (c.equals("java.sql.ClobLocator"))
                    type = FieldDescriptor.tClob;
                else if (serializableClass.isAssignableFrom(fieldClass))
                    type = FieldDescriptor.tClosure;
                else {
                    int nComponents = buildFieldsList(buf, fieldClass,
                                                      fd.name+fieldSeparator);
                    fd.inType = fd.outType =
                        FieldDescriptor.tCompound + nComponents;

                    try {
                        fd.constructor =
                            fieldClass.getDeclaredConstructor(new Class<?>[0]);
                        setBypass.invoke(fd.constructor, bypassFlag);
                    } catch(Exception ex) {}

                    n += nComponents;
                    continue;
                }
                if (listOfFields.length() != 0) {
                    listOfFields += ",";
                    qualifiedListOfFields += ",";
                    listOfAssignments += ",";
                }
                listOfFields += fullName;
                qualifiedListOfFields += this.name + "." + fullName;
                listOfAssignments += fullName + "=?";

                fd.inType = fd.outType = type;
                nColumns += 1;
            }
        }
        return n;
    }

    protected final String buildListOfAssignments (FieldMask mask)
    {
        StringBuilder sql = new StringBuilder();
        int fcount = fields.length;
        for (int i = 0; i < fcount; i++) {
            // skip non-modified fields
            if (!mask.isModified(i)) {
                continue;
            }
            // separate fields by a comma
            if (sql.length() > 0) {
                sql.append(",");
            }
            // append the necessary SQL to update this column
            sql.append(fields[i].name).append("=?");
        }
        return sql.toString();
    }

    protected final T load (ResultSet result) throws SQLException
    {
        T obj;
        try {
            obj = constructor.newInstance(constructorArgs);
        }
        catch(IllegalAccessException ex) { throw new IllegalAccessError(); }
        catch(InstantiationException ex) { throw new InstantiationError(); }
        catch(Exception ex) {
            throw new InstantiationError("Exception was thrown by constructor");
        }
        load(obj, 0, nFields, 0, result);
        return obj;
    }

    protected final int load (
        Object obj, int i, int end, int column, ResultSet result)
        throws SQLException
    {
        try {
            while (i < end) {
                FieldDescriptor fd = fields[i++];
                if (!fd.loadVariable(result, obj, ++column)) {
                    Object component =
                        fd.constructor.newInstance(constructorArgs);
                    fd.field.set(obj, component);
                    int nComponents = fd.inType - FieldDescriptor.tCompound;
                    column = load(component, i, i + nComponents,
                                  column-1, result);
                    i += nComponents;
                }
            }
        }
        catch(IllegalAccessException ex) { throw new IllegalAccessError(); }
        catch(InstantiationException ex) { throw new InstantiationError(); }
        catch(InvocationTargetException ex) {
            throw new InstantiationError("Exception was thrown by constructor");
        }
        return column;
    }

    protected final int bindUpdateVariables(PreparedStatement pstmt,
                                            T            obj,
                                            FieldMask         mask)
        throws SQLException
    {
        return bindUpdateVariables(pstmt, obj, 0, nFields, 0, mask);
    }

    protected final void bindQueryVariables(PreparedStatement pstmt,
                                            T            obj,
                                            FieldMask         mask)
        throws SQLException
    {
        bindQueryVariables(pstmt, obj, 0, nFields, 0, mask);
    }

    protected final void updateVariables(ResultSet result, T obj)
        throws SQLException
    {
        updateVariables(result, obj, 0, nFields, 0);
        result.updateRow();
    }

    protected final String buildUpdateWhere()
    {
        StringBuilder sql = new StringBuilder();
        sql.append(" where ").append(primaryKeys[0]).append(" = ?");
        for (int i = 1; i < primaryKeys.length; i++) {
            sql.append(" and ").append(primaryKeys[i]).append(" = ?");
        }
        return sql.toString();
    }

    protected final String buildQueryList(T qbe, FieldMask mask, boolean like)
    {
        StringBuilder buf = new StringBuilder();
        buildQueryList(buf, qbe, 0, nFields, mask, like);
        if (buf.length() > 0) {
            buf.insert(0, " where ");
        }
        return "select " + listOfFields + " from " + name + buf;
    }

    protected final int bindUpdateVariables (
        PreparedStatement pstmt, Object obj, int i, int end, int column,
        FieldMask mask)
        throws SQLException
    {
        try {
            while (i < end) {
                FieldDescriptor fd = fields[i++];
                Object comp = null;
                // skip non-modified fields
                if (mask != null && !mask.isModified(i-1)) {
                    continue;
                }
                if (!fd.isBuiltin() && (comp = fd.field.get(obj)) == null) {
                    if (fd.isCompound()) {
                        int nComponents = fd.outType-FieldDescriptor.tCompound;
                        while (--nComponents >= 0) {
                            fd = fields[i++];
                            if (!fd.isCompound()) {
                                pstmt.setNull(++column,
                                              FieldDescriptor.sqlTypeMapping[fd.outType]);
                            }
                        }
                    } else {
                        pstmt.setNull(
                            ++column,
                            FieldDescriptor.sqlTypeMapping[fd.outType]);
                    }
                } else {
                    if (!fd.bindVariable(pstmt, obj, ++column)) {
                        int nComponents = fd.outType-FieldDescriptor.tCompound;
                        column = bindUpdateVariables(
                            pstmt, comp, i, i+nComponents,column-1, mask);
                        i += nComponents;
                    }
                }
            }
        } catch(IllegalAccessException ex) { throw new IllegalAccessError(); }
        return column;
    }

    protected final int bindQueryVariables (
        PreparedStatement pstmt, Object obj, int i, int end, int column,
        FieldMask mask)
        throws SQLException
    {
        try {
            while (i < end) {
                Object comp;
                FieldDescriptor fd = fields[i++];
                if (!fd.field.getDeclaringClass().isInstance(obj)) {
                    return column;
                }
                int nComponents = fd.isCompound() ?
                    fd.outType-FieldDescriptor.tCompound : 0;

                try {
                    // skip closure fields (because querying by them makes
                    // no sense)
                    if (fd.outType == FieldDescriptor.tClosure) {
                        continue;
                    }

                    // if a field mask is specified, use that to determine
                    // whether or not the field should be skipped
                    if (mask != null) {
                        if (!mask.isModified(i-1)) {
                            continue;
                        }
                    }

                    // look up the value of the field
                    comp = fd.field.get(obj);

                    // if no field mask was specified, ignore builtin
                    // fields and those that are null
                    if (mask == null && (fd.isBuiltin() || comp == null)) {
                        continue;
                    }

                    if (!fd.bindVariable(pstmt, obj, ++column)) {
                        column = bindQueryVariables(
                            pstmt, comp, i, i+nComponents, column-1, mask);
                    }

                } finally {
                    i += nComponents;
                }
            }
        } catch(IllegalAccessException ex) { throw new IllegalAccessError(); }
        return column;
    }

    protected final void buildQueryList (
        StringBuilder buf, Object qbe, int i, int end, FieldMask mask,
        boolean like)
    {
        try {
            while (i < end) {
                Object comp;
                FieldDescriptor fd = fields[i++];
                int nComponents =
                    fd.isCompound() ? fd.outType-FieldDescriptor.tCompound : 0;

                try {
                    // skip closure fields (because querying by them makes
                    // no sense)
                    if (fd.outType == FieldDescriptor.tClosure) {
                        continue;
                    }

                    // if a field mask is specified, use that to determine
                    // whether or not the field should be skipped
                    if (mask != null) {
                        if (!mask.isModified(i-1)) {
                            continue;
                        }
                    }

                    // look up the value of the field
                    comp = fd.field.get(qbe);

                    // if no field mask was specified, ignore builtin
                    // fields and those that are null
                    if (mask == null && (fd.isBuiltin() || comp == null)) {
                        continue;
                    }

                    if (nComponents != 0) {
                        buildQueryList(buf, comp, i, i+nComponents, mask, like);
                    } else {
                        if (buf.length() != 0) {
                            buf.append(" AND ");
                        }
                        buf.append(fd.name);
                        if (like && (comp instanceof String)) {
                            buf.append(" like?");
                        } else {
                            buf.append("=?");
                        }
                    }

                } finally {
                    i += nComponents;
                }
            }
        } catch(IllegalAccessException ex) { throw new IllegalAccessError(); }
    }

    protected final int updateVariables (
        ResultSet result, Object obj, int i, int end, int column)
        throws SQLException
    {
        try {
            while (i < end) {
                FieldDescriptor fd = fields[i++];
                Object comp = null;
                if (!fd.isBuiltin() && (comp = fd.field.get(obj)) == null) {
                    if (fd.isCompound()) {
                        int nComponents = fd.outType-FieldDescriptor.tCompound;
                        while (--nComponents >= 0) {
                            fd = fields[i++];
                            if (!fd.isCompound()) {
                                result.updateNull(++column);
                            }
                        }
                    } else {
                        result.updateNull(++column);
                    }
                } else {
                    if (!fd.updateVariable(result, obj, ++column)) {
                        int nComponents = fd.outType-FieldDescriptor.tCompound;
                        column = updateVariables(result, comp,
                                                 i, i+nComponents, column-1);
                        i += nComponents;
                    }
                }
            }
        } catch(IllegalAccessException ex) { throw new IllegalAccessError(); }
        return column;
    }

    protected static Method getSetBypass ()
    {
        try {
            Class<?> c = Class.forName("java.lang.reflect.AccessibleObject");
            return c.getMethod("setAccessible", new Class<?>[] { Boolean.TYPE });
        } catch (Exception ex) {
            System.err.println("Unable to reflect AccessibleObject.setAccessible: " + ex);
            return null;
        }
    }

    protected String name;
    protected String listOfFields;
    protected String qualifiedListOfFields;
    protected String listOfAssignments;
    protected Class<T> _rowClass;

    protected boolean mixedCaseConvert = false;

    protected FieldDescriptor[] fields;
    protected FieldMask fMask;

    protected int nFields;  // length of "fields" array
    protected int nColumns; // number of atomic fields in "fields" array

    protected String primaryKeys[];
    protected int primaryKeyIndices[];

    protected Constructor<T> constructor;

    protected static final Method setBypass = getSetBypass();
    protected static final Class<Serializable> serializableClass = Serializable.class;
    protected static final Object[] bypassFlag = { Boolean.TRUE };
    protected static final Object[] constructorArgs = {};

    // used to identify byte[] fields
    protected static final byte[] BYTE_PROTO = new byte[0];
}
