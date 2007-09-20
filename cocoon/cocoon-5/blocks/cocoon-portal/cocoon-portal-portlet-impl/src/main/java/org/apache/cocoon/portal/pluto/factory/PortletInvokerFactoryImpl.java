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
package org.apache.cocoon.portal.pluto.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.pluto.om.PortletDefinitionImpl;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.pluto.factory.PortletInvokerFactory;
import org.apache.pluto.invoker.PortletInvoker;
import org.apache.pluto.om.portlet.PortletDefinition;

/**
 * The implementation of the invoker factory.
 *
 * @version $Id$
 */
public class PortletInvokerFactoryImpl 
extends AbstractFactory
implements PortletInvokerFactory, Serviceable, ThreadSafe, Disposable {

    /** The service manager */
    protected ServiceManager manager;

    /** All local portlets */
    protected List localPortlets = Collections.synchronizedList(new ArrayList());

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager serviceManager) throws ServiceException {
        this.manager = serviceManager;
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        final Iterator i = this.localPortlets.iterator();
        while (i.hasNext()) {
            LocalPortletInvokerImpl current = (LocalPortletInvokerImpl)i.next();
            current.destroy();
        }
        this.localPortlets.clear();
        this.manager = null;
    }

    /**
     * @see org.apache.pluto.factory.PortletInvokerFactory#getPortletInvoker(org.apache.pluto.om.portlet.PortletDefinition)
     */
    public PortletInvoker getPortletInvoker(PortletDefinition portletDefinition) {
        // test, if this is a local portlet
        boolean local = false;
        PortletInvoker invoker;
        if ( portletDefinition instanceof PortletDefinitionImpl ) {
            local = ((PortletDefinitionImpl)portletDefinition).isLocalPortlet();
        }
        if (local) {
            invoker = ((PortletDefinitionImpl)portletDefinition).getLocalPortletInvoker();
            if ( invoker == null ) {
                invoker = new LocalPortletInvokerImpl(portletDefinition, this.servletConfig);
                this.localPortlets.add(invoker);
                ((PortletDefinitionImpl)portletDefinition).setLocalPortletInvoker(invoker);
                try {
                    if ( invoker instanceof AbstractLogEnabled ) {
                        ((AbstractLogEnabled)invoker).setLogger(this.getLogger());
                    }
                    ContainerUtil.service(invoker, this.manager);
                    ContainerUtil.initialize(invoker);
                } catch (Exception ignore) {
                    this.getLogger().warn("Unable to initialize local portlet invoker.", ignore);
                }
            }
        } else {
            invoker = new PortletInvokerImpl(portletDefinition, this.servletConfig);
        }

        return invoker;
    }

    /**
     * @see org.apache.pluto.factory.PortletInvokerFactory#releasePortletInvoker(org.apache.pluto.invoker.PortletInvoker)
     */
    public void releasePortletInvoker(PortletInvoker invoker) {
        // nothing to do here
    }
}
