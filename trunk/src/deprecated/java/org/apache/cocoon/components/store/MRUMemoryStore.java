/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.store;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.MRUBucketMap;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Map;

/**
 * This class provides a cache algorithm for the requested documents.
 * It combines a HashMap and a LinkedList to create a so called MRU
 * (Most Recently Used) cache.
 *
 * <p>This implementation is based on MRUBucketMap - map with efficient
 * O(1) implementation of MRU removal policy.
 *
 * <p>TODO: Port improvments to the Excalibur implementation
 *
 * @deprecated Use the Avalon Excalibur Store instead.
 *
 * @author <a href="mailto:g-froehlich@gmx.de">Gerhard Froehlich</a>
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: MRUMemoryStore.java,v 1.3 2003/04/27 16:41:31 vgritsenko Exp $
 */
public final class MRUMemoryStore extends AbstractLogEnabled
    implements Store, Parameterizable, Composable, Disposable, ThreadSafe {

    private int maxobjects;
    private boolean persistent;
    private MRUBucketMap cache;
    private Store persistentStore;
    private StoreJanitor storeJanitor;
    private ComponentManager manager;

    /**
     * Get components of the ComponentManager
     *
     * @param manager The ComponentManager
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Looking up " + Store.PERSISTENT_CACHE);
            getLogger().debug("Looking up " + StoreJanitor.ROLE);
        }
        this.persistentStore = (Store)manager.lookup(Store.PERSISTENT_CACHE);
        this.storeJanitor = (StoreJanitor)manager.lookup(StoreJanitor.ROLE);
    }

    /**
     * Initialize the MRUMemoryStore.
     * A few options can be used:
     * <UL>
     *  <LI>maxobjects: Maximum number of objects stored in memory (Default: 100 objects)</LI>
     *  <LI>use-persistent-cache: Use persistent cache to keep objects persisted after
     *      container shutdown or not (Default: false)</LI>
     * </UL>
     *
     * @param params Store parameters
     * @exception ParameterException
     */
    public void parameterize(Parameters params) throws ParameterException {
        this.maxobjects = params.getParameterAsInteger("maxobjects", 100);
        this.persistent = params.getParameterAsBoolean("use-persistent-cache", false);
        if ((this.maxobjects < 1)) {
            throw new ParameterException("MRUMemoryStore maxobjects must be at least 1!");
        }

        this.cache = new MRUBucketMap((int)(this.maxobjects * 1.2));
        this.storeJanitor.register(this);
    }

    /**
     * Dispose the component
     */
    public void dispose() {
        if (this.manager != null) {
            getLogger().debug("Disposing component!");

            if (this.storeJanitor != null)
                this.storeJanitor.unregister(this);
            this.manager.release(this.storeJanitor);
            this.storeJanitor = null;

            // save all cache entries to filesystem
            if (this.persistent) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Final cache size: " + this.cache.size());
                }
                for (Iterator i = this.cache.keySet().iterator(); i.hasNext(); ) {
                    Object key = i.next();
                    try {
                        Object value = this.cache.remove(key);
                        if(checkSerializable(value)) {
                             persistentStore.store(getFileName(key.toString()),
                                                   value);
                        }
                    } catch (IOException ioe) {
                        getLogger().error("Error in dispose()", ioe);
                    }
                }
            }
            this.manager.release(this.persistentStore);
            this.persistentStore = null;
        }

        this.manager = null;
    }

    /**
     * Store the given object in a persistent state. It is up to the
     * caller to ensure that the key has a persistent state across
     * different JVM executions.
     *
     * @param key The key for the object to store
     * @param value The object to store
     */
    public void store(Object key, Object value) {
        this.hold(key,value);
    }

    /**
     * This method holds the requested object in a HashMap combined
     * with a LinkedList to create the MRU.
     * It also stores objects onto the filesystem if configured.
     *
     * @param key The key of the object to be stored
     * @param value The object to be stored
     */
    public void hold(Object key, Object value) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Holding object in memory:");
            getLogger().debug("  key: " + key);
            getLogger().debug("  value: " + value);
        }

        /** ...first test if the max. objects in cache is reached... */
        while (this.cache.size() >= this.maxobjects) {
            /** ...ok, heapsize is reached, remove the last element... */
            this.free();
        }

        /** ..put the new object in the cache, on the top of course ... */
        this.cache.put(key, value);
    }

    /**
     * Get the object associated to the given unique key.
     *
     * @param key The key of the requested object
     * @return the requested object
     */
    public Object get(Object key) {
        Object value = this.cache.get(key);
        if (value != null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Found key: " + key.toString());
            }
            return value;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("NOT Found key: " + key.toString());
        }

        /** try to fetch from filesystem */
        if (this.persistent) {
            value = this.persistentStore.get(getFileName(key.toString()));
            if (value != null) {
                try {
                    this.hold(key, value);
                    return value;
                } catch (Exception e) {
                    getLogger().error("Error in hold()!", e);
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Remove the object associated to the given key.
     *
     * @param key The key of to be removed object
     */
    public void remove(Object key) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Removing object from store");
            getLogger().debug("  key: " + key);
        }
        this.cache.remove(key);
        if(this.persistent && key != null) {
            this.persistentStore.remove(getFileName(key.toString()));
        }
    }

    /**
     * Indicates if the given key is associated to a contained object.
     *
     * @param key The key of the object
     * @return true if the key exists
     */
    public boolean containsKey(Object key) {
        return cache.containsKey(key) || (persistent && persistentStore.containsKey(key));
    }

    /**
     * Returns the list of used keys as an Enumeration.
     *
     * @return the enumeration of the cache
     */
    public Enumeration keys() {
        return new Enumeration() {
            private Iterator i = cache.keySet().iterator();

            public boolean hasMoreElements() {
                return i.hasNext();
            }

            public Object nextElement() {
                return i.next();
            }
        };
    }

    /**
     * Returns count of the objects in the store, or -1 if could not be
     * obtained.
     */
    public int size() {
        return this.cache.size();
    }

    /**
     * Frees some of the fast memory used by this store.
     * It removes the last element in the store.
     */
    public void free() {
        try {
            if (this.cache.size() > 0) {
                // This can throw NoSuchElementException
                Map.Entry node = this.cache.removeLast();
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Freeing cache.");
                    getLogger().debug("  key: " + node.getKey());
                    getLogger().debug("  value: " + node.getValue());
                }

                if (this.persistent) {
                    // Swap object on fs.
                    if(checkSerializable(node.getValue())) {
                        try {
                            this.persistentStore.store(
                                getFileName(node.getKey().toString()), node.getValue());
                        } catch(Exception e) {
                            getLogger().error("Error storing object on fs", e);
                        }
                    }
                }
            }
        } catch (NoSuchElementException e) {
            getLogger().warn("Concurrency error in free()", e);
        } catch (Exception e) {
            getLogger().error("Error in free()", e);
        }
    }

    /**
     * This method checks if an object is seriazable.
     *
     * @param object The object to be checked
     * @return true if the object is storeable
     */
    private boolean checkSerializable(Object object) {

        if (object == null) return false;

        try {
            String clazz = object.getClass().getName();
            if((clazz.equals("org.apache.cocoon.caching.CachedEventObject"))
              || (clazz.equals("org.apache.cocoon.caching.CachedStreamObject"))
              || (ClassUtils.implementsInterface(clazz, "org.apache.cocoon.caching.CacheValidity"))) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            getLogger().error("Error in checkSerializable()!", e);
            return false;
        }
    }

    /**
     * This method puts together a filename for
     * the object, which shall be stored on the
     * filesystem.
     *
     * @param key The key of the object
     * @return the filename of the key
     */
    private String getFileName(String key) {
        return URLEncoder.encode(key.toString());
    }
}
