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
package org.apache.cocoon.portal.pluto.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.serializers.util.EncodingSerializer;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.coplet.adapter.impl.AbstractCopletAdapter;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.coplet.CopletInstanceSizingEvent;
import org.apache.cocoon.portal.om.CopletDecorationProvider;
import org.apache.cocoon.portal.om.CopletDefinitionFeatures;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.DecorationAction;
import org.apache.cocoon.portal.pluto.PortletActionProviderImpl;
import org.apache.cocoon.portal.pluto.PortletContainerEnvironmentImpl;
import org.apache.cocoon.portal.pluto.PortletURLProviderImpl;
import org.apache.cocoon.portal.pluto.om.PortletDefinitionRegistry;
import org.apache.cocoon.portal.pluto.om.PortletEntityListImpl;
import org.apache.cocoon.portal.pluto.om.PortletWindowImpl;
import org.apache.cocoon.portal.pluto.om.common.ObjectIDImpl;
import org.apache.cocoon.portal.pluto.servlet.ServletRequestImpl;
import org.apache.cocoon.portal.pluto.servlet.ServletResponseImpl;
import org.apache.cocoon.portal.services.aspects.DynamicAspect;
import org.apache.cocoon.portal.services.aspects.RequestProcessorAspect;
import org.apache.cocoon.portal.services.aspects.RequestProcessorAspectContext;
import org.apache.cocoon.portal.services.aspects.ResponseProcessorAspect;
import org.apache.cocoon.portal.services.aspects.ResponseProcessorAspectContext;
import org.apache.cocoon.portal.util.HtmlSaxParser;
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
    implements RequestProcessorAspect, ResponseProcessorAspect, DynamicAspect, CopletDecorationProvider, Receiver {

    /** Name of the temporary coplet instance attribute holding the portlet window. */
    public static final String PORTLET_WINDOW_ATTRIBUTE_NAME = PortletAdapter.class.getName() + "/window";

    /** Name of the temporary coplet instance attribute holding the dynamic title (if any). */
    public static final String DYNAMIC_TITLE_ATTRIBUTE_NAME = PortletAdapter.class.getName() + "/dynamic-title";

    /** Name of the temporary coplet instance attribute holding the window state. */
    public static final String WINDOW_STATE_ATTRIBUTE_NAME = PortletAdapter.class.getName() + "/window-state";

    /** Name of the temporary coplet instance attribute holding the portlet mode. */
    public static final String PORTLET_MODE_ATTRIBUTE_NAME = PortletAdapter.class.getName() + "/portlet-mode";

    /** Name of the portlet mode for full screen (if supported). */
    public static final String FULL_SCREEN_WINDOW_STATE_ATTRIBUTE_NAME = "full-screen-mode";

    /** Name of attribute in the coplet definition storing the portlet identifier. */
    public static final String PORTLET_ATTRIBUTE_NAME = "portlet";

    /** The Portlet Container. */
    protected PortletContainer portletContainer;

    /** The Portlet Container environment. */
    protected PortletContainerEnvironmentImpl portletContainerEnvironment;

    /** Is full-screen enabled? */
    protected boolean enableFullScreen;

    /** Is maximized enabled? */
    protected boolean enableMaximized;

    protected Properties properties;

    protected ServiceManager manager;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setServiceManager(ServiceManager manager) {
        this.manager = manager;
    }

    /**
     * @see org.apache.cocoon.portal.om.CopletAdapter#login(org.apache.cocoon.portal.om.CopletInstance)
     */
    public void login(CopletInstance coplet) {
        super.login(coplet);

        PortletDefinitionRegistry registry = (PortletDefinitionRegistry) portletContainerEnvironment.getContainerService(PortletDefinitionRegistry.class);

        final String portletEntityId = (String) getConfiguration(coplet, PORTLET_ATTRIBUTE_NAME);
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
            coplet.setTemporaryAttribute(PORTLET_WINDOW_ATTRIBUTE_NAME, portletWindow);

            // load the portlet
            final HttpServletRequest servletRequest = this.portalService.getRequestContext().getRequest();
            ServletRequestImpl  req = (ServletRequestImpl) servletRequest.getAttribute("portlet-request-" + coplet.getId());
            if ( req == null ) {
                final HttpServletResponse res = this.portalService.getRequestContext().getResponse();
                servletRequest.setAttribute("portlet-response-" + coplet.getId(),  new ServletResponseImpl(res));
                req = new ServletRequestImpl(servletRequest, null);
                servletRequest.setAttribute("portlet-request-" + coplet.getId(),  req);
            }
            final HttpServletResponse res = (HttpServletResponse) servletRequest.getAttribute("portlet-response-" + coplet.getId());
            try {
                this.portletContainer.portletLoad(portletWindow, req.getRequest(portletWindow),
                                                  res);
            } catch (Exception e) {
                this.getLogger().error("Error loading portlet " + portletEntityId + " for instance " + coplet.getId(), e);
                // remove portlet entity
                coplet.removeTemporaryAttribute(PORTLET_WINDOW_ATTRIBUTE_NAME);
                ((PortletEntityListImpl)pae.getPortletEntityList()).remove(portletEntity);
            }
        } else {
            this.getLogger().error("Error finding portlet " + portletEntityId + " for instance " + coplet.getId() + " - no definition found.");
        }
    }

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.impl.AbstractCopletAdapter#streamContent(org.apache.cocoon.portal.om.CopletInstance, org.xml.sax.ContentHandler)
     */
    protected void streamContent(CopletInstance coplet,
                                 ContentHandler contentHandler)
    throws SAXException {
        try {
            final String portletEntityId = (String) getConfiguration(coplet, "portlet");
            // get the window
            final PortletWindow window = (PortletWindow)coplet.getTemporaryAttribute(PORTLET_WINDOW_ATTRIBUTE_NAME);
            if ( window == null ) {
                throw new SAXException("Portlet couldn't be loaded: " + coplet.getId() + "(" + portletEntityId + ")");
            }
            final HttpServletRequest servletRequest = this.portalService.getRequestContext().getRequest();

            ServletRequestImpl  req = (ServletRequestImpl) servletRequest.getAttribute("portlet-request-" + coplet.getId());
            HttpServletResponse res = (HttpServletResponse) servletRequest.getAttribute("portlet-response-" + coplet.getId());

            if ( res == null ) {
                res = new ServletResponseImpl(this.portalService.getRequestContext().getResponse());
                servletRequest.setAttribute("portlet-response-" + coplet.getId(), res);
            }
            if ( req == null ) {
                req = new ServletRequestImpl(this.portalService.getRequestContext().getRequest(), null);
                servletRequest.setAttribute("portlet-request-" + coplet.getId(),  req);
            }
            if ( !this.portalService.getUserService().getUser().isAnonymous() ) {
                req.setAttribute(PortletRequest.USER_INFO,
                        this.portalService.getUserService().getUser().getUserInfos());
            }

            // TODO - for parallel processing we have to clone the response!
            this.portletContainer.renderPortlet(window, req.getRequest(window), res);
            final String content = this.getResponse(coplet, res);

            final Boolean usePipeline = (Boolean)this.getConfiguration(coplet, "use-pipeline", Boolean.FALSE);
            if ( usePipeline.booleanValue() ) {
                HtmlSaxParser.parseString(content, HtmlSaxParser.getContentFilter(contentHandler));
            } else {
                // stream out the include for the serializer
                EncodingSerializer.include(content, this.portalService.getRequestContext().getRequest(), contentHandler);
            }
        } catch (SAXException se) {
            throw se;
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    /**
     * @see org.apache.cocoon.portal.om.CopletAdapter#logout(org.apache.cocoon.portal.om.CopletInstance)
     */
    public void logout(CopletInstance coplet) {
        super.logout(coplet);
        PortletWindow window = (PortletWindow)coplet.getTemporaryAttribute(PORTLET_WINDOW_ATTRIBUTE_NAME);
        if ( window != null ) {
            coplet.removeTemporaryAttribute(PORTLET_WINDOW_ATTRIBUTE_NAME);
            PortletDefinitionRegistry registry = (PortletDefinitionRegistry) portletContainerEnvironment.getContainerService(PortletDefinitionRegistry.class);

            PortletApplicationEntity pae = registry.getPortletApplicationEntityList().get(ObjectIDImpl.createFromString("cocoon"));
            ((PortletEntityListImpl)pae.getPortletEntityList()).remove(window.getPortletEntity());
        }
    }

    /**
     * Destroy this component.
     * Shutdown Pluto.
     */
    public void destroy() {
        try {
            ContainerUtil.dispose(this.portletContainerEnvironment);
            this.portletContainerEnvironment = null;
            if (this.portletContainer != null ) {
                this.portletContainer.shutdown();
                this.portletContainer = null;
            }
        } catch (Throwable t) {
            this.getLogger().error("Destruction failed!", t);
        }
    }

    /**
     * Initialize this component.
     * Setup Pluto.
     */
    public void init() throws Exception {
        this.enableFullScreen = this.portalService.getConfigurationAsBoolean(Constants.CONFIGURATION_FULL_SCREEN_ENABLED, Constants.DEFAULT_CONFIGURATION_FULL_SCREEN_ENABLED);
        this.enableMaximized = this.portalService.getConfigurationAsBoolean(Constants.CONFIGURATION_MAXIMIZED_ENABLED, Constants.DEFAULT_CONFIGURATION_MAXIMIZED_ENABLED);
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
            this.getLogger().info("Initializing PortletContainer...");

            final String uniqueContainerName = "cocoon-portal";

            this.portletContainerEnvironment = new PortletContainerEnvironmentImpl();
            this.portletContainerEnvironment.setLogger(this.getLogger());
            ContainerUtil.parameterize(this.portletContainerEnvironment, Parameters.fromProperties(this.properties));
            ContainerUtil.service(this.portletContainerEnvironment, this.manager);
            ContainerUtil.initialize(this.portletContainerEnvironment);

            Properties properties = new Properties();

            try {
                // TODO - Currently it's safe to pass in null as the ServletConfig into Pluto, but we
                //        should try to provide an object
                portletContainer.init(uniqueContainerName, null, this.portletContainerEnvironment, properties);
            } catch (PortletContainerException exc) {
                throw new PortalException("Initialization of the portlet container failed.", exc);
            }
        } else {
            this.getLogger().debug("PortletContainer already initialized.");
        }

        this.getLogger().info("PortletContainer initialized.");
    }

    /**
     * This method is invoked each time an event for a portlet is received (user clicking/activating
     * something in the portlet).
     * @see Receiver
     */
    public void inform(PortletURLProviderImpl event) {
        final HttpServletRequest servletRequest = this.portalService.getRequestContext().getRequest();
        final ServletRequestImpl req = new ServletRequestImpl(servletRequest, event);
        final HttpServletResponse res = new ServletResponseImpl(this.portalService.getRequestContext().getResponse());
        if ( !this.portalService.getUserService().getUser().isAnonymous() ) {
            req.setAttribute(PortletRequest.USER_INFO,
                    this.portalService.getUserService().getUser().getUserInfos());
        }
        servletRequest.setAttribute("portlet-response-" + event.getTarget().getId(),  res);
        servletRequest.setAttribute("portlet-request-" + event.getTarget().getId(), req);
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
            servletRequest.setAttribute("portlet-event", event);
            servletRequest.setAttribute("portlet-event-target", event.getTarget().getId());
        } else {
            ((PortletActionProviderImpl)pap).changeRenderParameters(event.getParameters());
        }
    }

    /**
     * This method is invoked each time a coplet instance is resized.
     * @see Receiver
     */
    public void inform(CopletInstanceSizingEvent event) {
        WindowState ws = WindowState.NORMAL;
        if ( event.getSize() == CopletInstance.SIZE_NORMAL ) {
            ws = WindowState.NORMAL;
        } else if ( event.getSize() == CopletInstance.SIZE_MAXIMIZED ) {
            ws = WindowState.MAXIMIZED;
        } else if ( event.getSize() == CopletInstance.SIZE_MINIMIZED ) {
            ws = WindowState.MINIMIZED;
        } else if ( event.getSize() == CopletInstance.SIZE_FULLSCREEN ) {
            ws = new WindowState((String)CopletDefinitionFeatures.getAttributeValue(event.getTarget().getCopletDefinition(), FULL_SCREEN_WINDOW_STATE_ATTRIBUTE_NAME, null));
        }
        final String wsString = (String)event.getTarget().getTemporaryAttribute(WINDOW_STATE_ATTRIBUTE_NAME);
        if ( !wsString.equals(ws.toString()) ) {
            event.getTarget().setTemporaryAttribute(WINDOW_STATE_ATTRIBUTE_NAME, ws.toString());
        }
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.RequestProcessorAspect#process(org.apache.cocoon.portal.services.aspects.RequestProcessorAspectContext)
     */
    public void process(RequestProcessorAspectContext aspectContext) {
        // process the events
        aspectContext.invokeNext();

        // do we already have an environment?
        // if not, create one
        final HttpServletRequest servletRequest = this.portalService.getRequestContext().getRequest();

        PortletURLProviderImpl event = (PortletURLProviderImpl) servletRequest.getAttribute("portlet-event");
        if ( event != null ) {
            final String targetId = (String) servletRequest.getAttribute("portlet-event-target");
            PortletWindow actionWindow = event.getPortletWindow();
            try {
                final ServletRequestImpl req = (ServletRequestImpl) servletRequest.getAttribute("portlet-request-" + targetId);
                final ServletResponseImpl res= (ServletResponseImpl)servletRequest.getAttribute("portlet-response-" + targetId);
                this.portletContainer.processPortletAction(actionWindow, req.getRequest(actionWindow), res);
            } catch (Exception ignore) {
                this.getLogger().error("Error during processing of portlet action.", ignore);
            }
        }
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.ResponseProcessorAspect#render(org.apache.cocoon.portal.services.aspects.ResponseProcessorAspectContext, org.xml.sax.ContentHandler, java.util.Properties)
     */
    public void render(ResponseProcessorAspectContext aspectContext,
                       ContentHandler ch,
                       Properties properties)
    throws SAXException {
        final HttpServletRequest servletRequest = aspectContext.getPortalService().getRequestContext().getRequest();

        // don't generate a response, if we issued a redirect
        if (servletRequest.getAttribute("portlet-event") == null) {
            aspectContext.invokeNext(ch, properties);
        }
        servletRequest.removeAttribute("portlet-event");
        servletRequest.removeAttribute("portlet-event-target");
    }

    protected String getResponse(CopletInstance instance, HttpServletResponse response) {
        return response.toString();
    }

    /**
     * @see org.apache.cocoon.portal.om.CopletDecorationProvider#getPossibleCopletModes(CopletInstance)
     */
    public List getPossibleCopletModes(CopletInstance copletInstanceData) {
        final List modes = new ArrayList();
        final PortletWindow window = (PortletWindow)copletInstanceData.getTemporaryAttribute(PORTLET_WINDOW_ATTRIBUTE_NAME);
        if ( window != null ) {
            InformationProviderService ips = (InformationProviderService) this.portletContainerEnvironment.getContainerService(InformationProviderService.class);
            DynamicInformationProvider dip = ips.getDynamicProvider(this.portalService.getRequestContext().getRequest());

            // portlet modes
            final String pmString = (String)copletInstanceData.getTemporaryAttribute(PORTLET_MODE_ATTRIBUTE_NAME);
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
                modes.add(new DecorationAction("edit-uri", url.toString()));
            }
            if ( !pm.equals(PortletMode.HELP) ) {
                PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                url.clearParameters();
                url.setPortletMode(PortletMode.HELP);
                modes.add(new DecorationAction("help-uri", url.toString()));
            }
            if ( !pm.equals(PortletMode.VIEW) ) {
                PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                url.clearParameters();
                url.setPortletMode(PortletMode.VIEW);
                modes.add(new DecorationAction("view-uri", url.toString()));
            }
        }

        return modes;
    }

    /**
     * @see org.apache.cocoon.portal.om.CopletDecorationProvider#getPossibleWindowStates(CopletInstance)
     */
    public List getPossibleWindowStates(CopletInstance copletInstanceData) {
        final List states = new ArrayList();
        final PortletWindow window = (PortletWindow)copletInstanceData.getTemporaryAttribute(PORTLET_WINDOW_ATTRIBUTE_NAME);
        if ( window != null ) {
            InformationProviderService ips = (InformationProviderService) this.portletContainerEnvironment.getContainerService(InformationProviderService.class);
            DynamicInformationProvider dip = ips.getDynamicProvider(this.portalService.getRequestContext().getRequest());

            // Sizing
            final String wsString = (String)copletInstanceData.getTemporaryAttribute(WINDOW_STATE_ATTRIBUTE_NAME);
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
                states.add(new DecorationAction(DecorationAction.WINDOW_STATE_MINIMIZED, url.toString()));
            }

            if ( !ws.equals(WindowState.NORMAL)) {
                PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                url.clearParameters();
                url.setWindowState(WindowState.NORMAL);
                states.add(new DecorationAction(DecorationAction.WINDOW_STATE_NORMAL, url.toString()));
            }

            if ( this.enableMaximized ) {
                if ( !ws.equals(WindowState.MAXIMIZED)) {
                    PortletURLProviderImpl url = (PortletURLProviderImpl)dip.getPortletURLProvider(window);
                    url.clearParameters();
                    url.setWindowState(WindowState.MAXIMIZED);
                    states.add(new DecorationAction(DecorationAction.WINDOW_STATE_MAXIMIZED, url.toString()));
                }
            }
            if ( this.enableFullScreen ) {
                // TODO - Implement full screen for portlets (= own mode)
            }
        }

        return states;
    }

    /**
     * @see org.apache.cocoon.portal.om.CopletDecorationProvider#getTitle(org.apache.cocoon.portal.om.CopletInstance)
     */
    public String getTitle(CopletInstance copletInstanceData) {
        String title = null;
        final PortletWindow window = (PortletWindow)copletInstanceData.getTemporaryAttribute(PORTLET_WINDOW_ATTRIBUTE_NAME);
        if ( window != null ) {
            title = (String) copletInstanceData.getTemporaryAttribute(DYNAMIC_TITLE_ATTRIBUTE_NAME);
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
