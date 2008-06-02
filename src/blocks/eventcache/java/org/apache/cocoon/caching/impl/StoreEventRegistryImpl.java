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

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.store.Store;

/**
 * This implementation of <code>EventRegistry</code> stores its <code>EventRegistryDataWrapper</code>
 * in the default <code>Store</code> defined in cocoon.xconf.
 * 
 * @since 2.1
 * @author <a href="mailto:ghoward@apache.org">Geoff Howard</a>
 * @version CVS $Id$
 */
public class StoreEventRegistryImpl extends AbstractDoubleMapEventRegistry 
    implements Serviceable, Parameterizable {

    private static final String EVENTREGISTRYKEY = "EVENTREGWRAPPER";
    private ServiceManager m_manager;
    private Store m_store;

    public void parameterize(Parameters parameters) throws ParameterException {
        String storeName = parameters.getParameter("store",Store.ROLE);
        try {
            this.m_store = (Store) m_manager.lookup(storeName);
        } catch (ServiceException e) {
            throw new ParameterException("Unable to lookup store: " + storeName, e);
        }
    }

    protected void persist(EventRegistryDataWrapper wrapper) {
        EventRegistryDataWrapper ecdw = wrapRegistry();
        try {
            m_store.store(EVENTREGISTRYKEY, ecdw);
        } catch (IOException e) {
            getLogger().warn("Unable to persist Event Registry");
        }
        this.m_manager.release(this.m_store);
        m_manager = null;
        m_store = null;
    }

    /**
	 * Obtain a reference to the Store
	 */
    public void service(ServiceManager manager) throws ServiceException {
        this.m_manager = manager;
    }

    /**
	 * Recover the datawrapper from the Store.
	 */
    protected boolean recover() {
        try {
            Object o = m_store.get(EVENTREGISTRYKEY);
            m_store.remove(EVENTREGISTRYKEY);
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
        } catch(Exception e) {
            getLogger().warn("Unable to recover Event Registry.",e);
            super.createBlankCache();
            return false;
        }
    }

}
