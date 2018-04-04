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
package org.apache.cocoon.core.container.spring.avalon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.acting.Action;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.pipeline.impl.PipelineComponentInfo;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.core.container.spring.logger.LoggerFactoryBean;
import org.apache.cocoon.core.container.spring.logger.LoggerUtils;
import org.apache.cocoon.core.container.spring.pipeline.PipelineComponentInfoFactoryBean;
import org.apache.cocoon.core.container.spring.pipeline.PipelineComponentInfoInitializer;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.spring.configurator.impl.AbstractElementParser;
import org.apache.cocoon.transformation.Transformer;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.ResourceLoader;

import org.w3c.dom.Element;

/**
 * This is the main implementation of the Avalon-Spring-bridge.
 * It creates the environment for Avalon components: a logger bean and a context
 * bean, reads the Avalon style configurations and registers the components
 * as beans in the Spring bean definition registry.
 *
 * @since 2.2
 * @version $Id$
 */
public class BridgeElementParser extends AbstractElementParser {

    public static final String DEFAULT_COCOON_XCONF_LOCATION = "resource://org/apache/cocoon/cocoon.xconf";


    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(Element, ParserContext)
     */
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        final ResourceLoader resourceLoader = parserContext.getReaderContext().getReader().getResourceLoader();

        // read avalon style configuration - it's optional for this element.
        // the schema for the sitemap element ensures that location is never null.
        final String location = getAttributeValue(element, "location", DEFAULT_COCOON_XCONF_LOCATION);
        try {
            final ConfigurationInfo info = readConfiguration(location, resourceLoader);

            createComponents(element,
                             info,
                             parserContext.getRegistry(),
                             parserContext.getDelegate().getReaderContext().getReader(),
                             resourceLoader);
        } catch (Exception e) {
            throw new BeanDefinitionStoreException("Unable to read Avalon configuration from '" + location + "'.",e);
        }

        return null;
    }

    /**
     * 
     * @param element        Can be null.
     * @param info           ConfigurationInfo.
     * @param registry       BeanDefinitionRegistry.
     * @param reader         Can be null.
     * @param resourceLoader ResourceLoader.
     * @throws Exception from called components.
     */
    public void createComponents(Element                element,
                                 ConfigurationInfo      info,
                                 BeanDefinitionRegistry registry,
                                 BeanDefinitionReader   reader,
                                 ResourceLoader         resourceLoader)
    throws Exception {
        // add context
        addContext(element, registry);

        // add service manager
        addComponent(AvalonServiceManager.class,
                     AvalonUtils.SERVICE_MANAGER_ROLE,
                     null,
                     false,
                     registry);

        // add logger
        addLogger(registry, info.getRootLogger());

        // handle includes of spring configurations
        final Iterator<String> includeIter = info.getImports().iterator();
        while ( includeIter.hasNext() ) {
            if ( reader == null ) {
                throw new Exception("Import of spring configuration files not supported. (Reader is null)");
            }
            final String uri = includeIter.next();
            reader.loadBeanDefinitions(resourceLoader.getResource(uri));
        }

        // then create components
        this.createConfig(info, registry);

        // register component infos for child factories
        this.registerComponentInfo(info, registry);

        // and finally add avalon bean post processor
        final RootBeanDefinition beanDef = createBeanDefinition(AvalonBeanPostProcessor.class, "init", true);
        beanDef.getPropertyValues().addPropertyValue(
                "context", new RuntimeBeanReference(AvalonUtils.CONTEXT_ROLE));
        beanDef.getPropertyValues().addPropertyValue(
                "configurationInfo", new RuntimeBeanReference(ConfigurationInfo.class.getName()));
        beanDef.getPropertyValues().addPropertyValue(
                "resourceLoader", resourceLoader);
        beanDef.getPropertyValues().addPropertyValue(
                "location", this.getConfigurationLocation());
        this.register(beanDef, AvalonBeanPostProcessor.class.getName(), registry);

        final RootBeanDefinition resolverDef = new RootBeanDefinition();
        resolverDef.setBeanClassName("org.apache.cocoon.components.treeprocessor.variables.PreparedVariableResolver");
        resolverDef.setLazyInit(false);
        resolverDef.setScope("prototype");
        resolverDef.getPropertyValues().addPropertyValue(
                "manager", new RuntimeBeanReference("org.apache.avalon.framework.service.ServiceManager"));
        this.register(
                resolverDef, "org.apache.cocoon.components.treeprocessor.variables.VariableResolver", null, registry);
    }

    protected ConfigurationInfo readConfiguration(String location, ResourceLoader resourceLoader)
    throws Exception {
        return ConfigurationReader.readConfiguration(location, resourceLoader);
    }

    protected void addContext(Element element, BeanDefinitionRegistry registry) {
        this.addComponent(AvalonContextFactoryBean.class,
                AvalonUtils.CONTEXT_ROLE,
                "init",
                true,
                registry);        
    }

    /**
     * Add the logger bean.
     *
     * @param registry       The bean registry.
     * @param loggerCategory The optional category for the logger.
     */
    protected void addLogger(BeanDefinitionRegistry registry,
                             String                 loggerCategory) {
        final RootBeanDefinition beanDef = createBeanDefinition(LoggerFactoryBean.class, "init", false);
        if (loggerCategory != null) {
            beanDef.getPropertyValues().addPropertyValue("category", loggerCategory);
        }

        register(beanDef, LoggerUtils.LOGGER_ROLE, registry);
    }

    public void createConfig(ConfigurationInfo      info,
                             BeanDefinitionRegistry registry) 
    throws Exception {
        final Map<String, ComponentInfo> components = info.getComponents();
        final List<String> pooledRoles = new ArrayList<String>();

        // Iterate over all definitions
        final Iterator<Map.Entry<String, ComponentInfo>> i = components.entrySet().iterator();
        while ( i.hasNext() ) {
            Map.Entry<String, ComponentInfo> entry = i.next();
            final ComponentInfo current = entry.getValue();
            final String role = current.getRole();
    
            String className = current.getComponentClassName();
            boolean isSelector = false;
            boolean singleton = true;
            boolean poolable = false;
            // Test for Selector - we just create a wrapper for them to flatten the hierarchy
            if ( current.isSelector() ) {
                // Add selector
                className = AvalonServiceSelector.class.getName();
                isSelector = true;
            } else {
                // test for unknown model
                if ( current.getModel() == ComponentInfo.MODEL_UNKNOWN ) {
                    try {
                        final Class serviceClass = Class.forName(className);
                        if ( ThreadSafe.class.isAssignableFrom(serviceClass) ) {
                            current.setModel(ComponentInfo.MODEL_SINGLETON);
                        } else if ( Poolable.class.isAssignableFrom(serviceClass) ) {
                            current.setModel(ComponentInfo.MODEL_POOLED);
                        } else {
                            current.setModel(ComponentInfo.MODEL_PRIMITIVE);
                        }
                    } catch (NoClassDefFoundError ncdfe) {
                        throw new ConfigurationException(
                                "Unable to create class for component with role " + current.getRole() 
                                        + " with class: " + className, ncdfe);
                    } catch (ClassNotFoundException cnfe) {
                        throw new ConfigurationException(
                                "Unable to create class for component with role " + current.getRole() 
                                        + " with class: " + className, cnfe);
                    }
                }
                if ( current.getModel() == ComponentInfo.MODEL_POOLED ) {
                    poolable = true;
                    singleton = false;
                } else if ( current.getModel() != ComponentInfo.MODEL_SINGLETON ) {
                    singleton = false;
                }
            }
            final String beanName;
            if ( !poolable ) {
                beanName = role;
            } else {
                beanName = role + "Pooled";                
            }
            final RootBeanDefinition beanDef = new RootBeanDefinition();
            beanDef.setBeanClassName(className);      
            if ( current.getInitMethodName() != null ) {
                beanDef.setInitMethodName(current.getInitMethodName());
            }
            if ( current.getDestroyMethodName() != null ) {
                beanDef.setDestroyMethodName(current.getDestroyMethodName());
            }
            
            beanDef.setScope( singleton ? BeanDefinition.SCOPE_SINGLETON : BeanDefinition.SCOPE_PROTOTYPE );
            
            beanDef.setLazyInit(singleton && current.isLazyInit());
            if ( isSelector ) {
                beanDef.getConstructorArgumentValues().
                        addGenericArgumentValue(role.substring(0, role.length()-8), "java.lang.String");
                if ( current.getDefaultValue() != null ) {
                    beanDef.getPropertyValues().addPropertyValue("default", current.getDefaultValue());
                }
            }
            this.register(beanDef, beanName, current.getAlias(), registry);

            if ( poolable ) {
                // add the factory for poolables
                final RootBeanDefinition poolableBeanDef = new RootBeanDefinition();
                poolableBeanDef.setBeanClass(PoolableFactoryBean.class);
                poolableBeanDef.setScope(BeanDefinition.SCOPE_SINGLETON);
                poolableBeanDef.setLazyInit(false);
                poolableBeanDef.setInitMethodName("initialize");
                poolableBeanDef.setDestroyMethodName("dispose");
                poolableBeanDef.getConstructorArgumentValues().
                        addIndexedArgumentValue(0, beanName, "java.lang.String");
                poolableBeanDef.getConstructorArgumentValues().
                        addIndexedArgumentValue(1, className, "java.lang.String");
                if ( current.getConfiguration() != null ) {
                    // we treat poolMax as a string to allow property replacements
                    final String poolMax = current.getConfiguration().getAttribute("pool-max", null);
                    if ( poolMax != null ) {
                        poolableBeanDef.getConstructorArgumentValues().addIndexedArgumentValue(2, poolMax);
                        poolableBeanDef.getConstructorArgumentValues().addIndexedArgumentValue(3, new RuntimeBeanReference(Settings.ROLE));
                    }
                }
                if ( current.getPoolInMethodName() != null ) {
                    poolableBeanDef.getPropertyValues().
                            addPropertyValue("poolInMethodName", current.getPoolInMethodName());
                }
                if ( current.getPoolOutMethodName() != null ) {
                    poolableBeanDef.getPropertyValues().
                            addPropertyValue("poolOutMethodName", current.getPoolOutMethodName());
                }
                this.register(poolableBeanDef, role, registry);
                pooledRoles.add(role);
            }
        }

        // now change roles for pooled components (from {role} to {role}Pooled
        final Iterator<String> prI = pooledRoles.iterator();
        while ( prI.hasNext() ) {
            final String role = prI.next();
            final ComponentInfo pooledInfo = components.remove(role);
            components.put(role + "Pooled", pooledInfo);
        }
    }

    protected void registerComponentInfo(ConfigurationInfo      configInfo,
                                         BeanDefinitionRegistry registry) {
        PipelineComponentInfo info = new PipelineComponentInfo(null);
        final Iterator i = configInfo.getComponents().values().iterator();
        while (i.hasNext()) {
            final ComponentInfo current = (ComponentInfo) i.next();
            info.componentAdded(current.getRole(), current.getComponentClassName(), current.getConfiguration());
        }
        prepareSelector(info, configInfo, Generator.ROLE);
        prepareSelector(info, configInfo, Transformer.ROLE);
        prepareSelector(info, configInfo, Serializer.ROLE);
        prepareSelector(info, configInfo, ProcessingPipeline.ROLE);
        prepareSelector(info, configInfo, Action.ROLE);
        prepareSelector(info, configInfo, Selector.ROLE);
        prepareSelector(info, configInfo, Matcher.ROLE);
        prepareSelector(info, configInfo, Reader.ROLE);
        info.lock();
        if (!registry.containsBeanDefinition(PipelineComponentInfo.ROLE)) {
            final RootBeanDefinition beanDef = new RootBeanDefinition();
            beanDef.setBeanClass(PipelineComponentInfoFactoryBean.class);
            beanDef.setScope(BeanDefinition.SCOPE_SINGLETON);
            beanDef.setLazyInit(false);
            beanDef.setInitMethodName("init");
            this.register(beanDef, PipelineComponentInfo.ROLE, registry);
        }
        BeanDefinitionBuilder initDefBuilder =
            BeanDefinitionBuilder.rootBeanDefinition(PipelineComponentInfoInitializer.class);
        initDefBuilder.addPropertyReference("info", PipelineComponentInfo.ROLE);
        initDefBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        initDefBuilder.setLazyInit(false);
        initDefBuilder.setInitMethodName("init");
        initDefBuilder.addPropertyValue("data", info.getData());
        final String beanName = this.getClass().getName() + "/init";
        this.register(initDefBuilder.getBeanDefinition(), beanName, registry);

        final RootBeanDefinition ciBeanDef = new RootBeanDefinition();
        ciBeanDef.setBeanClass(ConfigurationInfoFactoryBean.class);
        ciBeanDef.setScope(BeanDefinition.SCOPE_SINGLETON);
        ciBeanDef.setLazyInit(false);
        ciBeanDef.getPropertyValues().addPropertyValue("configurationInfo", configInfo);
        this.register(ciBeanDef, ConfigurationInfo.class.getName(), registry);
    }

    protected static void prepareSelector(PipelineComponentInfo info,
                                          ConfigurationInfo configInfo,
                                          String category) {
        final ComponentInfo component = configInfo.getComponents().get(category + "Selector");
        if (component != null) {
            info.setDefaultType(category, component.getDefaultValue());
        }
    }

    protected String getConfigurationLocation() {
        return "WEB-INF/cocoon/xconf";
    }
}
