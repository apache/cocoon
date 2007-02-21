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
package org.apache.cocoon.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/*
 * For now only some experimental stuff regarding the reload of the Spring application context.
 * 
 * @version $Id$
 */
public class ReloadingSpringFilter implements Filter {

    private FilterConfig config;

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException,
            ServletException {
        
        System.out.println("doFilter ReloadingSpringFilter!");
        
        if(CocoonReloadingListener.isReload()) {
            synchronized (this) {
                ApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(this.config.getServletContext());
                System.out.println("old appContext: " + appContext);
                
                ContextLoader springContextLoader = new ContextLoader();
                System.out.println("rsf: " + springContextLoader.getClass().getClassLoader());
                springContextLoader.closeWebApplicationContext(this.config.getServletContext());
                this.config.getServletContext().removeAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
                springContextLoader.initWebApplicationContext(this.config.getServletContext());
                
                appContext = WebApplicationContextUtils.getWebApplicationContext(this.config.getServletContext());
                System.out.println("new appContext: " + appContext);     
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
