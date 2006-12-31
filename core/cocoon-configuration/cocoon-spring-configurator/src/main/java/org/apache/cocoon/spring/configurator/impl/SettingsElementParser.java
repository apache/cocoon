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

import javax.servlet.ServletContext;

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.spring.configurator.BlockResourcesHolder;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Add a bean definition for the settings object to the bean factory.
 * 
 * @see SitemapNamespaceHandler
 * @see SettingsBeanFactoryPostProcessor
 * @version $Id$
 * @since 1.0
 */
public class SettingsElementParser extends AbstractSettingsElementParser {

    /** The name of the configuration attribute to specify the running mode. */
    public static final String RUNNING_MODE_ATTR = "runningMode";

    /** The name of the configuration attribute to specify if configurations are read from the classpath. */
    public static final String READ_FROM_CLASSPATH_ATTR = "readFromClasspath";
    
    /** The name of the configuration attribute to specify if configurations are read from the global location. */
    public static final String READ_FROM_GLOBAL_LOCATION_ATTR = "readFromGlobalLocation";

    /**
     * Create and register the settings bean factory post processor.
     */
    protected void createSettingsBeanFactoryPostProcessor(Element       element,
                                                          ParserContext parserContext,
                                                          String        runningMode) {
        // create bean definition for settings object
        final RootBeanDefinition beanDef = this.createBeanDefinition(SettingsBeanFactoryPostProcessor.class.getName(), "init", false);
        // add additional properties
        final Properties additionalProps = this.getAdditionalProperties(element);
        if ( additionalProps != null ) {
            beanDef.getPropertyValues().addPropertyValue("additionalProperties", additionalProps);                
        }

        // add additional property directories
        final List propertiesIncludes = this.getPropertyIncludes(element);
        if ( propertiesIncludes != null ) {
            beanDef.getPropertyValues().addPropertyValue("directories", propertiesIncludes);
        }

        // check for boolean settings
        final Boolean readFromClasspath = Boolean.valueOf(this.getAttributeValue(element, READ_FROM_CLASSPATH_ATTR, "true"));
        final Boolean readFromGlobalLocation = Boolean.valueOf(this.getAttributeValue(element, READ_FROM_GLOBAL_LOCATION_ATTR, "true"));

        beanDef.getPropertyValues().addPropertyValue(READ_FROM_CLASSPATH_ATTR, readFromClasspath);
        beanDef.getPropertyValues().addPropertyValue(READ_FROM_GLOBAL_LOCATION_ATTR, readFromGlobalLocation);

        // if running mode is specified add it as a property
        if (runningMode != null) {
            beanDef.getPropertyValues().addPropertyValue(RUNNING_MODE_ATTR, runningMode);
        }

        // register settings bean
        this.register(beanDef, Settings.ROLE, parserContext.getRegistry());        
    }

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element,
     *      org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        final String runningMode = RunningModeHelper.determineRunningMode( this.getAttributeValue(element, RUNNING_MODE_ATTR, null) );
        this.createSettingsBeanFactoryPostProcessor(element, parserContext, runningMode);

        // Get bean includes
        final List beanIncludes = this.getBeanIncludes(element);

        // register a PropertyPlaceholderConfigurer
        // we create a list with the default locations and add the optional location attribute
        final List dirs = new ArrayList();
        // check for boolean settings
        final boolean readFromClasspath = Boolean.valueOf(this.getAttributeValue(element, READ_FROM_CLASSPATH_ATTR, "true")).booleanValue();
        final boolean readFromGlobalLocation = Boolean.valueOf(this.getAttributeValue(element, READ_FROM_GLOBAL_LOCATION_ATTR, "true")).booleanValue();
        if ( readFromClasspath ) {
            dirs.add(Constants.CLASSPATH_SPRING_CONFIGURATION_LOCATION);
        }
        if ( readFromGlobalLocation ) {
            dirs.add(Constants.GLOBAL_SPRING_CONFIGURATION_LOCATION);
        }
        // If there are bean includes for a directory, we register them as well
        if ( beanIncludes.size() > 0 ) {
            // we need a list of directories
            final Iterator i = beanIncludes.iterator();
            while ( i.hasNext() ) {
                dirs.add(((IncludeInfo)i.next()).dir);
            }
        }
        if ( dirs.size() > 0 ) {
            this.registerPropertyOverrideConfigurer(parserContext, dirs);
        }

        // add the servlet context as a bean
        this.addComponent(ServletContextFactoryBean.class.getName(),
                          ServletContext.class.getName(),
                          null, false, parserContext.getRegistry());

        // deploy blocks and add a bean holding the information
        this.addComponent(DefaultBlockResourcesHolder.class.getName(), 
                          BlockResourcesHolder.class.getName(),
                          "init", true, parserContext.getRegistry());

        // handle includes - add default location
        if ( readFromClasspath ) {
            beanIncludes.add(0, new IncludeInfo(Constants.CLASSPATH_SPRING_CONFIGURATION_LOCATION, true));
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

        return null;
    }
}
