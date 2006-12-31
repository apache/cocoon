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
package org.apache.cocoon.spring.configurator.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.spring.configurator.WebAppContextUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.web.context.WebApplicationContext;
import org.w3c.dom.Element;

/**
 * Add a bean definition for the settings object of a child context to the bean factory
 * and process all includes of spring configurations.
 *
 * @see SitemapNamespaceHandler
 * @see ChildSettingsBeanFactoryPostProcessor
 * @version $Id$
 * @since 1.0
 */
public class ChildSettingsElementParser extends AbstractSettingsElementParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        // get root application context
        final WebApplicationContext rootAppContext = WebAppContextUtils.getCurrentWebApplicationContext();
        // get running mode from root settings
        final String runningMode = ((Settings)rootAppContext.getBean(Settings.ROLE)).getRunningMode();

        // Get bean includes
        final List beanIncludes = this.getBeanIncludes(element);

        // If there are bean includes for a directory, we register a property placeholder configurer
        if ( beanIncludes.size() > 0 ) {
            // we need a list of directories
            final List dirs = new ArrayList(beanIncludes.size());
            final Iterator i = beanIncludes.iterator();
            while ( i.hasNext() ) {
                dirs.add(((IncludeInfo)i.next()).dir);
            }
            this.registerPropertyOverrideConfigurer(parserContext, dirs); 
        }

        // Create definition for child settings
        RootBeanDefinition def =  this.createBeanDefinition(ChildSettingsBeanFactoryPostProcessor.class.getName(),
                "init",
                false);
        def.getPropertyValues().addPropertyValue("name", element.getAttribute("name"));

        final Properties additionalProps = this.getAdditionalProperties(element);
        if ( additionalProps != null ) {
            def.getPropertyValues().addPropertyValue("additionalProperties", additionalProps);                
        }

        final List propertiesIncludes = this.getPropertyIncludes(element);
        if ( propertiesIncludes != null ) {
            def.getPropertyValues().addPropertyValue("directories", propertiesIncludes);
        }

        // process bean includes!
        final Iterator beanIncludeIterator = beanIncludes.iterator();
        while ( beanIncludeIterator.hasNext() ) {
            final IncludeInfo info = (IncludeInfo)beanIncludeIterator.next();

            try {
                this.handleBeanInclude(parserContext, info.dir, info.optional);
                this.handleBeanInclude(parserContext, info.dir + "/" + runningMode, true);
            } catch (Exception e) {
                throw new BeanDefinitionStoreException("Unable to read spring configurations from " + info.dir, e);
            }
        }

        // and now we register the child settings
        this.register(def, Settings.ROLE, parserContext.getRegistry());
        return null;
    }
}
