//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.xml;

import java.lang.reflect.Method;

import org.apache.commons.digester.Rule;

/**
 * Like the <code>SetNextRule</code> except that the object on the top of
 * the stack is validated before the set next method is called. If the
 * object is determined not to be valid, the set next method will not be
 * called (and the assumption is that the validator will have emitted some
 * useful error message indicating to the user why the object was
 * invalid).
 */
public class ValidatedSetNextRule extends Rule
{
    /**
     * An implementor if this interface will be used to determine whether
     * the object on the top of the stack is valid before it is "set
     * next"ed. If the object is not valid, it will not be "set next"ed.
     */
    public static interface Validator
    {
        /**
         * Returns true if the object in question is valid (was properly
         * parsed), false if it is not.
         */
        public boolean isValid (Object target);
    }

    /**
     * Constructs a set method rule for the specified method.
     */
    public ValidatedSetNextRule (String methodName, Validator validator)
    {
        this(methodName, null, validator);
    }

    /**
     * Constructs a set method rule for the specified method with the
     * specified parameter type.
     */
    public ValidatedSetNextRule (
        String methodName, Class<?> paramType, Validator validator)
    {
        // keep this for later
        _methodName = methodName;
        _paramType = paramType;
        _validator = validator;
    }

    @Override
    public void end (String namespace, String name)
        throws Exception
    {
        // identify the objects to be used
        Object child = digester.peek(0);
        Object parent = digester.peek(1);

        // make sure the object in question is valid
        if (!_validator.isValid(child)) {
            return;
        }

        Class<?> pclass = parent.getClass();
        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug("Call " + pclass.getName() + "." +
                                       _methodName + " (" + child + ")");
        }

        // call the specified method
        Class<?>[] paramTypes = new Class<?>[1];
        paramTypes[0] = (_paramType == null) ? child.getClass() : _paramType;
        Method method = parent.getClass().getMethod(_methodName, paramTypes);
        method.invoke(parent, new Object[] { child });
    }

    /**
     * Render a printable version of this rule.
     */
    @Override
    public String toString ()
    {
        StringBuilder sb = new StringBuilder("ValidatedSetNextRule[");
        sb.append("methodName=");
        sb.append(_methodName);
        if (_paramType != null) {
            sb.append("paramType=");
            sb.append(_paramType.getName());
        }
        sb.append("]");
        return (sb.toString());
    }

    protected String _methodName;
    protected Class<?> _paramType;
    protected Validator _validator;
}
