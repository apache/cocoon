/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingUtil;
import org.apache.cocoon.configuration.PropertyProvider;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.core.CoreUtil;
import org.apache.cocoon.environment.http.HttpContext;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * The Cocoon Servlet listener starts and stops Cocoon and makes
 * the Cocoon Core Container available via the servlet context.
 *
 * @version $Id$
 */
public class CocoonServletListener implements ServletContextListener {

    /** The core container. */
    protected ConfigurableBeanFactory cocoonBeanFactory;

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent event) {
        final ServletContext servletContext = event.getServletContext();
        servletContext.log("Destroying Apache Cocoon Core Container.");
        if (this.cocoonBeanFactory != null) {
            this.cocoonBeanFactory.destroySingletons();
            this.cocoonBeanFactory = null;
        }
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent event) {
        final ServletContext servletContext = event.getServletContext();
        servletContext.log("Initializing Apache Cocoon " + Constants.VERSION);

        // initialize settings
        ServletPropertyProvider env = new ServletPropertyProvider(servletContext);

        try {
            this.cocoonBeanFactory = CoreUtil.createRootContainer(new HttpContext(servletContext), env);
        } catch (Exception e) {
            servletContext.log("Error during initializing Apache Cocoon " + Constants.VERSION + " - aborting.");
            servletContext.log(e.getMessage());
            servletContext.log(ExceptionUtils.getFullStackTrace(e));
            throw new RuntimeException(e);
        }

        servletContext.setAttribute(ProcessingUtil.CONTAINER_CONTEXT_ATTR_NAME, this.cocoonBeanFactory);
        servletContext.log("Apache Cocoon " + Constants.VERSION + " is up and ready.");
    }

    protected static final class ServletPropertyProvider
    implements PropertyProvider {

        private final ServletContext context;
    
        public ServletPropertyProvider(ServletContext context) {
            this.context = context;
        }

        /**
         * @see org.apache.cocoon.configuration.PropertyProvider#getProperties(Settings, java.lang.String, java.lang.String)
         */
        public Properties getProperties(Settings settings, String runningMode, String path) {
            final Properties p = new Properties();
            if ( settings.getWorkDirectory() == null ) {
                final File workDir = (File)context.getAttribute("javax.servlet.context.tempdir");
                p.setProperty(Settings.KEY_WORK_DIRECTORY, workDir.getAbsolutePath());
            }
            return p;
        }
    }
}
