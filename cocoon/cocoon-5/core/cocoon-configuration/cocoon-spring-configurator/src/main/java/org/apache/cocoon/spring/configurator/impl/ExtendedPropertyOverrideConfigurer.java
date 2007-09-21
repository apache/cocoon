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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.spring.configurator.ResourceUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * Our version of the property override configurer which reads properties from
 * the Cocoon spring configuration directory. A property should have the
 * following format: {bean name}/{property name}={value}.
 *
 * @version $Id$
 * @since 1.0
 */
public class ExtendedPropertyOverrideConfigurer extends PropertyOverrideConfigurer {

    /**
     * The locations of the directories where the different property files are
     * located.
     */
    protected List locations;

    /**
     * The resource loader used to load the property files. This loader is
     * either resolving relative to the current sitemap or the root of the
     * context.
     */
    protected ResourceLoader resourceLoader = new DefaultResourceLoader();

    /**
     * The settings object.
     */
    protected Settings settings;

    public ExtendedPropertyOverrideConfigurer() {
        // add default locations
        final List list = new ArrayList();
        list.add(Constants.CLASSPATH_SPRING_CONFIGURATION_LOCATION);
        list.add(Constants.GLOBAL_SPRING_CONFIGURATION_LOCATION);
    }

    /**
     * Set the directories to search in.
     *
     * @param list     A list of string pointing to directories.
     */
    public void setLocations(final List list) {
        this.locations = list;
    }

    /**
     * Set the settings.
     *
     * @param object The settings object.
     */
    public void setSettings(Settings object) {
        this.settings = object;
    }

    /**
     * Set the resource loader.
     *
     * @param loader The new resource loader.
     */
    public void setResourceLoader(final ResourceLoader loader) {
        this.resourceLoader = loader;
    }

    /**
     * Read all property files from the specified location and apply the
     * changes.
     *
     * @see org.springframework.beans.factory.config.PropertyResourceConfigurer#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
     */
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Processing bean factory: " + beanFactory);
        }
        final String mode = RunningModeHelper.determineRunningMode(this.settings != null ? this.settings.getRunningMode() : null);
        final Properties mergedProps = new Properties();

        if ( this.locations != null ) {
            final Iterator i = this.locations.iterator();
            while ( i.hasNext() ) {
                final String location = (String)i.next();
                ResourceUtils.readProperties(location, mergedProps, this.resourceLoader, this.logger);
                // read properties from running-mode dependent directory
                ResourceUtils.readProperties(location + '/' + mode, mergedProps, this.resourceLoader, this.logger);
            }
        }

        if (mergedProps.size() > 0) {
            // Convert the merged properties, if necessary.
            convertProperties(mergedProps);

            // Let the subclass process the properties.
            processProperties(beanFactory, mergedProps);
        }
    }
}
