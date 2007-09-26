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

/**
 * A CacheStorageProxy is an interface object between the {@link IncludeCacheManager}
 * and the real store caching the content.
 * Currently you can use the {@link StoreIncludeCacheStorageProxy} that uses the
 * usual store or the {@link ModifiableSourceIncludeCacheStorageProxy} that
 * uses a configured source.
 * 
 *  @version $Id$
 *  @since   2.1
 */
public interface IncludeCacheStorageProxy {

    /**
     * Get the cached content for the given URI.
     * @param uri Absolute URI specifying the content
     * @return Serializable
     */
    Serializable get(String uri);
    
    /**
     * Put the content into the cache for the given URI.
     * @param uri Absolute URI specifying the content
     * @param object The content
     * @throws IOException
     */
    void put(String uri, Serializable object)
    throws IOException;
    
    /**
     * Remove the cached content for the given URI
     * @param uri Absolute URI specifying the content
     */
    void remove(String uri);
}
