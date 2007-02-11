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
 * @version CVS $Id: PortletAdapter.java,v 1.7 2004/03/15 10:31:37 cziegeler Exp $
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
        try {
            // now get the portal manager
            ServletConfig servletConfig = (ServletConfig) context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG);
            PortletPortalManager portalManager = (PortletPortalManager) servletConfig.getServletContext().getAttribute(PortalManager.ROLE);
            
            this.portletContainer = portalManager.getPortletContainer();
            this.environment = portalManager.getPortletContainerEnvironment();
        } catch (ContextException ignore) {
            // we ignore the context exception
            // this avoids startup errors if the portal is configured for the CLI
            // environment
            this.getLogger().warn("The JSR-168 support is disabled as the servlet context is not available.", ignore);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplet.adapter.CopletAdapter#login(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void login(CopletInstanceData coplet) {
        super.login(coplet);

        if ( this.portletContainer == null ) {
            return;
        }
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
        if ( this.portletContainer == null ) {
            throw new SAXException("Unable to execute JSR-168 portlets because of missing servlet context.");
        }
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
            contentHandler.endPrefixMapping("portal");
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
        if ( this.portletContainer == null ) {
            return;
        }
        PortletWindow window = (PortletWindow)coplet.getAttribute("window");
        if ( window != null ) {
            coplet.removeAttribute("window");
            PortletDefinitionRegistry registry = (PortletDefinitionRegistry) environment.getContainerService(PortletDefinitionRegistry.class);
        
            PortletApplicationEntity pae = registry.getPortletApplicationEntityList().get(ObjectIDImpl.createFromString("cocoon"));
            ((PortletEntityListImpl)pae.getPortletEntityList()).remove(window.getPortletEntity());
        }
    }

}
