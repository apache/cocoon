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
package org.apache.cocoon.core.container.spring;

import org.apache.cocoon.util.ClassUtils;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Spring namespace handler for the cocoon core namespace.
 * Currently this namespace defines the following elements
 * (in the namespace "http://cocoon.apache.org/core"):
 * "settings" : This sets up the Cocoon Settings object (by reading the property
 *              files located under /WEB-INF/cocoon/properties.
 *              By specifying the attribute "processorClassName" an own implementation
 *              can be used (this should be a subclass of the {@link SettingsBeanFactoryPostProcessor}).
 *
 * @version $Id$
 * @since 2.2
 */
public class CocoonNamespaceHandler extends NamespaceHandlerSupport {

    /**
     * @see org.springframework.beans.factory.xml.NamespaceHandler#init()
     */
    public void init() {
        registerBeanDefinitionParser("settings", new SettingsElementParser());
        try {
            registerBeanDefinitionParser("sitemap", (BeanDefinitionParser) ClassUtils.newInstance("org.apache.cocoon.core.container.spring.SitemapElementParser"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
