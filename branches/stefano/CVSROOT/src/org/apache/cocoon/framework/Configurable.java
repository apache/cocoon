package org.apache.cocoon.framework;

/**
 * This interface must be implemented by all those classes that need 
 * parameters to specify their global behavior during their initialization.
 * 
 * <p>Every class must implement this interface and have empty contructor 
 * methods instead of relying on Reflection for configuration.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:18 $
 */

public interface Configurable {
    
    /**
     * Initialize the class by passing its configurations.
     */
    public void init(Configurations conf) throws InitializationException;
    
}