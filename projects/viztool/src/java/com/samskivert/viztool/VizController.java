//
// $Id: VizController.java,v 1.1 2001/08/14 00:45:56 mdb Exp $

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
            // tell the job to print our visualizer with the format we set
            // up earlier
            _job.setPrintable(_vpanel.getVisualizer(), _format);

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
