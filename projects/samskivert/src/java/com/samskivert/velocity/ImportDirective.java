//
// $Id: ImportDirective.java,v 1.1 2001/11/06 04:49:32 mdb Exp $
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

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.resource.Resource;

import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.samskivert.servlet.SiteIdentifier;
import com.samskivert.util.StringUtil;

/**
 * Pluggable directive that handles the #import() statement in VTL. Import
 * is like #parse() except that it creates a {@link SiteResourceKey} based
 * on information from the current request when fetching the imported
 * template.
 */
public class ImportDirective extends Directive
{
    /**
     * Returns name of this directive.
     */
    public String getName ()
    {
        return "import";
    }        
    
    /**
     * Return type of this directive.
     */
    public int getType ()
    {
        return LINE;
    }        
    
    /**
     * Loads and renders the imported template.
     */
    public boolean render (
        InternalContextAdapter context, Writer writer, Node node)
        throws IOException, ResourceNotFoundException, ParseErrorException,
               MethodInvocationException
    {
        // make sure an argument was supplied to the directive
        if (node.jjtGetChild(0) == null) {
            rsvc.error("#import() error :  null argument");
            return false;
        }

        // make sure that argument has a value
        Object value = node.jjtGetChild(0).value(context);
        if (value == null) {
            rsvc.error("#import() error :  null argument");
            return false;
        }

        // obtain the path to the desired template
        String path = value.toString();

        // make sure we haven't exceeded the configured parse depth
        Object[] templateStack = context.getTemplateNameStack();
        int maxlen = rsvc.getInt(
            RuntimeConstants.PARSE_DIRECTIVE_MAXDEPTH, 20);
        if (templateStack.length >= maxlen) {
            rsvc.error("Max recursion depth reached (" + maxlen + "). "  +
                       "File stack: " +
                       StringUtil.toString(templateStack) + ".");
            return false;
        }

        // inherit the current encoding if there is a current template,
        // otherwise use the configured encoding
        String encoding = null;
        Resource current = context.getCurrentResource();
        if (current != null) {
            encoding = current.getEncoding();
        } else {
            encoding = (String)
                rsvc.getProperty(RuntimeConstants.INPUT_ENCODING);
        }

        // construct the template key based on the desired path and the
        // site information in the current context. the siteid was shoved
        // into the context by the dispatcher servlet. this is a hack, i
        // know, but i couldn't convince the velocity peeps that we should
        // have access to a request context in places like this
        Object siteIdVal = context.get("__siteid__");
        int siteId = SiteIdentifier.DEFAULT_SITE_ID;
        try {
            siteId = ((Integer)siteIdVal).intValue();
        } catch (Exception e) {
            rsvc.error("#import() error: No siteId information in context.");
        }
        Object tkey = new SiteResourceKey(siteId, path);

        // locate the requested template
        Template t = null;
        try {
            t = rsvc.getTemplate(tkey, encoding);   

        } catch (ResourceNotFoundException rnfe) {
            rsvc.error("#import(): cannot find template '" + tkey +
                       "', called from template " +
                       context.getCurrentTemplateName() +
                       " at (" + getLine() + ", " + getColumn() + ")");       	
            throw rnfe;

        } catch (ParseErrorException pee) {
            rsvc.error("#import(): syntax error in #import()-ed template '" +
                       tkey + "', called from template " +
                       context.getCurrentTemplateName() +
                       " at (" + getLine() + ", " + getColumn() + ")");    
            throw pee;

        } catch (Exception e) {	
            rsvc.error("#import(): Error [path=" + tkey +
                       ", error=" + e + "].");
            return false;
        }

        // finally render the template
        try {
            context.pushCurrentTemplateName(path);
            ((SimpleNode)t.getData()).render(context, writer);

        } catch (Exception e) {        
            // we want to pass method invocation exceptions through
            if (e instanceof MethodInvocationException) {
                throw (MethodInvocationException)e;
            }
            rsvc.error("Exception rendering #import(" + path + "): " + e);
            return false;

        } finally {
            context.popCurrentTemplateName();
        }

        return true;
    }
}
