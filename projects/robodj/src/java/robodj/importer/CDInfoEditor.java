//
// $Id: CDInfoEditor.java,v 1.1 2000/12/10 07:02:09 mdb Exp $

package robodj.importer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

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
	String[] names = new String[details.length];
	for (int i = 0; i < details.length; i++) {
	    names[i] = details[i].title;
	}

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
}
