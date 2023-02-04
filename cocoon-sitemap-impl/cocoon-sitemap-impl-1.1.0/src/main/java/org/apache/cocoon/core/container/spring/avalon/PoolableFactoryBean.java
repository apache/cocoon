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
package org.apache.cocoon.core.container.spring.avalon;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.cocoon.configuration.PropertyHelper;
import org.apache.cocoon.configuration.Settings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;

/**
 * This factory bean adds simple pooling support to Spring.
 *
 * @since 2.2
 * @version $Id$
 */
public class PoolableFactoryBean
    implements FactoryBean, BeanFactoryAware {

    /** Logger (we use the same logging mechanism as Spring!) */
    protected final Log log = LogFactory.getLog(getClass());

    /** The default max size of the pool. */
    public static final String DEFAULT_MAX_POOL_SIZE = "64";

    /** All the interfaces for the proxy. */
    protected final Class[] interfaces;

    /** The pooled component. */
    protected final String name;

    /** The class. */
    protected final Class beanClass;

    /** The corresponding bean factory. */
    protected BeanFactory beanFactory;

    /**
     * Object used to synchronize access to the get and put methods
     */
    protected final Object semaphore = new Object();

    /**
     * The maximum size of the pool.
     */
    private final int max;

    /**
     * List of the Poolable instances which are available for use.
     */
    private LinkedList ready;

    /**
     * Store the size of the ready list to optimize operations which require this value.
     */
    private int readySize;

    /**
     * Total number of Poolable instances in the pool
     */
    private int size;

    /**
     * Total number of Poolable instances created
     */
    private int highWaterMark;

    /** Pool-in-method-name. */
    protected String poolInMethodName;

    /** Pool-out-method-name. */
    protected String poolOutMethodName;

    protected Method poolInMethod;
    protected Method poolOutMethod;

    /**
     * Create a PoolableComponentHandler which manages a pool of Components
     * created by the specified factory object.
     *
     * @param name The name of the bean which should be pooled.
     */
    public PoolableFactoryBean( String name, String className )
    throws Exception {
        this(name, className, DEFAULT_MAX_POOL_SIZE, null);
    }

    /**
     * Create a PoolableComponentHandler which manages a pool of Components
     * created by the specified factory object.
     *
     * @param name The name of the bean which should be pooled.
     */
    public PoolableFactoryBean( String name, String className, String poolMaxString, Settings settings )
    throws Exception {
        String value = poolMaxString;
        if ( settings != null ) {
            value = PropertyHelper.replace(poolMaxString, settings);
        }
        int poolMax = Integer.valueOf(value).intValue();
        this.name = name;
        this.max = ( poolMax <= 0 ? Integer.MAX_VALUE : poolMax );
        this.beanClass = Class.forName(className);
        final HashSet workInterfaces = new HashSet();

        // Get *all* interfaces
        this.guessWorkInterfaces( this.beanClass, workInterfaces );
        // Add AvalonPoolable
        workInterfaces.add(AvalonPoolable.class);

        this.interfaces = (Class[]) workInterfaces.toArray( new Class[workInterfaces.size()] );

        // Create the pool lists.
        this.ready = new LinkedList();
    }

    public void setPoolInMethodName(String poolInMethodName) {
        this.poolInMethodName = poolInMethodName;
    }

    public void setPoolOutMethodName(String poolOutMethodName) {
        this.poolOutMethodName = poolOutMethodName;
    }

    public void initialize() throws Exception {
        if ( this.poolInMethodName != null ) {
            this.poolInMethod = this.beanClass.getMethod(this.poolInMethodName, null);
        } else {
            this.poolInMethod = null;
        }
        if ( this.poolOutMethodName != null ) {
            this.poolOutMethod = this.beanClass.getMethod(this.poolOutMethodName, null);
        } else {
            this.poolOutMethod = null;
        }
    }

    /**
     * Dispose of associated Pools and Factories.
     */
    public void dispose() {
        // Any Poolables in the ready list need to be disposed of
        synchronized( this.semaphore ) {
            // Remove objects in the ready list.
            for( Iterator iter = this.ready.iterator(); iter.hasNext(); ) {
                Object poolable = iter.next();
                iter.remove();
                this.readySize--;
                this.permanentlyRemovePoolable( poolable );
            }
        }
    }

    /**
     * Permanently removes a poolable from the pool's active list and
     *  destroys it so that it will not ever be reused.
     * <p>
     * This method is only called by threads that have m_semaphore locked.
     */
    protected void permanentlyRemovePoolable( Object poolable ) {
        this.size--;
    }

    /**
     * Gets a Poolable from the pool.  If there is room in the pool, a new Poolable will be
     *  created.  Depending on the parameters to the constructor, the method may block or throw
     *  an exception if a Poolable is not available on the pool.
     *
     * @return Always returns a Poolable.  Contract requires that put must always be called with
     *  the Poolable returned.
     * @throws Exception An exception may be thrown as described above or if there is an exception
     *  thrown by the ObjectFactory's newInstance() method.
     */
    public Object getFromPool() throws Exception {
        Object poolable;
        synchronized( this.semaphore ) {
            // Look for a Poolable at the end of the m_ready list
            if ( this.readySize > 0 ){
                // A poolable is ready and waiting in the pool
                poolable = this.ready.removeLast();
                this.readySize--;
            } else {
                // Create a new poolable.  May throw an exception if the poolable can not be
                //  instantiated.
                poolable = this.beanFactory.getBean(this.name);
                this.size++;
                this.highWaterMark = (this.highWaterMark < this.size ? this.size : this.highWaterMark);
            }
        }

        this.exitingPool(poolable);

        return poolable;
    }

    /**
     * Returns a poolable to the pool
     *
     * @param poolable Poolable to return to the pool.
     */
    public void putIntoPool( final Object poolable ) {
        this.enteringPool(poolable);

        synchronized( this.semaphore ) {
            if( this.size <= this.max ) {
                this.ready.addLast( poolable );
                this.readySize++;
            } else {
                // More Poolables were created than can be held in the pool, so remove.
                this.permanentlyRemovePoolable( poolable );
            }
        }
    }

    /**
     * Handle service specific methods for getting it out of the pool
     */
    protected void exitingPool( final Object component )
    throws Exception {
        if ( this.poolOutMethod != null ) {
            this.poolOutMethod.invoke(component, null);
        }
    }

    /**
     * Handle service specific methods for putting it into the pool
     */
    protected void enteringPool( final Object component ) {
        try {
            // Handle Recyclable objects
            if( component instanceof Recyclable ) {
                ( (Recyclable)component ).recycle();
            }
            if ( this.poolInMethod != null ) {
                this.poolInMethod.invoke(component, null);
            }
        } catch (Exception ignore) {
            // we ignore exceptions during putting the component back
            // into the pool, as this should not bring the system
            // down - this is for compatibility with the original
            // Avalon implementation
            this.log.error("Exception while putting component '" + component + "' back into the pool.", ignore);
        }
    }

    /**
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),
                                      this.interfaces,
                                      new PoolableProxyHandler(this));
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType() {
        return this.beanClass;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return false;
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
     * @param classInterfaces      the array of interfaces
     * @param workInterfaces  the set of current work interfaces
     */
    private void addInterfaces( final Class[] classInterfaces,
                                final Set workInterfaces ) {
        for ( int i = 0; i < classInterfaces.length; i++ ) {
            workInterfaces.add( classInterfaces[i] );
            this.addInterfaces(classInterfaces[i].getInterfaces(), workInterfaces);
        }
    }
}
