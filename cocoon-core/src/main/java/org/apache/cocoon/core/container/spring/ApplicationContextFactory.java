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

import javax.servlet.ServletContext;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Log4JLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.Settings;
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
    public static ApplicationContext createApplicationContext(AvalonEnvironment  env,
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
                                                                                    info.getComponents(),
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
}
