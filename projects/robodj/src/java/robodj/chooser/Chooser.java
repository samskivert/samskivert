//
// $Id: Chooser.java,v 1.14 2004/01/26 16:33:40 mdb Exp $

package robodj.chooser;

import java.io.IOException;
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

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientObserver;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.net.UsernamePasswordCreds;

import robodj.Log;
import robodj.client.DJService;
import robodj.data.DJObject;
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
    implements Client.Invoker, ClientObserver, Subscriber
{
    public static Repository repository;

    public static Model model;

    public static Client client;

    public static DJObject djobj;

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

            try {
                String host = RDJPrefs.getMusicDaemonHost();
                if (StringUtil.blank(host)) {
                    throw new IOException(
                        "No music server host specified in configuration.");
                }
                // establish a connection with the music server
                client = new Client(new UsernamePasswordCreds(), this);
                client.addClientObserver(this);
                client.setServer(host, Client.DEFAULT_SERVER_PORT);
                client.logon();

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
    }

    // documentation inherited from interface
    public void clientDidLogon (Client lclient)
    {
        // subscribe to our DJObject
        DJService djsvc = (DJService)client.requireService(DJService.class);
        djsvc.getDJOid(client, new DJService.ResultListener() {
            public void requestProcessed (Object result) {
                int djoid = ((Integer)result).intValue();
                client.getDObjectManager().subscribeToObject(
                    djoid, Chooser.this);
            }
            public void requestFailed (String cause) {
                ErrorUtil.reportError("Failed to fetch DJ oid: " + cause, null);
                System.exit(-1);
            }
        });
    }

    // documentation inherited from interface
    public void clientObjectDidChange (Client client)
    {
    }

    // documentation inherited from interface
    public void clientDidLogoff (Client client)
    {
        System.exit(-1);
    }

    // documentation inherited from interface
    public void clientFailedToLogon (Client lclient, Exception cause)
    {
        if (ErrorUtil.reportError("Failed to connect to server", cause)) {
            System.exit(-1);
        } else {
            Thread t = new Thread() {
                public void run () {
                    RDJPrefsPanel.display(true);
                    client.setServer(RDJPrefs.getMusicDaemonHost(),
                                     Client.DEFAULT_SERVER_PORT);
                    client.logon();
                }
            };
            t.start();
        }
    }

    // documentation inherited from interface
    public void clientConnectionFailed (Client client, Exception cause)
    {
        ErrorUtil.reportError("Lost connection to server", cause);
        System.exit(-1);
    }

    // documentation inherited from interface
    public boolean clientWillLogoff (Client client)
    {
        return true;
    }

    // documentation inherited from interface
    public void invokeLater (Runnable run)
    {
        SwingUtilities.invokeLater(run);
    }

    // documentation inherited from interface
    public void objectAvailable (DObject object)
    {
        djobj = (DJObject)object;
        displayInterface();
    }

    // documentation inherited from interface
    public void requestFailed (int oid, ObjectAccessException cause)
    {
        ErrorUtil.reportError("Failed to fetch DJ object", cause);
        System.exit(-1);
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

    public static void exit ()
    {
        client.logoff(false);
    }

    public static void main (String[] args)
    {
        Chooser chooser = new Chooser();
        chooser.init();
    }
}
