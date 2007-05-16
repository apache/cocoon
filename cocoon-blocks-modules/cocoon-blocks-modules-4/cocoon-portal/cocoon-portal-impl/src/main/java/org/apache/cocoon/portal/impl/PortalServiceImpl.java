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
package org.apache.cocoon.portal.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.PortalRuntimeException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.event.EventConverter;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.apache.cocoon.portal.om.SkinDescription;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.portal.services.CopletFactory;
import org.apache.cocoon.portal.services.LayoutFactory;
import org.apache.cocoon.portal.services.LinkService;
import org.apache.cocoon.portal.services.PortalManager;
import org.apache.cocoon.portal.services.UserService;
import org.apache.cocoon.processing.ProcessInfoProvider;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;
import org.springframework.web.context.ServletContextAware;

/**
 * Default implementation of a portal service using a session to store
 * custom information.
 *
 * @version $Id$
 */
public class PortalServiceImpl
    extends AbstractLogEnabled
    implements Serviceable,
                ThreadSafe,
                PortalService,
                ServletContextAware,
                Disposable,
                Configurable {

    /** Parameter map for the context protocol. */
    protected static final Map CONTEXT_PARAMETERS = Collections.singletonMap("force-traversable", Boolean.TRUE);

    /** The servlet context. */
    protected ServletContext servletContext;

    /** The service locator. */
    protected ServiceManager manager;

    /** The list of skins. */
    protected List skinList = new ArrayList();

    /** The name of the portal. */
    protected String portalName;

    /** The portal configuration. */
    protected Configuration configuration;

    /** The profile manager. */
    protected ProfileManager profileManager;

    /** The link service. */
    protected LinkService linkService;

    /** The used renderers. */
    protected Map renderers = Collections.EMPTY_MAP;

    /** The used coplet adapters. */
    protected Map copletAdapters = Collections.EMPTY_MAP;

    /** The coplet factory. */
    protected CopletFactory copletFactory;

    /** The layout factory. */
    protected LayoutFactory layoutFactory;

    /** The event manager. */
    protected EventManager eventManager;

    /** The portal manager. */
    protected PortalManager portalManager;

    /** The process info provider. */
    protected ProcessInfoProvider processInfoProvider;

    /** The user service. */
    protected UserService userService;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager serviceManager) throws ServiceException {
        this.manager = serviceManager;
        this.processInfoProvider = (ProcessInfoProvider)this.manager.lookup(ProcessInfoProvider.ROLE);
        this.renderers = (Map)this.manager.lookup(Renderer.class.getName()+"Map");
        this.copletAdapters = (Map)this.manager.lookup(CopletAdapter.class.getName()+"Map");
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getPortalName()
     */
    public String getPortalName() {
        return this.portalName;
    }

    /**
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext context) {
        this.servletContext = context;
        // add the portal service to the servlet context
        this.servletContext.setAttribute(PortalService.class.getName(), this);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        // remove the portal service from the servlet context - if available
        if ( this.servletContext != null ) {
            this.servletContext.removeAttribute(PortalService.class.getName());
        }
        if ( this.manager != null ) {
            this.manager.release(this.profileManager);
            this.profileManager = null;
            this.manager.release(this.linkService);
            this.linkService = null;
            this.manager.release(this.copletFactory);
            this.copletFactory = null;
            this.manager.release(this.layoutFactory);
            this.layoutFactory = null;
            this.manager.release(this.eventManager);
            this.eventManager = null;
            this.manager.release(this.portalManager);
            this.portalManager = null;
            this.manager.release(this.processInfoProvider);
            this.processInfoProvider = null;
            this.manager.release(this.userService);
            this.userService = null;
            this.manager = null;
        }
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        final Configuration portal = config.getChild("portal", false);
        if ( portal == null ) {
            throw new ConfigurationException("No portal configured.", config);
        }
        this.portalName = portal.getAttribute("name");
        this.configuration = portal.getChild("configuration");
        this.configureSkins(this.getConfiguration(org.apache.cocoon.portal.Constants.CONFIGURATION_SKINS_PATH,
                                                  org.apache.cocoon.portal.Constants.DEFAULT_CONFIGURATION_SKINS_PATH),
                            this.getConfiguration(org.apache.cocoon.portal.Constants.CONFIGURATION_SKINS_PATH, null) != null);
    }

    protected void configureSkins(String directory, boolean check)
    throws ConfigurationException {
        SourceResolver resolver = null;
        Source dir = null;
        try {
            resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
            dir = resolver.resolveURI(directory, null, CONTEXT_PARAMETERS);
            if ( !dir.exists() ) {
                if ( check ) {
                    throw new ConfigurationException("Skin directory does not exist: '" + directory + "'.");
                }
                this.getLogger().warn("No skin directory found at location '" + directory + "'.");
                return;
            }
            if ( dir instanceof TraversableSource ) {
                final Iterator children = ((TraversableSource)dir).getChildren().iterator();
                while ( children.hasNext() ) {
                    final Source s = (Source)children.next();
                    try {
                        this.configureSkin(s);
                    } finally {
                        resolver.release(s);
                    }
                }
            } else {
                throw new ConfigurationException("Skin configuration must point to a directory, '" + dir.getURI() + "' is not a directory.'");
            }
        } catch (IOException ioe) {
            throw new ConfigurationException("Unable to read configurations from " + directory);
        } catch (ServiceException e) {
            throw new ConfigurationException("Unable to get source resolver.");
        } finally {
            if ( resolver != null ) {
                resolver.release(dir);
                this.manager.release(resolver);
            }
        }
    }

    protected void configureSkin(Source directory) {
        String uri = directory.getURI();
        if ( uri.endsWith("/") ) {
            uri = uri.substring(0, uri.length()-1);
        }
        int pos = uri.lastIndexOf('/');
        final String skinName = uri.substring(pos+1);
        final SkinDescription desc = new SkinDescription();
        desc.setName(skinName);
        desc.setBasePath(directory.getURI());
        desc.setThumbnailPath(directory.getURI() + '/' + "images/thumb.jpg");
        this.skinList.add(desc);
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
        return this.configuration.getChild(key).getValue(defaultValue);
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
        return this.configuration.getChild(key).getValueAsBoolean(defaultValue);
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getLinkService()
     */
    public LinkService getLinkService() {
        if ( null == this.linkService ) {
            try {
                this.linkService = (LinkService)this.manager.lookup( LinkService.class.getName() );
            } catch (ServiceException e) {
                throw new PortalRuntimeException("Unable to lookup link service.", e);
            }
        }
        return this.linkService;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getProfileManager()
     */
    public ProfileManager getProfileManager() {
        if ( null == this.profileManager ) {
            try {
                this.profileManager = (ProfileManager)this.manager.lookup( ProfileManager.class.getName() );
            } catch (ServiceException e) {
                throw new PortalRuntimeException("Unable to lookup profile manager.", e);
            }
        }
        return this.profileManager;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getEventManager()
     */
    public EventManager getEventManager() {
        if ( null == this.eventManager ) {
            try {
                this.eventManager = (EventManager)this.manager.lookup( EventManager.class.getName() );
            } catch (ServiceException e) {
                throw new PortalRuntimeException("Unable to lookup event manager.", e);
            }
        }
        return this.eventManager;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getRenderer(java.lang.String)
     */
    public Renderer getRenderer(String name) {
        final Renderer o = (Renderer) this.renderers.get( name );
        if ( o == null ) {
            throw new PortalRuntimeException("Unable to lookup renderer with name " + name);
        }
        return o;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getCopletAdapter(java.lang.String)
     */
    public CopletAdapter getCopletAdapter(String name) {
        CopletAdapter o = (CopletAdapter) this.copletAdapters.get( name );
        if ( o == null ) {
            throw new PortalRuntimeException("Unable to lookup coplet adapter with name " + name);
        }
        return o;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getCopletFactory()
     */
    public CopletFactory getCopletFactory() {
        if ( null == this.copletFactory ) {
            try {
                this.copletFactory = (CopletFactory)this.manager.lookup( CopletFactory.class.getName() );
            } catch (ServiceException e) {
                throw new PortalRuntimeException("Unable to lookup coplet factory.", e);
            }
        }
        return this.copletFactory;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getLayoutFactory()
     */
    public LayoutFactory getLayoutFactory() {
        if ( null == this.layoutFactory ) {
            try {
                this.layoutFactory = (LayoutFactory)this.manager.lookup( LayoutFactory.class.getName() );
            } catch (ServiceException e) {
                throw new PortalRuntimeException("Unable to lookup layout factory.", e);
            }
        }
        return this.layoutFactory;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getPortalManager()
     */
    public PortalManager getPortalManager() {
        if ( null == this.portalManager ) {
            try {
                this.portalManager = (PortalManager)this.manager.lookup( PortalManager.class.getName() );
            } catch (ServiceException e) {
                throw new PortalRuntimeException("Unable to lookup portal manager.", e);
            }
        }
        return this.portalManager;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getProcessInfoProvider()
     */
    public ProcessInfoProvider getProcessInfoProvider() {
        return this.processInfoProvider;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getUserService()
     */
    public UserService getUserService() {
        if ( this.userService == null ) {
            try {
                this.userService = (UserService)this.manager.lookup(UserService.class.getName());
            } catch (ServiceException e) {
                throw new PortalRuntimeException("Unable to lookup user service.", e);
            }
        }
        return this.userService;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getEventConverter()
     */
    public EventConverter getEventConverter() {
        try {
            return (EventConverter)this.manager.lookup(EventConverter.class.getName());
        } catch (ServiceException e) {
            throw new PortalRuntimeException("Unable to lookup event converter.", e);
        }
    }
}
