/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import java.lang.reflect.Method;

import org.apache.avalon.framework.configuration.Configuration;

/**
 * Meta-information about a service
 *  
 * @version CVS $Id$
 */
public class ServiceInfo {

    public static final int MODEL_PRIMITIVE = 0;
    public static final int MODEL_SINGLETON = 1;
    public static final int MODEL_POOLED    = 2;
    
    private int model;
    private String initMethodName;
    private String destroyMethodName;
    private String poolInMethodName;
    private String poolOutMethodName;
    private Class serviceClass;
    private String serviceClassName;
    private Method initMethod;
    private Method destroyMethod;
    private Method poolInMethod;
    private Method poolOutMethod;
    private Configuration configuration;
    
    public ServiceInfo() {
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
     * @return Returns the destroyMethod.
     */
    public Method getDestroyMethod() throws Exception {
        if ( this.destroyMethod == null && this.destroyMethodName != null ) {
            this.destroyMethod = this.serviceClass.getMethod(this.destroyMethodName, null);
        }
        return destroyMethod;
    }

    /**
     * @return Returns the initMethod.
     */
    public Method getInitMethod() throws Exception {
        if ( this.initMethod == null && this.initMethodName != null ) {
            this.initMethod = this.serviceClass.getMethod(this.initMethodName, null);
        }
        return initMethod;
    }

    /**
     * @return Returns the poolInMethod.
     */
    public Method getPoolInMethod() throws Exception {
        if ( this.poolInMethod == null && this.poolInMethodName != null ) {
            this.poolInMethod = this.serviceClass.getMethod(this.poolInMethodName, null);
        }
        return poolInMethod;
    }

    /**
     * @return Returns the poolInMethod.
     */
    public Method getPoolOutMethod() throws Exception {
        if ( this.poolOutMethod == null && this.poolOutMethodName != null ) {
            this.poolOutMethod = this.serviceClass.getMethod(this.poolOutMethodName, null);
        }
        return poolOutMethod;
    }

    /**
     * @return Returns the serviceClass.
     */
    public Class getServiceClass() {
        return serviceClass;
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
     * @param serviceClass The serviceClass to set.
     */
    public void setServiceClass(Class serviceClass) {
        this.serviceClass = serviceClass;
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
        if ( "pooled".equals(model) ) {
            this.setModel(ServiceInfo.MODEL_POOLED);
            this.setPoolInMethodName(attr.getAttribute("pool-in", null));
            this.setPoolOutMethodName(attr.getAttribute("pool-out", null));
        } else if ( "singleton".equals(model) ) {
            this.setModel(ServiceInfo.MODEL_SINGLETON);
        }
        this.setInitMethodName(attr.getAttribute("init", null));
        this.setDestroyMethodName(attr.getAttribute("destroy", null));
    }
}
