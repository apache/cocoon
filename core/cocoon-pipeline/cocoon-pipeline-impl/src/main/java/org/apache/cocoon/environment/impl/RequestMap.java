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
package org.apache.cocoon.environment.impl;

import org.apache.cocoon.environment.Request;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Request attributes map
 *
 * @version $Id$
 */
public class RequestMap extends BaseMap {

    private Request request;


    public RequestMap(Request request) {
        this.request = request;
    }

    public Object get(Object key) {
        return request.getAttribute(key.toString());
    }

    public Object put(Object key, Object value) {
        String sKey = key.toString();
        Object old = request.getAttribute(sKey);
        request.setAttribute(sKey, value);
        return old;
    }

    public Object remove(Object key) {
        String sKey = key.toString();
        Object old = request.getAttribute(sKey);
        request.removeAttribute(sKey);
        return old;
    }

    public Set entrySet() {
        Set entries = new HashSet();
        for (Enumeration e = request.getAttributeNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            entries.add(new BaseMap.Entry(name, request.getAttribute(name)));
        }

        return entries;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof RequestMap)) {
            return false;
        }

        return super.equals(obj);
    }
}
