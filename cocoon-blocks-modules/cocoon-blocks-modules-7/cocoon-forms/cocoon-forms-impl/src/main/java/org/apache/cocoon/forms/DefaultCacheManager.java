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
package org.apache.cocoon.forms;

import java.util.Map;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;

import org.apache.commons.collections.FastHashMap;

/**
 * Component implementing the {@link CacheManager} role.
 *
 * @version $Id$
 */
public class DefaultCacheManager implements CacheManager {
    // NOTE: Component is there to allow this block to also run in the 2.1 branch

    // FIXME Unbounded map - the road to OOME
    protected Map cache;


    public DefaultCacheManager() {
        this.cache = new FastHashMap();
    }

    public Object get(Source source, String prefix) {
        // Create a cache key
        final String key = prefix + source.getURI();

        // If object is not in the cache then return null
        Object[] objectAndValidity = (Object[]) this.cache.get(key);
        if (objectAndValidity == null) {
            return null;
        }

        // If object is in the cache, check stored object validity
        final SourceValidity validity = (SourceValidity) objectAndValidity[1];
        int valid = validity.isValid();
        if (valid == SourceValidity.UNKNOWN) {
            // Compare against current source validity
            valid = validity.isValid(source.getValidity());
        }

        // If stored object is not valid then remove object from cache and return null
        if (valid != SourceValidity.VALID) {
            this.cache.remove(key);
            return null;
        }

        // If valid then return cached object
        return objectAndValidity[0];
    }

    public void set(Object object, Source source, String prefix) {
        final SourceValidity validity = source.getValidity();
        if (validity != null) {
            final String key = prefix + source.getURI();
            this.cache.put(key, new Object[]{ object, validity });
        }
    }

    public void remove(Source source, String prefix) {
        final String key = prefix + source.getURI();
        this.cache.remove(key);
    }
}
