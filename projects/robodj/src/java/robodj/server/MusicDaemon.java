//
// $Id$

package robodj.server;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.StaticConnectionProvider;
import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.server.PresentsServer;

import robodj.Log;
import robodj.data.DJObject;
import robodj.repository.Repository;
import robodj.util.ErrorUtil;
import robodj.util.RDJPrefs;
import robodj.util.RDJPrefsPanel;

/**
 * The main entry point for the music playing daemon.
 */
public class MusicDaemon extends PresentsServer
{
    /** Provides access to the main RoboDJ services. */
    public static DJProvider djprov = new DJProvider();

    /** The entity that actually plays the music in the playlist. */
    public static MusicPlayer player = new MusicPlayer();

    /** Our shared server state. */
    public static DJObject djobj;

    /** Our music repository. */
    public static Repository repo;

    // documentation inherited
    public void init ()
        throws Exception
    {
        super.init();

        // loop until the user provides us with a configuration that works
        // or requests to exit
        for (int ii = 0; ii < 100; ii++) {
            String repodir = RDJPrefs.getRepositoryDirectory();
            if (StringUtil.blank(repodir) || ii > 0) {
                // display the initial configuration wizard if we are not
                // yet properly configured
                RDJPrefsPanel.display(true);
            }

            // create our repository
            try {
                repo = new Repository(
                    new StaticConnectionProvider(RDJPrefs.getJDBCConfig()));

            } catch (PersistenceException pe) {
                String errmsg = "Unable to communicate with database:";
                if (ErrorUtil.reportError(errmsg, pe)) {
                    System.exit(-1);
                }
                continue;
            }

            break;
        }

        // create and register our providers
        invmgr.registerDispatcher(new DJDispatcher(djprov), true);

        // create our DJ object
        omgr.createObject(DJObject.class, new Subscriber() {
            public void objectAvailable (DObject object) {
                djobj = (DJObject)object;
                player.init(djobj);
                Log.info("Music daemon ready [oid=" + djobj.getOid() + "].");
            }
            public void requestFailed (int oid, ObjectAccessException cause) {
                Log.warning("Failed to create DJ object: " + cause);
            }
        });
    }

    public static void main (String[] args)
    {
        Log.info("RoboDJ music server starting...");

        MusicDaemon server = new MusicDaemon();
        try {
            // initialize the server
            server.init();
            // start the server to running (this method call won't return
            // until the server is shut down)
            server.run();

        } catch (Exception e) {
            Log.warning("Unable to initialize server.");
            Log.logStackTrace(e);
            System.exit(-1);
        }
    }
}
