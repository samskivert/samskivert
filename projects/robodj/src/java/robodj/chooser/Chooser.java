//
// $Id: Chooser.java,v 1.2 2001/06/07 08:37:47 mdb Exp $

package robodj.chooser;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import com.samskivert.util.PropertiesUtil;

import robodj.Log;
import robodj.repository.Model;
import robodj.repository.Repository;
import robodj.util.ServerControl;

/**
 * The chooser is the GUI-based application for browsing the music
 * collection and managing the playlist.
 */
public class Chooser
{
    public static Properties config;

    public static Repository repository;

    public static Model model;

    public static ServerControl scontrol;

    public static void main (String[] args)
    {
        // set up our configuration by hand for now
        config = new Properties();
        config.setProperty("repository.db.driver", "org.gjt.mm.mysql.Driver");
        config.setProperty("repository.db.username", "www");
        config.setProperty("repository.db.password", "Il0ve2PL@Y");
        config.setProperty("repository.db.url",
                           "jdbc:mysql://depravity:3306/robodj");
        config.setProperty("repository.basedir", "/export/robodj/repository");

        // create an interface to the database repository
        try {
            Properties dbprops =
                PropertiesUtil.getSubProperties(config, "repository.db");
            repository = new Repository(dbprops);
            model = new Model(repository);

        } catch (SQLException sqe) {
            Log.warning("Unable to establish communication with music " +
                        "database: " + sqe);
            System.exit(-1);
        }

        try {
            // establish a connection with the music server
            scontrol = new ServerControl("depravity", 2500);
        } catch (IOException ioe) {
            Log.warning("Unable to establish communication with music " +
                        "server: " + ioe);
            System.exit(-1);
        }

        // create our primary user interface frame, center the frame in
        // the screen and show it
	ChooserFrame frame = new ChooserFrame();
        Toolkit tk = frame.getToolkit();
        Dimension ss = tk.getScreenSize();
        int width = 550, height = 500;
        frame.setBounds((ss.width-width)/2, (ss.height-height)/2,
                        width, height);
	frame.setVisible(true);
    }
}
