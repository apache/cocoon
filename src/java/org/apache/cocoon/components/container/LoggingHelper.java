/*
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.cocoon.components.container;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.excalibur.logger.Log4JLoggerManager;
import org.apache.avalon.excalibur.logger.LogKitLoggerManager;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.configuration.ConfigurationBuilder;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.util.log.CocoonLogFormatter;
import org.apache.cocoon.util.log.Log4JConfigurator;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;
import org.apache.log.ErrorHandler;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Priority;
import org.apache.log.util.DefaultErrorHandler;
import org.apache.log4j.LogManager;

/**
*
* @version SVN $Id$
*/
public class LoggingHelper {
    
    /** Parameter map for the context protocol */
    protected static final Map CONTEXT_PARAMETERS = Collections.singletonMap("force-traversable", Boolean.TRUE);

    protected final Settings settings;
    protected Logger log;
    protected LoggerManager loggerManager;
    protected final SourceResolver resolver;

    public LoggingHelper(Settings settings, SourceResolver resolver) {
        this.settings = settings;
        this.resolver = resolver;
    }
    
    /**
     * Set up the log level and path.  The default log level is
     * Priority.ERROR, although it can be overwritten by the parameter
     * "log-level".  The log system goes to both a file and the Servlet
     * container's log system.  Only messages that are Priority.ERROR
     * and above go to the servlet context.  The log messages can
     * be as restrictive (Priority.FATAL_ERROR and above) or as liberal
     * (Priority.DEBUG and above) as you want that get routed to the
     * file.
     */
    protected void initLogger(LogTarget defaultTarget, Context context) {
        final String logLevel = this.settings.get(this.settings.getLogLevel(), "INFO");
        
        final String accesslogger = this.settings.get(this.settings.getCocoonLogger(), "cocoon");

        final Priority logPriority = Priority.getPriorityForName(logLevel);

        final CocoonLogFormatter formatter = new CocoonLogFormatter();
        formatter.setFormat("%7.7{priority} %{time}   [%8.8{category}] " +
                            "(%{uri}) %{thread}/%{class:short}: %{message}\\n%{throwable}");
        final Hierarchy defaultHierarchy = Hierarchy.getDefaultHierarchy();
        final ErrorHandler errorHandler = new DefaultErrorHandler();
        defaultHierarchy.setErrorHandler(errorHandler);
        defaultHierarchy.setDefaultLogTarget(defaultTarget);
        defaultHierarchy.setDefaultPriority(logPriority);
        final Logger logger = new LogKitLogger(Hierarchy.getDefaultHierarchy().getLoggerFor(""));
        final String loggerManagerClass =
            this.settings.get(this.settings.getLoggerClassName(), LogKitLoggerManager.class.getName());

        // the log4j support requires currently that the log4j system is already configured elsewhere

        final LoggerManager loggerManager = newLoggerManager(loggerManagerClass, defaultHierarchy);
        ContainerUtil.enableLogging(loggerManager, logger);

        try {
            ContainerUtil.contextualize(loggerManager, context);
            this.loggerManager = loggerManager;

            if (loggerManager instanceof Configurable) {
                //Configure the logkit management
                String logkitConfig = this.settings.get(this.settings.getLoggingConfiguration(), "/WEB-INF/logkit.xconf");

                Source source = null;
                try {
                    source = this.resolver.resolveURI(logkitConfig);
                    final ConfigurationBuilder builder = new ConfigurationBuilder();
                    final Configuration conf = builder.build(source.getInputStream());
                    final Configuration[] children = conf.getChildren("include");
                    for(int i=0; i<children.length; i++) {
                        String directoryURI = children[i].getAttribute("dir");                    
                        final String ending = children[i].getAttribute("postfix", null);
                        Source directory = null;
                        try {
                            directory = this.resolver.resolveURI(directoryURI, source.getURI(), CONTEXT_PARAMETERS);
                            if ( directory instanceof TraversableSource ) {
                                final Iterator c = ((TraversableSource)directory).getChildren().iterator();
                                while ( c.hasNext() ) {
                                    final Source s = (Source)c.next();
                                    if ( ending == null || s.getURI().endsWith(ending) ) {
                                        final Configuration includeConf = builder.build(s.getInputStream());
                                        ((DefaultConfiguration)conf).addAllChildren(includeConf);
                                    }
                                }
                            } else {
                                throw new ConfigurationException("Include.dir must point to a directory, '" + directory.getURI() + "' is not a directory.'");
                            }
                        } catch (IOException ioe) {
                            throw new ConfigurationException("Unable to read configurations from " + directoryURI);
                        } finally {
                            this.resolver.release(directory);
                        }
                        
                        // finally remove include
                        ((DefaultConfiguration)conf).removeChild(children[i]);
                    }
                    ContainerUtil.configure(loggerManager, conf);
                } finally {
                    this.resolver.release(source);
                }
            }

            // let's configure log4j
            final String log4jConfig = this.settings.getLog4jConfiguration();
            if ( log4jConfig != null ) {
                final Log4JConfigurator configurator = new Log4JConfigurator(context);

                Source source = null;
                try {
                    source = this.resolver.resolveURI(log4jConfig);
                    configurator.doConfigure(source.getInputStream(), LogManager.getLoggerRepository());
                } finally {
                    this.resolver.release(source);
                }
            }

            ContainerUtil.initialize(loggerManager);
        } catch (Exception e) {
            errorHandler.error("Could not set up Cocoon Logger, will use screen instead", e, null);
        }

        this.log = this.loggerManager.getLoggerForCategory(accesslogger);
    }

    private LoggerManager newLoggerManager(String loggerManagerClass, Hierarchy hierarchy) {
        if (loggerManagerClass.equals(LogKitLoggerManager.class.getName())) {
            return new LogKitLoggerManager(hierarchy);
        } else if (loggerManagerClass.equals(Log4JLoggerManager.class.getName()) ||
                   loggerManagerClass.equalsIgnoreCase("LOG4J")) {
            return new Log4JLoggerManager();
        } else {
            try {
                Class clazz = Class.forName(loggerManagerClass);
                return (LoggerManager)clazz.newInstance();
            } catch (Exception e) {
                return new LogKitLoggerManager(hierarchy);
            }
        }
    }

    /**
     * @return Returns the log.
     */
    public Logger getLogger() {
        return this.log;
    }
    
    /**
     * @return Returns the loggerManager.
     */
    public LoggerManager getLoggerManager() {
        return this.loggerManager;
    }
}
