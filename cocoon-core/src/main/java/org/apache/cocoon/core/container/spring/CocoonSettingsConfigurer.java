/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring;

import java.util.List;
import java.util.Properties;

import org.apache.cocoon.core.Settings;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * Make the properties of the Cocoon settings available within Spring configuration.
 *
 * @since 2.2
 * @version $Id$
 */
public class CocoonSettingsConfigurer
    extends PropertyPlaceholderConfigurer
    implements BeanFactoryPostProcessor {

    protected final Settings settings;

    public CocoonSettingsConfigurer(Settings settings) {
        this.settings = settings;
    }

    /**
     * @see org.springframework.beans.factory.config.PropertyPlaceholderConfigurer#processProperties(org.springframework.beans.factory.config.ConfigurableListableBeanFactory, java.util.Properties)
     */
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
                                     Properties props)
    throws BeansException {
        final BeanDefinitionVisitor visitor = new CocoonSettingsResolvingBeanDefinitionVisitor(this.settings);
        String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
        for (int i = 0; i < beanNames.length; i++) {
            BeanDefinition bd = beanFactoryToProcess.getBeanDefinition(beanNames[i]);
            try {
                visitor.visitBeanDefinition(bd);
            } catch (BeanDefinitionStoreException ex) {
                throw new BeanDefinitionStoreException(bd
                        .getResourceDescription(), beanNames[i], ex
                        .getMessage());
            }
        }
    }

    protected class CocoonSettingsResolvingBeanDefinitionVisitor
        extends BeanDefinitionVisitor {

        protected final Properties props;

        public CocoonSettingsResolvingBeanDefinitionVisitor(Settings settings) {
            this.props = new Properties();

            List propsList = settings.getProperties();
            for (int i = 0; i < propsList.size(); i++) {
                String propName = (String) propsList.get(i);
                props.put(propName, settings.getProperty(propName));
            }
        }

        protected String resolveStringValue(String strVal) {
            return parseStringValue(strVal, this.props, null);
        }
    }

}
