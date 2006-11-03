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
package org.apache.cocoon.core.container.spring;

import java.util.Properties;

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.configuration.SettingsDefaults;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * Our version of the property override configurer which reads properties
 * from the Cocoon spring configuration directory.
 * A property should have the following format: {bean name}/{property name}={value}.
 *
 * @version $Id$
 */
public class CocoonPropertyOverrideConfigurer extends PropertyOverrideConfigurer {

    /** The location of the directory where the different property files are located. */
    protected String location = Constants.DEFAULT_SPRING_CONFIGURATION_LOCATION;

    /** The resource loader used to load the property files. 
     * This loader is either resolving relative to the current sitemap or the
     * root of the context.
     */
    protected ResourceLoader resourceLoader = new DefaultResourceLoader();

    /**
     * The settings object.
     */
    protected Settings settings;

    /**
     * Set the directory to search in.
     * @param object New value.
     */
    public void setLocation(final String object) {
        this.location = object;
    }

    /** Set the settings. */
    public void setSettings(Settings object) {
        this.settings = object;
    }

    /**
     * Set the resource loader.
     * @param loader The new resource loader.
     */
    public void setResourceLoader(final ResourceLoader loader) {
        this.resourceLoader = loader;
    }

    /**
     * Read all property files from the specified location and apply the changes.
     * @see org.springframework.beans.factory.config.PropertyResourceConfigurer#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
     */
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
    throws BeansException {
        if ( this.logger.isDebugEnabled() ) {
            this.logger.debug("Processing bean factory: " + beanFactory);
        }
        final String mode = (this.settings != null ? this.settings.getRunningMode() : SettingsDefaults.DEFAULT_RUNNING_MODE);
        final Properties mergedProps = new Properties();
        ResourceUtils.readProperties("classpath:*/META-INF/cocoon/spring", mergedProps, this.resourceLoader, this.logger);
        ResourceUtils.readProperties("classpath:*/META-INF/cocoon/spring/" + mode, mergedProps, this.resourceLoader, this.logger);

        ResourceUtils.readProperties(this.location, mergedProps, this.resourceLoader, this.logger);
        // read properties from running-mode dependent directory
        ResourceUtils.readProperties(this.location + '/' + mode, mergedProps, this.resourceLoader, this.logger);
        
        if ( mergedProps.size() > 0 ) {
            // Convert the merged properties, if necessary.
            convertProperties(mergedProps);
    
            // Let the subclass process the properties.
            processProperties(beanFactory, mergedProps);
        }
    }
}
