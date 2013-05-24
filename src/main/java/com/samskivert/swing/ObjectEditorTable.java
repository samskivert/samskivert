//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.event.ActionListener;

import java.lang.reflect.Field;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import com.samskivert.swing.event.CommandEvent;

import com.samskivert.util.ClassUtil;
import com.samskivert.util.ListUtil;
import com.samskivert.util.ObjectUtil;

import static com.samskivert.swing.Log.log;

/**
 * Allows simple displaying and editing of Objects in a table format.
 */
public class ObjectEditorTable extends JTable
{
    /**
     * The default FieldInterpreter, which can be used to customize the
     * name, values, and editing of a field in an Object.
     *
     * There are a number of ways that the field editing can be customized.
     * A custom renderer (and editor) may be installed on the table, possibly
     * in conjunction with overriding getClass(). Or you may simply override
     * getValue (and setValue) to interpret between types, say for instance
     * turning an integer field that may be one of three constant values into
     * String names of the values.
     */
    public static class FieldInterpreter
    {
        /**
         * Get the name that she be used for the column header for the specified
         * field. By default it's merely the name of the field.
         */
        public String getName (Field field)
        {
            return field.getName();
        }

        /**
         * Get the class of the specified field. By default, the class of
         * the field is used, or its object equivalent if it is a primitive
         * class.
         */
        public Class<?> getClass (Field field)
        {
            Class<?> clazz = field.getType();
            return ClassUtil.objectEquivalentOf(clazz);
        }

        /**
         * Get the value of the specified field in the specified object.
         * By default, the field is used to directly access the value.
         */
        public Object getValue (Object obj, Field field)
        {
            try {
                return field.get(obj);
            } catch (IllegalAccessException iae) {
                log.warning("Failed to get value", "field", field, iae);
                return null;
            }
        }

        /**
         * Set the value of the specified field in the specified object.
         * By default, the field is used to directly set the value.
         */
        public void setValue (Object obj, Object value, Field field)
        {
            try {
                field.set(obj, value);
            } catch (IllegalAccessException iae) {
                log.warning("Failed to set value", "field", field, iae);
            }
        }
    }

    /**
     * Construct a table to display the specified class.
     *
     * @param protoClass the Class of the data that will be displayed.
     */
    public ObjectEditorTable (Class<?> protoClass)
    {
        this(protoClass, null);
    }

    /**
     * Construct a table to display and edit the specified class.
     *
     * @param protoClass the Class of the data that will be displayed.
     * @param editableFields the names of the fields that are editable.
     */
    public ObjectEditorTable (Class<?> protoClass, String[] editableFields)
    {
        this(protoClass, editableFields, null);
    }

    /**
     * Construct a table to display and edit the specified class.
     *
     * @param protoClass the Class of the data that will be displayed.
     * @param editableFields the names of the fields that are editable.
     * @param interp The {@link FieldInterpreter} to use.
     */
    public ObjectEditorTable (Class<?> protoClass, String[] editableFields,
                              FieldInterpreter interp)
    {
        this(protoClass, editableFields, interp, null);
    }

    /**
     * Construct a table to display and edit the specified class.
     *
     * @param protoClass the Class of the data that will be displayed.
     * @param editableFields the names of the fields that are editable.
     * @param interp The {@link FieldInterpreter} to use.
     * @param displayFields the fields to display, or null to display all.
     */
    public ObjectEditorTable (Class<?> protoClass, String[] editableFields,
                              FieldInterpreter interp, String[] displayFields)
    {
        _interp = (interp != null) ? interp : new FieldInterpreter();

        // figure out which fields we're going to display
        Field[] fields = ClassUtil.getFields(protoClass);
        if (displayFields != null) {
            _fields = new Field[displayFields.length];
            for (int ii=0; ii < displayFields.length; ii++) {
                for (int jj=0; jj < fields.length; jj++) {
                    if (displayFields[ii].equals(fields[jj].getName())) {
                        _fields[ii] = fields[jj];
                        break;
                    }
                }
                if (_fields[ii] == null) {
                    throw new IllegalArgumentException(
                        "Field not found in prototype class! " +
                        "[class=" + protoClass +
                        ", field=" + displayFields[ii] + "].");
                }
            }

        } else {
            _fields = fields; // use all the fields
        }

        // figure out which fields are editable
        for (int ii=0, nn=_fields.length; ii < nn; ii++) {
            if (ListUtil.contains(editableFields, _fields[ii].getName())) {
                _editable.set(ii);
            }
        }

        setModel(_model);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    /**
     * Set the data to be viewed or edited.
     */
    public void setData (Object[] data)
    {
        _data.clear();
        Collections.addAll(_data, data);
        _model.fireTableDataChanged();
    }

    /**
     * Set the data to be viewed or edited.
     */
    public void setData (Collection<?> data)
    {
        _data.clear();
        _data.addAll(data);
        _model.fireTableDataChanged();
    }

    /**
     * Set this table to merely display / edit one lousy Object.
     */
    public void setData (Object data)
    {
        _data.clear();
        _data.add(data);
        _model.fireTableDataChanged();
    }

    /**
     * Add the specified element to the end of the data set.
     */
    public void addDatum (Object element)
    {
        insertDatum(element, _data.size());
    }

    /**
     * Insert the specified element at the specified row.
     */
    public void insertDatum (Object element, int row)
    {
        _data.add(row, element);
        _model.fireTableRowsInserted(row, row);
    }

    /**
     * Change the value of the specified row.
     */
    public void updateDatum (Object element, int row)
    {
        _data.set(row, element);
        _model.fireTableRowsUpdated(row, row);
    }

    /**
     * Remove the specified element from the set of data.
     */
    public void removeDatum (Object element)
    {
        int dex = _data.indexOf(element);
        if (dex != -1) {
            removeDatum(dex);
        }
    }

    /**
     * Remove the specified row.
     */
    public void removeDatum (int row)
    {
        _data.remove(row);
        _model.fireTableRowsDeleted(row, row);
    }

    /**
     * Get the edited data. Not really needed, since the the data being
     * edited is the same objects that were passed in.
     */
    public Object[] getData ()
    {
        return _data.toArray();
    }

    /**
     * Get the currently selected object, or null if none selected.
     */
    public Object getSelectedObject ()
    {
        int row = getSelectedRow();
        return (row == -1) ? null : _data.get(row);
    }

    /**
     * Add an action listener to this table.
     *
     * When any field is changed in the table, an action will be fired with
     * a {@link CommandEvent}, with source being the table, the command
     * being the name of the field that was updated, and the argument being
     * the object that was updated. Note that no event is fired if a field
     * was edited but the value did not change.
     */
    public void addActionListener (ActionListener listener)
    {
        listenerList.add(ActionListener.class, listener);
    }

    /**
     * Remove the specified action listener.
     */
    public void removeActionListener (ActionListener listener)
    {
        listenerList.remove(ActionListener.class, listener);
    }

    /**
     * A table model that uses the FieldInterpreter to muck with the objects.
     */
    protected AbstractTableModel _model = new AbstractTableModel() {
        /**
         * Get the object at the specified row. Useful for our subclass.
         */
        public Object getObjectAt (int row) {
            return _data.get(row);
        }

        // documentation inherited
        public int getColumnCount ()
        {
            return _fields.length;
        }

        // documentation inherited
        public int getRowCount()
        {
            return _data.size();
        }

        @Override public String getColumnName (int col)
        {
            return _interp.getName(_fields[col]);
        }

        @Override public boolean isCellEditable (int row, int col)
        {
            return _editable.get(col);
        }

        @Override public Class<?> getColumnClass (int col)
        {
            return _interp.getClass(_fields[col]);
        }

        // documentation inherited
        public Object getValueAt (int row, int col)
        {
            Object o = getObjectAt(row);
            return _interp.getValue(o, _fields[col]);
        }

        @Override public void setValueAt (Object value, int row, int col)
        {
            Object o = getObjectAt(row);
            Object oldValue = _interp.getValue(o, _fields[col]);
            // we only set the value if it has changed
            if (!ObjectUtil.equals(oldValue, value)) {
                _interp.setValue(o, value, _fields[col]);

                // fire the event
                CommandEvent event = null;
                Object[] listeners = ObjectEditorTable.this.listenerList
                    .getListenerList();
                for (int ii=listeners.length-2; ii >= 0; ii -= 2) {
                    if (listeners[ii] == ActionListener.class) {
                        // lazy-create the event
                        if (event == null) {
                            event = new CommandEvent(ObjectEditorTable.this,
                                _fields[col].getName(), o);
                        }
                        ((ActionListener) listeners[ii+1]).actionPerformed(
                            event);
                    }
                }
            }
        }
    };

    /** The list of fields in the prototypical object. */
    protected Field[] _fields;

    /** An interpreter that is used to massage values in and out of the
     * objects. */
    protected FieldInterpreter _interp;

    /** A list of flags corresponding to the _fields (and the table columns)
     * that indicate if the field is editable. */
    protected BitSet _editable = new BitSet();

    /** The data being edited. */
    protected ArrayList<Object> _data = new ArrayList<Object>();
}
