/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.store.Store;

/**
 * This implementation of <code>EventRegistry</code> stores its <code>EventRegistryDataWrapper</code>
 * in the <code>PersistentStore</code> defined in cocoon.xconf.
 * 
 * @since 2.1
 * @author <a href="mailto:ghoward@apache.org">Geoff Howard</a>
 * @version CVS $Id: StoreEventRegistryImpl.java,v 1.7 2004/03/05 13:01:56 bdelacretaz Exp $
 */
public class StoreEventRegistryImpl
    extends AbstractDoubleMapEventRegistry
    implements Serviceable {
    private static final String EVENTREGISTRYKEY = "EVENTREGWRAPPER";
    private ServiceManager m_manager;
    private Store m_persistentStore;

    protected void persist(EventRegistryDataWrapper wrapper) {
        EventRegistryDataWrapper ecdw = wrapRegistry();
        try {
            m_persistentStore.store(EVENTREGISTRYKEY, ecdw);
        } catch (IOException e) {
            getLogger().warn("Unable to persist Event Registry");
        }
        this.m_manager.release(this.m_persistentStore);
        m_manager = null;
        m_persistentStore = null;
    }

    /**
	 * Obtain a reference to the Store
	 */
    public void service(ServiceManager manager) throws ServiceException {
        this.m_manager = manager;
        this.m_persistentStore = (Store) manager.lookup(Store.PERSISTENT_STORE);
    }

    /**
	 * Recover the datawrapper from the Store.
	 */
    protected boolean recover() {
        Object o = m_persistentStore.get(EVENTREGISTRYKEY);
        m_persistentStore.remove(EVENTREGISTRYKEY);
        if (o != null && o instanceof EventRegistryDataWrapper) {
            if (getLogger().isInfoEnabled()) {
                getLogger().info(
                    "Retrieving EventRegistry from PersistentStore.");
            }
            unwrapRegistry((EventRegistryDataWrapper) o);
            return true;
        } else {
            getLogger().warn("Unable to recover Event Registry.");
            super.createBlankCache();
            return false;
        }
    }

}
