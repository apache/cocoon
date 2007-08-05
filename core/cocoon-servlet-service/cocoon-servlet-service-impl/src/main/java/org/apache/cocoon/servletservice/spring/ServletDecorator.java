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
package org.apache.cocoon.servletservice.spring;

import java.util.Map;

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
public class ServletDecorator implements BeanDefinitionDecorator {

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.xml.BeanDefinitionDecorator#decorate(org.w3c.dom.Node, org.springframework.beans.factory.config.BeanDefinitionHolder, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinitionHolder decorate(Node source, BeanDefinitionHolder holder, ParserContext ctx) {
        String embeddedServletBeanName = registerEmbeddedServletBean(holder, ctx);
        return createServletFactoryBeanDefinition((Element) source, holder, ctx, embeddedServletBeanName);
    }

    private String registerEmbeddedServletBean(BeanDefinitionHolder holder, ParserContext ctx) {
        String beanName = holder.getBeanName() + "/embedded";

        AbstractBeanDefinition definition = ((AbstractBeanDefinition) holder.getBeanDefinition());
        ctx.getRegistry().registerBeanDefinition(beanName, definition);

        return beanName;
    }

    private BeanDefinitionHolder createServletFactoryBeanDefinition(Element source,
                                                                    BeanDefinitionHolder holder,
                                                                    ParserContext ctx,
                                                                    String embeddedServletBeanName) {
        String ns = source.getNamespaceURI();
        String mountPath = source.hasAttribute("mount-path") ? source.getAttribute("mount-path") : null;
        String contextPath = source.hasAttribute("context-path") ? source.getAttribute("context-path") : null;

        Element initParamsElem = (Element) source.getElementsByTagNameNS(ns, "init-params").item(0);
        Element contextParamsElem = (Element) source.getElementsByTagNameNS(ns, "context-params").item(0);
        Element connectionsElem = (Element) source.getElementsByTagNameNS(ns, "connections").item(0);

        BeanDefinitionBuilder servletFactoryDefBuilder = BeanDefinitionBuilder.rootBeanDefinition(ServletFactoryBean.class);
        servletFactoryDefBuilder.setSource(ctx.extractSource(source));
        servletFactoryDefBuilder.addPropertyReference("embeddedServlet", embeddedServletBeanName);
        servletFactoryDefBuilder.setInitMethodName("init");
        servletFactoryDefBuilder.setDestroyMethodName("destroy");

        if (mountPath != null) {
            servletFactoryDefBuilder.addPropertyValue("mountPath", mountPath);
        }
        if (contextPath != null) {
            servletFactoryDefBuilder.addPropertyValue("contextPath", contextPath);
        }
        if (initParamsElem != null) {
            Map initParams = ctx.getDelegate().parseMapElement(initParamsElem, null);
            servletFactoryDefBuilder.addPropertyValue("initParams", initParams);
        }
        if (contextParamsElem != null) {
            Map contextParams = ctx.getDelegate().parseMapElement(contextParamsElem, null);
            servletFactoryDefBuilder.addPropertyValue("contextParams", contextParams);
        }
        if (connectionsElem != null) {
            Map connections = ctx.getDelegate().parseMapElement(connectionsElem, null);
            servletFactoryDefBuilder.addPropertyValue("connections", connections);
        }

        return new BeanDefinitionHolder(servletFactoryDefBuilder.getBeanDefinition(), holder.getBeanName());
    }
}
