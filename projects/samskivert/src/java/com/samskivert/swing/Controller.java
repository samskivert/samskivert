//
// $Id: Controller.java,v 1.3 2001/08/09 01:37:26 mdb Exp $

package com.threerings.yohoho.client;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.threerings.yohoho.Log;

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
     * This action listener can be wired up to any action event generator
     * and it will take care of forwarding that event on to the controller
     * in scope for the component that generated the action event.
     */
    public static final ActionListener DISPATCHER = new ActionListener() {
        public void actionPerformed (ActionEvent event)
        {
            Controller.postAction(event);
        }
    };

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

    /**
     * Posts the specified action to the nearest controller in scope. The
     * controller search begins with the source component of the action
     * and traverses up the component tree looking for a controller to
     * handle the action. The controller location and action event
     * processing is guaranteed to take place on the AWT thread regardless
     * of what thread calls <code>postAction</code> and that processing
     * will not occur immediately but is instead appended to the AWT event
     * dispatch queue for processing.
     */
    public static void postAction (ActionEvent action)
    {
        // slip things onto the event queue for later
        EventQueue.invokeLater(new ActionInvoker(action));
    }

    /**
     * Like {@link #postAction(ActionEvent)} except that it constructs the
     * action event for you with the supplied source component and string
     * comment. The <code>id</code> of the event will always be set to
     * zero.
     */
    public static void postAction (Component source, String command)
    {
        // slip things onto the event queue for later
        ActionEvent event = new ActionEvent(source, 0, command);
        EventQueue.invokeLater(new ActionInvoker(event));
    }

    /**
     * This class is used to dispatch action events to controllers within
     * the context of the AWT event dispatch mechanism.
     */
    protected static class ActionInvoker implements Runnable
    {
        public ActionInvoker (ActionEvent action)
        {
            _action = action;
        }

        public void run ()
        {
            // do some sanity checking on the source
            Object src = _action.getSource();
            if (src == null || !(src instanceof Component)) {
                Log.warning("Requested to dispatch action on " +
                            "non-component source [source=" + src +
                            ", action=" + _action + "].");
                return;
            }

            // scan up the component hierarchy looking for a controller on
            // which to dispatch this action
            Component source = (Component)src;
            do {
                if (source instanceof ControllerProvider) {
                    Controller ctrl =
                        ((ControllerProvider)source).getController();
                    try {
                        // if the controller returns true, it handled the
                        // action and we can call this business done
                        if (ctrl.handleAction(_action)) {
                            return;
                        }
                    } catch (Exception e) {
                        Log.warning("Controller choked on action " +
                                    "[ctrl=" + ctrl + "].");
                        Log.logStackTrace(e);
                    }
                }

                // move up the hierarchy
                source = source.getParent();

            } while (source != null);

            // if we got here, we didn't find a controller
            Log.warning("Unable to find a controller to process action " +
                        "[action=" + _action + "].");
        }

        protected ActionEvent _action;
    }
}
