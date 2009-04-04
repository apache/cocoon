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

import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.spring.configurator.ResourceFilter;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Add a bean definition for the settings object to the bean factory.
 *
 * @see ConfiguratorNamespaceHandler
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
    protected void createSettingsBeanFactoryPostProcessor(Element element, ParserContext parserContext,
            String runningMode) {
        // create bean definition for settings object
        final RootBeanDefinition beanDef = this.createBeanDefinition(SettingsBeanFactoryPostProcessor.class.getName(),
                "init", false);
        
        //pass resource filter
        final ResourceFilter resourceFilter = getResourceFilter();
        if (resourceFilter != null) {
            beanDef.getPropertyValues().addPropertyValue("resourceFilter", resourceFilter);
        }
        // add additional properties
        final Properties additionalProps = this.getAdditionalProperties(element);
        if (additionalProps != null) {
            beanDef.getPropertyValues().addPropertyValue("additionalProperties", additionalProps);
        }

        // add additional property directories
        final List propertiesIncludes = this.getPropertyIncludes(element);
        if (propertiesIncludes != null) {
            beanDef.getPropertyValues().addPropertyValue("directories", propertiesIncludes);
        }

        // check for boolean settings
        final Boolean readFromClasspath = Boolean.valueOf(this.getAttributeValue(element, READ_FROM_CLASSPATH_ATTR,
                "true"));
        final Boolean readFromGlobalLocation = Boolean.valueOf(this.getAttributeValue(element,
                READ_FROM_GLOBAL_LOCATION_ATTR, "true"));

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
     * @see org.apache.cocoon.spring.configurator.impl.AbstractSettingsElementParser#getRunningMode(org.w3c.dom.Element)
     */
    protected String getRunningMode(Element e) {
        return RunningModeHelper.determineRunningMode(this.getAttributeValue(e, RUNNING_MODE_ATTR, null));
    }

    /**
     * @see org.apache.cocoon.spring.configurator.impl.AbstractSettingsElementParser#getBeanIncludes(org.w3c.dom.Element)
     */
    protected List getBeanIncludes(Element settingsElement) {
        final List includes = super.getBeanIncludes(settingsElement);
        final boolean readFromClasspath = Boolean.valueOf(
                this.getAttributeValue(settingsElement, READ_FROM_CLASSPATH_ATTR, "true")).booleanValue();
        if (readFromClasspath) {
            includes.add(0, Constants.CLASSPATH_SPRING_CONFIGURATION_LOCATION);
        }
        return includes;
    }

    /**
     * @see org.apache.cocoon.spring.configurator.impl.AbstractSettingsElementParser#getBeanPropertyOverrideIncludes(org.w3c.dom.Element)
     */
    protected List getBeanPropertyOverrideIncludes(Element settingsElement) {
        final List includes = super.getBeanPropertyOverrideIncludes(settingsElement);
        final boolean readFromClasspath = Boolean.valueOf(
                this.getAttributeValue(settingsElement, READ_FROM_CLASSPATH_ATTR, "true")).booleanValue();
        final boolean readFromGlobalLocation = Boolean.valueOf(
                this.getAttributeValue(settingsElement, READ_FROM_GLOBAL_LOCATION_ATTR, "true")).booleanValue();
        if (readFromGlobalLocation) {
            int pos = (readFromClasspath ? 1 : 0);
            includes.add(pos, Constants.GLOBAL_SPRING_CONFIGURATION_LOCATION);
        }
        return includes;
    }

    /**
     * @see org.apache.cocoon.spring.configurator.impl.AbstractSettingsElementParser#registerComponents(org.w3c.dom.Element,
     *      org.springframework.beans.factory.xml.ParserContext)
     */
    protected void registerComponents(Element settingsElement, ParserContext parserContext) {
        super.registerComponents(settingsElement, parserContext);
        // add the servlet context as a bean
        this.addComponent(ServletContextFactoryBean.class.getName(), ServletContext.class.getName(), null, false,
                parserContext.getRegistry());

    }
}
