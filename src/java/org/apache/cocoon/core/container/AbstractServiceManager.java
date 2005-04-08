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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ComponentInfo;
import org.apache.cocoon.core.container.handler.AbstractComponentHandler;
import org.apache.cocoon.core.container.handler.ComponentHandler;

/**
 * Base class for all service managers: ServiceManager and ServiceSelector
 *
 * @version CVS $Id$
 */
public abstract class AbstractServiceManager
extends AbstractLogEnabled
implements Contextualizable, ThreadSafe, Disposable, Initializable {
    
    /** The application context for components */
    protected Context context;

    /** Static component mapping handlers. */
    protected final Map componentMapping = Collections.synchronizedMap(new HashMap());

    /** Used to map roles to ComponentHandlers. */
    protected final Map componentHandlers = Collections.synchronizedMap(new HashMap());

    /** Is the Manager disposed or not? */
    protected boolean disposed;

    /** Is the Manager initialized? */
    protected boolean initialized;

    /** RoleInfos. */
    protected RoleManager roleManager;

    /** LoggerManager. */
    protected LoggerManager loggerManager;
    
    protected ComponentEnvironment componentEnv;

    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize( final Context context ) {
        this.context = context;
    }

    public void setRoleManager( final RoleManager roles ) {
        this.roleManager = roles;
    }

    /**
     * Configure the LoggerManager.
     */
    public void setLoggerManager( final LoggerManager manager ) {
        this.loggerManager = manager;
    }
    
    /**
     * Obtain a new ComponentHandler for the specified component. 
     * 
     * @param role the component's role.
     * @param componentClass Class of the component for which the handle is
     *                       being requested.
     * @param configuration The configuration for this component.
     * @param serviceManager The service manager which will be managing the Component.
     *
     * @throws Exception If there were any problems obtaining a ComponentHandler
     */
    protected ComponentHandler getComponentHandler( final String role,
                                                    final Class componentClass,
                                                    final Configuration configuration,
                                                    final ServiceManager serviceManager,
                                                    final ComponentInfo  baseInfo)
    throws Exception {
        if (this.componentEnv == null) {
            this.componentEnv = new ComponentEnvironment(null, getLogger(), this.roleManager,
                    this.loggerManager, this.context, serviceManager);
        }
        // FIXME - we should always get an info here
        ComponentInfo info;
        if ( baseInfo != null ) {
            info = baseInfo.duplicate();
        } else {
            info = new ComponentInfo();
        }
        info.setConfiguration(configuration);
        info.setServiceClassName(componentClass.getName());
        
        return AbstractComponentHandler.getComponentHandler(role,
                                                     this.componentEnv,
                                                     info);
    }

    protected void addComponent(String className,
                                String role,
                                Configuration configuration,
                                ComponentInfo info) 
    throws ConfigurationException {
        // check for old excalibur class names - we only test against the selector
        // implementation
        if ( "org.apache.cocoon.components.ExtendedComponentSelector".equals(className)) {
            className = DefaultServiceSelector.class.getName();
        }
        
        try {
            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( "Adding component (" + role + " = " + className + ")" );
            }
            // FIXME - use different classloader
            final Class clazz = this.getClass().getClassLoader().loadClass( className );
            this.addComponent( role, clazz, configuration, info );
        } catch( final ClassNotFoundException cnfe ) {
            final String message = "Could not get class (" + className + ") for role "
                                 + role + " at " + configuration.getLocation();

            if( this.getLogger().isErrorEnabled() ) {
                this.getLogger().error( message, cnfe );
            }

            throw new ConfigurationException( message, cnfe );
        } catch( final ServiceException ce ) {
            final String message = "Cannot setup class "+ className + " for role " + role
                                 + " at " + configuration.getLocation();

            if( this.getLogger().isErrorEnabled() ) {
                this.getLogger().error( message, ce );
            }

            throw new ConfigurationException( message, ce );
        } catch( final Exception e ) {
            final String message = "Unexpected exception when setting up role " + role + " at " + configuration.getLocation();
            if( this.getLogger().isErrorEnabled() ) {
                this.getLogger().error( message, e );
            }
            throw new ConfigurationException( message, e );
        }        
    }
    
    protected abstract void addComponent(String role, 
                                         Class clazz, 
                                         Configuration config,
                                         ComponentInfo info)
    throws ServiceException;
    
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        this.disposed = true;
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        this.initialized = true;
    }
}
