package org.apache.cocoon.processor.dcp;

/**
 * This exception it thrown by a DCP processor to signal something went
 * wrong during processing. 
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:14 $
 */

public class DCPException extends Exception {

    public DCPException(String message) {
        super(message);
    }
}