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
import org.apache.avalon.util.pool.ThreadSafePool;
import org.apache.avalon.util.pool.ObjectFactory;
import org.apache.avalon.util.pool.PoolController;
import org.apache.cocoon.ComponentFactory;

/**
 * This is a implementation of <code>Pool</code> for SitemapComponents
 * that is thread safe.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 */
public class ComponentPool extends ThreadSafePool {

    public final static int DEFAULT_POOL_SIZE = 8;

    public ComponentPool(final ObjectFactory factory) throws Exception {
        super(factory, DEFAULT_POOL_SIZE/2, DEFAULT_POOL_SIZE);
    }

    public ComponentPool(final ObjectFactory factory,
                         final int initial) throws Exception {
        super(factory, initial, initial);
    }

    public ComponentPool(final ObjectFactory factory,
                         final int initial,
                         final int maximum) throws Exception {
        super(factory, initial, maximum);
    }
}
