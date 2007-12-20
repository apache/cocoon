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
package org.apache.cocoon.portal.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.cocoon.portal.spi.RequestContextProvider;

/**
 * This is a wrapper for a {@link java.util.Map} storing the real map
 * in a session attribute.
 * The idea is to have a global map for all users that delegates at run
 * time to a user specific map stored in a session.
 *
 * @version $Id$
 */
public class SessionMapWrapper implements Map {

    protected final String attrName;
    protected final RequestContextProvider requestContextProvider;

    public SessionMapWrapper(RequestContextProvider provider, String attrName) {
        this.requestContextProvider = provider;
        this.attrName = attrName;
    }

    /**
     * Get the real map for the current user.
     */
    protected Map getRealMap(boolean create) {
        final HttpServletRequest req = this.requestContextProvider.getCurrentRequestContext().getRequest();
        final HttpSession session = req.getSession(create);
        if ( session != null ) {
            Map map = (Map)session.getAttribute(this.attrName);
            if ( map != null ) {
                return map;
            } else if ( create ) {
                map = new HashMap();
                session.setAttribute(this.attrName, map);
            }
        }
        return Collections.EMPTY_MAP;
    }

    /**
     * @see java.util.Map#clear()
     */
    public void clear() {
        this.getRealMap(false).clear();
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return this.getRealMap(false).containsKey(key);
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        return this.getRealMap(false).containsValue(value);
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        return this.getRealMap(false).entrySet();
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        return this.getRealMap(false).get(key);
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return this.getRealMap(false).isEmpty();
    }

    /**
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
        return this.getRealMap(false).keySet();
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value) {
        return this.getRealMap(true).put(key, value);
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map t) {
        this.getRealMap(true).putAll(t);
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {
        return this.getRealMap(false).remove(key);
    }

    /**
     * @see java.util.Map#size()
     */
    public int size() {
        return this.getRealMap(false).size();
    }

    /**
     * @see java.util.Map#values()
     */
    public Collection values() {
        return this.getRealMap(false).values();
    }
}
