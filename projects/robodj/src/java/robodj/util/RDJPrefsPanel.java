//
// $Id: RDJPrefsPanel.java,v 1.2 2003/05/07 17:27:26 mdb Exp $

package robodj.util;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.samskivert.swing.Spacer;
import com.samskivert.swing.util.SwingUtil;

/**
 * Displays an interface for editing our RoboDJ preferences.
 */
public class RDJPrefsPanel extends JPanel
{
    /**
     * Displays a frame containing a panel for editing our preferences.
     *
     * @param blockCaller if true the caller will be blocked until the
     * user dismisses the preferences panel.
     */
    public static void display (boolean blockCaller)
    {
        JFrame frame = new JFrame("RoboDJ Configuration");
        RDJPrefsPanel panel = new RDJPrefsPanel(frame);
        frame.setContentPane(panel);
        frame.pack();
        SwingUtil.centerWindow(frame);
        frame.show();

        if (blockCaller) {
            panel.awaitDismiss();
        }
    }

    protected RDJPrefsPanel (JFrame frame)
    {
        super(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        _frame = frame;
        _frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        _frame.addWindowListener(new WindowAdapter() {
            public void windowClosed (WindowEvent event) {
                dismissed();
            }
        });

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 1;

        c.gridwidth = GridBagConstraints.REMAINDER;
        JLabel title = new JLabel("RoboDJ Preferences");
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setFont(title.getFont().deriveFont(16f));
        add(title, c);
        add(new Spacer(10, 10), c);

        for (int ii = 0; ii < PREFS.length; ii += 2) {
            if (PREFS[ii].equals("")) {
                c.gridwidth = GridBagConstraints.REMAINDER;
                add(new Spacer(10, 10), c);

            } else {
                JLabel label = new JLabel(PREFS[ii]);
                c.gridwidth = GridBagConstraints.RELATIVE;
                c.ipadx = 5;
                c.ipady = 8;
                c.weightx = 1;
                add(label, c);

                JTextField value = new JTextField(
                    RDJPrefs.config.getValue(PREFS[ii+1], "")) {
                    public Dimension getPreferredSize () {
                        Dimension d = super.getPreferredSize();
                        d.width = Math.min(250, d.width);
                        return d;
                    }
                };
                PrefListener plist = new PrefListener(value, PREFS[ii+1]);
                value.addFocusListener(plist);
                value.addActionListener(plist);
                c.ipadx = 0;
                c.ipady = 2;
                c.weightx = 2;
                c.gridwidth = GridBagConstraints.REMAINDER;
                add(value, c);
            }
        }

        // add a dismiss button
        JButton dismiss = new JButton("Dismiss");
        dismiss.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                _frame.dispose();
            }
        });
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        c.ipadx = c.ipady = 0;
        add(new Spacer(10, 10), c);
        add(dismiss, c);
    }

    protected synchronized void awaitDismiss ()
    {
        while (_frame.isShowing()) {
            try {
                wait();
            } catch (InterruptedException ie) {
            }
        }
    }

    protected synchronized void dismissed ()
    {
        notify();
    }

    protected static class PrefListener
        implements ActionListener, FocusListener
    {
        public PrefListener (JTextField text, String prefKey)
        {
            _text = text;
            _prefKey = prefKey;
        }

        // documentation inherited from interface FocusListener
        public void focusGained (FocusEvent e)
        {
            // nothing
        }

        // documentation inherited from interface FocusListener
        public void focusLost (FocusEvent e)
        {
            actionPerformed(new ActionEvent(_text, 0, "focusLost"));
        }

        // documentation inherited from interface ActionListener
        public void actionPerformed (ActionEvent e)
        {
            RDJPrefs.config.setValue(_prefKey, _text.getText());
            System.out.println("Updated " + _prefKey + " -> " +
                               _text.getText());
        }

        protected JTextField _text;
        protected String _prefKey;
    }

    public static void main (String[] args)
    {
        display(true);
    }

    protected JFrame _frame;

    /** Defines our configurable preferences. */
    protected static final String[] PREFS = {
        "Music repository directory", RDJPrefs.REPO_DIR_KEY,
        "Music temporary directory", RDJPrefs.REPO_TMPDIR_KEY,
        "", "",
        "Music database JDBC driver", RDJPrefs.JDBC_DRIVER_KEY,
        "Music database JDBC url", RDJPrefs.JDBC_URL_KEY,
        "Music database JDBC username", RDJPrefs.JDBC_USERNAME_KEY,
        "Music database JDBC password", RDJPrefs.JDBC_PASSWORD_KEY,
        "", "",
        "Music server hostname", RDJPrefs.MUSICD_HOST_KEY,
        "Music server port", RDJPrefs.MUSICD_PORT_KEY,
        "", "",
        "CDDB server hostname", RDJPrefs.CDDB_HOST_KEY,
    };
}
