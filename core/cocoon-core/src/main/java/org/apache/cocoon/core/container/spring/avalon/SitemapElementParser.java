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
package org.apache.cocoon.core.container.spring.avalon;

import org.apache.cocoon.ProcessingUtil;
import org.apache.cocoon.core.container.spring.AbstractElementParser;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @version $Id$
 * @since 2.2
 */
public class SitemapElementParser extends AbstractElementParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        // register per sitemap context
        // we get the uriPrefix from the configuration
        final String uriPrefix = element.getAttribute("uriPrefix");
        RootBeanDefinition beanDefinition = this.createBeanDefinition(AvalonSitemapContextFactoryBean.class,
                                                                      "init",
                                                                      false);
        beanDefinition.getPropertyValues().addPropertyValue("uriPrefix", uriPrefix);
        this.register(beanDefinition, ProcessingUtil.CONTEXT_ROLE, parserContext.getRegistry());

        return null;
    }
}
