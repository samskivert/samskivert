//-< Table.java >----------------------------------------------------*--------*
// JORA                       Version 2.0        (c) 1998  GARRET    *     ?  *
// (Java Object Relational Adapter)                                  *   /\|  *
//                                                                   *  /  \  *
//                          Created:     10-Jun-98    K.A. Knizhnik  * / [] \ *
//                          Last update: 20-Jun-98    K.A. Knizhnik  * GARRET *
//-------------------------------------------------------------------*--------*
// Class representing database table
//-------------------------------------------------------------------*--------*

package com.samskivert.jdbc.jora;

import java.util.*;
import java.sql.*;
import java.lang.reflect.*;

/** Table class is used to establish mapping between corteges of database
 *  tables and Java classes. This class is responsible for constructing
 *  SQL statements for extracting, updating and deleting records of the
 *  database table. 
 */
public class Table { 
    /** Constructor for table object. Make association between Java class 
     *  and database table.
     *  
     * @param tclassName name of Java class
     * @param tableName name of database table mapped on this Java class
     * @param key table's primary key. This parameter is used in UPDATE/DELETE
     *  operations to locate record in the table.
     * @param s session, which should be opened before first access to the table
     */
    public Table(String className, String tableName, Session s, String key) {
	String[] keys = {key};
	init(className, tableName, s, keys);
    }

    /** Constructor for table object. Make association between Java class 
     *  and database table.
     *  
     * @param tclassName name of Java class
     * @param tableName name of database table mapped on this Java class
     * @param keys table primary keys. This parameter is used in UPDATE/DELETE
     *  operations to locate record in the table.
     * @param s session, which should be opened before first access to the table
     */
    public Table(String className, String tableName, Session s, String[] keys) 
    { 
	init(className, tableName, s, keys);
    }

    /** Constructor for table object. Make association between Java class 
     *  and database table. Name of Java class should be the same as name of 
     *  the database table
     *  
     * @param className name of Java class, which should be (without 
     *  package prefix) be the same as the name of database table.
     * @param keys table primary keys. This parameter is used in UPDATE/DELETE
     *  operations to locate record in the table.
     * @param s session, which should be opened before first access to the table
     */
    public Table(String className, Session s, String[] keys) {
        init(className, className.substring(className.lastIndexOf('.')+1),
	     s, keys);
    }

    /** Constructor for table object. Make association between Java class 
     *  and database table. Name of Java class should be the same as name of 
     *  the database table
     *  
     * @param className name of Java class, which should be (without 
     *  package prefix) be the same as the name of database table.
     * @param key table primary key. This parameter is used in UPDATE/DELETE
     *  operations to locate record in the table.
     * @param s session, which should be opened before first access to the table
     */
    public Table(String className, Session s, String key) {
	String[] keys = {key};
        init(className, className.substring(className.lastIndexOf('.')+1),
	     s, keys);
    }

    /** Constructor of table without explicit key specification.
     *  Specification of key is necessary for update/remove operations. 
     *  If key is not specified, it is inherited from base table (if any).
     */
    public Table(String className, Session s) {
        init(className, className.substring(className.lastIndexOf('.')+1),
	     s, null);
    }

    /** Constructor of table with "key" and "session" parameters inherited 
     *  from base table. 
     */
    public Table(String className) {
        init(className, className.substring(className.lastIndexOf('.')+1),
	     null, null);
    }

    /** Select records from database table according to search condition
     * 
     * @param condition valid SQL condition expression started with WHERE
     *  or empty string if all records should be fetched.
     */
    public final Cursor select(String condition) { 
	String query = "select " + listOfFields + " from " + name +
	    " " + condition;
        return new Cursor(this, session, 1, query);
    }

    /** Select records from database table according to search condition
     * including the specified (comma separated) extra tables into the
     * SELECT clause to facilitate a join in determining the key.
     * 
     * @param tables the (comma separated) names of extra tables to
     * include in the SELECT clause.
     * @param condition valid SQL condition expression started with WHERE.
     */
    public final Cursor select(String tables, String condition) { 
	String query = "select " + qualifiedListOfFields +
	    " from " + name + "," + tables + " " + condition;
        return new Cursor(this, session, 1, query);
    }

    /** Select records from database table according to search condition
     * 
     * @param condition valid SQL condition expression started with WHERE
     *  or empty string if all records should be fetched.
     * @param session user database session
     */
    public final Cursor select(String condition, Session session) { 
	String query = "select " + listOfFields + " from " + name +
	    " " + condition;
        return new Cursor(this, session, 1, query);
    }

    /** Select records from specified and derived database tables
     * 
     * @param condition valid SQL condition expression started with WHERE
     *  or empty string if all records should be fetched.
     */
    public final Cursor selectAll(String condition) { 
        return new Cursor(this, session, nDerived+1, condition);
    }

    /** Select records from specified and derived database tables
     * 
     * @param condition valid SQL condition expression started with WHERE
     *  or empty string if all records should be fetched.
     * @param session user database session
     */
    public final Cursor selectAll(String condition, Session session) { 
        return new Cursor(this, session, nDerived+1, condition);
    }
    
    /** Select records from database table using <I>obj</I> object as 
     * template for selection. All non-builtin fields of this object,
     * which are not null, are compared with correspondent table values.
     * 
     * @param obj object for construction search condition: selected objects
     *  should match all non-null fields of specified object.
     */
    public final Cursor queryByExample(Object obj) { 
        return new Cursor(this, session, 1, obj);
    }

    /** Select records from database table using <I>obj</I> object as 
     * template for selection. All non-builtin fields of this object,
     * which are not null, are compared with correspondent table values.
     * 
     * @param obj object for construction search condition: selected objects
     *  should match all non-null fields of specified object.
     * @param session user database session
     */
    public final Cursor queryByExample(Object obj, Session session) { 
        return new Cursor(this, session, 1, obj);
    }

    /** Select records from specified and derived database tables using 
     * <I>obj</I> object as template for selection. 
     * All non-builtin fields of this object,
     * which are not null, are compared with correspondent table values.
     * 
     * @param obj object for construction search condition: selected objects
     *  should match all non-null fields of specified object.
     */
    public final Cursor queryAllByExample(Object obj) { 
        return new Cursor(this, session, nDerived+1, obj);
    }

    /** Select records from specified and derived database tables using 
     * <I>obj</I> object as template for selection. 
     * All non-builtin fields of this object,
     * which are not null, are compared with correspondent table values.
     * 
     * @param obj object for construction search condition: selected objects
     *  should match all non-null fields of specified object.
     * @param session user database session
     */
    public final Cursor queryAllByExample(Object obj, Session session) { 
        return new Cursor(this, session, nDerived+1, obj);
    }

    /** Insert new record in the table. Values of inserted record fields 
     *  are taken from specifed object.
     * 
     * @param obj object specifing values of inserted record fields
     */
    public void insert(Object obj)
	throws SQLException
    { 
	insert(obj, session);
    }

    /** Insert new record in the table using specified database session. 
     *  Values of inserted record fields 
     *  are taken from specifed object.
     * 
     * @param obj object specifing values of inserted record fields
     * @param session user database session
     */
    public synchronized void insert(Object obj, Session session)
	throws SQLException
    { 
	if (session == null) { 
	    session = ((SessionThread)Thread.currentThread()).session;
	}
//        try { 
	    checkConnection(session);
	    if (insertStmt == null) { 
	        String sql = "insert into " + name + " (" 
                           + listOfFields + ") values (?";
		for (int i = 1; i < nColumns; i++) {   
                     sql += ",?";
		}
		sql += ")";
	        insertStmt = session.connection.prepareStatement(sql);
   	    }
	    bindUpdateVariables(insertStmt, obj); 	
	    insertStmt.executeUpdate();
	    insertStmt.clearParameters();
//	} catch(SQLException ex) { session.handleSQLException(ex); }
    }

    /** Insert several new records in the table. Values of inserted records 
     *  fields are taken from objects of specified array.
     * 
     * @param objects array with objects specifing values of inserted record 
     * fields
     */
    public void insert(Object[] objects)
	throws SQLException
    { 
	insert(objects, session);
    }
  
    /** Insert several new records in the table. Values of inserted records 
     *  fields are taken from objects of specified array.
     * 
     * @param objects array with objects specifing values of inserted record 
     *                fields
     * @param session user database session
     */
    public synchronized void insert(Object[] objects, Session session)
	throws SQLException
    { 
	if (session == null) { 
	    session = ((SessionThread)Thread.currentThread()).session;
	}
//        try { 
	    checkConnection(session);
	    if (insertStmt == null) { 
	        String sql = "insert into " + name + " (" 
                           + listOfFields + ") values (?";
		for (int i = 1; i < nColumns; i++) {   
                     sql += ",?";
		}
		sql += ")";
	        insertStmt = session.connection.prepareStatement(sql);
   	    }
	    for (int i = 0; i < objects.length; i++) { 
  	        bindUpdateVariables(insertStmt, objects[i]); 
	        insertStmt.addBatch();
	    }
	    insertStmt.executeBatch();
	    insertStmt.clearParameters();
//	} catch(SQLException ex) { session.handleSQLException(ex); }
    }
  
    /** Update record in the table using table's primary key to locate 
     *  record in the table and values of fields of specified object <I>obj</I>
     *  to alter record fields.
     * 
     * @param obj object specifing value of primary key and new values of 
     *  updated record fields
     * 
     * @return number of objects actually updated
    */
    public int update(Object obj)
	throws SQLException
    { 
	return update(obj, session);
    }

    /** Update record in the table using table's primary key to locate 
     *  record in the table and values of fields of specified object <I>obj</I>
     *  to alter record fields.
     * 
     * @param obj object specifing value of primary key and new values of 
     *  updated record fields
     * @param session user database session
     * 
     * @return number of objects actually updated
     */
    public synchronized int update(Object obj, Session session)
	throws SQLException
    { 
        if (primaryKeys == null) { 
	    throw new NoPrimaryKeyError(this);
	}
	if (session == null) { 
	    session = ((SessionThread)Thread.currentThread()).session;
	}
	int nUpdated = 0;
//        try { 
	    checkConnection(session);
	    if (updateStmt == null) { 
	        String sql = "update " + name + " set " + listOfAssignments 
		           + " where " + primaryKeys[0] + " = ?"; 
		for (int i = 1; i < primaryKeys.length; i++) { 
		    sql += " and " + primaryKeys[i] + " = ?";
		}
		updateStmt = session.connection.prepareStatement(sql);
	    }
	    for (int i = 0; i < primaryKeys.length; i++) { 
		fields[primaryKeyIndices[i]].bindVariable(updateStmt, obj, 
							  nColumns+i+1);
	    } 	
	    bindUpdateVariables(updateStmt, obj); 
	    nUpdated = updateStmt.executeUpdate();
	    updateStmt.clearParameters();
//	} catch(SQLException ex) { session.handleSQLException(ex); }
	return nUpdated;
    }

    /** Update set of records in the table using table's primary key to locate 
     *  record in the table and values of fields of objects from sepecifed
     *  array <I>objects</I>  to alter record fields.
     * 
     * @param objects array of objects specifing primiray keys and and new 
     * values of updated record fields
     *
     * @return number of objects actually updated
     */
    public int update(Object[] objects)
	throws SQLException
    { 
	return update(objects, session);
    }

    /** Update set of records in the table using table's primary key to locate 
     *  record in the table and values of fields of objects from sepecifed
     *  array <I>objects</I>  to alter record fields.
     * 
     * @param objects array of objects specifing primiray keys and and new 
     * values of updated record fields
     * @param session user database session
     *
     * @return number of objects actually updated
     */
    public synchronized int update(Object[] objects, Session session)
	throws SQLException
    { 
        if (primaryKeys == null) { 
	    throw new NoPrimaryKeyError(this);
	}
	if (session == null) { 
	    session = ((SessionThread)Thread.currentThread()).session;
	}
	int nUpdated = 0;
//        try { 
	    checkConnection(session);
	    if (updateStmt == null) { 
	        String sql = "update " + name + " set " + listOfAssignments 
		           + " where " + primaryKeys[0] + " = ?"; 
		for (int i = 1; i < primaryKeys.length; i++) { 
		    sql += " and " + primaryKeys[i] + " = ?";
		}
		updateStmt = session.connection.prepareStatement(sql);
	    }
	    for (int i = 0; i < objects.length; i++) { 
		for (int j = 0; j < primaryKeys.length; j++) { 
		    fields[primaryKeyIndices[j]].bindVariable(updateStmt,
							      objects[i],
							      nColumns+1+j);
		}
 	        bindUpdateVariables(updateStmt, objects[i]); 
	        updateStmt.addBatch();
	    }
	    int rc[] = updateStmt.executeBatch();
	    for (int k = 0; k < rc.length; k++) { 
		nUpdated += rc[k];
	    }
	    updateStmt.clearParameters();
//	} catch(SQLException ex) { session.handleSQLException(ex); }
	return nUpdated;
    }
  
    /** Delete record with specified value of primary key from the table. 
     * 
     * @param obj object containing value of primary key.
     *
     * @return number of objects actually deleted
     */
    public int delete(Object obj)
	throws SQLException
    { 
	return delete(obj, session);
    }

    /** Delete record with specified value of primary key from the table. 
     * 
     * @param obj object containing value of primary key.
     * @param session user database session
     */
    public synchronized int delete(Object obj, Session session)
	throws SQLException
    { 
        if (primaryKeys == null) { 
	    throw new NoPrimaryKeyError(this);
	}
	if (session == null) { 
	    session = ((SessionThread)Thread.currentThread()).session;
	}
	int nDeleted = 0;
//        try { 
	    checkConnection(session);
	    if (deleteStmt == null) { 
	        String sql = "delete from " + name + 
		    " where " + primaryKeys[0] + " = ?";
		for (int i = 1; i < primaryKeys.length; i++) { 
		    sql += " and " + primaryKeys[i] + " = ?";
		}
		deleteStmt = session.connection.prepareStatement(sql);
	    }
	    for (int i = 0; i < primaryKeys.length; i++) { 
		fields[primaryKeyIndices[i]].bindVariable(deleteStmt, obj,i+1);
	    } 	
	    nDeleted = deleteStmt.executeUpdate();
	    deleteStmt.clearParameters();
//	} catch(SQLException ex) { session.handleSQLException(ex); }
	return nDeleted;
    }


    /** Delete records with specified primary keys from the table. 
     * 
     * @param objects array of objects containing values of primary key.
     *
     * @return number of objects actually deleted
     */
    public int delete(Object[] objects)
	throws SQLException
    { 
	return delete(objects, session);
    }

    /** Delete records with specified primary keys from the table. 
     * 
     * @param objects array of objects containing values of primary key.
     *
     * @return number of objects actually deleted
     */
    public synchronized int delete(Object[] objects, Session session)
	throws SQLException
    { 
        if (primaryKeys == null) { 
	    throw new NoPrimaryKeyError(this);
	}
	if (session == null) { 
	    session = ((SessionThread)Thread.currentThread()).session;
	}
	int nDeleted = 0;
//        try { 
	    checkConnection(session);
	    if (deleteStmt == null) { 
	        String sql = "delete from " + name + 
		    " where " + primaryKeys[0] + " = ?"; 
		for (int i = 1; i < primaryKeys.length; i++) { 
		    sql += " and " + primaryKeys[i] + " = ?";
		}
		deleteStmt = session.connection.prepareStatement(sql);
	    }
	    for (int i = 0; i < objects.length; i++) { 
		for (int j = 0; j < primaryKeys.length; j++) { 
		    fields[primaryKeyIndices[j]].bindVariable(deleteStmt, 
							      objects[i], j+1);
		}
		deleteStmt.addBatch();
	    }
	    int rc[] = deleteStmt.executeBatch();
	    for (int k = 0; k < rc.length; k++) { 
		nDeleted += rc[k];
	    }
	    deleteStmt.clearParameters();
//	} catch(SQLException ex) { session.handleSQLException(ex); }
	return nDeleted;
    }

    /**
     * Configures the table to convert mixed case Java fields
     * (e.g. <code>locationId</code>) into underscore separated uppercase
     * fields (e.g. <code>LOCATION_ID</code>).
     */
    public void setMixedCaseConvert (boolean mixedCaseConvert)
    {
        this.mixedCaseConvert = mixedCaseConvert;
    }

    /** Spearator of name components of compound field. For example, if Java
     *  class constains component "location" of Point class, which
     *  has two components "x" and "y", then database table should
     *  have columns "location_x" and "location_y" (if '_' is used 
     *  as separator)
     */
    public static String fieldSeparator = "_";


    /** Some versions of JDBC driver doesn't support 
     *  <code>getBigDecimal(int columnIndex)</code>. Setting this variable to 
     *  <code>true</code> makes JORA to explicitly request scale from result 
     *  set metadata and use deprecated version of <code>getBigDecimal</code>
     *  with extra <code>scale</code> parameter.
     */
    public static boolean useDepricatedGetBigDecimal = true;


    // --- Implementation -----------------------------------------
   
    /** Is table abstract - not present in database.
     */
    protected boolean isAbstract;
    protected Table   derived;
    protected int     nDerived;

    protected String  name;
    protected String  listOfFields;
    protected String  qualifiedListOfFields;
    protected String  listOfAssignments;
    protected Class   cls;
    protected Session session;

    protected boolean mixedCaseConvert = false;

    static private Class serializableClass;
    private   FieldDescriptor[] fields;

    private   int     nFields;  // length of "fields" array
    private   int     nColumns; // number of atomic fields in "fields" array

    private   String  primaryKeys[];
    private   int     primaryKeyIndices[];

    protected int     connectionID;

    private PreparedStatement updateStmt;
    private PreparedStatement deleteStmt;
    private PreparedStatement insertStmt;

    private static Table  allTables;
    private Constructor   constructor;
    private static Method setBypass;

    private static final Object[] bypassFlag = { new Boolean(true) };
    private static final Object[] constructorArgs = {};

    static { 
        try { 
	    serializableClass = Class.forName("java.io.Serializable");
	    Class c = Class.forName("java.lang.reflect.AccessibleObject");
	    Class[] param = { Boolean.TYPE };
	    setBypass = c.getMethod("setAccessible", param);
        } catch(Exception ex) {}
    }      


    private final void init(String className, String tableName, Session s, 
			    String[] keys) 
    {
        name = tableName;
	try { 
	    cls = Class.forName(className);
	} catch(ClassNotFoundException ex) {throw new NoClassDefFoundError();}
	isAbstract = tableName == null;
	session = s;
	primaryKeys = keys;
	listOfFields = "";
	qualifiedListOfFields = "";
	listOfAssignments = "";
	connectionID = 0;
	Vector fieldsVector = new Vector();
	nFields = buildFieldsList(fieldsVector, cls, "");
	fields = new FieldDescriptor[nFields];
	fieldsVector.copyInto(fields);

	try { 
	    constructor = cls.getDeclaredConstructor(new Class[0]);
	    setBypass.invoke(constructor, bypassFlag);
	} catch(Exception ex) {}

	if (keys != null) { 
	    if (keys.length == 0) { 
		throw new NoPrimaryKeyError(this);
	    }
	    primaryKeyIndices = new int[keys.length];
	    for (int j = keys.length; --j >= 0;) { 
		int i = nFields; 
		while (--i >= 0) { 
		    if (fields[i].name.equals(keys[j])) { 
			if (!fields[i].isAtomic()) {
			    throw new NoPrimaryKeyError(this);
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
	insertIntoTableHierarchy();
    }


    private final void insertIntoTableHierarchy()
    {
	Table t, prev = null;
	Table after = null;
	int nChilds = 0;
	for (t = allTables; t != null; prev = t, t = t.derived) { 
	    if (t.cls.isAssignableFrom(cls)) { 
 	        if (primaryKeys == null && t.primaryKeys != null) { 
		    primaryKeys = t.primaryKeys;
		    primaryKeyIndices = t.primaryKeyIndices;
		}
		if (session == null) { 
		    session = t.session;
		}
		t.nDerived += 1;
		after = t;
	    } else if (cls.isAssignableFrom(t.cls)) { 
		after = prev;
		do { 		  
		    if (cls.isAssignableFrom(t.cls)) { 
		        if (primaryKeys != null && t.primaryKeys == null) { 
			    t.primaryKeys = primaryKeys;
			    t.primaryKeyIndices = primaryKeyIndices;
			}
			if (t.session == null) { 
			    t.session = session;
			}
			nChilds += 1;
		    }
		} while ((t = t.derived) != null); 
		break;
	    }
	}	    
	if (after == null) { 
	    derived = allTables;
	    allTables = this;
	} else {
	    derived = after.derived;
	    after.derived = this;
	}
	nDerived = nChilds;
    }

    private final void checkConnection(Session s) throws SQLException {
        if (connectionID != s.connectionID) { 
	    if (insertStmt != null) { 
	        insertStmt.close();
	        insertStmt = null;
  	    }
	    if (updateStmt != null) { 
	        updateStmt.close();
	        updateStmt = null;
	    }
	    if (deleteStmt != null) { 
	        deleteStmt.close();
	        deleteStmt = null;
	    }
	    connectionID = s.connectionID;
        }
    }

    private final String convertName (String name)
    {
        if (mixedCaseConvert) {
            boolean seenLower = false;
            StringBuffer nname = new StringBuffer();
            int nlen = name.length();
            for (int i = 0; i < nlen; i++) {
                char c = name.charAt(i);
                // if we see an upper case character and we've seen a lower
                // case character since the last time we did so, slip in an _
                if (Character.isUpperCase(c)) {
                    nname.append("_");
                    seenLower = false;
                    nname.append(c);
                } else {
                    seenLower = true;
                    nname.append(Character.toUpperCase(c));
                }
            }
            return nname.toString();

        } else {
            return name;
        }
    }

    private final int buildFieldsList(Vector buf, Class cls, String prefix)
    {
	Field[] f = cls.getDeclaredFields();

	Class superclass = cls;
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
	} catch(Exception ex) {
	    System.err.println("Failed to set bypass attribute");
	}

	int n = 0;
	for (int i = 0; i < f.length; i++) { 
	    if ((f[i].getModifiers()&(Modifier.TRANSIENT|Modifier.STATIC))==0)
	    {
		String name = f[i].getName();
		Class fieldClass = f[i].getType();
		String fullName = prefix + convertName(name);
		FieldDescriptor fd = new FieldDescriptor(f[i], fullName);
		int type;

		buf.addElement(fd);
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
			  fieldClass.getDeclaredConstructor(new Class[0]);
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
	    

    protected final Object load(ResultSet result) throws SQLException { 
	Object obj;
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

    private final int load(Object obj, int i, int end, int column,
			    ResultSet result) 
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

    protected final void bindUpdateVariables(PreparedStatement pstmt,
					     Object            obj)
      throws SQLException
    {
        bindUpdateVariables(pstmt, obj, 0, nFields, 0); 	
    }

    protected final void bindQueryVariables(PreparedStatement pstmt,
					    Object            obj)
       throws SQLException
    {
	bindQueryVariables(pstmt, obj, 0, nFields, 0); 	
    }


    protected final void updateVariables(ResultSet result, Object obj)
       throws SQLException
    {
        updateVariables(result, obj, 0, nFields, 0);
	result.updateRow();
    }


    protected final String buildQueryList(Object qbe)
    {
        StringBuffer buf = new StringBuffer();
        buildQueryList(buf, qbe, 0, nFields);
	if (buf.length() > 0) {
	    buf.insert(0, " where ");
	}
	return "select " + listOfFields + " from " + name + buf;
    }


    private final int bindUpdateVariables(PreparedStatement pstmt, Object obj,
					  int i, int end, int column) 
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
			        pstmt.setNull(++column, 
				   FieldDescriptor.sqlTypeMapping[fd.outType]);
			    } 
			}
		    } else { 
		        pstmt.setNull(++column, 
				      FieldDescriptor.sqlTypeMapping[fd.outType]);
		    } 
		} else { 
		    if (!fd.bindVariable(pstmt, obj, ++column)) { 
			int nComponents = fd.outType-FieldDescriptor.tCompound;
			column = bindUpdateVariables(pstmt, comp, 
						     i,i+nComponents,column-1);
			i += nComponents;
		    }
		}
	    }
	} catch(IllegalAccessException ex) { throw new IllegalAccessError(); }
	return column;
    }

    private final int bindQueryVariables(PreparedStatement pstmt, Object obj,
					 int i, int end, int column) 
      throws SQLException
    { 
        try { 
	    while (i < end) { 
		Object comp;
		FieldDescriptor fd = fields[i++];
		if (!fd.field.getDeclaringClass().isInstance(obj)) { 
		    return column;
		}
		int nComponents = 
		    fd.isCompound() ? fd.outType-FieldDescriptor.tCompound : 0;
		if (!fd.isBuiltin() 
		    && fd.outType != FieldDescriptor.tClosure 
		    && (comp = fd.field.get(obj)) != null) 
	        {
		    if (!fd.bindVariable(pstmt, obj, ++column)) { 
		        column = bindQueryVariables(pstmt, comp, 
						    i,i+nComponents,column-1);
		    }
		}
		i += nComponents;
	    }
	} catch(IllegalAccessException ex) { throw new IllegalAccessError(); }
	return column;
    }

    private final void buildQueryList(StringBuffer buf, Object qbe, 
				      int i, int end)
    {
	try { 
	    while (i < end) { 
		Object comp;
		FieldDescriptor fd = fields[i++];
		int nComponents = 
		    fd.isCompound() ? fd.outType-FieldDescriptor.tCompound : 0;
		if (!fd.isBuiltin() 
		    && fd.outType != FieldDescriptor.tClosure 
		    && (comp = fd.field.get(qbe)) != null) 
		{
		    if (nComponents != 0) { 
			buildQueryList(buf, comp, i, i+nComponents);
		    } else { 
			if (buf.length() != 0) { 
			    buf.append(",");
			}
			buf.append(fd.name);
			buf.append("=?");
		    }
		} 
		i += nComponents;
	    }
	} catch(IllegalAccessException ex) { throw new IllegalAccessError(); }
    }

    protected final int updateVariables(ResultSet result, Object obj,
					 int i, int end, int column) 
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

    // used to identify byte[] fields
    private static final byte[] BYTE_PROTO = new byte[0];
}
