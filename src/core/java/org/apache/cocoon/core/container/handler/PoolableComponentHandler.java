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
package org.apache.cocoon.core.container.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.components.ComponentInfo;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.container.ComponentFactory;

/**
 * The PoolableComponentHandler to make sure that poolable components are initialized
 * destroyed and pooled correctly.
 * <p>
 * Components which implement Poolable may be configured to be pooled using the following
 *  example configuration.  This example assumes that the user component class MyComp
 *  implements Poolable.
 * <p>
 * Configuration Example:
 * <pre>
 *   &lt;my-comp pool-max="8"/&gt;
 * </pre>
 * <p>
 * Roles Example:
 * <pre>
 *   &lt;role name="com.mypkg.MyComponent"
 *         shorthand="my-comp"
 *         default-class="com.mypkg.DefaultMyComponent"/&gt;
 * </pre>
 * <p>
 * Configuration Attributes:
 * <ul>
 * <li>The <code>pool-max</code> attribute is used to set the maximum number of components which
 *  will be pooled. (Defaults to "8") If additional instances are required, they're created,
 *  but not pooled.</li>
 * </ul>
 *
 * @version $Id$
 */
public class PoolableComponentHandler
extends NonThreadSafePoolableComponentHandler {
    
    /** All the interfaces for the proxy */
    protected final Class[] interfaces;
    
    /**
     * Create a PoolableComponentHandler which manages a pool of Components
     *  created by the specified factory object.
     *
     * @param factory The factory object which is responsible for creating the components
     *                managed by the ComponentHandler.
     * @param config The configuration to use to configure the pool.
     */
    public PoolableComponentHandler( final ComponentInfo info,
                                     final Logger logger,
                                     final ComponentFactory factory,
                                     final Configuration config )
    throws Exception {
        super(info, logger, factory, config);
        this.interfaces = this.guessWorkInterfaces(factory.getCreatedClass());
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.handler.AbstractComponentHandler#doGet()
     */
    protected Object doGet() throws Exception {
        return this.createProxy();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.core.container.handler.AbstractComponentHandler#doPut(java.lang.Object)
     */
    protected void doPut(Object component) throws Exception {
        // nothing to do
    }

    protected void doInitialize() {
        // nothing to do here
    }
    
    protected Object createProxy() {
        return Proxy.newProxyInstance(this.factory.getCreatedClass().getClassLoader(), 
                                      this.interfaces, 
                                      new ProxyHandler(this));
    }

    /**
     * Get a list of interfaces to proxy by scanning through
     * all interfaces a class implements and skipping invalid interfaces
     * (as defined in {@link #INVALID_INTERFACES}).
     *
     * @param clazz the class
     * @return the list of interfaces to proxy
     */
    protected Class[] guessWorkInterfaces( final Class clazz ) {
        final HashSet workInterfaces = new HashSet();

        // Get *all* interfaces
        this.guessWorkInterfaces( clazz, workInterfaces );

        return (Class[]) workInterfaces.toArray( new Class[workInterfaces.size()] );
    }

    /**
     * Get a list of interfaces to proxy by scanning through
     * all interfaces a class implements.
     *
     * @param clazz           the class
     * @param workInterfaces  the set of current work interfaces
     */
    private void guessWorkInterfaces( final Class clazz,
                                      final Set workInterfaces ) {
        if ( null != clazz ) {
            this.addInterfaces( clazz.getInterfaces(), workInterfaces );

            this.guessWorkInterfaces( clazz.getSuperclass(), workInterfaces );
        }
    }

    /**
     * Get a list of interfaces to proxy by scanning through
     * all interfaces a class implements.
     *
     * @param interfaces      the array of interfaces
     * @param workInterfaces  the set of current work interfaces
     */
    private void addInterfaces( final Class[] interfaces,
                                final Set workInterfaces ) {
        for ( int i = 0; i < interfaces.length; i++ ) {
            workInterfaces.add( interfaces[i] );
            this.addInterfaces(interfaces[i].getInterfaces(), workInterfaces);
        }
    }

    protected static final class ProxyHandler implements InvocationHandler, Core.CleanupTask {
        
        private ThreadLocal componentHolder = new InheritableThreadLocal();
        private final PoolableComponentHandler handler;
        
        public ProxyHandler(PoolableComponentHandler handler) {
            this.handler = handler;
        }
        
        /* (non-Javadoc)
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
            if ( method.getName().equals("hashCode") && args == null ) {
                return new Integer(this.hashCode());
            }
            if ( this.componentHolder.get() == null ) {
                this.componentHolder.set(this.handler.getFromPool());
                Core.addCleanupTask(this);
            }
            try {
                return method.invoke(this.componentHolder.get(), args);
            } catch (InvocationTargetException ite) {
                throw ite.getTargetException();
            }
        }
        
        
        /* (non-Javadoc)
         * @see org.apache.cocoon.core.Core.CleanupTask#invoke()
         */
        public void invoke() {
            try {
                final Object o = this.componentHolder.get();
                /*if ( o == null ) {
                    System.out.println("Releasing null for " + this.handler.factory.getCreatedClass());
                } else {
                    System.out.println("Releasing: " + o);
                }*/
                this.handler.putIntoPool(o);
            } catch (Exception ignore) {
                // we ignore this
            }
            this.componentHolder.set(null);
        }
    }
    
    
}
