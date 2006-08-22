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

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.core.container.spring.avalon.AvalonBeanPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
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
            AvalonBeanPostProcessor processor = new AvalonBeanPostProcessor();
            processor.setBeanFactory(this);
            processor.setLogger(this.avalonLogger);
            processor.setContext(this.avalonContext);
            processor.setConfigurationInfo(this.avalonConfiguration);
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
}
