/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.cocoon.portal.services.aspects.AspectChain;
import org.apache.cocoon.portal.services.aspects.support.AspectChainImpl;
import org.apache.cocoon.spring.configurator.impl.AbstractElementParser;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 *
 * @version $Id$
 */
public class AspectsBeanDefinitionParser extends AbstractElementParser {

    protected final String baseClass;

    protected final Class aspectChainClass;

    public AspectsBeanDefinitionParser(final String className) {
        this.baseClass = className;
        this.aspectChainClass = AspectChainImpl.class;
    }

    public AspectsBeanDefinitionParser(final String className, Class aspectChainClass) {
        this.baseClass = className;
        this.aspectChainClass = aspectChainClass;
    }

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        // create a new bean definition for the aspect chain
        RootBeanDefinition beanDef = this.createBeanDefinition(this.aspectChainClass, null, false);
        final ManagedList aspectBeans = new ManagedList();
        aspectBeans.setSource(parserContext.getReaderContext().extractSource(element));
        final List aspectProperties = new ArrayList();
        final Element[] aspects = this.getChildElements(element, "aspect");
        for(int i=0; i<aspects.length; i++) {
            final String ref = this.getAttributeValue(aspects[i], "ref", null);
            final String type= this.getAttributeValue(aspects[i], "type", null);
            if ( ref == null && type == null ) {
                throw new BeanDefinitionStoreException("Element 'aspect' must have either a 'ref' or a 'type' attribute.");
            }
            if ( ref != null && type != null ) {
                throw new BeanDefinitionStoreException("Element 'aspect' must have either a 'ref' or a 'type' attribute, but not both.");
            }
            final String beanName = (ref != null ? ref : this.baseClass + '.' + type);
            aspectBeans.add(new RuntimeBeanReference(beanName));
            // properties
            final Properties props = new Properties();
            final Element[] properties = this.getChildElements(aspects[i], "property");
            for(int m=0; m<properties.length;m++) {
                props.setProperty(this.getAttributeValue(properties[m], "name", null),
                                  this.getAttributeValue(properties[m], "value", null));
            }
            if ( props.size() == 0 ) {
                aspectProperties.add(AspectChain.EMPTY_PROPERTIES);
            } else {
                aspectProperties.add(props);
            }
        }
        try {
        	beanDef.getConstructorArgumentValues().addIndexedArgumentValue(0, ClassUtils.forName(this.baseClass, getClass().getClassLoader()));
        } catch (ClassNotFoundException e) {
            throw new BeanDefinitionStoreException("Unable to load aspect class: " + this.baseClass, e);
        }
        beanDef.getConstructorArgumentValues().addIndexedArgumentValue(1, aspectBeans);
        beanDef.getConstructorArgumentValues().addIndexedArgumentValue(2, aspectProperties);

        if ( !parserContext.isNested() ) {
            String id = element.getAttribute(BeanDefinitionParserDelegate.ID_ATTRIBUTE);
            String nameAttr = element.getAttribute(BeanDefinitionParserDelegate.NAME_ATTRIBUTE);

            List aliases = new ArrayList();
            if (StringUtils.hasLength(nameAttr)) {
            	String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
                aliases.addAll(Arrays.asList(nameArr));
            }

            String beanName = id;
            if (!StringUtils.hasText(beanName) && !aliases.isEmpty()) {
                beanName = (String) aliases.remove(0);
                if (logger.isDebugEnabled()) {
                    logger.debug("No XML 'id' specified - using '" + beanName +
                            "' as bean name and " + aliases + " as aliases");
                }
            }
            final BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDef, beanName, StringUtils.toStringArray(aliases));
            BeanDefinitionReaderUtils.registerBeanDefinition(holder, parserContext.getRegistry());
        }
        return beanDef;
    }
}
