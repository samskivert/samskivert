//
// $Id: SearchPanel.java,v 1.3 2002/03/03 20:56:12 mdb Exp $

package robodj.chooser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import com.samskivert.swing.*;
import com.samskivert.util.StringUtil;

import robodj.repository.*;

public class SearchPanel extends JPanel
    implements ActionListener
{
    public SearchPanel ()
    {
	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        gl.setJustification(GroupLayout.TOP);
	setLayout(gl);

	// give ourselves a wee bit of a border
	setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(new JLabel("Enter a word to be matched against the " +
                       "artists, titles and songs:"), GroupLayout.FIXED);

        gl = new HGroupLayout(GroupLayout.STRETCH);
        JPanel qpanel = new JPanel(gl);
        _qbox = new JTextField();
        _qbox.addActionListener(this);
        _qbox.setActionCommand("query");
        qpanel.add(_qbox);

        JButton qbutton = new JButton("Query");
        qbutton.addActionListener(this);
        qbutton.setActionCommand("query");
        qpanel.add(qbutton, GroupLayout.FIXED);
        add(qpanel, GroupLayout.FIXED);
    }

    public void actionPerformed (ActionEvent e)
    {
        if (e.getActionCommand().equals("query")) {
            String text = _qbox.getText();
            if (StringUtil.blank(text) || text.equals(_query)) {
                // ignore empty text or repeat queries
                return;
            }

            // make a note of the query
            _query = text;

            // remove any previous query results
            if (_qlist != null) {
                remove(_qlist);
                _qlist = null;
            }

            // create and add a query entry list for the query
            _qlist = new QueryEntryList(_query);
            add(_qlist);

            // we need to revalidate ourselves and repaint because we've
            // added a component
            revalidate();
            repaint();
        }
    }

    /** The most recent query. */
    protected String _query;

    /** The query text box. */
    protected JTextField _qbox;

    /** The entry list that displays the results of our query. */
    protected QueryEntryList _qlist;
}
