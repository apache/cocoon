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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AbstractAggregatedValidity;

/**
 * An aggregated validity for multiple sources.
 * 
 * @author <a href="http://www.apache.org/~sylvain">Sylvain Wallez</a>
 * @version CVS $Id: MultiSourceValidity.java,v 1.3 2004/03/05 13:02:21 bdelacretaz Exp $
 */
public class MultiSourceValidity extends AbstractAggregatedValidity implements SourceValidity {

    private long expiry;
    private long delay;
    private List uris = new ArrayList();
    private boolean isClosed = false;
    
    /** SourceResolver. Transient in order not to be serialized */
    private transient SourceResolver resolver;
    
    public MultiSourceValidity(SourceResolver resolver, long delay) {
        this.resolver = resolver;
        this.expiry = System.currentTimeMillis() + delay;
        this.delay = delay;
    }
    
    public void addSource(Source src) {
        if (this.uris != null) {
            SourceValidity validity = src.getValidity();
            if (validity == null) {
                // one of the sources has no validity : this object will always be invalid
                this.uris = null;
            } else {
                // Add the validity and URI to the list
                super.add(validity);
                this.uris.add(src.getURI());
            }
        }
    }
    
    public void close() {
        this.isClosed = true;
        this.resolver = null;
    }
    
    public int isValid() {
        if (System.currentTimeMillis() <= expiry) {
            return 1;
        }
        expiry = System.currentTimeMillis() + delay;
        
        if (uris == null || !isClosed) {
            return -1;
        } else {
            return computeStatus(null);
        }
    }

    public int isValid(SourceValidity newValidity) {
        if (uris == null || !isClosed) {
            return -1;
        }
        
        if (newValidity instanceof MultiSourceValidity) {
            return computeStatus(((MultiSourceValidity)newValidity).resolver);
        } else {
            // Don't know
            return -1;
        }
    }
    
    private int computeStatus(SourceResolver resolver) {
        List validities = super.getValidities();
        for (int i = 0; i < validities.size(); i++) {
            SourceValidity validity = (SourceValidity) validities.get(i);
            switch(validity.isValid()) {
                case -1:
                    // invalid : stop examining
                    return -1;
                case 1:
                    // valid : just continue to next source
                    break;
                case 0:
                    // don't know : check with the new source
                    if (resolver == null) {
                        // we have no resolver : definitely don't know (need to have one)
                        return 0;
                    }
                    try {
                        Source newSrc = resolver.resolveURI((String) uris.get(i));
                        int value = validity.isValid(newSrc.getValidity());
                        resolver.release(newSrc);
                        if (value != 1) {
                            return -1;
                        }
                    } catch(IOException ioe) {
                        return -1;
                    }
            }
        }
        
        // All items checked successfully
        return 1;
    }
}
