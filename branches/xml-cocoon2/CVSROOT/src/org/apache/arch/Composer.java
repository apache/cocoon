/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.arch;

/**
 * A composer is a class that need to connect to software components using 
 * a "role" abstraction, thus not depending on particular implementations 
 * but on behavioral interfaces.
 * 
 * @author <a href="mailto:scoobie@betaversion.org">Federico Barbieri</a>
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.1 $ $Date: 1999-12-11 23:28:46 $
 */

public interface Composer {
    
    /**
     * Pass the component manager to the composer. The composer implementation
     * should use this method to acquire the components it needs for
     * execution.
     *
     * @param manager the component manager to which this composer can 
     * request the needed components.
     * @exception if a required Component is not found, this method
     * should throw an <code>IllegalStateException</code> indicating
     * the cause of such failure.
     */
    public void setComponentManager(ComponentManager manager);
    
}