/*
 * Copyright 2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring.avalon;

import javax.servlet.ServletContext;

import org.apache.cocoon.core.container.spring.ConfigurationInfo;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResourceLoader;

/**
 *
 * @since 2.2
 * @version $Id$
 */
public class AvalonConfigurator
    implements FactoryBean, ServletContextAware {

    /** The servlet context. */
    protected ServletContext servletContext;

    /** The configuration. */
    protected ConfigurationInfo ConfigurationInfo;

    /** The avalon configuration location. */
    protected String location = "/WEB-INF/cocoon/cocoon.xconf";

    /**
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext sContext) {
        this.servletContext = sContext;
    }

    protected void init()
    throws Exception {
        System.out.println("Reading config");
        this.ConfigurationInfo = ConfigReader.readConfiguration(getLocation(), new ServletContextResourceLoader(this.servletContext));
        System.out.println("Read config");
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        return this.ConfigurationInfo;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType() {
        return ConfigurationInfo.class;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
