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
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Add a bean definition for the settings object to the bean factory.
 *
 * @see CocoonNamespaceHandler
 * @see SettingsBeanFactoryPostProcessor
 * @version $Id$
 * @since 2.2
 */
public class SettingsElementParser implements BeanDefinitionParser {

    /** Logger (we use the same logging mechanism as Spring!) */
    protected final Log logger = LogFactory.getLog(getClass());

    /** The name of the configuration attribute to use a different processor class. */
    public static final String PROCESSOR_CLASS_NAME_ATTR = "processorClassName";

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        this.logger.info("Initializing Apache Cocoon " + Constants.VERSION);
        String componentClassName = SettingsBeanFactoryPostProcessor.class.getName();
        String value = element.getAttribute(PROCESSOR_CLASS_NAME_ATTR);
        if ( value != null && value.trim().length() > 0 ) {
            componentClassName = element.getAttribute(PROCESSOR_CLASS_NAME_ATTR);
        }
        final RootBeanDefinition beanDef = new RootBeanDefinition();
        beanDef.setBeanClassName(componentClassName);      
        beanDef.setSingleton(true);
        beanDef.setLazyInit(false);
        beanDef.setInitMethodName("init");
        
        final BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDef, Settings.ROLE);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, parserContext.getRegistry());

        return null;
    }
}
