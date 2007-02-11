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

import java.util.Map;

/**
 * A validation object using a set of key/value pairs contained in a <code>Map</code>.
 *
 * @deprecated Use the Avalon Excalibur SourceValidity implementations instead
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Id: ParametersCacheValidity.java,v 1.2 2004/03/05 13:02:38 bdelacretaz Exp $
 */
public final class ParametersCacheValidity
implements CacheValidity {

    private Map map;

    /**
     * Constructor
     */
    public ParametersCacheValidity(Map map) {
        this.map = map;
    }

    public boolean isValid(CacheValidity validity) {
        if (validity instanceof ParametersCacheValidity) {
            return this.map.toString().equals(((ParametersCacheValidity)validity).getParameters().toString());
        }
        return false;
    }

    public Map getParameters() {
        return this.map;
    }

    public String toString() {
        return "Parameters Validity[" + this.map + ']';
    }
}
