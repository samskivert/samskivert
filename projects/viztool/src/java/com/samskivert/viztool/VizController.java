//
// $Id: VizController.java,v 1.4 2001/12/01 05:28:01 mdb Exp $
// 
// viztool - a tool for visualizing collections of java classes
// Copyright (C) 2001 Michael Bayne
// 
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation; either version 2.1 of the License, or (at your
// option) any later version.
// 
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.viztool;

import java.awt.event.ActionEvent;
import java.awt.print.*;

import com.samskivert.swing.*;

/**
 * The viz controller manages the user interface and effects actions that
 * are requested by the user (like moving forward or backward a page).
 */
public class VizController extends Controller
{
    /** The action command for moving forward one page. */
    public static final String FORWARD_PAGE = "forward_page";

    /** The action command for moving backward one page. */
    public static final String BACKWARD_PAGE = "backward_page";

    /** The action command for printing. */
    public static final String PRINT = "print";

    /** The action command for quitting. */
    public static final String QUIT = "quit";

    public VizController (VizPanel vpanel)
    {
        _vpanel = vpanel;

        // create a print job in case we need to print
        _job = PrinterJob.getPrinterJob();
        _format = _job.defaultPage();

        // use sensible margins
        Paper paper = new Paper();
        paper.setImageableArea(
            LEFT_MARGIN, TOP_MARGIN, PAGE_WIDTH, PAGE_HEIGHT);
        _format.setPaper(paper);
    }

    public boolean handleAction (ActionEvent action)
    {
        String cmd = action.getActionCommand();

        if (cmd.equals(FORWARD_PAGE)) {
            int pno = _vpanel.getPage();
            if (pno < _vpanel.getPageCount()-1) {
                _vpanel.setPage(pno+1);
            }
            return true;

        } else if (cmd.equals(BACKWARD_PAGE)) {
            int pno = _vpanel.getPage();
            if (pno > 0) {
                _vpanel.setPage(pno-1);
            }
            return true;

        } else if (cmd.equals(PRINT)) {
            // create a pageable to be used by our print job that does the
            // right thing
            Pageable pable = new Pageable() {
                public int getNumberOfPages () {
                    return _vpanel.getVisualizer().getPageCount();
                }

                public PageFormat getPageFormat (int pageIndex) {
                    return _format;
                }

                public Printable getPrintable (int pageIndex) {
                    return _vpanel.getVisualizer();
                }
            };
            _job.setPageable(pable);

            // pop up a dialog to control printing
            if (_job.printDialog()) {
                try {
                    // invoke the printing process
                    _job.print();
                } catch (PrinterException pe) {
                    pe.printStackTrace(System.err);
                }
            }
            return true;

        } else if (cmd.equals(QUIT)) {
            System.exit(0);
        }

        return false;
    }

    protected VizPanel _vpanel;
    protected PrinterJob _job;
    protected PageFormat _format;

    // these should be configurable...
    protected static final double LEFT_MARGIN = 72*0.5;
    protected static final double TOP_MARGIN = 72*0.5;
    protected static final double PAGE_WIDTH = 72*7.5;
    protected static final double PAGE_HEIGHT = 72*10;
}
