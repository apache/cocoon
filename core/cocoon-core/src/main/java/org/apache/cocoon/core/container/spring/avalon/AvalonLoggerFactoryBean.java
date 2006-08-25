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
import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.avalon.excalibur.logger.Log4JConfLoggerManager;
import org.apache.avalon.excalibur.logger.ServletLogger;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.core.container.util.ConfigurationBuilder;
import org.apache.cocoon.core.container.util.SettingsContext;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.web.context.ServletContextAware;

/**
 *
 * @since 2.2
 * @version $Id$
 */
public class AvalonLoggerFactoryBean
    implements FactoryBean, ServletContextAware {

    /** The servlet context. */
    protected ServletContext servletContext;

    /** The settings. */
    protected Settings settings;

    protected Logger logger;

    /** The logging configuration. */
    protected String loggingConfiguration;

    /**
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext sContext) {
        this.servletContext = sContext;
    }

    /**
     * Create a bootstrap logger that uses the servlet context
     * @param servletContext
     * @param logLevelString
     * @return the logger
     */
    protected Logger createBootstrapLogger(String logLevelString) {
        // create a bootstrap logger
        int logLevel;
        if ( "DEBUG".equalsIgnoreCase(logLevelString) ) {
            logLevel = ServletLogger.LEVEL_DEBUG;
        } else if ( "WARN".equalsIgnoreCase(logLevelString) ) {
            logLevel = ServletLogger.LEVEL_WARN;
        } else if ( "ERROR".equalsIgnoreCase(logLevelString) ) {
            logLevel = ServletLogger.LEVEL_ERROR;
        } else {
            logLevel = ServletLogger.LEVEL_INFO;
        }
        return new ServletLogger(this.servletContext, "Cocoon", logLevel);
    }

    protected void init()
    throws Exception {
        // create a bootstrap logger
        final String logLevelString = settings.getBootstrapLogLevel();
        final Logger bootstrapLogger = this.createBootstrapLogger(logLevelString);

        // create an own context for the logger manager
        final DefaultContext subcontext = new SettingsContext(settings);
        subcontext.put("context-work", new File(settings.getWorkDirectory()));
        final File logSCDir = new File(settings.getWorkDirectory(), "cocoon-logs");
        logSCDir.mkdirs();
        subcontext.put("log-dir", logSCDir.toString());
        subcontext.put("servlet-context", servletContext);

        final Log4JConfLoggerManager loggerManager = new Log4JConfLoggerManager();
        loggerManager.enableLogging(bootstrapLogger);
        loggerManager.contextualize(subcontext);

        // Configure the log4j manager
        String loggerConfig = this.loggingConfiguration;
        if ( loggerConfig != null && !loggerConfig.startsWith("/") ) {
            loggerConfig = '/' + loggerConfig;
        }
        if ( loggerConfig != null ) {
            final URL url = servletContext.getResource(loggerConfig);
            if ( url != null ) {
                final ConfigurationBuilder builder = new ConfigurationBuilder(settings);
                final Configuration conf = builder.build(servletContext.getResourceAsStream(loggerConfig));
                // override log level?
                if (settings.getOverrideLogLevel() != null) {
                    changeLogLevel(conf.getChildren(), settings.getOverrideLogLevel());
                }
                loggerManager.configure(conf);
            } else {
                bootstrapLogger.warn("The logging configuration '" + loggerConfig + "' is not available.");
                loggerManager.configure(new DefaultConfiguration("empty"));
            }
        } else {
            loggerManager.configure(new DefaultConfiguration("empty"));
        }

        String accesslogger = settings.getEnvironmentLogger();
        if (accesslogger == null) {
            accesslogger = "cocoon";
        }
        this.logger = loggerManager.getLoggerForCategory(accesslogger);
    }

    protected static void changeLogLevel(Configuration[] configs, String level) {
        for(int i=0; i<configs.length; i++) {
            if ( configs[i].getName().equals("priority") ) {
                // we now that this is a DefaultConfiguration
                ((DefaultConfiguration)configs[i]).setAttribute("value", level);
            }
            changeLogLevel(configs[i].getChildren(), level);
        }
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

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public String getLoggingConfiguration() {
        return loggingConfiguration;
    }

    public void setLoggingConfiguration(String loggingConfiguration) {
        this.loggingConfiguration = loggingConfiguration;
    }
}
