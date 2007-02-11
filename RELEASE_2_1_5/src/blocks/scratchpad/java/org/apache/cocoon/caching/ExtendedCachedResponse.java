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

import org.apache.excalibur.source.SourceValidity;

/**
 * This is a cached response. It extends the {@link CachedResponse} by 
 * a second array that could contain an alternative response.
 *
 * @since 2.1.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ExtendedCachedResponse.java,v 1.2 2004/03/05 10:07:25 bdelacretaz Exp $
 */
public class ExtendedCachedResponse extends CachedResponse {

    protected byte[] alternativeResponse;
    
    /**
     * Create a new entry for the cache.
     *
     * @param validityObjects The SourceValidity objects in the order
     *                        they occured in the pipeline
     * @param response        The cached sax stream or character stream
     */
    public ExtendedCachedResponse(SourceValidity[] validityObjects,
                          byte[]           response) {
        this(validityObjects, response, null);
    }

    /**
     * Create a new entry for the cache.
     *
     * @param validityObject  The SourceValidity object 
     * @param response        The cached sax stream or character stream
     */
    public ExtendedCachedResponse(SourceValidity   validityObject,
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
    public ExtendedCachedResponse(SourceValidity[] validityObjects,
                          byte[]           response,
                          Long expires) {
        super(validityObjects, response, expires);                              
    }

    /**
     * @return The alternative response
     */
    public byte[] getAlternativeResponse() {
        return this.alternativeResponse;
    }

    /**
     * @param response Set the alternative response
     */
    public void setAlternativeResponse(byte[] response) {
        this.alternativeResponse = response;
    }

}
