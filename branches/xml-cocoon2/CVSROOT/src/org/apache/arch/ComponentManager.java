/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.arch;

/**
 * A component manager holds the mapping between component roles and their
 * actual implementations. It's beyond the scope of this interface
 * to define how the mapping is created and managed, but only to
 * provide a way for composers to access the components they require.
 * 
 * @author <a href="mailto:scoobie@betaversion.org">Federico Barbieri</a>
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.1 $ $Date: 1999-12-11 23:28:46 $
 */

public interface ComponentManager {
    
    /**
     * Get the component associated with the given role.
     *
     * @exception IllegalStateException this exception is thrown
     * if there is no object associated to the requested role. This
     * signal is preferred instead of returning null since
     * this should occur rarely and only for design problems
     */
    Component getComponent(String role);
   
}