//
// $Id: FormTool.java,v 1.1 2001/10/31 11:07:55 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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

package com.samskivert.velocity;

import javax.servlet.http.HttpServletRequest;

import com.samskivert.servlet.util.HTMLUtil;
import com.samskivert.servlet.util.ParameterUtil;
import com.samskivert.util.StringUtil;

/**
 * The form tool can be placed into an invocation context and used by the
 * template to create form elements which automatically inherit form
 * values which were provided to the page at request time.
 *
 * For example:
 *
 * <pre>
 * Please enter your name: $form.text("name", 40) $form.submit("submit")
 * </pre>
 *
 * If the servlet was invoked with a value for "name", it will be
 * automatically filled into the "name" form field when it is requested.
 */
public class FormTool
{
    /**
     * Constructs a form tool that will use the supplied HTTP servlet
     * request object to prefetch form values.
     */
    public FormTool (HttpServletRequest req)
    {
        _req = req;
    }

    /**
     * Creates a text input field with the specified name and no extra
     * arguments or default value.
     *
     * @see #text(String,String,String)
     */
    public String text (String name)
    {
        return text(name, "", "");
    }

    /**
     * Creates a text input field with the specified name and the
     * specified extra arguments with no default value.
     *
     * @see #text(String,String,String)
     */
    public String text (String name, String extra)
    {
        return text(name, extra, "");
    }

    /**
     * Creates a text input field with the specified name, extra arguments
     * and default value. For example, a call to <code>text("foo",
     * "size=\"5\" maxLength=\"8\"", "bar")</code> would result in an
     * input field looking like:
     *
     * <pre>
     * &lt;input type="text" name="foo" value="bar" size="5" maxLength="8"&gt;
     * </pre>
     *
     * Assuming the <code>foo</code> parameter had no pre-existing value.
     */
    public String text (String name, String extra, Object defaultValue)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("<input type=\"text\"");
        buf.append(" name=\"").append(name).append("\"");

        // fetch the form value, entify it and stick it in
        buf.append(" value=\"").append(getValue(name, defaultValue));
        buf.append("\"");

        // append any extra arguments the user may want (maxlength=25
        // size=10, for example)
        if (!StringUtil.blank(extra)) {
            buf.append(" ").append(extra);
        }

        // close the tag and be done with it
        buf.append(">");
        return buf.toString();
    }

    /**
     * Constructs a submit element with the specified parameter name and
     * the specified button text.
     */
    public String submit (String name, String text)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("<input type=\"submit\"");
        buf.append(" name=\"").append(name).append("\"");
        buf.append(" value=\"").append(text).append("\"");
        buf.append(">");
        return buf.toString();
    }

    /**
     * Constructs a hidden element with the specified parameter name where
     * the value is extracted from the appropriate request parameter.
     */
    public String hidden (String name)
    {
        return hidden(name, "");
    }

    /**
     * Constructs a hidden element with the specified parameter name where
     * the value is extracted from the appropriate request parameter
     * unless there is no value in which case the supplied default value
     * is used.
     */
    public String hidden (String name, Object defaultValue)
    {
        return fixedHidden(name, getValue(name, defaultValue));
    }

    /**
     * Constructs a fixed hidden element with the specified parameter name
     * and value. The contents of the parameter of the same name are
     * ignored when creating this element.
     */
    public String fixedHidden (String name, String value)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("<input type=\"hidden\"");
        buf.append(" name=\"").append(name).append("\"");
        buf.append(" value=\"").append(value).append("\"");
        buf.append(">");
        return buf.toString();
    }

    /**
     * Fetches the requested value from the servlet request and entifies
     * it appropriately.
     */
    protected String getValue (String name, Object defaultValue)
    {
        String value = ParameterUtil.getParameter(_req, name, true);
        if (StringUtil.blank(value)) {
            value = String.valueOf(defaultValue);
        }
        return HTMLUtil.entify(value);
    }

    /** A reference to the servlet request in use by this form tool. */
    protected HttpServletRequest _req;
}
