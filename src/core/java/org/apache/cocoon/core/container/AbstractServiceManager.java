/* 
 * Copyright 2002-2004 The Apache Software Foundation
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

/**
 * Base class for all service managers: ServiceManager and ServiceSelector
 *
 * @version CVS $Id: AbstractServiceManager.java 55144 2004-10-20 12:26:09Z ugo $
 */
public abstract class AbstractServiceManager
extends AbstractLogEnabled
implements Contextualizable, ThreadSafe, Disposable, Initializable {
    
    /** The classloader used for this system. */
    protected final ClassLoader loader;

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

    
    /** 
     * Create the ServiceManager with a Classloader
     */
    public AbstractServiceManager( final ClassLoader loader ) {
        if( null == loader ) {
            this.loader = Thread.currentThread().getContextClassLoader();
        } else {
            this.loader = loader;
        }
    }

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
     * @param componentClass Class of the component for which the handle is
     *                       being requested.
     * @param configuration The configuration for this component.
     * @param serviceManager The service manager which will be managing the Component.
     *
     * @throws Exception If there were any problems obtaining a ComponentHandler
     */
    protected AbstractComponentHandler getComponentHandler( final Class componentClass,
                                                    final Configuration configuration,
                                                    final ServiceManager serviceManager)
    throws Exception {
        return AbstractComponentHandler.getComponentHandler(componentClass,
                                                     configuration,
                                                     serviceManager,
                                                     this.context,
                                                     this.getLogger(),
                                                     this.loggerManager,
                                                     this.roleManager);
    }

    protected void addComponent(String className,
                                Object role,
                                Configuration configuration) 
    throws ConfigurationException {
        // check for old excalibur class names - we only test against the selector
        // implementation
        if ( "org.apache.cocoon.components.ExtendedComponentSelector".equals(className)) {
            className = CocoonServiceSelector.class.getName();
        }
        
        try {
            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( "Adding component (" + role + " = " + className + ")" );
            }

            final Class clazz = this.loader.loadClass( className );
            this.addComponent( role, clazz, configuration );
        } catch( final ClassNotFoundException cnfe ) {
            final String message = "Could not get class (" + className + ") for role "
                                 + role + " on configuration element " + configuration.getName();

            if( this.getLogger().isErrorEnabled() ) {
                this.getLogger().error( message, cnfe );
            }

            throw new ConfigurationException( message, cnfe );
        } catch( final ServiceException ce ) {
            final String message = "Bad component "+ className + " for role " + role
                                 + " on configuration element " + configuration.getName();

            if( this.getLogger().isErrorEnabled() ) {
                this.getLogger().error( message, ce );
            }

            throw new ConfigurationException( message, ce );
        } catch( final Exception e ) {
            final String message = "Unexpected exception for role: " + role;
            if( this.getLogger().isErrorEnabled() ) {
                this.getLogger().error( "Unexpected exception for role: " + role, e );
            }
            throw new ConfigurationException( message, e );
        }        
    }
    
    protected abstract void addComponent(Object role, Class clazz, Configuration config)
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
