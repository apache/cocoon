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
package org.apache.cocoon.core.container.spring.pipeline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.cocoon.components.pipeline.impl.PipelineComponentInfo;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version $Id$
 * @since 2.2
 */
public class PipelineComponentInfoInitializerDecorator implements BeanDefinitionDecorator {

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.xml.BeanDefinitionDecorator#decorate(org.w3c.dom.Node,
     * org.springframework.beans.factory.config.BeanDefinitionHolder,
     * org.springframework.beans.factory.xml.ParserContext)
     */
    @Override
    public BeanDefinitionHolder decorate(Node source, BeanDefinitionHolder holder, ParserContext ctx) {
        registerPipelineComponentInfo(ctx);
        String initializerBeanName = registerPipelineComponentInfoInitializer(source, holder, ctx);
        createDependencyOnPipelineComponentInfoInitializer(holder, initializerBeanName);
        return holder;
    }

    private void registerPipelineComponentInfo(ParserContext ctx) {
        if (!ctx.getRegistry().containsBeanDefinition(PipelineComponentInfo.ROLE)) {
            BeanDefinitionBuilder defBuilder = BeanDefinitionBuilder.rootBeanDefinition(
                    PipelineComponentInfoFactoryBean.class);
            defBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
            defBuilder.setLazyInit(false);
            defBuilder.setInitMethodName("init");
            ctx.getRegistry().registerBeanDefinition(PipelineComponentInfo.ROLE, defBuilder.getBeanDefinition());
        }
    }

    private String registerPipelineComponentInfoInitializer(Node source, BeanDefinitionHolder holder, ParserContext ctx) {
        String componentName = holder.getBeanName();
        String mimeType = ((Element) source).hasAttribute("mime-type")
                ? ((Element) source).getAttribute("mime-type")
                : null;
        String label = ((Element) source).hasAttribute("label")
                ? ((Element) source).getAttribute("label")
                : null;
        String hint = ((Element) source).hasAttribute("hint")
                ? ((Element) source).getAttribute("hint")
                : null;

        BeanDefinitionBuilder initializer =
                BeanDefinitionBuilder.rootBeanDefinition(PipelineComponentInfoInitializer.class);
        initializer.addPropertyReference("info", PipelineComponentInfo.ROLE);
        initializer.addPropertyValue("componentName", componentName);
        if (mimeType != null) {
            initializer.addPropertyValue("mimeType", mimeType);
        }
        if (label != null) {
            initializer.addPropertyValue("label", label);
        }
        if (hint != null) {
            initializer.addPropertyValue("hint", hint);
        }
        initializer.setInitMethodName("init");

        String beanName = componentName + "/info";
        ctx.getRegistry().registerBeanDefinition(beanName, initializer.getBeanDefinition());

        return beanName;
    }

    private void createDependencyOnPipelineComponentInfoInitializer(BeanDefinitionHolder holder,
            String initializerBeanName) {
        AbstractBeanDefinition definition = ((AbstractBeanDefinition) holder.getBeanDefinition());
        String[] dependsOn = definition.getDependsOn();
        if (dependsOn == null) {
            dependsOn = new String[] { initializerBeanName };
        } else {
            List<String> dependencies = new ArrayList<String>(Arrays.asList(dependsOn));
            dependencies.add(initializerBeanName);
            dependsOn = dependencies.toArray(new String[dependencies.size()]);
        }
        definition.setDependsOn(dependsOn);
    }
}
