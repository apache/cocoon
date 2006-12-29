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
import org.apache.cocoon.util.Deprecation;
import org.apache.cocoon.xml.dom.DomHelper;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

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

    protected Element readSitemap(String location, ResourceLoader resourceLoader)
    throws Exception {
        // read the sitemap
        final Resource sitemapResource = resourceLoader.getResource(location);
        final InputSource is = new InputSource(sitemapResource.getInputStream());
        is.setSystemId(sitemapResource.getURL().toExternalForm());
        final Document doc = DomHelper.parse(is);
        final Element rootElement = doc.getDocumentElement();
        return rootElement;
    }

    protected Element getComponentsElement(Element rootElement) {
        final Element componentsElement = DomHelper.getChildElement(rootElement, rootElement.getNamespaceURI(), "components");
        return componentsElement;
    }

    protected boolean isUseDefaultIncludes(Element componentsElement) {
        if ( componentsElement != null ) {
            return DomHelper.getAttributeAsBoolean(componentsElement, "use-default-includes", true);
        }
        return true;
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
     * compatibility with 2.1.x - check for global variables in sitemap
     * TODO - This will be removed in later versions!
     */
    protected Properties getGlobalSitemapVariables(Element rootElement)
    throws Exception {
        Properties variables = null;
        final Element pipelines = DomHelper.getChildElement(rootElement, rootElement.getNamespaceURI(), "pipelines");
        if ( pipelines != null ) {
            final Element configs = DomHelper.getChildElement(pipelines, pipelines.getNamespaceURI(), "component-configurations");
            if ( configs != null ) {
                Deprecation.logger.warn("The 'component-configurations' section in the sitemap is deprecated. Please check for alternatives.");
                variables = new Properties();
                // now check for global variables - if any other element occurs: throw exception
                Element[] children = DomHelper.getChildElements(configs, configs.getNamespaceURI());
                for(int i=0; i<children.length; i++) {
                    if ( "global-variables".equals(children[i].getLocalName()) ) {
                        Element[] variableElements = DomHelper.getChildElements(children[i], children[i].getNamespaceURI());
                        for(int v=0; v<variableElements.length; v++) {
                            variables.setProperty(variableElements[v].getLocalName(), DomHelper.getElementText(variableElements[v]));
                        }
                    } else {
                        throw new Exception("Component configurations in the sitemap are not allowed for component: " + children[i].getLocalName());
                    }
                }
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
        final ResourceLoader resourceLoader = parserContext.getReaderContext().getReader().getResourceLoader();
        try {
            final Element rootElement = this.readSitemap(location, resourceLoader);
            final Element componentsElement = this.getComponentsElement(rootElement);
            final boolean useDefaultIncludes = this.isUseDefaultIncludes(componentsElement);

            // register a PropertyPlaceholderConfigurer
            if ( useDefaultIncludes ) {
                this.registerPropertyOverrideConfigurer(parserContext, Collections.singletonList(Constants.DEFAULT_CHILD_SPRING_CONFIGURATION_LOCATION));
            }
            
            RootBeanDefinition def =  this.createBeanDefinition(ChildSettingsBeanFactoryPostProcessor.class.getName(),
                    "init",
                    false);
            def.getPropertyValues().addPropertyValue("location", location);
            def.getPropertyValues().addPropertyValue("useDefaultIncludes", Boolean.valueOf(useDefaultIncludes));

            final Properties globalSitemapVariables = this.getGlobalSitemapVariables(rootElement);
            if ( globalSitemapVariables != null ) {
                def.getPropertyValues().addPropertyValue("additionalProperties", globalSitemapVariables);                
            }

            final List includes = this.getPropertyIncludes(componentsElement);
            if ( includes != null ) {
                def.getPropertyValues().addPropertyValue("directories", includes);
            }

            if ( useDefaultIncludes ) {
                this.handleBeanInclude(parserContext, null, Constants.DEFAULT_CHILD_SPRING_CONFIGURATION_LOCATION, "*.xml", true);
                this.handleBeanInclude(parserContext, null, Constants.DEFAULT_CHILD_SPRING_CONFIGURATION_LOCATION + "/" + runningMode, "*.xml", true);
            }
            // search for includes
            if ( componentsElement != null ) {
                final Element[] includeElements = DomHelper.getChildElements(componentsElement, componentsElement.getNamespaceURI(), "include-beans");
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
