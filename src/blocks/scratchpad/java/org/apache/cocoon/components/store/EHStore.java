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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.cocoon.Constants;
import org.apache.cocoon.util.IOUtils;

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
import org.apache.excalibur.store.Store;
import org.apache.excalibur.store.StoreJanitor;

/**
 * Store implementation based on EHCache.
 * (http://ehcache.sourceforge.net/)
 */
public class EHStore extends AbstractLogEnabled 
implements Store, Contextualizable, Serviceable, Parameterizable, Initializable, Disposable, ThreadSafe {

    // ---------------------------------------------------- Constants

    private static final String CONFIG_FILE = "org/apache/cocoon/components/store/ehcache.xml";

    private static int instanceCount = 0;

    // ---------------------------------------------------- Instance variables

    private Cache cache;
    private CacheManager cacheManager;

    private final String cacheName;

    // configuration options
    private int maxObjects;
    private boolean overflowToDisk;

    /** The service manager */
    private ServiceManager manager;
    
    /** The store janitor */
    private StoreJanitor storeJanitor;

    private File workDir;
    private File cacheDir;

    // ---------------------------------------------------- Lifecycle

    public EHStore() {
        instanceCount++;
        this.cacheName = "cocoon-ehcache-" + instanceCount;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.workDir = (File)context.get(Constants.CONTEXT_WORK_DIR);
        this.cacheDir = (File)context.get(Constants.CONTEXT_CACHE_DIR);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager aManager) throws ServiceException {
        this.manager = aManager;
        this.storeJanitor = (StoreJanitor) this.manager.lookup(StoreJanitor.ROLE);
    }

    /**
     * Configure the store. The following options can be used:
     * <ul>
     *  <li><code>maxobjects</code> (10000) - The maximum number of in-memory objects.</li>
     *  <li><code>overflow-to-disk</code> (true) - Whether to spool elements to disk after
     *   maxobjects has been exceeded.</li>
     *  <li><code>use-cache-directory</code> (false) - If true the <i>cache-directory</i>
     *   context entry will be used as the location of the disk store. 
     *   Within the servlet environment this is set in web.xml.</li>
     *  <li><code>use-work-directory</code> (false) - If true the <i>work-directory</i>
     *   context entry will be used as the location of the disk store.
     *   Within the servlet environment this is set in web.xml.</li>
     *  <li><code>directory</code> - Specify an alternative location of the disk store.
     * </ul>
     */
    public void parameterize(Parameters parameters) throws ParameterException {

        this.maxObjects = parameters.getParameterAsInteger("maxobjects", 10000);
        this.overflowToDisk = parameters.getParameterAsBoolean("overflow-to-disk", true);

        try {
            if (parameters.getParameterAsBoolean("use-cache-directory", false)) {
                if (this.getLogger().isDebugEnabled()) {
                    getLogger().debug("Using cache directory: " + cacheDir);
                }
                setDirectory(cacheDir);
            }
            else if (parameters.getParameterAsBoolean("use-work-directory", false)) {
                if (this.getLogger().isDebugEnabled()) {
                    getLogger().debug("Using work directory: " + workDir);
                }
                setDirectory(workDir);
            }
            else if (parameters.getParameter("directory", null) != null) {
                String dir = parameters.getParameter("directory");
                dir = IOUtils.getContextFilePath(workDir.getPath(), dir);
                if (this.getLogger().isDebugEnabled()) {
                    getLogger().debug("Using directory: " + dir);
                }
                setDirectory(new File(dir));
            }
            else {
                try {
                    // Legacy: use working directory by default
                    setDirectory(workDir);
                } catch (IOException e) {
                }
            }
        } catch (IOException e) {
            throw new ParameterException("Unable to set directory", e);
        }

    }

    /**
     * Sets the cache directory
     */
    private void setDirectory(final File directory) throws IOException  {
        
        /* Save directory path prefix */
        String directoryPath = getFullFilename(directory);
        directoryPath += File.separator;

        /* If directory doesn't exist, create it anew */
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                throw new IOException("Error creating store directory '" + directoryPath + "': ");
            }
        }

        /* Is given file actually a directory? */
        if (!directory.isDirectory()) {
            throw new IOException("'" + directoryPath + "' is not a directory");
        }

        /* Is directory readable and writable? */
        if (!(directory.canRead() && directory.canWrite())) {
            throw new IOException("Directory '" + directoryPath + "' is not readable/writable");
        }

        System.setProperty("java.io.tmpdir", directoryPath);
    }

    /**
     * Get the complete filename corresponding to a (typically relative)
     * <code>File</code>.
     * This method accounts for the possibility of an error in getting
     * the filename's <i>canonical</i> path, returning the io/error-safe
     * <i>absolute</i> form instead
     *
     * @param file The file
     * @return The file's absolute filename
     */
    private static String getFullFilename(File file) {
        try {
            return file.getCanonicalPath();
        }
        catch (Exception e) {
            return file.getAbsolutePath();
        }
    }

    /**
     * Initialize the CacheManager and created the Cache.
     */
    public void initialize() throws Exception {
        URL configFileURL = Thread.currentThread().getContextClassLoader().getResource(CONFIG_FILE);
        this.cacheManager = CacheManager.create(configFileURL);
        this.cache = new Cache(this.cacheName, this.maxObjects, this.overflowToDisk, true, 0, 0, true, 120);
        this.cacheManager.addCache(this.cache);
        this.storeJanitor.register(this);
    }
    
    /**
     * Shutdown the CacheManager.
     */
    public void dispose() {
        if (this.storeJanitor != null) {
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
        try {
            final List keys = this.cache.getKeysNoDuplicateCheck();
            if (!keys.isEmpty()) {
            	// TODO find a way to get to the LRU one.
                final Serializable key = (Serializable) keys.get(0);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Freeing cache");
                    getLogger().debug("key: " + key);
                    getLogger().debug("value: " + this.cache.get(key));
                }
                if (!this.cache.remove(key)) {
                    if (getLogger().isInfoEnabled()) {
                        getLogger().info("Concurrency condition in free()");
                    }
                }
            }
        }
        catch (CacheException e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Error in free()", e);
            }
        }
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
        List keys = null;
        try {
            keys = this.cache.getKeys();
        }
        catch (CacheException e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Error while getting cache keys", e);
            }
            keys = Collections.EMPTY_LIST;
        }
        return Collections.enumeration(keys);
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.store.Store#size()
     */
    public int size() {
        try {
            return this.cache.getSize();
        }
        catch (CacheException e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Error while getting cache size", e);
            }
            return 0;
        }
    }

}
