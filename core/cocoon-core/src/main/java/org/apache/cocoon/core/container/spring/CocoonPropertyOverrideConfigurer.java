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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;

/**
 * Our version of the property override configurer which uses the settings
 * object to get the properties.
 * @version $Id$
 */
public class CocoonPropertyOverrideConfigurer extends PropertyOverrideConfigurer {

    protected Settings settings;

    public void setSettings(Settings object) {
        this.settings = object;
    }

    /**
     * @see org.springframework.beans.factory.config.PropertyResourceConfigurer#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
     */
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Properties mergedProps = new SettingsProperties(this.settings);

        // Convert the merged properties, if necessary.
        convertProperties(mergedProps);

        // Let the subclass process the properties.
        processProperties(beanFactory, mergedProps);
    }
}
