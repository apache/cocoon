package org.apache.cocoon.processor;

/**
 * This exception it thrown by a DOM processor to signal something went
 * wrong during processing.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:13 $
 */

public class ProcessorException extends Exception {

    public ProcessorException() {
        super();
    }

    // ???: should we add some direct DOM node for exception evaluation? (SM)
    public ProcessorException(String message) {
        super(message);
    }
}