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
 * A CacheValidity object wrapping the Avalon Excalibur
 * {@link SourceValidity} object.
 *
 * @since 2.1
 * @deprecated Use the Avalon Excalibur SourceValidity implementations instead
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SourceCacheValidity.java,v 1.3 2004/03/05 13:02:45 bdelacretaz Exp $
 */
public final class SourceCacheValidity
implements CacheValidity {

    protected SourceValidity sourceValidity;

    /**
     * Constructor
     */
    public SourceCacheValidity(SourceValidity validity) {
        this.sourceValidity = validity;
    }

    /**
     * Check if the component is still valid.
     * This is only true, if the incoming CacheValidity is of the same
     * type and has the same values.
     */
    public boolean isValid(CacheValidity validity) {
        final int valid = this.sourceValidity.isValid();
        if (valid == 1) return true;
        if (valid == 0 && validity instanceof SourceCacheValidity) {
            if (this.sourceValidity.isValid(((SourceCacheValidity)validity).getSourceValidity()) == 1) {
                return true;
            }
            
        }
        return false;
    }

    /**
     * Get the real validity
     */
    public SourceValidity getSourceValidity() {
        return this.sourceValidity;
    }

    public String toString() {
        return "Source Validity[" + this.sourceValidity + ']';
    }
}
