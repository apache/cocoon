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

import javax.servlet.ServletContext;

import org.apache.cocoon.configuration.Settings;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Add a bean definition for the settings object to the bean factory.
 * 
 * @see CocoonNamespaceHandler
 * @see SettingsBeanFactoryPostProcessor
 * @version $Id: SettingsElementParser.java 470732 2006-11-03 09:10:13Z
 *          cziegeler $
 * @since 2.2
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
        final String springConfigLocation = this.getAttributeValue(element, "location",
                Constants.DEFAULT_SPRING_CONFIGURATION_LOCATION);

        // create bean definition for settings object
        final String componentClassName = this.getAttributeValue(element, PROCESSOR_CLASS_NAME_ATTR,
                SettingsBeanFactoryPostProcessor.class.getName());
        final RootBeanDefinition beanDef = this.createBeanDefinition(componentClassName, "init", false);
        // if running mode is specified add it as a property
        final String runningMode = this.getAttributeValue(element, RUNNING_MODE_ATTR, null);
        if (runningMode != null) {
            beanDef.getPropertyValues().addPropertyValue("runningMode", runningMode);
        }
        // register settings bean
        this.register(beanDef, Settings.ROLE, parserContext.getRegistry());

        // register a PropertyPlaceholderConfigurer
        this.registerPropertyOverrideConfigurer(parserContext, springConfigLocation);

        // add the servlet context as a bean
        this.addComponent(ServletContextFactoryBean.class.getName(), ServletContext.class.getName(), null, false,
                parserContext.getRegistry());

        // handle includes
        try {
            this.handleBeanInclude(parserContext, null, Constants.DEFAULT_SPRING_CONFIGURATION_LOCATION, "*.xml", true);
            this.handleBeanInclude(parserContext, null, Constants.DEFAULT_SPRING_CONFIGURATION_LOCATION + "/" + runningMode, "*.xml", true);
        } catch (Exception e) {
            throw new BeanDefinitionStoreException("Unable to read spring configurations from " + Constants.DEFAULT_SPRING_CONFIGURATION_LOCATION, e);
        }
        return null;
    }
}
