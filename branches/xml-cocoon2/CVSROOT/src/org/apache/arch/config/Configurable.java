/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.arch.config;

/**
 * This inteface should be implemented by classes that need to be
 * configured with custom parameters before initialization. 
 *
 * @author <a href="mailto:scoobie@betaversion.org">Federico Barbieri</a>
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.1 $ $Date: 1999-12-11 23:28:47 $
 */

public interface Configurable {
    
    /**
     * Pass the configurations to the configurable class. This method
     * is always called after the constructor and before any other
     * method.
     *
     * @param conf the class configurations.
     * @exception if a required Configuration is not found, this method
     * should throw an <code>IllegalArgumentException</code> indicating
     * the cause of such failure.
     */
     void setConfiguration(Configuration conf);
    
}