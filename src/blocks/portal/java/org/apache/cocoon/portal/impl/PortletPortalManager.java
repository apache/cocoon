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
package org.apache.cocoon.portal.impl;

import java.util.Map;
import java.util.Properties;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.portal.PortalManager;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Filter;
import org.apache.cocoon.portal.event.Subscriber;
import org.apache.cocoon.portal.pluto.PortletContainerEnvironmentImpl;
import org.apache.cocoon.portal.pluto.PortletURLProviderImpl;
import org.apache.cocoon.portal.pluto.servlet.ServletRequestImpl;
import org.apache.cocoon.portal.pluto.servlet.ServletResponseImpl;
import org.apache.cocoon.servlet.CocoonServlet;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletContainerImpl;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.PortletContainerEnvironment;
import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.InformationProviderService;
import org.apache.pluto.services.information.PortletActionProvider;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Extends the PortalManager by initializing Pluto
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletPortalManager.java,v 1.3 2004/02/06 13:07:17 cziegeler Exp $
 */
public class PortletPortalManager
	extends PortalManagerImpl
	implements Initializable, Contextualizable, Disposable, Subscriber {

    public static ThreadLocal copletInstanceData = new InheritableThreadLocal();
    
    /** The servlet configuration for pluto */
    protected ServletConfig servletConfig;
    
    /** The Portlet Container */
    protected PortletContainer portletContainer;
    
    /** The Portlet Container environment */
    protected PortletContainerEnvironmentImpl portletContainerEnvironment;
    
    /** The component context */
    protected Context context;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.servletConfig = (ServletConfig) context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG);
        // we have to somehow pass this component down to other components!
        // This is ugly, but it's the only chance for sofisticated component containers
        // that wrap component implementations!
        this.servletConfig.getServletContext().setAttribute(PortalManager.ROLE, this);
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        EventManager eventManager = null;
        try {
            eventManager = (EventManager)this.manager.lookup(EventManager.ROLE);
            eventManager.getRegister().subscribe(this);
        } finally {
            this.manager.release(eventManager);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        try {
            if (this.portletContainer != null ) {
                this.portletContainer.shutdown();
                this.portletContainer = null;
            }
            ContainerUtil.dispose(this.portletContainerEnvironment);
            this.portletContainerEnvironment = null;
        } catch (Throwable t) {
            this.getLogger().error("Destruction failed!", t);
        }
        if ( this.servletConfig != null ) {
            this.servletConfig.getServletContext().removeAttribute(PortalManager.ROLE);
            this.servletConfig = null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        this.initContainer();
    }
    
    /**
     * Return the portlet container
     */
    public PortletContainer getPortletContainer() {
        return this.portletContainer;        
    }
    
    /**
     * Return the portlet container environment
     */
    public PortletContainerEnvironment getPortletContainerEnvironment() {
        return this.portletContainerEnvironment;        
    }

    /**
     * Initialize the container
     */
    public void initContainer() throws Exception {
        this.portletContainer = new PortletContainerImpl();
        
        if (!portletContainer.isInitialized()) {
            this.getLogger().debug ("Initializing PortletContainer...");
            
            final String uniqueContainerName = "cocoon-portal";
            
            this.portletContainerEnvironment = new PortletContainerEnvironmentImpl();
            ContainerUtil.enableLogging(this.portletContainerEnvironment, this.getLogger());
            ContainerUtil.contextualize(this.portletContainerEnvironment, this.context);
            ContainerUtil.service(this.portletContainerEnvironment, this.manager);
            ContainerUtil.initialize(this.portletContainerEnvironment);
            
            Properties properties = new Properties();
            
            try {
                portletContainer.init(uniqueContainerName, servletConfig, this.portletContainerEnvironment, properties);
            } catch (PortletContainerException exc) {
                throw new ProcessingException("Initialization of the portlet container failed.", exc);
            }
        } else {
            this.getLogger().debug("PortletContainer already initialized.");
        }
        
        this.getLogger().debug("PortletContainer initialized.");
        
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.PortalManager#process()
     */
    public void process() throws ProcessingException {
        // process the events
        super.process();
        // do we already have an environment?
        // if not, create one
        final Map objectModel = ContextHelper.getObjectModel(this.context);

        PortletURLProviderImpl event = (PortletURLProviderImpl) objectModel.get("portlet-event");
        if ( event != null ) {
            PortletWindow actionWindow = event.getPortletWindow();
            try {
                final ServletRequestImpl req = (ServletRequestImpl) objectModel.get("portlet-request");
                final ServletResponseImpl res= (ServletResponseImpl)objectModel.get("portlet-response");
                this.portletContainer.processPortletAction(actionWindow, req.getRequest(actionWindow), res);

                final String redirectURL = res.getRedirectURL();
                HttpServletResponse response = (HttpServletResponse) objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
                response.sendRedirect(redirectURL);
            } catch (Exception ignore) {
                this.getLogger().error("Error during processing of portlet action.", ignore);
            }
        } else if ( objectModel.get("portlet-response") == null ) {
            final HttpServletResponse res = (HttpServletResponse) objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
            objectModel.put("portlet-response",  new ServletResponseImpl(res));
            final ServletRequestImpl req = new ServletRequestImpl((HttpServletRequest) objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT), null);
            objectModel.put("portlet-request",  req);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.PortalManager#showPortal(org.xml.sax.ContentHandler, org.apache.avalon.framework.parameters.Parameters)
     */
    public void showPortal(ContentHandler contentHandler, Parameters parameters)
    throws SAXException {
        final Map objectModel = ContextHelper.getObjectModel(this.context);

        // don't generate a response, if we issued a redirect
        if (objectModel.get("portlet-event") == null) {
            super.showPortal(contentHandler, parameters);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#getEventType()
     */
    public Class getEventType() {
        return PortletURLProviderImpl.class;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#getFilter()
     */
    public Filter getFilter() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#inform(org.apache.cocoon.portal.event.Event)
     */
    public void inform(Event e) {
        PortletURLProviderImpl event = (PortletURLProviderImpl)e;
        final Map objectModel = ContextHelper.getObjectModel(this.context);
        final ServletRequestImpl req = new ServletRequestImpl((HttpServletRequest) objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT), event);
        final HttpServletResponse res = new ServletResponseImpl((HttpServletResponse) objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT));
        objectModel.put("portlet-response",  res);
        objectModel.put("portlet-request", req);        
        
        if ( event.isAction() ) {
            // This means we can only have ONE portlet event per request!
            objectModel.put("portlet-event", event);
        } else {
            DynamicInformationProvider dynProv;
            InformationProviderService ips;
            PortletActionProvider pap;
            
            ips = (InformationProviderService)this.portletContainerEnvironment.getContainerService(InformationProviderService.class);
            dynProv = ips.getDynamicProvider(req);
            pap = dynProv.getPortletActionProvider(event.getPortletWindow());
            final PortletMode mode = event.getPortletMode();
            if ( mode != null ) {
                pap.changePortletMode(mode);
            }
            final WindowState state = event.getWindowState();
            if ( state != null ) {
                pap.changePortletWindowState(state);
            }
        }
    }

}

