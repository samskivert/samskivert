//
// $Id: Importer.java,v 1.1 2000/12/10 07:02:09 mdb Exp $

package robodj.importer;

import com.samskivert.util.Log;

/**
 * The importer is the GUI-based application for ripping, encoding and
 * importing CDs and other music into the RoboDJ system.
 */
public class Importer
{
    public static Log log = new Log("importer");

    public static void main (String[] args)
    {
	ImporterFrame frame = new ImporterFrame();
	InsertCDPanel panel = new InsertCDPanel();
	frame.setPanel(panel);
	frame.setVisible(true);
    }
}
