/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * Extended Version of {@link java.util.HashMap} that provides an extended
 * get method accpeting a default value. The default value is returned if
 * the map does not contain a value for the provided key.
 *
 * @version $Id$
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
    public Object get( Object key, Object _default ) {
        if (this.containsKey(key)) {
            return this.get(key);
        }
        return _default;
    }

}
