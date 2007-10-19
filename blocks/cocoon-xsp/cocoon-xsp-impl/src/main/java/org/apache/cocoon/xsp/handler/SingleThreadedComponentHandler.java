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

import org.apache.cocoon.core.container.spring.avalon.ComponentInfo;

/**
 * The DefaultComponentHandler to make sure components are initialized
 * and destroyed correctly.
 *
 * @since 2.2
 * @version $Id$
 */
public class SingleThreadedComponentHandler
extends AbstractFactoryHandler {

    private long maxCreated;
    private long maxDecommissioned;


    /**
     * Create a SingleThreadedComponentHandler which manages a pool of Components
     * created by the specified factory object.
     *
     * @param factory The factory object which is responsible for creating the components
     *                managed by the handler.
     */
    public SingleThreadedComponentHandler(final ComponentInfo info,
                                          final ComponentFactory factory) {
        super(info, factory);
    }

    /**
     * Get a reference of the desired Component
     *
     * @return A component instance.
     *
     * @throws Exception If there are any problems encountered acquiring a
     *                   component instance.
     */
    protected Object doGet()
            throws Exception {
        maxCreated++;
        return this.factory.newInstance();
    }

    /**
     * Return a reference of the desired Component
     *
     * @param component Component to be be put/released back to the handler.
     */
    protected void doPut(final Object component) {
        this.decommission(component);
        maxDecommissioned++;
    }

    protected void doInitialize() {
        // nothing to do here
    }

    /**
     * @return Returns the maxCreated.
     */
    public long getMaxCreated() {
        return maxCreated;
    }

    /**
     * @return Returns the maxDecommisioned.
     */
    public long getMaxDecommissioned() {
        return maxDecommissioned;
    }
}
