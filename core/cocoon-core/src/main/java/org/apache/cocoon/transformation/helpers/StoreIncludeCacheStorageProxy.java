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
package org.apache.cocoon.transformation.helpers;

import java.io.IOException;
import java.io.Serializable;

import org.apache.excalibur.store.Store;

import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * This is the interface between the {@link IncludeCacheManager} and the usual
 * store.
 * 
 * @since   2.1
 * @version $Id$
 */
public final class StoreIncludeCacheStorageProxy extends AbstractLogEnabled
                                                 implements IncludeCacheStorageProxy {

    private Store store;

    /**
     * Constructor
     * @param store  The store for the cached content
     */
    public StoreIncludeCacheStorageProxy(Store store) {
        this.store = store;
    }
    
    /** A string representation for a key */
    private String getKey(String uri) {
        return "DCS:" + uri;
    }
    
    /**
     * @see IncludeCacheStorageProxy#get(java.lang.String)
     */
    public Serializable get(String uri) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("StoreProxy: Getting content for " + uri);
        }

        Serializable result = (Serializable) this.store.get(getKey(uri));

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("StoreProxy: Result for " + uri + " : " + (result == null ? "Not in cache" : "Found"));
        }
        return result;
    }

    /**
     * @see IncludeCacheStorageProxy#put(java.lang.String, java.io.Serializable)
     */
    public void put(String uri, Serializable object) 
    throws IOException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("StoreProxy: Storing content for " + uri);
        }
        this.store.store(getKey(uri), object);
    }

    /**
     * @see IncludeCacheStorageProxy#remove(java.lang.String)
     */
    public void remove(String uri) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("StoreProxy: Removing content for " + uri);
        }
        this.store.remove(getKey(uri));
    }
}
