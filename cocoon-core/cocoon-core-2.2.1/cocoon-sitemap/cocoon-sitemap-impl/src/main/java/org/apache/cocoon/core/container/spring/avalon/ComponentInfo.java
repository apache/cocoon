/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring.avalon;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.parameters.Parameters;

/**
 * Meta-information about an Avalon based component.
 * This simple bean holds most information of a component defined in the
 * Avalon based configuration files, like the configuration for the component,
 * it's logger etc.
 *
 * Avalon supports different component models:
 * MODEL_PRIMITIVE: Each time a component of this type is requested, a new instance is
 *                  created.
 * MODEL_SINGLETON: Only on component of this type exists per container.
 * MODEL_POOLED:    The container creates a pool of components for this type and serves
 *                  a request out of this pool. If the pool exceeds, then the pool will
 *                  create new instances which are not put into the pool, so the
 *                  model "primitive" will be used.
 *
 * @since 2.2
 * @version $Id$
 */
public final class ComponentInfo {

    /** The model of the component is unknown. Reflection is used later on to determine the model. */
    public static final int MODEL_UNKNOWN   = -1;
    /** New instance per lookup. */
    public static final int MODEL_PRIMITIVE = 0;
    /** One single instance per container. */
    public static final int MODEL_SINGLETON = 1;
    /** Several instances are pooled by the container. */
    public static final int MODEL_POOLED    = 2;

    /** One single instance per container. */
    private static final String TYPE_SINGLETON = "singleton";
    /** Several instances are pooled by the container. */
    private static final String TYPE_POOLED = "pooled";
    /** Several instances are pooled by the container. */
    private static final String TYPE_NON_THREAD_SAFE_POOLED = "non-thread-safe-pooled";

    /** The model for this component. */
    private int model;

    /** An optional method which is invoked by the container on initialization. */
    private String initMethodName;

    /** An optional method which is invoked by the container on destruction. */
    private String destroyMethodName;

    /** An optional method which is invoked by the container when the component is put back into the pool. */
    private String poolInMethodName;

    /** An optional method which is invoked by the container when the component is fetched from the pool. */
    private String poolOutMethodName;

    /** The class name of the component. */
    private String componentClassName;

    /** The configuration of the component. */
    private Configuration configuration;

    /** Processed configuration. */
    private Configuration processedConfiguration;

    /** The configuration of the component as parameters. */
    private Parameters parameters;

    /** The optional logger category (relative to the category of the container). */
    private String loggerCategory;

    /** The role of the component (= bean name in Spring). */
    private String role;

    /** An alias for the component role. */    
    private String alias;

    /** The default component for selectors. */
    private String defaultValue;

    /** Lazy init. */
    private boolean lazyInit = false;

    /**
     * Create a new info.
     */
    public ComponentInfo() {
        this.model = MODEL_UNKNOWN;
    }

    /**
     * @return Returns the model.
     */
    public int getModel() {
        return model;
    }

    /**
     * @param model The model to set.
     */
    public void setModel(int model) {
        this.model = model;
    }

    /**
     * @return Returns the destroyMethod.
     */
    public String getDestroyMethodName() {
        return destroyMethodName;
    }

    /**
     * @param destroyMethod The destroyMethod to set.
     */
    public void setDestroyMethodName(String destroyMethod) {
        this.destroyMethodName = destroyMethod;
    }

    /**
     * @return Returns the initMethod.
     */
    public String getInitMethodName() {
        return initMethodName;
    }

    /**
     * @param initMethod The initMethod to set.
     */
    public void setInitMethodName(String initMethod) {
        this.initMethodName = initMethod;
    }

    /**
     * @return Returns the poolInMethodName
     */
    public String getPoolInMethodName() {
        return this.poolInMethodName;
    }

    /**
     * @param poolMethod The poolInMethod name to set.
     */
    public void setPoolInMethodName(String poolMethod) {
        this.poolInMethodName = poolMethod;
    }

    /**
     * @return Returns the poolOutMethodName
     */
    public String getPoolOutMethodName() {
        return this.poolOutMethodName;
    }

    /**
     * @param poolMethod The poolOutMethod name to set.
     */
    public void setPoolOutMethodName(String poolMethod) {
        this.poolOutMethodName = poolMethod;
    }

    /**
     * @return Returns the serviceClassName.
     */
    public String getComponentClassName() {
        return componentClassName;
    }

    /**
     * @param serviceClassName The serviceClassName to set.
     */
    public void setComponentClassName(String serviceClassName) {
        this.componentClassName = serviceClassName;
    }

    /**
     * @return Returns the configuration.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * @param configuration The configuration to set.
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public String getLocation() {
        return this.configuration.getLocation();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "ServiceInfo: {class=" + this.getComponentClassName()+"}";
    }

    public void fill(Configuration config) {
        // test model
        final String componentModel = config.getAttribute("model", null);
        if ( TYPE_POOLED.equals(componentModel) ) {
            this.setModel(ComponentInfo.MODEL_POOLED);
            this.setPoolInMethodName(config.getAttribute("pool-in", null));
            this.setPoolOutMethodName(config.getAttribute("pool-out", null));
        } else if (TYPE_NON_THREAD_SAFE_POOLED.equals(componentModel)) {
            this.setModel(ComponentInfo.MODEL_POOLED);
            this.setPoolInMethodName(config.getAttribute("pool-in", null));
            this.setPoolOutMethodName(config.getAttribute("pool-out", null));
        } else if ( TYPE_SINGLETON.equals(componentModel) ) {
            this.setModel(ComponentInfo.MODEL_SINGLETON);
        }
        // init/destroy methods
        this.setInitMethodName(config.getAttribute("init", null));
        this.setDestroyMethodName(config.getAttribute("destroy", null));
        // logging
        this.setLoggerCategory(config.getAttribute("logger", null));
        this.setRole(config.getAttribute("role", null));
        // default value
        final String newDefaultValue = config.getAttribute("default", null);
        if ( newDefaultValue != null ) {
            this.defaultValue = newDefaultValue;
        }
        this.lazyInit = config.getAttributeAsBoolean("lazy-init", this.lazyInit);
    }

    /**
     * @return Returns the loggerCategory.
     */
    public String getLoggerCategory() {
        return this.loggerCategory;
    }

    /**
     * @param loggerCategory The loggerCategory to set.
     */
    public void setLoggerCategory(String loggerCategory) {
        this.loggerCategory = loggerCategory;
    }
    
    /**
     * @param role The role to set.
     */
    public void setRole( String role ) {
        this.role = role;
    }

    /**
     * @return Returns the role.
     */
    public String getRole() {
        return role;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Is this a selector?
     */
    public boolean isSelector() {
        return this.componentClassName.equals("org.apache.cocoon.core.container.DefaultServiceSelector") ||
               this.componentClassName.equals("org.apache.cocoon.components.treeprocessor.sitemap.ComponentsSelector");
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Create a new component info with the same configuration
     * as the current one.
     * @return An identical component info.
     */
    public ComponentInfo copy() {
        final ComponentInfo info = new ComponentInfo();
        info.model = this.model;
        info.initMethodName = this.initMethodName;
        info.destroyMethodName = this.destroyMethodName;
        info.poolInMethodName = this.poolInMethodName;
        info.poolOutMethodName = this.poolOutMethodName;
        info.componentClassName = this.componentClassName;
        info.configuration = this.configuration;
        info.parameters = this.parameters;
        info.loggerCategory = this.loggerCategory;
        info.role = this.role;
        info.alias = this.alias;
        info.defaultValue = this.defaultValue;
        info.lazyInit = this.lazyInit;
        return info;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    public Configuration getProcessedConfiguration() {
        return processedConfiguration;
    }

    public void setProcessedConfiguration(Configuration processedConfiguration) {
        this.processedConfiguration = processedConfiguration;
    }

    public boolean hasConfiguredLazyInit() {
        return this.configuration != null && this.configuration.getAttribute("lazy-init", null) != null;
    }
}
