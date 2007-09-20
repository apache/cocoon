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
package org.apache.cocoon.blocks.util;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * @version $Id: ServletConfigurationWrapper.java 368000 2006-01-11 10:45:20Z
 *          danielf $
 * 
 */
public class ServletConfigurationWrapper implements ServletConfig {

    protected ServletConfig servletConfig;

    protected ServletContext servletContext;

    /**
     * @param servletConfig
     */
    public ServletConfigurationWrapper(ServletConfig servletConfig) {
        this(servletConfig, null);
    }

    /**
     * @param servletConfig
     * @param servletContext
     */
    public ServletConfigurationWrapper(ServletConfig servletConfig,
            ServletContext servletContext) {
        this.servletConfig = servletConfig;
        this.servletContext = servletContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletConfig#getServletName()
     */
    public String getServletName() {
        return this.servletConfig.getServletName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletConfig#getServletContext()
     */
    public ServletContext getServletContext() {
        return this.servletContext != null ? this.servletContext
                : this.servletConfig.getServletContext();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletConfig#getInitParameter(java.lang.String)
     */
    public String getInitParameter(String name) {
        return this.servletConfig.getInitParameter(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletConfig#getInitParameterNames()
     */
    public Enumeration getInitParameterNames() {
        return this.servletConfig.getInitParameterNames();
    }

}
