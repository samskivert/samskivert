//
// $Id: Importer.java,v 1.5 2001/07/21 02:04:37 shaper Exp $

package robodj.importer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.sql.SQLException;
import java.util.Properties;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.Log;
import com.samskivert.util.PropertiesUtil;

import robodj.repository.Repository;

/**
 * The importer is the GUI-based application for ripping, encoding and
 * importing CDs and other music into the RoboDJ system.
 */
public class Importer
{
    /**
     * This is the string we use for the source field of entries created
     * by the importer.
     */
    public static final String ENTRY_SOURCE = "CD";

    public static Log log = new Log("importer");

    public static Properties config;

    public static Repository repository;

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
        } catch (SQLException sqe) {
            System.err.println("Unable to establish communication " +
                               "with music database: " + sqe);
            System.exit(-1);
        }

        // create our frame and first panel
	ImporterFrame frame = new ImporterFrame();
	InsertCDPanel panel = new InsertCDPanel();
	frame.setPanel(panel);

        // center the frame in the screen and show it
	frame.setSize(640, 480);
	SwingUtil.centerFrame(frame);
	frame.setVisible(true);
    }
}
