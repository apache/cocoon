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

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.cocoon.core.container.spring.avalon.ComponentInfo;

/**
 * The PoolableComponentHandler to make sure that poolable components are initialized
 * destroyed and pooled correctly.
 *
 * <p>Components which implement Poolable may be configured to be pooled using the
 * following example configuration.  This example assumes that the user component
 * class MyComp implements Poolable.
 *
 * <p>Configuration Example:
 * <pre>
 *   &lt;my-comp pool-max="8"/&gt;
 * </pre>
 *
 * <p>Roles Example:
 * <pre>
 *   &lt;role name="com.mypkg.MyComponent"
 *         shorthand="my-comp"
 *         default-class="com.mypkg.DefaultMyComponent"/&gt;
 * </pre>
 *
 * <p>Configuration Attributes:
 * <ul>
 * <li>The <code>pool-max</code> attribute is used to set the maximum number of components which
 * will be pooled. (Defaults to "8") If additional instances are required, they're created,
 * but not pooled.</li>
 * </ul>
 *
 * @since 2.2
 * @version $Id$
 */
public class NonThreadSafePoolableComponentHandler extends AbstractFactoryHandler {
    
    /** The default max size of the pool */
    public static final int DEFAULT_MAX_POOL_SIZE = 8;

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
    
    /**
     * Create a PoolableComponentHandler which manages a pool of Components
     *  created by the specified factory object.
     *
     * @param factory The factory object which is responsible for creating the components
     *                managed by the ComponentHandler.
     * @param config The configuration to use to configure the pool.
     */
    public NonThreadSafePoolableComponentHandler(final ComponentInfo info,
                                                 final ComponentFactory factory,
                                                 final Configuration config )
    throws Exception {
        super(info, factory);

        final int poolMax = config.getAttributeAsInteger( "pool-max", DEFAULT_MAX_POOL_SIZE );
        this.max = ( poolMax <= 0 ? Integer.MAX_VALUE : poolMax );

        // Create the pool lists.
        this.ready = new LinkedList();
    }

    /**
     * Dispose of the ComponentHandler and any associated Pools and Factories.
     */
    public void dispose() {
        super.dispose();

        // Any Poolables in the m_ready list need to be disposed of
        synchronized (this.semaphore) {
            // Remove objects in the ready list.
            for (Iterator iter = this.ready.iterator(); iter.hasNext();) {
                Object poolable = iter.next();
                iter.remove();
                this.readySize--;
                this.permanentlyRemovePoolable(poolable);
            }

            if (this.size > 0 && getLogger().isDebugEnabled()) {
                getLogger().debug("There were " + this.size +
                                  " outstanding objects when the pool was disposed.");
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
        this.decommission( poolable );
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
    protected Object getFromPool() throws Exception {
        Object poolable;
        synchronized (this.semaphore) {
            // Look for a Poolable at the end of the m_ready list
            if (this.readySize > 0) {
                // A poolable is ready and waiting in the pool
                poolable = this.ready.removeLast();
                this.readySize--;
            } else {
                // Create a new poolable.  May throw an exception if the poolable can not be
                //  instantiated.
                poolable = this.factory.newInstance();
                this.size++;
                this.highWaterMark = (this.highWaterMark < this.size ? this.size : this.highWaterMark);

                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Created a new " + poolable.getClass().getName() +
                                      " from the object factory.");
                }
            }
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Got a " + poolable.getClass().getName() + " from the pool.");
        }

        return poolable;
    }

    /**
     * Returns a poolable to the pool 
     *
     * @param poolable Poolable to return to the pool.
     */
    protected void putIntoPool(final Object poolable) {
        try {
            this.factory.enteringPool(poolable);
        } catch (Exception ignore) {
            getLogger().warn("Exception during putting component back into the pool.", ignore);
        }

        synchronized (this.semaphore) {
            if (this.size <= this.max) {
                if (this.disposed) {
                    // The pool has already been disposed.
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Put called for a " + poolable.getClass().getName() +
                                          " after the pool was disposed.");
                    }

                    this.permanentlyRemovePoolable(poolable);
                } else {
                    // There is room in the pool to keep this poolable.
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Put a " + poolable.getClass().getName() +
                                          " back into the pool.");
                    }

                    this.ready.addLast(poolable);
                    this.readySize++;

                }
            } else {
                // More Poolables were created than can be held in the pool, so remove.
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("No room to put a " + poolable.getClass().getName() +
                                      " back into the pool, so remove it.");
                }

                permanentlyRemovePoolable(poolable);
            }
        }
    }
    
    protected void doInitialize() {
        // nothing to do here
    }
    
    
    protected Object doGet() throws Exception {
        return getFromPool();
    }

    protected void doPut(Object component) throws Exception {
        this.putIntoPool(component);
    }

    /**
     * @return Returns the max.
     */
    public int getMax()
    {
        return max;
    }

    /**
     * @return Returns the readySize.
     */
    public int getReadySize()
    {
        return readySize;
    }

    /**
     * @return Returns the size.
     */
    public int getSize()
    {
        return size;
    }

    /**
     * @return Returns the highWaterMark.
     */
    public int getHighWaterMark()
    {
        return highWaterMark;
    }
}
