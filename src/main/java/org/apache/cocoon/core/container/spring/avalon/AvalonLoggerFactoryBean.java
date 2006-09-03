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
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.configuration.Settings;
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
    protected String loggingConfiguration;

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
        loggerManager.enableLogging(new LoggerWrapper(this.log));
        loggerManager.contextualize(subcontext);

        // Configure the log4j manager
        String loggerConfig = this.loggingConfiguration;
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

        this.logger = loggerManager.getLoggerForCategory("cocoon");
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

    public void setLoggingConfiguration(String loggingConfiguration) {
        this.loggingConfiguration = loggingConfiguration;
    }

    protected static final class LoggerWrapper implements Logger {

        protected final Log log;

        public LoggerWrapper(Log l) {
            this.log = l;
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#debug(java.lang.String, java.lang.Throwable)
         */
        public void debug(String arg0, Throwable arg1) {
            log.debug(arg0, arg1);
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#debug(java.lang.String)
         */
        public void debug(String arg0) {
            log.debug(arg0);
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#error(java.lang.String, java.lang.Throwable)
         */
        public void error(String arg0, Throwable arg1) {
            log.error(arg0, arg1);
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#error(java.lang.String)
         */
        public void error(String arg0) {
            log.error(arg0);
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#fatalError(java.lang.String, java.lang.Throwable)
         */
        public void fatalError(String arg0, Throwable arg1) {
            log.fatal(arg0, arg1);
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#fatalError(java.lang.String)
         */
        public void fatalError(String arg0) {
            log.fatal(arg0);
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#info(java.lang.String, java.lang.Throwable)
         */
        public void info(String arg0, Throwable arg1) {
            log.info(arg0, arg1);
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#info(java.lang.String)
         */
        public void info(String arg0) {
            log.info(arg0);
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#isDebugEnabled()
         */
        public boolean isDebugEnabled() {
            return log.isDebugEnabled();
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#isErrorEnabled()
         */
        public boolean isErrorEnabled() {
            return log.isErrorEnabled();
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#isInfoEnabled()
         */
        public boolean isInfoEnabled() {
            return log.isInfoEnabled();
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#isWarnEnabled()
         */
        public boolean isWarnEnabled() {
            return log.isWarnEnabled();
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#warn(java.lang.String, java.lang.Throwable)
         */
        public void warn(String arg0, Throwable arg1) {
            log.warn(arg0, arg1);
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#warn(java.lang.String)
         */
        public void warn(String arg0) {
            log.warn(arg0);
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#getChildLogger(java.lang.String)
         */
        public Logger getChildLogger(String arg0) {
            return this;
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#isFatalErrorEnabled()
         */
        public boolean isFatalErrorEnabled() {
            return this.log.isFatalEnabled();
        }
    }
}
