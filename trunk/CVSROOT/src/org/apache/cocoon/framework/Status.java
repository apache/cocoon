package org.apache.cocoon.framework;

/**
 * This interface must be implemented by all those classes that are queried
 * for their status at runtime.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:18 $
 */

public interface Status {
    
    /**
     * Returns information about the status of the implementing class.
     * <b>Note</b>: this is use instead of the usual <code>toString()</code>
     * method because some of these methods are declared final in some
     * classes in JDK 1.1.
     */
    public String getStatus();
    
}