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
import java.util.Enumeration;
import java.util.Properties;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.impl.AbstractReadWriteStore;
import org.apache.jcs.access.GroupCacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;

/**
 * For JCS Configuration details see:
 * http://jakarta.apache.org/turbine/jcs/BasicJCSConfiguration.html
 * 
 * @author <a href="mailto:cmoss@tvnz.co.nz">Corin Moss</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 */
public abstract class AbstractJCSStore extends AbstractReadWriteStore
    implements Store, Parameterizable, Initializable, Disposable, ThreadSafe {
    
    /** The JCS configuration properties */
    protected Properties m_properties;
    
    /** The JCS region name */
    protected String m_region;
    
    /** JCS Cache manager */
    private CompositeCacheManager m_cacheManager;
    
    /** The Java Cache System object */
    private JCSCacheAccess m_JCS;
    
    
    // ---------------------------------------------------- Lifecycle
    
    public AbstractJCSStore() {
    }
    
    public void parameterize(Parameters parameters) throws ParameterException {
        
        m_region = parameters.getParameter("region-name","main");
        
        Properties defaults = new Properties();
        try {
            String defaultsFile = getDefaultPropertiesFile();
            if (defaultsFile != null) {
                defaults.load(Thread.currentThread().getContextClassLoader().
                    getResourceAsStream(defaultsFile));
            }
        }
        catch (IOException e) {
            throw new ParameterException("Failure loading cache defaults",e);
        }
        
        m_properties = new Properties(defaults);
        String[] names = parameters.getNames();
        for (int i = 0; i < names.length; i++) {
            if (names[i].startsWith("jcs.")) {
                m_properties.put(names[i], parameters.getParameter(names[i]));
            }
        }

    }
    
    protected String getDefaultPropertiesFile() {
        return null;
    }
    
    public void initialize() throws Exception {
        m_cacheManager = CompositeCacheManager.getUnconfiguredInstance();
        m_cacheManager.configure(m_properties);
        m_JCS = new JCSCacheAccess(m_cacheManager.getCache(m_region));
    }
    
    public void dispose() {
        m_JCS.dispose();
        m_cacheManager.release();
        m_JCS = null;
        m_cacheManager = null;
        m_properties = null;
    }
    
    // ---------------------------------------------------- Store implementation
    
    /**
     * Returns a Object from the store associated with the Key Object
     *
     * @param key the Key object
     * @return the Object associated with Key Object
     */
    protected Object doGet(Object key) 
    {
        Object value = null;
        
        value = m_JCS.get(key);
        if (getLogger().isDebugEnabled()) 
        {
            if (value != null) 
            {
                getLogger().debug("Found key: " + key);
            } 
            else 
            {
                getLogger().debug("NOT Found key: " + key);
            }
        }
        
        return value;
    }
    
    /**
     * Store the given object.
     *
     * @param key the key object
     * @param value the value object
     * @exception  IOException
     */
    protected void doStore(Object key, Object value)
        throws IOException 
    {
        
        if (getLogger().isDebugEnabled()) 
        {
            getLogger().debug("Store object " + value + " with key "+ key);
        }
        
        try 
        {
            m_JCS.put(key, value);
        } 
        catch (CacheException ce) 
        {
            getLogger().error("Failure storing object ", ce);
        }
    }
    
    /**
     * Frees some values of the store.
     * TODO: implementation?
     */
    protected void doFree() {
    }
    
    /**
     * Clear the Store of all elements
     */
    protected void doClear() 
    {
        
        if (getLogger().isDebugEnabled()) 
        {
            getLogger().debug("Clearing the store");
        }
        
        try
        {
            m_JCS.remove();               
        } 
        catch (CacheException ce) 
        {
            getLogger().error("Failure clearing store", ce);
        }
    }
    
    /**
     * Removes a value from the data file with the given key.
     *
     * @param key the key object
     */
    protected void doRemove(Object key)
    {
        if (getLogger().isDebugEnabled()) 
        {
            getLogger().debug("Removing item " + key);
        }
        
        try 
        {
           m_JCS.remove(key);
        }
        catch (CacheException ce)
        {
            getLogger().error("Failure removing object", ce);
        }
    }
    
    /**
     * Test if the store contains the element.
     *
     * @param key the key object
     * @return true if Key exists and false if not
     */
    protected boolean doContainsKey(Object key) 
    {
        return m_JCS.get(key) != null;
    }
    
    
    /**
     * Return all existing keys.
     */
    protected Enumeration doGetKeys() 
    {
      return new IteratorEnumeration(this.m_JCS.getGroupKeys("").iterator());
    }
    
    protected int doGetSize() 
    {
        return m_JCS.getSize();
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
