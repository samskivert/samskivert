//
// $Id: RipException.java,v 1.1 2000/10/30 21:08:53 mdb Exp $

package robodj.rip;

/**
 * A rip exception can be thrown to indicate that some problem was
 * encountered during some part of the ripping process.
 */
public class RipException extends Exception
{
    public RipException (String message)
    {
	super(message);
    }
}
