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
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.store.Store;

/**
 * Store implementation based on EHCache.
 * (http://ehcache.sourceforge.net/)
 * 
 * TODO: CacheManager expects to be a singleton. So configuring
 * multiple EHStore intances could lead to errors.
 */
public class EHStore extends AbstractLogEnabled 
implements Store, Parameterizable, Initializable, Disposable, ThreadSafe {
    
    private Cache m_cache;
    private CacheManager m_cacheManager;
    
    private String m_cacheName;
    private int m_maximumSize;
    private boolean m_overflowToDisk;
    
    public EHStore() {
    }
    
    public void parameterize(Parameters parameters) throws ParameterException {
        m_cacheName = parameters.getParameter("cache-name","main");
        m_maximumSize = parameters.getParameterAsInteger("maxobjects",100);
        m_overflowToDisk = parameters.getParameterAsBoolean("overflow-to-disk",true);
    }
    
    public void initialize() throws Exception {
        m_cacheManager = CacheManager.create();
        m_cache = new Cache(m_cacheName,m_maximumSize,m_overflowToDisk,true,0,0);
        m_cacheManager.addCache(m_cache);
    }
    
    public void dispose() {
        m_cacheManager.shutdown();
        m_cacheManager = null;
        m_cache = null;
    }
    
    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#free()
     */
    public Object get(Object key) {
        Object value = null;
        try {
            final Element element = m_cache.get((Serializable) key);
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
        m_cache.put(element);
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#free()
     */
    public void free() {
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#remove(java.lang.Object)
     */
    public void remove(Object key) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Removing item " + key);
        }
        m_cache.remove((Serializable) key);
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#clear()
     */
    public void clear() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Clearing the store");
        }
        try {
            m_cache.removeAll();
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
            return m_cache.get((Serializable) key) != null;
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
        return Collections.enumeration(m_cache.getKeys());
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#size()
     */
    public int size() {
        return m_cache.getSize();
    }

}
