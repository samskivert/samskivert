//
// $Id: EntryModel.java,v 1.1 2001/07/26 00:24:22 mdb Exp $

package robodj.repository;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;

import robodj.Log;

public class EntryModel
    extends AbstractTableModel
    implements DocumentListener
{
    public EntryModel (Entry entry)
    {
	_entry = entry;
	_names = new String[entry.songs.length];
        parseEntry();
    }

    /**
     * Resets the model from the information in the entry.
     */
    public void reset ()
    {
        // reread our data
        parseEntry();
	// fire a data changed event to let the table know what's up
	fireTableDataChanged();
    }

    /**
     * Extracts the info from the entry, overwriting any local
     * modifications made to the model.
     */
    protected void parseEntry ()
    {
        _title = _entry.title;
        _artist = _entry.artist;

        for (int i = 0; i < _names.length; i++) {
            _names[i] = _entry.songs[i].title;
        }
    }

    /**
     * Updates the entry with the values stored in the model.
     */
    public void flushToEntry ()
    {
        _entry.title = _title;
        _entry.artist = _artist;

        for (int i = 0; i < _names.length; i++) {
            _entry.songs[i].title = _names[i];
        }
    }

    public String getTitle ()
    {
	return _title;
    }

    public String getArtist ()
    {
	return _artist;
    }

    // this does not cause the UI displaying the title to update because
    // it isn't listening to the model, we're only listening to it
    public void setTitle (String title)
    {
        _title = title;
    }

    // this does not cause the UI displaying the artist to update because
    // it isn't listening to the model, we're only listening to it
    public void setArtist (String artist)
    {
        _artist = artist;
    }

    // this does cause the track display to update
    public void setTrackName (int index, String name)
    {
        _names[index] = name;
        fireTableCellUpdated(index, TITLE_COLUMN);
    }

    public String[] getTrackNames ()
    {
	return _names;
    }

    public void insertUpdate (DocumentEvent e)
    {
	updateText(e.getDocument());
    }

    public void removeUpdate (DocumentEvent e)
    {
	updateText(e.getDocument());
    }

    public void changedUpdate (DocumentEvent e)
    {
	updateText(e.getDocument());
    }

    protected void updateText (Document doc)
    {
	try {
	    if (doc.getProperty("name").equals("title")) {
		_title = doc.getText(0, doc.getLength());
	    } else if (doc.getProperty("name").equals("artist")) {
		_artist = doc.getText(0, doc.getLength());
	    }

	} catch (BadLocationException ble) {
	    Log.warning("Can't extract text from text field?!");
	    Log.logStackTrace(ble);
	}
    }

    public int getRowCount ()
    {
	return _names.length;
    }

    public int getColumnCount ()
    {
	return COLUMN_NAMES.length;
    }

    public Object getValueAt (int rowIndex, int columnIndex)
    {
	switch (columnIndex) {
	case TITLE_COLUMN:
	    return _names[rowIndex];

	default:
	case 0:
	    return new Integer(rowIndex+1);
	}
    }

    public String getColumnName (int column)
    {
	return COLUMN_NAMES[column];
    }

    public Class getColumnClass (int column)
    {
	return COLUMN_CLASSES[column];
    }

    public boolean isCellEditable (int row, int column)
    {
	return column == TITLE_COLUMN;
    }

    public void setValueAt (Object value, int row, int column)
    {
	if (column == TITLE_COLUMN) {
	    _names[row] = (String)value;
	    fireTableCellUpdated(row, column);
	}
    }

    protected Entry _entry;

    protected String _title;
    protected String _artist;
    protected String[] _names;

    protected static final String[] COLUMN_NAMES = { "T#", "Title" };
    protected static final Class[] COLUMN_CLASSES = {
	Integer.class, String.class };

    protected static final int TITLE_COLUMN = 1;
}
