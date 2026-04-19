//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc.jora;

import java.sql.*;
import java.math.*;
import java.lang.reflect.*;

class FieldDescriptor
{
    protected FieldDescriptor (Field field, String name)
    {
        this.name = name;
        this.field = field;
        this.scale = -1;
    }

    protected final boolean isAtomic ()
    {
        return inType < tClosure;
    }

    protected final boolean isCompound ()
    {
        return inType >= tCompound;
    }

    protected final boolean isBuiltin ()
    {
        return inType <= t_boolean;
    }

    protected final boolean bindVariable (
        PreparedStatement pstmt, Object obj, int column)
        throws SQLException
    {
        try {
            switch (outType) {
            case t_byte:
                pstmt.setByte(column, field.getByte(obj));
                break;
            case t_short:
                pstmt.setShort(column, field.getShort(obj));
                break;
            case t_int:
                pstmt.setInt(column, field.getInt(obj));
                break;
            case t_long:
                pstmt.setLong(column, field.getLong(obj));
                break;
            case t_float:
                pstmt.setFloat(column, field.getFloat(obj));
                break;
            case t_double:
                pstmt.setDouble(column, field.getDouble(obj));
                break;
            case t_boolean:
                pstmt.setBoolean(column, field.getBoolean(obj));
                break;
            case tByte:
                pstmt.setByte(column, ((Byte)field.get(obj)).byteValue());
                break;

            case tShort:
                pstmt.setShort(column, ((Short)field.get(obj)).shortValue());
                break;
            case tInteger:
                pstmt.setInt(column, ((Integer)field.get(obj)).intValue());
                break;
            case tLong:
                pstmt.setLong(column, ((Long)field.get(obj)).longValue());
                break;
            case tFloat:
                pstmt.setFloat(column, ((Float)field.get(obj)).floatValue());
                break;
            case tDouble:
                pstmt.setDouble(column,((Double)field.get(obj)).doubleValue());
                break;
            case tBoolean:
                pstmt.setBoolean(column, ((Boolean)field.get(obj)).booleanValue());
                break;

            case tDecimal:
                pstmt.setBigDecimal(column, (BigDecimal)field.get(obj));
                break;
            case tString:
                pstmt.setString(column, (String)field.get(obj));
                break;
            case tBytes:
                pstmt.setBytes(column, (byte[])field.get(obj));
                break;
            case tDate:
                pstmt.setDate(column, (java.sql.Date)field.get(obj));
                break;
            case tTime:
                pstmt.setTime(column, (java.sql.Time)field.get(obj));
                break;
            case tTimestamp:
                pstmt.setTimestamp(column, (java.sql.Timestamp)field.get(obj));
                break;
            case tStream:
                java.io.InputStream in = (java.io.InputStream)field.get(obj);
                pstmt.setBinaryStream(column, in, in.available());
                break;
            case tBlob:
                pstmt.setBlob(column, (Blob)field.get(obj));
                break;
            case tClob:
                pstmt.setClob(column, (Clob)field.get(obj));
                break;
            case tAsString:
                pstmt.setString(column, field.get(obj).toString());
                break;
            case tClosure:
                // There is no reason to use piped streams because
                // we need to pass total number of bytes to JDBC driver
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                java.io.ObjectOutputStream clu = new java.io.ObjectOutputStream(out);
                clu.writeObject(field.get(obj));
                clu.close();
                pstmt.setBytes(column, out.toByteArray());
                break;
            default:
                return false;
            }
        } catch(SQLException ex) {
            if (outType != tClosure && outType != tAsString) {
                outType = tAsString;
                return bindVariable(pstmt, obj, column);
            } else {
                throw ex;
            }
        } catch(IllegalAccessException ex) {
            ex.printStackTrace();
            throw new IllegalAccessError();
        } catch(java.io.IOException ex) {
            throw new DataTransferError(ex);
        }
        return true;
    }

    protected final boolean updateVariable (ResultSet result, Object obj, int column)
        throws SQLException
    {
        try {
            switch (outType) {
            case t_byte:
                result.updateByte(column, field.getByte(obj));
                break;
            case t_short:
                result.updateShort(column, field.getShort(obj));
                break;
            case t_int:
                result.updateInt(column, field.getInt(obj));
                break;
            case t_long:
                result.updateLong(column, field.getLong(obj));
                break;
            case t_float:
                result.updateFloat(column, field.getFloat(obj));
                break;
            case t_double:
                result.updateDouble(column, field.getDouble(obj));
                break;
            case t_boolean:
                result.updateBoolean(column, field.getBoolean(obj));
                break;

            case tByte:
                result.updateByte(column, ((Byte)field.get(obj)).byteValue());
                break;
            case tShort:
                result.updateShort(column, ((Short)field.get(obj)).shortValue());
                break;
            case tInteger:
                result.updateInt(column, ((Integer)field.get(obj)).intValue());
                break;
            case tLong:
                result.updateLong(column, ((Long)field.get(obj)).longValue());
                break;
            case tFloat:
                result.updateFloat(column, ((Float)field.get(obj)).floatValue());
                break;
            case tDouble:
                result.updateDouble(column, ((Double)field.get(obj)).doubleValue());
                break;
            case tBoolean:
                result.updateBoolean(column, ((Boolean)field.get(obj)).booleanValue());
                break;

            case tDecimal:
                result.updateBigDecimal(column, (BigDecimal)field.get(obj));
                break;
            case tString:
                result.updateString(column, (String)field.get(obj));
                break;
            case tBytes:
                result.updateBytes(column, (byte[])field.get(obj));
                break;
            case tDate:
                result.updateDate(column, (java.sql.Date)field.get(obj));
                break;
            case tTime:
                result.updateTime(column, (java.sql.Time)field.get(obj));
                break;
            case tTimestamp:
                result.updateTimestamp(column, (java.sql.Timestamp)field.get(obj));
                break;
            case tStream:
                java.io.InputStream in = (java.io.InputStream)field.get(obj);
                result.updateBinaryStream(column, in, in.available());
                break;
            case tBlob:
                Blob blob = (Blob)field.get(obj);
                result.updateBinaryStream(column, blob.getBinaryStream(), (int)blob.length());
                break;
            case tClob:
                Clob clob = (Clob)field.get(obj);
                result.updateCharacterStream(column, clob.getCharacterStream(), (int)clob.length());
                break;
            case tAsString:
                result.updateString(column, field.get(obj).toString());
                break;
            case tClosure:
                // There is no reason to use piped streams because
                // we need to pass total number of bytes to JDBC driver
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                java.io.ObjectOutputStream clu = new java.io.ObjectOutputStream(out);
                clu.writeObject(field.get(obj));
                clu.close();
                result.updateBytes(column, out.toByteArray());
                break;
            default:
                return false;
            }
        } catch(SQLException ex) {
            if (outType != tClosure && outType != tAsString) {
                outType = tAsString;
                return updateVariable(result, obj, column);
            } else {
                throw ex;
            }
        } catch(IllegalAccessException ex) {
            ex.printStackTrace();
            throw new IllegalAccessError();
        } catch(java.io.IOException ex) {
            throw new DataTransferError(ex);
        }
        return true;
    }

    protected final boolean loadVariable (
        ResultSet result, Object obj, int column)
        throws SQLException, IllegalAccessException
    {
        switch (inType) {
        case t_byte:
            field.setByte(obj, result.getByte(column));
            break;
        case t_short:
            field.setShort(obj, result.getShort(column));
            break;
        case t_int:
            field.setInt(obj, result.getInt(column));
            break;
        case t_long:
            field.setLong(obj, result.getLong(column));
            break;
        case t_float:
            field.setFloat(obj, result.getFloat(column));
            break;
        case t_double:
            field.setDouble(obj, result.getDouble(column));
            break;
        case t_boolean:
            field.setBoolean(obj, result.getBoolean(column));
            break;

        case tByte:
            byte b = result.getByte(column);
            field.set(obj, result.wasNull() ? null : Byte.valueOf(b));
            break;
        case tShort:
            short s = result.getShort(column);
            field.set(obj, result.wasNull() ? null : Short.valueOf(s));
            break;
        case tInteger:
            int i = result.getInt(column);
            field.set(obj, result.wasNull() ? null : Integer.valueOf(i));
            break;
        case tLong:
            long l = result.getLong(column);
            field.set(obj, result.wasNull() ? null : Long.valueOf(l));
            break;
        case tFloat:
            float f = result.getFloat(column);
            field.set(obj, result.wasNull() ? null : Float.valueOf(f));
            field.setFloat(obj, result.getFloat(column));
            break;
        case tDouble:
            double d = result.getDouble(column);
            field.set(obj, result.wasNull() ? null : Double.valueOf(d));
            break;
        case tBoolean:
            boolean bl = result.getBoolean(column);
            field.set(obj, result.wasNull() ? null : Boolean.valueOf(bl));
            break;

        case tDecimal:
            field.set(obj, result.getBigDecimal(column));
            break;
        case tString:
            field.set(obj, result.getString(column));
            break;
        case tBytes:
            field.set(obj, result.getBytes(column));
            break;
        case tDate:
            field.set(obj, result.getDate(column));
            break;
        case tTime:
            field.set(obj, result.getTime(column));
            break;
        case tTimestamp:
            field.set(obj, result.getTimestamp(column));
            break;
        case tStream:
            field.set(obj, result.getBinaryStream(column));
            break;
        case tBlob:
            field.set(obj, result.getBlob(column));
            break;
        case tClob:
            field.set(obj, result.getClob(column));
            break;
        case tClosure:
            try {
                java.io.InputStream input = result.getBinaryStream(column);
                java.io.ObjectInputStream in = new java.io.ObjectInputStream(input);
                field.set(obj, in.readObject());
                in.close();
            } catch(ClassNotFoundException ex) {
                throw new DataTransferError(ex);
            } catch(java.io.IOException ex) {
                throw new DataTransferError(ex);
            }
            break;
        default:
            return false;
        }
        return true;
    }

    protected int    inType;  // type tag for field input (see constants below)
    protected int    outType; // type tag for field output
    protected int    scale;   // scale for tDecimal type,
    protected String name;    // full (compound) name of component
    protected Field  field;   // field info from java.lang.reflect

    protected Constructor<?> constructor; // constructor of object component

    protected static final int t_byte         = 0;
    protected static final int t_short        = 1;
    protected static final int t_int          = 2;
    protected static final int t_long         = 3;
    protected static final int t_float        = 4;
    protected static final int t_double       = 5;
    protected static final int t_boolean      = 6;
    protected static final int tByte          = 7;
    protected static final int tShort         = 8;
    protected static final int tInteger       = 9;
    protected static final int tLong          = 10;
    protected static final int tFloat         = 11;
    protected static final int tDouble        = 12;
    protected static final int tBoolean       = 13;
    protected static final int tDecimal       = 14;
    protected static final int tString        = 15;
    protected static final int tBytes         = 16;
    protected static final int tDate          = 17;
    protected static final int tTime          = 18;
    protected static final int tTimestamp     = 19;
    protected static final int tStream        = 20;
    protected static final int tBlob          = 21;
    protected static final int tClob          = 22;
    protected static final int tAsString      = 23;

    protected static final int tClosure       = 24;
    protected static final int tCompound      = 25;

    protected static final int[] sqlTypeMapping = {
        Types.INTEGER, // t_byte
        Types.INTEGER, // t_short
        Types.INTEGER, // t_int
        Types.BIGINT,  // t_long
        Types.FLOAT,   // t_float
        Types.DOUBLE,  // t_double
        Types.BIT,     // t_boolean
        Types.INTEGER, // tByte
        Types.INTEGER, // tShort
        Types.INTEGER, // tInteger
        Types.BIGINT,  // tLong
        Types.FLOAT,   // tFloat
        Types.DOUBLE,  // tDouble
        Types.BIT,     // tBoolean
        Types.NUMERIC, // tDecimal
        Types.VARCHAR,  // tString
        Types.VARBINARY,// tBytes
        Types.DATE,     // tDate
        Types.TIME,      // tTime
        Types.TIMESTAMP, // tTimestamp
        Types.LONGVARBINARY, // tStream
        Types.LONGVARBINARY, // tBlob
        Types.LONGVARCHAR,   // tClob
        Types.VARCHAR,       // tAsString
        Types.LONGVARBINARY  // tClosure
    };
}
