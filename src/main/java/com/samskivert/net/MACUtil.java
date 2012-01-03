//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.samskivert.util.RunAnywhere;

/**
 * Attempts to find all the MAC addresses on the machine.
 * This is accomplished by calling external programs specific
 * to the platform we are on, and parsing the results.
 */
public class MACUtil
{
/**
    public static void main (String[] args)
    {
        String testOutput = "YOUR TEST STRING GOES HERE";

        String[] macs = parseMACs(testOutput);
        for (int ii = 0; ii < macs.length; ii++) {
            System.err.println("mac: " + macs[ii]);
        }
    }
*/

    /**
     * Get all the MAC addresses of the hardware we are running on that we can find.
     */
    public static String[] getMACAddresses ()
    {
        String [] cmds;
        if (RunAnywhere.isWindows()) {
            cmds = WINDOWS_CMDS;
        } else {
            cmds = UNIX_CMDS;
        }

        return parseMACs(tryCommands(cmds));
    }

    /**
     * Look through the text for all the MAC addresses we can find.
     */
    protected static String[] parseMACs (String text)
    {
        if (text == null) {
            return new String[0];
        }

        Matcher m = MACRegex.matcher(text);

        ArrayList<String> list = new ArrayList<String>();
        while (m.find()) {
            String mac = m.group(1).toUpperCase();
            mac = mac.replace(':', '-');

            // "Didn't you get that memo?" Apparently some people are not
            // up on MAC addresses actually being unique, so we will ignore those.
            //
            // 44-45-53-XX-XX-XX - PPP Adaptor
            // 00-53-45-XX-XX-XX - PPP Adaptor
            // 00-E0-06-09-55-66 - Some bogus run of ASUS motherboards
            // 00-04-4B-80-80-03 - Some nvidia built-in lan issues
            // 00-03-8A-XX-XX-XX - MiniWAN or AOL software
            // 02-03-8A-00-00-11 - Westell Dual (USB/Ethernet) modem
            // FF-FF-FF-FF-FF-FF - Tunnel adapter Teredo
            // 02-00-4C-4F-4F-50 - MSFT thinger, loopback of some sort
            // 00-00-00-00-00-00(-00-E0) - IP6 tunnel
            if (mac.startsWith("44-45-53")) {
                continue;
            } else if (mac.startsWith("00-53-45-00")) {
                continue;
            } else if (mac.startsWith("00-E0-06-09-55-66")) {
                continue;
            } else if (mac.startsWith("00-04-4B-80-80-03")) {
                continue;
            } else if (mac.startsWith("00-03-8A")) {
                continue;
            } else if (mac.startsWith("02-03-8A-00-00-11")) {
                continue;
            } else if (mac.startsWith("FF-FF-FF-FF-FF-FF")) {
                continue;
            } else if (mac.startsWith("02-00-4C-4F-4F-50")) {
                continue;
            } else if (mac.startsWith("00-00-00-00-00-00")) {
                continue;
            }

            list.add(mac);
        }

        return list.toArray(new String[0]);
    }

    /**
     * Takes a lists of commands and tries to run each one till one works.
     * The idea being to be able to 'gracefully' cope with not knowing
     * where a command is installed on different installations.
     */
    protected static String tryCommands (String[] cmds)
    {
        if (cmds == null) {
            return null;
        }

        String output;

        for (int ii = 0; ii < cmds.length; ii++) {
            output = runCommand(cmds[ii]);
            if (output != null) {
                return output;
            }
        }

        return null;
    }

    /**
     * Run the specified command and return the output as a string.
     */
    protected static String runCommand (String cmd)
    {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader cin = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder buffer= new StringBuilder();

            String line = "";
            while (line != null)
            {
                buffer.append(line);
                line = cin.readLine();
            }
            cin.close();

            return buffer.toString();
        } catch (IOException e) {
            // don't want to log anything for the client to know what we are doing.
            // This will almost always be thrown/caught when the cmd isn't there.
            return null;
        }
    }

    /** Look for 2 hex values in a row followed by a ':' or '-' repeated 5 times, followed by 2 hex
     * values. */
    protected static final Pattern MACRegex =
        Pattern.compile("((?:\\p{XDigit}{2}+[:\\-]){5}+\\p{XDigit}{2}+)", Pattern.CASE_INSENSITIVE);

    // TODO maybe we should obfuscate these with rot13 or something so
    // that people can't run 'strings' on us and instantly see what we try
    protected static final String[] WINDOWS_CMDS = {"ipconfig /all"};
    protected static final String[] UNIX_CMDS = {"/sbin/ifconfig", "/etc/ifconfig"};
}
