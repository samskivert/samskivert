//
// $Id: CDInfoEditor.java,v 1.3 2001/06/05 17:41:00 mdb Exp $

package robodj.importer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.StringTokenizer;

import com.samskivert.swing.*;
import com.samskivert.net.cddb.CDDB;

public class CDInfoEditor
    extends JPanel
{
    public CDInfoEditor (CDDB.Detail[] details, int numTracks)
    {
	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	setLayout(gl);

	// create the selection components
	JPanel selPanel = new JPanel(new HGroupLayout(GroupLayout.STRETCH));
	JLabel selLabel = new JLabel("Select...");

	// create a string array for our combo box
	String[] names = new String[details.length + 1];
	for (int i = 0; i < details.length; i++) {
	    names[i] = details[i].title;
	}
        names[details.length] = "<blank>";

	_selBox = new JComboBox(names);
	selPanel.add(selLabel, GroupLayout.FIXED);
	selPanel.add(_selBox);

	_selBox.addActionListener(new ActionListener ()
	{
	    public void actionPerformed (ActionEvent e)
	    {
		JComboBox cb = (JComboBox)e.getSource();
		selectDetail(cb.getSelectedIndex());
	    }
	});

	add(selPanel, GroupLayout.FIXED);

	// create the table model because we need it to listen to the
	// title and artist text fields
	_tmodel = new DetailTableModel(details, numTracks);

	// create the title editing components
	JPanel titPanel = new JPanel(new HGroupLayout(GroupLayout.STRETCH));
	JLabel titLabel = new JLabel("Title");
	_titText = new JTextField();
	_titText.getDocument().addDocumentListener(_tmodel);
	_titText.getDocument().putProperty("name", "title");
	titPanel.add(titLabel, GroupLayout.FIXED);
	titPanel.add(_titText);
	add(titPanel, GroupLayout.FIXED);

	// create the artist editing components
	JPanel artPanel = new JPanel(new HGroupLayout(GroupLayout.STRETCH));
	JLabel artLabel = new JLabel("Artist");
	_artText = new JTextField();
	_artText.getDocument().addDocumentListener(_tmodel);
	_artText.getDocument().putProperty("name", "artist");
	artPanel.add(artLabel, GroupLayout.FIXED);
	artPanel.add(_artText);
	add(artPanel, GroupLayout.FIXED);

	// create the track table
	_trackTable = new JTable(_tmodel);
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

	selectDetail(0);
    }

    public String getTitle ()
    {
	return _tmodel.getTitle();
    }

    public String getArtist ()
    {
	return _tmodel.getArtist();
    }

    public String[] getTrackNames ()
    {
	// commit any uncommitted edits the user might have in progress
	TableCellEditor editor = _trackTable.getCellEditor();
	if (editor != null) {
	    editor.stopCellEditing();
	}
	return _tmodel.getTrackNames();
    }

    /**
     * Cleans up the case of the track, title and artist text. This
     * currently means capitalizing every word and performing some case
     * fixups according to an exception list.
     */
    public void fixCase ()
    {
        // clean up the title
        String title = fixCase(_tmodel.getTitle());
        _tmodel.setTitle(title);
        _titText.setText(title);

        // clean up the artist
        String artist = fixCase(_tmodel.getArtist());
        _tmodel.setArtist(artist);
        _artText.setText(artist);

        // clean up the tracks
        String[] names = getTrackNames();
        for (int i = 0; i < names.length; i++) {
            _tmodel.setTrackName(i, fixCase(names[i]));
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

        // report modifications for now
        String ntext = ntbuf.toString();
//          if (!ntext.equals(text)) {
//              System.out.println("Adjusted case '" + text + "' to '" +
//                                 ntext + "'.");
//          }

        return ntext;
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

    protected void selectDetail (int index)
    {
	_tmodel.selectDetail(index);
	_titText.setText(_tmodel.getTitle());
	_artText.setText(_tmodel.getArtist());
    }

    protected JComboBox _selBox;
    protected JTextField _titText;
    protected JTextField _artText;
    protected JTable _trackTable;

    protected DetailTableModel _tmodel;

    // used in case fixups
    protected static String[] OVERRIDE_KEYS = {
        "vs.", "dj", "/"
    };
    protected static String[] OVERRIDE_VALS = {
        "vs.", "DJ", "-"
    };
}
