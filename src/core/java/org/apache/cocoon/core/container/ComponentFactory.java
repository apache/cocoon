/* 
 * Copyright 2002-2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
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
package org.apache.cocoon.core.container;

import java.lang.reflect.Method;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.ComponentInfo;

/**
 * Factory for Avalon based components.
 *
 * @version CVS $Id$
 */
public class ComponentFactory {
    
    protected final ComponentInfo serviceInfo;
    
    protected final ComponentEnvironment environment;
    
    /**
     * The component's logger, which may be different from the environment's logger
     */
    protected final Logger componentLogger;
    
    /** The parameters for this component
     */
    protected Parameters parameters;
    
    protected final Class serviceClass;
    protected final Method initMethod;
    protected final Method destroyMethod;
    protected final Method poolInMethod;
    protected final Method poolOutMethod;

    /**
     * Construct a new component factory for the specified component.
     *
     * @param componentClass the class to instantiate (must have a default constructor).
     * @param configuration the <code>Configuration</code> object to pass to new instances.
     * @param seerviceManager the service manager to pass to <code>Serviceable</code>s.
     * @param context the <code>Context</code> to pass to <code>Contexutalizable</code>s.
     *
     */
    public ComponentFactory( final ComponentEnvironment environment,
                             final ComponentInfo info) 
    throws Exception {
        this.environment = environment;
        this.serviceInfo = info;
        
        Logger actualLogger = this.environment.logger;
        // If the handler is created "manually" (e.g. XSP engine), loggerManager can be null
        if( this.environment.loggerManager != null && this.serviceInfo.getConfiguration() != null) {
            final String category = this.serviceInfo.getConfiguration().getAttribute("logger", null);
            if (category != null) {
                actualLogger = this.environment.loggerManager.getLoggerForCategory(category);
            }
        }
        this.componentLogger = actualLogger;
        
        this.serviceClass = this.environment.loadClass(this.serviceInfo.getServiceClassName());
        if ( this.serviceInfo.getDestroyMethodName() != null ) {
            this.destroyMethod = this.serviceClass.getMethod(this.serviceInfo.getDestroyMethodName(), null);
        } else {
            this.destroyMethod = null;
        }
        if ( this.serviceInfo.getInitMethodName() != null ) {
            this.initMethod = this.serviceClass.getMethod(this.serviceInfo.getInitMethodName(), null);
        } else {
            this.initMethod = null;
        }
        if ( this.serviceInfo.getPoolInMethodName() != null ) {
            this.poolInMethod = this.serviceClass.getMethod(this.serviceInfo.getPoolInMethodName(), null);
        } else {
            this.poolInMethod = null;
        }
        if ( this.serviceInfo.getPoolOutMethodName() != null ) {
            this.poolOutMethod = this.serviceClass.getMethod(this.serviceInfo.getPoolOutMethodName(), null);
        } else {
            this.poolOutMethod = null;
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
        if( this.environment.logger.isDebugEnabled() ) {
            this.environment.logger.debug( "ComponentFactory creating new instance of " +
                    this.serviceClass.getName() + "." );
        }

        ContainerUtil.enableLogging(component, this.componentLogger);
        ContainerUtil.contextualize( component, this.environment.context );
        ContainerUtil.service( component, this.environment.serviceManager );
        ContainerUtil.configure( component, this.serviceInfo.getConfiguration() );

        if( component instanceof Parameterizable ) {
            if ( this.parameters == null ) {
                this.parameters = Parameters.fromConfiguration( this.serviceInfo.getConfiguration() );
            }
            ContainerUtil.parameterize( component, this.parameters );
        }

        ContainerUtil.initialize( component );

        if ( this.initMethod != null ) {
            this.initMethod.invoke(component, null);
        }

        ContainerUtil.start( component );
    }

    public Class getCreatedClass() {
        return this.serviceClass;
    }

    /**
     * Destroy an instance
     */
    public void decommission( final Object component )
    throws Exception {
        if( this.environment.logger.isDebugEnabled() ) {
            this.environment.logger.debug( "ComponentFactory decommissioning instance of " +
                    this.serviceClass.getName() + "." );
        }

        ContainerUtil.stop( component );
        ContainerUtil.dispose( component );

        if ( this.destroyMethod != null ) {
            this.destroyMethod.invoke(component, null);
        }
    }

    /**
     * Handle service specific methods for getting it out of the pool
     */
    public void exitingPool( final Object component )
    throws Exception {
        if ( this.poolOutMethod != null ) {
            this.poolOutMethod.invoke(component, null);
        }         
    }

    /**
     * Handle service specific methods for putting it into the pool
     */
    public void enteringPool( final Object component )
    throws Exception {
        // Handle Recyclable objects
        if( component instanceof Recyclable ) {
            ( (Recyclable)component ).recycle();
        }
        if ( this.poolInMethod != null ) {
            this.poolInMethod.invoke(component, null);
        }         
    }
}
