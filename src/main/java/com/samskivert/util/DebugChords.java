//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import java.util.ArrayList;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.Tuple;

import static com.samskivert.util.UtilLog.log;

/**
 * Provides a mechanism for causing code to be invoked when a particular
 * key combination is pressed in a GUI application. The code invoked is
 * presumed to be global debugging code; this is not a mechanism for
 * implementing a client's normal user interface.
 */
public class DebugChords
{
    /**
     * Provides the mechanism by which code is provided to be run when a
     * user depresses a particular key combination.
     */
    public static interface Hook
    {
        /**
         * Called when a key combination to which this hook is bound is
         * invoked.
         */
        public void invoke ();
    }

    /**
     * Initializes the debug chords services and wires up the key event
     * listener that will be used to invoke the bound code.
     */
    public static void activate ()
    {
        // capture low-level keyboard events via the keyboard focus manager
        if (_dispatcher == null) {
            _dispatcher = new KeyEventDispatcher() {
                public boolean dispatchKeyEvent (KeyEvent e) {
                    return DebugChords.dispatchKeyEvent(e);
                }
            };

            KeyboardFocusManager keymgr =
                KeyboardFocusManager.getCurrentKeyboardFocusManager();
            keymgr.addKeyEventDispatcher(_dispatcher);
        }
    }

    /**
     * Registers the supplied debug hook to be invoked when the specified
     * key combination is depressed.
     *
     * @param modifierMask a mask with bits on for all modifiers that must
     * be present when the specified key code is received (e.g. {@link
     * KeyEvent#CTRL_DOWN_MASK}|{@link KeyEvent#ALT_DOWN_MASK}).
     * @param keyCode the code that identifies the normal key that must be
     * pressed to activate the hook (e.g. {@link KeyEvent#VK_E}).
     * @param hook the hook to be invoked when the specified key
     * combination is received.
     */
    public static void registerHook (int modifierMask, int keyCode, Hook hook)
    {
        // store the hooks mapped by key code
        ArrayList<Tuple<Integer,Hook>> list = _bindings.get(keyCode);
        if (list == null) {
            list = new ArrayList<Tuple<Integer,Hook>>();
            _bindings.put(keyCode, list);
        }

        // append the hook and modifier mask to the list
        list.add(new Tuple<Integer,Hook>(modifierMask, hook));
    }

    /**
     * Called when the {@link KeyboardFocusManager} has a key event for us
     * to scrutinize.  Returns whether we've handled the key event.
     */
    protected static boolean dispatchKeyEvent (KeyEvent e)
    {
        // ignore non-keypress events
        if (e.getID() != KeyEvent.KEY_PRESSED) {
            return false;
        }

        // bail here if we have no hooks registered for this key code
        ArrayList<Tuple<Integer,Hook>> list = _bindings.get(e.getKeyCode());
        if (list == null) {
            return false;
        }

        // scan the list of registered hooks checking for any that match
        // the depressed set of modifier keys
        boolean handled = false;
        int hcount = list.size();
        for (int ii = 0; ii < hcount; ii++) {
            Tuple<Integer,Hook> tup = list.get(ii);
            int mask = tup.left.intValue();
            if ((e.getModifiersEx() & mask) == mask) {
                try {
                    tup.right.invoke();
                    handled = true;
                } catch (Throwable t) {
                    log.warning("Hook failed", "event", e, "hook", tup.right, t);
                }
            }
        }

        return handled;
    }

    /** Intercepts key events and presents them to us for scrutiny. */
    protected static KeyEventDispatcher _dispatcher;

    /** A mapping from key binding to debug code. */
    protected static HashIntMap<ArrayList<Tuple<Integer,Hook>>> _bindings =
        new HashIntMap<ArrayList<Tuple<Integer,Hook>>>();
}
