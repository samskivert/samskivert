//
// $Id$

package robodj.importer;

import java.util.ArrayList;
import java.util.Iterator;

import java.awt.BorderLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import robodj.repository.Entry;
import robodj.repository.Song;

/**
 * Displays an Entry and a list of similar entries and a check box.
 */
public class EntryUploader extends JPanel
{
    public EntryUploader (Entry entry, ArrayList similar)
    {
        super(new BorderLayout(5, 5));
        add(new JLabel(entry.artist + " - " + entry.title),
            BorderLayout.CENTER);
        add(_checked = new JCheckBox(), BorderLayout.WEST);
        StringBuffer extra = new StringBuffer("<html>");
        for (int ii = 0; ii < entry.songs.length; ii++) {
            Song song = entry.songs[ii];
            extra.append(song.position).append(". ");
            extra.append(song.title).append("<br>");
        }
        if (similar.size() > 0) {
            extra.append("<p><u>Similar entries:</u><br>");
            for (Iterator iter = similar.iterator(); iter.hasNext(); ) {
                Entry siment = (Entry)iter.next();
                extra.append(siment.artist).append(" - ").append(siment.title);
                extra.append("<br>");
            }
        }
        extra.append("</html>");
        JLabel exlab = new JLabel(extra.toString());
        exlab.setFont(getFont().deriveFont(11));
        add(exlab, BorderLayout.SOUTH);
    }

    protected JCheckBox _checked;
}
