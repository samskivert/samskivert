//
// $Id: Chooser.java,v 1.12 2003/05/07 17:27:12 mdb Exp $

package robodj.chooser;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.StaticConnectionProvider;
import com.samskivert.util.StringUtil;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.ConfigUtil;
import com.samskivert.util.PropertiesUtil;

import robodj.Log;
import robodj.repository.Model;
import robodj.repository.Repository;
import robodj.util.ErrorUtil;
import robodj.util.RDJPrefs;
import robodj.util.RDJPrefsPanel;
import robodj.util.ServerControl;

/**
 * The chooser is the GUI-based application for browsing the music
 * collection and managing the playlist.
 */
public class Chooser
{
    public static Repository repository;

    public static Model model;

    public static ServerControl scontrol;

    public static ChooserFrame frame;

    public static void main (String[] args)
    {
        boolean error = false;

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

            try {
                String host = RDJPrefs.getMusicDaemonHost();
                if (StringUtil.blank(host)) {
                    throw new IOException(
                        "No music server host specified in configuration.");
                }
                // establish a connection with the music server
                scontrol = new ServerControl(
                    host, RDJPrefs.getMusicDaemonPort());
            } catch (IOException ioe) {
                String errmsg = "Unable to communicate with music " +
                    "server on host '" + RDJPrefs.getMusicDaemonHost() + "' ";
                if (ErrorUtil.reportError(errmsg, ioe)) {
                    System.exit(-1);
                }
                continue;
            }

            break;
        }

        // create our primary user interface frame, center the frame in
        // the screen and show it
        frame = new ChooserFrame();
        frame.setSize(650, 665);
        SwingUtil.centerWindow(frame);
        frame.setVisible(true);
    }
}
