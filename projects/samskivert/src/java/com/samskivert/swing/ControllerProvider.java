//
// $Id: ControllerProvider.java,v 1.1 2001/08/09 01:08:50 mdb Exp $

package com.threerings.yohoho.client;

/**
 * The controller provider interface is implemented by user interface
 * elements that have an associated controller. The hierarchy of the user
 * interface elements defines the hierarchy of controllers and at any
 * point in the UI element hierarchy, an element can implement controller
 * provider and provide a controller that will process actions received at
 * that scope or below.
 */
public interface ControllerProvider
{
    /**
     * Returns the controller to be used at the scope of the UI element
     * that implements controller provider.
     */
    public Controller getController ();
}
