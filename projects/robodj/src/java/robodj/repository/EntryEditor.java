//
// $Id: EntryEditor.java,v 1.1 2001/07/26 00:24:22 mdb Exp $

package robodj.repository;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;
import java.util.StringTokenizer;

import com.samskivert.swing.*;
import com.samskivert.net.cddb.CDDB;

import robodj.Log;

public class EntryEditor
    extends JPanel
{
    public EntryEditor (Model model, Entry entry)
    {
	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	setLayout(gl);

	// we'll need the table model to listen to the title and artist
	// text fields
	_emodel = new EntryModel(entry);

	// create the title editing components
	JPanel titPanel = new JPanel(new HGroupLayout(GroupLayout.STRETCH));
	JLabel titLabel = new JLabel("Title");
	_titText = new JTextField();
	_titText.getDocument().addDocumentListener(_emodel);
	_titText.getDocument().putProperty("name", "title");
	titPanel.add(titLabel, GroupLayout.FIXED);
	titPanel.add(_titText);
	add(titPanel, GroupLayout.FIXED);

	// create the artist editing components
	JPanel artPanel = new JPanel(new HGroupLayout(GroupLayout.STRETCH));
	JLabel artLabel = new JLabel("Artist");
	_artText = new JTextField();
	_artText.getDocument().addDocumentListener(_emodel);
	_artText.getDocument().putProperty("name", "artist");
	artPanel.add(artLabel, GroupLayout.FIXED);
	artPanel.add(_artText);
	add(artPanel, GroupLayout.FIXED);

	// create the track table
	_trackTable = new JTable(_emodel);
	TableColumn tcol = _trackTable.getColumnModel().getColumn(0);
	// i wish this fucking worked
	// tcol.sizeWidthToFit();
	// then i wouldn't have do this nasty hack
	tcol.setMaxWidth(20);

	// make something for it to scroll around in
	JScrollPane tscroll = new JScrollPane(_trackTable);
	add(tscroll);

	JPanel butPanel = new JPanel(new HGroupLayout(GroupLayout.NONE));
        JButton fixCaseBtn = new JButton("Canonicalize text");
        fixCaseBtn.addActionListener(new ActionListener () {
            public void actionPerformed (ActionEvent ae) {
                fixCase();
            }
        });
        butPanel.add(fixCaseBtn);
	add(butPanel, GroupLayout.FIXED);

        // reset the editor which will read in the values from the model
	reset();
    }

    /**
     * Applies the edits made in the editor to the entry.
     */
    public void applyToEntry ()
    {
        flushEdits();
        _emodel.flushToEntry();
    }

    protected void flushEdits ()
    {
	// commit any uncommitted edits the user might have in progress
	TableCellEditor editor = _trackTable.getCellEditor();
	if (editor != null) {
	    editor.stopCellEditing();
	}
    }

    /**
     * Cleans up the case of the track, title and artist text. This
     * currently means capitalizing every word and performing some case
     * fixups according to an exception list.
     */
    public void fixCase ()
    {
        // clean up the title
        String title = fixCase(_emodel.getTitle());
        _emodel.setTitle(title);
        _titText.setText(title);

        // clean up the artist
        String artist = fixCase(_emodel.getArtist());
        _emodel.setArtist(artist);
        _artText.setText(artist);

        // clean up the tracks
        flushEdits();
        String[] names = _emodel.getTrackNames();
        for (int i = 0; i < names.length; i++) {
            _emodel.setTrackName(i, fixCase(names[i]));
        }
    }

    /**
     * Fixes the case of the supplied string according to our case fixing
     * rules.
     */
    protected String fixCase (String text)
    {
        StringTokenizer tok = new StringTokenizer(text);
        StringBuffer ntbuf = new StringBuffer();
        String excep;

        for (int i = 0; tok.hasMoreTokens(); i++) {
            String token = tok.nextToken();
            if (i > 0) {
                ntbuf.append(" ");
            }

            if ((excep = getException(token)) != null) {
                // if this token should be overridden explicitly, do so
                ntbuf.append(excep);

            } else if (Character.isLowerCase(token.charAt(0))) {
                // if the first character is lower case, fix it
                ntbuf.append(Character.toUpperCase(token.charAt(0)));
                if (token.length() > 1) {
                    ntbuf.append(token.substring(1));
                }

            } else {
                // otherwise don't mess with it
                ntbuf.append(token);
            }
        }

        return ntbuf.toString();
    }

    /**
     * Returns the exception string for this token if there is one
     * otherwise null.
     */
    protected String getException (String token)
    {
        // we could toLowerCase() the token and keep all this stuff in a
        // hashtable, but the list is small and we'd rather not create a
        // zillion objects with all those toLowerCase() calls, so we just
        // search the exception list linearly
        for (int i = 0; i < OVERRIDE_KEYS.length; i++) {
            if (token.equalsIgnoreCase(OVERRIDE_KEYS[i])) {
                return OVERRIDE_VALS[i];
            }
        }
        return null;
    }

    public void reset ()
    {
        flushEdits();
	_emodel.reset();
	_titText.setText(_emodel.getTitle());
	_artText.setText(_emodel.getArtist());
    }

    protected JTextField _titText;
    protected JTextField _artText;
    protected JTable _trackTable;

    protected EntryModel _emodel;
    protected int _newcatid;

    // used in case fixups
    protected static String[] OVERRIDE_KEYS = {
        "vs.", "dj", "/"
    };
    protected static String[] OVERRIDE_VALS = {
        "vs.", "DJ", "-"
    };
}
