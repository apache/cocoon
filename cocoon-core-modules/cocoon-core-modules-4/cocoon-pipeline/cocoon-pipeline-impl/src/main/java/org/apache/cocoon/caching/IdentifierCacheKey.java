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

/**
 * This is a "simple" cache key that does not consider the components used in the
 * pipeline. It simply consists of a key (unique identifier for the request) and
 * a boolean value that defines if the key is for a complete pipeline call or
 * for an internal pipeline call.
 *
 * @version $Id$
 * @since 2.1.1
 */
public class IdentifierCacheKey implements Serializable {

    /** The key. Must not be null. */
    protected final String key;

    /** True if this is an external pipeline call. */
    protected final boolean external;

    /** Caches toString() value. */
    protected transient String toString;

    /**
     * @param key Not null key value
     * @param external True if key represents external pipeline call
     */
    public IdentifierCacheKey(String key, boolean external) {
        this.key = key;
        this.external = external;
    }

    /**
     * The cache key
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Compare
     */
    public boolean equals(Object object) {
        if (object instanceof IdentifierCacheKey) {
            IdentifierCacheKey pck = (IdentifierCacheKey) object;
            if (external != pck.external) {
                return false;
            }
            return this.key.equals(pck.key);
        }
        return false;
    }

    /**
     * Generate a hash code
     */
    public int hashCode() {
        return key.hashCode() + (external ? Boolean.TRUE : Boolean.FALSE).hashCode();
    }

    /**
     * toString
     * The FilesystemStore uses toString!
     */
    public String toString() {
        if (this.toString == null) {
            this.toString = "IK:" + external + ':' + key;
        }
        return toString;
    }
}
