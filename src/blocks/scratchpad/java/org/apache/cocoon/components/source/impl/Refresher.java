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
package org.apache.cocoon.components.source.impl;

import org.apache.cocoon.caching.SimpleCacheKey;
import org.apache.excalibur.source.SourceException;

/**
 * The refresher is a component that updates uri and stores
 * there response in a cache.
 *  
 * @since 2.1.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: Refresher.java,v 1.3 2004/03/05 10:07:25 bdelacretaz Exp $
 */
public interface Refresher {
    
    String ROLE = Refresher.class.getName();

    /**
     * Refresh the content now.
     * 
     * @param cacheKey The key used to cache the content
     * @param uri      The uri to cache, every valid protocol can be used, except the Cocoon protocol!
     * @param expires  The time in seconds the cached content is valid
     * @param cacheRole The role of the cache component to store the content
     */
    void refresh(SimpleCacheKey cacheKey,
                 String uri,
                 long   expires,
                 String cacheRole)
    throws SourceException;

    /**
     * Refresh the content periodically in the background.
     * 
     * @param cacheKey The key used to cache the content
     * @param uri      The uri to cache, every valid protocol can be used, except the Cocoon protocol!
     * @param expires  The time in seconds the cached content is valid
     * @param cacheRole The role of the cache component to store the content
     */
    void refreshPeriodically(SimpleCacheKey cacheKey,
                             String uri,
                             long   expires,
                             String cacheRole);
}
