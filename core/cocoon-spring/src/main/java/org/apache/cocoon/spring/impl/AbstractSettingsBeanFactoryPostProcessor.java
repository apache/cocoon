/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.cocoon.spring.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.cocoon.configuration.MutableSettings;
import org.apache.cocoon.configuration.Settings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResourceLoader;

/**
 * This is a bean factory post processor which handles all the settings stuff
 * for Cocoon. It reads in all properties files and replaces references to
 * them in the spring configuration files.
 * In addition this bean acts as a factory bean providing the settings object.
 * @see SettingsBeanFactoryPostProcessor
 * @see SubSettingsBeanFactoryPostProcessor
 *
 * @since 1.0
 * @version $Id$
 */
public abstract class AbstractSettingsBeanFactoryPostProcessor
    extends PropertyPlaceholderConfigurer
    implements ServletContextAware, BeanFactoryPostProcessor, ResourceLoaderAware, FactoryBean {

    /** Logger (we use the same logging mechanism as Spring!) */
    protected final Log logger = LogFactory.getLog(getClass());

    protected ServletContext servletContext;

    protected MutableSettings settings;

    protected BeanFactory beanFactory;

    protected ResourceLoader resourceLoader;

    /**
     * @see org.springframework.beans.factory.config.PropertyPlaceholderConfigurer#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory factory) {
        super.setBeanFactory(factory);
        this.beanFactory = factory;
    }

    /**
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext sContext) {
        this.servletContext = sContext;
    }

    /**
     * @see org.springframework.context.ResourceLoaderAware#setResourceLoader(org.springframework.core.io.ResourceLoader)
     */
    public void setResourceLoader(ResourceLoader loader) {
        this.resourceLoader = loader;
    }

    /**
     * This method can be overwritten by subclasses to further initialize the settings
     */
    protected void doInit() {
        // nothing to do here
    }

    protected ResourceLoader getResourceLoader() {
        if ( this.resourceLoader != null ) {
            return this.resourceLoader;
        }
        return new ServletContextResourceLoader(this.servletContext);
    }

    protected String getSystemProperty(String key) {
        return this.getSystemProperty(key, null);
    }

    protected String getSystemProperty(String key, String defaultValue) {
        try {
            return System.getProperty(key, defaultValue);
        } catch (SecurityException se) {
            // we ignore this
            return defaultValue;
        }
    }

    /**
     * @see org.springframework.beans.factory.config.PropertyPlaceholderConfigurer#processProperties(org.springframework.beans.factory.config.ConfigurableListableBeanFactory, java.util.Properties)
     */
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
                                     Properties props)
    throws BeansException {
        final BeanDefinitionVisitor visitor = new CocoonSettingsResolvingBeanDefinitionVisitor(this.settings);
        String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
        for (int i = 0; i < beanNames.length; i++) {
            BeanDefinition bd = beanFactoryToProcess.getBeanDefinition(beanNames[i]);
            try {
                visitor.visitBeanDefinition(bd);
            } catch (BeanDefinitionStoreException ex) {
                throw new BeanDefinitionStoreException(bd
                        .getResourceDescription(), beanNames[i], ex
                        .getMessage());
            }
        }
    }

    protected class CocoonSettingsResolvingBeanDefinitionVisitor
        extends BeanDefinitionVisitor {

        protected final Properties props;
        protected final Set visitedPlaceholders = new HashSet();

        public CocoonSettingsResolvingBeanDefinitionVisitor(Settings settings) {
            this.props = new SettingsProperties(settings);
        }

        protected String resolveStringValue(String strVal) {
            return parseStringValue(strVal, this.props, visitedPlaceholders);
        }
    }

    /**
     * Dump the settings object
     */
    protected void dumpSettings() {
        if ( this.logger.isDebugEnabled() ) {
            this.logger.debug("===== Settings Start =====");
            this.logger.debug(this.settings.toString());
            final List names = this.settings.getPropertyNames();
            final Iterator i = names.iterator();
            while ( i.hasNext() ) {
                final String name = (String)i.next();
                this.logger.debug("Property: " + name + "=" + this.settings.getProperty(name));
            }
            this.logger.debug("===== Settings End =====");
        }
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        return this.settings;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType() {
        return Settings.class;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }
}
