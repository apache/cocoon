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
package org.apache.cocoon.environment.mock;

import java.util.Enumeration;
import java.util.Hashtable;

import junit.framework.AssertionFailedError;

import org.apache.cocoon.environment.Session;

public class MockSession implements Session {

    private long creationtime = System.currentTimeMillis();
    private String id = "MockSession";
    private long lastaccessedtime = System.currentTimeMillis();
    private int maxinactiveinterval = -1;
    private Hashtable attributes = new Hashtable();
    private boolean valid = true;

    public long getCreationTime() {
        checkValid();
        return creationtime;
    }
  
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        checkValid();
        return id;
    }

    public long getLastAccessedTime() {
        checkValid();
        return lastaccessedtime;
    }

    public void setMaxInactiveInterval(int interval) {
        checkValid();
        this.maxinactiveinterval = interval;
    }

    public int getMaxInactiveInterval() {
        checkValid();
        return maxinactiveinterval;
    }

    public Object getAttribute(String name) {
        checkValid();
        return attributes.get(name);
    }

    public Enumeration getAttributeNames() {
        checkValid();
        return attributes.keys();
    }

    public void setAttribute(String name, Object value) {
        checkValid();
        attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        checkValid();
        attributes.remove(name);
    }

    public void invalidate() {
        checkValid();
        this.valid = false;
    }

    public boolean isNew() {
        checkValid();
        return false;
    }

    private void checkValid() throws IllegalStateException {
        if (!valid)
            throw new AssertionFailedError("session has been invalidated!");
    }

    public boolean isValid() {
        return valid;
    }
}

