//
// $Id: BrowsePanel.java,v 1.2 2002/02/22 07:06:33 mdb Exp $

package robodj.chooser;

import javax.swing.*;

import robodj.repository.*;

public class BrowsePanel extends JTabbedPane
{
    public BrowsePanel ()
    {
        EntryList elist;
        Category[] cats = Chooser.model.getCategories();

        // create a tab for each category
        for (int i = 0; i < cats.length; i++) {
            elist = new CategoryEntryList(cats[i].categoryid);
            String tip = "Browse entries in '" + cats[i].name + "' category.";
            addTab(cats[i].name, null, elist, tip);
        }

        // and add one for uncategorized entries
        elist = new CategoryEntryList(-1);
        addTab("Uncategorized", null, elist,
               "Browse uncategorized entries.");

        setSelectedIndex(0);
    }
}
