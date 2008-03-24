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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <p>
 * This {@link BeanDefinitionDecorator} deals with the
 * <code>http://cocoon.apache.org/schema/servlet</code> namespace which
 * defines following elements: <code>context</code> : with optional attributes
 * <code>mountPath</code> and <code>contextPath</code>. With optional sub
 * elements <code>init-params", "context-params" and "connections".</p>
 * <p>
 * The actual creation of the servlet service (= a bean) is done by {@link ServletFactoryBean}.</p>
 *
 * @version $Id$
 * @since 1.0.0
 */
public class ServletDecorator implements BeanDefinitionDecorator {

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

    private BeanDefinitionHolder createServletFactoryBeanDefinition(Element source, BeanDefinitionHolder holder,
                    ParserContext ctx, String embeddedServletBeanName) {
        String ns = source.getNamespaceURI();
        if (!source.hasAttribute("mount-path"))
            throw new RuntimeException("The mount-path attribute is required.");
        String mountPath = source.getAttribute("mount-path");
        if (!source.hasAttribute("context-path"))
            throw new RuntimeException("The context-path attribute is required.");
        String contextPath = source.getAttribute("context-path");

        Element initParamsElem = (Element) source.getElementsByTagNameNS(ns, "init-params").item(0);
        Element contextParamsElem = (Element) source.getElementsByTagNameNS(ns, "context-params").item(0);
        Element connectionsElem = (Element) source.getElementsByTagNameNS(ns, "connections").item(0);

        BeanDefinitionBuilder servletFactoryDefBuilder = BeanDefinitionBuilder
                        .rootBeanDefinition(ServletFactoryBean.class);
        servletFactoryDefBuilder.setSource(ctx.extractSource(source));
        servletFactoryDefBuilder.addPropertyReference("embeddedServlet", embeddedServletBeanName);
        //FIXME: it's a dirty hack here, this dependency is added in order to assure that URLHandlerFactory is installed before
        //ServletFactoryBean is used.
        servletFactoryDefBuilder.addPropertyReference("URLHandlerFactoryInstaller", "org.apache.cocoon.servletservice.URLStreamFactoryInstaller");
        servletFactoryDefBuilder.setInitMethodName("init");
        servletFactoryDefBuilder.setDestroyMethodName("destroy");
        servletFactoryDefBuilder.addPropertyValue("serviceName", holder.getBeanName());

        servletFactoryDefBuilder.addPropertyValue("mountPath", mountPath);
        servletFactoryDefBuilder.addPropertyValue("contextPath", contextPath);
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
            Map connectionNames = new HashMap();
            for (Iterator it = connections.keySet().iterator(); it.hasNext();) {
                TypedStringValue key = (TypedStringValue) it.next();
                if (key.getValue().endsWith("+")) {
                    throw new InvalidBeanReferenceNameException(
                                    "The key of a servlet connection mustn't use '+' as its last character. "
                                                    + "This is reserved for absolute references in servlet sources.");
                }
                RuntimeBeanReference beanNameReference = (RuntimeBeanReference) connections.get(key);
                connectionNames.put(key.getValue(), beanNameReference.getBeanName());
            }
            servletFactoryDefBuilder.addPropertyValue("connectionServiceNames", connectionNames);
        }

        return new BeanDefinitionHolder(servletFactoryDefBuilder.getBeanDefinition(), holder.getBeanName());
    }

    private class InvalidBeanReferenceNameException extends RuntimeException {

        public InvalidBeanReferenceNameException(String message) {
            super(message);
        }

    }

}
