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
package org.apache.cocoon.components.store.impl;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.excalibur.store.Store;
import org.apache.jcs.access.GroupCacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;

/**
 * This is the base class for a store implementation based on JCS.
 * For JCS Configuration details see:
 * http://jakarta.apache.org/turbine/jcs/BasicJCSConfiguration.html
 * 
 * @version CVS $Id: AbstractJCSStore.java,v 1.1 2004/05/17 14:02:50 cziegeler Exp $
 */
public abstract class AbstractJCSStore 
    extends AbstractLogEnabled
    implements Store, 
               Parameterizable,
               Initializable,
               Disposable, 
               ThreadSafe {
    
    /** The JCS configuration properties */
    protected Properties properties;
    
    /** The JCS region name */
    protected String region;
    
    /** JCS Cache manager */
    private CompositeCacheManager cacheManager;
    
    /** The Java Cache System object */
    private JCSCacheAccess jcs;
    
    /**
     * Override in sub classes
     */
    protected abstract String getDefaultPropertiesFile();
    
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) 
    throws ParameterException {
        
        this.region = parameters.getParameter("region-name","main");
        
        Properties defaults = new Properties();
        try {
            String defaultsFile = this.getDefaultPropertiesFile();
            if (defaultsFile != null) {
                defaults.load(Thread.currentThread().getContextClassLoader().
                    getResourceAsStream(defaultsFile));
            }
        } catch (IOException e) {
            throw new ParameterException("Failure loading cache defaults",e);
        }
        
        this.properties = new Properties(defaults);
        String[] names = parameters.getNames();
        for (int i = 0; i < names.length; i++) {
            if (names[i].startsWith("jcs.")) {
                this.properties.put(names[i], parameters.getParameter(names[i]));
            }
        }

    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        this.cacheManager = CompositeCacheManager.getUnconfiguredInstance();
        this.cacheManager.configure(this.properties);
        this.jcs = new JCSCacheAccess(cacheManager.getCache(region));
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.jcs != null ) {
            this.jcs.dispose();
            this.jcs = null;
        }
        if ( this.cacheManager != null ) {
            this.cacheManager.release();
            this.cacheManager = null;            
        }
        this.properties = null;
    }
    
    // ---------------------------------------------------- Store implementation
    
    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#get(java.lang.Object)
     */
    public Object get(Object key) {
        Object value = this.jcs.get(key);
        if (getLogger().isDebugEnabled()) {
            if (value != null) {
                getLogger().debug("Found key: " + key);
            } else {
                getLogger().debug("NOT Found key: " + key);
            }
        }
        
        return value;
    }
    
    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#store(java.lang.Object, java.lang.Object)
     */
    public void store(Object key, Object value)
    throws IOException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Store object " + value + " with key "+ key);
        }
        
        try {
            this.jcs.put(key, value);
        } catch (CacheException ce) {
            getLogger().error("Failure storing object ", ce);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#free()
     */
    public void free() {
        // TODO
    }
    
    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#clear()
     */
    public void clear() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Clearing the store");
        }
        
        try {
            this.jcs.remove();               
        } catch (CacheException ce) {
            getLogger().error("Failure clearing store", ce);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#remove(java.lang.Object)
     */
    public void remove(Object key) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Removing item " + key);
        }
        
        try {
           this.jcs.remove(key);
        } catch (CacheException ce) {
            getLogger().error("Failure removing object", ce);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return this.jcs.get(key) != null;
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#keys()
     */
    public Enumeration keys() {
      return new IteratorEnumeration(this.jcs.getGroupKeys("").iterator());
    }
    
    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#size()
     */
    public int size() {
        return this.jcs.getSize();
    }
    

    private static class JCSCacheAccess extends GroupCacheAccess {
        private JCSCacheAccess(CompositeCache cacheControl) {
            super(cacheControl);
        }
        
        private int getSize() {
            return super.cacheControl.getSize();
        }
        
        protected void dispose() {
            super.dispose();
        }
    }
}
