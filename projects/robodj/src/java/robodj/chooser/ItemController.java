//
// $Id: ItemController.java,v 1.1 2003/05/04 18:16:06 mdb Exp $

package robodj.chooser;

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import com.samskivert.io.PersistenceException;
import com.samskivert.swing.Controller;
import com.samskivert.swing.util.TaskMaster;
import com.samskivert.swing.util.TaskObserver;

import robodj.Log;
import robodj.repository.Song;

/**
 * Handles standard commands when displaying lists of items.
 */
public abstract class ItemController extends Controller
    implements TaskObserver
{
    public ItemController (JComponent panel)
    {
        _panel = panel;
    }

    // documentation inherited from interface
    public boolean handleAction (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals(SongItem.ADD_VOTE)) {
            JButton button = (JButton)e.getSource();
            SongItem item =  (SongItem)button.getParent();
            _song = item.getSong();
            if (_song.addVote(Chooser.frame.getUser(true))) {
                item.update();
                TaskMaster.invokeMethodTask("updateSong", this, this);
            }

        } else if (cmd.equals(SongItem.CLEAR_VOTE)) {
            JButton button = (JButton)e.getSource();
            SongItem item =  (SongItem)button.getParent();
            _song = item.getSong();
            if (_song.clearVote(Chooser.frame.getUser(true))) {
                item.update();
                TaskMaster.invokeMethodTask("updateSong", this, this);
            }

	} else {
	    Log.warning("Unknown action event: " + cmd);
            return false;
	}

        return true;
    }

    /**
     * Updates the song referenced by {@link #_song} in the repository.
     */
    public void updateSong ()
    {
        if (_song != null) {
            try {
                Chooser.repository.updateSong(_song);
            } catch (PersistenceException pe) {
                Chooser.reportError(
                    "Failure updating song '" + _song + "'", pe);
            }
        }
    }

    // documentation inherited from interface
    public void taskCompleted (String name, Object result)
    {
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
        JOptionPane.showMessageDialog(_panel, msg, "Error",
                                      JOptionPane.ERROR_MESSAGE); 
        Log.logStackTrace(exception);
    }

    protected JComponent _panel;
    protected Song _song;
}
