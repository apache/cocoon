/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.store;

import java.util.Enumeration;
import org.apache.avalon.Component;

/**
 *
 * @author <a href="mailto:scoobie@betaversion.org">Federico Barbieri</a>
 *         (Betaversion Productions)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.7 $ $Date: 2000-12-11 16:06:46 $
 */
public interface Store extends Component {

    /**
     * Get the object associated to the given unique key.
     */
    Object get(Object key);

    /**
     * Store the given object in a persistent state. It is up to the
     * caller to ensure that the key has a persistent state across
     * different JVM executions.
     */
    void store(Object key, Object value);

    /**
     * Holds the given object in a volatile state. This means
     * the object store will discard held objects if the
     * virtual machine is restarted or some error happens.
     */
    void hold(Object key, Object value);

    /**
     * Remove the object associated to the given key.
     */
    void remove(Object key);

    /**
     * Indicates if the given key is associated to a contained object.
     */
    boolean containsKey(Object key);

    /**
     * Returns the list of used keys as an Enumeration of Objects.
     */
    Enumeration keys();

}