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

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.core.CoreUtil;
import org.apache.cocoon.core.container.util.ComponentContext;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.context.ServletContextAware;

/**
 *
 * @since 2.2
 * @version $Id$
 */
public class AvalonContextFactoryBean
    implements FactoryBean, ServletContextAware {

    /** The servlet context. */
    protected ServletContext servletContext;

    /** The settings. */
    protected Settings settings;

    protected Context context;

    /**
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext sContext) {
        this.servletContext = sContext;
    }

    protected void init()
    throws Exception {
        final DefaultContext appContext = new ComponentContext();

        // add root url
        String contextUrl = CoreUtil.getContextUrl(this.servletContext, "/WEB-INF/web.xml");
        CoreUtil.addSourceResolverContext(appContext, servletContext, contextUrl);

        // add the Avalon context attributes that are contained in the settings
        CoreUtil.addSettingsContext(appContext, settings);
        this.context = appContext;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        return this.context;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType() {
        return Context.class;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }
}
