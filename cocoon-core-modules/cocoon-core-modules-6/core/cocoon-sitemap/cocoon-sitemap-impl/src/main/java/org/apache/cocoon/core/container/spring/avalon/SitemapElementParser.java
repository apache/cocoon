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

import org.apache.cocoon.core.container.spring.logger.ChildLoggerFactoryBean;
import org.apache.cocoon.core.container.spring.logger.LoggerUtils;
import org.apache.cocoon.spring.configurator.WebAppContextUtils;
import org.apache.excalibur.source.SourceResolver;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.WebApplicationContext;
import org.w3c.dom.Element;

/**
 *
 * @since 2.2
 * @version $Id$
 */
public class SitemapElementParser extends BridgeElementParser {

    /**
     * @see org.apache.cocoon.core.container.spring.avalon.BridgeElementParser#createComponents(org.w3c.dom.Element, org.apache.cocoon.core.container.spring.avalon.ConfigurationInfo, org.springframework.beans.factory.support.BeanDefinitionRegistry, org.springframework.beans.factory.support.BeanDefinitionReader, org.springframework.core.io.ResourceLoader)
     */
    @Override
    public void createComponents(Element element, ConfigurationInfo info,
            BeanDefinitionRegistry registry, BeanDefinitionReader reader,
            ResourceLoader resourceLoader) throws Exception {
        
        super.createComponents(element, info, registry, reader, resourceLoader);
        // add string template parser for sitemap variable substitution
        final ChildBeanDefinition beanDef = 
                new ChildBeanDefinition("org.apache.cocoon.template.expression.AbstractStringTemplateParser");
        beanDef.setBeanClassName(
                "org.apache.cocoon.components.treeprocessor.variables.LegacySitemapStringTemplateParser");
        beanDef.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDef.setLazyInit(false);
        beanDef.getPropertyValues().addPropertyValue(
                "serviceManager", new RuntimeBeanReference("org.apache.avalon.framework.service.ServiceManager"));
        this.register(beanDef, "org.apache.cocoon.el.parsing.StringTemplateParser/legacySitemap", null, registry);

        final RootBeanDefinition resolverDef = new RootBeanDefinition();
        resolverDef.setBeanClassName(
                "org.apache.cocoon.components.treeprocessor.variables.StringTemplateParserVariableResolver");
        resolverDef.setLazyInit(false);
        resolverDef.setScope("prototype");
        resolverDef.getPropertyValues().addPropertyValue(
                "stringTemplateParser", new RuntimeBeanReference("org.apache.cocoon.el.parsing.StringTemplateParser/legacySitemap"));
        resolverDef.getPropertyValues().addPropertyValue(
                "objectModel", new RuntimeBeanReference("org.apache.cocoon.el.objectmodel.ObjectModel"));
        this.register(resolverDef, 
                "org.apache.cocoon.components.treeprocessor.variables.VariableResolver", null, registry);
    }

    /**
     * @see BridgeElementParser#addContext(Element, BeanDefinitionRegistry)
     */
    @Override
    protected void addContext(Element element, BeanDefinitionRegistry registry) {
        // we get the uriPrefix from the configuration
        final String uriPrefix = element.getAttribute("uriPrefix");
        RootBeanDefinition beanDefinition = createBeanDefinition(AvalonSitemapContextFactoryBean.class,
                                                                 "init",
                                                                 false);
        beanDefinition.getPropertyValues().addPropertyValue("uriPrefix", uriPrefix);
        register(beanDefinition, AvalonUtils.CONTEXT_ROLE, registry);
    }

    /**
     * Add the logger bean.
     *
     * @param registry       The bean registry.
     * @param loggerCategory The optional category for the logger.
     */
    @Override
    protected void addLogger(BeanDefinitionRegistry registry,
                             String                 loggerCategory) {
        final RootBeanDefinition beanDef = createBeanDefinition(ChildLoggerFactoryBean.class, "init", false);
        if (loggerCategory != null) {
            beanDef.getPropertyValues().addPropertyValue("category", loggerCategory);
        }
        register(beanDef, LoggerUtils.LOGGER_ROLE, registry);
    }

    /**
     * @see BridgeElementParser#readConfiguration(String, ResourceLoader)
     */
    @Override
    protected ConfigurationInfo readConfiguration(String location, ResourceLoader resourceLoader) throws Exception {
        WebApplicationContext parentContext = WebAppContextUtils.getCurrentWebApplicationContext();
        return ConfigurationReader.readSitemap(
                (ConfigurationInfo) parentContext.getBean(ConfigurationInfo.class.getName()),
                location,
                new SourceResourceLoader(resourceLoader, (SourceResolver) parentContext.getBean(SourceResolver.ROLE)));
    }

    @Override
    protected String getConfigurationLocation() {
        return "config/avalon";
    }
}
