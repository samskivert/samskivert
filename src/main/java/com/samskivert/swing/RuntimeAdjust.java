//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Scrollable;

import com.samskivert.util.ComparableArrayList;
import com.samskivert.util.PrefsConfig;
import com.samskivert.util.DebugChords;
import com.samskivert.util.ListUtil;
import com.samskivert.util.StringUtil;

import static com.samskivert.swing.Log.log;

/**
 * Provides a service where named variables can be registered as adjustable by the developer at
 * runtime. Generally a special key is configured (via {@link DebugChords}) to pop up the
 * adjustable variables interface which can then be used to toggle booleans, change integer values
 * and generally adjust debugging and tuning parameters. This is not meant to be an interface for
 * end-user configurable parameters, but to allow the developer to tweak runtime parameters more
 * easily.
 *
 * <p> Adjustments are bound to a {@link PrefsConfig} property which can be changed through the
 * config interface or through the bound runtime adjust.
 *
 * <p> <em>Note:</em> adjusts are meant to be arranged in a two level hierarchy. An adjust's name,
 * therefore, should be of the form: <code>library.package.adjustment</code>. The
 * <code>package</code> component can consist of multiple words joined with a period, but the
 * <code>library</code> will always be the first word before the first period and the
 * <code>adjustment</code> always the last word after the final period. This is mainly only
 * important for organizing the runtime adjustment editing interface.
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

        JTabbedPane editor = new EditorPane();
        Font font = editor.getFont();
        Font smaller = font.deriveFont(font.getSize()-1f);

        String library = null;
        JPanel lpanel = null;
        String pkgname = null;
        CollapsiblePanel pkgpanel = null;

        int acount = _adjusts.size();
        for (int ii = 0; ii < acount; ii++) {
            Adjust adjust = _adjusts.get(ii);

            // create a new library label if necessary
            if (!adjust.getLibrary().equals(library)) {
                library = adjust.getLibrary();
                pkgname = null;
                lpanel = new JPanel(layout);
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

    /** Used to make our editor scroll sanely. */
    protected static class EditorPane extends JTabbedPane
        implements Scrollable
    {
        public Dimension getPreferredScrollableViewportSize () {
            return getPreferredSize();
        }

        public int getScrollableUnitIncrement (
            Rectangle visibleRect, int orientation, int direction) {
            return 50;
        }

        public int getScrollableBlockIncrement (
            Rectangle visibleRect, int orientation, int direction) {
            return 200;
        }

        public boolean getScrollableTracksViewportWidth () {
            return true;
        }

        public boolean getScrollableTracksViewportHeight () {
            return false;
        }
    }

    /** Provides runtime adjustable boolean variables. */
    public static class BooleanAdjust extends Adjust
        implements ActionListener
    {
        public BooleanAdjust (String descrip, String name, PrefsConfig config, boolean defval)
        {
            super(descrip, name, config);
            _defval = defval;
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

        @Override protected void populateEditor (JPanel editor)
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
            _value = toValue(evt.getNewValue());
            adjusted(_value);
            if (_valbox != null) {
                _valbox.setSelected(_value);
            }
        }

        protected boolean toValue (Object o)
        {
            if (o instanceof Boolean) {
                return ((Boolean)o).booleanValue();
            }
            if (o instanceof String) {
                // Oh my god: this is how Config parses booleans
                return !((String)o).equalsIgnoreCase("false");
                // What you'd expect: return Boolean.parseBoolean((String)o); // never throws
            }
            return _defval;
        }

        protected void adjusted (boolean newValue)
        {
//             Log.info(_name + " => " + newValue);
        }

        protected final boolean _defval;
        protected boolean _value;
        protected JCheckBox _valbox;
    }

    /** Provides runtime adjustable integer variables. */
    public static class IntAdjust extends TextFieldAdjust
    {
        public IntAdjust (String descrip, String name, PrefsConfig config, int defval)
        {
            super(descrip, name, config);
            _defval = defval;
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

        @Override protected void populateEditor (JPanel editor)
        {
            super.populateEditor(editor);
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
            _value = toValue(evt.getNewValue());
            adjusted(_value, toValue(evt.getOldValue()));
            if (_valbox != null) {
                _valbox.setText("" + _value);
            }
        }

        protected int toValue (Object o)
        {
            if (o instanceof Integer) {
                return ((Integer)o).intValue();
            }
            if (o instanceof String) {
                try {
                    return Integer.parseInt((String)o);
                } catch (NumberFormatException nfe) {
                    // fall through
                }
            }
            return _defval;
        }

        protected void adjusted (int newValue, int oldValue)
        {
//             Log.info(_name + " => " + newValue);
        }

        protected final int _defval;
        protected int _value;
    }

    /** Provides runtime adjustable enumerated variables. */
    public static class EnumAdjust extends Adjust
        implements ActionListener
    {
        public EnumAdjust (String descrip, String name, PrefsConfig config,
                           String[] values, String defval)
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
            if (!ListUtil.contains(_values, value)) {
                log.warning("Refusing invalid adjustment", "name", _name, "values", _values,
                            "value", value);
            } else {
                _config.setValue(_name, value);
            }
        }

        @Override protected void populateEditor (JPanel editor)
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

    /** Provides runtime adjustable file path variables. */
    public static class FileAdjust extends Adjust
        implements ActionListener
    {
        public FileAdjust (String descrip, String name, PrefsConfig config,
                           boolean directoriesOnly, String defval)
        {
            super(descrip, name, config);
            _value = _config.getValue(_name, defval);
            _directories = directoriesOnly;
            _default = defval;
        }

        public final String getValue ()
        {
            return _value;
        }

        public void setValue (String value)
        {
            _config.setValue(_name, value);
        }

        @Override protected void populateEditor (JPanel editor)
        {
            // set up the label
            JPanel p = GroupLayout.makeVBox();
            p.add(_display = new JLabel());
            redisplay();

            JPanel buts = GroupLayout.makeHBox();
            JButton set = new JButton("set");
            set.setActionCommand("set");
            set.addActionListener(this);
            buts.add(set);
            JButton def = new JButton("default");
            def.setActionCommand("default");
            def.addActionListener(this);
            buts.add(def);
            p.add(buts);

            editor.add(p, GroupLayout.FIXED);
        }

        public void actionPerformed (ActionEvent e)
        {
            if (e.getActionCommand().equals("default")) {
                setValue(_default);
                return;
            }

            // else
            File f = new File(_value);
            JFileChooser chooser = f.exists() ? new JFileChooser(f)
                                              : new JFileChooser();
            // set it up like we like
            if (_directories) {
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }
            chooser.setSelectedFile(f);
            int result = chooser.showDialog(_editor, "Select");
            if (JFileChooser.APPROVE_OPTION == result) {
                f = chooser.getSelectedFile();
                setValue(f.getPath());
            }
        }

        public void propertyChange (PropertyChangeEvent evt)
        {
            _value = (String)evt.getNewValue();
            adjusted(_value, (String)evt.getOldValue());
            redisplay();
        }

        protected void redisplay ()
        {
            if (_display != null) {
                _display.setText(StringUtil.isBlank(_value) ? "(unset)" : _value);
            }
        }

        protected void adjusted (String newValue, String oldValue)
        {
//             Log.info(_name + " => " + newValue);
        }

        protected String _value, _default;
        protected boolean _directories;
        protected JLabel _display;
    }

    /** Provides the ability to click a button and fire an action. */
    public static abstract class Action extends Adjust
        implements ActionListener, Runnable
    {
        public Action (String descrip, String name)
        {
            super(descrip, name, null);
        }

        @Override protected void populateEditor (JPanel editor)
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

    /**
     * Base class for adjusts which use a text field for entry.
     */
    protected abstract static class TextFieldAdjust extends Adjust
        implements ActionListener, FocusListener
    {
        public TextFieldAdjust (String descrip, String name, PrefsConfig config)
        {
            super(descrip, name, config);
        }

        @Override protected void populateEditor (JPanel editor)
        {
            editor.add(_valbox = new JTextField(6), GroupLayout.FIXED);
            _valbox.addFocusListener(this);
            _valbox.addActionListener(this);
        }

        // documentation inherited from interface FocusListener
        public void focusGained (FocusEvent e)
        {
            // nothing
        }

        // documentation inherited from interface FocusListener
        public void focusLost (FocusEvent e)
        {
            actionPerformed(new ActionEvent(_valbox, 0, "focusLost"));
        }

        /** The textbox that holds the value. */
        protected JTextField _valbox;
    }

    /** Base class for type-specific adjustments. */
    protected abstract static class Adjust
        implements PropertyChangeListener, Comparable<Adjust>
    {
        public Adjust (String descrip, String name, PrefsConfig config)
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
                log.warning("Invalid adjustment name '" + _name + "', must be of the form " +
                            "'library.package.adjustment'.");
                return;
            }

            // make sure there isn't another with the same name
            int idx = _adjusts.binarySearch(this);
            if (idx >= 0) {
                log.warning("Error: duplicate adjust registration", "new", this,
                            "old", _adjusts.get(idx));
                return;
            }

            _adjusts.insertSorted(this); // keep 'em sorted
        }

        @Override public boolean equals (Object other)
        {
            return (other instanceof Adjust) && _name.equals(((Adjust)other)._name);
        }

        @Override public int hashCode ()
        {
            return _name.hashCode();
        }

        // from Comparable<Adjust>
        public int compareTo (Adjust other)
        {
            return _name.compareTo(other._name);
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

        @Override public String toString ()
        {
            return StringUtil.shortClassName(this) +
                "[name=" + _name + ", desc=" + _descrip + "]";
        }

        protected String _name, _descrip;
        protected PrefsConfig _config;
        protected JPanel _editor;
    }

    protected static ComparableArrayList<Adjust> _adjusts = new ComparableArrayList<Adjust>();
}
