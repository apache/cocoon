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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.cocoon.spring.configurator.ResourceFilter;
import org.apache.cocoon.spring.configurator.ResourceUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.w3c.dom.Element;

/**
 * Abstract class for the settings element parsers.
 *
 * @see ChildSettingsElementParser
 * @see SettingsElementParser
 * @version $Id$
 * @since 1.0
 */
public abstract class AbstractSettingsElementParser extends AbstractElementParser {

    /**
     * Get the current running mode
     */
    protected abstract String getRunningMode(Element e);

    /**
     * Create and register the settings bean factory post processor.
     */
    protected abstract void createSettingsBeanFactoryPostProcessor(Element       element,
                                                                   ParserContext parserContext,
                                                                   String        runningMode);

    private ResourceFilter resourceFilter;
    
    /**
     * Get additional includes of property directories.
     */
    protected List getPropertyIncludes(Element childSettingsElement) {
        List propertyDirs = null;
        if ( childSettingsElement != null ) {
            final Element[] propertyDirConfigs = this.getChildElements(childSettingsElement, "include-properties");
            if ( propertyDirConfigs != null && propertyDirConfigs.length > 0 ) {
                propertyDirs = new ArrayList();
                for(int i=0; i < propertyDirConfigs.length; i++) {
                    propertyDirs.add(this.getAttributeValue(propertyDirConfigs[i], "dir", null));
                }
            }
        }
        return propertyDirs;
    }

    /**
     * Get additional properties.
     */
    protected Properties getAdditionalProperties(Element childSettingsElement) {
        Properties variables = null;
        final Element[] properties = this.getChildElements(childSettingsElement, "property");
        if ( properties != null && properties.length > 0 ) {
            variables = new Properties();
            for(int i=0; i<properties.length; i++) {
                variables.setProperty(this.getAttributeValue(properties[i], "name", null),
                                      this.getAttributeValue(properties[i], "value", null));
            }
        }
        return variables;
    }

    /**
     * Get additional includes of bean configurations.
     */
    protected List getBeanIncludes(Element childSettingsElement) {
        final List includes = new ArrayList();
        // search for includes
        if ( childSettingsElement.hasChildNodes() ) {
            final Element[] includeElements = this.getChildElements(childSettingsElement, "include-beans");
            if ( includeElements != null ) {
                for(int i = 0 ; i < includeElements.length; i++ ) {
                    final String dir = this.getAttributeValue(includeElements[i], "dir", null);

                    includes.add(dir);
                }
            }
        }
        return includes;
    }

    /**
     * Return the includes for the property override configuration
     */
    protected List getBeanPropertyOverrideIncludes(Element settingsElement) {
        return this.getBeanIncludes(settingsElement);
    }

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        final String runningMode = this.getRunningMode(element);
        
        try {
            this.resourceFilter = getResourceFilter(element);
        } catch (Exception e) {
            throw new BeanDefinitionStoreException("Unable to read filter configuration", e);
        }

        // create factory for settings object
        this.createSettingsBeanFactoryPostProcessor(element, parserContext, runningMode);

        // Get bean includes for property overrides
        final List overridePropertyIncludes = this.getBeanPropertyOverrideIncludes(element);

        // If there are bean includes for a directory, we register a property placeholder configurer
        if ( overridePropertyIncludes.size() > 0 ) {
            this.registerPropertyOverrideConfigurer(parserContext, overridePropertyIncludes);
        }

        // register additonal components
        this.registerComponents(element, parserContext);

        // Get bean includes
        final List beanIncludes = this.getBeanIncludes(element);
        // process bean includes!
        final Iterator beanIncludeIterator = beanIncludes.iterator();
        while ( beanIncludeIterator.hasNext() ) {
            final String dir = (String)beanIncludeIterator.next();

            try {
                this.handleBeanInclude(parserContext, dir, false);
                this.handleBeanInclude(parserContext, dir + "/" + runningMode, true);
            } catch (Exception e) {
                throw new BeanDefinitionStoreException("Unable to read spring configurations from " + dir, e);
            }
        }

        return null;
    }

    /**
     * This method can be used for subclasses to register additional components.
     */
    protected void registerComponents(Element settingsElement, ParserContext parserContext) {
        // nothing to do here
    }

    /**
     * Handle include for spring bean configurations.
     */
    protected void handleBeanInclude(final ParserContext parserContext,
                                     final String        path,
                                     final boolean       optional)
    throws Exception {
        final ResourcePatternResolver resolver = 
            (ResourcePatternResolver) parserContext.getReaderContext().getReader().getResourceLoader();

        // check if the directory to read from exists
        // we only check if optional is set to true
        boolean load = true;
        if ( optional
             && !ResourceUtils.isClasspathUri(path) ) {
            final Resource rsrc = resolver.getResource(path);
            if ( !rsrc.exists()) {
                load = false;
            }
        }
        if ( load ) {
            try {
                Resource[] resources = resolver.getResources(path + "/*.xml");
                resources = ResourceUtils.filterResources(resources, getResourceFilter());
                Arrays.sort(resources, ResourceUtils.getResourceComparator());
                for (int i = 0; i < resources.length; i++) {
                    this.handleImport(parserContext, resources[i].getURL().toExternalForm());
                }
            } catch (IOException ioe) {
                throw new Exception("Unable to read configurations from " + path, ioe);
            }
        }
    }

    protected void handleImport(ParserContext parserContext, String uri) {
        final ResourceLoader resourceLoader = parserContext.getReaderContext().getReader().getResourceLoader();
        parserContext.getDelegate().getReaderContext().getReader().loadBeanDefinitions(resourceLoader.getResource(uri));
    }

    /**
     * Register a property placeholder configurer. The configurer will read all
     * *.properties files from the specified locations.
     *
     * @param parserContext
     * @param locations
     */
    protected void registerPropertyOverrideConfigurer(final ParserContext parserContext,
                                                      final List          locations) {
        final RootBeanDefinition beanDef = this.createBeanDefinition(ExtendedPropertyOverrideConfigurer.class.getName(),
                null, true);
        beanDef.getPropertyValues().addPropertyValue("locations", locations);
        beanDef.getPropertyValues().addPropertyValue("resourceLoader",
                parserContext.getReaderContext().getReader().getResourceLoader());
        beanDef.getPropertyValues().addPropertyValue("resourceFilter", getResourceFilter());
        beanDef.getPropertyValues().addPropertyValue("beanNameSeparator", "/");
        this.register(beanDef, ExtendedPropertyOverrideConfigurer.class.getName(), parserContext.getRegistry());
    }
    
    
    protected ResourceFilter getResourceFilter(Element e) throws Exception {
        Element[] filters = this.getChildElements(e, "filter");
        
        if (filters.length == 0)
            return null;
        else if (filters.length > 1)
            throw new RuntimeException("Only one filter definition is allowed and you configured " + filters.length + " filters.");
        
        String filterClassName = filters[0].getAttribute("class");
        if (filterClassName.length() == 0)
            throw new RuntimeException("Missing 'class' attribute.");
        return (ResourceFilter)java.lang.Class.forName(filterClassName).newInstance();
    }
    
    protected final ResourceFilter getResourceFilter() {
        return this.resourceFilter;
    }
}
