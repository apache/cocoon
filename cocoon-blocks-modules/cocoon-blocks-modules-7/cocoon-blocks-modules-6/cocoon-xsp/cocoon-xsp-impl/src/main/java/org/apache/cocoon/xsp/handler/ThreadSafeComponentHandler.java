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
 * The ThreadSafeComponentHandler to make sure components are initialized
 * and destroyed correctly.
 *
 * @since 2.2
 * @version $Id$
 */
public class ThreadSafeComponentHandler extends AbstractFactoryHandler {
    
    private Object instance;

    /**
     * Create a ThreadSafeComponentHandler which manages a single instance
     * of an object return by the component factory.
     *
     * @param factory The factory object which is responsible for creating the components
     *                managed by the handler.
     */
    public ThreadSafeComponentHandler(final ComponentInfo info,
                                      final ComponentFactory factory) {
        super(info, factory);
    }

    public boolean isSingleton() {
        return true;
    }

    public void doInitialize() throws Exception {
        if( this.instance == null ) {
            this.instance = this.factory.newInstance();
        }
    }

    /**
     * Get a reference of the desired Component
     */
    protected Object doGet()
    throws Exception {
        return this.instance;
    }

    /**
     * Return a reference of the desired Component
     */
    protected void doPut( final Object component ) {
        // nothing to do
    }

    /**
     * Dispose of the ComponentHandler and any associated Pools and Factories.
     */
    public void dispose() {
        this.decommission( this.instance );
        this.instance = null;

        super.dispose();
    }
}
