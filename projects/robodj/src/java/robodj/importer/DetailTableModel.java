//
// $Id: DetailTableModel.java,v 1.4 2004/01/07 01:57:10 mdb Exp $

package robodj.importer;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;

import com.samskivert.net.cddb.CDDB;
import robodj.Log;

public class DetailTableModel
    extends AbstractTableModel
    implements DocumentListener
{
    public DetailTableModel (CDDB.Detail[] details, int numTracks)
    {
	_details = details;
	_numTracks = numTracks;
	_names = new String[_numTracks];

	// copy in the names from the first detail record (or empty
	// strings if there is no first detail record)
	copyInfo(0);
    }

    public void selectDetail (int index)
    {
	// copy the track names from the specified detail record into the
	// names array
	copyInfo(index);

	// fire a data changed event to let the table know what's up
	fireTableDataChanged();
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

    protected void copyInfo (int index)
    {
	if (_details.length > index) {
	    // extract the artist and title from the combined title
	    // provided by the detail record
	    String src = _details[index].title;
	    int sidx = src.indexOf("/");
	    if (sidx != -1) {
		_artist = src.substring(0, sidx).trim();
		_title = src.substring(sidx+1).trim();
	    } else {
		_title = src;
		_artist = "Unknown";
	    }

	    for (int i = 0; i < _names.length; i++) {
		_names[i] = _details[index].trackNames[i];
	    }

	} else {
	    _title = "Unknown";
	    _artist = "Unknown";

	    for (int i = 0; i < _names.length; i++) {
		_names[i] = "Track " + (i+1);
	    }
	}
    }

    public int getRowCount ()
    {
	return _numTracks;
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

    protected CDDB.Detail[] _details;
    protected String _title;
    protected String _artist;

    protected String[] _names;
    protected int _numTracks;

    protected static final String[] COLUMN_NAMES = { "T#", "Title" };
    protected static final Class[] COLUMN_CLASSES = {
	Integer.class, String.class };

    protected static final int TITLE_COLUMN = 1;
}
