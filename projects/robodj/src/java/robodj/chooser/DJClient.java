//
// $Id$

package robodj.chooser;

import java.io.IOException;

import com.samskivert.util.StringUtil;

import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientObserver;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.net.UsernamePasswordCreds;

import robodj.client.DJService;
import robodj.data.DJObject;
import robodj.util.ErrorUtil;
import robodj.util.RDJPrefs;
import robodj.util.RDJPrefsPanel;

/**
 * Handles the common client business for RoboDJ.
 */
public abstract class DJClient
    implements Client.Invoker, ClientObserver, Subscriber
{
    public static Client client;

    public static DJObject djobj;

    /**
     * Creates our {@link Client} instance and connects to the RoboDJ
     * server.
     *
     * @return true if the client was created successfully and the
     * connection process was initiated, false if it was not due to
     * missing or bogus configuration.
     */
    public boolean connectToServer ()
    {
        try {
            String host = RDJPrefs.getMusicDaemonHost();
            if (StringUtil.blank(host)) {
                throw new IOException(
                    "No music server host specified in configuration.");
            }
            // establish a connection with the music server
            Name name = new Name(RDJPrefs.getUser());
            client = new Client(new UsernamePasswordCreds(name, ""), this);
            client.addClientObserver(this);
            client.setServer(host, Client.DEFAULT_SERVER_PORT);
            client.logon();
            return true;

        } catch (IOException ioe) {
            String errmsg = "Unable to communicate with music " +
                "server on host '" + RDJPrefs.getMusicDaemonHost() + "' ";
            if (ErrorUtil.reportError(errmsg, ioe)) {
                System.exit(-1);
            }
            return false;
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
                    djoid, DJClient.this);
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
    public void objectAvailable (DObject object)
    {
        djobj = (DJObject)object;
        didInit();
    }

    // documentation inherited from interface
    public void requestFailed (int oid, ObjectAccessException cause)
    {
        ErrorUtil.reportError("Failed to fetch DJ object", cause);
        System.exit(-1);
    }

    /**
     * Called when we have successfully connected to the server and
     * subscribed to our {@link DJObject}.
     */
    protected void didInit ()
    {
    }

    public static void exit ()
    {
        client.logoff(false);
    }
}
