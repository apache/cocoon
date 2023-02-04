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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsrp4j.consumer.PortletKey;
import org.apache.wsrp4j.consumer.WSRPPortlet;
import org.apache.wsrp4j.consumer.driver.ConsumerPortletContext;
import org.apache.wsrp4j.consumer.driver.GenericPortletRegistryImpl;
import org.apache.wsrp4j.exception.WSRPException;
import org.apache.wsrp4j.util.StateChangedEvent;
import org.apache.wsrp4j.util.StateChangedListener;
import org.apache.wsrp4j.util.StateChangedService;

/**
 * This class is the <code>PortletRegistry</code> implementation used
 * to administer, store, load and manage portlets.<br/>
 *  
 * @version $Id$
 */
public class PortletRegistryImpl extends GenericPortletRegistryImpl
                                 implements StateChangedListener {

    /** The logger. */
    protected final Log logger = LogFactory.getLog(getClass());

    /** maps portlet keys to portlet context. */
    private Hashtable contextMap = new Hashtable();


    /**
     * Add a portlet to the registry<br/>
     *
     * @param portlet The portlet to add
     * @throws WSRPException on error
     */
    public void addPortlet(WSRPPortlet portlet) throws WSRPException {
        if (portlet != null) {
            super.addPortlet(portlet);

            // store PortletContext to persistent file
            ConsumerPortletContext consumerPortletContext = new ConsumerPortletContext();
            consumerPortletContext.setPortletContext(portlet.getPortletContext());
            consumerPortletContext.setPortletKey(portlet.getPortletKey());
            this.contextMap.put(portlet.getPortletKey().toString(), consumerPortletContext);

            // add as listener
            if (portlet instanceof StateChangedService) {
                ((StateChangedService)portlet).addListener(this);
            }
        }
    }

    /**
     * Remove the portlet with the given portlet key<br/>
     *
     * @param portletKey The portlet key identifying the portlet
     * @return returns the removed instance of WSRPPortlet
     **/
    public WSRPPortlet removePortlet(PortletKey portletKey) {
        WSRPPortlet portlet = null;
        if (portletKey != null) {
            portlet = super.removePortlet(portletKey);
            contextMap.remove(portletKey.toString());
        }
        return portlet;
    }

    /**
     * Remove all portlets from the registry and delete them in the
     * persistent store.
     **/
    public void removeAllPortlets() {
        Iterator iterator = getAllPortlets();
        while (iterator.hasNext()) {
            WSRPPortlet portlet = (WSRPPortlet)iterator.next();

            // remove ConsumerPortletContext from map
            contextMap.remove(portlet.getPortletKey().toString());
        }
        super.removeAllPortlets();
    }

    /**
     * StateChanged Event occured by a registered WSRPPortlet. The
     * input source object, contained in the event will be
     * updated in the persistence store.
     * 
     * @param event 
     * @see StateChangedEvent
     */
    public void stateChanged(StateChangedEvent event) {
        WSRPPortlet portlet = null;
        try {
            portlet = (WSRPPortlet)event.getSource();

            //store PortletContext to persistent file
            ConsumerPortletContext consumerPortletContext =
                (ConsumerPortletContext)contextMap.get(portlet.getPortletKey().toString());

            consumerPortletContext.setPortletContext(portlet.getPortletContext());
            consumerPortletContext.setPortletKey(portlet.getPortletKey());
        } catch (ClassCastException ce) {
            logger.error("StateChanged-error in portlet: " + portlet.getPortletKey().getPortletHandle(), ce);
        }
    }
}
