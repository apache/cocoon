package org.apache.cocoon.store;

import java.io.*;
import java.util.*;
import org.apache.cocoon.framework.*;

/**
 * This is the interface that a generic object storage system must
 * implement.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:12 $
 */

public interface Store extends Actor {

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
     * Returns the list of used keys.
     */
    Enumeration list();
    
}