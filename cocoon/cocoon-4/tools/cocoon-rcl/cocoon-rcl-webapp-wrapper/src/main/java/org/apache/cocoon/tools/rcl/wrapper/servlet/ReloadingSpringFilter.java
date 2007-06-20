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
 * classpath occurs. 
 * 
 * @version $Id$
 */
public class ReloadingSpringFilter implements Filter {
    
    private final Log log = LogFactory.getLog(ReloadingSpringFilter.class);    
    
    private static final String WEB_INF_RCLWRAPPER_PROPERTIES = "/WEB-INF/cocoon/rclwrapper.properties";         

    private FilterConfig config;

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException,
            ServletException {
        
        if(isReloadingEnabled() && CocoonReloadingListener.isReload()) {
            synchronized (this) {        
                log.info("Performing a reload of the Spring application context.");
                // load the spring context loader from the reloading classloader
                ClassLoader cl = ReloadingClassloaderManager.getClassLoader(config.getServletContext());
                Object reloader = null;
                try {
                    reloader = cl.loadClass("org.apache.cocoon.tools.rcl.springreloader.SpringReloader").newInstance();
                } catch (Exception e) {
                    throw new ServletException("Can't create SpringReloader.", e);
                }
                try {
                    Method reloadMethod = reloader.getClass().getMethod("reload", new Class[]{ServletContext.class} ); 
                    reloadMethod.invoke(reloader, new Object[]{config.getServletContext()});
                } catch(Exception e) {
                    new ServletException("Problems occurred, while invoking the SpringReloader reload method.", e);
                }
            }
        }
        // continue processing the request
        filterChain.doFilter(req, res);            
    }

    public void destroy() {
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        this.config = filterConfig;
    }
    
    private boolean isReloadingEnabled() throws IOException {
        Properties rclProps = new Properties();
        rclProps.load(this.config.getServletContext().getResourceAsStream(WEB_INF_RCLWRAPPER_PROPERTIES));
        String reloadingEnabled = rclProps.getProperty("reloading.spring.enabled", "true");
        return reloadingEnabled.trim().toLowerCase().equals("true");
    }
 
}