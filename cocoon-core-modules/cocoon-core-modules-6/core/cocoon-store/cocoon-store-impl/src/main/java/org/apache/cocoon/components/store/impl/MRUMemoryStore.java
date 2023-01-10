/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.store.impl;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.StoreJanitor;

/**
 * This class provides a cache algorithm for the requested documents. It
 * combines a HashMap and a LinkedList to create a so called MRU (Most Recently
 * Used) cache.
 * 
 * Adapted from org.apache.excalibur.store.impl.MRUMemoryStore
 * 
 * @version $Id$
 */
public class MRUMemoryStore implements Store {
    private static final int MAX_OBJECTS = 100;

    /** By default we use the logger for this class. */
    private Log logger = LogFactory.getLog(getClass());

    private int maxObjects = MAX_OBJECTS;

    protected boolean persistent;

    private Store persistentStore;

    private StoreJanitor storeJanitor;

    private Hashtable cache;

    private LinkedList mrulist;


    public Log getLogger() {
        return this.logger;
    }

    public void setLogger(Log l) {
        this.logger = l;
    }

    /**
     * Indicates how many objects will be held in the cache. When the number of
     * maxobjects has been reached. The last object in the cache will be thrown
     * out. (Default: 100 objects)
     * 
     * @param maxobjects
     */
    public void setMaxObjects(int maxobjects) {
        this.maxObjects = maxobjects;
    }

    /**
     * @param persistent
     */
    public void setUsePersistentStore(boolean persistent) {
        this.persistent = persistent;
    }

    /**
     * Set to keep objects persisted after container shutdown
     * 
     * @param persistentStore
     */
    public void setPersistentStore(Store persistentStore) {
        this.persistentStore = persistentStore;
    }

    /**
     * @param storeJanitor
     */
    public void setStoreJanitor(StoreJanitor storeJanitor) {
        this.storeJanitor = storeJanitor;
    }

    /**
     * Initialize the MRUMemoryStore.
     * 
     * @throws Exception
     * @exception ParameterException
     */
    public void init() throws Exception {
        if (this.maxObjects < 1) {
            throw new Exception("MRUMemoryStore maxobjects must be at least 1!");
        }

        if (this.persistent && this.persistentStore == null) {
            throw new Exception("The persistent store must be set if usePersistentStore is required");
        }

        this.persistent = this.persistentStore != null;

        this.cache = new Hashtable((int) (this.maxObjects * 1.2));
        this.mrulist = new LinkedList();
        if (this.storeJanitor != null) {
            this.storeJanitor.register(this);
        }
    }

    /**
     * Dispose the component
     */
    public void destroy() {
        getLogger().debug("Destroying component!");

        if (this.storeJanitor != null) {
            this.storeJanitor.unregister(this);
        }

        // save all cache entries to filesystem
        if (this.persistent) {
            getLogger().debug("Final cache size: " + this.cache.size());
            Enumeration enumer = this.cache.keys();
            while (enumer.hasMoreElements()) {
                Object key = enumer.nextElement();
                if (key == null) {
                    continue;
                }
                try {
                    Object value = this.cache.remove(key);
                    if (checkSerializable(value)) {
                        this.persistentStore.store(key, value);
                    }
                } catch (IOException ioe) {
                    getLogger().error("Error in dispose()", ioe);
                }
            }
        }
    }

    /**
     * Store the given object in a persistent state. It is up to the caller to
     * ensure that the key has a persistent state across different JVM
     * executions.
     * 
     * @param key
     *            The key for the object to store
     * @param value
     *            The object to store
     */
    public synchronized void store(Object key, Object value) {
        hold(key, value);
    }

    /**
     * This method holds the requested object in a HashMap combined with a
     * LinkedList to create the MRU. It also stores objects onto the filesystem
     * if configured.
     * 
     * @param key
     *            The key of the object to be stored
     * @param value
     *            The object to be stored
     */
    public synchronized void hold(Object key, Object value) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Holding object in memory:");
            getLogger().debug("  key: " + key);
            getLogger().debug("  value: " + value);
        }
        /** ...first test if the max. objects in cache is reached... */
        while (this.mrulist.size() >= this.maxObjects) {
            /** ...ok, heapsize is reached, remove the last element... */
            free();
        }
        /** ..put the new object in the cache, on the top of course ... */
        this.cache.put(key, value);
        this.mrulist.remove(key);
        this.mrulist.addFirst(key);
    }

    /**
     * Get the object associated to the given unique key.
     * 
     * @param key
     *            The key of the requested object
     * @return the requested object
     */
    public synchronized Object get(Object key) {
        Object value = this.cache.get(key);
        if (value != null) {
            /** put the accessed key on top of the linked list */
            this.mrulist.remove(key);
            this.mrulist.addFirst(key);
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
            value = this.persistentStore.get(key);
            if (value != null) {
                try {
                    if (!this.cache.containsKey(key)) {
                        hold(key, value);
                    }
                    return value;
                } catch (Exception e) {
                    getLogger().error("Error in get()!", e);
                }
            }
        }
        return null;
    }

    /**
     * Remove the object associated to the given key.
     * 
     * @param key
     *            The key of to be removed object
     */
    public synchronized void remove(Object key) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Removing object from store");
            getLogger().debug("  key: " + key);
        }
        this.cache.remove(key);
        this.mrulist.remove(key);

        if (this.persistent && key != null) {
            this.persistentStore.remove(key);
        }
    }

    /**
     * Clear the Store of all elements
     */
    public synchronized void clear() {
        Enumeration enumer = this.cache.keys();
        while (enumer.hasMoreElements()) {
            Object key = enumer.nextElement();
            if (key == null) {
                continue;
            }
            remove(key);
        }
    }

    /**
     * Indicates if the given key is associated to a contained object.
     * 
     * @param key
     *            The key of the object
     * @return true if the key exists
     */
    public synchronized boolean containsKey(Object key) {
        if (this.persistent) {
            return this.cache.containsKey(key) || this.persistentStore.containsKey(key);
        } else {
            return this.cache.containsKey(key);
        }
    }

    /**
     * Returns the list of used keys as an Enumeration.
     * 
     * @return the enumeration of the cache
     */
    public synchronized Enumeration keys() {
        return this.cache.keys();
    }

    /**
     * Returns count of the objects in the store, or -1 if could not be
     * obtained.
     */
    public synchronized int size() {
        return this.cache.size();
    }

    /**
     * Frees some of the fast memory used by this store. It removes the last
     * element in the store.
     */
    public synchronized void free() {
        try {
            if (this.cache.size() > 0) {
                // This can throw NoSuchElementException
                Object key = this.mrulist.removeLast();
                Object value = this.cache.remove(key);
                if (value == null) {
                    getLogger().warn("Concurrency condition in free()");
                }

                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Freeing cache.");
                    getLogger().debug("  key: " + key);
                    getLogger().debug("  value: " + value);
                }

                if (this.persistent) {
                    // Swap object on fs.
                    if (checkSerializable(value)) {
                        try {
                            this.persistentStore.store(key, value);
                        } catch (Exception e) {
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
     * This method checks if an object is serializable.
     * 
     * @param object
     *            The object to be checked
     * @return true if the object is storeable
     */
    private boolean checkSerializable(Object object) {
        return object instanceof java.io.Serializable;
    }
}
