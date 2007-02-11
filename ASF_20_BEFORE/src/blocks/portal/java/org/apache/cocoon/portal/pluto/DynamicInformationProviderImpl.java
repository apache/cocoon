/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

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
 * @version CVS $Id: DynamicInformationProviderImpl.java,v 1.1 2004/01/22 14:01:21 cziegeler Exp $
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
