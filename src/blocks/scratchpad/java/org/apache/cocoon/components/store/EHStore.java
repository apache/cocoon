/*
 * Copyright 2004,2004 The Apache Software Foundation.
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

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.StoreJanitor;

/**
 * Store implementation based on EHCache.
 * (http://ehcache.sourceforge.net/)
 * 
 * <p>
 *  IMPORTANT:<br>
 *  (from http://ehcache.sourceforge.net/documentation/) 
 *  Persistence:
 *  The Disk Cache used by EHCache is not meant to be persistence mechanism. 
 *  The data file for each cache is deleted, if it exists, on startup.
 *  No data from a previous instance of an application is persisted through the disk cache. 
 *  The data file for each cache is also deleted on shutdown.
 * </p>
 */
public class EHStore extends AbstractLogEnabled 
implements Store, Parameterizable, Initializable, Disposable, ThreadSafe, Serviceable {
    
    private Cache cache;
    private CacheManager cacheManager;
    
    // configuration options
    private String cacheName;
    private int maximumSize;
    private boolean overflowToDisk;
    private String configFile;
    
    /** The service manager */
    private ServiceManager manager;
    
    /** The store janitor */
    private StoreJanitor storeJanitor;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager aManager) throws ServiceException {
        this.manager = aManager;
        this.storeJanitor = (StoreJanitor)this.manager.lookup(StoreJanitor.ROLE);
    }
    
    /**
     * Configure the store. The following options can be used:
     * <ul>
     *  <li><code>cache-name</code> (main) - When configuring multiple 
     *  EHStore intances you must specify a different name for each.</li>
     *  <li><code>maxobjects</code> (10000) - The maximum number of in-memory objects.</li>
     *  <li><code>overflow-to-disk</disk> (true) - Whether to spool elements to disk after
     *   maxobjects has been exceeded.
     *  <li><code>config-file</code> (org/apache/cocoon/components/store/ehcache-defaults.xml) -
     *   The default configuration file to use. This file is the only way to specify the path where
     *   the disk store puts its .cache files. The current default value is <code>java.io.tmp</code>.
     *   (On a standard Linux system this will be /tmp). Note that since the EHCache manager is
     *   a singleton object the value of this parameter will only have effect when this store is the
     *   first to create it. Configuring different stores with different values for this parameter
     *   will have no effect.
     * </ul>
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        this.cacheName = parameters.getParameter("cache-name", "main");
        this.maximumSize = parameters.getParameterAsInteger("maxobjects", 10000);
        this.overflowToDisk = parameters.getParameterAsBoolean("overflow-to-disk", true);
        this.configFile = parameters.getParameter("config-file", 
            "org/apache/cocoon/components/store/ehcache-defaults.xml");
    }
    
    /**
     * Initialize the CacheManager and created the Cache.
     */
    public void initialize() throws Exception {
        URL configFileURL = Thread.currentThread().getContextClassLoader().getResource(this.configFile);
        this.cacheManager = CacheManager.create(configFileURL);
        this.cache = new Cache(this.cacheName, this.maximumSize, this.overflowToDisk, true, 0, 0);
        this.cacheManager.addCache(this.cache);
        this.storeJanitor.register(this);
    }
    
    /**
     * Shutdown the CacheManager.
     */
    public void dispose() {
        if ( this.storeJanitor != null ) {
            this.storeJanitor.unregister(this);
            this.manager.release(this.storeJanitor);
            this.storeJanitor = null;
        }
        this.manager = null;
        this.cacheManager.shutdown();
        this.cacheManager = null;
        this.cache = null;
    }
    
    // ---------------------------------------------------- Store implementation
    
    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#free()
     */
    public Object get(Object key) {
        Object value = null;
        try {
            final Element element = this.cache.get((Serializable) key);
            if (element != null) {
                value = element.getValue();
            }
        }
        catch (CacheException e) {
            getLogger().error("Failure retrieving object from store", e);
        }
        if (getLogger().isDebugEnabled()) {
            if (value != null) {
                getLogger().debug("Found key: " + key);
            } 
            else {
                getLogger().debug("NOT Found key: " + key);
            }
        }
        return value;
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#free()
     */
    public void store(Object key, Object value) throws IOException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Store object " + value + " with key "+ key);
        }
        final Element element = new Element((Serializable) key, (Serializable) value);
        this.cache.put(element);
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#free()
     */
    public void free() {
        // FIXME - we have to implement this!
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#remove(java.lang.Object)
     */
    public void remove(Object key) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Removing item " + key);
        }
        this.cache.remove((Serializable) key);
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#clear()
     */
    public void clear() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Clearing the store");
        }
        try {
            this.cache.removeAll();
        }
        catch (IOException e) {
            getLogger().error("Failure to clearing store", e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        try {
            return this.cache.get((Serializable) key) != null;
        }
        catch (CacheException e) {
            getLogger().error("Failure retrieving object from store",e);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#keys()
     */
    public Enumeration keys() {
        return Collections.enumeration(this.cache.getKeys());
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#size()
     */
    public int size() {
        return this.cache.getSize();
    }

}
