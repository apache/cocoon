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

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.caching.IdentifierCacheKey;
import org.apache.excalibur.source.SourceException;

/**
 * A Refresher is a component that updates the cached contents
 * of a Source.
 * <p>
 * Implementations can for instance trigger updates based on a timeout value or
 * in response to an external event.
 * </p>
 * 
 * @since 2.1.1
 * @version CVS $Id: Refresher.java,v 1.5 2004/04/15 08:05:56 cziegeler Exp $
 */
public interface Refresher {
    
    String ROLE = Refresher.class.getName();

    /**
     * Add a uri to the Refresher.
     * 
     * @param cacheKey  The key used to cache the content
     * @param uri       The uri to cache, every valid protocol can be used (except the Cocoon protocol!)
     * @param cacheRole The role of the cache component to store the content
     * @param params    Additional parameters such as a timout value
     */
    void refresh(IdentifierCacheKey cacheKey,
                 String uri,
                 String cacheRole,
                 Parameters params)
    throws SourceException;

}
