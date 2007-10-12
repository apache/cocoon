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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.configuration.MutableSettings;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.spring.configurator.ResourceUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;

/**
 * This is a Spring BeanPostProcessor adding support for the Avalon lifecycle
 * interfaces.
 * 
 * @version $Id: AvalonBeanPostProcessor.java 470890 2006-11-03 16:41:24Z
 *          lgawron $
 * @since 2.2
 */
public class AvalonBeanPostProcessor implements DestructionAwareBeanPostProcessor, BeanFactoryAware {

    protected static final Configuration EMPTY_CONFIG = new DefaultConfiguration("empty");

    protected Logger logger;
    protected Context context;
    protected BeanFactory beanFactory;
    protected ConfigurationInfo configurationInfo;
    protected Settings settings = new MutableSettings("test");
    protected ResourceLoader resourceLoader = new DefaultResourceLoader();

    protected String location = "classpath*:META-INF/cocoon/avalon";

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public void setResourceLoader(final ResourceLoader loader) {
        this.resourceLoader = loader;
    }

    public void setLocation(final String value) {
        this.location = value;
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

    public void init() {
        if (true) {
            return;
        }
        // replace properties in configuration objects
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Processing component configurations.");
            this.logger.debug("Trying to read properties from directory: " + this.location);
        }
        final Properties mergedProps = new Properties();
        final ServletContextResourcePatternResolver resolver = new ServletContextResourcePatternResolver(resourceLoader);
        final Resource dirResource = resourceLoader.getResource(this.location);

        try {
            Resource[] resources = resolver.getResources(this.location + "/*.properties");
            if (resources != null) {
                Arrays.sort(resources, ResourceUtils.getResourceComparator());
                for (int i = 0; i < resources.length; i++) {
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("Reading property file: " + resources[i]);
                    }
                    final Properties p = new Properties();
                    p.load(resources[i].getInputStream());
                    mergedProps.putAll(p);
                }
            }
        } catch (IOException ioe) {
            throw new BeanDefinitionStoreException("Unable to read property configurations from " + this.location, ioe);
        }
        
        if (mergedProps.size() > 0) {
            final Iterator i = this.configurationInfo.getComponents().values().iterator();
            while (i.hasNext()) {
                final ComponentInfo info = (ComponentInfo) i.next();
                if (info.getConfiguration() != null) {
                    final List names = this.getKeys(mergedProps, info.getRole());
                    if (info.getAlias() != null) {
                        names.addAll(this.getKeys(mergedProps, info.getAlias()));
                    }
                    final Iterator namesIter = names.iterator();
                    while (namesIter.hasNext()) {
                        final String name = (String) namesIter.next();
                        final String value = mergedProps.getProperty(name);
                        String propName;
                        if (name.startsWith(info.getRole())) {
                            propName = name.substring(info.getRole().length() + 1);
                        } else {
                            propName = name.substring(info.getAlias().length() + 1);
                        }
                        Configuration config = info.getConfiguration();
                        int pos;
                        do {
                            pos = propName.indexOf('.');
                            if (pos != -1) {
                                config = this.getAndCreateConfiguration(config, propName.substring(0, pos));
                                propName = propName.substring(pos + 1);
                            }
                        } while (pos != -1);
                        if (propName.startsWith("@")) {
                            ((DefaultConfiguration) config).setAttribute(propName.substring(1), value);
                        } else {
                            config = this.getAndCreateConfiguration(config, propName);
                            ((DefaultConfiguration) config).setValue(value);
                        }
                    }
                }
            }
        }
    }

    protected List getKeys(Properties mergedProps, String role) {
        final String prefix = role + '/';
        final List l = new ArrayList();
        final Iterator i = mergedProps.keySet().iterator();
        while (i.hasNext()) {
            final String key = (String) i.next();
            if (key.startsWith(prefix)) {
                l.add(key);
            }
        }
        return l;
    }

    protected Configuration getAndCreateConfiguration(Configuration config, String name) {
        if (config.getChild(name, false) == null) {
            final DefaultConfiguration newConfig = new DefaultConfiguration(name, config.getLocation());
            ((DefaultConfiguration) config).addChild(newConfig);
        }
        return config.getChild(name, false);
    }

    /**
     * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object,
     *      java.lang.String)
     */
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            ContainerUtil.start(bean);
        } catch (Exception e) {
            throw new BeanInitializationException("Unable to start bean " + beanName, e);
        }
        return bean;
    }

    /**
     * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object,
     *      java.lang.String)
     */
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        final ComponentInfo info = (ComponentInfo) this.configurationInfo.getComponents().get(beanName);
        try {
            if (info == null) {
                // no info so we just return the bean and don't apply any
                // lifecycle interfaces
                return bean;
            }
            if (info.getLoggerCategory() != null) {
                ContainerUtil.enableLogging(bean, this.logger.getChildLogger(info.getLoggerCategory()));
            } else {
                ContainerUtil.enableLogging(bean, this.logger);
            }
            ContainerUtil.contextualize(bean, this.context);
            ContainerUtil.service(bean, (ServiceManager) this.beanFactory.getBean(ServiceManager.class.getName()));
            Configuration config = info.getProcessedConfiguration();
            if (config == null) {
                config = info.getConfiguration();
                if (config == null) {
                    config = EMPTY_CONFIG;
                }
                config = AvalonUtils.replaceProperties(config, this.settings);
                info.setProcessedConfiguration(config);
            }
            if (bean instanceof Configurable) {
                ContainerUtil.configure(bean, config);
            } else if (bean instanceof Parameterizable) {
                Parameters p = info.getParameters();
                if (p == null) {
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
     * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor#postProcessBeforeDestruction(java.lang.Object,
     *      java.lang.String)
     */
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        try {
            ContainerUtil.stop(bean);
        } catch (Exception e) {
            throw new BeanInitializationException("Unable to stop bean " + beanName, e);
        }
        ContainerUtil.dispose(bean);
    }
}
