//
// $Id: RuntimeAdjust.java,v 1.6 2003/01/15 03:28:17 mdb Exp $

package com.samskivert.util;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import com.samskivert.Log;
import com.samskivert.swing.CollapsiblePanel;
import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.MultiLineLabel;
import com.samskivert.swing.ScrollablePanel;
import com.samskivert.swing.VGroupLayout;

/**
 * Provides a service where named variables can be registered as
 * adjustable by the developer at runtime. Generally a special key is
 * configured (via {@link DebugChords}) to pop up the adjustable variables
 * interface which can then be used to toggle booleans, change integer
 * values and generally adjust debugging and tuning parameters. This is
 * not meant to be an interface for end-user configurable parameters, but
 * to allow the developer to tweak runtime parameters more easily.
 *
 * <p> Adjustments are bound to a {@link Config} property which can be
 * changed through the config interface or through the bound runtime
 * adjust.
 *
 * <p> <em>Note:</em> adjusts are meant to be arranged in a two level
 * hierarchy. An adjust's name, therefore, should be of the form:
 * <code>library.package.adjustment</code>. The <code>package</code>
 * component can consist of multiple words joined with a period, but the
 * <code>library</code> will always be the first word before the first
 * period and the <code>adjustment</code> always the last word after the
 * final period. This is mainly only important for organizing the runtime
 * adjustment editing interface.
 */
public class RuntimeAdjust
{
    /**
     * Creates a Swing user interface that can be used to adjust all
     * registered runtime adjustments.
     */
    public static JComponent createAdjustEditor ()
    {
        VGroupLayout layout = new VGroupLayout(
            VGroupLayout.NONE, VGroupLayout.STRETCH,
            3, VGroupLayout.TOP);
        layout.setOffAxisJustification(VGroupLayout.LEFT);

        JTabbedPane editor = new JTabbedPane();
        Font font = editor.getFont();
        Font smaller = font.deriveFont(font.getSize()-1f);

        String library = null;
        ScrollablePanel lpanel = null;
        String pkgname = null;
        CollapsiblePanel pkgpanel = null;

        int acount = _adjusts.size();
        for (int ii = 0; ii < acount; ii++) {
            Adjust adjust = (Adjust)_adjusts.get(ii);

            // create a new library label if necessary
            if (!adjust.getLibrary().equals(library)) {
                library = adjust.getLibrary();
                pkgname = null;
                lpanel = new ScrollablePanel(layout);
                lpanel.setTracksViewportWidth(true);
                lpanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
                editor.addTab(library, lpanel);
            }

            // create a new package panel if necessary
            if (!adjust.getPackage().equals(pkgname)) {
                pkgname = adjust.getPackage();
                pkgpanel = new CollapsiblePanel();
                JCheckBox pkgcheck = new JCheckBox(pkgname);
                pkgcheck.setSelected(true);
                pkgpanel.setTrigger(pkgcheck, null, null);
                pkgpanel.setTriggerContainer(pkgcheck);
                pkgpanel.getContent().setLayout(layout);
                pkgpanel.setCollapsed(false);
                lpanel.add(pkgpanel);
            }

            // add an entry for this adjustment
            pkgpanel.getContent().add(new JSeparator());
            JPanel aeditor = adjust.getEditor();
            aeditor.setFont(smaller);
            pkgpanel.getContent().add(aeditor);
        }

        return editor;
    }

    /** Provides runtime adjustable boolean variables. */
    public static class BooleanAdjust extends Adjust
        implements ActionListener
    {
        public BooleanAdjust (String descrip, String name,
                              Config config, boolean defval)
        {
            super(descrip, name, config);
            _value = _config.getValue(_name, defval);
        }

        public final boolean getValue ()
        {
            return _value;
        }

        public void setValue (boolean value)
        {
            _config.setValue(_name, value);
        }

        protected void populateEditor (JPanel editor)
        {
            editor.add(_valbox = new JCheckBox(), GroupLayout.FIXED);
            _valbox.setSelected(getValue());
            _valbox.addActionListener(this);
        }

        public void actionPerformed (ActionEvent e)
        {
            setValue(_valbox.isSelected());
        }

        public void propertyChange (PropertyChangeEvent evt)
        {
            _value = ((Boolean)evt.getNewValue()).booleanValue();
            adjusted(_value);
            if (_valbox != null) {
                _valbox.setSelected(_value);
            }
        }

        protected void adjusted (boolean newValue)
        {
//             Log.info(_name + " => " + newValue);
        }

        protected boolean _value;
        protected JCheckBox _valbox;
    }

    /** Provides runtime adjustable integer variables. */
    public static class IntAdjust extends Adjust
        implements ActionListener
    {
        public IntAdjust (String descrip, String name,
                          Config config, int defval)
        {
            super(descrip, name, config);
            _value = _config.getValue(_name, defval);
        }

        public final int getValue ()
        {
            return _value;
        }

        public void setValue (int value)
        {
            _config.setValue(_name, value);
        }

        protected void populateEditor (JPanel editor)
        {
            editor.add(_valbox = new JTextField(), GroupLayout.FIXED);
            _valbox.addActionListener(this);
            _valbox.setText("" + getValue());
        }

        public void actionPerformed (ActionEvent e)
        {
            try {
                setValue(Integer.parseInt(_valbox.getText()));
            } catch (NumberFormatException nfe) {
                _valbox.setText("" + getValue());
            }
        }

        public void propertyChange (PropertyChangeEvent evt)
        {
            _value = ((Integer)evt.getNewValue()).intValue();
            adjusted(_value, ((Integer)evt.getOldValue()).intValue());
            if (_valbox != null) {
                _valbox.setText("" + _value);
            }
        }

        protected void adjusted (int newValue, int oldValue)
        {
//             Log.info(_name + " => " + newValue);
        }

        protected int _value;
        protected JTextField _valbox;
    }

    /** Provides runtime adjustable enumerated variables. */
    public static class EnumAdjust extends Adjust
        implements ActionListener
    {
        public EnumAdjust (String descrip, String name,
                           Config config, String[] values, String defval)
        {
            super(descrip, name, config);
            _values = values;
            _value = _config.getValue(_name, defval);
        }

        public final String getValue ()
        {
            return _value;
        }

        public void setValue (String value)
        {
            if (!ListUtil.containsEqual(_values, value)) {
                Log.warning("Refusing invalid adjustment [name=" + _name +
                            ", values=" + StringUtil.toString(_values) +
                            ", value=" + value + "].");
            } else {
                _config.setValue(_name, value);
            }
        }

        protected void populateEditor (JPanel editor)
        {
            editor.add(_valbox = new JComboBox(_values), GroupLayout.FIXED);
            _valbox.addActionListener(this);
            _valbox.setSelectedItem(getValue());
        }

        public void actionPerformed (ActionEvent e)
        {
            setValue((String)_valbox.getSelectedItem());
        }

        public void propertyChange (PropertyChangeEvent evt)
        {
            _value = (String)evt.getNewValue();
            adjusted(_value, (String)evt.getOldValue());
            if (_valbox != null) {
                _valbox.setSelectedItem(_value);
            }
        }

        protected void adjusted (String newValue, String oldValue)
        {
//             Log.info(_name + " => " + newValue);
        }

        protected String _value;
        protected String[] _values;
        protected JComboBox _valbox;
    }

    /** Provides the ability to click a button and fire an action. */
    public static abstract class Action extends Adjust
        implements ActionListener, Runnable
    {
        public Action (String descrip, String name)
        {
            super(descrip, name, null);
        }

        protected void populateEditor (JPanel editor)
        {
            JButton actbut = new JButton("Go");
            editor.add(actbut, GroupLayout.FIXED);
            actbut.addActionListener(this);
        }

        public void actionPerformed (ActionEvent e)
        {
            run();
        }

        public void propertyChange (PropertyChangeEvent evt)
        {
            // not needed
        }
    }

    /** Base class for type-specific adjustments. */
    protected abstract static class Adjust
        implements PropertyChangeListener, Comparable
    {
        public Adjust (String descrip, String name, Config config)
        {
            _name = name;
            _descrip = descrip;
            _config = config;
            if (_config != null) {
                _config.addPropertyChangeListener(_name, this);
            }

            // validate the structure of the name
            int fdidx = _name.indexOf("."), ldidx = _name.lastIndexOf(".");
            if (fdidx == -1 || ldidx == -1) {
                Log.warning("Invalid adjustment name '" + _name +
                            "', must be of the form " +
                            "'library.package.adjustment'.");
                return;
            }

            // make sure there isn't another with the same name
            int idx = _adjusts.binarySearch(this);
            if (idx >= 0) {
                Log.warning("Error: duplicate adjust registration " +
                            "[new=" + this +
                            ", old=" + _adjusts.get(idx) + "].");
                return;
            }

            _adjusts.insertSorted(this); // keep 'em sorted
        }

        public boolean equals (Object other)
        {
            return _name.equals(((Adjust)other)._name);
        }

        public int compareTo (Object other)
        {
            return _name.compareTo(((Adjust)other)._name);
        }

        public String getName ()
        {
            return _name;
        }

        public String getDescription ()
        {
            return _descrip;
        }

        public String getLibrary ()
        {
            return _name.substring(0, _name.indexOf("."));
        }

        public String getPackage ()
        {
            return _name.substring(_name.indexOf(".")+1,
                                   _name.lastIndexOf("."));
        }

        public String getAdjustment ()
        {
            return _name.substring(_name.lastIndexOf(".")+1);
        }

        public JPanel getEditor ()
        {
            if (_editor == null) {
                _editor = GroupLayout.makeHBox(GroupLayout.STRETCH);
                _editor.add(new MultiLineLabel(_descrip, MultiLineLabel.LEFT,
                                               MultiLineLabel.VERTICAL, 25));
                populateEditor(_editor);
            }
            return _editor;
        }

        protected abstract void populateEditor (JPanel editor);

        public String toString ()
        {
            return StringUtil.shortClassName(this) +
                "[name=" + _name + ", desc=" + _descrip + "]";
        }

        protected String _name, _descrip;
        protected Config _config;
        protected JPanel _editor;
    }

    protected static SortableArrayList _adjusts = new SortableArrayList();
}
