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

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.spring.configurator.WebAppContextUtils;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.web.context.WebApplicationContext;
import org.w3c.dom.Element;

/**
 * Add a bean definition for the settings object of a child context to the bean factory
 * and process all includes of spring configurations.
 *
 * @see ConfiguratorNamespaceHandler
 * @see ChildSettingsBeanFactoryPostProcessor
 * @version $Id$
 * @since 1.0
 */
public class ChildSettingsElementParser extends AbstractSettingsElementParser {

    /**
     * @see org.apache.cocoon.spring.configurator.impl.AbstractSettingsElementParser#getRunningMode(org.w3c.dom.Element)
     */
    protected String getRunningMode(Element e) {
        // get root application context
        final WebApplicationContext rootAppContext = WebAppContextUtils.getCurrentWebApplicationContext();
        // get running mode from root settings
        return ((Settings)rootAppContext.getBean(Settings.ROLE)).getRunningMode();
    }

    /**
     * Create and register the settings bean factory post processor.
     */
    protected void createSettingsBeanFactoryPostProcessor(Element       element,
                                                          ParserContext parserContext,
                                                          String        runningMode) {
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

        // and now we register the child settings
        this.register(def, Settings.ROLE, parserContext.getRegistry());
    }
}
