//
// $Id: ErrorUtil.java,v 1.1 2003/05/07 17:27:12 mdb Exp $

package robodj.util;

import javax.swing.JOptionPane;

/**
 * Contains a useful method for reporting errors.
 */
public class ErrorUtil
{
    /**
     * Reports an error, giving the details from the supplied (potentially
     * nested) exception. The user is given the choice of editing their
     * configuration or exiting the application.
     *
     * @return false if the user chose to edit the configuration, true if
     * they chose to exit.
     */
    public static boolean reportError (String error, Exception e)
    {
        String text = error;
        if (e != null) {
            text = text + "\n\n" + e.getMessage();
            if (e.getCause() != null) {
                text = text + "\n" + e.getCause().getMessage();
            }
        }
        Object[] options = { "Edit configuration", "Exit" };
        int choice = JOptionPane.showOptionDialog(
            null, text, "Error", JOptionPane.DEFAULT_OPTION,
            JOptionPane.ERROR_MESSAGE, null, options, options[0]);
        return (choice == 1 || choice == JOptionPane.CLOSED_OPTION);
    }
}
