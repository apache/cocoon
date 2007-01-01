/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.avalon.excalibur.logger.Log4JConfLoggerManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.util.avalon.CLLoggerWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.context.ServletContextAware;

/**
 * Spring factory bean to setup the Avalon logger.
 *
 * @since 2.2
 * @version $Id$
 */
public class AvalonLoggerFactoryBean
    implements FactoryBean, ServletContextAware {

    /** Logger (we use the same logging mechanism as Spring!) */
    protected final Log log = LogFactory.getLog(getClass());

    /** The servlet context. */
    protected ServletContext servletContext;

    /** The settings. */
    protected Settings settings;

    protected Logger logger;

    /** The logging configuration. */
    protected String configuration;

    /** The logging category. */
    protected String category;

    /**
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext sContext) {
        this.servletContext = sContext;
    }

    protected void init()
    throws Exception {
        // create log directory
        final File logSCDir = new File(settings.getWorkDirectory(), "cocoon-logs");
        logSCDir.mkdirs();

        // create an own context for the logger manager
        final DefaultContext subcontext = new SettingsContext(settings);
        subcontext.put("context-work", new File(settings.getWorkDirectory()));
        subcontext.put("log-dir", logSCDir.toString());
        subcontext.put("servlet-context", servletContext);

        final Log4JConfLoggerManager loggerManager = new Log4JConfLoggerManager();
        loggerManager.enableLogging(new CLLoggerWrapper(this.log));
        loggerManager.contextualize(subcontext);

        // Configure the log4j manager
        String loggerConfig = this.configuration;
        if ( loggerConfig != null && !loggerConfig.startsWith("/") ) {
            loggerConfig = '/' + loggerConfig;
        }
        if ( loggerConfig != null ) {
            final URL url = servletContext.getResource(loggerConfig);
            if ( url != null ) {
                final DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder(true);
                final Configuration conf = builder.build(servletContext.getResourceAsStream(loggerConfig));
                loggerManager.configure(conf);
            } else {
                this.log.warn("The logging configuration '" + loggerConfig + "' is not available.");
                loggerManager.configure(new DefaultConfiguration("empty"));
            }
        } else {
            loggerManager.configure(new DefaultConfiguration("empty"));
        }

        String loggingCategory = this.category;
        if ( loggingCategory == null ) {
            loggingCategory = "cocoon";
        }
        this.logger = loggerManager.getLoggerForCategory(loggingCategory);
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        return this.logger;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType() {
        return Logger.class;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public void setConfiguration(String loggingConfiguration) {
        this.configuration = loggingConfiguration;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
