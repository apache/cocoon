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

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Our version of the property override configurer which uses the settings
 * object to get the properties.
 *
 * @version $Id$
 */
public class CocoonPropertyOverrideConfigurer extends PropertyOverrideConfigurer {

    protected String location = Constants.DEFAULT_SPRING_CONFIGURATION_LOCATION;
    protected ResourceLoader resourceLoader = new DefaultResourceLoader();

    public void setLocation(final String object) {
        this.location = object;
    }

    public void setResourceLoader(final ResourceLoader loader) {
        this.resourceLoader = loader;
    }

    /**
     * @see org.springframework.beans.factory.config.PropertyResourceConfigurer#postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory)
     */
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
    throws BeansException {
        if ( this.logger.isDebugEnabled() ) {
            this.logger.debug("Processing bean factory: " + beanFactory);
            this.logger.debug("Trying to read from directory: " + this.location);
        }
        final Properties mergedProps = new Properties();
        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(resourceLoader);
        final Resource dirResource = resourceLoader.getResource(this.location);

        if ( dirResource.exists() ) {
            if ( this.logger.isDebugEnabled() ) {
                this.logger.debug("Scanning directory: " + dirResource);
            }
            try {
                Resource[] resources = resolver.getResources(this.location + "/*.properties");
                if ( resources != null ) {
                    Arrays.sort(resources, AbstractSettingsBeanFactoryPostProcessor.getResourceComparator());
                    for(int i=0; i < resources.length; i++) {
                        if ( this.logger.isDebugEnabled() ) {
                            this.logger.debug("Reading property file: " + resources[i]);
                        }
                        final Properties p = new Properties();
                        p.load(resources[i].getInputStream());
                        mergedProps.putAll(p);
                    }
                }
            } catch (IOException ioe) {
                throw new BeanDefinitionStoreException("Unable to read property configurations from " + this.location, ioe);
            }
        }

        if ( mergedProps.size() > 0 ) {
            // Convert the merged properties, if necessary.
            convertProperties(mergedProps);
    
            // Let the subclass process the properties.
            processProperties(beanFactory, mergedProps);
        }
    }
}
