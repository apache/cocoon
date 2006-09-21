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
package org.apache.cocoon.core.container.spring;

import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.springframework.web.context.scope.RequestAttributes;

/**
 * This is an implementation of Springs {@link RequestAttributes} based
 * on the Cocoon request interface.
 * Request scope is mapped to Cocoon request scope, session scope to Cocoon's
 * request global scope and global session scope to session scope!
 *
 * @version $Id$
 * @since 2.2
 */
public class CocoonRequestAttributes implements RequestAttributes {

    final protected Request request;

    public CocoonRequestAttributes(Request r) {
        this.request = r;
    }

    /**
     * @see org.springframework.web.context.scope.RequestAttributes#getAttribute(java.lang.String, int)
     */
    /**
     * @see org.springframework.web.context.scope.RequestAttributes#getAttribute(java.lang.String, int)
     */
    public Object getAttribute(String key, int scope) {
        if ( scope == RequestAttributes.SCOPE_REQUEST ) {
            return this.request.getAttribute(key, Request.REQUEST_SCOPE);
        }
        if ( scope == RequestAttributes.SCOPE_SESSION ) {
            return this.request.getAttribute(key, Request.GLOBAL_SCOPE);
        }
        final Session session = this.request.getSession(false);
        if ( session != null ) {
            return session.getAttribute(key);
        }
        return null;
    }

    /**
     * @see org.springframework.web.context.scope.RequestAttributes#getSessionMutex()
     */
    public Object getSessionMutex() {
        return this.request.getSession();
    }

    /**
     * @see org.springframework.web.context.scope.RequestAttributes#removeAttribute(java.lang.String, int)
     */
    public void removeAttribute(String key, int scope) {
        if ( scope == RequestAttributes.SCOPE_REQUEST ) {
            this.request.removeAttribute(key, Request.REQUEST_SCOPE);
        }
        if ( scope == RequestAttributes.SCOPE_SESSION ) {
            this.request.removeAttribute(key, Request.GLOBAL_SCOPE);
        }
        if ( scope == RequestAttributes.SCOPE_GLOBAL_SESSION ) {
            final Session session = this.request.getSession(false);
            if ( session != null ) {
                session.removeAttribute(key);
            }
        }
    }

    /**
     * @see org.springframework.web.context.scope.RequestAttributes#setAttribute(java.lang.String, java.lang.Object, int)
     */
    public void setAttribute(String key, Object value, int scope) {
        if ( scope == RequestAttributes.SCOPE_REQUEST ) {
            this.request.setAttribute(key, value, Request.REQUEST_SCOPE);
        }
        if ( scope == RequestAttributes.SCOPE_SESSION ) {
            this.request.setAttribute(key, value, Request.GLOBAL_SCOPE);
        }
        if ( scope == RequestAttributes.SCOPE_GLOBAL_SESSION ) {
            final Session session = this.request.getSession(true);
            session.setAttribute(key, value);
        }
    }

}
