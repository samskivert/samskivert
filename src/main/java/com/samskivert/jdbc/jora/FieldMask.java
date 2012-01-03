//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc.jora;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Provides support for doing partial updates to objects in a JORA table.
 * A field mask can be obtained for a particular table, fields marked as
 * modified and the object subsequently updated in a fairly
 * straightforward manner:
 *
 * <pre>
 * // updating parts of a table that contains User objects
 * User user = // load user object from table
 * FieldMask mask = table.getFieldMask();
 * user.firstName = newFirstName;
 * mask.setModified("firstName");
 * user.lastName = newLastName;
 * mask.setModified("lastName");
 * table.update(user, mask);
 * </pre>
 */
public class FieldMask
    implements Cloneable
{
    /**
     * Creates a field mask for a {@link Table} that uses the supplied field descriptors.
     */
    public FieldMask (FieldDescriptor[] descrips)
    {
        this(toNames(descrips));
    }

    /**
     * Creates a field mask using the supplied field names.
     */
    public FieldMask (String[] names)
    {
        for (int ii = 0; ii < names.length; ii++) {
            _descripMap.put(names[ii], ii);
        }
        _modified = new boolean[names.length];
    }

    /**
     * Returns true if any of the fields in this mask are modified.
     */
    public final boolean isModified ()
    {
        int mcount = _modified.length;
        for (int ii = 0; ii < mcount; ii++) {
            if (_modified[ii]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the field with the specified index is modified.
     */
    public final boolean isModified (int index)
    {
        return _modified[index];
    }

    /**
     * Returns true if the field with the specified name is modifed.
     */
    public final boolean isModified (String fieldName)
    {
        Integer index = _descripMap.get(fieldName);
        if (index == null) {
            throw new IllegalArgumentException("Field not in mask: " + fieldName);
        }
        return _modified[index.intValue()];
    }

    /**
     * Returns true only if the set of modified fields is a subset of the
     * fields specified.
     */
    public final boolean onlySubsetModified (Set<String> fieldSet)
    {
        for (String field : _descripMap.keySet()) {
            if (isModified(field) && (!fieldSet.contains(field))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Marks the specified field as modified.
     */
    public void setModified (String fieldName)
    {
        Integer index = _descripMap.get(fieldName);
        if (index == null) {
            throw new IllegalArgumentException("Field not in mask: " + fieldName);
        }
        _modified[index.intValue()] = true;
    }

    /**
     * Clears out the modification state of the fields in this mask.
     */
    public void clear ()
    {
        Arrays.fill(_modified, false);
    }

    /**
     * Creates a copy of this field mask, with all fields set to
     * not-modified.
     */
    @Override
    public FieldMask clone ()
    {
        try {
            FieldMask mask = (FieldMask)super.clone();
            mask._modified = new boolean[_modified.length];
            return mask;
        } catch (CloneNotSupportedException cnse) {
            throw new AssertionError(cnse); // won't happen; we're Cloneable
        }
    }

    @Override
    public String toString ()
    {
        // return a list of the modified fields
        StringBuilder buf = new StringBuilder("FieldMask [modified={");
        boolean added = false;
        for (Map.Entry<String,Integer> entry : _descripMap.entrySet()) {
            if (_modified[entry.getValue().intValue()]) {
                if (added) {
                    buf.append(", ");
                } else {
                    added = true;
                }
                buf.append(entry.getKey());
            }
        }
        buf.append("}]");

        return buf.toString();
    }

    protected static String[] toNames (FieldDescriptor[] descrips)
    {
        // create a mapping from field name to descriptor index
        int dcount = (descrips == null ? 0 : descrips.length);
        String[] names = new String[dcount];
        for (int ii = 0; ii < dcount; ii++) {
            names[ii] = descrips[ii].field.getName();
        }
        return names;
    }

    /** Modified flags for each field of an object in this table. */
    protected boolean[] _modified;

    /** A mapping from field names to field descriptor index. */
    protected Map<String, Integer> _descripMap = new HashMap<String, Integer>();
}
