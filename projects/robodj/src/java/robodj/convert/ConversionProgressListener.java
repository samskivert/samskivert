//
// $Id: ConversionProgressListener.java,v 1.1 2000/10/30 22:21:11 mdb Exp $

package robodj.convert;

/**
 * This interface is used to communicate ripping progress from a ripper or
 * encoder implementation to code using the conversion services (and
 * hopefully ultimately to the end user).
 */
public interface ConversionProgressListener
{
    /**
     * Informs the listener that the converter has completed the specified
     * percentage of the conversion process. The percentage should reflect
     * only the conversion being performed by the converter at that time
     * (ripping or converting a single track). The information provided
     * herein will be mapped to a global progress indication by the
     * calling software (in the case of converting an entire CD).
     */
    public void updateProgress (float percentComplete);
}
