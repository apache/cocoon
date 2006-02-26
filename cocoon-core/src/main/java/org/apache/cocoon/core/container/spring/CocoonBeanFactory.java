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
import org.apache.cocoon.components.SitemapConfigurable;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.core.container.util.DefaultSitemapConfigurationHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

/**
 * This is a Cocoon specific implementation of a Spring {@link ApplicationContext}.
 *
 * @since 2.2
 * @version $Id$
 */
public class CocoonBeanFactory
    extends DefaultListableBeanFactory
    implements NameForAliasAware {

    protected final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);

    /** The name of the request attribute containing the current bean factory. */
    public static final String BEAN_FACTORY_REQUEST_ATTRIBUTE = CocoonBeanFactory.class.getName();

    final private Resource avalonResource;

    protected final Logger avalonLogger;
    protected final Context avalonContext;
    protected final ConfigurationInfo avalonConfiguration;

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
     * @see org.apache.cocoon.core.container.spring.NameForAliasAware#getNameForAlias(java.lang.String)
     */
    public String getNameForAlias(String alias) {
        if ( this.avalonConfiguration != null ) {
            final String value = this.avalonConfiguration.getRoleForName(alias);
            if ( value != null ) {
                return value;
            }
            if ( this.getParentBeanFactory() instanceof NameForAliasAware ) {
                return ((NameForAliasAware)this.getParentBeanFactory()).getNameForAlias(alias);
            }
        }
        // default: we just return the alias
        return alias;
    }

    /**
     * This is a Spring BeanPostProcessor adding support for the Avalon lifecycle interfaces.
     */
    protected static final class AvalonPostProcessor implements DestructionAwareBeanPostProcessor {

        protected static final Configuration EMPTY_CONFIG = new DefaultConfiguration("empty");

        protected final Logger logger;
        protected final Context context;
        protected final BeanFactory beanFactory;
        protected final Map components;

        public AvalonPostProcessor(Map         components,
                                   Context     context,
                                   Logger      logger,
                                   BeanFactory factory) {
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
                if ( bean instanceof SitemapConfigurable ) {
                    ((SitemapConfigurable)bean).configure(new DefaultSitemapConfigurationHolder(beanName));
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
