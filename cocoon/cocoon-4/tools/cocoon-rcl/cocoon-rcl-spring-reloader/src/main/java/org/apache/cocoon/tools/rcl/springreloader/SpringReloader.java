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
package org.apache.cocoon.tools.rcl.springreloader;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.ServletContextFactoryBean;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * <p>
 * This class is used by the ReloadingSpringFilter to reload a Spring
 * web application context. In order not to run into {@link NoClassDefFoundError}s,
 * is is important that this class is loaded through the reloading classloader.
 * </p>
 * 
 * @version $Id$
 */
public class SpringReloader {
    
    private final Log log = LogFactory.getLog(SpringReloader.class);      

    public synchronized void reload(ServletContext servletContext) {
        ContextLoader springContextLoader = new ContextLoader();

        // close old Spring application context
        XmlWebApplicationContext oldAc = (XmlWebApplicationContext) 
                WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        if(log.isDebugEnabled()) {                     
            this.log.debug("Removing old application context: " + oldAc);
        }
        oldAc.close();        
        servletContext.removeAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        // create the new Spring application context
        ServletContextFactoryBean b = new ServletContextFactoryBean();
        b.setServletContext(servletContext);
        XmlWebApplicationContext xac = (XmlWebApplicationContext) springContextLoader.
                initWebApplicationContext(servletContext);
        if(log.isDebugEnabled()) {
            log.debug("Reloading Spring application context: " + xac);
        }
    }
    
}
