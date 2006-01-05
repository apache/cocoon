/*
 * Copyright 2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.blocks;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletConfig;
import org.apache.avalon.excalibur.logger.Log4JConfLoggerManager;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.excalibur.logger.ServletLogger;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.configuration.ConfigurationBuilder;
import org.apache.cocoon.core.BootstrapEnvironment;
import org.apache.cocoon.core.CoreInitializationException;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.core.container.SingleComponentServiceManager;
import org.apache.cocoon.core.logging.CocoonLogKitLoggerManager;
import org.apache.cocoon.core.logging.PerRequestLoggerManager;
import org.apache.cocoon.core.logging.SettingsContext;
import org.apache.cocoon.core.source.SimpleSourceResolver;
import org.apache.cocoon.matching.helpers.WildcardHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;

/**
 * This is an utility class to create a new Cocoon instance.
 * 
 * TODO - Remove dependencies to LogKit and Log4J
 *
 * @version $Id$
 * @since 2.2
 */
public class LoggerUtil {

    /** Parameter map for the context protocol */
    private static final Map CONTEXT_PARAMETERS = Collections.singletonMap("force-traversable", Boolean.TRUE);

    private ServletConfig config;
	/** "legacy" support: create an avalon context. */
    private Context appContext;
    
    /** The settings. */
    private Settings settings;

    /** The root logger. */
    private Logger log;

    /** The logger manager. */
    private LoggerManager loggerManager;

    private File contextForWriting;

    /**
     * Setup a new instance.
     * @param config
     */
    public LoggerUtil(ServletConfig config, Context appContext, Settings settings) {
    	this.config = config;
    	this.appContext = appContext;
    	this.settings = settings;
    	// Init logger
		this.initLogger();
    }
    
    /**
     * Create a simple source resolver.
     */
    private SourceResolver createSourceResolver(Logger logger) {
        // Create our own resolver
        final SimpleSourceResolver resolver = new SimpleSourceResolver();
        resolver.enableLogging(logger);
        try {
            resolver.contextualize(this.appContext);
        } catch (ContextException ce) {
            throw new CoreInitializationException(
                    "Cannot setup source resolver.", ce);
        }
        return resolver;        
    }

    private void initLogger() {
        String logLevel = settings.getBootstrapLogLevel();
        if (logLevel == null) {
            logLevel = "INFO";
        }

        String accesslogger = settings.getEnvironmentLogger();
        if (accesslogger == null) {
            accesslogger = "cocoon";
        }

        // create bootstrap logger
        final BootstrapEnvironment.LogLevel level = BootstrapEnvironment.LogLevel.getLogLevelForName(logLevel);
        final Logger bootstrapLogger = new ServletLogger(this.config, level.getLevel());

        // Create our own resolver
        final SourceResolver resolver = this.createSourceResolver(bootstrapLogger);

        // create an own service manager for the logger manager
        final ServiceManager loggerManagerServiceManager = new SingleComponentServiceManager(
                 null, resolver, SourceResolver.ROLE);

        // create an own context for the logger manager
        final DefaultContext subcontext = new SettingsContext(this.appContext, this.settings);
        subcontext.put("context-work", new File(this.settings.getWorkDirectory()));
        if (this.contextForWriting == null) {
            File logSCDir = new File(this.settings.getWorkDirectory(), "log");
            logSCDir.mkdirs();
            subcontext.put("context-root", logSCDir.toString());
        } else {
            subcontext.put("context-root", this.contextForWriting.toString());
        }
        subcontext.put("servlet-context", this.config.getServletContext());

        String loggerManagerClass = settings.getLoggerManagerClassName();

        // the log4j support requires currently that the log4j system is already
        // configured elsewhere

        final LoggerManager loggerManager = this.newLoggerManager(loggerManagerClass);
        ContainerUtil.enableLogging(loggerManager, bootstrapLogger);

        try {
            ContainerUtil.contextualize(loggerManager, subcontext);
            ContainerUtil.service(loggerManager, loggerManagerServiceManager);

            this.loggerManager = loggerManager;

            if (loggerManager instanceof Configurable) {
                //Configure the logkit management
                String logkitConfig = settings.getLoggingConfiguration();

                if ( logkitConfig != null ) {
                    Source source = null;
                    try {
                        source = resolver.resolveURI(logkitConfig);
                        final ConfigurationBuilder builder = new ConfigurationBuilder(
                                settings);
                        final Configuration conf = builder.build(source.getInputStream());
                        final DefaultConfiguration categories = (DefaultConfiguration) conf
                                .getChild("categories");
                        final DefaultConfiguration targets = (DefaultConfiguration) conf
                                .getChild("targets");
                        final DefaultConfiguration factories = (DefaultConfiguration) conf
                                .getChild("factories");
    
                        // now process includes
                        final Configuration[] children = conf
                                .getChildren("include");
                        for (int i = 0; i < children.length; i++) {
                            String directoryURI = children[i].getAttribute("dir");
                            final String pattern = children[i].getAttribute(
                                    "pattern", null);
                            int[] parsedPattern = null;
                            if (pattern != null) {
                                parsedPattern = WildcardHelper
                                        .compilePattern(pattern);
                            }
                            Source directory = null;
                            try {
                                directory = resolver.resolveURI(directoryURI,
                                        source.getURI(), CONTEXT_PARAMETERS);
                                if (directory instanceof TraversableSource) {
                                    final Iterator c = ((TraversableSource) directory)
                                            .getChildren().iterator();
                                    while (c.hasNext()) {
                                        final Source s = (Source) c.next();
                                        if (parsedPattern == null
                                                || this.match(s.getURI(),
                                                        parsedPattern)) {
                                            final Configuration includeConf = builder
                                                    .build(s.getInputStream());
                                            // add targets and categories
                                            categories.addAllChildren(includeConf
                                                    .getChild("categories"));
                                            targets.addAllChildren(includeConf
                                                    .getChild("targets"));
                                            factories.addAllChildren(includeConf
                                                    .getChild("factories"));
                                        }
                                    }
                                } else {
                                    throw new ConfigurationException(
                                            "Include.dir must point to a directory, '"
                                                    + directory.getURI()
                                                    + "' is not a directory.'");
                                }
                            } catch (IOException ioe) {
                                throw new ConfigurationException(
                                        "Unable to read configurations from "
                                                + directoryURI);
                            } finally {
                                resolver.release(directory);
                            }
    
                            // finally remove include
                            ((DefaultConfiguration) conf).removeChild(children[i]);
                        }
                        // override log level?
                        if (settings.getOverrideLogLevel() != null) {
                            this.overrideLogLevel(conf.getChild("categories"),
                                    settings.getOverrideLogLevel());
                        }
                        ContainerUtil.configure(loggerManager, conf);
                    } finally {
                        resolver.release(source);
                    }
                }
            }
            ContainerUtil.initialize(loggerManager);
        } catch (Exception e) {
            bootstrapLogger.error(
                    "Could not set up Cocoon Logger, will use screen instead",
                    e);
        }

        this.log = this.loggerManager.getLoggerForCategory(accesslogger);
    }

    /**
     * Create a new logger manager.
     * @param loggerManagerClass The class name or one of the allowed shortcuts.
     * @return A new logger manager.
     */
    private LoggerManager newLoggerManager(String loggerManagerClass) {
        if ("LogKit".equalsIgnoreCase(loggerManagerClass) || loggerManagerClass == null) {
            loggerManagerClass = CocoonLogKitLoggerManager.class.getName();
        } else if ("LOG4J".equalsIgnoreCase(loggerManagerClass)) {
            loggerManagerClass = Log4JConfLoggerManager.class.getName();
        }
        try {
            Class clazz = Class.forName(loggerManagerClass);
            if ( PerRequestLoggerManager.class.isAssignableFrom(clazz) ) {
            }
            return (LoggerManager) clazz.newInstance();
        } catch (Exception e) {
            return new CocoonLogKitLoggerManager();
        }
    }

    public void overrideLogLevel(Configuration root, String value) {
        Configuration[] c = root.getChildren("category");
        for(int i=0;i<c.length;i++) {
            ((DefaultConfiguration)c[i]).setAttribute("log-level", value);
            this.overrideLogLevel(c[i], value);
        }
    }

    private boolean match(String uri, int[] parsedPattern ) {
        int pos = uri.lastIndexOf('/');
        if ( pos != -1 ) {
            uri = uri.substring(pos+1);
        }
        return WildcardHelper.match(null, uri, parsedPattern);
    }

    public Logger getCocoonLogger() {
        final String rootlogger = this.settings.getCocoonLogger();
        if (rootlogger != null) {
            return this.loggerManager.getLoggerForCategory(rootlogger);
        }
        return this.log;
    }
    
    public LoggerManager getCocoonLoggerManager() {
    	return this.loggerManager;
    }
}
