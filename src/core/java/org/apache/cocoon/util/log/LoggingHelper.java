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
package org.apache.cocoon.util.log;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.excalibur.logger.Log4JLoggerManager;
import org.apache.avalon.excalibur.logger.LogKitLoggerManager;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.configuration.ConfigurationBuilder;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.core.source.SimpleSourceResolver;
import org.apache.cocoon.matching.helpers.WildcardHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.TraversableSource;
import org.apache.log.ErrorHandler;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Priority;
import org.apache.log.util.DefaultErrorHandler;
import org.apache.log4j.LogManager;

/**
* TODO Delete this class as soon as the CoreUtil is used
* @version SVN $Id$
*/
public class LoggingHelper {
    
    /** Parameter map for the context protocol */
    protected static final Map CONTEXT_PARAMETERS = Collections.singletonMap("force-traversable", Boolean.TRUE);

    protected Logger log;
    protected LoggerManager loggerManager;

    public LoggingHelper(Settings settings, 
                         LogTarget defaultTarget, 
                         Context context) {
        String logLevel = settings.getBootstrapLogLevel();
        if ( logLevel == null ) {
            logLevel = "INFO";
        }
        
        String accesslogger = settings.getAccessLogger();
        if ( accesslogger == null ) {
            accesslogger = "cocoon";
        }

        final Priority logPriority = Priority.getPriorityForName(logLevel);

        final Hierarchy defaultHierarchy = Hierarchy.getDefaultHierarchy();
        final ErrorHandler errorHandler = new DefaultErrorHandler();
        defaultHierarchy.setErrorHandler(errorHandler);
        defaultHierarchy.setDefaultLogTarget(defaultTarget);
        defaultHierarchy.setDefaultPriority(logPriority);
        final Logger logger = new LogKitLogger(Hierarchy.getDefaultHierarchy().getLoggerFor(""));

        // we can't pass the context-root to our resolver
        Object value = null;
        try {
            value = context.get("context-root");
            ((DefaultContext)context).put("context-root", null);
        } catch ( ContextException ignore ) {
            // not available
        }
        // Create our own resolver
        SimpleSourceResolver resolver = new SimpleSourceResolver();
        resolver.enableLogging(logger);
        try {
            resolver.contextualize(context);
        } catch (ContextException ce) {
            throw new CascadingRuntimeException("Cannot setup source resolver.", ce);
        }
        if ( value != null ) {
            ((DefaultContext)context).put("context-root", value);
        }
        String loggerManagerClass = settings.getLoggerClassName();
        if ( loggerManagerClass == null ) {
            loggerManagerClass = LogKitLoggerManager.class.getName();
        }

        // the log4j support requires currently that the log4j system is already configured elsewhere

        final LoggerManager loggerManager = newLoggerManager(loggerManagerClass, defaultHierarchy);
        ContainerUtil.enableLogging(loggerManager, logger);

        try {
            ContainerUtil.contextualize(loggerManager, context);
            this.loggerManager = loggerManager;

            if (loggerManager instanceof Configurable) {
                //Configure the logkit management
                String logkitConfig = settings.getLoggingConfiguration();
                if ( logkitConfig == null ) {
                    logkitConfig = "/WEB-INF/logkit.xconf";
                }

                Source source = null;
                try {
                    source = resolver.resolveURI(logkitConfig);
                    final ConfigurationBuilder builder = new ConfigurationBuilder(settings);
                    final Configuration conf = builder.build(source.getInputStream());
                    final DefaultConfiguration categories = (DefaultConfiguration)conf.getChild("categories");
                    final DefaultConfiguration targets = (DefaultConfiguration)conf.getChild("targets");
                    final DefaultConfiguration factories = (DefaultConfiguration)conf.getChild("factories");
                    
                    // now process includes
                    final Configuration[] children = conf.getChildren("include");
                    for(int i=0; i<children.length; i++) {
                        String directoryURI = children[i].getAttribute("dir");                    
                        final String pattern = children[i].getAttribute("pattern", null);
                        int[] parsedPattern = null;
                        if ( pattern != null ) {
                            parsedPattern = WildcardHelper.compilePattern(pattern);
                        }
                        Source directory = null;
                        try {
                            directory = resolver.resolveURI(directoryURI, source.getURI(), CONTEXT_PARAMETERS);
                            if ( directory instanceof TraversableSource ) {
                                final Iterator c = ((TraversableSource)directory).getChildren().iterator();
                                while ( c.hasNext() ) {
                                    final Source s = (Source)c.next();
                                    if ( parsedPattern == null || this.match(s.getURI(), parsedPattern) ) {
                                        final Configuration includeConf = builder.build(s.getInputStream());
                                        // add targets and categories
                                        categories.addAllChildren(includeConf.getChild("categories"));
                                        targets.addAllChildren(includeConf.getChild("targets"));
                                        factories.addAllChildren(includeConf.getChild("factories"));
                                    }
                                }
                            } else {
                                throw new ConfigurationException("Include.dir must point to a directory, '" + directory.getURI() + "' is not a directory.'");
                            }
                        } catch (IOException ioe) {
                            throw new ConfigurationException("Unable to read configurations from " + directoryURI);
                        } finally {
                            resolver.release(directory);
                        }
                        
                        // finally remove include
                        ((DefaultConfiguration)conf).removeChild(children[i]);
                    }
                    // override log level?
                    if ( settings.getOverrideLogLevel() != null ) {
                        this.overrideLogLevel(conf.getChild("categories"), settings.getOverrideLogLevel());
                    }
                    ContainerUtil.configure(loggerManager, conf);
                } finally {
                    resolver.release(source);
                }
            }

            // let's configure log4j
            final String log4jConfig = settings.getLog4jConfiguration();
            if ( log4jConfig != null ) {
                final Log4JConfigurator configurator = new Log4JConfigurator(context);

                Source source = null;
                try {
                    source = resolver.resolveURI(log4jConfig);
                    configurator.doConfigure(source.getInputStream(), LogManager.getLoggerRepository());
                } finally {
                    resolver.release(source);
                }
            }

            ContainerUtil.initialize(loggerManager);
        } catch (Exception e) {
            errorHandler.error("Could not set up Cocoon Logger, will use screen instead", e, null);
        }

        this.log = this.loggerManager.getLoggerForCategory(accesslogger);
    }

    private LoggerManager newLoggerManager(String loggerManagerClass, 
                                           Hierarchy hierarchy) {
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

    protected void overrideLogLevel(Configuration root, String value) {
        Configuration[] c = root.getChildren("category");
        for(int i=0;i<c.length;i++) {
            ((DefaultConfiguration)c[i]).setAttribute("log-level", value);
            this.overrideLogLevel(c[i], value);
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

    private boolean match(String uri, int[] parsedPattern ) {
        int pos = uri.lastIndexOf('/');
        if ( pos != -1 ) {
            uri = uri.substring(pos+1);
        }
        return WildcardHelper.match(null, uri, parsedPattern);      
    }
    
}
