package org.apache.cocoon.framework;

/**
 * This exception is thrown when a Configurable object is initialized
 * with illegal parameters and cannot complete its initialization. 
 *
 * <p>When such exception is thrown, the object is not guaranteed
 * to be usable and the factory should behave accordingly.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:18 $
 */

public class InitializationException extends InstantiationException {
    
    public InitializationException() {
        super();
    }
    
    public InitializationException(String message) {
        super(message);
    }    
}