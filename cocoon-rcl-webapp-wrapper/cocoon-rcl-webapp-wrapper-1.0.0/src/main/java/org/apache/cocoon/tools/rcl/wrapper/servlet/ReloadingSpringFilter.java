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
import java.lang.reflect.Method;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This servlet filter reloads the Spring application context whenever a relevant change in the
 * classpath occurs. It uses the Spring reloader, which has to be loaded by the same classloader
 * as the Spring application context. In order to get access to it without having to pull all
 * dependencies into the this module, the Java reflection API is used.
 *
 * @version $Id$
 */
public class ReloadingSpringFilter implements Filter {

    private final Log log = LogFactory.getLog(ReloadingSpringFilter.class);

    private FilterConfig config;

    public void destroy() {
        // do nothing
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException,
    ServletException {

        if(this.isReloadingEnabled() && CocoonReloadingListener.isReload()) {
            synchronized (this) {
                this.log.info("Performing a reload of the Spring application context.");
                // load the spring context loader from the reloading classloader
                ClassLoader cl = ReloadingClassloaderManager.getClassLoader(this.config.getServletContext());
                Object reloader = null;
                try {
                    reloader = cl.loadClass("org.apache.cocoon.tools.rcl.springreloader.SpringReloader").newInstance();
                    Method reloadMethod = reloader.getClass().getMethod("reload", new Class[]{ServletContext.class} );
                    reloadMethod.invoke(reloader, new Object[]{this.config.getServletContext()});
                } catch (Exception e) {
                    throw new ServletException("Can't use SpringReloader.", e);
                }
            }
        }
        // continue processing the request
        filterChain.doFilter(req, res);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        this.config = filterConfig;
    }

    private boolean isReloadingEnabled() throws IOException {
        Properties rclProps = new Properties();
        rclProps.load(this.config.getServletContext().getResourceAsStream(Constants.WEB_INF_RCLWRAPPER_PROPERTIES));
        String reloadingEnabled = rclProps.getProperty(Constants.RELOADING_SPRING_ENABLED, "true");
        return reloadingEnabled.trim().toLowerCase().equals("true");
    }
}