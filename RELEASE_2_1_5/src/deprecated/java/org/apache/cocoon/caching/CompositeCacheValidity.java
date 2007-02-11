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



/**
 * A validation object aggregating two validity objects. This is similar to the
 * {@link AggregatedCacheValidity} with the difference that the amount of
 * aggregated objects is limited.
 *
 * @deprecated Use the Avalon Excalibur SourceValidity implementations instead
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Id: CompositeCacheValidity.java,v 1.2 2004/03/05 13:02:38 bdelacretaz Exp $
 */
public final class CompositeCacheValidity
implements CacheValidity {

    private CacheValidity v1;
    private CacheValidity v2;

    /**
     * Constructor
     */
    public CompositeCacheValidity(CacheValidity v1, CacheValidity v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public boolean isValid(CacheValidity validity) {
        if (validity instanceof CompositeCacheValidity) {
            return (v1.isValid(((CompositeCacheValidity)validity).getValidity1()) &&
                    v2.isValid(((CompositeCacheValidity)validity).getValidity2()));
        }
        return false;
    }

    public CacheValidity getValidity1() {
        return this.v1;
    }

    public CacheValidity getValidity2() {
        return this.v2;
    }

    public String toString() {
        return "Composite Validity[" + v1 + ':' + v2 + ']';
    }
}
