//
// $Id: ImporterPanel.java,v 1.2 2002/03/03 21:17:03 mdb Exp $

package robodj.importer;

import java.awt.LayoutManager;
import javax.swing.JPanel;

public abstract class ImporterPanel extends JPanel
{
    public ImporterPanel ()
    {
    }

    public ImporterPanel (LayoutManager layout)
    {
	super(layout);
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
}
