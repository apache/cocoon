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
package org.apache.cocoon.portal.coplet.adapter.impl;

import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.portal.PortalManager;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.impl.PortletPortalManager;
import org.apache.cocoon.portal.pluto.om.PortletDefinitionRegistry;
import org.apache.cocoon.portal.pluto.om.PortletEntityListImpl;
import org.apache.cocoon.portal.pluto.om.PortletWindowImpl;
import org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl;
import org.apache.cocoon.portal.pluto.servlet.ServletRequestImpl;
import org.apache.cocoon.portal.pluto.servlet.ServletResponseImpl;
import org.apache.cocoon.portal.serialization.IncludingHTMLSerializer;
import org.apache.cocoon.servlet.CocoonServlet;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.om.window.PortletWindowCtrl;
import org.apache.pluto.om.window.PortletWindowList;
import org.apache.pluto.om.window.PortletWindowListCtrl;
import org.apache.pluto.services.PortletContainerEnvironment;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/**
 * This is the adapter to use JSR-168 portlets as coplets
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletAdapter.java,v 1.4 2004/02/06 13:07:17 cziegeler Exp $
 */
public class PortletAdapter 
    extends AbstractCopletAdapter
    implements Contextualizable {
	
    /** The avalon context */
    protected Context context;
    
    /** The portlet container */    
    protected PortletContainer portletContainer;
    
    /** The portlet container environment */
    protected PortletContainerEnvironment environment;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
        // now get the portal manager
        ServletConfig servletConfig = (ServletConfig) context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG);
        PortletPortalManager portalManager = (PortletPortalManager) servletConfig.getServletContext().getAttribute(PortalManager.ROLE);
        
        this.portletContainer = portalManager.getPortletContainer();
        this.environment = portalManager.getPortletContainerEnvironment();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplet.adapter.CopletAdapter#login(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void login(CopletInstanceData coplet) {
        super.login(coplet);

        PortletDefinitionRegistry registry = (PortletDefinitionRegistry) environment.getContainerService(PortletDefinitionRegistry.class);
        
        final String portletEntityId = (String) getConfiguration(coplet, "portlet");   
        
        PortletApplicationEntity pae = registry.getPortletApplicationEntityList().get(ObjectIDImpl.createFromString("cocoon"));
        PortletEntity portletEntity = ((PortletEntityListImpl)pae.getPortletEntityList()).add(pae, portletEntityId, coplet, registry);
        
        if ( portletEntity.getPortletDefinition() != null ) {
            // create the window
            PortletWindow portletWindow = new PortletWindowImpl(portletEntityId);                
            ((PortletWindowCtrl)portletWindow).setId(coplet.getId());
            ((PortletWindowCtrl)portletWindow).setPortletEntity(portletEntity);
            PortletWindowList windowList = portletEntity.getPortletWindowList();        
            ((PortletWindowListCtrl)windowList).add(portletWindow);    
            coplet.setAttribute("window", portletWindow);
            
            // load the portlet
            final Map objectModel = ContextHelper.getObjectModel(this.context);
            ServletRequestImpl  req = (ServletRequestImpl) objectModel.get("portlet-request");
            if ( req == null ) {
                final HttpServletResponse res = (HttpServletResponse) objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
                objectModel.put("portlet-response",  new ServletResponseImpl(res));
                req = new ServletRequestImpl((HttpServletRequest) objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT), null);
                objectModel.put("portlet-request",  req);                
            }
            final HttpServletResponse res = (HttpServletResponse) objectModel.get("portlet-response");
            PortletPortalManager.copletInstanceData.set(coplet);
            try {
                this.portletContainer.portletLoad(portletWindow, req.getRequest(portletWindow),  
                                                  res);
            } catch (Exception e) {
                this.getLogger().error("Error loading portlet " + portletEntityId, e);
                // remove portlet entity
                coplet.removeAttribute("window");
                ((PortletEntityListImpl)pae.getPortletEntityList()).remove(portletEntity);
            } finally {
                PortletPortalManager.copletInstanceData.set(null);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplet.adapter.impl.AbstractCopletAdapter#streamContent(org.apache.cocoon.portal.coplet.CopletInstanceData, org.xml.sax.ContentHandler)
     */
    public void streamContent(CopletInstanceData coplet,
                              ContentHandler contentHandler)
    throws SAXException {
        try {
            final String portlet = (String)super.getConfiguration(coplet, "portlet");
            if ( portlet == null ) {
                throw new SAXException("Portlet configuration is missing.");
            }
            // get the window
            final PortletWindow window = (PortletWindow)coplet.getAttribute("window");
            if ( window == null ) {
                throw new SAXException("Portlet couldn't be loaded: " + portlet);
            }
            final Map objectModel = ContextHelper.getObjectModel(this.context);
            final ServletRequestImpl  req = (ServletRequestImpl) objectModel.get("portlet-request");
            final HttpServletResponse res = (HttpServletResponse) objectModel.get("portlet-response");
            PortletPortalManager.copletInstanceData.set(coplet);
            
            // TODO - for parallel processing we have to clone the response!
            this.portletContainer.renderPortlet(window, req.getRequest(window), res);
            final String value = res.toString();
            
            // stream out the include for the serializer
            IncludingHTMLSerializer.addPortlet(portlet, value);
            contentHandler.startPrefixMapping("portal", IncludingHTMLSerializer.NAMESPACE);
            AttributesImpl attr = new AttributesImpl();
            attr.addCDATAAttribute("portlet", portlet);
            contentHandler.startElement(IncludingHTMLSerializer.NAMESPACE, 
                                        "include", "portal:include", attr);
            contentHandler.endElement(IncludingHTMLSerializer.NAMESPACE, 
                                      "include", "portal:include");
            contentHandler.endPrefixMapping("portlet");
        } catch (SAXException se) {
            throw se;
        } catch (Exception e) {
            throw new SAXException(e);
        } finally {
            PortletPortalManager.copletInstanceData.set(null);            
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplet.adapter.CopletAdapter#logout(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void logout(CopletInstanceData coplet) {
        super.logout(coplet);
        PortletWindow window = (PortletWindow)coplet.getAttribute("window");
        if ( window != null ) {
            coplet.removeAttribute("window");
            PortletDefinitionRegistry registry = (PortletDefinitionRegistry) environment.getContainerService(PortletDefinitionRegistry.class);
        
            PortletApplicationEntity pae = registry.getPortletApplicationEntityList().get(ObjectIDImpl.createFromString("cocoon"));
            ((PortletEntityListImpl)pae.getPortletEntityList()).remove(window.getPortletEntity());
        }
    }

}
