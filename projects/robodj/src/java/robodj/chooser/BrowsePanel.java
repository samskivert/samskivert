//
// $Id: BrowsePanel.java,v 1.1 2001/07/12 23:06:55 mdb Exp $

package robodj.chooser;

// import java.awt.*;
//  import java.awt.event.ActionEvent;
//  import java.awt.event.ActionListener;
import javax.swing.*;

//  import com.samskivert.swing.*;
import robodj.repository.*;

public class BrowsePanel extends JTabbedPane
{
    public BrowsePanel ()
    {
        EntryList elist;
        Category[] cats = Chooser.model.getCategories();

        // create a tab for each category
        for (int i = 0; i < cats.length; i++) {
            elist = new EntryList(cats[i].categoryid);
            String tip = "Browse entries in '" + cats[i].name + "' category.";
            addTab(cats[i].name, null, elist, tip);
        }

        // and add one for uncategorized entries
        elist = new EntryList(-1);
        addTab("Uncategorized", null, elist,
               "Browse uncategorized entries.");

        setSelectedIndex(0);
    }
}
