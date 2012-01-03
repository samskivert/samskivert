//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
 * Pluggable directive that handles the #import() statement in VTL. Import is like #parse() except
 * that it creates a compound key (<code>siteId:template_path</code>) based on information from the
 * current request when fetching the imported template.
 */
public class ImportDirective extends Directive
{
    /**
     * Returns name of this directive.
     */
    @Override
    public String getName ()
    {
        return "import";
    }

    /**
     * Return type of this directive.
     */
    @Override
    public int getType ()
    {
        return LINE;
    }

    /**
     * Loads and renders the imported template.
     */
    @Override
    public boolean render (
        InternalContextAdapter context, Writer writer, Node node)
        throws IOException, ResourceNotFoundException, ParseErrorException,
               MethodInvocationException
    {
        // make sure an argument was supplied to the directive
        if (node.jjtGetChild(0) == null) {
            rsvc.getLog().error("#import() error :  null argument");
            return false;
        }

        // make sure that argument has a value
        Object value = node.jjtGetChild(0).value(context);
        if (value == null) {
            rsvc.getLog().error("#import() error :  null argument");
            return false;
        }

        // obtain the path to the desired template
        String path = value.toString();

        // make sure we haven't exceeded the configured parse depth
        Object[] templateStack = context.getTemplateNameStack();
        int maxlen = rsvc.getInt(
            RuntimeConstants.PARSE_DIRECTIVE_MAXDEPTH, 20);
        if (templateStack.length >= maxlen) {
            rsvc.getLog().error("Max recursion depth reached (" + maxlen + "). "  +
                                "File stack: " + StringUtil.toString(templateStack) + ".");
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

        // adjust the template path with the site information in the current
        // context if available. the siteid is shoved into the context by the
        // dispatcher servlet when we're using the site resource loader. this
        // is a hack, i know, but i couldn't convince the velocity peeps that
        // we should have access to a request context in places like this
        Object siteIdVal = context.get("__siteid__");
        if (siteIdVal != null) {
            int siteId = SiteIdentifier.DEFAULT_SITE_ID;
            try {
                siteId = ((Integer)siteIdVal).intValue();
            } catch (Exception e) {
                rsvc.getLog().error("#import() error: No siteId information in context.");
            }
            path = siteId + ":" + path;
        }

        // locate the requested template
        Template t = null;
        try {
            t = rsvc.getTemplate(path, encoding);

        } catch (ResourceNotFoundException rnfe) {
            rsvc.getLog().error("#import(): cannot find template '" + path +
                                "', called from template " + context.getCurrentTemplateName() +
                                " at (" + getLine() + ", " + getColumn() + ")");
            throw rnfe;

        } catch (ParseErrorException pee) {
            rsvc.getLog().error("#import(): syntax error in #import()-ed template '" + path +
                                "', called from template " + context.getCurrentTemplateName() +
                                " at (" + getLine() + ", " + getColumn() + ")");
            throw pee;

        } catch (Exception e) {
            rsvc.getLog().error("#import(): Error [path=" + path + ", error=" + e + "].");
            return false;
        }

        // finally render the template
        try {
            context.pushCurrentTemplateName(path);
            ((SimpleNode)t.getData()).render(context, writer);

        } catch (Throwable th) {
            rsvc.getLog().error("Exception rendering #import(" + path + "): " + th);
            // we want to pass method invocation exceptions through
            if (th instanceof MethodInvocationException) {
                throw (MethodInvocationException)th;
            }
            return false;

        } finally {
            context.popCurrentTemplateName();
        }

        return true;
    }
}
