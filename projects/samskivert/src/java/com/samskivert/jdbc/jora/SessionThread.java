//-< SessionThread.java >--------------------------------------------*--------*
// JORA                       Version 2.0        (c) 1998  GARRET    *     ?  *
// (Java Object Relational Adapter)                                  *   /\|  *
//                                                                   *  /  \  *
//                          Created:     18-Jun-99    K.A. Knizhnik  * / [] \ *
//                          Last update: 18-Jun-99    K.A. Knizhnik  * GARRET *
//-------------------------------------------------------------------*--------*
// Thread associated with database session
//-------------------------------------------------------------------*--------*

package com.samskivert.jdbc.jora;

/**
 *  Class representing thread associated with users database session. 
 *  If there is single database session opened by application, that it is 
 *  possible to associate it with Table object statically. Otherwise it is 
 *  needed either to explicitly specify session object in each insert,select
 *  or update statement or associate session with thread by means of 
 *  SessionThread class.
 */
public class SessionThread extends Thread {
    Session session;

    /**
     * Allocates a new <code>SessionThread</code> object and associate it with 
     * the specified session. This constructor has the same effect as 
     * <code>SessionThread(session, null, null,</code>
     * <i>gname</i><code>)</code>, where <b><i>gname</i></b> is 
     * a newly generated name. Automatically generated names are of the 
     * form <code>"Thread-"+</code><i>n</i>, where <i>n</i> is an integer. 
     * <p>
     * Threads created this way must have overridden their
     * <code>run()</code> method to actually do anything.  
     *
     * @param   session   user database session associated with this thread
     * @see     java.lang.Thread#Thread(java.lang.ThreadGroup, 
     *          java.lang.Runnable, java.lang.String)
     */ 
    public SessionThread(Session session) { 
	this.session = session;
    }
    
    /**
     * Allocates a new <code>SessionThread</code> object and associate it with 
     * the specified session. This constructor has the same effect as 
     * <code>SessionThread(session, target, null,</code>
     * <i>gname</i><code>)</code>, where <b><i>gname</i></b> is 
     * a newly generated name. Automatically generated names are of the 
     * form <code>"Thread-"+</code><i>n</i>, where <i>n</i> is an integer. 
     * <p>
     * Threads created this way must have overridden their
     * <code>run()</code> method to actually do anything.  
     *
     * @param   session   user database session associated with this thread
     * @param   target   the object whose <code>run</code> method is called.
     * @see     java.lang.Thread#Thread(java.lang.ThreadGroup, 
     *          java.lang.Runnable, java.lang.String)
     */
    public SessionThread(Session session, Runnable target) {
	super(target);
	this.session = session;
    }

    /**
     * Allocates a new <code>SessionThread</code> object and associate it with 
     * the specified session. This constructor has the same effect as 
     * <code>SessionThread(session, target, group,</code>
     * <i>gname</i><code>)</code>, where <b><i>gname</i></b> is 
     * a newly generated name. Automatically generated names are of the 
     * form <code>"Thread-"+</code><i>n</i>, where <i>n</i> is an integer. 
     * <p>
     * Threads created this way must have overridden their
     * <code>run()</code> method to actually do anything.  
     *
     * @param   session  user database session associated with this thread
     * @param   group    the thread group.
     * @param   target   the object whose <code>run</code> method is called.
     * @see     java.lang.Thread#Thread(java.lang.ThreadGroup, 
     *          java.lang.Runnable, java.lang.String)
     */
    public SessionThread(Session session, ThreadGroup group, Runnable target) {
	super(target);
	this.session = session;
    }

    /**
     * Allocates a new <code>SessionThread</code> object. This constructor has 
     * the same effect as <code>SessionThread(session, null, null, name)</code>. 
     *
     * @param   session  user database session associated with this thread
     * @param   name   the name of the new thread.
     * @see     java.lang.Thread#Thread(java.lang.ThreadGroup, 
     *          java.lang.Runnable, java.lang.String)
     */
    public SessionThread(Session session, String name) {
	super(name);
	this.session = session;
    }

    /**
     * Allocates a new <code>SessionThread</code> object. This constructor has 
     * the same effect as <code>SessionThread(session, group, null, name)</code>.
     *
     * @param   session  user database session associated with this thread
     * @param   group    the thread group.
     * @param   name     the name of the new thread.
     * @see     java.lang.Thread#Thread(java.lang.ThreadGroup, 
     *          java.lang.Runnable, java.lang.String)
     */
    public SessionThread(Session session, ThreadGroup group, String name) {
	super(group, name);
	this.session = session;
    }

    /**
     * Allocates a new <code>SessionThread</code> object. This constructor has 
     * the same effect as <code>SessionThread(session, null, target, name)</code>     *
     * @param   session  user database session associated with this thread
     * @param   target   the object whose <code>run</code> method is called.
     * @param   name     the name of the new thread.
     * @see     java.lang.Thread#Thread(java.lang.ThreadGroup, 
     *          java.lang.Runnable, java.lang.String)
     */
    public SessionThread(Session session, Runnable target, String name) {
	super(target, name);
	this.session = session;
    }

    /**
     * Allocates a new <code>SessionThread</code> object so that it has 
     * <code>target</code> as its run object, has the specified 
     * <code>name</code> as its name, and belongs to the thread group 
     * referred to by <code>group</code>.
     * <p>
     * @param   session  user database session associated with this thread
     * @param   group    the thread group.
     * @param   target   the object whose <code>run</code> method is called.
     * @param   name     the name of the new thread.
     * @see     java.lang.Thread#Thread(java.lang.ThreadGroup, 
     *          java.lang.Runnable, java.lang.String)
     */
    public SessionThread(Session session, ThreadGroup group, Runnable target, 
		  String name) 
    {
	super(group, target, name);
	this.session = session;
    }
}
