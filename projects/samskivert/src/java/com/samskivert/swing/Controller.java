//
// $Id: Controller.java,v 1.16 2003/07/11 03:42:27 mdb Exp $
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

package com.samskivert.swing;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.AncestorEvent;

import com.samskivert.Log;
import com.samskivert.swing.event.AncestorAdapter;
import com.samskivert.swing.event.CommandEvent;

/**
 * The controller class provides a basis for the separation of user
 * interface code into display code and control code. The display code
 * lives in a panel class (<code>javax.swing.JPanel</code> or something
 * conceptually similar) and the control code lives in an associated
 * controller class.
 *
 * <p> The controller philosophy is thus: The panel class (and its UI
 * components) convert basic user interface actions into higher level
 * actions that more cleanly encapsulate the action desired by the user
 * and they pass those actions on to their controller. The controller then
 * performs abstract processing based on the users desires and the
 * changing state of the application and calls back to the panel to affect
 * changes to the display.
 *
 * <p> Controllers also support the notion of scope. When a panel wishes
 * to post an action, it doesn't do it directly to the controller. Instead
 * it does it using a controller utility function called {@link
 * #postAction}, which searches up the user interface hierarchy looking
 * for a component that implements {@link
 * com.samskivert.swing.ControllerProvider} which it will use to obtain
 * the controller "in scope" for that component. That controller is
 * requested to handle the action, but if it cannot handle the action, the
 * next controller up the chain is located and requested to process the
 * action. In this manner, a hierarchy of controllers (often just two: one
 * application wide and one for whatever particular mode the application
 * is in at the moment) can provide a set of services that are available
 * to all user interface elements in the entire application and in a way
 * that doesn't require tight connectedness between the UI elements and
 * the controllers.
 */
public abstract class Controller
{
    /**
     * This action listener can be wired up to any action event generator
     * and it will take care of forwarding that event on to the controller
     * in scope for the component that generated the action event.
     *
     * <p> For example, wiring a button up to a dispatcher would look like
     * so:
     *
     * <pre>
     * JButton button = new JButton("Do thing");
     * button.setActionCommand("dothing");
     * button.addActionListener(Controller.DISPATCHER);
     * </pre>
     *
     * or, use the provided convenience function:
     *
     * <pre>
     * JButton button =
     *     Controller.createActionButton("Do thing", "dothing");
     * </pre>
     *
     * The controllers in scope would then be requested (in order) to
     * process the <code>dothing</code> action whenever the button was
     * clicked.
     */
    public static final ActionListener DISPATCHER = new ActionListener() {
        public void actionPerformed (ActionEvent event)
        {
            Controller.postAction(event);
        }
    };

    /**
     * Lets this controller know about the panel that it is controlling.
     */
    public void setControlledPanel (JPanel panel)
    {
        panel.addAncestorListener(new AncestorAdapter() {
            public void ancestorAdded (AncestorEvent event) {
                wasAdded();
            }
            public void ancestorRemoved (AncestorEvent event) {
                wasRemoved();
            }
        });
    }

    /**
     * Called when the panel controlled by this controller was added to
     * the user interface hierarchy. This assumes that the controlled
     * panel made itself known to the controller via {@link
     * #setControlledPanel} (which is done automatically by {@link
     * ControlledPanel} and derived classes).
     */
    public void wasAdded ()
    {
    }

    /**
     * Called when the panel controlled by this controller was removed
     * from the user interface hierarchy. This assumes that the controlled
     * panel made itself known to the controller via {@link
     * #setControlledPanel} (which is done automatically by {@link
     * ControlledPanel} and derived classes).
     */
    public void wasRemoved ()
    {
    }

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
     * <p> The default implementation of this method will reflect on the
     * controller class, looking for a method that matches the name of the
     * action event. A handler method must provide one of three
     * signatures: one accepting no arguments, one including only a
     * reference to the source object, or one including the source object
     * and an extra argument (which can be used only if the action event
     * is an instance of {@link CommandEvent}. For example:
     *
     * <pre>
     * public void handleCancelClicked (Object source);
     * public void handleTextEntered (Object source, String text);
     * </pre>
     *
     * The arguments to the method can be as specific or as generic as
     * desired and reflection will perform the appropriate conversions at
     * runtime. For example, a method could be declared like so:
     *
     * <pre>
     * public void handleCancelClicked (JButton source);
     * </pre>
     *
     * One would have to ensure that the only action events generated with
     * the action command string "Cancel" were generated by
     * <code>JButton</code> instances if such a signature were used.
     *
     * @param action The action to be processed.
     *
     * @return true if the action was processed, false if it should be
     * propagated up to the next controller in scope.
     */
    public boolean handleAction (ActionEvent action)
    {
        Method method = null;
        Object[] args = null;

        try {
            // look for the appropriate method
            String targetName = "handle" + action.getActionCommand();
            Method[] methods = getClass().getMethods();
            int mcount = methods.length;

            for (int i = 0; i < mcount; i++) {
                if (methods[i].getName().equals(targetName)) {
                    // see if we can generate the appropriate arguments
                    args = generateArguments(methods[i], action);
                    // if we were able to, go ahead and use this method
                    if (args != null) {
                        method = methods[i];
                        break;
                    }
                }
            }

        } catch (Exception e) {
            Log.warning("Error searching for action handler method " +
                        "[controller=" + this + ", event=" + action + "].");
            return false;
        }

        try {
            if (method != null) {
                method.invoke(this, args);
                return true;

            } else {
                return false;
            }

        } catch (Exception e) {
            Log.warning("Error invoking action handler [controller=" + this +
                        ", event=" + action + "].");
            Log.logStackTrace(e);
            // even though we choked, we still "handled" the action
            return true;
        }
    }

    /**
     * A convenience method for constructing and immediately handling an
     * event on this controller (via {@link #handleAction(ActionEvent)}).
     *
     * @return true if the controller knew how to handle the action, false
     * otherwise.
     */
    public boolean handleAction (Component source, String command)
    {
        return handleAction(new ActionEvent(source, 0, command));
    }

    /**
     * A convenience method for constructing and immediately handling an
     * event on this controller (via {@link #handleAction(ActionEvent)}).
     *
     * @return true if the controller knew how to handle the action, false
     * otherwise.
     */
    public boolean handleAction (Component source, String command, Object arg)
    {
        return handleAction(new CommandEvent(source, command, arg));
    }

    /**
     * Used by {@link #handleAction} to generate arguments to the action
     * handler method.
     */
    protected Object[] generateArguments (Method method, ActionEvent action)
    {
        // figure out what sort of arguments are required by the method
        Class[] atypes = method.getParameterTypes();

        if (atypes == null || atypes.length == 0) {
            return new Object[0];

        } else if (atypes.length == 1) {
            return new Object[] { action.getSource() };

        } else if (atypes.length == 2) {
            if (action instanceof CommandEvent) {
                CommandEvent command = (CommandEvent)action;
                return new Object[] { action.getSource(),
                                      command.getArgument() };
            }

            Log.warning("Unable to map non-command event to " +
                        "handler method that requires extra " +
                        "argument [controller=" + this +
                        ", action=" + action + "].");
        }

        // we would have handled it, but we couldn't
        return null;
    }

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
     * command. The <code>id</code> of the event will always be set to
     * zero.
     */
    public static void postAction (Component source, String command)
    {
        // slip things onto the event queue for later
        ActionEvent event = new ActionEvent(source, 0, command);
        EventQueue.invokeLater(new ActionInvoker(event));
    }

    /**
     * Like {@link #postAction(ActionEvent)} except that it constructs a
     * {@link CommandEvent} with the supplied source component, string
     * command and argument.
     */
    public static void postAction (
        Component source, String command, Object argument)
    {
        // slip things onto the event queue for later
        CommandEvent event = new CommandEvent(source, command, argument);
        EventQueue.invokeLater(new ActionInvoker(event));
    }

    /**
     * Creates a button and configures it with the specified label and
     * action command and adds {@link #DISPATCHER} as an action listener.
     */
    public static JButton createActionButton (String label, String command)
    {
        JButton button = new JButton(label);
        button.setActionCommand(command);
        button.addActionListener(DISPATCHER);
        return button;
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
            for (Component source = (Component)src; source != null;
                 source = source.getParent()) {
                if (!(source instanceof ControllerProvider)) {
                    continue;
                }

                Controller ctrl = ((ControllerProvider)source).getController();
                if (ctrl == null) {
                    Log.warning("Provider returned null controller " +
                                "[provider=" + source + "].");
                    continue;
                }

                try {
                    // if the controller returns true, it handled the
                    // action and we can call this business done
                    if (ctrl.handleAction(_action)) {
                        return;
                    }

                } catch (Exception e) {
                    Log.warning("Controller choked on action " +
                                "[ctrl=" + ctrl +
                                ", action=" + _action + "].");
                    Log.logStackTrace(e);
                }
            }

            // if we got here, we didn't find a controller
            Log.warning("Unable to find a controller to process action " +
                        "[action=" + _action + "].");
        }

        protected ActionEvent _action;
    }
}
