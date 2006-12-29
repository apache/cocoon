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
import java.util.List;

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
public class SettingsElementParser extends AbstractElementParser {

    /**
     * The name of the configuration attribute to use a different processor
     * class.
     */
    public static final String PROCESSOR_CLASS_NAME_ATTR = "processorClassName";

    /** The name of the configuration attribute to specify the running mode. */
    public static final String RUNNING_MODE_ATTR = "runningMode";

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element,
     *      org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {

        // create bean definition for settings object
        final String componentClassName = this.getAttributeValue(element, PROCESSOR_CLASS_NAME_ATTR,
                SettingsBeanFactoryPostProcessor.class.getName());
        final RootBeanDefinition beanDef = this.createBeanDefinition(componentClassName, "init", false);
        // if running mode is specified add it as a property
        final String runningMode = RunningModeHelper.determineRunningMode( this.getAttributeValue(element, RUNNING_MODE_ATTR, null) );
        if (runningMode != null) {
            beanDef.getPropertyValues().addPropertyValue("runningMode", runningMode);
        }
        // register settings bean
        this.register(beanDef, Settings.ROLE, parserContext.getRegistry());

        // register a PropertyPlaceholderConfigurer
        // we create a list with the default locations and add the optional location attribute
        final List locations = new ArrayList();
        locations.add(Constants.CLASSPATH_SPRING_CONFIGURATION_LOCATION);
        locations.add(Constants.GLOBAL_SPRING_CONFIGURATION_LOCATION);
        final String springConfigLocation = this.getAttributeValue(element, "location", null);
        if ( springConfigLocation != null ) {
            locations.add(springConfigLocation);
        }
        this.registerPropertyOverrideConfigurer(parserContext, locations);

        // add the servlet context as a bean
        this.addComponent(ServletContextFactoryBean.class.getName(),
                          ServletContext.class.getName(),
                          null, false, parserContext.getRegistry());

        // deploy blocks and add a bean holding the information
        this.addComponent(DefaultBlockResourcesHolder.class.getName(), 
                          BlockResourcesHolder.class.getName(),
                          "init", true, parserContext.getRegistry());

        // handle includes
        try {
            this.handleBeanInclude(parserContext, null, Constants.CLASSPATH_SPRING_CONFIGURATION_LOCATION, "*.xml", true);
            this.handleBeanInclude(parserContext, null, Constants.CLASSPATH_SPRING_CONFIGURATION_LOCATION + "/" + runningMode, "*.xml", true);
        } catch (Exception e) {
            throw new BeanDefinitionStoreException("Unable to read spring configurations from " + Constants.CLASSPATH_SPRING_CONFIGURATION_LOCATION, e);
        }

        if ( springConfigLocation != null ) {
            try {
                this.handleBeanInclude(parserContext, null, springConfigLocation, "*.xml", true);
                this.handleBeanInclude(parserContext, null, springConfigLocation + "/" + runningMode, "*.xml", true);            
            } catch (Exception e) {
                throw new BeanDefinitionStoreException("Unable to read spring configurations from " + springConfigLocation, e);
            }
        }
        return null;
    }
}
