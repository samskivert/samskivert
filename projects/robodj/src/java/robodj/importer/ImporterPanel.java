//
// $Id: ImporterPanel.java,v 1.2 2002/03/03 21:17:03 mdb Exp $

package robodj.importer;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public abstract class ImporterPanel extends JPanel
{
    public ImporterPanel ()
    {
        this(new BorderLayout());
    }

    public ImporterPanel (LayoutManager layout)
    {
	super(layout);

	_status = new JTextArea();
	_status.setLineWrap(true);
	_status.setEditable(false);
    }

    /**
     * When an importer panel is added to the importer frame, it is
     * notified so that it can create the appropriate control buttons.
     *
     * @param frame a reference to the importer frame.
     * @param popped if true, this panel was popped from the stack rather
     * than added to the frame for the first time.
     */
    public abstract void wasAddedToFrame (ImporterFrame frame, boolean popped);

    /**
     * Displays the supplied string in the status field and scrolls the
     * status field to the bottom.
     */
    protected void postAsyncStatus (final String status)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run ()
            {
                _status.append(status + "\n");
                Rectangle bot = _status.getBounds();
                bot.y = bot.height-1;
                bot.height = 1;
                _status.scrollRectToVisible(bot);
            }
        });
    }

    protected JTextArea _status;
}
