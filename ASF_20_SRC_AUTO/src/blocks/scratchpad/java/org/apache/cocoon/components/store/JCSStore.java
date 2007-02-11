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
import java.util.Enumeration;
import java.util.Properties;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.impl.AbstractReadWriteStore;
import org.apache.jcs.access.GroupCacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;

/**
 * TODO - This store implementation should be moved to excalibur store
 * 
 * The JCS Configuration file - for details see: 
 * http://jakarta.apache.org/turbine/jcs/BasicJCSConfiguration.html
 * 
 * @author <a href="mailto:cmoss@tvnz.co.nz">Corin Moss</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 */
public class JCSStore extends AbstractReadWriteStore
    implements Store, Serviceable, Parameterizable, Initializable, Disposable, ThreadSafe {
    
    /** The JCS configuration properties */
    private Properties m_properties;
    
    /** The Region as used by JCS*/
    private String m_region;
    
    /** The group name as used by JCS getGroupKeys*/
    private String m_group;
    
    /** JCS Cache manager */
    private CompositeCacheManager m_cacheManager;
    
    /** The Java Cache System object */
    private JCSCacheAccess m_JCS;
    
    /** Access to the SourceResolver */
    private ServiceManager m_manager;
    
    
    // ---------------------------------------------------- Lifecycle
    
    public JCSStore() {
    }
    
    public void service(ServiceManager manager) throws ServiceException {
        m_manager = manager;
    }
    
    /**
     *  Configure the Component.<br>
     *  A few options can be used
     *  <ul>
     *    <li>
     *      <code>config-file</code>: the name of the file which specifies 
     *       the configuration parameters
     *    </li>
     *    <li>
     *      <code>region-name<code>: the region to be used as defined in the config file
     *    </li>
     *    <li>
     *      <code>group-name</code>: the group to be used as defined in the config file
     *    </li>
     *  </ul>
     *
     * @param params the configuration paramters
     * @exception  ParameterException
     */
    public void parameterize(Parameters params) throws ParameterException {
        // TODO - These are only values for testing:
        String configFile = params.getParameter("config-file", "context://WEB-INF/cache.ccf");
        m_region = params.getParameter("region-name", "indexedRegion1");
        m_group = params.getParameter("group-name", "indexedDiskCache");

        if (this.getLogger().isDebugEnabled()) {
            getLogger().debug("CEM Loading config: '" + configFile + "'");
            getLogger().debug("CEM Loading region: '" + m_region + "'");
            getLogger().debug("CEM Loading group: '" + m_group + "'");
        }
        
        Source source = null;
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver) m_manager.lookup(SourceResolver.ROLE);
            source = resolver.resolveURI(configFile);
            if (!source.exists()) {
                throw new ParameterException("No config file at configured location: " + configFile);
            }
            m_properties = new Properties();
            m_properties.load(source.getInputStream());
        }
        catch (IOException e) {
            throw new ParameterException("Failed to load JCS properties file",e);
        }
        catch (ServiceException e) {
            throw new ParameterException("Missing service dependency: SourceResolver",e);
        }
        finally {
            if (resolver != null && source != null) {
                resolver.release(source);
            }
        }
    }
    
    public void initialize() throws Exception {
        
        m_cacheManager = CompositeCacheManager.getUnconfiguredInstance();
        m_cacheManager.configure(m_properties);
        m_JCS = new JCSCacheAccess(m_cacheManager.getCache(m_region));
        
    }

    public void dispose() {
        // FIXME: how to actually persist the stored items?
        m_JCS.save();
        m_cacheManager.freeCache(m_region);
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
     *  Store the given object in the indexed data file.
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
            getLogger().debug("store(): Store file with key: "
                + key.toString());
            getLogger().debug("store(): Store file with value: "
                + value.toString());
        }
        
        //This test is not really pertinent here - we
        //won't always be storing serializable objects (I don't think)
        if (value instanceof Serializable) 
        {
            try 
            {
                m_JCS.put(key, value);
            } 
            catch (CacheException ce) 
            {
                getLogger().error("store(..): Exception", ce);
            }
        } 
        else 
        {
            throw new IOException("Object not Serializable");
        }
    }
    
    /**
     * Frees some values of the data file.<br>
     * TODO: implementation
     */
    public void free() 
    {
        // if we ever implement this, we should implement doFree()
    }
    
    /* (non-Javadoc)
     * @see org.apache.excalibur.store.impl.AbstractReadWriteStore#doFree()
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
            getLogger().debug("clear(): Clearing the database ");
        }
        
        try 
        {
            //No args - should remove all
            //This is not well documented, although the source
            //suggests that this will work
            m_JCS.remove();               
        } 
        catch (CacheException ce) 
        {
            getLogger().error("store(..): Exception", ce);
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
            getLogger().debug("remove(..) Remove item");
        }
        
        try 
        {
           m_JCS.remove(key); 
        } 
         //Need to revisit this exception - what happens
         //if no match found for key  - is an exception thrown?
        catch (CacheException ce) 
        {
            getLogger().error("remove(..): Exception", ce);
        }
    }
    
    /**
     *  Test if the the index file contains the given key
     *
     * @param key the key object
     * @return true if Key exists and false if not
     */
    protected boolean doContainsKey(Object key) 
    {
         
        //All we have available is a null check
        if (m_JCS.get(key) != null) {
            return true;
        } 
        else {
            return false;
        }
    }
    
    
     /**
     * Returns an Enumeration of all Keys in the cache.<br>
     * this is a bit of a hack - I don't believe that the group
     * needs to be passed in as a string in this way - we should
     * be able to retreive it.
     * FIX ME!!
     *
     * @return  Enumeration Object with all existing keys
     */
    protected Enumeration doGetKeys() 
    {
      return new org.apache.commons.collections.iterators.IteratorEnumeration(
         this.m_JCS.getGroupKeys(this.m_group).iterator()
      );
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
    }
}
