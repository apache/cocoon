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

import javax.portlet.RenderRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.portal.PortalService;
import org.apache.pluto.factory.RenderRequestFactory;
import org.apache.pluto.om.window.PortletWindow;

/**
 * Implementation for the render request factory.
 *
 * @version $Id$
 */
public class RenderRequestFactoryImpl
    extends AbstractFactory
    implements RenderRequestFactory, Serviceable, Disposable {

    /** The service manager. */
    protected ServiceManager manager;

    /** The portal service. */
    protected PortalService portalService;

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.portalService);
            this.portalService = null;
            this.manager = null;
        }
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.portalService = (PortalService)this.manager.lookup(PortalService.ROLE);
    }

    /**
     * @see org.apache.pluto.factory.RenderRequestFactory#getRenderRequest(org.apache.pluto.om.window.PortletWindow, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public RenderRequest getRenderRequest(PortletWindow       portletWindow,
                                          HttpServletRequest  servletRequest,
                                          HttpServletResponse servletResponse) {
        RenderRequest renderRequest = new RenderRequestImpl( portletWindow,
                                                             servletRequest,
                                                             this.portalService.getComponentManager().getProfileManager().getUser());
        return renderRequest;
    }
}
