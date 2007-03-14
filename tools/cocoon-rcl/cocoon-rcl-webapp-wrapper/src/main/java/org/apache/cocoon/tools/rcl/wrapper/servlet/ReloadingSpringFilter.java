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
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.ServletContextFactoryBean;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * This servlet filter reloads the Spring application context whenever a relevant change in the
 * classpath occurs. 
 * 
 * @version $Id$
 */
public class ReloadingSpringFilter implements Filter {
    
    private final Log log = LogFactory.getLog(ReloadingSpringFilter.class);    

    private FilterConfig config;

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException,
            ServletException {
        
        if(CocoonReloadingListener.isReload()) {
            synchronized (this) {            
                // load the spring context loader from the reloading classloader
                ClassLoader cl = ReloadingClassloaderManager.getClassLoader(config.getServletContext());
                ContextLoader springContextLoader = null;
                try {
                    Class contextLoaderClass = cl.loadClass(ContextLoader.class.getName());
                    springContextLoader = (ContextLoader) contextLoaderClass.newInstance();
                } catch (Exception e) {
                    throw new ServletException("Cannot load class " + ContextLoader.class.getName(), e);
                }

                // close old Spring application context
                if(log.isDebugEnabled()) {                
                    ApplicationContext oldAc = WebApplicationContextUtils.getRequiredWebApplicationContext(this.config.getServletContext());
                    this.log.debug("Removing old application context: " + oldAc);      
                }
                springContextLoader.closeWebApplicationContext(this.config.getServletContext());
                this.config.getServletContext().removeAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

                // create the new Spring application context
                ServletContextFactoryBean b = new ServletContextFactoryBean();
                b.setServletContext(this.config.getServletContext());
                XmlWebApplicationContext xac = (XmlWebApplicationContext) springContextLoader.initWebApplicationContext(this.config.getServletContext());
                if(log.isDebugEnabled()) {
                    log.debug("Reloading Spring application context: " + xac);
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
 
}