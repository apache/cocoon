/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE file.
 */
package org.apache.cocoon.util;

import org.apache.avalon.Poolable;
import org.apache.avalon.ThreadSafe;
import org.apache.avalon.Loggable;
import org.apache.avalon.util.pool.AbstractPool;
import org.apache.avalon.util.pool.ObjectFactory;
import org.apache.avalon.util.pool.PoolController;

import org.apache.log.Logger;

/**
 * This is a implementation of <code>Pool</code> for SitemapComponents
 * that is thread safe.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 */
public class ComponentPool extends AbstractPool implements ThreadSafe, Loggable {

    public final static int DEFAULT_POOL_SIZE = 16;

    private Logger log;

    public ComponentPool(final ObjectFactory factory,
                         final PoolController controller) throws Exception {
        super(factory, controller, DEFAULT_POOL_SIZE/2, DEFAULT_POOL_SIZE);
    }

    public ComponentPool(final ObjectFactory factory,
                         final PoolController controller,
                         final int initial) throws Exception {
        super(factory, controller, initial, initial);
    }

    public ComponentPool(final ObjectFactory factory,
                         final PoolController controller,
                         final int initial,
                         final int maximum) throws Exception {
        super(factory, controller, initial, maximum);
    }

    public void setLogger(Logger log) {
        if (this.log == null) {
            this.log = log;
        }
    }

    /**
     * Retrieve an object from pool.
     *
     * @return an object from Pool
     */
    public final Poolable get() throws Exception {
        synchronized(m_pool) {
            Poolable component = super.get();
            log.debug(
                "ComponentPool retrieved "
                + component.getClass().getName()
                + " (" + component.toString() + ")"
            );
            return component;
        }
    }

    /**
     * Place an object in pool.
     *
     * @param poolable the object to be placed in pool
     */
    public final void put(final Poolable poolable) {
        synchronized(m_pool) {
            super.put(poolable);
            log.debug(
                "ComponentPool returned "
                + poolable.getClass().getName()
                + " (" + poolable.toString() + ")"
            );
        }
    }
}
