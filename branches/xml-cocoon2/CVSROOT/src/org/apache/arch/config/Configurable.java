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
 *         (Betaversion Productions)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-27 01:19:57 $
 */
public interface Configurable {
    
    /**
     * Pass a <code>Configuration</code> instance to this
     * <code>Configurable</code> class. This method is always called after the
     * constructor and before any other method.
     *
     * @param conf The <code>Configuration</code> instance.
     * @exception ConfigurationException If the given <code>Configuration</code>
     *                                   is not valid or doesn't contain the
     *                                   proper configuration data.
     */
     public void setConfiguration(Configuration conf)
     throws ConfigurationException;
}