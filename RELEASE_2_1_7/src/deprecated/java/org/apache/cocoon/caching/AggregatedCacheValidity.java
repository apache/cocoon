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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A validation object aggregating several validity objects. This is similar to the
 * {@link CompositeCacheValidity} with the difference that the amount of
 * aggregated objects is not limited.
 *
 * @deprecated Use the Avalon Excalibur SourceValidity implementations instead
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Id: AggregatedCacheValidity.java,v 1.2 2004/03/05 13:02:38 bdelacretaz Exp $
 */
public final class AggregatedCacheValidity
implements CacheValidity {

    private List a;

    /**
     * Constructor
     */
    public AggregatedCacheValidity() {
        this.a = new ArrayList();
    }

    /**
     * Add another validity object
     */
    public void add(CacheValidity validity) {
        this.a.add(validity);
    }

    public boolean isValid(CacheValidity validity) {
        if (validity instanceof AggregatedCacheValidity) {
            List b = ((AggregatedCacheValidity)validity).a;
            if(a.size() != b.size())
                return false;
            for(Iterator i = a.iterator(), j = b.iterator(); i.hasNext();) {
                if(!((CacheValidity)i.next()).isValid((CacheValidity)j.next()))
                    return false;
            }
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuffer b = new StringBuffer("Aggregated Validity[");
        for(Iterator i = a.iterator(); i.hasNext();) {
            b.append(i.next());
            if(i.hasNext()) b.append(':');
        }
        b.append(']');
        return b.toString();
    }
}

