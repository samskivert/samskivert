//
// $Id: ChooserController.java,v 1.1 2003/05/04 18:16:06 mdb Exp $

package robodj.chooser;

import java.awt.event.ActionEvent;

import com.samskivert.swing.Controller;

import robodj.Log;

/**
 * Handles top-level chooser UI commands.
 */
public class ChooserController extends Controller
{
    public ChooserController ()
    {
    }

    // documentation inherited
    public boolean handleAction (ActionEvent event)
    {
	String cmd = event.getActionCommand();
	if (cmd.equals("exit")) {
	    Chooser.exit();

        } else if (cmd.equals("skip")) {
            Chooser.djobj.skip();

        } else if (cmd.equals("back")) {
            Chooser.djobj.back();

        } else if (cmd.equals("pause")) {
            Chooser.djobj.pause();

        } else if (cmd.equals("play")) {
            Chooser.djobj.play();

        } else if (cmd.equals("stop")) {
            Chooser.djobj.stop();

	} else {
	    Log.warning("Unknown action event: " + cmd);
            return false;
	}

        return true;
    }
}
