//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.Arrays;

/**
 * Provides support for delaying an action until some set of conditions
 * have been satisfied. When those conditions are satisfied, the {@link
 * Runnable} associated with the multex is executed. This class is not
 * intended for use by multiple threads, and runs the action during the
 * final call to {@link #satisfied}.
 *
 * <p> When the final condition has been {@link #satisfied}, the action is
 * fired and the multex is reset.
 */
public class Multex
{
    /** A convenient constant for the first action. */
    public static final int CONDITION_ONE = 0;

    /** A convenient constant for the second action. */
    public static final int CONDITION_TWO = 1;

    /** A convenient constant for the third action. */
    public static final int CONDITION_THREE = 2;

    /** A convenient constant for the fourth action. */
    public static final int CONDITION_FOUR = 3;

    /** A convenient constant for the fifth action. */
    public static final int CONDITION_FIVE = 4;

    public Multex (Runnable action, int conditions)
    {
        _action = action;
        _conditions = new boolean[conditions];
    }

    /**
     * Indicates that the specified condition has been satisfied. If this
     * condition is the last remaining condition, the multex will be reset
     * and the action will be fired (in that order) before this method
     * call returns.
     */
    public void satisfied (int condition)
    {
        // sanity check
        if (condition < 0 || condition >= _conditions.length) {
            throw new IllegalArgumentException(
                "Invalid condition supplied: " + condition + ". " +
                "Maximum is " + (_conditions.length-1) + ".");
        }

        // note that this condition was satisfied
        _conditions[condition] = true;

        // check to see if unsatisfied conditions remain
        for (int ii = 0; ii < _conditions.length; ii++) {
            if (!_conditions[ii]) {
                return;
            }
        }

        // reset ourselves and fire the action
        reset();
        _action.run();
    }

    /**
     * Resets this multex, setting all conditions to unsatisfied.
     */
    public void reset ()
    {
        Arrays.fill(_conditions, false);
    }

    protected Runnable _action;
    protected boolean[] _conditions;
}
