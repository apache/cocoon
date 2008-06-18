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
package org.apache.cocoon.environment.background;

import org.apache.cocoon.environment.impl.AbstractSession;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.http.HttpSession;

/**
 *
 * Command-line version of Http Session.
 *
 * @version $Id$
 */
public final class BackgroundSession
extends AbstractSession {

    private long creationTime = System.currentTimeMillis();

    private Hashtable attributes = new Hashtable();

    public BackgroundSession() {
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public String getId() {
        return "1";
    }

    public long getLastAccessedTime() {
        return this.creationTime;
    }

    public void setMaxInactiveInterval(int interval) {
        // ignored
    }

    public int getMaxInactiveInterval() {
        return -1;
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public Enumeration getAttributeNames() {
        return this.attributes.keys();
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    public void invalidate() {
        this.attributes.clear();
        invalidateSession();
    }

    public boolean isNew() {
        return false;
    }

    protected static BackgroundSession session;

    /**
     * Get the current session object - if available
     */
    public static HttpSession getSession(boolean create) {
        if (create && session == null) {
            session = new BackgroundSession();
        }
        return session;
    }

    /**
     * Invalidate the current session
     */
    public static void invalidateSession() {
        session = null;
    }

}

