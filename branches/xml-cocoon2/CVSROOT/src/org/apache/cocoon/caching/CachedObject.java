/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.caching;

import java.util.Map;

/**
 * This is an cached object as it is stored in the <code>EventCache</code>
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-04-11 10:52:14 $
 */
public final class CachedObject {

    private Map validityObjects;
    private Object saxFragment;

    /**
     * Create a new entry for the cache.
     *
     * @param validityObjects The CacheValidity objects hashed by their
     *        <code>ComponentCacheKey</code>
     * @param saxFragment     The cached sax stream
     */
    public CachedObject(Map validityObjects,
                        Object saxFragment) {
        this.validityObjects = validityObjects;
        this.saxFragment = saxFragment;
    }

    /**
     * Checks if the CacheValidity object is still valid.
     */
    public boolean isValid(ComponentCacheKey componentKey,
                           CacheValidity     componentValidity) {
        CacheValidity ownValidity = (CacheValidity)this.validityObjects.get(componentKey);
        if (ownValidity != null && ownValidity.isValid(componentValidity) == true) {
            return true;
        }
        return false;
    }

    /**
     * Get the cached sax stream.
     *
     * @return The sax stream
     */
    public Object getSAXFragment() {
        return this.saxFragment;
    }
}