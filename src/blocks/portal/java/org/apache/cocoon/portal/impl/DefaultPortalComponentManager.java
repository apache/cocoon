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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.PortalComponentManager;
import org.apache.cocoon.portal.PortalManager;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.layout.LayoutFactory;
import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.apache.cocoon.portal.profile.ProfileManager;

/**
 * Default {@link PortalComponentManager} implementation
 *
 * @see org.apache.cocoon.portal.PortalComponentManager
 *
 * TODO Handle non ThreadSafe components
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 *
 * @version CVS $Id$
 */
public class DefaultPortalComponentManager
    extends AbstractLogEnabled
    implements PortalComponentManager, Serviceable, Disposable, ThreadSafe, Configurable {

    /** The avalon component manager */
    protected ServiceManager manager;

    /** The portal service */
    protected final PortalService portalService;

    protected String profileManagerRole;
    protected ProfileManager profileManager;

    protected String linkServiceRole;
    protected LinkService linkService;

    protected String rendererSelectorRole;
    protected ServiceSelector rendererSelector;

    protected Map renderers;

    protected String copletFactoryRole;
    protected CopletFactory copletFactory;

    protected String layoutFactoryRole;
    protected LayoutFactory layoutFactory;

    protected String eventManagerRole;
    protected EventManager eventManager;

    protected String portalManagerRole;
    protected PortalManager portalManager;

    protected final Context context;

    /**
     * Create a new portal component manager. Each portal has a own
     * component manager that manages all central components for this
     * portal.
     * This implementation stores the portal service (a global singleton)
     * to pass it to the other components (TODO).
     * @param service The portal service.
     */
    public DefaultPortalComponentManager(final PortalService service, Context context) {
        this.portalService = service;
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.PortalComponentManager#getLinkService()
     */
    public LinkService getLinkService() {
        if ( null == this.linkService ) {
            try {
                this.linkService = (LinkService)this.manager.lookup( this.linkServiceRole );
            } catch (ServiceException e) {
                throw new CascadingRuntimeException("Unable to lookup link service with role " + this.linkServiceRole, e);
            }
        }
        return this.linkService;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.PortalComponentManager#getProfileManager()
     */
    public ProfileManager getProfileManager() {
        if ( null == this.profileManager ) {
            try {
                this.profileManager = (ProfileManager)this.manager.lookup( this.profileManagerRole );
            } catch (ServiceException e) {
                throw new CascadingRuntimeException("Unable to lookup profile manager with role " + this.profileManagerRole, e);
            }
        }
        return this.profileManager;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.PortalComponentManager#getEventManager()
     */
    public EventManager getEventManager() {
        if ( null == this.eventManager ) {
            try {
                this.eventManager = (EventManager)this.manager.lookup( this.eventManagerRole );
            } catch (ServiceException e) {
                throw new CascadingRuntimeException("Unable to lookup event manager with role " + this.eventManagerRole, e);
            }
        }
        return this.eventManager;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            if (this.rendererSelector != null) {
                Iterator i = this.renderers.values().iterator();
                while (i.hasNext()) {
                    this.rendererSelector.release(i.next());
                }
                this.manager.release(this.rendererSelector);
                this.rendererSelector = null;
                this.renderers = null;
            }
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
            this.manager = null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        this.profileManagerRole = config.getChild("profile-manager").getValue(ProfileManager.ROLE);
        this.linkServiceRole = config.getChild("link-service").getValue(LinkService.ROLE);
        this.rendererSelectorRole = config.getChild("renderer-selector").getValue(Renderer.ROLE+"Selector");
        this.copletFactoryRole = config.getChild("coplet-factory").getValue(CopletFactory.ROLE);
        this.layoutFactoryRole = config.getChild("layout-factory").getValue(LayoutFactory.ROLE);
        this.eventManagerRole = config.getChild("event-manager").getValue(EventManager.ROLE);
        this.portalManagerRole = config.getChild("portal-manager").getValue(PortalManager.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.PortalComponentManager#getRenderer(java.lang.String)
     */
    public Renderer getRenderer(String hint) {
        if ( rendererSelector == null ) {
            try {
                this.rendererSelector = (ServiceSelector)this.manager.lookup( this.rendererSelectorRole );
            } catch (ServiceException e) {
                throw new CascadingRuntimeException("Unable to lookup renderer selector with role " + this.rendererSelectorRole, e);
            }
            this.renderers = new HashMap();
        }
        Renderer o = (Renderer) this.renderers.get( hint );
        if ( o == null ) {
            try {
                o = (Renderer) this.rendererSelector.select( hint );
                this.renderers.put( hint, o );
            } catch (ServiceException e) {
                throw new CascadingRuntimeException("Unable to lookup renderer with hint " + hint, e);
            }
        }
        return o;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.PortalComponentManager#getCopletFactory()
     */
    public CopletFactory getCopletFactory() {
        if ( null == this.copletFactory ) {
            try {
                this.copletFactory = (CopletFactory)this.manager.lookup( this.copletFactoryRole);
            } catch (ServiceException e) {
                throw new CascadingRuntimeException("Unable to lookup coplet factory with role " + this.copletFactoryRole, e);
            }
        }
        return this.copletFactory;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.PortalComponentManager#getLayoutFactory()
     */
    public LayoutFactory getLayoutFactory() {
        if ( null == this.layoutFactory ) {
            try {
                this.layoutFactory = (LayoutFactory)this.manager.lookup( this.layoutFactoryRole);
            } catch (ServiceException e) {
                throw new CascadingRuntimeException("Unable to lookup layout factory with role " + this.layoutFactoryRole, e);
            }
        }
        return this.layoutFactory;
    }


    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getPortalManager()
     */
    public PortalManager getPortalManager() {
        if ( null == this.portalManager ) {
            try {
                this.portalManager = (PortalManager)this.manager.lookup( this.portalManagerRole);
            } catch (ServiceException e) {
                throw new CascadingRuntimeException("Unable to lookup portal manager with role " + this.portalManagerRole, e);
            }
        }
        return this.portalManager;
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getComponentContext()
     */
    public Context getComponentContext() {
        return this.context;
    }
}
