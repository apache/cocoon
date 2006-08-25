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

import org.apache.cocoon.Constants;
import org.apache.cocoon.configuration.Settings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Add a bean definition for the settings object to the bean factory.
 * @see CocoonNamespaceHandler
 * @see SettingsBeanFactoryPostProcessor
 * @version $Id$
 * @since 2.2
 */
public class SettingsElementParser implements BeanDefinitionParser {

    /** Logger (we use the same logging mechanism as Spring!) */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        this.logger.info("Initializing Apache Cocoon " + Constants.VERSION);
        this.addComponent(SettingsBeanFactoryPostProcessor.class,
                          Settings.ROLE,
                          "init",
                          parserContext.getRegistry());
        return null;
    }

    protected void addComponent(Class  componentClass,
                                String role,
                                String initMethod,
                                BeanDefinitionRegistry registry) {
        RootBeanDefinition beanDef = new RootBeanDefinition();
        beanDef.setBeanClass(componentClass);      
        beanDef.setSingleton(true);
        if ( initMethod != null ) {
            beanDef.setInitMethodName(initMethod);
        }
        
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDef, role);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }
}
