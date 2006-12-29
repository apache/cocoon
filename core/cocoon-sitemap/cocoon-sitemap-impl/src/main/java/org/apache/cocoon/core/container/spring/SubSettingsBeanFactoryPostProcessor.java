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

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.cocoon.configuration.MutableSettings;
import org.apache.cocoon.configuration.PropertyHelper;
import org.apache.cocoon.configuration.PropertyProvider;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.spring.configurator.ResourceUtils;
import org.apache.cocoon.spring.configurator.impl.AbstractSettingsBeanFactoryPostProcessor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.core.io.Resource;

/**
 * This is a bean factory post processor which sets up a child settings object.
 *
 * @since 2.2
 * @version $Id$
 */
public class SubSettingsBeanFactoryPostProcessor
    extends AbstractSettingsBeanFactoryPostProcessor {

    protected String location;

    protected List directories;

    protected boolean useDefaultIncludes = true;

    /**
     * Initialize this settings.
     * Setup the settings object.
     * @throws Exception
     */
    public void init()
    throws Exception {
        this.settings = this.createSettings();

        this.doInit();

        // settings can't be changed anymore
        this.settings.makeReadOnly();
    }

    public void setLocation(String sitemapUri) {
        this.location = sitemapUri;
    }

    public void setDirectories(List directories) {
        this.directories = directories;
    }

    public void setAdditionalProperties(Properties props) {
        this.additionalProperties = props;
    }

    public void setUseDefaultIncludes(boolean useDefaultIncludes) {
        this.useDefaultIncludes = useDefaultIncludes;
    }

    /**
     * Get the settings for Cocoon.
     * This method reads several property files and merges the result. If there
     * is more than one definition for a property, the last one wins.
     * The property files are read in the following order:
     * 1) PROPERTYDIR/*.properties
     *    Default values for the core and each block - the order in which the files are read is not guaranteed.
     * 2) PROPERTYDIR/[RUNNING_MODE]/*.properties
     *    Default values for the running mode - the order in which the files are read is not guaranteed.
     * 3) Property providers (ToBeDocumented)
     *
     * @return A new Settings object
     */
    protected MutableSettings createSettings() {
        final BeanFactory parentBeanFactory = ((HierarchicalBeanFactory)this.beanFactory).getParentBeanFactory();
        final Settings parent = (Settings)parentBeanFactory.getBean(Settings.ROLE);
        // get the running mode
        final String mode = parent.getRunningMode();
        // get properties
        final Properties properties = new Properties();

        // create an empty settings objects
        final MutableSettings s = new MutableSettings(parent);

        // read properties from default includes
        if ( this.useDefaultIncludes ) {
            ResourceUtils.readProperties(Constants.DEFAULT_CHILD_PROPERTIES_LOCATION, properties, this.getResourceLoader(), this.logger);
            // read all properties from the mode dependent directory
            ResourceUtils.readProperties(Constants.DEFAULT_CHILD_PROPERTIES_LOCATION + '/' + mode, properties, this.getResourceLoader(), this.logger);    
        }

        if ( this.directories != null ) {
            final Iterator i = directories.iterator();
            while ( i.hasNext() ) {
                final String directory = (String)i.next();
                // now read all properties from the properties directory
                ResourceUtils.readProperties(directory, properties, this.getResourceLoader(), this.logger);
                // read all properties from the mode dependent directory
                ResourceUtils.readProperties(directory + '/' + mode, properties, this.getResourceLoader(), this.logger);
            }
        }

        // Next look for a custom property provider in the parent bean factory
        if (parentBeanFactory.containsBean(PropertyProvider.ROLE) ) {
            try {
                final Resource r = this.resourceLoader.getResource(this.location);
                final PropertyProvider provider = (PropertyProvider)parentBeanFactory.getBean(PropertyProvider.ROLE);
                final Properties providedProperties = provider.getProperties(s, mode, r.getURL().toExternalForm());
                if ( providedProperties != null ) {
                    properties.putAll(providedProperties);
                }
            } catch (Exception ignore) {
                this.logger.warn("Unable to get properties from provider.", ignore);
                this.logger.warn("Continuing initialization.");            
            }
        }

        if ( this.additionalProperties != null ) {
            PropertyHelper.replaceAll(this.additionalProperties, s);
            properties.putAll(this.additionalProperties);
        }
        PropertyHelper.replaceAll(properties, parent);
        s.configure(properties);

        return s;
    }
}
