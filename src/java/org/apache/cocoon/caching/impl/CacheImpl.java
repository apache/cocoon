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
package org.apache.cocoon.caching.impl;

import java.io.IOException;
import java.io.Serializable;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.excalibur.store.Store;

/**
 * This is the Cocoon cache. This component is responsible for storing
 * and retrieving cached responses. It can be used to monitor the cache
 * or the investigate which responses are cached etc.
 * This component will grow!
 *
 * @since 2.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CacheImpl.java,v 1.10 2004/03/05 13:02:45 bdelacretaz Exp $
 */
public class CacheImpl
extends AbstractLogEnabled
implements Cache, ThreadSafe, Serviceable, Disposable, Parameterizable {

    /** The store containing the cached responses */
    protected Store store;

    /** The service manager */
    protected ServiceManager manager;

    /**
     * Serviceable Interface
     */
    public void service (ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Disposable Interface
     */
    public void dispose() {
        this.manager.release(this.store);
        this.store = null;
        this.manager = null;
    }

    /**
     * Store a cached response
     * @param key         the key used by the caching algorithm to identify the
     *                    request
     * @param response    the cached response
     */
    public void store(Serializable     key,
                      CachedResponse   response)
    throws ProcessingException {
        if ( this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Caching new response for " + key);
        }
        try {
            this.store.store(key, response);
        } catch (IOException ioe) {
            throw new ProcessingException("Unable to cache response.", ioe);
        }
    }

    /**
     * Get a cached response.
     * If it is not available <code>null</code> is returned.
     * @param key         the key used by the caching algorithm to identify the
     *                    request
     */
    public CachedResponse get(Serializable key) {
        final CachedResponse r = (CachedResponse)this.store.get(key);
        if ( this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Cached response for " + key + " : " + 
                                   (r == null ? "not found" : "found"));
        }
        return r;
    }

    /**
     * Remove a cached response.
     * If it is not available no operation is performed.
     * @param key         the key used by the caching algorithm to identify the
     *                    request
     */
    public void remove(Serializable key) {
        if ( this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Removing cached response for " + key); 
        }
        this.store.remove(key);
    }

    /**
     * clear cache of all cached responses 
     */
    public void clear() {
        if ( this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Clearing cache"); 
        }
        // FIXME this clears the whole store!
        this.store.clear();
    }

	/**
	 * See if a response is cached under this key
	 */
	public boolean containsKey(Serializable key) {
		return this.store.containsKey(key);
	}

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        String storeName = parameters.getParameter("store", Store.ROLE);
        try {
            this.store = (Store)this.manager.lookup(storeName);
        } catch (ServiceException e) {
            throw new ParameterException("Unable to lookup store: " + storeName, e);
        }
    }

}
