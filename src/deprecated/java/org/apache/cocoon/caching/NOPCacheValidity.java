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
 * A validation object which is always valid.
 * This might be the most used CacheValidity object for serializers.
 *
 * @deprecated Use the Avalon Excalibur SourceValidity implementations instead
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: NOPCacheValidity.java,v 1.2 2004/03/05 13:02:38 bdelacretaz Exp $
 */
public final class NOPCacheValidity
implements CacheValidity {

    public static final CacheValidity CACHE_VALIDITY = new NOPCacheValidity();

    public boolean isValid(CacheValidity validity) {
        return validity instanceof NOPCacheValidity;
    }

    public String toString() {
        return "NOP Validity";
    }
}
