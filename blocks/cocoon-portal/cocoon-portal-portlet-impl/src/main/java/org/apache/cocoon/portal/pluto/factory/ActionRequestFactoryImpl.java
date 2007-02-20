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

import javax.portlet.ActionRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.portal.PortalService;
import org.apache.pluto.factory.ActionRequestFactory;
import org.apache.pluto.om.window.PortletWindow;

/**
 * Implementation for the render request factory.
 *
 * @version $Id$
 */
public class ActionRequestFactoryImpl
    extends AbstractFactory
    implements ActionRequestFactory, Serviceable, Disposable {

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
    public void service(ServiceManager serviceManager) throws ServiceException {
        this.manager = serviceManager;
        this.portalService = (PortalService)this.manager.lookup(PortalService.class.getName());
    }

    /**
     * @see org.apache.pluto.factory.ActionRequestFactory#getActionRequest(org.apache.pluto.om.window.PortletWindow, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public ActionRequest getActionRequest(PortletWindow       portletWindow,
                                          HttpServletRequest  servletRequest,
                                          HttpServletResponse servletResponse) {
        ActionRequest actionRequest = new ActionRequestImpl(portletWindow, servletRequest, this.portalService.getUserService().getUser());
        return actionRequest;
    }
}
