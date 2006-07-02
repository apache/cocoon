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
package org.apache.cocoon.portal.pluto.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.portal.PortalManagerAspect;
import org.apache.cocoon.portal.PortalManagerAspectPrepareContext;
import org.apache.cocoon.portal.PortalManagerAspectRenderContext;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.adapter.CopletDecorationProvider;
import org.apache.cocoon.portal.coplet.adapter.DecorationAction;
import org.apache.cocoon.portal.coplet.adapter.impl.AbstractCopletAdapter;
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
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletContainerImpl;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.portlet.PortletDefinition;
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
 * @version $Id$
 */
public class PortletAdapter 
    extends AbstractCopletAdapter
    implements PortalManagerAspect, CopletDecorationProvider, Receiver, Parameterizable {

    /** The Portlet Container. */
    protected PortletContainer portletContainer;

    /** The Portlet Container environment. */
    protected PortletContainerEnvironmentImpl portletContainerEnvironment;

    /** The configuration. */
    protected Parameters parameters;

    /**
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters params) throws ParameterException {
        this.parameters = params;
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
            PortletWindow portletWindow = new PortletWindowImpl(coplet, portletEntityId);                
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
        super.dispose();
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        super.initialize();
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
            ContainerUtil.parameterize(this.portletContainerEnvironment, this.parameters);
            ContainerUtil.service(this.portletContainerEnvironment, this.manager);
            ContainerUtil.initialize(this.portletContainerEnvironment);

            Properties properties = new Properties();

            try {
                // TODO - Currently it's safe to pass in null as the ServletConfig into Pluto, but we
                //        should try to provide an object
                portletContainer.init(uniqueContainerName, null, this.portletContainerEnvironment, properties);
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
        if ( !service.getProfileManager().getUser().isAnonymous() ) {
            req.setAttribute(PortletRequest.USER_INFO,
                             service.getProfileManager().getUser().getUserInfos());
        }
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
            if ( !service.getProfileManager().getUser().isAnonymous() ) {
                req.setAttribute(PortletRequest.USER_INFO,
                                 service.getProfileManager().getUser().getUserInfos());
            }
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

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.CopletDecorationProvider#getPossibleCopletModes(CopletInstanceData)
     */
    public List getPossibleCopletModes(CopletInstanceData copletInstanceData) {
        final List modes = new ArrayList();
        final PortletWindow window = (PortletWindow)copletInstanceData.getTemporaryAttribute("window");
        if ( window != null && this.portletContainerEnvironment != null) {
            InformationProviderService ips = (InformationProviderService) this.portletContainerEnvironment.getContainerService(InformationProviderService.class);
            DynamicInformationProvider dip = ips.getDynamicProvider((HttpServletRequest) ContextHelper.getObjectModel(this.context).get("portlet-request"));

            // portlet modes
            final String pmString = (String)copletInstanceData.getTemporaryAttribute("portlet-mode");
            final PortletMode pm; 
            if ( pmString == null ) {
                pm = PortletMode.VIEW;
            } else {
                pm = new PortletMode(pmString);
            }
            if ( !pm.equals(PortletMode.EDIT) ) {
                PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                url.clearParameters();
                url.setPortletMode(PortletMode.EDIT);
                modes.add(new DecorationAction("edit", url.toString()));
            }
            if ( !pm.equals(PortletMode.HELP) ) {
                PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                url.clearParameters();
                url.setPortletMode(PortletMode.HELP);
                modes.add(new DecorationAction("help", url.toString()));
            }                
            if ( !pm.equals(PortletMode.VIEW) ) {
                PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                url.clearParameters();
                url.setPortletMode(PortletMode.VIEW);
                modes.add(new DecorationAction("view", url.toString()));
            }                
        }

        return modes;
    }

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.CopletDecorationProvider#getPossibleWindowStates(CopletInstanceData)
     */
    public List getPossibleWindowStates(CopletInstanceData copletInstanceData) {
        final List states = new ArrayList();
        final PortletWindow window = (PortletWindow)copletInstanceData.getTemporaryAttribute("window");
        if ( window != null && this.portletContainerEnvironment != null) {
            InformationProviderService ips = (InformationProviderService) this.portletContainerEnvironment.getContainerService(InformationProviderService.class);
            DynamicInformationProvider dip = ips.getDynamicProvider((HttpServletRequest) ContextHelper.getObjectModel(this.context).get("portlet-request"));

            // Sizing
            final String wsString = (String)copletInstanceData.getTemporaryAttribute("window-state");
            final WindowState ws; 
            if ( wsString == null ) {
                ws = WindowState.NORMAL;
            } else {
                ws = new WindowState(wsString);
            }

            if ( !ws.equals(WindowState.MINIMIZED) && !ws.equals(WindowState.MAXIMIZED)) {
                PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                url.clearParameters();
                url.setWindowState(WindowState.MINIMIZED);
                states.add(new DecorationAction("minimize", url.toString()));
            }

            if ( !ws.equals(WindowState.NORMAL)) {
                PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                url.clearParameters();
                url.setWindowState(WindowState.NORMAL);
                states.add(new DecorationAction("normal", url.toString()));
            }

            if ( !ws.equals(WindowState.MAXIMIZED)) {
                PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                url.clearParameters();
                url.setWindowState(WindowState.MAXIMIZED);
                states.add(new DecorationAction("maximize", url.toString()));
            }
        }

        return states;
    }

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.CopletDecorationProvider#getTitle(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public String getTitle(CopletInstanceData copletInstanceData) {
        String title = null;
        final PortletWindow window = (PortletWindow)copletInstanceData.getTemporaryAttribute("window");
        if ( window != null ) {
            title = (String) copletInstanceData.getTemporaryAttribute("dynamic-title");
            if ( title == null ) {
                final PortletDefinition def = window.getPortletEntity().getPortletDefinition();
                try {
                    title = def.getDisplayName(def.getLanguageSet().getDefaultLocale()).getDisplayName();
                } catch (Exception ignore)  {
                    // we ignore this
                }
            }
        }
        if ( title == null ) {
            title = copletInstanceData.getTitle();
        }
        return title;
    }
}
