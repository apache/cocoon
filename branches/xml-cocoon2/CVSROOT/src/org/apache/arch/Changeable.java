/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
 
package org.apache.arch;

/**
 * This interface is implemented by those classes that change
 * their behavior/results over time for the given context (non-ergodic).
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.1 $ $Date: 1999-12-11 23:28:46 $
 */

public interface Changeable {
    
    /**
     * Queries the class to estimate its ergodic period termination.
     * 
     * Returns true if the class ergodic period is over and the 
     * class would behave differently if processed again, false if
     * the resource is still ergodic so that it doesn't require
     * reprocessing.
     *
     * This method is called to ensure the validity of a cached 
     * product. It is the producer responsibility to provide the 
     * fastest possible implementation of this method or, whether 
     * this is not possible and the costs of the change evaluation is
     * comparable to the production costs, to return
     * true directly with no further delay, thus reducing
     * the evaluation overhead to a minimum.
     * 
     * This method is guaranteed to be called after at least
     * a single call to any production methods.
     */
    boolean hasChanged(Object context);
    
}