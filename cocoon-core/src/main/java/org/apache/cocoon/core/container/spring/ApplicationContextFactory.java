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

import java.util.Map;


import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.Settings;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
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
                                                              ApplicationContext parent)
    throws Exception {
        final String xmlConfig = (new XmlConfigCreator()).createConfig(info.getComponents());
        Resource rsc = new ByteArrayResource(xmlConfig.getBytes("utf-8"));
        CocoonXmlWebApplicationContext context = new CocoonXmlWebApplicationContext(rsc, parent);
        context.addBeanFactoryPostProcessor(new CocoonSettingsConfigurer(env.settings));

        // TODO: Add context specific information
        //context.setSourceResolver(this.resolver);
        //context.setEnvironmentHelper(this.environmentHelper);
        context.setServletContext(env.servletContext);
        AvalonPostProcessor processor = new AvalonPostProcessor();
        processor.components = info.getComponents();
        processor.logger = env.logger;
        if ( info.rootLogger != null ) {
            processor.logger = env.logger.getChildLogger(info.rootLogger);
        }
        processor.context = env.context;
        context.refresh();
        processor.beanFactory = context.getBeanFactory();
        context.getBeanFactory().addBeanPostProcessor(processor);
        if ( info.rootLogger != null ) {
            context.getBeanFactory().registerSingleton(Logger.class.getName(), processor.logger);
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
        CocoonXmlWebApplicationContext context = new CocoonXmlWebApplicationContext(null, parent);
        context.refresh();
        final ConfigurableListableBeanFactory factory = context.getBeanFactory();
        factory.registerSingleton(Context.class.getName(), env.context);
        factory.registerSingleton(Logger.class.getName(), env.logger);
        factory.registerSingleton(Core.class.getName(), env.core);
        factory.registerSingleton(Settings.class.getName(), env.settings);
        return context;
    }

    /**
     * This is a Spring BeanPostProcessor adding support for the Avalon lifecycle interfaces.
     */
    protected static final class AvalonPostProcessor implements DestructionAwareBeanPostProcessor {

        protected static final Configuration EMPTY_CONFIG = new DefaultConfiguration("empty");

        protected Logger logger;
        protected Context context;
        protected BeanFactory beanFactory;
        protected Map components;

        /**
         * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
         */
        public Object postProcessAfterInitialization(Object bean, String beanName)
        throws BeansException {
            try {
                ContainerUtil.start(bean);
            } catch (Exception e) {
                throw new BeanInitializationException("Unable to start bean " + beanName, e);
            }
            return bean;
        }

        /**
         * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
         */
        public Object postProcessBeforeInitialization(Object bean, String beanName)
        throws BeansException {
            final ComponentInfo info = (ComponentInfo)this.components.get(beanName);
            try {
                if ( info == null ) {
                    // no info so we just return the bean and don't apply any lifecycle interfaces
                    return bean;
                }
                if ( info.getLoggerCategory() != null ) {
                    ContainerUtil.enableLogging(bean, this.logger.getChildLogger(info.getLoggerCategory()));
                } else {
                    ContainerUtil.enableLogging(bean, this.logger);
                }
                ContainerUtil.contextualize(bean, this.context);
                ContainerUtil.service(bean, (ServiceManager)this.beanFactory.getBean(ServiceManager.class.getName()));
                if ( info != null ) {
                    Configuration config = info.getConfiguration();
                    if ( config == null ) {
                        config = EMPTY_CONFIG;
                    }
                    if ( bean instanceof Configurable ) {
                        ContainerUtil.configure(bean, config);
                    } else if ( bean instanceof Parameterizable ) {
                        Parameters p = info.getParameters();
                        if ( p == null ) {
                            p = Parameters.fromConfiguration(config);
                            info.setParameters(p);
                        }
                        ContainerUtil.parameterize(bean, p);
                    }
                }
                ContainerUtil.initialize(bean);
            } catch (Exception e) {
                throw new BeanCreationException("Unable to initialize Avalon component with role " + beanName, e);
            }
            return bean;
        }

        /**
         * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor#postProcessBeforeDestruction(java.lang.Object, java.lang.String)
         */
        public void postProcessBeforeDestruction(Object bean, String beanName)
        throws BeansException {
            try {
                ContainerUtil.stop(bean);
            } catch (Exception e) {
                throw new BeanInitializationException("Unable to stop bean " + beanName, e);
            }
            ContainerUtil.dispose(bean);
        }
    }
}
