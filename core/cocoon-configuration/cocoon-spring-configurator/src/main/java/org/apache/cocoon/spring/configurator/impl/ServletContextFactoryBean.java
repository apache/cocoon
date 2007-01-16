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
package org.apache.cocoon.spring.configurator.impl;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.context.ServletContextAware;

/**
 * Simple Spring factory bean which provides the servlet context as a bean.
 * This avoids a dependence to Springs ServletContextAware interface.
 *
 * @version $Id$
 * @since 1.0
 */
public class ServletContextFactoryBean implements FactoryBean, ServletContextAware {

    protected static ServletContext servletContext;

    public static ServletContext getServletContext() {
        if ( servletContext == null ) {
            throw new RuntimeException("ServletContext in ServletContextFactoryBean is not initialized yet. Check your configuration.");
        }
        return servletContext;
    }

    /**
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext context) {
        servletContext = context;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        return servletContext;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType() {
        return ServletContext.class;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }
}
