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
package org.apache.cocoon.portal.pluto;

import java.util.HashSet;
import java.util.Iterator;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.pluto.om.PortletEntityImpl;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.PortletActionProvider;
import org.apache.pluto.services.information.PortletURLProvider;
import org.apache.pluto.services.information.ResourceURLProvider;

/**
 * Our own dynamic information provider
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: DynamicInformationProviderImpl.java,v 1.2 2004/03/05 13:02:14 bdelacretaz Exp $
 */
public class DynamicInformationProviderImpl 
implements DynamicInformationProvider {

    /** Service manager */
    protected final ServiceManager manager;
    
    /** The portal context provider */
    protected final PortalContextProviderImpl provider;
    
    /**
     * Constructor
     */
    public DynamicInformationProviderImpl(ServiceManager manager,
                                          PortalContextProviderImpl provider) {
        this.manager = manager;
        this.provider = provider;
    }

    /* (non-Javadoc)
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

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.DynamicInformationProvider#getPortletActionProvider(org.apache.pluto.om.window.PortletWindow)
     */
    public PortletActionProvider getPortletActionProvider(PortletWindow portletWindow) {
        return new PortletActionProviderImpl(portletWindow);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.DynamicInformationProvider#getPortletMode(org.apache.pluto.om.window.PortletWindow)
     */
    public PortletMode getPortletMode(PortletWindow portletWindow) {
        final CopletInstanceData cid = ((PortletEntityImpl)portletWindow.getPortletEntity()).getCopletInstanceData();
        PortletMode pm = (PortletMode) cid.getAttribute("portlet-mode");
        if ( pm == null ) {
            pm = PortletMode.VIEW;
        }
        return pm;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.DynamicInformationProvider#getPreviousPortletMode(org.apache.pluto.om.window.PortletWindow)
     */
    public PortletMode getPreviousPortletMode(PortletWindow portletWindow) {
        final CopletInstanceData cid = ((PortletEntityImpl)portletWindow.getPortletEntity()).getCopletInstanceData();
        PortletMode pm = (PortletMode) cid.getAttribute("previous-portlet-mode");
        return pm;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.DynamicInformationProvider#getWindowState(org.apache.pluto.om.window.PortletWindow)
     */
    public WindowState getWindowState(PortletWindow portletWindow) {
        final CopletInstanceData cid = ((PortletEntityImpl)portletWindow.getPortletEntity()).getCopletInstanceData();
        WindowState ws = (WindowState) cid.getAttribute("window-state");
        if ( ws == null ) {
            ws = WindowState.NORMAL;
        }
        return ws;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.DynamicInformationProvider#getPreviousWindowState(org.apache.pluto.om.window.PortletWindow)
     */
    public WindowState getPreviousWindowState(PortletWindow portletWindow) {
        final CopletInstanceData cid = ((PortletEntityImpl)portletWindow.getPortletEntity()).getCopletInstanceData();
        WindowState ws = (WindowState) cid.getAttribute("previous-window-state");        
        return ws;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.DynamicInformationProvider#getResponseContentType()
     */
    public String getResponseContentType() {
        return "text/html";
    }

    static protected HashSet responseMimeTypes;
    
    static {
        responseMimeTypes = new HashSet();
        responseMimeTypes.add("text/html");        
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.DynamicInformationProvider#getResponseContentTypes()
     */
    public Iterator getResponseContentTypes() {
        return responseMimeTypes.iterator();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.DynamicInformationProvider#isPortletModeAllowed(javax.portlet.PortletMode)
     */
    public boolean isPortletModeAllowed(PortletMode mode) {
        return this.provider.getSupportedPortletModes().contains(mode);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.DynamicInformationProvider#isWindowStateAllowed(javax.portlet.WindowState)
     */
    public boolean isWindowStateAllowed(WindowState state) {
        return this.provider.getSupportedWindowStates().contains(state);
    }
    
}
