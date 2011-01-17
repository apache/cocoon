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
package org.apache.cocoon.tools.rcl.wrapper.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * This filter can be used as a wrapper around a "real" filter to
 * support the reloading class loader.
 *
 * @version $Id$
 */
public class ReloadingServletFilter implements Filter {

    protected Filter filter;
    protected ServletContext context;

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        if (this.filter != null) {
            final ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(ReloadingClassloaderManager.getClassLoader(this.context));
                this.filter.destroy();
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        }
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
        if ( this.filter != null ) {
            final ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(ReloadingClassloaderManager.getClassLoader(this.context));

                CocoonReloadingListener.enableConsoleOutput();
                this.filter.doFilter(request, response, chain);
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        }
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {
        String filterName = config.getInitParameter("filter-class");
        this.context = config.getServletContext();
        if (filterName == null) {
            throw new ServletException("ReloadingServletFilter: 'filter-class' parameter is missing.");
        }

        // Create the filter
        try {
            Class filterClass = ReloadingClassloaderManager.getClassLoader(this.context).loadClass(filterName);
            this.filter = (Filter) filterClass.newInstance();

        } catch (Exception e) {
            throw new ServletException("Cannot load filter " + filterName, e);
        }

        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(ReloadingClassloaderManager.getClassLoader(this.context));

            // Initialize the actual filter
            this.filter.init(config);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
}
