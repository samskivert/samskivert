//
// $Id: ConvertException.java,v 1.1 2000/10/30 22:21:11 mdb Exp $

package robodj.convert;

/**
 * A convert exception can be thrown to indicate that some problem was
 * encountered during some part of the conversion process (ripping or
 * encoding).
 */
public class ConvertException extends Exception
{
    public ConvertException (String message)
    {
	super(message);
    }
}
