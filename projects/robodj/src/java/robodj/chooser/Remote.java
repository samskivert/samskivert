//
// $Id$

package robodj.chooser;

import com.samskivert.util.Queue;

/**
 * Provides the ability to perform some basic RoboDJ commands from the
 * command line.
 */
public class Remote extends DJClient
{
    public void invoke (String command)
    {
        _command = command;
        connectToServer();
    }

    protected void didInit ()
    {
        super.didInit();

        if (_command.equals("play")) {
            djobj.play();

        } else if (_command.equals("pause")) {
            djobj.pause();

        } else if (_command.equals("stop")) {
            djobj.stop();

        } else {
            System.err.println("Unknown command: " + _command);
        }

        exit();
    }

    // documentation inherited from interface
    public void invokeLater (Runnable run)
    {
        _queue.append(run);
    }

    public static void main (String[] args)
    {
        if (args.length == 0) {
            System.out.println("Usage: Remote [play|pause|stop]");
            System.exit(-1);
        }
        Remote remote = new Remote();
        remote.invoke(args[0]);

        Runnable run;
        while ((run = (Runnable)_queue.get()) != null) {
            run.run();
        }
    }

    protected String _command;
    protected static Queue _queue = new Queue();
}
