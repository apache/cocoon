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

import org.apache.cocoon.portal.om.LayoutType;
import org.apache.cocoon.portal.om.Renderer;
import org.apache.cocoon.spring.configurator.impl.AbstractElementParser;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
*
* @version $Id$
*/
public class LayoutTypeDefinitionParser extends AbstractElementParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        // create a new bean definition for the layout type
        RootBeanDefinition beanDef = this.createBeanDefinition(LayoutType.class, null, false);
        final String layoutTypeId = element.getAttribute("id");
        beanDef.getConstructorArgumentValues().addIndexedArgumentValue(0, layoutTypeId);

        beanDef.getPropertyValues().addPropertyValue("layoutClassName", element.getAttribute("layoutClass"));
        if ( this.getAttributeValue(element, "createLayoutId", null) != null ) {
            beanDef.getPropertyValues().addPropertyValue("createId", this.getAttributeValue(element, "createLayoutId", "false"));
        }
        beanDef.getPropertyValues().addPropertyValue("itemClassName", element.getAttribute("itemClass"));
        if ( this.getAttributeValue(element, "defaultIsStatic", null) != null ) {
            beanDef.getPropertyValues().addPropertyValue("defaultIsStatic", this.getAttributeValue(element, "defaultIStatic", "false"));
        }

        final ManagedList rendererBeans = new ManagedList();
        rendererBeans.setSource(parserContext.getReaderContext().extractSource(element));

        final Element[] rendererElements = this.getChildElements(element, "renderer");
        for(int i=0; i<rendererElements.length; i++) {
            final String ref = this.getAttributeValue(rendererElements[i], "ref", null);
            final String type= this.getAttributeValue(rendererElements[i], "type", null);
            if ( ref == null && type == null ) {
                throw new BeanDefinitionStoreException("Element 'renderer' must have either a 'ref' or a 'type' attribute.");
            }
            if ( ref != null && type != null ) {
                throw new BeanDefinitionStoreException("Element 'renderer' must have either a 'ref' or a 'type' attribute, but not both.");
            }
            final String beanName = (ref != null ? ref : Renderer.class.getName() + '.' + type);
            rendererBeans.add(new RuntimeBeanReference(beanName));
        }
        beanDef.getPropertyValues().addPropertyValue("renderers", rendererBeans);

        this.register(beanDef, LayoutType.class.getName() + "." + layoutTypeId, parserContext.getRegistry());
        return null;
    }

}
