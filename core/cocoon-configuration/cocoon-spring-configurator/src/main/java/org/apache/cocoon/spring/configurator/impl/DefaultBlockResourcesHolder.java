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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.spring.configurator.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.spring.configurator.BlockResourcesHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * Default implementation of a {@link BlockResourcesHolder}.
 *
 * @version $Id$
 * @since 1.0
 */
public class DefaultBlockResourcesHolder
    extends PropertyPlaceholderConfigurer
    implements BlockResourcesHolder, BeanFactoryPostProcessor {

    /** The settings object. */
    protected Settings settings;

    /** extractBlockResources. */
    protected boolean extractBlockResources = true;

    protected Map blockContexts = new HashMap();

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public void setExtractBlockResources(boolean extractBlockResources) {
        this.extractBlockResources = extractBlockResources;
    }

    /**
     * Initialize this component.
     * @throws Exception
     */
    public void init()
    throws Exception {
        if ( this.extractBlockResources ) {
            this.blockContexts = DeploymentUtil.deployBlockArtifacts(this.settings.getWorkDirectory());
        } else {
            this.blockContexts = Collections.EMPTY_MAP;
        }
    }

    /**
     * Return a map with deployed block names as keys and the url of the deployed
     * resources as value.
     */
    public Map getBlockContexts() {
        return this.blockContexts;
    }

    /**
     * @see org.springframework.beans.factory.config.PropertyPlaceholderConfigurer#processProperties(org.springframework.beans.factory.config.ConfigurableListableBeanFactory, java.util.Properties)
     */
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
                                     Properties props)
    throws BeansException {
        final BeanDefinitionVisitor visitor = new ResolvingBeanDefinitionVisitor(this.blockContexts);
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

    protected class ResolvingBeanDefinitionVisitor
    extends BeanDefinitionVisitor {

        protected final Properties props;
        protected final Set visitedPlaceholders = new HashSet();

        public ResolvingBeanDefinitionVisitor(Map blockContexts) {
            this.props = new Properties();
            final Iterator i = blockContexts.entrySet().iterator();
            while ( i.hasNext() ) {
                final Map.Entry current = (Map.Entry)i.next();
                final String key = "org.apache.cocoon.blocks." + current.getKey() + ".resources";
                this.props.put(key, current.getValue().toString());
            }
        }

        protected String resolveStringValue(String strVal) {
            return parseStringValue(strVal, this.props, visitedPlaceholders);
        }
    }
}
