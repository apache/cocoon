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

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.ProcessingUtil;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.treeprocessor.ProcessorComponentInfo;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.core.container.spring.avalon.AvalonEnvironment;
import org.apache.cocoon.core.container.spring.avalon.ComponentInfo;
import org.apache.cocoon.core.container.spring.avalon.ConfigurationInfo;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.transformation.Transformer;
import org.apache.excalibur.source.SourceResolver;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This utility class helps in creating new Spring {@link ConfigurableListableBeanFactory} objects
 * which support the Avalon style component configuration. It also offers help in setting
 * up the root logger for Cocoon.
 *
 * @since 2.2
 * @version $Id$
 */
public class BeanFactoryUtil {

    protected ConfigurableListableBeanFactory beanFactory;

    /**
     * Create a new (sub) bean factory.
     *
     * @param env  The avalon environment.
     * @param info The avalon configuration.
     * @param parent The parent factory or null.
     * @return A new bean factory.
     * @throws Exception
     */
    public static ConfigurableListableBeanFactory createBeanFactory(AvalonEnvironment env,
                                                                    ConfigurationInfo info,
                                                                    SourceResolver    resolver,
                                                                    BeanFactory       parent)
    throws Exception {
        return createBeanFactory(env, info, resolver, parent, true);
    }
    
    /**
     * Create a new (sub) bean factory.
     *
     * @param env  The avalon environment.
     * @param info The avalon configuration.
     * @param parent The parent factory or null.
     * @return A new bean factory.
     * @throws Exception
     */
    public static ConfigurableListableBeanFactory createBeanFactory(ClassLoader       classLoader,
                                                                    AvalonEnvironment env,
                                                                    ConfigurationInfo info,
                                                                    SourceResolver    resolver,
                                                                    BeanFactory       parent)
    throws Exception {
        return createBeanFactory(classLoader, env, info, resolver, parent, true);
    }

    /**
     * Create a new (sub) bean factory.
     *
     * @param env  The avalon environment.
     * @param info The avalon configuration.
     * @param parent The parent factory or null.
     * @return A new bean factory.
     * @throws Exception
     */
    public static ConfigurableListableBeanFactory createBeanFactory(AvalonEnvironment env,
                                                                    ConfigurationInfo info,
                                                                    SourceResolver    resolver,
                                                                    BeanFactory       parent,
                                                                    boolean           preInstantiateSingletons)
    throws Exception {
        return createBeanFactory(Thread.currentThread().getContextClassLoader(), env, info, resolver, parent, preInstantiateSingletons);
    }

    /**
     * Create a new (sub) bean factory.
     *
     * @param env  The avalon environment.
     * @param info The avalon configuration.
     * @param parent The parent factory or null.
     * @return A new bean factory.
     * @throws Exception
     */
    public static ConfigurableListableBeanFactory createBeanFactory(ClassLoader       classLoader,
                                                                    AvalonEnvironment env,
                                                                    ConfigurationInfo info,
                                                                    SourceResolver    resolver,
                                                                    BeanFactory       parent,
                                                                    boolean           preInstantiateSingletons)
    throws Exception {
        final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            if ( classLoader != null ) {
                Thread.currentThread().setContextClassLoader(classLoader);
            }
            final String xmlConfig = (new XmlConfigCreator(env.logger)).createConfig(info);
            Resource rsc = new ByteArrayResource(xmlConfig.getBytes("utf-8"));
            Logger logger = env.logger;
            if ( info.getRootLogger() != null ) {
                logger = env.logger.getChildLogger(info.getRootLogger());
            }
            // Spring uses the context classloader to create new beans
            CocoonBeanFactory factory;
            factory = new CocoonBeanFactory(rsc, 
                                            parent,
                                            logger,
                                            info,
                                            env.context,
                                            env.settings);
            // register logger
            factory.registerSingleton(ProcessingUtil.LOGGER_ROLE, logger);
            // register avalon context - if available
            if ( env.context != null ) {
                factory.registerSingleton(ProcessingUtil.CONTEXT_ROLE, env.context);
            }
            // add local settings
            factory.registerSingleton(Settings.ROLE, env.settings);
            prepareBeanFactory(factory, info);
            if (preInstantiateSingletons) {
                factory.preInstantiateSingletons();
            }
            return factory;
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    /**
     * Get a possible root context.
     */
    public static ApplicationContext getWebApplicationContext(ServletContext servletContext) {
        ApplicationContext parent = null;
        if( servletContext != null) {
            parent = WebApplicationContextUtils.getWebApplicationContext(servletContext);            
            parent = (ApplicationContext)servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        }
        return parent;
    }

    protected static void prepareBeanFactory(CocoonBeanFactory factory,
                                             ConfigurationInfo configInfo) {
        // TODO - we should find a better way
        // add ProcessorComponentInfo
        ProcessorComponentInfo parentInfo = null;
        if ( factory.getParentBeanFactory() != null && factory.getParentBeanFactory().containsBean(ProcessorComponentInfo.ROLE) ) {
            parentInfo = (ProcessorComponentInfo)factory.getParentBeanFactory().getBean(ProcessorComponentInfo.ROLE);
        }
        ProcessorComponentInfo info = new ProcessorComponentInfo(parentInfo);
        final Iterator i = configInfo.getComponents().values().iterator();
        while (i.hasNext()) {
            final ComponentInfo current = (ComponentInfo)i.next();
            info.componentAdded(current.getRole(), current.getComponentClassName(), current.getConfiguration());
        }
        prepareSelector(info, factory, configInfo, Generator.ROLE);
        prepareSelector(info, factory, configInfo, Transformer.ROLE);
        prepareSelector(info, factory, configInfo, Serializer.ROLE);
        prepareSelector(info, factory, configInfo, ProcessingPipeline.ROLE);
        prepareSelector(info, factory, configInfo, Action.ROLE);
        prepareSelector(info, factory, configInfo, Selector.ROLE);
        prepareSelector(info, factory, configInfo, Matcher.ROLE);
        prepareSelector(info, factory, configInfo, Reader.ROLE);
        info.lock();
        factory.registerSingleton(ProcessorComponentInfo.ROLE, info);
    }

    protected static void prepareSelector(ProcessorComponentInfo info,
                                          CocoonBeanFactory      factory,
                                          ConfigurationInfo      configInfo,
                                          String                 category) {
        final ComponentInfo component = (ComponentInfo)configInfo.getComponents().get(category + "Selector");
        if ( component != null ) {
            info.setDefaultType(category, component.getDefaultValue());
        }
    }
}
