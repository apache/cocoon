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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.configuration.Settings;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.OrderComparator;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.ServletContextAwareProcessor;

/**
 * This is a Cocoon specific implementation of a Spring {@link DefaultListableBeanFactory}.
 *
 * @since 2.2
 * @version $Id$
 */
public class CocoonBeanFactory
    extends DefaultListableBeanFactory {

    protected final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);

    /** The name of the request attribute containing the current bean factory. */
    public static final String BEAN_FACTORY_REQUEST_ATTRIBUTE = CocoonBeanFactory.class.getName();

    final private Resource avalonResource;

    protected final Logger avalonLogger;
    protected final Context avalonContext;
    protected final ConfigurationInfo avalonConfiguration;
    protected final ServletContext servletContext;

    public CocoonBeanFactory(BeanFactory parent) {
        this(null, parent, null, null, null, null);
    }

    public CocoonBeanFactory(Resource                avalonResource,
                             BeanFactory             parent,
                             Logger                  avalonLogger,
                             ConfigurationInfo       avalonConfiguration,
                             Context                 avalonContext,
                             Settings                settings) {
        super(parent);
        // try to get servlet context
        if ( parent instanceof WebApplicationContext ) {
            this.servletContext = ((WebApplicationContext)parent).getServletContext();
        } else if ( parent instanceof CocoonBeanFactory ) {
            this.servletContext = ((CocoonBeanFactory)parent).servletContext;
        } else {
            this.servletContext = null;
        }
        // add support for ServletContextAware
        if ( this.servletContext != null ) {
            this.addBeanPostProcessor(new ServletContextAwareProcessor(this.servletContext));
            this.ignoreDependencyInterface(ServletContextAware.class);
        }
        this.avalonResource = avalonResource;
        this.avalonLogger = avalonLogger;
        this.avalonConfiguration = avalonConfiguration;
        this.avalonContext = avalonContext;
        
        if ( this.avalonConfiguration != null ) {
            AvalonPostProcessor processor = new AvalonPostProcessor(this.avalonConfiguration.getComponents(),
                                                                    this.avalonContext,
                                                                    this.avalonLogger,
                                                                    this);
            this.addBeanPostProcessor(processor);
            this.registerSingleton(ConfigurationInfo.class.getName(), this.avalonConfiguration);
        }
        if ( this.avalonResource != null ) {
            this.reader.loadBeanDefinitions(this.avalonResource);
        }
        // post processing
        if ( settings != null ) {
            (new CocoonSettingsConfigurer(settings)).postProcessBeanFactory(this);
        }
    }

    /**
     * Instantiate and invoke all registered BeanPostProcessor beans,
     * respecting explicit order if given.
     * @see org.springframework.beans.factory.support.DefaultListableBeanFactory#preInstantiateSingletons()
     */
    public void preInstantiateSingletons() throws BeansException {
        // Actually fetch and register the BeanPostProcessor beans.
        // Do not initialize FactoryBeans here: We need to leave all regular beans
        // uninitialized to let the bean post-processors apply to them!
        final Map beanProcessorMap = this.getBeansOfType(BeanPostProcessor.class, true, false);
        final List beanProcessors = new ArrayList(beanProcessorMap.values());
        Collections.sort(beanProcessors, new OrderComparator());
        for (Iterator it = beanProcessors.iterator(); it.hasNext();) {
            this.addBeanPostProcessor((BeanPostProcessor) it.next());
        }
        super.preInstantiateSingletons();
    }

    /**
     * This is a Spring BeanPostProcessor adding support for the Avalon lifecycle interfaces.
     */
    protected static final class AvalonPostProcessor implements DestructionAwareBeanPostProcessor {

        protected static final Configuration EMPTY_CONFIG = new DefaultConfiguration("empty");

        protected final Logger logger;
        protected final Context context;
        protected final CocoonBeanFactory beanFactory;
        protected final Map components;

        public AvalonPostProcessor(Map         components,
                                   Context     context,
                                   Logger      logger,
                                   CocoonBeanFactory factory) {
            this.components = components;
            this.context = context;
            this.logger = logger;
            this.beanFactory = factory;
        }

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
