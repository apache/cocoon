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
package org.apache.cocoon.environment.impl;

import org.apache.cocoon.environment.Session;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * Session attributes map
 *
 * @version $Id$
 */
public class SessionMap extends BaseMap {

    private Session session;


    public SessionMap(Session session) {
        this.session = session;
    }

    public Object get(Object key) {
        return session.getAttribute(key.toString());
    }

    public Object put(Object key, Object value) {
        String sKey = key.toString();
        Object old = session.getAttribute(sKey);
        session.setAttribute(sKey, value);
        return old;
    }

    public Object remove(Object key) {
        String sKey = key.toString();
        Object old = session.getAttribute(sKey);
        session.removeAttribute(sKey);
        return old;
    }

    public Set entrySet() {
        Set entries = new HashSet();
        for (Enumeration e = session.getAttributeNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            entries.add(new BaseMap.Entry(name, session.getAttribute(name)));
        }

        return entries;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SessionMap)) {
            return false;
        }

        return super.equals(obj);
    }
}
