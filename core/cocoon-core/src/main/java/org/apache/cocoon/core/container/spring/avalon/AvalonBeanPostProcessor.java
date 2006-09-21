/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.core.container.spring.avalon;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.configuration.Settings;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

/**
 * This is a Spring BeanPostProcessor adding support for the Avalon lifecycle interfaces.
 *
 * @version $Id$
 * @since 2.2
 */
public class AvalonBeanPostProcessor
    implements DestructionAwareBeanPostProcessor, BeanFactoryAware {

    protected static final Configuration EMPTY_CONFIG = new DefaultConfiguration("empty");

    protected Logger logger;
    protected Context context;
    protected BeanFactory beanFactory;
    protected ConfigurationInfo configurationInfo;
    protected Settings settings;

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    /**
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory factory) throws BeansException {
        this.beanFactory = factory;
    }

    public void setConfigurationInfo(ConfigurationInfo configurationInfo) {
        this.configurationInfo = configurationInfo;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
     */
    public Object postProcessAfterInitialization(Object bean, String beanName)
    throws BeansException {
        try {
            ContainerUtil.start(bean);
        } catch (Exception e) {
            throw new BeanInitializationException("Unable to start bean " + beanName, e);
        }
        return bean;
    }

    /**
     * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
     */
    public Object postProcessBeforeInitialization(Object bean, String beanName)
    throws BeansException {
        final ComponentInfo info = (ComponentInfo)this.configurationInfo.getComponents().get(beanName);
        try {
            if ( info == null ) {
                // no info so we just return the bean and don't apply any lifecycle interfaces
                return bean;
            }
            if ( info.getLoggerCategory() != null ) {
                ContainerUtil.enableLogging(bean, this.logger.getChildLogger(info.getLoggerCategory()));
            } else {
                ContainerUtil.enableLogging(bean, this.logger);
            }
            ContainerUtil.contextualize(bean, this.context);
            ContainerUtil.service(bean, (ServiceManager)this.beanFactory.getBean(ServiceManager.class.getName()));
            Configuration config = info.getProcessedConfiguration();
            if ( config == null ) {
                config = info.getConfiguration();
                if ( config == null ) {
                    config = EMPTY_CONFIG;
                }
                config = AvalonUtils.replaceProperties(config, this.settings);
                info.setProcessedConfiguration(config);
            }
            if ( bean instanceof Configurable ) {
                ContainerUtil.configure(bean, config);
            } else if ( bean instanceof Parameterizable ) {
                Parameters p = info.getParameters();
                if ( p == null ) {
                    p = Parameters.fromConfiguration(config);
                    info.setParameters(p);
                }
                ContainerUtil.parameterize(bean, p);
            }
            ContainerUtil.initialize(bean);
        } catch (Exception e) {
            throw new BeanCreationException("Unable to initialize Avalon component with role " + beanName, e);
        }
        return bean;
    }

    /**
     * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor#postProcessBeforeDestruction(java.lang.Object, java.lang.String)
     */
    public void postProcessBeforeDestruction(Object bean, String beanName)
    throws BeansException {
        try {
            ContainerUtil.stop(bean);
        } catch (Exception e) {
            throw new BeanInitializationException("Unable to stop bean " + beanName, e);
        }
        ContainerUtil.dispose(bean);
    }
}
