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
package org.apache.cocoon.core.container.spring;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import javax.servlet.ServletContext;

import org.apache.avalon.excalibur.logger.Log4JConfLoggerManager;
import org.apache.avalon.excalibur.logger.ServletLogger;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.treeprocessor.ProcessorComponentInfo;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.CoreInitializationException;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.core.container.util.ConfigurationBuilder;
import org.apache.cocoon.core.container.util.SettingsContext;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.transformation.Transformer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;

/**
 * This utility class helps in creating new Spring {@link BeanFactory} objects which support
 * the Avalon style component configuration. It also offers help in setting up the root
 * logger for Cocoon.
 *
 * @since 2.2
 * @version $Id$
 */
public class BeanFactoryUtil {

    /**
     * Create a new (sub) bean factory.
     *
     * @param env  The avalon environment.
     * @param info The avalon configuration.
     * @param parent The parent factory or null.
     * @return A new bean factory.
     * @throws Exception
     */
    public static ConfigurableBeanFactory createApplicationContext(AvalonEnvironment  env,
                                                                   ConfigurationInfo  info,
                                                                   BeanFactory        parent,
                                                                   boolean            addCocoon)
    throws Exception {
        final String xmlConfig = (new XmlConfigCreator(env.logger)).createConfig(info, addCocoon);
        Resource rsc = new ByteArrayResource(xmlConfig.getBytes("utf-8"));
        Logger logger = env.logger;
        if ( info.rootLogger != null ) {
            logger = env.logger.getChildLogger(info.rootLogger);
        }
        CocoonBeanFactory context = new CocoonBeanFactory(rsc, 
                                                          parent,
                                                          logger,
                                                          info,
                                                          env.context,
                                                          env.settings);
        if ( info.rootLogger != null ) {
            context.registerSingleton(Logger.class.getName(), logger);
        }
        prepareApplicationContext(context, info);
        return context;
    }

    /**
     * Create the root bean factory.
     * This factory is the root of all Cocoon based Spring bean factories. If
     * the default Spring application context is created using the Spring context listener, that
     * default context will be the parent of this factory.
     *
     * @param env The avalon environment.
     * @return A new root application factory.
     * @throws Exception
     */
    public static ConfigurableBeanFactory createRootApplicationContext(AvalonEnvironment  env)
    throws Exception {
        final ApplicationContext parent = (ApplicationContext)env.servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        CocoonBeanFactory factory = new CocoonBeanFactory(parent);
        factory.registerSingleton(Context.class.getName(), env.context);
        factory.registerSingleton(Logger.class.getName(), env.logger);
        factory.registerSingleton(Core.class.getName(), env.core);
        factory.registerSingleton(Settings.class.getName(), env.settings);
        return factory;
    }

    /**
     * Create the root logger for Cocoon.
     * If the root spring application context is setup (using the spring listener) and
     * if it contains a Logger bean (a bean with the name of the Logger class), then
     * that bean is used as the root logger.
     * If either the context is not available or the Logger bean is not defined, we setup
     * our own Logger based on Log4j.
     *
     * @param context The servlet context.
     * @param settings The core settings.
     * @return The root logger for Cocoon.
     */
    public static Logger createRootLogger(ServletContext context,
                                          Settings       settings) {
        final ApplicationContext parent = (ApplicationContext)context.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        // test for a logger in the parent context
        if ( parent != null && parent.containsBean(Logger.class.getName()) ) {
            if ( settings.getEnvironmentLogger() == null || settings.getEnvironmentLogger().length() == 0 ) {
                return (Logger)parent.getBean(Logger.class.getName());
            }
            return ((Logger)parent.getBean(Logger.class.getName())).getChildLogger(settings.getEnvironmentLogger());
        }
        // create a new log4j logger
        try {
            return initLogger(context, settings);
        } catch (Exception ce) {
            ce.printStackTrace();
            throw new CoreInitializationException("Cannot setup log4j logging system.", ce);
        }
    }

    protected static Logger initLogger(ServletContext servletContext,
                                       Settings       settings)
    throws Exception {
        // create a bootstrap logger
        int logLevel;
        final String logLevelString = settings.getBootstrapLogLevel();
        if ( "DEBUG".equalsIgnoreCase(logLevelString) ) {
            logLevel = ServletLogger.LEVEL_DEBUG;
        } else if ( "WARN".equalsIgnoreCase(logLevelString) ) {
            logLevel = ServletLogger.LEVEL_WARN;
        } else if ( "ERROR".equalsIgnoreCase(logLevelString) ) {
            logLevel = ServletLogger.LEVEL_ERROR;
        } else {
            logLevel = ServletLogger.LEVEL_INFO;
        }
        final Logger bootstrapLogger = new ServletLogger(servletContext, "Cocoon", logLevel);


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
        String loggerConfig = settings.getLoggingConfiguration();
        if ( !loggerConfig.startsWith("/") ) {
            loggerConfig = '/' + loggerConfig;
        }
        if ( loggerConfig != null ) {
            final URL url = servletContext.getResource(loggerConfig);
            if ( url != null ) {
                final ConfigurationBuilder builder = new ConfigurationBuilder(settings);
                final Configuration conf = builder.build(servletContext.getResourceAsStream(loggerConfig));
                // override log level?
                if (settings.getOverrideLogLevel() != null) {
                    // TODO - override loglevel for log4j
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
        return loggerManager.getLoggerForCategory(accesslogger);
    }

    protected static void prepareApplicationContext(CocoonBeanFactory context,
                                                    ConfigurationInfo configInfo) {
        // TODO - we should find a better way
        // add ProcessorComponentInfo
        ProcessorComponentInfo parentInfo = null;
        if ( context.getParentBeanFactory() != null && context.getParentBeanFactory().containsBean(ProcessorComponentInfo.ROLE) ) {
            parentInfo = (ProcessorComponentInfo)context.getParentBeanFactory().getBean(ProcessorComponentInfo.ROLE);
        }
        ProcessorComponentInfo info = new ProcessorComponentInfo(parentInfo);
        final Iterator i = configInfo.getComponents().values().iterator();
        while (i.hasNext()) {
            final ComponentInfo current = (ComponentInfo)i.next();
            info.componentAdded(current.getRole(), current.getComponentClassName(), current.getConfiguration());
        }
        prepareSelector(info, context, configInfo, Generator.ROLE);
        prepareSelector(info, context, configInfo, Transformer.ROLE);
        prepareSelector(info, context, configInfo, Serializer.ROLE);
        prepareSelector(info, context, configInfo, ProcessingPipeline.ROLE);
        prepareSelector(info, context, configInfo, Action.ROLE);
        prepareSelector(info, context, configInfo, Selector.ROLE);
        prepareSelector(info, context, configInfo, Matcher.ROLE);
        prepareSelector(info, context, configInfo, Reader.ROLE);
        info.lock();
        context.registerSingleton(ProcessorComponentInfo.ROLE, info);
    }

    protected static void prepareSelector(ProcessorComponentInfo info,
                                          CocoonBeanFactory      context,
                                          ConfigurationInfo      configInfo,
                                          String                 category) {
        final ComponentInfo component = (ComponentInfo)configInfo.getComponents().get(category + "Selector");
        if ( component != null ) {
            info.setDefaultType(category, component.getDefaultValue());
        }
    }
                                          
}
