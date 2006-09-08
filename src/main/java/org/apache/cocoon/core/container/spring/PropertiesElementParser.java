/*
 * Copyright 2006 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
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
package org.apache.cocoon.core.container.spring;

import org.apache.cocoon.configuration.Settings;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Add a bean definition for the settings object of a child context to the bean factory.
 *
 * @see CocoonNamespaceHandler
 * @see SubSettingsBeanFactoryPostProcessor
 * @version $Id$
 * @since 2.2
 */
public class PropertiesElementParser extends AbstractElementParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition def =  this.createBeanDefinition(SubSettingsBeanFactoryPostProcessor.class.getName(),
                "init",
                false);
        def.getPropertyValues().addPropertyValue("sitemapUri", element.getAttribute("sitemapUri"));
        def.getPropertyValues().addPropertyValue("useDefaultIncludes", element.getAttribute("useDefaultIncludes"));
        this.register(def, Settings.ROLE, parserContext.getRegistry());
        return def;
    }
}
