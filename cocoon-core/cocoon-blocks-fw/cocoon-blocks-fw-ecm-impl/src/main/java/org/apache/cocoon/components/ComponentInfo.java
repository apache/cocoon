/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.components;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.cocoon.core.container.CoreServiceManager;

/**
 * Meta-information about a service
 *  
 * @version $Id$
 */
public final class ComponentInfo {

    public static final int MODEL_UNKNOWN   = -1;
    public static final int MODEL_PRIMITIVE = 0;
    public static final int MODEL_SINGLETON = 1;
    public static final int MODEL_POOLED    = 2;
    public static final int MODEL_NON_THREAD_SAFE_POOLED = 3;

    public static final String TYPE_SINGLETON = "singleton";
    public static final String TYPE_POOLED = "pooled";
    public static final String TYPE_NON_THREAD_SAFE_POOLED = "non-thread-safe-pooled";

    private int model;
    private String initMethodName;
    private String destroyMethodName;
    private String poolInMethodName;
    private String poolOutMethodName;
    private String serviceClassName;
    private Configuration configuration;
    private String loggerCategory;
    private String jmxDomain;
    private String jmxName;
    private String role;

    public ComponentInfo() {
        this.model = MODEL_PRIMITIVE;
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
    public String getServiceClassName() {
        return serviceClassName;
    }

    /**
     * @param serviceClassName The serviceClassName to set.
     */
    public void setServiceClassName(String serviceClassName) {
        this.serviceClassName = serviceClassName;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "ServiceInfo: {class=" + this.getServiceClassName()+"}";
    }

    public void fill(Configuration attr) {
        // test model
        final String model = attr.getAttribute("model", null);
        if ( TYPE_POOLED.equals(model) ) {
            this.setModel(ComponentInfo.MODEL_POOLED);
            this.setPoolInMethodName(attr.getAttribute("pool-in", null));
            this.setPoolOutMethodName(attr.getAttribute("pool-out", null));
        } else if (TYPE_NON_THREAD_SAFE_POOLED.equals(model)) {
            this.setModel(ComponentInfo.MODEL_NON_THREAD_SAFE_POOLED);
            this.setPoolInMethodName(attr.getAttribute("pool-in", null));
            this.setPoolOutMethodName(attr.getAttribute("pool-out", null));
        } else if ( TYPE_SINGLETON.equals(model) ) {
            this.setModel(ComponentInfo.MODEL_SINGLETON);
        }
        // init/destroy methods
        this.setInitMethodName(attr.getAttribute("init", null));
        this.setDestroyMethodName(attr.getAttribute("destroy", null));
        // logging
        this.setLoggerCategory(attr.getAttribute("logger", null));
        this.setJmxDomain(attr.getAttribute(CoreServiceManager.JMX_DOMAIN_ATTR_NAME, null));
        this.setJmxName(attr.getAttribute(CoreServiceManager.JMX_NAME_ATTR_NAME, null));
        this.setRole(attr.getAttribute("role", null));
    }

    public ComponentInfo duplicate() {
        ComponentInfo info = new ComponentInfo();
        info.model = this.model;
        info.initMethodName = this.initMethodName;
        info.destroyMethodName = this.destroyMethodName;
        info.poolInMethodName = this.poolInMethodName;
        info.poolOutMethodName = this.poolOutMethodName;
        info.serviceClassName = this.serviceClassName;
        info.configuration = this.configuration;
        info.loggerCategory = this.loggerCategory;
        info.jmxDomain = this.jmxDomain;
        info.jmxName = this.jmxName;
        info.role = this.role;

        return info;
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
     * @return The JMX domain name
     */
    public String getJmxDomain() {
        if (this.jmxDomain == null && this.configuration != null) {
            this.setJmxDomain(getConfiguration().getAttribute(CoreServiceManager.JMX_DOMAIN_ATTR_NAME, null));
        }
        if(this.jmxDomain == null) {
            return CoreServiceManager.JMX_DEFAULT_DOMAIN_NAME;
        }
        return this.jmxDomain;
    }
    
    /**
     * @param jmxDomain JMX domain name
     */
    public void setJmxDomain(final String jmxDomain) {
        this.jmxDomain = jmxDomain;
    }
    
    /**
     * @return The JMX object name (without domain prefix)
     */
    public String getJmxName() {
        if (this.jmxName == null && this.configuration != null) {
            this.setJmxName(getConfiguration().getAttribute(CoreServiceManager.JMX_NAME_ATTR_NAME, null));
        }
        return this.jmxName;
    }
    
    /**
     * @param jmxName JMX object name (without domain prefix)
     */
    public void setJmxName(final String jmxName) {
        this.jmxName = jmxName;
    }

    /**
     * @param role The role to set.
     */
    public void setRole( String role )
    {
        this.role = role;
    }

    /**
     * @return Returns the role.
     */
    public String getRole()
    {
        return role;
    }
}
