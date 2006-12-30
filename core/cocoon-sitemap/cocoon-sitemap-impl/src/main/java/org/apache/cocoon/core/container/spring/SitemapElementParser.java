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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.spring.configurator.impl.AbstractElementParser;
import org.apache.cocoon.spring.configurator.impl.RunningModeHelper;
import org.apache.cocoon.spring.configurator.impl.SettingsElementParser;
import org.apache.cocoon.xml.dom.DomHelper;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Add a bean definition for the settings object of a child context to the bean factory
 * and process all includes of spring configurations.
 *
 * @see SitemapNamespaceHandler
 * @see ChildSettingsBeanFactoryPostProcessor
 * @version $Id$
 * @since 2.2
 */
public class SitemapElementParser extends AbstractElementParser {

    protected Element getComponentsElement(Element rootElement) {
        final Element componentsElement = DomHelper.getChildElement(rootElement, rootElement.getNamespaceURI(), "components");
        return componentsElement;
    }

    protected List getPropertyIncludes(Element componentsElement)
    throws Exception {
        List propertyDirs = null;
        if ( componentsElement != null ) {
            final Element[] propertyDirConfigs = DomHelper.getChildElements(componentsElement, componentsElement.getNamespaceURI(), "include-properties");
            if ( propertyDirConfigs != null && propertyDirConfigs.length > 0 ) {
                propertyDirs = new ArrayList();
                for(int i=0; i < propertyDirConfigs.length; i++) {
                    propertyDirs.add(DomHelper.getAttribute(propertyDirConfigs[i], "dir"));
                }
            }
        }
        return propertyDirs;        
    }

    /**
     * Get additional properties.
     */
    protected Properties getAdditionalProperties(Element rootElement)
    throws Exception {
        Properties variables = null;
        final Element[] properties = DomHelper.getChildElements(rootElement, rootElement.getNamespaceURI(), "property");
        if ( properties != null && properties.length > 0 ) {
            variables = new Properties();
            for(int i=0; i<properties.length; i++) {
                variables.setProperty(DomHelper.getAttribute(properties[i], "name"), DomHelper.getAttribute(properties[i], "value"));
            }
        }
        return variables;
    }

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        final String runningMode = RunningModeHelper.determineRunningMode( this.getAttributeValue(element, SettingsElementParser.RUNNING_MODE_ATTR, null) );
        final String location = element.getAttribute("location");
        try {
            final boolean useDefaultIncludes = Boolean.valueOf(this.getAttributeValue(element, "useDefaultIncludes", "true")).booleanValue();

            // register a PropertyPlaceholderConfigurer
            if ( useDefaultIncludes ) {
                this.registerPropertyOverrideConfigurer(parserContext, Collections.singletonList(Constants.DEFAULT_CHILD_SPRING_CONFIGURATION_LOCATION));
            }
            
            RootBeanDefinition def =  this.createBeanDefinition(ChildSettingsBeanFactoryPostProcessor.class.getName(),
                    "init",
                    false);
            def.getPropertyValues().addPropertyValue("location", location);
            def.getPropertyValues().addPropertyValue("useDefaultIncludes", Boolean.valueOf(useDefaultIncludes));

            final Properties additionalProps = this.getAdditionalProperties(element);
            if ( additionalProps != null ) {
                def.getPropertyValues().addPropertyValue("additionalProperties", additionalProps);                
            }

            final List includes = this.getPropertyIncludes(element);
            if ( includes != null ) {
                def.getPropertyValues().addPropertyValue("directories", includes);
            }

            if ( useDefaultIncludes ) {
                this.handleBeanInclude(parserContext, null, Constants.DEFAULT_CHILD_SPRING_CONFIGURATION_LOCATION, "*.xml", true);
                this.handleBeanInclude(parserContext, null, Constants.DEFAULT_CHILD_SPRING_CONFIGURATION_LOCATION + "/" + runningMode, "*.xml", true);
            }
            // search for includes
            if ( element.hasChildNodes() ) {
                final Element[] includeElements = DomHelper.getChildElements(element, element.getNamespaceURI(), "include-beans");
                if ( includeElements != null ) {
                    for(int i = 0 ; i < includeElements.length; i++ ) {
                        final String src = DomHelper.getAttribute(includeElements[i], "src", null);
                        final String dir = DomHelper.getAttribute(includeElements[i], "dir", null);
                        final String pattern = DomHelper.getAttribute(includeElements[i], "pattern", "*.xml");
                        final boolean optional = DomHelper.getAttributeAsBoolean(includeElements[i], "optional", false);

                        this.handleBeanInclude(parserContext, src, dir, pattern, optional);
                        
                        // TODO do we really need both src/dir attributes? The
                        // quiet precedence of 'src' over 'dir' attribute is at
                        // least unclear.
                        if (src == null && dir != null)
                            this.handleBeanInclude(parserContext, null, dir + "/" + runningMode, pattern, optional);
                    }
                }
            }
            this.register(def, Settings.ROLE, parserContext.getRegistry());
        } catch (Exception e) {
            throw new BeanDefinitionStoreException("Unable to process sitemap at '" + location + "'.",e);
        }
        return null;
    }
}
