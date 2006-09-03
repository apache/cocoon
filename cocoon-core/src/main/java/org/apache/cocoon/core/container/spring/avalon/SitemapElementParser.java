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
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.ResourceLoader;
import org.w3c.dom.Element;

/**
 * @version $Id$
 * @since 2.2
 */
public class SitemapElementParser extends AvalonElementParser {

    /**
     * @see org.apache.cocoon.core.container.spring.avalon.AvalonElementParser#addContext(org.w3c.dom.Element, org.springframework.beans.factory.support.BeanDefinitionRegistry)
     */
    protected void addContext(Element element, BeanDefinitionRegistry registry) {
        // we get the uriPrefix from the configuration
        final String uriPrefix = element.getAttribute("uriPrefix");
        RootBeanDefinition beanDefinition = this.createBeanDefinition(AvalonSitemapContextFactoryBean.class,
                                                                      "init",
                                                                      false);
        beanDefinition.getPropertyValues().addPropertyValue("uriPrefix", uriPrefix);
        this.register(beanDefinition, ProcessingUtil.CONTEXT_ROLE, registry);
    }

    /**
     * @see org.apache.cocoon.core.container.spring.avalon.AvalonElementParser#readConfiguration(java.lang.String, org.springframework.core.io.ResourceLoader)
     */
    protected ConfigurationInfo readConfiguration(String location, ResourceLoader resourceLoader) throws Exception {
        return super.readConfiguration(location, resourceLoader);
    }

}
