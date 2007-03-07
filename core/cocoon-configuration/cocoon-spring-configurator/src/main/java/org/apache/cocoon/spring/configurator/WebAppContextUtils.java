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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.spring.configurator;

import org.apache.cocoon.spring.configurator.impl.ServletContextFactoryBean;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Utility class to manage hierarchical application contexts.
 *
 * @version $Id$
 * @since 1.0
 */
public abstract class WebAppContextUtils {

    /** The name of the request attribute containing the current bean factory. */
    public static final String CONTAINER_REQUEST_ATTRIBUTE = WebAppContextUtils.class.getName();

    /**
     * Get the current web application context.
     * @throws IllegalStateException if no WebApplicationContext could not be found
     * @return The current web application context.
     */
    public static WebApplicationContext getCurrentWebApplicationContext() {
        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return getCurrentWebApplicationContext(attributes);
    }

    /**
     * Return the current web application context or if the attributes are null, the parent context.
     * @param attributes     The request attributes.
     * @throws IllegalStateException if no WebApplicationContext could not be found
     * @return The web application context.
     */
    protected static WebApplicationContext getCurrentWebApplicationContext(RequestAttributes attributes) {
        if ( attributes != null ) {
            if (attributes.getAttribute(CONTAINER_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST) != null) {
                return (WebApplicationContext) attributes.getAttribute(CONTAINER_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
            }
        }
        // return the root context
        return WebApplicationContextUtils.getRequiredWebApplicationContext(ServletContextFactoryBean.getServletContext());
    }

    /**
     * Notify about entering this context.
     * @param webAppContext The current web application context.
     * @return A handle which should be passed to {@link #leavingContext(WebApplicationContext, Object)}.
     */
    public static Object enteringContext(WebApplicationContext webAppContext) {
        // get request attributes
        final RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        final WebApplicationContext oldContext = (WebApplicationContext)attributes.getAttribute(CONTAINER_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        final ContextInfo info = new ContextInfo(oldContext, oldClassLoader);
        attributes.setAttribute(CONTAINER_REQUEST_ATTRIBUTE, webAppContext, RequestAttributes.SCOPE_REQUEST);
        Thread.currentThread().setContextClassLoader(webAppContext.getClassLoader());
        return info;
    }

    /**
     * Notify about leaving this context.
     * @param webAppContext The current web application context.
     * @param handle     The returned handle from {@link #enteringContext(WebApplicationContext)}.
     */
    public static void leavingContext(WebApplicationContext webAppContext, Object handle) {
        if ( !(handle instanceof ContextInfo) ) {
            throw new IllegalArgumentException("Handle must be an instance of ContextInfo and not " + handle);
        }
        final ContextInfo info = (ContextInfo)handle;
        // get request attributes
        final RequestAttributes attributes = RequestContextHolder.currentRequestAttributes();
        // restore class loader
        Thread.currentThread().setContextClassLoader(info.classLoader);
        // restore previous web application context (or remove attribute)
        if ( info.webAppContext == null ) {
            attributes.removeAttribute(CONTAINER_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        } else {
            attributes.setAttribute(CONTAINER_REQUEST_ATTRIBUTE, info.webAppContext, RequestAttributes.SCOPE_REQUEST);
        }
    }

    /**
     * Private bean keeping track of the class loader and web application context.
     */
    protected static final class ContextInfo {
        public final ClassLoader classLoader;
        public final WebApplicationContext webAppContext;

        public ContextInfo(WebApplicationContext w, ClassLoader c) {
            this.classLoader = c;
            this.webAppContext = w;
        }
    }
}
