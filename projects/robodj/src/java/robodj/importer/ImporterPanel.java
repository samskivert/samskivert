//
// $Id: ImporterPanel.java,v 1.1 2000/12/10 07:02:09 mdb Exp $

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
     */
    public abstract void wasAddedToFrame (ImporterFrame frame);
}
