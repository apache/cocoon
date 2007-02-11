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
package org.apache.cocoon.transformation.helpers;

import java.io.IOException;
import java.io.Serializable;

import org.apache.avalon.framework.logger.Logger;
import org.apache.excalibur.store.Store;

/**
 * This is the interface between the {@link IncludeCacheManager} and the usual
 * store.
 * 
 *  @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 *  @version CVS $Id: StoreIncludeCacheStorageProxy.java,v 1.3 2004/03/05 13:03:00 bdelacretaz Exp $
 *  @since   2.1
 */
public final class StoreIncludeCacheStorageProxy
    implements IncludeCacheStorageProxy {

    private Store  store;
    
    private Logger logger;
    
    /**
     * Constructor
     * @param store  The store for the cached content
     * @param logger A logger for debugging
     */
    public StoreIncludeCacheStorageProxy(Store store, Logger logger) {
        this.store = store;
        this.logger = logger;
    }
    
    /** A string representation for a key */
    private String getKey(String uri) {
        return "DCS:" + uri;
    }
    
    /**
     * @see IncludeCacheStorageProxy#get(java.lang.String)
     */
    public Serializable get(String uri) {
        if (logger.isDebugEnabled()) {
            logger.debug("StoreProxy: Getting content for " + uri);
        }

        Serializable result = (Serializable)this.store.get(this.getKey(uri));

        if (logger.isDebugEnabled()) {
            logger.debug("StoreProxy: Result for " + uri + " : " + (result == null ? "Not in cache" : "Found"));
        }
        return result;
    }

    /**
     * @see IncludeCacheStorageProxy#put(java.lang.String, java.io.Serializable)
     */
    public void put(String uri, Serializable object) 
    throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("StoreProxy: Storing content for " + uri);
        }
        this.store.store(this.getKey(uri), object);
    }

    /**
     * @see IncludeCacheStorageProxy#remove(java.lang.String)
     */
    public void remove(String uri) {
        if (logger.isDebugEnabled()) {
            logger.debug("StoreProxy: Removing content for " + uri);
        }
        this.store.remove(this.getKey(uri));
    }
}
