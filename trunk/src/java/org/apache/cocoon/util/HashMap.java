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
package org.apache.cocoon.util;

import java.util.Map;

/**
 * Extended Version of {@link java.util.HashMap}.
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: HashMap.java,v 1.2 2004/03/08 14:03:30 cziegeler Exp $
 */
public class HashMap extends java.util.HashMap {

    public HashMap () {
    super();
    }

    public HashMap ( int initialCapacity ) {
    super(initialCapacity);
    }

    public HashMap ( int initialCapacity, float loadFactor ) {
    super(initialCapacity, loadFactor);
    }

    public HashMap ( Map t) {
    super(t);
    }


    /**
     * Get method extended by default object to be returned when key
     * is not found.
     *
     * @param key key to look up
     * @param _default default value to return if key is not found
     * @return value that is associated with key
     */
    public Object get ( Object key, Object _default ) {
    if (this.containsKey(key)) {
        return this.get(key);
    } else {
        return _default;
    }
    }

}
