//
// $Id: Chooser.java,v 1.14 2004/01/26 16:33:40 mdb Exp $

package robodj.chooser;

import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.StaticConnectionProvider;
import com.samskivert.util.StringUtil;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.ConfigUtil;
import com.samskivert.util.PropertiesUtil;
import com.samskivert.util.RunAnywhere;

import robodj.Log;
import robodj.repository.Model;
import robodj.repository.Repository;
import robodj.util.ErrorUtil;
import robodj.util.RDJPrefs;
import robodj.util.RDJPrefsPanel;

/**
 * The chooser is the GUI-based application for browsing the music
 * collection and managing the playlist.
 */
public class Chooser extends DJClient
{
    public static Repository repository;

    public static Model model;

    public static ChooserFrame frame;

    public void init ()
    {
        boolean error = false;

//         try {
//             UIManager.setLookAndFeel(
//                 RunAnywhere.isLinux() ?
//                 "com.sun.java.swing.plaf.gtk.GTKLookAndFeel" :
//                 UIManager.getSystemLookAndFeelClassName());
//         } catch (Exception e) {
//             Log.info("Failed to set GTK look and feel: " + e);
//         }

        // loop until the user provides us with a configuration that works
        // or requests to exit
        for (int ii = 0; ii < 100; ii++) {
            String repodir = RDJPrefs.getRepositoryDirectory();
            if (StringUtil.blank(repodir) || ii > 0) {
                // display the initial configuration wizard if we are not
                // yet properly configured
                RDJPrefsPanel.display(true);
            }

            // create an interface to the database repository
            try {
                StaticConnectionProvider scp =
                    new StaticConnectionProvider(RDJPrefs.getJDBCConfig());
                repository = new Repository(scp);
                model = new Model(repository);

            } catch (PersistenceException pe) {
                String errmsg = "Unable to communicate with database:";
                if (ErrorUtil.reportError(errmsg, pe)) {
                    System.exit(-1);
                }
                continue;
            }

            if (!connectToServer()) {
                continue;
            }

            break;
        }
    }

    // documentation inherited from interface
    public void invokeLater (Runnable run)
    {
        SwingUtilities.invokeLater(run);
    }

    protected void didInit ()
    {
        super.didInit();
        displayInterface();
    }

    protected void displayInterface ()
    {
        if (frame == null) {
            // create our primary user interface frame, center the frame
            // in the screen and show it
            frame = new ChooserFrame();
            frame.setSize(660, frame.getToolkit().getScreenSize().height - 60);
            SwingUtil.centerWindow(frame);
            frame.setVisible(true);
        } else {
            frame.toFront();
        }
    }        

    public static void main (String[] args)
    {
        Chooser chooser = new Chooser();
        chooser.init();
    }
}
