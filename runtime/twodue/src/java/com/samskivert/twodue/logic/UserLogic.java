//
// $Id: UserLogic.java,v 1.2 2003/12/10 20:33:42 mdb Exp $

package com.samskivert.twodue.logic;

import com.samskivert.servlet.user.User;
import com.samskivert.velocity.Application;
import com.samskivert.velocity.InvocationContext;
import com.samskivert.velocity.Logic;

import com.samskivert.twodue.TwoDueApp;

/**
 * A base logic class for pages that require an authenticated user.
 */
public abstract class UserLogic implements Logic
{
    /**
     * Logic classes should implement this method to perform their normal
     * duties.
     *
     * @param ctx the context in which the request is being invoked.
     * @param app the web application.
     * @param user the user record for the authenticated user.
     */
    public abstract void invoke (
        InvocationContext ctx, TwoDueApp app, User user)
        throws Exception;

    // documentation inherited from interface
    public void invoke (Application app, InvocationContext ctx)
        throws Exception
    {
        TwoDueApp tdapp = (TwoDueApp)app;
	User user = tdapp.getUserManager().requireUser(ctx.getRequest());
        ctx.put("username", user.username);
        invoke(ctx, tdapp, user);
    }
}
