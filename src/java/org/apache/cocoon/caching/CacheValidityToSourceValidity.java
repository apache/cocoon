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
 * <code>SourceValidity</code> object.
 *
 * @since 2.1
 * @deprecated Use the Avalon Excalibur SourceValidity implementations instead
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CacheValidityToSourceValidity.java,v 1.3 2004/03/05 13:02:45 bdelacretaz Exp $
 */
public final class CacheValidityToSourceValidity
implements SourceValidity {

    protected CacheValidity cacheValidity;

    /**
     * Create a new instance
     */
    public static CacheValidityToSourceValidity createValidity(CacheValidity validity) {
        if ( null != validity) {
            return new CacheValidityToSourceValidity(validity);
        }
        return null;
    }

    /**
     * Constructor
     */
    protected CacheValidityToSourceValidity(CacheValidity validity) {
        this.cacheValidity = validity;
    }


    /**
     * Check if the component is still valid.
     * If <code>0</code> is returned the isValid(SourceValidity) must be
     * called afterwards!
     * If -1 is returned, the component is not valid anymore and if +1
     * is returnd, the component is valid.
     */
    public int isValid() {
        return 0;
    }

    /**
     * Check if the component is still valid.
     * This is only true, if the incoming Validity is of the same
     * type and has the same values.
     * The invocation order is that the isValid method of the
     * old Validity object is called with the new one as a parameter
     */
    public int isValid( SourceValidity newValidity ) {
        if (newValidity instanceof CacheValidityToSourceValidity) {
            if (this.cacheValidity.isValid(((CacheValidityToSourceValidity)newValidity).cacheValidity)) {
                return 1;
            }
            return -1;
        }
        return -1;
    }

    public String toString() {
        return "Cache Validity To Source Validity[" + this.cacheValidity + ']';
    }
}
