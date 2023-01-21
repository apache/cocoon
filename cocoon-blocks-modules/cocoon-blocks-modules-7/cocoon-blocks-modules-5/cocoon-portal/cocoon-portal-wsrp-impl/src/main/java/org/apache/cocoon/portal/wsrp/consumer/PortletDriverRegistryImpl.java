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
package org.apache.cocoon.portal.wsrp.consumer;

import java.util.Hashtable;
import java.util.Iterator;

import org.apache.cocoon.portal.wsrp.adapter.WSRPAdapter;
import org.apache.wsrp4j.consumer.ConsumerEnvironment;
import org.apache.wsrp4j.consumer.PortletDriver;
import org.apache.wsrp4j.consumer.PortletDriverRegistry;
import org.apache.wsrp4j.consumer.WSRPPortlet;
import org.apache.wsrp4j.exception.WSRPException;

/**
 * Manages the drivers for the portlets the consumerEnvironment holds<br/>
 * Per portlet one portletDriver will be stored<br/>
 *
 * @version $Id$
 */
public class PortletDriverRegistryImpl
    implements PortletDriverRegistry,
               RequiresConsumerEnvironment,
               RequiresWSRPAdapter {

    /** All portletDrivers the consumerEnvironment needs. */
    protected final Hashtable portletDrivers = new Hashtable();

    /** The consumer environment. */
    protected ConsumerEnvironment consumerEnv;

    /** The WSRP adapter. */
    protected WSRPAdapter adapter;

    /**
     * @see org.apache.cocoon.portal.wsrp.consumer.RequiresConsumerEnvironment#setConsumerEnvironment(org.apache.wsrp4j.consumer.ConsumerEnvironment)
     */
    public void setConsumerEnvironment(ConsumerEnvironment env) {
        this.consumerEnv = env;
    }

    /**
     * @see org.apache.cocoon.portal.wsrp.consumer.RequiresWSRPAdapter#setWSRPAdapter(org.apache.cocoon.portal.wsrp.adapter.WSRPAdapter)
     */
    public void setWSRPAdapter(WSRPAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Get an portlet driver for the given portlet. If there is no portlet driver
     * object cached a new portlet driver will be created and returned.
     *
     * @param portlet The portlet the returned portlet driver is bound to.
     *
     * @return The portlet driver for this portlet.
     **/
    public PortletDriver getPortletDriver(WSRPPortlet portlet)
    throws WSRPException {
        PortletDriver driver = null;

        if ((driver = (PortletDriver)portletDrivers.get(portlet.getPortletKey().toString())) == null) {
            String driverClass = this.adapter.getAdapterConfiguration().getProperty("portlet-driver-class", PortletDriverImpl.class.getName());
            try {
                driver = (PortletDriverImpl)this.adapter.createObject(driverClass);
            } catch (Exception e) {
                throw new WSRPException(0, e);
            }
            ((PortletDriverImpl)driver).init(portlet);
            this.portletDrivers.put(portlet.getPortletKey().toString(), driver);
        }
        return driver;
    }

    /**
     * Get all cached portlet drivers.
     *
     * @return Iterator with all portlet drivers in the registry.
     **/
    public Iterator getAllPortletDrivers() {
        return portletDrivers.values().iterator();
    }
}
