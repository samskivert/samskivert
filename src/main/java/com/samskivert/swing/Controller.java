//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.reflect.Method;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import com.samskivert.swing.event.CommandEvent;

import static com.samskivert.swing.Log.log;

/**
 * The controller class provides a basis for the separation of user interface
 * code into display code and control code. The display code lives in a panel
 * class (<code>javax.swing.JPanel</code> or something conceptually similar)
 * and the control code lives in an associated controller class.
 *
 * <p> The controller philosophy is thus: The panel class (and its UI
 * components) convert basic user interface actions into higher level actions
 * that more cleanly encapsulate the action desired by the user and they pass
 * those actions on to their controller. The controller then performs abstract
 * processing based on the users desires and the changing state of the
 * application and calls back to the panel to affect changes to the display.
 *
 * <p> Controllers also support the notion of scope. When a panel wishes to
 * post an action, it doesn't do it directly to the controller. Instead it does
 * it using a controller utility function called {@link #postAction}, which
 * searches up the user interface hierarchy looking for a component that
 * implements {@link ControllerProvider} which it will use to obtain the
 * controller "in scope" for that component. That controller is requested to
 * handle the action, but if it cannot handle the action, the next controller
 * up the chain is located and requested to process the action.
 *
 * <p> In this manner, a hierarchy of controllers (often just two: one
 * application wide and one for whatever particular mode the application is in
 * at the moment) can provide a set of services that are available to all user
 * interface elements in the entire application and in a way that doesn't
 * require tight connectedness between the UI elements and the controllers.
 */
public abstract class Controller
    implements ActionListener
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
     * Posts the specified action to the nearest controller in scope. The
     * controller search begins with the source component of the action and
     * traverses up the component tree looking for a controller to handle the
     * action. The controller location and action event processing is
     * guaranteed to take place on the AWT thread regardless of what thread
     * calls <code>postAction</code> and that processing will not occur
     * immediately but is instead appended to the AWT event dispatch queue for
     * processing.
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
     * Creates a button and configures it with the specified label and action
     * command and adds {@link #DISPATCHER} as an action listener.
     */
    public static JButton createActionButton (String label, String command)
    {
        JButton button = new JButton(label);
        configureAction(button, command);
        return button;
    }

    /**
     * Configures the supplied button with the {@link #DISPATCHER} action
     * listener and the specified action command (which, if it is a method name
     * will be looked up dynamically on the matching controller).
     */
    public static void configureAction (AbstractButton button, String action)
    {
        button.setActionCommand(action);
        button.addActionListener(DISPATCHER);
    }

    /**
     * Lets this controller know about the panel that it is controlling.
     */
    public void setControlledPanel (final JComponent panel)
    {
        panel.addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged (HierarchyEvent e) {
                boolean nowShowing = panel.isDisplayable();
                //System.err.println("Controller." + Controller.this +
                //    "  nowShowing=" + nowShowing +
                //    ", wasShowing=" + _showing);
                if (_showing != nowShowing) {
                    _showing = nowShowing;
                    if (_showing) {
                        wasAdded();
                    } else {
                        wasRemoved();
                    }
                }
            }

            boolean _showing = false;
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
     * Instructs this controller to process this action event. When an action
     * is posted by a user interface element, it will be posted to the
     * controller in closest scope for that element. If that controller handles
     * the event, it should return true from this method to indicate that
     * processing should stop. If it cannot handle the event, it can return
     * false to indicate that the event should be propagated to the next
     * controller up the chain.
     *
     * <p> This method will be called on the AWT thread, so the controller can
     * safely manipulate user interface components while handling an action.
     * However, this means that action handling cannot block and should not
     * take an undue amount of time. If the controller needs to perform
     * complicated, lengthy processing it should do so with a separate thread,
     * for example via {@link com.samskivert.swing.util.TaskMaster}.
     *
     * <p> The default implementation of this method will reflect on the
     * controller class, looking for a method that matches the name of the
     * action event. For example, if the action was "exit" a method named
     * "exit" would be sought. A handler method must provide one of three
     * signatures: one accepting no arguments, one including only a reference
     * to the source object, or one including the source object and an extra
     * argument (which can be used only if the action event is an instance of
     * {@link CommandEvent}). For example:
     *
     * <pre>
     * public void cancelClicked (Object source);
     * public void textEntered (Object source, String text);
     * </pre>
     *
     * The arguments to the method can be as specific or as generic as desired
     * and reflection will perform the appropriate conversions at runtime. For
     * example, a method could be declared like so:
     *
     * <pre>
     * public void cancelClicked (JButton source);
     * </pre>
     *
     * One would have to ensure that the only action events generated with the
     * action command string "cancelClicked" were generated by JButton
     * instances if such a signature were used.
     *
     * @param action the action to be processed.
     *
     * @return true if the action was processed, false if it should be
     * propagated up to the next controller in scope.
     */
    public boolean handleAction (ActionEvent action)
    {
        Object arg = null;
        if (action instanceof CommandEvent) {
            arg = ((CommandEvent)action).getArgument();
        }
        return handleAction(action.getSource(), action.getActionCommand(), arg);
    }

    /**
     * A version of {@link #handleAction(ActionEvent)} with the parameters
     * broken out so that it can be used by non-Swing interface toolkits.
     */
    public boolean handleAction (Object source, String action, Object arg)
    {
        Method method = null;
        Object[] args = null;

        try {
            // look for the appropriate method
            Method[] methods = getClass().getMethods();
            int mcount = methods.length;

            for (int i = 0; i < mcount; i++) {
                if (methods[i].getName().equals(action) ||
                    // handle our old style of prepending "handle"
                    methods[i].getName().equals("handle" + action)) {
                    // see if we can generate the appropriate arguments
                    args = generateArguments(methods[i], source, arg);
                    // if we were able to, go ahead and use this method
                    if (args != null) {
                        method = methods[i];
                        break;
                    }
                }
            }

        } catch (Exception e) {
            log.warning("Error searching for action handler method", "controller", this,
                        "action", action);
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
            log.warning("Error invoking action handler", "controller", this, "action", action, e);
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

    // documentation inherited from interface ActionListener
    public void actionPerformed (ActionEvent event)
    {
        handleAction(event);
    }

    /**
     * Used by {@link #handleAction} to generate arguments to the action
     * handler method.
     */
    protected Object[] generateArguments (
        Method method, Object source, Object argument)
    {
        // figure out what sort of arguments are required by the method
        Class<?>[] atypes = method.getParameterTypes();

        if (atypes == null || atypes.length == 0) {
            return new Object[0];

        } else if (atypes.length == 1) {
            return new Object[] { source };

        } else if (atypes.length == 2) {
            if (argument != null) {
                return new Object[] { source, argument };
            }
            log.warning("Unable to map argumentless event to handler method that requires an " +
                        "argument", "controller", this, "method", method, "source", source);
        }

        // we would have handled it, but we couldn't
        return null;
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
                log.warning("Requested to dispatch action on non-component source", "source", src,
                            "action", _action);
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
                    log.warning("Provider returned null controller", "provider", source);
                    continue;
                }

                try {
                    // if the controller returns true, it handled the
                    // action and we can call this business done
                    if (ctrl.handleAction(_action)) {
                        return;
                    }

                } catch (Exception e) {
                    log.warning("Controller choked on action", "ctrl", ctrl, "action", _action, e);
                }
            }

            // if we got here, we didn't find a controller
            log.warning("Unable to find a controller to process action", "action", _action);
        }

        protected ActionEvent _action;
    }
}
