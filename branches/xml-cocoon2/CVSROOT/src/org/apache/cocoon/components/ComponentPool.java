/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE file.
 */
package org.apache.cocoon.components;

import java.util.List;
import java.util.ArrayList;

import org.apache.avalon.Poolable;
import org.apache.avalon.ThreadSafe;
import org.apache.avalon.Initializable;
import org.apache.avalon.Disposable;
import org.apache.avalon.util.pool.Pool;
import org.apache.avalon.util.pool.ObjectFactory;
import org.apache.avalon.util.Lock;
import org.apache.avalon.util.LockException;
import org.apache.avalon.Recyclable;
import org.apache.cocoon.components.ComponentFactory;
import org.apache.avalon.AbstractLoggable;

/**
 * This is a implementation of <code>Pool</code> for SitemapComponents
 * that is thread safe.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 */
public class ComponentPool extends AbstractLoggable implements Pool, Initializable, Disposable, Runnable, ThreadSafe {

    public final static int DEFAULT_POOL_SIZE = 8;

    /** The resources that are currently free */
    protected List availableResources = new ArrayList();

    /** Resources that have been allocated out of the pool */
    protected List usedResources = new ArrayList();

    private boolean initialized = false;
    private boolean disposed = false;

    private Lock lock = new Lock();
    private Thread initializationThread;

    protected ObjectFactory factory = null;

    protected int initial = DEFAULT_POOL_SIZE/2;

    protected int maximum = DEFAULT_POOL_SIZE;

    public ComponentPool(final ObjectFactory factory) throws Exception {
        init(factory, DEFAULT_POOL_SIZE/2, DEFAULT_POOL_SIZE);
    }

    public ComponentPool(final ObjectFactory factory,
                         final int initial) throws Exception {
        init(factory, initial, initial);
    }

    public ComponentPool(final ObjectFactory factory,
                         final int initial,
                         final int maximum) throws Exception {
        init(factory, initial, maximum);
    }

    private void init(final ObjectFactory factory,
                      final int initial,
                      final int maximum) throws Exception {
        this.factory = factory;
        this.initial = initial;
        this.maximum = maximum;
    }

    public void init() throws Exception {
        this.initializationThread = new Thread(this);
        this.initializationThread.start();
    }

    public void run() {
        this.lock.lock(this.availableResources);

        for( int i = 0; i < this.initial; i++ ) {
            try {
                this.availableResources.add(this.factory.newInstance());
            } catch (Exception e) {
                getLogger().warn("Could not create poolable resource", e);
            }
        }

        if ((this.availableResources.size() < this.initial) && (this.availableResources.size() > 0)) {
            while (this.availableResources.size() < this.initial) {
                try {
                    this.availableResources.add(this.factory.newInstance());
                } catch (Exception e) {
                    getLogger().warn("Could not create poolable resource", e);
                }
            }
        }

        if (this.availableResources.size() > 0) {
            this.initialized = true;
        }

        this.lock.unlock(this.availableResources);
    }

    public void dispose() {
        this.lock.lock(this.availableResources);
        this.disposed = true;

        while ( ! this.availableResources.isEmpty() ) {
            this.availableResources.remove(0);
        }

        this.lock.unlock(this.availableResources);
    }

    /**
     * Allocates a resource when the pool is empty. By default, this method
     * returns null, indicating that the requesting. This
     * allows a thread pool to expand when necessary, allowing for spikes in
     * activity.
     *
     * @return A new resource
     */
    protected Poolable getOverflowResource() throws Exception {
        Poolable poolable = (Poolable) this.factory.newInstance();
        getLogger().debug("Component Pool - creating Overflow Resource:"
                        + " Resource=" + poolable
                        + " Available=" + availableResources.size()
                        + " Used=" + usedResources.size() );
        return poolable;
    }

    /** Requests a resource from the pool.
     * No extra information is associated with the allocated resource.
     * @return The allocated resource
     */
    public Poolable get() throws Exception {
        if (! this.initialized) {
            if (this.initializationThread == null) {
                throw new IllegalStateException("You cannot get a resource before the pool is initialized");
            } else {
                this.initializationThread.join();
            }
        }

        if (this.disposed) {
            throw new IllegalStateException("You cannot get a resource after the pool is disposed");
        }

        this.lock.lock(this.availableResources);
        // See if there is a resource in the pool already
        Poolable resource = null;

        if (this.availableResources.size() > 0) {
            resource = (Poolable)this.availableResources.remove(0);

            this.lock.lock(this.usedResources);
            this.usedResources.add(resource);
            this.lock.unlock(this.usedResources);
        } else {
            resource = this.getOverflowResource();

            if (resource != null) {
                this.lock.lock(this.usedResources);
                this.usedResources.add(resource);
                this.lock.unlock(this.usedResources);
            }
        }

        this.lock.unlock(this.availableResources);

        if (resource == null) {
            throw new RuntimeException("Could not get the component from the pool");
        }

        return resource;
    }

    /** Releases a resource back to the pool of available resources
     * @param resource The resource to be returned to the pool
     */
    public void put(Poolable resource)
    {
        int pos = -1;

        this.lock.lock(this.usedResources);

        // Make sure the resource is in the used list
        pos = usedResources.indexOf(resource);

        if (resource instanceof Recyclable) {
            ((Recyclable)resource).recycle();
        }

        // If the resource was in the used list, remove it from the used list and
        // add it back to the free list
        if (pos >= 0) {
            this.usedResources.remove(pos);

            this.lock.lock(this.availableResources);

            if (this.availableResources.size() < this.maximum) {
                // If the available resources are below the maximum add this back.
                this.availableResources.add(resource);
            } else {
                // If the available are above the maximum destroy this resource.
                try {
                    this.factory.decommission(resource);
                    getLogger().debug("Component Pool - decommissioning Overflow Resource:"
                                    + " Resource=" + resource
                                    + " Available=" + availableResources.size()
                                    + " Used=" + usedResources.size() );
                    resource = null;
                } catch (Exception e) {
                    throw new RuntimeException("caught exception decommissioning resource: " + resource);
                }
            }

            this.lock.unlock(this.availableResources);
        }

        this.lock.unlock(this.usedResources);
    }
}
