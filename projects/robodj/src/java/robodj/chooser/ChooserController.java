//
// $Id: ChooserController.java,v 1.1 2003/05/04 18:16:06 mdb Exp $

package robodj.chooser;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

import com.samskivert.swing.Controller;
import com.samskivert.swing.util.TaskMaster;
import com.samskivert.swing.util.TaskObserver;

import robodj.Log;

/**
 * Handles top-level chooser UI commands.
 */
public class ChooserController extends Controller
    implements TaskObserver
{
    public ChooserController ()
    {
        // read our playing state
        TaskMaster.invokeMethodTask("refreshPlaying", Chooser.scontrol, this);
    }

    // documentation inherited
    public boolean handleAction (ActionEvent event)
    {
	String cmd = event.getActionCommand();
	if (cmd.equals("exit")) {
	    System.exit(0);

        } else if (cmd.equals("skip")) {
            TaskMaster.invokeMethodTask("skip", Chooser.scontrol, this);

        } else if (cmd.equals("back")) {
            TaskMaster.invokeMethodTask("back", Chooser.scontrol, this);

        } else if (cmd.equals("pause")) {
            TaskMaster.invokeMethodTask("pause", Chooser.scontrol, this);

        } else if (cmd.equals("play")) {
            TaskMaster.invokeMethodTask("play", Chooser.scontrol, this);

        } else if (cmd.equals("stop")) {
            TaskMaster.invokeMethodTask("stop", Chooser.scontrol, this);

	} else {
	    System.out.println("Unknown action event: " + cmd);
            return false;
	}

        return true;
    }

    // documentation inherited from interface
    public void taskCompleted (String name, Object result)
    {
        // nothing to do here
    }

    // documentation inherited from interface
    public void taskFailed (String name, Throwable exception)
    {
        String msg;
        if (Exception.class.equals(exception.getClass())) {
            msg = exception.getMessage();
        } else {
            msg = exception.toString();
        }
        JOptionPane.showMessageDialog(Chooser.frame, msg, "Error",
                                      JOptionPane.ERROR_MESSAGE); 
        Log.logStackTrace(exception);
    }
}
