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

import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.thread.SingleThreaded;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.core.container.spring.avalon.ComponentInfo;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * This class acts like a Factory to instantiate the correct version
 * of the component handler that you need.
 *
 * @since 2.2
 * @version $Id$
 */
public abstract class AbstractComponentHandler extends AbstractLogEnabled
                                               implements ComponentHandler {
    
    private final Object referenceSemaphore = new Object();
    private int references;

    /** State management boolean stating whether the Handler is disposed or not */
    protected boolean disposed;

    /** State management boolean stating whether the Handler is initialized or not */
    private boolean initialized;
    
    /** Information about the component */
    private ComponentInfo info;
    
    /**
     * Looks up and returns a component handler for a given component class.
     *
     * @param role the component's role. Can be <code>null</code> if the role isn't known.
     * @param componentEnv The component's creation environment.
     * @param info          The description of the component (configuration, lifecycle etc.)
     *
     * @throws Exception If there were any problems obtaining a ComponentHandler
     */
    public static ComponentHandler getComponentHandler(String role, 
                                                       ComponentEnvironment componentEnv,
                                                       ComponentInfo info) 
    throws Exception {
        
       // Load the class
        Class componentClass;
        
        try {
            componentClass = componentEnv.loadClass(info.getComponentClassName());
        } catch (ClassNotFoundException cnfe) {
            throw new Exception("Cannot find class " + info.getComponentClassName() + " for component at " +
                    info.getConfiguration().getLocation(), cnfe);
        }

        int numInterfaces = 0;

        // Early check for Composable
        if ( Composable.class.isAssignableFrom( componentClass ) ) {
            throw new Exception("Interface Composable is not supported anymore. Please change class "
                                + componentClass.getName() + " to use Serviceable instead.");
        }

        if (SingleThreaded.class.isAssignableFrom(componentClass)) {
            numInterfaces++;
            info.setModel(ComponentInfo.MODEL_PRIMITIVE);
        }

        if (ThreadSafe.class.isAssignableFrom(componentClass)) {
            numInterfaces++;
            info.setModel(ComponentInfo.MODEL_SINGLETON);
        }

        if (Poolable.class.isAssignableFrom(componentClass)) {
            numInterfaces++;
        }

        if( numInterfaces > 1 ) {
            throw new Exception( "[CONFLICT] More than one lifecycle interface in "
                                 + componentClass.getName() + "  May implement no more than one of "
                                 + "SingleThreaded, ThreadSafe, or Poolable" );
        }

        if ( numInterfaces == 0 ) {
            // this component does not use avalon interfaces, so get the info from the configuration
            info.fill(info.getConfiguration());
        }
        info.setRole(role);
        
        // Create the factory to use to create the instances of the Component.
        ComponentFactory factory;
        ComponentHandler handler;
                
        factory = new ComponentFactory(componentEnv, info);

        if (info.getModel() == ComponentInfo.MODEL_POOLED) {
            handler = new NonThreadSafePoolableComponentHandler(info, factory, info.getConfiguration());
        } else if (info.getModel() == ComponentInfo.MODEL_SINGLETON) {
            handler = new ThreadSafeComponentHandler(info, factory);
        } else {
            // This is a SingleThreaded component
            handler = new SingleThreadedComponentHandler(info, factory);
        }

        return handler;
    }

    /**
     * Creates a new ComponentHandler.
     */
    public AbstractComponentHandler(ComponentInfo info) {
        this.info = info;
    }
    
    public ComponentInfo getInfo() {
        return this.info;
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
        initialize();
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
        if (!this.initialized) {
            throw new IllegalStateException(
                    "You cannot put a component to an uninitialized handler.");
        }

        //  The reference count must be decremented before any calls to doPut.
        //  If there is another thread blocking, then this thread could stay deep inside
        //  doPut for an undetermined amount of time until the thread scheduler gives it
        //  some cycles again.  (It happened).  All ComponentHandler state must therefor
        //  reflect the thread having left this method before the call to doPut to avoid
        //  warning messages from the dispose() cycle if that takes place before this
        //  thread has a chance to continue.
        synchronized (this.referenceSemaphore) {
            this.references--;
        }

        try {
            this.doPut(component);
        } catch (Throwable t) {
            getLogger().error("Exception during putting back a component.", t);
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
     * Default here is to return <code>false</code>
     */
    public boolean isSingleton() {
        return false;
    }
    
    /**
     * Returns <code>true</code> if this component handler can safely be
     * disposed (i.e. none of the components it is handling are still
     * being used).
     *
     * @return <code>true</code> if this component handler can safely be
     *         disposed; <code>false</code> otherwise
     */
    public final boolean canBeDisposed() {
        return this.references == 0;
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
    public final void initialize() throws Exception {
        if (this.initialized) {
            return;
        }

        doInitialize();
        this.initialized = true;
    }

    protected abstract void doInitialize() throws Exception;

    /**
     * Create a component handler (version used by XSP)
     * TODO - perhaps we can remove this later?
     */
    public static ComponentHandler getComponentHandler(Class clazz, Context context, ServiceManager manager, Configuration config) throws Exception {
        ComponentEnvironment env = new ComponentEnvironment(context, manager, clazz.getClassLoader());
        ComponentInfo info = new ComponentInfo();
        info.setComponentClassName(clazz.getName());
        info.setConfiguration(config);
        info.setRole("XSP");
        return getComponentHandler(null, env, info);

    }
}
