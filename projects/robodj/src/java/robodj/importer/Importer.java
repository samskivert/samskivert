//
// $Id: Importer.java,v 1.11 2003/05/07 17:27:39 mdb Exp $

package robodj.importer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.Properties;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.StaticConnectionProvider;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.ConfigUtil;
import com.samskivert.util.PropertiesUtil;
import com.samskivert.util.StringUtil;

import robodj.Log;
import robodj.repository.Repository;
import robodj.util.RDJPrefs;
import robodj.util.RDJPrefsPanel;
import robodj.util.ErrorUtil;

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

    public static Repository repository;

    public static void main (String[] args)
    {
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

            } catch (PersistenceException pe) {
                String errmsg = "Unable to communicate with database:";
                if (ErrorUtil.reportError(errmsg, pe)) {
                    System.exit(-1);
                }
                continue;
            }

            break;
        }

        // create our frame and first panel
	ImporterFrame frame = new ImporterFrame();
	InsertCDPanel panel = new InsertCDPanel();
	frame.pushPanel(panel);

        // center the frame in the screen and show it
	frame.setSize(640, 480);
	SwingUtil.centerWindow(frame);
	frame.setVisible(true);
    }
            
}
