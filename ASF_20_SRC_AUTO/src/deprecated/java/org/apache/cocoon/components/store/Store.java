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

import org.apache.avalon.framework.component.Component;

import java.io.IOException;
import java.util.Enumeration;

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
 * @version CVS $Id: Store.java,v 1.2 2004/03/05 13:02:41 bdelacretaz Exp $
 */
public interface Store extends Component {

    String ROLE = "org.apache.cocoon.components.store.Store/Repository";

    String TRANSIENT_CACHE = "org.apache.cocoon.components.store.Store/TransientCache";
    String PERSISTENT_CACHE = "org.apache.cocoon.components.store.Store/PersistentCache";

    /**
     * Get the object associated to the given unique key.
     */
    Object get(Object key);

    /**
     * Store the given object in a persistent state. It is up to the
     * caller to ensure that the key has a persistent state across
     * different JVM executions.
     */
    void store(Object key, Object value) throws IOException;

    /**
     * Holds the given object in a volatile state. This means
     * the object store will discard held objects if the
     * virtual machine is restarted or some error happens.
     */
    void hold(Object key, Object value) throws IOException;

    void free();

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

    /**
     * Returns count of the objects in the store, or -1 if could not be
     * obtained.
     */
    int size();
}
