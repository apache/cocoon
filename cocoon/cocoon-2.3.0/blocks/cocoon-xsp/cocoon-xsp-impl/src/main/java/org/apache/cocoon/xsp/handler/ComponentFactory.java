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
package org.apache.cocoon.xsp.handler;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.core.container.spring.avalon.ComponentInfo;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * Factory for Avalon based components.
 *
 * @since 2.2
 * @version $Id$
 */
public class ComponentFactory extends AbstractLogEnabled {
    
    protected final ComponentInfo serviceInfo;
    
    protected final ComponentEnvironment environment;
    
    /** The parameters for this component */
    protected final Parameters parameters;
    
    protected final Class serviceClass;

    /**
     * Construct a new component factory for the specified component.
     *
     * @param environment Describes the environment for the component.
     * @param info Describes the configuration/settings for the component.
     *
     */
    public ComponentFactory(final ComponentEnvironment environment,
                            final ComponentInfo info)
    throws Exception {
        this.environment = environment;
        this.serviceInfo = info;

        this.serviceClass = this.environment.loadClass(this.serviceInfo.getComponentClassName());
        if (Parameterizable.class.isAssignableFrom(this.serviceClass)) {
            this.parameters = Parameters.fromConfiguration(this.serviceInfo.getConfiguration());
        } else {
            this.parameters = null;
        }
    }

    /**
     * Create a new instance
     */
    public final Object newInstance()
    throws Exception {
        final Object component = this.serviceClass.newInstance();

        setupInstance(component);
        return component;
    }
    
    /**
     * Invoke the various lifecycle interfaces to setup a newly created component
     * @param component
     * @throws Exception
     */
    protected void setupInstance(Object component) throws Exception {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("ComponentFactory creating new instance of " +
                              this.serviceClass.getName() + ".");
        }

        ContainerUtil.contextualize(component, this.environment.context);
        ContainerUtil.service(component, this.environment.serviceManager);
        ContainerUtil.configure(component, this.serviceInfo.getConfiguration());
        if (component instanceof Parameterizable) {
            ContainerUtil.parameterize(component, this.parameters);
        }

        ContainerUtil.initialize(component);

        ContainerUtil.start(component);
    }

    public Class getCreatedClass() {
        return this.serviceClass;
    }

    /**
     * Destroy an instance
     */
    public void decommission(final Object component)
    throws Exception {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("ComponentFactory decommissioning instance of " +
                              this.serviceClass.getName() + ".");
        }

        ContainerUtil.stop(component);
        ContainerUtil.dispose(component);
    }

    /**
     * Handle service specific methods for putting it into the pool
     */
    public void enteringPool(final Object component)
    throws Exception {
        // Handle Recyclable objects
        if (component instanceof Recyclable) {
            ((Recyclable) component).recycle();
        }
    }
}
