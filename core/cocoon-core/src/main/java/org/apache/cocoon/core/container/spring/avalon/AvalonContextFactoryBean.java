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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.core.CoreUtil;
import org.apache.cocoon.core.container.util.ComponentContext;
import org.apache.cocoon.environment.http.HttpContext;
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

    /**
     * Create the Avalon context object.
     * @throws Exception
     */
    protected void init()
    throws Exception {
        // create new Avalon context
        final DefaultContext appContext = new ComponentContext();

        // add root url
        final String contextUrl = CoreUtil.getContextUrl(this.servletContext, "/WEB-INF/web.xml");
        try {
            appContext.put(ContextHelper.CONTEXT_ROOT_URL, new URL(contextUrl));
        } catch (MalformedURLException ignore) {
            // we simply ignore this
        }
    
        // add environment context and config
        appContext.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, new HttpContext(this.servletContext));

        // add the Avalon context attributes that are contained in the settings
        appContext.put(Constants.CONTEXT_WORK_DIR, new File(this.settings.getWorkDirectory()));
        appContext.put(Constants.CONTEXT_UPLOAD_DIR, new File(this.settings.getUploadDirectory()));
        appContext.put(Constants.CONTEXT_CACHE_DIR, new File(this.settings.getCacheDirectory()));
        appContext.put(Constants.CONTEXT_DEFAULT_ENCODING, this.settings.getFormEncoding());

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
