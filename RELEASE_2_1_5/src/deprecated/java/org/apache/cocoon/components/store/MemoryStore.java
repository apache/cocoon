/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.store;

import org.apache.avalon.framework.thread.ThreadSafe;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @deprecated Use the Avalon Excalibur Store instead.
 *
 * @author <a href="mailto:scoobie@betaversion.org">Federico Barbieri</a>
 *         (Betaversion Productions)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @version CVS $Id: MemoryStore.java,v 1.2 2004/03/05 13:02:41 bdelacretaz Exp $
 */
public class MemoryStore implements Store, ThreadSafe {
    /* WARNING: Hashtable is threadsafe, whereas HashMap is not.
     * Should we move this class over to the Collections API,
     * use Collections.synchronizedMap(Map map) to ensure
     * accesses are synchronized.
     */

    /** The shared store */
    private Hashtable table = new Hashtable();

    /**
     * Get the object associated to the given unique key.
     */
    public synchronized Object get(Object key) {
        return(table.get(key));
    }

    /**
     * Store the given object in a persistent state. It is up to the
     * caller to ensure that the key has a persistent state across
     * different JVM executions.
     */
    public synchronized void store(Object key, Object value) {
        this.hold(key,value);
    }

    /**
     * Holds the given object in a volatile state. This means
     * the object store will discard held objects if the
     * virtual machine is restarted or some error happens.
     */
    public synchronized void hold(Object key, Object value) {
        table.put(key,value);
    }

    /**
     * Remove the object associated to the given key.
     */
    public synchronized void remove(Object key) {
        table.remove(key);
    }

    public synchronized void free() {}

    /**
     * Indicates if the given key is associated to a contained object.
     */
    public synchronized boolean containsKey(Object key) {
        return(table.containsKey(key));
    }

    /**
     * Returns the list of used keys as an Enumeration of Objects.
     */
    public synchronized Enumeration keys() {
        return(table.keys());
    }

    /**
     * Returns count of the objects in the store, or -1 if could not be
     * obtained.
     */
    public synchronized int size()
    {
        return table.size();
    }
}
