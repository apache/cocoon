/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.store.Store;

/**
 * This is the Cocoon cache. This component is responsible for storing
 * and retrieving cached responses. It can be used to monitor the cache
 * or the investigate which responses are cached etc.
 * This component will grow!
 *
 * @since 2.1
 * @version $Id$
 */
public class CacheImpl implements Cache {

    private Log logger = LogFactory.getLog(getClass());

    /** The store containing the cached responses */
    protected Store store;

    /**
     * Store a cached response
     * @param key         the key used by the caching algorithm to identify the
     *                    request
     * @param response    the cached response
     */
    public void store(Serializable     key,
                      CachedResponse   response)
    throws ProcessingException {
        if (getLogger().isInfoEnabled()) {
            getLogger().info("Caching new response for " + key);
        }
        try {
            this.store.store(key, response);
        } catch (IOException e) {
            throw new ProcessingException("Unable to cache response.", e);
        }
    }

    /**
     * Get a cached response.
     * If it is not available <code>null</code> is returned.
     * @param key         the key used by the caching algorithm to identify the
     *                    request
     */
    public CachedResponse get(Serializable key) {
        final CachedResponse r = (CachedResponse) this.store.get(key);
        if (getLogger().isInfoEnabled()) {
            getLogger().info("Cache " + (r == null ? "MISS" : "HIT") + " for " + key);
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
        if (getLogger().isInfoEnabled()) {
            getLogger().info("Removing cached response for " + key);
        }
        this.store.remove(key);
    }

    /**
     * clear cache of all cached responses
     */
    public void clear() {
        if (getLogger().isInfoEnabled()) {
            getLogger().info("Clearing cache");
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

    /**
     * Set the Store implementation
     */
    public void setStore(Store store) {
        this.store = store;
    }

    protected Log getLogger() {
        return this.logger;
    }
}
