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

import org.apache.excalibur.source.SourceValidity;

/**
 * This is a cached response. This can either contain a byte array with
 * the complete character response or a byte array with compiled SAX events.
 *
 * This class replaces the <code>CachedEventObject</code> and the
 * <code>CachedStreamObject</code>.
 *
 * @since 2.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CachedResponse.java,v 1.6 2004/03/08 13:57:40 cziegeler Exp $
 */
public class CachedResponse
        implements Serializable {

    protected final SourceValidity[] validityObjects;
    protected final byte[]           response;
    protected Long                   expires;
    protected final long             lastModified;

    /**
     * Create a new entry for the cache.
     *
     * @param validityObjects The SourceValidity objects in the order
     *                        they occured in the pipeline
     * @param response        The cached sax stream or character stream
     */
    public CachedResponse(SourceValidity[] validityObjects,
                          byte[]           response) {
        this(validityObjects, response, null);
    }

    /**
     * Create a new entry for the cache.
     *
     * @param validityObject  The SourceValidity object 
     * @param response        The cached sax stream or character stream
     */
    public CachedResponse(SourceValidity   validityObject,
                          byte[]           response) {
        this(new SourceValidity[] {validityObject}, response, null);
    }

    /**
     * Create a new entry for the cache.
     *
     * @param validityObjects The SourceValidity objects in the order
     *                        they occured in the pipeline
     * @param response        The cached sax stream or character stream
     * @param expires         The configured expires, or null if no
     *                        expires was defined.
     */
    public CachedResponse(SourceValidity[] validityObjects,
                          byte[]           response,
                          Long expires) {
        this.validityObjects = validityObjects;
        this.response = response;
        this.expires = expires;
        this.lastModified = this.setLastModified(System.currentTimeMillis());
    }

    /**
     * Get the validity objects
     */
    public SourceValidity[] getValidityObjects() {
        return this.validityObjects;
    }

    /**
     * Get the cached response.
     *
     * @return The sax stream or character stream
     */
    public byte[] getResponse() {
        return this.response;
    }

    /**
     * Get the configured expires.
     *
     * @return The configured expires, or null if no expires was defined
     */
    public Long getExpires() {
        return this.expires;
    }
    
    /**
     * Set the (newly) configured expires.
     * 
     */
    public void setExpires(Long newExpires) {
        this.expires = newExpires;    
    }
    
    /**
     * Set the (newly) configured last modified.
     * 
     */
    protected long setLastModified(long lastModified) {
        // Return the value rounded to the nearest second.
        return lastModified - (lastModified % 1000);
    }
    
    /**
     * @return the last modified time 
     */
    public long getLastModified() {
        return lastModified;
    }

}
