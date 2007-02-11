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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.util.IOUtils;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.StoreJanitor;
import org.apache.jcs.access.GroupCacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.memory.MemoryCache;


/**
 * This is the default store implementation based on JCS
 * http://jakarta.apache.org/turbine/jcs/BasicJCSConfiguration.html
 * 
 * @version CVS $Id: JCSDefaultStore.java,v 1.3 2004/05/20 10:48:16 cziegeler Exp $
 */
public class JCSDefaultStore 
    extends AbstractLogEnabled
    implements Store,
               Contextualizable,
               Parameterizable,
               Initializable,
               Disposable, 
               ThreadSafe,
               Serviceable {

    /** The JCS configuration properties */
    protected Properties properties;
    
    /** The JCS region name */
    protected String region;
    
    /** JCS Cache manager */
    private CompositeCacheManager cacheManager;
    
    /** The Java Cache System object */
    private JCSCacheAccess jcs;

    /** The location of the JCS default properties file */
    private static final String DEFAULT_PROPERTIES = "org/apache/cocoon/components/store/default.ccf";

    /** The context containing the work and the cache directory */
    private Context context;

    /** Service Manager */
    private ServiceManager manager;
    
    /** Store janitor */
    private StoreJanitor janitor;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context aContext) throws ContextException {
        this.context = aContext;
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager aManager) throws ServiceException {
        this.manager = aManager;
        this.janitor = (StoreJanitor)this.manager.lookup(StoreJanitor.ROLE);
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) 
    throws ParameterException {
        // TODO describe options
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
        
        int maxobjects = parameters.getParameterAsInteger("maxobjects", -1);
        if (maxobjects != -1) {
            String key = "jcs.region." + region + ".cacheattributes.MaxObjects";
            this.properties.setProperty(key, String.valueOf(maxobjects));
        }

        // get the directory to use
        try {
            final File workDir = (File) context.get(Constants.CONTEXT_WORK_DIR);
            if (parameters.getParameterAsBoolean("use-cache-directory", false)) {
                final File cacheDir = (File) context.get(Constants.CONTEXT_CACHE_DIR);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using cache directory: " + cacheDir);
                }
                setDirectory(cacheDir);
            } else if (parameters.getParameterAsBoolean("use-work-directory", false)) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using work directory: " + workDir);
                }
                setDirectory(workDir);
            } else if (parameters.getParameter("directory", null) != null) {
                String dir = parameters.getParameter("directory");
                dir = IOUtils.getContextFilePath(workDir.getPath(), dir);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using directory: " + dir);
                }
                setDirectory(new File(dir));
            } else {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Using default directory: " + workDir);
                }
                setDirectory(workDir);
            }
        } catch (ContextException ce) {
            throw new ParameterException("Unable to get directory information from context.", ce);
        } catch (IOException e) {
            throw new ParameterException("Unable to set directory", e);
        }
        
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        this.cacheManager = CompositeCacheManager.getUnconfiguredInstance();
        this.cacheManager.configure(this.properties);
        this.jcs = new JCSCacheAccess(cacheManager.getCache(region));
        this.janitor.register(this);
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if( this.janitor != null ) {
            this.janitor.unregister( this );
        }
        if ( this.jcs != null ) {
            this.jcs.dispose();
            this.jcs = null;
        }
        if ( this.cacheManager != null ) {
            this.cacheManager.release();
            this.cacheManager = null;            
        }
        this.properties = null;
        if ( this.manager != null ) {
            this.manager.release( this.janitor );
            this.janitor = null;
            this.manager = null;
        }
    }
    
    protected String getDefaultPropertiesFile() {
        return DEFAULT_PROPERTIES;
    }
    
    /**
     * Sets the disk cache location.
     */
    private void setDirectory(final File directory)
    throws IOException {

        /* Does directory exist? */
        if (!directory.exists()) {
            /* Create it anew */
            if (!directory.mkdirs()) {
                throw new IOException(
                "Error creating store directory '" + directory.getAbsolutePath() + "'. ");
            }
        }

        /* Is given file actually a directory? */
        if (!directory.isDirectory()) {
            throw new IOException("'" + directory.getAbsolutePath() + "' is not a directory");
        }

        /* Is directory readable and writable? */
        if (!(directory.canRead() && directory.canWrite())) {
            throw new IOException(
                "Directory '" + directory.getAbsolutePath() + "' is not readable/writable"
            );
        }
        
        this.properties.setProperty("jcs.auxiliary.DC.attributes.DiskPath",
                                    directory.getAbsolutePath());
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
        // TODO Find a better way
        MemoryCache memoryCache = this.cacheManager.getCache(region).getMemoryCache();
        Object[] keys = memoryCache.getKeyArray();
        if ( keys != null && keys.length > 0 ) {
            final Object key = keys[0];
            try {
                memoryCache.remove((Serializable)key);
            } catch (Exception ignore) {                
            }
        }
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
        // TODO Find a better way
        final MemoryCache memoryCache = this.cacheManager.getCache(region).getMemoryCache();
        final Object[] keys = memoryCache.getKeyArray();
        return new IteratorEnumeration(Arrays.asList(keys).iterator());
        //return new IteratorEnumeration(this.jcs.getGroupKeys("").iterator());
    }
    
    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#size()
     */
    public int size() {
        // TODO Find a better way
        MemoryCache memoryCache = this.cacheManager.getCache(region).getMemoryCache();
        return memoryCache.getSize();
        //return this.jcs.getSize();
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
