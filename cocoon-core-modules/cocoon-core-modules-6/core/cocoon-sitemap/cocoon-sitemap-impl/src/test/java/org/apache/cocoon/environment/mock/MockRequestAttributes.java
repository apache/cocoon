/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.environment.mock;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.cocoon.environment.Request;
import org.springframework.web.context.request.RequestAttributes;

/**
 * This is an implementation of Springs {@link RequestAttributes} based
 * on the Cocoon request interface.
 * Request scope is mapped to Cocoon request scope, session scope to Cocoon's
 * request global scope and global session scope to session scope!
 *
 * @version $Id$
 * @since 2.2
 */
public class MockRequestAttributes implements RequestAttributes {

    final protected Request request;

    final protected Map<String, Runnable> callbacks = new HashMap<String, Runnable>();

    public MockRequestAttributes(Request request) {
        this.request = request;
    }

    /**
     * @see org.springframework.web.context.scope.RequestAttributes#getAttribute(java.lang.String, int)
     */
    @Override
    public Object getAttribute(String key, int scope) {
        if (scope == RequestAttributes.SCOPE_REQUEST) {
            return this.request.getLocalAttribute(key);
        }
        if (scope == RequestAttributes.SCOPE_SESSION) {
            return this.request.getAttribute(key);
        }
        final HttpSession session = this.request.getSession(false);
        if (session != null) {
            return session.getAttribute(key);
        }
        return null;
    }

    /**
     * @see org.springframework.web.context.scope.RequestAttributes#getSessionMutex()
     */
    @Override
    public HttpSession getSessionMutex() {
        return this.request.getSession();
    }

    /**
     * @see org.springframework.web.context.scope.RequestAttributes#removeAttribute(java.lang.String, int)
     */
    @Override
    public void removeAttribute(String key, int scope) {
        if (scope == RequestAttributes.SCOPE_REQUEST) {
            this.request.removeLocalAttribute(key);
        }
        if (scope == RequestAttributes.SCOPE_SESSION) {
            this.request.removeAttribute(key);
        }
        if (scope == RequestAttributes.SCOPE_GLOBAL_SESSION) {
            final HttpSession session = this.request.getSession(false);
            if (session != null) {
                session.removeAttribute(key);
            }
        }
    }

    /**
     * @see org.springframework.web.context.scope.RequestAttributes#setAttribute(java.lang.String, java.lang.Object,
     * int)
     */
    @Override
    public void setAttribute(String key, Object value, int scope) {
        if (scope == RequestAttributes.SCOPE_REQUEST) {
            this.request.setLocalAttribute(key, value);
        }
        if (scope == RequestAttributes.SCOPE_SESSION) {
            this.request.setAttribute(key, value);
        }
        if (scope == RequestAttributes.SCOPE_GLOBAL_SESSION) {
            final HttpSession session = this.request.getSession(true);
            session.setAttribute(key, value);
        }
    }

    /**
     * @see org.springframework.web.context.request.RequestAttributes#getSessionId()
     */
    @Override
    public String getSessionId() {
        return this.request.getSession().getId();
    }

    /**
     * @see org.springframework.web.context.request.RequestAttributes#registerDestructionCallback(java.lang.String,
     * java.lang.Runnable, int)
     */
    @Override
    public void registerDestructionCallback(String name, Runnable task, int scope) {
        this.callbacks.put(name, task);
    }

    public void requestCompleted() {
        final Iterator<Runnable> i = this.callbacks.values().iterator();
        while (i.hasNext()) {
            final Runnable task = i.next();
            task.run();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public String[] getAttributeNames(int scope) {
        Enumeration<String> names = null;
        if (scope == RequestAttributes.SCOPE_REQUEST) {
            names = this.request.getLocalAttributeNames();
        } else if (scope == RequestAttributes.SCOPE_SESSION) {
            names = this.request.getAttributeNames();
        } else {
            final HttpSession session = this.request.getSession(false);
            if (session != null) {
                names = session.getAttributeNames();
            }
        }
        if (names == null) {
            return new String[0];
        }
        List<String> attributeNames = Collections.list(names);
        return attributeNames.toArray(new String[attributeNames.size()]);
    }

    @Override
    public Object resolveReference(String string) {
        return null;
    }

}
