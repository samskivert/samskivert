//
// $Id: Importer.java,v 1.9 2002/01/17 18:22:53 mdb Exp $

package robodj.importer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JOptionPane;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.StaticConnectionProvider;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.ConfigUtil;
import com.samskivert.util.PropertiesUtil;

import robodj.Log;
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

    public static Properties config;

    public static Repository repository;

    public static void main (String[] args)
    {
        // load our main configuration
        String cpath = "conf/importer.properties";
        try {
            config = ConfigUtil.loadProperties(cpath);
        } catch (IOException ioe) {
            String err = "Unable to load configuration " +
                "[path=" + cpath + "]: " + ioe;
            reportError(err);
            System.exit(-1);
        }

        // create an interface to the database repository
        try {
            StaticConnectionProvider scp =
                new StaticConnectionProvider("conf/repository.properties");
            repository = new Repository(scp);

        } catch (IOException ioe) {
            reportError("Error loading repository config: " + ioe);
            System.exit(-1);

        } catch (PersistenceException pe) {
            reportError("Unable to establish communication " +
                        "with music database: " + pe);
            System.exit(-1);
        }

        // create our frame and first panel
	ImporterFrame frame = new ImporterFrame();
	InsertCDPanel panel = new InsertCDPanel();
	frame.setPanel(panel);

        // center the frame in the screen and show it
	frame.setSize(640, 480);
	SwingUtil.centerWindow(frame);
	frame.setVisible(true);
    }
            
    protected static void reportError (String error)
    {
        Object[] options = { "OK" };
        JOptionPane.showOptionDialog(null, error, "Error",
                                     JOptionPane.DEFAULT_OPTION,
                                     JOptionPane.ERROR_MESSAGE,
                                     null, options, options[0]);
    }
}
