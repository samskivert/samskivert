//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.xml;

import org.apache.commons.digester.Rule;

import com.samskivert.util.StringUtil;

/**
 * Parses the bodies of elements and sets the value on the top object on
 * the stack using a special parser/setter interface.
 */
public abstract class CallMethodSpecialRule extends Rule
{
    @Override
    public void body (String namespace, String name, String bodyText)
        throws Exception
    {
        _bodyText = bodyText.trim();
    }

    @Override
    public void end (String namespace, String name)
        throws Exception
    {
        Object top = digester.peek();

        if (digester.getLogger().isDebugEnabled()) {
            digester.getLogger().debug(
                "CallSpecial " + StringUtil.shortClassName(this) +
                ".parseAndSet(" + StringUtil.shortClassName(top) + ")");
        }

        parseAndSet(_bodyText, top);
    }

    public abstract void parseAndSet (String bodyText, Object target)
        throws Exception;

    protected String _bodyText;
}
