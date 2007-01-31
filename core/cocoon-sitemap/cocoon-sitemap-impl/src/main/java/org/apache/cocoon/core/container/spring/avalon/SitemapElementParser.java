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
package org.apache.cocoon.core.container.spring.avalon;

import org.apache.cocoon.spring.configurator.WebAppContextUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.WebApplicationContext;
import org.w3c.dom.Element;

/**
 * @version $Id$
 * @since 2.2
 */
public class SitemapElementParser extends BridgeElementParser {

    /**
     * @see org.apache.cocoon.core.container.spring.avalon.BridgeElementParser#addContext(org.w3c.dom.Element, org.springframework.beans.factory.support.BeanDefinitionRegistry)
     */
    protected void addContext(Element element, BeanDefinitionRegistry registry) {
        // we get the uriPrefix from the configuration
        final String uriPrefix = element.getAttribute("uriPrefix");
        RootBeanDefinition beanDefinition = this.createBeanDefinition(AvalonSitemapContextFactoryBean.class,
                                                                      "init",
                                                                      false);
        beanDefinition.getPropertyValues().addPropertyValue("uriPrefix", uriPrefix);
        this.register(beanDefinition, AvalonUtils.CONTEXT_ROLE, registry);
    }

    /**
     * Add the logger bean.
     * @param registry       The bean registry.
     * @param loggerCategory The optional category for the logger.
     */
    protected void addLogger(BeanDefinitionRegistry registry,
                             String                 loggerCategory) {
        final RootBeanDefinition beanDef = this.createBeanDefinition(AvalonChildLoggerFactoryBean.class, "init", false);
        if ( loggerCategory != null ) {
            beanDef.getPropertyValues().addPropertyValue("category", loggerCategory);
        }
        this.register(beanDef, AvalonUtils.LOGGER_ROLE, registry);
    }

    /**
     * @see org.apache.cocoon.core.container.spring.avalon.BridgeElementParser#readConfiguration(java.lang.String, org.springframework.core.io.ResourceLoader)
     */
    protected ConfigurationInfo readConfiguration(String location, ResourceLoader resourceLoader)
    throws Exception {
        WebApplicationContext parentContext = WebAppContextUtils.getCurrentWebApplicationContext();
        return ConfigurationReader.readSitemap((ConfigurationInfo)parentContext.getBean(ConfigurationInfo.class.getName()), location, resourceLoader);
    }

    protected String getConfigurationLocation() {
        return "config/avalon";
    }
}
