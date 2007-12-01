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

import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.portal.om.CopletAdapter;
import org.apache.cocoon.portal.om.CopletType;
import org.apache.cocoon.spring.configurator.impl.AbstractElementParser;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * This is the parser for the coplet type element.
 * @version $Id$
 */
public class CopletTypeDefinitionParser extends AbstractElementParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        // create a new bean definition for the coplet type
        RootBeanDefinition beanDef = this.createBeanDefinition(CopletType.class, null, false);
        final String copletTypeId = element.getAttribute("id");
        beanDef.getConstructorArgumentValues().addIndexedArgumentValue(0, copletTypeId);

        // handle coplet adapter element
        final Element copletAdapterElement = this.getChildElements(element, "coplet-adapter")[0];
        final String ref = this.getAttributeValue(copletAdapterElement, "ref", null);
        final String type= this.getAttributeValue(copletAdapterElement, "type", null);
        if ( ref == null && type == null ) {
            throw new BeanDefinitionStoreException("Element 'coplet-adapter' must have either a 'ref' or a 'type' attribute.");
        }
        if ( ref != null && type != null ) {
            throw new BeanDefinitionStoreException("Element 'coplet-adapter' must have either a 'ref' or a 'type' attribute, but not both.");
        }
        final String beanName = (ref != null ? ref : CopletAdapter.class.getName() + '.' + type);
        beanDef.getPropertyValues().addPropertyValue("copletAdapter", new RuntimeBeanReference(beanName));

        // handle configuration
        final Map config = new HashMap();
        final Element[] configElements = this.getChildElements(element, "configuration");
        for(int i=0; i<configElements.length; i++) {
            final String key = this.getAttributeValue(configElements[i], "key", null);
            final String value = this.getAttributeValue(configElements[i], "value", null);
            final String propType= this.getAttributeValue(configElements[i], "type", "string");
            Object propValue = value;
            if ( "boolean".equalsIgnoreCase(propType) ) {
                propValue = Boolean.valueOf(value);
            } else if ( "int".equalsIgnoreCase(propType) ) {
                propValue = Integer.valueOf(value);
            }
            config.put(key, propValue);
        }
        if ( config.size() > 0 ) {
            beanDef.getPropertyValues().addPropertyValue("copletConfig", config);
        }

        this.register(beanDef, CopletType.class.getName() + "." + copletTypeId, parserContext.getRegistry());
        return null;
    }

}
