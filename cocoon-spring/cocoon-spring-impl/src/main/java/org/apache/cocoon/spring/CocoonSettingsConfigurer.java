/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.spring;

import java.util.List;
import java.util.Properties;

import org.apache.cocoon.core.Core;
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
 * @version $Id:$
 */
public class CocoonSettingsConfigurer extends PropertyPlaceholderConfigurer
        implements BeanFactoryPostProcessor {

    private Core core;

    public CocoonSettingsConfigurer(Core core) {
        this.core = core;
    }

    protected void processProperties(
            ConfigurableListableBeanFactory beanFactoryToProcess,
            Properties props) throws BeansException {

        BeanDefinitionVisitor visitor = new CocoonSettingsResolvingBeanDefinitionVisitor(
                this.core);
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

    class CocoonSettingsResolvingBeanDefinitionVisitor extends
            BeanDefinitionVisitor {

        private Properties props;

        public CocoonSettingsResolvingBeanDefinitionVisitor(Core core) {
            props = new Properties();

            Settings settings = core.getSettings();
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
