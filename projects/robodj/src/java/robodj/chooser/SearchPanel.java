//
// $Id: SearchPanel.java,v 1.1 2002/02/22 07:06:33 mdb Exp $

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
        }
    }

    /** The most recent query. */
    protected String _query;

    /** The query text box. */
    protected JTextField _qbox;

    /** The entry list that displays the results of our query. */
    protected QueryEntryList _qlist;
}
