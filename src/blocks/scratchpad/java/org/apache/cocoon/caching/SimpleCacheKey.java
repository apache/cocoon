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

import java.io.Serializable;

/**
 * This is a "simple" cache key that does not consider the components used in the
 * pipeline. It simply consists of a key (unique identifier for the request) and
 * a boolean value that defines if the key is for a complete pipeline call or
 * for an internal pipeline call.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SimpleCacheKey.java,v 1.3 2004/04/15 08:04:39 cziegeler Exp $
 * @since 2.1.1
 */
public class SimpleCacheKey
    implements Serializable {

    /** The key */
    final protected String key;

    /** Is this an external pipeline call? */
    final protected boolean external;

    /** cache key */
    final protected String cacheKey;
    
    /** cache toString() */
    protected String toString;
    
    /**
     * Constructor
     */
    public SimpleCacheKey(String key, boolean external) {
        this.key = key;
        this.external = external;
        final StringBuffer buf = new StringBuffer();
        buf.append(this.external).append(':').append(this.key);
        this.cacheKey = buf.toString();
    }

    /**
     * Compare
     */
    public boolean equals(Object object) {
        if (object instanceof SimpleCacheKey) {
            SimpleCacheKey pck = (SimpleCacheKey)object;
            return this.cacheKey.equals( pck.cacheKey );
        }
        return false;
    }

    /**
     * Generate a hash code
     */
    public int hashCode() {
        return this.cacheKey.hashCode();
    }

    /**
     * toString
     * The FilesystemStore uses toString!
     */
    public String toString() {
        if (this.toString == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("SCK:");
            buffer.append(this.cacheKey);
            this.toString = buffer.toString();
        }
        return toString;
    }
    
    /**
     * The cache key
     */
    public String getKey() {
        return this.key;
    }
}
