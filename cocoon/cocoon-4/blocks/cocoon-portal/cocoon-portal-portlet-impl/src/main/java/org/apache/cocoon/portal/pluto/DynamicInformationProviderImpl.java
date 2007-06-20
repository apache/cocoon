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
package org.apache.cocoon.portal.pluto;

import java.util.HashSet;
import java.util.Iterator;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.pluto.adapter.PortletAdapter;
import org.apache.cocoon.portal.pluto.om.PortletEntityImpl;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.PortletActionProvider;
import org.apache.pluto.services.information.PortletURLProvider;
import org.apache.pluto.services.information.ResourceURLProvider;

/**
 * Our own dynamic information provider.
 *
 * @version $Id$
 */
public class DynamicInformationProviderImpl 
    implements DynamicInformationProvider {

    /** Service manager. */
    protected final ServiceManager manager;

    /** The portal context provider. */
    protected final PortalContextProviderImpl provider;

    /** The portal service. */
    protected final PortalService portalService;

    static protected final HashSet responseMimeTypes = new HashSet();

    static {
        responseMimeTypes.add("text/html");        
    }

    /**
     * Constructor
     */
    public DynamicInformationProviderImpl(ServiceManager manager,
                                          PortalContextProviderImpl provider,
                                          PortalService service) {
        this.manager = manager;
        this.provider = provider;
        this.portalService = service;
    }

    /**
     * @see org.apache.pluto.services.information.DynamicInformationProvider#getPortletURLProvider(org.apache.pluto.om.window.PortletWindow)
     */
    public PortletURLProvider getPortletURLProvider(PortletWindow portletWindow) {
        return new PortletURLProviderImpl(portletWindow, this.manager);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.DynamicInformationProvider#getResourceURLProvider(org.apache.pluto.om.window.PortletWindow)
     */
    public ResourceURLProvider getResourceURLProvider(PortletWindow portletWindow) {
        return new ResourceURLProviderImpl(this.provider);
    }

    /**
     * @see org.apache.pluto.services.information.DynamicInformationProvider#getPortletActionProvider(org.apache.pluto.om.window.PortletWindow)
     */
    public PortletActionProvider getPortletActionProvider(PortletWindow portletWindow) {
        return new PortletActionProviderImpl(portletWindow, this.portalService);
    }

    /**
     * @see org.apache.pluto.services.information.DynamicInformationProvider#getPortletMode(org.apache.pluto.om.window.PortletWindow)
     */
    public PortletMode getPortletMode(PortletWindow portletWindow) {
        final CopletInstance cid = ((PortletEntityImpl)portletWindow.getPortletEntity()).getCopletInstanceData();
        final String pmString = (String)cid.getTemporaryAttribute(PortletAdapter.PORTLET_MODE_ATTRIBUTE_NAME);
        if ( pmString == null ) {
            return PortletMode.VIEW;
        }
        return new PortletMode(pmString);
    }

    /**
     * @see org.apache.pluto.services.information.DynamicInformationProvider#getWindowState(org.apache.pluto.om.window.PortletWindow)
     */
    public WindowState getWindowState(PortletWindow portletWindow) {
        final CopletInstance cid = ((PortletEntityImpl)portletWindow.getPortletEntity()).getCopletInstanceData();
        final String wsString = (String)cid.getTemporaryAttribute(PortletAdapter.WINDOW_STATE_ATTRIBUTE_NAME);
        if ( wsString == null ) {
            return WindowState.NORMAL;
        }
        return new WindowState(wsString);
    }

    /**
     * @see org.apache.pluto.services.information.DynamicInformationProvider#getResponseContentType()
     */
    public String getResponseContentType() {
        return "text/html";
    }

    /**
     * @see org.apache.pluto.services.information.DynamicInformationProvider#getResponseContentTypes()
     */
    public Iterator getResponseContentTypes() {
        return responseMimeTypes.iterator();
    }

    /**
     * @see org.apache.pluto.services.information.DynamicInformationProvider#isPortletModeAllowed(javax.portlet.PortletMode)
     */
    public boolean isPortletModeAllowed(PortletMode mode) {
        return this.provider.getSupportedPortletModes().contains(mode);
    }

    /**
     * @see org.apache.pluto.services.information.DynamicInformationProvider#isWindowStateAllowed(javax.portlet.WindowState)
     */
    public boolean isWindowStateAllowed(WindowState state) {
        return this.provider.getSupportedWindowStates().contains(state);
    }
}
