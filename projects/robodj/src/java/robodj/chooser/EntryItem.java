//
// $Id: EntryItem.java,v 1.1 2003/05/04 18:16:06 mdb Exp $

package robodj.chooser;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import com.samskivert.swing.HGroupLayout;

import robodj.repository.Entry;
import robodj.util.ButtonUtil;

/**
 * Displays an entry in the browser.
 */
public class EntryItem extends Item
{
    public static final String BROWSE = "entry:browse";

    public static final String PLAY = "entry:play";

    public EntryItem (Entry entry)
    {
        _entry = entry;

        // set up our layout manager
        HGroupLayout gl = new HGroupLayout(HGroupLayout.NONE);
        gl.setOffAxisPolicy(HGroupLayout.STRETCH);
        gl.setJustification(HGroupLayout.LEFT);
        setLayout(gl);

        // create a browse and a play button
        JButton button;

        // add a browse button
        button = ButtonUtil.createControlButton(
            BROWSE_ENTRY_TIP, BROWSE, _browseIcon, true);
        button.putClientProperty(ENTRY_PROP, entry);
        add(button, HGroupLayout.FIXED);

        // add a play all button
        button = ButtonUtil.createControlButton(
            PLAY_ENTRY_TIP, PLAY, _playIcon, true);
        button.putClientProperty(ENTRY_PROP, entry);
        add(button, HGroupLayout.FIXED);

        // add the entry title
        JLabel entryLabel = new JLabel(entry.title);
        entryLabel.setFont(_nameFont);
        entryLabel.setToolTipText(entry.title + " (" + entry.entryid + ")");
        add(entryLabel);
    }

    public static Entry getEntry (Object source)
    {
        return (Entry)((JButton)source).getClientProperty(ENTRY_PROP);
    }

    protected Entry _entry;

    protected static final String ENTRY_PROP = "entry";

    protected static final String BROWSE_ENTRY_TIP =
        "Browse the songs in this album";
    protected static final String PLAY_ENTRY_TIP =
        "Append this album to the playlist";

    protected static ImageIcon _browseIcon =
        ButtonUtil.getIcon(ICON_ROOT + "browse.png");
    }
