/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.caching.impl;

import java.io.IOException;

import org.apache.excalibur.store.Store;

/**
 * This implementation of <code>EventRegistry</code> stores its <code>EventRegistryDataWrapper</code>
 * in the default <code>Store</code> injected by Spring.
 * 
 * @since 2.1
 * @version $Id$
 */
public class StoreEventRegistryImpl extends AbstractDoubleMapEventRegistry {

    /**
     * Event registry key.
     */
    private static final String EVENTREGISTRYKEY = "EVENTREGWRAPPER";

    /**
     * Store to use.
     */
    private Store store;

    /**
     * Wraps the registry and stores it.
     * @param wrapper The EventRegistryDataWrapper to store.
     */
    protected void persist(EventRegistryDataWrapper wrapper) {
        EventRegistryDataWrapper ecdw = wrapRegistry();
        try {
            this.store.store(EVENTREGISTRYKEY, ecdw);
        } catch (IOException e) {
            getLogger().warn("Unable to persist Event Registry");
        }
    }

    /**
	 * Recover the datawrapper from the Store.
	 */
    protected boolean recover() {
        Object o = store.get(EVENTREGISTRYKEY);
        this.store.remove(EVENTREGISTRYKEY);
        if (o != null && o instanceof EventRegistryDataWrapper) {
            if (getLogger().isInfoEnabled()) {
                getLogger().info("Retrieving EventRegistry from Store.");
            }
            unwrapRegistry((EventRegistryDataWrapper) o);
            return true;
        } else {
            getLogger().warn("Unable to recover Event Registry.");
            super.createBlankCache();
            return false;
        }
    }

	/**
	 * Sets the Store.
	 * @param store The Store to set.
	 */
	public void setStore(Store store) {
		this.store = store;
	}
}
