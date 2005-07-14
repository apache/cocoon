/*
 * Copyright 2004-2005 The Apache Software Foundation.
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
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.portal.PortalManager;
import org.apache.cocoon.portal.PortalManagerAspect;
import org.apache.cocoon.portal.PortalManagerAspectPrepareContext;
import org.apache.cocoon.portal.PortalManagerAspectRenderContext;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Receiver;
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
 * This aspect provides the JSR-168 support by initializing Pluto.
 * The aspect can be configured at the portal manager.
 *
 * @version SVN $Id$
 */
public class PortletPortalManagerAspect
	extends AbstractLogEnabled
	implements PortalManagerAspect,
               Contextualizable,
               Serviceable,
               Initializable,
               Disposable,
               Receiver {

    public static final ThreadLocal copletInstanceData = new InheritableThreadLocal();

    /** The servlet configuration for pluto */
    protected ServletConfig servletConfig;

    /** The Portlet Container */
    protected PortletContainer portletContainer;

    /** The Portlet Container environment */
    protected PortletContainerEnvironmentImpl portletContainerEnvironment;

    /** The service locator */
    protected ServiceManager manager;

    /** The component context */
    protected Context context;

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
        try {
            this.servletConfig = (ServletConfig) context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG);
            // we have to somehow pass this component down to other components!
            // This is ugly, but it's the only chance for sofisticated component containers
            // that wrap component implementations!
            this.servletConfig.getServletContext().setAttribute(PortletPortalManagerAspect.class.getName(), this);
        } catch (ContextException ignore) {
            // we ignore the context exception
            // this avoids startup errors if the portal is configured for the CLI
            // environment
            this.getLogger().warn("The JSR-168 support is disabled as the servlet context is not available.", ignore);
        }
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        EventManager eventManager = null;
        try {
            eventManager = (EventManager)this.manager.lookup(EventManager.ROLE);
            eventManager.subscribe(this);
        } finally {
            this.manager.release(eventManager);
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            EventManager eventManager = null;
            try {
                eventManager = (EventManager)this.manager.lookup(EventManager.ROLE);
                eventManager.unsubscribe(this);
            } catch (Exception ignore) {
                // let's ignore it
            } finally {
                this.manager.release(eventManager);
            }
            this.manager = null;
        }
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

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        if ( this.servletConfig != null ) {
            this.initContainer();
        }
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

    /**
     * @see Receiver
     */
    public void inform(PortletURLProviderImpl event, PortalService service) {
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

    /**
     * @see org.apache.cocoon.portal.PortalManagerAspect#prepare(org.apache.cocoon.portal.PortalManagerAspectPrepareContext, org.apache.cocoon.portal.PortalService)
     */
    public void prepare(PortalManagerAspectPrepareContext aspectContext,
                        PortalService service)
    throws ProcessingException {
        // process the events
        aspectContext.invokeNext();
        
        // if we aren't running in a servlet environment, just skip the JSR-168 part
        if ( this.servletConfig == null ) {
            return;
        }
        
        // do we already have an environment?
        // if not, create one
        final Map objectModel = aspectContext.getObjectModel();

        PortletURLProviderImpl event = (PortletURLProviderImpl) objectModel.get("portlet-event");
        if ( event != null ) {
            PortletWindow actionWindow = event.getPortletWindow();
            try {
                final ServletRequestImpl req = (ServletRequestImpl) objectModel.get("portlet-request");
                final ServletResponseImpl res= (ServletResponseImpl)objectModel.get("portlet-response");
                this.portletContainer.processPortletAction(actionWindow, req.getRequest(actionWindow), res);

                // this redirect is only for 2.1.x, don't add it to 2.2 
                // (see #32157 for more information)
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

    /**
     * @see org.apache.cocoon.portal.PortalManagerAspect#render(org.apache.cocoon.portal.PortalManagerAspectRenderContext, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler, org.apache.avalon.framework.parameters.Parameters)
     */
    public void render(PortalManagerAspectRenderContext aspectContext,
                       PortalService service,
                       ContentHandler ch,
                       Parameters parameters)
    throws SAXException {
        final Map objectModel = aspectContext.getObjectModel();

        // don't generate a response, if we issued a redirect
        if (objectModel.get("portlet-event") == null) {
            aspectContext.invokeNext(ch, parameters);
        }
    }


}

