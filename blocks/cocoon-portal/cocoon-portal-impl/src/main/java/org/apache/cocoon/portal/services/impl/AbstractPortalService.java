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
package org.apache.cocoon.portal.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.RequestContext;
import org.apache.cocoon.portal.event.EventConverter;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.services.CopletFactory;
import org.apache.cocoon.portal.services.LayoutFactory;
import org.apache.cocoon.portal.services.LinkService;
import org.apache.cocoon.portal.services.PortalManager;
import org.apache.cocoon.portal.services.ProfileManager;
import org.apache.cocoon.portal.services.UserService;
import org.apache.cocoon.portal.services.VariableResolver;
import org.apache.cocoon.portal.spi.RequestContextProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default implementation of a portal service using a session to store
 * custom information.
 *
 * @version $Id$
 */
public abstract class AbstractPortalService
    implements PortalService {

    /** The servlet context. */
    protected ServletContext servletContext;

    /** The list of skins. */
    protected List skinList = new ArrayList();

    /** The name of the portal. */
    protected String portalName;

    /** The profile manager. */
    protected ProfileManager profileManager;

    /** The link service. */
    protected LinkService linkService;

    /** The coplet factory. */
    protected CopletFactory copletFactory;

    /** The layout factory. */
    protected LayoutFactory layoutFactory;

    /** The event manager. */
    protected EventManager eventManager;

    /** The portal manager. */
    protected PortalManager portalManager;

    /** The user service. */
    protected UserService userService;

    /** The request context provider. */
    protected RequestContextProvider requestContextProvider;

    /** The event converter. */
    protected EventConverter eventConverter;

    /** Configuration. */
    protected Properties configuration;

    /** The variable resolver. */
    protected VariableResolver variableResolver;

    /** By default we use the logger for this class. */
    private Log logger = LogFactory.getLog(getClass());

    public Log getLogger() {
        return this.logger;
    }

    public void setLogger(Log l) {
        this.logger = l;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getPortalName()
     */
    public String getPortalName() {
        return this.portalName;
    }

    /**
     * Set the portal name.
     * @param name The name of the portal.
     */
    public void setPortalName(String name) {
        this.portalName = name;
    }

    public void setServletContext(final ServletContext sContext) {
        this.servletContext = sContext;
        // add the portal service to the servlet context
        this.servletContext.setAttribute(PortalService.class.getName(), this);
    }

    protected abstract Object getService(String name);

    public void dispose() {
        // remove the portal service from the servlet context - if available
        if ( this.servletContext != null ) {
            this.servletContext.removeAttribute(PortalService.class.getName());
        }
    }

    public void setSkinDescriptions(List skins) {
        if ( skins == null ) {
            this.skinList = Collections.EMPTY_LIST;
        } else {
            this.skinList = new ArrayList(skins);
        }
    }

    public void setConfiguration(Properties props) {
        this.configuration = props;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getSkinDescriptions()
     */
    public List getSkinDescriptions() {
        return this.skinList;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getConfiguration(java.lang.String, java.lang.String)
     */
    public String getConfiguration(String key, String defaultValue) {
        String result = null;
        if ( this.configuration != null ) {
            result = this.configuration.getProperty(key);
        }
        if ( result == null ) {
            result = defaultValue;
        }
        return defaultValue;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getConfiguration(java.lang.String)
     */
    public String getConfiguration(String key) {
        return this.getConfiguration(key, null);
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getConfigurationAsBoolean(java.lang.String, boolean)
     */
    public boolean getConfigurationAsBoolean(String key, boolean defaultValue) {
        final String value = this.getConfiguration(key);
        if ( value == null ) {
            return defaultValue;
        }
        return Boolean.valueOf(value).booleanValue();
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getLinkService()
     */
    public LinkService getLinkService() {
        if ( this.linkService == null ) {
            this.linkService = (LinkService)this.getService(LinkService.class.getName());
        }
        return this.linkService;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getProfileManager()
     */
    public ProfileManager getProfileManager() {
        if ( this.profileManager == null ) {
            this.profileManager = (ProfileManager)this.getService(ProfileManager.class.getName());
        }
        return this.profileManager;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getEventManager()
     */
    public EventManager getEventManager() {
        if ( this.eventManager == null ) {
            this.eventManager = (EventManager)this.getService(EventManager.class.getName());
        }
        return this.eventManager;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getCopletFactory()
     */
    public CopletFactory getCopletFactory() {
        if ( this.copletFactory == null ) {
            this.copletFactory = (CopletFactory)this.getService(CopletFactory.class.getName());
        }
        return this.copletFactory;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getLayoutFactory()
     */
    public LayoutFactory getLayoutFactory() {
        if ( this.layoutFactory == null ) {
            this.layoutFactory = (LayoutFactory)this.getService(LayoutFactory.class.getName());
        }
        return this.layoutFactory;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getPortalManager()
     */
    public PortalManager getPortalManager() {
        if ( this.portalManager == null ) {
            this.portalManager = (PortalManager)this.getService(PortalManager.class.getName());
        }
        return this.portalManager;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getUserService()
     */
    public UserService getUserService() {
        if ( this.userService == null ) {
            this.userService = (UserService)this.getService(UserService.class.getName());
        }
        return this.userService;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getEventConverter()
     */
    public EventConverter getEventConverter() {
        if ( this.eventConverter == null ) {
            this.eventConverter = (EventConverter)this.getService(EventConverter.class.getName());
        }
        return this.eventConverter;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getRequestContext()
     */
    public RequestContext getRequestContext() {
        if ( this.requestContextProvider == null ) {
            this.requestContextProvider = (RequestContextProvider)this.getService(RequestContextProvider.class.getName());
        }
        return this.requestContextProvider.getCurrentRequestContext();
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getVariableResolver()
     */
    public VariableResolver getVariableResolver() {
        if ( this.variableResolver == null ) {
            this.variableResolver = (VariableResolver)this.getService(VariableResolver.class.getName());
        }
        return this.variableResolver;
    }
}

