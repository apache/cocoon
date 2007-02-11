/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.pluto.om.PortletDefinitionImpl;
import org.apache.pluto.factory.PortletInvokerFactory;
import org.apache.pluto.invoker.PortletInvoker;
import org.apache.pluto.invoker.impl.PortletInvokerImpl;
import org.apache.pluto.om.portlet.PortletDefinition;

/**
 * The implementation of the invoker factory
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletInvokerFactoryImpl.java,v 1.4 2004/03/05 13:02:14 bdelacretaz Exp $
 */
public class PortletInvokerFactoryImpl 
extends AbstractFactory
implements PortletInvokerFactory, Serviceable, Contextualizable, ThreadSafe, Disposable {

    /** The avalon context */
    protected Context context;
    
    /** The service manager */
    protected ServiceManager manager;
    
    /** All local portlets */
    protected List localPortlets = Collections.synchronizedList(new ArrayList());
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /* (non-Javadoc)
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
        this.context = null;
    }

    /* (non-Javadoc)
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
                    ContainerUtil.enableLogging(invoker, this.getLogger());
                    ContainerUtil.contextualize(invoker, this.context);
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

    /* (non-Javadoc)
     * @see org.apache.pluto.factory.PortletInvokerFactory#releasePortletInvoker(org.apache.pluto.invoker.PortletInvoker)
     */
    public void releasePortletInvoker(PortletInvoker invoker) {
        // nothing to do here
    }

}
