/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.arch;

/**
 * This interface should be implemented by those classes that
 * need to provide a service that requires some resources to be
 * initialized before being able to operate and properly destroyed
 * before termination and unloading.
 *
 * @author <a href="mailto:scoobie@betaversion.org">Federico Barbieri</a>
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.1 $ $Date: 1999-12-11 23:28:47 $
 */

public interface Service {
    
    /**
     * Initialize the service. This method is guaranteed to be called always
     * after methods in <code>Configurable</code> and <code>Component</code>, 
     * if the class implements those interfaces and before the run() method
     * if the class implements <code>Runnable</code>.
     */
    void init();
    
    /**
     * Destroys the service. This method is guaranteed to be called always
     * after the stop() method if this class implements <code>Stoppable</code>.
     */
    void destroy();
    
}