//
// $Id: Controller.java,v 1.1 2001/08/09 01:08:50 mdb Exp $

package com.threerings.yohoho.client;

import java.awt.event.ActionEvent;

/**
 * The controller class provides a basis for the separation of user
 * interface code into display code and control code. The display code
 * lives in a panel class and the control code lives in the controller
 * class. All of the primary user interfaces in the Yohoho client are
 * separated thus.
 *
 * <p> The controller philosophy is thus: The panel class (and its UI
 * components) convert basic user interface actions into higher level
 * actions that more cleanly encapsulate the action desired by the user
 * and they pass those actions on to their controller. The controller then
 * performs abstract processing based on the users desires and the
 * changing state of the application and calls back to the panel to affect
 * changes to the display.
 */
public abstract class Controller
{
    /**
     * Instructs this controller to process this action event. When an
     * action is posted by a user interface element, it will be posted to
     * the controller in closest scope for that element. If that
     * controller handles the event, it should return true from this
     * method to indicate that processing should stop. If it cannot handle
     * the event, it can return false to indicate that the event should be
     * propagated to the next controller up the chain.
     *
     * <p> This method will be called on the AWT thread, so the controller
     * can safely manipulate user interface components while handling an
     * action. However, this means that action handling cannot block and
     * should not take an undue amount of time. If the controller needs to
     * perform complicated, lengthy processing it should do so with a
     * separate thread, for example via {@link
     * com.samskivert.swing.util.TaskMaster}.
     *
     * @param action The action to be processed.
     *
     * @return true if the action was processed, false if it should be
     * propagated up to the next controller in scope.
     */
    public abstract boolean handleAction (ActionEvent action);
}
