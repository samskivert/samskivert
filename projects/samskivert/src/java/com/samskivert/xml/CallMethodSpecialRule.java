//
// $Id: CallMethodSpecialRule.java,v 1.1 2001/11/17 03:46:48 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Walter Korman
// 
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.xml;

import org.xml.sax.Attributes;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;

/**
 * Parses the bodies of elements and sets the value on the top object on
 * the stack using a special parser/setter interface.
 */
public abstract class CallMethodSpecialRule extends Rule
{
    public static interface Helper
    {
        public void parseAndSet (String bodyText, Object target);
    }

    public CallMethodSpecialRule (Digester digester)
    {
        super(digester);
    }

    public void body (String bodyText)
        throws Exception
    {
        _bodyText = bodyText.trim();
    }

    public void end ()
        throws Exception
    {
	Object top = digester.peek();
        parseAndSet(_bodyText, top);
    }

    public abstract void parseAndSet (String bodyText, Object target);

    protected String _bodyText;
}
