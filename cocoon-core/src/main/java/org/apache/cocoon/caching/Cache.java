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
package org.apache.cocoon.caching;

import java.io.Serializable;

import org.apache.cocoon.ProcessingException;

/**
 * This is the Cocoon cache. This component is responsible for storing
 * and retrieving cached responses. It can be used to monitor the cache
 * or to investigate which responses are cached etc.
 * This interface will grow!
 *
 * @since 2.1
 * @version $Id$
 */
public interface Cache {

    /** The Avalon Role **/
    String ROLE = Cache.class.getName();

    /**
     * Store a cached response
     * @param key         the key used by the caching algorithm to identify the
     *                    request
     * @param response    the cached response
     */
    void store(Serializable     key,
               CachedResponse   response)
    throws ProcessingException;

    /**
     * Get a cached response.
     * If it is not available <code>null</code> is returned.
     * @param key         the key used by the caching algorithm to identify the
     *                    request
     */
    CachedResponse get(Serializable key);

    /**
     * Remove a cached response.
     * If it is not available no operation is performed.
     * @param key         the key used by the caching algorithm to identify the
     *                    request
     */
    void remove(Serializable key);
    
    /**
     * clear cache of all cached responses 
     */
    void clear();

    /**
     * See if a response is cached under this key.
     */
    boolean containsKey(Serializable key);
}
