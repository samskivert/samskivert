//
// $Id: MessageResolver.java,v 1.1 2001/10/31 09:44:22 mdb Exp $
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

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;

import org.apache.velocity.runtime.parser.node.ASTIdentifier;
import org.apache.velocity.runtime.parser.node.ASTMethod;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.ReferenceResolver;

import com.samskivert.Log;
import com.samskivert.servlet.MessageManager;

/**
 * Maps a set of appliation messages (translation strings) into the
 * context so that they are available to templates that wish to display
 * localized text.
 *
 * <p> If a message resolver is mapped into the context as
 * <code>i18n</code>, then it might be accessed as follows:
 *
 * <pre>
 * $i18n.main.intro($user.username)
 * </pre>
 *
 * where <code>main.intro</code> might be defined in the message resource
 * file as:
 *
 * <pre>
 * main.intro=Hello there {0}, welcome to our site!
 * </pre>
 *
 * @see MessageManager
 */
public class MessageResolver implements ReferenceResolver
{
    /**
     * Constructs a new message resolver which will use the supplied
     * message manager to obtain translated strings.
     */
    public MessageResolver (MessageManager msgmgr)
    {
        _msgmgr = msgmgr;
    }

    /**
     * Concatenates the remaining reference nodes into a single message
     * name and looks up the matching translation string. If the last node
     * is an {@link ASTMethod}, then the arguments from that are
     * substituted into the translated message.
     */
    public Object resolveReference (
        InternalContextAdapter ctx, Node[] nodes, int offset)
        throws MethodInvocationException
    {
        StringBuffer path = new StringBuffer();
        boolean bogus = false;
        int lastidx = nodes.length-1;
        Object result = this;

        // reconstruct the path to the property
        for (int i = offset; i < nodes.length; i++) {
            // separate components with dots
            if (path.length() > 0) {
                path.append(".");
            }

            // if the node is an ASTMethod, it dang well better be the
            // last thing in the chain or we've got a problem
            if (nodes[i] instanceof ASTMethod) {
                ASTMethod mnode = (ASTMethod)nodes[i];
                // make a note to freak out if this is anything but
                // the last component of the path. we'd freak out now
                // but we want to reconstruct the path so that we can
                // report it in the exception that's thrown
                if (i != lastidx) {
                    bogus = true;
                }
                path.append(mnode.getMethodName());

            } else if (nodes[i] instanceof ASTIdentifier) {
                ASTIdentifier inode = (ASTIdentifier)nodes[i];
                path.append(inode.getIdentifier());

            } else {
                // what is it man?
                bogus = true;
                path.append("?").append(nodes[i]);
            }
        }

        // do any pending freaking out
        if (bogus) {
            throw new MethodInvocationException(
                "Invalid message resource path.", null, path.toString());
        }

        // we need to get the invocation context from our internal context
        // adapter
        InvocationContext ictx = (InvocationContext)
            ctx.getInternalUserContext();

        // if the last component is a property method, we want to use
        // it's arguments when looking up the message
        if (nodes[lastidx] instanceof ASTMethod) {
            ASTMethod mnode = (ASTMethod)nodes[lastidx];
            // we may cache message formatters later, but for now just
            // use the static convenience function
            Object[] args = mnode.getParams(ctx);
            return _msgmgr.getMessage(ictx.getRequest(),
                                      path.toString(), args);

        } else {
            // otherwise just look up the path
            return _msgmgr.getMessage(ictx.getRequest(), path.toString());
        }
    }

    protected MessageManager _msgmgr;
}
