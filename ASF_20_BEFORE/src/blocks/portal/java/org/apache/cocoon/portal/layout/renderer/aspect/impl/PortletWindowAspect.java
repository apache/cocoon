/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.portal.PortalManager;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.status.SizingStatus;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.impl.ChangeCopletInstanceAspectDataEvent;
import org.apache.cocoon.portal.event.impl.FullScreenCopletEvent;
import org.apache.cocoon.portal.impl.PortletPortalManager;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.portal.pluto.PortletURLProviderImpl;
import org.apache.cocoon.servlet.CocoonServlet;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.PortletContainerEnvironment;
import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.InformationProviderService;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This aspect draws a portlet window for a JSR-168 implementation
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletWindowAspect.java,v 1.2 2004/02/06 13:07:17 cziegeler Exp $
 */
public final class PortletWindowAspect 
extends AbstractAspect 
implements Contextualizable {

    /** The environment */
    protected PortletContainerEnvironment environment;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        // now get the portal manager
        ServletConfig servletConfig = (ServletConfig) context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG);
        PortletPortalManager portalManager = (PortletPortalManager) servletConfig.getServletContext().getAttribute(PortalManager.ROLE);
        if ( portalManager != null ) {
            this.environment = portalManager.getPortletContainerEnvironment();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.renderer.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext context,
                      Layout layout,
                      PortalService service,
                      ContentHandler contenthandler)
    throws SAXException {
        final PreparedConfiguration config = (PreparedConfiguration)context.getAspectConfiguration();
        final CopletInstanceData copletInstanceData = ((CopletLayout)layout).getCopletInstanceData();

        if ( config.rootTag ) {
            XMLUtils.startElement(contenthandler, config.tagName);
        }
        final PortletWindow window = (PortletWindow)copletInstanceData.getAttribute("window");
        if ( window == null ) {
            // no portlet window, so use a default behaviour
            XMLUtils.createElement(contenthandler, "title", copletInstanceData.getCopletData().getTitle());
        } else {
            String title = (String) copletInstanceData.getAttribute("dynamic-title");
            if ( title == null ) {
                final PortletDefinition def = window.getPortletEntity().getPortletDefinition();
                try {
                    title = def.getDisplayName(def.getLanguageSet().getDefaultLocale()).getDisplayName();
                } catch (Exception ignore)  {
                    title = copletInstanceData.getCopletData().getTitle();
                }
            }
            XMLUtils.createElement(contenthandler, "title", title);            
        

            if ( this.environment != null ) {
                InformationProviderService ips = (InformationProviderService) this.environment.getContainerService(InformationProviderService.class);
                DynamicInformationProvider dip = ips.getDynamicProvider((HttpServletRequest) context.getObjectModel().get("portlet-request"));
                
                Event event;    

                // Sizing
                WindowState ws = (WindowState)copletInstanceData.getAttribute("window-state"); 
                if ( ws == null ) {
                    ws = WindowState.NORMAL;
                }
                
                if ( ws.equals(WindowState.NORMAL) ) {
                    event = new ChangeCopletInstanceAspectDataEvent(copletInstanceData, "size", SizingStatus.STATUS_MINIMIZED);
                    PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                    url.clearParameters();
                    url.setWindowState(WindowState.MINIMIZED);
                    
                    XMLUtils.createElement(contenthandler, "minimize-uri", url.toString(event));
                }

                if ( ws.equals(WindowState.MINIMIZED)) {
                    event = new ChangeCopletInstanceAspectDataEvent(copletInstanceData, "size", SizingStatus.STATUS_MAXIMIZED);
                    PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                    url.clearParameters();
                    url.setWindowState(WindowState.NORMAL);
                    XMLUtils.createElement(contenthandler, "maximize-uri", url.toString(event));
                }

                final Layout fullScreenLayout = service.getComponentManager().getProfileManager().getEntryLayout();
                if ( fullScreenLayout != null && fullScreenLayout.equals( layout )) {
                    event = new FullScreenCopletEvent( copletInstanceData, null );
                    PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                    url.clearParameters();
                    url.setWindowState(WindowState.NORMAL);
                    XMLUtils.createElement(contenthandler, "fullscreen-uri", url.toString(event));
                } else {
                    event = new FullScreenCopletEvent( copletInstanceData, layout );
                    PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                    url.clearParameters();
                    url.setWindowState(WindowState.MAXIMIZED);
                    XMLUtils.createElement(contenthandler, "fullscreen-uri", url.toString(event));
                }
                
                // portlet modes
                PortletMode pm = (PortletMode)copletInstanceData.getAttribute("portlet-mode"); 
                if ( pm == null ) {
                    pm = PortletMode.VIEW;
                }
                if ( !pm.equals(PortletMode.EDIT) ) {
                    PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                    url.clearParameters();
                    url.setPortletMode(PortletMode.EDIT);
                    XMLUtils.createElement(contenthandler, "edit-uri", url.toString());                    
                }
                if ( !pm.equals(PortletMode.HELP) ) {
                    PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                    url.clearParameters();
                    url.setPortletMode(PortletMode.HELP);
                    XMLUtils.createElement(contenthandler, "help-uri", url.toString());                    
                }                
                if ( !pm.equals(PortletMode.VIEW) ) {
                    PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                    url.clearParameters();
                    url.setPortletMode(PortletMode.VIEW);
                    XMLUtils.createElement(contenthandler, "view-uri", url.toString());                    
                }                
            }
        }

        context.invokeNext( layout, service, contenthandler );
        
        if ( config.rootTag ) {
            XMLUtils.endElement(contenthandler, config.tagName);
        }
    }

    protected class PreparedConfiguration {
        public String tagName;
        public boolean rootTag;
        
        public void takeValues(PreparedConfiguration from) {
            this.tagName = from.tagName;
            this.rootTag = from.rootTag;
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#prepareConfiguration(org.apache.avalon.framework.parameters.Parameters)
     */
    public Object prepareConfiguration(Parameters configuration) 
    throws ParameterException {
        PreparedConfiguration pc = new PreparedConfiguration();
        pc.tagName = configuration.getParameter("tag-name", "window");
        pc.rootTag = configuration.getParameterAsBoolean("root-tag", true);
        return pc;
    }

}
