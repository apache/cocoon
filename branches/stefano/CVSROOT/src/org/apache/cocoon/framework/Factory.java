package org.apache.cocoon.framework;

import java.util.*;

/**
 * A factory is responsible to create and properly initialize 
 * dynamically loaded classes. The use of dynamic linking allows
 * simpler management and stronger decoupling between the core
 * classes and the actors.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:18 $
 */

public interface Factory extends Actor {

    /**
     * Create the instance of a class given its name.
     */
    Object create(String name);

    /**
     * Create the instance of a class and, if configurable, use 
     * the given configurations to configure it.
     */
    Object create(String name, Configurations conf);

    /**
     * Create a vector of instances given a vector
     * of strings indicating their respective names.
     */
    Vector create(Vector names);
    
    /**
     * Create a vector of instances with given configurations.
     */
    Vector create(Vector names, Configurations conf);

}