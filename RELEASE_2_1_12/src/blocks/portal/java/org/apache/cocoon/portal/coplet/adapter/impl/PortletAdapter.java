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
package org.apache.cocoon.portal.coplet.adapter.impl;

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
import org.apache.cocoon.portal.PortalManagerAspect;
import org.apache.cocoon.portal.PortalManagerAspectPrepareContext;
import org.apache.cocoon.portal.PortalManagerAspectRenderContext;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.pluto.PortletActionProviderImpl;
import org.apache.cocoon.portal.pluto.PortletContainerEnvironmentImpl;
import org.apache.cocoon.portal.pluto.PortletURLProviderImpl;
import org.apache.cocoon.portal.pluto.om.PortletDefinitionRegistry;
import org.apache.cocoon.portal.pluto.om.PortletEntityListImpl;
import org.apache.cocoon.portal.pluto.om.PortletWindowImpl;
import org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl;
import org.apache.cocoon.portal.pluto.servlet.ServletRequestImpl;
import org.apache.cocoon.portal.pluto.servlet.ServletResponseImpl;
import org.apache.cocoon.portal.serialization.IncludingHTMLSerializer;
import org.apache.cocoon.portal.util.HtmlSaxParser;
import org.apache.cocoon.servlet.CocoonServlet;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletContainerImpl;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.om.window.PortletWindowCtrl;
import org.apache.pluto.om.window.PortletWindowList;
import org.apache.pluto.om.window.PortletWindowListCtrl;
import org.apache.pluto.services.PortletContainerEnvironment;
import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.InformationProviderService;
import org.apache.pluto.services.information.PortletActionProvider;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/**
 * This is the adapter to use JSR-168 portlets as coplets.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version $Id$
 */
public class PortletAdapter 
    extends AbstractCopletAdapter
    implements Contextualizable, Initializable, PortalManagerAspect, Receiver, Disposable {

    /** The avalon context */
    protected Context context;

    /** The servlet configuration for pluto */
    protected ServletConfig servletConfig;

    /** The Portlet Container */
    protected PortletContainer portletContainer;

    /** The Portlet Container environment */
    protected PortletContainerEnvironmentImpl portletContainerEnvironment;

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
        try {
            // TODO - we could lookup the component from the adapter selector
            this.servletConfig = (ServletConfig) context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG);
            // we have to somehow pass this component down to other components!
            // This is ugly, but it's the only chance for sofisticated component containers
            // that wrap component implementations!
            this.servletConfig.getServletContext().setAttribute(PortletAdapter.class.getName(), this);
        } catch (ContextException ignore) {
            // we ignore the context exception
            // this avoids startup errors if the portal is configured for the CLI
            // environment
            this.getLogger().warn("The JSR-168 support is disabled as the servlet context is not available.", ignore);
        }
    }

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.CopletAdapter#login(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void login(CopletInstanceData coplet) {
        super.login(coplet);

        if ( this.portletContainer == null ) {
            return;
        }
        PortletDefinitionRegistry registry = (PortletDefinitionRegistry) portletContainerEnvironment.getContainerService(PortletDefinitionRegistry.class);

        final String portletEntityId = (String) getConfiguration(coplet, "portlet");
        if ( this.getLogger().isDebugEnabled() ) {
            this.getLogger().debug("Coplet " + coplet.getId() + " tries to login into portlet " + portletEntityId);
        }

        PortletApplicationEntity pae = registry.getPortletApplicationEntityList().get(ObjectIDImpl.createFromString("cocoon"));
        PortletEntity portletEntity = ((PortletEntityListImpl)pae.getPortletEntityList()).add(pae, portletEntityId, coplet, registry);

        if ( portletEntity.getPortletDefinition() != null ) {
            // create the window
            PortletWindow portletWindow = new PortletWindowImpl(portletEntityId);                
            ((PortletWindowCtrl)portletWindow).setId(coplet.getId());
            ((PortletWindowCtrl)portletWindow).setPortletEntity(portletEntity);
            PortletWindowList windowList = portletEntity.getPortletWindowList();        
            ((PortletWindowListCtrl)windowList).add(portletWindow);    
            coplet.setTemporaryAttribute("window", portletWindow);

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
            try {
                this.portletContainer.portletLoad(portletWindow, req.getRequest(portletWindow),  
                                                  res);
            } catch (Exception e) {
                this.getLogger().error("Error loading portlet " + portletEntityId + " for instance " + coplet.getId(), e);
                // remove portlet entity
                coplet.removeTemporaryAttribute("window");
                ((PortletEntityListImpl)pae.getPortletEntityList()).remove(portletEntity);
            }
        } else {
            this.getLogger().error("Error finding portlet " + portletEntityId + " for instance " + coplet.getId() + " - no definition found.");
        }
    }

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.impl.AbstractCopletAdapter#streamContent(org.apache.cocoon.portal.coplet.CopletInstanceData, org.xml.sax.ContentHandler)
     */
    public void streamContent(CopletInstanceData coplet,
                              ContentHandler contentHandler)
    throws SAXException {
        if ( this.portletContainer == null ) {
            throw new SAXException("Unable to execute JSR-168 portlets because of missing servlet context.");
        }
        try {
            final String portletEntityId = (String) getConfiguration(coplet, "portlet");
            // get the window
            final PortletWindow window = (PortletWindow)coplet.getTemporaryAttribute("window");
            if ( window == null ) {
                throw new SAXException("Portlet couldn't be loaded: " + coplet.getId() + "(" + portletEntityId + ")");
            }
            final Map objectModel = ContextHelper.getObjectModel(this.context);
            final ServletRequestImpl  req = (ServletRequestImpl) objectModel.get("portlet-request");
            final HttpServletResponse res = (HttpServletResponse) objectModel.get("portlet-response");

            // TODO - for parallel processing we have to clone the response!
            this.portletContainer.renderPortlet(window, req.getRequest(window), res);
            final String value = this.getResponse(coplet, res);

            final Boolean usePipeline = (Boolean)this.getConfiguration(coplet, "use-pipeline", Boolean.FALSE);
            if ( usePipeline.booleanValue() ) {
                HtmlSaxParser.parseString(value, HtmlSaxParser.getContentFilter(contentHandler));
            } else {
                // stream out the include for the serializer
                IncludingHTMLSerializer.addPortlet(coplet.getId(), value);
                contentHandler.startPrefixMapping("portal", IncludingHTMLSerializer.NAMESPACE);
                AttributesImpl attr = new AttributesImpl();
                attr.addCDATAAttribute("portlet", coplet.getId());
                contentHandler.startElement(IncludingHTMLSerializer.NAMESPACE, 
                                            "include", "portal:include", attr);
                contentHandler.endElement(IncludingHTMLSerializer.NAMESPACE, 
                                          "include", "portal:include");
                contentHandler.endPrefixMapping("portal");
            }
        } catch (SAXException se) {
            throw se;
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.CopletAdapter#logout(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void logout(CopletInstanceData coplet) {
        super.logout(coplet);
        if ( this.portletContainer == null ) {
            return;
        }
        PortletWindow window = (PortletWindow)coplet.getTemporaryAttribute("window");
        if ( window != null ) {
            coplet.removeTemporaryAttribute("window");
            PortletDefinitionRegistry registry = (PortletDefinitionRegistry) portletContainerEnvironment.getContainerService(PortletDefinitionRegistry.class);

            PortletApplicationEntity pae = registry.getPortletApplicationEntityList().get(ObjectIDImpl.createFromString("cocoon"));
            ((PortletEntityListImpl)pae.getPortletEntityList()).remove(window.getPortletEntity());
        }
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
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
            this.servletConfig.getServletContext().removeAttribute(PortletAdapter.class.getName());
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

        // change portlet mode and window state
        final InformationProviderService ips = (InformationProviderService)this.portletContainerEnvironment.getContainerService(InformationProviderService.class);
        final DynamicInformationProvider dynProv = ips.getDynamicProvider(req);
        final PortletActionProvider pap = dynProv.getPortletActionProvider(event.getPortletWindow());

        final PortletMode mode = event.getPortletMode();
        if ( mode != null ) {
            pap.changePortletMode(mode);
        }
        final WindowState state = event.getWindowState();
        if ( state != null ) {
            pap.changePortletWindowState(state);
        }
        if ( event.isAction() ) {
            // This means we can only have ONE portlet event per request!
            objectModel.put("portlet-event", event);
        } else {
            ((PortletActionProviderImpl)pap).changeRenderParameters(event.getParameters());
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
        if (objectModel.remove("portlet-event") == null) {
            aspectContext.invokeNext(ch, parameters);
        }
    }

    protected String getResponse(CopletInstanceData instance, HttpServletResponse response) {
        return response.toString();
    }
}
