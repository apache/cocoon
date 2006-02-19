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

import java.util.Iterator;

import javax.servlet.ServletContext;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Log4JLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.treeprocessor.ProcessorComponentInfo;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.transformation.Transformer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;

/**
 * This factory creates new Spring {@link ApplicationContext} objects which support
 * the Avalon style component configuration.
 *
 * @since 2.2
 * @version $Id$
 */
public class ApplicationContextFactory {

    /**
     * Create a new (sub) application context.
     *
     * @param env
     * @param info
     * @param parent The parent application context or null.
     * @return A new application context
     * @throws Exception
     */
    public static CocoonXmlWebApplicationContext createApplicationContext(AvalonEnvironment  env,
                                                                          ConfigurationInfo  info,
                                                                          ApplicationContext parent,
                                                                          boolean            addCocoon)
    throws Exception {
        final String xmlConfig = (new XmlConfigCreator()).createConfig(info.getComponents(), addCocoon);
        Resource rsc = new ByteArrayResource(xmlConfig.getBytes("utf-8"));
        Logger logger = env.logger;
        if ( info.rootLogger != null ) {
            logger = env.logger.getChildLogger(info.rootLogger);
        }
        CocoonXmlWebApplicationContext context = new CocoonXmlWebApplicationContext(rsc, 
                                                                                    parent,
                                                                                    logger,
                                                                                    info,
                                                                                    env.context);
        context.addBeanFactoryPostProcessor(new CocoonSettingsConfigurer(env.settings));

        // TODO: Add context specific information
        //context.setSourceResolver(this.resolver);
        //context.setEnvironmentHelper(this.environmentHelper);
        context.setServletContext(env.servletContext);
        context.refresh();
        if ( info.rootLogger != null ) {
            context.getBeanFactory().registerSingleton(Logger.class.getName(), logger);
        }
        prepareApplicationContext(context);
        return context;
    }

    /**
     * Create the root application context.
     * This context is the root of all Cocoon based Spring application contexts. If
     * the default Spring context is created using the Spring context listener, that
     * default context will be the parent of this root context.
     *
     * @param env
     * @return A new root application context.
     * @throws Exception
     */
    public static ApplicationContext createRootApplicationContext(AvalonEnvironment  env)
    throws Exception {
        final ApplicationContext parent = (ApplicationContext)env.servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        CocoonXmlWebApplicationContext context = new CocoonXmlWebApplicationContext(parent);
        context.refresh();
        final ConfigurableListableBeanFactory factory = context.getBeanFactory();
        factory.registerSingleton(Context.class.getName(), env.context);
        factory.registerSingleton(Logger.class.getName(), env.logger);
        factory.registerSingleton(Core.class.getName(), env.core);
        factory.registerSingleton(Settings.class.getName(), env.settings);
        prepareApplicationContext(context);
        return context;
    }

    public static Logger createRootLogger(ServletContext context, String category) {
        final ApplicationContext parent = (ApplicationContext)context.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        // test for a logger in the parent context
        if ( parent != null && parent.containsBean(Logger.class.getName()) ) {
            if ( category == null || category.length() == 0 ) {
                return (Logger)parent.getBean(Logger.class.getName());
            }
            return ((Logger)parent.getBean(Logger.class.getName())).getChildLogger(category);
        }
        // create a new log4j logger
        org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(category);
        return new Log4JLogger(logger);
    }

    protected static void prepareApplicationContext(CocoonXmlWebApplicationContext context) {
        if ( context.getConfigurationInfo() != null ) {
            // TODO - we should find a better way
            // add ProcessorComponentInfo
            ProcessorComponentInfo parentInfo = null;
            if ( context.getParent() != null && context.getParent().containsBean(ProcessorComponentInfo.ROLE) ) {
                parentInfo = (ProcessorComponentInfo)context.getParent().getBean(ProcessorComponentInfo.ROLE);
            }
            ProcessorComponentInfo info = new ProcessorComponentInfo(parentInfo);
            final Iterator i = context.getConfigurationInfo().getComponents().values().iterator();
            while (i.hasNext()) {
                final ComponentInfo current = (ComponentInfo)i.next();
                info.componentAdded(current.getRole(), current.getComponentClassName(), current.getConfiguration());
            }
            prepareSelector(info, context, Generator.ROLE);
            prepareSelector(info, context, Transformer.ROLE);
            prepareSelector(info, context, Serializer.ROLE);
            prepareSelector(info, context, ProcessingPipeline.ROLE);
            prepareSelector(info, context, Action.ROLE);
            prepareSelector(info, context, Selector.ROLE);
            prepareSelector(info, context, Matcher.ROLE);
            prepareSelector(info, context, Reader.ROLE);
            info.lock();
            context.getBeanFactory().registerSingleton(ProcessorComponentInfo.ROLE, info);
        }
    }

    protected static void prepareSelector(ProcessorComponentInfo         info,
                                          CocoonXmlWebApplicationContext context,
                                          String                         category) {
        final ComponentInfo component = (ComponentInfo)context.getConfigurationInfo().getComponents().get(category + "Selector");
        if ( component != null ) {
            final String defaultComponent = component.getConfiguration().getAttribute("default", null);
            if ( defaultComponent != null ) {
                info.roleAliased(category + "/" + defaultComponent, category + "/$default$");
            }
        }
    }
                                          
}
