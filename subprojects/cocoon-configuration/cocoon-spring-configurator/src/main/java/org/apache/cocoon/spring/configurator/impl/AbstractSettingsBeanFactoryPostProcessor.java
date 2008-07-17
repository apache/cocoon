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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.spring.configurator.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.cocoon.configuration.MutableSettings;
import org.apache.cocoon.configuration.PropertyHelper;
import org.apache.cocoon.configuration.PropertyProvider;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.spring.configurator.ResourceFilter;
import org.apache.cocoon.spring.configurator.ResourceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.ServletContextResourceLoader;

/**
 * This is a bean factory post processor which handles all the settings stuff
 * for Cocoon. It reads in all properties files and replaces references to
 * them in the spring configuration files.
 * In addition this bean acts as a factory bean providing the settings object.
 *
 * @see SettingsBeanFactoryPostProcessor
 * @see ChildSettingsBeanFactoryPostProcessor
 * @since 1.0
 * @version $Id$
 */
public abstract class AbstractSettingsBeanFactoryPostProcessor
    extends PropertyPlaceholderConfigurer
    implements ServletContextAware, ResourceLoaderAware, FactoryBean {

    /** Logger (we use the same logging mechanism as Spring!) */
    protected final Log logger = LogFactory.getLog(getClass());

    protected ServletContext servletContext;

    protected MutableSettings settings;

    protected BeanFactory beanFactory;

    protected ResourceLoader resourceLoader;
    
    protected ResourceFilter resourceFilter;

    /**
     * Additional properties.
     */
    protected Properties additionalProperties;

    /**
     * List of additional property directories.
     */
    protected List directories;

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
    
    public void setResourceFilter(ResourceFilter resourceFilter) {
        this.resourceFilter = resourceFilter;
    }

    public void setDirectories(List directories) {
        this.directories = directories;
    }

    public void setAdditionalProperties(Properties props) {
        this.additionalProperties = props;
    }

    /**
     * Initialize this processor.
     * Setup the settings object.
     * 
     * @throws Exception
     */
    public void init()
    throws Exception {
        settings = createSettings();
        dumpSettings();
    }

    /**
     * Get the running mode.
     * This method should be implemented by subclasses.
     */
    protected abstract String getRunningMode();

    /**
     * Return a parent settings object if available.
     */
    protected Settings getParentSettings() {
        final BeanFactory parentBeanFactory = ((HierarchicalBeanFactory) this.beanFactory).getParentBeanFactory();
        if (parentBeanFactory != null) {
            return (Settings) parentBeanFactory.getBean(Settings.ROLE);
        }

        return null;
    }

    /**
     * Create a new settings object.
     * If a parent settings object is available a new child settings object is created.
     * Otherwise a new root settings object with the running mode is instantiated.
     */
    protected MutableSettings createMutableSettingsInstance() {
        final Settings parentSettings = getParentSettings();
        if (parentSettings == null) {
            return new MutableSettings(getRunningMode());
        }

        return new MutableSettings(parentSettings);
    }

    /**
     * This method can be used by subclasses to initialize the settings and/or
     * the properties before {@link #createSettings()} does it's work.
     */
    protected void preInit(final MutableSettings s, final Properties properties) {
        // default implementation does nothing
    }

    /**
     * This method can be used by subclasses to initialize the settings and/or
     * the properties after {@link #createSettings()} did it's work.
     */
    protected void postInit(final MutableSettings s, final Properties properties) {
        // default implementation does nothing
    }

    protected String getNameForPropertyProvider() {
        return null;
    }

    /**
     * Create a settings object.
     * This method creates the settings by executing the following task:
     * <ol>
     * <li>Create a new mutable settings object invoking {@link #createMutableSettingsInstance()}.
     * <li>Configure the properties and settings object by calling {@link #preInit(MutableSettings, Properties)}.
     * <li>Invoke a {@link PropertyProvider} if configured in the same application context (or its parent)
     * <li>Add properties from configured directories {@link #directories}.
     * <li>Add additional properties configured at {@link #additionalProperties}
     * <li>Apply system properties
     * <li>Configure the properties and settings object by calling {@link #postInit(MutableSettings, Properties)}.
     * <li>Replace references in properties
     * <li>Configure the settings object with the properties
     * <li>Make the settings object read-only.
     * </ol>
     *
     * @return A new Settings object
     */
    protected MutableSettings createSettings() {
        final String mode = getRunningMode();
        // create an empty settings objects
        final MutableSettings s = createMutableSettingsInstance();
        // create the initial properties
        final Properties properties = new Properties();

        // invoke pre initialization hook
        preInit(s, properties);

        // check for property providers
        if (this.beanFactory != null && this.beanFactory.containsBean(PropertyProvider.ROLE) ) {
            try {
                final PropertyProvider provider = (PropertyProvider) this.beanFactory.getBean(PropertyProvider.ROLE);
                final Properties providedProperties = provider.getProperties(s, mode, this.getNameForPropertyProvider());
                if (providedProperties != null) {
                    properties.putAll(providedProperties);
                }
            } catch (Exception ignore) {
                this.logger.warn("Unable to get properties from provider.", ignore);
                this.logger.warn("Continuing initialization.");
            }
        }

        // add aditional directories
        if (this.directories != null) {
            final Iterator i = directories.iterator();
            while (i.hasNext()) {
                final String directory = (String) i.next();
                // now read all properties from the properties directory
                ResourceUtils.readProperties(directory, properties, getResourceLoader(), this.resourceFilter, this.logger);
                // read all properties from the mode dependent directory
                ResourceUtils.readProperties(directory + '/' + mode, properties, getResourceLoader(), this.resourceFilter, this.logger);
            }
        }

        // add additional properties
        if (this.additionalProperties != null) {
            PropertyHelper.replaceAll(this.additionalProperties, s);
            properties.putAll(this.additionalProperties);
        }

        // now overwrite with system properties
        try {
            properties.putAll(System.getProperties());
        } catch (SecurityException se) {
            // we ignore this
        }

        // invoke post initialization hook
        postInit(s, properties);

        PropertyHelper.replaceAll(properties, getParentSettings());
        // configure settings
        s.configure(properties);
        s.makeReadOnly();

        return s;
    }

    protected ResourceLoader getResourceLoader() {
        if (this.resourceLoader != null) {
            return this.resourceLoader;
        }
        if (this.servletContext != null) {
            return new ServletContextResourceLoader(this.servletContext);
        } else {
            return new FileSystemResourceLoader();
        }
    }

    protected String getSystemProperty(String key) {
        return getSystemProperty(key, null);
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
            } catch (BeanDefinitionStoreException e) {
                throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanNames[i], e);
            }
        }
    }

    /**
     * @see org.springframework.beans.factory.config.PropertyPlaceholderConfigurer#resolvePlaceholder(java.lang.String, java.util.Properties)
     */
    protected String resolvePlaceholder(String placeholder, Properties props) {
        return PropertyHelper.getProperty(placeholder, props, null);
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
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("===== Settings Start =====");
            this.logger.debug(this.settings.toString());
            final List names = this.settings.getPropertyNames();
            final Iterator i = names.iterator();
            while (i.hasNext()) {
                final String name = (String) i.next();
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
