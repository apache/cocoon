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

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.thread.SingleThreaded;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ServiceInfo;

/**
 * This class acts like a Factory to instantiate the correct version
 * of the component handler that you need.
 *
 * @version CVS $Id: AbstractComponentHandler.java 55144 2004-10-20 12:26:09Z ugo $
 */
public abstract class AbstractComponentHandler 
implements ComponentHandler {
    
    private final Object referenceSemaphore = new Object();
    private int references = 0;

    protected final Logger logger;
    
    /** This factory is used to created new objects */
    protected final ComponentFactory factory;
    
    /** State management boolean stating whether the Handler is disposed or not */
    protected boolean disposed = false;

    /** State management boolean stating whether the Handler is initialized or not */
    protected boolean initialized = false;

    /**
     * Looks up and returns a component handler for a given component class.
     *
     * @param componentClass Class of the component for which the handle is
     *                       being requested.
     * @param configuration The configuration for this component.
     * @param serviceManager The service manager which will be managing the service.
     * @param context The current context object.
     * @param logger  The current logger
     * @param loggerManager The current LoggerManager.
     *
     * @throws Exception If there were any problems obtaining a ComponentHandler
     */
    public static ComponentHandler getComponentHandler( final Class componentClass,
                                                        final Configuration configuration,
                                                        final ServiceManager serviceManager,
                                                        final Context context,
                                                        final Logger logger,
                                                        final LoggerManager loggerManager,
                                                        final RoleManager roleManager)
    throws Exception {
        int numInterfaces = 0;

        final ServiceInfo info = new ServiceInfo();
        info.setServiceClass(componentClass);
        info.setServiceClassName(componentClass.getName());
        info.setConfiguration(configuration);
        
        // Early check for Composable
        if ( Composable.class.isAssignableFrom( componentClass ) ) {
            throw new Exception("Interface Composable is not supported anymore. Please change class "
                                + componentClass.getName() + " to use Serviceable instead.");
        }

        if( SingleThreaded.class.isAssignableFrom( componentClass ) ) {
            numInterfaces++;
            info.setModel(ServiceInfo.MODEL_PRIMITIVE);
        }

        if( ThreadSafe.class.isAssignableFrom( componentClass ) ) {
            numInterfaces++;
            info.setModel(ServiceInfo.MODEL_SINGLETON);
        }

        if( Poolable.class.isAssignableFrom( componentClass ) ) {
            numInterfaces++;
            info.setModel(ServiceInfo.MODEL_POOLED);
        }

        if( numInterfaces > 1 ) {
            throw new Exception( "[CONFLICT] More than one lifecycle interface in "
                                 + componentClass.getName() + "  May implement no more than one of "
                                 + "SingleThreaded, ThreadSafe, or Poolable" );
        }

        if ( numInterfaces == 0 ) {
            // this component does not use avalon interfaces, so get the info from the configuration
            info.fill(configuration);
        }
        
        // Create the factory to use to create the instances of the Component.
        ComponentFactory factory = new ComponentFactory( 
                                         serviceManager,
                                         context,
                                         logger,
                                         loggerManager,
                                         roleManager,
                                         info);

        AbstractComponentHandler handler;
        
        if( info.getModel() == ServiceInfo.MODEL_POOLED )  {
            handler = new PoolableComponentHandler( logger, factory, configuration );
        } else if( info.getModel() == ServiceInfo.MODEL_SINGLETON ) {
            handler = new ThreadSafeComponentHandler( logger, factory );
        } else {
            // This is a SingleThreaded component
            handler = new SingleThreadedComponentHandler( logger, factory );
        }

        return handler;
    }

    /**
     * Creates a new ComponentHandler.
     */
    public AbstractComponentHandler(Logger logger, ComponentFactory factory) {
        this.logger = logger;
        this.factory = factory;
    }

    /**
     * Get an instance of the type of component handled by this handler.
     * <p>
     * Subclasses should not extend this method but rather the doGet method below otherwise
     *  reference counts will not be supported.
     * <p>
     *
     * @return an instance
     * @exception Exception if an error occurs
     */
    public final Object get() throws Exception {
        if( !this.initialized ) {
            throw new IllegalStateException(
                "You cannot get a component from an uninitialized handler." );
        }
        if( this.disposed ) {
            throw new IllegalStateException( "You cannot get a component from a disposed handler." );
        }

        final Object component = this.doGet();

        synchronized( this.referenceSemaphore ) {
            this.references++;
        }

        return component;
    }

    /**
     * Put back an instance of the type of component handled by this handler.
     * <p>
     * Subclasses should not extend this method but rather the doPut method below otherwise
     *  reference counts will not be supported.
     * <p>
     *
     * @param component a service
     * @exception Exception if an error occurs
     */
    public final void put( Object component ) 
    throws Exception {
        if( !this.initialized ) {
            throw new IllegalStateException(
                "You cannot put a component to an uninitialized handler." );
        }
        //  The reference count must be decremented before any calls to doPut.
        //  If there is another thread blocking, then this thread could stay deep inside
        //  doPut for an undetermined amount of time until the thread scheduler gives it
        //  some cycles again.  (It happened).  All ComponentHandler state must therefor
        //  reflect the thread having left this method before the call to doPut to avoid
        //  warning messages from the dispose() cycle if that takes place before this
        //  thread has a chance to continue.
        synchronized( this.referenceSemaphore ) {
            this.references--;
        }

        try {
            this.doPut( component );
        } catch( Throwable t ) {
            this.logger.error("Exception during putting back a component.", t);
        }
    }

    /**
     * Concrete implementation of getting a component.
     *
     * @return a service
     * @exception Exception if an error occurs
     */
    protected abstract Object doGet() throws Exception;

    /**
     * Concrete implementation of putting back a component.
     *
     * @param component a <code>Component</code> value
     * @exception Exception if an error occurs
     */
    protected abstract void doPut( Object component ) throws Exception;

    /**
     * Returns <code>true</code> if this component handler can safely be
     * disposed (i.e. none of the components it is handling are still
     * being used).
     *
     * @return <code>true</code> if this component handler can safely be
     *         disposed; <code>false</code> otherwise
     */
    public final boolean canBeDisposed() {
        return ( this.references == 0 );
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.ComponentHandler#dispose()
     */
    public void dispose() {
        this.disposed = true;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.ComponentHandler#initialize()
     */
    public void initialize() throws Exception {
        if( this.initialized ) {
            return;
        }
        this.initialized = true;
        if( this.logger.isDebugEnabled() ) {
            this.logger.debug( "ThreadSafeComponentHandler initialized for: " + this.factory.getCreatedClass().getName() );
        }
    }
    
    /**
     * Decommission a component
     * @param component Object to be decommissioned
     */
    protected void decommission( final Object component ) {
        try {
            this.factory.decommission( component );
        } catch( final Exception e ) {
            if( this.logger.isWarnEnabled() ) {
                this.logger.warn( "Error decommissioning component: "
                    + this.factory.getCreatedClass().getName(), e );
            }
        }
    }
    
}
