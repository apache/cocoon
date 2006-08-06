/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto;

import javax.portlet.PortalContext;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.RequiredContainerServices;
import org.apache.pluto.internal.InternalPortletWindow;
import org.apache.pluto.spi.PortalCallbackService;
import org.apache.pluto.spi.optional.PortletEnvironmentService;
import org.apache.pluto.spi.optional.PortletInvokerService;
import org.apache.pluto.spi.optional.PortletPreferencesService;

/**
 * 
 * @version $Id$
 *
 */
public class ContainerServicesImpl
    extends AbstractLogEnabled
    implements RequiredContainerServices, OptionalContainerServices {

    protected PortalContext portalContext = new PortalContextImpl();

    /**
     * @see org.apache.pluto.RequiredContainerServices#getPortalCallbackService()
     */
    public PortalCallbackService getPortalCallbackService() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.apache.pluto.RequiredContainerServices#getPortalContext()
     */
    public PortalContext getPortalContext() {
        return this.portalContext;
    }

    /**
     * @see org.apache.pluto.OptionalContainerServices#getPortletEnvironmentService()
     */
    public PortletEnvironmentService getPortletEnvironmentService() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.apache.pluto.OptionalContainerServices#getPortletInvokerService(org.apache.pluto.internal.InternalPortletWindow)
     */
    public PortletInvokerService getPortletInvokerService(InternalPortletWindow arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.apache.pluto.OptionalContainerServices#getPortletPreferencesService()
     */
    public PortletPreferencesService getPortletPreferencesService() {
        // TODO Auto-generated method stub
        return null;
    }
}
