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
import java.util.ArrayList;
import java.util.List;

/**
 * This is the cache key for one pipeline (or the first part of a pipeline).
 * It consists of one or more {@link ComponentCacheKey}s.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: PipelineCacheKey.java,v 1.3 2004/05/19 08:42:40 cziegeler Exp $
 */
public final class PipelineCacheKey
        implements Serializable {

    /** The keys */
    private final List keys;

    /** the hash code */
    private int hashCode;

    /**
     * Constructor
     */
    public PipelineCacheKey() {
        this.keys = new ArrayList(6);
    }

    /**
     * Constructor
     */
    public PipelineCacheKey(int size) {
        this.keys = new ArrayList(size);
    }

    /**
     * Add a key
     */
    public void addKey(ComponentCacheKey key) {
        this.keys.add(key);
        this.hashCode = 0;
        this.toString = null;
    }

    /**
     * Remove the last key
     */
    public void removeLastKey() {
        this.keys.remove(this.keys.size()-1);
        this.hashCode = 0;
        this.toString = null;
    }

    /**
     * Remove unitl cachepoint (including cachePoint) 
     */
    public void removeUntilCachePoint() {
        this.hashCode = 0;
        this.toString = null;
        int keyCount = this.keys.size();

        while (keyCount > 0) {
            if (((ComponentCacheKey)this.keys.get(keyCount-1)).isCachePoint()) {
                this.keys.remove(keyCount-1);
                return;
            }
            this.keys.remove(keyCount-1);
            keyCount--;
        }
    }

    /**
     * Return the number of keys
     */
    public int size() {
        return this.keys.size();
    }

    /**
     * Compare
     */
    public boolean equals(Object object) {
        if (object instanceof PipelineCacheKey) {
            PipelineCacheKey pck = (PipelineCacheKey)object;
            final int len = this.keys.size();
            if (pck.keys.size() == len) {
                boolean cont = true;
                int i = 0;
                while (i < len && cont) {
                    cont = this.keys.get(i).equals(pck.keys.get(i));
                    i++;
                }
                return cont;
            }
        }
        return false;
    }

    /**
     * Generate a hash code
     */
    public int hashCode() {
        if (this.hashCode == 0) {
            final int len = this.keys.size();
            for(int i=0; i < len; i++) {
                this.hashCode += this.keys.get(i).hashCode();
            }
            if (len % 2 == 0) this.hashCode++;
        }
        return this.hashCode;
    }

    /**
     * Clone the object (but not the component keys)
     */
    public PipelineCacheKey copy() {
        final int len = this.keys.size();
        PipelineCacheKey pck = new PipelineCacheKey(len);
        for(int i=0; i < len; i++) {
            pck.keys.add(this.keys.get(i));
        }
        return pck;
    }

    private String toString;

    /**
     * toString
     * The FilesystemStore uses toString!
     */
    public String toString() {
        if (this.toString == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("PK");
            final int len = this.keys.size();
            for(int i=0; i < len; i++) {
                buffer.append('_').append(this.keys.get(i).toString());
            }
            this.toString = buffer.toString();
        }
        return toString;
    }
}
