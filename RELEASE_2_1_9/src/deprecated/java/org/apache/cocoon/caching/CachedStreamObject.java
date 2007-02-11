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
package org.apache.cocoon.caching;

import java.util.Map;

/**
 * This is an cached object as it is stored in the <code>StreamCache</code>
 *
 * @deprecated by the {@link CachedResponse}
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CachedStreamObject.java,v 1.2 2004/03/05 13:02:38 bdelacretaz Exp $
 */
public final class CachedStreamObject implements java.io.Serializable {

    private Map validityObjects;
    private byte[] response;

    /**
     * Create a new entry for the cache.
     *
     * @param validityObjects The CacheValidity objects hashed by their
     *        <code>ComponentCacheKey</code>
     * @param response     The cached response
     */
    public CachedStreamObject(Map validityObjects,
                              byte[] response) {
        this.validityObjects = validityObjects;
        this.response = response;
    }

    /**
     * Checks if the CacheValidity object is still valid.
     */
    public boolean isValid(ComponentCacheKey componentKey,
                           CacheValidity     componentValidity) {
        CacheValidity ownValidity = (CacheValidity)this.validityObjects.get(componentKey);
        if (ownValidity != null && ownValidity.isValid(componentValidity)) {
            return true;
        }
        return false;
    }

    /**
     * Get the validity object
     * @return The <CODE>CacheValidity</CODE> object or <CODE>null</CODE>.
     */
    public CacheValidity getCacheValidity(ComponentCacheKey componentKey) {
        return (CacheValidity)this.validityObjects.get(componentKey);
    }

    /**
     * Get the cached response.
     *
     * @return The response
     */
    public byte[] getResponse() {
        return this.response;
    }
}
